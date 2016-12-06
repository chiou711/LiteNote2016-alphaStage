package com.cwc.litenote;

import java.util.ArrayList;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.util.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

public class TabsHostFragment extends Fragment 
{
    static FragmentTabHost mTabHost; 
    static int mTabCount;
	static String TAB_SPEC_PREFIX = "tab";
	static String TAB_SPEC;
	static String mClassName;
	boolean bTabNameByDefault = true; 
	// for DB
	public static DB mDbTabs;
	private static Cursor mTabCursor;
	
	static SharedPreferences mPref_FinalPageViewed;
	private static SharedPreferences mPref_delete_warn;
	public static int mFinalPageViewed_TabIndex;
	public static int mCurrent_tabIndex;
	public static int mCurrent_notesTableId;
	static ArrayList<String> mTabIndicator_ArrayList = new ArrayList<String>();
	static int mFirstExist_TabId =0;
	static int mLastExist_TabId =0;
	static int mLastExist_notesTableId;
	static HorizontalScrollView mHorScrollView;
    public static Activity mAct;

    public TabsHostFragment(){};
    
	@Override
	public void onCreate(final Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        // get final viewed table Id
		mAct = getActivity();
		String tableId = Util.getPref_lastTimeView_notes_tableId(mAct);
		mClassName = getClass().getSimpleName();
//        System.out.println("TabsHostFragment / onCreate / strFinalPageViewed_tableId = " + tableId);
        System.out.println(mClassName + " / onCreate / strFinalPageViewed_tableId = " + tableId);
    } 

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	System.out.println("TabsHostFragment / _onCreateView");
    	
    	View rootView = inflater.inflate(R.layout.activity_main, container, false);
        setRootView(rootView);
        
        if(savedInstanceState == null)
        {
        	mDbTabs = new DB(mAct,Util.getPref_lastTimeView_tabs_tableId(mAct));
        	mDbTabs.initTabsDb(mDbTabs);
        }
        
        setTabHost();
        setTab(mAct);
        
