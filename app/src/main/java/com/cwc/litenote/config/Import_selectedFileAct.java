/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cwc.litenote.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.util.Util;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Import_selectedFileAct extends Activity 
{

    private TextView mTitleViewText;
    private TextView mBodyViewText;
    Bundle extras ;
    static File mFile;
    FileInputStream fileInputStream = null;
    View mViewFile,mViewFileProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
    	System.out.println("Import_selectedFileAct / onCreate");

        setContentView(R.layout.view_file);
        mViewFile = findViewById(R.id.view_file);
        mViewFileProgressBar = findViewById(R.id.view_file_progress_bar);
        
        mTitleViewText = (TextView) findViewById(R.id.view_title);
        mBodyViewText = (TextView) findViewById(R.id.view_body);
        
	    getActionBar().setDisplayShowHomeEnabled(false);
        
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progress);
		ImportAsyncTask task = new ImportAsyncTask();
        task.setProgressBar(progressBar);
        task.enableSaveDB(false);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        

		int style = 2;
        //set title color
		mTitleViewText.setTextColor(Util.mText_ColorArray[style]);
		mTitleViewText.setBackgroundColor(Util.mBG_ColorArray[style]);
		//set body color 
		mBodyViewText.setTextColor(Util.mText_ColorArray[style]);
		mBodyViewText.setBackgroundColor(Util.mBG_ColorArray[style]);
		
        // back button
        Button backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);

        // confirm button
        Button confirmButton = (Button) findViewById(R.id.view_confirm);
        
        // do cancel
        backButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                finish();
            }
        });
        
		// confirm to import view to DB
        confirmButton.setOnClickListener(new View.OnClickListener() 
        {

            public void onClick(View view) 
            {
        		ProgressBar progressBar = (ProgressBar) findViewById(R.id.import_progress);
        		ImportAsyncTask task = new ImportAsyncTask();
                task.setProgressBar(progressBar);
                task.enableSaveDB(true);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    static Import_handleXmlFile importObject;
    private void insertSelectedFileContentToDB(boolean enableInsertDB) 
    {
		extras = getIntent().getExtras();
    	mFile = new File(extras.getString("FILE_PATH"));
    	
    	try 
    	{
    		fileInputStream = new FileInputStream(mFile);
    	} 
    	catch (FileNotFoundException e) 
    	{
    		e.printStackTrace();
    	}
		 
    	// import data by HandleXmlByFile class
    	importObject = new Import_handleXmlFile(fileInputStream,this);
    	importObject.enableInsertDB(enableInsertDB);
    	importObject.handleXML();
    	while(importObject.parsingComplete);
    }
    
    public static void createDefaultTables(Activity act,String fileName) 
    {
    	AssetManager am = act.getAssets();
    	InputStream inputStream = null;
    	try {
    		inputStream = am.open(fileName);
    	} catch (IOException e1) {
    		e1.printStackTrace();
    	}

		// main directory
	    String dirString = Environment.getExternalStorageDirectory().toString() + 
	    		              "/" + Util.getStorageDirName(act);
	    
		File dir = new File(dirString);
		if(!dir.isDirectory())
			dir.mkdir();    	
    	
		String filePath = dirString + "/" + fileName;
		
		try
		{
			mFile = new File(filePath);
			OutputStream outputStream = new FileOutputStream(mFile);
			byte buffer[] = new byte[1024];
			int length = 0;

			while((length=inputStream.read(buffer)) > 0) {
				outputStream.write(buffer,0,length);
			}

			outputStream.close();
			inputStream.close();

			FileInputStream fileInputStream = null;
			try 
			{
				fileInputStream = new FileInputStream(mFile);
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
			 
			// import data by HandleXmlByFile class
			importObject = new Import_handleXmlFile(fileInputStream,act);
			importObject.enableInsertDB(true);
			importObject.handleXML();
			while(importObject.parsingComplete);
		}
		catch (IOException e) 
		{
			//Logging exception
		}
	}    

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
	// Show progress bar
	public class ImportAsyncTask extends AsyncTask<Void, Integer, Void> {

		ProgressBar bar;
		boolean enableSaveDB;
		public void setProgressBar(ProgressBar bar) {
		    this.bar = bar;
		    mViewFile.setVisibility(View.GONE);
		    mViewFileProgressBar.setVisibility(View.VISIBLE);
		    bar.setVisibility(View.VISIBLE);
		}
		
		public void enableSaveDB(boolean enable)
		{
			enableSaveDB = enable;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		    super.onProgressUpdate(values);
		    if (this.bar != null) {
		        bar.setProgress(values[0]);
		    }
		}
		
		@Override
		protected Void doInBackground(Void... params) 
		{
			insertSelectedFileContentToDB(enableSaveDB);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			bar.setVisibility(View.GONE);
			mViewFile.setVisibility(View.VISIBLE);
			
			if(enableSaveDB)
			{
				finish();
				Toast.makeText(Import_selectedFileAct.this,R.string.toast_import_finished,Toast.LENGTH_SHORT).show();
			}
			else
			{
			    // show Import content
		    	mTitleViewText.setText(mFile.getName());
		    	mBodyViewText.setText(importObject.fileBody);
			}
		}
	}    
}
