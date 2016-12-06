package com.cwc.litenote.media.image;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.media.video.AsyncTaskVideoBitmap;
import com.cwc.litenote.media.video.UtilVideo;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class UtilImage_bitmapLoader 
{
	Bitmap thumbnail;
	AsyncTaskVideoBitmap mVideoAsyncTask;
	SimpleImageLoadingListener mSimpleUilListener, mSimpleUilListenerForVideo;
	ImageLoadingProgressListener mUilProgressListener;
	ProgressBar mProgressBar;
	ImageView mPicImageView;
  
	public UtilImage_bitmapLoader(){}
  
	public UtilImage_bitmapLoader(ImageView picImageView,
								  String mPictureUriInDB,
								  final ProgressBar progressBar,
								  DisplayImageOptions options,
								  Activity mAct )
	{
 	    setLoadingListerners();
	    mPicImageView = picImageView;
	    mProgressBar = progressBar;
	    
		Bitmap bmVideoIcon = BitmapFactory.decodeResource(mAct.getResources(), R.drawable.ic_media_play);
		Uri imageUri = Uri.parse(mPictureUriInDB);
		String pictureUri = imageUri.toString();
//		System.out.println("UtilImage_bitmapLoader / _UtilImage_bitmapLoader / pictureUri = " + pictureUri);
		
		// 1 for image check
		if (UtilImage.hasImageExtension(pictureUri,mAct)) 
		{
//			System.out.println("UtilImage_bitmapLoader / _UtilImage_bitmapLoader / has imager extension");
			UilCommon.imageLoader
					 .displayImage(	pictureUri,
									mPicImageView,
									options,
									mSimpleUilListener,
									mUilProgressListener);
		}
		// 2 for video check
		else if (UtilVideo.hasVideoExtension(pictureUri,mAct)) 
		{
//			System.out.println("UtilImage_bitmapLoader / _UtilImage_bitmapLoader / has video extension");
			Uri uri = Uri.parse(pictureUri);
			String path = uri.getPath();
			
			// check if content is local or remote
			if(Util.getUriScheme(pictureUri).equals("content"))
			{
				path = Util.getLocalRealPathByUri(mAct,uri);
			}
			
			// for local
			if(path != null)
			{
				thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);

				// check if video thumb nail exists
				if (thumbnail != null) 
				{
					// add video play icon overlay
					thumbnail = UtilImage.setIconOnThumbnail(thumbnail,	bmVideoIcon, 50);
					UilCommon.imageLoader
							 .displayImage( "drawable://" + R.drawable.ic_media_play,
									 mPicImageView,
									 options,
									 mSimpleUilListenerForVideo,
									 mUilProgressListener);
				}
				// video file is not found
				else 
				{
					UilCommon.imageLoader
				 			 .displayImage( "drawable://" + R.drawable.ic_cab_done_holo,
				 					 mPicImageView,
				 					 options,
									 mSimpleUilListener,
									 mUilProgressListener);
	
				}
			}
			else // for remote
			{
				// refer to
				// http://android-developers.blogspot.tw/2009/05/painless-threading.html
				mVideoAsyncTask = new AsyncTaskVideoBitmap(mAct, pictureUri, mPicImageView, mProgressBar);
				mVideoAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");
			}
		}
		else 
		{
//			System.out.println("UtilImage_bitmapLoader / _UtilImage_bitmapLoader / can not decide image and video");
			mPicImageView.setVisibility(View.GONE);
		}
	}
  
    public void setLoadingListerners()
    {
        // set image loading listener
        mSimpleUilListener = new SimpleImageLoadingListener() 
        {
      	  @Override
      	  public void onLoadingStarted(String imageUri, View view) 
      	  {
//      		  System.out.println("----------------onLoadingStarted 1");
      		  mPicImageView.setVisibility(View.GONE);
      		  mProgressBar.setProgress(0);
      		  mProgressBar.setVisibility(View.VISIBLE);
      	  }

      	  @Override
      	  public void onLoadingFailed(String imageUri, View view, FailReason failReason) 
      	  {
//      		  System.out.println("----------------onLoadingFailed 1");
      		  mProgressBar.setVisibility(View.GONE);
      		  mPicImageView.setVisibility(View.VISIBLE);
      	  }

      	  @Override
      	  public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) 
      	  {
      		  super.onLoadingComplete(imageUri, view, loadedImage);
//      		  System.out.println("----------------onLoadingComplete 1");
      		  mProgressBar.setVisibility(View.GONE);
      		  mPicImageView.setVisibility(View.VISIBLE);
      	  }
  		};

  		// set image loading listener for video
  		mSimpleUilListenerForVideo = new SimpleImageLoadingListener() 
  		{
  			@Override
  			public void onLoadingStarted(String imageUri, View view) 
  			{
//  				System.out.println("----------------onLoadingStarted 2");
  				mPicImageView.setVisibility(View.GONE);
  				mProgressBar.setProgress(0);
  				mProgressBar.setVisibility(View.VISIBLE);
  			}

  			@Override
  			public void onLoadingFailed(String imageUri, View view, FailReason failReason) 
  			{
//  				System.out.println("----------------onLoadingFailed 2");
  				mProgressBar.setVisibility(View.GONE);
  				mPicImageView.setVisibility(View.VISIBLE);

  			}

  			@Override
  			public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) 
  			{
  				super.onLoadingComplete(imageUri, view, loadedImage);
//  				System.out.println("----------------onLoadingComplete 2");
  				mProgressBar.setVisibility(View.GONE);
  				mPicImageView.setVisibility(View.VISIBLE);
  				// set thumb nail bitmap instead of video play icon
  				mPicImageView.setImageBitmap(thumbnail);
  			}
  		};

  		// Set image loading process listener
  		mUilProgressListener = new ImageLoadingProgressListener() 
  		{
  			@Override
  			public void onProgressUpdate(String imageUri, View view, int current, int total) 
  			{
  				mProgressBar.setProgress(Math.round(100.0f * current / total));
  			}
  		};
    }
    
}
