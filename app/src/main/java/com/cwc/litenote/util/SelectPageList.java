package com.cwc.litenote.util;

import java.util.ArrayList;
import java.util.List;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.alpha.R;
import com.cwc.litenote.db.DB;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectPageList {
	View mView;
	CheckedTextView mCheckTvSelAll;
    ListView mListView;
    List<String> mListStrArr; // list view string array
    public List<Boolean> mCheckedArr; // checked list view items array
    DB mDb_drawer,mDb_tabs;
    int COUNT;
    Activity mAct;
    public String mXML_default_filename;//??? for only 2 cases (all and single)?
    public String mDrawerChildTitle;
    
	public SelectPageList(Activity act, View view) 
	{
		mAct = act;
		
		mDb_drawer = new DB(act);  
		mDb_drawer.initDrawerDb(mDb_drawer);	
		int pos = DrawerActivity.mDrawerListView.getCheckedItemPosition();
		mDrawerChildTitle = mDb_drawer.getDrawerChild_Title(pos);
		
		mDb_tabs = new DB(mAct,Util.getPref_lastTimeView_tabs_tableId(mAct));
		mDb_tabs.initTabsDb(mDb_tabs);
		
		// checked Text View: select all 
		mCheckTvSelAll = (CheckedTextView) mAct.findViewById(R.id.chkSelectAllPages);
		mCheckTvSelAll.setOnClickListener(new OnClickListener()
		{	@Override
			public void onClick(View checkSelAll) 
			{
				boolean currentCheck = ((CheckedTextView)checkSelAll).isChecked();
				((CheckedTextView)checkSelAll).setChecked(!currentCheck);
				
				if(((CheckedTextView)checkSelAll).isChecked())
				{
					selectAllPages(true);
					mXML_default_filename = mDrawerChildTitle;					
				}
				else
					selectAllPages(false);
			}
		});
		
		// list view: selecting which pages to send 
		mListView = (ListView)view;
		listForSelect();
    }
    
	// select all pages
	public void selectAllPages(boolean enAll) {
		mChkNum = 0;

		mDb_tabs.doOpenTabs(Util.getPref_lastTimeView_tabs_tableId(mAct));
        COUNT = mDb_tabs.getTabsCount(false);
        for(int i=0;i<COUNT;i++)
        {
	         CheckedTextView chkTV = (CheckedTextView) mListView.findViewById(R.id.checkTV);
	         mCheckedArr.set(i, enAll);
             mListStrArr.set(i,mDb_tabs.getTabTitle(i,false));
             
             int style = mDb_tabs.getTabStyle(i, false);
             
 			 if( enAll)
 				 chkTV.setCompoundDrawablesWithIntrinsicBounds(style%2 == 1 ?
 		    			R.drawable.btn_check_on_holo_light:
 		    			R.drawable.btn_check_on_holo_dark,0,0,0);
 			 else
 				 chkTV.setCompoundDrawablesWithIntrinsicBounds(style%2 == 1 ?
 						R.drawable.btn_check_off_holo_light:
 						R.drawable.btn_check_off_holo_dark,0,0,0);
        }
        mDb_tabs.doCloseTabs();
        	
    	mChkNum = (enAll == true)? COUNT : 0;
        
        // set list adapter
        ListAdapter listAdapter = new ListAdapter(mAct, mListStrArr);
        
        // list view: set adapter 
        mListView.setAdapter(listAdapter);
	}

	// show list for Select
    public int mChkNum;
    void listForSelect()
    {
		mChkNum = 0;
        // set list view
        mListView = (ListView) mAct.findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id)
            {
                 CheckedTextView chkTV = (CheckedTextView) vw.findViewById(R.id.checkTV);
                 chkTV.setChecked(!chkTV.isChecked());
                 mCheckedArr.set(position, chkTV.isChecked());
                 if(mCheckedArr.get(position) == true)
                	 mChkNum++;
                 else
                	 mChkNum--;
                 
                 if(!chkTV.isChecked())
                 {
                	 mCheckTvSelAll.setChecked(false);
                 }
                 
                 // set for contrast
                 int mStyle = mDb_tabs.getTabStyle(position, true);
     			 if( chkTV.isChecked())
     				 chkTV.setCompoundDrawablesWithIntrinsicBounds(mStyle%2 == 1 ?
     		    			R.drawable.btn_check_on_holo_light:
     		    			R.drawable.btn_check_on_holo_dark,0,0,0);
     			 else
     				 chkTV.setCompoundDrawablesWithIntrinsicBounds(mStyle%2 == 1 ?
     						R.drawable.btn_check_off_holo_light:
     						R.drawable.btn_check_off_holo_dark,0,0,0);
     			 
     			 // set default file name
   				 mXML_default_filename = mDb_tabs.getTabTitle(position, true);
            }
        });
 
        // set list string array
        mCheckedArr = new ArrayList<Boolean>();
        mListStrArr = new ArrayList<String>();
        
        // DB
		String strFinalPageViewed_tableId = Util.getPref_lastTimeView_notes_tableId(mAct);
        DB.setFocus_notes_tableId(strFinalPageViewed_tableId);
        
        mDb_tabs.doOpenTabs(Util.getPref_lastTimeView_tabs_tableId(mAct));
        COUNT = mDb_tabs.getTabsCount(false);
        for(int i=0;i<COUNT;i++)
        {
        	 // list string array: init
             mListStrArr.add(mDb_tabs.getTabTitle(i,false));
             // checked mark array: init
             mCheckedArr.add(false);
        }
        mDb_tabs.doCloseTabs();
        
        // set list adapter
        ListAdapter listAdapter = new ListAdapter(mAct, mListStrArr);
        
        // list view: set adapter 
        mListView.setAdapter(listAdapter);
    }
    
	// list adapter
    public class ListAdapter extends BaseAdapter
    {
        private Activity activity;
        private List<String> mList;
        private LayoutInflater inflater = null;
         
        public ListAdapter(Activity a, List<String> list)
        {
            activity = a;
            mList = list;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
     
        public int getCount()
        {
            return mList.size();
        }
     
        public Object getItem(int position)
        {
            return mCheckedArr.get(position);
        }
     
        public long getItemId(int position)
        {
            return position;
        }
         
        public View getView(int position, View convertView, ViewGroup parent)
        {
            mView = inflater.inflate(R.layout.select_page_list_row, null);
            
            // set checked text view
            CheckedTextView chkTV = (CheckedTextView) mView.findViewById(R.id.checkTV);
            // show style
            int style = mDb_tabs.getTabStyle(position, true);
            chkTV.setBackgroundColor(Util.mBG_ColorArray[style]);
            chkTV.setTextColor(Util.mText_ColorArray[style]);
            
            // Show current page
            //??? how to set left padding of text view of a CheckedTextview
            // workaround: set single line to true and add one space in front of the text
            if(mDb_tabs.getNotesTableId(position,true) == Integer.valueOf(DB.getFocus_notes_tableId()))
            {
        		chkTV.setTypeface(chkTV.getTypeface(), Typeface.BOLD_ITALIC);
            	chkTV.setText( " " + mList.get(position) + "*" );
            }
            else	
            	chkTV.setText( " " + mList.get(position).toString());
            
            chkTV.setChecked(mCheckedArr.get(position));

             // set for contrast
			 if( chkTV.isChecked())
	        	 // note: have to remove the following in XML file
	             // android:drawableLeft="?android:attr/listChoiceIndicatorMultiple"
	             // otherwise, setCompoundDrawablesWithIntrinsicBounds will not work on ICS
				 chkTV.setCompoundDrawablesWithIntrinsicBounds(style%2 == 1 ?
		    			R.drawable.btn_check_on_holo_light:
		    			R.drawable.btn_check_on_holo_dark,0,0,0);
			 else
				 chkTV.setCompoundDrawablesWithIntrinsicBounds(style%2 == 1 ?
						R.drawable.btn_check_off_holo_light:
						R.drawable.btn_check_off_holo_dark,0,0,0);

			 return mView;
        }
    }

}