        return rootView;
    }	
	
	@Override
	public void onResume() {
		super.onResume();
		System.out.println("TabsHostFragment / onResume");
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
//		System.out.println("TabsHostFragment / onPause");		
		if( (mTabHost != null) && DrawerActivity.bEnableConfig)
			mTabHost.clearAllTabs(); // workaround: clear for changing to Config
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
//		System.out.println("TabsHostFragment / onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onStop() {
//		System.out.println("TabsHostFragment / onStop");
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
//		System.out.println("TabsHostFragment / onDestroy");
		if(mTabHost != null)
			mTabHost.clearAllTabs(); // clear for changing drawer
	}
    
    static View mRootView;
	private void setRootView(View rootView) {
		mRootView = rootView;
	}
	
	private static View getRootView()
	{
		return mRootView;
	}

	
	/**
	 * set tab host
	 * 
	 */
	protected void setTabHost()
	{
		// declare tab widget
        TabWidget tabWidget = (TabWidget) getRootView().findViewById(android.R.id.tabs);
        
        // declare linear layout
        LinearLayout linearLayout = (LinearLayout) tabWidget.getParent();
        
        // set horizontal scroll view
        HorizontalScrollView horScrollView = new HorizontalScrollView(mAct);
        horScrollView.setLayoutParams(new FrameLayout.LayoutParams(
								            FrameLayout.LayoutParams.MATCH_PARENT,
								            FrameLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.addView(horScrollView, 0);
        linearLayout.removeView(tabWidget);
        
        horScrollView.addView(tabWidget);
        horScrollView.setHorizontalScrollBarEnabled(true); //set scroll bar
        horScrollView.setHorizontalFadingEdgeEnabled(true); // set fading edge
        mHorScrollView = horScrollView;

		// tab host
        mTabHost = (FragmentTabHost)getRootView().findViewById(android.R.id.tabhost);
        
        //for android-support-v4.jar
        //mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent); 
        
        //add frame layout for android-support-v13.jar
        //Note: must use getChildFragmentManager() for nested fragment
        mTabHost.setup(mAct, getChildFragmentManager(), android.R.id.tabcontent);
	}
	
	static void setTab(Activity activity)
	{
//		System.out.println("TabsHostFragment/ _setTab");
        //set tab indicator
    	setTabIndicator(activity);
    	
    	// set tab listener
    	setTabChangeListener(activity);
    	setTabEditListener(activity);
	}
	
	/**
	 * set tab indicator
	 * 
	 */
	protected static void setTabIndicator(final Activity activity)
	{
		int tabsTableId = DB.getFocus_tabsTableId();
		System.out.println("TabsHostFragment / setTabIndicator / drawer_tabsTableId = " + tabsTableId);
		
		// get final viewed table Id
        String tblId = Util.getPref_lastTimeView_notes_tableId(activity);
		
		mDbTabs.doOpenTabs(tabsTableId);
		mTabCount = mDbTabs.getTabsCount(false);
		System.out.println("--- TabsHostFragment / setTabIndicator / mTabCount = " + mTabCount);

		// get first tab id and last tab id
		int i = 0;
		while(i < mTabCount)
    	{
    		mTabIndicator_ArrayList.add(i,mDbTabs.getTabTitle(i, false));  //??? Why rotate will cause exception
    		
    		int tabId = mDbTabs.getTabId(i, false);
    		
    		mTabCursor = mDbTabs.getTabsCursor();
    		mTabCursor.moveToPosition(i);
    		
			if(mTabCursor.isFirst())
			{
				mFirstExist_TabId = tabId ;//???
			}
			
			if(mTabCursor.isLast())
			{
				mLastExist_TabId = tabId ;
			}
			i++;
    	}
    	
		mLastExist_notesTableId = 0;
		// get final view table id of last time
		for(int iPosition =0; iPosition<mTabCount; iPosition++)
		{
			int notesTableId = mDbTabs.getNotesTableId(iPosition,false);
			if(Integer.valueOf(tblId) == notesTableId)
			{
				mFinalPageViewed_TabIndex = iPosition;	// starts from 0
			}
			
			if( notesTableId >= mLastExist_notesTableId)
				mLastExist_notesTableId = notesTableId;
		}
		mDbTabs.doCloseTabs();
		
		System.out.println("TabsHostFragment / mLastExist_notesTableId = " + mLastExist_notesTableId);
		
    	//add tab
//        mTabHost.getTabWidget().setStripEnabled(true); // enable strip
        i = 0;
        while(i < mTabCount)
        {
            TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(mDbTabs.getTabId(i,true)));
//        	System.out.println(mClassName + " / addTab / " + i);
            mTabHost.addTab(mTabHost.newTabSpec(TAB_SPEC)
									.setIndicator(mTabIndicator_ArrayList.get(i)),
							NoteFragment.class, //interconnection
							null);
            
            //set round corner and background color
            int style = mDbTabs.getTabStyle(i, true);
    		switch(style)
    		{
    			case 0:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_0);
    				break;
    			case 1:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_1);
    				break;
    			case 2:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_2);
    				break;
    			case 3:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_3);
    				break;
    			case 4:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_4);
    				break;	
    			case 5:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_5);
    				break;	
    			case 6:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_6);
    				break;	
    			case 7:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_7);
    				break;	
    			case 8:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_8);
    				break;		
    			case 9:
    				mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.bg_9);
    				break;		
    			default:
    				break;
    		}
    		
            //set text color
	        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
		    if((style%2) == 1)
    		{	
		        tv.setTextColor(Color.argb(255,0,0,0));
    		}
           	else
           	{
		        tv.setTextColor(Color.argb(255,255,255,255));
           	}
            // set tab text center
	    	int tabCount = mTabHost.getTabWidget().getTabCount();
	    	for (int j = 0; j < tabCount; j++) {
	    	    final View view = mTabHost.getTabWidget().getChildTabViewAt(j);
	    	    if ( view != null ) {
	    	        //  get title text view
	    	        final View textView = view.findViewById(android.R.id.title);
	    	        if ( textView instanceof TextView ) {
	    	            ((TextView) textView).setGravity(Gravity.CENTER);
	    	            ((TextView) textView).setSingleLine(true);
	    	            ((TextView) textView).setPadding(6, 0, 6, 0);
	    	            ((TextView) textView).setMinimumWidth(96);
	    	            ((TextView) textView).setMaxWidth(UtilImage.getScreenWidth(activity)/2);
	    	            textView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
	    	        }
	    	    }
	    	}
	    	i++;
        }
        
        setTabMargin(activity);

		mCurrent_tabIndex = mFinalPageViewed_TabIndex;
		
		System.out.println("TabsHostFragment / setTabIndicator / mCurrentTabIndex = " + mCurrent_tabIndex);
		
		//set background color to selected tab 
		mTabHost.setCurrentTab(mCurrent_tabIndex); 
        
		// scroll to last view
        mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
		        mPref_FinalPageViewed = activity.getSharedPreferences("last_time_view", 0);
		        int scrollX = Util.getPref_lastTimeView_scrollX_byDrawerNumber(activity);
	        	mHorScrollView.scrollTo(scrollX, 0);
	            updateTabSpec(mTabHost.getCurrentTabTag(),activity);
	        } 
	    });
        
	}
	
	public static void setAudioPlayingTab_WithHighlight(boolean highlightIsOn)
	{
		// get first tab id and last tab id
		int tabCount = mTabHost.getTabWidget().getTabCount();
		for (int i = 0; i < tabCount; i++)	
		{
	        TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			if(highlightIsOn && (DrawerActivity.mCurrentPlaying_tabIndex == i))
			    tv.setTextColor(Color.argb(0xff,0xff,0x80,0x00));			
			else
			{
		        int style = mDbTabs.getTabStyle(i, true);
			    if((style%2) == 1)
				{	
			        tv.setTextColor(Color.argb(255,0,0,0));
				}
		       	else
		       	{
			        tv.setTextColor(Color.argb(255,255,255,255));
		       	}
			}
		}
	}

	static void setTabMargin(Activity activity)
	{
    	mTabHost.getTabWidget().setShowDividers(TabWidget.SHOW_DIVIDER_MIDDLE);
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.ic_tab_divider);
    	
        TabWidget tabWidget = (TabWidget) getRootView().findViewById(android.R.id.tabs);
        
        LinearLayout.LayoutParams tabWidgetLayout;
        for (int j = 0; j < mTabCount; j++) 
        {
        	tabWidgetLayout = (LinearLayout.LayoutParams) tabWidget.getChildAt(j).getLayoutParams();
        	int oriLeftMargin = tabWidgetLayout.leftMargin;
        	int oriRightMargin = tabWidgetLayout.rightMargin;
        	
        	// fix right edge been cut issue when single one note
        	if(mTabCount == 1)
        		oriRightMargin = 0;
        	
        	if (j == 0) {
        		tabWidgetLayout.setMargins(0, 2, oriRightMargin, 5);
        	} else if (j == (mTabCount - 1)) {
        		tabWidgetLayout.setMargins(oriLeftMargin, 2, 0, 5);
        	} else {
        		tabWidgetLayout.setMargins(oriLeftMargin, 2, oriRightMargin, 5);
        	}
        }
        tabWidget.requestLayout();
	}
	
	
	/**
	 * set tab change listener
	 * 
	 */
	static String mTabSpec;
	protected static void setTabChangeListener(final Activity activity)
	{
        // set on tab changed listener
	    mTabHost.setOnTabChangedListener(new OnTabChangeListener()
	    {
			@Override
			public void onTabChanged(String tabSpec)
			{
				System.out.println(mClassName + " / onTabChanged");
				mTabSpec = tabSpec;
				updateTabSpec(tabSpec,activity);
			}
		}
	    );    
	}
	
	static void updateTabSpec(String tabSpec,Activity activity)
	{
		System.out.println("TabsHostFragment / _updateTabSpec");
		// get scroll X
		int scrollX = mHorScrollView.getScrollX();
		
		//update final page currently viewed: scroll x
        mPref_FinalPageViewed = activity.getSharedPreferences("last_time_view", 0);
    	Util.setPref_lastTimeView_scrollX_byDrawerNumber(activity, scrollX );
		
    	mDbTabs.doOpenTabs(Util.getPref_lastTimeView_tabs_tableId(mAct));
		int tabsCount = mDbTabs.getTabsCount(false);
		for(int i=0;i<tabsCount;i++)
		{
			int iTabId = mDbTabs.getTabId(i, false);
			int notesTableId = mDbTabs.getNotesTableId(i, false);
			TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(iTabId)); // TAB_SPEC starts from 1
	    	
			if(TAB_SPEC.equals(tabSpec) )
	    	{
	    		mCurrent_tabIndex = i;
	    		//update final page currently viewed: tab Id
	    		Util.setPref_lastTimeView_notes_tableId(activity,notesTableId);

				// get current playing notes table Id
				mCurrent_notesTableId = Integer.valueOf(Util.getPref_lastTimeView_notes_tableId(activity));
	    		DB.setFocus_notes_tableId(String.valueOf(notesTableId));
	    		System.out.println(mClassName + " / _updateTabSpec / tabSpec = " + tabSpec);
	    	} 
		}
		mDbTabs.doCloseTabs();
		
    	// set current audio playing tab with highlight
		if( (AudioPlayer.mMediaPlayer != null) &&
			(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP)&&
		    (DrawerActivity.mCurrentPlaying_drawerIndex == DrawerActivity.mFocus_drawerChildPos))	
			setAudioPlayingTab_WithHighlight(true);
		else
			setAudioPlayingTab_WithHighlight(false);
	}
	
	/**
	 * set tab Edit listener
	 * @param activity 
	 * 
	 */
	protected static void setTabEditListener(final Activity activity)
	{
	    // set listener for editing tab info
	    int i = 0;
	    while(i < mTabCount)
		{
			final int tabCursor = i;
			View tabView= mTabHost.getTabWidget().getChildAt(i);
			
			// on long click listener
			tabView.setOnLongClickListener(new OnLongClickListener() 
	    	{	
				@Override
				public boolean onLongClick(View v) 
				{
					editTab(tabCursor, activity);
					return true;
				}
			});
			i++;
		}
	}
	
	/**
	 * delete page
	 * 
	 */
	public static  void deletePage(int TabId, final Activity activity) 
	{
		mDbTabs.doOpenTabs(Util.getPref_lastTimeView_tabs_tableId(activity));
		// check if only one page left
		int tabsCount = mDbTabs.getTabsCount(false);
		if(tabsCount != 1)
		{
			final int tabId =  mDbTabs.getTabId(mCurrent_tabIndex, false);
			//if current page is the first page and will be delete,
			//try to get next existence of note page
			System.out.println("deletePage / mCurrentTabIndex = " + mCurrent_tabIndex);
			System.out.println("deletePage / mFirstExist_TabId = " + mFirstExist_TabId);
	        if(tabId == mFirstExist_TabId)
	        {
	        	int cGetNextExistIndex = mCurrent_tabIndex+1;
	        	boolean bGotNext = false;
				while(!bGotNext){
		        	try{
		        	   	mFirstExist_TabId =  mDbTabs.getTabId(cGetNextExistIndex, false);
		        		bGotNext = true;
		        	}catch(Exception e){
    		        	 bGotNext = false;
    		        	 cGetNextExistIndex++;}}		            		        	
	        }
            
	        //change to first existing page
	        int newFirstNotesTblId = 0;
	        for(int i=0 ; i<tabsCount; i++)
	        {
	        	if(	mDbTabs.getTabId(i, false)== mFirstExist_TabId)
	        	{
	        		newFirstNotesTblId =  mDbTabs.getNotesTableId(i, false);
	    			System.out.println("deletePage / newFirstNotesTblId = " + newFirstNotesTblId);
	        	}
	        }
	        System.out.println("--- after delete / newFirstNotesTblId = " + newFirstNotesTblId);
	        Util.setPref_lastTimeView_notes_tableId(activity, newFirstNotesTblId);
		}
		else{
             Toast.makeText(activity, R.string.toast_keep_one_page , Toast.LENGTH_SHORT).show();
             return;
		}
		mDbTabs.doCloseTabs();
		
		// set scroll X
		int scrollX = 0; //over the last scroll X
        mPref_FinalPageViewed = activity.getSharedPreferences("last_time_view", 0);
    	Util.setPref_lastTimeView_scrollX_byDrawerNumber(activity, scrollX );
	 	  
		
		// get notes table Id for dropping
		int notesTableId = mDbTabs.getNotesTableId(mCurrent_tabIndex, true);
		System.out.println("TabsHostFragment / _deletePage / notesTableId =  " + notesTableId);
		
 	    // delete tab name
		mDbTabs.dropNotesTable(notesTableId);
		mDbTabs.deleteTab(DB.getFocusTabsTableName(),TabId);
		mTabCount--;
		
		// After Delete page, update highlight tab
    	if(mCurrent_tabIndex < DrawerActivity.mCurrentPlaying_tabIndex)
    	{
    		DrawerActivity.mCurrentPlaying_tabIndex--;
    	}
    	else if(mCurrent_tabIndex == DrawerActivity.mCurrentPlaying_tabIndex)
    	{
    		if(AudioPlayer.mMediaPlayer != null)
    		{
    			UtilAudio.stopAudioPlayer();
				AudioPlayer.mAudioIndex = 0;
				AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_STOP;
    		}    		
    	}
    	
    	// update change after deleting tab
		updateTabChange(activity);
    	
    	// Note: _onTabChanged will reset scroll X to another value,
    	// so we need to add the following to set scroll X again
        mHorScrollView.post(new Runnable() 
        {
	        @Override
	        public void run() {
	        	mHorScrollView.scrollTo(0, 0);
	        	Util.setPref_lastTimeView_scrollX_byDrawerNumber(activity, 0 );
	        }
	    });
	}

	/**
	 * edit tab 
	 * 
	 */
	public static int mStyle = 0;
	static void editTab(int tabCursor, final Activity activity)
	{
		final int tabId = mDbTabs.getTabId(tabCursor, true);
		mDbTabs.doOpenTabs(DB.getFocus_tabsTableId());
		mTabCursor = mDbTabs.getTabsCursor();
		if(mTabCursor.isFirst())
			mFirstExist_TabId = tabId;
		mDbTabs.doCloseTabs();

		// get tab name
		String tabName = mDbTabs.getTabTitle(tabCursor, true);
		
		if(tabCursor == mCurrent_tabIndex )
		{
	        final EditText editText1 = new EditText(activity.getBaseContext());
	        editText1.setText(tabName);
	        editText1.setSelection(tabName.length()); // set edit text start position
	        //update tab info
	        Builder builder = new Builder(mTabHost.getContext());
	        builder.setTitle(R.string.edit_page_tab_title)
	                .setMessage(R.string.edit_page_tab_message)
	                .setView(editText1)   
	                .setNegativeButton(R.string.btn_Cancel, new OnClickListener()
	                {   @Override
	                    public void onClick(DialogInterface dialog, int which)
	                    {/*cancel*/}
	                })
	                .setNeutralButton(R.string.edit_page_button_delete, new OnClickListener()
	                {   @Override
	                    public void onClick(DialogInterface dialog, int which)
	                	{
	                		// delete
							//warning:start
		                	mPref_delete_warn = activity.getSharedPreferences("delete_warn", 0);
		                	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
		 	                   mPref_delete_warn.getString("KEY_DELETE_PAGE_WARN","yes").equalsIgnoreCase("yes")) 
		                	{
		            			Util util = new Util(activity);
		        				util.vibrate();
		        				
		                		Builder builder1 = new Builder(mTabHost.getContext()); 
		                		builder1.setTitle(R.string.confirm_dialog_title)
	                            .setMessage(R.string.confirm_dialog_message_page)
	                            .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener(){
	                            	@Override
	                                public void onClick(DialogInterface dialog1, int which1){
	                            		/*nothing to do*/}})
	                            .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener(){
	                            	@Override
	                                public void onClick(DialogInterface dialog1, int which1){
	                                	deletePage(tabId, activity);
	                            	}})
	                            .show();
		                	} //warning:end
		                	else
		                	{
		                		deletePage(tabId, activity);
		                	}
		                	
	                    }
	                })	
	                .setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
	                {   @Override
	                    public void onClick(DialogInterface dialog, int which)
	                    {
	                		// save
        					final int tabId =  mDbTabs.getTabId(mCurrent_tabIndex, true);
        					final int tblNotesTblId =  mDbTabs.getNotesTableId(mCurrent_tabIndex, true);
        					
	                        int tabStyle = mDbTabs.getTabStyle(mCurrent_tabIndex, true);
							mDbTabs.updateTab(tabId,
											  editText1.getText().toString(),
											  tblNotesTblId, 
											  tabStyle );
	                        
							// Before _recreate, store latest page number currently viewed
							Util.setPref_lastTimeView_notes_tableId(activity, tblNotesTblId);
	                        
	                        updateTabChange(activity);
	                    }
	                })	
	                .setIcon(android.R.drawable.ic_menu_edit);
	        
			        AlertDialog d1 = builder.create();
			        d1.show();
			        // android.R.id.button1 for positive: save
			        ((Button)d1.findViewById(android.R.id.button1))
			        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
			        
			        // android.R.id.button2 for negative: color 
			        ((Button)d1.findViewById(android.R.id.button2))
  			        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
			        
			        // android.R.id.button3 for neutral: delete
			        ((Button)d1.findViewById(android.R.id.button3))
			        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
			}
	}
	
    
	/**
	 * update tab change 
	 */
	static void updateTabChange(Activity activity)
	{
//		System.out.println("TabsHostFragment / _updateChange ");
		mTabHost.clearAllTabs(); //must add this in order to clear onTanChange event
    	setTab(activity);
	}    
	
	static public int getLastExist_TabId()
	{
		return mLastExist_TabId;
	}
	
	static public void setLastExist_tabId(int lastTabId)
	{
		mLastExist_TabId = lastTabId;
	}
}