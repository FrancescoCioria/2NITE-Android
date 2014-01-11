package com.mosquitolabs.tonight;

import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

public class SearchActivity extends SherlockActivity {
	final String APP_ID = "219909391458551";
	private EventCollection eventCollection = EventCollection.getInstance();
	private PageCollection pageCollection = PageCollection.getInstance();
	private Preferences preferences = Preferences.getInstance();

	private JSONArray jArray;
	private ListView listView;
	// private EditText editTextSearch;
	private myCustomAdapterStar pageArrayAdapter;
	private SearchView searchView;

	private String mySearch;

	private LinearLayout progressSearch;
	// private boolean isFirstTime = false;
	private TextView textNoResult;
	private com.actionbarsherlock.app.ActionBar actionbar;

	// private Button buttonSearch;

	private int numberPicture;

	private int searchSortChecked;

	private Tracker MyTracker;
	private long counterStart;
	private long counterStop;

	private boolean isSearchActivity = true;
	private boolean isSearching = false;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();

	private Session session;

	private int oldFirstVisibleItem = 0;
	private int scrollState = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.MainTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		EasyTracker.getTracker().sendView();

		actionbar = getSupportActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		Drawable background = getResources().getDrawable(
				R.drawable.darkstripes_action);

		actionbar.setBackgroundDrawable(background);

