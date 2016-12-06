package com.cwc.litenote;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.config.Config;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.lib.DragSortController;
import com.cwc.litenote.lib.DragSortListView;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.NoisyAudioStreamReceiver;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.GalleryGridAct;
import com.cwc.litenote.media.image.SlideshowInfo;
import com.cwc.litenote.media.image.SlideshowPlayer;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.note.Note_addAudio;
import com.cwc.litenote.note.Note_addCameraImage;
import com.cwc.litenote.note.Note_addCameraVideo;
import com.cwc.litenote.note.Note_addNewText;
import com.cwc.litenote.note.Note_addNew_optional;
import com.cwc.litenote.note.Note_addNew_optional_for_multiple;
import com.cwc.litenote.note.Note_addReadyImage;
import com.cwc.litenote.note.Note_addReadyVideo;
import com.cwc.litenote.util.SendMailAct;
import com.cwc.litenote.util.Util;

import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

public class DrawerActivity extends FragmentActivity implements OnBackStackChangedListener 
{
    static DrawerLayout mDrawerLayout;
    private DragSortController mController;
    public static DragSortListView mDrawerListView;
    static ActionBarDrawerToggle mDrawerToggle;
    static CharSequence mDrawerChildTitle;
    private CharSequence mAppTitle;
    static Context mContext;
	static Config mConfigFragment;
	static boolean bEnableConfig;
    static Menu mMenu;
    public static DB mDb;
    public static DB mDb_tabs;
    public static DB mDb_notes;
    static DrawerAdapter drawerAdapter;
    static List<String> mDrawerChildTitles;
    public static int mFocus_drawerChildPos;
    SharedPreferences mPref_add_new_note_option;
	static NoisyAudioStreamReceiver noisyAudioStreamReceiver;
	static IntentFilter intentFilter;
	public static FragmentActivity mDrawerActivity;
	static int mDrawerChildCount;
	static FragmentManager fragmentManager;
	public static int mLastOkTabId = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	///
//    	 StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//
//    	   .detectDiskReads()
//    	   .detectDiskWrites()
//    	   .detectNetwork() 
//    	   .penaltyLog()
//    	   .build());
//
//    	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
////    	   .detectLeakedSqlLiteObjects() //??? unmark this line will cause strict mode error
//    	   .penaltyLog() 
//    	   .penaltyDeath()
//    	   .build());     	
    	///
        super.onCreate(savedInstanceState);
        
        ///
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        ///
        
        mDrawerActivity = this;
        setContentView(R.layout.drawer_activity_main);
        
        if(Util.CODE_MODE == Util.RELEASE_MODE) 
        {
        	OutputStream nullDev = new OutputStream() 
            {
                public  void    close() {}
                public  void    flush() {}
                public  void    write(byte[] b) {}
                public  void    write(byte[] b, int off, int len) {}
                public  void    write(int b) {}
            }; 
            System.setOut( new PrintStream(nullDev));
        }
        
        //Log.d below can be disabled by applying proguard
        //1. enable proguard-android-optimize.txt in project.properties
        //2. be sure to use newest version to avoid build error
        //3. add the following in proguard-project.txt
        /*-assumenosideeffects class android.util.Log {
        public static boolean isLoggable(java.lang.String, int);
        public static int v(...);
        public static int i(...);
        public static int w(...);
        public static int d(...);
        public static int e(...);
    	}
        */
        Log.d("test log tag","start app");         
        
        System.out.println("================start application ==================");
        System.out.println("DrawerActivity / onCreate");

        UtilImage.getDefaultSacleInPercent(DrawerActivity.this);
        
        mAppTitle = getTitle();
        
        mDrawerChildTitles = new ArrayList<String>();

		Context context = getApplicationContext();

		if(savedInstanceState == null)
		{
			mDb = new DB(context);  
			mDb.initDrawerDb(mDb);
			
    		mDb_tabs = new DB(context,Util.getPref_lastTimeView_tabs_tableId(this));
    		mDb_tabs.initTabsDb(mDb_tabs);			
			
			mDb_notes = new DB(context,Util.getPref_lastTimeView_notes_tableId(this));
			mDb_notes.initNotesDb(mDb_notes);
		}
        
		///
		//Add note with the link
		Bundle extras = getIntent().getExtras();
		String path = null;
		if(extras != null)
			path = extras.getString(Intent.EXTRA_TEXT);
		
