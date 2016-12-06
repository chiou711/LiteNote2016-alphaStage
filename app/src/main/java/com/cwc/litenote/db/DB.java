package com.cwc.litenote.db;

import java.util.Date;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.alpha.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.util.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.widget.Toast;


public class DB   
{

	public static boolean HAS_DEFAULT_TABLES = true;
//	public static boolean HAS_DEFAULT_TABLES = false;
    private static Context mContext = null;
    private static DatabaseHelper mDbHelper ;
    public static SQLiteDatabase mSqlDb;
    
    // default count
	public static int DEFAULT_NOTES_TABLE_COUNT = 1;//5; // Notes1_1, Notes1_2, Notes1_3, Notes1_4, Notes1_5  
	public static int DEFAULT_TABS_TABLE_COUNT = 5;  // Tabs1, Tabs2, Tabs3
    
    // Table name format: Drawer, Tabs1, Notes1_2 
    static String DB_DRAWER_TABLE_NAME = "Drawer";
    private static String DB_TABS_TABLE_PREFIX = "Tabs";
    private static String DB_NOTES_TABLE_PREFIX = "Notes";
    private static String DB_NOTES_TABLE_NAME; // Note: name = prefix + id
    
    public static final String KEY_NOTE_ID = "_id"; //do not rename _id for using CursorAdapter 
    public static final String KEY_NOTE_TITLE = "note_title";
    public static final String KEY_NOTE_BODY = "note_body";
    public static final String KEY_NOTE_MARKING = "note_marking";
    public static final String KEY_NOTE_PICTURE_URI = "note_picture_uri";
    public static final String KEY_NOTE_AUDIO_URI = "note_audio_uri";
    public static final String KEY_NOTE_DRAWING_URI = "note_drawing_uri";
    public static final String KEY_NOTE_LINK_URI = "note_link_uri";
    public static final String KEY_NOTE_CREATED = "note_created";
    
    static final String KEY_TAB_ID = "tab_id"; //can rename _id for using BaseAdapter
    static final String KEY_TAB_TITLE = "tab_title";
    static final String KEY_TAB_NOTES_TABLE_ID = "tab_notes_table_id";
    static final String KEY_TAB_STYLE = "tab_style";
    static final String KEY_TAB_CREATED = "tab_created";
    
    static final String KEY_DRAWER_ID = "drawer_id"; //can rename _id for using BaseAdapter
//    static final String KEY_DRAWER_ID = "_id"; //BaseColumns._ID
    static final String KEY_DRAWER_TABS_TABLE_ID = "drawer_tabs_table_id"; //can rename _id for using BaseAdapter
    public static final String KEY_DRAWER_TITLE = "drawer_title";
    static final String KEY_DRAWER_CREATED = "drawer_created";
    
    public static DB mDb_drawer;
    public static DB mDb_tabs;    
    public static DB mDb_notes;    
	public static Cursor mCursor_drawerChild;
	public static Cursor mTabCursor;
	public static Cursor mNoteCursor;	

	private static int mTableId_tabs;
    private static String mTableId_notes;

    /** Constructor */
    
	// for drawer
	public DB(Context context) 
    {
        mContext = context;
    }

	public void initDrawerDb(DB db)
    {
    	mDb_drawer = db;
    }    
    
	// for tabs
    public DB(Context context, int tabsTableId) 
    {
        mContext = context;
        setFocus_tabsTableId(tabsTableId);  
	}

    public void initTabsDb(DB db)
    {
    	mDb_tabs = db;
    }
    
    // for notes
    public DB(Context context, String notesTableId) 
    {
        mContext = context;
        setFocus_notes_tableId(notesTableId);
    }
    
    public void initNotesDb(DB db)
    {
    	mDb_notes = db;
    }    
    
    public DB open() throws SQLException 
    {
        mDbHelper = new DatabaseHelper(mContext); 
        
        // Will call DatabaseHelper.onCreate()first time when WritableDatabase is not created yet
        mSqlDb = mDbHelper.getWritableDatabase();
        return DB.this;  
    }

    public void close() 
    {
        mDbHelper.close(); 
    }
    
    /*
     * DB functions
     * 
     */
	public void doOpenDrawer() 
	{
		this.open();
		mCursor_drawerChild = this.getDrawerChildCursor();
	}		

