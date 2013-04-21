package com.mosquitolabs.tonight;

import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;

public class UserLikesPagerAdapter extends PagerAdapter {
	private static final int USER_LIKES = 0;
	private static final int PLACES = 1;

	private Session session;

	private Button buttonLogin;
	private Button buttonContinue;

	private ListView listUserLike;
	private ListView listPlaces;

	private int userLikesInt = 0;
	private int placesInt = 0;

	private final FacebookeventsActivity context;
	private View v;

	private boolean likesSorted = false;
	private boolean aroundSorted = false;

	private PageCollection pageCollection = PageCollection.getInstance();
	private Preferences preferences = Preferences.getInstance();

	private myCustomAdapterPlaces customAdapterPlaces;
	private myCustomAdapterUserLikes customAdapterUserLikes;

	private LinearLayout progressLoginUserLikes;
	private LinearLayout progressLoginPlaces;

	private TextView noPagesUserLikes;
	private TextView noPagesPlaces;

	public UserLikesPagerAdapter(FacebookeventsActivity context) {
		this.context = context;
		// session = Session.getActiveSession();
	}

	@Override
	public int getCount() {
		return (5);
	}

	@Override
	public Object instantiateItem(View pager, int position) {
		LayoutInflater inflater = (LayoutInflater) pager.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		v = null;

		switch (position) {

		case USER_LIKES:
			v = inflater.inflate(R.layout.pages_i_like, null);
			noPagesUserLikes = (TextView) v
					.findViewById(R.id.textViewNoPagesUserLikes);
			progressLoginUserLikes = (LinearLayout) v
					.findViewById(R.id.linearLayoutPagesILikeProgress);
			listUserLike = (ListView) v.findViewById(R.id.listViewPagesILike);
			customAdapterUserLikes = new myCustomAdapterUserLikes(context);
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

							} else {
								pageCollection
										.removePageFromFavourites(pageCollection
												.getPageSearchList().get(i));
								preferences.setModifiedPages(true);
								preferences.setModifiedSinglePage(true);
							}
						}
					});

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

							} else {
								pageCollection
										.removePageFromFavourites(pageCollection
												.getPageAroundMe().get(i));
								preferences.setModifiedPages(true);
								preferences.setModifiedSinglePage(true);
							}
						}
					});

			break;

		}
		((CustomViewPager) pager).addView(v, 0);
		return v;
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
						JSONArray jarrayLikes = context.getJArrayUserLikes();

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
				userLikesInt++;
				return i;
			}

			@Override
			public void onPostExecute(Integer i) {
				if (i < pageCollection.getPageSearchList().size()) {
					{
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
					if (i < pageCollection.getPageSearchList().size()) {
						getUserLikesImages(userLikesInt);
					}
				}
			}
		};
		task.execute();
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
			paramView = mInflater.inflate(R.layout.list_pages, null);
			localViewHolder = new ViewHolderStar();
			localViewHolder.text = (TextView) paramView
					.findViewById(R.id.textViewListPages);
			localViewHolder.text_fan = (TextView) paramView
					.findViewById(R.id.textViewListPagesFanCount);
			localViewHolder.star = (ImageView) paramView
					.findViewById(R.id.imageViewStar);
			localViewHolder.image = (ImageView) paramView
					.findViewById(R.id.imageViewPage);
			localViewHolder.text.setText(page.name);

			int counter = userLikesInt;

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
			localViewHolder.image
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

	static class ViewHolderStar {
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
			localViewHolder.image
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
		ImageView star;
		ImageView image;
		TextView text;
		TextView text_fan;
	}

	public void refreshUserLikesAdapter() {
		customAdapterUserLikes.notifyDataSetChanged();
	}

	public void refreshPlacesAdapter() {
		customAdapterPlaces.notifyDataSetChanged();
	}

	public void initializeUserLikes() {
		if (!likesSorted) {
			pageCollection.sortSearchByName();
			likesSorted = true;
		}
		listUserLike.setVisibility(View.VISIBLE);
		progressLoginUserLikes.setVisibility(View.GONE);
		refreshUserLikesAdapter();
		if (pageCollection.getPageSearchList().isEmpty()) {
			noPagesUserLikes
					.setText("Sorry, couldn't find any place to suggest you based on your likes.");
			noPagesUserLikes.setVisibility(View.VISIBLE);
		} else {
			noPagesUserLikes.setVisibility(View.GONE);

		}
	}

	public void initializePlaces() {
		if (!aroundSorted) {
			pageCollection.sortSearchByLikesAroundMeActivity();
			aroundSorted = true;
		}
		refreshPlacesAdapter();
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
	
	public void setProgressPlacesVisible(boolean b){
		if(b){
			progressLoginPlaces.setVisibility(View.VISIBLE);
		}else{
			progressLoginPlaces.setVisibility(View.GONE);

		}
	}
	public void setProgressUserLikesVisible(boolean b){
		if(b){
			progressLoginUserLikes.setVisibility(View.VISIBLE);
		}else{
			progressLoginUserLikes.setVisibility(View.GONE);
			
		}
	}

}
