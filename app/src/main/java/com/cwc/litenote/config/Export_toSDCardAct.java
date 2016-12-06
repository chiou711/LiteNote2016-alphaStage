package com.cwc.litenote.config;

import java.util.List;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.util.SelectPageList;
import com.cwc.litenote.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Export_toSDCardAct extends Activity{
	Context mContext;
	Intent mEMmailIntent;
	CheckedTextView mCheckTvSelAll;
    ListView mListView;
    List<String> mListStrArr;
    List<Boolean> mCheckedArr;    // 這個用來記錄哪幾個 item 是被打勾的
    int COUNT;
    int mStyle;
	String mSentString;
	SelectPageList mSelectPageList;
	View mSelPageDlg,mProgressBar;

	public Export_toSDCardAct(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		mContext = Export_toSDCardAct.this;
		
		setContentView(R.layout.select_page_list);
		mSelPageDlg = findViewById(R.id.selectPageDlg);
		mProgressBar = findViewById(R.id.progressBar);
		
		// checked Text View: select all 
		mCheckTvSelAll = (CheckedTextView) findViewById(R.id.chkSelectAllPages);
		mCheckTvSelAll.setOnClickListener(new OnClickListener()
		{	@Override
			public void onClick(View checkSelAll) 
			{
				boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
				((CheckedTextView)checkSelAll).setChecked(!currentCheck);
				
				if(((CheckedTextView)checkSelAll).isChecked())
					mSelectPageList.selectAllPages(true);
				else
					mSelectPageList.selectAllPages(false);
			}
		});
		mStyle = Util.getCurrentPageStyle(mContext);
		
		// list view: selecting which pages to send 
		mListView = (ListView)findViewById(R.id.listView1);
		
		// OK button: click to do next
		Button btnSelPageOK = (Button) findViewById(R.id.btnSelPageOK);
		btnSelPageOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// input mail address: dialog
				if(mSelectPageList.mChkNum > 0)
					inputFileNameDialog(); // call next dialog
				else
	    			Toast.makeText(Export_toSDCardAct.this,
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT).show();
			}
		});
		
		// cancel button
		Button btnSelPageCancel = (Button) findViewById(R.id.btnSelPageCancel);
		btnSelPageCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		// step 1: show list for Select
		mSelectPageList = new SelectPageList(Export_toSDCardAct.this,mListView);
	}
	
	// step 2: input file name 
    String mDefaultFileName;
    SharedPreferences mPref_email;
	EditText editSDCardFileNameText;
	Activity mActVE; // activity from ViewEdit
	String mEMailBodyString;
	AlertDialog mDialog;
	
	void inputFileNameDialog()
	{
		AlertDialog.Builder builder1;

		mPref_email = getSharedPreferences("sd_card_file_name", 0);
	    editSDCardFileNameText = (EditText)getLayoutInflater()
	    							.inflate(R.layout.edit_text_dlg, null);
		builder1 = new AlertDialog.Builder(Export_toSDCardAct.this);
		
		// default file name: with tab title
		mDefaultFileName = mSelectPageList.mXML_default_filename + ".xml";
		
		// with time stamp
//		mDefaultFileName = Util.getAppName(Export_toSDCardAct.this) + "_SAVE_" + // file name 
//				Util.getCurrentTimeString() + // time
//				".xml"; // extension name

		editSDCardFileNameText.setText(mDefaultFileName);
		
		builder1.setTitle(R.string.config_export_SDCard_edit_filename)
				.setMessage(R.string.config_SDCard_filename)
				.setView(editSDCardFileNameText)
				.setNegativeButton(R.string.edit_note_button_back, 
						new DialogInterface.OnClickListener() 
				{   @Override
					public void onClick(DialogInterface dialog, int which) 
					{/*cancel*/dialog.dismiss(); }
				})
				.setPositiveButton(R.string.btn_OK, null); //call override
		
		mDialog = builder1.create();
		mDialog.show();
		
		// override positive button
		Button enterButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		enterButton.setOnClickListener(new CustomListener(mDialog));
	}
	
	String mStrSDCardFileName;
	//for keeping dialog if no input
	class CustomListener implements View.OnClickListener 
	{
		public CustomListener(Dialog dialog){
	    }
	    
	    @Override
	    public void onClick(View v){
	        mStrSDCardFileName = editSDCardFileNameText.getText().toString();
	        if(mStrSDCardFileName.length() > 0)
	        {
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.export_progress);
				ShowProgressBarAsyncTask task = new ShowProgressBarAsyncTask();
		        task.setProgressBar(progressBar);
		        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	        }
	        else
	        {
    			Toast.makeText(Export_toSDCardAct.this,
						R.string.toast_input_filename,
						Toast.LENGTH_SHORT).show();
	        }
	    }
	}
	
	// Show progress bar
	public class ShowProgressBarAsyncTask extends AsyncTask<Void, Integer, Void> {

		ProgressBar bar;
		public void setProgressBar(ProgressBar bar) {
		    this.bar = bar;
		    mDialog.dismiss();
			mSelPageDlg.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
		    bar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		    super.onProgressUpdate(values);
		    if (this.bar != null) {
		        bar.setProgress(values[0]);
		    }
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Util util = new Util(Export_toSDCardAct.this);
			util.exportToSdCard(mStrSDCardFileName, // attachment name
								mSelectPageList.mCheckedArr,
								false); // checked page array
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			bar.setVisibility(View.GONE);
			Toast.makeText(Export_toSDCardAct.this,
					   R.string.btn_Finish, 
					   Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			finish();	
		}
	}
}