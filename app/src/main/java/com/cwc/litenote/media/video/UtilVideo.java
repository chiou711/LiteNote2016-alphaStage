package com.cwc.litenote.media.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.note.Note_view_pagerUI;
import com.cwc.litenote.util.Util;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;

public class UtilVideo 
{
	// Play mode setting, true:media control widget / false:custom control buttons
	public static boolean hasMediaControlWidget = false;// true;//      
	
	public static FragmentActivity mAct;
	private static final String TAG_VIDEO = "Video View";
	public final static int VIDEO_AT_STOP = 1;
	public final static int VIDEO_AT_PLAY = 2;
	public final static int VIDEO_AT_PAUSE = 3;

	public static VideoViewCustom mVideoView;
	public static int mVideoState;
	public static int mPlayVideoPosition;
	static String mPictureString;
	public static String currentPicturePath;
	public static View mCurrentPagerView;
	public static Button mVideoPlayButton;
	
	UtilVideo()	{}
	
	/***********************************************
	* init video view
	* 
	initVideoView
		setVideoViewLayout
				getBitmapDrawableByPath
				branch __ new PagerVideoAsyncTask
					   |_____ setVideoViewDimensions()
						   |_ setBitmapDrawableToVideoView
		setVideoViewUI
	*/
	public static void initVideoView(final String strPicture, final FragmentActivity act)
    {
    	System.out.println("UtilVideo / _initVideoView / strPicture = " + strPicture);
		mAct = act;
		mPictureString = strPicture;
    	
    	if(hasVideoExtension(mPictureString,mAct))
	  	{
        	System.out.println("UtilVideo / _initVideoView / has video extenion");
        	setVideoViewLayout(mPictureString);
        	
        	if(!UtilVideo.hasMediaControlWidget)
        		setVideoViewUI();
        	else
				playOrPauseVideo(mPictureString);
  		}
    } // handle video entry
	
    public static BitmapDrawable mBitmapDrawable;
	
    // set video view layout
    public static void setVideoViewLayout(String picStr)
    {
    	System.out.println("UtilVideo / _setVideoViewLayout");
		// set video view
		ViewGroup viewGroup = (ViewGroup) mCurrentPagerView.findViewById(R.id.pictureContent);
	    mVideoPlayButton = (Button) (viewGroup.findViewById(R.id.video_view_play_video));
      	mVideoView = (VideoViewCustom) mCurrentPagerView.findViewById(R.id.video_view);
      	ProgressBar spinner = (ProgressBar) mCurrentPagerView.findViewById(R.id.loading);
      	
      	mVideoView.setPlayPauseListener(new VideoViewCustom.PlayPauseListener() 
      	{
      	    @Override
      	    public void onPlay() {
      	    	setVideoState(VIDEO_AT_PLAY);
      	        System.out.println("UtilVideo / _setVideoViewLayout / setPlayPauseListener / Play!");
      	    }

      	    @Override
      	    public void onPause() {
      	    	setVideoState(VIDEO_AT_PAUSE);
      	        System.out.println("UtilVideo / _setVideoViewLayout / setPlayPauseListener / Pause!");
      	    }
      	});      	
      	
		mVideoView.setVisibility(View.VISIBLE);
		
//		System.out.println("UtilVideo / _setVideoViewLayout / video view h = " + mVideoView.getHeight());
//		System.out.println("UtilVideo / _setVideoViewLayout / video view w = " + mVideoView.getWidth());
		
		// get bitmap by path
      	mBitmapDrawable = getBitmapDrawableByPath(mAct,picStr);
      	
      	// if bitmap drawable is null, start an Async task
	  	if(mBitmapDrawable.getBitmap() == null)
	  	{
	  		System.out.println("UtilVideo / _setVideoViewLayout / mBitmapDrawable.getBitmap() == null");
	      	AsyncTaskVideoBitmapPager mPagerVideoAsyncTask = null;
			mPagerVideoAsyncTask = new AsyncTaskVideoBitmapPager(mAct,mPictureString,mVideoView,spinner); 
			mPagerVideoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");
	  	}
	  	else // if bitmap is not null, set bitmap drawable to video view directly
	  	{
	  		AsyncTaskVideoBitmapPager.mVideoUrl = null;
	  		
	  		System.out.println("UtilVideo / _setVideoViewLayout / mBitmapDrawable.getBitmap() != null");
	  		setVideoViewDimensions(mBitmapDrawable);
	  		
	  		if(!hasMediaControlWidget)
	  		{
		      	// set bitmap drawable to video view
		  		if(UtilVideo.mVideoView.getCurrentPosition() == 0)
		  			setBitmapDrawableToVideoView(mBitmapDrawable,mVideoView);
	  		}
	  	}

//		if(mVideoView != null)
//		{
//	    	System.out.println("UtilVideo / _setVideoViewLayout / mVideoView != null");
//	      	mVideoView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
//		}
		
    }    
    