		if(!Util.isEmptyString(path))
		{
			System.out.println("-------link path of YouTube Share = " + path);
		    mDb_notes.doOpenNotes();
		    mDb_notes.insertNote("", "", "", "", path, "", 0, (long) 0);// add new note, get return row Id
		    mDb_notes.doCloseNotes();
		    String title = Util.getYoutubeTitle(path);
		    Toast.makeText(this, 
		    			   getResources().getText(R.string.add_new_note_option_title) + title,
		    			   Toast.LENGTH_SHORT)
		    	 .show();
		    finish();
		}
		else
		///
		{
			// check DB
			final boolean ENABLE_DB_CHECK = false;//true;//
			if(ENABLE_DB_CHECK)
			{
		        // list all drawer tables
				int drawerCount = mDb.getDrawerChildCount();
				for(int drawerPos=0; drawerPos<drawerCount; drawerPos++)
		    	{
		    		String drawerTitle = mDb.getDrawerChild_Title(drawerPos);
		    		DrawerActivity.mFocus_drawerChildPos = drawerPos;
	
		    		// list all tab tables
		    		int tabsTableId = mDb.getTabsTableId(drawerPos);
		    		System.out.println("--- tabs table Id = " + tabsTableId +
									   ", drawer title = " + drawerTitle);
		    		mDb_tabs = new DB(context,tabsTableId);
		    		mDb_tabs.initTabsDb(mDb_tabs);
		    		int tabsCount = mDb_tabs.getTabsCount(true);
		        	for(int tabPos=0; tabPos<tabsCount; tabPos++)
		        	{
		        		TabsHostFragment.mCurrent_tabIndex = tabPos;
		        		int tabId = mDb_tabs.getTabId(tabPos, true);
		        		int notesTableId = mDb_tabs.getNotesTableId(tabPos, true);
		        		String tabTitle = mDb_tabs.getTabTitle(tabPos, true);
		        		System.out.println("   --- tab Id = " + tabId);
		        		System.out.println("   --- notes table Id = " + notesTableId);
		        		System.out.println("   --- tab title = " + tabTitle);
		        		
		        		mLastOkTabId = tabId;
		        		
		        		try {
	        				 mDb_notes = new DB(context,String.valueOf(notesTableId));
	        				 mDb_notes.initNotesDb(mDb_notes);
		        			 mDb_notes.doOpenNotes();
		        			 mDb_notes.doCloseNotes();
						} catch (Exception e) {
						}
		        	}
		    	}
				
				// recover focus
				int tabsTableId = Util.getPref_lastTimeView_tabs_tableId(this);
	    		DB.setFocus_tabsTableId(tabsTableId);
				String notesTableId = Util.getPref_lastTimeView_notes_tableId(this);
				DB.setFocus_notes_tableId(notesTableId);				
			}//if(ENABLE_DB_CHECK)
			
	        // get last time drawer number, default drawer number: 1
	        if (savedInstanceState == null)
	        {
	        	for(int i=0;i<mDb.getDrawerChildCount();i++)
	        	{
		        	if(	mDb.getTabsTableId(i)== 
		        		Util.getPref_lastTimeView_tabs_tableId(this))
		        	{
		        		mFocus_drawerChildPos =  i;
		    			System.out.println("DrawerActivity / onCreate /  mFocusDrawerId = " + mFocus_drawerChildPos);
		        	}
	        	}
	        	AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_STOP;
	        	UtilAudio.mIsCalledWhilePlayingAudio = false;
	        }
	
			if(mDb.getDrawerChildCount() == 0)
			{
				String drawerPrefix = "D";
		        for(int i=0;i< DB.DEFAULT_TABS_TABLE_COUNT;i++)
		        {
		        	String drawerTitle = drawerPrefix.concat(String.valueOf(i+1));
		        	mDrawerChildTitles.add(drawerTitle);
		        	mDb.insertDrawerChild(i+1, drawerTitle );
		        }
			}
			else
			{
			    for(int i=0;i< mDb.getDrawerChildCount();i++)
		        {
		        	mDrawerChildTitles.add(""); // init only
		        	mDrawerChildTitles.set(i, mDb.getDrawerChild_Title(i)); 
		        }
			}
	
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	        mDrawerListView = (DragSortListView) findViewById(R.id.left_drawer);
	
	        // set a custom shadow that overlays the main content when the drawer opens
	        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	        
	        // set adapter
	    	mDb.doOpenDrawer();
	    	Cursor cursor = DB.mCursor_drawerChild;
	        
	        String[] from = new String[] { DB.KEY_DRAWER_TITLE};
	        int[] to = new int[] { R.id.drawerText };
	        
	        drawerAdapter = new DrawerAdapter(
					this,
					R.layout.drawer_list_item,
					cursor,
					from,
					to,
					0
					);
	        
	        mDb.doCloseDrawer();
	        
	        mDrawerListView.setAdapter(drawerAdapter);
	   
	        // set up click listener
	        MainUi.addDrawerItemListeners();//??? move to resume?
	        mDrawerListView.setOnItemClickListener(MainUi.itemClick);
	        // set up long click listener
	        mDrawerListView.setOnItemLongClickListener(MainUi.itemLongClick);
	        
	        mController = DrawerListview.buildController(mDrawerListView);
	        mDrawerListView.setFloatViewManager(mController);
	        mDrawerListView.setOnTouchListener(mController);
	
	        // init drawer dragger
	    	mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
	    	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAWER_DRAGGABLE", "no")
	    			                    .equalsIgnoreCase("yes"))
	    		mDrawerListView.setDragEnabled(true);
	    	else
	    		mDrawerListView.setDragEnabled(false);
	        