	public void doCloseDrawer()	
	{
		if((mCursor_drawerChild != null) && (!mCursor_drawerChild.isClosed()))
			mCursor_drawerChild.close();
		this.close();
	}	

	public void doOpenTabs(int i) 
	{
		this.open();
		try
		{
			mTabCursor = this.getTabsCursorByTabsTableId(i);
			
			// since no tab is created in table, delete the tabs table 
			if(mTabCursor.getCount() == 0)
			{
				int drawerId =  (int) getDrawerChildId(DrawerActivity.mFocus_drawerChildPos); 
				// since the tabs table does not exist, delete the tabs Id in drawer table
				deleteDrawerChildId(drawerId);
			}
		}
		catch (Exception e)
		{
			System.out.println("open tabs table NG! / table id = " + i);
			int drawerId =  (int) getDrawerChildId(DrawerActivity.mFocus_drawerChildPos); 
			// since the tabs table does not exist, delete the tabs Id in drawer table
			deleteDrawerChildId(drawerId);
		}
	}
	
	public void doCloseTabs()	
	{
		if((mTabCursor != null) && (!mTabCursor.isClosed()))
			mTabCursor.close();
		this.close();
	}		
	
	public void doOpenNotes() 
	{
		this.open();
		
		//try to get notes cursor 
		//??? unknown reason, last view notes table id could be changed and then cause 
		// an exception when getting this cursor
		// workaround: to apply an existing notes table that is found firstly
		try
		{
			mNoteCursor = this.getNotesCursor();
//			System.out.println("open notes table OK / table name = " + DB_NOTES_TABLE_NAME);
		}
		catch(Exception e)
		{
			System.out.println("open notes table NG! / table name = " + DB_NOTES_TABLE_NAME);
			 
			 // since the notes table does not exist, delete the tab in tabs table
			System.out.println("   getFocusTabsTableName() = " + getFocusTabsTableName());
			System.out.println("   TabsHostFragment.mCurrentTabIndex = " + TabsHostFragment.mCurrent_tabIndex);
			deleteTab(getFocusTabsTableName(),DrawerActivity.mLastOkTabId);
			 
			// find a new one last view notes table, if notes table dose not exist
			int drawerCount = getDrawerChildCount();
			
			for(int drawerPos=0; drawerPos< drawerCount; drawerPos++)
			{
				doOpenDrawer();
				if(mCursor_drawerChild.moveToPosition(drawerPos) )
				{
					int tabs_tableId = getTabsTableId(drawerPos);
					System.out.println("DB / find drawer_TabsTableId = " + tabs_tableId);			
					setFocus_tabsTableId(tabs_tableId);
					Util.setPref_lastTimeView_tabs_tableId(DrawerActivity.mDrawerActivity, tabs_tableId);
					
					int tabsCount = getTabsCount(true);
					for(int tabPos=0; tabPos < tabsCount; tabPos++)
					{
						if(mTabCursor.moveToPosition(tabPos))
						{
							int notes_tableId = getNotesTableId(tabPos,true);
							System.out.println("DB / find notesTableId = " + notes_tableId);
							setFocus_notes_tableId(String.valueOf(notes_tableId));
							Util.setPref_lastTimeView_notes_tableId(DrawerActivity.mDrawerActivity, notes_tableId);
							doCloseTabs();
							doCloseDrawer();
							DrawerActivity.mDrawerActivity.recreate();
							return;
						}
					}//for
				}
				doCloseDrawer();
			}//for
		}//catch
	}

	public void doCloseNotes()	
	{
		if((mNoteCursor != null)&& (!mNoteCursor.isClosed()))
			mNoteCursor.close();//fix: Could not allocate CursorWindow '/data/data/com.cwc.litenote/databases/notes.db' of size 2097152 due to error -12.
		
		this.close();
	}
	
    // delete DB
	public static void deleteDB()
	{
        mSqlDb = mDbHelper.getWritableDatabase();
        try {
	    	mSqlDb.beginTransaction();
	        mContext.deleteDatabase(DatabaseHelper.DB_NAME);
	        mSqlDb.setTransactionSuccessful();
	    }
	    catch (Exception e) {
	    }
	    finally {
	    	Toast.makeText(mContext,R.string.config_delete_DB_toast,Toast.LENGTH_SHORT).show();
	    	mSqlDb.endTransaction();
	    }
	}         
	