    // Set video UI
    public static void setVideoViewUI()
    {
    	System.out.println("UtilVideo / _setVideoViewUI");
        if(hasVideoExtension(Note_view_pager.getCurrentPictureString(), Note_view_pager.mAct))
        	showVideoPlayButtonState();
		
		mVideoPlayButton.setOnClickListener(videoPlayBtnListener); 
    }    
    
    // Set video view dimensions
    public static void setVideoViewDimensions(BitmapDrawable bitmapDrawable)
    {
    	int screenHeight = UtilImage.getScreenHeight(mAct);
	    int screenWidth = UtilImage.getScreenWidth(mAct);
//  		System.out.println("UtilVideo / _setVideoViewDimensions / screenHeight = " + screenHeight + ", screenWidth = " + screenWidth);
	    
      	int bitmapHeight = 0,bitmapWidth = 0;
      	int config_orientation = mAct.getResources().getConfiguration().orientation;

      	Bitmap bitmap = bitmapDrawable.getBitmap();
      	boolean bitmapIsLandScape = false;
      	boolean	bitmapIsPortrait = false;
      	
      	if(bitmap != null)
      	{
      		bitmapHeight = bitmap.getHeight();
      		bitmapWidth = bitmap.getWidth();
      		System.out.println("UtilVideo / _setVideoViewDimensions / bitmapHeight = " + bitmapHeight + ", bitmapWidth = " + bitmapWidth);
          	bitmapIsLandScape = ( bitmapWidth > bitmapHeight )?true:false;
          	bitmapIsPortrait = ( bitmapHeight > bitmapWidth )?true:false;
//          	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsLandScape 1 = " + bitmapIsLandScape);
//          	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsPortrait 1 = " + bitmapIsPortrait);
      	}
      	
      	String rotDeg = AsyncTaskVideoBitmapPager.mRotationStr;
      	if(rotDeg != null)
      	{
      		System.out.println("UtilVideo / _setVideoViewDimensions / rotDeg = " + rotDeg);
      		if( rotDeg.equalsIgnoreCase("0"))
      		{
      	       	bitmapIsLandScape = true;
      	       	bitmapIsPortrait = false;
      	    }
      		else if( rotDeg.equalsIgnoreCase("90"))
      		{
      			bitmapIsLandScape = false;
      			bitmapIsPortrait = true;
      		}
      	}
//      	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsLandScape 2 = " + bitmapIsLandScape);
//      	System.out.println("UtilVideo / _setVideoViewDimensions / bitmapIsPortrait 2 = " + bitmapIsPortrait);
      	
      	int dimWidth = 0;
      	int dimHeight = 0;
      	// for landscape screen
  		if (config_orientation == Configuration.ORIENTATION_LANDSCAPE)
  		{
  			// for landscape bitmap
  			if(bitmapIsLandScape)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / L_scr L_bmp");
          		dimWidth = screenWidth;
          		dimHeight = screenHeight;
  			}// for portrait bitmap
  			else if (bitmapIsPortrait)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / L_scr P_bmp");
  				// set screen height to be constant, and set screen width by proportional
  	          	int propotionalWidth = 0;
  	          	if(bitmap != null)
  	          	{
  	          		propotionalWidth = (bitmapWidth > bitmapHeight)?
  							  		   Math.round(screenHeight * bitmapHeight/bitmapWidth) : 
  							  		   Math.round(screenHeight * bitmapWidth/bitmapHeight);
  	          	}
  	          	else
  	          		propotionalWidth = Math.round(screenHeight * screenHeight/screenWidth);
  	          	
          		dimWidth = propotionalWidth;
          		dimHeight = screenHeight;
  			}
  		}// for portrait screen
  		else if (config_orientation == Configuration.ORIENTATION_PORTRAIT)
  		{
  			// for landscape bitmap
  			if(bitmapIsLandScape)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / P_scr L_bmp");
	    		// set screen width to be constant, and set screen height by proportional
  	          	int propotiaonalHeight = 0;
  	          	if(bitmap != null)
  	          	{
  	          		
  	          		propotiaonalHeight = (bitmapWidth > bitmapHeight)?
  	          							  Math.round(screenWidth * bitmapHeight/bitmapWidth) : 
  	          							  Math.round(screenWidth * bitmapWidth/bitmapHeight);
  	          	}
  	          	else
  	          		propotiaonalHeight = Math.round(screenWidth * screenWidth/screenHeight );
  	          	
          		dimWidth = screenWidth;
          		dimHeight = propotiaonalHeight;
  			}// for portrait bitmap
  			else if (bitmapIsPortrait)
  			{
  	          	System.out.println("UtilVideo / _setVideoViewDimensions / P_scr P_bmp");
  	          	
          		dimWidth = screenWidth;
          		dimHeight = screenHeight;
  			}
  		}
  		
  		// set dimensions
    	if(UtilVideo.mVideoView != null)
    	{
    		UtilVideo.mVideoView.setDimensions(dimWidth, dimHeight);
    		UtilVideo.mVideoView.getHolder().setFixedSize(dimWidth, dimHeight);
    		System.out.println("UtilVideo / _setVideoViewDimensions / dim Width = " + dimWidth + ", dim Height = " + dimHeight);
    	}

  		
  	} //setVideoViewDimensions
    
    
    // on global layout listener
