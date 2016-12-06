package com.cwc.litenote.media.audio;

import java.io.File;
import java.util.Locale;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.alpha.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.util.Util;

import android.graphics.Color;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.TextView;

public class UtilAudio {
	
	static AudioPlayer mAudioPlayer;
	
	// Stop audio media player and audio handler
    public static void stopAudioPlayer()
    {
    	System.out.println("_stopAudioPlayer");
        if(AudioPlayer.mMediaPlayer != null)
    	{
			if(AudioPlayer.mMediaPlayer.isPlaying())
				AudioPlayer.mMediaPlayer.pause();
    		AudioPlayer.mMediaPlayer.release();
    		AudioPlayer.mMediaPlayer = null;
    		AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunOneTimeMode); 
    		AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunContinueMode); 
    		AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_STOP;
    	}
    }
    
    public static void stopAudioIfNeeded()
    {
		if( (AudioPlayer.mMediaPlayer != null)    &&
			(TabsHostFragment.mCurrent_tabIndex == DrawerActivity.mCurrentPlaying_tabIndex)&&
			(DrawerActivity.mCurrentPlaying_drawerIndex == DrawerActivity.mFocus_drawerChildPos)&&
			(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP)      )
		{
			UtilAudio.stopAudioPlayer();
			AudioPlayer.mAudioIndex = 0;
			AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_STOP;
			TabsHostFragment.setAudioPlayingTab_WithHighlight(false);
			DrawerActivity.mSubMenuItemAudio.setIcon(R.drawable.ic_menu_slideshow);
			NoteFragment.mItemAdapter.notifyDataSetChanged(); // disable focus
		}     	
    }
    
    // update footer audio state
    public static void updateFooterAudioState(ImageView footerAudioImage, TextView textView)
    {
    	System.out.println("UtilAudio/ _updateFooterAudioState / AudioPlayer.mPlayerState = " + AudioPlayer.mPlayerState);
		if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
		{
//			footerTextView.setEnabled(true);
			textView.setTextColor(Color.rgb(255,128,0));
			textView.setSelected(true);
//			footerAudioImage.setImageResource(R.drawable.ic_audio_selected); //highlight
			footerAudioImage.setImageResource(R.drawable.ic_media_pause);
		}
		else if( (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) ||
				 (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_STOP)    )
		{
//			footerTextView.setEnabled(false); // gray out effect if no setting text color
			textView.setSelected(false);
			textView.setTextColor(Color.rgb(64,64,64));
//			footerAudioImage.setImageResource(R.drawable.ic_lock_ringer_on);
			footerAudioImage.setImageResource(R.drawable.ic_media_play);
		}
    }
    
    // check if file has audio extension
    // refer to http://developer.android.com/intl/zh-tw/guide/appendix/media-formats.html
    public static boolean hasAudioExtension(File file)
    {
    	boolean hasAudio = false;
    	String fn = file.getName().toLowerCase(Locale.getDefault());
    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||	fn.endsWith("m4a") || fn.endsWith("aac") ||
       		fn.endsWith("ts") || fn.endsWith("flac") ||	fn.endsWith("mp3") || fn.endsWith("mid") ||  
       		fn.endsWith("xmf") || fn.endsWith("mxmf")|| fn.endsWith("rtttl") || fn.endsWith("rtx") ||  
       		fn.endsWith("ota") || fn.endsWith("imy")|| fn.endsWith("ogg") || fn.endsWith("mkv") ||
       		fn.endsWith("wav") || fn.endsWith("wma")
    		) 
	    	hasAudio = true;
	    
    	return hasAudio;
    }
    
    // check if string has audio extension
    public static boolean hasAudioExtension(String string)
    {
    	boolean hasAduio = false;
    	if(!Util.isEmptyString(string))
    	{
	    	String fn = string.toLowerCase(Locale.getDefault());
	    	if(	fn.endsWith("3gp") || fn.endsWith("mp4") ||	fn.endsWith("m4a") || fn.endsWith("aac") ||
	           		fn.endsWith("ts") || fn.endsWith("flac") ||	fn.endsWith("mp3") || fn.endsWith("mid") ||  
	           		fn.endsWith("xmf") || fn.endsWith("mxmf")|| fn.endsWith("rtttl") || fn.endsWith("rtx") ||  
	           		fn.endsWith("ota") || fn.endsWith("imy")|| fn.endsWith("ogg") || fn.endsWith("mkv") ||
	           		fn.endsWith("wav") || fn.endsWith("wma")
	        		) 
	    		hasAduio = true;
    	}
    	return hasAduio;
    }     
    
    public static boolean mIsCalledWhilePlayingAudio;
    // for Pause audio player when incoming call
    // http://stackoverflow.com/questions/5610464/stopping-starting-music-on-incoming-calls
    public static PhoneStateListener phoneStateListener = new PhoneStateListener() 
    {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) 
        {
            if ( (state == TelephonyManager.CALL_STATE_RINGING) || 
                 (state == TelephonyManager.CALL_STATE_OFFHOOK )   ) 
            {
                //Incoming call or Call out: Pause music
            	System.out.println("Incoming call:");
            	if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
            	{
            		AudioPlayer.manageAudioState(DrawerActivity.mDrawerActivity );
            		mIsCalledWhilePlayingAudio = true;
            	}
            } 
            else if(state == TelephonyManager.CALL_STATE_IDLE) 
            {
                //Not in call: Play music
            	System.out.println("Not in call:");
            	if( (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) && 
            		mIsCalledWhilePlayingAudio )	
            	{
            		AudioPlayer.manageAudioState(DrawerActivity.mDrawerActivity); // pause => play
            		mIsCalledWhilePlayingAudio = false;
            	}
            } 
            else if(state == TelephonyManager.CALL_STATE_OFFHOOK) 
            {
                //A call is dialing, active or on hold
            	System.out.println("A call is dialing, active or on hold:");
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };
    
    
}
