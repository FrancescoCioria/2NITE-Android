package com.mosquitolabs.tonight;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Formatter;
import java.util.TimeZone;

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
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.AccessToken;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class AroundMeActivity extends SherlockActivity {

	final String APP_ID = "219909391458551";
	private EventCollection eventCollection = EventCollection.getInstance();
	private JSONObject json = new JSONObject();
	private JSONArray jarrayPlaces;
	private JSONArray jarrayAround;
	private JSONObject jsonObject;
	private ListView listView;
	private ListView listViewAroundMe;
	private double latitude;
	private double longitude;
	private MenuItem sort;
	private MenuItem distance;
	private MenuItem rsvp;
	private MenuItem share;
	private MenuItem seeOnFacebook;
	private MenuItem sortsearch;
	private PageCollection pageCollection = PageCollection.getInstance();

	private String loc;
	private String desc;
	private String currentPageID = null;
	private String attendingCount;
	private String name;
	private SharedPreferences mPrefs;
	private Preferences preferences = Preferences.getInstance();
	private boolean firstLocation = true;
	private boolean eventHasAnEnd = true;
	private boolean isBirthdayWeek = false;
	private boolean placesSorted = false;
	private com.actionbarsherlock.app.ActionBar actionbar;
	private CustomViewPager viewPager;
	private int page = 0;
	private Button tab1;
	private Button tab2;
	private AroundMePagerAdapter adapter;
	private TextView textEventEmpty;
	private TextView textDesc;
	private TextView textDescYEY;
	private TextView textEnd;
	private TextView textStart;
	private TextView textLoc;
	private TextView textName;
	private TextView textNamePage;
	private TextView textPageEmpty;
	private TextView textAttending;
	private ImageView eventPicture;
	private Bitmap mIcon1;
	private String dateStart;
	private String status;
	private String dayStart;
	private String dayEnd;
	private String timeStart;
	private String dateEnd;
	private String timeEnd;
	private String dayOfWeekStart;
	private String dayOfWeekEnd;
	private String monthNameStart = "";
	private String monthNameEnd = "";
	private boolean isInProgress = false;
	private boolean noPicture = true;
	private AdView adView;
	private String gender;
	private int searchSortChecked;
	private AdRequest adRequest = new AdRequest();
	private Button buttonPlace;
	private Button buttonNavigate;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private Session session;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		this.setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_aroundme);
		EasyTracker.getTracker().sendView();
		pageCollection.clearPageAroundMe();
		eventCollection.getAroundMeEventList().clear();
		pageCollection.restorePreviousPage();
		
		actionbar = getSupportActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		adapter = new AroundMePagerAdapter(AroundMeActivity.this);
		viewPager = (CustomViewPager) findViewById(R.id.viewpager);
		viewPager.setPagingEnabled(false);
		viewPager.setAdapter(adapter);
		Drawable background = getResources().getDrawable(
				R.drawable.darkstripes_action);
		actionbar.setBackgroundDrawable(background);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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

		tab1 = (Button) findViewById(R.id.tab1);
		tab1.setText("Events");
		tab2 = (Button) findViewById(R.id.tab2);
		tab2.setText("Places");
		tab2.setTextColor(getResources().getColor(R.color.android_gray));
		tab1.setBackgroundColor(Color.rgb(217, 117, 0));

		tab1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tab2.setBackgroundResource(R.color.gray);
				tab2.setTextColor(getResources().getColor(R.color.android_gray));
				tab1.setBackgroundResource(R.color.orange_title);
				tab1.setTextColor(Color.WHITE);
				// page = 0;
				// viewPager.invalidate();
				// viewPager.setAdapter(adapter);
				sort.setVisible(true);
				distance.setVisible(false);
				share.setVisible(false);
				sortsearch.setVisible(false);
				seeOnFacebook.setVisible(false);
				rsvp.setVisible(false);
				viewPager.setCurrentItem(0);
				// aroundMe();

			}
		});

		tab2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tab1.setBackgroundResource(R.color.gray);
				tab1.setTextColor(getResources().getColor(R.color.android_gray));
				tab2.setBackgroundResource(R.color.orange_title);
				tab2.setTextColor(Color.WHITE);
				// page = 1;
				// viewPager.invalidate();
				// viewPager.setAdapter(adapter);

				sortsearch.setVisible(false);
				sort.setVisible(false);
				distance.setVisible(false);
				share.setVisible(false);
				seeOnFacebook.setVisible(false);
				rsvp.setVisible(false);
				// adapter.initializePlaces();
				viewPager.setCurrentItem(1);

			}
		});

		isBirthdayWeek = mPrefs.getBoolean("isBirthdayWeek", false);
		adRequest.addTestDevice("B51A78A2EC2CF273BB3EDAE13C5591AC");
		adRequest.addTestDevice("3DBF295B2FE2E3DF5CFF75D962FF409C");
		gender = mPrefs.getString("gender", null);
		String year = mPrefs.getString("year", null);
		String day = mPrefs.getString("day", null);
		String month = mPrefs.getString("month", null);
		isBirthdayWeek = mPrefs.getBoolean("isBirthdayWeek", false);
		if (year != null && day != null && month != null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1,
					Integer.parseInt(day));
			adRequest.setBirthday(cal);
			cal.set(cal.get(Calendar.YEAR), Integer.parseInt("07") - 1,
					Integer.parseInt("08"));
			Calendar today = Calendar.getInstance();
			SharedPreferences.Editor editor = mPrefs.edit();
			if (!isBirthdayWeek
					&& today.get(Calendar.DAY_OF_YEAR) == cal
							.get(Calendar.DAY_OF_YEAR)) {

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
			editor.commit();

		}

		if (gender != null) {
			if (gender.equals("male"))
				adRequest.setGender(AdRequest.Gender.MALE);
			else
				adRequest.setGender(AdRequest.Gender.FEMALE);
		}
	}

	public void getLocation() {
		Log.e("location", "getting location");
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener ll = new mylocationlistener();
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
	}

	@Override
	protected void onResume() {
		pageCollection.readFromDisk(this);
		if (listView != null) {
			adapter.refreshPageAdapter();
		}
		if (listViewAroundMe != null) {
			adapter.refreshEventsAround();
		}
		
		

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
		AroundMeActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(AroundMeActivity.this, paramString, 0).show();
			}
		});
	}

	public void infoPage(final PageData paramPageData) {
		final Dialog layout = new Dialog(AroundMeActivity.this);
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
				Intent localIntent = new Intent(AroundMeActivity.this,
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
				Intent localIntent = new Intent(AroundMeActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 1);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});
		image3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(AroundMeActivity.this,
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
				builder = new AlertDialog.Builder(AroundMeActivity.this);
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
									AroundMeActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image1.setImageDrawable(d);
												}
											});
									break;

								case 1:
									AroundMeActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image2.setImageDrawable(d);
												}
											});
									break;

								case 2:
									AroundMeActivity.this
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
										AroundMeActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text1.setVisibility(View.VISIBLE);
														progress1
																.setVisibility(View.GONE);

													}
												});
										break;

									case 1:
										AroundMeActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text2.setVisibility(View.VISIBLE);
														progress2
																.setVisibility(View.GONE);

													}
												});
										break;

									case 2:
										AroundMeActivity.this
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
		super.onBackPressed();

	}

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getSupportMenuInflater();

		inflater.inflate(R.menu.menu_search, menu);
		sortsearch = menu.findItem(R.id.menusearch_sort);
		sort = menu.findItem(R.id.menu_sort);
		distance = menu.findItem(R.id.menu_distance);
		share = menu.findItem(R.id.menu_share);
		rsvp = menu.findItem(R.id.menu_rsvp);
		seeOnFacebook = menu.findItem(R.id.menu_facebook);
		if (page == 0) {
			sort.setVisible(true);
		} else {
			sort.setVisible(true);

		}
		distance.setVisible(false);
		share.setVisible(false);
		sortsearch.setVisible(false);
		seeOnFacebook.setVisible(false);
		rsvp.setVisible(false);

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

		case R.id.menusearch_sort:
			setSearchSortBy();
			return true;

		case R.id.menu_sort:
			setSortBy();
			return false;

		case R.id.menu_distance:
			setDistance();
			return false;
		case R.id.menu_facebook:
			seeEventOnFacebook();
			return false;
		case R.id.menu_rsvp:
			updateRSVP();
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
										.getAroundMeEventByID(currentPageID).name);
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						shareBody);
				startActivity(Intent
						.createChooser(
								shareIntent,
								"Share "
										+ "\""
										+ eventCollection
												.getAroundMeEventByID(currentPageID).name
										+ "\"" + " using"));
			}
			return false;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean isOnline() {

		ConnectivityManager cm = (ConnectivityManager) AroundMeActivity.this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private void updateRSVP() {
		if (isOnline()) {
			status = eventCollection.getAroundMeEventByID(currentPageID).status_attending;
			if (!status.equals("Not Invited") && !status.equals("not_replied")) {
				final CharSequence[] items = { "Going", "Maybe", "Declined" };
				final CharSequence[] element = { "attending", "maybe",
						"declined" };
				int checked = 0;
				if (status.equals("attending"))
					checked = 0;
				if (status.equals("unsure"))
					checked = 1;
				if (status.equals("declined"))
					checked = 2;
				statusMenu(items, element, checked);
			} else {
				final CharSequence[] items = { "Not Answered", "Going",
						"Maybe", "Declined" };
				final CharSequence[] element = { "empty", "attending", "maybe",
						"declined" };
				statusMenu(items, element, 0);
			}
		} else
			toast("No internet connection");
	}

	private void statusMenu(final CharSequence[] items,
			final CharSequence[] element, int checked) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Change your RSVP status");
		builder.setSingleChoiceItems(items, checked,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						EventData event = eventCollection
								.getAroundMeEventByID(currentPageID);
						if (items.length == 4 && item == 1
								&& !event.status_attending.equals("attending")) {
							event.attending_count++;
							attendingCount = Integer
									.toString(event.attending_count);
							textAttending.setText(attendingCount
									+ " people going!");
						}
						if (event.status_attending.equals("attending")
								&& item != 0) {
							event.attending_count--;
							event.attending_count++;
							attendingCount = Integer
									.toString(event.attending_count);
							textAttending.setText(attendingCount
									+ " people going!");
						}
						if (items.length == 3 && item == 0
								&& !event.status_attending.equals("attending")) {
							event.attending_count++;
							event.attending_count++;
							attendingCount = Integer
									.toString(event.attending_count);
							textAttending.setText(attendingCount
									+ " people going!");
						}
						if (items.length != 4) {
							updateButtonStatus(element[item].toString());
							rsvp.setTitle("RSVP: " + items[item].toString());
						} else {
							if (item != 0) {
								updateButtonStatus(element[item].toString());
								rsvp.setTitle("RSVP: " + items[item].toString());
							}
						}
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private synchronized void updateButtonStatus(final String element) {
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			public String doInBackground(Void... params) {
				Bundle bundle = new Bundle();
				final EventData event = eventCollection
						.getAroundMeEventByID(currentPageID);
				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {

						try {
							if (element.equals("maybe")) {
								event.status_attending = "unsure";
							} else {
								event.status_attending = element;
							}
							eventCollection.saveToDisk(AroundMeActivity.this);
							if (event.status_attending.equals("attending")) {
								SharedPreferences.Editor editor = mPrefs.edit();
								editor.putBoolean("update", true);
								editor.commit();
							}
							toast("RSVP status updated");
						} catch (Exception e) {
							Log.e("rsvp", e.toString());
							toast("an error occurred while trying to update your RSVP status");

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

	private void seeEventOnFacebook() {
		try {
			if (isOnline()) {
				String uri = "fb://event/" + currentPageID;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(intent);
				toast("Opening facebook app, please wait..");
			} else {
				toast("No internet connection");
			}
		} catch (Exception e) {
			toast("Can't open facebook app, be sure to have installed it on your phone.");
		}
	}

	private void setSortBy() {
		final CharSequence[] items = { "Start time", "Attending count" };
		int checked = 1;
		if (mPrefs.getBoolean("sort_by_date", true))
			checked = 0;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Sort by..");
		builder.setSingleChoiceItems(items, checked,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						SharedPreferences.Editor editor = mPrefs.edit();
						if (item == 0) {
							editor.putBoolean("sort_by_date", true);
							eventCollection.aroundMeSortByDate();
						} else {
							editor.putBoolean("sort_by_date", false);
							eventCollection.aroundMeSortByAttendingCount();
						}
						editor.commit();
						adapter.aroundMePicture();
						adapter.refreshEventsAround();
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setSearchSortBy() {
		final CharSequence[] items = { "Relevance", "Name", "Number of likes" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Sort by..");
		builder.setSingleChoiceItems(items, searchSortChecked,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0:
							pageCollection.sortSearchByRelevance();
							searchSortChecked = 0;
							break;

						case 1:
							pageCollection.sortSearchByName();
							searchSortChecked = 1;
							break;

						case 2:
							pageCollection.sortSearchByLikes();
							searchSortChecked = 2;
							break;
						}
						adapter.refreshPageAdapter();
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setDistance() {

		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.distance_dialog, null);

		final EditText et = (EditText) layout
				.findViewById(R.id.editTextDistance);
		et.setText(Integer.toString(mPrefs.getInt("distance", 5)));
		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setMessage("Set a distance (MAX 50km)");
		builder.setNegativeButton("Cancel", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setPositiveButton("Set", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int x = Integer.parseInt(et.getText().toString());
				if (x <= 50) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putInt("distance", x);
					editor.commit();
					textEventEmpty.setVisibility(View.VISIBLE);
					textEventEmpty
							.setText("Please wait..\nLooking for events nearby \nwithin three days.\nThis may take a while.");
					firstLocation = true;
					LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					LocationListener ll = new mylocationlistener();
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							0, 0, ll);
					dialog.dismiss();
				} else {
					AlertDialog.Builder builderMax;
					AlertDialog alertDialogMax;
					builderMax = new AlertDialog.Builder(AroundMeActivity.this);
					builderMax
							.setMessage("Facebook accepts a max. distance of 50km.\nPlease set a lower value.");
					builderMax.setNeutralButton("Ok",
							new Dialog.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					alertDialogMax = builderMax.create();
					alertDialogMax.show();

				}
			}
		});
		alertDialog = builder.create();
		alertDialog.show();

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
		form = new Formatter();
		form.format("%02d", endCal.get(Calendar.MINUTE));
		timeEnd = Integer.toString(endCal.get(Calendar.HOUR_OF_DAY)) + ":"
				+ form.toString();
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

	public synchronized void getEventsAroundMe() {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				eventCollection.getAroundMeEventList().clear();
				Calendar cal = Calendar.getInstance();
				/*
				 * Calendar cal2 = Calendar.getInstance(TimeZone
				 * .getTimeZone("America/Los_Angeles")); long mi =
				 * cal.get(Calendar.ZONE_OFFSET); long mu =
				 * cal2.get(Calendar.ZONE_OFFSET); long def = mi - mu; if (def <
				 * 0) def = def * (-1); String current_time =
				 * Long.toString(cal.getTimeInMillis() + def);
				 */

				String current_time = Long.toString(cal.getTimeInMillis());
				long Hours24 = 86400000;

				// String hours24FromNow = Long.toString(cal.getTimeInMillis()
				// + def + Hours24);

				String hours24FromNow = Long.toString(cal.getTimeInMillis()
						+ Hours24);
				current_time = current_time.substring(0, 10);
				hours24FromNow = hours24FromNow.substring(0, 10);
				String distance = Integer
						.toString(mPrefs.getInt("distance", 5) * 1000);
				String limit = "500";
				if (mPrefs.getInt("distance", 5) > 25)
					limit = "1000";
				String a = "Select eid,name,attending_count,venue,creator,description,location,start_time,end_time,pic_big from event where eid in (SELECT eid from event_member WHERE uid in (Select page_id,name from place where distance(latitude, longitude,"
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
				Bundle bundle = new Bundle();
				bundle.putString("q", a);

				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {
						int z = 0;
						try {
							JSONObject jsonAround = response.getGraphObject()
									.getInnerJSONObject();
							jarrayAround = jsonAround.getJSONArray("data");

							for (z = 0; z < jarrayAround.length(); z++) {
								jsonAround = jarrayAround.getJSONObject(z);
								final EventData event = new EventData();
								event.name = jsonAround.getString("name");
								event.desc = jsonAround
										.getString("description");
								event.event_ID = jsonAround.getString("eid");
								event.loc = jsonAround.getString("location");
								event.startMillis = getMillis(jsonAround
										.getString("start_time"));
								event.endMillis = getMillis(jsonAround
										.getString("end_time"));
								event.attending_count = jsonAround
										.getInt("attending_count");
								dayOfWeek(event.startMillis, event.endMillis);
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
										event.event_ID
												+ "/invited/"
												+ mPrefs.getString("user_id",
														null), new Bundle(),
										HttpMethod.GET, callback);
								request.executeAndWait();

								isInProgress = false;
								// if(event.endMillis.compareTo(current_time)>0){
								eventCollection.getAroundMeEventList().add(
										event);

								// }

							}

						} catch (Exception e) {
							Log.e("aroundMe",
									e.toString() + " " + Integer.toString(z));
						}
					}
				};

				Request request = new Request(session, "fql", bundle,
						HttpMethod.GET, callback);
				request.executeAndWait();

				AroundMeActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						adapter.initializeEvents();
						adapter.aroundMePicture();
					}
				});

				return null;
			}

		};
		task.execute();

	}

	private void activity() {
		View v = null;

	}

	private synchronized void adViewLoad() {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				AroundMeActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						adView.loadAd(adRequest);
					}
				});
				return null;
			}
		};
		task.execute();

	}

	private void singlePage(int eventIndex) {
		if (!eventCollection.getAroundMeEventList().isEmpty()
				&& currentPageID != null) {

			EventData my = eventCollection.getAroundMeEventList().get(
					eventIndex);
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
			if (readImageFromDisk(my.event_ID) != null) {
				Bitmap image = readImageFromDisk(my.event_ID);
				mIcon1 = image;
				noPicture = false;
			} else {
				noPicture = true;
			}
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
		if (!noPicture) {
			eventPicture.setImageBitmap(mIcon1);
		}
		textName.setText(name);
		if (desc.length() > 0) {
			textDesc.setText(desc);
		} else {
			textDesc.setText("No description available for this event.");
		}
		textDescYEY.setTextColor(Color.DKGRAY);
		textAttending.setText(attendingCount + " people going!");

		if (dayStart.equals("Today") || dayStart.equals("Tomorrow")) {
			textStart.setText(dayStart + " at " + timeStart);
		} else {
			textStart.setText(dayStart + " " + dateStart + " at " + timeStart);
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

		} else {
			buttonPlace.setText("( i )" + loc);
		}
		textLoc.setText("");
		textLoc.setTextColor(Color.rgb(11, 100, 217));
	}

	private class mylocationlistener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {

			if (firstLocation) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				pageCollection.getPageAroundMe().clear();
				eventCollection.getAroundMeEventList().clear();
				getEventsAroundMe();
				getPlacesAroundMe();
				firstLocation = false;
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

	public synchronized void getPlacesAroundMe() {
		AsyncTask<Void, Integer, Bitmap> task = new AsyncTask<Void, Integer, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {

				Bundle bundle = new Bundle();

				String az = "Select page_id,name,location,fan_count,checkins,phone,description,pic_square,pic,pic_large,categories from page where page_id in (select page_id from place where distance(latitude, longitude,"
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
				// String result_events = mFacebook.request("fql", bundle);
				Request.Callback callback = new Request.Callback() {
					public void onCompleted(Response response) {
						try {
							json = response.getGraphObject()
									.getInnerJSONObject();
							jarrayPlaces = json.getJSONArray("data");
							int i = 0;
							while (i < jarrayPlaces.length()) {
								json = jarrayPlaces.getJSONObject(i);

								PageData page = new PageData();
								jsonObject = json.getJSONObject("location");
								page._ID = json.getString("page_id");
								page.name = json.getString("name");
								page.number_of_likes = json.getInt("fan_count");
								page.checkins = json.getInt("checkins");
								page.phone = json.getString("phone");
								page.desc = json.getString("description");
								page.picURL = new URL(
										json.getString("pic_large"));

								String az = "";
								JSONArray jj = json.optJSONArray("categories");
								for (int j = 0; j < jj.length(); j++) {
									json = jj.getJSONObject(j);
									if (az.length() == 0)
										az += json.getString("name");
									else
										az += ", " + json.getString("name");
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
									az += jsonObject.getString("country");

								page.address = az;
								pageCollection.getPageAroundMe().add(page);
								Log.i("added", "added");

								i++;

							}

						} catch (Exception e) {
							toast("An error occurred");
							Log.e("getplaces", e.toString());
						}
					}
				};

				Request request = new Request(session, "fql", bundle,
						HttpMethod.GET, callback);
				request.executeAndWait();

				return null;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (!placesSorted) {
					pageCollection.sortSearchByLikesAroundMeActivity();
					placesSorted = true;
				}
				adapter.initializePlaces();

				int i = 0;

				adapter.getPlacesImages(i);

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
	public JSONArray getJArrayAround() {
		return jarrayAround;
	}

}
