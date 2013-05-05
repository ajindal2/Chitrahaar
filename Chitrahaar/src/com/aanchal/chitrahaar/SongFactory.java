package com.aanchal.chitrahaar;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

// Retrieves and manages songs
public class SongFactory {	
	private static final int MAX_QUERY_SONGS = 10; // maximum query results per songs
	private static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?&video_id=";
	private static final String DISHANT_URL = "http://anyorigin.com/get/?url=dishant.com/radiojukebox.php?channel=new&action=0";
	private static final String DISHANT_METADATA_URL = "http://anyorigin.com/get/?url=dishant.com/trackPlaylist.php?trackid=";
	private Queue<Song> dishantSongs_; // queue containing yet to be played dishant songs
	private Queue<Song> dubaiSongs_;  // yet to be played dubai songs
	private Song lastPlayedDubaiSong_ = null; 
	private Song lastPlayedDishantSong_ = null;
	
	private static Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
	
	// query dubai, parse and return the last 10 songs, returns null if query fails
	private static Song[] getLastDubaiSongs() {
		
		return null;
	}
	
	// query dishant and return last
	private static Song[] getLastDishantSongs() {
		try {
			URL jsonURL = new URL(DISHANT_URL);
			URLConnection jc = jsonURL.openConnection();
			InputStream is = jc.getInputStream();
			String jsonTxt = IOUtils.toString( is );
			JSONObject jj = new JSONObject(jsonTxt);
			JSONObject htmlData = jj.getJSONObject("data");
			String contents = htmlData.getString("contents");
			int idx1 = contents.indexOf("HTMLNAME=") + 9;
			int idx2 = contents.indexOf("&SERVERIP=");
			String trackids = contents.substring(idx1, idx2);
			jsonURL = new URL(DISHANT_METADATA_URL + trackids);
			jc = jsonURL.openConnection();
			is = jc.getInputStream();
			jsonTxt = IOUtils.toString( is );
			jj = new JSONObject(jsonTxt);
			htmlData = jj.getJSONObject("data");
			contents = htmlData.getString("contents");
			Document songData = loadXMLFromString(contents);
			// parse all the songs
			// TODO
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	private Boolean updateDubaiSongs() {
		assert dubaiSongs_.isEmpty();
		// get last 10 songs
		// if most recent song is same as lastPlayedDubaiSong
		// return false
		// else update the queue and return true
		return false;
	}
	
	private Boolean updateDishantSongs() {
		assert dishantSongs_.isEmpty();
		// similar logic as above except there is no need to check for updation
		// return false if the query fails
		Song[] newSongs = getLastDishantSongs();
		if (newSongs != null && newSongs.length != 0) {
			for(int i = 0; i < newSongs.length; ++i) 
				dishantSongs_.add(newSongs[i]);
			return true;
		}
		return false;
	}
	
	// returns query results in JSON form
	private static JSONArray getResults(String query) {
		try {
		// TODO: Get short and medium duration only, exclude long videos
		String url="http://gdata.youtube.com/feeds/api/videos?q="+query+"&max-results="+MAX_QUERY_SONGS+"&v=2&format=5&alt=jsonc";
		URL jsonURL = new URL(url);
		URLConnection jc = jsonURL.openConnection();
		InputStream is = jc.getInputStream();
		String jsonTxt = IOUtils.toString( is );
		JSONObject jj = new JSONObject(jsonTxt);
		JSONObject jdata = jj.getJSONObject("data");
		int totalItems = Math.min(MAX_QUERY_SONGS,jdata.getInt("totalItems"));
		JSONArray aitems = null;
		if (totalItems > 0)
			aitems = jdata.getJSONArray("items");
		return aitems;
		} catch (Exception e) {
			return null;
		}
	}
	
	// given a song, retrieve the youtube id, returns null if no playable video is found
	private static String getVideoIdForSong(Song song) {
		JSONArray[] results = new JSONArray[3];
		results[0] = getResults(song.getAlbumQueryString());
		results[1] = getResults(song.getArtistQueryString());
		results[2] = getResults(song.getTitleQueryString());
		int[] indexes = new int[3];
		indexes[0] = indexes[1] = indexes[2] = 0;
		while(true) {
			Boolean exhausted = true;
			for(int i = 0; i < 3; ++i) 
				if (results[i] != null && indexes[i] < results[i].length()) {
					try {
					  JSONObject item0 = results[i].getJSONObject(indexes[i]);
					  indexes[i]++;
					  String ret = item0.getString("id");
					  HttpClient lClient = new DefaultHttpClient();
					  HttpGet lGetMethod = new HttpGet(YOUTUBE_VIDEO_INFORMATION_URL + ret);
					  HttpResponse lResp = null;
					  lResp = lClient.execute(lGetMethod);
					  ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
					  lResp.getEntity().writeTo(lBOS);
					  String lInfoStr = new String(lBOS.toString("UTF-8"));
					  if (!lInfoStr.contains("fail"))
						  return ret;
					  exhausted = false;
					} catch (Exception e) {
						// do nothing, continue
					}
					  
				}
			if (exhausted)
				break;
		}
		return null;
	}
	
	// returns the next video id for the played
	// returns null if unable to find one
	public String getNextVideoId() {
		// first try to play a dubai song
		if (dubaiSongs_.isEmpty())
			updateDubaiSongs();
		while (!dubaiSongs_.isEmpty()) {
			Song song = dubaiSongs_.peek();
			dubaiSongs_.remove();
			String ret = getVideoIdForSong(song);
			if (ret != null) {
				lastPlayedDubaiSong_ = song;
				return ret;
			}
		}
		// could not play a dubai song, play a dishant song
		if (dishantSongs_.isEmpty())
			updateDishantSongs();
		while (!dishantSongs_.isEmpty()) {
			Song song = dishantSongs_.peek();
			dishantSongs_.remove();
			String ret = getVideoIdForSong(song);
			if (ret != null) {
				lastPlayedDishantSong_ = song;
				return ret;
			}
		}
		return null;
	}
	
}