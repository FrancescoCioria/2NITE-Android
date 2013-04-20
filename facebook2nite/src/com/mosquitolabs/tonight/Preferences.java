package com.mosquitolabs.tonight;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Preferences {
	private boolean isModifiedPageListToClear=false;
	private boolean modifiedPages = true;
	private boolean modifiedSinglePage = true;
	private boolean isSelectedPage = false;
	private static Preferences instance = new Preferences();
	
	public static Preferences getInstance()
	  {
	    return instance;
	  }
	
	public boolean getModifiedPages(){
		return modifiedPages;
	}
	
	public boolean getIsSelectedPage(){
		return isSelectedPage;
	}
	
	public boolean getModifiedSinglePage(){
		return modifiedSinglePage;
	}
	
	public boolean getisModifiedPageListToClear(){
		return isModifiedPageListToClear;
	}
	
	public void setModifiedPages(boolean b){
		modifiedPages = b;
	}
	
	public void setIsSelectedPage(boolean b){
		isSelectedPage = b;
	}
	public void setisModifiedPageListToClear(boolean b){
		isModifiedPageListToClear=b;
	}
	public void setModifiedSinglePage(boolean b){
		modifiedSinglePage = b;
	}
	
	public boolean isOnline(Activity context) {
	
		    ConnectivityManager cm =
		        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnected()) {
		        return true;
		    }
		    return false;
		}
	}
	