package google.geofence;

import java.util.HashMap;

public class GeoFenceEvent {
	public final HashMap<String, String> details;
	public final String eventName;

	public GeoFenceEvent(String eventName, HashMap<String, String> details) {
		this.details = details;
		this.eventName = eventName;
	}

	public String getEventName() {
		return eventName;
	}

	public HashMap<String, String> getData() {
		return details;
	}

}
