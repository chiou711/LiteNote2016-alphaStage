package com.cwc.litenote.db;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Data Base Helper 
class DatabaseHelper extends SQLiteOpenHelper
{  
    static final String DB_NAME = "notes.db";
    private static int DB_VERSION = 1;
    
    public DatabaseHelper(Context context) 
    {  
        super(context, DB_NAME , null, DB_VERSION);
    }

    @Override
    //Called when the database is created ONLY for the first time.
    public void onCreate(SQLiteDatabase sqlDb)
    {   
    	String tableCreated;
    	String DB_CREATE;
    	
    	// WritableDatabase(i.e. sqlDb) is created
    	DB.mSqlDb = sqlDb; 
    	
    	System.out.println("DatabaseHelper / _onCreate");
    	
    	if(!DB.HAS_DEFAULT_TABLES)
    	{
	    	// tables for notes
	    	for(int i=1; i<= DB.DEFAULT_TABS_TABLE_COUNT; i++)
	    	{
	        	System.out.println("DatabaseHelper / _onCreate / will insert tabs table " + i);
	        	for(int j=1; j<=DB.DEFAULT_NOTES_TABLE_COUNT; j++)
	        	{
	            	System.out.println("DatabaseHelper / _onCreate / will insert note table " + j);
	        		DB.insertNotesTable(DB.mDb_drawer, i, j, true);
	        	}
	    	}
    	}
    	
    	// table for Tabs
    	for(int i=1;i<= DB.DEFAULT_TABS_TABLE_COUNT; i++)
    	{
    		DB.insertTabsTable(DB.mDb_drawer, i, true);
    	}
    	
    	// table for Drawer
    	tableCreated = DB.DB_DRAWER_TABLE_NAME;
        DB_CREATE = "CREATE TABLE IF NOT EXISTS " + tableCreated + "(" + 
        					DB.KEY_DRAWER_ID + " INTEGER PRIMARY KEY," +
        					DB.KEY_DRAWER_TABS_TABLE_ID + " INTEGER," +
        					DB.KEY_DRAWER_TITLE + " TEXT," +
        					DB.KEY_DRAWER_CREATED + " INTEGER);";
        sqlDb.execSQL(DB_CREATE);  
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    { //??? how to upgrade?
//            db.execSQL("DROP DATABASE IF EXISTS "+DATABASE_TABLE); 
//            System.out.println("DB / _onUpgrade / drop DB / DATABASE_NAME = " + DB_NAME);
 	    onCreate(db);
    }
    
    @Override
    public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion)
    { 
//            db.execSQL("DROP DATABASE IF EXISTS "+DATABASE_TABLE); 
//            System.out.println("DB / _onDowngrade / drop DB / DATABASE_NAME = " + DB_NAME);
 	    onCreate(db);
    }
}
