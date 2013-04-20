package com.mosquitolabs.tonight;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.AccessToken;
import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class FacebookeventsActivity extends SherlockFragmentActivity {

	private EventCollection eventCollection = EventCollection.getInstance();
	private PageCollection pageCollection = PageCollection.getInstance();
	private Preferences preferences = Preferences.getInstance();

	private JSONArray jDataArray;
	private JSONObject json;
	private JSONObject jsonObject;
	private JSONArray jarrayLikes;
	private JSONArray jarrayAround;

	private ListView listViewMain;
	private ListView listViewPagesILike;
	private ListView listViewPage;

	private int z;
	private int entrate = 0;
	private int userLikesInt;
	private int placesInt = 0;
	private int tabSelected = 1;
	private int selectedPage = 0;

	private myCustomAdapter eventArrayAdapter;
	private myCustomAdapterPage pageArrayAdapter;
	private myCustomAdapterPagesILike pagesILikeArrayAdapter;

	private String userID = "";
	private String gender;
	private String name_page;
	private String currentPageID = null;
	private String status;
	private String me;
	final String APP_ID = "219909391458551";

	private SharedPreferences mPrefs;

	private Button buttonPlace;
	private Button buttonNavigate;
	private Button login;

	private TextView textDesc;
	private TextView textDescYEY;
	private TextView textEnd;
	private TextView textStart;
	private TextView textLoc;
	private TextView textName;
	private TextView textNamePage;
	private TextView textEventEmpty;
	private TextView textPageEmpty;
	private TextView textAttending;
	private TextView filterPages;
	private TextView filterEvents;

	private ImageView eventPicture;
	private double latitude;
	private double longitude;

	private com.actionbarsherlock.view.MenuItem share;
	private com.actionbarsherlock.view.MenuItem refresh;
	private com.actionbarsherlock.view.MenuItem seeOnFacebook;
	private com.actionbarsherlock.view.MenuItem search;
	private com.actionbarsherlock.view.MenuItem viewall;
	private com.actionbarsherlock.view.MenuItem rsvp;
	private com.actionbarsherlock.view.MenuItem like;
	private com.actionbarsherlock.view.MenuItem info_2nite;
	private com.actionbarsherlock.view.MenuItem info_menu;
	private com.actionbarsherlock.view.MenuItem reset;
	private com.actionbarsherlock.view.MenuItem sortEvents;
	private com.actionbarsherlock.view.MenuItem sortPages;
	private com.actionbarsherlock.view.MenuItem places;
	private com.actionbarsherlock.view.MenuItem calendar;

	private long counterStart;
	private long counterStop;
	private RelativeLayout relativeFilter;
	private String name;
	private String desc;
	private String dateStart;
	private String dayStart;
	private String dayEnd;
	private String timeStart;
	private String dateEnd;
	private String timeEnd;
	private String dayOfWeekStart;
	private String dayOfWeekEnd;
	private String monthNameStart = "";
	private String monthNameEnd = "";
	private String attendingCount;
	private String loc;
	private String title2 = "events";
	private String title3 = "description";
	private boolean isReading = false;
	private boolean isDoownloading = false;
	private boolean isComingFromUserLikes = false;
	private boolean isInProgress = false;
	private boolean isEventsJustOpened = true;
	private boolean isPageJustOpened = true;
	private boolean isPageAvailable = false;
	private boolean newDownloads = false;
	private boolean isReloading = false;
	private boolean isFirstTimeAround = true;
	private boolean likesSorted = false;
	private boolean aroundSorted = false;
	private boolean eventHasAnEnd = true;
	private boolean isBirthdayWeek = false;
	private boolean isActionbarAvailable = false;
	private boolean isAdViewVisible;
	private boolean isFromFilter = false;
	private boolean hasCover = false;
	private boolean isContentMain = false;
	private boolean myEventsSettingsDownload = false;
	private boolean permissionAsked = false;

	private ProgressDialog progressDialog;
	private ProgressThread progressThread;

	private ViewPager pagerMain;
	private Bitmap mIcon1;
	private ProgressBar progressLogin;
	private LinearLayout progressLoginPagesILike;
	private ArrayList<String> items = new ArrayList<String>();
	private JSONArray jArrayPhotoStream = new JSONArray();

	private AdView adView;

	private TextView noPagesLike;
	private String myEventsSettings = "";
	public String filter = "all";
	private Tracker MyTracker;
	private com.actionbarsherlock.app.ActionBar actionbar;
	private AdRequest adRequest = new AdRequest();

	private Session session;

	// private MainFragment mainFragment;
	private static final String TAG = "MainFragment";
	private UiLifecycleHelper uiHelper;
	private boolean isResumed = false;
	private Fragment[] fragments = new Fragment[3];

	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private static final List<String> READ_PERMISSIONS = Arrays.asList(
			"user_likes", "user_events", "user_birthday");
	private static final List<String> PUBLISH_PERMISSIONS = Arrays
			.asList("rsvp_event");

	// private Facebook mFacebook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		actionbar = getSupportActionBar();
		actionbar.hide();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = mPrefs.edit();
		String access_token = mPrefs.getString("access_token", null);
		Context ctx = getApplicationContext();
		session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(ctx);
			}

			if (access_token != null) {

				editor.putString("access_token", null);
				editor.commit();
				AccessToken accessToken = AccessToken
						.createFromExistingAccessToken(access_token, null,
								null, null, null);

				session.open(accessToken, statusCallback);
				Session.setActiveSession(session);
			} else {

				Session.setActiveSession(session);
				if (session.getState()
						.equals(SessionState.CREATED_TOKEN_LOADED)) {
					session.openForRead(new Session.OpenRequest(this)
							.setCallback(statusCallback));
				}

			}
		}
		editor = mPrefs.edit();
		editor.putBoolean("service_updated", false);
		editor.putString("access_tokenService", session.getAccessToken());
		editor.commit();
		initializeMain();
		mPrefs.registerOnSharedPreferenceChangeListener(mListener);
		login = (Button) findViewById(R.id.buttonLogin);
		progressLogin = (ProgressBar) findViewById(R.id.progressBarLogin);

		if (!session.isOpened()) {

			login.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					loginVisible(false);
					progressLoginVisible(true);
					Session session = Session.getActiveSession();
					if ((!session.isOpened() && !session.isClosed())) {
						session.openForRead(new Session.OpenRequest(
								FacebookeventsActivity.this).setCallback(
								statusCallback)
								.setPermissions(READ_PERMISSIONS));
					} else {
						Session.openActiveSession(FacebookeventsActivity.this,
								true, statusCallback);
					}

				}

			});
			if (access_token != null) {
			}
		} else {
			loginVisible(false);
			progressLoginVisible(true);
			showMyLogo();
		}

	}

	private void initializeMain() {
		gender = mPrefs.getString("gender", null);
		String year = mPrefs.getString("year", null);
		String day = mPrefs.getString("day", null);
		String month = mPrefs.getString("month", null);
		int checked = mPrefs.getInt("myEventsSettings", 2);
		SharedPreferences.Editor editor = mPrefs.edit();

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
		editor.putInt("myEventsSettings", checked);
		int x = mPrefs.getInt("entrate", 0);
		editor.putInt("entrate", x + 1);

		isBirthdayWeek = mPrefs.getBoolean("isBirthdayWeek", false);
		if (year != null && day != null && month != null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1,
					Integer.parseInt(day));
			adRequest.setBirthday(cal);
			cal.set(cal.get(Calendar.YEAR), Integer.parseInt(month) - 1,
					Integer.parseInt(day));
			Calendar today = Calendar.getInstance();

			if (!isBirthdayWeek
					&& today.get(Calendar.DAY_OF_YEAR) == cal
							.get(Calendar.DAY_OF_YEAR)) {
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;
				builder = new AlertDialog.Builder(FacebookeventsActivity.this);
				builder.setTitle("A present for you!");
				builder.setMessage("HAPPY BIRTDAY!\n2NITE is going to celebrate your birthday by removing every ad for one week!");
				builder.setNeutralButton("Ok",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
				alertDialog = builder.create();
				alertDialog.show();
				isBirthdayWeek = true;
				editor.putBoolean("isBirthdayWeek", isBirthdayWeek);
			}
			if (isBirthdayWeek) {
				cal.add(Calendar.DAY_OF_YEAR, 7);
				if (cal.get(Calendar.DAY_OF_YEAR) <= today
						.get(Calendar.DAY_OF_YEAR)
						|| (cal.get(Calendar.DAY_OF_YEAR) - 7) > today
								.get(Calendar.DAY_OF_YEAR)) {
					isBirthdayWeek = false;
					editor.putBoolean("isBirthdayWeek", isBirthdayWeek);
				}
			}
		}

		if (gender != null) {
			if (gender.equals("male"))
				adRequest.setGender(AdRequest.Gender.MALE);
			else
				adRequest.setGender(AdRequest.Gender.FEMALE);
		}
		editor.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
		EasyTracker.getInstance().activityStart(this);
		MyTracker = EasyTracker.getTracker();
	}

	@Override
	public void onPause() {
		super.onPause();
		isResumed = false;
	}

	@Override
	protected void onResume() {
		eventCollection.readFromDisk(this);
		pageCollection.readFromDisk(this);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if ((preferences.getModifiedPages() || isEventsJustOpened)
				&& !isReading && isContentMain) {
			read();

		} else {
			if (mPrefs.getBoolean("update", false)) {
				if (isOnline()) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putBoolean("update", false);
					editor.commit();
					listViewMain.setVisibility(View.GONE);
					getMyEvents(true);
				} else {
					toast("No internet connection", false);
				}

			}
		}
		if (!isEventsJustOpened) {
			listViewMain.setAdapter(eventArrayAdapter);
			refreshEventsAdapter();
		}
		viewAll();
		Log.i("onResume", "onResume");
		super.onResume();
		isResumed = true;

	}

	@Override
	protected void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
		eventCollection.saveToDisk(this);
		pageCollection.saveToDisk(this);
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onDestroy() {
		mPrefs.unregisterOnSharedPreferenceChangeListener(mListener);
		startService();
		super.onDestroy();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private synchronized void showMyLogo() {

		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
			@Override
			public Boolean doInBackground(Void... params) {
				eventCollection.readFromDisk(FacebookeventsActivity.this);
				pageCollection.readFromDisk(FacebookeventsActivity.this);
				updateEventDays();
				// permissions();
				updateAttendingCount();
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					setContentView(R.layout.main);

					isContentMain = true;
					actionbar.show();
					BitmapDrawable background = new BitmapDrawable(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.darkstripes_action));

					actionbar.setBackgroundDrawable(background);
					pageArrayAdapter = new myCustomAdapterPage(
							FacebookeventsActivity.this);
					eventArrayAdapter = new myCustomAdapter(
							FacebookeventsActivity.this,
							FacebookeventsActivity.this);

					ViewPagerAdapter adapter = new ViewPagerAdapter(
							FacebookeventsActivity.this);
					pagerMain = (ViewPager) findViewById(R.id.viewpager);
					pagerMain.setPageMargin(15);
					TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
					indicator.setBackgroundColor(Color.rgb(251, 148, 11));
					Bitmap bmp = BitmapFactory.decodeResource(getResources(),
							R.drawable.stripe_orange);
					BitmapDrawable background2 = new BitmapDrawable(bmp);
					background2.setTileModeXY(Shader.TileMode.REPEAT,
							Shader.TileMode.REPEAT);
					indicator.setBackgroundDrawable(background2);
					pagerMain.setAdapter(adapter);
					indicator.setViewPager(pagerMain,
							FacebookeventsActivity.this);

					if (isEventsJustOpened
							&& (pageCollection.getPageList().isEmpty() || eventCollection
									.getCompleteEventList().isEmpty())) {

						pagerMain.setCurrentItem(0);
						setPageOne();

					} else {

						pagerMain.setCurrentItem(1);
					}

					pagesILikeArrayAdapter = new myCustomAdapterPagesILike(
							FacebookeventsActivity.this);

					items.add("Not Answered");
					items.add("Going");
					items.add("Maybe");
					items.add("Declined");
					startService();
					if (pageCollection.getPageList().isEmpty()) {
						dialogUserLikes();
					} else {
						int x = mPrefs.getInt("entrate", 0);
						if (x > 3
								&& eventCollection.getCompleteEventList()
										.size() > 10
								&& !mPrefs.getBoolean("entrateDone", false)) {
							dialogFeedback();
						}
					}
				}
			}

		};
		task.execute();

	}

	private void startService() {
		Object obj = mPrefs.getBoolean("service_status", false);
		if (obj == null || !mPrefs.getBoolean("service_status", false)) {
			Intent serviceIntent = new Intent(FacebookeventsActivity.this,
					ServiceUpdate.class);
			serviceIntent.putExtra("userID", mPrefs.getString("user_id", null));
			serviceIntent.putExtra("accessToken",
					mPrefs.getString("access_token", null));
			serviceIntent.putExtra("expires",
					mPrefs.getLong("access_expires", 0));
			startService(serviceIntent);
		} else {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putBoolean("service_status", false);
			editor.commit();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
		if (Session.getActiveSession().isOpened()) {
			facebook_auth_complete();
		} else {
			loginVisible(true);
			progressLoginVisible(false);
		}
	}

	private void facebook_auth_complete() {
		setContentView(R.layout.main);

		isContentMain = true;
		actionbar.show();
		BitmapDrawable background = new BitmapDrawable(
				BitmapFactory.decodeResource(getResources(),
						R.drawable.darkstripes_action));

		actionbar.setBackgroundDrawable(background);

		pageArrayAdapter = new myCustomAdapterPage(FacebookeventsActivity.this);
		eventArrayAdapter = new myCustomAdapter(FacebookeventsActivity.this,
				FacebookeventsActivity.this);

		ViewPagerAdapter adapter = new ViewPagerAdapter(
				FacebookeventsActivity.this);
		pagerMain = (ViewPager) findViewById(R.id.viewpager);
		pagerMain.setPageMargin(15);
		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setBackgroundColor(Color.rgb(251, 148, 11));
		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.stripe_orange);
		BitmapDrawable background2 = new BitmapDrawable(bmp);
		background2.setTileModeXY(Shader.TileMode.REPEAT,
				Shader.TileMode.REPEAT);
		indicator.setBackgroundDrawable(background2);
		pagerMain.setAdapter(adapter);
		indicator.setViewPager(pagerMain, FacebookeventsActivity.this);

		eventCollection.readFromDisk(FacebookeventsActivity.this);
		pageCollection.readFromDisk(FacebookeventsActivity.this);
		if (pageCollection.getPageList().isEmpty()
				|| eventCollection.getCompleteEventList().isEmpty()) {

			pagerMain.setCurrentItem(0);
			setPageOne();

		} else {
			pagerMain.setCurrentItem(1);
		}
		pagesILikeArrayAdapter = new myCustomAdapterPagesILike(
				FacebookeventsActivity.this);

		if (pageCollection.getPageList().isEmpty()) {

			dialogUserLikes();
		}

	}

	private synchronized void permissions() {
		AsyncTask<Void, Integer, Bitmap> task = new AsyncTask<Void, Integer, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				Bundle bundle = new Bundle();
				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {
						json = response.getGraphObject().getInnerJSONObject();
						try {
							jDataArray = json.getJSONArray("data");
							json = jDataArray.getJSONObject(0);
							SharedPreferences.Editor editor = mPrefs.edit();
							boolean b = json.isNull("rsvp_event");
							editor.putBoolean("rsvp", !b);
							editor.commit();
						} catch (JSONException e) {
							Log.i(TAG, "JSON error " + e.getMessage());
						}
					}
				};
				Request request = new Request(session, "me/permissions",
						bundle, HttpMethod.GET, callback);
				request.executeAndWait();

				return null;
			}
		};
		task.execute();
	}

	private synchronized void getUserLikes(final boolean likes) {
		AsyncTask<Void, Integer, Bitmap> task = new AsyncTask<Void, Integer, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				if (pageCollection.getPageSearchListRelevant().isEmpty()) {
					final Bundle bundle = new Bundle();
					final SharedPreferences.Editor editor = mPrefs.edit();
					if (pageCollection.getPageList().isEmpty()) {

						editor.putBoolean("sort_by_date", true);
						editor.putInt("distance", 5);

						Request.Callback callback = new Request.Callback() {
							public void onCompleted(Response response) {
								json = response.getGraphObject()
										.getInnerJSONObject();
								try {
									userID = json.getString("id");
									gender = json.getString("gender");

									if (!json.isNull("birthday")) {

										String birthday_temp = json
												.getString("birthday");
										if (birthday_temp.length() == 10) {
											String myMonth = birthday_temp
													.substring(0, 2);
											String myDay = birthday_temp
													.substring(3, 5);
											String myYear = birthday_temp
													.substring(6);
											Calendar cal = Calendar
													.getInstance();
											cal.set(Integer.parseInt(myYear),
													Integer.parseInt(myMonth) - 1,
													Integer.parseInt(myDay));

											editor.putString("year", myYear);
											editor.putString("month", myMonth);
											editor.putString("day", myDay);
											adRequest.setBirthday(cal);
										}
									}
									editor.putString("user_id", userID);
									editor.putString("gender", gender);
									editor.commit();
									if (gender.equals("male"))
										adRequest
												.setGender(AdRequest.Gender.MALE);
									else
										adRequest
												.setGender(AdRequest.Gender.FEMALE);
								} catch (Exception e) {
									// TODO: handle exception
								}
							}
						};
						Request request = new Request(session, "me", bundle,
								HttpMethod.GET, callback);
						request.executeAndWait();
					}

					bundle.putString("fields", "likes");
					Request.Callback callback = new Request.Callback() {
						public void onCompleted(Response response) {
							json = response.getGraphObject()
									.getInnerJSONObject();
						}
					};
					Request request = new Request(session, "me", bundle,
							HttpMethod.GET, callback);
					request.executeAndWait();

					if (tabSelected == 1) {
						try {
							bundle.clear();

							try {

								if (!json.isNull("likes")) {
									json = json.getJSONObject("likes");

									jDataArray = json.getJSONArray("data");
									String a = "SELECT pic_square,pic,pic_large,pic_cover,type,checkins,location,phone,fan_count,categories,description,name,page_id FROM page WHERE (page_id=";

									for (int i = 0; i < jDataArray.length(); i++) {
										json = jDataArray.getJSONObject(i);
										a += json.getString("id");
										if (i != jDataArray.length() - 1)
											a += " or page_id=";
									}
									a += ")";

									bundle.clear();
									bundle.putString("q", a);
								}
							} catch (Exception e) {
								// TODO: handle exception
							}

							Request.Callback callbackbig = new Request.Callback() {
								public void onCompleted(Response response) {
									json = response.getGraphObject()
											.getInnerJSONObject();
									try {
										jarrayLikes = json.getJSONArray("data");
										String a = "";
										a += "{";
										int l = jarrayLikes.length();
										for (int i = 0; i < jarrayLikes
												.length(); i++) {
											json = jarrayLikes.getJSONObject(i);
											PageData currentpage = new PageData();
											currentpage._ID = json
													.getString("page_id");
											String my = Integer.toString(i);
											a += "\""
													+ my
													+ "\""
													+ ":\"SELECT eid,name,creator,start_time,end_time,description,location,pic_big,update_time FROM event WHERE eid IN (SELECT eid from event_member WHERE uid = "
													+ currentpage._ID + ")"

													+ "\"";
											if (i != l - 1) {
												a += ",";
											}
										}
										a += "}";

										bundle.clear();
										bundle.putString("q", a);

									} catch (Exception e) {
										// TODO: handle exception
									}

									Request.Callback callback2 = new Request.Callback() {
										public void onCompleted(
												Response response) {
											json = response.getGraphObject()
													.getInnerJSONObject();
											try {
												jDataArray = json
														.getJSONArray("data");
												ArrayList<String> s = new ArrayList<String>();
												int q = 0;
												while (q < jDataArray.length()) {

													jsonObject = jDataArray
															.getJSONObject(q);
													JSONArray jArray = jsonObject
															.getJSONArray("fql_result_set");
													if (!jArray.isNull(0)) {
														json = jArray
																.getJSONObject(0);
														s.add(json
																.getString("creator"));
													}

													q++;
												}

												for (int i = 0; i < jarrayLikes
														.length(); i++) {
													json = jarrayLikes
															.getJSONObject(i);
													boolean add = false;

													for (String r : s) {
														if (pageCollection
																.getPageByID(json
																		.getString("page_id")) != null)
															break;

														String type = json
																.getString("type");
														if (r.equals(json
																.getString("page_id"))
																|| (type.equals("CONCERT VENUE")
																		|| type.equals("ARTS/ENTERTAINMENT/NIGHTLIFE")
																		|| type.equals("CLUB")
																		|| type.equals("BAR")
																		|| type.equals("ATTRACTIONS/THINGS TO DO")
																		|| type.equals("MOVIE THEATER")
																		|| type.equals("BOOK STORE")
																		|| type.equals("EVENT PLANNING/EVENT SERVICES")
																		|| type.equals("RESTAURANT/CAFE") || type
																			.equals("UNIVERSITY"))) {
															add = true;
															break;
														}
													}

													if (add && tabSelected == 1) {
														PageData page = new PageData();
														jsonObject = json
																.getJSONObject("location");
														page._ID = json
																.getString("page_id");
														page.name = json
																.getString("name");
														page.number_of_likes = json
																.getInt("fan_count");
														page.checkins = json
																.getInt("checkins");
														page.phone = json
																.getString("phone");
														page.desc = json
																.getString("description");
														page.picURL = new URL(
																json.getString("pic_large"));
														if (!json
																.isNull("pic_cover")) {
															JSONObject js = json
																	.getJSONObject("pic_cover");
															page.coverURL = new URL(
																	js.getString("source"));
														}

														String a = "";
														JSONArray jj = json
																.optJSONArray("categories");
														for (int j = 0; j < jj
																.length(); j++) {
															json = jj
																	.getJSONObject(j);
															if (a.length() == 0)
																a += json
																		.getString("name");
															else
																a += ", "
																		+ json.getString("name");
														}

														page.category = a;

														a = "";
														a += jsonObject
																.getString("street");
														if (a.length() > 0)
															a += ", ";
														if (jsonObject
																.has("city")) {
															a += jsonObject
																	.getString("city");
															if (a.length() > 0) {
																a += ", ";
															}
														}
														if (jsonObject
																.has("country"))
															a += jsonObject
																	.getString("country");

														page.address = a;
														pageCollection
																.getPageSearchListRelevant()
																.add(page);

													}
												}

											} catch (Exception e) {
												// TODO: handle exception
											}
										}
									};

									Request request2 = new Request(session,
											"fql", bundle, HttpMethod.GET,
											callback2);
									request2.executeAndWait();

								}
							};

							Request requestbig = new Request(session, "fql",
									bundle, HttpMethod.GET, callbackbig);
							requestbig.executeAndWait();

						} catch (Exception e) {
							Log.e("facebook_me", e.toString());
							if (!isOnline()) {
								toast("Internet connection lost. Try again later from the \"Suggest Pages\" menu",
										true);

							} else {
								toast("Well that's embarassing..\nAn error occurred. Try again later from the \"Suggested Pages\" menu",
										true);
							}
						}
					}
				}

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (tabSelected == 1) {
					long cStop = Calendar.getInstance().getTimeInMillis();
					MyTracker.sendTiming("Timing", cStop - counterStart,
							"Suggested Pages", "");
					if (!likesSorted) {
						pageCollection.sortSearchByName();
						likesSorted = true;
					}
					listViewPagesILike.setVisibility(View.VISIBLE);
					progressLoginPagesILikeVisible(false);
					refreshPagesILikeAdapter();
					if (pageCollection.getPageSearchList().isEmpty()) {
						noPagesLike
								.setText("Sorry, couldn't find any place to suggest you based on your likes.");
						noPagesLikeVisible(true);
					} else {
						noPagesLikeVisible(false);

					}

					startService();

					int i = 0;
					if (jarrayLikes.length() > 0) {
						getUserLikesImages(i);
					}
				}
			}

		};

		task.execute();
	}

	private synchronized void getPlacesAroundMe() {
		AsyncTask<Void, Integer, Bitmap> task = new AsyncTask<Void, Integer, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {

				if (pageCollection.getPageAroundMe().isEmpty()) {
					Bundle bundle = new Bundle();
					final SharedPreferences.Editor editor = mPrefs.edit();
					if (pageCollection.getPageList().isEmpty()) {
						Request.Callback callback = new Request.Callback() {
							public void onCompleted(Response response) {
								try {
									editor.putBoolean("sort_by_date", true);
									editor.putInt("distance", 5);
									json = response.getGraphObject()
											.getInnerJSONObject();
									userID = json.getString("id");
									gender = json.getString("gender");

									if (!json.isNull("birthday")) {

										String birthday_temp = json
												.getString("birthday");
										if (birthday_temp.length() == 10) {
											String myMonth = birthday_temp
													.substring(0, 2);
											String myDay = birthday_temp
													.substring(3, 5);
											String myYear = birthday_temp
													.substring(6);
											Calendar cal = Calendar
													.getInstance();
											cal.set(Integer.parseInt(myYear),
													Integer.parseInt(myMonth) - 1,
													Integer.parseInt(myDay));
											editor.putString("year", myYear);
											editor.putString("month", myMonth);
											editor.putString("day", myDay);
											adRequest.setBirthday(cal);
										}
									}
									editor.putString("user_id", userID);
									editor.putString("gender", gender);
									editor.commit();
									if (gender.equals("male"))
										adRequest
												.setGender(AdRequest.Gender.MALE);
									else
										adRequest
												.setGender(AdRequest.Gender.FEMALE);

								} catch (Exception e) {
									editor.commit();
								}
							}
						};
						Request request = new Request(session, "me", bundle,
								HttpMethod.GET, callback);
						request.executeAndWait();
					}

					if (mPrefs.getString("user_id", null) == null) {
						getUserID();
					}
					if (tabSelected == 2) {

						final String az = "Select page_id,name,location,fan_count,checkins,phone,description,pic,pic_square,pic_large,pic_cover,categories from page where page_id in (select page_id from place where distance(latitude, longitude,"
								+ "\""
								+ Double.toString(latitude)
								+ "\""
								+ ","
								+ "\""
								+ Double.toString(longitude)
								+ "\""
								+ ")<"
								+ 50000
								+ " and checkin_count>0"

								+ " limit "
								+ 500
								+ ")"

								+ "AND (type=\"CONCERT VENUE\" or type=\"ARTS/ENTERTAINMENT/NIGHTLIFE\"  or type=\"CLUB\" or type = \"ATTRACTIONS/THINGS TO DO\" or type =\"BAR\" or type=\"MOVIE THEATER\" or type=\"BOOK STORE\" or type = \"EVENT PLANNING/EVENT SERVICES\" or type=\"RESTAURANT/CAFE\" or type = \"UNIVERSITY\")";

						bundle = new Bundle();
						bundle.putString("q", az);

						Request.Callback callback = new Request.Callback() {
							public void onCompleted(Response response) {
								try {
									json = response.getGraphObject()
											.getInnerJSONObject();
									jarrayAround = json.getJSONArray("data");
									int i = 0;
									while (i < jarrayAround.length()) {
										json = jarrayAround.getJSONObject(i);

										PageData page = new PageData();
										jsonObject = json
												.getJSONObject("location");
										page._ID = json.getString("page_id");
										page.name = json.getString("name");
										page.number_of_likes = json
												.getInt("fan_count");
										page.checkins = json.getInt("checkins");
										page.phone = json.getString("phone");
										page.desc = json
												.getString("description");
										page.picURL = new URL(
												json.getString("pic_large"));
										if (!json.isNull("pic_cover")) {
											JSONObject js = json
													.getJSONObject("pic_cover");
											page.coverURL = new URL(
													js.getString("source"));
										}

										String az = "";
										JSONArray jj = json
												.optJSONArray("categories");
										for (int j = 0; j < jj.length(); j++) {
											json = jj.getJSONObject(j);
											if (az.length() == 0)
												az += json.getString("name");
											else
												az += ", "
														+ json.getString("name");
										}

										page.category = az;

										az = "";
										az += jsonObject.getString("street");
										if (az.length() > 0)
											az += ", ";
										if (jsonObject.has("city")) {
											az += jsonObject.getString("city");
											if (az.length() > 0) {
												az += ", ";
											}
										}
										if (jsonObject.has("country"))
											az += jsonObject
													.getString("country");

										page.address = az;
										pageCollection.getPageAroundMe().add(
												page);
										i++;

									}

								} catch (Exception e) {
									Log.e("around_me", e.toString());
								}
							}
						};
						Request request = new Request(session, "fql", bundle,
								HttpMethod.GET, callback);
						request.executeAndWait();

					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (tabSelected == 2) {
					if (!aroundSorted) {
						pageCollection.sortSearchByLikesAroundMeActivity();
						aroundSorted = true;
					}
					refreshPagesILikeAdapter();
					listViewPagesILike.setVisibility(View.VISIBLE);
					progressLoginPagesILikeVisible(false);
					if (pageCollection.getPageAroundMe().isEmpty()) {
						noPagesLike
								.setText("Sorry, couldn't find any place nearby your location.");
						noPagesLikeVisible(true);
					} else {
						noPagesLikeVisible(false);

					}

					startService();

					int i = 0;
					if (jarrayAround.length() > 0) {
						getPlacesImages(i);

					}
				}
			}

		};

		task.execute();
	}

	private synchronized void getUserLikesImages(final int i) {
		AsyncTask<Void, Integer, Integer> task = new AsyncTask<Void, Integer, Integer>() {

			@Override
			public Integer doInBackground(Void... params) {
				if (tabSelected == 1
						&& i < pageCollection.getPageSearchList().size()) {
					try {
						int z = 0;
						String index_ID = pageCollection.getPageSearchList()
								.get(i)._ID;
						boolean b = true;
						while (z < jarrayLikes.length() && b) {
							json = jarrayLikes.getJSONObject(z);
							if (index_ID.equals(json.getString("page_id"))) {
								b = false;
							} else {
								z++;
							}

						}
						if (!b) {
							if (readImageFromDisk(json.getString("page_id")) == null) {

								URL img_value = new URL(json.getString("pic"));
								saveImageToDisk(json.getString("page_id"),
										BitmapFactory.decodeStream(img_value
												.openConnection()
												.getInputStream()));
							}
						}

					} catch (Exception e) {
						Log.e("image_userlike", e.toString());
					}
				}
				userLikesInt++;
				return i;
			}

			@Override
			public void onPostExecute(Integer i) {
				if (tabSelected == 1
						&& i < pageCollection.getPageSearchList().size()) {
					{
						if (listViewPagesILike.getFirstVisiblePosition() <= i
								&& i <= listViewPagesILike
										.getLastVisiblePosition()) {
							View v = listViewPagesILike.getChildAt(i
									- listViewPagesILike
											.getFirstVisiblePosition());
							ImageView image = (ImageView) v
									.findViewById(R.id.imageViewPage);
							image.setImageBitmap(readImageFromDisk(pageCollection
									.getPageSearchList().get(i)._ID));
						}
					}
					if (i < pageCollection.getPageSearchList().size()) {
						getUserLikesImages(userLikesInt);
					}
				}
			}
		};
		task.execute();
	}

	private synchronized void getPlacesImages(final int i) {
		AsyncTask<Void, Integer, Integer> task = new AsyncTask<Void, Integer, Integer>() {

			@Override
			public Integer doInBackground(Void... params) {
				if (tabSelected == 2) {
					try {
						Log.i("around_picture", Integer.toString(placesInt));
						int z = 0;
						String index_ID = pageCollection.getPageAroundMe().get(
								i)._ID;
						boolean b = true;
						while (z < jarrayAround.length() && b) {
							json = jarrayAround.getJSONObject(z);
							if (index_ID.equals(json.getString("page_id"))) {
								b = false;
							} else {
								z++;
							}

						}
						if (!b) {
							if (readImageFromDisk(json.getString("page_id")) == null) {
								Log.i("around_picture", "download");

								URL img_value = new URL(json.getString("pic"));
								saveImageToDisk(json.getString("page_id"),
										BitmapFactory.decodeStream(img_value
												.openConnection()
												.getInputStream()));
							}
						}

					} catch (Exception e) {
						Log.e("image_userlike", e.toString());
					}
				}
				placesInt++;
				return i;
			}

			@Override
			public void onPostExecute(Integer i) {
				if (tabSelected == 2) {
					{
						if (listViewPagesILike.getFirstVisiblePosition() <= i
								&& i <= listViewPagesILike
										.getLastVisiblePosition()) {
							View v = listViewPagesILike.getChildAt(i
									- listViewPagesILike
											.getFirstVisiblePosition());
							ImageView image = (ImageView) v
									.findViewById(R.id.imageViewPage);
							if (pageCollection.getPageAroundMe().size() > i) {
								image.setImageBitmap(readImageFromDisk(pageCollection
										.getPageAroundMe().get(i)._ID));
							}
						}
					}
					if (i < pageCollection.getPageAroundMe().size()) {
						getPlacesImages(placesInt);
					}
				}
			}
		};
		task.execute();
	}

	public void read() {

		if (isEventsJustOpened) {
			isEventsJustOpened = false;
			textEventEmpty.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);

			if (eventCollection.getEventList().isEmpty()) {
				listViewMain.setVisibility(View.GONE);
				textEventEmpty.setText("No incoming events");
				textEventEmpty.setVisibility(View.VISIBLE);
			} else {
				refreshEventsAdapter();
				listViewMain.setAdapter(eventArrayAdapter);
			}

			preferences.setModifiedPages(false);
			refreshEventsAdapter();
			if (isComingFromUserLikes) {
				preferences.setModifiedPages(true);
				read();
			}
		} else {
			if (!isReading) {
				if (preferences.getisModifiedPageListToClear()) {
					pageCollection.getModifiedPageList().clear();
					preferences.setisModifiedPageListToClear(false);
				}

				if (!pageCollection.getSelectedPageList().isEmpty()
						|| isComingFromUserLikes) {
					if (!pageCollection.getModifiedPageList().isEmpty()
							|| pageCollection.getPageList().isEmpty()) {

						isReading = true;
						eventCollection.getEventList().clear();
						if (!preferences.getModifiedPages()
								&& !pageCollection.getPageList().isEmpty()) {
							pageCollection.getModifiedPageList().clear();
							if (preferences.getIsSelectedPage()) {
								// fake if
								if (!pageCollection.getSelectedPageList()
										.get(0)._ID.equals("")) {
									for (EventData current : eventCollection
											.getCompleteEventList()) {
										if (current.parentPage_ID
												.equals(pageCollection
														.getSelectedPageList()
														.get(0)._ID)) {
											eventCollection.getEventList().add(
													current);
										}
									}
								} else {
									// dead code
									for (EventData event : eventCollection
											.getCompleteEventList()) {
										if (event.status_attending
												.equals("attending"))
											eventCollection.getEventList().add(
													event);
									}
								}

							} else {
								eventCollection.restoreEventList();
							}

							if (eventCollection.getEventList().isEmpty()) {
								textEventEmpty
										.setText("No incoming events\nfor \""
												+ pageCollection
														.getSelectedPageList()
														.get(0).name + "\"");
								textEventEmpty.setVisibility(View.VISIBLE);
								listViewMain.setVisibility(View.GONE);
							} else {
								textEventEmpty.setVisibility(View.GONE);
								listViewMain.setVisibility(View.VISIBLE);
							}
							refreshEventsAdapter();

						} else {
							if (pageCollection.getPageByID("1") == null
									&& pageCollection.getModifiedPageList()
											.isEmpty()) {
								listViewMain.setVisibility(View.GONE);

								progressDialog = new ProgressDialog(
										FacebookeventsActivity.this);
								progressDialog
										.setProgressStyle(ProgressDialog.STYLE_SPINNER);
								progressDialog.setCancelable(false);
								progressDialog
										.setMessage("Downloading: \"My Events..\"");
								progressDialog.show();
								getMyEvents(false);
							}

							if (!pageCollection.getPreviousPageList().isEmpty()) {

								int f = 0;
								int g = 0;
								ArrayList<String> removeEvent = new ArrayList<String>();
								ArrayList<String> removePage = new ArrayList<String>();
								for (PageData modified : pageCollection
										.getModifiedPageList()) {
									for (PageData page : pageCollection
											.getPreviousPageList()) {
										if (modified._ID.equals(page._ID)) {
											int i = 0;
											for (EventData event : eventCollection
													.getCompleteEventList()) {
												if (event.parentPage_ID
														.equals(modified._ID)) {
													removeEvent.add(Integer
															.toString(i));
												}
												i++;
											}
											for (String s : removeEvent) {
												eventCollection
														.getCompleteEventList()
														.remove(Integer
																.parseInt(s)
																- g);
												g++;
											}
											removePage.add(Integer.toString(f));
										}
									}
									f++;
								}
								if (!removePage.isEmpty()) {
									g = 0;
									for (String s : removePage) {
										pageCollection
												.getModifiedPageList()
												.remove(Integer.parseInt(s) - g);
										g++;
									}
								}
							}
							if (!pageCollection.getModifiedPageList().isEmpty()
									&& isOnline()) {
								listViewMain.setVisibility(View.GONE);

								progressDialog = new ProgressDialog(
										FacebookeventsActivity.this);
								progressDialog
										.setProgressStyle(ProgressDialog.STYLE_SPINNER);
								progressDialog
										.setMessage("Downloading: \"My Events..\"");

								progressDialog.setCancelable(false);
								progressDialog.show();
								progressThread = new ProgressThread(handler);

								preferences.setIsSelectedPage(false);

								progressThread.start();
							} else {
								if (!isOnline()) {
									toast("No internet connection", true);
									for (PageData modified : pageCollection
											.getModifiedPageList()) {
										PageData page = new PageData();
										page._ID = modified._ID;
										pageCollection
												.removePageFromFavourites(page);
									}
								}
								eventCollection.restoreEventList();
								eventCollection.saveToDisk(this);
								refreshEventsAdapter();
								pageCollection.restoreSelectedPageList();
								pageCollection.getModifiedPageList().clear();
								pageCollection.getPreviousPageList().clear();
								refreshPageAdapter();
							}
						}

					}
				} else {
					eventCollection.getEventList().clear();
					eventCollection.getCompleteEventList().clear();
					eventCollection.saveToDisk(this);
					textEventEmpty
							.setText("You have no favourite pages.\nSlide left to add one!");
					textEventEmpty.setVisibility(View.VISIBLE);
					listViewMain.setVisibility(View.GONE);

				}

			}
			isReading = false;
			preferences.setModifiedPages(false);
		}

	}

	private void getMyEvents(boolean update) {

		String s = mPrefs.getString("user_id", null);
		if (s == null) {
			getUserID();
		}
		if (s != null) {
			userID = s;
		}
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
		String a = "SELECT name,attending_count,update_time,venue,host,creator, location,description, pic_big, eid,start_time,end_time FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid ="
				+ userID
				+ myEventsSettings
				+ ") and end_time>"
				+ "'"
				+ current_time + "'";
		bundle.putString("q", a);
		getMyEventsComplete(bundle, update);

	}

	private synchronized void getMyEventsComplete(final Bundle bundle,
			final boolean update) {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {
			boolean newdownloads = false;

			@Override
			public Bitmap doInBackground(Void... params) {

				if (update)
					FacebookeventsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							progressDialog = new ProgressDialog(
									FacebookeventsActivity.this);
							progressDialog
									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							progressDialog.setCancelable(false);
							progressDialog
									.setMessage("Updating, please wait..");

							progressDialog.show();
						}
					});

				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {
						JSONArray jDataArray = new JSONArray();
						JSONObject json = new JSONObject();
						try {
							json = response.getGraphObject()
									.getInnerJSONObject();
							jDataArray = json.getJSONArray("data");
							int counter = 0;
							int progress = 0;
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
										.getCompleteEventList()) {
									if (currentEvent.event_ID.equals(json
											.getString("eid")))
										add = false;
								}

								if (add) {
									counter++;
								}
							}
							final int k = counter;
							if (counter > 0) {
								FacebookeventsActivity.this
										.runOnUiThread(new Runnable() {
											public void run() {
												progressDialog.dismiss();
												progressDialog = new ProgressDialog(
														FacebookeventsActivity.this);
												progressDialog
														.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
												progressDialog
														.setCancelable(false);
												progressDialog
														.setMessage("Downloading: \"My Events..\"");
												progressDialog.setProgress(0);
												progressDialog.setMax(k);
												progressDialog.show();
											}
										});
							}

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
										.getCompleteEventList()) {
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
									event.endMillis = getMillis(
											json.getString("end_time"), event);
									event.parentPage_ID = "1";
									event.parentPageName = "My Events..";
									event.last_update = getMillis(
											json.getString("update_time"),
											event);
									event.attending_count = json
											.getInt("attending_count");
									dayOfWeek(event.startMillis,
											event.endMillis, event.unix);
									event.dateStart = monthNameStart;
									event.dayStart = dayOfWeekStart;
									event.timeStart = timeStart;
									event.dateEnd = monthNameEnd;
									event.dayEnd = dayOfWeekEnd;
									event.timeEnd = timeEnd;
									event.isInProgress = isInProgress;
									isInProgress = false;
									String b = "";
									try {
										if (!json.isNull("venue")) {
											JSONObject jsonO = json
													.getJSONObject("venue");
											if (!jsonO.isNull("street")
													|| !jsonO.isNull("city")) {
												b += jsonO.getString("street");
												if (b.length() > 0)
													b += ", ";
												if (jsonO.has("city")) {
													b += jsonO
															.getString("city");
													if (b.length() > 0) {
														b += ", ";
													}
												}
												if (jsonO.has("country")) {
													b += json
															.getString("country");
												}
											}
										}
									} catch (Exception e) {
										Log.e("my events", "no venue");
									}
									event.venue = b;

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
												// TODO: handle exception
											}
										}
									};
									Request request = new Request(session,
											event.event_ID + "/invited/"
													+ userID, new Bundle(),
											HttpMethod.GET, callback);
									request.executeAndWait();

									eventCollection.getCompleteEventList().add(
											event);
									URL img_value = null;
									img_value = new URL(
											json.getString("pic_big"));
									Bitmap image = BitmapFactory
											.decodeStream(img_value
													.openConnection()
													.getInputStream());
									saveImageToDisk(event.event_ID, image);
									newdownloads = true;
									final int p = progress;
									FacebookeventsActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													progressDialog
															.setProgress(p);
												}
											});
									progress++;

								}
							}
						} catch (Exception e) {
							Log.e("get my events", e.toString());
							if (isOnline()) {
								toast("An error occurred while downloading \"My events\". Try again later.",
										false);
							} else {
								toast("Internet connection lost. Try again later.",
										false);
							}
						}
					}
				};

				Request request = new Request(session, "fql", bundle,
						HttpMethod.GET, callback);
				request.executeAndWait();

				if (newdownloads) {
					newDownloads = true;
					eventCollection.restoreEventList();
					eventCollection.sortByDate();
					eventCollection.saveCompleteEventList();
					// eventCollection.getEventList().clear();//
				}
				pageCollection.saveToDisk(FacebookeventsActivity.this);

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (update)
					updateCompleteEventList();

				else {
					if (!eventCollection.getEventList().isEmpty())
						textEventEmpty.setVisibility(View.GONE);

					listViewMain.setVisibility(View.VISIBLE);
					FacebookeventsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
						}
					});
					eventCollection.saveToDisk(FacebookeventsActivity.this);
					refreshEventsAdapter();
				}
			}

		};
		task.execute();
	}

	private void singlePage(int eventIndex) {
		if (!eventCollection.getEventList().isEmpty() && currentPageID != null) {
			if (!isAdViewVisible) {
				adViewLoad();
			}

			EventData my = eventCollection.getEventList().get(eventIndex);
			name = my.name;
			loc = my.loc;
			desc = my.desc;
			dayEnd = my.dayEnd;
			dateEnd = my.dateEnd;
			timeEnd = my.timeEnd;
			dateStart = my.dateStart;
			attendingCount = Integer.toString(my.attending_count);
			dayStart = my.dayStart;
			timeStart = my.timeStart;
			eventHasAnEnd = my.hasAnEnd;
			hasCover = my.hasCover;

			Bitmap image = readImageFromDisk(my.event_ID);
			mIcon1 = image;
			preferences.setModifiedSinglePage(false);
			status = my.status_attending;

			if (status.equals("Not Invited") || status.equals("not_replied")) {
				rsvp.setTitle("RSVP: " + "Not Answered");
			} else {
				if (status.equals("attending")) {
					rsvp.setTitle("RSVP: " + "Going");
				}
				if (status.equals("unsure")) {
					rsvp.setTitle("RSVP: " + "Maybe");
				}
				if (status.equals("declined")) {
					rsvp.setTitle("RSVP: " + "Declined");
				}
			}
			singlePageQuickLoading();
		}
	}

	private void singlePageQuickLoading() {
		if (currentPageID != null
				&& eventCollection.getCompleteEventByID(currentPageID) != null) {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int wwidth = displaymetrics.widthPixels;

			float q = wwidth;
			int w = (int) (q / 2.7);
			eventPicture.setMaxHeight(w);
			eventPicture.setMinimumHeight(w - 1);

			eventPicture.setImageBitmap(mIcon1);
			textName.setText(name);
			if (desc.length() > 0) {
				textDesc.setText(desc);
			} else {
				textDesc.setText("No description available for this event.");

			}
			textDescYEY.setTextColor(Color.DKGRAY);
			textAttending.setText("Going: " + attendingCount);
			if (dayStart.equals("Today") || dayStart.equals("Tomorrow")) {
				textStart.setText(dayStart + " at " + timeStart);
			} else {
				textStart.setText(dayStart + " " + dateStart + " at "
						+ timeStart);
			}
			if (eventHasAnEnd) {
				if (dayEnd.equals("Tomorrow") || dayEnd.equals("Today")) {
					textEnd.setText(dayEnd + " at " + timeEnd);
				} else {
					textEnd.setText(dayEnd + " " + dateEnd + " at " + timeEnd);
				}
			} else {
				textEnd.setText("End date not set");
			}
			textNamePage.setText(loc);
			if (loc.equals("null")) {
				buttonPlace.setText("N/A");
				buttonPlace.setClickable(false);
			} else {
				buttonPlace.setText("( i ) " + loc);
				buttonPlace.setClickable(true);
			}

			String venue = "";
			if (eventCollection.getCompleteEventByID(currentPageID).venue != null) {
				venue = eventCollection.getCompleteEventByID(currentPageID).venue;
			}
			if (venue.length() == 0
					&& pageCollection.getPageByID(eventCollection
							.getCompleteEventByID(currentPageID).parentPage_ID).address
							.length() == 0) {
				buttonNavigate.setVisibility(View.GONE);
			} else {
				buttonNavigate.setVisibility(View.VISIBLE);
			}
			SpannableString content = new SpannableString(
					pageCollection.getPageByID(eventCollection
							.getCompleteEventByID(currentPageID).parentPage_ID).address);
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			textLoc.setText(content);
			textLoc.setTextColor(Color.rgb(11, 100, 217));
		} else {
			textPageEmpty.setVisibility(View.VISIBLE);
		}
	}

	private void dayOfWeek(String start, String end, boolean unix) {
		int dayOfWeekInteger;
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		if (!unix) {
			startCal = Calendar.getInstance(TimeZone
					.getTimeZone("America/Los_Angeles"));
			endCal = Calendar.getInstance(TimeZone
					.getTimeZone("America/Los_Angeles"));
		}

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
		monthNameStart = "";
		monthNameEnd = "";
		if (startCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)) {
			if (startCal.get(Calendar.DAY_OF_YEAR) == currentCal
					.get(Calendar.DAY_OF_YEAR)) {
				dayOfWeekStart = "Today";
			}

			if (startCal.get(Calendar.DAY_OF_YEAR) == currentCal
					.get(Calendar.DAY_OF_YEAR) + 1) {
				dayOfWeekStart = "Tomorrow";
			}
		}
		if (startCal.get(Calendar.YEAR) <= currentCal.get(Calendar.YEAR)) {
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

			dayOfWeekInteger = endCal.get(Calendar.DAY_OF_WEEK);
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

	// }

	private void toast(final String string, final boolean Long) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				if (!Long)
					Toast.makeText(FacebookeventsActivity.this, string,
							Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(FacebookeventsActivity.this, string,
							Toast.LENGTH_LONG).show();
			}
		});

	}

	@Override
	public void onBackPressed() {
		if (isContentMain) {
			switch (pagerMain.getCurrentItem()) {
			case 0:
				super.onBackPressed();
				break;

			case 1:
				pagerMain.setCurrentItem(0);
				setPageOne();
				break;

			case 2:
				pagerMain.setCurrentItem(1);
				setPageTwo();
				break;
			}

		}

	}

	private class ViewPagerAdapter extends PagerAdapter implements
			TitleProvider {

		private String[] titles = new String[] { "my pages", title2, title3 };
		private final Context context;
		private View v;

		public ViewPagerAdapter(Context context) {
			this.context = context;
		}

		@Override
		public String getTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			if (pageCollection.getPageList().isEmpty()) {
				isPageAvailable = false;
				return (2);
			} else {

				return (3);
			}
		}

		@Override
		public Object instantiateItem(View pager, int position) {

			LayoutInflater inflater = (LayoutInflater) pager.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			v = null;

			switch (position) {

			case 0:
				v = inflater.inflate(R.layout.pages, null);
				ImageView plus = (ImageView) v.findViewById(R.id.imageViewPlus);
				listViewPage = (ListView) v.findViewById(R.id.listViewPages);
				listViewPage.setAdapter(pageArrayAdapter);
				listViewPage
						.setOnItemClickListener(new ListView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> a, View v,
									int i, long l) {
								if (preferences.getIsSelectedPage()
										&& i == selectedPage) {

								} else {
									preferences
											.setisModifiedPageListToClear(false);
									pageCollection.getModifiedPageList()
											.add(pageCollection.getPageList()
													.get(0));
									preferences.setModifiedPages(false);

									preferences.setIsSelectedPage(true);

									pageCollection
											.selectedPageList(pageCollection
													.getPageList().get(i));
									selectedPage = i;

									read();

									toast("\""
											+ pageCollection.getPageList().get(
													i).name + "\""
											+ " selected", false);

									preferences.setModifiedSinglePage(true);
									currentPageID = null;
									refreshPageAdapter();
								}
								filterBar();
								pagerMain.setCurrentItem(1);
							}
						});

				listViewPage
						.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
							public boolean onItemLongClick(
									AdapterView<?> paramAdapterView,
									View paramView, int paramInt, long paramLong) {

								onLongClick(
										pageCollection.getPageList().get(
												paramInt), paramInt);
								return true;
							}
						});

				plus.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (isOnline()) {
							Intent localIntent = new Intent(
									FacebookeventsActivity.this,
									SearchActivity.class);
							localIntent.putExtra("search", "");
							startActivity(localIntent);
						} else {
							toast("No internet connection", false);
						}
					}
				});

				break;

			case 1:
				if (isEventsJustOpened) {
					eventCollection.restoreEventList();
				}

				v = inflater.inflate(R.layout.main_activity, null);
				listViewMain = (ListView) v.findViewById(R.id.listViewMain);
				listViewMain.setAdapter(eventArrayAdapter);

				listViewMain
						.setOnItemClickListener(new ListView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> a, View v,
									int i, long l) {
								if (isOnline()
										|| readImageFromDisk(eventCollection
												.getEventList().get(i).event_ID) != null) {
									preferences.setModifiedSinglePage(true);

									currentPageID = eventCollection
											.getEventList().get(i).event_ID;
									singlePage(i);
									textPageEmpty.setVisibility(View.GONE);
									pagerMain.setCurrentItem(2);
								} else {
									toast("You can't load events that haven't been cached in offline mode!",
											true);
								}

							}
						});

				listViewMain
						.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
							public boolean onItemLongClick(
									AdapterView<?> paramAdapterView,
									View paramView, int paramInt, long paramLong) {

								onLongClickListViewMain(eventCollection
										.getEventList().get(paramInt).event_ID);

								return true;
							}
						});

				textEventEmpty = (TextView) v
						.findViewById(R.id.textViewEventEmpty);

				relativeFilter = (RelativeLayout) v
						.findViewById(R.id.LayoutFilter);
				filterEvents = (TextView) v.findViewById(R.id.spinnerFilter);
				filterPages = (TextView) v.findViewById(R.id.spinnerPages);

				filterEvents.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						spinnerEvent();
					}
				});
				filterPages.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						spinnerPage();
					}
				});

				break;

			case 2:
				v = inflater.inflate(R.layout.single_page_prova, null);

				textDesc = (TextView) v.findViewById(R.id.textViewDescription);
				textDescYEY = (TextView) v.findViewById(R.id.textViewDescYEY);
				textEnd = (TextView) v.findViewById(R.id.textViewEndSinglePage);
				textStart = (TextView) v
						.findViewById(R.id.textViewStartSinglePage);
				textLoc = (TextView) v
						.findViewById(R.id.textViewLocationSinglePage);
				textName = (TextView) v
						.findViewById(R.id.textViewNameSinglePage);
				textNamePage = (TextView) v
						.findViewById(R.id.textViewLocationNamePage);

				textPageEmpty = (TextView) v
						.findViewById(R.id.textViewPageEmpty);

				textAttending = (TextView) v
						.findViewById(R.id.textViewAttendingCount);
				buttonPlace = (Button) v.findViewById(R.id.buttonPlace);
				buttonNavigate = (Button) v.findViewById(R.id.buttonNavigate);

				eventPicture = (ImageView) v
						.findViewById(R.id.imageViewEventCover);

				eventPicture.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						LayoutInflater inflater = getLayoutInflater();
						View dialoglayout = inflater.inflate(
								R.layout.dialog_image, null);

						try {
							java.io.FileInputStream in = FacebookeventsActivity.this.openFileInput(eventCollection
									.getCompleteEventByID(currentPageID).event_ID);
							final Bitmap image = BitmapFactory.decodeStream(in);
							int width = image.getWidth();
							int height = image.getHeight();
							DisplayMetrics displaymetrics = new DisplayMetrics();
							getWindowManager().getDefaultDisplay().getMetrics(
									displaymetrics);
							int wwidth = displaymetrics.widthPixels;
							int hheight = displaymetrics.heightPixels;
							if (width > wwidth || height > hheight) {
								final ImageView imageView = (ImageView) dialoglayout
										.findViewById(R.id.imageView);
								final RelativeLayout rel = (RelativeLayout) dialoglayout
										.findViewById(R.id.rel);
								imageView.setImageBitmap(image);
								AlertDialog.Builder builder = new AlertDialog.Builder(
										FacebookeventsActivity.this);
								builder.setView(dialoglayout);
								builder.show();
							} else {

								final Dialog help = new Dialog(
										FacebookeventsActivity.this);
								help.requestWindowFeature(Window.FEATURE_NO_TITLE);

								WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
								lp.copyFrom(help.getWindow().getAttributes());
								help.setContentView(R.layout.dialog_image);
								final ImageView imageView = (ImageView) help
										.findViewById(R.id.imageView);

								imageView.setImageBitmap(image);
								lp.width = (int) (image.getWidth());
								lp.height = (int) (image.getHeight());
								lp.alpha = 10;
								help.getWindow().setAttributes(lp);
								help.show();

							}

						} catch (Exception e) {
							toast("Can't open the image, an error occurred.",
									false);
						}

					}
				});

				if (currentPageID != null
						&& !eventCollection.getEventList().isEmpty()
						&& eventCollection.getCompleteEventByID(currentPageID) != null) {
					status = eventCollection
							.getCompleteEventByID(currentPageID).status_attending;
					if (eventCollection.getCompleteEventByID(currentPageID).hasCover) {

					}
				}

				buttonNavigate.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder;
						final AlertDialog alertDialog;
						final PageData paramPageData = pageCollection.getPageByID(eventCollection
								.getCompleteEventByID(currentPageID).parentPage_ID);
						String a = "";
						EventData event = eventCollection
								.getCompleteEventByID(currentPageID);
						if (event.venue != null && event.venue.length() > 0) {
							a = event.venue;
						} else {
							a = paramPageData.address;
						}
						final String address = a;

						builder = new AlertDialog.Builder(
								FacebookeventsActivity.this);
						builder.setTitle(event.loc);
						builder.setMessage(address);
						builder.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
						builder.setNeutralButton("See on GMaps",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										try {
											String uri = "geo:0,0?q=" + address;
											startActivity(new Intent(
													android.content.Intent.ACTION_VIEW,
													Uri.parse(uri)));
										} catch (Exception e) {
											Log.e("Gmaps", e.toString());
											toast("Can't open Google Maps, be sure to have installed it on your phone.",
													false);
										}
									}
								});
						builder.setPositiveButton("Navigate",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										try {
											if (address.length() > 0) {
												Intent i = new Intent(
														Intent.ACTION_VIEW,
														Uri.parse("google.navigation:q="
																+ address));
												startActivity(i);
											} else {
												toast("Sorry, no address available for "
														+ paramPageData.name,
														false);
											}
										} catch (Exception e) {
											toast("Can't open navigator app, be sure to have installed it on your phone.",
													true);

										}
									}
								});

						alertDialog = builder.create();
						alertDialog.show();

					}
				});

				buttonPlace.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final EventData event = eventCollection
								.getCompleteEventByID(currentPageID);
						if (event != null && event.parentPage_ID.equals("1")) {

							AlertDialog.Builder builder;
							final AlertDialog alertDialog;
							builder = new AlertDialog.Builder(
									FacebookeventsActivity.this);
							builder.setTitle(event.loc);
							builder.setMessage("You don't have "
									+ "\""
									+ event.loc
									+ "\""
									+ " in your pages.\nWould you like to search for it?");
							builder.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									});

							builder.setPositiveButton("Search",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (isOnline()) {
												Intent localIntent = new Intent(
														FacebookeventsActivity.this,
														SearchActivity.class);
												localIntent.putExtra("search",
														event.loc);
												startActivity(localIntent);

											} else {
												toast("No internet connection",
														false);
											}
										}
									});

							alertDialog = builder.create();
							alertDialog.show();

						} else {
							infoPage(pageCollection
									.getPageByID(event.parentPage_ID));
						}
					}
				});

				isPageAvailable = true;
				adView = (AdView) v.findViewById(R.id.adView35);
				if (isPageJustOpened) {
					textPageEmpty.setVisibility(View.VISIBLE);
					adRequest.addTestDevice("B51A78A2EC2CF273BB3EDAE13C5591AC");
					isPageJustOpened = false;
				}
				int visible = textPageEmpty.getVisibility();
				if (!isBirthdayWeek
						&& !eventCollection.getEventList().isEmpty()
						&& visible != 0 && !preferences.getModifiedSinglePage()) {
					adViewLoad();
				} else {
					isAdViewVisible = false;
				}

				break;

			}
			((ViewPager) pager).addView(v, 0);
			return v;
		}

		@Override
		public void destroyItem(View pager, int position, Object view) {
			((ViewPager) pager).removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			if (isEventsJustOpened) {
				refreshPageAdapter();
				read();
				if (!pageCollection.getPageList().isEmpty()) {

					newFeature();
				}
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putBoolean("newFeature", false);
				editor.commit();

			}
			criticalUpdate();

			if (preferences.getModifiedPages() && !isReading) {
				read();
			}
			refreshPageAdapter();
			if ((eventCollection.getEventList().isEmpty()) && isPageAvailable) {
				textPageEmpty.setVisibility(View.VISIBLE);
			}
			if (!eventCollection.getEventList().isEmpty() && isPageAvailable) {

				if (preferences.getModifiedSinglePage()) {
					textPageEmpty.setVisibility(View.VISIBLE);
				} else {
					if (currentPageID != null
							&& eventCollection
									.getCompleteEventByID(currentPageID) != null) {
						singlePageQuickLoading();
						textPageEmpty.setVisibility(View.GONE);
					} else {
						textPageEmpty.setVisibility(View.VISIBLE);
					}
				}
			}

			if (isComingFromUserLikes) {
				listViewMain.setVisibility(View.VISIBLE);
				listViewPage.setVisibility(View.VISIBLE);
				isComingFromUserLikes = false;
			}
			if ((eventCollection.getEventList().isEmpty() && !preferences
					.getIsSelectedPage())) {
				textEventEmpty.setText("No incoming events");
				textEventEmpty.setVisibility(View.VISIBLE);
			}

		}

		@Override
		public void restoreState(Parcelable p, ClassLoader c) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(ViewGroup container) {

		}

	}

	private void newFeature() {
		if (false) {
			if (mPrefs.getBoolean("newFeature", true)) {
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putBoolean("newFeature", false);
				editor.commit();
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;
				builder = new AlertDialog.Builder(FacebookeventsActivity.this);
				builder.setTitle("Happy to work for you");
				builder.setMessage("Hi!\nIn the last month we've been working a lot on 2NITE as you might have seen. We redesigned the app giving it a slighter and better look. We introduced many new features and finally we're introducing the new activity \"Around Me\" where you'll be albe to discover every event and place around you!\n\nIf you like our job please let us know by giving us a good feedback :)\n\nMosquitoLabs");
				builder.setNeutralButton("Ok",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
				builder.setPositiveButton("Thank you",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								marketIntent();
								dialog.dismiss();
							}
						});
				alertDialog = builder.create();
				alertDialog.show();

			}
		}
	}

	private void criticalUpdate() {
		if (mPrefs.getBoolean("criticalUpdate", false)) {
			int versionCode = mPrefs.getInt("versionCode", 0);
			try {
				PackageInfo pInfo = getPackageManager().getPackageInfo(
						getPackageName(), 0);
				int version = pInfo.versionCode;
				if (version < versionCode) {
					AlertDialog.Builder builder;
					final AlertDialog alertDialog;
					builder = new AlertDialog.Builder(
							FacebookeventsActivity.this);
					builder.setCancelable(false);
					builder.setTitle("Please update");
					builder.setMessage(mPrefs.getString("message",
							"Update to the last version to keep using 2NITE."));
					builder.setPositiveButton("Update",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									marketIntent();
									dialog.dismiss();
								}
							});
					alertDialog = builder.create();
					alertDialog.show();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	}

	public void pageDescription() {
		if (eventCollection.getEventList().isEmpty() && isPageAvailable) {
			textPageEmpty.setVisibility(View.VISIBLE);
		}
		if (!eventCollection.getEventList().isEmpty() && isPageAvailable) {

			if (preferences.getModifiedSinglePage()) {
				textPageEmpty.setVisibility(View.VISIBLE);
			} else {
				singlePageQuickLoading();
				textPageEmpty.setVisibility(View.GONE);
			}
		}
	}

	private synchronized void adViewLoad() {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				FacebookeventsActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						adView.loadAd(adRequest);
						isAdViewVisible = true;
					}
				});
				return null;
			}
		};
		task.execute();

	}

	private void seeEventOnFacebook() {
		try {
			if (isOnline()) {
				String uri = "fb://event/" + currentPageID;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(intent);
				toast("Opening facebook app, please wait..", true);
			} else {
				toast("No internet connection", false);
			}
		} catch (Exception e) {
			toast("Can't open facebook app, be sure to have installed it on your phone.",
					true);
		}
	}

	private void aboutUs() {
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_us, null);

		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setTitle("About 2NITE");
		builder.setNegativeButton("Rate us!", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				marketIntent();
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("More..", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://www.2niteonfacebook.com"));
				startActivity(browserIntent);
				dialog.dismiss();
			}
		});
		alertDialog = builder.create();
		alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_actionbar, menu);
		info_menu = menu.findItem(R.id.menu_info);
		info_2nite = menu.findItem(R.id.menu_info_2nite);
		search = menu.findItem(R.id.menu_search);
		share = menu.findItem(R.id.menu_share);
		refresh = menu.findItem(R.id.menu_refresh);
		seeOnFacebook = menu.findItem(R.id.menu_facebook);
		rsvp = menu.findItem(R.id.menu_rsvp);
		viewall = menu.findItem(R.id.menu_viewall);
		like = menu.findItem(R.id.menu_like);
		reset = menu.findItem(R.id.menu_reset);
		sortEvents = menu.findItem(R.id.menu_sortEvents);
		sortPages = menu.findItem(R.id.menu_sortPages);
		places = menu.findItem(R.id.menu_places);
		calendar = menu.findItem(R.id.menu_calendar);
		MenuItem sort = menu.findItem(R.id.menu_sort);
		MenuItem distance = menu.findItem(R.id.menu_distance);
		sort.setVisible(false);
		distance.setVisible(false);

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("text/plain");
		String shareBody = "www.facebook.com/" + currentPageID
				+ "\n\nSent using 2nite.";
		if (currentPageID != null)
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					eventCollection.getCompleteEventByID(currentPageID).name);

		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		if (!isActionbarAvailable) {
			setSearch(false);
			setLike(false);
			setRSVP(false);
			setViewAll(false);
			setShare(false);
			setSortEvents(false);
			setSortPages(false);
			setRefresh(false);
			setFacebook(false);
			setReset(false);
			setInfo2NITE(false);
			setInfo(false);
			setCalendar(false);
		}
		isActionbarAvailable = true;

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onPrepareOptionsMenu(menu);
	}

	private void marketIntent() {

		// AMAZON MARKET LINK//

		/*
		 * Intent intent = new Intent(Intent.ACTION_VIEW, Uri .parse(
		 * "http://www.amazon.com/gp/mas/dl/android?p=com.mosquitolabs.tonight"
		 * ));
		 */

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri
				.parse("market://details?id=com.mosquitolabs.tonight"));

		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			if (isOnline()) {
				listViewMain.setVisibility(View.GONE);
				getMyEvents(true);
			} else {
				toast("No internet connection", false);
			}
			return false;

		case R.id.menu_search:
			if (isOnline()) {
				Intent localIntent = new Intent(FacebookeventsActivity.this,
						SearchActivity.class);
				localIntent.putExtra("search", "");
				startActivity(localIntent);
			} else {
				toast("No internet connection", false);
			}
			return false;
		case R.id.menu_places:
			if (isOnline()) {
				pageCollection.clearPageAroundMe();
				Intent localIntent = new Intent(FacebookeventsActivity.this,
						AroundMeActivity.class);
				startActivity(localIntent);
			} else {
				toast("No internet connection", false);
			}
			return false;

		case R.id.menu_like:
			if (isOnline()) {
				dialogUserLikes();
			} else {
				toast("No internet connection", false);
			}
			return false;

		case R.id.menu_viewall:
			pageCollection.getModifiedPageList().add(
					pageCollection.getPageList().get(0));
			preferences.setModifiedPages(false);
			preferences.setIsSelectedPage(false);
			pageCollection.restoreSelectedPageList();
			filter = "all";
			read();
			filterBar();
			viewall.setVisible(false);
			return false;

		case R.id.menu_info:

			buttonPlace.performClick();
			return false;

		case R.id.menu_facebook:
			seeEventOnFacebook();
			return false;

		case R.id.menu_rsvp:
			List<String> g = session.getPermissions();
			if (g.contains("rsvp_event")) {
				updateRSVP(currentPageID, false);
			} else {
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;
				builder = new AlertDialog.Builder(FacebookeventsActivity.this);
				builder.setMessage("To edit your RSVP you need to give permission on facebook");
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								session.requestNewPublishPermissions(new NewPermissionsRequest(
										FacebookeventsActivity.this,
										PUBLISH_PERMISSIONS));
							}
						});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
				alertDialog = builder.create();
				alertDialog.show();
			}
			return false;

		case R.id.menu_share:
			if (currentPageID != null) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				shareIntent.setType("text/plain");
				String shareBody = "www.facebook.com/" + currentPageID
						+ "\n\nSent using 2nite.";
				shareIntent
						.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								eventCollection
										.getCompleteEventByID(currentPageID).name);
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						shareBody);
				startActivity(Intent
						.createChooser(
								shareIntent,
								"Share "
										+ "\""
										+ eventCollection
												.getCompleteEventByID(currentPageID).name
										+ "\"" + " using"));
			}
			return false;

		case R.id.menu_info_2nite:
			aboutUs();
			return false;
		case R.id.menu_reset:
			AlertDialog.Builder builder;
			final AlertDialog alertDialog;
			builder = new AlertDialog.Builder(FacebookeventsActivity.this);
			builder.setTitle("Reset");
			builder.setMessage("Ups! If you're redownloading everything it probably means that something went wrong. If that's your case please tell us what happened and will try to fix it as soon as possible!");
			builder.setPositiveButton("Send email",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(Intent.ACTION_VIEW,
									Uri.parse("mailto:"
											+ "mosquitolabs@gmail.com"));
							intent.putExtra(Intent.EXTRA_SUBJECT,
									"2NITE - had to make a complete reset");
							startActivity(intent);
						}
					});
			builder.setNegativeButton("Continue with the reset",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (isOnline()) {
								pageCollection.getModifiedPageList().clear();
								preferences.setModifiedPages(true);
								preferences.setModifiedSinglePage(true);
								currentPageID = null;
								preferences.setIsSelectedPage(false);
								preferences.setisModifiedPageListToClear(false);
								redownloadAll();
							} else {
								toast("No internet connection", false);
								dialog.cancel();
							}
						}
					});
			alertDialog = builder.create();
			alertDialog.show();

			return false;

		case R.id.menu_sortEvents:
			spinnerEvent();
			return false;

		case R.id.menu_sortPages:
			spinnerPage();
			return false;
		case R.id.menu_calendar:
			addToCalendar();
			return false;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	private void addToCalendar() {
		try {
			EventData event = eventCollection
					.getCompleteEventByID(currentPageID);
			long def = 0;

			if (!event.unix) {
				Calendar cal = Calendar.getInstance();
				Calendar cal2 = Calendar.getInstance(TimeZone
						.getTimeZone("America/Los_Angeles"));
				long mi = cal.get(Calendar.ZONE_OFFSET);
				long mu = cal2.get(Calendar.ZONE_OFFSET);
				def = mi - mu;
				if (def < 0)
					def = def * (-1);
			}

			long beginTime = Long.parseLong(event.startMillis + "000") - def;
			long endTime = Long.parseLong(event.endMillis + "000") - def;
			Intent intent = new Intent(Intent.ACTION_EDIT);
			intent.setType("vnd.android.cursor.item/event");
			intent.putExtra("beginTime", beginTime);
			intent.putExtra("endTime", endTime);
			intent.putExtra("title", event.name);
			intent.putExtra("eventLocation", event.loc);
			intent.putExtra("description", event.desc);
			startActivity(intent);
		} catch (Exception e) {
			toast("Can't open Google Calendar, be sure to have installed it on your phone.",
					true);
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (FacebookeventsActivity.this.findViewById(android.R.id.content) != findViewById(R.layout.welcome)) {
			Intent localIntent = new Intent(FacebookeventsActivity.this,
					SearchActivity.class);
			localIntent.putExtra("search", "");
			startActivity(localIntent);
			return false;
		}
		return false;
	}

	public void filter() {
		textEventEmpty.setVisibility(View.GONE);
		listViewMain.setVisibility(View.VISIBLE);
		if (!preferences.getIsSelectedPage()) {
			if (filter.equals("all")) {
				eventCollection.restoreEventList();
				/*
				 * for (EventData event :
				 * eventCollection.getCompleteEventList()) { if
				 * (!event.status_attending.equals("declined"))
				 * eventCollection.addToEventList(event); }
				 */
			}
			if (filter.equals("going")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.status_attending.equals("attending"))
						eventCollection.addToEventList(event);
				}
			}
			if (filter.equals("maybe")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.status_attending.equals("unsure"))
						eventCollection.addToEventList(event);
				}
			}
			if (filter.equals("declined")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.status_attending.equals("declined"))
						eventCollection.addToEventList(event);
				}
			}
			if (filter.equals("not answered")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.status_attending.equals("Not Invited")
							|| event.status_attending.equals("not_replied"))
						eventCollection.addToEventList(event);
				}
			}
		} else {
			if (filter.equals("all")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.parentPage_ID.equals(pageCollection
							.getSelectedPageList().get(0)._ID))
						eventCollection.getEventList().add(event);
				}
			}
			if (filter.equals("going")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.parentPage_ID.equals(pageCollection
							.getSelectedPageList().get(0)._ID)
							&& event.status_attending.equals("attending"))
						eventCollection.getEventList().add(event);
				}
			}

			if (filter.equals("maybe")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.parentPage_ID.equals(pageCollection
							.getSelectedPageList().get(0)._ID)
							&& event.status_attending.equals("unsure"))
						eventCollection.getEventList().add(event);
				}
			}
			if (filter.equals("declined")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.parentPage_ID.equals(pageCollection
							.getSelectedPageList().get(0)._ID)
							&& event.status_attending.equals("declined"))
						eventCollection.getEventList().add(event);
				}
			}
			if (filter.equals("not answered")) {
				eventCollection.getEventList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.parentPage_ID.equals(pageCollection
							.getSelectedPageList().get(0)._ID)
							&& (event.status_attending.equals("Not Invited") || event.status_attending
									.equals("not_replied")))
						eventCollection.getEventList().add(event);
				}
			}
		}
		if (!filter.equals("all")) {
			setViewAll(true);
		} else {
			if (filterPages.getText().toString().equals("All Pages")) {
				setViewAll(false);
			}
		}
		if (filter.equals("declined")) {
			isFromFilter = true;
		}
		refreshEventsAdapter();

	}

	private boolean isOnline() {

		ConnectivityManager cm = (ConnectivityManager) FacebookeventsActivity.this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private void updateRSVP(final String ID, final boolean longclick) {
		if (isOnline()) {
			status = eventCollection.getCompleteEventByID(ID).status_attending;
			if (!status.equals("Not Invited") && !status.equals("not_replied")) {
				final CharSequence[] items = { "Going", "Maybe",
						"Declined (send to trash)" };
				final CharSequence[] element = { "attending", "maybe",
						"declined" };
				int checked = 0;
				if (status.equals("attending"))
					checked = 0;
				if (status.equals("unsure"))
					checked = 1;
				if (status.equals("declined"))
					checked = 2;
				statusMenu(items, element, checked, ID, longclick);
			} else {
				final CharSequence[] items = { "Not Answered", "Going",
						"Maybe", "Declined (send to trash)" };
				final CharSequence[] element = { "empty", "attending", "maybe",
						"declined" };
				statusMenu(items, element, 0, ID, longclick);
			}
		} else
			toast("No internet connection", false);
	}

	private void statusMenu(final CharSequence[] items,
			final CharSequence[] element, int checked, final String ID,
			final boolean longclick) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Change your RSVP status");
		builder.setSingleChoiceItems(items, checked,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						EventData event = eventCollection
								.getCompleteEventByID(ID);
						if (items.length == 4 && item == 1
								&& !event.status_attending.equals("attending")) {

							event.attending_count++;
							if (!longclick) {
								attendingCount = Integer
										.toString(event.attending_count);
								textAttending.setText(attendingCount
										+ " people going!");
							}
						}
						if (event.status_attending.equals("attending")
								&& item != 0) {
							event.attending_count--;
							event.attending_count++;
							if (!longclick) {
								attendingCount = Integer
										.toString(event.attending_count);
								textAttending.setText("Going: "
										+ attendingCount);
							}
						}
						if (items.length == 3 && item == 0
								&& !event.status_attending.equals("attending")) {
							event.attending_count++;
							event.attending_count++;
							if (!longclick) {
								attendingCount = Integer
										.toString(event.attending_count);
								textAttending.setText("Going: "
										+ attendingCount);
							}
						}
						if (items.length != 4) {
							updateButtonStatus(element[item].toString(), ID);
							if (!longclick) {
								if (item == 4) {
									rsvp.setTitle("RSVP: Declined");
								} else {
									rsvp.setTitle("RSVP: "
											+ items[item].toString());
								}
							}
						} else {
							if (item != 0) {
								updateButtonStatus(element[item].toString(), ID);
								if (!longclick) {
									if (item == 3) {
										rsvp.setTitle("RSVP: Declined");
									} else {
										rsvp.setTitle("RSVP: "
												+ items[item].toString());
									}
								}
							}
						}
						refreshEventsAdapter();
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private synchronized void updateButtonStatus(final String element,
			final String ID) {
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			public String doInBackground(Void... params) {
				Bundle bundle = new Bundle();
				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {

						try {
							if (element.equals("maybe")) {
								eventCollection.getCompleteEventByID(ID).status_attending = "unsure";
							} else
								eventCollection.getCompleteEventByID(ID).status_attending = element;
							eventCollection
									.saveToDisk(FacebookeventsActivity.this);
							toast("RSVP status updated", false);
							refreshEventsAdapter();
						} catch (Exception e) {
							Log.e("rsvp", e.toString());
							toast("an error occurred while trying to update your RSVP status",
									true);

						}
					}
				};
				Request request = new Request(session, currentPageID + "/"
						+ element, bundle, HttpMethod.POST, callback);
				request.executeAndWait();

				return null;
			}

		};
		task.execute();
	}

	protected void onLongClick(final PageData paramPageData, final int index) {
		List<String> itemsLongClick;
		if (!paramPageData._ID.equals("1")) {
			itemsLongClick = Arrays.asList("Remove from favourites",
					"Reload this page", "Info");
		} else {
			itemsLongClick = Arrays.asList("Settings", "Reload \"My Events\"",
					"Info");
		}
		CharSequence[] cs = itemsLongClick
				.toArray(new CharSequence[itemsLongClick.size()]);
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder.setTitle(paramPageData.name);
		localBuilder.setItems(cs, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (which == 0) {
					if (paramPageData._ID.equals("1")) {
						final CharSequence[] items = { "Events i'm going to",
								"Events i'm going or might go to",
								"Every events i've been invited to" };
						AlertDialog.Builder builder = new AlertDialog.Builder(
								FacebookeventsActivity.this);

						builder.setTitle("Which events do you want to download?");
						final int checked = mPrefs
								.getInt("myEventsSettings", 2);
						builder.setSingleChoiceItems(items, checked,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										SharedPreferences.Editor editor = mPrefs
												.edit();
										int checked_editor = 2;
										ArrayList<String> remove = new ArrayList<String>();
										int i = 0;
										switch (item) {
										case 0:
											if (checked != 0) {
												myEventsSettings = "and rsvp_status=\"attending\"";
												checked_editor = 0;
												for (EventData event : eventCollection
														.getCompleteEventList()) {
													if (event.parentPage_ID
															.equals("1")
															&& (event.status_attending
																	.equals("declined")
																	|| event.status_attending
																			.equals("Not Invited")
																	|| event.status_attending
																			.equals("not_replied") || event.status_attending
																		.equals("unsure"))) {
														remove.add(Integer
																.toString(i));
													}
													i++;
												}
												int g = 0;
												for (String index : remove) {
													eventCollection
															.getCompleteEventList()
															.remove(Integer
																	.parseInt(index)
																	- g);
													g++;
												}
												eventCollection
														.restoreEventList();
												eventCollection
														.saveToDisk(FacebookeventsActivity.this);
												refreshEventsAdapter();
											}
											break;

										case 1:
											if (checked != 1) {
												myEventsSettings = "and (rsvp_status=\"attending\" or rsvp_status=\"unsure\")";
												if (checked == 0) {
													checked_editor = 1;
													pageCollection
															.getModifiedPageList()
															.clear();
													myEventsSettingsDownload();
												} else {
													checked_editor = 1;

													for (EventData event : eventCollection
															.getCompleteEventList()) {
														if (event.parentPage_ID
																.equals("1")
																&& (event.status_attending
																		.equals("declined")
																		|| event.status_attending
																				.equals("Not Invited") || event.status_attending
																			.equals("not_replied"))) {
															remove.add(Integer
																	.toString(i));
														}
														i++;
													}
													int g = 0;
													for (String index : remove) {
														eventCollection
																.getCompleteEventList()
																.remove(Integer
																		.parseInt(index)
																		- g);
														g++;
													}
													eventCollection
															.restoreEventList();
													eventCollection
															.saveToDisk(FacebookeventsActivity.this);
													refreshEventsAdapter();
												}

											}
											break;

										case 2:
											if (checked != 2) {
												myEventsSettings = "";
												checked_editor = 2;
												pageCollection
														.getModifiedPageList()
														.clear();
												myEventsSettingsDownload();
											}
											break;
										}
										editor.putInt("myEventsSettings",
												checked_editor);
										editor.commit();
										dialog.dismiss();

									}
								});
						AlertDialog alert = builder.create();
						alert.show();

					} else {
						pageCollection.restorePreviousPage();
						if (pageCollection
								.removePageFromFavourites(paramPageData)) {
							toast(paramPageData.name
									+ " removed from favourites", false);
							preferences.setModifiedSinglePage(true);
							currentPageID = null;
							/*
							 * preferences.setModifiedPages(true);
							 * pageCollection.getModifiedPageList().add(
							 * paramPageData);
							 * preferences.setIsSelectedPage(false);
							 */
							viewAll();
							pageCollection
									.saveToDisk(FacebookeventsActivity.this);
							eventCollection
									.saveToDisk(FacebookeventsActivity.this);
							refreshPageAdapter();
							refreshEventsAdapter();
							pageCollection.restoreSelectedPageList();
							if (eventCollection.getCompleteEventList()
									.isEmpty())
								textEventEmpty.setVisibility(View.VISIBLE);

						}
					}
				}
				if (which == 1) {

					if (isOnline()) {
						pageCollection.getModifiedPageList().clear();
						preferences.setModifiedPages(true);
						preferences.setModifiedSinglePage(true);
						currentPageID = null;
						preferences.setIsSelectedPage(false);
						preferences.setisModifiedPageListToClear(false);
						redownloadPageEvents(index);
					} else
						toast("No internet connection", false);
				}

				if (which == 2) {
					if (paramPageData._ID.equals("1")) {
						AlertDialog.Builder builder;
						final AlertDialog alertDialog;
						int checked = mPrefs.getInt("myEventsSettings", 2);
						String sentence = "every event you've been invited to.\n";
						if (checked == 0) {
							sentence = "every event you're attending.\n";
						}
						if (checked == 1) {
							sentence = "every event you're attending or are unsure about.\n";
						}
						builder = new AlertDialog.Builder(
								FacebookeventsActivity.this);
						builder.setTitle("My Events: your personal place!");
						builder.setMessage("\""
								+ paramPageData.name
								+ "\""
								+ " is not a facebook page, it's just a place that contains "
								+ sentence + "See the settings to edit this.");
						builder.setNeutralButton("Ok",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
						builder.setPositiveButton("Settings",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										myEventsSettings(paramPageData);
									}
								});
						alertDialog = builder.create();
						alertDialog.show();
					} else {
						infoPage(paramPageData);
					}
				}
			}
		});

		localBuilder.show();

	}

	private void onLongClickListViewMain(String ID) {
		List<String> g = session.getPermissions();
		if (g.contains("rsvp_event")) {
			updateRSVP(ID, true);
		} else {
			AlertDialog.Builder builder;
			final AlertDialog alertDialog;
			builder = new AlertDialog.Builder(FacebookeventsActivity.this);
			builder.setMessage("To edit your RSVP you need to give permission on facebook");
			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							session.requestNewPublishPermissions(new NewPermissionsRequest(
									FacebookeventsActivity.this,
									PUBLISH_PERMISSIONS));
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			alertDialog = builder.create();
			alertDialog.show();
		}
	}

	private void myEventsSettings(PageData paramPageData) {
		if (paramPageData._ID.equals("1")) {
			final CharSequence[] items = { "Events i'm going to",
					"Events i'm going or might go to",
					"Every events i've been invited to" };
			AlertDialog.Builder builder = new AlertDialog.Builder(
					FacebookeventsActivity.this);

			builder.setTitle("Which events do you want to download?");
			final int checked = mPrefs.getInt("myEventsSettings", 2);
			builder.setSingleChoiceItems(items, checked,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							SharedPreferences.Editor editor = mPrefs.edit();
							int checked_editor = 2;
							ArrayList<String> remove = new ArrayList<String>();
							int i = 0;
							switch (item) {
							case 0:
								if (checked != 0) {
									myEventsSettings = "and rsvp_status=\"attending\"";
									checked_editor = 0;
									for (EventData event : eventCollection
											.getCompleteEventList()) {
										if (event.parentPage_ID.equals("1")
												&& (event.status_attending
														.equals("declined")
														|| event.status_attending
																.equals("Not Invited")
														|| event.status_attending
																.equals("not_replied") || event.status_attending
															.equals("unsure"))) {
											remove.add(Integer.toString(i));
										}
										i++;
									}
									int g = 0;
									for (String index : remove) {
										eventCollection.getCompleteEventList()
												.remove(Integer.parseInt(index)
														- g);
										g++;
									}
									eventCollection.restoreEventList();
									refreshEventsAdapter();
								}
								break;

							case 1:
								if (checked != 1) {
									myEventsSettings = "and (rsvp_status=\"attending\" or rsvp_status=\"unsure\")";
									if (checked == 0) {
										checked_editor = 1;
										pageCollection.getModifiedPageList()
												.clear();
										myEventsSettingsDownload();
									} else {
										checked_editor = 1;

										for (EventData event : eventCollection
												.getCompleteEventList()) {
											if (event.parentPage_ID.equals("1")
													&& (event.status_attending
															.equals("declined")
															|| event.status_attending
																	.equals("Not Invited") || event.status_attending
																.equals("not_replied"))) {
												remove.add(Integer.toString(i));
											}
											i++;
										}
										int g = 0;
										for (String index : remove) {
											eventCollection
													.getCompleteEventList()
													.remove(Integer
															.parseInt(index)
															- g);
											g++;
										}
										eventCollection.restoreEventList();
										refreshEventsAdapter();
									}

								}
								break;

							case 2:
								if (checked != 2) {
									myEventsSettings = "";
									checked_editor = 2;
									pageCollection.getModifiedPageList()
											.clear();
									myEventsSettingsDownload();
								}
								break;
							}
							editor.putInt("myEventsSettings", checked_editor);
							editor.commit();
							dialog.dismiss();

						}
					});
			AlertDialog alert = builder.create();
			alert.show();

		} else {
			pageCollection.restorePreviousPage();
			if (pageCollection.removePageFromFavourites(paramPageData)) {
				toast(paramPageData.name + " removed from favourites", false);
				preferences.setModifiedSinglePage(true);
				currentPageID = null;
				/*
				 * preferences.setModifiedPages(true);
				 * pageCollection.getModifiedPageList().add( paramPageData);
				 * preferences.setIsSelectedPage(false);
				 */
				viewAll();
				pageCollection.saveToDisk(FacebookeventsActivity.this);
				eventCollection.saveToDisk(FacebookeventsActivity.this);
				refreshPageAdapter();
				refreshEventsAdapter();
				pageCollection.restoreSelectedPageList();
				if (eventCollection.getCompleteEventList().isEmpty())
					textEventEmpty.setVisibility(View.VISIBLE);

			}
		}
	}

	private synchronized void myEventsSettingsDownload() {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {
			@Override
			public Bitmap doInBackground(Void... params) {
				FacebookeventsActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						listViewMain.setVisibility(View.GONE);

						progressDialog = new ProgressDialog(
								FacebookeventsActivity.this);
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progressDialog.setCancelable(false);
						progressDialog.setMessage("Updating: \"My Events..\"");

						progressDialog.show();
					}
				});
				myEventsSettingsDownload = true;
				newDownloadEvents();
				pageCollection.getPreviousPageList().clear();
				for (EventData event : eventCollection.getCompleteEventList()) {
					eventCollection.addToEventList(event);
				}
				eventCollection.sortByDate();
				eventCollection.saveCompleteEventList();
				eventCollection.saveToDisk(FacebookeventsActivity.this);
				if (pageCollection.getPageList().size() == pageCollection
						.getSelectedPageList().size()) {
				}
				FacebookeventsActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						textEventEmpty.setVisibility(View.GONE);
						listViewMain.setVisibility(View.VISIBLE);
					}
				});

				if (eventCollection.getEventList().isEmpty()) {
					FacebookeventsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							textEventEmpty.setText("No incoming events");
							textEventEmpty.setVisibility(View.VISIBLE);
							listViewMain.setVisibility(View.GONE);
						}
					});

				}

				preferences.setModifiedPages(false);
				refreshEventsAdapter();
				FacebookeventsActivity.this.runOnUiThread(new Runnable() {
					public void run() {

						listViewMain.setVisibility(View.VISIBLE);
						// eventArrayAdapter.notifyDataSetChanged();
						refreshPageAdapter();
						progressDialog.dismiss();
					}
				});

				preferences.setisModifiedPageListToClear(true);

				FacebookeventsActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						progressDialog.dismiss();
					}
				});
				return mIcon1;
			}
		};
		task.execute();
	}

	private void infoPage(final PageData paramPageData) {
		final Dialog layout = new Dialog(FacebookeventsActivity.this);
		layout.requestWindowFeature(Window.FEATURE_NO_TITLE);
		layout.setContentView(R.layout.page_info_prova);

		TextView textPhone = (TextView) layout.findViewById(R.id.textViewPhone);
		TextView textPhoneTitle = (TextView) layout
				.findViewById(R.id.textViewPhoneTitle);
		TextView textTitle = (TextView) layout.findViewById(R.id.textViewTitle);

		TextView lineBlue1 = (TextView) layout
				.findViewById(R.id.textViewDescriptionBlue1);
		TextView lineBlue2 = (TextView) layout
				.findViewById(R.id.textViewDescriptionBlue2);
		TextView textWebsite = (TextView) layout
				.findViewById(R.id.textViewWebsite);
		TextView textWebsiteTitle = (TextView) layout
				.findViewById(R.id.textViewWebsiteTitle);

		final Button buttonNavigate = (Button) layout
				.findViewById(R.id.buttonNavigate);
		Button buttonSeeOnFacebook = (Button) layout
				.findViewById(R.id.buttonSeeOnFacebookInfo);
		TextView textCategory = (TextView) layout
				.findViewById(R.id.textViewCategory);
		TextView textCategoryTitle = (TextView) layout
				.findViewById(R.id.textViewCategoryTitle);
		TextView textDesc = (TextView) layout
				.findViewById(R.id.textViewDescriptionInfo);
		TextView textDescTitle = (TextView) layout
				.findViewById(R.id.textViewDescriptionInfoTitle);
		ImageView imagePage = (ImageView) layout
				.findViewById(R.id.imageViewPageInfo);
		ImageView coverPage = (ImageView) layout
				.findViewById(R.id.imageViewPageInfoCover);
		ImageView image1 = (ImageView) layout
				.findViewById(R.id.imageViewPageInfoSmall1);
		ImageView image2 = (ImageView) layout
				.findViewById(R.id.imageViewPageInfoSmall2);
		ImageView image3 = (ImageView) layout
				.findViewById(R.id.imageViewPageInfoSmall3);

		image1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(FacebookeventsActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 0);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
				// photoStream(paramPageData._ID,0);
			}
		});
		image2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(FacebookeventsActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 1);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});
		image3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(FacebookeventsActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 2);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});

		downloadInfoPageSmallImages(layout, paramPageData._ID);

		textTitle.setText(paramPageData.name);

		if (paramPageData.phone.length() > 0)
			textPhone.setText(paramPageData.phone);
		else {
			textPhone.setVisibility(View.GONE);
			textPhoneTitle.setVisibility(View.GONE);
		}
		if (paramPageData.category.length() > 0)
			textCategory.setText(paramPageData.category);
		else {
			textCategory.setVisibility(View.GONE);
			textCategoryTitle.setVisibility(View.GONE);
		}
		if (paramPageData.desc.length() > 0)
			textDesc.setText(paramPageData.desc);
		else {
			textDesc.setVisibility(View.GONE);
			textDescTitle.setVisibility(View.GONE);
			lineBlue1.setVisibility(View.GONE);
			lineBlue2.setVisibility(View.GONE);
		}
		if (paramPageData.website.length() > 0) {
			String[] split = paramPageData.website.split("\\s+");
			String web = split[0].replace("http://www", "www");
			textWebsite.setText(web);
		} else {
			textWebsite.setVisibility(View.GONE);
			textWebsiteTitle.setVisibility(View.GONE);
		}

		buttonNavigate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder;
				final AlertDialog alertDialog;
				builder = new AlertDialog.Builder(FacebookeventsActivity.this);
				builder.setTitle(paramPageData.name);
				builder.setMessage(paramPageData.address);
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
				builder.setNeutralButton("See on GMaps",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									String uri = "geo:0,0?q="
											+ paramPageData.address;
									startActivity(new Intent(
											android.content.Intent.ACTION_VIEW,
											Uri.parse(uri)));
								} catch (Exception e) {
									toast("Can't open Google Maps, be sure to have installed it on your phone.",
											false);
								}
							}
						});
				builder.setPositiveButton("Navigate",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									if (paramPageData.address.length() > 0) {
										Intent i = new Intent(
												Intent.ACTION_VIEW,
												Uri.parse("google.navigation:q="
														+ paramPageData.address));
										startActivity(i);
									} else {
										toast("Sorry, no address available for "
												+ paramPageData.name, false);
									}

								} catch (Exception e) {
									toast("Can't open navigator app, be sure to have installed it on your phone.",
											true);
								}
							}
						});

				alertDialog = builder.create();
				alertDialog.show();

			}
		});

		if (paramPageData.address.length() == 0) {
			buttonNavigate.setVisibility(View.GONE);
		}

		buttonSeeOnFacebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (isOnline()) {
						String uri = "fb://page/" + paramPageData._ID;
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(uri));
						startActivity(intent);
						toast("Loading facebook app, please wait..", true);
					} else {
						toast("No internet connection", false);
					}
				} catch (Exception e) {
					toast("Can't open facebook app, be sure to have installed it on your phone.",
							true);
				}

			}
		});
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int wwidth = displaymetrics.widthPixels;
		float dp;
		int widthdp = 100;
		switch (getResources().getDisplayMetrics().densityDpi) {
		case DisplayMetrics.DENSITY_LOW:

			dp = wwidth * (120f / displaymetrics.densityDpi);
			widthdp = (int) (dp / 3);

			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			dp = wwidth * (160f / displaymetrics.densityDpi);
			widthdp = (int) (dp / 3);

			break;
		case DisplayMetrics.DENSITY_HIGH:
			dp = wwidth * (240f / displaymetrics.densityDpi);
			widthdp = (int) (dp / 3);

			break;
		case DisplayMetrics.DENSITY_XHIGH:
			dp = wwidth * (320f / displaymetrics.densityDpi);
			widthdp = (int) (dp / 3);
			break;
		}
		Bitmap icon = readImageFromDisk(paramPageData._ID);
		if (icon != null) {
			imagePage.setAdjustViewBounds(true);
			imagePage.setMaxWidth(widthdp);
			imagePage.setImageBitmap(icon);
		}
		Bitmap cover = readImageFromDisk("cover" + paramPageData._ID);
		if (cover != null) {
			coverPage.setImageBitmap(cover);
		} else {
			if (!paramPageData.coverChecked) {
				downloadCover(layout, paramPageData._ID);
			}
		}

		layout.show();
	}

	private synchronized void downloadCover(final Dialog layout,
			final String page_ID) {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				String a = "SELECT pic_cover FROM page WHERE page_id = "
						+ page_ID;
				Bundle bun = new Bundle();
				bun.putString("q", a);
				final ImageView cover = (ImageView) layout
						.findViewById(R.id.imageViewPageInfoCover);

				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {
						URL img_value = null;
						try {
							JSONObject json = response.getGraphObject()
									.getInnerJSONObject();
							JSONArray jarray = json.getJSONArray("data");
							json = jarray.getJSONObject(0);
							if (!json.isNull("pic_cover")) {
								JSONObject j = json.getJSONObject("pic_cover");
								img_value = new URL(j.getString("source"));
								final Bitmap image = BitmapFactory
										.decodeStream(img_value
												.openConnection()
												.getInputStream());
								FacebookeventsActivity.this
										.runOnUiThread(new Runnable() {
											public void run() {
												cover.setImageBitmap(image);
											}
										});

								saveImageToDisk("cover" + page_ID, image);
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				};
				Request request = new Request(session, "fql", bun,
						HttpMethod.GET, callback);
				request.executeAndWait();

				pageCollection.getPageByID(page_ID).coverChecked = true;

				return null;
			}
		};
		task.execute();
	}

	private synchronized void downloadInfoPageSmallImages(final Dialog layout,
			final String page_ID) {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				String a = "SELECT src_big,src FROM photo WHERE pid IN (SELECT pid FROM photo WHERE aid IN (SELECT aid FROM album WHERE owner='"
						+ page_ID + "' AND type!='profile')) limit 3";
				Bundle bun = new Bundle();
				bun.putString("q", a);
				final ImageView image1 = (ImageView) layout
						.findViewById(R.id.imageViewPageInfoSmall1);
				final ImageView image2 = (ImageView) layout
						.findViewById(R.id.imageViewPageInfoSmall2);
				final ImageView image3 = (ImageView) layout
						.findViewById(R.id.imageViewPageInfoSmall3);
				final TextView text1 = (TextView) layout
						.findViewById(R.id.textViewPhoto1);
				final TextView text2 = (TextView) layout
						.findViewById(R.id.textViewPhoto2);
				final TextView text3 = (TextView) layout
						.findViewById(R.id.textViewPhoto3);

				final ProgressBar progress1 = (ProgressBar) layout
						.findViewById(R.id.progressBar1);
				final ProgressBar progress2 = (ProgressBar) layout
						.findViewById(R.id.progressBar2);
				final ProgressBar progress3 = (ProgressBar) layout
						.findViewById(R.id.progressBar3);

				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {

						try {
							JSONObject json = response.getGraphObject()
									.getInnerJSONObject();
							JSONArray jDataArray = json.getJSONArray("data");
							int i = 0;
							while (i < jDataArray.length()) {
								json = jDataArray.getJSONObject(i);
								URL url = new URL(json.getString("src"));
								InputStream content = (InputStream) url
										.getContent();
								final Drawable d = Drawable.createFromStream(
										content, "src");
								switch (i) {
								case 0:
									FacebookeventsActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image1.setImageDrawable(d);
												}
											});
									break;

								case 1:
									FacebookeventsActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image2.setImageDrawable(d);
												}
											});
									break;

								case 2:
									FacebookeventsActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image3.setImageDrawable(d);
												}
											});
									break;
								}
								i++;
							}
							if (jDataArray.length() < 3) {
								i = 2;
								while (i >= jDataArray.length()) {
									switch (i) {
									case 0:
										FacebookeventsActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text1.setVisibility(View.VISIBLE);
														progress1
																.setVisibility(View.GONE);

													}
												});
										break;

									case 1:
										FacebookeventsActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text2.setVisibility(View.VISIBLE);
														progress2
																.setVisibility(View.GONE);

													}
												});
										break;

									case 2:
										FacebookeventsActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text3.setVisibility(View.VISIBLE);
														progress3
																.setVisibility(View.GONE);

													}
												});
										break;
									}

									i--;
								}
							}

						} catch (Exception e) {
							com.google.analytics.tracking.android.Log.e(e
									.toString());
						}
					}
				};

				Request request = new Request(session, "fql", bun,
						HttpMethod.GET, callback);
				request.executeAndWait();

				return null;
			}
		};
		task.execute();

	}

	public class myCustomAdapterPage extends BaseAdapter {
		private LayoutInflater mInflater;

		public myCustomAdapterPage(Context paramContext) {
			this.mInflater = LayoutInflater.from(paramContext);
		}

		public int getCount() {
			return pageCollection.getPageList().size();
		}

		public Object getItem(int paramInt) {
			return Integer.valueOf(paramInt);
		}

		public long getItemId(int paramInt) {
			return paramInt;
		}

		public View getView(final int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			final ViewHolderStar localViewHolder;

			paramView = mInflater.inflate(R.layout.list_pages_main, null);
			localViewHolder = new ViewHolderStar();
			localViewHolder.text = (TextView) paramView
					.findViewById(R.id.textViewListPages);
			localViewHolder.selected = (TextView) paramView
					.findViewById(R.id.imageViewSelected);
			localViewHolder.image = (ImageView) paramView
					.findViewById(R.id.imageViewPage);
			localViewHolder.image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final PageData paramPageData = pageCollection.getPageList()
							.get(paramInt);
					if (paramInt != 0)
						infoPage(paramPageData);
					else {
						AlertDialog.Builder builder;
						final AlertDialog alertDialog;
						int checked = mPrefs.getInt("myEventsSettings", 2);
						String sentence = "every event you've been invited to.\n";
						if (checked == 0) {
							sentence = "every event you're attending.\n";
						}
						if (checked == 1) {
							sentence = "every event you're attending or are unsure about.\n";
						}
						builder = new AlertDialog.Builder(
								FacebookeventsActivity.this);
						builder.setTitle("My Events: your personal place!");
						builder.setMessage("\""
								+ paramPageData.name
								+ "\""
								+ " is not a facebook page, it's just a place that contains "
								+ sentence + "See settings to edit this.");
						builder.setNeutralButton("Ok",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
						builder.setPositiveButton("Settings",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										myEventsSettings(paramPageData);
									}
								});
						alertDialog = builder.create();
						alertDialog.show();
					}
				}
			});

			localViewHolder.text.setText(pageCollection.getPageList().get(
					paramInt).name);

			if (readImageFromDisk(pageCollection.getPageList().get(paramInt)._ID) != null) {
				localViewHolder.image
						.setImageBitmap(readImageFromDisk(pageCollection
								.getPageList().get(paramInt)._ID));
			}

			paramView.setTag(localViewHolder);

			return paramView;
		}
	}

	static class ViewHolderStar {
		TextView selected;
		ImageView image;
		TextView text;
		boolean selection = false;
	}

	private synchronized void newDownloadEvents() {
		if (!isDoownloading) {
			isDoownloading = true;
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
			String a = "";
			String s = mPrefs.getString("user_id", null);
			if (s == null) {
				getUserID();
			}
			if (s != null) {
				userID = s;
			}

			PageData page = new PageData();
			page._ID = "1";
			page.name = "My Events..";
			page.address = "";
			pageCollection.addPageToFavourites(page);
			a = "SELECT name, update_time,host,creator,location,description,venue,pic_big,pic_cover,eid,start_time,end_time FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid ="
					+ userID
					+ myEventsSettings
					+ " ) and end_time>"
					+ "'"
					+ current_time + "'";
			bundle.putString("q", a);

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					try {
						json = response.getGraphObject().getInnerJSONObject();
						jDataArray = json.getJSONArray("data");
						int counter = 0;
						for (int i = 0; i < jDataArray.length(); i++) {
							json = jDataArray.getJSONObject(i);
							boolean add = true;
							if (!isReloading) {
								for (PageData currentPage : pageCollection
										.getPageList()) {
									if (currentPage._ID.equals(json
											.getString("creator")))
										add = false;
								}
							}

							for (EventData currentEvent : eventCollection
									.getCompleteEventList()) {
								if (currentEvent.event_ID.equals(json
										.getString("eid")))
									add = false;
							}

							if (add) {
								counter++;
							}

						}

						if (counter > 0) {
							final int k = counter;
							FacebookeventsActivity.this
									.runOnUiThread(new Runnable() {
										public void run() {
											progressDialog.dismiss();
											progressDialog = new ProgressDialog(
													FacebookeventsActivity.this);
											progressDialog.setCancelable(false);
											progressDialog
													.setMessage("Downloading: \"My Events..\"");
											progressDialog
													.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
											progressDialog.setProgress(0);
											progressDialog.setMax(k);
											progressDialog.show();
										}
									});
						}

						int progress = 0;

						for (int i = 0; i < jDataArray.length(); i++) {
							json = jDataArray.getJSONObject(i);
							boolean add = true;
							if (!isReloading) {
								for (PageData currentPage : pageCollection
										.getPageList()) {
									if (currentPage._ID.equals(json
											.getString("creator")))
										add = false;
								}
							}

							for (EventData currentEvent : eventCollection
									.getCompleteEventList()) {
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
								event.endMillis = getMillis(
										json.getString("end_time"), event);
								event.parentPage_ID = "1";
								event.parentPageName = "My Events";
								event.last_update = getMillis(
										json.getString("update_time"), event);
								dayOfWeek(event.startMillis, event.endMillis,
										event.unix);
								event.dateStart = monthNameStart;
								event.dayStart = dayOfWeekStart;
								event.timeStart = timeStart;
								event.dateEnd = monthNameEnd;
								event.dayEnd = dayOfWeekEnd;
								event.timeEnd = timeEnd;
								event.isInProgress = isInProgress;

								String b = "";
								try {
									if (!json.isNull("venue")) {
										JSONObject jsonO = json
												.getJSONObject("venue");
										if (!jsonO.isNull("street")
												|| !jsonO.isNull("city")) {
											b += jsonO.getString("street");
											if (b.length() > 0)
												b += ", ";
											if (jsonO.has("city")) {
												b += jsonO.getString("city");
												if (b.length() > 0) {
													b += ", ";
												}
											}
											if (jsonO.has("country")) {
												b += json.getString("country");
											}
										}
									}
								} catch (Exception e) {

								}

								event.venue = b;
								Bundle bundle = new Bundle();

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
												event.status_attending = jsonRSVP
														.getString("rsvp_status");
											}
										} catch (Exception e) {
											// TODO: handle exception
										}
									}
								};

								Request request = new Request(session,
										event.event_ID + "/invited/" + userID,
										bundle, HttpMethod.GET, callback);
								request.executeAndWait();

								isInProgress = false;
								eventCollection.addToCompleteEventList(event);
								URL img_value = null;
								if (!json.isNull("pic_cover")) {
									JSONObject j = json
											.getJSONObject("pic_cover");
									img_value = new URL(j.getString("source"));
									eventCollection
											.getCompleteEventByID(event.event_ID).hasCover = true;

								} else {
									img_value = new URL(
											json.getString("pic_big"));
								}
								Bitmap image = BitmapFactory
										.decodeStream(img_value
												.openConnection()
												.getInputStream());
								saveImageToDisk(event.event_ID, image);

								final int p = progress;
								;
								FacebookeventsActivity.this
										.runOnUiThread(new Runnable() {
											public void run() {

												progressDialog.setProgress(p);
											}
										});

								progress++;

							}

						}
					} catch (Exception e) {
						Log.e("get my events", e.toString());
					}
				}
			};

			Request request = new Request(session, "fql", bundle,
					HttpMethod.GET, callback);
			request.executeAndWait();

			// fine primo callback

			pageCollection.saveToDisk(FacebookeventsActivity.this);

			bundle = new Bundle();
			a = "";
			a += "{";
			for (PageData currentpage : pageCollection.getModifiedPageList()) {
				if (!currentpage._ID.equals("1")) {
					String my = currentpage._ID;
					try {
						Bitmap icon = BitmapFactory
								.decodeStream(currentpage.picURL
										.openConnection().getInputStream());
						Bitmap cover = BitmapFactory
								.decodeStream(currentpage.coverURL
										.openConnection().getInputStream());
						saveImageToDisk(currentpage._ID, icon);
						saveImageToDisk("cover" + currentpage._ID, cover);

					} catch (Exception e) {
						Log.e("image newdownloadevents", e.toString());
					}

					a += "\""
							+ my
							+ "\""
							+ ":\"SELECT eid,creator,name,attending_count,start_time,end_time,venue,description,location,pic_big,pic_cover,update_time FROM event WHERE eid IN (SELECT eid from event_member WHERE uid = "
							+ currentpage._ID + ")" + " AND end_time > " + "'"
							+ current_time + "'" + "\"" + ",";
				}
			}
			a += "}";
			bundle = new Bundle();
			bundle.putString("q", a);
			Request.Callback callback2 = new Request.Callback() {
				public void onCompleted(Response response) {
					try {
						jsonObject = response.getGraphObject()
								.getInnerJSONObject();
						jDataArray = jsonObject.getJSONArray("data");
						int m = 0;
						JSONArray jArray = new JSONArray();
						userID = mPrefs.getString("user_id", null);
						Bundle bundle = new Bundle();
						int h = jDataArray.length();

						while (m < h
								&& pageCollection.getModifiedPageList().size() != 0) {
							int n = 0;
							jsonObject = jDataArray.getJSONObject(m);
							String ID = jsonObject.getString("name");
							name_page = pageCollection.getPageByID(ID).name;
							jArray = jsonObject.getJSONArray("fql_result_set");
							final int lenght = jArray.length();
							final int start = m;
							FacebookeventsActivity.this
									.runOnUiThread(new Runnable() {
										public void run() {
											if (start == 0) {
												progressDialog.dismiss();
												progressDialog = new ProgressDialog(
														FacebookeventsActivity.this);
												progressDialog
														.setCancelable(false);
												progressDialog
														.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
												progressDialog.setProgress(0);
											}

											progressDialog
													.setMessage("Downloading: "
															+ name_page);
											progressDialog.setProgress(0);
											progressDialog.setMax(lenght);
											if (start == 0)
												progressDialog.show();
										}
									});

							while (n < jArray.length()) {
								int p = 0;
								jsonObject = jArray.getJSONObject(n);
								ArrayList<String> remove = new ArrayList<String>();
								for (EventData my : eventCollection
										.getCompleteEventList()) {
									if (my.event_ID.equals(jsonObject
											.getString("eid"))) {
										remove.add(Integer.toString(p));
									}
									p++;
								}
								int g = 0;
								for (String index : remove) {
									eventCollection
											.getCompleteEventList()
											.remove(Integer.parseInt(index) - g);
									g++;
								}
								final EventData event = new EventData();
								event.event_ID = jsonObject.getString("eid");
								event.desc = jsonObject
										.getString("description");
								event.loc = jsonObject.getString("location");
								event.name = jsonObject.getString("name");
								event.startMillis = getMillis(
										jsonObject.getString("start_time"),
										event);
								event.endMillis = getMillis(
										jsonObject.getString("end_time"), event);
								event.attending_count = jsonObject
										.getInt("attending_count");
								event.last_update = getMillis(
										jsonObject.getString("update_time"),
										event);
								dayOfWeek(event.startMillis, event.endMillis,
										event.unix);
								event.dayStart = dayOfWeekStart;
								event.dateStart = monthNameStart;
								event.timeStart = timeStart;
								event.dayEnd = dayOfWeekEnd;
								event.dateEnd = monthNameEnd;
								event.timeEnd = timeEnd;
								event.isInProgress = isInProgress;
								isInProgress = false;
								event.parentPage_ID = ID;
								event.parentPageName = name_page;

								String b = "";
								try {
									if (!json.isNull("venue")) {
										JSONObject jsonO = json
												.getJSONObject("venue");
										if (!jsonO.isNull("street")
												|| !jsonO.isNull("city")) {
											b += jsonO.getString("street");
											if (b.length() > 0)
												b += ", ";
											if (jsonO.has("city")) {
												b += jsonO.getString("city");
												if (b.length() > 0) {
													b += ", ";
												}
											}
											if (jsonO.has("country")) {
												b += json.getString("country");
											}
										}
									}
								} catch (Exception e) {
									// TODO: handle exception
								}
								event.venue = b;

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
												event.status_attending = jsonRSVP
														.getString("rsvp_status");
											}
										} catch (Exception e) {
											// TODO: handle exception
										}
									}
								};

								Request request = new Request(session,
										event.event_ID + "/invited/" + userID,
										bundle, HttpMethod.GET, callback);
								request.executeAndWait();

								eventCollection.addToEventList(event);

								URL img_value = null;
								Bitmap image = null;
								if (!jsonObject.isNull("pic_cover")) {
									JSONObject j = jsonObject
											.getJSONObject("pic_cover");
									img_value = new URL(j.getString("source"));
									eventCollection
											.getEventByID(event.event_ID).hasCover = true;
								} else {
									img_value = new URL(
											jsonObject.getString("pic_big"));
								}
								if (img_value
										.toString()
										.equals("https://fbcdn-profile-a.akamaihd.net/static-ak/rsrc.php/v2/yn/r/5uwzdFmIMKQ.png")) {
									try {
										java.io.FileInputStream in = FacebookeventsActivity.this
												.openFileInput(jsonObject
														.getString("creator"));
										image = BitmapFactory.decodeStream(in);
									} catch (Exception e) {
										image = BitmapFactory
												.decodeStream(img_value
														.openConnection()
														.getInputStream());
									}

								} else {
									image = BitmapFactory
											.decodeStream(img_value
													.openConnection()
													.getInputStream());
								}
								saveImageToDisk(event.event_ID, image);

								n++;
								final int q = n;
								FacebookeventsActivity.this
										.runOnUiThread(new Runnable() {
											public void run() {
												progressDialog.setProgress(q);
											}
										});

							}
							m++;
						}
						isReloading = false;
						isReading = false;

					} catch (Exception e) {
						Log.e("download", e.toString());
						if (!myEventsSettingsDownload) {
							Log.e("new downloads", e.toString());
							FacebookeventsActivity.this
									.runOnUiThread(new Runnable() {
										public void run() {
											if (isOnline()) {

												toast("An error occurred. Please try reloading (long click on the page)",
														true);

											} else {
												toast("Internet connection lost. Please try reloading later.",
														true);
											}
										}
									});
						} else {
							myEventsSettingsDownload = false;
						}
					}
				}
			};
			Request request2 = new Request(session, "fql", bundle,
					HttpMethod.GET, callback2);
			request2.executeAndWait();
			// fine secondo

		}
		isDoownloading = false;
	}

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			progressDialog.setProgress(z);

		}
	};

	/** Nested class that performs progress calculations (counting) */
	private class ProgressThread extends Thread {
		Handler mHandler;
		final static int STATE_DONE = 0;
		final static int STATE_RUNNING = 1;
		int mState;
		int total;

		ProgressThread(Handler h) {
			mHandler = h;
		}

		public void run() {
			mState = STATE_RUNNING;
			total = 0;
			while (mState == STATE_RUNNING) {

				newDownloadEvents();

				if (!isReading) {

					FacebookeventsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							progressDialog = new ProgressDialog(
									FacebookeventsActivity.this);
							progressDialog
									.setProgressStyle(ProgressDialog.STYLE_SPINNER);
							progressDialog.setCancelable(false);
							progressDialog.setMessage("Saving..");
							progressDialog.show();
						}
					});

					pageCollection.getPreviousPageList().clear();
					for (EventData event : eventCollection
							.getCompleteEventList()) {
						eventCollection.addToEventList(event);
					}
					eventCollection.sortByDate();
					eventCollection.saveCompleteEventList();
					eventCollection.saveToDisk(FacebookeventsActivity.this);
					if (pageCollection.getPageList().size() == pageCollection
							.getSelectedPageList().size()) {
					}
					FacebookeventsActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							textEventEmpty.setVisibility(View.GONE);
							listViewMain.setVisibility(View.VISIBLE);
						}
					});

					if (eventCollection.getEventList().isEmpty()) {
						FacebookeventsActivity.this
								.runOnUiThread(new Runnable() {
									public void run() {
										textEventEmpty
												.setText("No incoming events");
										textEventEmpty
												.setVisibility(View.VISIBLE);
										listViewMain.setVisibility(View.GONE);
									}
								});

					}

					preferences.setModifiedPages(false);
					refreshEventsAdapter();
					FacebookeventsActivity.this.runOnUiThread(new Runnable() {
						public void run() {

							listViewMain.setVisibility(View.VISIBLE);
							refreshPageAdapter();
							progressDialog.dismiss();
						}
					});

					mState = STATE_DONE;
					preferences.setisModifiedPageListToClear(true);
					updateAttendingCount();
				}

			}
		}

		public void setState(int state) {
			mState = state;
		}

	}

	private void textEventEmptyVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					textEventEmpty.setVisibility(View.VISIBLE);
				} else {
					textEventEmpty.setVisibility(View.GONE);
				}

			}
		});
	}

	private void textPageEmptyVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					textPageEmpty.setVisibility(View.VISIBLE);
				} else {
					textPageEmpty.setVisibility(View.GONE);
				}

			}
		});
	}

	private void listViewMainVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					listViewMain.setVisibility(View.VISIBLE);
				} else {
					listViewMain.setVisibility(View.GONE);
				}

			}
		});
	}

	private void listViewPageVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					listViewPage.setVisibility(View.VISIBLE);
				} else {
					listViewPage.setVisibility(View.GONE);
				}

			}
		});
	}

	private void listViewPagesILikeVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					listViewPagesILike.setVisibility(View.VISIBLE);
				} else {
					listViewPagesILike.setVisibility(View.GONE);
				}

			}
		});
	}

	private void progressLoginPagesILikeVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					progressLoginPagesILike.setVisibility(View.VISIBLE);
				} else {
					progressLoginPagesILike.setVisibility(View.GONE);
				}

			}
		});
	}

	private void noPagesLikeVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					noPagesLike.setVisibility(View.VISIBLE);
				} else {
					noPagesLike.setVisibility(View.GONE);
				}

			}
		});
	}

	private void relativeFilterVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					relativeFilter.setVisibility(View.VISIBLE);
				} else {
					relativeFilter.setVisibility(View.GONE);
				}

			}
		});
	}

	private void loginVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					login.setVisibility(View.VISIBLE);
				} else {
					login.setVisibility(View.GONE);
				}

			}
		});
	}

	private void progressLoginVisible(final boolean b) {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {

				if (b) {
					progressLogin.setVisibility(View.VISIBLE);
				} else {
					progressLogin.setVisibility(View.GONE);
				}

			}
		});
	}

	private void updateEventDays() {
		eventCollection.cleanCompleteEventList();
		for (EventData event : eventCollection.getCompleteEventList()) {

			dayOfWeek(event.startMillis, event.endMillis, event.unix);
			event.dayStart = dayOfWeekStart;
			event.isInProgress = isInProgress;
			event.dayEnd = dayOfWeekEnd;
			isInProgress = false;
		}
		eventCollection.restoreEventList();
		eventCollection.sortByDate();
		eventCollection.saveCompleteEventList();
		eventCollection.saveToDisk(this);
		cleanImageList(this);
		// singlePageCollection.cleanImageList(this);
	}

	private synchronized void updateCompleteEventList() {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			private JSONArray jArray = new JSONArray();
			ArrayList<String> remove = new ArrayList<String>();

			@Override
			public Bitmap doInBackground(Void... params) {
				Bundle bundle = new Bundle();
				final int i = 0;
				JSONObject myJson = new JSONObject();
				// final JSONArray jArray = new JSONArray();
				if (eventCollection.getCompleteEventList().size() > 0) {
					String a = "SELECT eid,attending_count,venue,start_time,end_time,location,description,name,update_time FROM event WHERE eid=";

					for (EventData event : eventCollection
							.getCompleteEventList()) {
						a += event.event_ID;
						if (!event.event_ID.equals(eventCollection
								.getCompleteEventList().get(
										eventCollection.getCompleteEventList()
												.size() - 1).event_ID))
							a += " or eid=";
					}

					bundle.putString("q", a);
					Request.Callback callback = new Request.Callback() {
						public void onCompleted(Response response) {
							JSONObject myJson = new JSONObject();
							// JSONArray jArray = new JSONArray();
							try {
								myJson = response.getGraphObject()
										.getInnerJSONObject();
								jArray = myJson.getJSONArray("data");

								for (int h = 0; h < jArray.length(); h++) {
									try {
										myJson = jArray.getJSONObject(h);
										EventData event = eventCollection
												.getCompleteEventByID(myJson
														.getString("eid"));
										event.attending_count = myJson
												.getInt("attending_count");
									} catch (Exception e) {
										Log.e("attending_count", e.toString());
									}
								}

								int m = 0;
								ArrayList<String> eventsToDelete = new ArrayList<String>();
								if (eventCollection.getCompleteEventList()
										.size() != jArray.length()) {
									boolean removeEvent;
									int i = 0;
									for (EventData eventToDelete : eventCollection
											.getCompleteEventList()) {
										removeEvent = true;
										m = 0;
										while (m < jArray.length()
												&& removeEvent) {
											myJson = jArray.getJSONObject(m);
											m++;
											if (eventToDelete.event_ID
													.equals(myJson
															.getString("eid")))
												removeEvent = false;
										}
										if (removeEvent) {
											eventsToDelete.add(Integer
													.toString(i));
										}
										i++;
									}
								}
								int g = 0;
								for (String s : eventsToDelete) {
									eventCollection.getCompleteEventList()
											.remove(Integer.parseInt(s) - g);
									g++;
									newDownloads = true;
								}

								m = 0;
								while (m < jArray.length()) {
									myJson = jArray.getJSONObject(m);

									if (!eventCollection.getCompleteEventList()
											.get(m).last_update.equals(myJson
											.getString("update_time"))) {
										remove.add(Integer.toString(m));
									}
									m++;
								}
							} catch (Exception e) {
								Log.e("update", e.toString());
								Log.e("update", response.getGraphObject()
										.toString());
								FacebookeventsActivity.this
										.runOnUiThread(new Runnable() {
											public void run() {
												toast("an error occurred",
														false);
											}
										});
							}
						}
					};

					Request request = new Request(session, "fql", bundle,
							HttpMethod.GET, callback);
					request.executeAndWait();

				}

				int n = 0;
				for (String s : remove) {
					try {
						myJson = jArray.getJSONObject(Integer.parseInt(s));
						EventData event = eventCollection
								.getCompleteEventList()
								.get(Integer.parseInt(s));
						event.desc = myJson.getString("description");
						event.loc = myJson.getString("location");
						event.startMillis = getMillis(
								myJson.getString("start_time"), event);
						event.endMillis = getMillis(
								myJson.getString("end_time"), event);
						event.name = myJson.getString("name");
						event.attending_count = myJson
								.getInt("attending_count");
						event.last_update = getMillis(
								myJson.getString("update_time"), event);
						dayOfWeek(event.startMillis, event.endMillis,
								event.unix);
						event.dateStart = monthNameStart;
						event.dayStart = dayOfWeekStart;
						event.timeStart = timeStart;
						event.dateEnd = monthNameEnd;
						event.dayEnd = dayOfWeekEnd;
						event.timeEnd = timeEnd;
						event.isInProgress = isInProgress;
						isInProgress = false;
						String b = "";
						try {
							if (!json.isNull("venue")) {
								JSONObject jsonO = json.getJSONObject("venue");
								if (!jsonO.isNull("street")
										|| !jsonO.isNull("city")) {
									b += jsonO.getString("street");
									if (b.length() > 0)
										b += ", ";
									if (jsonO.has("city")) {
										b += jsonO.getString("city");
										if (b.length() > 0) {
											b += ", ";
										}
									}
									if (jsonO.has("country")) {
										b += json.getString("country");
									}
								}
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
						event.venue = b;
						n++;
						final int k = n;
						FacebookeventsActivity.this
								.runOnUiThread(new Runnable() {
									public void run() {
										progressDialog.setProgress(k);
									}
								});
					} catch (Exception e) {
						Log.e("update_completeEventList1", e.toString());

						FacebookeventsActivity.this
								.runOnUiThread(new Runnable() {
									public void run() {
										if (isOnline()) {
											toast("An error occurred", false);
										} else {
											toast("Internet connection lost. Try again later.",
													true);
										}
									}
								});
					}
				}

				updateRSVPstatus();

				// see if new events are available

				eventCollection.restoreEventList();
				if (pageCollection.getPageList().size() > 1) {
					bundle = new Bundle();
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
									+ ":\"SELECT eid,name,start_time,end_time,description,venue,location,pic_big,update_time,creator FROM event WHERE eid IN (SELECT eid from event_member WHERE uid = "
									+ page._ID + ") AND end_time > " + "'"
									+ current_time + "'" + "\"";
							if (p != pageCollection.getPageList().size()) {
								az += ",";
							}
							p++;
							pagename.add(page.name);
						}
					}
					az += "}";
					bundle = new Bundle();
					bundle.putString("q", az);

					Request.Callback callback2 = new Request.Callback() {
						public void onCompleted(Response response) {

							try {
								jsonObject = response.getGraphObject()
										.getInnerJSONObject();
								jDataArray = jsonObject.getJSONArray("data");
								int m = 0;
								JSONArray jArray = new JSONArray();
								userID = mPrefs.getString("user_id", null);
								int h = jDataArray.length();
								while (m < h) {
									int n = 0;

									jsonObject = jDataArray.getJSONObject(m);
									jArray = jsonObject
											.getJSONArray("fql_result_set");
									final int z = m;
									FacebookeventsActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {

													progressDialog
															.setMessage("Checking new events for:\n "
																	+ pagename
																			.get(z)
																			.toString());

												}
											});

									while (n < jArray.length()) {
										boolean add = true;
										JSONObject myJson = jArray
												.getJSONObject(n);
										ArrayList<String> removeMyEvent = new ArrayList<String>();
										int p = 0;
										for (EventData my : eventCollection
												.getCompleteEventList()) {
											if (my.parentPage_ID == "1"
													&& my.event_ID
															.equals(myJson
																	.getString("eid"))) {
												removeMyEvent.add(Integer
														.toString(p));
											}
											p++;
										}
										int g = 0;
										for (String index : removeMyEvent) {
											eventCollection
													.getCompleteEventList()
													.remove(Integer
															.parseInt(index)
															- g);
											g++;
										}

										for (EventData event : eventCollection
												.getCompleteEventList()) {
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
											event.endMillis = getMillis(
													myJson.getString("end_time"),
													event);
											event.name = myJson
													.getString("name");
											event.last_update = getMillis(
													myJson.getString("update_time"),
													event);
											URL img_value = new URL(
													myJson.getString("pic_big"));
											Bitmap image = BitmapFactory
													.decodeStream(img_value
															.openConnection()
															.getInputStream());
											saveImageToDisk(event.event_ID,
													image);
											dayOfWeek(event.startMillis,
													event.endMillis, event.unix);
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

											String b = "";
											try {
												if (!json.isNull("venue")) {
													JSONObject jsonO = json
															.getJSONObject("venue");
													if (!jsonO.isNull("street")
															|| !jsonO
																	.isNull("city")) {
														b += jsonO
																.getString("street");
														if (b.length() > 0)
															b += ", ";
														if (jsonO.has("city")) {
															b += jsonO
																	.getString("city");
															if (b.length() > 0) {
																b += ", ";
															}
														}
														if (jsonO
																.has("country")) {
															b += json
																	.getString("country");
														}
													}
												}
											} catch (Exception e) {
												// TODO: handle exception
											}
											event.venue = b;

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
															event.status_attending = jsonRSVP
																	.getString("rsvp_status");
														}
													} catch (Exception e) {
														// TODO: handle
														// exception
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
													.addToCompleteEventList(event);
											newDownloads = true;

										}
										n++;
									}
									m++;
								}
							} catch (Exception e) {
								Log.e("update_completeEventList2", e.toString());
								FacebookeventsActivity.this
										.runOnUiThread(new Runnable() {
											public void run() {
												if (isOnline()) {
													toast("An error occurred",
															false);
												} else {
													toast("Internet connection lost. Try again later.",
															true);
												}
												progressDialog.dismiss();
											}
										});

							}

						}
					};

					Request request2 = new Request(session, "fql", bundle,
							HttpMethod.GET, callback2);
					request2.executeAndWait();

				}
				return mIcon1;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (!remove.isEmpty() || newDownloads) {
					eventCollection.cleanCompleteEventList();
					eventCollection.restoreEventList();
					eventCollection.sortByDate();
					eventCollection.saveCompleteEventList();
					eventCollection.saveToDisk(FacebookeventsActivity.this);
					refreshEventsAdapter();
					newDownloads = false;

				}
				if (!eventCollection.getEventList().isEmpty()) {
					textEventEmpty.setVisibility(View.GONE);
				}

				listViewMain.setVisibility(View.VISIBLE);

				FacebookeventsActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						progressDialog.dismiss();
					}
				});

				refreshEventsAdapter();

			}

		};
		task.execute();
	}

	private synchronized void updateAttendingCount() {

		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

			@Override
			public String doInBackground(Void... params) {
				Bundle bundle = new Bundle();
				String a = "SELECT eid,attending_count,start_time,end_time,location,description,name,update_time FROM event WHERE eid=";
				// JSONObject myJson = new JSONObject();
				// JSONArray jArray = new JSONArray();

				for (EventData event : eventCollection.getCompleteEventList()) {
					a += event.event_ID;
					if (!event.event_ID.equals(eventCollection
							.getCompleteEventList().get(
									eventCollection.getCompleteEventList()
											.size() - 1).event_ID))
						a += " or eid=";
				}

				bundle.putString("q", a);
				// String myResult = mFacebook.request("fql", bundle);
				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {
						JSONObject myJson = new JSONObject();
						JSONArray jArray = new JSONArray();
						try {
							myJson = response.getGraphObject()
									.getInnerJSONObject();
							jArray = myJson.getJSONArray("data");
						} catch (Exception e) {
							if (!eventCollection.getCompleteEventList()
									.isEmpty() && !myJson.isNull("error")) {
								SharedPreferences.Editor editor = mPrefs.edit();
								editor.putBoolean("session_valid", false);
								editor.commit();
								authenticateDialog();
							}
						}
						for (int h = 0; h < jArray.length(); h++) {
							try {
								myJson = jArray.getJSONObject(h);
								EventData event = eventCollection
										.getCompleteEventByID(myJson
												.getString("eid"));
								event.attending_count = myJson
										.getInt("attending_count");
							} catch (Exception e) {
								Log.e("attending_count", e.toString());
							}
						}
					}
				};
				Request request = new Request(session, "fql", bundle,
						HttpMethod.GET, callback);
				request.executeAndWait();

				return null;
			}
		};
		task.execute();
	}

	private synchronized void updateRSVPstatus() {

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

	private void redownloadAll() {
		int index = 1;
		if (pageCollection.getPageList().size() > 1) {
			while (index < pageCollection.getPageList().size()) {
				PageData pageTemp = new PageData();
				pageTemp._ID = pageCollection.getPageList().get(index)._ID;
				pageTemp.name = pageCollection.getPageList().get(index).name;
				pageTemp.desc = pageCollection.getPageList().get(index).desc;
				pageTemp.address = pageCollection.getPageList().get(index).address;
				pageTemp.category = pageCollection.getPageList().get(index).category;
				pageTemp.phone = pageCollection.getPageList().get(index).phone;
				pageTemp.number_of_likes = pageCollection.getPageList().get(
						index).number_of_likes;
				pageTemp.checkins = pageCollection.getPageList().get(index).checkins;
				pageTemp.you_like_it = pageCollection.getPageList().get(index).you_like_it;
				pageTemp.website = pageCollection.getPageList().get(index).website;
				pageTemp.picURL = pageCollection.getPageList().get(index).picURL;

				pageCollection.removePageFromFavourites(pageTemp);
				pageCollection.getModifiedPageList().add(pageTemp);
				pageCollection.addPageToFavourites(pageTemp);
				eventCollection.restoreEventList();
				ArrayList<String> removeEvent = new ArrayList<String>();
				int i = 0;
				for (EventData event : eventCollection.getCompleteEventList()) {
					if (event.parentPage_ID.equals(pageTemp._ID)) {
						removeEvent.add(Integer.toString(i));
					}
					i++;
				}
				int g = 0;
				for (String s : removeEvent) {
					eventCollection.getCompleteEventList().remove(
							Integer.parseInt(s) - g);
					g++;
				}
				index++;
			}
			eventCollection.getCompleteEventList().clear();
			eventCollection.getEventList().clear();
			pageCollection.getPreviousPageList().clear();
			isReloading = true;
			read();
		} else {
			listViewMain.setVisibility(View.GONE);

			progressDialog = new ProgressDialog(FacebookeventsActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.setMessage("Downloading: \"My Events..\"");
			progressDialog.show();
			getMyEvents(false);
		}
	}

	private void redownloadPageEvents(int index) {
		if (index != 0) {
			PageData pageTemp = new PageData();
			pageTemp._ID = pageCollection.getPageList().get(index)._ID;
			pageTemp.name = pageCollection.getPageList().get(index).name;
			pageTemp.desc = pageCollection.getPageList().get(index).desc;
			pageTemp.address = pageCollection.getPageList().get(index).address;
			pageTemp.category = pageCollection.getPageList().get(index).category;
			pageTemp.phone = pageCollection.getPageList().get(index).phone;
			pageTemp.number_of_likes = pageCollection.getPageList().get(index).number_of_likes;
			pageTemp.checkins = pageCollection.getPageList().get(index).checkins;
			pageTemp.you_like_it = pageCollection.getPageList().get(index).you_like_it;
			pageTemp.website = pageCollection.getPageList().get(index).website;
			pageTemp.picURL = pageCollection.getPageList().get(index).picURL;

			pageCollection.removePageFromFavourites(pageTemp);
			pageCollection.restorePreviousPage();
			pageCollection.getModifiedPageList().add(pageTemp);
			pageCollection.addPageToFavourites(pageTemp);
			eventCollection.restoreEventList();
			ArrayList<String> removeEvent = new ArrayList<String>();
			int i = 0;
			for (EventData event : eventCollection.getCompleteEventList()) {
				if (event.parentPage_ID.equals(pageTemp._ID)) {
					removeEvent.add(Integer.toString(i));
				}
				i++;
			}
			int g = 0;
			for (String s : removeEvent) {
				eventCollection.getCompleteEventList().remove(
						Integer.parseInt(s) - g);
				g++;
			}
			isReloading = true;
			read();
		} else {
			listViewMain.setVisibility(View.GONE);
			progressDialog = new ProgressDialog(FacebookeventsActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.setMessage("Updating: \"My Events..\"");
			progressDialog.show();
			getMyEvents(false);
		}

	}

	private void dialogUserLikes() {
		counterStart = Calendar.getInstance().getTimeInMillis();
		isFirstTimeAround = true;
		likesSorted = false;
		aroundSorted = false;
		pageCollection.getPageSearchList().clear();
		pageCollection.getPageSearchListRelevant().clear();
		pageCollection.getPageAroundMe().clear();
		pageCollection.restorePreviousPage();
		pageCollection.getModifiedPageList().clear();
		preferences.setisModifiedPageListToClear(false);
		jarrayAround = new JSONArray();
		jarrayLikes = new JSONArray();
		isReading = true;
		final Dialog layout = new Dialog(FacebookeventsActivity.this);
		layout.requestWindowFeature(Window.FEATURE_NO_TITLE);
		layout.setContentView(R.layout.pages_i_like);
		layout.setCancelable(false);

		LinearLayout saveLike = (LinearLayout) layout
				.findViewById(R.id.linearLayoutPagesILike);
		TextView start = (TextView) layout.findViewById(R.id.buttonPagesILike);
		noPagesLike = (TextView) layout.findViewById(R.id.textViewNoPagesLike);

		saveLike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				layout.dismiss();
				isReading = false;
				if (pageCollection.getModifiedPageList().size() > 0) {
					pageCollection.restoreSelectedPageList();
					pageCollection.saveToDisk(FacebookeventsActivity.this);
					isComingFromUserLikes = true;
					read();
					layout.dismiss();

				} else {
					if (pageCollection.getPageList().isEmpty()) {
						isComingFromUserLikes = true;
						read();
						layout.dismiss();
					}
				}
				pageCollection.getPageAroundMe().clear();
				pageCollection.getPageSearchListRelevant().clear();

			}
		});
		final TextView tab1 = (TextView) layout.findViewById(R.id.tab1);
		final TextView tab2 = (TextView) layout.findViewById(R.id.tab2);
		progressLoginPagesILike = (LinearLayout) layout
				.findViewById(R.id.linearLayoutPagesILikeProgress);

		listViewPagesILike = (ListView) layout
				.findViewById(R.id.listViewPagesILike);
		listViewPagesILike.setAdapter(pagesILikeArrayAdapter);

		listViewPagesILike
				.setOnItemClickListener(new ListView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> a, View v, int i,
							long l) {
						if (tabSelected == 1) {
							pageCollection.addModifiedPage(pageCollection
									.getPageSearchList().get(i));
							if (pageCollection
									.addPageToFavourites(pageCollection
											.getPageSearchList().get(i))) {
								preferences.setModifiedPages(true);

							} else {
								pageCollection
										.removePageFromFavourites(pageCollection
												.getPageSearchList().get(i));
								preferences.setModifiedPages(true);
								preferences.setModifiedSinglePage(true);
							}
						} else {
							pageCollection.addModifiedPage(pageCollection
									.getPageAroundMe().get(i));
							if (pageCollection
									.addPageToFavourites(pageCollection
											.getPageAroundMe().get(i))) {
								preferences.setModifiedPages(true);

							} else {
								pageCollection
										.removePageFromFavourites(pageCollection
												.getPageAroundMe().get(i));
								preferences.setModifiedPages(true);
								preferences.setModifiedSinglePage(true);
							}
						}
						refreshPagesILikeAdapter();
					}
				});

		tab1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tabSelected == 2) {
					tabSelected = 1;
					userLikesInt = 0;
					if (pageCollection.getPageSearchListRelevant().isEmpty()) {
						progressLoginPagesILikeVisible(true);
						noPagesLikeVisible(false);
					}
					getUserLikes(true);
					tab2.setBackgroundResource(R.color.gray);
					tab2.setTextColor(getResources().getColor(
							R.color.android_gray));
					tab1.setBackgroundResource(R.color.orange_title);
					tab1.setTextColor(Color.WHITE);
				}
			}
		});

		tab2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tabSelected == 1) {
					tabSelected = 2;
					placesInt = 0;
					if (isFirstTimeAround) {
						progressLoginPagesILikeVisible(true);
						noPagesLikeVisible(false);
						LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
						LocationListener ll = new mylocationlistener();

						lm.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, 0, 0, ll);
					} else {
						getPlacesAroundMe();
					}
					tab1.setBackgroundResource(R.color.gray);
					tab1.setTextColor(getResources().getColor(
							R.color.android_gray));
					tab2.setBackgroundResource(R.color.orange_title);
					tab2.setTextColor(Color.WHITE);
				}
			}
		});

		layout.show();

		if (pageCollection.getPageList().isEmpty()) {
			AlertDialog.Builder builder;
			final AlertDialog alertDialog;
			builder = new AlertDialog.Builder(FacebookeventsActivity.this);
			builder.setTitle("Welcome!");
			builder.setMessage("Hi! We're selecting for you some clubs, pubs and other places you might like to help you starting with 2NITE.\nClick on any page you like and add it immediatly to your list!\n\nDon't forget to change the tab at the bottom of the screen to see also a list of all the places nearby!\n\nNever miss an event again!");
			builder.setNeutralButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			alertDialog = builder.create();
			alertDialog.show();
		} else {
			start.setText("Save");
		}
		getUserLikes(true);
	}

	public class myCustomAdapterPagesILike extends BaseAdapter {
		private LayoutInflater mInflater;

		public myCustomAdapterPagesILike(Context paramContext) {
			this.mInflater = LayoutInflater.from(paramContext);
		}

		public int getCount() {
			if (tabSelected == 1) {
				return pageCollection.getPageSearchList().size();
			} else {
				return pageCollection.getPageAroundMe().size();
			}
		}

		public Object getItem(int paramInt) {
			return Integer.valueOf(paramInt);
		}

		public long getItemId(int paramInt) {
			return paramInt;
		}

		public View getView(final int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			ViewHolderStar2 localViewHolder;
			PageData temp = new PageData();
			if (tabSelected == 1) {
				temp = (pageCollection.getPageSearchList().get(paramInt));
			}
			if (tabSelected == 2) {
				temp = (pageCollection.getPageAroundMe().get(paramInt));
			}

			final PageData page = temp;
			paramView = mInflater.inflate(R.layout.list_pages, null);
			localViewHolder = new ViewHolderStar2();
			localViewHolder.text = (TextView) paramView
					.findViewById(R.id.textViewListPages);
			localViewHolder.text_fan = (TextView) paramView
					.findViewById(R.id.textViewListPagesFanCount);
			localViewHolder.star = (ImageView) paramView
					.findViewById(R.id.imageViewStar);
			localViewHolder.image = (ImageView) paramView
					.findViewById(R.id.imageViewPage);
			localViewHolder.text.setText(page.name);
			int counter = 0;
			if (tabSelected == 1) {
				counter = userLikesInt;
			} else {
				counter = placesInt;
			}
			if (paramInt <= counter && readImageFromDisk(page._ID) != null) {
				localViewHolder.image
						.setImageBitmap(readImageFromDisk(page._ID));
			}

			localViewHolder.star
					.setBackgroundResource(android.R.drawable.btn_star_big_off);
			for (PageData currentPage : pageCollection.getPageList()) {
				if (currentPage._ID.equals(page._ID)) {
					localViewHolder.star
							.setBackgroundResource(android.R.drawable.btn_star_big_on);
					break;
				}
			}
			localViewHolder.image
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							infoPage(page);
						}
					});

			localViewHolder.text_fan.setText(Integer
					.toString(page.number_of_likes) + " likes");
			paramView.setTag(localViewHolder);
			/*
			 * if (tabSelected == 1 && userLikesInt < jarrayLikes.length() &&
			 * isOneAdapter) { getUserLikesImages(userLikesInt); isOneAdapter =
			 * false; }
			 * 
			 * if (tabSelected == 2 && placesInt < jarrayAround.length() &&
			 * isOneAdapterPlaces) { isOneAdapterPlaces = false;
			 * getPlacesImages(placesInt); }
			 */

			return paramView;
		}
	}

	static class ViewHolderStar2 {
		ImageView star;
		ImageView image;
		TextView text;
		TextView text_fan;
	}

	public String getLoc() {
		return loc;
	}

	public void setPageOne() {
		setInfo2NITE(true);
		setSearch(false);
		setShare(false);
		setInfo(false);
		setRefresh(true);
		setViewAll(false);
		setRSVP(false);
		setLike(true);
		setFacebook(false);
		setPlaces(true);
		viewAll();
	}

	public void setPageTwo() {
		setInfo2NITE(true);
		setSearch(false);
		setPlaces(true);
		setSortEvents(true);
		setSortPages(true);
		setShare(false);
		setInfo(false);
		setRefresh(true);
		setRSVP(false);
		setLike(false);
		setFacebook(false);
	}

	public void setInfo(boolean b) {
		info_menu.setVisible(b);
	}

	public void setShare(boolean b) {
		share.setVisible(b);
	}

	public void setSearch(boolean b) {
		search.setVisible(b);
	}

	public void setInfo2NITE(boolean b) {
		info_2nite.setVisible(b);
	}

	public void setRSVP(boolean b) {
		rsvp.setVisible(b);
	}

	public void setRefresh(boolean b) {
		refresh.setVisible(b);
	}

	public void setViewAll(boolean b) {
		viewall.setVisible(b);
	}

	public void setFacebook(boolean b) {
		seeOnFacebook.setVisible(b);
	}

	public void setLike(boolean b) {
		like.setVisible(b);
	}

	public void setReset(boolean b) {
		reset.setVisible(b);
	}

	public void setSortEvents(boolean b) {
		sortEvents.setVisible(b);
	}

	public void setSortPages(boolean b) {
		sortPages.setVisible(b);
	}

	public void setPlaces(boolean b) {
		places.setVisible(b);
	}

	public void setCalendar(boolean b) {
		calendar.setVisible(b);
	}

	public String getCurrentPageName() {
		if (currentPageID != null) {
			String name = eventCollection.getCompleteEventByID(currentPageID).loc;
			return name;
		}
		return null;
	}

	public void setViewAllName() {
		viewall.setTitle("(click to view all)");
	}

	public void setInfoPageName(String name) {
		if (name != null)
			info_menu.setTitle("About " + name);
		else
			info_menu.setTitle("Info about current page");
	}

	public boolean isPageSelected() {
		return preferences.getIsSelectedPage();
	}

	public boolean isEventPageFilled() {
		if (textPageEmpty.getVisibility() == 0)
			return false;
		else
			return true;
	}

	public void viewAll() {
		if (preferences.getIsSelectedPage()) {
			pageCollection.getModifiedPageList().add(
					pageCollection.getPageList().get(0));
			preferences.setModifiedPages(false);
			preferences.setIsSelectedPage(false);
			pageCollection.restoreSelectedPageList();
			filter = "all";
			read();
			filterBar();
			viewall.setVisible(false);

		}

	}

	public void filterAll() {
		if (!filter.equals("all")) {
			textEventEmpty.setVisibility(View.GONE);
			listViewMain.setVisibility(View.VISIBLE);
			filter = "all";
			eventCollection.restoreEventList();
			refreshEventsAdapter();
		}
	}

	public void setShareIntent() {
		if (currentPageID != null) {

			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			shareIntent.setType("text/plain");
			String shareBody = "www.facebook.com/" + currentPageID
					+ "\n\nSent using 2nite.";
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					eventCollection.getCompleteEventByID(currentPageID).name);

			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		}
	}

	public void spinnerEvent() {
		final CharSequence[] items = { "All", "Going", "Maybe", "Not Answered",
				"Declined (Trash)" };
		AlertDialog.Builder builder2 = new AlertDialog.Builder(
				FacebookeventsActivity.this);

		builder2.setTitle("Filter events:");
		int checked = 0;
		if (filter.equals("all"))
			checked = 0;
		if (filter.equals("going"))
			checked = 1;
		if (filter.equals("maybe"))
			checked = 2;
		if (filter.equals("declined"))
			checked = 4;
		if (filter.equals("not answered"))
			checked = 3;

		builder2.setSingleChoiceItems(items, checked,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
						case 0:
							filter = "all";
							break;

						case 1:
							filter = "going";
							break;

						case 2:
							filter = "maybe";
							break;
						case 4:
							filter = "declined";
							break;
						case 3:
							filter = "not answered";
							break;
						}
						filter();
						filterBar();
						dialog.dismiss();

					}
				});
		AlertDialog alert = builder2.create();
		alert.show();
	}

	public void spinnerPage() {

		final ArrayList<String> pagesName = new ArrayList<String>();
		AlertDialog.Builder builder2 = new AlertDialog.Builder(
				FacebookeventsActivity.this);

		builder2.setTitle("Filter Pages:");
		int i = 1;
		int checked = 0;
		pagesName.add("All");
		for (PageData page : pageCollection.getPageList()) {
			pagesName.add(page.name);
			if (preferences.getIsSelectedPage()
					&& page._ID.equals(pageCollection.getSelectedPageList()
							.get(0)._ID)) {
				checked = i;
			}
			i++;
		}
		final CharSequence[] items = pagesName
				.toArray(new CharSequence[pagesName.size()]);

		builder2.setSingleChoiceItems(items, checked,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						if (item != 0) {
							if (preferences.getIsSelectedPage()
									&& item - 1 == selectedPage) {

							} else {
								preferences.setisModifiedPageListToClear(false);
								pageCollection.getModifiedPageList().add(
										pageCollection.getPageList().get(0));
								preferences.setModifiedPages(false);

								preferences.setIsSelectedPage(true);

								pageCollection.selectedPageList(pageCollection
										.getPageList().get(item - 1));
								selectedPage = item - 1;

								read();

								toast("\""
										+ pageCollection.getPageList().get(
												item - 1).name + "\""
										+ " selected", false);

								preferences.setModifiedSinglePage(true);
								currentPageID = null;
								refreshPageAdapter();
								filter();
								setViewAll(true);
							}
						} else {
							if (!filter.equals("all")) {
								preferences.setIsSelectedPage(false);
								pageCollection.getSelectedPageList().clear();
								eventCollection.restoreEventList();
								filterPages.setText("All Pages");
								filter();
								refreshEventsAdapter();
							} else {
								viewAll();
							}
						}
						filterBar();
						dialog.dismiss();

					}
				});
		AlertDialog alert = builder2.create();
		alert.show();
	}

	public boolean isActionbarAvailable() {
		return isActionbarAvailable;
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
			e.printStackTrace();
			return null;
		}
	}

	private void refreshEventsAdapter() {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				if (!isFromFilter) {
					eventCollection.cleanEventListDeclined();
				} else {
					isFromFilter = false;
				}
				eventArrayAdapter.notifyDataSetChanged();
			}

		});
	}

	public void filterBar() {
		if (eventCollection.getEventList().isEmpty()
				&& eventCollection.getCompleteEventList().size() != 0) {
			relativeFilter.setVisibility(View.VISIBLE);
			if (preferences.getIsSelectedPage()) {
				filterPages
						.setText(pageCollection.getSelectedPageList().get(0).name);
			} else {
				filterPages.setText("All Pages");
			}
			if (filter.equals("all"))
				filterEvents.setText("All Events");
			if (filter.equals("going"))
				filterEvents.setText("Going");
			if (filter.equals("maybe"))
				filterEvents.setText("Maybe");
			if (filter.equals("declined"))
				filterEvents.setText("Declined (Trash)");
			if (filter.equals("not answered"))
				filterEvents.setText("Not Answered");
			textEventEmpty.setVisibility(View.VISIBLE);
		} else {
			relativeFilter.setVisibility(View.GONE);
			if (eventCollection.getCompleteEventList().size() != 0) {
				textEventEmpty.setVisibility(View.GONE);
			} else {
				textEventEmpty.setVisibility(View.VISIBLE);
			}
		}
	}

	private void refreshPagesILikeAdapter() {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				pagesILikeArrayAdapter.notifyDataSetChanged();
			}
		});
	}

	private void refreshPageAdapter() {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				pageArrayAdapter.notifyDataSetChanged();
			}
		});
	}

	private class mylocationlistener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			if (isFirstTimeAround) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
			getPlacesAroundMe();

			isFirstTimeAround = false;
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	private void authenticateDialog() {
		FacebookeventsActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;
				builder = new AlertDialog.Builder(FacebookeventsActivity.this);
				builder.setMessage("Your session expired or is no longer valid. Please sign in again.");
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// facebook_auth();
							}
						});
				builder.setCancelable(false);
				alertDialog = builder.create();
				alertDialog.show();
			}
		});

	}

	private void dialogFeedback() {
		AlertDialog.Builder builder;
		final AlertDialog alertDialog;
		final SharedPreferences.Editor editor = mPrefs.edit();

		builder = new AlertDialog.Builder(FacebookeventsActivity.this);
		builder.setTitle("Help us grow");
		builder.setMessage("If you like using 2NITE please leave a good feedback on Google Play and help us grow.\nThank you!");
		builder.setNegativeButton("Never",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						editor.putBoolean("entrateDone", true);
						editor.commit();
						dialog.cancel();
					}
				});
		builder.setNeutralButton("Later",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// later//
					}
				});
		builder.setPositiveButton("Rate us!",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						editor.putBoolean("entrateDone", true);
						editor.commit();
						marketIntent();
					}
				});

		alertDialog = builder.create();
		alertDialog.show();

	}

	private boolean cleanImageList(Context paramActivity) {

		ArrayList<String> paths = new ArrayList<String>();
		File directory = paramActivity.getFilesDir();
		try {
			File[] files = directory.listFiles();

			for (int i = 0; i < files.length; ++i) {
				paths.add(files[i].getName());
			}

			for (String file : paths) {
				boolean remove = true;
				for (EventData event : eventCollection.getCompleteEventList()) {

					if (file.equals(event.event_ID)) {
						remove = false;
					}
				}
				if (remove) {
					for (PageData page : pageCollection.getPageList()) {
						if (page._ID.equals(file)
								|| file.equals("cover" + page._ID)) {
							remove = false;
						}
					}
					if (remove && !file.equals("pages.data")
							&& !file.equals("events.data")) {
						File f = new File(directory.getAbsolutePath() + "/"
								+ file);
						f.delete();
					}
				}
			}
		} catch (Exception e) {
			Log.e("clean", e.toString());
			return false;
		}
		if (paths.size() > 0)
			return true;
		else
			return false;
	}

	public SharedPreferences mSharedPreferences;

	// Listener defined by anonymous inner class.
	public OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key.equals("service_updated")
					&& mPrefs.getBoolean("service_updated", true)) {
				filter();
				// refreshEventsAdapter();
			}
			Log.d("debug", "\"" + key + "\"" + " preference has been changed");
		}
	};

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (session.isClosed()) {
			session.getActiveSession();
		}

	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session.isOpened() && permissionAsked && false) {

				facebook_auth_complete();

			}
			if (session.isClosed()) {
				session = session.getActiveSession();
			}

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
		request.executeAsync();

	}

	private String getMillis(String UNIX, EventData event) {
		if (UNIX.length() == 10) {
			event.unix = true;
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

}