	public static boolean isTableExisted(String tableName) 
	{
	    Cursor cursor = mSqlDb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
	    if(cursor!=null) 
	    {
	        if(cursor.getCount()>0) 
	        {
	        	cursor.close();
	            return true;
	        }
	        cursor.close();
	    }
	    return false;
	}		
	
    /**
     *  Notes table
     * 
     */
    // table columns: for note
    String[] strNoteColumns = new String[] {
          KEY_NOTE_ID,
          KEY_NOTE_TITLE,
          KEY_NOTE_PICTURE_URI,
          KEY_NOTE_AUDIO_URI,
          KEY_NOTE_DRAWING_URI,
          KEY_NOTE_LINK_URI,
          KEY_NOTE_BODY,
          KEY_NOTE_MARKING,
          KEY_NOTE_CREATED
      };

    //insert new notes table by 
    // 1 SQLiteDatabase
    // 2 assigned drawer Id
    // 3 notes table Id
    public static void insertNotesTable(DB db, int drawerId, int notesId, boolean is_SQLiteOpenHelper_onCreate)
    {   
    	if(!is_SQLiteOpenHelper_onCreate)
    		db.open();
		//format "Notes1_2"
    	DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(drawerId)+
    														"_"+
    														String.valueOf(notesId));
        String dB_insert_table = "CREATE TABLE IF NOT EXISTS " + DB_NOTES_TABLE_NAME + "(" +
        							KEY_NOTE_ID + " INTEGER PRIMARY KEY," +
        							KEY_NOTE_TITLE + " TEXT," +  
        							KEY_NOTE_PICTURE_URI + " TEXT," +  
        							KEY_NOTE_AUDIO_URI + " TEXT," +  
        							KEY_NOTE_DRAWING_URI + " TEXT," +  
        							KEY_NOTE_LINK_URI + " TEXT," +  
        							KEY_NOTE_BODY + " TEXT," +
        							KEY_NOTE_MARKING + " INTEGER," +
        							KEY_NOTE_CREATED + " INTEGER);";
        mSqlDb.execSQL(dB_insert_table); 

