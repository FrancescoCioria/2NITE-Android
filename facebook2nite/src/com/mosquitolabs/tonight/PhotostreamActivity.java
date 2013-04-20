package com.mosquitolabs.tonight;

import java.io.InputStream;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.android.Facebook;

public class PhotostreamActivity extends Activity {

	private JSONArray jArrayPhotoStream = new JSONArray();
	private Facebook mFacebook;
	private SharedPreferences mPrefs;
	private ProgressBar progress;
	private ProgressBar progressImage;
	private ViewPager pagerPhotostream;
	private int lastItem;
	private Drawable currentDrawable=null;
	private boolean changedOrientation = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photostream);
		Bundle bundle = getIntent().getExtras();
		String pageID = bundle.getString("ID");
		int item = bundle.getInt("item");
		mFacebook = new Facebook("219909391458551");

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null) {
			mFacebook.setAccessToken(access_token);
		}

		if (expires != 0) {
			mFacebook.setAccessExpires(expires);
		}

		progress = (ProgressBar) findViewById(R.id.progressBar);

		photoStream(pageID, item);
	}

	private class ViewPagerPhotoStreamAdapter extends PagerAdapter {

		private final Context context;
		private View v;

		public ViewPagerPhotoStreamAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return jArrayPhotoStream.length();
		}

		@Override
		public Object instantiateItem(View pager, int position) {

			LayoutInflater inflater = (LayoutInflater) pager.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			v = inflater.inflate(R.layout.photostream_image, null);
			progressImage = (ProgressBar) v.findViewById(R.id.progressBarImage);
			ImageView image = (ImageView) v.findViewById(R.id.imageView);
			TextView text = (TextView) v.findViewById(R.id.textView);
			downloadPhotostreamImages(image, text, position);

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

	private synchronized void downloadPhotostreamImages(final ImageView image,
			final TextView text, final int position) {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {

				try {
					final Drawable d;
					if (!changedOrientation) {
						JSONObject json = jArrayPhotoStream
								.getJSONObject(position);
						final URL url = new URL(json.getString("src_big"));
						InputStream content = (InputStream) url.getContent();
						d = Drawable.createFromStream(content, "src");
					} else {
						d = currentDrawable;
						changedOrientation=false;
					}
					if (position == pagerPhotostream.getCurrentItem()) {
						currentDrawable = d;
						lastItem=position;
					}

					PhotostreamActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							image.setImageDrawable(d);
							text.setText(Integer.toString(position + 1)
									+ " of "
									+ Integer.toString(jArrayPhotoStream
											.length()));
							//progressImage.setVisibility(View.GONE);

						}
					});

				} catch (Exception e) {
					Log.e("photostream", e.toString());
				}

				return null;
			}
		};
		task.execute();

	}

	private synchronized void photoStream(final String page_ID, final int item) {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {
				final ViewPagerPhotoStreamAdapter adapter = new ViewPagerPhotoStreamAdapter(
						PhotostreamActivity.this);

				String a = "SELECT src_big FROM photo WHERE pid IN (SELECT pid FROM photo WHERE aid IN (SELECT aid FROM album WHERE owner='"
						+ page_ID + "' AND type!='profile'))";
				Bundle bun = new Bundle();
				bun.putString("q", a);

				try {
					String string = mFacebook.request("fql", bun);
					JSONObject json = new JSONObject(string);
					jArrayPhotoStream = json.getJSONArray("data");
				} catch (Exception e) {
					// TODO: handle exception
				}

				pagerPhotostream = (ViewPager) findViewById(R.id.viewpagerPhotostream);

				PhotostreamActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						pagerPhotostream.setPageMargin(15);
						pagerPhotostream.setAdapter(adapter);
						pagerPhotostream.setCurrentItem(item);
						progress.setVisibility(View.GONE);
					}
				});

				return null;
			}
		};
		task.execute();

	}

	

	@Override
	protected void onResume() {
		if(!changedOrientation&&currentDrawable!=null){
			changedOrientation = true;
			pagerPhotostream.setCurrentItem(lastItem);
		}
		super.onResume();
	}

}