//	static OnGlobalLayoutListener onGlobalLayoutListener = new OnGlobalLayoutListener() 
//	{
//		@SuppressWarnings("deprecation")
//		@Override
//		public void onGlobalLayout() 
//		{
//			//action bar height is 120(landscape),144(portrait)
//			System.out.println("OnGlobalLayoutListener / getActionBar().getHeight() = " + mFragAct.getActionBar().getHeight());
//
//	      	// get bitmap by path
//	      	BitmapDrawable bitmapDrawable = getBitmapDrawableByPath(mFragAct,mPictureString);
//	      	
//		  	if(bitmapDrawable.getBitmap() == null)
//		  	{
//		      	PagerVideoAsyncTask mPagerVideoAsyncTask = null;
//				mPagerVideoAsyncTask = new PagerVideoAsyncTask(mFragAct,mPictureString,mVideoView); 
//				mPagerVideoAsyncTask.execute("Searching media ...");
//		  	}
//		  	else
//		      	// set bitmap drawable to video view
//		  		setBitmapDrawableToVideoView(bitmapDrawable,mVideoView);
//		} 
//	};	
	
	// Set Bitmap Drawable to Video View
	public static void setBitmapDrawableToVideoView(BitmapDrawable bitmapDrawable, VideoViewCustom videoView)
  	{
		//set bitmap drawable to video view
		System.out.println("UtilVideo / _setBitmapDrawableToVideoView / mPlayVideoPosition = " + mPlayVideoPosition);
		if(Build.VERSION.SDK_INT >= 16)
		{
			if(mPlayVideoPosition == 0)
				videoView.setBackground(bitmapDrawable);
		}
		else
		{
			if(mPlayVideoPosition == 0)
				videoView.setBackgroundDrawable(bitmapDrawable);
		}
		
		//??? add the following, why 720p video shows small bitmap?
		//this is an important step not to keep receiving callback
		//we should remove this listener
//		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
//			videoView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
//		else
//			videoView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
  	}
	
	// get bitmap drawable by path
	static BitmapDrawable getBitmapDrawableByPath(Activity mAct,String picPathStr)
	{
		String path = Uri.parse(picPathStr).getPath();
		Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(mAct.getResources(),bmThumbnail);
		return bitmapDrawable;
	}
	
	public static VideoPlayer mVideoPlayer;
	// Play or Pause video
	public static void playOrPauseVideo(String picString)
	{
		System.out.println("UtilVideo / _playOrPauseVideo / picString = " + picString);
		
		if( mVideoView!= null)
			System.out.println("UtilVideo / _playOrPauseVideo / mVideoView != null");
		else
			System.out.println("UtilVideo / _playOrPauseVideo / mVideoView == null");

		if( mVideoView!= null)
		{
			if(!mVideoView.isPlaying())
			{
				if(Build.VERSION.SDK_INT >= 16)
					mVideoView.setBackground(null);
				else
					mVideoView.setBackgroundDrawable(null);
			
				mVideoView.setVisibility(View.VISIBLE);
				
				//start a new Video player instance
				mVideoPlayer = new VideoPlayer(picString, mAct);//??? why not change video?
			}
			else if(mVideoPlayer != null)
			{
				mVideoPlayer.goOnVideo();
			}
		}
	}
	
	// get video data source path
	public static String getVideoDataSource(String path) throws IOException
	{
		if (!URLUtil.isNetworkUrl(path)) 
		{
			return path;
		} 
		else 
		{
			URL url = new URL(path);
			URLConnection cn = url.openConnection();
			cn.connect();
			InputStream stream = cn.getInputStream();
			
			if (stream == null)
				throw new RuntimeException("stream is null");
			
			File temp = File.createTempFile("mediaplayertmp", "dat");
			temp.deleteOnExit();
			String tempPath = temp.getAbsolutePath();
			FileOutputStream out = new FileOutputStream(temp);
			byte buf[] = new byte[128];
			
			do
			{
				System.setProperty("http.keepAlive", "false");//??? needed?
				int numread = stream.read(buf); //09-23 13:17:57.598: W/System.err(24621): java.net.SocketException: recvfrom failed: ECONNRESET (Connection reset by peer)
				if (numread <= 0)
					break;
				out.write(buf, 0, numread);
			} while (true);
			
			try 
			{
				stream.close();
				out.close();
			} catch (IOException ex) 
			{
				Log.e(TAG_VIDEO, "error: " + ex.getMessage(), ex);
			}
			return tempPath;
		}
	}	
	
    // check if file has video extension
    // refer to http://developer.android.com/intl/zh-tw/guide/appendix/media-formats.html
    public static boolean hasVideoExtension(File file)
    {
    	boolean isVideo = false;
    	String fn = file.getName().toLowerCase(Locale.getDefault());
    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
	    	isVideo = true;
	    
    	return isVideo;
    } 
    
    // check if string has video extension
    public static boolean hasVideoExtension(String string, Activity act)
    {
    	boolean hasVideo = false;
    	if(!Util.isEmptyString(string))
    	{
	    	String fn = string.toLowerCase(Locale.getDefault());
//	    	System.out.println("UtilVideo / _hasVideoExtension / fn 1 = " + fn);
	    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
	    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
		    	hasVideo = true;
    	}
    	
    	if(!hasVideo)
    	{
    		String fn = Util.getDisplayNameByUriString(string, act);
	    	fn = fn.toLowerCase(Locale.getDefault());
//	    	System.out.println("UtilVideo / _hasVideoExtension / fn 2 = " + fn);
	    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||
	    		fn.endsWith("ts") || fn.endsWith("webm") || fn.endsWith("mkv")  ) 
		    	hasVideo = true;    		
    	}
    	
    	return hasVideo;
    } 
    
    // show video play button state
    public static void showVideoPlayButtonState()
    {
    	Button btn = mVideoPlayButton;
        // show video play button icon
    	if(btn != null)
    	{
        	System.out.println("showVideoPlayButtonState / getVideoState() = " + getVideoState());
        	
        	if(getVideoState() == VIDEO_AT_PLAY)
	        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0, 0, 0);
	        else if((getVideoState() == VIDEO_AT_PAUSE) || (getVideoState() == VIDEO_AT_STOP))
	        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
	        
        	btn.setVisibility(View.VISIBLE);
    	}
    }    
    
    // update video play button state
    public static void updateVideoPlayButtonState()
    {
    	Button btn = mVideoPlayButton;
    	
    	if(btn == null)
    		return;
    	
        // show video play button icon
        if(getVideoState() == VIDEO_AT_PLAY)
        	btn.setVisibility(View.GONE);
        else if((getVideoState() == VIDEO_AT_PAUSE) || (getVideoState() == VIDEO_AT_STOP))
        {
        	btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
        	btn.setVisibility(View.VISIBLE);
        }
    }
    
    // video play button OnClickListener
    public static OnClickListener videoPlayBtnListener = new View.OnClickListener() 
    {
        public void onClick(View view) 
        {
        	System.out.println("UtilVideo / videoPlayBtnListener / getVideoState() = " + getVideoState());
        	// change video state
        	if(getVideoState() == VIDEO_AT_PLAY)
        		setVideoState(VIDEO_AT_PAUSE);
        	else if((getVideoState() == VIDEO_AT_PAUSE) || (getVideoState() == VIDEO_AT_STOP))
        		setVideoState(VIDEO_AT_PLAY); 
        	
        	playOrPauseVideo(Note_view_pager.getCurrentPictureString());
        }
    }; 
    
    // Set video player listeners
	static void setVideoPlayerListeners()	
	{
		// on complete listener
		mVideoView.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				System.out.println("UtilVideo / _setOnCompletionListener / _onCompletion");
				mPlayVideoPosition = 0;
				setVideoState(VIDEO_AT_PAUSE);
				
				if(!hasMediaControlWidget)
					updateVideoPlayButtonState();								
			}
		});					
		
		// on prepared listener
		mVideoView.setOnPreparedListener(new OnPreparedListener() 
		{
			@Override
			public void onPrepared(MediaPlayer mp) 
			{
				System.out.println("UtilVideo / _setOnPreparedListener");
				
//				Note_view_pager.mMP = mp;//TO
				
				if(!hasMediaControlWidget)
				{
					updateVideoPlayButtonState();	
					Note_view_pagerUI.primaryVideoSeekBarProgressUpdater(UtilVideo.mPlayVideoPosition);
					mp.setOnSeekCompleteListener(new OnSeekCompleteListener() 
					{
						@Override
						public void onSeekComplete(MediaPlayer mp) 
						{}
					});
				}
			}
		});
	}
    
	public static int getVideoState() {
		return mVideoState;
	}

	public static void setVideoState(int videoState) {
		mVideoState = videoState;
	}    
}