        if(!is_SQLiteOpenHelper_onCreate)
        	db.close();
    }

    //delete notes table
    public void dropNotesTable(int id)
    {   
    	this.open();
		//format "Notes1_2"
    	DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(getFocus_tabsTableId())+"_"+String.valueOf(id));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_NOTES_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);         
    	this.close();
    }   
    
    //delete notes table by drawer tabs table Id
    public void dropNotesTable(int drawerTabsTableId, int id)
    {   
    	this.open();
		//format "Notes1_2"
    	DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(drawerTabsTableId)+"_"+String.valueOf(id));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_NOTES_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);         
    	this.close();
    } 
    
    // select all notes
    public Cursor getNotesCursor() {
        return mSqlDb.query(DB_NOTES_TABLE_NAME, 
             strNoteColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    //set note table id
    public static void setFocus_notes_tableId(String id)
    {
    	mTableId_notes = id;
    	// table number initialization: name = prefix + id
        DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(getFocus_tabsTableId())+
        													"_"+
        													mTableId_notes);
//    	System.out.println("DB / _setFocus_NotesTableId / mNotesTableId = " + mNotesTableId);
    }  
    
    //set note table id
    public static void setSelected_NotesTableId(String id)
    {
    	mTableId_notes = id;
    	// table number initialization: name = prefix + id
        DB_NOTES_TABLE_NAME = DB_NOTES_TABLE_PREFIX.concat(String.valueOf(getFocus_tabsTableId())+"_"+mTableId_notes);
    	System.out.println("DB / _setNoteTableId mNoteTableId=" + mTableId_notes);
    }  
    
    //get note table id
    public static String getFocus_notes_tableId()
    {
    	return mTableId_notes;
    }  
    
    // Insert note
    // createTime: 0 will update time
    public long insertNote(String title,String pictureUri, String audioUri, String drawingUri, String linkUri, String body, int marking, Long createTime)
    { 
    	this.open();
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_NOTE_TITLE, title);   
        args.put(KEY_NOTE_PICTURE_URI, pictureUri);
        args.put(KEY_NOTE_AUDIO_URI, audioUri);
        args.put(KEY_NOTE_DRAWING_URI, drawingUri);
        args.put(KEY_NOTE_LINK_URI, linkUri);
        args.put(KEY_NOTE_BODY, body);
        if(createTime == 0)
        	args.put(KEY_NOTE_CREATED, now.getTime());
        else
        	args.put(KEY_NOTE_CREATED, createTime);
        	
        args.put(KEY_NOTE_MARKING,marking);
        long rowId = mSqlDb.insert(DB_NOTES_TABLE_NAME, null, args);
        this.close();
        return rowId;  
    }  
    
    public boolean deleteNote(long rowId,boolean enDbOpenClose) 
    {
    	if(enDbOpenClose)
    		this.open();
    	int rowsEffected = mSqlDb.delete(DB_NOTES_TABLE_NAME, KEY_NOTE_ID + "=" + rowId, null);
        if(enDbOpenClose)
        	this.close();
        return (rowsEffected > 0)?true:false;
    }    
    
    //query note
    public Cursor queryNote(long rowId) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
					                DB_NOTES_TABLE_NAME,
					                new String[] {KEY_NOTE_ID,
				  								  KEY_NOTE_TITLE,
				  								  KEY_NOTE_PICTURE_URI,
				  								  KEY_NOTE_AUDIO_URI,
				  								  KEY_NOTE_DRAWING_URI,
				  								  KEY_NOTE_LINK_URI,
        										  KEY_NOTE_BODY,
        										  KEY_NOTE_MARKING,
        										  KEY_NOTE_CREATED},
					                KEY_NOTE_ID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    // update note
    // 		createTime:  0 for Don't update time
    public boolean updateNote(long rowId, String title, String pictureUri, String audioUri, String drawingUri, 
    						  String linkUri, String body, long marking, long createTime,boolean enDbOpenClose) 
    {
    	if(enDbOpenClose)
    		this.open();
        ContentValues args = new ContentValues();
        args.put(KEY_NOTE_TITLE, title);
        args.put(KEY_NOTE_PICTURE_URI, pictureUri);
        args.put(KEY_NOTE_AUDIO_URI, audioUri);
        args.put(KEY_NOTE_DRAWING_URI, drawingUri);
        args.put(KEY_NOTE_LINK_URI, linkUri);
        args.put(KEY_NOTE_BODY, body);
        args.put(KEY_NOTE_MARKING, marking);
        
        Cursor cursor = queryNote(rowId);
        if(createTime == 0)
        	args.put(KEY_NOTE_CREATED, cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED)));
        else
        	args.put(KEY_NOTE_CREATED, createTime);

        int cUpdateItems = mSqlDb.update(DB_NOTES_TABLE_NAME, args, KEY_NOTE_ID + "=" + rowId, null);
        if(enDbOpenClose)
        	this.close();
        return cUpdateItems > 0;
    }    
    
    
	public int getNotesCount(boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		int count = mNoteCursor.getCount();
		if(enDbOpenClose)
			mDb_notes.doCloseNotes();
		return count;
	}	
	
	public int getCheckedNotesCount()
	{
		int countCheck =0;
		mDb_notes.doOpenNotes();
		int notesCount = getNotesCount(false); 
		for(int i=0;i< notesCount ;i++)
		{
			if(getNoteMarking(i,false) == 1)//??? why exception
				countCheck++;
		}
		mDb_notes.doCloseNotes();
		return countCheck;
	}		
	
	
	// get note by Id
	public String getNoteLinkById(Long mRowId) 
	{
		this.open();
		String link = queryNote(mRowId).getString(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_LINK_URI));
		this.close();
		return link;
	}	
	
	public String getNoteTitleById(Long mRowId) 
	{
		this.open();
		String title = queryNote(mRowId).getString(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_TITLE));
		this.close();
		return title;
	}
	
	public String getNoteBodyById(Long mRowId) 
	{
		this.open();
		String id = queryNote(mRowId).getString(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_BODY));
		this.close();
		return id;
	}

	public String getNotePictureUriById(Long mRowId)
	{
		this.open();
        String pictureUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB.KEY_NOTE_PICTURE_URI));
		this.close();
		return pictureUri;
	}
	
	public String getNotePictureUriById(Long mRowId,boolean enOpen,boolean enClose)
	{
		if(enOpen)
			this.open();
        String pictureUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB.KEY_NOTE_PICTURE_URI));
		if(enClose)
			this.close();
		return pictureUri;
	}	
	
	public String getNoteAudioUriById(Long mRowId)
	{
		this.open();
		String audioUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB.KEY_NOTE_AUDIO_URI));
		this.close();
		return audioUri;
	}	
	
	public String getNoteDrawingUriById(Long mRowId)
	{
		this.open();
		String drawingUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB.KEY_NOTE_DRAWING_URI));
		this.close();
		return drawingUri;
	}	
	
	public String getNoteLinkUriById(Long mRowId)
	{
		this.open();
		String linkUri = queryNote(mRowId).getString(queryNote(mRowId)
														.getColumnIndexOrThrow(DB.KEY_NOTE_LINK_URI));
		this.close();
		return linkUri;
	}		
	
	public Long getNoteMarkingById(Long mRowId) 
	{
		this.open();
		Long marking = queryNote(mRowId).getLong(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_MARKING));
		this.close();
		return marking;
		
	}

	public Long getNoteCreatedTimeById(Long mRowId)
	{
		this.open();
		Long time = queryNote(mRowId).getLong(queryNote(mRowId)
											.getColumnIndexOrThrow(DB.KEY_NOTE_CREATED));
		this.close();
		return time;
	}

	// get note by position
	public Long getNoteId(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
	    Long id = mNoteCursor.getLong(mNoteCursor.getColumnIndex(KEY_NOTE_ID));
	    if(enDbOpenClose)
	    	mDb_notes.doCloseNotes();
        return id;
	}	
	
	public String getNoteTitle(int position,boolean enDbOpenClose)
	{
		String title = null;
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		if(mNoteCursor.moveToPosition(position))
			title = mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_TITLE));
        if(enDbOpenClose)
        	mDb_notes.doCloseNotes();
        return title;
	}	
	
	public String getNoteBody(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
	    String body = mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_BODY));
        if(enDbOpenClose)
        	mDb_notes.doCloseNotes();
        return body;
	}
	
	public String getNotePictureUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
        String pictureUri = mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_PICTURE_URI));
        if(enDbOpenClose)
        	mDb_notes.doCloseNotes();
        return pictureUri;
	}
	
	public String getNoteAudioUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
        String audioUri = mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_AUDIO_URI));
        if(enDbOpenClose)
        	mDb_notes.doCloseNotes();
        return audioUri;
	}	
	
	public String getNoteDrawingUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose) 
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
        String drawingUri = mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_DRAWING_URI));
        if(enDbOpenClose)
        	mDb_notes.doCloseNotes();
        return drawingUri;
	}	
	
	public String getNoteLinkUri(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose) 
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
        String linkUri = mNoteCursor.getString(mNoteCursor.getColumnIndex(KEY_NOTE_LINK_URI));
        if(enDbOpenClose)
        	mDb_notes.doCloseNotes();
        return linkUri;
	}	
	
	public int getNoteMarking(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
		int marking = mNoteCursor.getInt(mNoteCursor.getColumnIndex(KEY_NOTE_MARKING));
		if(enDbOpenClose)
			mDb_notes.doCloseNotes();
		return marking;
	}
	
	public Long getNoteCreatedTime(int position,boolean enDbOpenClose)
	{
		if(enDbOpenClose)
			mDb_notes.doOpenNotes();
		mNoteCursor.moveToPosition(position);
		Long time = mNoteCursor.getLong(mNoteCursor.getColumnIndex(KEY_NOTE_CREATED));
		if(enDbOpenClose)
			mDb_notes.doCloseNotes();
		return time;
	}
	
    /*
     * Tabs 
     * 
     */
	
    // table columns: for tab info
    String[] strTabColumns = new String[] {
            KEY_TAB_ID,
            KEY_TAB_TITLE,
            KEY_TAB_NOTES_TABLE_ID,
            KEY_TAB_STYLE,
            KEY_TAB_CREATED
        };   

    // select tabs cursor
    public Cursor getTabsCursorByTabsTableId(int i) {
        return mSqlDb.query(DB_TABS_TABLE_PREFIX + String.valueOf(i), 
             strTabColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }     
    
    // insert tabs table
    public static void insertTabsTable(DB db, int id, boolean is_SQLiteOpenHelper_onCreate)
    {
    	if(!is_SQLiteOpenHelper_onCreate)
    		db.doOpenDrawer();
    	
    	// table for Tabs
		String tableCreated = DB_TABS_TABLE_PREFIX.concat(String.valueOf(id));
        String DB_CREATE = "CREATE TABLE IF NOT EXISTS " + tableCreated + "(" + 
		            		KEY_TAB_ID + " INTEGER PRIMARY KEY," +
		            		KEY_TAB_TITLE + " TEXT," +
		            		KEY_TAB_NOTES_TABLE_ID + " INTEGER," +
		            		KEY_TAB_STYLE + " INTEGER," +
		            		KEY_TAB_CREATED + " INTEGER);";
        mSqlDb.execSQL(DB_CREATE);  
        
        if(DB.HAS_DEFAULT_TABLES)
        {
        	// set default tab info
	        if(!is_SQLiteOpenHelper_onCreate)
	        {
	    		String tabs_table = DB_TABS_TABLE_PREFIX.concat(String.valueOf(id));
	    		insertTab(mSqlDb,tabs_table,"N1",1,"N1",1,1);
	        }
        }
        else
        {
        	String tabs_table = DB_TABS_TABLE_PREFIX.concat(String.valueOf(id));
        	insertTab(mSqlDb,tabs_table,"N1",1,"N1",1,0);
        	//insertTab(mSqlDb,tabs_table,"N2",2,"N2",2,1); 
        	//insertTab(mSqlDb,tabs_table,"N3",3,"N3",3,2); 
        	//insertTab(mSqlDb,tabs_table,"N4",4,"N4",4,3); 
        	//insertTab(mSqlDb,tabs_table,"N5",5,"N5",5,4);  
        }
        
		if(!is_SQLiteOpenHelper_onCreate)
			db.doCloseDrawer();
    }
    
    // delete tabs table
    public void dropTabsTable(int tableId)
    {
    	this.open();
		//format "Tabs1"
    	String DB_TABS_TABLE_NAME = DB_TABS_TABLE_PREFIX.concat(String.valueOf(tableId));
        String dB_drop_table = "DROP TABLE IF EXISTS " + DB_TABS_TABLE_NAME + ";";
        mSqlDb.execSQL(dB_drop_table);
        this.close();
    }
    
    // insert tab with SqlDb parameter
    public static long insertTab(SQLiteDatabase sqlDb, String intoTable,String title,long ntId, String plTitle, long plId, int style) 
    { 
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_TAB_TITLE, title);
        args.put(KEY_TAB_NOTES_TABLE_ID, ntId);
        args.put(KEY_TAB_STYLE, style);
        args.put(KEY_TAB_CREATED, now.getTime());
        return sqlDb.insert(intoTable, null, args);  
    }    
    
    // insert tab
    public long insertTab(String intoTable,String title,long ntId, int style) 
    { 
    	this.open();
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_TAB_TITLE, title);
        args.put(KEY_TAB_NOTES_TABLE_ID, ntId);
        args.put(KEY_TAB_STYLE, style);
        args.put(KEY_TAB_CREATED, now.getTime());
        long rowId = mSqlDb.insert(intoTable, null, args);
        this.close();
        return rowId;
        
    }        
    
    // delete tab
    public long deleteTab(String table,int tabId) 
    { 
    	System.out.println("DB / deleteTab / table = " + table + ", tab Id = " + tabId);
    	this.open();
        long rowsNumber = mSqlDb.delete(table, KEY_TAB_ID + "='" + tabId +"'", null);  
        this.close();
        if(rowsNumber > 0)
        	System.out.println("DB / deleteTab / rowsNumber =" + rowsNumber);
        else
        	System.out.println("DB / deleteTab / failed to delete");
        return rowsNumber;
    }

    //query single tab info
    public Cursor queryTab(String table, long id) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
        							table,
					                new String[] {KEY_TAB_ID,
        										  KEY_TAB_TITLE,
        										  KEY_TAB_NOTES_TABLE_ID,
        										  KEY_TAB_STYLE,
        										  KEY_TAB_CREATED},
					                KEY_TAB_ID + "=" + id,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }

        return mCursor;
    }
    
    //update tab
    public boolean updateTab(long id, String title, long ntId, int style) 
    { 
    	this.open();
        ContentValues args = new ContentValues();
        Date now = new Date(); 
        args.put(KEY_TAB_TITLE, title);
        args.put(KEY_TAB_NOTES_TABLE_ID, ntId);
        args.put(KEY_TAB_STYLE, style);
        args.put(KEY_TAB_CREATED, now.getTime());
        int rowsNumber = mSqlDb.update(DB_TABS_TABLE_PREFIX+String.valueOf(getFocus_tabsTableId()), args, KEY_TAB_ID + "=" + id, null);
        this.close();
        return  (rowsNumber>0)?true:false;
    }    

    public Cursor getTabsCursor()
    {
		return mTabCursor;
    }
    
	public int getTabsCount(boolean enDbOpenClose)	
	{
		if(enDbOpenClose)
			mDb_tabs.doOpenTabs(mTableId_tabs);
		int count = mTabCursor.getCount();
		if(enDbOpenClose)
			mDb_tabs.doCloseTabs();
		return count;
	}

	public int getTabId(int position, boolean enDbOpenClose) 
	{
//		System.out.println("DB / getTabId / position = " + position);
//		System.out.println("DB / getTabId / mTabsTableId = " + mTabsTableId);
		if(enDbOpenClose)
			mDb_tabs.doOpenTabs(mTableId_tabs);
		
		if(mTabCursor.moveToPosition(position))//??? why exception
		{
			int tabId = mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_ID));
