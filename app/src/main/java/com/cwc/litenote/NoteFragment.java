package com.cwc.litenote;

import java.util.ArrayList;
import java.util.List;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.lib.DragSortController;
import com.cwc.litenote.lib.DragSortListView;
import com.cwc.litenote.media.audio.AudioInfo;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.note.Note_edit;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.util.SendMailAct;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.UilListViewBaseFragment;
import com.cwc.litenote.util.Util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class NoteFragment extends UilListViewBaseFragment 
						  implements LoaderManager.LoaderCallbacks<List<String>> 
{
	private static Cursor mCursor_notes;
	public static DB mDb_notes;
    SharedPreferences mPref_delete_warn;
    SharedPreferences mPref_style;
	static SharedPreferences mPref_show_note_attribute;

    private static Long mNoteNumber1 = (long) 1;
	private static String mNoteTitle1;
	private static String mNotePictureUri1;
	private static String mNoteAudioUri1;
	private static String mNoteLinkUri1;
	private static String mNoteBodyString1;
	private static int mMarkingIndex1;
	private static Long mCreateTime1;
	private static Long mNoteNumber2 ;
	private static String mNotePictureUri2;
	private static String mNoteAudioUri2;
	private static String mNoteLinkUri2;
	private static String mNoteTitle2;
	private static String mNoteBodyString2;
	private static int mMarkingIndex2;
	private static Long mCreateTime2;
	private List<Boolean> mSelectedList = new ArrayList<Boolean>();
	
	// This is the Adapter being used to display the list's data.
	NoteListAdapter mAdapter;
	static DragSortListView mDndListView;
	private DragSortController mController;
	int MOVE_TO = 0;
	int COPY_TO = 1;
    public static int mStyle = 0;
	public static FragmentActivity mAct;
	String mClassName;
    public static int mHighlightPosition;
	public static SeekBar seekBarProgress;
	public static int mediaFileLength_MilliSeconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
	static ProgressBar mSpinner;
	
	public NoteFragment(){}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		System.out.println("NoteFragment / _onActivityCreated");  
		mAct = getActivity();
		
		mClassName = getClass().getSimpleName();
		
		// recover scroll Y
		mFirstVisibleIndex = Util.getPref_lastTimeView_list_view_first_visible_index(getActivity());
		mFirstVisibleIndexTop = Util.getPref_lastTimeView_list_view_first_visible_index_top(getActivity());
		
		listView = (DragSortListView)getActivity().findViewById(R.id.list1);
		mDndListView = listView;
		
	    mFooterTextView = (TextView) mAct.findViewById(R.id.footerText);
		mSpinner = (ProgressBar) getActivity().findViewById(R.id.list1_progress);
		new SpinnerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		//new ProgressBarTask().execute();
		//refer to 
		// http://stackoverflow.com/questions/9119627/android-sdk-asynctask-doinbackground-not-running-subclass
		//Behavior of AsyncTask().execute(); has changed through Android versions. 
		// -Before Donut (Android:1.6 API:4) tasks were executed serially, 
		// -from Donut to Gingerbread (Android:2.3 API:9) tasks executed paralleled; 
		// -since Honeycomb (Android:3.0 API:11) execution was switched back to sequential; 
		// a new method AsyncTask().executeOnExecutor(Executor) however, was added for parallel execution.
		
		// show scroll thumb
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
//			mDndListView.setFastScrollAlwaysVisible(true);
		
		mDndListView.setScrollbarFadingEnabled(true);
		mDndListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
		Util.setScrollThumb(getActivity(),mDndListView);
		
    	mStyle = Util.getCurrentPageStyle(getActivity());
//    	System.out.println("NoteFragment / _onActivityCreated / mStyle = " + mStyle);

    	UilCommon.init();
    	
    	//listener: view note 
    	mDndListView.setOnItemClickListener(new OnItemClickListener()
    	{   @Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) 
			{
    			System.out.println("NoteFragment / _onItemClick");
    			
    			//workaround to avoid onAudio being conflict with onItemClick
    			//??? better way?
    			//??? also conflict with onMark
//	    		boolean isAudioContentOnly = false;
    			mDb_notes.doOpenNotes();
	    		int count = mDb_notes.getNotesCount(false);
//	    		if(Util.isEmptyString(mDb_notes.getNoteTitle(position, false)) &&
//	    		   Util.isEmptyString(mDb_notes.getNoteBody(position, false)) &&
//	    		   Util.isEmptyString(mDb_notes.getNotePictureUri(position,false)) &&
//	    		   (!Util.isEmptyString(mDb_notes.getNoteAudioUri(position,false)))     )
//	    		{
//	    			isAudioContentOnly = true;
//	    		}
	    		mDb_notes.doCloseNotes();
	    		
//				if((position < count) && !isAudioContentOnly)// avoid footer error
				if(position < count)// avoid footer error
				{
					Intent intent;
					intent = new Intent(getActivity(), Note_view_pager.class);
			        intent.putExtra("POSITION", position);
			        startActivity(intent);
				}
			}
    	});
    	
    	// listener: edit note 
    	mDndListView.setOnItemLongClickListener(new OnItemLongClickListener()
    	{
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
             {	
		        Intent i = new Intent(getActivity(), Note_edit.class);
				Long rowId = mDb_notes.getNoteId(position,true);
		        i.putExtra("list_view_position", position);
		        i.putExtra(DB.KEY_NOTE_ID, rowId);
		        i.putExtra(DB.KEY_NOTE_TITLE, mDb_notes.getNoteTitleById(rowId));
		        i.putExtra(DB.KEY_NOTE_PICTURE_URI , mDb_notes.getNotePictureUriById(rowId));
		        i.putExtra(DB.KEY_NOTE_AUDIO_URI , mDb_notes.getNoteAudioUriById(rowId));
		        i.putExtra(DB.KEY_NOTE_LINK_URI , mDb_notes.getNoteLinkUriById(rowId));
		        i.putExtra(DB.KEY_NOTE_BODY, mDb_notes.getNoteBodyById(rowId));
		        i.putExtra(DB.KEY_NOTE_CREATED, mDb_notes.getNoteCreatedTimeById(rowId));
		        startActivity(i);
            	return true;
             }
	    });
    	
        mController = buildController(mDndListView);
        mDndListView.setFloatViewManager(mController);
        mDndListView.setOnTouchListener(mController);
        //??? Custom view com/cwc/litenote/lib/DragSortListView has setOnTouchListener 
        //called on it but does not override performClick
  		mDndListView.setDragEnabled(true);
	  	
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new NoteListAdapter(getActivity());

		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(true); //set progress indicator

		// Prepare the loader. Either re-connect with an existing one or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}
	
	private class SpinnerTask extends AsyncTask <Void,Void,Void>{
	    @Override
	    protected void onPreExecute(){
			mDndListView.setVisibility(View.GONE);
			mFooterTextView.setVisibility(View.GONE);
			mSpinner.setVisibility(View.VISIBLE);
	    }

	    @Override
	    protected Void doInBackground(Void... arg0) {
			return null;   
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	    	mSpinner.setVisibility(View.GONE);
			mDndListView.setVisibility(View.VISIBLE);
			mFooterTextView.setVisibility(View.VISIBLE);
			if(!this.isCancelled())
			{
				this.cancel(true);
			}
	    }
	}
	
    // list view listener: on drag
    private DragSortListView.DragListener onDrag = new DragSortListView.DragListener() 
    {
                @Override
                public void drag(int startPosition, int endPosition) {
                	//add highlight boarder
//                    View v = mDndListView.mFloatView;
//                    v.setBackgroundColor(Color.rgb(255,128,0));
//                	v.setBackgroundResource(R.drawable.listview_item_shape_dragging);
//                    v.setPadding(0, 4, 0,4);
                }
    };
	
    // list view listener: on drop
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() 
    {
        @Override
        public void drop(int startPosition, int endPosition) {
        	
        	int oriStartPos = startPosition;
        	int oriEndPos = endPosition;
        	
			if(startPosition >= mDb_notes.getNotesCount(true)) // avoid footer error
				return;
			
			mSelectedList.set(startPosition, true);
			mSelectedList.set(endPosition, true);
			
			
			//reorder data base storage
			int loop = Math.abs(startPosition-endPosition);
			for(int i=0;i< loop;i++)
			{
				swapRows(startPosition,endPosition);
				if((startPosition-endPosition) >0)
					endPosition++;
				else
					endPosition--;
			}
			
			if( MainUi.isSameNotesTable() &&
	     		(AudioPlayer.mMediaPlayer != null)				   )
			{
				if( (mHighlightPosition == oriEndPos)  && (oriStartPos > oriEndPos))      
				{
					mHighlightPosition = oriEndPos+1;
				}
				else if( (mHighlightPosition == oriEndPos) && (oriStartPos < oriEndPos))
				{
					mHighlightPosition = oriEndPos-1;
				}
				else if( (mHighlightPosition == oriStartPos)  && (oriStartPos > oriEndPos))      
				{
					mHighlightPosition = oriEndPos;
				}
				else if( (mHighlightPosition == oriStartPos) && (oriStartPos < oriEndPos))
				{
					mHighlightPosition = oriEndPos;
				}				
				else if(  (mHighlightPosition < oriEndPos) && 
						  (mHighlightPosition > oriStartPos)   )    
				{
					mHighlightPosition--;
				}
				else if( (mHighlightPosition > oriEndPos) && 
						 (mHighlightPosition < oriStartPos)  )
				{
					mHighlightPosition++;
				}

				AudioPlayer.mAudioIndex = (int)mHighlightPosition;
				AudioPlayer.prepareAudioInfo(mAct);
			}
			mItemAdapter.notifyDataSetChanged();
			setFooter();
        }
    };
	
    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public DragSortController buildController(DragSortListView dslv)
    {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        
        //drag
	  	mPref_show_note_attribute = getActivity().getSharedPreferences("show_note_attribute", 0);
	  	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "no").equalsIgnoreCase("yes"))
	  		controller.setDragInitMode(DragSortController.ON_DOWN); // click
	  	else
	        controller.setDragInitMode(DragSortController.MISS); 

	  	controller.setDragHandleId(R.id.img_dragger);// handler
