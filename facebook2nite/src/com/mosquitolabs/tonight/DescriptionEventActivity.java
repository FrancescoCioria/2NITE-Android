package com.mosquitolabs.tonight;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;

public class DescriptionEventActivity extends SherlockActivity {
	EventCollection eventCollection = EventCollection.getInstance();
	PageCollection pageCollection = PageCollection.getInstance();

	private SharedPreferences mPrefs;

	private MenuItem sort;
	private MenuItem distance;
	private MenuItem rsvp;
	private MenuItem share;
	private MenuItem seeOnFacebook;

	private String currentPageID;
	private String status;
	private String attendingCount;

	private TextView textAttending;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_page_prova);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		currentPageID = bundle.getString("currentPageID");
		status = eventCollection.getAroundMeEventByID(currentPageID).status_attending;

		ActionBar actionbar = getSupportActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		Drawable background = getResources().getDrawable(R.drawable.darkstripes_action);
		actionbar.setBackgroundDrawable(background);
		
		ImageView eventPicture = (ImageView) findViewById(R.id.imageViewEventCover);
		TextView textDesc = (TextView) findViewById(R.id.textViewDescription);
		TextView textDescYEY = (TextView) findViewById(R.id.textViewDescYEY);
		TextView textEnd = (TextView) findViewById(R.id.textViewEndSinglePage);
		TextView textStart = (TextView) findViewById(R.id.textViewStartSinglePage);
		TextView textLoc = (TextView) findViewById(R.id.textViewLocationSinglePage);
		TextView textName = (TextView) findViewById(R.id.textViewNameSinglePage);
		TextView textNamePage = (TextView) findViewById(R.id.textViewLocationNamePage);
		TextView textPageEmpty = (TextView) findViewById(R.id.textViewPageEmpty);
		textAttending = (TextView) findViewById(R.id.textViewAttendingCount);
		Button buttonPlace = (Button) findViewById(R.id.buttonPlace);
		Button buttonNavigate = (Button) findViewById(R.id.buttonNavigate);


		textLoc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PageData paramPageData = pageCollection.getPageByID(eventCollection
						.getAroundMeEventByID(currentPageID).parentPage_ID);
				try {
					if (paramPageData.address.length() > 0) {
						Intent i = new Intent(Intent.ACTION_VIEW, Uri
								.parse("google.navigation:q="
										+ paramPageData.address));
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
		// adView = (AdView) v.findViewById(R.id.adView35);
		textPageEmpty.setVisibility(View.GONE);
		// if (!isBirthdayWeek) {
		// adViewLoad();
		// }
		buttonNavigate.setVisibility(View.GONE);
		buttonPlace.setClickable(false);

		EventData my = eventCollection.getAroundMeEventByID(currentPageID);
		textName.setText(my.name);

		if (my.desc.length() > 0) {
			textDesc.setText(my.desc);
		} else {
			textDesc.setText("No description available for this event.");

		}

		if (readImageFromDisk(my.event_ID) != null) {
			eventPicture.setImageBitmap(readImageFromDisk(my.event_ID));

		}

		String dayEnd = my.dayEnd;
		String dateEnd = my.dateEnd;
		String timeEnd = my.timeEnd;

		String dateStart = my.dateStart;
		String dayStart = my.dayStart;
		String timeStart = my.timeStart;

		boolean eventHasAnEnd = my.hasAnEnd;

		

		textDescYEY.setTextColor(Color.DKGRAY);

		attendingCount = Integer.toString(my.attending_count);

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
		textNamePage.setText(my.loc);
		if (my.loc.equals("null")) {
			buttonPlace.setText("N/A");

		} else {
			buttonPlace.setText("( i )" + my.loc);
		}
		textLoc.setText("");
		textLoc.setTextColor(Color.rgb(11, 100, 217));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();

		inflater.inflate(R.menu.menu_search, menu);
		sort = menu.findItem(R.id.menu_sort);
		distance = menu.findItem(R.id.menu_distance);
		share = menu.findItem(R.id.menu_share);
		rsvp = menu.findItem(R.id.menu_rsvp);
		seeOnFacebook = menu.findItem(R.id.menu_facebook);
		MenuItem sortsearch = menu.findItem(R.id.menusearch_sort);

		sortsearch.setVisible(false);
		sort.setVisible(false);
		distance.setVisible(false);
		share.setVisible(true);
		rsvp.setVisible(true);
		seeOnFacebook.setVisible(true);
		
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
		
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

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

		ConnectivityManager cm = (ConnectivityManager) this
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
							eventCollection
									.saveToDisk(DescriptionEventActivity.this);
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
				Request request = new Request(Session.getActiveSession(),
						currentPageID + "/" + element, bundle, HttpMethod.POST,
						callback);
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

	private void toast(final String paramString) {
		DescriptionEventActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(DescriptionEventActivity.this, paramString, 0)
						.show();
			}
		});
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

}
