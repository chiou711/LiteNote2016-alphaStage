package com.cwc.litenote.note;

import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.alpha.R;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.AsyncTaskAudioBitmap;
import com.cwc.litenote.media.image.TouchImageView;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.media.video.UtilVideo;
import com.cwc.litenote.media.video.VideoPlayer;
import com.cwc.litenote.util.Util;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.Layout.Alignment;
import android.text.style.AlignmentSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class Note_view_pager_adapter extends FragmentStatePagerAdapter //PagerAdapter 
{
	static final int VIEW_LINK = 7;
	static int mLastPosition;
	static LayoutInflater inflater;
	static FragmentActivity mAct;
	static String mWebTitle;
	
    public Note_view_pager_adapter(FragmentManager fm,FragmentActivity act) 
    {
    	super(fm);
    	mAct = act;
        inflater = mAct.getLayoutInflater();
        mLastPosition = -1;
        hasSet_textWebView = false;
        System.out.println("Note_view_pager_adapter / mLastPosition = -1;");
    }
    
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
		object = null;
	}

    @SuppressLint("SetJavaScriptEnabled")
	@Override
	public Object instantiateItem(ViewGroup container, final int position) 
    {
    	System.out.println("Note_view_pager_adapter / instantiateItem / position = " + position);
    	// Inflate the layout containing 
    	// 1. picture group: image,video, thumb nail, control buttons
    	// 2. text group: title, body, time 
    	View pagerView = (ViewGroup) inflater.inflate(R.layout.note_view_pager, container, false);
    	int style = Note_view_pager.getStyle();
        pagerView.setBackgroundColor(Util.mBG_ColorArray[style]);

    	// Picture group
        ViewGroup pictureGroup = (ViewGroup) pagerView.findViewById(R.id.pictureContent);
        String tagPictureStr = "current"+ position +"pictureView";
        pictureGroup.setTag(tagPictureStr);
    	
        // image view
    	TouchImageView imageView = new TouchImageView(container.getContext());
		imageView = ((TouchImageView) pagerView.findViewById(R.id.image_view));
		
		// video view
    	VideoView videoView = new VideoView(container.getContext());
		videoView = ((VideoView) pagerView.findViewById(R.id.video_view));

        final ProgressBar spinner = (ProgressBar) pagerView.findViewById(R.id.loading);

        // link web view
//		CustomWebView linkWebView = new CustomWebView(container.getContext());
		CustomWebView linkWebView = ((CustomWebView) pagerView.findViewById(R.id.link_web_view));
        String tagStr = "current"+position+"linkWebView";
        linkWebView.setTag(tagStr);
        setWebView(linkWebView,spinner,CustomWebView.LINK_VIEW);
		
    	// text group
        ViewGroup textGroup = (ViewGroup) pagerView.findViewById(R.id.textGroup);

        // Set tag for text web view
//    	CustomWebView textWebView = new CustomWebView(mAct);
    	CustomWebView textWebView = ((CustomWebView) pagerView.findViewById(R.id.textGroup).findViewById(R.id.textBody));
        tagStr = "current"+position+"textWebView";
        textWebView.setTag(tagStr);
        setWebView(textWebView,spinner,CustomWebView.TEXT_VIEW);

        String linkUri = Note_view_pager.mDb.getNoteLinkUri(position,true);
        String strTitle = Note_view_pager.mDb.getNoteTitle(position,true);
        String strBody = Note_view_pager.mDb.getNoteBody(position,true);

        // View mode
    	// picture only
	  	if(Note_view_pager.isPictureMode())
	  	{
	  		pictureGroup.setVisibility(View.VISIBLE);
	  	    showPictureView(position,pictureGroup,imageView,videoView,linkWebView,spinner);
	  		
	  	    textGroup.setVisibility(View.GONE);
	  	}
	    // text only
	  	else if(Note_view_pager.isTextMode())
	  	{
	  		pictureGroup.setVisibility(View.GONE);
	  		
	  		textGroup.setVisibility(View.VISIBLE);
	  		
	  	    if( Util.isYouTubeLink(linkUri) ||
	 	  	       !Util.isEmptyString(strTitle)||
	 	  	       !Util.isEmptyString(strBody)   )
	  	    {
	  	    	showTextWebView(position,textWebView);
	  	    }
	  	}
  		// picture and text
	  	else if(Note_view_pager.isViewAllMode())
	  	{
	  		pictureGroup.setVisibility(View.VISIBLE);
	  	    showPictureView(position,pictureGroup,imageView,videoView,linkWebView,spinner);
	  		
	  	    textGroup.setVisibility(View.VISIBLE);
	  	    
	  	    if( Util.isYouTubeLink(linkUri) ||
	  	       !Util.isEmptyString(strTitle)||
	  	       !Util.isEmptyString(strBody)   )
	  	    {
	  	    	showTextWebView(position,textWebView);
	  	    }
	  	}
        
    	container.addView(pagerView, 0);
    	
		return pagerView;			
    } //instantiateItem
	
    // show text web view
    static void showTextWebView(int position,CustomWebView textWebView)
    {
    	System.out.println("Note_view_pager_adapter/ _showTextView / position = " + position);

    	int viewPort;
    	// load text view data
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			viewPort = VIEW_PORT_BY_DEVICE_WIDTH;
    	else
    		viewPort = VIEW_PORT_BY_NONE;

    	String strHtml;
		strHtml = getHtmlStringWithViewPort(position,viewPort);
		textWebView.loadData(strHtml,"text/html; charset=utf-8", "UTF-8");
    }
    
    // show picture view
    void showPictureView(int position,
    		             ViewGroup pictureGroup, 
    		             TouchImageView imageView,
    		             VideoView videoView,
    		             CustomWebView linkWebView,
    		             ProgressBar spinner          )
    {
		String linkUri = Note_view_pager.mDb.getNoteLinkUri(position,true);
    	String pictureUri = Note_view_pager.mDb.getNotePictureUri(position,true);
    	String audioUri = Note_view_pager.mDb.getNoteAudioUri(position,true);

    	// Get YouTube thumb nail
    	if(Util.isEmptyString(pictureUri) && 
    	   Util.isYouTubeLink(linkUri)       )
    	{
			pictureUri = "http://img.youtube.com/vi/"+Util.getYoutubeId(linkUri)+"/0.jpg";
    	}
    	
		Note_view_pagerUI.showPictureViewUI(position);

        // show image view
  		if( UtilImage.hasImageExtension(pictureUri,mAct)||
  		    (Util.isEmptyString(pictureUri)&& 
  		     Util.isEmptyString(audioUri)&& 
  		     Util.isEmptyString(linkUri)      )             ) // for wrong path icon
  		{
  			videoView.setVisibility(View.GONE);
  			linkWebView.setVisibility(View.GONE);
  			UtilVideo.mVideoView = null;
  			imageView.setVisibility(View.VISIBLE);
  			Note_view_pager.showImageByTouchImageView(spinner, imageView, pictureUri);
  		}
  		// show video view
  		else if(UtilVideo.hasVideoExtension(pictureUri, mAct))
  		{
  			linkWebView.setVisibility(View.GONE);
  			imageView.setVisibility(View.GONE);
  			videoView.setVisibility(View.VISIBLE);
  		}
  		// show audio thumb nail view
  		else if(Util.isEmptyString(pictureUri)&& 
  				!Util.isEmptyString(audioUri)    )
  		{
  			videoView.setVisibility(View.GONE);
  			UtilVideo.mVideoView = null;
  			linkWebView.setVisibility(View.GONE);
  			imageView.setVisibility(View.VISIBLE);
  			try
			{
			    AsyncTaskAudioBitmap audioAsyncTask;
			    audioAsyncTask = new AsyncTaskAudioBitmap(NoteFragment.mAct,
						    							  audioUri, 
						    							  imageView, 
						    							  spinner);
				audioAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"Searching media ...");
			}
			catch(Exception e)
			{
				Log.e("Note_view_pager_adapter / UtilImage_bitmapLoader", "error");	
			}
  		}
  		// show link view
  		else if(Util.isEmptyString(pictureUri)&&
  				Util.isEmptyString(audioUri)  &&
  				!Util.isEmptyString(linkUri))
  		{
  			videoView.setVisibility(View.GONE);
  			UtilVideo.mVideoView = null;
  			imageView.setVisibility(View.GONE);	
  			linkWebView.setVisibility(View.VISIBLE);
  		}
    }
    
    
    // Add for FragmentStatePagerAdapter
    @Override
	public Fragment getItem(int arg0) {
		return null;
	}
    
    // Add for calling mPagerAdapter.notifyDataSetChanged() 
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    
	@Override
    public int getCount() 
    {
    	int count = Note_view_pager.mDb.getNotesCount(true);
    	//08-26 14:42:00.230: E/AndroidRuntime(13936): android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed. # Open Cursors=612 (# cursors opened by this proc=612)
    	//11-18 17:31:00.594: E/AndroidRuntime(7372): java.lang.RuntimeException: Unable to start activity ComponentInfo{com.cwc.litenote.alpha/com.cwc.litenote.note.Note_view_pager}: java.lang.NullPointerException
    	//12-11 21:56:00.458: E/AndroidRuntime(15089): Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'int com.cwc.litenote.db.DB.getNotesCount(boolean)' on a null object reference

    	return count;
    }

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
	
	static Intent mIntentView;
	
	@Override
	public void setPrimaryItem(final ViewGroup container, int position, Object object) 
	{
	    // Only refresh when primary changes
		System.out.println("Note_view_pager_adapter / _setPrimaryItem / mLastPosition = " + mLastPosition);
		System.out.println("Note_view_pager_adapter / _setPrimaryItem / position = " + position);

    	String strPicture = Note_view_pager.mDb.getNotePictureUri(position,true);
		String linkUri = Note_view_pager.mDb.getNoteLinkUri(position,true);
		
		// Text web view
		// stop last text web view
	    if((mLastPosition != position) && 
	       !Note_view_pager.isPictureMode() ) 
	    {
			// make sure the playing state can be stopped
	    	String tag = "current"+mLastPosition+"textWebView";
	    	CustomWebView textWebView = (CustomWebView) Note_view_pager.mPager.findViewWithTag(tag);
	    	if(textWebView != null)
	    	{
    			textWebView.onPause();
    			textWebView.onResume();
	    	}
	    	hasSet_textWebView = false;
	    }

    	// fix overlay issue: remove last position video view or link web view
        String tagPictureStr = "current"+ mLastPosition +"pictureView";
        System.out.println("Note_view_pager_adapter / _setPrimaryItem / tagPictureStr = " + tagPictureStr);
        ViewGroup lastPictureGroup = (ViewGroup) Note_view_pager.mPager.findViewWithTag(tagPictureStr);
		
        // Set link view
	    if((mLastPosition != position) && 
 	       !UtilVideo.hasVideoExtension(strPicture,mAct) &&	
	       !UtilImage.hasImageExtension(strPicture,mAct) &&	
	       !Util.isYouTubeLink(linkUri) &&
	       (linkUri.startsWith("http")) &&
	       !Note_view_pager.isTextMode()  )  
	    {
	    	// check if last page has image
	    	String lastPictureStr;
	    	boolean lastPageHasImage = false;
	    	if(mLastPosition != -1)
	    	{
	    		lastPictureStr = Note_view_pager.mDb.getNotePictureUri(mLastPosition,true);
	    		if(!UtilImage.hasImageExtension(lastPictureStr,mAct))
	    			lastPageHasImage = true;
	    	}
	        
	    	if( (lastPictureGroup != null) && !lastPageHasImage)
	        {
		        // remove last link web view
				CustomWebView linkWebView = ((CustomWebView) lastPictureGroup.findViewById(R.id.link_web_view));
				CustomWebView.pauseWebView(linkWebView);//if last page has image, this will blank the image 
		    	CustomWebView.blankWebView(linkWebView);
	        }
		    
    		// Set link web view
        	if(Util.isEmptyString(strPicture)&&		
        	   !Util.isYouTubeLink(linkUri)	    )
        	{
        		String tagStr = "current"+position+"linkWebView";
        		CustomWebView linkWebView = (CustomWebView) Note_view_pager.mPager.findViewWithTag(tagStr);
        		linkWebView.setVisibility(View.VISIBLE);
       			linkWebView.loadUrl(linkUri);
        	}
	    }	    
	    
		// Set video view
	    if((mLastPosition != position) && 
	    	UtilVideo.hasVideoExtension(strPicture,mAct) &&	
	       !UtilImage.hasImageExtension(strPicture,mAct) &&	
	       !Note_view_pager.isTextMode()  )  
	    {
	        if(lastPictureGroup != null)
	        {
		        VideoView videoView = (VideoView) (lastPictureGroup.findViewById(R.id.video_view));
		        videoView.setVisibility(View.GONE);
	        }
        	
        	// update current pager view
		    UtilVideo.mCurrentPagerView = (View)object;
		    
		    if(Note_view_pager.mIsViewModeChanged )
		    {
		    	System.out.println("Note_view_pager_adapter / _setPrimaryItem / Note_view_pager.mPosistionOfChangeView = " + Note_view_pager.mPosistionOfChangeView);
		    	UtilVideo.mPlayVideoPosition = Note_view_pager.mPosistionOfChangeView;
		    	UtilVideo.setVideoViewLayout(strPicture);
		    	
		    	if(!UtilVideo.hasMediaControlWidget)
		    		UtilVideo.setVideoViewUI();
		   		
		    	if(UtilVideo.mPlayVideoPosition > 0)
		   			UtilVideo.playOrPauseVideo(Note_view_pager.getCurrentPictureString());
		    }
		    else
		    {
		    	if(Note_view_pager.mPlayVideoPositionOfInstance > 0)
			    {
		    		UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PAUSE);
			    	UtilVideo.setVideoViewLayout(strPicture);
			    	
			    	if(!UtilVideo.hasMediaControlWidget)
			    		UtilVideo.setVideoViewUI();
		   			
			    	UtilVideo.playOrPauseVideo(Note_view_pager.getCurrentPictureString());			    	
			    }
		    	else
		    	{
			    	if(UtilVideo.hasMediaControlWidget)
			    		UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PLAY);
			    	else
		    			UtilVideo.setVideoState(UtilVideo.VIDEO_AT_STOP);
			    	
				    UtilVideo.mPlayVideoPosition = 0; // make sure play video position is 0 after page is changed
			    	UtilVideo.initVideoView(strPicture,mAct);
		    	}
		    }
		    
	    	Note_view_pagerUI.showPictureViewUI(position);
			UtilVideo.currentPicturePath = strPicture;
	    }
	    
	    mLastPosition = position;
	    
	} //setPrimaryItem		
	
	static boolean hasSet_textWebView;
	// Set web view
	static void setWebView(final CustomWebView webView,final ProgressBar spinner, int whichView)
	{
        final SharedPreferences pref_web_view = mAct.getSharedPreferences("web_view", 0);
    	int scale = pref_web_view.getInt("KEY_WEB_VIEW_SCALE",0);
    	int style = Note_view_pager.getStyle();
    	
    	webView.setInitialScale(scale);        	
    	webView.setBackgroundColor(Util.mBG_ColorArray[style]);
    	webView.getSettings().setBuiltInZoomControls(true);
    	webView.getSettings().setSupportZoom(true);
    	webView.getSettings().setUseWideViewPort(true);
//    	customWebView.getSettings().setLoadWithOverviewMode(true);
    	webView.getSettings().setJavaScriptEnabled(true);//Using setJavaScriptEnabled can introduce XSS vulnerabilities
        
    	if( (whichView == CustomWebView.LINK_VIEW) ||
    		(whichView == CustomWebView.TEXT_VIEW)   )
   		{ 
	    	webView.setWebViewClient(new WebViewClient() 
	        {
	            @Override
	            public void onScaleChanged(WebView web_view, float oldScale, float newScale) 
	            {
	                super.onScaleChanged(web_view, oldScale, newScale);
	//                System.out.println("Note_view_pager / onScaleChanged");
	//                System.out.println("    oldScale = " + oldScale); 
	//                System.out.println("    newScale = " + newScale);
	                
	                int newDefaultScale = (int) (newScale*100);
	                pref_web_view.edit().putInt("KEY_WEB_VIEW_SCALE",newDefaultScale).commit();
	                
	                //update current position
	                Note_view_pager.mCurrentPosition = Note_view_pager.mPager.getCurrentItem();
	            }
	            
	            @Override
	            public void onPageFinished(WebView view, String url) {
	            }
	        });
	        
   		}
	    
    	if(whichView == CustomWebView.LINK_VIEW)
    	{
	        webView.setWebChromeClient(new WebChromeClient()
	        {
	
	            public void onProgressChanged(WebView view, int progress)   
	            {
	            	if(spinner != null)
	            	{
		            	if(progress < 100 && (spinner.getVisibility() == ProgressBar.GONE))
		            	{
		            		webView.setVisibility(View.GONE);
		            		spinner.setVisibility(ProgressBar.VISIBLE);
		            	}
		            	
		            	spinner.setProgress(progress);
		                
		            	if(progress == 100) 
		            	{
		                	spinner.setVisibility(ProgressBar.GONE);
		                	webView.setVisibility(View.VISIBLE);
		            	}
	            	}
	            }        	
	            
	            @Override
			    public void onReceivedTitle(WebView view, String title) {
			        super.onReceivedTitle(view, title);
			        if (!TextUtils.isEmpty(title) && 
			        	!title.equalsIgnoreCase("about:blank")) 
			        {
			        	System.out.println("NoteFragmentAdapter / _onReceivedTitle / title = " + title);
		        	
			        	int position = Note_view_pager.mPager.getCurrentItem();
				    	String tag = "current"+position+"textWebView";
				    	CustomWebView textWebView = (CustomWebView) Note_view_pager.mPager.findViewWithTag(tag);
				    	
				    	DB mDb = Note_view_pager.mDb;
				    	String strLink = mDb.getNoteLinkUri(position,true);
			        	
				    	if((textWebView != null) &&
				    		!hasSet_textWebView && 
				    	    !Util.isYouTubeLink(strLink) &&
				    	    strLink.startsWith("http")        )
			        	{
				        	mWebTitle = title;
				        	System.out.println(";;;;;;;;;;;;;;;;; mWebTitle = " + mWebTitle);
		        			showTextWebView(position,textWebView);
			        		hasSet_textWebView = true;
			        	}
			        }
			    }            
	        });
    	}
	}
	
	
	public static void stopAV()
	{
		if(AudioPlayer.mAudioPlayMode == AudioPlayer.ONE_TIME_MODE)
			UtilAudio.stopAudioPlayer(); 
		
		if(UtilVideo.mVideoPlayer != null)
			VideoPlayer.stopVideo();
	}
	
	
    static int VIEW_PORT_BY_NONE = 0;
    static int VIEW_PORT_BY_DEVICE_WIDTH = 1;
    static int VIEW_PORT_BY_SCREEN_WIDTH = 2; 
    
    // Get HTML string with view port
    static String getHtmlStringWithViewPort(int position, int viewPort)
    {
    	DB mDb = Note_view_pager.mDb;
    	int mStyle = Note_view_pager.mStyle;
    	
    	System.out.println("Note_view_pager_adapter / _getHTMLstringWithViewPort");
    	String strTitle = mDb.getNoteTitle(position,true);
    	String strBody = mDb.getNoteBody(position,true);
    	String audioUri = mDb.getNoteAudioUri(position,true);
    	String linkUri = mDb.getNoteLinkUri(position,true);

    	// replace note title 
    	if(Util.isEmptyString(strTitle))
    	{
    		// with web title
    		if( Util.isEmptyString(audioUri) &&
    		   !Util.isYouTubeLink(linkUri)  &&
    		   !Util.isEmptyString(mWebTitle)  )
    	   	{
    		   strTitle = mWebTitle;
    	   	}
    	   	// with YouTube title
	   		else if(Util.isYouTubeLink(linkUri))
				strTitle = Util.getYoutubeTitle(linkUri);
    	}
    	
    	Long createTime = mDb.getNoteCreatedTime(position,true);
    	String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
		       	  	  "<html><head>" +
	  		       	  "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />";
    	
    	if(viewPort == VIEW_PORT_BY_NONE)
    	{
	    	head = head + "<head>";
    	}
    	else if(viewPort == VIEW_PORT_BY_DEVICE_WIDTH)
    	{
	    	head = head + 
	    		   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
	     	  	   "<head>";
    	}
    	else if(viewPort == VIEW_PORT_BY_SCREEN_WIDTH)
    	{
//        	int screen_width = UtilImage.getScreenWidth(mAct);
        	int screen_width = 640;
	    	head = head +
	    		   "<meta name=\"viewport\" content=\"width=" + String.valueOf(screen_width) + ", initial-scale=1\">"+
   	  			   "<head>";
    	}
    		
       	String seperatedLineTitle = (!Util.isEmptyString(strTitle) == true)?"<hr size=2 color=blue width=99% >":"";
       	String seperatedLineBody = (!Util.isEmptyString(strBody) == true)?"<hr size=1 color=black width=99% >":"";

       	// title
       	if(!Util.isEmptyString(strTitle))
       	{
       		Spannable spanTitle = new SpannableString(strTitle);
       		Linkify.addLinks(spanTitle, Linkify.ALL);
       		spanTitle.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_CENTER), 
       							0,
       							spanTitle.length(), 
       							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
       		strTitle = Html.toHtml(spanTitle);
       	}
       	else
       		strTitle = "";
    	
    	// body
       	if(!Util.isEmptyString(strBody))
       	{
	    	Spannable spanBody = new SpannableString(strBody);
	    	Linkify.addLinks(spanBody, Linkify.ALL);
	    	strBody = Html.toHtml(spanBody);
       	}
       	else
       		strBody = "";
	    	
    	// set web view text color
    	String colorStr = Integer.toHexString(Util.mText_ColorArray[mStyle]);
    	colorStr = colorStr.substring(2);
    	
    	String bgColorStr = Integer.toHexString(Util.mBG_ColorArray[mStyle]);
    	bgColorStr = bgColorStr.substring(2);
    	
    	String content = head + "<body color=\"" + bgColorStr + "\">" +
		         "<p align=\"center\"><b>" + 
				 "<font color=\"" + colorStr + "\">" + strTitle + "</font>" + 
         		 "</b></p>" + seperatedLineTitle + 
		         "<p>" + 
				 "<font color=\"" + colorStr + "\">" + strBody + "</font>" +
				 "</p>" + seperatedLineBody + 
		         "<p align=\"right\">" + 
				 "<font color=\"" + colorStr + "\">"  + Util.getTimeString(createTime) + "</font>" +
		         "</p>" + 
		         "</body></html>";
		return content;
    }	
	
}//class Note_view_pager_adapter extends PagerAdapter