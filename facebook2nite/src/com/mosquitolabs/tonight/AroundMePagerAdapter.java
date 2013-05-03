package com.mosquitolabs.tonight;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AroundMePagerAdapter extends PagerAdapter {
	private static final int EVENTS = 0;
	private static final int PLACES = 1;

	private ListView listPlaces;

	private int userLikesInt = 0;
	private int placesInt = 0;

	private final AroundMeActivity context;
	private View v;

	private PageCollection pageCollection = PageCollection.getInstance();
	private Preferences preferences = Preferences.getInstance();

	private myCustomAdapterPlaces customAdapterPlaces;

	private LinearLayout progressLoginUserLikes;
	private LinearLayout progressLoginPlaces;

	private TextView noPagesPlaces;
	private TextView textEventEmpty;

	final String APP_ID = "219909391458551";
	private EventCollection eventCollection = EventCollection.getInstance();

	private ListView listViewAroundMe;

	private MyCustomAdapterEventsAroundMe eventArrayAdapter;

	private SharedPreferences mPrefs;

	// private String result_events;

	private int aroundMePictures;

	public AroundMePagerAdapter(AroundMeActivity context) {
		this.context = context;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public int getCount() {
		return (2);
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		LayoutInflater inflater = (LayoutInflater) pager.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		v = null;

		switch (position) {

		case EVENTS:
			v = inflater.inflate(R.layout.main_activity, null);
			listViewAroundMe = (ListView) v.findViewById(R.id.listViewMain);
			eventArrayAdapter = new MyCustomAdapterEventsAroundMe(context);
			listViewAroundMe.setAdapter(eventArrayAdapter);
			textEventEmpty = (TextView) v.findViewById(R.id.textViewEventEmpty);

			textEventEmpty.setVisibility(View.VISIBLE);
			textEventEmpty
					.setText("Please wait..\nLooking for events nearby \nwithin three days.\nThis may take a while.");

			listViewAroundMe
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(
								AdapterView<?> paramAdapterView,
								View paramView, int paramInt, long paramLong) {
							Intent localIntent = new Intent(context,
									DescriptionEventActivity.class);
							localIntent.putExtra(
									"currentPageID",
									eventCollection.getAroundMeEventList().get(
											paramInt).event_ID);

							context.startActivity(localIntent);

						}
					});

			context.getLocation();

			break;

		case PLACES:
			v = inflater.inflate(R.layout.places_user_likes, null);
			noPagesPlaces = (TextView) v
					.findViewById(R.id.textViewNoPagesPlaces);
			progressLoginPlaces = (LinearLayout) v
					.findViewById(R.id.linearLayoutPagesILikeProgress);
			listPlaces = (ListView) v
					.findViewById(R.id.listViewPlacesUserLikes);
			customAdapterPlaces = new myCustomAdapterPlaces(context);
			listPlaces.setAdapter(customAdapterPlaces);
			listPlaces
					.setOnItemClickListener(new ListView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> a, View v,
								int i, long l) {
							pageCollection.addModifiedPage(pageCollection
									.getPageAroundMe().get(i));
							if (pageCollection
									.addPageToFavourites(pageCollection
											.getPageAroundMe().get(i))) {
								preferences.setModifiedPages(true);
								toast(pageCollection.getPageAroundMe().get(i).name
										+ " has been added to your pages.");

							} else {
								pageCollection
										.removePageFromFavourites(pageCollection
												.getPageAroundMe().get(i));
								preferences.setModifiedPages(true);
								preferences.setModifiedSinglePage(true);
								toast(pageCollection.getPageAroundMe().get(i).name
										+ " has been removed from your pages.");
							}
							refreshPageAdapter();
						}
					});

			break;

		}
		((CustomViewPager) pager).addView(v, 0);
		return v;
	}

	public synchronized void getPlacesImages(final int i) {
		AsyncTask<Void, Integer, Integer> task = new AsyncTask<Void, Integer, Integer>() {

			@Override
			public Integer doInBackground(Void... params) {

				try {
					Log.i("around_picture", Integer.toString(placesInt));
					int z = 0;
					String index_ID = pageCollection.getPageAroundMe().get(i)._ID;
					boolean b = true;
					JSONObject json = new JSONObject();
					JSONArray jarrayAround = context.getJArrayPlaces();
					while (z < jarrayAround.length() && b) {
						json = jarrayAround.getJSONObject(z);
						if (index_ID.equals(json.getString("page_id"))) {
							b = false;
						} else {
							z++;
						}

					}
					if (!b) {
						if (context
								.readImageFromDisk(json.getString("page_id")) == null) {
							Log.i("around_picture", "download");

							URL img_value = new URL(json.getString("pic"));
							context.saveImageToDisk(json.getString("page_id"),
									BitmapFactory.decodeStream(img_value
											.openConnection().getInputStream()));
						}
					}

				} catch (Exception e) {
					Log.e("image_userlike", e.toString());
				}

				placesInt++;
				return i;
			}

			@Override
			public void onPostExecute(Integer i) {

				{
					if (listPlaces.getFirstVisiblePosition() <= i
							&& i <= listPlaces.getLastVisiblePosition()) {
						View v = listPlaces.getChildAt(i
								- listPlaces.getFirstVisiblePosition());
						ImageView image = (ImageView) v
								.findViewById(R.id.imageViewPage);
						if (pageCollection.getPageAroundMe().size() > i) {
							image.setImageBitmap(context
									.readImageFromDisk(pageCollection
											.getPageAroundMe().get(i)._ID));
						}
					}
				}
				if (i < pageCollection.getPageAroundMe().size()) {
					getPlacesImages(placesInt);
				}
			}

		};
		task.execute();
	}

	public synchronized void aroundMePicture() {
		AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {

				Bitmap picture = null;
				try {
					JSONObject jsonAround = new JSONObject();
					JSONArray jarrayAround = context.getJArrayAround();
					int z = 0;
					boolean b = true;
					String index_ID = eventCollection.getAroundMeEventList()
							.get(aroundMePictures).event_ID;
					while (z < jarrayAround.length() && b) {
						jsonAround = jarrayAround.getJSONObject(z);
						if (index_ID.equals(jsonAround.getString("eid"))) {
							b = false;
						} else {
							z++;
						}
					}
					URL img_value = new URL(jsonAround.getString("pic_big"));
					picture = BitmapFactory.decodeStream(img_value
							.openConnection().getInputStream());
					context.saveImageToDisk(jsonAround.getString("eid"),
							picture);
				} catch (Exception e) {
					Log.e("aroundMePicture", e.toString());
				}
				final Bitmap pic = picture;

				return pic;
			}

			@Override
			public void onPostExecute(Bitmap pic) {

				if (listViewAroundMe.getFirstVisiblePosition() <= aroundMePictures
						&& aroundMePictures <= listViewAroundMe
								.getLastVisiblePosition()) {
					View v = listViewAroundMe.getChildAt(aroundMePictures
							- listViewAroundMe.getFirstVisiblePosition());
					ImageView image = (ImageView) v
							.findViewById(R.id.imageViewList);
					if (pic != null) {
						image.setImageBitmap(pic);
					}
				}

				aroundMePictures++;
				if (aroundMePictures < eventCollection.getAroundMeEventList()
						.size()) {
					aroundMePicture();
				}

			}

		};
		task.execute();
	}

	@Override
	public void destroyItem(View pager, int position, Object view) {
		((CustomViewPager) pager).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public Parcelable saveState() {
		return null;
	}



	public class myCustomAdapterPlaces extends BaseAdapter {
		private LayoutInflater mInflater;

		public myCustomAdapterPlaces(Context paramContext) {
			this.mInflater = LayoutInflater.from(paramContext);
		}

		public int getCount() {

			return pageCollection.getPageAroundMe().size();

		}

		public Object getItem(int paramInt) {
			return Integer.valueOf(paramInt);
		}

		public long getItemId(int paramInt) {
			return paramInt;
		}

		public View getView(final int paramInt, View paramView,
				ViewGroup paramViewGroup) {
			ViewHolderStarPlaces localViewHolder;
			PageData temp = new PageData();

			temp = (pageCollection.getPageAroundMe().get(paramInt));

			final PageData page = temp;
			paramView = mInflater.inflate(R.layout.list_pages, null);
			localViewHolder = new ViewHolderStarPlaces();
			localViewHolder.selected = (RelativeLayout) paramView
					.findViewById(R.id.imageViewSelected);
			localViewHolder.text = (TextView) paramView
					.findViewById(R.id.textViewListPages);
			localViewHolder.text_fan = (TextView) paramView
					.findViewById(R.id.textViewListPagesFanCount);
			localViewHolder.star = (ImageView) paramView
					.findViewById(R.id.imageViewStar);
			localViewHolder.image = (ImageView) paramView
					.findViewById(R.id.imageViewPage);
			localViewHolder.text.setText(page.name);

			int counter = placesInt;

			if (paramInt <= counter
					&& context.readImageFromDisk(page._ID) != null) {
				localViewHolder.image.setImageBitmap(context
						.readImageFromDisk(page._ID));
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
			localViewHolder.selected
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							context.infoPage(page);
						}
					});

			localViewHolder.text_fan.setText(Integer
					.toString(page.number_of_likes) + " likes");
			paramView.setTag(localViewHolder);

			return paramView;
		}
	}

	static class ViewHolderStarPlaces {
		RelativeLayout selected;
		ImageView star;
		ImageView image;
		TextView text;
		TextView text_fan;
	}

	public void initializePlaces() {

		pageCollection.sortSearchByLikesAroundMeActivity();

		refreshPageAdapter();
		listPlaces.setVisibility(View.VISIBLE);
		progressLoginPlaces.setVisibility(View.GONE);
		if (pageCollection.getPageAroundMe().isEmpty()) {
			noPagesPlaces
					.setText("Sorry, couldn't find any place nearby your location.");
			noPagesPlaces.setVisibility(View.VISIBLE);
		} else {
			noPagesPlaces.setVisibility(View.GONE);
		}

	}

	public void initializeEvents() {
		if (mPrefs.getBoolean("sort_by_date", true)) {
			eventCollection.aroundMeSortByDate();
		} else {
			eventCollection.aroundMeSortByAttendingCount();
		}
		refreshEventsAround();
		textEventEmpty.setText("Sorry, can't find any event nearby");
		if (eventCollection.getAroundMeEventList().isEmpty()) {
			textEventEmpty.setVisibility(View.VISIBLE);
		} else {
			textEventEmpty.setVisibility(View.GONE);
		}
	}

	public void setProgressPlacesVisible(boolean b) {
		if (b) {
			progressLoginPlaces.setVisibility(View.VISIBLE);
		} else {
			progressLoginPlaces.setVisibility(View.GONE);

		}
	}

	public void setProgressUserLikesVisible(boolean b) {
		if (b) {
			progressLoginUserLikes.setVisibility(View.VISIBLE);
		} else {
			progressLoginUserLikes.setVisibility(View.GONE);

		}
	}

	public void refreshPageAdapter() {

		customAdapterPlaces.notifyDataSetChanged();

	}

	public void refreshEventsAround() {

		eventArrayAdapter.notifyDataSetChanged();

	}

	private void toast(final String paramString) {

		Toast.makeText(context, paramString, Toast.LENGTH_SHORT).show();

	}

}
