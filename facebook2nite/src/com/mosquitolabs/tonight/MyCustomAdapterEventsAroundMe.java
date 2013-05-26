package com.mosquitolabs.tonight;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyCustomAdapterEventsAroundMe extends BaseAdapter {
	EventCollection eventCollection = EventCollection.getInstance();
	private SharedPreferences mPrefs;

	private LayoutInflater mInflater;
	// private Display display;
	private DiscoverActivity context;

	private Bitmap standardImage = null;
	private Bitmap standardImagePage = null;

	private Bitmap triangleYellow = null;
	private Bitmap triangleRed = null;
	private Bitmap triangleGreen = null;

	public MyCustomAdapterEventsAroundMe(DiscoverActivity paramContext) {
		this.mInflater = LayoutInflater.from(paramContext);
		context = paramContext;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

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

	public View getView(final int paramInt, View paramView, ViewGroup paramViewGroup) {
		ViewHolder localViewHolder;

		if (paramView == null) {
			switch (context.getCurrentListStyle()) {
			case 0:
				paramView = mInflater.inflate(R.layout.list_item_main, null);
				break;
			case 1:
				paramView = mInflater.inflate(R.layout.list_item, null);
				break;
			}
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

			standardImage = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.standard_image);
			
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
				context.listAroundItemClick(paramInt);
			}
		});

		
		final EventData event = eventCollection.getAroundMeEventList().get(paramInt);

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
		

		Bitmap image = null;
		Bitmap imagePage = null;

		try {
			java.io.FileInputStream in = context.openFileInput(event.event_ID);
			image = BitmapFactory.decodeStream(in);
			if (image != null) {
				localViewHolder.image.setImageBitmap(image);
			}
		} catch (Exception e) {
		}

		/*try {
			if (!event.parentPage_ID.equals("1")) {
				java.io.FileInputStream in = context
						.openFileInput(event.parentPage_ID);
				imagePage = BitmapFactory.decodeStream(in);
				localViewHolder.image_page.setImageBitmap(imagePage);
			}

		} catch (Exception e) {

		}*/
		
		localViewHolder.image_page.setVisibility(View.GONE);

		boolean previousEventIsInProgress = false;
		boolean currentEventIsInProgress = false;
		String previousEventDay = "";
		if (paramInt != 0) {
			previousEventIsInProgress = eventCollection.getAroundMeEventList().get(
					paramInt - 1).isInProgress;
			previousEventDay = eventCollection.getAroundMeEventList().get(paramInt - 1).dateStart;
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

				localViewHolder.separatorMonth.setText(event.dateStart);
				localViewHolder.separatorDay.setText(event.dayStart);

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

		if (paramInt == 0) {
			localViewHolder.relativeFilter.setVisibility(View.VISIBLE);
		} else {
			localViewHolder.relativeFilter.setVisibility(View.GONE);
		}

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

		// DISABILITO IL FILTER!!

		localViewHolder.relativeFilter.setVisibility(View.GONE);
		

		return paramView;
	}

	private class ViewHolder {
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
		TextView attendingCount;

		ImageView image;
		ImageView triangle_attending;
		ImageView image_page;
		View selector;

	}
}
