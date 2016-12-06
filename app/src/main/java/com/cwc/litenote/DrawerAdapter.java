package com.cwc.litenote;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cwc.litenote.alpha.R;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.lib.SimpleDragSortCursorAdapter;
import com.cwc.litenote.media.audio.AudioPlayer;

class DrawerAdapter extends SimpleDragSortCursorAdapter
{
	public DrawerAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) 
	{
		super(context, layout, c, from, to, flags);
	}

	@Override
	public int getCount() {
		int count = DB.mDb_drawer.getDrawerChildCount();
		return count;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}		
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder; // holds references to current item's GUI
     
		// if convertView is null, inflate GUI and create ViewHolder;
		// otherwise, get existing ViewHolder
		if (convertView == null)
		{
			convertView = DrawerActivity.mDrawerActivity.getLayoutInflater().inflate(R.layout.drawer_list_item, parent, false);
			
			// set up ViewHolder for this ListView item
			viewHolder = new ViewHolder();
			viewHolder.drawerTitle = (TextView) convertView.findViewById(R.id.drawerText);
			viewHolder.imageDragger = (ImageView) convertView.findViewById(R.id.drawer_dragger);
			convertView.setTag(viewHolder); // store as View's tag
		}
		else // get the ViewHolder from the convertView's tag
			viewHolder = (ViewHolder) convertView.getTag();

		// set highlight of selected drawer
        if((AudioPlayer.mMediaPlayer != null) &&
       	   (DrawerActivity.mCurrentPlaying_drawerIndex == position) )
        	viewHolder.drawerTitle.setTextColor(Color.argb(0xff, 0xff, 0x80, 0x00));
        else
        	viewHolder.drawerTitle.setTextColor(Color.argb(0xff, 0xff, 0xff, 0xff));
		
		viewHolder.drawerTitle.setText(DB.mDb_drawer.getDrawerChild_Title(position));

	  	// dragger
	  	if(DrawerActivity.mPref_show_note_attribute.getString("KEY_ENABLE_DRAWER_DRAGGABLE", "no")
	  											   .equalsIgnoreCase("yes"))
	  		viewHolder.imageDragger.setVisibility(View.VISIBLE); 
	  	else
	  		viewHolder.imageDragger.setVisibility(View.GONE); 		
		
		return convertView;
	}
}

class ViewHolder
{
	TextView drawerTitle; // refers to ListView item's ImageView
	ImageView imageDragger;
}
