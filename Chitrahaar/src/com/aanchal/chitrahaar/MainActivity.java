package com.aanchal.chitrahaar;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import com.aanchal.chitrahaar.YouTubeFailureRecoveryActivity;
import com.aanchal.chitrahaar.MainActivity.ActionBarPaddedFrameLayout;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.aanchal.chitrahaar.R;
import com.aanchal.chitrahaar.DeveloperKey;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

@TargetApi(11)
public class MainActivity extends YouTubeFailureRecoveryActivity implements
    YouTubePlayer.OnFullscreenListener {
	
	
	private final class MyPlayerStateChangeListener implements PlayerStateChangeListener {
	
	    @Override
	    public void onLoaded(String videoId) {
	    	m_player.play();
	    }

	    @Override
	    public void onVideoEnded() {
	    	new Process().execute(null,null,null); 
	    }

		@Override
		public void onAdStarted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(ErrorReason arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLoading() {
			// TODO Auto-generated method stub
		}

		@Override
		public void onVideoStarted() {
			// TODO Auto-generated method stub
			
		}
	  }

	class Process extends AsyncTask<Object, Void, String> {
		
		 private ProgressDialog progressDialog; 
		 @Override
	        protected void onPreExecute()
	        {
	            super.onPreExecute();
	            progressDialog = ProgressDialog.show(MainActivity.this, null, "Loading...", true, false); 
	        }

	        @Override
	        protected String doInBackground(Object... param) {
	        	String videoId=songFactory.getNextVideoId();
	        	 if (videoId != null) {
	           	  m_player.cueVideo(videoId);
	             }
	             else
	           	  Toast.makeText(getApplicationContext(), "Unable to get a new song. Please try again later", Toast.LENGTH_SHORT).show();
	        	 progressDialog.dismiss();
	        	 return null;
	        }

	        @Override
	        protected void onPostExecute(String result)
	        {
	            super.onPostExecute(result);	            
	            progressDialog.dismiss();
	        }
	}
		
  private ActionBarPaddedFrameLayout viewContainer;
  private YouTubePlayerFragment playerFragment;
  private YouTubePlayer m_player;
  private SongFactory songFactory;
  private MyPlayerStateChangeListener playerStateChangeListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
    playerStateChangeListener = new MyPlayerStateChangeListener();
    
    // TODO: This allows us to make networking calls on UI thread but discouraged
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    
    songFactory = new SongFactory();

    setContentView(R.layout.activity_main);

    viewContainer = (ActionBarPaddedFrameLayout) findViewById(R.id.view_container);
    playerFragment =
        (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);
    playerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
    viewContainer.setActionBar(getActionBar());

    // Action bar background is transparent by default.
    getActionBar().setBackgroundDrawable(new ColorDrawable(0xAA000000));
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main, menu);
      return true;
  }
    
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

    case R.id.next_song:   	 
      new Process().execute(null,null,null); 
      break;
      default:
        break;
      }
      return true;
 }
  
  @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
	  
	m_player = player;
    player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
    player.setFullscreen(true);
    player.setOnFullscreenListener(this);
    player.setPlayerStateChangeListener(playerStateChangeListener);

    if (!wasRestored) {	
    	new Process().execute(null,null,null); 
    }
    player.setShowFullscreenButton(false);
  }

  @Override
  protected YouTubePlayer.Provider getYouTubePlayerProvider() {
    return (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);
  }

  @Override
  public void onFullscreen(boolean fullscreen) {
    viewContainer.setEnablePadding(!fullscreen);
    ViewGroup.LayoutParams playerParams = playerFragment.getView().getLayoutParams();
    if (fullscreen) {
      playerParams.width = MATCH_PARENT;
      playerParams.height = MATCH_PARENT;
    } else {
      m_player.setFullscreen(true);
    }
  }

  /**
   * This is a FrameLayout which adds top-padding equal to the height of the ActionBar unless
   * disabled by {@link #setEnablePadding(boolean)}.
   */
  public static final class ActionBarPaddedFrameLayout extends FrameLayout {

    private ActionBar actionBar;
    private boolean paddingEnabled;

    public ActionBarPaddedFrameLayout(Context context) {
      this(context, null);
    }

    public ActionBarPaddedFrameLayout(Context context, AttributeSet attrs) {
      this(context, attrs, 0);
    }

    public ActionBarPaddedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      paddingEnabled = true;
    }

    public void setActionBar(ActionBar actionBar) {
      this.actionBar = actionBar;
      requestLayout();
    }

    public void setEnablePadding(boolean enable) {
      paddingEnabled = enable;
      requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int topPadding =
          paddingEnabled && actionBar != null && actionBar.isShowing() ? actionBar.getHeight() : 0;
      setPadding(0, topPadding, 0, 0);

      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

  }

}