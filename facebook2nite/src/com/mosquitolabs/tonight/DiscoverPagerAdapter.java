package com.mosquitolabs.tonight;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DiscoverPagerAdapter extends PagerAdapter implements TitleProvider {
	private String[] TITLES = { "interesting", "places nearby",
			"suggested pages" };
	private static final int EVENTS = 0;
	private static final int PLACES_AROUND = 1;
	private static final int PLACES_I_LIKE = 2;

	private ListView listPlaces;

	private int currentFirstVisibleItem = -1;
	private RelativeLayout separatorFake;
	private View now;
	private View next;
	private TextView dayFake;
	private TextView dateFake;
	private Rect rectList = new Rect();

	private int userLikesInt = 0;
	private int placesInt = 0;

	private final DiscoverActivity context;
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

	private ListView listUserLike;

	private boolean likesSorted = false;
	private boolean aroundSorted = false;
	private boolean isFirstTimeLike = true;

	private myCustomAdapterUserLikes customAdapterUserLikes;

	private TextView noPagesUserLikes;

	private int aroundMePictures;

	public DiscoverPagerAdapter(DiscoverActivity context) {
		this.context = context;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		customAdapterPlaces = new myCustomAdapterPlaces(context);
		eventArrayAdapter = new MyCustomAdapterEventsAroundMe(context);
		customAdapterUserLikes = new myCustomAdapterUserLikes(context);

	}

	@Override
	public String getTitle(int position) {
		return TITLES[position];
	}

	@Override
	public int getCount() {
		return (3);
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
			listViewAroundMe.setAdapter(eventArrayAdapter);
			textEventEmpty = (TextView) v.findViewById(R.id.textViewEventEmpty);
			separatorFake = (RelativeLayout) v
					.findViewById(R.id.LayoutSeparatorFake);
			dayFake = (TextView) v.findViewById(R.id.textViewSeparatorDay);
			dateFake = (TextView) v.findViewById(R.id.textViewSeparatorMonth);

			if (eventCollection.getAroundMeEventList().size() == 0) {
				textEventEmpty.setVisibility(View.VISIBLE);
			} else {
				textEventEmpty.setVisibility(View.GONE);
			}
			textEventEmpty
					.setText("Please wait..\nLooking for events nearby \nwithin three days.\nThis may take a while.");

			listViewAroundMe.setOnScrollListener(new OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view,
						int scrollState) {

				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {

					if (currentFirstVisibleItem != firstVisibleItem) {

						now = listViewAroundMe.getChildAt(0);
						next = listViewAroundMe.getChildAt(1);
						if (now != null && next != null) {
							currentFirstVisibleItem = firstVisibleItem;
							Log.i("scroll", Integer.toString(firstVisibleItem));
						}

					}

					boolean isSeparatorTopVisible = false;
					boolean isSeparatorBottomVisible = false;
					boolean isSeparatorEnabled = false;

					if (rectList.isEmpty() && listViewAroundMe != null) {
						listViewAroundMe.getHitRect(rectList);
					}

					if (now != null && next != null
							&& context.getPagerCurrentItem() == EVENTS) {

						View separatorBottom = now
								.findViewById(R.id.LayoutSeparatorBottom);

						if (now.findViewById(R.id.controlViewTop)
								.getLocalVisibleRect(rectList)) {
							isSeparatorTopVisible = true;
						}
						if (now.findViewById(R.id.controlViewBottom)
								.getLocalVisibleRect(rectList)) {
							isSeparatorBottomVisible = true;
						}

						isSeparatorEnabled = next.findViewById(
								R.id.LayoutSeparatorTop).isEnabled();

						boolean isSeparatorFakeVisible = separatorFake
								.isShown();

						if (firstVisibleItem == 0
								&& isSeparatorTopVisible
								&& (isSeparatorFakeVisible || separatorBottom
										.isShown())) {
							separatorFake.setVisibility(View.GONE);
							separatorBottom.setVisibility(View.GONE);
						} else {
							if ((!isSeparatorFakeVisible || separatorBottom
									.isShown())
									&& !isSeparatorTopVisible
									&& isSeparatorBottomVisible) {
								if (!eventCollection.getAroundMeEventList().get(
										listViewAroundMe
												.getFirstVisiblePosition()).isInProgress) {
									dayFake.setText(eventCollection
											.getAroundMeEventList()
											.get(listViewAroundMe
													.getFirstVisiblePosition()).dayStart);
									dateFake.setText(eventCollection
											.getAroundMeEventList()
											.get(listViewAroundMe
													.getFirstVisiblePosition()).dateStart);
								} else {
									dayFake.setText("Right Now");
									dateFake.setText("");
								}

								separatorFake.setVisibility(View.VISIBLE);
								separatorBottom.setVisibility(View.GONE);
							}

							if ((isSeparatorFakeVisible || !separatorBottom
									.isShown())
									&& isSeparatorEnabled
									&& !isSeparatorTopVisible
									&& !isSeparatorBottomVisible) {
								separatorFake.setVisibility(View.GONE);
								separatorBottom.setVisibility(View.VISIBLE);
							}

						}
					}
				}
			});

			/*
			 * listViewAroundMe .setOnItemClickListener(new
			 * AdapterView.OnItemClickListener() { public void onItemClick(
			 * AdapterView<?> paramAdapterView, View paramView, int paramInt,
			 * long paramLong) { Intent localIntent = new Intent(context,
			 * DescriptionEventActivity.class); localIntent.putExtra(
			 * "currentPageID", eventCollection.getAroundMeEventList().get(
			 * paramInt).event_ID);
			 * 
			 * context.startActivity(localIntent);
			 * 
			 * } });
			 */

			context.getLocation();

			break;

		case PLACES_AROUND:
			v = inflater.inflate(R.layout.places_user_likes, null);
			noPagesPlaces = (TextView) v
					.findViewById(R.id.textViewNoPagesPlaces);
			progressLoginPlaces = (LinearLayout) v
					.findViewById(R.id.linearLayoutPagesILikeProgress);
			listPlaces = (ListView) v
					.findViewById(R.id.listViewPlacesUserLikes);
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

		case PLACES_I_LIKE:
			v = inflater.inflate(R.layout.pages_i_like, null);
			noPagesUserLikes = (TextView) v
					.findViewById(R.id.textViewNoPagesUserLikes);
			progressLoginUserLikes = (LinearLayout) v
					.findViewById(R.id.linearLayoutPagesILikeProgress);
			listUserLike = (ListView) v.findViewById(R.id.listViewPagesILike);
			listUserLike.setAdapter(customAdapterUserLikes);
			listUserLike
					.setOnItemClickListener(new ListView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> a, View v,
								int i, long l) {

							pageCollection.addModifiedPage(pageCollection
									.getPageSearchList().get(i));
							if (pageCollection
									.addPageToFavourites(pageCollection
											.getPageSearchList().get(i))) {
								preferences.setModifiedPages(true);
								toast(pageCollection.getPageSearchList().get(i).name
										+ " has been added to your pages.");

							} else {
								pageCollection
										.removePageFromFavourites(pageCollection
												.getPageSearchList().get(i));
								preferences.setModifiedPages(true);
								preferences.setModifiedSinglePage(true);
								toast(pageCollection.getPageSearchList().get(i).name
										+ " has been removed from your pages.");
							}
							refreshLikeAdapter();
						}
					});

			if (pageCollection.getPageSearchList().isEmpty()) {
				if (!likesSorted) {
					progressLoginUserLikes.setVisibility(View.VISIBLE);
					noPagesUserLikes.setVisibility(View.GONE);
				} else {
					noPagesUserLikes.setVisibility(View.VISIBLE);
					progressLoginUserLikes.setVisibility(View.GONE);
				}
			} else {
				noPagesUserLikes.setVisibility(View.GONE);
				progressLoginUserLikes.setVisibility(View.GONE);

			}

			if (isFirstTimeLike) {
				isFirstTimeLike = false;
			}
			break;

		}
		((ViewPager) pager).addView(v, 0);
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
					Log.e("around_picture", e.toString());
				}

				placesInt++;
				return i;
			}

			@Override
			public void onPostExecute(Integer i) {

				if (listPlaces != null
						&& listPlaces.getFirstVisiblePosition() <= i
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

				if (i < pageCollection.getPageAroundMe().size()
						&& context.isDiscover()) {
					getPlacesImages(placesInt);
				}
			}

		};
		task.execute();
	}

	public synchronized void getUserLikesImages(final int i) {
		AsyncTask<Void, Integer, Integer> task = new AsyncTask<Void, Integer, Integer>() {

			@Override
			public Integer doInBackground(Void... params) {
				if (i < pageCollection.getPageSearchList().size()) {
					try {
						int z = 0;
						String index_ID = pageCollection.getPageSearchList()
								.get(i)._ID;
						JSONObject json = new JSONObject();
						JSONArray jarrayLikes = context.getJArrayLike();

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
							if (context.readImageFromDisk(json
									.getString("page_id")) == null) {

								URL img_value = new URL(json.getString("pic"));
								context.saveImageToDisk(json
										.getString("page_id"), BitmapFactory
										.decodeStream(img_value
												.openConnection()
												.getInputStream()));
							}
						}

					} catch (Exception e) {
						Log.e("image_userlike", e.toString());
					}
				}

				return i;
			}

			@Override
			public void onPostExecute(Integer i) {
				if (i < pageCollection.getPageSearchList().size()
						&& listUserLike != null) {

					if (listUserLike.getFirstVisiblePosition() <= i
							&& i <= listUserLike.getLastVisiblePosition()) {
						View v = listUserLike.getChildAt(i
								- listUserLike.getFirstVisiblePosition());
						ImageView image = (ImageView) v
								.findViewById(R.id.imageViewPage);
						image.setImageBitmap(context
								.readImageFromDisk(pageCollection
										.getPageSearchList().get(i)._ID));
					}
				}
				userLikesInt++;
				if (userLikesInt < pageCollection.getPageSearchList().size()
						&& context.isDiscover()) {
					getUserLikesImages(userLikesInt);
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
					JSONArray jarrayAround = context.getJArrayEventAround();
					JSONArray jarrayInteresting = context
							.getJArrayEventInteresting();
					int z = 0;
					int k = 0;

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

					if (b) {

						while (k < jarrayInteresting.length() && b) {
							jsonAround = jarrayInteresting.getJSONObject(k);
							if (index_ID.equals(jsonAround.getString("eid"))) {
								b = false;
							} else {
								k++;
							}
						}

					}

					if (!b) {
						picture = context.readImageFromDisk(jsonAround
								.getString("eid"));
						if (picture == null) {
							URL img_value = new URL(
									jsonAround.getString("pic_big"));
							picture = BitmapFactory.decodeStream(img_value
									.openConnection().getInputStream());
							context.saveImageToDisk(
									jsonAround.getString("eid"), picture);
						}
					}
				} catch (Exception e) {
					Log.e("aroundMePicture", e.toString());
				}

				final Bitmap pic = picture;

				return pic;
			}

			@Override
			public void onPostExecute(Bitmap pic) {

				if (pic != null && listViewAroundMe != null) {
					if (listViewAroundMe.getFirstVisiblePosition() <= aroundMePictures
							&& aroundMePictures <= listViewAroundMe
									.getLastVisiblePosition()) {
						View v = listViewAroundMe.getChildAt(aroundMePictures
								- listViewAroundMe.getFirstVisiblePosition());
						ImageView image = (ImageView) v
								.findViewById(R.id.imageViewList);

						image.setImageBitmap(pic);

					}
				}

				aroundMePictures++;
				if (aroundMePictures < eventCollection.getAroundMeEventList()
						.size() && context.isDiscover()) {
					aroundMePicture();
				}

			}

		};
		task.execute();
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
	public Parcelable saveState() {
		return null;
	}

	public class myCustomAdapterUserLikes extends BaseAdapter {
		private LayoutInflater mInflater;

		public myCustomAdapterUserLikes(Context paramContext) {
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
			PageData temp = new PageData();

			temp = (pageCollection.getPageSearchList().get(paramInt));

			final PageData page = temp;
			if (paramView == null) {
				paramView = mInflater.inflate(R.layout.list_pages, null);
				localViewHolder = new ViewHolderStar();
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
				paramView.setTag(localViewHolder);
			}
			localViewHolder = (ViewHolderStar) paramView.getTag();

			localViewHolder.text.setText(page.name);

			int counter = userLikesInt;

			if (paramInt <= counter
					&& context.readImageFromDisk(page._ID) != null) {
				localViewHolder.image.setImageBitmap(context
						.readImageFromDisk(page._ID));
			} else {
				localViewHolder.image.setImageBitmap(BitmapFactory
						.decodeResource(context.getResources(),
								R.drawable.icon_other_events));
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
			} else {
				localViewHolder.image.setImageBitmap(BitmapFactory
						.decodeResource(context.getResources(),
								R.drawable.icon_other_events));
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

	public void initializeLike() {
		if (!likesSorted) {
			pageCollection.sortSearchByName();
			likesSorted = true;
		}
		refreshLikeAdapter();
		if (listUserLike != null) {
			listUserLike.setVisibility(View.VISIBLE);
			progressLoginUserLikes.setVisibility(View.GONE);
			if (pageCollection.getPageSearchList().isEmpty()) {
				noPagesUserLikes
						.setText("Sorry, couldn't find any place to suggest you based on your likes.");
				noPagesUserLikes.setVisibility(View.VISIBLE);
			} else {
				noPagesUserLikes.setVisibility(View.GONE);
			}
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

	public void refreshLikeAdapter() {

		customAdapterUserLikes.notifyDataSetChanged();

	}

	public void refreshEventsAround() {

		eventArrayAdapter.notifyDataSetChanged();

	}

	private void toast(final String paramString) {

		Toast.makeText(context, paramString, Toast.LENGTH_SHORT).show();

	}

	public void listAroundItemClick(int paramInt) {
		Intent localIntent = new Intent(context, DescriptionEventActivity.class);
		localIntent.putExtra("currentPageID", eventCollection
				.getAroundMeEventList().get(paramInt).event_ID);

		context.startActivity(localIntent);
	}

}