	        mDrawerListView.setDragListener(DrawerListview.onDrag);
	        mDrawerListView.setDropListener(DrawerListview.onDrop);
	        
	        // enable ActionBar app icon to behave as action to toggle nav drawer
	        getActionBar().setDisplayHomeAsUpEnabled(true);
	        getActionBar().setHomeButtonEnabled(true);
	
	        // ActionBarDrawerToggle ties together the the proper interactions
	        // between the sliding drawer and the action bar app icon
	        mDrawerToggle = new ActionBarDrawerToggle(
		                this,                  /* host Activity */
		                mDrawerLayout,         /* DrawerLayout object */
		                R.drawable.ic_drawer,  /* navigation drawer image to replace 'Up' caret */
		                R.string.drawer_open,  /* "open drawer" description for accessibility */
		                R.string.drawer_close  /* "close drawer" description for accessibility */
	                ) 
	        {
	            public void onDrawerClosed(View view) 
	            {
	        		System.out.println("mDrawerToggle onDrawerClosed ");
	        		int pos = mDrawerListView.getCheckedItemPosition();
	        		int tblId = mDb.getTabsTableId(pos);
	        		DB.setSelected_tabsTableId(tblId);        		
	        		mDrawerChildTitle = mDb.getDrawerChild_Title(pos);
	                setTitle(mDrawerChildTitle);
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	                
	                // add for deleting drawer
	                if(TabsHostFragment.mTabHost == null)
	                {
	                	MainUi.selectDrawerChild(mFocus_drawerChildPos);
	            		setTitle(mDrawerChildTitle);
	                }
	            }
	
	            public void onDrawerOpened(View drawerView) 
	            {
	        		System.out.println("mDrawerToggle onDrawerOpened ");
	                setTitle(mAppTitle);
	                drawerAdapter.notifyDataSetChanged();
	                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
	            }
	        };
	        mDrawerLayout.setDrawerListener(mDrawerToggle);
	
	        mContext = getBaseContext();
	        bEnableConfig = false;
	        
	        fragmentManager = getSupportFragmentManager();
	        fragmentManager.addOnBackStackChangedListener(this);
	        
			// register an audio stream receiver
			if(noisyAudioStreamReceiver == null)
			{
				noisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
				intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY); 
				registerReceiver(noisyAudioStreamReceiver, intentFilter);
			}
			
		}
    }

    /*
     * Life cycle
     * 
     */
    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
       super.onSaveInstanceState(outState);
