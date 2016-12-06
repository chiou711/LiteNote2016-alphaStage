package com.cwc.litenote;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.config.Import_selectedFileAct;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.lib.DragSortListView;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.util.Util;

public class MainUi 
{
	MainUi(){};

	public class Constant {
	    static final int ADD_TEXT = R.id.ADD_TEXT;
	    static final int ADD_CAMERA_PICTURE = R.id.ADD_NEW_IMAGE;
	    static final int ADD_READY_PICTURE = R.id.ADD_OLD_PICTURE;
	    static final int ADD_CAMERA_VIDEO = R.id.ADD_NEW_VIDEO;
	    static final int ADD_READY_VIDEO = R.id.ADD_OLD_VIDEO;
	    static final int ADD_AUDIO = R.id.ADD_AUDIO;
	    static final int ADD_YOUTUBE_LINK = R.id.ADD_YOUTUBE_LINK;
	    static final int ADD_WEB_LINK = R.id.ADD_WEB_LINK;
	    static final int OPEN_PLAY_SUBMENU = R.id.PLAY;
	    static final int PLAY_OR_STOP_AUDIO = R.id.PLAY_OR_STOP_MUSIC;
	    static final int SLIDE_SHOW = R.id.SLIDE_SHOW;
		static final int ADD_NEW_FOLDER = R.id.ADD_NEW_FOLDER;
		static final int ENABLE_DRAWER_DRAG_AND_DROP = R.id.ENABLE_DRAWER_DRAG_AND_DROP;
		static final int ADD_NEW_PAGE = R.id.ADD_NEW_PAGE;
		static final int CHANGE_PAGE_COLOR = R.id.CHANGE_PAGE_COLOR;
		static final int CHECK_ALL = R.id.CHECK_ALL;
		static final int CONFIG_PREFERENCE = R.id.CONFIG_PREF;
		static final int COPY_CHECKED_NOTE = R.id.COPY_CHECKED_NOTE;
		static final int DELETE_CHECKED_NOTE = R.id.DELETE_CHECKED_NOTE;
		static final int ENABLE_NOTE_DRAG_AND_DROP = R.id.ENABLE_NOTE_DRAG_AND_DROP;
		static final int GALLERY = R.id.GALLERY;
		static final int INVERT_SELECTED = R.id.INVERT_SELECTED;
		static final int MAIL_CHECKED_NOTE = R.id.MAIL_CHECKED_NOTE;
		static final int MOVE_CHECKED_NOTE = R.id.MOVE_CHECKED_NOTE;
		static final int SEND_PAGES = R.id.SEND_PAGES;
		static final int SHIFT_PAGE = R.id.SHIFT_PAGE;
		static final int SHOW_BODY = R.id.SHOW_BODY;
		static final int UNCHECK_ALL = R.id.UNCHECK_ALL;
	}

	static DrawerItemClickListener itemClick;
	static DrawerItemLongClickListener itemLongClick;
	
