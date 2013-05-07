package com.aanchal.chitrahaar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import android.sax.Element;
import android.util.Log;

import android.util.Log;

// Retrieves and manages songs
public class SongFactory {	
	private static final int MAX_QUERY_SONGS = 10; // maximum query results per songs
	private static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?&video_id=";
	private static final String DISHANT_URL = "http://anyorigin.com/get/?url=dishant.com/radiojukebox.php?channel=new&action=0";
	private static final String DISHANT_METADATA_URL = "http://anyorigin.com/get/?url=dishant.com/trackPlaylist.php?trackid=";

	private static final String DUBAI_METADATA_URL = "http://www.arn.ae/city1016.ae/fb_playing_now/ServerScripts/GetSongInfo20.php";
	
	private Queue<Song> dishantSongs_ = new LinkedList<Song>(); // queue containing yet to be played dishant songs
	private Queue<Song> dubaiSongs_ = new LinkedList<Song>();  // yet to be played dubai songs

	private Song lastPlayedDubaiSong_ = new Song("","",""); 
	private Song lastPlayedDishantSong_ = new Song("","","");
	
	private static Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
	
	// query dubai, parse and return the last 10 songs, returns null if query fails
	private Song[] getLastDubaiSongs() {
		try{
		String xml = getXmlFromUrl(DUBAI_METADATA_URL); // getting XML
		LinkedList<Song> temp=new LinkedList<Song>();
		xml=xml.substring(41);
		Document doc = loadXMLFromString(xml); // getting DOM element
		
		NodeList songs = doc.getElementsByTagName("property");
		
		String title="", album="";
		for(int i = 0; i < songs.getLength(); ++i) {
			Element ele = (Element) songs.item(i);
			String name = ele.getAttribute("name");
			if(name.equals("cue_title")){
				title=getCharacterDataFromElement(ele);
			}
			else if(name.equals("track_artist_name")){
				album=getCharacterDataFromElement(ele);
				Song s= new Song(title, album, album);
				if(title.equals(lastPlayedDubaiSong_.title)&&album.equals(lastPlayedDubaiSong_.album)){				
					break;
				}
				else
					temp.add(s);
			}	
		}
		int i=temp.size()-1;
		Song[] ret = new Song[temp.size()];
		while(i>=0){
			ret[i]=temp.removeFirst();
			i--;
		}
		return ret;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	      CharacterData cd = (CharacterData) child;
	      return cd.getData();
	    }
	    return "";
	  }
	
	public String getXmlFromUrl(String url) {
        String xml = null;
 
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
 
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // return XML
        return xml;
    }
	
	// query dishant and return last
	private static Song[] getLastDishantSongs() {
		try {
			URL jsonURL = new URL(DISHANT_URL);
			URLConnection jc = jsonURL.openConnection();
			InputStream is = jc.getInputStream();
			String jsonTxt = IOUtils.toString( is );
			JSONObject jj = new JSONObject(jsonTxt);
			String contents = jj.getString("contents");
			int idx1 = contents.indexOf("HTMLNAME=") + 9;
			int idx2 = contents.indexOf("&SERVERIP=");
			String trackids = contents.substring(idx1, idx2);
			jsonURL = new URL(DISHANT_METADATA_URL + trackids);
			jc = jsonURL.openConnection();
			is = jc.getInputStream();
			jsonTxt = IOUtils.toString( is );
			jj = new JSONObject(jsonTxt);
			contents = jj.getString("contents");
			Document songData = loadXMLFromString(contents);
			NodeList songs = songData.getElementsByTagName("song");
			Song[] ret = new Song[songs.getLength()];
			for(int i = 0; i < songs.getLength(); ++i) {
				Element ele = (Element) songs.item(i);
				String title = ele.getAttribute("title");
				String album = ele.getAttribute("album");
				String artist = ele.getAttribute("musicdir");
				ret[i] = new Song(title, album, artist);
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Boolean updateDubaiSongs() {
		//assert dubaiSongs_.isEmpty();
		// get last 10 songs
		// if most recent song is same as lastPlayedDubaiSong
		// return false
		// else update the queue and return true
		assert dubaiSongs_.isEmpty();
		Song[] newSongs = getLastDubaiSongs();
		if (newSongs != null && newSongs.length != 0) {
			for(int i = 0; i < newSongs.length; ++i) 
				dubaiSongs_.add(newSongs[i]);
			return true;
		}
		return false;
		
	}
	
	private Boolean updateDishantSongs() {
		assert dishantSongs_.isEmpty();
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
		results[2] = getResults(song.getArtistAlbumQueryString());
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