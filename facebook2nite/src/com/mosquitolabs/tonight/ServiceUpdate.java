package com.mosquitolabs.tonight;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;

public class ServiceUpdate extends Service {
	final String APP_ID = "219909391458551";
	private String dayOfWeekStart;
	private String dayOfWeekEnd;
	private String monthNameStart;
	private String monthNameEnd;
	private String timeEnd;
	private String timeStart;
	private String userID;
	private String myEventsSettings = "";
	private boolean newDownloads = false;
	private boolean isInProgress = false;
	private Timer timer = new Timer();
	private Timer timerOffline = new Timer();
	private PageCollection pageCollection = PageCollection.getInstance();
	private EventCollection eventCollection = EventCollection.getInstance();
	private SharedPreferences mPrefs;
	private Session session;

	public class LocalBinder extends Binder {
		ServiceUpdate getService() {
			return ServiceUpdate.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	public IBinder onBind(Intent intent) {
		Log.i("ServiceUpdate", "BIND");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("ServiceUpdate", "Received start id " + startId + ": " + intent);
		Context ctx = getApplicationContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		userID = mPrefs.getString("user_id", null);
		// session = Session.getActiveSession();

		session = Session.getActiveSession();
		if (session == null) {
			// toast("session_cache", true);
			session = Session.openActiveSessionFromCache(ctx);
		}

		if (session != null) {
			if (userID == null) {
				getUserIDasync();
			}
			checkCriticalUpdate();
			startservice();
		} else {
			Log.e("ServiceUpdate", "session_null");
		}
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void startservice() {

		timer.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				if (session != null && session.isOpened()) {
					if (isOnline()) {
						service();
					} else {
						timerOffline.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								if (isOnline()) {
									service();
									stopTimerOffline();
								}
							}

						}, 60000, 60000);
					}

				} else {
					getSession();
				}
			}

		}, 600000, 6 * 3600000);

	}

	private void service() {
		Log.i("ServiceUpdate", "start timer");
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean("service_status", true);
		editor.commit();
		eventCollection.readFromDisk(ServiceUpdate.this);
		pageCollection.readFromDisk(ServiceUpdate.this);
		update();
		editor.putBoolean("service_status", false);
		editor.commit();
	}

	private void stopservice() {
		if (timer != null) {
			timer.cancel();
		}
	}

	private void stopTimerOffline() {
		if (timerOffline != null) {
			timerOffline.cancel();
			timerOffline = new Timer();
		}
	}

	private void update() {
		eventCollection.restoreUpdateEventList();
		eventCollection.cleanUpdateEventList();
		final Bundle bundle = new Bundle();
		final ArrayList<String> addEvent = new ArrayList<String>();
		final ArrayList<String> removeEvent = new ArrayList<String>();
		String a = "SELECT eid,start_time,end_time,attending_count,location,description,name,update_time FROM event WHERE eid=";

		for (EventData event : eventCollection.getUpdateEventList()) {
			a += event.event_ID;
			if (!event.event_ID
					.equals(eventCollection.getUpdateEventList().get(
							eventCollection.getUpdateEventList().size() - 1).event_ID))
				a += " or eid=";
		}
		bundle.putString("q", a);

		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				JSONObject myJson = new JSONObject();
				JSONArray jArray = new JSONArray();
				try {
					myJson = response.getGraphObject().getInnerJSONObject();
					jArray = myJson.getJSONArray("data");

					int m = 0;
					for (int h = 0; h < jArray.length(); h++) {
						try {
							myJson = jArray.getJSONObject(h);
							EventData event = eventCollection
									.getUpdateEventByID(myJson.getString("eid"));
							event.attending_count = myJson
									.getInt("attending_count");
						} catch (Exception e) {
							Log.e("attending_count", e.toString());
						}
					}
					if (eventCollection.getUpdateEventList().size() != jArray
							.length()) {
						boolean remove;
						int i = 0;
						for (EventData eventToDelete : eventCollection
								.getUpdateEventList()) {
							remove = true;
							m = 0;
							while (m < jArray.length() && remove) {
								myJson = jArray.getJSONObject(m);
								m++;
								if (eventToDelete.event_ID.equals(myJson
										.getString("eid")))
									remove = false;
							}
							if (remove) {
								removeEvent.add(Integer.toString(i));
							}
							i++;
						}
					}
					int g = 0;
					for (String s : removeEvent) {
						eventCollection.getUpdateEventList().remove(
								Integer.parseInt(s) - g);
						g++;
					}

					m = 0;

					while (m < jArray.length()) {
						myJson = jArray.getJSONObject(m);

						if (!eventCollection.getUpdateEventList().get(m).last_update
								.equals(myJson.getString("update_time"))) {
							addEvent.add(Integer.toString(m));
						}
						m++;
					}
				} catch (Exception e) {
					Log.e("update events - service", e.toString());

					if (!eventCollection.getUpdateEventList().isEmpty()
							&& !myJson.isNull("error")) {
						Log.e("update service", "session not valid");
						SharedPreferences.Editor editor = mPrefs.edit();
						editor.putBoolean("session_valid", false);
						editor.commit();
					}
				}

				if (mPrefs.getBoolean("session", true)) {
					int n = 0;
					for (String s : addEvent) {
						try {
							myJson = jArray.getJSONObject(Integer.parseInt(s));

							EventData event = eventCollection
									.getUpdateEventList().get(
											Integer.parseInt(s));
							event.desc = myJson.getString("description");
							event.loc = myJson.getString("location");
							event.startMillis = getMillis(
									myJson.getString("start_time"), event);
							String end_time = "";
							long day = 0;

							if (myJson.isNull("end_time")) {
								end_time = myJson.getString("start_time");
								day = 86400;
							} else {
								end_time = myJson.getString("end_time");
							}
							if (day > 0) {
								long millis = Long.parseLong(getMillis(
										end_time, event)) + day;
								event.endMillis = Long.toString(millis);
							} else {
								event.endMillis = getMillis(end_time, event);
							}
							event.name = myJson.getString("name");
							event.last_update = getMillis(
									myJson.getString("update_time"), event);
							event.attending_count = myJson
									.getInt("attending_count");
							dayOfWeek(event.startMillis, event.endMillis);
							event.dateStart = monthNameStart;
							event.dayStart = dayOfWeekStart;
							event.timeStart = timeStart;
							event.dateEnd = monthNameEnd;
							event.dayEnd = dayOfWeekEnd;
							event.timeEnd = timeEnd;
							event.isInProgress = isInProgress;
							isInProgress = false;
							n++;
						} catch (Exception e) {
							Log.e("addService", e.toString());

						}
					}
					if (!addEvent.isEmpty())
						Log.i("ServiceUpdate", "updating");

					updateRSVPstatus();

					// see if new events are available
					getMyEvents();

					eventCollection.restoreEventList();
					bundle.clear();

					Calendar cal = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance(TimeZone
							.getTimeZone("America/Los_Angeles"));
					long mi = cal.get(Calendar.ZONE_OFFSET);
					long mu = cal2.get(Calendar.ZONE_OFFSET);
					long def = mi - mu;
					if (def < 0)
						def = def * (-1);
					String current_time = Long.toString(cal.getTimeInMillis()
							+ def);
					current_time = current_time.substring(0, 10);

					String az = "";
					az += "{";
					int p = 1;
					final ArrayList<String> pagename = new ArrayList<String>();
					for (PageData page : pageCollection.getPageList()) {
						if (!page._ID.equals("1")) {
							String my = Integer.toString(p);

							az += "\""
									+ my
									+ "\""
									+ ":\"SELECT eid,name,start_time,end_time,attending_count,description,location,pic_big,pic_cover,update_time,creator FROM event WHERE eid IN (SELECT eid from event_member WHERE uid = "
									+ page._ID + ")" + " AND (end_time > "
									+ "'" + current_time + "'"
									+ "OR (end_time = '' AND start_time > "
									+ "'" + current_time + "'))" + "\"" + ",";
							p++;
							pagename.add(page.name);
						}
					}
					az += "}";
					bundle.clear();
					bundle.putString("q", az);

					Request.Callback callback = new Request.Callback() {
						public void onCompleted(Response response) {
							try {
								JSONObject jsonObject = response
										.getGraphObject().getInnerJSONObject();
								JSONArray jDataArray = jsonObject
										.getJSONArray("data");
								int m = 0;
								JSONArray jArray = new JSONArray();
								JSONObject myJson = new JSONObject();
								bundle.clear();
								int h = jDataArray.length();
								while (m < h) {
									int n = 0;
									jsonObject = jDataArray.getJSONObject(m);
									jArray = jsonObject
											.getJSONArray("fql_result_set");

									while (n < jArray.length()) {
										boolean add = true;
										myJson = jArray.getJSONObject(n);
										for (EventData event : eventCollection
												.getUpdateEventList()) {
											if (event.event_ID.equals(myJson
													.getString("eid"))) {
												add = false;
											}
										}
										if (add) {
											final EventData event = new EventData();
											// scarico il nuovo evento
											event.event_ID = myJson
													.getString("eid");
											event.desc = myJson
													.getString("description");
											event.loc = myJson
													.getString("location");
											event.startMillis = getMillis(
													myJson.getString("start_time"),
													event);
											String end_time = "";
											long day = 0;

											if (jsonObject.isNull("end_time")) {
												end_time = jsonObject
														.getString("start_time");
												day = 86400;
											} else {
												end_time = jsonObject
														.getString("end_time");
											}
											if (day > 0) {
												long millis = Long
														.parseLong(getMillis(
																end_time, event))
														+ day;
												event.endMillis = Long
														.toString(millis);
											} else {
												event.endMillis = getMillis(
														end_time, event);
											}
											event.name = myJson
													.getString("name");
											event.last_update = getMillis(
													myJson.getString("update_time"),
													event);
											event.attending_count = myJson
													.getInt("attending_count");
											URL img_value = null;
											if (!myJson.isNull("pic_cover")) {
												JSONObject j = myJson
														.getJSONObject("pic_cover");
												img_value = new URL(
														j.getString("source"));
												event.hasCover = true;

											} else {
												img_value = new URL(
														myJson.getString("pic_big"));
											}
											Bitmap image = BitmapFactory
													.decodeStream(img_value
															.openConnection()
															.getInputStream());
											saveImageToDisk(event.event_ID,
													image);

											dayOfWeek(event.startMillis,
													event.endMillis);
											event.dateStart = monthNameStart;
											event.dayStart = dayOfWeekStart;
											event.timeStart = timeStart;
											event.dateEnd = monthNameEnd;
											event.dayEnd = dayOfWeekEnd;
											event.timeEnd = timeEnd;
											event.isInProgress = isInProgress;
											isInProgress = false;
											event.parentPage_ID = pageCollection
													.getPageList().get(m + 1)._ID;
											event.parentPageName = pageCollection
													.getPageList().get(m + 1).name;

											if (userID == null) {
												getUserID();
											}

											Request.Callback callback = new Request.Callback() {
												public void onCompleted(
														Response response) {
													JSONArray jArrayRSVP = new JSONArray();
													JSONObject jsonRSVP = response
															.getGraphObject()
															.getInnerJSONObject();
													try {
														jArrayRSVP = jsonRSVP
																.getJSONArray("data");

														if (jArrayRSVP.length() == 0) {
															event.status_attending = "Not Invited";
														} else {
															jsonRSVP = jArrayRSVP
																	.getJSONObject(0);
															String rsvp = jsonRSVP
																	.getString("rsvp_status");
															event.status_attending = rsvp;
														}
													} catch (Exception e) {
														Log.e("RSVPservicer",
																e.toString());
													}
												}
											};
											Request request = new Request(
													session, event.event_ID
															+ "/invited/"
															+ userID,
													new Bundle(),
													HttpMethod.GET, callback);
											request.executeAndWait();
											eventCollection
													.getUpdateEventList().add(
															event);
											newDownloads = true;

										}
										n++;
									}
									m++;
								}
							} catch (Exception e) {
								Log.e("new events - service", e.toString());
							}
						}
					};
					Request request = new Request(session, "fql", bundle,
							HttpMethod.GET, callback);
					request.executeAndWait();

				}

			}
		};
		Request request = new Request(session, "fql", bundle, HttpMethod.GET,
				callback);
		request.executeAndWait();

		if (newDownloads) {
			Log.i("ServiceUpdate", "new downloads");
		}

		if (!addEvent.isEmpty() || newDownloads || !removeEvent.isEmpty()) {
			eventCollection.cleanUpdateEventList();
			eventCollection.restoreEventListFromUpdate();
			eventCollection.sortByDate();
			eventCollection.saveCompleteEventList();
			eventCollection.saveToDisk(ServiceUpdate.this);
			newDownloads = false;
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean("service_updated", true);
			editor.commit();
			Log.i("ServiceUpdate", "saved updates");

		}

		Log.i("ServiceUpdate", "end timer");

	}

	private boolean isOnline() {

		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private void updateRSVPstatus() {

		Bundle bundleA = new Bundle();
		Bundle bundleB = new Bundle();
		Bundle bundleC = new Bundle();

		if (userID == null) {
			getUserID();
		}

		String a = "SELECT eid FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid ="
				+ userID + " and rsvp_status=\"attending\" )";
		String b = "SELECT eid FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid ="
				+ userID + " and rsvp_status=\"unsure\" )";
		String c = "SELECT eid FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid ="
				+ userID + " and rsvp_status=\"declined\" )";

		bundleA.putString("q", a);
		bundleB.putString("q", b);
		bundleC.putString("q", c);

		Request requestA = new Request(session, "fql", bundleA, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						try {
							JSONObject jsonA = response.getGraphObject()
									.getInnerJSONObject();
							JSONArray jDataArrayA = jsonA.getJSONArray("data");
							for (int i = 0; i < jDataArrayA.length(); i++) {
								jsonA = jDataArrayA.getJSONObject(i);
								for (EventData event : eventCollection
										.getCompleteEventList()) {
									if (event.event_ID.equals(jsonA
											.getString("eid"))) {
										event.status_attending = "attending";
									}
								}
							}
						} catch (Exception e) {
							Log.e("batchA", e.toString());
						}
					}
				});

		Request requestB = new Request(session, "fql", bundleB, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						try {
							JSONObject jsonB = response.getGraphObject()
									.getInnerJSONObject();
							JSONArray jDataArrayB = jsonB.getJSONArray("data");

							for (int i = 0; i < jDataArrayB.length(); i++) {
								jsonB = jDataArrayB.getJSONObject(i);
								for (EventData event : eventCollection
										.getCompleteEventList()) {
									if (event.event_ID.equals(jsonB
											.getString("eid"))) {
										event.status_attending = "unsure";
									}
								}
							}
						} catch (Exception e) {
							Log.e("batchB", e.toString());
						}
					}
				});

		Request requestC = new Request(session, "fql", bundleC, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						try {
							JSONObject jsonC = response.getGraphObject()
									.getInnerJSONObject();
							JSONArray jDataArrayC = jsonC.getJSONArray("data");

							for (int i = 0; i < jDataArrayC.length(); i++) {
								jsonC = jDataArrayC.getJSONObject(i);
								for (EventData event : eventCollection
										.getCompleteEventList()) {
									if (event.event_ID.equals(jsonC
											.getString("eid"))) {
										event.status_attending = "declined";
									}
								}
							}
						} catch (Exception e) {
							Log.e("batchC", e.toString());
						}

					}
				});

		RequestBatch batch = new RequestBatch(requestA, requestB, requestC);
		batch.executeAndWait();

	}

	private void dayOfWeek(String start, String end) {

		int dayOfWeekInteger;
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		Calendar currentCal = Calendar.getInstance();
		start += "000";
		end += "000";
		long start_millis = Long.parseLong(start);
		long end_millis = Long.parseLong(end);
		startCal.setTimeInMillis(start_millis);
		endCal.setTimeInMillis(end_millis);

		Formatter form = new Formatter();
		form.format("%02d", startCal.get(Calendar.MINUTE));
		timeStart = Integer.toString(startCal.get(Calendar.HOUR_OF_DAY)) + ":"
				+ form.toString();
		form = new Formatter();
		form.format("%02d", endCal.get(Calendar.MINUTE));
		timeEnd = Integer.toString(endCal.get(Calendar.HOUR_OF_DAY)) + ":"
				+ form.toString();
		dayOfWeekStart = "vuoto";
		dayOfWeekEnd = "vuoto";
		if (startCal.get(Calendar.DAY_OF_YEAR) == currentCal
				.get(Calendar.DAY_OF_YEAR)) {
			dayOfWeekStart = "Today";
		}

		if (startCal.get(Calendar.DAY_OF_YEAR) == currentCal
				.get(Calendar.DAY_OF_YEAR) + 1) {
			dayOfWeekStart = "Tomorrow";
		}

		if (startCal.get(Calendar.DAY_OF_YEAR) < currentCal
				.get(Calendar.DAY_OF_YEAR)
				|| (startCal.get(Calendar.DAY_OF_YEAR) == currentCal
						.get(Calendar.DAY_OF_YEAR) && (startCal
						.get(Calendar.HOUR_OF_DAY)
						* 100
						+ startCal.get(Calendar.MINUTE) < currentCal
						.get(Calendar.HOUR_OF_DAY)
						* 100
						+ currentCal.get(Calendar.MINUTE)))) {
			isInProgress = true;
		}
		if (dayOfWeekStart.equals("vuoto")) {

			dayOfWeekInteger = startCal.get(Calendar.DAY_OF_WEEK);
			switch (dayOfWeekInteger - 1) {
			case 0:
				dayOfWeekStart = "Sunday";
				break;
			case 1:
				dayOfWeekStart = "Monday";
				break;
			case 2:
				dayOfWeekStart = "Tuesday";
				break;
			case 3:
				dayOfWeekStart = "Wednesday";
				break;
			case 4:
				dayOfWeekStart = "Thrusday";
				break;
			case 5:
				dayOfWeekStart = "Friday";
				break;
			case 6:
				dayOfWeekStart = "Saturday";
				break;

			}
		}

		if (endCal.get(Calendar.DAY_OF_YEAR) == currentCal
				.get(Calendar.DAY_OF_YEAR)) {
			dayOfWeekEnd = "Today";
		}

		if (endCal.get(Calendar.DAY_OF_YEAR) == currentCal
				.get(Calendar.DAY_OF_YEAR) + 1) {
			dayOfWeekEnd = "Tomorrow";
		}

		if (dayOfWeekEnd.equals("vuoto")) {

			dayOfWeekInteger = startCal.get(Calendar.DAY_OF_WEEK);
			switch (dayOfWeekInteger - 1) {
			case 0:
				dayOfWeekEnd = "Sunday";
				break;
			case 1:
				dayOfWeekEnd = "Monday";
				break;
			case 2:
				dayOfWeekEnd = "Tuesday";
				break;
			case 3:
				dayOfWeekEnd = "Wednesday";
				break;
			case 4:
				dayOfWeekEnd = "Thrusday";
				break;
			case 5:
				dayOfWeekEnd = "Friday";
				break;
			case 6:
				dayOfWeekEnd = "Saturday";
				break;

			}
		}
		switch (startCal.get(Calendar.MONTH)) {
		case 0:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Jan";
			break;
		case 1:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Feb";
			break;
		case 2:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Mar";
			break;
		case 3:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Apr";
			break;
		case 4:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "May";
			break;
		case 5:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Jun";
			break;
		case 6:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Jul";
			break;
		case 7:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Aug";
			break;
		case 8:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Sep";
			break;
		case 9:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Oct";
			break;
		case 10:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Nov";
			break;
		case 11:
			monthNameStart = Integer.toString(startCal
					.get(Calendar.DAY_OF_MONTH)) + " " + "Dec";
			break;

		}

		switch (endCal.get(Calendar.MONTH)) {
		case 0:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Jan";
			break;
		case 1:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Feb";
			break;
		case 2:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Mar";
			break;
		case 3:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Apr";
			break;
		case 4:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "May";
			break;
		case 5:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Jun";
			break;
		case 6:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Jul";
			break;
		case 7:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Aug";
			break;
		case 8:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Sep";
			break;
		case 9:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Oct";
			break;
		case 10:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Nov";
			break;
		case 11:
			monthNameEnd = Integer.toString(endCal.get(Calendar.DAY_OF_MONTH))
					+ " " + "Dec";
			break;

		}

	}

	private void getMyEvents() {
		Bundle bundle = new Bundle();
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance(TimeZone
				.getTimeZone("America/Los_Angeles"));
		long mi = cal.get(Calendar.ZONE_OFFSET);
		long mu = cal2.get(Calendar.ZONE_OFFSET);
		long def = mi - mu;
		if (def < 0)
			def = def * (-1);
		String current_time = Long.toString(cal.getTimeInMillis() + def);
		current_time = current_time.substring(0, 10);
		PageData page = new PageData();
		page._ID = "1";
		page.name = "My Events..";
		page.address = "";
		pageCollection.addPageToFavourites(page);
		int checked = mPrefs.getInt("myEventsSettings", 2);
		switch (checked) {
		case 0:
			myEventsSettings = "and rsvp_status=\"attending\"";
			break;
		case 1:
			myEventsSettings = "and (rsvp_status=\"attending\" or rsvp_status=\"unsure\")";
			break;
		case 2:
			myEventsSettings = "";
			break;
		}

		if (userID == null) {
			getUserID();
		}

		String a = "SELECT name, update_time,host,creator, location,description, pic_big, eid,start_time,end_time FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid ="
				+ userID
				+ myEventsSettings
				+ ")"
				+ " AND (end_time > "
				+ "'"
				+ current_time
				+ "'"
				+ "OR (end_time = '' AND start_time > "
				+ "'" + current_time + "'))";
		bundle.putString("q", a);

		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				JSONObject json;
				JSONArray jDataArray;
				try {
					json = response.getGraphObject().getInnerJSONObject();
					jDataArray = json.getJSONArray("data");
					for (int i = 0; i < jDataArray.length(); i++) {
						json = jDataArray.getJSONObject(i);
						boolean add = true;
						for (PageData currentPage : pageCollection
								.getPageList()) {
							if (currentPage._ID.equals(json
									.getString("creator")))
								add = false;
						}

						for (EventData currentEvent : eventCollection
								.getUpdateEventList()) {
							if (currentEvent.event_ID.equals(json
									.getString("eid")))
								add = false;
						}

						if (add) {
							final EventData event = new EventData();
							event.event_ID = json.getString("eid");
							event.name = json.getString("name");
							event.desc = json.getString("description");
							event.loc = json.getString("location");
							event.startMillis = getMillis(
									json.getString("start_time"), event);
							String end_time = "";
							long day = 0;

							if (json.isNull("end_time")) {
								end_time = json.getString("start_time");
								day = 86400;
							} else {
								end_time = json.getString("end_time");
							}
							if (day > 0) {
								long millis = Long.parseLong(getMillis(
										end_time, event)) + day;
								event.endMillis = Long.toString(millis);
							} else {
								event.endMillis = getMillis(end_time, event);
							}
							event.parentPage_ID = "1";
							event.parentPageName = pageCollection
									.getPageByID("1").name;
							event.last_update = getMillis(
									json.getString("update_time"), event);
							dayOfWeek(event.startMillis, event.endMillis);
							event.dateStart = monthNameStart;
							event.dayStart = dayOfWeekStart;
							event.timeStart = timeStart;
							event.dateEnd = monthNameEnd;
							event.dayEnd = dayOfWeekEnd;
							event.timeEnd = timeEnd;
							event.isInProgress = isInProgress;
							isInProgress = false;

							if (userID == null) {
								getUserID();
							}

							Request.Callback callback = new Request.Callback() {
								public void onCompleted(Response response) {
									JSONArray jArrayRSVP = new JSONArray();
									JSONObject jsonRSVP = response
											.getGraphObject()
											.getInnerJSONObject();
									try {
										jArrayRSVP = jsonRSVP
												.getJSONArray("data");

										if (jArrayRSVP.length() == 0) {
											event.status_attending = "Not Invited";
										} else {
											jsonRSVP = jArrayRSVP
													.getJSONObject(0);
											String rsvp = jsonRSVP
													.getString("rsvp_status");
											event.status_attending = rsvp;
										}
									} catch (Exception e) {
										Log.e("RSVPservicemyevents",
												e.toString());
									}
								}
							};
							Request request = new Request(session,
									event.event_ID + "/invited/" + userID,
									new Bundle(), HttpMethod.GET, callback);
							request.executeAndWait();

							eventCollection.getUpdateEventList().add(event);
							URL img_value = null;
							if (!json.isNull("pic_cover")) {
								JSONObject j = json.getJSONObject("pic_cover");
								img_value = new URL(j.getString("source"));
								eventCollection
										.getUpdateEventByID(event.event_ID).hasCover = true;

							} else {
								img_value = new URL(json.getString("pic_big"));
							}
							Bitmap image = BitmapFactory.decodeStream(img_value
									.openConnection().getInputStream());
							saveImageToDisk(event.event_ID, image);
							newDownloads = true;
							;

						}
					}
				} catch (Exception e) {
					Log.e("getMyEvents - service", e.toString());
				}
			}
		};
		Request request = new Request(session, "fql", bundle, HttpMethod.GET,
				callback);
		request.executeAndWait();

		pageCollection.saveToDisk(ServiceUpdate.this);
	}

	private void saveImageToDisk(String ID, Bitmap image) {
		try {
			String path = new String(ID);
			java.io.FileOutputStream out = this.openFileOutput(path,
					Context.MODE_PRIVATE);
			image.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Bitmap readImageFromDisk(String ID) {
		try {
			java.io.FileInputStream in = this.openFileInput(ID);
			Bitmap image = BitmapFactory.decodeStream(in);
			return image;
		} catch (Exception e) {
			return null;
		}
	}

	private String getMillis(String UNIX, EventData event) {
		if (UNIX.length() == 10) {
			event.unix = true;

			if (UNIX.substring(4, 5).equals("-")) {
				long millis = 0;
				Calendar cal = Calendar.getInstance();
				int year = Integer.parseInt(UNIX.substring(0, 4));
				int month = Integer.parseInt(UNIX.substring(5, 7)) - 1;
				int day = Integer.parseInt(UNIX.substring(8, 10));
				cal.set(year, month, day, 0, 0, 0);
				millis = cal.getTimeInMillis();
				return Long.toString(millis).substring(0, 10);
			}

			return UNIX;
		} else {
			long millis = 0;
			Calendar cal = Calendar.getInstance();
			int year = Integer.parseInt(UNIX.substring(0, 4));
			int month = Integer.parseInt(UNIX.substring(5, 7)) - 1;
			int day = Integer.parseInt(UNIX.substring(8, 10));
			int hour = Integer.parseInt(UNIX.substring(11, 13));
			int minute = Integer.parseInt(UNIX.substring(14, 16));
			cal.set(year, month, day, hour, minute, 0);
			millis = cal.getTimeInMillis();
			event.unix = true;
			return Long.toString(millis).substring(0, 10);
		}
	}

	private void getUserID() {

		Bundle bundle = new Bundle();

		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				JSONObject j = response.getGraphObject().getInnerJSONObject();
				try {
					String s = j.getString("id");
					userID = s;
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("user_id", userID);
					editor.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		Request request = new Request(session, "me", bundle, HttpMethod.GET,
				callback);
		request.executeAndWait();

	}

	private void getUserIDasync() {

		Bundle bundle = new Bundle();

		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				JSONObject j = response.getGraphObject().getInnerJSONObject();
				try {
					String s = j.getString("id");
					userID = s;
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("user_id", userID);
					editor.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		Request request = new Request(session, "me", bundle, HttpMethod.GET,
				callback);
		request.executeAsync();

	}

	private void checkCriticalUpdate() {
		Bundle bundle = new Bundle();
		String a = "SELECT name,description FROM event WHERE eid = 510273559035940";
		bundle.putString("q", a);
		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {

				JSONArray jDataArray;
				JSONObject json = new JSONObject();
				try {
					json = response.getGraphObject().getInnerJSONObject();
					jDataArray = json.getJSONArray("data");
					json = jDataArray.getJSONObject(0);
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("message", json.getString("description"));
					editor.putInt("versionCode",
							Integer.parseInt(json.getString("name")));
					editor.putBoolean("criticalUpdate", true);
					editor.commit();
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		};
		Request request = new Request(session, "fql", bundle, HttpMethod.GET,
				callback);
		request.executeAsync();

	}

	private void toast(final String string, final boolean Long) {

		if (!Long)
			Toast.makeText(ServiceUpdate.this, string, Toast.LENGTH_SHORT)
					.show();
		else
			Toast.makeText(ServiceUpdate.this, string, Toast.LENGTH_LONG)
					.show();
	}

	private void getSession() {
		if ((session == null) || session.isClosed()) {
			session = Session.getActiveSession();
			if (session == null) {
				session = Session.openActiveSessionFromCache(this);
			}
			if (session != null & session.isOpened()) {
				service();
			}
		}
	}

}
