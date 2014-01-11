package com.mosquitolabs.tonight;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Request.Callback;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class EventCollection {
	private static EventCollection instance = new EventCollection();
	private ArrayList<EventData> eventList = new ArrayList<EventData>();
	private ArrayList<EventData> AroundMeEventList = new ArrayList<EventData>();
	private ArrayList<EventData> completeEventList = new ArrayList<EventData>();
	private ArrayList<EventData> updateEventList = new ArrayList<EventData>();
	private ArrayList<EventData> updateActivityEventList = new ArrayList<EventData>();

	public static EventCollection getInstance() {
		return instance;
	}

	public ArrayList<EventData> getEventList() {
		return this.eventList;
	}

	public ArrayList<EventData> getAroundMeEventList() {
		return this.AroundMeEventList;
	}

	public ArrayList<EventData> getCompleteEventList() {
		return completeEventList;
	}

	public ArrayList<EventData> getUpdateEventList() {
		return updateEventList;
	}

	public void saveCompleteEventList() {
		completeEventList.clear();
		for (EventData currentEvent : eventList) {
			addToCompleteEventList(currentEvent);
		}
	}

	public void restoreEventList() {
		eventList.clear();
		for (EventData currentEvent : completeEventList) {

			addToEventList(currentEvent);

		}
	}

	public void restoreEventListFromUpdate() {
		eventList.clear();
		for (EventData currentEvent : updateEventList) {

			addToEventList(currentEvent);

		}
	}

	public void restoreUpdateEventList() {
		updateEventList.clear();
		for (EventData currentEvent : completeEventList) {

			updateEventList.add(currentEvent);

		}
	}

	public void cleanEventListDeclined() {
		Log.i("cleanEventListDeclined", "cleanEventListDeclined");

		ArrayList<String> remove = new ArrayList<String>();
		int i = 0;
		boolean rsvp = false;
		for (EventData currentEvent : eventList) {
			if(currentEvent.status_attending==null){
				currentEvent.status_attending="Not Invited";
				rsvp = true;
			}
			if (currentEvent.status_attending.equals("declined")) {
				remove.add(Integer.toString(i));
			}
			i++;
		}
		int g = 0;
		for (String index : remove) {
			eventList.remove(Integer.parseInt(index) - g);
			g++;
		}
		if (rsvp) {
			getRsvp();
		}
		
		Log.i("cleanEventListDeclined", "cleanEventListDeclined");


	}

	public EventData getEventByID(String _ID) {
		if (_ID == null) {
			return null;
		}
		for (EventData event : eventList) {
			if (event.event_ID.equals(_ID))
				return event;
		}
		return null;
	}
	
	public EventData getEventByIndex(int index) {
		try{
			return eventList.get(index);
		}catch (Exception e){
			return new EventData();
		}
	}

	public EventData getAroundMeEventByID(String _ID) {
		if (_ID == null) {
			return null;
		}
		for (EventData event : AroundMeEventList) {
			if (event.event_ID.equals(_ID))
				return event;
		}
		return null;
	}

	public EventData getCompleteEventByID(String _ID) {
		if (_ID == null) {
			return null;
		}
		for (EventData event : completeEventList) {
			if (event.event_ID.equals(_ID))
				return event;
		}
		return null;
	}

	public EventData getUpdateEventByID(String _ID) {
		if (_ID == null) {
			return null;
		}
		for (EventData event : updateEventList) {
			if (event.event_ID.equals(_ID))
				return event;
		}
		return null;
	}

	public void readFromDisk(Context paramActivity) {
		try {
			ObjectInputStream localObjectInputStream = new ObjectInputStream(
					paramActivity.openFileInput("events.data"));
			ArrayList localArrayList = (ArrayList) localObjectInputStream
					.readObject();
			localObjectInputStream.close();
			completeEventList = localArrayList;
			return;
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		} catch (ClassNotFoundException localClassNotFoundException) {
			localClassNotFoundException.printStackTrace();
		}
	}

	public void readUpdateFromDisk(Context paramActivity) {
		try {
			ObjectInputStream localObjectInputStream = new ObjectInputStream(
					paramActivity.openFileInput("update.data"));
			ArrayList localArrayList = (ArrayList) localObjectInputStream
					.readObject();
			localObjectInputStream.close();
			updateActivityEventList = localArrayList;
			return;
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		} catch (ClassNotFoundException localClassNotFoundException) {
			localClassNotFoundException.printStackTrace();
		}
	}

	public void saveToDisk(Context paramActivity) {
		if (eventList.size() > completeEventList.size()) {
			saveCompleteEventList();
		}
		try {
			ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(
					paramActivity.openFileOutput("events.data", 0));
			localObjectOutputStream.writeObject(completeEventList);
			localObjectOutputStream.close();
			return;
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
	}

	public void saveUpdateToDisk(Context paramActivity) {
		try {
			ObjectOutputStream localObjectOutputStream = new ObjectOutputStream(
					paramActivity.openFileOutput("update.data", 0));
			localObjectOutputStream.writeObject(updateEventList);
			localObjectOutputStream.close();
			return;
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		}
	}

	public void cleanCompleteEventList() {
		ArrayList<String> removeEvent = new ArrayList<String>();
		int i = 0;
		Calendar localCalendar = Calendar.getInstance();

		// Calendar eventCalendar = Calendar.getInstance(TimeZone
		// .getTimeZone("America/Los_Angeles"));
		Calendar eventCalendar = Calendar.getInstance();

		Formatter localFormatter = new Formatter();
		Formatter formatterTime = new Formatter();
		Object[] arrayOfObject = new Object[3];
		arrayOfObject[0] = Integer.valueOf(localCalendar.get(1));
		arrayOfObject[1] = Integer.valueOf(1 + localCalendar.get(2));
		arrayOfObject[2] = Integer.valueOf(localCalendar.get(5));
		localFormatter.format("%s-%02d-%02d", arrayOfObject);
		String currentTime = localFormatter.toString();
		formatterTime.format("%02d%02d",
				localCalendar.get(Calendar.HOUR_OF_DAY),
				localCalendar.get(Calendar.MINUTE));
		String currentHour = formatterTime.toString();
		// String currentHour = new String(Integer.toString(localCalendar
		// .get(Calendar.HOUR_OF_DAY))
		// + Integer.toString(localCalendar.get(Calendar.MINUTE)));

		for (EventData event : completeEventList) {
			eventCalendar.setTimeInMillis(Long.parseLong(event.endMillis
					+ "000"));
			Formatter formatter = new Formatter();
			Object[] arrayOfObject2 = new Object[3];
			arrayOfObject2[0] = Integer.valueOf(eventCalendar.get(1));
			arrayOfObject2[1] = Integer.valueOf(1 + eventCalendar.get(2));
			arrayOfObject2[2] = Integer.valueOf(eventCalendar.get(5));
			formatter.format("%s-%02d-%02d", arrayOfObject2);
			String endTime = formatter.toString();
			if (endTime.compareTo(currentTime) < 0)
				removeEvent.add(Integer.toString(i));
			if (endTime.compareTo(currentTime) == 0) {
				formatter = new Formatter();
				formatter.format("%02d%02d",
						eventCalendar.get(Calendar.HOUR_OF_DAY),
						eventCalendar.get(Calendar.MINUTE));
				if (formatter.toString().compareTo(currentHour) < 0)
					removeEvent.add(Integer.toString(i));
			}

			i++;
		}

		int g = 0;
		for (String s : removeEvent) {
			completeEventList.remove(Integer.parseInt(s) - g);
			g++;
		}
	}

	public void cleanUpdateEventList() {
		ArrayList<String> removeEvent = new ArrayList<String>();
		int i = 0;
		Calendar localCalendar = Calendar.getInstance();
		Calendar eventCalendar = Calendar.getInstance();

		Formatter localFormatter = new Formatter();
		Formatter formatterTime = new Formatter();
		Object[] arrayOfObject = new Object[3];
		arrayOfObject[0] = Integer.valueOf(localCalendar.get(1));
		arrayOfObject[1] = Integer.valueOf(1 + localCalendar.get(2));
		arrayOfObject[2] = Integer.valueOf(localCalendar.get(5));
		localFormatter.format("%s-%02d-%02d", arrayOfObject);
		String currentTime = localFormatter.toString();
		formatterTime.format("%02d%02d",
				localCalendar.get(Calendar.HOUR_OF_DAY),
				localCalendar.get(Calendar.MINUTE));
		String currentHour = formatterTime.toString();
		for (EventData event : updateEventList) {
			eventCalendar.setTimeInMillis(Long.parseLong(event.endMillis
					+ "000"));
			Formatter formatter = new Formatter();
			Object[] arrayOfObject2 = new Object[3];
			arrayOfObject2[0] = Integer.valueOf(eventCalendar.get(1));
			arrayOfObject2[1] = Integer.valueOf(1 + eventCalendar.get(2));
			arrayOfObject2[2] = Integer.valueOf(eventCalendar.get(5));
			formatter.format("%s-%02d-%02d", arrayOfObject2);
			String endTime = formatter.toString();
			if (endTime.compareTo(currentTime) < 0)
				removeEvent.add(Integer.toString(i));
			if (endTime.compareTo(currentTime) == 0) {
				formatter = new Formatter();
				formatter.format("%02d%02d",
						eventCalendar.get(Calendar.HOUR_OF_DAY),
						eventCalendar.get(Calendar.MINUTE));
				if (formatter.toString().compareTo(currentHour) < 0)
					removeEvent.add(Integer.toString(i));
			}
			i++;
		}

		int g = 0;
		for (String s : removeEvent) {
			updateEventList.remove(Integer.parseInt(s) - g);
			g++;
		}
	}

	public void sortByDate() {
		ArrayList<EventData> currentList = new ArrayList<EventData>();
		int i = 0;
		for (EventData currentEvent : eventList) {
			if (i == 0) {
				currentList.add(currentEvent);
				i++;
			} else {
				int j = 0;
				while (j != currentList.size()
						&& currentEvent.startMillis.compareTo(currentList
								.get(j).startMillis) > 0) {
					j++;

				}
				if (j == currentList.size()) {
					currentList.add(currentEvent);
				} else {

					currentList.add(j, currentEvent);
				}

			}
		}

		eventList.clear();
		for (EventData currentEvent : currentList) {
			eventList.add(currentEvent);
		}

	}

	public void aroundMeSortByDate() {
		ArrayList<EventData> currentList = new ArrayList<EventData>();
		int i = 0;
		for (EventData currentEvent : AroundMeEventList) {
			if (i == 0) {
				currentList.add(currentEvent);
				i++;
			} else {
				int j = 0;
				while (j != currentList.size()
						&& currentEvent.startMillis.compareTo(currentList
								.get(j).startMillis) > 0) {
					j++;

				}
				if (j == currentList.size()) {
					currentList.add(currentEvent);
				} else {

					currentList.add(j, currentEvent);
				}

			}
		}

		AroundMeEventList.clear();
		for (EventData currentEvent : currentList) {
			AroundMeEventList.add(currentEvent);
		}

	}

	public void sortByAttendingCount() {
		ArrayList<EventData> currentList = new ArrayList<EventData>();
		int i = 0;
		for (EventData currentEvent : eventList) {
			if (i == 0) {
				currentList.add(currentEvent);
				i++;
			} else {

				int j = 0;
				while (j != currentList.size()
						&& currentEvent.attending_count < currentList.get(j).attending_count) {
					j++;

				}
				if (j == currentList.size()) {
					currentList.add(currentEvent);
				} else {

					currentList.add(j, currentEvent);
				}

			}
		}

		eventList.clear();
		for (EventData currentEvent : currentList) {
			eventList.add(currentEvent);
		}

	}

	public void aroundMeSortByAttendingCount() {
		ArrayList<EventData> currentList = new ArrayList<EventData>();
		int i = 0;
		for (EventData currentEvent : AroundMeEventList) {
			if (i == 0) {
				currentList.add(currentEvent);
				i++;
			} else {

				int j = 0;
				while (j != currentList.size()
						&& currentEvent.attending_count < currentList.get(j).attending_count) {
					j++;

				}
				if (j == currentList.size()) {
					currentList.add(currentEvent);
				} else {

					currentList.add(j, currentEvent);
				}

			}
		}

		AroundMeEventList.clear();
		for (EventData currentEvent : currentList) {
			AroundMeEventList.add(currentEvent);
		}

	}

	public boolean addToEventList(EventData event) {
		for (EventData current : eventList) {
			if (event.event_ID.equals(current.event_ID)) {
				return false;
			}
		}
		eventList.add(event);
		return true;
	}

	public boolean addToAroundMeEventList(EventData event) {
		for (EventData current : AroundMeEventList) {
			if (event.event_ID.equals(current.event_ID)) {
				return false;
			}
		}
		AroundMeEventList.add(event);
		return true;
	}

	public boolean addToCompleteEventList(EventData event) {
		for (EventData current : completeEventList) {
			if (event.event_ID.equals(current.event_ID)) {
				return false;
			}
		}
		completeEventList.add(event);
		return true;
	}

	public void removeEventsByParentPageID(FacebookeventsActivity context,
			String pageID) {

		ArrayList<String> remove = new ArrayList<String>();
		final int ATTENDING = 0;
		final int ATTENDING_UNSURE = 1;
		final int NOT_DECLINED = 2;
		final int ALL = 3;

		String container = "";

		switch (1) {
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

		for (EventData event : completeEventList) {
			if (event.parentPage_ID.equals(pageID)
					&& container.contains(event.status_attending)) {
				event.parentPage_ID = "1";
				event.parentPageName = "My Events";
			}
		}
		int q = 0;
		for (EventData event : completeEventList) {
			if (event.parentPage_ID.equals(pageID)) {
				remove.add(Integer.toString(q));
			}
			q++;
		}
		q = 0;
		for (String s : remove) {
			completeEventList.remove(Integer.parseInt(s) - q);
			q++;
		}

		if (context.isViewAllVisible()) {
			context.viewAll();
		} else {
			restoreEventList();
			context.refreshEventsAdapter();
		}

	}

	private void getRsvp() {
		/*Log.i("getRSVP", "getRSVP");

		String rsvpRequest = "{";

		for (EventData event : completeEventList) {
			rsvpRequest += "\"" + event.event_ID + "\":"
					+ "\"SELECT rsvp_status FROM event_member where eid = "
					+ event.event_ID + "  and uid = me()\",";
		}
		rsvpRequest += "}";

		Bundle bundle = new Bundle();
		bundle.putString("q", rsvpRequest);

		Callback callback = new Callback() {
			@Override
			public void onCompleted(Response response) {
				try {
					JSONObject jObject = response.getGraphObject()
							.getInnerJSONObject();
					JSONArray jArray = jObject.getJSONArray("data");
					for (int i = 0; i < jArray.length(); i++) {
						jObject = jArray.getJSONObject(i);
						String ID = jObject.getString("name");

						JSONArray jj = jObject.getJSONArray("fql_result_set");
						if (jj.length() == 0) {
							getCompleteEventByID(ID).status_attending = "Not Invited";
						} else {
							jObject = jj.getJSONObject(0);
							getCompleteEventByID(ID).status_attending = jObject
									.getString("rsvp_status");
						}

					}
				} catch (Exception e) {
					Log.e("rsvp_request", e.toString());
				}
			}
		};
		Request request = new Request(Session.getActiveSession(), "fql",
				bundle, HttpMethod.GET, callback);
		request.executeAsync();
		*/

	}

}
