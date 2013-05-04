package com.aanchal.chitrahaar;

import java.util.List;

import com.aanchal.chitrahaar.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int REQ_START_STANDALONE_PLAYER = 1;
	  private static final int REQ_RESOLVE_SERVICE_MISSING = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Button   viewVideoButton = (Button)   findViewById(R.id.viewVideoButton);
		viewVideoButton.setOnClickListener(new View.OnClickListener() {
		      
		      @Override
		      public void onClick(View pV) {

		        String videoId = "__BYsQo4QaY";
		        
		        Intent intent = YouTubeStandalonePlayer.createVideoIntent(MainActivity.this, "", videoId);
		        if (intent != null) {
		            if (canResolveIntent(intent)) {
		              startActivityForResult(intent, REQ_START_STANDALONE_PLAYER);
		            } else {
		              Log.v("aanchal","youtube error");
		            }
		          }
		        
		      }
		    });
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

}