//  	   System.out.println("DrawerActivity / onSaveInstanceState / mFocusDrawerPos = " + mFocusDrawerPos);
       outState.putInt("CurrentDrawerIndex",mFocus_drawerChildPos);
       outState.putInt("CurrentPlaying_TabIndex",mCurrentPlaying_tabIndex);
       outState.putInt("CurrentPlaying_DrawerIndex",mCurrentPlaying_drawerIndex);
       outState.putInt("SeekBarProgress",NoteFragment.mProgress);
       outState.putInt("PlayerState",AudioPlayer.mPlayerState);
       outState.putBoolean("CalledWhilePlayingAudio", UtilAudio.mIsCalledWhilePlayingAudio);
       if(MainUi.mHandler != null)
    	   MainUi.mHandler.removeCallbacks(MainUi.mTabsHostRun);
       MainUi.mHandler = null;
    }
    
    // for After Rotate
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
		System.out.println("DrawerActivity / _onRestoreInstanceState ");
    	if(savedInstanceState != null)
    	{
    		mFocus_drawerChildPos = savedInstanceState.getInt("CurrentDrawerIndex");
    		mCurrentPlaying_tabIndex = savedInstanceState.getInt("CurrentPlaying_TabIndex");
    		mCurrentPlaying_drawerIndex = savedInstanceState.getInt("CurrentPlaying_DrawerIndex");
    		AudioPlayer.mPlayerState = savedInstanceState.getInt("PlayerState");
    		NoteFragment.mProgress = savedInstanceState.getInt("SeekBarProgress");
//    		System.out.println("DrawerActivity / onRestoreInstanceState / AudioPlayer.mPlayerState = " + AudioPlayer.mPlayerState);
    		UtilAudio.mIsCalledWhilePlayingAudio = savedInstanceState.getBoolean("CalledWhilePlayingAudio");
    	}    
    	
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	System.out.println("DrawerActivity / _onPause"); 
    }
    
    @Override
    protected void onResume() 
    {
    	System.out.println("DrawerActivity / _onResume"); 
    	
    	// mDrawerActivity will be destroyed after adding note with a YouTube link,
    	// so it is necessary to recreate activity
    	if(mDrawerActivity.isDestroyed())
    	{
    		System.out.println("DrawerActivity / _onResume / do recreate");
    		recreate();
    	}
    	
      	// To Registers a listener object to receive notification when incoming call 
     	TelephonyManager telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
     	if(telMgr != null) 
     	{
     		telMgr.listen(UtilAudio.phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
     	}         	 	
        super.onResume();
    }
	
    @Override
    protected void onResumeFragments() {
    	System.out.println("DrawerActivity / _onResumeFragments ");
    	super.onResumeFragments();
    	
    	MainUi.selectDrawerChild(mFocus_drawerChildPos);
    	setTitle(mDrawerChildTitle);
    }
    
    @Override
    protected void onDestroy() 
    {
    	System.out.println("DrawerActivity / onDestroy");
    	
    	//unregister TelephonyManager listener 
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(UtilAudio.phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        
		// unregister an audio stream receiver
		if(noisyAudioStreamReceiver != null)
		{
			try
			{
				unregisterReceiver(noisyAudioStreamReceiver);//??? unregister here? 
			}
			catch (Exception e)
			{
			}
			noisyAudioStreamReceiver = null;
		}        
		super.onDestroy();
    }
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        System.out.println("DrawerActivity / onPostCreate");
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        System.out.println("DrawerActivity / onConfigurationChanged");
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        System.out.println("DrawerActivity / onPrepareOptionsMenu");
        // If the navigation drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListView);
        if(drawerOpen)
        {
        	mMenu.setGroupVisible(0, false); 
    		mMenu.setGroupVisible(1, true); 
        }
        else
        {
            setTitle(mDrawerChildTitle);
    		mMenu.setGroupVisible(1, false);             
        }
        return super.onPrepareOptionsMenu(menu);
    }

	@Override
    public void setTitle(CharSequence title) {
    	super.setTitle(title);
    	setDrawerTitle(title);
    }

    public static void setDrawerTitle(CharSequence title) {
        if(title == null)
        {
        	title = mDrawerChildTitle;
        	fragmentManager.popBackStack();
        	initActionBar();
            mDrawerLayout.closeDrawer(mDrawerListView);
        }
        mDrawerActivity.getActionBar().setTitle(title);
    }	    
    
	/******************************************************
	 * Menu
	 * 
	 */
    // Menu identifiers
	static SharedPreferences mPref_show_note_attribute;
	/*
	 * onCreate Options Menu
	 */
	public static MenuItem mSubMenuItemAudio;
	MenuItem playOrStopMusicButton;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
//		System.out.println("DrawerActivity / onCreateOptionsMenu");
		mMenu = menu;
		//
		// Group 0 sub_menu for note operation
		// set sub menu 0: add new note
		//
	    SubMenu subMenu0 = menu.addSubMenu(0, 0, 0, R.string.add_new_note);//order starts from 0
	    
	    // add item
	    subMenu0.add(0, MainUi.Constant.ADD_TEXT, 1, R.string.note_text)
        		.setIcon(android.R.drawable.ic_menu_edit);
	    
	    // check camera feature
	    PackageManager packageManager = this.getPackageManager();
	    if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) 
	    {
	    	subMenu0.add(0, MainUi.Constant.ADD_CAMERA_PICTURE, 2, R.string.note_camera_image)
					.setIcon(android.R.drawable.ic_menu_camera);
	    	subMenu0.add(0, MainUi.Constant.ADD_CAMERA_VIDEO, 3,  R.string.note_camera_video)
					.setIcon(android.R.drawable.presence_video_online); //??? with better icon?
	    }
	    
	    subMenu0.add(0, MainUi.Constant.ADD_READY_PICTURE, 3, R.string.note_ready_image)
		.setIcon(android.R.drawable.ic_menu_gallery);	    
	    subMenu0.add(0, MainUi.Constant.ADD_READY_VIDEO, 4, R.string.note_ready_video)
		.setIcon(R.drawable.ic_ready_video);	   
	    subMenu0.add(0, MainUi.Constant.ADD_AUDIO, 5, R.string.note_audio)
        		.setIcon(R.drawable.ic_lock_ringer_on);
	    subMenu0.add(0, MainUi.Constant.ADD_YOUTUBE_LINK, 6, R.string.note_youtube_link)
				.setIcon(android.R.drawable.ic_menu_share);
	    subMenu0.add(0, MainUi.Constant.ADD_WEB_LINK, 7, R.string.note_web_link)
				.setIcon(android.R.drawable.ic_menu_share);
	    
	    // icon
	    MenuItem subMenuItem0 = subMenu0.getItem();
	    subMenuItem0.setIcon(R.drawable.ic_input_add);
		
	    // set sub menu display
		subMenuItem0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);	 
		
		//
		// set sub menu 1: Play music & slide show
		//
	    SubMenu subMenu1 = menu.addSubMenu(0, MainUi.Constant.OPEN_PLAY_SUBMENU, 1, R.string.menu_button_play);//order starts from 0
	    
	    // add item
	    subMenu1.add(0, MainUi.Constant.PLAY_OR_STOP_AUDIO, 1, R.string.menu_button_play_audio)
   				.setIcon(R.drawable.ic_media_play);	    	
	    playOrStopMusicButton = subMenu1.getItem(0);
		  
	    subMenu1.add(0, MainUi.Constant.SLIDE_SHOW, 3, R.string.menu_button_slide_show)
				.setIcon(R.drawable.ic_menu_play_clip);
	    
	    mSubMenuItemAudio = subMenu1.getItem();
		mSubMenuItemAudio.setIcon(R.drawable.ic_menu_slideshow);
		
	    // set sub menu display
		mSubMenuItemAudio.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                     MenuItem.SHOW_AS_ACTION_WITH_TEXT);	 
		
	    //
		// set sub menu 2: handle checked note
	    //
	    SubMenu subMenu2 = menu.addSubMenu(0, 0, 2, R.string.checked_notes);//order starts from 0
	    
	    // add item
	    subMenu2.add(0, MainUi.Constant.CHECK_ALL, 1, R.string.checked_notes_check_all)
        		.setIcon(R.drawable.btn_check_on_holo_dark);
	    subMenu2.add(0, MainUi.Constant.UNCHECK_ALL, 2, R.string.checked_notes_uncheck_all)
				.setIcon(R.drawable.btn_check_off_holo_dark);
	    subMenu2.add(0, MainUi.Constant.INVERT_SELECTED, 3, R.string.checked_notes_invert_selected)
				.setIcon(R.drawable.btn_check_on_focused_holo_dark);
	    subMenu2.add(0, MainUi.Constant.MOVE_CHECKED_NOTE, 4, R.string.checked_notes_move_to)
        		.setIcon(R.drawable.ic_menu_goto);	    
	    subMenu2.add(0, MainUi.Constant.COPY_CHECKED_NOTE, 5, R.string.checked_notes_copy_to)
        		.setIcon(R.drawable.ic_menu_copy_holo_dark);
	    subMenu2.add(0, MainUi.Constant.MAIL_CHECKED_NOTE, 6, R.string.mail_notes_btn)
        		.setIcon(android.R.drawable.ic_menu_send);
	    subMenu2.add(0, MainUi.Constant.DELETE_CHECKED_NOTE, 7, R.string.checked_notes_delete)
        		.setIcon(R.drawable.ic_menu_clear_playlist);
	    // icon
	    MenuItem subMenuItem2 = subMenu2.getItem();
	    subMenuItem2.setIcon(R.drawable.ic_menu_mark);
	    
	    // set sub menu display
		subMenuItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                    MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		
		//
		// set sub menu 3: overflow
		//
	    SubMenu subMenu3 = menu.addSubMenu(0, 0, 3, R.string.options);//order starts from 0
	    // add item
	    subMenu3.add(0, MainUi.Constant.ADD_NEW_PAGE, 1, R.string.add_new_page)
	            .setIcon(R.drawable.ic_menu_add_new_page);
	    
	    subMenu3.add(0, MainUi.Constant.CHANGE_PAGE_COLOR, 2, R.string.change_page_color)
        	    .setIcon(R.drawable.ic_color_a);
	    
	    subMenu3.add(0, MainUi.Constant.SHIFT_PAGE, 3, R.string.rearrange_page)
	            .setIcon(R.drawable.ic_dragger_h);
    	
	    // show body
	    mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
    	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
    		subMenu3.add(0, MainUi.Constant.SHOW_BODY, 4, R.string.preview_note_body_no)
     	   		    .setIcon(R.drawable.ic_media_group_collapse);
    	else
    		subMenu3.add(0, MainUi.Constant.SHOW_BODY, 4, R.string.preview_note_body_yes)
        	        .setIcon(R.drawable.ic_media_group_expand);
    	
    	// show draggable
	    mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);
    	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "no").equalsIgnoreCase("yes"))
    		subMenu3.add(0, MainUi.Constant.ENABLE_NOTE_DRAG_AND_DROP, 5, getResources().getText(R.string.draggable_no))
		    				.setIcon(R.drawable.ic_dragger_off);
    	else
    		subMenu3.add(0, MainUi.Constant.ENABLE_NOTE_DRAG_AND_DROP, 5, getResources().getText(R.string.draggable_yes))
    						.setIcon(R.drawable.ic_dragger_on);
    	
	    subMenu3.add(0, MainUi.Constant.SEND_PAGES, 6, R.string.mail_notes_title)
 	   			.setIcon(android.R.drawable.ic_menu_send);

	    subMenu3.add(0, MainUi.Constant.GALLERY, 7, R.string.gallery)
				.setIcon(android.R.drawable.ic_menu_gallery);	    
	    
	    subMenu3.add(0, MainUi.Constant.CONFIG_PREFERENCE, 8, R.string.settings)
	    	   .setIcon(R.drawable.ic_menu_preferences);
	    
	    // set icon
	    MenuItem subMenuItem3 = subMenu3.getItem();
	    subMenuItem3.setIcon(R.drawable.ic_menu_moreoverflow);
	    
	    // set sub menu display
		subMenuItem3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                     MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		//
	    // Group 1 sub_menu for drawer operation
		//
	    SubMenu subMenuDrawer = menu.addSubMenu(1, 0, 0, R.string.options);//order starts from 0
	    // add sub_menu item: add new folder
	    subMenuDrawer.add(0, MainUi.Constant.ADD_NEW_FOLDER, 1, R.string.add_new_drawer)
        			 .setIcon(android.R.drawable.ic_menu_add);
	    MenuItem subMenuItemDrawer1 = subMenuDrawer.getItem();
