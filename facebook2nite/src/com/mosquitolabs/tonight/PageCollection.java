package com.mosquitolabs.tonight;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;

public class PageCollection {
	private static PageCollection instance = new PageCollection();
	private ArrayList<PageData> PageList = new ArrayList<PageData>();
	private ArrayList<PageData> selectedPageList = new ArrayList<PageData>();
	private ArrayList<PageData> PageSearchList = new ArrayList<PageData>();
	private ArrayList<PageData> PageAroundMeList = new ArrayList<PageData>();
	private ArrayList<PageData> PageSearchListRelevant = new ArrayList<PageData>();
	private ArrayList<PageData> modifiedPageList = new ArrayList<PageData>();
	private ArrayList<PageData> previousPageList = new ArrayList<PageData>();
	EventCollection eventCollection = EventCollection.getInstance();

	public static PageCollection getInstance() {
		return instance;
	}

	public void restoreSelectedPageList() {
		selectedPageList.clear();
		for (PageData currentPage : PageList) {
			selectedPageList.add(currentPage);
		}
	}

	public ArrayList<PageData> getSelectedPageList() {
		return selectedPageList;
	}

	public ArrayList<PageData> getPreviousPageList() {
		return previousPageList;
	}

	public ArrayList<PageData> getModifiedPageList() {
		return modifiedPageList;
	}

	public void selectedPageList(PageData x) {
		selectedPageList.clear();
		selectedPageList.add(x);
	}

	public void restoreModifiedPage() {
		modifiedPageList.clear();
		for (PageData current : PageList) {
			modifiedPageList.add(current);
		}
	}

	public void restorePreviousPage() {
		previousPageList.clear();
		for (PageData current : PageList) {
			previousPageList.add(current);
		}
	}

	public boolean addModifiedPage(PageData paramPageData) {
		if (modifiedPageList.isEmpty()) {
			modifiedPageList.add(paramPageData);
			return true;
		} else {
			int i = 0;
			for (PageData currentPage : modifiedPageList) {
				if (currentPage._ID.equals(paramPageData._ID)) {
					modifiedPageList.remove(i);
					return false;
				}
				i++;
			}
			
			modifiedPageList.add(paramPageData);
			return true;

		}
	}

	public boolean addPageToFavourites(PageData paramPageData) {
		if (PageList.isEmpty()) {
			PageList.add(paramPageData);
			return true;
		} else {
			for (PageData currentPage : PageList) {
				if (currentPage._ID.equals(paramPageData._ID)) {
					return false;
				}
			}
			if(paramPageData._ID.equals("1")){
				PageList.add(0, paramPageData);
				return true;
		}
			int i = 0;
			for (PageData currentPage : PageList) {
				
				if(!currentPage._ID.equals("1")){
					
				if (paramPageData.name.compareTo(currentPage.name) < 0) {
					PageList.add(i, paramPageData);
					return true;
				}
				if (i == PageList.size() - 1) {
					PageList.add(paramPageData);
					return true;
				}
				}else{
					if(PageList.size()==1){
						PageList.add(paramPageData);
						return true;}
				}
				
				i++;
			}
			return false;
		}
	}

	public ArrayList<PageData> getPageList() {
		return PageList;
	}

	public ArrayList<PageData> getPageSearchList() {
		return PageSearchList;
	}
	public ArrayList<PageData> getPageSearchListRelevant() {
		return PageSearchListRelevant;
	}
	public ArrayList<PageData> getPageAroundMe() {
		return PageAroundMeList;
	}
	
	public void clearPageAroundMe(){
		PageAroundMeList.clear();
	}
	
	public void sortSearchByRelevance(){
		PageSearchList.clear();
		for(PageData page : PageSearchListRelevant){
			PageSearchList.add(page);
		}
	}

	public void readFromDisk(Context paramActivity) {
		File file = paramActivity.getFileStreamPath("pages.data");
		if (file.exists()) {
			try {
				ObjectInputStream localObjectInputStream = new ObjectInputStream(
						paramActivity.openFileInput("pages.data"));
				ArrayList localArrayList = (ArrayList) localObjectInputStream
						.readObject();
				localObjectInputStream.close();
				PageList = localArrayList;
				restoreSelectedPageList();
			} catch (IOException localIOException) {
				localIOException.printStackTrace();
			} catch (ClassNotFoundException localClassNotFoundException) {
				localClassNotFoundException.printStackTrace();
			}
		}

	}