	static void addDrawerItemListeners()
	{
		itemClick = new DrawerItemClickListener();
		itemLongClick = new DrawerItemLongClickListener();
	}
    /**
     * Add new folder
     * 
     */
	public static  void addNewFolder(final Activity act, final int newTableId) 
	{
		final DB db = DB.mDb_drawer;
		// get tab name
		String tabName = "D".concat(String.valueOf(newTableId));
        
        final EditText editText1 = new EditText(act.getBaseContext());
        editText1.setText(tabName);
        editText1.setSelection(tabName.length()); // set edit text start position
        
        //update tab info
        Builder builder = new Builder(act);
        builder.setTitle(R.string.edit_drawer_title)
                .setMessage(R.string.edit_drawer_message)
                .setView(editText1)   
                .setNegativeButton(R.string.edit_page_button_ignore, new OnClickListener(){   
                	@Override
                    public void onClick(DialogInterface dialog, int which)
                    {/*nothing*/}
                })
                .setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
                {   @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                		
	    	            final String[] items = new String[]{
	    	            		act.getResources().getText(R.string.add_new_note_top).toString(),
	    	            		act.getResources().getText(R.string.add_new_note_bottom).toString() };
	    	            
						AlertDialog.Builder builder = new AlertDialog.Builder(act);
						  
						builder.setTitle(R.string.add_new_page_select_position)
						.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
				        	String drawerTitle =  editText1.getText().toString();
				        	DrawerActivity.mDrawerChildTitles.add(drawerTitle);
				    		// insert new drawer Id and Title
				        	db.insertDrawerChild(newTableId, drawerTitle ); 
				        	
				        	// insert tabs table
				        	DB.insertTabsTable(db,newTableId, false);
				        	
				    		// insert notes table
				    		for(int i=1;i<= DB.DEFAULT_NOTES_TABLE_COUNT;i++)
				    		{
				    			DB.insertNotesTable(db,newTableId, i, false);
				    		}
				    		
				    		// add new drawer to the top
							if(which == 0)
							{
						        int startCursor = db.getDrawerChildCount()-1;
						        int endCursor = 0;
								
								//reorder data base storage for ADD_NEW_TO_TOP option
								int loop = Math.abs(startCursor-endCursor);
								for(int i=0;i< loop;i++)
								{
									swapDrawerRows(startCursor,endCursor);
									if((startCursor-endCursor) >0)
										endCursor++;
									else
										endCursor--;
								}
								
								// focus position is 0 for Add to top
								DrawerActivity.mFocus_drawerChildPos = 0;
								Util.setPref_lastTimeView_tabs_tableId(act,db.getTabsTableId(DrawerActivity.mFocus_drawerChildPos) );
								
								// update playing highlight if needed
								if(AudioPlayer.mMediaPlayer != null) 
									DrawerActivity.mCurrentPlaying_drawerIndex++;
							}				    		
				    		
							DrawerActivity.drawerAdapter.notifyDataSetChanged();
				    		
				        	//end
							dialog.dismiss();
							
							updateDrawerFocusPosition();
						}})
						.setNegativeButton(R.string.btn_Cancel, null)
						.show();
                    }
                })	 
                .setIcon(android.R.drawable.ic_menu_edit);
        
	        final AlertDialog dlg = builder.create();
	        dlg.show();
	        // android.R.id.button1 for negative: cancel 
	        ((Button)dlg.findViewById(android.R.id.button1))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
	        // android.R.id.button2 for positive: save
	        ((Button)dlg.findViewById(android.R.id.button2))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	}	
    
    /*
	 * Change Page Color
	 * 
	 */
	static void changePageColor(final Activity act)
	{
		// set color
		final AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setTitle(R.string.edit_page_color_title)
	    	   .setPositiveButton(R.string.edit_page_button_ignore, new OnClickListener(){   
	            	@Override
	                public void onClick(DialogInterface dialog, int which)
	                {/*cancel*/}
	            	});
		// inflate select style layout
		LayoutInflater mInflator= (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflator.inflate(R.layout.select_style, null);//??? how to set group view?
		RadioGroup RG_view = (RadioGroup)view.findViewById(R.id.radioGroup1);
		
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio0),0);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio1),1);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio2),2);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio3),3);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio4),4);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio5),5);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio6),6);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio7),7);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio8),8);
		Util.setButtonColor((RadioButton)RG_view.findViewById(R.id.radio9),9);
		
		// set current selection
		for(int i=0;i< Util.getStyleCount();i++)
		{
			if(Util.getCurrentPageStyle(act) == i)
			{
				RadioButton buttton = (RadioButton) RG_view.getChildAt(i);
		    	if(i%2 == 0)
		    		buttton.setButtonDrawable(R.drawable.btn_radio_on_holo_dark);
		    	else
		    		buttton.setButtonDrawable(R.drawable.btn_radio_on_holo_light);		    		
			}
		}
		
		builder.setView(view);
		
		RadioGroup radioGroup = (RadioGroup) RG_view.findViewById(R.id.radioGroup1);
		    
		final AlertDialog dlg = builder.create();
	    dlg.show();
	    
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup RG, int id) {
				DB db = TabsHostFragment.mDbTabs;
				TabsHostFragment.mStyle = RG.indexOfChild(RG.findViewById(id));
				db.updateTab(db.getTabId(TabsHostFragment.mCurrent_tabIndex, true),
									db.getTabTitle(TabsHostFragment.mCurrent_tabIndex, true),
									db.getNotesTableId(TabsHostFragment.mCurrent_tabIndex, true),
									TabsHostFragment.mStyle );
	 			dlg.dismiss();
	 			TabsHostFragment.updateTabChange(act);
		}});
	}
	
	/**
	 * shift page right or left
	 * 
	 */
	static void shiftPage(final Activity act)
	{
	    Builder builder = new Builder(act);
	    builder.setTitle(R.string.rearrange_page_title)
	      	   .setMessage(null)
	           .setNegativeButton(R.string.rearrange_page_left, null)
	           .setNeutralButton(R.string.edit_note_button_back, null)
	           .setPositiveButton(R.string.rearrange_page_right,null)
	           .setIcon(R.drawable.ic_dragger_h);
	    final AlertDialog d = builder.create();
	    
	    // disable dim background 
		d.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		d.show();
		
		
		final int dividerWidth = act.getResources().getDrawable(R.drawable.ic_tab_divider).getMinimumWidth();
		
		// Shift to left
	    d.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
	    {  @Override
	       public void onClick(View v)
	       {
	    		//change to OK
	    		Button mButton=(Button)d.findViewById(android.R.id.button3);
		        mButton.setText(R.string.btn_Finish);
		        mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);
	
		        int[] leftMargin = {0,0};
		        if(TabsHostFragment.mCurrent_tabIndex == 0)
		        	TabsHostFragment.mTabHost.getTabWidget().getChildAt(0).getLocationInWindow(leftMargin);
		        else
		        	TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrent_tabIndex-1).getLocationInWindow(leftMargin);
	
				int curTabWidth,nextTabWidth;
				curTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrent_tabIndex).getWidth();
				if(TabsHostFragment.mCurrent_tabIndex == 0)
					nextTabWidth = curTabWidth;
				else
					nextTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrent_tabIndex-1).getWidth(); 
	
				// when leftmost tab margin over window border
	       		if(leftMargin[0] < 0) 
	       			TabsHostFragment.mHorScrollView.scrollBy(- (nextTabWidth + dividerWidth) , 0);
				
	    		d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
	    	    if(TabsHostFragment.mCurrent_tabIndex == 0)
	    	    {
	    	    	Toast.makeText(TabsHostFragment.mTabHost.getContext(), R.string.toast_leftmost ,Toast.LENGTH_SHORT).show();
	    	    	d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);//avoid long time toast
	    	    }
	    	    else
	    	    {
	    	    	Util.setPref_lastTimeView_notes_tableId(act, TabsHostFragment.mDbTabs.getNotesTableId(TabsHostFragment.mCurrent_tabIndex, true));
	    	    	swapTabInfo(TabsHostFragment.mCurrent_tabIndex,
	    	    			    TabsHostFragment.mCurrent_tabIndex-1);
					
					// shift left when audio playing
					// target is playing index 
					if(TabsHostFragment.mCurrent_tabIndex == DrawerActivity.mCurrentPlaying_tabIndex)
						DrawerActivity.mCurrentPlaying_tabIndex--;
					// target is at right side of playing index
					else if((TabsHostFragment.mCurrent_tabIndex - DrawerActivity.mCurrentPlaying_tabIndex)== 1 )
						DrawerActivity.mCurrentPlaying_tabIndex++;
					
					TabsHostFragment.updateTabChange(act);
	    	    }
	       }
	    });
	    
	    // done
	    d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
	    {   @Override
	       public void onClick(View v)
	       {
	           d.dismiss();
	       }
	    });
	    
	    // Shift to right
	    d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
	    {  @Override
	       public void onClick(View v)
	       {
	    		d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
	    		
	    		// middle button text: change to OK
	    		Button mButton=(Button)d.findViewById(android.R.id.button3);
		        mButton.setText(R.string.btn_Finish);
		        mButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_finish , 0, 0, 0);
	    		
		        DB db = TabsHostFragment.mDbTabs;
		        db.initTabsDb(db);
	    		int count = db.getTabsCount(true);
	            
				int[] rightMargin = {0,0};
				if(TabsHostFragment.mCurrent_tabIndex == (count-1))
					TabsHostFragment.mTabHost.getTabWidget().getChildAt(count-1).getLocationInWindow(rightMargin);
				else
					TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrent_tabIndex+1).getLocationInWindow(rightMargin);
	
				int curTabWidth, nextTabWidth;
				curTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrent_tabIndex).getWidth();
				if(TabsHostFragment.mCurrent_tabIndex == (count-1))
					nextTabWidth = curTabWidth;
				else
					nextTabWidth = TabsHostFragment.mTabHost.getTabWidget().getChildAt(TabsHostFragment.mCurrent_tabIndex+1).getWidth();
				
	    		// when rightmost tab margin plus its tab width over screen border 
				int screenWidth = UtilImage.getScreenWidth(act);
	    		if( screenWidth <= rightMargin[0] + nextTabWidth )
	    			TabsHostFragment.mHorScrollView.scrollBy(nextTabWidth + dividerWidth, 0);	
				
	    		d.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
	    		
	   	    	if(TabsHostFragment.mCurrent_tabIndex == (count-1))
	   	    	{
	   	    		// end of the right side
	   	    		Toast.makeText(TabsHostFragment.mTabHost.getContext(),R.string.toast_rightmost,Toast.LENGTH_SHORT).show();
	   	    		d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);//avoid long time toast
	   	    	}
	   	    	else
	   	    	{
	    	    	Util.setPref_lastTimeView_notes_tableId(act, db.getNotesTableId(TabsHostFragment.mCurrent_tabIndex, true));
					swapTabInfo(TabsHostFragment.mCurrent_tabIndex,TabsHostFragment.mCurrent_tabIndex+1);
					
					// shift right when audio playing
					// target is playing index
					if(TabsHostFragment.mCurrent_tabIndex == DrawerActivity.mCurrentPlaying_tabIndex)
						DrawerActivity.mCurrentPlaying_tabIndex++;
					// target is at left side of plying index
					else if((DrawerActivity.mCurrentPlaying_tabIndex - TabsHostFragment.mCurrent_tabIndex)== 1 )
						DrawerActivity.mCurrentPlaying_tabIndex--;
						
					TabsHostFragment.updateTabChange(act);
	   	    	}
	       }
	    });
	    
	    // android.R.id.button1 for positive: next 
	    ((Button)d.findViewById(android.R.id.button1))
	    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_forward, 0, 0, 0);
	    // android.R.id.button2 for negative: previous
	    ((Button)d.findViewById(android.R.id.button2))
	    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
	    // android.R.id.button3 for neutral: cancel
	    ((Button)d.findViewById(android.R.id.button3))
	    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	}

	/**
	 * swap tab info
	 * 
	 */
	static void swapTabInfo(int start, int end)
	{
		System.out.println("MainUi / swapTabInfo / start = " + start + " , end = " + end);
		DB db = TabsHostFragment.mDbTabs;
		
		int tabsTableId = Util.getPref_lastTimeView_tabs_tableId(TabsHostFragment.mAct);
		db.doOpenTabs(tabsTableId);
		int tempTabId = db.getTabId(end,false);
		String tempTabTitle = db.getTabTitle(end,false);
		int tempTabNotesTableId = db.getNotesTableId(end,false);
		int tempTabStyle = db.getTabStyle(end, false);
		
		db.updateTab(tempTabId,
					 db.getTabTitle(start,false),
					 db.getNotesTableId(start,false),
					 db.getTabStyle(start, false));		        
		
		db.updateTab(db.getTabId(start,false),
					tempTabTitle,
					tempTabNotesTableId,
					tempTabStyle);	
		
		db.doCloseTabs();
	}


	/**
	 * Add new page
	 * @param act 
	 * 
	 */
	public static  void addNewPage(final FragmentActivity act, final int newTabId) {
		// get tab name
		String tabName = "N".concat(String.valueOf(newTabId));
	    
		// check if name is duplicated
		DB dbTabs = TabsHostFragment.mDbTabs;
		dbTabs.doOpenTabs(Util.getPref_lastTimeView_tabs_tableId(TabsHostFragment.mAct));
		int tabsCount = dbTabs.getTabsCount(false);
		
		for(int i=0; i<tabsCount; i++ )
		{
			String tabTitle = dbTabs.getTabTitle(i,false);
			// new name for differentiation
			if(tabName.equalsIgnoreCase(tabTitle))
			{
				tabName = tabTitle.concat("b");
			}
		}
		dbTabs.doCloseTabs();
		
	    final EditText editText1 = new EditText(act.getBaseContext());
	    editText1.setText(tabName);
	    editText1.setSelection(tabName.length()); // set edit text start position
	    
	    //update tab info
	    Builder builder = new Builder(act);
	    builder.setTitle(R.string.edit_page_tab_title)
	            .setMessage(R.string.edit_page_tab_message)
	            .setView(editText1)   
	            .setNegativeButton(R.string.edit_page_button_ignore, new OnClickListener(){   
	            	@Override
	                public void onClick(DialogInterface dialog, int which)
	                {/*nothing*/}
	            })
	            .setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
	            {   @Override
	                public void onClick(DialogInterface dialog, int which)
	                {
	            		
	    	            final String[] items = new String[]{
	    	            		act.getResources().getText(R.string.add_new_page_leftmost).toString(),
	    	            		act.getResources().getText(R.string.add_new_page_rightmost).toString() };
	    	            
						AlertDialog.Builder builder = new AlertDialog.Builder(act);
						  
						builder.setTitle(R.string.add_new_page_select_position)
						.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
						
							if(which ==0)
								insertTabLeftmost(act, newTabId, editText1.getText().toString());
							else
								insertTabRightmost(act, newTabId, editText1.getText().toString());
							//end
							dialog.dismiss();
						}})
						.setNegativeButton(R.string.btn_Cancel, null)
						.show();
	                }
	            })	 
	            .setIcon(android.R.drawable.ic_menu_edit);
	    
	        final AlertDialog d = builder.create();
	        d.show();
	        // android.R.id.button1 for negative: cancel 
	        ((Button)d.findViewById(android.R.id.button1))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
	        // android.R.id.button2 for positive: save
	        ((Button)d.findViewById(android.R.id.button2))
	        .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	}

	/*
	 * Insert Tab to Rightmost
	 * 
	 */
	static void insertTabRightmost(final FragmentActivity act, int newTblId,String tabName)
	{
		DB db = TabsHostFragment.mDbTabs;
	    // insert tab name
		int style = Util.getNewPageStyle(act);
		db.insertTab(DB.getFocusTabsTableName(),tabName,newTblId,style );
		
		// insert table for new tab
		DB.insertNotesTable(db,DB.getFocus_tabsTableId(),newTblId, false);
		TabsHostFragment.mTabCount++;
		
		// commit: final page viewed
		Util.setPref_lastTimeView_notes_tableId(act, newTblId);
		
	    // set scroll X
		final int scrollX = (TabsHostFragment.mTabCount) * 60 * 5; //over the last scroll X
		
		TabsHostFragment.updateTabChange(act);
		
		TabsHostFragment.mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
	        	TabsHostFragment.mHorScrollView.scrollTo(scrollX, 0);
	        	Util.setPref_lastTimeView_scrollX_byDrawerNumber(act, scrollX );
	        } 
	    });
	}

	/* 
	 * Insert Tab to Leftmost
	 * 
	 */
	static void insertTabLeftmost(final FragmentActivity act, int newTabId,String tabName)//??? why exception
	{
		DB db = TabsHostFragment.mDbTabs;
		
		
	    // insert tab name
		int style = Util.getNewPageStyle(act);
		db.insertTab(DB.getFocusTabsTableName(),tabName, newTabId, style );
		
		// insert table for new tab
		DB.insertNotesTable(db,DB.getFocus_tabsTableId(),newTabId, false);
		TabsHostFragment.mTabCount++;
		
		//change to leftmost tab Id
		int tabTotalCount = db.getTabsCount(true);
		for(int i=0;i <(tabTotalCount-1);i++)
		{
			int tabIndex = tabTotalCount -1 -i ;
			swapTabInfo(tabIndex,tabIndex-1);
			updateFinalPageViewed(act);
		}
		
	    // set scroll X
		final int scrollX = 0; // leftmost
		
		// commit: scroll X
		TabsHostFragment.updateTabChange(act);
		
		TabsHostFragment.mHorScrollView.post(new Runnable() {
	        @Override
	        public void run() {
	        	TabsHostFragment.mHorScrollView.scrollTo(scrollX, 0);
	        	Util.setPref_lastTimeView_scrollX_byDrawerNumber(act, scrollX );
	        } 
	    });
		
		// update highlight tab
		if(DrawerActivity.mCurrentPlaying_drawerIndex == DrawerActivity.mFocus_drawerChildPos)
			DrawerActivity.mCurrentPlaying_tabIndex++;
	}
	
	
	/*
	 * Update Final page which was viewed last time
	 * 
	 */
	protected static void updateFinalPageViewed(FragmentActivity act)
	{
	    // get final viewed table Id
	    String tblId = Util.getPref_lastTimeView_notes_tableId(act);
		DB.setFocus_notes_tableId(tblId);
	
//		Context context = act.getApplicationContext();
		DB dbTabs = TabsHostFragment.mDbTabs;
		dbTabs.doOpenTabs(Util.getPref_lastTimeView_tabs_tableId(TabsHostFragment.mAct));
		// get final view tab index of last time
		for(int i =0;i<dbTabs.getTabsCount(false);i++)
		{
			if(Integer.valueOf(tblId) == dbTabs.getNotesTableId(i, false))
				TabsHostFragment.mFinalPageViewed_TabIndex = i;	// starts from 0
			
	    	if(	dbTabs.getTabId(i, false)== TabsHostFragment.mFirstExist_TabId)
	    	{
	    		System.out.println("--- db.getTab_NotesTableId(i) = "  + dbTabs.getNotesTableId(i, false));
	    		Util.setPref_lastTimeView_notes_tableId(act, dbTabs.getNotesTableId(i, false) );
	    	}
		}
		dbTabs.doCloseTabs();
	}

	/**
	 * delete selected drawer
	 * 
	 */
	static int mFirstExist_DrawerId = 0;		
	static int mLastExist_drawerTabsTableId;
	public static void deleteSelectedDrawer(int position, final Activity act) 
	{
		DB db = DB.mDb_drawer;
		// set selected drawer tabs table Id
		DB.setSelected_tabsTableId(db.getTabsTableId(position));
		// set selected notes table Id
		DB.setSelected_NotesTableId(Util.getPref_lastTimeView_notes_tableId(act)); 
		
		// Before delete
		// renew first DrawerTabsTableId and last DrawerTabsTableId
		renewFirstAndLastDrawerId();
		
		// keep one drawer at least
		int drawerCount = db.getDrawerChildCount();
		if(drawerCount == 1)
		{
			 // show toast for only one drawer
             Toast.makeText(act, R.string.toast_keep_one_drawer , Toast.LENGTH_SHORT).show();
             return;
		}

		// get drawer tabs table Id
		int drawerTabsTableId = db.getTabsTableId(position);
		// get drawer Id
		int drawerId =  (int) db.getDrawerChildId(position);

		DB dbTabs = new DB(TabsHostFragment.mAct, drawerTabsTableId);
		dbTabs.initTabsDb(dbTabs);
		dbTabs.doOpenTabs(drawerTabsTableId);
		// 1) delete related notes table
		for(int i=0;i< dbTabs.getTabsCount(false);i++)
		{
			int notesTableId = dbTabs.getNotesTableId(i, false);
			db.dropNotesTable(drawerTabsTableId, notesTableId);
		}
		dbTabs.doCloseTabs();
		
		// 2) delete tabs table
		db.dropTabsTable(drawerTabsTableId);
		
		// 3) delete tabs info in drawer table
		db.deleteDrawerChildId(drawerId);		
		
		renewFirstAndLastDrawerId();

		// After Delete
        // - update mFocusDrawerPos
        // - select first existing drawer item 
		drawerCount = db.getDrawerChildCount();
		
		// get new focus position
		// if focus item is deleted, set focus to new first existing drawer
        if(DrawerActivity.mFocus_drawerChildPos == position)
        {
	        for(int item = 0; item < drawerCount; item++)
	        {
	        	if(	db.getDrawerChildId(item)== mFirstExist_DrawerId)
	        		DrawerActivity.mFocus_drawerChildPos = item; 
	        }
        }
        else if(position < DrawerActivity.mFocus_drawerChildPos)
        	DrawerActivity.mFocus_drawerChildPos--;

        // set new focus position
        DrawerActivity.mDrawerListView.setItemChecked(DrawerActivity.mFocus_drawerChildPos, true); 
        
        // update tabs table Id of last time view
        Util.setPref_lastTimeView_tabs_tableId(act,
        											db.getTabsTableId(DrawerActivity.mFocus_drawerChildPos) );
        
        // update audio playing highlight if needed
        if(AudioPlayer.mMediaPlayer != null)
        {
           if( DrawerActivity.mCurrentPlaying_drawerIndex > position)
        	   DrawerActivity.mCurrentPlaying_drawerIndex--;
           else if(DrawerActivity.mCurrentPlaying_drawerIndex == position)
           {
        	   UtilAudio.stopAudioPlayer();
        	   selectDrawerChild(DrawerActivity.mFocus_drawerChildPos); // select drawer to clear old playing view
        	   DrawerActivity.setDrawerTitle(DrawerActivity.mDrawerChildTitle);
           }
        }
		
        // refresh drawer list view
        DrawerActivity.drawerAdapter.notifyDataSetChanged();
        
        // clear tabs
        if(TabsHostFragment.mTabHost != null)
        	TabsHostFragment.mTabHost.clearAllTabs();
        TabsHostFragment.mTabHost = null;

        // remove last time view Key
        Util.removePref_lastTimeView_key(act,drawerTabsTableId);
	}

	private static Cursor mDrawerCursor;
	// Renew first and last drawer Id
	static void renewFirstAndLastDrawerId()
	{
		DB db = DB.mDb_drawer;
		int i = 0;
		int drawerCount = db.getDrawerChildCount();
		mLastExist_drawerTabsTableId = 0;
		while(i < drawerCount)
    	{
			db.doOpenDrawer();
			mDrawerCursor = DB.mCursor_drawerChild;
			mDrawerCursor.moveToPosition(i);
			db.doCloseDrawer();	
						
			if(mDrawerCursor.isFirst())
				mFirstExist_DrawerId = (int) db.getDrawerChildId(i) ;
			
			if(db.getTabsTableId(i) >= mLastExist_drawerTabsTableId)
				mLastExist_drawerTabsTableId = db.getTabsTableId(i);
			
			i++;
    	} 
//		System.out.println("mFirstExist_DrawerId = " + mFirstExist_DrawerId);
//		System.out.println("mLastExist_drawerTabsTableId = " + mLastExist_drawerTabsTableId);
	}

    private static SharedPreferences mPref_delete_warn;
	static void editDrawerItem(final int position)
	{
		DB db = DB.mDb_drawer;
		final Activity act = DrawerActivity.mDrawerActivity;
		
		// insert when table is empty, activated only for the first time 
		final String drawerName = db.getDrawerChild_Title(position);
	
		final EditText editText = new EditText(act);
	    editText.setText(drawerName);
	    editText.setSelection(drawerName.length()); // set edit text start position
	    //update tab info
	    Builder builder = new Builder(act);
	    builder.setTitle(R.string.edit_drawer_title)
	    	.setMessage(R.string.edit_drawer_message)
	    	.setView(editText)   
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
	            	mPref_delete_warn = act.getSharedPreferences("delete_warn", 0);
	            	if(mPref_delete_warn.getString("KEY_DELETE_WARN_MAIN","enable").equalsIgnoreCase("enable") &&
	                   mPref_delete_warn.getString("KEY_DELETE_DRAWER_WARN","yes").equalsIgnoreCase("yes")) 
	            	{
	        			Util util = new Util(act);
	    				util.vibrate();
	    				
	            		Builder builder1 = new Builder(act); 
	            		builder1.setTitle(R.string.confirm_dialog_title)
	                    .setMessage(R.string.confirm_dialog_message_drawer)
	                    .setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener(){
	                    	@Override
	                        public void onClick(DialogInterface dialog1, int which1){
	                    		/*nothing to do*/}})
	                    .setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener(){
	                    	@Override
	                        public void onClick(DialogInterface dialog1, int which1){
	                    		deleteSelectedDrawer(position, act);
	                    	}})
	                    .show();
	            	} //warning:end
	            	else
	            	{
	            		deleteSelectedDrawer(position, act);
	            	}
	            	
	            }
	        })		    	
	    	.setPositiveButton(R.string.edit_page_button_update, new OnClickListener()
	    	{   @Override
	    		public void onClick(DialogInterface dialog, int which)
	    		{
	    			DB db = DB.mDb_drawer;
	    			// save
	    			int drawerId =  (int) db.getDrawerChildId(position);
	    			int drawerTabInfoTableId =  db.getTabsTableId(position);
					db.updateDrawer(drawerId,
									drawerTabInfoTableId,
									editText.getText().toString());
	                DrawerActivity.drawerAdapter.notifyDataSetChanged();
	                DrawerActivity.setDrawerTitle(editText.getText().toString());
	            }
	        })	
	        .setIcon(android.R.drawable.ic_menu_edit);
	        
	    AlertDialog d1 = builder.create();
	    d1.show();
	    // android.R.id.button1 for positive: save
	    ((Button)d1.findViewById(android.R.id.button1))
	    .setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
	    
	    // android.R.id.button2 for negative: cancel 
	    ((Button)d1.findViewById(android.R.id.button2))
	    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
	    
	    // android.R.id.button3 for neutral: delete
	    ((Button)d1.findViewById(android.R.id.button3))
	    .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
	}

    private static Long mDrawerId1 = (long) 1;
    private static Long mDrawerId2 = (long) 1;
	private static int mDrawerTabsTableId1;
	private static int mDrawerTabsTableId2;
	private static String mDrawerTitle1;
	private static String mDrawerTitle2;
	
	// swap rows
	protected static void swapDrawerRows(int startPosition, int endPosition) 
	{
		DB db = DB.mDb_drawer;
	
		mDrawerId1 = db.getDrawerChildId(startPosition);
		mDrawerTabsTableId1 = db.getTabsTableId(startPosition);
		mDrawerTitle1 = db.getDrawerChild_Title(startPosition);
	
		mDrawerId2 = db.getDrawerChildId(endPosition);
		mDrawerTabsTableId2 = db.getTabsTableId(endPosition);
		mDrawerTitle2 = db.getDrawerChild_Title(endPosition);
	
	    db.updateDrawer(mDrawerId1,
			    		mDrawerTabsTableId2,
			    		mDrawerTitle2);		        
		
		db.updateDrawer(mDrawerId2,
						mDrawerTabsTableId1,
						mDrawerTitle1);	
	}

    // Update focus position
    static void updateDrawerFocusPosition()
    {
    	DB db = DB.mDb_drawer;
    	Activity act = DrawerActivity.mDrawerActivity;
    	
		//update focus position
		int iLastView_DrawerTabsTableId = Util.getPref_lastTimeView_tabs_tableId(act);
		int count = db.getDrawerChildCount();
    	for(int i=0;i<count;i++)
    	{
        	if(	db.getTabsTableId(i)== iLastView_DrawerTabsTableId)
        	{
        		DrawerActivity.mFocus_drawerChildPos =  i;
        		DrawerActivity.mDrawerListView.setItemChecked(DrawerActivity.mFocus_drawerChildPos, true); 		
        	}
    	}
    	
    }	
    
    // select drawer item
    public static void selectDrawerChild(final int position) 
    {
    	System.out.println("MainUi / _selectDrawerChild / position = " + position);
    	DrawerActivity.mDrawerChildTitle = DB.mDb_drawer.getDrawerChild_Title(position);

		// update selected item and title, then close the drawer
		DrawerActivity.mDrawerListView.setItemChecked(position, true);		
		DrawerActivity.mDrawerLayout.closeDrawer(DrawerActivity.mDrawerListView);    	
        
		if(DB.HAS_DEFAULT_TABLES)
		{
			// Create default tables
			if( (position < DB.DEFAULT_TABS_TABLE_COUNT) && 
				(Util.getPref_has_default_import(DrawerActivity.mDrawerActivity,position) == false))
			{
				String fileName = "default"+ (position+1) + ".xml";
				Activity act = DrawerActivity.mDrawerActivity;
				int tabsTableId = Util.getPref_lastTimeView_tabs_tableId(act);
				DB.setFocus_tabsTableId(tabsTableId);
	//			String notesTableId = Util.getPref_lastTimeView_notes_tableId(act);
	//			DB.setFocus_notes_tableId(notesTableId);
				TabsHostFragment.setLastExist_tabId(0);//???
	
				Import_selectedFileAct.createDefaultTables(act,fileName);
	//			TabsHostFragment.updateTabChange(DrawerActivity.mDrawerActivity);
				Util.setPref_has_default_import(act,true,position);
			}
		}

        // use Runnable to make sure only one drawer background is seen
        mHandler = new Handler();
       	mHandler.post(mTabsHostRun);
    }
    
    static Handler mHandler;
    // runnable to launch tabs host
    static Runnable mTabsHostRun =  new Runnable() 
    {
        @Override
        public void run() 
        {
        	System.out.println("MainUi / mTabsHostRun");
            Fragment mTabsHostFragment = new TabsHostFragment();
        	FragmentTransaction fragmentTransaction = DrawerActivity.fragmentManager.beginTransaction();
            
        	fragmentTransaction.replace(R.id.content_frame, mTabsHostFragment).commit(); 
            //??? 09-11 00:40:49.634: E/AndroidRuntime(28200): java.lang.IllegalStateException: 
        	// Can not perform this action after onSaveInstanceState
            
        	DrawerActivity.fragmentManager.executePendingTransactions();
        } 
    };    
    
    public static boolean isSameNotesTable()
    {
	    if( (DrawerActivity.mCurrentPlaying_notesTableId == 
			 TabsHostFragment.mCurrent_notesTableId        ) &&
			(DrawerActivity.mCurrentPlaying_tabIndex == 
			 TabsHostFragment.mCurrent_tabIndex            ) &&
	     	(DrawerActivity.mCurrentPlaying_drawerIndex == 
	     	 DrawerActivity.mFocus_drawerChildPos               )   )  
	    	return true;
	    else
	    	return false;
    }
    
}

/*
 * Listeners
 * 
 */
/* The click listener for ListView in the navigation drawer */
class DrawerItemClickListener implements OnItemClickListener 
{
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
    {
    	System.out.println("DrawerActivity / DrawerItemClickListener");
    	DrawerActivity.mFocus_drawerChildPos = position;

    	DB db = DB.mDb_drawer;
        Util.setPref_lastTimeView_tabs_tableId(DrawerActivity.mDrawerActivity,
        									   db.getTabsTableId(position) );
    	
    	MainUi.selectDrawerChild(position);
    	DrawerActivity.setDrawerTitle(DrawerActivity.mDrawerChildTitle);
    }
}

/* The click listener for ListView in the navigation drawer */
class DrawerItemLongClickListener implements DragSortListView.OnItemLongClickListener 
{
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
    {
    	MainUi.editDrawerItem(position);
		return true;
    }
}


