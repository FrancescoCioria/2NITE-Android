package com.mosquitolabs.tonight;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyCustomAdapterEventsAroundMe extends BaseAdapter {
	EventCollection eventCollection = EventCollection.getInstance();
	private SharedPreferences mPrefs;

	private LayoutInflater mInflater;
	// private Display display;
	private AroundMeActivity context;

	public MyCustomAdapterEventsAroundMe(AroundMeActivity paramContext) {
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

	public View getView(int paramInt, View paramView,
			ViewGroup paramViewGroup) {
		ViewHolder localViewHolder;

		if (paramView == null) {
			paramView = mInflater.inflate(R.layout.list_item, null);
			localViewHolder = new ViewHolder();
			localViewHolder.text = (TextView) paramView
					.findViewById(R.id.textViewText);
			localViewHolder.desc = (TextView) paramView
					.findViewById(R.id.textDescription);
			localViewHolder.separatorMonth = (TextView) paramView
					.findViewById(R.id.textViewSeparatorMonth);
			localViewHolder.separatorDay = (TextView) paramView
					.findViewById(R.id.textViewSeparatorDay);
			localViewHolder.page = (TextView) paramView
					.findViewById(R.id.textViewPage);
			localViewHolder.image = (ImageView) paramView
					.findViewById(R.id.imageViewList);
			localViewHolder.relative = (RelativeLayout) paramView
					.findViewById(R.id.RealativeListEvent);
			localViewHolder.layout_separator = (LinearLayout) paramView
					.findViewById(R.id.LayoutSeparator);
			localViewHolder.triangle_attending = (ImageView) paramView
					.findViewById(R.id.imageViewTriangleAttending);
			localViewHolder.filterEvents = (TextView) paramView
					.findViewById(R.id.spinnerFilter);
			localViewHolder.filterPages = (TextView) paramView
					.findViewById(R.id.spinnerPages);
			localViewHolder.relativeFilter = (RelativeLayout) paramView
					.findViewById(R.id.LayoutFilter);

			paramView.setTag(localViewHolder);
		}

		localViewHolder = (ViewHolder) paramView.getTag();

		EventData event = eventCollection.getAroundMeEventList().get(
				paramInt);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int wwidth = displaymetrics.widthPixels;
		float dp = 0;
		dp = wwidth - (145 * displaymetrics.density);

		String name = event.name;
		localViewHolder.text.setText(name);
		int i = event.name.length() - 1;

		float currentTextWidth = localViewHolder.text.getPaint()
				.measureText(name);
		while (dp <= currentTextWidth && i >= 1) {
			name = event.name.substring(0, i) + "...";
			i--;
			currentTextWidth = localViewHolder.text.getPaint().measureText(
					name);
		}

		String lastChar = name.substring(name.length() - 4,
				name.length() - 3);
		if (lastChar.equals(" ")) {
			name = name.substring(0, name.length() - 4) + "...";
		}

		localViewHolder.text.setText(name);

		if (event.desc.length() > 0) {
			String desc = event.desc.replaceAll("(?m)^[ \t]*\r?\n", "");

			localViewHolder.desc.setText(desc);
		} else {
			localViewHolder.desc.setText("No description available.");
		}
		localViewHolder.logo = (ImageView) paramView
				.findViewById(R.id.imageViewLogoList);

		// localViewHolder.text.setText(event.name);
		localViewHolder.page.setText(event.parentPageName);
		try {
			java.io.FileInputStream in = context
					.openFileInput(event.event_ID);
			Bitmap image = BitmapFactory.decodeStream(in);
			localViewHolder.image.setImageBitmap(image);
		} catch (Exception e) {
			// TODO: handle exception
		}

		boolean previousEventIsInProgress = false;
		boolean currentEventIsInProgress = false;
		String previousEventDay = "";
		if (paramInt != 0) {
			previousEventIsInProgress = eventCollection
					.getAroundMeEventList().get(paramInt - 1).isInProgress;
			previousEventDay = eventCollection.getAroundMeEventList().get(
					paramInt - 1).dateStart;
		}
		currentEventIsInProgress = event.isInProgress;
		String currentEventDay = event.dateStart;

		if ((paramInt == 0)
				|| (!previousEventDay.equals(currentEventDay) && !currentEventIsInProgress)
				|| (previousEventDay.equals(currentEventDay)
						&& previousEventIsInProgress && !currentEventIsInProgress)) {

			if (currentEventIsInProgress) {
				localViewHolder.separatorDay.setBackgroundColor(Color.rgb(
						250, 60, 60));
				localViewHolder.separatorMonth.setText("Now");

				Bitmap bmp = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.stripes_redd);
				BitmapDrawable background = new BitmapDrawable(bmp);
				background.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
				localViewHolder.separatorDay
						.setBackgroundDrawable(background);
				localViewHolder.separatorDay.setText("In Progress");
				localViewHolder.separatorDay.setTextColor(Color.WHITE);
			} else {
				localViewHolder.logo.setVisibility(View.GONE);

				localViewHolder.separatorDay
						.setBackgroundResource(R.color.dark_gray);

				Bitmap bmp = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.stripe_darkk);
				BitmapDrawable background = new BitmapDrawable(bmp);
				background.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
				localViewHolder.separatorDay
						.setBackgroundDrawable(background);

				localViewHolder.separatorMonth.setText(event.dateStart);
				localViewHolder.separatorDay.setText(event.dayStart);
				localViewHolder.separatorDay.setTextColor(Color.DKGRAY);
				localViewHolder.separatorDay.setTextColor(Color.WHITE);// kokokoko
			}
			localViewHolder.layout_separator.setVisibility(View.VISIBLE);
		} else {
			localViewHolder.layout_separator.setVisibility(View.GONE);

		}

		if (!mPrefs.getBoolean("sort_by_date", true)) {
			localViewHolder.layout_separator.setVisibility(View.GONE);
		}

		String status = event.status_attending;
		if (status.equals("attending")) {
			BitmapDrawable triangle = new BitmapDrawable(
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.triangle_green));
			localViewHolder.triangle_attending.setImageDrawable(triangle);
			localViewHolder.triangle_attending.setVisibility(View.VISIBLE);
		}
		if (status.equals("unsure")) {
			BitmapDrawable triangle = new BitmapDrawable(
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.triangle_yellow));
			localViewHolder.triangle_attending.setImageDrawable(triangle);
			localViewHolder.triangle_attending.setVisibility(View.VISIBLE);
		}
		if (status.equals("declined")) {
			BitmapDrawable triangle = new BitmapDrawable(
					BitmapFactory.decodeResource(context.getResources(),
							R.drawable.triangle_red));
			localViewHolder.triangle_attending.setImageDrawable(triangle);
			localViewHolder.triangle_attending.setVisibility(View.VISIBLE);
		}
		if (!status.equals("attending") && !status.equals("unsure")
				&& !status.equals("declined")) {
			localViewHolder.triangle_attending.setVisibility(View.GONE);
		}

		/*
		 * if (aroundMePictures < jarrayAround.length() && isOneAdapter) {
		 * aroundMePicture(); isOneAdapter = false; }
		 */

		// DISABILITO IL FILTER!!

		localViewHolder.relativeFilter.setVisibility(View.GONE);

		// DA IMPLEMENTARE PRIMA O POI

		return paramView;
	}

	private class ViewHolder {
		LinearLayout layout_separator;
		TextView separatorMonth;
		TextView separatorDay;
		TextView text;
		TextView desc;
		TextView page;
		TextView filterPages;
		TextView filterEvents;
		ImageView image;
		ImageView triangle_attending;
		ImageView logo;
		RelativeLayout relative;
		RelativeLayout relativeFilter;

	}
}

