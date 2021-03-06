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
import com.actionbarsherlock.view.MenuItem;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;

public class DescriptionEventActivity extends SherlockActivity {
	EventCollection eventCollection = EventCollection.getInstance();
	PageCollection pageCollection = PageCollection.getInstance();

	private SharedPreferences mPrefs;

	private MenuItem rsvp;
	private MenuItem share;
	private MenuItem seeOnFacebook;

//	private String currentPageID;
//	private String status;
	private String attendingCount;
	
	private EventData event = new EventData();

	private TextView textAttending;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setTheme(R.style.Theme_Sherlock);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_page_prova);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String currentPageID = bundle.getString("currentPageID");
		event = eventCollection.getAroundMeEventByID(currentPageID);
//		status = event.status_attending;

		ActionBar actionbar = getSupportActionBar();
		actionbar.setHomeButtonEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);
		Drawable background = getResources().getDrawable(
				R.drawable.darkstripes_action);
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
//				PageData paramPageData = pageCollection.getPageByID(eventCollection
//						.getAroundMeEventByID(currentPageID).parentPage_ID);
//				try {
//					if (paramPageData.address.length() > 0) {
//						Intent i = new Intent(Intent.ACTION_VIEW, Uri
//								.parse("google.navigation:q="
//										+ paramPageData.address));
//						startActivity(i);
//					} else {
//						toast("Sorry, no address available for "
//								+ paramPageData.name);
//					}
//				} catch (Exception e) {
//					toast("Can't open navigator app, be sure to have installed it on your phone.");
//
//				}
			}
		});
		// adView = (AdView) v.findViewById(R.id.adView35);
		textPageEmpty.setVisibility(View.GONE);
		// if (!isBirthdayWeek) {
		// adViewLoad();
		// }

		buttonNavigate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder;
				final AlertDialog alertDialog;
				
				String a = "";
				
				if (event.venue != null && event.venue.length() > 0) {
					a = event.venue;
				} 
				final String address = a;

				builder = new AlertDialog.Builder(DescriptionEventActivity.this);
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

		buttonNavigate.setVisibility(View.GONE);
		// buttonPlace.setClickable(false);

		textName.setText(event.name);

		if (event.desc.length() > 0) {
			textDesc.setText(event.desc);
		} else {
			textDesc.setText("No description available for this event.");

		}

		if (readImageFromDisk(event.event_ID) != null) {
			eventPicture.setImageBitmap(readImageFromDisk(event.event_ID));

		}

		String dayEnd = event.dayEnd;
		String dateEnd = event.dateEnd;
		String timeEnd = event.timeEnd;

		String dateStart = event.dateStart;
		String dayStart = event.dayStart;
		String timeStart = event.timeStart;

		boolean eventHasAnEnd = event.hasAnEnd;

		textDescYEY.setTextColor(Color.DKGRAY);

		attendingCount = Integer.toString(event.attending_count);

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
		textNamePage.setText(event.loc);
		if (event.loc.equals("null")) {
			buttonPlace.setText("N/A");

		} else {
			buttonPlace.setText("( i )" + event.loc);
		}
		textLoc.setText("");
		textLoc.setTextColor(Color.rgb(11, 100, 217));

		if (event.venue != null && event.venue.length() > 0) {
			buttonNavigate.setVisibility(View.VISIBLE);
		}

		buttonPlace.setEnabled(false);
		buttonPlace.setTextColor(getResources().getColor(R.color.android_gray));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_description, menu);

		share = menu.findItem(R.id.menu_share);
		rsvp = menu.findItem(R.id.menu_rsvp);
		seeOnFacebook = menu.findItem(R.id.menu_facebook);

		share.setVisible(true);
		rsvp.setVisible(true);
		seeOnFacebook.setVisible(true);
		
		String status = event.status_attending;

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
			if (event.event_ID != null) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				shareIntent.setType("text/plain");
				String shareBody = "www.facebook.com/" + event.event_ID
						+ "\n\nSent using 2nite.";
				shareIntent
						.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								event.name);
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						shareBody);
				startActivity(Intent
						.createChooser(
								shareIntent,
								"Share "
										+ "\""
										+ event.name
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
			String status = event.status_attending;
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
						event.event_ID + "/" + element, bundle, HttpMethod.POST,
						callback);
				request.executeAndWait();

				boolean add = true;
				for (EventData temp : eventCollection.getCompleteEventList()) {
					if (event.event_ID.equals(temp.event_ID)) {
						add = false;
						break;
					}
				}
				if (add) {
					event.parentPage_ID = "1";
					event.parentPageName = "My Events..";
					eventCollection.addToCompleteEventList(event);
				} else {
					eventCollection.getCompleteEventByID(event.event_ID).status_attending = event.status_attending;
					eventCollection.getCompleteEventByID(event.event_ID).attending_count = event.attending_count;
				}

				return null;
			}
		};
		task.execute();
	}

	private void seeEventOnFacebook() {
		try {
			if (isOnline()) {
				String uri = "fb://event/" + event.event_ID;
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
