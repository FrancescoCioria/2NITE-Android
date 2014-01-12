package com.mosquitolabs.tonight;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.AccessToken;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.analytics.tracking.android.EasyTracker;

public class DiscoverActivity extends SherlockActivity {

	final String APP_ID = "219909391458551";
	private EventCollection eventCollection = EventCollection.getInstance();
//	private JSONObject json = new JSONObject();
	private JSONArray jarrayPlaces;
	private JSONArray jarrayLikes;
	private JSONArray jarrayEventAround;
	private JSONArray jarrayEventInteresting;
	// private ListView listViewAroundMe;
	private double latitude;
	private double longitude;

	private PageCollection pageCollection = PageCollection.getInstance();

	private SharedPreferences mPrefs;
	private Preferences preferences = Preferences.getInstance();
	private boolean firstLocation = true;
	private boolean placesSorted = false;
	private com.actionbarsherlock.app.ActionBar actionbar;
	private ViewPager viewPager;

	private DiscoverPagerAdapter adapter;

	private String timeStart;
	private String timeEnd;
	private String dayOfWeekStart;
	private String dayOfWeekEnd;
	private String monthNameStart = "";
	private String monthNameEnd = "";
	private boolean isInProgress = false;
	private boolean isDiscover = true;
	private boolean isError = false;

	private RelativeLayout dialog;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private Session session;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		this.setTheme(R.style.MainTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discover);
		EasyTracker.getTracker().sendView();

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		pageCollection.clearPageAroundMe();
		eventCollection.getAroundMeEventList().clear();
		pageCollection.getPageSearchList().clear();
		pageCollection.getPageSearchListRelevant().clear();
		pageCollection.restorePreviousPage();

		actionbar = getSupportActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		Drawable background = getResources().getDrawable(
				R.drawable.darkstripes_action);
		actionbar.setBackgroundDrawable(background);

		adapter = new DiscoverPagerAdapter(DiscoverActivity.this);
		viewPager = (ViewPager) findViewById(R.id.viewpager);

