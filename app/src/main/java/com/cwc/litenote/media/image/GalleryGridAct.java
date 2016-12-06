/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cwc.litenote.media.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.note.Note_addCameraImage;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.Util;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class GalleryGridAct extends Activity 
{
	String IMAGES = "IMAGES";
	String IMAGE_POSITION = "IMAGE_POSITION";		
    DB mDb;
    Long mRowId;
    int mEntryPosition;
	String[] mImageUrls;
	private List<String> items = null;
    List<String> fileNames = null;
	protected AbsListView gridView;
	String dirString;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_grid);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		UilCommon.init();
		
		int lastContentId = Note_addCameraImage.getLastCapturedImageId(this);
		File dir = UtilImage.getImageDirectoryByContentId(this,lastContentId);
	    getFiles(dir.listFiles());
		
		gridView = (GridView) findViewById(R.id.gridview);
		
		// check if directory is created AND not empty
		if( (mImageUrls != null  ) && (mImageUrls.length > 0)) 
		{
			((GridView) gridView).setAdapter(new ImageAdapter());
		}
		else
		{
			Toast.makeText(GalleryGridAct.this,R.string.gallery_toast_no_file,Toast.LENGTH_SHORT).show();
			finish();
		}
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startImagePagerActivity(position);
			}
		});
		
		// set scroll bar thumb
		gridView.setScrollbarFadingEnabled(true);
		gridView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
		Util.setScrollThumb(this,gridView);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
    private void getFiles(File[] files)
    {
        if(files == null)
        {
        	Toast.makeText(GalleryGridAct.this,R.string.gallery_toast_no_file,Toast.LENGTH_SHORT).show();
        	finish();
        }
        else
        {
//        	System.out.println("files length = " + files.length);
        	String imagePath[] = new String[files.length];
        	
            items = new ArrayList<String>();
            fileNames = new ArrayList<String>();
            items.add("");
            fileNames.add("ROOT");
            int i=0;
            
	        for(File file : files)
	        {
	            items.add(file.getPath());
	            fileNames.add(file.getName());
		        if( UtilImage.hasImageExtension(file.getName(), this) )
	            {
//		            System.out.println("file.getPath() = " + file.getPath());
//		            System.out.println("i = " + i);
		            if(i< files.length)
		            {
		            	imagePath[i] = "file:///" + file.getPath();
		            	System.out.println("imagePath[i] = " + imagePath[i]);
		            	i++;
		            }
	            }
	        }
	        mImageUrls = imagePath;
        }
    }

	private void startImagePagerActivity(int position) 
	{
		Intent intent = new Intent(this, GalleryPagerAct.class);
		intent.putExtra(IMAGES, mImageUrls);
		intent.putExtra(IMAGE_POSITION, position);
		startActivity(intent);
	}

	public class ImageAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mImageUrls.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			View view = convertView;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.gallery_grid_item, parent, false);
				holder = new ViewHolder();
				assert view != null;
				holder.imageView = (ImageView) view.findViewById(R.id.grid_item_image);
				holder.progressBar = (ProgressBar) view.findViewById(R.id.grid_item_progress);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			UilCommon.imageLoader.displayImage(mImageUrls[position],
									 holder.imageView,
									 UilCommon.options, 
						 new SimpleImageLoadingListener() 
						 {
							 @Override
							 public void onLoadingStarted(String imageUri, View view) {
								 holder.progressBar.setProgress(0);
								 holder.progressBar.setVisibility(View.VISIBLE);
							 }

							 @Override
							 public void onLoadingFailed(String imageUri, View view,
									 FailReason failReason) {
								 holder.progressBar.setVisibility(View.GONE);
							 }

							 @Override
							 public void onLoadingComplete(String imageUri, View view, 
									 Bitmap loadedImage) {
								 holder.progressBar.setVisibility(View.GONE);
							 }
						 }, 
						 new ImageLoadingProgressListener() 
						 {
							 @Override
							 public void onProgressUpdate(String imageUri, View view, int current,
									 int total) {
								 holder.progressBar.setProgress(Math.round(100.0f * current / total));
							 }
						 }
			);
			return view;
		}

		class ViewHolder {
			ImageView imageView;
			ProgressBar progressBar;
		}
	}
}