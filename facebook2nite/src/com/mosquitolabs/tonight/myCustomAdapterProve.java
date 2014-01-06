package com.mosquitolabs.tonight;

import java.util.ArrayList;
import java.util.Calendar;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class myCustomAdapterProve extends BaseAdapter implements
		StickyListHeadersAdapter, SectionIndexer {
	EventCollection eventCollection = EventCollection.getInstance();

	PageCollection pageCollection = PageCollection.getInstance();
	Preferences preferences = Preferences.getInstance();

	private LayoutInflater mInflater;
	// private Display display;
	private Activity context;

	int counter = 0;
	int counterDownloading = 0;

	private Bitmap standardImage = null;
	private Bitmap standardImagePage = null;

	private Bitmap triangleYellow = null;
	private Bitmap triangleRed = null;
	private Bitmap triangleGreen = null;

	private final static int BIG = 0;
	private final static int SMALL = 1;

	private final FacebookeventsActivity parentActivity;

	// private final Context mContext;
	private String[] mCountries;
	private int[] mSectionIndices;
	private String[] mSectionLetters;

	public myCustomAdapterProve(Activity paramContext,
			FacebookeventsActivity activity) {
		this.mInflater = LayoutInflater.from(paramContext);
		context = paramContext;
		parentActivity = activity;
		mSectionIndices = getSectionIndices();
		mSectionLetters = getSectionLetters();

	}

	private int[] getSectionIndices() {

		ArrayList<Integer> sectionIndices = new ArrayList<Integer>();

		Calendar lastCal = Calendar.getInstance();
		Calendar currentCal = Calendar.getInstance();

		lastCal.setTimeInMillis(Long.parseLong(eventCollection.getEventList()
				.get(0).startMillis) * 1000);

		sectionIndices.add(0);

		for (int i = 1; i < eventCollection.getEventList().size(); i++) {
			final EventData currentEvent = eventCollection.getEventList()
					.get(i);
			currentCal
					.setTimeInMillis(Long.parseLong(currentEvent.startMillis) * 1000);

			if (currentCal.get(Calendar.DAY_OF_YEAR) > lastCal
					.get(Calendar.DAY_OF_YEAR) && !currentEvent.isInProgress) {
				lastCal.setTimeInMillis(Long
						.parseLong(currentEvent.startMillis) * 1000);
				sectionIndices.add(i);
			}

		}

		int[] sections = new int[sectionIndices.size()];
		for (int i = 0; i < sectionIndices.size(); i++) {
			sections[i] = sectionIndices.get(i);
		}

		return sections;
	}

	private String[] getSectionLetters() {
		String[] letters = new String[mSectionIndices.length];
		Calendar currentCal = Calendar.getInstance();
		for (int i = 0; i < mSectionIndices.length; i++) {
			EventData event = eventCollection.getEventList().get(
					mSectionIndices[i]);
			currentCal
					.setTimeInMillis(Long.parseLong(event.startMillis) * 1000);
			if (event.isInProgress) {
				letters[i] = "Now";
			} else {
				letters[i] = Integer.toString(currentCal
						.get(Calendar.DAY_OF_MONTH));
			}

		}
		return letters;
	}

	public int getCount() {
		return this.eventCollection.getEventList().size();
	}

	public Object getItem(int paramInt) {
		return Integer.valueOf(paramInt);
	}

	public long getItemId(int paramInt) {
		return paramInt;
	}

	public View getView(final int paramInt, View paramView,
			ViewGroup paramViewGroup) {
		ViewHolder localViewHolder;

		if (paramView == null) {
			switch (parentActivity.getCurrentListStyle()) {
			case BIG:
				paramView = mInflater.inflate(R.layout.list_item_main, null);
				break;
			case SMALL:
				paramView = mInflater.inflate(R.layout.list_item, null);
				break;
			}
			// paramView = mInflater.inflate(R.layout.list_item, null);
			localViewHolder = new ViewHolder();
			localViewHolder.text = (TextView) paramView
					.findViewById(R.id.textViewText);
			localViewHolder.desc = (TextView) paramView
					.findViewById(R.id.textDescription);

			localViewHolder.page = (TextView) paramView
					.findViewById(R.id.textViewPage);
			localViewHolder.attendingCount = (TextView) paramView
					.findViewById(R.id.textViewAttending);
			localViewHolder.image = (ImageView) paramView
					.findViewById(R.id.imageViewList);
			localViewHolder.image_page = (ImageView) paramView
					.findViewById(R.id.imageViewParentPage);
			localViewHolder.filterEvents = (TextView) paramView
					.findViewById(R.id.spinnerFilter);
			localViewHolder.filterPages = (TextView) paramView
					.findViewById(R.id.spinnerPages);
			localViewHolder.filterEventsLayout = (LinearLayout) paramView
					.findViewById(R.id.spinnerFilterLayout);
			localViewHolder.filterPagesLayout = (LinearLayout) paramView
					.findViewById(R.id.spinnerPagesLayout);
			localViewHolder.relativeFilter = (RelativeLayout) paramView
					.findViewById(R.id.LayoutFilter);
			localViewHolder.selector = (View) paramView
					.findViewById(R.id.listItemSelector);

			localViewHolder.triangle_attending = (ImageView) paramView
					.findViewById(R.id.imageViewTriangleAttending);
			localViewHolder.filterEventsLayout
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							parentActivity.spinnerEvent();
						}
					});
			localViewHolder.filterPagesLayout
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							parentActivity.spinnerPage();

						}
					});

			standardImage = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.icon_gray);
			standardImagePage = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.icon_gray);
			triangleGreen = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.triangle_green);
			triangleYellow = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.triangle_yellow);
			triangleRed = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.triangle_red);

			paramView.setTag(localViewHolder);
		}

		localViewHolder = (ViewHolder) paramView.getTag();

		localViewHolder.selector.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parentActivity.listViewMainItemClick(paramInt);
			}
		});

		localViewHolder.selector
				.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						parentActivity.listViewMainItemLongClick(paramInt);
						return true;
					}
				});

		final EventData event = eventCollection.getEventList().get(paramInt);

		String name = event.name;
		localViewHolder.text.setText(name);

		if (event.desc.length() > 0) {
			String desc = event.desc.replaceAll("(?m)^[ \t]*\r?\n", "");

			localViewHolder.desc.setText(desc);
		} else {
			localViewHolder.desc.setText("No description available.");
		}
		if (event.loc.equals("null")) {
			localViewHolder.page.setText("N/A");
		} else {
			localViewHolder.page.setText(event.loc);
		}

		localViewHolder.image.setImageBitmap(standardImage);
		localViewHolder.image_page.setImageBitmap(standardImagePage);

		if (counter <= 3 || parentActivity.isDownloadingImages()) {
			if (parentActivity.isDownloadingImages() && counterDownloading == 0) {
				parentActivity.showImageEventList(paramInt);
				counterDownloading++;
			}
			Bitmap image = null;
			Bitmap imagePage = null;

			try {
				java.io.FileInputStream in = context
						.openFileInput(event.event_ID);
				image = BitmapFactory.decodeStream(in);
				if (image != null) {
					localViewHolder.image.setImageBitmap(image);
				}
			} catch (Exception e) {
			}

			try {
				if (!event.parentPage_ID.equals("1")) {
					java.io.FileInputStream in = context
							.openFileInput(event.parentPage_ID);
					imagePage = BitmapFactory.decodeStream(in);
					localViewHolder.image_page.setImageBitmap(imagePage);
				} else {
					localViewHolder.image_page.setImageBitmap(BitmapFactory
							.decodeResource(context.getResources(),
									R.drawable.icon_other_events));
				}

			} catch (Exception e) {

			}

			counter++;

		} else {
			if (counterDownloading > 0) {
				counterDownloading = 0;
			}
			parentActivity.showImageEventList(paramInt);
		}

		boolean previousEventIsInProgress = false;
		boolean currentEventIsInProgress = false;
		String previousEventDay = "";
		if (paramInt != 0) {
			previousEventIsInProgress = eventCollection.getEventList().get(
					paramInt - 1).isInProgress;
			previousEventDay = eventCollection.getEventList().get(paramInt - 1).dateStart;
		}
		currentEventIsInProgress = event.isInProgress;
		String currentEventDay = event.dateStart;

		// if ((paramInt == 0)
		// || (!previousEventDay.equals(currentEventDay) &&
		// !currentEventIsInProgress)
		// || (previousEventDay.equals(currentEventDay)
		// && previousEventIsInProgress && !currentEventIsInProgress)) {
		//
		// if (currentEventIsInProgress) {
		//
		// // localViewHolder.separatorDay.setTextColor(Color.WHITE);
		// } else {
		//
		// // localViewHolder.separatorDay.setBackgroundDrawable(background);
		//
		// // localViewHolder.separatorDay.setTextColor(Color.WHITE);
		// }
		//
		// // localViewHolder.controlSeparator.setVisibility(View.VISIBLE);
		// } else {
		//
		// // localViewHolder.controlSeparator.setVisibility(View.GONE);
		//
		// }

		if (preferences.getIsSelectedPage()) {
			localViewHolder.filterPages.setText(pageCollection
					.getSelectedPageList().get(0).name);
		} else {
			localViewHolder.filterPages.setText("All Pages");
		}

		if (paramInt == 0) {
			localViewHolder.relativeFilter.setVisibility(View.VISIBLE);
		} else {
			localViewHolder.relativeFilter.setVisibility(View.GONE);
		}

		if (parentActivity.filter.equals("all"))
			localViewHolder.filterEvents.setText("All Events");
		if (parentActivity.filter.equals("going"))
			localViewHolder.filterEvents.setText("Going");
		if (parentActivity.filter.equals("maybe"))
			localViewHolder.filterEvents.setText("Maybe");
		if (parentActivity.filter.equals("declined"))
			localViewHolder.filterEvents.setText("Declined");
		if (parentActivity.filter.equals("not answered"))
			localViewHolder.filterEvents.setText("Not Answered");

		String status = eventCollection.getEventList().get(paramInt).status_attending;
		if (status.equals("attending")) {
			localViewHolder.triangle_attending.setImageBitmap(triangleGreen);
			localViewHolder.triangle_attending.setVisibility(View.VISIBLE);
		}
		if (status.equals("unsure")) {
			localViewHolder.triangle_attending.setImageBitmap(triangleYellow);
			localViewHolder.triangle_attending.setVisibility(View.VISIBLE);
		}
		if (status.equals("declined")) {
			localViewHolder.triangle_attending.setImageBitmap(triangleRed);
			localViewHolder.triangle_attending.setVisibility(View.VISIBLE);
		}
		if (!status.equals("attending") && !status.equals("unsure")
				&& !status.equals("declined")) {
			localViewHolder.triangle_attending.setVisibility(View.GONE);
		}

		if ((!parentActivity.filter.equals("declined") && status
				.equals("declined"))
				|| (parentActivity.filter.equals("declined") && !status
						.equals("declined"))) {
			parentActivity.filter();
		}

		localViewHolder.attendingCount.setText("Going: "
				+ Integer.toString(event.attending_count));

		return paramView;

	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;

		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.header_big_images, parent,
					false);
			holder.layout_separator_top = (RelativeLayout) convertView
					.findViewById(R.id.LayoutSeparatorTop);
			holder.separatorDay = (TextView) convertView
					.findViewById(R.id.textViewSeparatorDay);
			holder.separatorMonth = (TextView) convertView
					.findViewById(R.id.textViewSeparatorMonth);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}

		// set header text as first char in name

		final EventData event = eventCollection.getEventList().get(position);
		if (event.isInProgress) {
			holder.separatorMonth.setText("");
			holder.separatorDay.setText("Right Now");
		} else {
			holder.separatorMonth.setText(event.dateStart);
			holder.separatorDay.setText(event.dayStart);
		}

		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		// return the first character of the country as ID because this is what
		// headers are based upon
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong(eventCollection.getEventList().get(
				position).startMillis));

		return cal.get(Calendar.DAY_OF_YEAR);
	}

	@Override
	public int getPositionForSection(int section) {
		if (section >= mSectionIndices.length) {
			section = mSectionIndices.length - 1;
		} else if (section < 0) {
			section = 0;
		}
		return mSectionIndices[section];
	}

	@Override
	public int getSectionForPosition(int position) {
		for (int i = 0; i < mSectionIndices.length; i++) {
			if (position < mSectionIndices[i]) {
				return i - 1;
			}
		}
		return mSectionIndices.length - 1;
	}

	@Override
	public Object[] getSections() {
		return mSectionLetters;
	}

	public interface StickyListHeadersAdapter extends ListAdapter {
		View getHeaderView(int position, View convertView, ViewGroup parent);

		long getHeaderId(int position);
	}

	public void getImage(final View v, final int i) {
		AsyncTask<Void, Bitmap, Bitmap> task = new AsyncTask<Void, Bitmap, Bitmap>() {

			@Override
			public Bitmap doInBackground(Void... params) {

				return parentActivity.readImageFromDisk(eventCollection
						.getEventList().get(i).event_ID);
			}

			@Override
			protected void onPostExecute(Bitmap bmp) {
				ImageView image = (ImageView) v
						.findViewById(R.id.imageViewPage);
				if (eventCollection.getEventList().size() > i) {
					image.setImageBitmap(bmp);
				}

				super.onPostExecute(null);
			}

		};
		task.execute();
	}

	static class ViewHolder {

		RelativeLayout relativeFilter;

		TextView text;
		TextView desc;
		TextView page;
		TextView filterPages;
		TextView attendingCount;
		LinearLayout filterPagesLayout;
		TextView filterEvents;
		LinearLayout filterEventsLayout;
		ImageView image;
		ImageView triangle_attending;
		ImageView image_page;
		View selector;
	}

	static class HeaderViewHolder {
		RelativeLayout layout_separator_top;
		TextView separatorMonth;
		TextView separatorDay;
	}

}