//        controller.setDragInitMode(DragSortController.ON_LONG_PRESS); //long click to drag
	  	controller.setBackgroundColor(Color.argb(128,128,64,0));// background color when dragging
//        controller.setBackgroundColor(Util.mBG_ColorArray[mStyle]);// background color when dragging
        
	  	// mark
        controller.setMarkEnabled(true);
        controller.setClickMarkId(R.id.img_check);
        controller.setMarkMode(DragSortController.ON_DOWN);//??? how to avoid conflict?
        // audio
        controller.setAudioEnabled(true);
//        controller.setClickAudioId(R.id.img_audio);
        controller.setClickAudioId(R.id.audio_block);
        controller.setAudioMode(DragSortController.ON_DOWN);

        return controller;
    }        

    @Override
    public void onResume() {
    	super.onResume();
		mDb_notes = new DB(getActivity(),Util.getPref_lastTimeView_notes_tableId(getActivity())); 
		mDb_notes.initNotesDb(mDb_notes);    
    	System.out.println(mClassName + " / _onResume");
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	System.out.println("NoteFragment / _onPause");
		// make sure progress dialog will disappear after rotation
    	// to avoid exception: java.lang.IllegalArgumentException... not attached to window manager
 		if(AudioPlayer.mAudioUrlVerifyTask != null)
	 	{ 
	 		if((AudioPlayer.mAudioUrlVerifyTask.mUrlVerifyDialog != null) &&
	 		    AudioPlayer.mAudioUrlVerifyTask.mUrlVerifyDialog.isShowing()	)
	 		{
	 			AudioPlayer.mAudioUrlVerifyTask.mUrlVerifyDialog.dismiss();
	 		}
	
	 		if( (AudioPlayer.mAudioUrlVerifyTask.mAudioPrepareTask != null) &&
	 			(AudioPlayer.mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog != null) &&
	 			AudioPlayer.mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog.isShowing()	)
	 		{
	 			AudioPlayer.mAudioUrlVerifyTask.mAudioPrepareTask.mPrepareDialog.dismiss();
	 		}
 		}
	 }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	System.out.println(mClassName + " / onSaveInstanceState");
    }
    
	@Override
	public Loader<List<String>> onCreateLoader(int id, Bundle args) 
	{
		// This is called when a new Loader needs to be created. 
		return new NoteListLoader(getActivity());
	}
	
	@Override
	public void onLoadFinished(Loader<List<String>> loader,
							   List<String> data) 
	{
		System.out.println("NoteFragment / _onLoadFinished");
		// Set the new data in the adapter.
		mAdapter.setData(data);

		// The list should now be shown.
		if (isResumed()) 
			setListShown(true);
		else 
			setListShownNoAnimation(true);
		
		fillData();
		
		getLoaderManager().destroyLoader(0); // add for fixing callback twice
	}
	
	@Override
	public void onLoaderReset(Loader<List<String>> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}

	int mFirstVisibleIndex;
	int mFirstVisibleIndexTop;
	/**
	 * fill data
	 */
	public static NoteFragmentAdapter mItemAdapter;
    public void fillData()
    {
    	System.out.println("NoteFragment / _fillData");
    	
    	// save index and top position
//    	int index = mDndListView.getFirstVisiblePosition();
//      View v = mDndListView.getChildAt(0);
//      int top = (v == null) ? 0 : v.getTop();

    	/*
        // set background color of list view
        mDndListView.setBackgroundColor(Util.mBG_ColorArray[mStyle]);

    	//show divider color
        if(mStyle%2 == 0)
	    	mDndListView.setDivider(new ColorDrawable(0xFFffffff));//for dark
        else
          mDndListView.setDivider(new ColorDrawable(0xff000000));//for light

        mDndListView.setDividerHeight(3);
        */
    	int count = mDb_notes.getNotesCount(true);
    	
    	mDb_notes.doOpenNotes();
    	mCursor_notes = DB.mNoteCursor;
        mDb_notes.doCloseNotes();
        
        // set adapter
        String[] from = new String[] { DB.KEY_NOTE_TITLE};
        int[] to = new int[] { R.id.text_whole };
        mItemAdapter = new NoteFragmentAdapter(
				getActivity(),
				R.layout.activity_main_list_row,
				mCursor_notes,
				from,
				to,
				0
				);
        
         mDndListView.setAdapter(mItemAdapter);
        
		// selected list
		for(int i=0; i< count ; i++ )
		{
			mSelectedList.add(true);
			mSelectedList.set(i,true);
		}
		
        // restore index and top position
        mDndListView.setSelectionFromTop(mFirstVisibleIndex, mFirstVisibleIndexTop);
        
        mDndListView.setDropListener(onDrop);
        mDndListView.setDragListener(onDrag);
        mDndListView.setMarkListener(onMark);
        mDndListView.setAudioListener(onAudio);
		mDndListView.setOnScrollListener(onScroll);
        
        setFooter();
    }

    OnScrollListener onScroll = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			System.out.println("_onScrollStateChanged");
	        mFirstVisibleIndex = mDndListView.getFirstVisiblePosition();
	        View v = mDndListView.getChildAt(0);
	        mFirstVisibleIndexTop = (v == null) ? 0 : v.getTop();
            // keep index and top position
	        Util.setPref_lastTimeView_list_view_first_visible_index(getActivity(),mFirstVisibleIndex);
	        Util.setPref_lastTimeView_list_view_first_visible_index_top(getActivity(),mFirstVisibleIndexTop);
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
//			System.out.println("_onScroll / firstVisibleItem " + firstVisibleItem);
//			System.out.println("_onScroll / visibleItemCount " + visibleItemCount);
//			System.out.println("_onScroll / totalItemCount " + totalItemCount);
			
		}
	};

	
	
    // swap rows
	protected static void swapRows(int startPosition, int endPosition) 
	{
		mDb_notes.doOpenNotes();
		mNoteNumber1 = mDb_notes.getNoteId(startPosition,false);
        mNoteTitle1 = mDb_notes.getNoteTitle(startPosition,false);
        mNotePictureUri1 = mDb_notes.getNotePictureUri(startPosition,false);
        mNoteAudioUri1 = mDb_notes.getNoteAudioUri(startPosition,false);
        mNoteLinkUri1 = mDb_notes.getNoteLinkUri(startPosition,false);
        mNoteBodyString1 = mDb_notes.getNoteBody(startPosition,false);
        mMarkingIndex1 = mDb_notes.getNoteMarking(startPosition,false);
    	mCreateTime1 = mDb_notes.getNoteCreatedTime(startPosition,false); 

		mNoteNumber2 = mDb_notes.getNoteId(endPosition,false);
        mNoteTitle2 = mDb_notes.getNoteTitle(endPosition,false);
        mNotePictureUri2 = mDb_notes.getNotePictureUri(endPosition,false);
        mNoteAudioUri2 = mDb_notes.getNoteAudioUri(endPosition,false);
        mNoteLinkUri2 = mDb_notes.getNoteLinkUri(endPosition,false);
        mNoteBodyString2 = mDb_notes.getNoteBody(endPosition,false);
        mMarkingIndex2 = mDb_notes.getNoteMarking(endPosition,false);
    	mCreateTime2 = mDb_notes.getNoteCreatedTime(endPosition,false); 
        mDb_notes.updateNote(mNoteNumber2,
				 mNoteTitle1,
				 mNotePictureUri1,
				 mNoteAudioUri1, 
				 "", //??? TBD
				 mNoteLinkUri1,
				 mNoteBodyString1,
				 mMarkingIndex1,
				 mCreateTime1,false);		        
		
		mDb_notes.updateNote(mNoteNumber1,
		 		 mNoteTitle2,
		 		 mNotePictureUri2,
		 		 mNoteAudioUri2, 
				 "", //??? TBD
				 mNoteLinkUri2,
		 		 mNoteBodyString2,
		 		 mMarkingIndex2,
		 		 mCreateTime2,false);	
		mDb_notes.doCloseNotes();
	}

    // list view listener: on mark
    private DragSortListView.MarkListener onMark =
    new DragSortListView.MarkListener() 
	{   @Override
        public void mark(int position) 
		{
			System.out.println("NoteFragment / _onMark");
			
			mDb_notes.doOpenNotes();
			int count = mDb_notes.getNotesCount(false);
            if(position >= count) //end of list
            {
            	mDb_notes.doCloseNotes();
            	return ;
            }
            
            String strNote = mDb_notes.getNoteTitle(position,false);
            String strPictureUri = mDb_notes.getNotePictureUri(position,false);
            String strAudioUri = mDb_notes.getNoteAudioUri(position,false);
            String strLinkUri = mDb_notes.getNoteLinkUri(position,false);
            String strNoteBody = mDb_notes.getNoteBody(position,false);
            Long idNote =  mDb_notes.getNoteId(position,false);
		
            // toggle the marking
            if(mDb_notes.getNoteMarking(position,false) == 0)                
          	  mDb_notes.updateNote(idNote, strNote, strPictureUri, strAudioUri , "", strLinkUri, strNoteBody , 1, 0,false); //??? TBD
            else
          	  mDb_notes.updateNote(idNote, strNote, strPictureUri, strAudioUri , "", strLinkUri, strNoteBody ,0, 0,false); //??? TBD
            
            mDb_notes.doCloseNotes();
            
            // Stop if unmarked item is at playing state
            if(AudioPlayer.mAudioIndex == position) 
            	UtilAudio.stopAudioIfNeeded();
            
            mItemAdapter.notifyDataSetChanged();
            setFooter();

            if(MainUi.isSameNotesTable())
            	AudioPlayer.prepareAudioInfo(mAct);            
            
            return;
        }
    };    
    
    // list view listener: on audio
    private DragSortListView.AudioListener onAudio = new DragSortListView.AudioListener() 
	{   @Override
        public void audio(int position) 
		{
//			System.out.println("NoteFragment / _onAudio");
			AudioPlayer.mAudioPlayMode = AudioPlayer.CONTINUE_MODE;
			
			mDb_notes.doOpenNotes();
    		int count = mDb_notes.getNotesCount(false);
            if(position >= count) //end of list
            {
            	mDb_notes.doCloseNotes();
            	return ;
            }
    		int marking = mDb_notes.getNoteMarking(position,false);
    		String uriString = mDb_notes.getNoteAudioUri(position,false);
    		mDb_notes.doCloseNotes();

    		boolean isAudioUri = false;
    		if( !Util.isEmptyString(uriString) && (marking == 1))
    			isAudioUri = true;
    		System.out.println("NoteFragment / _onAudio / isAudioUri = " + isAudioUri);

    		boolean itemIsMarked = (marking == 1?true:false);
    		
            if(position < count) // avoid footer error
			{
				if(isAudioUri && itemIsMarked)
				{
					// cancel playing
					if(AudioPlayer.mMediaPlayer != null)
					{
						if(AudioPlayer.mMediaPlayer.isPlaying())
		   			   	{
		   					AudioPlayer.mMediaPlayer.pause();
		   			   	}
						AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunOneTimeMode);     
						AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunContinueMode);
						AudioPlayer.mMediaPlayer.release();
						AudioPlayer.mMediaPlayer = null;
					}
					
					// create new Intent to play audio
					AudioPlayer.mAudioIndex = (int)position;
					AudioPlayer.prepareAudioInfo(mAct);
					AudioPlayer.manageAudioState(mAct);
					setFooter();// call _setFooterMessage at AudioPlayer _onPrepared stage
					
					// update notes table Id
					DrawerActivity.mCurrentPlaying_notesTableId = TabsHostFragment.mCurrent_notesTableId;
					// update playing tab index
					DrawerActivity.mCurrentPlaying_tabIndex = TabsHostFragment.mCurrent_tabIndex;
					// update playing drawer index
				    DrawerActivity.mCurrentPlaying_drawerIndex = DrawerActivity.mFocus_drawerChildPos;
				    // update current audio playing drawer tabs table Id
					DrawerActivity.mCurrentPlaying_drawerTabsTableId = DrawerActivity.mDb.getTabsTableId(DrawerActivity.mCurrentPlaying_drawerIndex);
					
		            mItemAdapter.notifyDataSetChanged();    		
				}
			}
			
            return;
        }
	};            

    static TextView mFooterTextView;
    public static TextView footer_audio_title;
    static TextView footerAudioCurrPlayPosTextView;
    static TextView footerAudioFileLengthTextView;
    static TextView footerAudioNumberTextView;
    public static ImageView footerAudio_playOrPause_button;
    static ImageView footerAudio_playPrevious_button;
    static ImageView footerAudio_playNext_button;
    static View footer_audio_control;
    static String strFooterAudioMessage;
    
	// set footer
    static void setFooter()
    {
    	System.out.println("NoteFragment / _setFooter ");
    	strFooterAudioMessage = null;
	    mFooterTextView.setTextColor(Color.rgb(255,255,255));
	    if(mFooterTextView != null) //add this for avoiding null exception when after e-Mail action
	    {
	    	mFooterTextView.setText(getFooterMessage());
	    }

	    footer_audio_control = mAct.findViewById(R.id.footer_audio_control);
	    footer_audio_control.setVisibility(View.GONE);
	    footer_audio_title = (TextView) mAct.findViewById(R.id.footer_audio_title);
	    footer_audio_title.setVisibility(View.GONE);
	    
	    // init footer audio image
	    footerAudio_playOrPause_button = (ImageView) mAct.findViewById(R.id.footer_img_audio_play);
	    footerAudio_playOrPause_button.setImageResource(R.drawable.ic_audio_selected);
	    footerAudio_playPrevious_button = (ImageView) mAct.findViewById(R.id.footer_img_audio_previous);
	    footerAudio_playPrevious_button.setImageResource(R.drawable.ic_media_previous);
	    footerAudio_playNext_button = (ImageView) mAct.findViewById(R.id.footer_img_audio_next);
	    footerAudio_playNext_button.setImageResource(R.drawable.ic_media_next);
	    footerAudioCurrPlayPosTextView = (TextView) mAct.findViewById(R.id.footer_audio_current_pos);
	    footerAudioFileLengthTextView = (TextView) mAct.findViewById(R.id.footer_audio_file_length);
	    footerAudioNumberTextView = (TextView) mAct.findViewById(R.id.footer_audio_number);
	    
	    // init audio seek bar
		seekBarProgress = (SeekBar)mAct.findViewById(R.id.footer_img_audio_seek_bar);	
		seekBarProgress.setMax(99); // It means 100% .0-99
		seekBarProgress.setProgress(mProgress);
		
		// set seek bar listener
		seekBarProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
		{
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) 
			{
				if( AudioPlayer.mMediaPlayer != null  )
				{
					int mPlayAudioPosition = (int) (((float)(mediaFileLength_MilliSeconds / 100)) * seekBar.getProgress());
					AudioPlayer.mMediaPlayer.seekTo(mPlayAudioPosition);
				}
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				if(fromUser)
				{	
					// show progress change
			    	int currentPos = mediaFileLength_MilliSeconds*progress/(seekBar.getMax()+1);
			    	int curHour = Math.round((float)(currentPos / 1000 / 60 / 60));
			    	int curMin = Math.round((float)((currentPos - curHour * 60 * 60 * 1000) / 1000 / 60));
			     	int curSec = Math.round((float)((currentPos - curHour * 60 * 60 * 1000 - curMin * 60 * 1000)/ 1000));
	
			    	// set current play time
			    	footerAudioCurrPlayPosTextView.setText(String.format("%2d", curHour)+":" +
			    										   String.format("%02d", curMin)+":" +
			    										   String.format("%02d", curSec) );
				}
			}
		});

        // for showing footer when playing audio
        if( (AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP) &&
        	!Util.isEmptyString(AudioPlayer.mAudioStr)                  )
        {
    		setFooterAudioControl(AudioPlayer.mAudioStr);
        }
    }
    
    static String getFooterMessage()
    {
    	String str = mAct.getResources().getText(R.string.footer_checked).toString() + 
			        "/" +
			        mAct.getResources().getText(R.string.footer_total).toString() +
				       ": " +
			        mDb_notes.getCheckedNotesCount() + 
					   "/" +
					mDb_notes.getNotesCount(true);
    	return str;
    }
    
    public static void setFooterAudioControl(String string)
    {
    	// show audio icon
    	footer_audio_control.setVisibility(View.VISIBLE);
    	footer_audio_title.setVisibility(View.VISIBLE);
    	
    	// seek bar behavior is not like other control item
    	//, it is seen when changing drawer, so set invisible at xml 
    	seekBarProgress.setVisibility(View.VISIBLE); 

    	// show audio file length of playing
     	int fileHour = Math.round((float)(mediaFileLength_MilliSeconds / 1000 / 60 / 60));
     	int fileMin = Math.round((float)((mediaFileLength_MilliSeconds - fileHour * 60 * 60 * 1000) / 1000 / 60));
    	int fileSec = Math.round((float)((mediaFileLength_MilliSeconds - fileHour * 60 * 60 * 1000 - fileMin * 1000 * 60 )/ 1000));
    	footerAudioFileLengthTextView.setText(String.format("%2d", fileHour)+":" +
    										  String.format("%02d", fileMin)+":" +
    										  String.format("%02d", fileSec));
    	
    	// show playing item
    	strFooterAudioMessage =	mAct.getResources().getString(R.string.menu_button_play) +
			    				"#" +
			    				(AudioPlayer.mAudioIndex +1);	    	
    	
    	footerAudioNumberTextView.setText(strFooterAudioMessage);
    	
	    // set footer message with audio name
    	footer_audio_title.setText(Util.getDisplayNameByUriString(string, mAct));
		// set marquee
    	footer_audio_title.setSelected(true);
		
		// update status
	    UtilAudio.updateFooterAudioState(footerAudio_playOrPause_button,footer_audio_title);
	    
	    // set audio play and pause control image
	    footerAudio_playOrPause_button.setOnClickListener(new View.OnClickListener() 
	    {
			@Override
			public void onClick(View v) 
			{
				AudioPlayer.manageAudioState(DrawerActivity.mDrawerActivity);
				// update status
			    UtilAudio.updateFooterAudioState(footerAudio_playOrPause_button,footer_audio_title);
			}
		});
	    
	    // play previous audio 
	    footerAudio_playPrevious_button.setOnClickListener(new View.OnClickListener() 
	    {
			@Override
			public void onClick(View v) 
			{
				AudioPlayer.willPlayNext = false;
				AudioPlayer.stopAudio();
				AudioPlayer.mAudioIndex--;
	   			if( AudioPlayer.mAudioIndex < 0)
	   				AudioPlayer.mAudioIndex++; //back to first index
				
	   			AudioPlayer.manageAudioState(DrawerActivity.mDrawerActivity);
				
				// update status
			    UtilAudio.updateFooterAudioState(footerAudio_playOrPause_button,footer_audio_title);
			}
		});
	    
	    // play next audio
	    footerAudio_playNext_button.setOnClickListener(new View.OnClickListener() 
	    {
			@Override
			public void onClick(View v) 
			{
				AudioPlayer.willPlayNext = true;
				AudioPlayer.stopAudio();
				AudioPlayer.mAudioIndex++;
	   			if( AudioPlayer.mAudioIndex >= AudioInfo.getAudioList().size())
	   				AudioPlayer.mAudioIndex = 0; //back to first index
	   			
	   			AudioPlayer.manageAudioState(DrawerActivity.mDrawerActivity);
				// update status
			    UtilAudio.updateFooterAudioState(footerAudio_playOrPause_button,footer_audio_title);
			}
		});	    
    }
	
	/*******************************************
	 * 					menu
	 *******************************************/
    // Menu identifiers
    static final int CHECK_ALL = R.id.CHECK_ALL;
    static final int UNCHECK_ALL = R.id.UNCHECK_ALL;
    static final int INVERT_SELECTED = R.id.INVERT_SELECTED;
    static final int MOVE_CHECKED_NOTE = R.id.MOVE_CHECKED_NOTE;
    static final int COPY_CHECKED_NOTE = R.id.COPY_CHECKED_NOTE;
    static final int MAIL_CHECKED_NOTE = R.id.MAIL_CHECKED_NOTE;
    static final int DELETE_CHECKED_NOTE = R.id.DELETE_CHECKED_NOTE;
    
	int noteCount;
    
    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) 
        {
	        case CHECK_ALL:
	        	checkAll(1); 
	            return true;
	        case UNCHECK_ALL:
	        	checkAll(0); 
	            return true;
	        case INVERT_SELECTED:
	        	invertSelected(); 
	            return true;
	        case MOVE_CHECKED_NOTE:
	        case COPY_CHECKED_NOTE:
	    		if(!noItemChecked())
	        	{
	    			int count = mDb_notes.getCheckedNotesCount();
		    		String copyItems[] = new String[count];
		    		String copyItemsPicture[] = new String[count];
		    		String copyItemsLink[] = new String[count];
		    		String copyItemsAudio[] = new String[count];
		    		String copyItemsBody[] = new String[count];
		    		Long copyItemsTime[] = new Long[count];
		    		int cCopy = 0;
		    		
		    		mDb_notes.doOpenNotes();
		    		int noteCount = mDb_notes.getNotesCount(false);
		    		for(int i=0; i<noteCount; i++)
		    		{
		    			if(mDb_notes.getNoteMarking(i,false) == 1)
		    			{
		    				copyItems[cCopy] = mDb_notes.getNoteTitle(i,false);
		    				copyItemsPicture[cCopy] = mDb_notes.getNotePictureUri(i,false);
		    				copyItemsLink[cCopy] = mDb_notes.getNoteLinkUri(i,false);
		    				copyItemsAudio[cCopy] = mDb_notes.getNoteAudioUri(i,false);
		    				copyItemsBody[cCopy] = mDb_notes.getNoteBody(i,false);
		    				copyItemsTime[cCopy] = mDb_notes.getNoteCreatedTime(i,false);
		    				cCopy++;
		    			}
		    		}
		    		mDb_notes.doCloseNotes();
		           
		    		if(item.getItemId() == MOVE_CHECKED_NOTE)
		    			operateCheckedTo(copyItems, copyItemsPicture, copyItemsLink, copyItemsAudio, copyItemsBody, copyItemsTime, MOVE_TO); // move to
		    		else if(item.getItemId() == COPY_CHECKED_NOTE)
			    		operateCheckedTo(copyItems, copyItemsPicture, copyItemsLink, copyItemsAudio, copyItemsBody, copyItemsTime, COPY_TO);// copy to
		    			
	        	}
	        	else
	    			Toast.makeText(getActivity(),
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT)
					     .show();
	            return true;
	            
	        case MAIL_CHECKED_NOTE:
	    		if(!noItemChecked())
	        	{
		        	// set Sent string Id
					List<Long> rowArr = new ArrayList<Long>();
					List<String> pictureFileNameList = new ArrayList<String>();
	            	int j=0;
	            	mDb_notes.doOpenNotes();
	            	int count = mDb_notes.getNotesCount(false);
		    		for(int i=0; i<count; i++)
		    		{
		    			if(mDb_notes.getNoteMarking(i,false) == 1)
		    			{
		    				rowArr.add(j,(long) mDb_notes.getNoteId(i,false));
		    				j++;
		    				
		    				String picFile = mDb_notes.getNotePictureUriById((long) mDb_notes.getNoteId(i,false),false,false);
		    				if((picFile != null) && (picFile.length() > 0))
		    					pictureFileNameList.add(picFile);
		    			}
		    		}
		    		mDb_notes.doCloseNotes();
		    		
		    		// message
		    		Intent intentMail = new Intent(getActivity(), SendMailAct.class);
		    		String extraStr = Util.getStringWithXmlTag(rowArr);
		    		extraStr = Util.addXmlTag(extraStr);
		    		intentMail.putExtra("SentString", extraStr);
		    		
		    		// picture array
		    		int cnt = pictureFileNameList.size();
		    		String pictureFileNameArr[] = new String[cnt];
		    		for(int i=0; i < cnt ; i++ )
		    		{
		    			pictureFileNameArr[i] = pictureFileNameList.get(i);
		    		}
		    		intentMail.putExtra("SentPictureFileNameArray", pictureFileNameArr );
		    		
					startActivity(intentMail);
	        	}
	        	else
	    			Toast.makeText(getActivity(),
							   R.string.delete_checked_no_checked_items,
							   Toast.LENGTH_SHORT)
						 .show();
	        	return true;
	        	
	        case DELETE_CHECKED_NOTE:
	        	if(!noItemChecked())
	        		deleteCheckedNotes();
	        	else
	    			Toast.makeText(getActivity(),
	    						   R.string.delete_checked_no_checked_items,
	    						   Toast.LENGTH_SHORT)
	    				 .show();
	            return true;     

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	
	static public void swap()
	{
        int startCursor = mDb_notes.getNotesCount(true)-1;
        int endCursor = 0;
		
		//reorder data base storage for ADD_NEW_TO_TOP option
		int loop = Math.abs(startCursor-endCursor);
		for(int i=0;i< loop;i++)
		{
			swapRows(startCursor,endCursor);
			if((startCursor-endCursor) >0)
				endCursor++;
			else
				endCursor--;
		}
	}
    
	/**
	 *  check all or uncheck all
	 */
	public void checkAll(int action) 
	{
		boolean bStopAudio = false;
		mDb_notes.doOpenNotes();
		int count = mDb_notes.getNotesCount(false);
		for(int i=0; i<count; i++)
		{
			Long rowId = mDb_notes.getNoteId(i,false);
			String noteTitle = mDb_notes.getNoteTitle(i,false);
			String pictureUri = mDb_notes.getNotePictureUri(i,false);
			String audioUri = mDb_notes.getNoteAudioUri(i,false);
			String linkUri = mDb_notes.getNoteLinkUri(i,false);
			String noteBody = mDb_notes.getNoteBody(i,false);
			mDb_notes.updateNote(rowId, noteTitle, pictureUri, audioUri, "", linkUri, noteBody , action, 0,false);// action 1:check all, 0:uncheck all
	        // Stop if unmarked item is at playing state
	        if((AudioPlayer.mAudioIndex == i) && (action == 0) ) 
	        	bStopAudio = true;		
		}
		mDb_notes.doCloseNotes();
		
		if(bStopAudio)
			UtilAudio.stopAudioIfNeeded();	
		
		// update audio play list
        if(MainUi.isSameNotesTable())
        	AudioPlayer.prepareAudioInfo(mAct);
        
		mItemAdapter.notifyDataSetChanged();
		setFooter();
	}
	
	/**
	 *  Invert Selected
	 */
	public void invertSelected() 
	{
		boolean bStopAudio = false;
		mDb_notes.doOpenNotes();
		int count = mDb_notes.getNotesCount(false);
		for(int i=0; i<count; i++)
		{
			Long rowId = mDb_notes.getNoteId(i,false);
			String noteTitle = mDb_notes.getNoteTitle(i,false);
			String pictureUri = mDb_notes.getNotePictureUri(i,false);
			String audioUri = mDb_notes.getNoteAudioUri(i,false);
			String linkUri = mDb_notes.getNoteLinkUri(i,false);
			String noteBody = mDb_notes.getNoteBody(i,false);
			long marking = (mDb_notes.getNoteMarking(i,false)==1)?0:1;
			mDb_notes.updateNote(rowId, noteTitle, pictureUri, audioUri, "", linkUri, noteBody , marking, 0,false);// action 1:check all, 0:uncheck all
	        // Stop if unmarked item is at playing state
	        if((AudioPlayer.mAudioIndex == i) && (marking == 0) ) 
	        	bStopAudio = true;			
		}
		mDb_notes.doCloseNotes();
		
		if(bStopAudio)
			UtilAudio.stopAudioIfNeeded();	
		
		// update audio play list
        if(MainUi.isSameNotesTable())
        	AudioPlayer.prepareAudioInfo(mAct);
		
		mItemAdapter.notifyDataSetChanged();
		setFooter();
	}	
	
	
    /**
     *   operate checked to: move to, copy to
     * 
     */
	void operateCheckedTo(final String[] copyItems, final String[] copyItemsPicture, final String[] copyItemsLink, 
						  final String[] copyItemsAudio, final String[] copyItemsBody,
						  final Long[] copyItemsTime, final int action)
	{
		//list all tabs
		int tabsTableId = Util.getPref_lastTimeView_tabs_tableId(getActivity());
		mDb_notes.doOpenTabs(tabsTableId);
		int tabCount = mDb_notes.getTabsCount(false);
		final String[] tabNames = new String[tabCount];
		final int[] tableIds = new int[tabCount];
		for(int i=0;i<tabCount;i++)
		{
			tabNames[i] = mDb_notes.getTabTitle(i,false);
			tableIds[i] = mDb_notes.getNotesTableId(i,false);
		}
		mDb_notes.doCloseTabs();
		
		tabNames[TabsHostFragment.mCurrent_tabIndex] = tabNames[TabsHostFragment.mCurrent_tabIndex] + " *"; // add mark to current page 
		   
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//keep original table id
				String curTableNum = DB.getFocus_notes_tableId();

				//copy checked item to destination tab
				String destTableNum = String.valueOf(tableIds[which]);
				DB.setFocus_notes_tableId(destTableNum);
				for(int i=0;i< copyItems.length;i++)
				{
					int marking = 0;
					// default marking of picture or audio is 1
					if( (!Util.isEmptyString(copyItemsPicture[i])) || (!Util.isEmptyString(copyItemsAudio[i])))
						marking = 1;
					
					mDb_notes.insertNote(copyItems[i],copyItemsPicture[i], copyItemsAudio[i], "", copyItemsLink[i], copyItemsBody[i],marking, copyItemsTime[i]); //??? TBD
				}
				
				//recover to original table id
				if(action == MOVE_TO)
				{
					DB.setFocus_notes_tableId(curTableNum);
					mDb_notes.doOpenNotes();
					int count = mDb_notes.getNotesCount(false);
					//delete checked
					for(int i=0; i<count; i++)
					{
						if(mDb_notes.getNoteMarking(i,false) == 1)
						{
							mDb_notes.deleteNote(mDb_notes.getNoteId(i,false),false);
							// update playing highlight
							UtilAudio.stopAudioIfNeeded();
						}
					}
					mDb_notes.doCloseNotes();
					
					mItemAdapter.notifyDataSetChanged();
					setFooter();
				}
				else if(action == COPY_TO)
				{
					DB.setFocus_notes_tableId(curTableNum);
					if(destTableNum.equalsIgnoreCase(curTableNum))
					{
						mItemAdapter.notifyDataSetChanged();
						setFooter();
					}
				}
				
				dialog.dismiss();
			}
		};
		
		if(action == MOVE_TO)
			builder.setTitle(R.string.checked_notes_move_to_dlg);
		else if(action == COPY_TO)
			builder.setTitle(R.string.checked_notes_copy_to_dlg);
		
		builder.setSingleChoiceItems(tabNames, -1, listener)
		  	.setNegativeButton(R.string.btn_Cancel, null);
		
		// override onShow to mark current page status
		AlertDialog alertDlg = builder.create();
		alertDlg.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dlgInterface) {
				// add mark for current page
				Util util = new Util(getActivity());
				util.addMarkToCurrentPage(dlgInterface);
			}
		});
		alertDlg.show();
	}
	
	
	/**
	 * delete checked notes
	 */
	public void deleteCheckedNotes()
	{
		final Context context = getActivity();

		mPref_delete_warn = context.getSharedPreferences("delete_warn", 0);
    	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
           mPref_delete_warn.getString("KEY_DELETE_CHECKED_WARN","yes").equalsIgnoreCase("yes"))
    	{
			Util util = new Util(getActivity());
			util.vibrate();
    		
    		// show warning dialog
			Builder builder = new Builder(context);
			builder.setTitle(R.string.delete_checked_note_title)
					.setMessage(R.string.delete_checked_message)
					.setNegativeButton(R.string.btn_Cancel, 
							new OnClickListener() 
					{	@Override
						public void onClick(DialogInterface dialog, int which) 
						{/*cancel*/} })
					.setPositiveButton(R.string.btn_OK, 
							new OnClickListener() 
					{	@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							mDb_notes.doOpenNotes();
							int count = mDb_notes.getNotesCount(false);
							for(int i=0; i<count; i++)
							{
								if(mDb_notes.getNoteMarking(i,false) == 1)
									mDb_notes.deleteNote(mDb_notes.getNoteId(i,false),false);
							}
							mDb_notes.doCloseNotes();
							
							// Stop Play/Pause if current tab's item is played and is not at Stop state
							if(AudioPlayer.mAudioIndex == NoteFragment.mHighlightPosition)
								UtilAudio.stopAudioIfNeeded();
							
							mItemAdapter.notifyDataSetChanged();
							setFooter();
						}
					});
			
	        AlertDialog d = builder.create();
	        d.show();
    	}
    	else
    	{
    		// not show warning dialog
    		mDb_notes.doOpenNotes();
    		int count = mDb_notes.getNotesCount(false);
			for(int i=0; i<count; i++)
			{
				if(mDb_notes.getNoteMarking(i,false) == 1)
					mDb_notes.deleteNote(mDb_notes.getNoteId(i,false),false);
			}
			mDb_notes.doCloseNotes();
			
			mItemAdapter.notifyDataSetChanged();
			setFooter();
    	}
	}
    
	@Override
	public void onDestroy() {
		mDb_notes.doCloseNotes();
		super.onDestroy();
	}
	
	boolean noItemChecked()
	{
		int checkedItemCount = mDb_notes.getCheckedNotesCount(); 
		return (checkedItemCount == 0);
	}
	
	/*
	 * inner class for note list loader
	 */
	public static class NoteListLoader extends AsyncTaskLoader<List<String>> 
	{
		List<String> mApps;

		public NoteListLoader(Context context) {
			super(context);

		}

		@Override
		public List<String> loadInBackground() {
			List<String> entries = new ArrayList<String>();
			return entries;
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}
	}

	/*
	 * 	inner class for note list adapter
	 */
	public static class NoteListAdapter extends ArrayAdapter<String> 
	{
		public NoteListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}
		public void setData(List<String> data) {
			clear();
			if (data != null) {		
					addAll(data);
			}
		}
	}

	
	// set seek bar progress
	public static int mProgress;
    public static void primaryAudioSeekBarProgressUpdater() 
    {
    	int currentPos = AudioPlayer.mMediaPlayer.getCurrentPosition();
    	int curHour = Math.round((float)(currentPos / 1000 / 60 / 60));
    	int curMin = Math.round((float)((currentPos - curHour * 60 * 60 * 1000) / 1000 / 60));
     	int curSec = Math.round((float)((currentPos - curHour * 60 * 60 * 1000 - curMin * 60 * 1000)/ 1000));
    	// set current play time and the play length of audio file
    	footerAudioCurrPlayPosTextView.setText(String.format("%2d", curHour)+":" +
    										   String.format("%02d", curMin)+":" +
    										   String.format("%02d", curSec) );//??? why affect audio title?
    	mProgress = (int)(((float)currentPos/mediaFileLength_MilliSeconds)*100);
    	seekBarProgress.setProgress(mProgress); // This math construction give a percentage of "was playing"/"song length"
    }
    
    
}
