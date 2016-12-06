package com.cwc.litenote.media.video;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.note.Note_view_pager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

//Video Async Task for applying MediaMetadataRetriever
//Note: setDataSource could hang up system for a long time when accessing remote content
public class AsyncTaskVideoBitmap extends AsyncTask<String,Integer,String>
{
	 Activity mAct;
	 String mPictureUri;
	 ImageView mImageView;
	 MediaMetadataRetriever mmr;
	 Bitmap bitmap;
	 ProgressBar mProgressBar;
	 
	 public AsyncTaskVideoBitmap(Activity act,String picString, ImageView view, ProgressBar progressBar)
	 {
		 mAct = act;
		 mPictureUri = picString;
		 mImageView = view;
		 mProgressBar = progressBar;
	 }	 
	 
	 @Override
	 protected void onPreExecute() 
	 {
		 super.onPreExecute();
		 mImageView.setVisibility(View.GONE);
		 mProgressBar.setProgress(0);
		 mProgressBar.setVisibility(View.VISIBLE);
	 } 
	 
	 @Override
	 protected String doInBackground(String... params) 
	 {
		 
		 if(Note_view_pager.isPagerActive)
		 {
			if(this != null)
			{
				System.out.println("NoteFragment.mVideoAsyncTask != null");
				
				if(this.isCancelled())
					System.out.println("NoteFragment.mVideoAsyncTask.isCancelled()");
				else
					System.out.println("NoteFragment.mVideoAsyncTask is not Cancelled()");
				
				 if( (this != null) && (!this.isCancelled()) )
				 {
					 System.out.println("    NoteFragment.mVideoAsyncTask cancel");
					 this.cancel(true);
					 return "cancel";
				 }				
			}
			else
				System.out.println("NoteFragment.mVideoAsyncTask = null");
			
		 
		 }
		 
		 mmr = new MediaMetadataRetriever();
		 try
		 {
			 System.out.println("VideoAsyncTask / setDataSource start");
			 mmr.setDataSource(mAct,Uri.parse(mPictureUri));//??? why hang up?
			 System.out.println("VideoAsyncTask / setDataSource done");
			 bitmap = mmr.getFrameAtTime(-1);
			 bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
			 mmr.release();
		 }
		 catch(Exception e)
		 { }

		 return null;
	 }
	
	 @Override
	 protected void onProgressUpdate(Integer... progress)
	 { 
	     super.onProgressUpdate(progress);
	 }
	 
	 // This is executed in the context of the main GUI thread
	 protected void onPostExecute(String result)
	 {
		 mProgressBar.setVisibility(View.GONE);
		 mImageView.setVisibility(View.VISIBLE);
		 
		 Bitmap bmVideoIcon = BitmapFactory.decodeResource(mAct.getResources(), R.drawable.ic_media_play);
		 bitmap = UtilImage.setIconOnThumbnail(bitmap,bmVideoIcon,50);
		 
		 if(bitmap != null)
		 {
			 mImageView.setImageBitmap(bitmap);
			 System.out.println("VideoAsyncTask / set image bitmap");
		 }
	 }
}