package com.mosquitolabs.tonight;

import java.io.Serializable;

public class EventData implements Serializable {
	private static final long serialVersionUID = 1L;
	public String dateStart;
	public String dayStart;
	public String timeStart;
	public String dayEnd;
	public String dateEnd;
	public String startMillis;
	public String endMillis;
	public String last_update;
	public String name;
	public String parentPageName;
	public String parentPage_ID;
	public String event_ID;
	public String timeEnd;
	public String desc;
	public String loc;
	public String venue = "";
	public String status_attending;
	public int attending_count;

	public boolean isInProgress = false;
	public boolean hasAnEnd = true;
	public boolean hasCover = false;
	public boolean unix = false;

	public String toString() {
		return this.name;
	}
}