//	    subMenuItemDrawer.setIcon(R.drawable.ic_menu_moreoverflow);
	    subMenuItemDrawer1.setIcon(android.R.drawable.ic_menu_more);
	    // set sub menu display
	    subMenuItemDrawer1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | 
				                    	  MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	    // add sub_menu item: add drawer dragger setting
    	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAWER_DRAGGABLE", "no")
    								.equalsIgnoreCase("yes"))
    		subMenuDrawer.add(0, MainUi.Constant.ENABLE_DRAWER_DRAG_AND_DROP, 2, getResources().getText(R.string.draggable_no))
		    			 .setIcon(R.drawable.ic_dragger_off);
    	else
    		subMenuDrawer.add(0, MainUi.Constant.ENABLE_DRAWER_DRAG_AND_DROP, 2, getResources().getText(R.string.draggable_yes))
    					 .setIcon(R.drawable.ic_dragger_on);	    
	    
		return super.onCreateOptionsMenu(menu);
	}
	
	// set activity Enabled/Disabled
//	public static void setActivityEnabled(Context context,final Class<? extends Activity> activityClass,final boolean enable)
//    {
//	    final PackageManager pm=context.getPackageManager();
//	    final int enableFlag=enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
//	    pm.setComponentEnabledSetting(new ComponentName(context,activityClass),enableFlag,PackageManager.DONT_KILL_APP);
//    }
	
	/*
	 * on options item selected
	 * 
	 */
	public static SlideshowInfo slideshowInfo;
	static FragmentTransaction mFragmentTransaction;
	public static int mCurrentPlaying_notesTableId;
	public static int mCurrentPlaying_tabIndex;
	public static int mCurrentPlaying_drawerIndex;
	public static int mCurrentPlaying_drawerTabsTableId;
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) //??? java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    {
		// Go back: check if Configure fragment now
    	if( (item.getItemId() == android.R.id.home) && bEnableConfig)
    	{
    		System.out.println("DrawerActivity / onOptionsItemSelected / Home key of Config is pressed ");
    		fragmentManager.popBackStack();
    		initActionBar();
			setTitle(mDrawerChildTitle);
	        mDrawerLayout.closeDrawer(mDrawerListView);
            return true;
    	}
    	
    	// The action bar home/up action should open or close the drawer.
    	// ActionBarDrawerToggle will take care of this.
    	if (mDrawerToggle.onOptionsItemSelected(item))
    	{
    		System.out.println("mDrawerToggle.onOptionsItemSelected(item) / ActionBarDrawerToggle");
    		return true;
    	}
    	
    	final Intent intent;
        switch (item.getItemId()) 
        {
	    	case MainUi.Constant.ADD_NEW_FOLDER:
	    		MainUi.renewFirstAndLastDrawerId();
	    		MainUi.addNewFolder(mDrawerActivity, MainUi.mLastExist_drawerTabsTableId+1);
				return true;
				
	    	case MainUi.Constant.ENABLE_DRAWER_DRAG_AND_DROP:
            	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAWER_DRAGGABLE", "no")
            			                    .equalsIgnoreCase("yes"))
            	{
            		mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAWER_DRAGGABLE","no")
            								 .commit();
            		mDrawerListView.setDragEnabled(false);
            	}
            	else
            	{
            		mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAWER_DRAGGABLE","yes")
            								 .commit();
            		mDrawerListView.setDragEnabled(true);
            	}
            	drawerAdapter.notifyDataSetChanged();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                return true; 				
			
        	case MainUi.Constant.ADD_TEXT:
				intent = new Intent(this, Note_addNewText.class);
				new Note_addNew_optional(this, intent);
				return true;

        	case MainUi.Constant.ADD_CAMERA_PICTURE:
