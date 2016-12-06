package com.cwc.litenote.media.audio;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.alpha.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.note.Note_view_pagerUI;
import com.cwc.litenote.util.Util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class AudioPlayer 
{
	private static final String TAG = "AUDIO_PLAYER"; // error logging tag
	static final int DURATION_1S = 1000; // 1 seconds per slide
	static AudioInfo mAudioInfo; // slide show being played
	public static Handler mAudioHandler; // used to update the slide show
	public static int mAudioIndex; // index of current media to play
	public static int mPlaybackTime; // time in miniSeconds from which media should play 
	public static MediaPlayer mMediaPlayer; // plays the background music, if any
	static FragmentActivity mAct;
	public static int mPlayerState;
	public static int PLAYER_AT_STOP = 0;
	public static int PLAYER_AT_PLAY = 1;
	public static int PLAYER_AT_PAUSE = 2;
	public static int mAudioPlayMode;
	static int mAudio_tryTimes; // use to avoid useless looping in Continue mode
	public final static int ONE_TIME_MODE = 0;
	public final static int CONTINUE_MODE = 1;
   
	// Manage audio state
	public static void manageAudioState(FragmentActivity fragAct)
	{
	   	System.out.println("AudioPlayer / _manageAudioState ");
	   	// if media player is null, set new fragment
		if(mMediaPlayer == null)
		{
		 	// show toast if Audio file is not found or No selection of audio file
			if( (AudioInfo.getAudioFilesSize() == 0) &&
				(mAudioPlayMode == AudioPlayer.CONTINUE_MODE)        )
			{
				Toast.makeText(fragAct,R.string.audio_file_not_found,Toast.LENGTH_SHORT).show();
			}
			else
			{
				mPlaybackTime = 0;
				mPlayerState = PLAYER_AT_PLAY;
				mAudio_tryTimes = 0;
				mAct = fragAct;
				startNewAudio();
			}
		}
		else
		{
			// from play to pause
			if(mMediaPlayer.isPlaying())
			{
				System.out.println("AudioPlayer / _manageAudioState / play -> pause");
				mMediaPlayer.pause();
				mAudioHandler.removeCallbacks(mRunOneTimeMode); 
				mAudioHandler.removeCallbacks(mRunContinueMode); 
				mPlayerState = PLAYER_AT_PAUSE;
			}
			else // from pause to play
			{
				System.out.println("AudioPlayer / _manageAudioState / pause -> play");
				mMediaPlayer.start();
				mAudioHandler.post(mRunOneTimeMode);  
				mAudioHandler.post(mRunContinueMode);  
				mPlayerState = PLAYER_AT_PLAY;
			}
		}
	}   	
	
	//
	// One time mode
	//
	public static Runnable mRunOneTimeMode = new Runnable()
	{   @Override
		public void run()
		{
	   		if(mMediaPlayer == null)
	   		{
	   			String audioStr = mAudioInfo.getAudioAt(mAudioIndex);
	   			if(AsyncTaskAudioUrlVerify.mIsOkUrl)
	   			{
				    System.out.println("Runnable updateMediaPlay / play mode: OneTime");
	   				
				    //create a MediaPlayer
				    mMediaPlayer = new MediaPlayer();
	   				mMediaPlayer.reset();
	   				
	   				//set audio player listeners
	   				setAudioPlayerListeners();
	   				
	   				try
	   				{
	   					mMediaPlayer.setDataSource(mAct, Uri.parse(audioStr));
	   					
					    // prepare the MediaPlayer to play, this will delay system response 
   						mMediaPlayer.prepare();
   						
	   					//Note: below
	   					//Set 1 second will cause Media player abnormal on Power key short click
	   					mAudioHandler.postDelayed(mRunOneTimeMode,DURATION_1S * 2);
	   				}
	   				catch(Exception e)
	   				{
	   					Toast.makeText(mAct,R.string.audio_message_could_not_open_file,Toast.LENGTH_SHORT).show();
	   					stopAudio();
	   				}
	   			}
	   			else
	   			{
	   				Toast.makeText(mAct,R.string.audio_message_no_media_file_is_found,Toast.LENGTH_SHORT).show();
   					stopAudio();
	   			}
	   		}
	   		else if(mMediaPlayer != null)
	   		{
	   			Note_view_pager.primaryAudioSeekBarProgressUpdater();
				mAudioHandler.postDelayed(mRunOneTimeMode,DURATION_1S);
	   		}		    		
		} 
	};

	//
	// Continue mode
	//
	public static String mAudioStr;
	public static Runnable mRunContinueMode = new Runnable()
	{   @Override
		public void run()
		{
	   		if( AudioInfo.getAudioMarking(mAudioIndex) == 1 )
	   		{ 
	   			if(mMediaPlayer == null)
	   			{
		    		// check if audio file exists or not
   					mAudioStr = mAudioInfo.getAudioAt(mAudioIndex);

					if(!AsyncTaskAudioUrlVerify.mIsOkUrl)
					{
						mAudio_tryTimes++;
						playNextAudio();
					}
					else
   					{
   						System.out.println("* Runnable updateMediaPlay / play mode: continue");
	   					
   						//create a MediaPlayer 
   						mMediaPlayer = new MediaPlayer(); 
	   					mMediaPlayer.reset();
	   					willPlayNext = true; // default: play next
	   					NoteFragment.mProgress = 0;
	   					
	   					// for network stream buffer change
	   					mMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener()
	   					{
	   						@Override
	   						public void onBufferingUpdate(MediaPlayer mp, int percent) {
	   							NoteFragment.seekBarProgress.setSecondaryProgress(percent);
	   						}
	   					});
   						
	   					// set listeners
   						setAudioPlayerListeners();
   						
   						try
   						{
   							// set data source
							mMediaPlayer.setDataSource(mAct, Uri.parse(mAudioStr));
   							
   							// prepare the MediaPlayer to play, could delay system response
   							mMediaPlayer.prepare();
   						}
   						catch(Exception e)
   						{
   							System.out.println("on Exception");
   							Log.e(TAG, e.toString());
							mAudio_tryTimes++;
   							playNextAudio();
   						}
   					}
	   			}
	   			else if(mMediaPlayer != null )
	   			{
	   				// keep looping, do not set post() here, it will affect slide show timing
	   				if(mAudio_tryTimes < AudioInfo.getAudioFilesSize())
	   				{
						// update seek bar
	   					NoteFragment.primaryAudioSeekBarProgressUpdater();
						
						if(mAudio_tryTimes == 0)
							mAudioHandler.postDelayed(mRunContinueMode,DURATION_1S);
						else
							mAudioHandler.postDelayed(mRunContinueMode,DURATION_1S/10);
	   				}
	   			}
	   		}
	   		else if( AudioInfo.getAudioMarking(mAudioIndex) == 0 )// for non-marking item
	   		{
	   			System.out.println("--- for non-marking item");
	   			// get next index
	   			if(willPlayNext)
	   				mAudioIndex++;
	   			else
	   				mAudioIndex--;
	   			
	   			if( mAudioIndex >= AudioInfo.getAudioList().size())
	   				mAudioIndex = 0; //back to first index
	   			else if( mAudioIndex < 0)
	   			{
	   				mAudioIndex ++;
	   				willPlayNext = true;
	   			}
	   			
	   			startNewAudio();
	   		}
		} 
	};	
	
	static boolean mIsPrepared;
	static void setAudioPlayerListeners()	
	{
			// - on completion listener
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener()
			{	@Override
				public void onCompletion(MediaPlayer mp) 
				{
					System.out.println("AudioPlayer / _setAudioPlayerListeners / onCompletion");
					
					if(mMediaPlayer != null)
								mMediaPlayer.release();
	
					mMediaPlayer = null;
					mPlaybackTime = 0;
					
					// get next index
					if(mAudioPlayMode == CONTINUE_MODE)
					{
						mAudio_tryTimes = 0; //reset try times 
						mAudioIndex++;
						if(mAudioIndex == AudioInfo.getAudioList().size())
							mAudioIndex = 0;	// back to first index
						
						startNewAudio();
			    		NoteFragment.mItemAdapter.notifyDataSetChanged();
					}
					else // one time mode
					{
	   					stopAudio();
	   					Note_view_pager.initAudioProgress();
					}
				}
			});
			
			// - on prepared listener
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener()
			{	@Override
				public void onPrepared(MediaPlayer mp) 
				{
					System.out.println("AudioPlayer / _setAudioPlayerListeners / _onPrepared");

					// set seek bar progress
					NoteFragment.mediaFileLength_MilliSeconds = mMediaPlayer.getDuration(); // gets the song length in milliseconds from URL
					NoteFragment.primaryAudioSeekBarProgressUpdater();
					
					if(!Note_view_pager.isPausedAtSeekerAnchor)
						Note_view_pager.primaryAudioSeekBarProgressUpdater();
					
					// set footer message: media name
					if(!Util.isEmptyString(mAudioStr))
						NoteFragment.setFooterAudioControl(mAudioStr);//??? not for pager mode, add getMode?
					
					if(mMediaPlayer!= null)
					{
						mIsPrepared = true;
						if(!Note_view_pager.isPausedAtSeekerAnchor)
						{
							mMediaPlayer.start();
							mMediaPlayer.getDuration();
							mMediaPlayer.seekTo(mPlaybackTime);
						}
						else
						{
							mMediaPlayer.seekTo(Note_view_pager.mAnchorPosition);
//							Note_view_pager.mMenuItemAudio.setIcon(R.drawable.ic_lock_ringer_on); // no highlight
							Note_view_pager.mPager_audio_play_button.setImageResource(R.drawable.ic_media_play);
							Note_view_pager.mPager_audio_title.setSelected(false);				
						}
						
						// set highlight of playing tab
						if((mAudioPlayMode == CONTINUE_MODE) &&
						   (DrawerActivity.mCurrentPlaying_drawerIndex == DrawerActivity.mFocus_drawerChildPos) )
							TabsHostFragment.setAudioPlayingTab_WithHighlight(true);
						else
							TabsHostFragment.setAudioPlayingTab_WithHighlight(false);
						
						NoteFragment.mItemAdapter.notifyDataSetChanged();
						
						// add for calling runnable
						if(mAudioPlayMode == CONTINUE_MODE )
							mAudioHandler.postDelayed(mRunContinueMode,Util.oneSecond/4);
					}						
				}
				
			});	 
			
			// - on error listener
			mMediaPlayer.setOnErrorListener(new OnErrorListener()
			{	@Override
				public boolean onError(MediaPlayer mp,int what,int extra) 
				{
					// more than one error when playing an index 
					System.out.println("AudioPlayer / _setAudioPlayerListeners / on Error: what = " + what + " , extra = " + extra);
					return false;
				}
			});
	}
	
	public static AsyncTaskAudioUrlVerify mAudioUrlVerifyTask;
	static void startNewAudio()
	{
		// remove call backs to make sure next toast will appear soon
		if(mAudioHandler != null)
		{
			mAudioHandler.removeCallbacks(mRunOneTimeMode); 
			mAudioHandler.removeCallbacks(mRunContinueMode);
		}

		// start a new handler
		mAudioHandler = new Handler();
		
		if( (mAudioPlayMode == CONTINUE_MODE) && (AudioInfo.getAudioMarking(mAudioIndex) == 0))
		{
			mAudioHandler.postDelayed(mRunContinueMode,Util.oneSecond/4);		}
		else
		{
			mAudioUrlVerifyTask = new AsyncTaskAudioUrlVerify(mAct);
			mAudioUrlVerifyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");
		}
	}
	
	public static boolean willPlayNext;
	public static void playNextAudio()
	{		
//		Toast.makeText(mAct,"Can not open file, try next one.",Toast.LENGTH_SHORT).show();
		System.out.println("AudioPlayer / _playNextAudio");
		if(mMediaPlayer != null)
		{
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		mPlaybackTime = 0;
   
		// new audio index
		mAudioIndex++;
		
		if(mAudioIndex >= AudioInfo.getAudioList().size())
			mAudioIndex = 0; //back to first index

		// check try times,had tried or not tried yet, anyway the audio file is found
		System.out.println("check mTryTimes = " + mAudio_tryTimes);
		if(mAudio_tryTimes < AudioInfo.getAudioFilesSize())
		{
			startNewAudio();
		}
		else // try enough times: still no audio file is found 
		{
			Toast.makeText(mAct,R.string.audio_message_no_media_file_is_found,Toast.LENGTH_SHORT).show();
			
			// do not show highlight
			DrawerActivity.mSubMenuItemAudio.setIcon(R.drawable.ic_menu_slideshow);
			TabsHostFragment.setAudioPlayingTab_WithHighlight(false);
			NoteFragment.mItemAdapter.notifyDataSetChanged();

			// stop media player
			stopAudio();
		}		
		System.out.println("Next mAudioIndex = " + mAudioIndex);
	}

	public static void stopAudio()
	{
		System.out.println("AudioPlayer / _stopAudio");
		if(mMediaPlayer != null)
			mMediaPlayer.release();
		mMediaPlayer = null;
		mAudioHandler.removeCallbacks(mRunOneTimeMode); 
		mAudioHandler.removeCallbacks(mRunContinueMode); 
		mPlayerState = PLAYER_AT_STOP;
		
		// make sure progress dialog will disappear
	 	if( !mAudioUrlVerifyTask.isCancelled() )
	 	{
	 		mAudioUrlVerifyTask.cancel(true);
	 		
	 		if( (mAudioUrlVerifyTask.mUrlVerifyDialog != null) &&
	 		 	mAudioUrlVerifyTask.mUrlVerifyDialog.isShowing()	)
	 		{
	 			mAudioUrlVerifyTask.mUrlVerifyDialog.dismiss();
	 		}

	 		if( (mAudioUrlVerifyTask.mAudioPrepareTask != null) &&
	 			(mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog != null) &&
	 			mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog.isShowing()	)
	 		{
	 			mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog.dismiss();
	 		}
	 	}
	}
	
	// prepare audio info
	public static void prepareAudioInfo(Context context)
	{
		mAudioInfo = new AudioInfo(); 
		mAudioInfo.updateAudioInfo(context);
	}

}