	public PageData getPageByID(String _ID){
		for(PageData page : PageList){
			if(page._ID.equals(_ID)){
				return page;
			}
		}
		return null;
	}
	
	
	public boolean removePageFromFavouritesAndEvents(FacebookeventsActivity context,PageData paramPageData) {
		int i = 0;
		if (PageList.isEmpty()) {
			return false;
		}
		
		final int ATTENDING = 0;
		final int ATTENDING_UNSURE = 1;
		final int NOT_DECLINED = 2;
		final int ALL = 3;
		
		String container = "";
		
		switch (1){
		case ATTENDING:
			container = "attending";
			break;
			
		case ATTENDING_UNSURE:
			container = "attending,unsure";

			break;
		case NOT_DECLINED:
			container = "attending,unsure,Not Invited,not_replied";

			break;
		case ALL:
			container = "attending,unsure,Not Invited,not_replied,declined";

			break;
			
		}
		
		
		ArrayList<String> remove = new ArrayList<String>();
		for (PageData currentPage : PageList) {
			if (currentPage._ID.equals(paramPageData._ID)) {
				for(EventData event : eventCollection.getCompleteEventList()){
					if(event.parentPage_ID.equals(currentPage._ID)&&container.contains(event.status_attending)){
						event.parentPage_ID="1";
						event.parentPageName="My Events";
					}
				}
				int q=0;
				for(EventData event : eventCollection.getCompleteEventList()){
					if(event.parentPage_ID.equals(currentPage._ID)){
						remove.add(Integer.toString(q));
					}
					q++;
				}
				q=0;
				for(String s : remove){
					eventCollection.getCompleteEventList().remove(Integer.parseInt(s)-q);
					q++;
				}
				eventCollection.restoreEventList();
				PageList.remove(i);
				return true;
			}
			i++;
		}

		return false;

	}
	public boolean removePageFromFavourites(PageData paramPageData) {
		int i = 0;
		
		for (PageData currentPage : PageList) {
			if (currentPage._ID.equals(paramPageData._ID)) {
				PageList.remove(i);
				return true;
			}
			i++;
		}
		
		return false;
		
	}
	
	
	public void sortSearchByLikes(){
		ArrayList<PageData> searchCopy = new ArrayList<PageData>();
		for(PageData page : PageSearchList){
		int k =0;
			for(PageData copy : searchCopy){
				if(page.number_of_likes>copy.number_of_likes){
					k++;
				}
			}
			
				searchCopy.add(k, page);
			
		}
		PageSearchList.clear();
		for(PageData copy: searchCopy){
			PageSearchList.add(copy);
		}
	}
	
	public void sortSearchByName(){
		ArrayList<PageData> searchCopy = new ArrayList<PageData>();
		for(PageData page : PageSearchListRelevant){
			int k=0;
			for(PageData copy : searchCopy){
				if(page.name.compareTo(copy.name)>0){
					k++;
				}
			}
				searchCopy.add(k, page);
		}
		
		PageSearchList.clear();
		for(PageData copy: searchCopy){
			PageSearchList.add(copy);

		}
	}
	
	public void sortSearchByLikesAroundMe(){
		ArrayList<PageData> searchCopy = new ArrayList<PageData>();
		for(PageData page : PageAroundMeList){
			int k=0;
			for(PageData copy : searchCopy){
				if(page.number_of_likes>copy.number_of_likes){
					k++;
				}
			}
				searchCopy.add(k, page);
		}
		
		PageSearchList.clear();
		for(PageData copy: searchCopy){
			PageSearchList.add(0,copy);

		}
	}
	
	public void sortSearchByLikesAroundMeActivity(){
		ArrayList<PageData> searchCopy = new ArrayList<PageData>();
		for(PageData page : PageAroundMeList){
			int k=0;
			for(PageData copy : searchCopy){
				if(page.number_of_likes>copy.number_of_likes){
					k++;
				}
			}
			searchCopy.add(k, page);
		}
		
		PageAroundMeList.clear();
		for(PageData copy: searchCopy){
			PageAroundMeList.add(0,copy);
			
		}
	}
	

	public void saveToDisk(Context paramActivity) {
		try {
			ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(
					paramActivity.openFileOutput("pages.data", 0));
			localObjectOutputStream.writeObject(PageList);
			localObjectOutputStream.close();
			return;
		} catch (IOException localIOException) {
			while (true)
				localIOException.printStackTrace();
		}
	}
}
