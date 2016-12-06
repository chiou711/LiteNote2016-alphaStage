package com.cwc.litenote;

import android.graphics.Color;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.lib.DragSortController;
import com.cwc.litenote.lib.DragSortListView;

public class DrawerListview 
{

	// list view listener: on drag
    static DragSortListView.DragListener onDrag = new DragSortListView.DragListener() 
    {
                @Override
                public void drag(int startPosition, int endPosition) {
	//                	System.out.println("DrawerActivity / onDrag");
            }
    };
	    
	// list view listener: on drop
    static DragSortListView.DropListener onDrop = new DragSortListView.DropListener() 
    {
        @Override
        public void drop(int startPosition, int endPosition) {
//	        	System.out.println("DrawerActivity / onDrop / startPosition = " + startPosition);    
//	        	System.out.println("DrawerActivity / onDrop / endPosition = " + endPosition);    
			
			//reorder data base storage
			int loop = Math.abs(startPosition-endPosition);
			for(int i=0;i< loop;i++)
			{
				MainUi.swapDrawerRows(startPosition,endPosition);
				if((startPosition-endPosition) >0)
					endPosition++;
				else
					endPosition--;
			}
			
			// update audio playing drawer index
			int drawerCount = DB.mDb_drawer.getDrawerChildCount();
	        for(int i=0;i<drawerCount;i++)
        	{
	        	if(DB.mDb_drawer.getTabsTableId(i) == DrawerActivity.mCurrentPlaying_drawerTabsTableId)
	        		DrawerActivity.mCurrentPlaying_drawerIndex = i;
        	}			        
			DrawerActivity.drawerAdapter.notifyDataSetChanged();
			MainUi.updateDrawerFocusPosition();
        }
    };

    /**
     * Called in onCreateView. Override this to provide a custom
     * DragSortController.
     */
    public static DragSortController buildController(DragSortListView dslv)
    {
        // defaults are
        DragSortController controller = new DragSortController(dslv);
        controller.setSortEnabled(true);
        
        // Enable dragger or not
//        DrawerActivity.mPref_show_note_attribute = DrawerActivity.mDrawerActivity.getSharedPreferences("show_note_attribute", 0);
//	  	if(DrawerActivity.mPref_show_note_attribute.getString("KEY_ENABLE_DRAWER_DRAGGABLE", "no")
//	  											   .equalsIgnoreCase("yes"))
	  		controller.setDragInitMode(DragSortController.ON_DOWN); // click
//	  	else
//	        controller.setDragInitMode(DragSortController.MISS); 
//
	  	controller.setDragHandleId(R.id.drawer_dragger);// handler
	  	controller.setBackgroundColor(Color.argb(128,128,64,0));// background color when dragging

        return controller;
    }

}