		viewPager.setPageMargin(15);
		TitlePageIndicatorDiscover indicator = (TitlePageIndicatorDiscover) findViewById(R.id.indicator);
		indicator.setBackgroundColor(Color.rgb(251, 148, 11));
		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.stripe_orange);
		BitmapDrawable background2 = new BitmapDrawable(bmp);
		background2.setTileModeXY(Shader.TileMode.REPEAT,
				Shader.TileMode.REPEAT);
		indicator.setBackgroundDrawable(background2);
		viewPager.setAdapter(adapter);
		indicator.setViewPager(viewPager, this);

		String access_token = mPrefs.getString("access_token", null);

		session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}

			if (access_token != null) {

				SharedPreferences.Editor editor = mPrefs.edit();
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

		preferences.setisModifiedPageListToClear(false);

		dialog = (RelativeLayout) findViewById(R.id.progressDiscover);
		visibilityDialog(true);

	}

	public void getLocation() {
		Log.i("location", "getting location");
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener ll = new mylocationlistener();
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000,
				1000, ll);
	}

	@Override
	protected void onResume() {
		// pageCollection.readFromDisk(this);
		adapter.refreshPageAdapter();
		adapter.refreshEventsAround();
		adapter.refreshEventsAround();

		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void toast(final String paramString) {
		DiscoverActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(DiscoverActivity.this, paramString,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void infoPage(final PageData paramPageData) {
		Dialog dialog;
		if (Build.VERSION.SDK_INT >= 19) {
			dialog = new Dialog(this, R.style.DialogLightButtons);
		} else {
			dialog = new Dialog(this);
		}
		final Dialog layout = dialog;
		layout.requestWindowFeature(Window.FEATURE_NO_TITLE);
		layout.setContentView(R.layout.page_info);

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

		textTitle.setText(paramPageData.name);

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
				Intent localIntent = new Intent(DiscoverActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 0);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});
		image2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(DiscoverActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 1);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});
		image3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(DiscoverActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 2);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});

		downloadInfoPageSmallImages(layout, paramPageData._ID);

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

		final String address = paramPageData.address;

		buttonNavigate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder;
				final AlertDialog alertDialog;
				builder = new AlertDialog.Builder(DiscoverActivity.this);
				builder.setTitle(paramPageData.name);
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
									toast("Can't open Google Maps, be sure to have installed it on your phone.");
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
												+ paramPageData.name);
									}

								} catch (Exception e) {
									toast("Can't open navigator app, be sure to have installed it on your phone.");
								}
							}
						});

				alertDialog = builder.create();
				alertDialog.show();

			}
		});

		if (address.length() == 0) {
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
						toast("Loading facebook app, please wait..");
					} else {
						toast("No internet connection");
					}
				} catch (Exception e) {
					toast("Can't open facebook app, be sure to have installed it on your phone.");
				}

			}
		});

		Bitmap icon = readImageFromDisk(paramPageData._ID);
		if (icon != null) {
			imagePage.setAdjustViewBounds(true);
			imagePage.setImageBitmap(icon);
		}
		Bitmap cover = readImageFromDisk("cover" + paramPageData._ID);
		if (cover != null) {
			coverPage.setImageBitmap(cover);
		}

		layout.show();
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
							// String string = mFacebook.request("fql", bun);
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
									DiscoverActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image1.setImageDrawable(d);
												}
											});
									break;

								case 1:
									DiscoverActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image2.setImageDrawable(d);
												}
											});
									break;

								case 2:
									DiscoverActivity.this
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
										DiscoverActivity.this
												.runOnUiThread(new Runnable() {

													public void run() {
														text1.setVisibility(View.VISIBLE);
														progress1
																.setVisibility(View.GONE);

													}

												});
										break;

									case 1:
										DiscoverActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text2.setVisibility(View.VISIBLE);
														progress2
																.setVisibility(View.GONE);

													}
												});
										break;

									case 2:
										DiscoverActivity.this
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
				Request request = new Request(Session.getActiveSession(),
						"fql", bun, HttpMethod.GET, callback);
				request.executeAndWait();

				return null;
			}
		};
		task.execute();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);

	}

	@Override
	public void onBackPressed() {

		if (!pageCollection.getModifiedPageList().isEmpty()) {
			pageCollection.restoreSelectedPageList();
			preferences.setisModifiedPageListToClear(false);
		}
		pageCollection.saveToDisk(this);
		eventCollection.restoreEventList();
		isDiscover = false;
		super.onBackPressed();

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_discover, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		case R.id.menu_info:
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("We do the hard work for you!");
			dialog.setMessage("With DISCOVER it's so easy to always be aware of all that's happening around you!\nDISCOVER gets:\n\n -Events starting within 3 days close to your location or for which you or your friends have been invited.\n\n -Places nearby you.\n\n -Places and pages of pubs, discos, cinemas ... (or that have created at least an event in the last month) that you liked on Facebook.");
			dialog.create().show();
			return false;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean isOnline() {

		ConnectivityManager cm = (ConnectivityManager) DiscoverActivity.this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public void saveImageToDisk(String ID, Bitmap image) {
		try {
			String path = new String(ID);
			java.io.FileOutputStream out = this.openFileOutput(path,
					Context.MODE_PRIVATE);
			image.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Bitmap readImageFromDisk(String ID) {
		try {
			java.io.FileInputStream in = this.openFileInput(ID);
			Bitmap image = BitmapFactory.decodeStream(in);
			return image;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void dayOfWeek(String start, String end) {
		int dayOfWeekInteger;
		// TimeZone.getTimeZone("America/Los_Angeles")
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
		form.close();
		form = new Formatter();
		form.format("%02d", endCal.get(Calendar.MINUTE));
		timeEnd = Integer.toString(endCal.get(Calendar.HOUR_OF_DAY)) + ":"
				+ form.toString();
		form.close();
		dayOfWeekStart = "vuoto";
		dayOfWeekEnd = "vuoto";
		monthNameStart = "";
		monthNameEnd = "";
		if (startCal.get(Calendar.DAY_OF_YEAR) == currentCal
				.get(Calendar.DAY_OF_YEAR)) {
			dayOfWeekStart = "Today";
		}

		if (startCal.get(Calendar.DAY_OF_YEAR) == currentCal
				.get(Calendar.DAY_OF_YEAR) + 1) {
			dayOfWeekStart = "Tomorrow";
		}

		if (startCal.get(Calendar.YEAR) < currentCal.get(Calendar.YEAR)
				|| startCal.get(Calendar.DAY_OF_YEAR) < currentCal
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
				dayOfWeekStart = "Thursday";
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
				dayOfWeekEnd = "Thursday";
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

	private class mylocationlistener implements LocationListener {

		@Override
		public synchronized void onLocationChanged(Location location) {

			if (firstLocation) {
				firstLocation = false;
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				pageCollection.getPageAroundMe().clear();
				eventCollection.getAroundMeEventList().clear();
				visibilityDialog(true);

				discover();

			}

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

	public synchronized void discover() {
		AsyncTask<Void, Integer, Bitmap> task = new AsyncTask<Void, Integer, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {

				String current_time = Long.toString(Calendar.getInstance()
						.getTimeInMillis());
				long Hours24 = 86400000;
				String hours24FromNow = Long.toString(Calendar.getInstance()
						.getTimeInMillis() + Hours24);
				current_time = current_time.substring(0, 10);
				hours24FromNow = hours24FromNow.substring(0, 10);
				String distance = Integer
						.toString(mPrefs.getInt("distance", 5) * 1000);
				String limit = "500";
				if (mPrefs.getInt("distance", 5) > 25)
					limit = "1000";

				// BUNDLE

				Bundle bundleEventAround = new Bundle();
				Bundle bundleEventInteresting = new Bundle();
				Bundle bundlePlacesAround = new Bundle();
				Bundle bundlePagesLike = new Bundle();

				String stringPlacesAround = "Select page_id,name,location,fan_count,checkins,phone,description,pic_square,pic,pic_large,categories from page where page_id in (select page_id from place where distance(latitude, longitude,"
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
				bundlePlacesAround.putString("q", stringPlacesAround);

				String stringPagesLike = "SELECT pic_square,pic,pic_large,pic_cover,type,checkins,location,phone,fan_count,categories,description,name,page_id FROM page WHERE page_id IN ( SELECT page_id FROM page_fan WHERE uid=me())";
				bundlePagesLike.putString("q", stringPagesLike);

				String stringEventAround = "Select eid,name,attending_count,venue,creator,description,location,start_time,end_time,pic_big from event where eid in (SELECT eid from event_member WHERE uid in (Select page_id,name from place where distance(latitude, longitude,"
						+ "\""
						+ Double.toString(latitude)
						+ "\""
						+ ","
						+ "\""
						+ Double.toString(longitude)
						+ "\""
						+ ")<"
						+ distance
						+ " and checkin_count>0"
						+ " limit "
						+ limit
						+ "))"
						+ " and end_time>"
						+ current_time
						+ " and start_time<"
						+ Integer
								.toString(Integer.parseInt(current_time) + 259200);
				bundleEventAround.putString("q", stringEventAround);

				String stringEventInteresting = "SELECT eid,name,attending_count,venue,creator,description,location,start_time,end_time,pic_big FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) or uid = me() ) AND end_time > "
						+ current_time
						+ "  AND start_time< "
						+ Integer
								.toString(Integer.parseInt(current_time) + 259200)
						+ " AND attending_count>0";
				bundleEventInteresting.putString("q", stringEventInteresting);

				// REQUESTS

				Request requestPlacesAround = new Request(
						Session.getActiveSession(), "fql", bundlePlacesAround,
						HttpMethod.GET, new Request.Callback() {
							public void onCompleted(Response response) {
								try {
									JSONObject json = response.getGraphObject()
											.getInnerJSONObject();
									jarrayPlaces = json.getJSONArray("data");
									int i = 0;
									while (i < jarrayPlaces.length()) {
										json = jarrayPlaces.getJSONObject(i);

										PageData page = new PageData();
										JSONObject jsonObject = json
												.getJSONObject("location");
										page._ID = json.getString("page_id");
										page.name = json.getString("name");
										page.number_of_likes = json
												.getInt("fan_count");
										page.checkins = json.getInt("checkins");
										page.phone = json.getString("phone");
										page.desc = json
												.getString("description");
										page.picURL = new URL(json
												.getString("pic_large"));

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
										boolean add = true;
										for (PageData temp : pageCollection
												.getPageAroundMe()) {
											if (temp._ID.equals(page._ID)) {
												add = false;
												break;
											}
										}
										if (add) {
											pageCollection.getPageAroundMe()
													.add(page);
											Log.i("places", "added place");
										} else {
											Log.i("places",
													"already there. not added");
										}

										i++;

									}

								} catch (Exception e) {
									if (isOnline()) {
										toast("Server timed out, please retry later.");
									} else {
										toast("Internet connection lost.");
									}
									Log.e("getplaces", e.toString());
								}
							}
						});

				Request requestEventInteresting = new Request(
						Session.getActiveSession(), "fql",
						bundleEventInteresting, HttpMethod.GET,
						new Request.Callback() {

							@Override
							public void onCompleted(Response response) {

								int z = 0;
								try {
									JSONObject jsonAround = response
											.getGraphObject()
											.getInnerJSONObject();

									// json array separato = immagini non
									// caricate !? sistemare

									jarrayEventInteresting = jsonAround
											.getJSONArray("data");

									for (z = 0; z < jarrayEventInteresting
											.length(); z++) {
										jsonAround = jarrayEventInteresting
												.getJSONObject(z);
										final EventData event = new EventData();
										event.name = jsonAround
												.getString("name");
										event.desc = jsonAround
												.getString("description");
										event.event_ID = jsonAround
												.getString("eid");
										event.loc = jsonAround
												.getString("location");
										event.startMillis = getMillis(jsonAround
												.getString("start_time"));
										event.endMillis = getMillis(jsonAround
												.getString("end_time"));
										event.attending_count = jsonAround
												.getInt("attending_count");
										dayOfWeek(event.startMillis,
												event.endMillis);
										event.dateStart = monthNameStart;
										event.dayStart = dayOfWeekStart;
										event.timeStart = timeStart;
										event.dateEnd = monthNameEnd;
										event.dayEnd = dayOfWeekEnd;
										event.timeEnd = timeEnd;
										event.isInProgress = isInProgress;
										event.parentPageName = event.loc;
										event.attending_count = jsonAround
												.getInt("attending_count");

										String b = "";
										try {
											if (!jsonAround.isNull("venue")) {
												JSONObject json = jsonAround
														.getJSONObject("venue");
												if (!json.isNull("street")
														|| !json
																.isNull("city")) {
													b += json
															.getString("street");
													if (b.length() > 0)
														b += ", ";
													if (json.has("city")) {
														b += json
																.getString("city");
														if (b.length() > 0) {
															b += ", ";
														}
													}
													if (json.has("country")) {
														b += json
																.getString("country");
													}
												}
											}
										} catch (Exception e) {
											//
										}
										event.venue = b;

										isInProgress = false;
										boolean add = true;
										int i = 0;
										for (EventData temp : eventCollection
												.getAroundMeEventList()) {
											if ((event.event_ID
													.equals(temp.event_ID))
													|| (event.name
															.equals(temp.name)
															&& event.desc
																	.equals(temp.desc) && event.venue
																.equals(temp.venue))) {

												if (event.attending_count > temp.attending_count) {
													add = true;
													eventCollection
															.getAroundMeEventList()
															.remove(i);
												} else {
													add = false;
												}
												break;
											}
											i++;
										}
										if (add) {
											eventCollection
													.getAroundMeEventList()
													.add(event);
										}

									}

								} catch (Exception e) {
									Log.e("interesting", e.toString() + " "
											+ Integer.toString(z));
								}
							}

						});

				Request requestEventAround = new Request(
						Session.getActiveSession(), "fql", bundleEventAround,
						HttpMethod.GET, new Request.Callback() {
							public void onCompleted(Response response) {
								int z = 0;
								try {
									JSONObject jsonAround = response
											.getGraphObject()
											.getInnerJSONObject();
									jarrayEventAround = jsonAround
											.getJSONArray("data");

									for (z = 0; z < jarrayEventAround.length(); z++) {
										jsonAround = jarrayEventAround
												.getJSONObject(z);
										final EventData event = new EventData();
										event.name = jsonAround
												.getString("name");
										event.desc = jsonAround
												.getString("description");
										event.event_ID = jsonAround
												.getString("eid");
										event.loc = jsonAround
												.getString("location");
										event.startMillis = getMillis(jsonAround
												.getString("start_time"));
										event.endMillis = getMillis(jsonAround
												.getString("end_time"));
										event.attending_count = jsonAround
												.getInt("attending_count");
										dayOfWeek(event.startMillis,
												event.endMillis);
										event.dateStart = monthNameStart;
										event.dayStart = dayOfWeekStart;
										event.timeStart = timeStart;
										event.dateEnd = monthNameEnd;
										event.dayEnd = dayOfWeekEnd;
										event.timeEnd = timeEnd;
										event.isInProgress = isInProgress;
										event.parentPageName = event.loc;
										event.attending_count = jsonAround
												.getInt("attending_count");

										String b = "";
										try {
											if (!jsonAround.isNull("venue")) {
												JSONObject json = jsonAround
														.getJSONObject("venue");
												if (!json.isNull("street")
														|| !json
																.isNull("city")) {
													b += json
															.getString("street");
													if (b.length() > 0)
														b += ", ";
													if (json.has("city")) {
														b += json
																.getString("city");
														if (b.length() > 0) {
															b += ", ";
														}
													}
													if (json.has("country")) {
														b += json
																.getString("country");
													}
												}
											}
										} catch (Exception e) {
											//
										}
										event.venue = b;

										isInProgress = false;
										if (eventCollection
												.getAroundMeEventByID(event.event_ID) == null) {
											eventCollection
													.getAroundMeEventList()
													.add(event);
										}

									}

								} catch (Exception e) {
									Log.e("aroundMe", e.toString() + " "
											+ Integer.toString(z));
								}
							}
						});

				Request requestPagesLike = new Request(
						Session.getActiveSession(), "fql", bundlePagesLike,
						HttpMethod.GET, new Request.Callback() {
							public void onCompleted(Response response) {
								Bundle bundleEventsLike = new Bundle();
								try {
									JSONObject json = response.getGraphObject()
											.getInnerJSONObject();

									jarrayLikes = json.getJSONArray("data");
									String a = "";
									a += "{";
									int l = jarrayLikes.length();
									for (int i = 0; i < jarrayLikes.length(); i++) {
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

									bundleEventsLike.putString("q", a);

								} catch (Exception e) {
									//
								}

								new Request(Session.getActiveSession(), "fql",
										bundleEventsLike, HttpMethod.GET,
										new Request.Callback() {
											public void onCompleted(
													Response response) {

												try {

													JSONObject json = response
															.getGraphObject()
															.getInnerJSONObject();

													JSONArray jDataArray = json
															.getJSONArray("data");
													ArrayList<String> s = new ArrayList<String>();
													int q = 0;
													while (q < jDataArray
															.length()) {

														JSONObject jsonObject = jDataArray
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

														if (add) {
															PageData page = new PageData();
															JSONObject jsonObject = json
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
													//
												}
											}
										}).executeAndWait();
							}
						});

				new RequestBatch(requestEventAround, requestEventInteresting,
						requestPagesLike, requestPlacesAround).executeAndWait();

				// RSVP

				String rsvpRequest = "{";
				for (EventData event : eventCollection.getAroundMeEventList()) {
					rsvpRequest += "\""
							+ event.event_ID
							+ "\":"
							+ "\"SELECT rsvp_status FROM event_member where eid = "
							+ event.event_ID + "  and uid = me()\",";
				}
				rsvpRequest += "}";

				Bundle bundleRSVP = new Bundle();
				bundleRSVP.putString("q", rsvpRequest);

				new Request(Session.getActiveSession(), "fql", bundleRSVP,
						HttpMethod.GET, new Callback() {
							@Override
							public void onCompleted(Response response) {
								try {
									JSONObject jObject = response
											.getGraphObject()
											.getInnerJSONObject();
									JSONArray jArray = jObject
											.getJSONArray("data");
									for (int i = 0; i < jArray.length(); i++) {
										jObject = jArray.getJSONObject(i);
										String ID = jObject.getString("name");
										JSONArray jj = jObject
												.getJSONArray("fql_result_set");
										if (jj.length() == 0) {
											eventCollection
													.getAroundMeEventByID(ID).status_attending = "Not Invited";
										} else {
											jObject = jj.getJSONObject(0);
											eventCollection
													.getAroundMeEventByID(ID).status_attending = jObject
													.getString("rsvp_status");
										}

									}
								} catch (Exception e) {
									Log.e("rsvp_request", e.toString());
								}
							}
						}).executeAndWait();

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (!placesSorted) {
					pageCollection.sortSearchByLikesAroundMeActivity();
					placesSorted = true;
				}
				int counter = 0;
				ArrayList<Integer> removeList = new ArrayList<Integer>();
				for (EventData event : eventCollection.getAroundMeEventList()) {
					if (event.status_attending.equals("declined")) {
						removeList.add(counter);
					}
					counter++;
				}

				int g = 0;
				for (int i : removeList) {
					eventCollection.getAroundMeEventList().remove(i - g);
					g++;
				}

				adapter.initializeLike();
				adapter.initializePlaces();
				adapter.initializeEvents();

				adapter.aroundMePicture();
				adapter.getPlacesImages(0);
				adapter.getUserLikesImages(0);

				visibilityDialog(false);

			}

		};

		task.execute();
	}

	private String getMillis(String UNIX) {
		if (UNIX.length() == 10) {
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
			return Long.toString(millis).substring(0, 10);
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

		}
	}

	public JSONArray getJArrayPlaces() {
		return jarrayPlaces;
	}

	public JSONArray getJArrayEventAround() {
		return jarrayEventAround;
	}

	public JSONArray getJArrayEventInteresting() {
		return jarrayEventInteresting;
	}

	public JSONArray getJArrayLike() {
		return jarrayLikes;
	}

	private void visibilityDialog(boolean b) {
		if (b) {
			dialog.setVisibility(View.VISIBLE);
			viewPager.setVisibility(View.INVISIBLE);
		} else {
			dialog.setVisibility(View.GONE);
			viewPager.setVisibility(View.VISIBLE);
		}
	}

	public boolean isDiscover() {
		return isDiscover;
	}

	public void listAroundItemClick(int paramInt) {
		adapter.listAroundItemClick(paramInt);
	}

	public int getPagerCurrentItem() {
		return viewPager.getCurrentItem();
	}

	public int getCurrentListStyle() {
		int currentListStyle = mPrefs.getInt("listStyle", 0);
		return currentListStyle;
	}

	public void showImageEventList(final int i) {
		AsyncTask<Void, Integer, Bitmap[]> task = new AsyncTask<Void, Integer, Bitmap[]>() {

			@Override
			public Bitmap[] doInBackground(Void... params) {

				adapter.showImageEventList(i);

				return null;
			}

		};
		task.execute();
	}

}