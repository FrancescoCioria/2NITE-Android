package com.mosquitolabs.tonight;

import java.util.ArrayList;
import java.util.Calendar;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class MyCustomAdapterEventsAroundMe extends BaseAdapter implements
		StickyListHeadersAdapter, SectionIndexer {
	EventCollection eventCollection = EventCollection.getInstance();

	PageCollection pageCollection = PageCollection.getInstance();
	Preferences preferences = Preferences.getInstance();

	private LayoutInflater mInflater;
	private DiscoverActivity context;

	int counter = 0;
	int counterDownloading = 0;

	private Bitmap standardImage = null;
	private Bitmap standardImagePage = null;

	private Bitmap triangleYellow = null;
	private Bitmap triangleRed = null;
	private Bitmap triangleGreen = null;

	private final static int BIG = 0;
	private final static int SMALL = 1;

	private final DiscoverActivity parentActivity;

	private int[] mSectionIndices;
	private String[] mSectionLetters;

	private int descMaxLength = 0;

	public MyCustomAdapterEventsAroundMe(DiscoverActivity paramContext) {
		this.mInflater = LayoutInflater.from(paramContext);
		context = paramContext;
		parentActivity = paramContext;

	}

	public void initSections() {
		mSectionIndices = getSectionIndices();
		mSectionLetters = getSectionLetters();
	}

	private int[] getSectionIndices() {

		if (!eventCollection.getAroundMeEventList().isEmpty()) {
			ArrayList<Integer> sectionIndices = new ArrayList<Integer>();

			Calendar lastCal = Calendar.getInstance();
			Calendar currentCal = Calendar.getInstance();

			lastCal.setTimeInMillis(Long.parseLong(eventCollection
					.getAroundMeEventList().get(0).startMillis) * 1000);

			sectionIndices.add(0);

			for (int i = 1; i < eventCollection.getAroundMeEventList().size(); i++) {
				final EventData currentEvent = eventCollection
						.getAroundMeEventList().get(i);
				currentCal.setTimeInMillis(Long
						.parseLong(currentEvent.startMillis) * 1000);

				if ((currentCal.get(Calendar.DAY_OF_YEAR) > lastCal
						.get(Calendar.DAY_OF_YEAR) || currentCal
						.get(Calendar.YEAR) > lastCal.get(Calendar.YEAR))
						&& !currentEvent.isInProgress) {
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
		
		return new int[0];
	}

	private String[] getSectionLetters() {
		String[] letters = new String[mSectionIndices.length];
		Calendar currentCal = Calendar.getInstance();
		for (int i = 0; i < mSectionIndices.length; i++) {
			EventData event = eventCollection.getAroundMeEventList().get(
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
		return this.eventCollection.getAroundMeEventList().size();
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
			DisplayMetrics displaymetrics = new DisplayMetrics();
			parentActivity.getWindowManager().getDefaultDisplay()
					.getMetrics(displaymetrics);
			int width = displaymetrics.widthPixels;
			int density = displaymetrics.densityDpi;
			int space;
			switch (parentActivity.getCurrentListStyle()) {
			case BIG:
				paramView = mInflater.inflate(R.layout.list_item_big_images,
						null);

				space = width - 30 * 160 / density;
				descMaxLength = (space * 160 / density) / 5;
				descMaxLength *= 3;

				break;
			case SMALL:
				paramView = mInflater.inflate(R.layout.list_item_small_images,
						null);

				space = width - 165 * 160 / density;
				descMaxLength = (space * 160 / density) / 5;
				descMaxLength *= 4;

				break;
			}

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
			localViewHolder.selector = (View) paramView
					.findViewById(R.id.listItemSelector);
			localViewHolder.triangle_attending = (ImageView) paramView
					.findViewById(R.id.imageViewTriangleAttending);
			localViewHolder.progressBar = (ProgressBar) paramView
					.findViewById(R.id.progressBarImageEventList);

			standardImage = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.icon_gray);

			triangleGreen = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.triangle_green);
			triangleYellow = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.triangle_yellow);
			triangleRed = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.triangle_red);
			initSections();

			paramView.setTag(localViewHolder);
		} else {
			localViewHolder = (ViewHolder) paramView.getTag();
		}

		localViewHolder.selector.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				context.listAroundItemClick(paramInt);
			}
		});

		final EventData event = eventCollection.getAroundMeEventList().get(
				paramInt);

		String name = event.name;
		localViewHolder.text.setText(name);

		if (event.desc.length() > 0) {

			String desc = event.desc;

			int d = descMaxLength * 2;

			try {
				desc = event.desc.substring(0, d);

			} catch (Exception e) {
			}
			desc = desc.replaceAll("(?m)^[ \t]*\r?\n", "");

			String lines[] = desc.split("\\r?\\n");
			if (lines.length > 4) {
				desc = "";
				for (int q = 0; q < Math.min(lines.length, 4); q++) {
					desc += lines[q] + "\n";
				}
			}

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
		localViewHolder.progressBar.setVisibility(View.VISIBLE);

		parentActivity.showImageEventList(paramInt);

		// Bitmap image = null;
		//
		// try {
		// java.io.FileInputStream in = context.openFileInput(event.event_ID);
		// image = BitmapFactory.decodeStream(in);
		// if (image != null) {
		// localViewHolder.image.setImageBitmap(image);
		// }
		// } catch (Exception e) {
		// }

		localViewHolder.image_page.setVisibility(View.GONE);

		String status = event.status_attending;
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

		localViewHolder.attendingCount.setText("Going: "
				+ Integer.toString(event.attending_count));

		// DISABILITO IL FILTER!!

		return paramView;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;

		if (convertView == null) {
			holder = new HeaderViewHolder();
			switch (parentActivity.getCurrentListStyle()) {
			case BIG:
				convertView = mInflater.inflate(R.layout.header_big_images,
						parent, false);
				break;
			case SMALL:
				convertView = mInflater.inflate(R.layout.header_small_images,
						parent, false);
				break;
			}

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

		final EventData event = eventCollection.getAroundMeEventList().get(
				position);
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

		for (int i = 0; i < mSectionIndices.length; i++) {
			if (position < mSectionIndices[i]) {
				return i - 1;
			}
		}

		return mSectionIndices.length - 1;
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

	private class ViewHolder {
		TextView text;
		TextView desc;
		TextView page;
		TextView attendingCount;

		ImageView image;
		ImageView triangle_attending;
		ImageView image_page;

		ProgressBar progressBar;

		View selector;
	}

	static class HeaderViewHolder {
		RelativeLayout layout_separator_top;
		TextView separatorMonth;
		TextView separatorDay;
	}

}