		pageCollection.restorePreviousPage();
		pageCollection.getPageSearchList().clear();

		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mySearch = intent.getStringExtra(SearchManager.QUERY);
			onButtonClickDoTHisThing();

		}

		session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}

			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this)
						.setCallback(statusCallback));

			}
		}
		eventCollection.getEventList().clear();
		pageCollection.getModifiedPageList().clear();
		preferences.setisModifiedPageListToClear(false);

		initialize();

	}

	@Override
	protected void onResume() {
		pageCollection.readFromDisk(this);
		if (listView != null) {
			refreshSearchPage();
		}
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
		counterStart = Calendar.getInstance().getTimeInMillis();
		MyTracker = EasyTracker.getTracker();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
		counterStart = Calendar.getInstance().getTimeInMillis();
		MyTracker.sendTiming("Timing", counterStop - counterStart,
				"Search Activity", "");
	}

	@Override
	public void onBackPressed() {
		if (!pageCollection.getModifiedPageList().isEmpty()) {
			pageCollection.restoreSelectedPageList();
			preferences.setisModifiedPageListToClear(false);
		}
		pageCollection.saveToDisk(this);
		eventCollection.restoreEventList();
		isSearchActivity = false;
		super.onBackPressed();
	}

	@TargetApi(11)
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_search, menu);

		searchView = new SearchView(getSupportActionBar().getThemedContext());

		searchView.setIconifiedByDefault(false);
		searchView.setQueryHint("Search..");
		searchView.setFocusable(true);

		menu.add("Search")
				.setActionView(searchView)
				.setIcon(
						getResources().getDrawable(
								R.drawable.ic_menu_search_holo_light))
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
			public boolean onQueryTextChange(String newText) {
				return true;
			}

			public boolean onQueryTextSubmit(String query) {
				mySearch = query;
				// searchView.setQuery("", false);
				searchNew();
				return true;
			}
		};

		searchView.setOnQueryTextListener(queryTextListener);
		searchView.setSubmitButtonEnabled(true);

		openKeyboard();

		return super.onCreateOptionsMenu(menu);
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

			// case R.id.menusearch_sort:
			// setSearchSortBy();
			// return true;

			// case R.id.menu_search:
			// search();
			// return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onButtonClickDoTHisThing() {
		if (isOnline()) {
			Log.d("start_search", "start");

			textNoResult.setVisibility(View.GONE);
			setProgressBarVisible(true);
			setListViewVisible(false);
			// progressSearch.setVisibility(View.VISIBLE);

			// listView.setVisibility(View.GONE);

			Bundle localBundle = new Bundle();
			localBundle.putString("q", mySearch);
			localBundle.putString("type", "page");

			Log.d("request", "start_first");

			numberPicture = 0;

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					Log.i("request", "end_first");
					try {
						JSONObject j = response.getGraphObject()
								.getInnerJSONObject();
						searchComplete(j);
					} catch (Exception e) {
						isSearching=false;
					}
				}
			};

			Request request = new Request(Session.getActiveSession(), "search",
					localBundle, HttpMethod.GET, callback);
			request.executeAsync();

		} else {
			toast("Internet connection lost");
		}

	}

	private void toast(final String paramString) {
		SearchActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(SearchActivity.this, paramString,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@TargetApi(11)
	private void closeKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		// searchView.clearFocus();
	}

	private void openKeyboard() {
		searchView.requestFocus();
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(searchView.getWindowToken(),
		// InputMethodManager.SHOW_FORCED);

	}

	private void infoPage(final PageData paramPageData) {
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
		LinearLayout layoutButtons = (LinearLayout) layout
				.findViewById(R.id.layoutButtons);
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
				Intent localIntent = new Intent(SearchActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 0);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});
		image2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(SearchActivity.this,
						PhotostreamActivity.class);
				localIntent.putExtra("item", 1);
				localIntent.putExtra("ID", paramPageData._ID);
				startActivity(localIntent);
			}
		});
		image3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent localIntent = new Intent(SearchActivity.this,
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

		layoutButtons.setVisibility(View.GONE);

		buttonNavigate.setVisibility(View.GONE);

		buttonSeeOnFacebook.setVisibility(View.GONE);

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
									SearchActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image1.setImageDrawable(d);
												}
											});
									break;

								case 1:
									SearchActivity.this
											.runOnUiThread(new Runnable() {
												public void run() {
													image2.setImageDrawable(d);
												}
											});
									break;

								case 2:
									SearchActivity.this
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
										SearchActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text1.setVisibility(View.VISIBLE);
														progress1
																.setVisibility(View.GONE);
													}
												});
										break;

									case 1:
										SearchActivity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														text2.setVisibility(View.VISIBLE);
														progress2
																.setVisibility(View.GONE);

													}
												});
										break;

									case 2:
										SearchActivity.this
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

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) SearchActivity.this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
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
						refreshSearchPage();
						numberPicture = 0;
						downloadOnePicture(numberPicture);
						dialog.dismiss();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	public class myCustomAdapterStar extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap standardImage = null;

		public myCustomAdapterStar(Context paramContext) {
			this.mInflater = LayoutInflater.from(paramContext);
		}

		public int getCount() {
			return pageCollection.getPageSearchList().size();
		}

		public Object getItem(int paramInt) {
			return Integer.valueOf(paramInt);
		}

		public long getItemId(int paramInt) {
			return paramInt;
		}

		public View getView(final int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			ViewHolderStar localViewHolder;
			if (paramView == null) {
				paramView = mInflater.inflate(R.layout.list_pages, null);
				localViewHolder = new ViewHolderStar();
				localViewHolder.selected = (RelativeLayout) paramView
						.findViewById(R.id.imageViewSelected);
				localViewHolder.text = (TextView) paramView
						.findViewById(R.id.textViewListPages);
				localViewHolder.star = (ImageView) paramView
						.findViewById(R.id.imageViewStar);
				localViewHolder.image = (ImageView) paramView
						.findViewById(R.id.imageViewPage);
				localViewHolder.text_fan = (TextView) paramView
						.findViewById(R.id.textViewListPagesFanCount);
				standardImage = BitmapFactory.decodeResource(
						SearchActivity.this.getResources(),
						R.drawable.icon_gray);

				paramView.setTag(localViewHolder);
			} else {
				localViewHolder = (ViewHolderStar) paramView.getTag();
			}

			// if (readImageFromDisk(pageCollection.getPageSearchList().get(
			// paramInt)._ID) != null) {
			// localViewHolder.image
			// .setImageBitmap(readImageFromDisk(pageCollection
			// .getPageSearchList().get(paramInt)._ID));
			// }
			paramView.findViewById(R.id.progressBarImagePageList)
					.setVisibility(View.VISIBLE);

			localViewHolder.image.setImageBitmap(standardImage);

			showImagePageList(paramInt);

			localViewHolder.selected
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

							try {

								JSONObject json = jArray
										.getJSONObject(paramInt);
								JSONObject jsonObject = json
										.getJSONObject("location");

								pageCollection.getPageSearchList()
										.get(paramInt).checkins = json
										.getInt("checkins");
								pageCollection.getPageSearchList()
										.get(paramInt).website = json
										.getString("website");
								pageCollection.getPageSearchList()
										.get(paramInt).phone = json
										.getString("phone");
								pageCollection.getPageSearchList()
										.get(paramInt).desc = json
										.getString("description");

								String a = "";
								JSONArray jj = json.optJSONArray("categories");
								for (int j = 0; j < jj.length(); j++) {
									json = jj.getJSONObject(j);
									if (a.length() == 0)
										a += json.getString("name");
									else
										a += ", " + json.getString("name");
								}

								pageCollection.getPageSearchList()
										.get(paramInt).category = a;

								a = "";
								a += jsonObject.getString("street");
								if (a.length() > 0)
									a += ", ";
								a += jsonObject.getString("city");
								if (a.length() > 0)
									a += ", ";
								a += jsonObject.getString("country");
								pageCollection.getPageSearchList()
										.get(paramInt).address = a;

							} catch (Exception e) {
								Log.e("page_info", e.toString());
							}
							infoPage(pageCollection.getPageSearchList().get(
									paramInt));
						}
					});
			try {
				JSONObject json = jArray.getJSONObject(paramInt);
				pageCollection.getPageSearchList().get(paramInt).number_of_likes = json
						.getInt("fan_count");
			} catch (Exception e) {
				Log.e("fan_count", e.toString());
			}

			localViewHolder.text_fan.setText(Integer.toString(pageCollection
					.getPageSearchList().get(paramInt).number_of_likes)
					+ " likes");

			localViewHolder.text.setText(pageCollection.getPageSearchList()
					.get(paramInt).name);

			localViewHolder.star
					.setBackgroundResource(android.R.drawable.btn_star_big_off);

			for (PageData currentPage : pageCollection.getPageList()) {
				if (currentPage._ID.equals(pageCollection.getPageSearchList()
						.get(paramInt)._ID)) {
					localViewHolder.star
							.setBackgroundResource(android.R.drawable.btn_star_big_on);
					break;
				}
			}

			return paramView;
		}
	}

	static class ViewHolderStar {
		RelativeLayout selected;
		ImageView star;
		ImageView image;
		TextView text;
		TextView text_fan;
	}

	private void searchComplete(final JSONObject json) {
		JSONArray jDataArray = new JSONArray();
		try {
			jDataArray = json.getJSONArray("data");
			pageCollection.getPageSearchListRelevant().clear();

			for (int j = 0; j < jDataArray.length(); j++) {
				PageData localPageData = new PageData();
				JSONObject jsonObject = jDataArray.getJSONObject(j);
				localPageData.name = jsonObject.getString("name");
				localPageData._ID = jsonObject.getString("id");
				pageCollection.getPageSearchListRelevant().add(localPageData);
			}

			pageCollection.sortSearchByRelevance();

		} catch (JSONException localJSONException) {
			Log.e("search complete", localJSONException.toString());
			SearchActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					setProgressBarVisible(false);
					setListViewVisible(false);
					
					if (json.isNull("error")) {
						toast("An error occurred. Check your internet connection and try again.");
					} else {
						AlertDialog.Builder builder;
						final AlertDialog alertDialog;
						builder = new AlertDialog.Builder(SearchActivity.this);
						builder.setMessage("Your session expired or is no longer valid. Please sign in again.");
						builder.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Session.getActiveSession()
												.openForRead(
														new Session.OpenRequest(
																SearchActivity.this)
																.setCallback(statusCallback));
									}
								});
						builder.setCancelable(false);
						alertDialog = builder.create();
						alertDialog.show();
					}
				}
			});
		}

		refreshSearchPage();
		isSearching=false;
		if (jDataArray != null && jDataArray.length() > 0) {
			downloadPictures();
		} else {
			SearchActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					refreshSearchPage();
					setProgressBarVisible(false);
					setListViewVisible(true);
					textNoResult.setText("\"" + mySearch + "\""
							+ " didn't match any result.");
					textNoResult.setVisibility(View.VISIBLE);

				}
			});
		}

	}

	private void downloadPictures() {

		Bundle bundle = new Bundle();
		String a = "SELECT pic,pic_square,pic_large,type,checkins,location,phone,fan_count,categories,description,website FROM page WHERE page_id=";

		for (PageData page : pageCollection.getPageSearchList()) {
			a += page._ID;
			if (!page._ID.equals(pageCollection.getPageSearchList().get(
					pageCollection.getPageSearchList().size() - 1)._ID))
				a += " or page_id=";
		}

		bundle.putString("q", a);
		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				try {

					JSONObject json = response.getGraphObject()
							.getInnerJSONObject();
					jArray = json.getJSONArray("data");
					int searchListSize = pageCollection.getPageSearchList()
							.size();
					if (searchListSize > jArray.length())
						for (int i = searchListSize - 1; i >= jArray.length(); i--) {
							pageCollection.getPageSearchList().remove(i);
						}
					Log.i("image", "start_image");
					downloadOnePicture(0);

				} catch (Exception e) {
					setProgressBarVisible(false);
					setListViewVisible(false);

					Log.e("downloadPictures", e.toString());
					toast("an error has occurred");
				}
			}
		};
		Request request = new Request(Session.getActiveSession(), "fql",
				bundle, HttpMethod.GET, callback);
		request.executeAsync();
	}

	private synchronized void downloadOnePicture(final int j) {
		AsyncTask<Void, Integer, Bitmap> task = new AsyncTask<Void, Integer, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				if (isOnline()) {

					try {
						if (readImageFromDisk(pageCollection
								.getPageSearchList().get(j)._ID) == null) {
							try {
								JSONObject json = jArray.getJSONObject(j);
								URL img_value = null;
								if (json.getString("pic").length() > 0) {
									img_value = new URL(json.getString("pic"));

								} else {
									img_value = new URL(
											json.getString("pic_square"));
								}
								Bitmap icon = (BitmapFactory
										.decodeStream(img_value
												.openConnection()
												.getInputStream()));
								saveImageToDisk(pageCollection
										.getPageSearchList().get(j)._ID, icon);

								Log.i("image", Integer.toString(j));
							} catch (Exception e) {
								Log.e("image_fuck", e.toString());

							}

						}
						SearchActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								if (j == 0) {
									setProgressBarVisible(false);
									setListViewVisible(true);
								}

								showImagePageList(j);

								if (j + 1 < pageCollection.getPageSearchList()
										.size() && isSearchActivity() && !isSearching()) {
									downloadOnePicture(j + 1);

								}
							}
						});

					} catch (Exception e) {
						SearchActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								setProgressBarVisible(false);
								setListViewVisible(true);
								refreshSearchPage();
							}
						});
						Log.e("downloadOnePicture", e.toString());
					}
				} else {
					SearchActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							setProgressBarVisible(false);
							setListViewVisible(false);
							
							toast("Internet connection lost");
						}
					});
				}
				return null;
			}
		};
		task.execute();
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

	private void refreshSearchPage() {
		SearchActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				pageArrayAdapter.notifyDataSetChanged();
			}
		});
	}

	private void setProgressBarVisible(final boolean b) {
		SearchActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				if (b) {
					progressSearch.setVisibility(View.VISIBLE);
				} else {
					progressSearch.setVisibility(View.GONE);
				}
			}
		});
	}

	private void setListViewVisible(final boolean b) {
		SearchActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				if (b) {
					listView.setVisibility(View.VISIBLE);
				} else {
					listView.setVisibility(View.GONE);
				}
			}
		});
	}

	private void initialize() {
		progressSearch = (LinearLayout) findViewById(R.id.progressBarSearch);
		textNoResult = (TextView) findViewById(R.id.textViewNoResult);
		if (pageCollection.getPageSearchList().isEmpty()) {
			textNoResult
					.setText("Add pages to your favorite list to never miss their events again.");
			textNoResult.setVisibility(View.VISIBLE);
		}
		// buttonBack = (Button) findViewById(R.id.buttonBack);
		// buttonBack.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// onBackPressed();
		// }
		// });

		pageArrayAdapter = new myCustomAdapterStar(SearchActivity.this);
		listView = (ListView) findViewById(R.id.listSearch);
		listView.setAdapter(pageArrayAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {

				try {
					JSONObject json = jArray.getJSONObject(paramInt);

					JSONObject jsonObject = json.getJSONObject("location");
					pageCollection.getPageSearchList().get(paramInt).number_of_likes = json
							.getInt("fan_count");
					pageCollection.getPageSearchList().get(paramInt).checkins = json
							.getInt("checkins");
					pageCollection.getPageSearchList().get(paramInt).website = json
							.getString("website");
					pageCollection.getPageSearchList().get(paramInt).phone = json
							.getString("phone");
					pageCollection.getPageSearchList().get(paramInt).desc = json
							.getString("description");
					pageCollection.getPageSearchList().get(paramInt).picURL = new URL(
							json.getString("pic_large"));
					String a = "";
					JSONArray jj = json.optJSONArray("categories");
					for (int j = 0; j < jj.length(); j++) {
						json = jj.getJSONObject(j);
						if (a.length() == 0)
							a += json.getString("name");
						else
							a += ", " + json.getString("name");
					}

					pageCollection.getPageSearchList().get(paramInt).category = a;

					a = "";
					a += jsonObject.getString("street");
					if (a.length() > 0)
						a += ", ";
					a += jsonObject.getString("city");
					if (a.length() > 0)
						a += ", ";
					a += jsonObject.getString("country");
					pageCollection.getPageSearchList().get(paramInt).address = a;

				} catch (Exception e) {
				}
				pageCollection.addModifiedPage(pageCollection
						.getPageSearchList().get(paramInt));
				if (pageCollection.addPageToFavourites(pageCollection
						.getPageSearchList().get(paramInt))) {
					toast(pageCollection.getPageSearchList().get(paramInt).name
							+ " added as favourite!");
					// editTextSearch.setText("");
					preferences.setModifiedPages(true);
					try {
						listView.getChildAt(paramInt)
								.findViewById(R.id.imageViewStar)
								.setBackgroundResource(
										android.R.drawable.btn_star_big_on);
					} catch (Exception e) {
						Log.d("listitem click", "error setting star ON");
					}

				} else {
					pageCollection.removePageFromFavourites(pageCollection
							.getPageSearchList().get(paramInt));
					preferences.setModifiedPages(true);
					preferences.setModifiedSinglePage(true);
					toast(pageCollection.getPageSearchList().get(paramInt).name
							+ " removed from favourites!");
					try {
						listView.getChildAt(paramInt)
								.findViewById(R.id.imageViewStar)
								.setBackgroundResource(
										android.R.drawable.btn_star_big_off);
					} catch (Exception e) {
						Log.d("listitem click", "error setting star OFF");
					}
				}

			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> paramAdapterView,
					View paramView, int paramInt, long paramLong) {
				try {
					JSONObject json = jArray.getJSONObject(paramInt);
					JSONObject jsonObject = json.getJSONObject("location");
					pageCollection.getPageSearchList().get(paramInt).number_of_likes = json
							.getInt("fan_count");
					pageCollection.getPageSearchList().get(paramInt).checkins = json
							.getInt("checkins");
					pageCollection.getPageSearchList().get(paramInt).website = json
							.getString("website");
					pageCollection.getPageSearchList().get(paramInt).phone = json
							.getString("phone");
					pageCollection.getPageSearchList().get(paramInt).desc = json
							.getString("description");

					String a = "";
					JSONArray jj = json.optJSONArray("categories");
					for (int j = 0; j < jj.length(); j++) {
						json = jj.getJSONObject(j);
						if (a.length() == 0)
							a += json.getString("name");
						else
							a += ", " + json.getString("name");
					}

					pageCollection.getPageSearchList().get(paramInt).category = a;

					a = "";
					a += jsonObject.getString("street");
					if (a.length() > 0)
						a += ", ";
					a += jsonObject.getString("city");
					if (a.length() > 0)
						a += ", ";
					a += jsonObject.getString("country");
					pageCollection.getPageSearchList().get(paramInt).address = a;

				} catch (Exception e) {
				}
				infoPage(pageCollection.getPageSearchList().get(paramInt));
				return false;
			}
		});

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				SearchActivity.this.scrollState = scrollState;

				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					int first = listView.getFirstVisiblePosition();
					if (first < oldFirstVisibleItem) {
						for (int z = listView.getLastVisiblePosition(); z >= first; z--) {
							showImagePageList(z);
						}
					} else {

						for (int z = first; z <= listView
								.getLastVisiblePosition(); z++) {
							showImagePageList(z);
						}
					}

					oldFirstVisibleItem = first;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

		// buttonSearch = (Button) findViewById(R.id.buttonSearch);
		// editTextSearch = (EditText) findViewById(R.id.editTextSearch);
		//
		// buttonSearch.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// search();
		// }
		// });
		//
		// editTextSearch.setOnEditorActionListener(new OnEditorActionListener()
		// {
		//
		// @Override
		// public boolean onEditorAction(TextView v, int actionId,
		// KeyEvent event) {
		// if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		// search();
		// }
		// return false;
		// }
		// });
		//
		// if (isFirstTime && mySearch.length() > 0) {
		// editTextSearch.setText(mySearch);
		// onButtonClickDoTHisThing();
		// isFirstTime = false;
		// }
	}

	// private void search() {
	// mySearch = editTextSearch.getText().toString();
	// if (mySearch.length() > 0) {
	// onButtonClickDoTHisThing();
	// }
	// }

	private void searchNew() {
		if (mySearch.length() > 0) {
			isSearching = true;
			closeKeyboard();
			onButtonClickDoTHisThing();
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {

		}
	}

	@TargetApi(11)
	public void showImagePageList(final int i) {
		AsyncTask<Void, Integer, Bitmap[]> task = new AsyncTask<Void, Integer, Bitmap[]>() {

			@Override
			public Bitmap[] doInBackground(Void... params) {
				// android.os.Process
				// .setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND
				// + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
				Bitmap bmp = null;
				Bitmap imagePage = null;

				if (scrollState != OnScrollListener.SCROLL_STATE_FLING) {
					if (pageCollection.getPageSearchList().size() > i) {

						// GET PAGE PICTURE

						try {
							java.io.FileInputStream in = SearchActivity.this
									.openFileInput(pageCollection
											.getPageSearchList().get(i)._ID);
							bmp = BitmapFactory.decodeStream(in);

						} catch (Exception e) {

						}
					}

				}

				Bitmap[] toReturn = { bmp, imagePage };
				return toReturn;
			}

			@Override
			public void onPostExecute(Bitmap[] value) {
				if (value[0] != null) {
					int first = listView.getFirstVisiblePosition();
					int last = listView.getLastVisiblePosition();
					int current = i;
					if (first <= current && current <= last) {
						try {
							View v = listView.getChildAt(current - first);

							ImageView image = (ImageView) v
									.findViewById(R.id.imageViewPage);

							image.setImageBitmap(value[0]);

							v.findViewById(R.id.progressBarImagePageList)
									.setVisibility(View.GONE);

						} catch (Exception e) {
						}
					}
				}
			}

		};
		try {
			if (Build.VERSION.SDK_INT >= 11) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				task.execute();
			}
		} catch (Exception e) {
			Log.d("ThreadOnExecutor", e.toString());
		}
	}

	private boolean isSearchActivity() {
		return isSearchActivity;
	}
	private boolean isSearching() {
		return isSearching;
	}

}
