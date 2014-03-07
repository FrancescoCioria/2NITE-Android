package com.mosquitolabs.tonight;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
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

	private StickyListHeadersListView listViewAroundMe;

	private MyCustomAdapterEventsAroundMe eventArrayAdapter;

	private SharedPreferences mPrefs;

	private ListView listUserLike;

	private boolean likesSorted = false;
	private boolean isFirstTimeLike = true;

	private myCustomAdapterUserLikes customAdapterUserLikes;

	private TextView noPagesUserLikes;

	private int aroundMePictures;

	private int oldFirstVisibleItem = 0;
	private int scrollState = 0;

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
			listViewAroundMe = (StickyListHeadersListView) v
					.findViewById(R.id.listViewMain);
			listViewAroundMe.setAdapter(eventArrayAdapter);
			textEventEmpty = (TextView) v.findViewById(R.id.textViewEventEmpty);

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
					DiscoverPagerAdapter.this.scrollState = scrollState;

					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						int first = listViewAroundMe.getFirstVisiblePosition();
						if (first < oldFirstVisibleItem) {
							for (int z = listViewAroundMe
									.getLastVisiblePosition(); z >= first; z--) {
								showImageEventList(z);
							}
						} else {

							for (int z = first; z <= listViewAroundMe
									.getLastVisiblePosition(); z++) {
								showImageEventList(z);
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
							refreshLikeAdapter();
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
							refreshPageAdapter();
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
					Log.i("around_picture", Integer.toString(i));
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

				if (i + 1 < pageCollection.getPageAroundMe().size()
						&& context.isDiscover()) {
					getPlacesImages(i + 1);
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
				if (i + 1 < pageCollection.getPageSearchList().size()
						&& context.isDiscover()) {
					getUserLikesImages(i + 1);
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
					EventData event = eventCollection.getAroundMeEventList()
							.get(aroundMePictures);
					JSONObject jsonAround = new JSONObject();
					JSONArray jarrayAround = context.getJArrayEventAround();
					JSONArray jarrayInteresting = context
							.getJArrayEventInteresting();
					int z = 0;
					int k = 0;

					boolean b = true;
					String index_ID = event.event_ID;

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
							String ID = jsonAround.getString("eid");
							event.imageUri = img_value;
							try {
								picture = BitmapFactory.decodeStream(img_value
										.openConnection().getInputStream());
								context.saveImageToDisk(ID, picture);
								event.imageDownloaded = true;
							} catch (Exception e) {
								Log.d("Failed downloadImage aroundme",
										e.toString());
							}
						} else {
							URL img_value = new URL(
									jsonAround.getString("pic_big"));
							event.imageUri = img_value;
							event.imageDownloaded = true;
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

				if (pic != null) {
					int first = listViewAroundMe.getFirstVisiblePosition();
					int last = listViewAroundMe.getLastVisiblePosition();
					int current = aroundMePictures;
					if (first <= current && current <= last) {
						try {
							View v = listViewAroundMe.getListChildAt(current
									- first);

							ImageView image = (ImageView) v
									.findViewById(R.id.imageViewList);

							image.setImageBitmap(pic);

							v.findViewById(R.id.progressBarImageEventList)
									.setVisibility(View.GONE);

						} catch (Exception e) {
						}

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

	@TargetApi(11)
	public void showImageEventList(final int i) {
		AsyncTask<Void, Integer, Bitmap[]> task = new AsyncTask<Void, Integer, Bitmap[]>() {

			@Override
			public Bitmap[] doInBackground(Void... params) {
				// android.os.Process
				// .setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND
				// + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
				Bitmap bmp = null;
				Bitmap imagePage = null;

				if (eventCollection.getAroundMeEventList().size() > i) {

					EventData event = eventCollection.getAroundMeEventList()
							.get(i);

					if (scrollState != OnScrollListener.SCROLL_STATE_FLING
							&& event.imageDownloaded) {

						// GET EVENT PICTURE

						try {
							java.io.FileInputStream in = context
									.openFileInput(event.event_ID);
							bmp = BitmapFactory.decodeStream(in);
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (!(aroundMePictures < eventCollection
							.getAroundMeEventList().size() && context
							.isDiscover())
							&& !event.imageDownloaded) {
						URL img_value = event.imageUri;
						String ID = event.event_ID;
						try {
							bmp = BitmapFactory.decodeStream(img_value
									.openConnection().getInputStream());
							context.saveImageToDisk(ID, bmp);
							event.imageDownloaded = true;
						} catch (Exception e) {
							Log.d("Failed downloadImage showimage",
									e.toString());
						}

					}
				}

				Bitmap[] toReturn = { bmp, imagePage };
				return toReturn;
			}

			@Override
			public void onPostExecute(Bitmap[] value) {
				if (value[0] != null) {
					int first = listViewAroundMe.getFirstVisiblePosition();
					int last = listViewAroundMe.getLastVisiblePosition();
					int current = i;
					if (first <= current && current <= last) {
						try {
							View v = listViewAroundMe.getListChildAt(current
									- first);

							ImageView image = (ImageView) v
									.findViewById(R.id.imageViewList);

							image.setImageBitmap(value[0]);

							v.findViewById(R.id.progressBarImageEventList)
									.setVisibility(View.GONE);

						} catch (Exception e) {
						}

					}

				}

			}

		};
		if (Build.VERSION.SDK_INT >= 11) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
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
			} else {
				localViewHolder = (ViewHolderStar) paramView.getTag();
			}

			paramView.findViewById(R.id.progressBarImagePageList)
					.setVisibility(View.GONE);

			localViewHolder.text.setText(page.name);

			localViewHolder.image.setImageBitmap(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.icon_gray));

			localViewHolder.image.setImageBitmap(context
					.readImageFromDisk(page._ID));

			// if (paramInt <= counter
			// && context.readImageFromDisk(page._ID) != null) {
			// localViewHolder.image.setImageBitmap(context
			// .readImageFromDisk(page._ID));
			// } else {
			// localViewHolder.image.setImageBitmap(BitmapFactory
			// .decodeResource(context.getResources(),
			// R.drawable.icon_other_events));
			// }

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
			if (paramView == null) {
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

				paramView.setTag(localViewHolder);
			} else {
				localViewHolder = (ViewHolderStarPlaces) paramView.getTag();
			}
			paramView.findViewById(R.id.progressBarImagePageList)
					.setVisibility(View.GONE);

			localViewHolder.image.setImageBitmap(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.icon_gray));

			localViewHolder.image.setImageBitmap(context
					.readImageFromDisk(page._ID));

			// if (paramInt <= counter
			// && context.readImageFromDisk(page._ID) != null) {
			// localViewHolder.image.setImageBitmap(context
			// .readImageFromDisk(page._ID));
			// } else {
			// localViewHolder.image.setImageBitmap(BitmapFactory
			// .decodeResource(context.getResources(),
			// R.drawable.icon_other_events));
			// }

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
		eventArrayAdapter.initSections();
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
