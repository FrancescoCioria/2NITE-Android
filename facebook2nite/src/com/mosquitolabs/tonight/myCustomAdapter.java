package com.mosquitolabs.tonight;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
	private Display display;
	private Activity context;

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

	public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
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

			localViewHolder.page = (TextView) paramView
					.findViewById(R.id.textViewPage);
			localViewHolder.image = (ImageView) paramView
					.findViewById(R.id.imageViewList);
			localViewHolder.filterEvents = (TextView) paramView
					.findViewById(R.id.spinnerFilter);
			localViewHolder.filterPages = (TextView) paramView
					.findViewById(R.id.spinnerPages);
			localViewHolder.relativeFilter = (RelativeLayout) paramView
					.findViewById(R.id.LayoutFilter);
			localViewHolder.layout_separator = (LinearLayout) paramView
					.findViewById(R.id.LayoutSeparator);
			localViewHolder.triangle_attending = (ImageView) paramView
					.findViewById(R.id.imageViewTriangleAttending);
			localViewHolder.filterEvents
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							parentActivity.spinnerEvent();
						}
					});
			localViewHolder.filterPages
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							parentActivity.spinnerPage();

						}
					});

			localViewHolder.relativeFilter
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
						}
					});

			paramView.setTag(localViewHolder);
		}

		localViewHolder = (ViewHolder) paramView.getTag();
		/*
		 * if(paramInt%2!=0){
		 * localViewHolder.relative.setBackgroundColor(Color.rgb(135, 206,
		 * 250)); localViewHolder.page.setTextColor(Color.WHITE); }else{
		 * localViewHolder.relative.setBackgroundColor(Color.WHITE);
		 * localViewHolder.page.setTextColor(Color.GRAY); }
		 */

		final EventData event = eventCollection.getEventList().get(paramInt);

		display = context.getWindowManager().getDefaultDisplay();
		
		localViewHolder.logo = (ImageView) paramView
				.findViewById(R.id.imageViewLogoList);

		
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int wwidth = displaymetrics.widthPixels;
		float dp=0;
		 dp=wwidth-(145*displaymetrics.density);
		
		String name = event.name;
		localViewHolder.text.setText(name);
		int i = event.name.length() - 1;
		
		float currentTextWidth = localViewHolder.text.getPaint().measureText(name);
		while (dp<=currentTextWidth && i>=1) {
			name = event.name.substring(0, i) + "...";
			i--;
			currentTextWidth = localViewHolder.text.getPaint().measureText(name);
		}
		
		String lastChar = name.substring(name.length()-4,name.length()-3);
		if(lastChar.equals(" ")){
			name = name.substring(0, name.length()-4)+"...";
		}

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
		try {
			java.io.FileInputStream in = context.openFileInput(event.event_ID);
			Bitmap image = BitmapFactory.decodeStream(in);
			localViewHolder.image.setImageBitmap(image);
		} catch (Exception e) {
			// TODO: handle exception
		}/*
		 * if (!singlePageCollection.getImageList().isEmpty() &&
		 * singlePageCollection.getImageByID(eventCollection
		 * .getEventList().get(paramInt).event_ID) != null) {
		 * localViewHolder.image .setImageBitmap(singlePageCollection
		 * .getImageByID(eventCollection.getEventList().get(
		 * paramInt).event_ID).image);
		 * 
		 * }
		 */

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
		/*
		 * Calendar cal = Calendar.getInstance(); Formatter form = new
		 * Formatter(); form.format("%d-%02d-%02dT%02d:%02d:00",
		 * cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
		 * cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY),
		 * cal.get(Calendar.MINUTE)); String current_time = form.toString();
		 * 
		 * String previousEventDay = ""; String currentEventDay =
		 * eventCollection.getEventList().get(paramInt).startString
		 * .substring(0, 10); if
		 * (eventCollection.getEventList().get(paramInt).startString
		 * .compareTo(current_time) < 0) { currentEventIsInProgress = true; }
		 * 
		 * if (paramInt != 0) { previousEventDay =
		 * eventCollection.getEventList().get(paramInt - 1).startString
		 * .substring(0, 10); if (eventCollection.getEventList().get(paramInt -
		 * 1).startString .compareTo(current_time) < 0) {
		 * previousEventIsInProgress = true; } }
		 */

		if ((paramInt == 0)
				|| (!previousEventDay.equals(currentEventDay) && !currentEventIsInProgress)
				|| (previousEventDay.equals(currentEventDay)
						&& previousEventIsInProgress && !currentEventIsInProgress)) {

			if (currentEventIsInProgress) {
				localViewHolder.separatorDay.setBackgroundColor(Color.rgb(250,
						60, 60));
				// -16401681 azzurro
				localViewHolder.separatorMonth.setText("Now");
				// localViewHolder.separatorMonth.setTextColor(Color.BLACK);
				// localViewHolder.separatorDay.setTextColor(Color.BLACK);
				//
				// localViewHolder.logo.setVisibility(View.VISIBLE);
				// localViewHolder.separatorMonth.setTextColor(Color.rgb(250,
				// 60,
				// 60));

				Bitmap bmp = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.stripes_redd);
				BitmapDrawable background = new BitmapDrawable(bmp);
				background.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
				localViewHolder.separatorDay.setBackgroundDrawable(background);
				localViewHolder.separatorDay.setText("In Progress");
				localViewHolder.separatorDay.setTextColor(Color.WHITE);
			} else {
				localViewHolder.logo.setVisibility(View.GONE);

				localViewHolder.separatorDay
						.setBackgroundResource(R.color.dark_gray);
				// (Color.rgb(251,148, 11)); // verde -16001681
				// localViewHolder.separatorDay.setBackgroundColor(Color.rgb(235,
				// 163, 91));

				Bitmap bmp = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.stripe_darkk);
				BitmapDrawable background = new BitmapDrawable(bmp);
				background.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
				localViewHolder.separatorDay.setBackgroundDrawable(background);

				localViewHolder.separatorMonth.setText(eventCollection
						.getEventList().get(paramInt).dateStart);
				localViewHolder.separatorDay.setText(eventCollection
						.getEventList().get(paramInt).dayStart);
				localViewHolder.separatorDay.setTextColor(Color.DKGRAY);
				localViewHolder.separatorDay.setTextColor(Color.WHITE);// kokokoko
			}
			localViewHolder.layout_separator.setVisibility(View.VISIBLE);
		} else {
			localViewHolder.layout_separator.setVisibility(View.GONE);

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

		/*
		 * if(paramInt%2==0)
		 * localViewHolder.relative.setBackgroundResource(R.layout.gray_item);
		 * else
		 * localViewHolder.relative.setBackgroundResource(R.layout.gradient_item
		 * );
		 */
		// localViewHolder.separatorMonth.setBackgroundColor(Color.WHITE);
		// localViewHolder.separatorMonth.setTextColor(Color.BLACK);
		String status = eventCollection.getEventList().get(paramInt).status_attending;
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

		if ((!parentActivity.filter.equals("declined") && status
				.equals("declined"))
				|| (parentActivity.filter.equals("declined") && !status
						.equals("declined"))) {
			parentActivity.filter();
		}

		return paramView;

	}

	private boolean isPerfectLenght(TextView text, String newText) {
		float textWidth = text.getPaint().measureText(newText);
		float myTextWidth = text.getMeasuredWidth();
		if (textWidth <= myTextWidth) {
			return true;
		} else {
			return false;
		}
	}

	static class ViewHolder {
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
		RelativeLayout relativeFilter;
	}

}