//			System.out.println("DB / getTabId / tabId = " + tabId);
			if(enDbOpenClose)
				mDb_tabs.doCloseTabs();
			return tabId;
		}
		else
		{
			if(enDbOpenClose)
				mDb_tabs.doCloseTabs();
	        return -1;
		}
	}

    //get current tab info title
    public String getCurrentTabTitle()
    {
    	String title = null;
    	
    	doOpenTabs(mTableId_tabs);
    	int tabsCount = getTabsCount(false);
    	for(int i=0;i< tabsCount; i++ )
    	{
    		if( Integer.valueOf(getFocus_notes_tableId()) == getNotesTableId(i,false))
    		{
    			title = getTabTitle(i,false);
    		}
    	}
    	doCloseTabs();
    	return title;
    }     	

	public int getNotesTableId(int position,boolean enDbOpenClose)	
	{
		if(enDbOpenClose)
			mDb_tabs.doOpenTabs(mTableId_tabs);
		mTabCursor.moveToPosition(position);
        int id = mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_NOTES_TABLE_ID));
        if(enDbOpenClose)
        	mDb_tabs.doCloseTabs();
        return id;
	}
	
	public String getTabTitle(int position, boolean enDbOpenClose) 
	{
		if(enDbOpenClose)
			mDb_tabs.doOpenTabs(mTableId_tabs);
		mTabCursor.moveToPosition(position);
        String title = mTabCursor.getString(mTabCursor.getColumnIndex(KEY_TAB_TITLE));
        if(enDbOpenClose)
        	mDb_tabs.doCloseTabs();
        return title;
	}
	
	public int getTabStyle(int position, boolean enDbOpenClose)	
	{
		int style = 0;
		if(enDbOpenClose)
			mDb_tabs.doOpenTabs(mTableId_tabs);
		if(mTabCursor.moveToPosition(position))
			style = mTabCursor.getInt(mTabCursor.getColumnIndex(KEY_TAB_STYLE));
		if(enDbOpenClose)
			mDb_tabs.doCloseTabs();
		return style;
	}
    
	/*
	 * Drawer
	 * 
	 * 
	 */
    
    // table columns: for drawer
    String[] strDrawerColumns = new String[] {
        KEY_DRAWER_ID + " AS " + BaseColumns._ID,
    	KEY_DRAWER_TABS_TABLE_ID,
    	KEY_DRAWER_TITLE,
    	KEY_DRAWER_CREATED
      };
    
    
    public Cursor getDrawerChildCursor() {
        return mSqlDb.query(DB_DRAWER_TABLE_NAME, 
        	 strDrawerColumns,
             null, 
             null, 
             null, 
             null, 
             null  
             );    
    }   
    
    //query note
    public Cursor queryDrawer(long rowId) throws SQLException 
    {  
        Cursor mCursor = mSqlDb.query(true,
					                DB_DRAWER_TABLE_NAME,
        							strDrawerColumns,
        							KEY_DRAWER_ID + "=" + rowId,
					                null, null, null, null, null);

        if (mCursor != null) { 
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public long insertDrawerChild(int tableId, String title) 
    { 
    	this.open();
        Date now = new Date();  
        ContentValues args = new ContentValues(); 
        args.put(KEY_DRAWER_TABS_TABLE_ID, tableId);
        args.put(KEY_DRAWER_TITLE, title);
        args.put(KEY_DRAWER_CREATED, now.getTime());
        long rowId = mSqlDb.insert(DB_DRAWER_TABLE_NAME, null, args);
        this.close();
        return rowId;  
    }  
    
    public long deleteDrawerChildId(int id) 
    { 
    	this.open();
        long rowsNumber = mSqlDb.delete(DB_DRAWER_TABLE_NAME, KEY_DRAWER_ID + "='" + id +"'", null);
        this.close();
        return  rowsNumber;
    }
    
    
    // update drawer
    public boolean updateDrawer(long rowId, int drawerTabsTableId, String drawerTitle) {
    	this.open();
        ContentValues args = new ContentValues();
        Date now = new Date();  
        args.put(KEY_DRAWER_TABS_TABLE_ID, drawerTabsTableId);
        args.put(KEY_DRAWER_TITLE, drawerTitle);
       	args.put(KEY_DRAWER_CREATED, now.getTime());

        int cUpdateItems = mSqlDb.update(DB_DRAWER_TABLE_NAME, args, KEY_DRAWER_ID + "=" + rowId, null);
        boolean bUpdate = cUpdateItems > 0? true:false;
        this.close();
        return bUpdate;
    }    
    
    public long getDrawerChildId(int position)
    {
    	mDb_drawer.doOpenDrawer();
    	mCursor_drawerChild.moveToPosition(position);
    	// note: KEY_DRAWER_ID + " AS " + BaseColumns._ID 
    	long column = (long) mCursor_drawerChild.getInt(mCursor_drawerChild.getColumnIndex(BaseColumns._ID));
    	mDb_drawer.doCloseDrawer();
        return column;
    }
    
    public static void setFocus_tabsTableId(int i)
    {
    	mTableId_tabs = i;
    }
    
    public static int getFocus_tabsTableId()
    {
    	return mTableId_tabs;
    }
    
    public static void setSelected_tabsTableId(int i)
    {
    	mTableId_tabs = i;
    }
    
    static int getSelected_tabsTableId()
    {
    	return mTableId_tabs;
    }    
    

    public int getDrawerChildCount()
    {
    	mDb_drawer.doOpenDrawer();
    	int count = mCursor_drawerChild.getCount();
    	mDb_drawer.doCloseDrawer();
    	return count;
    }
    
    public int getTabsTableId(int position)
    {
    	mDb_drawer.doOpenDrawer();
		mCursor_drawerChild.moveToPosition(position);
		int id = mCursor_drawerChild.getInt(mCursor_drawerChild.getColumnIndex(KEY_DRAWER_TABS_TABLE_ID));
		mDb_drawer.doCloseDrawer();
        return id;
    	
    }
    
	public String getDrawerChild_Title(int position)	
	{
		mDb_drawer.doOpenDrawer();
		mCursor_drawerChild.moveToPosition(position);
		String str = mCursor_drawerChild.getString(mCursor_drawerChild.getColumnIndex(KEY_DRAWER_TITLE)); 
		mDb_drawer.doCloseDrawer();
        return str;
	}
	
	// get drawer title by Id
	public String getDrawerChildTitleById(Long mRowId) 
	{
		return queryDrawer(mRowId).getString(queryDrawer(mRowId)
											.getColumnIndexOrThrow(DB.KEY_DRAWER_TITLE));
	}
    
	// get current tabs table name
	public static String getFocusTabsTableName()
	{
		return DB.DB_TABS_TABLE_PREFIX + DB.getFocus_tabsTableId();
	}
}