package com.aanchal.chitrahaar;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import com.aanchal.chitrahaar.YouTubeFailureRecoveryActivity;
import com.aanchal.chitrahaar.MainActivity.ActionBarPaddedFrameLayout;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerView;
import com.aanchal.chitrahaar.R;
import com.aanchal.chitrahaar.DeveloperKey;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/*public class MainActivity extends YouTubeFailureRecoveryActivity {
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
	    youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this); 
	  }

	  @Override
	  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
		  //this.player=player;
	    if (!wasRestored) {
	      player.cueVideo("wKJ9KzGQq0w");
	      player.setFullscreen(true);
	    }
	  }

	  @Override
	  protected YouTubePlayer.Provider getYouTubePlayerProvider() {
	    return (YouTubePlayerView) findViewById(R.id.youtube_view);
	  }

	}*/




@TargetApi(11)
public class MainActivity extends YouTubeFailureRecoveryActivity implements
    YouTubePlayer.OnFullscreenListener {

  private ActionBarPaddedFrameLayout viewContainer;
  private YouTubePlayerFragment playerFragment;
  private View tutorialTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    viewContainer = (ActionBarPaddedFrameLayout) findViewById(R.id.view_container);
    playerFragment =
        (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);
    tutorialTextView = findViewById(R.id.tutorial_text);
    playerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
    viewContainer.setActionBar(getActionBar());

    // Action bar background is transparent by default.
    getActionBar().setBackgroundDrawable(new ColorDrawable(0xAA000000));
   // getActionBar().
  }

  @Override
  public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
      boolean wasRestored) {
    player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
    player.setOnFullscreenListener(this);

    if (!wasRestored) {
      player.cueVideo("9c6W4CCU9M4");
      player.setFullscreen(true);
    }
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
      tutorialTextView.setVisibility(View.GONE);
      playerParams.width = MATCH_PARENT;
      playerParams.height = MATCH_PARENT;
    } else {
      tutorialTextView.setVisibility(View.VISIBLE);
      playerParams.width = 0;
      playerParams.height = WRAP_CONTENT;
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






/*public class MainActivity extends   Activity {
	private static final int REQ_START_STANDALONE_PLAYER = 1;
	  private static final int REQ_RESOLVE_SERVICE_MISSING = 2;
	  YouTubePlayerView  playerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//playerView = (YouTubePlayerView) findViewById(R.id.player);
		//playerView.addView(a)
		 //playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);
		        String videoId = "__BYsQo4QaY";   
		        Intent intent = YouTubeStandalonePlayer.createVideoIntent(MainActivity.this, DeveloperKey.DEVELOPER_KEY, videoId);
		        if (intent != null) {
		            if (canResolveIntent(intent)) {
		              startActivityForResult(intent, REQ_START_STANDALONE_PLAYER);
		            } else {Log.v("aanchal","youtube error");}
		          } 
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REQ_START_STANDALONE_PLAYER && resultCode != RESULT_OK) {
	      YouTubeInitializationResult errorReason =
	          YouTubeStandalonePlayer.getReturnedInitializationResult(data);
	      if (errorReason.isUserRecoverableError()) {
	        errorReason.getErrorDialog(this, 0).show();
	      } else {
	        String errorMessage =
	            String.format(getString(R.string.error_player), errorReason.toString());
	        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
	      }
	    }
	  }

	  private boolean canResolveIntent(Intent intent) {
	    List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(intent, 0);
	    return resolveInfo != null && !resolveInfo.isEmpty();
	  }

	  private int parseInt(String text, int defaultValue) {
	    if (!TextUtils.isEmpty(text)) {
	      try {
	        return Integer.parseInt(text);
	      } catch (NumberFormatException e) {
	        // fall through
	      }
	    }
	    return defaultValue;
	  }  
}*/
