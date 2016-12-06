package com.cwc.litenote.media.image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

//Audio bitmap Async Task for applying MediaMetadataRetriever
//Note: setDataSource could hang up system for a long time when accessing remote content
public class AsyncTaskAudioBitmap extends AsyncTask<String,Integer,String>
{
	 Activity mAct;
	 String mAudioUri;
	 ImageView mImageView;
	 MediaMetadataRetriever mmr;
	 Bitmap bitmap;
	 ProgressBar mProgressBar;
	 
	 public AsyncTaskAudioBitmap(Activity act,String audioString, ImageView view, ProgressBar progressBar)
	 {
		 mAct = act;
		 mAudioUri = audioString;
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
		 mmr = new MediaMetadataRetriever();
		 try
		 {
			 mmr.setDataSource(mAct,Uri.parse(mAudioUri));//??? why hang up?
			 
			 byte[] artBytes =  mmr.getEmbeddedPicture();
			 if(artBytes != null)
			 {
				 InputStream is = new ByteArrayInputStream(mmr.getEmbeddedPicture());
				 bitmap = BitmapFactory.decodeStream(is);
			 }
			 mmr.release();
		 }
		 catch(Exception e)
		 { 
			 Log.e("AsyncTaskAudioBitmap / mmr.setDataSouce", "exception of illeagal argument");			 
		 }

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
		 
		 if(bitmap != null)
		 {
			 ((ViewGroup) mImageView.getParent()).setVisibility(View.VISIBLE);
			 mImageView.setImageBitmap(bitmap);
			 mImageView.setVisibility(View.VISIBLE);
		 }
		 else
		 {
			 mImageView.setVisibility(View.GONE);
			 ((ViewGroup) mImageView.getParent()).setVisibility(View.GONE);
		 }
	 }
}