//        		setActivityEnabled(this,Note_addCameraPicture.class,true);
				intent = new Intent(this, Note_addCameraImage.class);
				new Note_addNew_optional(this, intent);
	            return true;

        	case MainUi.Constant.ADD_CAMERA_VIDEO:
				intent = new Intent(this, Note_addCameraVideo.class);
				new Note_addNew_optional(this, intent);
	            return true;	            
	            
        	case MainUi.Constant.ADD_READY_PICTURE:
				intent = new Intent(this, Note_addReadyImage.class); 
				new Note_addNew_optional_for_multiple(this, intent);
				return true;

        	case MainUi.Constant.ADD_READY_VIDEO:
				intent = new Intent(this, Note_addReadyVideo.class); 
				new Note_addNew_optional_for_multiple(this, intent);
				return true;
				
        	case MainUi.Constant.ADD_AUDIO:
				intent = new Intent(this, Note_addAudio.class); 
				new Note_addNew_optional_for_multiple(this, intent);
				return true;
        	
        	case MainUi.Constant.ADD_YOUTUBE_LINK:
	    		Intent intent_youtube_link = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com"));
	    		startActivity(intent_youtube_link);	    		
				return true;				

        	case MainUi.Constant.ADD_WEB_LINK:
	    		Intent intent_web_link = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.google.com"));
	    		startActivity(intent_web_link);	    		
				return true;				
				
        	case MainUi.Constant.OPEN_PLAY_SUBMENU:
        		// new play instance: stop button is off
        	    if( (AudioPlayer.mMediaPlayer != null) && 
        	    	(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP))
        		{
       		    	// show Stop
           			playOrStopMusicButton.setTitle(R.string.menu_button_stop_audio);
           			playOrStopMusicButton.setIcon(R.drawable.ic_media_stop);
        	    }
        	    else
        	    {
       		    	// show Play
           			playOrStopMusicButton.setTitle(R.string.menu_button_play_audio);
           			playOrStopMusicButton.setIcon(R.drawable.ic_media_play);        	    	
        	    }
        		return true;
        	
        	case MainUi.Constant.PLAY_OR_STOP_AUDIO:
        		if( (AudioPlayer.mMediaPlayer != null) &&
        			(AudioPlayer.mPlayerState != AudioPlayer.PLAYER_AT_STOP))
        		{
					UtilAudio.stopAudioPlayer();
					TabsHostFragment.setAudioPlayingTab_WithHighlight(false);
					NoteFragment.mItemAdapter.notifyDataSetChanged();
					NoteFragment.setFooter();
					return true; // just stop playing, wait for user action
        		}
        		else
        		{
        			AudioPlayer.mAudioPlayMode = AudioPlayer.CONTINUE_MODE;
        			AudioPlayer.mAudioIndex = 0;
       				AudioPlayer.prepareAudioInfo(this);
        			
        			AudioPlayer.manageAudioState(this);
        			
					NoteFragment.mItemAdapter.notifyDataSetChanged();
	        		NoteFragment.setFooter();
	        		
					// update notes table Id
					mCurrentPlaying_notesTableId = TabsHostFragment.mCurrent_notesTableId;
					// update playing tab index
					mCurrentPlaying_tabIndex = TabsHostFragment.mCurrent_tabIndex;
					// update playing drawer index
				    mCurrentPlaying_drawerIndex = mFocus_drawerChildPos;	        		
        		}
        		return true;

        	case MainUi.Constant.SLIDE_SHOW:
        		slideshowInfo = new SlideshowInfo();
    			
        		String notesTableId = Util.getPref_lastTimeView_notes_tableId(this);
    			DB.setFocus_notes_tableId(notesTableId);	
    			
        		// add images for slide show
    			mDb_notes.doOpenNotes();
        		for(int i=0;i< mDb_notes.getNotesCount(false) ;i++)
        		{
        			if(mDb_notes.getNoteMarking(i,false) == 1)
        			{
        				String pictureUri = mDb_notes.getNotePictureUri(i,false);
        				if((pictureUri.length() > 0) && UtilImage.hasImageExtension(pictureUri,this)) // skip empty
        					slideshowInfo.addImage(pictureUri);
        			}
        		}
        		mDb_notes.doCloseNotes();
        		          		
        		if(slideshowInfo.imageSize() > 0)
        		{
					// create new Intent to launch the slideShow player Activity
					Intent playSlideshow = new Intent(this, SlideshowPlayer.class);
					startActivity(playSlideshow);  
        		}
        		else
        			Toast.makeText(mContext,R.string.file_not_found,Toast.LENGTH_SHORT).show();
        		return true;
				
            case MainUi.Constant.ADD_NEW_PAGE:
            	System.out.println("--- MainUi.Constant.ADD_NEW_PAGE / TabsHostFragment.mLastExist_notesTableId = " + TabsHostFragment.mLastExist_notesTableId);
                MainUi.addNewPage(mDrawerActivity,TabsHostFragment.mLastExist_notesTableId + 1);
                
                return true;
                
            case MainUi.Constant.CHANGE_PAGE_COLOR:
            	MainUi.changePageColor(mDrawerActivity);
                return true;    
                
            case MainUi.Constant.SHIFT_PAGE:
            	MainUi.shiftPage(mDrawerActivity);
                return true;  
                
            case MainUi.Constant.SHOW_BODY:
            	mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
            	if(mPref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
            		mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY","no").commit();
            	else
            		mPref_show_note_attribute.edit().putString("KEY_SHOW_BODY","yes").commit();
            	TabsHostFragment.updateTabChange(this);
                return true; 

            case MainUi.Constant.ENABLE_NOTE_DRAG_AND_DROP:
            	mPref_show_note_attribute = mContext.getSharedPreferences("show_note_attribute", 0);
            	if(mPref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "no").equalsIgnoreCase("yes"))
            		mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAGGABLE","no").commit();
            	else
            		mPref_show_note_attribute.edit().putString("KEY_ENABLE_DRAGGABLE","yes").commit();
            	TabsHostFragment.updateTabChange(this);
                return true;                 
                
            case MainUi.Constant.SEND_PAGES:
				Intent intentSend = new Intent(this, SendMailAct.class);
				startActivity(intentSend);
				TabsHostFragment.updateTabChange(this);
            	return true;

            case MainUi.Constant.GALLERY:
				Intent i_browsePic = new Intent(this, GalleryGridAct.class);
				startActivity(i_browsePic);
            	return true; 	

            case MainUi.Constant.CONFIG_PREFERENCE:
            	mMenu.setGroupVisible(0, false); //hide the menu
        		setTitle(R.string.settings);
        		bEnableConfig = true;
        		
            	mConfigFragment = new Config();
            	mFragmentTransaction = fragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.content_frame, mConfigFragment).addToBackStack("config").commit();
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    
    /*
     *  on Back button pressed
     *
     */
    @Override
    public void onBackPressed()
    {
        System.out.println("DrawerActivity / _onBackPressed");
        if(!bEnableConfig)
        {
        	super.onBackPressed();
            // stop audio player
            UtilAudio.stopAudioPlayer();
        }
        else
        {
        	fragmentManager.popBackStack();
        	initActionBar();
			setTitle(mDrawerChildTitle);
	        mDrawerLayout.closeDrawer(mDrawerListView);
		}
    }
    
    static void initActionBar()
    {
		mConfigFragment = null;  
		bEnableConfig = false;
		mMenu.setGroupVisible(0, true);
		mDrawerActivity.getActionBar().setDisplayShowHomeEnabled(true);
		mDrawerActivity.getActionBar().setDisplayHomeAsUpEnabled(true);
		mDrawerToggle.setDrawerIndicatorEnabled(true); 
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		// Note: No need to keep AudioPlayer.audioIndex for NOT one-time-mode
//		//http://blog.shamanland.com/2014/01/nested-fragments-for-result.html
//        if( (requestCode & 0xffff /*to strip off the fragment index*/) 
//        	== Util.ACTIVITY_VIEW_NOTE ) 
//        {
//        	if (resultCode == Activity.RESULT_OK)
//        		AudioPlayer.audioIndex =  data.getIntExtra("audioIndexBack", AudioPlayer.audioIndex);
//        }  
	}	
	
	@Override
	public void onBackStackChanged() {
		int backStackEntryCount = fragmentManager.getBackStackEntryCount();
		System.out.println("--- DrawerActivity / _onBackStackChanged / backStackEntryCount = " + backStackEntryCount);
		 
		if(backStackEntryCount > 0){
			getActionBar().setDisplayShowHomeEnabled(false);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}else{
			initActionBar();
		}		
	}
}