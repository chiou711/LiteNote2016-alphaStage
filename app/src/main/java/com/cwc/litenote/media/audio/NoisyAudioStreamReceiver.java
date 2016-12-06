package com.cwc.litenote.media.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.alpha.R;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.note.Note_view_pagerUI;

public class NoisyAudioStreamReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
			if((AudioPlayer.mMediaPlayer != null) &&
			    AudioPlayer.mMediaPlayer.isPlaying() )
			{
				System.out.println("NoisyAudioStreamReceiver / play -> pause");
				AudioPlayer.mMediaPlayer.pause();
				AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunOneTimeMode); 
				AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunContinueMode); 
				AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_PAUSE;
				//update audio control state
				UtilAudio.updateFooterAudioState(NoteFragment.footerAudio_playOrPause_button,NoteFragment.footer_audio_title);
			}        	
        }
    }
}