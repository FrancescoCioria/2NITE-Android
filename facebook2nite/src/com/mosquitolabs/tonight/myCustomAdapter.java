package com.mosquitolabs.tonight;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class myCustomAdapter extends BaseAdapter {
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

	private FacebookeventsActivity parentActivity;

	public myCustomAdapter(Activity paramContext,
			FacebookeventsActivity activity) {
		this.mInflater = LayoutInflater.from(paramContext);
		context = paramContext;
		parentActivity = activity;
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
			paramView = mInflater.inflate(R.layout.list_item_temp2, null);
			localViewHolder = new ViewHolder();
			localViewHolder.text = (TextView) paramView
					.findViewById(R.id.textViewText);
			localViewHolder.desc = (TextView) paramView
					.findViewById(R.id.textDescription);
			localViewHolder.separatorMonth = (TextView) paramView
					.findViewById(R.id.textViewSeparatorMonth);
			localViewHolder.separatorDay = (TextView) paramView
					.findViewById(R.id.textViewSeparatorDay);
			localViewHolder.separatorMonthBottom = (TextView) paramView
					.findViewById(R.id.textViewSeparatorMonthBottom);
			localViewHolder.separatorDayBottom = (TextView) paramView
					.findViewById(R.id.textViewSeparatorDayBottom);
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
			localViewHolder.layout_separator_top = (RelativeLayout) paramView
					.findViewById(R.id.LayoutSeparatorTop);
			localViewHolder.layout_separator_bottom = (RelativeLayout) paramView
					.findViewById(R.id.LayoutSeparatorBottom);
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
					context.getResources(),  R.drawable.icon_gray);
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
				}else{
					localViewHolder.image_page.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_other_events));
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

		if ((paramInt == 0)
				|| (!previousEventDay.equals(currentEventDay) && !currentEventIsInProgress)
				|| (previousEventDay.equals(currentEventDay)
						&& previousEventIsInProgress && !currentEventIsInProgress)) {

			if (currentEventIsInProgress) {

				Bitmap bmp = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.stripes_redd);
				BitmapDrawable background = new BitmapDrawable(
						context.getResources(), bmp);
				background.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
				// localViewHolder.separatorDay.setBackgroundDrawable(background);
				localViewHolder.separatorMonth.setText("");
				localViewHolder.separatorDay.setText("Right Now");

				// localViewHolder.separatorDay.setTextColor(Color.WHITE);
			} else {

				Bitmap bmp = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.stripe_darkk);
				BitmapDrawable background = new BitmapDrawable(
						context.getResources(), bmp);
				background.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
				// localViewHolder.separatorDay.setBackgroundDrawable(background);

				localViewHolder.separatorMonth.setText(eventCollection
						.getEventList().get(paramInt).dateStart);
				localViewHolder.separatorDay.setText(eventCollection
						.getEventList().get(paramInt).dayStart);

				// localViewHolder.separatorDay.setTextColor(Color.WHITE);
			}
			localViewHolder.layout_separator_top.setVisibility(View.VISIBLE);
			localViewHolder.layout_separator_top.setEnabled(true);

			// localViewHolder.controlSeparator.setVisibility(View.VISIBLE);
		} else {
			localViewHolder.layout_separator_top.setVisibility(View.GONE);
			localViewHolder.layout_separator_top.setEnabled(false);

			// localViewHolder.controlSeparator.setVisibility(View.GONE);

		}
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

		localViewHolder.layout_separator_bottom.setVisibility(View.GONE);

		if (event.isInProgress) {
			localViewHolder.separatorMonthBottom.setText("");
			localViewHolder.separatorDayBottom.setText("Right Now");
		} else {
			localViewHolder.separatorMonthBottom.setText(event.dateStart);
			localViewHolder.separatorDayBottom.setText(event.dayStart);
		}

		localViewHolder.attendingCount.setText("Going: "
				+ Integer.toString(event.attending_count));

		// localViewHolder.layout_separator.isShown()

		// parentActivity.isFirstView(paramInt, paramView);

		return paramView;

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
		RelativeLayout layout_separator_top;
		RelativeLayout layout_separator_bottom;
		RelativeLayout relativeFilter;
		TextView separatorMonth;
		TextView separatorDay;
		TextView separatorMonthBottom;
		TextView separatorDayBottom;
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

}
