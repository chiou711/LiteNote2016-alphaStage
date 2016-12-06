package com.cwc.litenote.media.video;


import java.io.IOException;

import com.cwc.litenote.media.image.UtilImage;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

// pager video async task
public class AsyncTaskVideoBitmapPager extends AsyncTask<String,Integer,String>
{
	 FragmentActivity mAct;
	 String mPictureUri;
	 VideoViewCustom mVideoView;
	 MediaMetadataRetriever mmr;
	 Bitmap bitmap;
	 static String mVideoUrl;
	 public static String mRotationStr = null;
	 ProgressBar mProgressBar;
	 
	 public AsyncTaskVideoBitmapPager(FragmentActivity act, String mPictureString,VideoViewCustom view, ProgressBar spinner) 
	 {
		 mAct = act;
		 mPictureUri = mPictureString;
		 System.out.println("AsyncTaskVideoBitmapPager constructor / mPictureUri = " + mPictureUri);
		 mVideoView = view;
		 mProgressBar = spinner;
	 }

	@Override
	 protected void onPreExecute() 
	 {
		 super.onPreExecute();
		 mVideoView.setVisibility(View.INVISIBLE);
		 mProgressBar.setProgress(0);
		 mProgressBar.setVisibility(View.VISIBLE);
	 } 
	 
	 @Override
	 protected String doInBackground(String... params) 
	 {
		 /// 
		 // for remote video
//		 String strPath = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"; // not always connected
//		 String strPath = "http://mgalaxy.net/load/Videos/Funny_Videos/Talking_Dog.3gp";
//		 String strPath = "http://techslides.com/demos/sample-videos/small.mp4";
//		 String strPath = "http://khalsaonline.net/Video/Nitnem/load/Nitnem/Sukhmani%20Sahib/Sukhmani%20Sahib%20Path%20Full.mp4";
//		 String strPath = "http://videomobi.in/files/Old%20is%20Gold%20Videos/MP4%20Videos/Collection%2001/Rs%20Ye%20Dunya%20Mere%20Kaam%20Ki%20(VideoMobi.IN).mp4";		 
//		 String strPath = mPictureUri;
//		 System.out.println("AsyncTaskVideoBitmapPager / _doInBackground / strPath = " + strPath);
//		 try {
//		     mVideoUrl = UtilVideo.getVideoDataSource(strPath);
//		     System.out.println("AsyncTaskVideoBitmapPager / _doInBackground / mVideoUrl = " + mVideoUrl);
//		 } catch (IOException e1) {
//		     e1.printStackTrace();
//		 }
		 ///

	     try {
			mVideoUrl = UtilVideo.getVideoDataSource(mPictureUri);
	     } catch (IOException e1) {
			e1.printStackTrace();
	     }
		 
		 mmr = new MediaMetadataRetriever();
		 try
		 {
			 System.out.println("AsyncTaskVideoBitmapPager / setDataSource start / mPictureUri = " + mPictureUri);
			 mmr.setDataSource(mAct,Uri.parse(mPictureUri));//??? why hang up?
//			 System.out.println("AsyncTaskVideoBitmapPager / setDataSource done / mPictureUri = " + mPictureUri );
			 bitmap = mmr.getFrameAtTime(-1);
			 bitmap = Bitmap.createScaledBitmap(bitmap, UtilImage.getScreenWidth(mAct), UtilImage.getScreenHeight(mAct), true);
			 
			 if (Build.VERSION.SDK_INT >= 17) {
				 mRotationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
				 Log.d("PagerVideoAsyncTask / Rotation = ", mRotationStr);
			 }
			 
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
		 mVideoView.setVisibility(View.VISIBLE);
		 
		 System.out.println("AsyncTaskVideoBitmapPager / _onPostExecute / mVideoUrl = " + mVideoUrl);
		 BitmapDrawable bitmapDrawable = new BitmapDrawable(mAct.getResources(),bitmap);
		 
		 if( (mVideoView != null) && (bitmapDrawable != null) )
		 {
			 UtilVideo.setVideoViewDimensions(bitmapDrawable);
			 if(!UtilVideo.hasMediaControlWidget)
			 {
				 if(mVideoView.getCurrentPosition() == 0)
					 UtilVideo.setBitmapDrawableToVideoView(bitmapDrawable,mVideoView);
			 }
		 }
		 else
			 return;
		 
		 ///
//		 System.out.println("AsyncTaskVideoBitmapPager / _onPostExecute / mVideoUrl = " + mVideoUrl);
		 //set  dim Width = 1080, dim Height = 607
//		 int dimWidth = 1080;
//		 int dimHeight = 607;
// 		 UtilVideo.mVideoView.setDimensions(dimWidth, dimHeight);
// 		 UtilVideo.mVideoView.getHolder().setFixedSize(dimWidth, dimHeight);
		 ///
	 }
}
