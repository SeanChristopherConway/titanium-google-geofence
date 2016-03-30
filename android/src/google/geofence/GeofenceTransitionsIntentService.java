/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package google.geofence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;

import de.greenrobot.event.EventBus;

/**
 * Listener for geofence transition changes.
 * 
 * Receives geofence transition events from Location Services in the form of an
 * Intent containing the transition type and geofence id(s) that triggered the
 * transition. Creates a notification as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

	protected static final String TAG = "geofence-transitions-service";
	Gson gson = new Gson();
	private KrollModule proxy;

	/**
	 * This constructor is required, and calls the super IntentService(String)
	 * constructor with the name for a worker thread.
	 */
	public GeofenceTransitionsIntentService() {
		// Use the TAG to name the worker thread.
		super(TAG);

	}

	@Override
	public void onCreate() {
		super.onCreate();

		System.out.println("Creating geofence transition service");
	}

	@Override
	public void onDestroy() {
		System.out.println("Destroying geofence transition service");
	}

	/**
	 * Handles incoming intents.
	 * 
	 * @param intent
	 *            sent by Location Services. This Intent is provided to Location
	 *            Services (inside a PendingIntent) when addGeofences() is
	 *            called.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		HashMap<String, String> event = new HashMap<String, String>();
		if (geofencingEvent.hasError()) {

			try {
				String errorMessage = GeofenceErrorMessages.getErrorString(
						this, geofencingEvent.getErrorCode());

				event.put("error", errorMessage);
				event.put("errorcode",
						Integer.toString(geofencingEvent.getErrorCode()));
				if (geofencingEvent.getTriggeringGeofences() != null) {
					event.put("regions", geofencingEvent.getTriggeringGeofences().toString());
				}

			} catch (ResourceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				triggerEvent("error", event);
			} catch (Error err) {
				System.out
						.println("Error triggering proxy fire method: " + err);
			}
			// Log.e(TAG, errorMessage);
			System.out.println("Handle intent geofence error is: "
					+ geofencingEvent.getErrorCode());
			return;
		}

		// Get the transition type.
		int geofenceTransition = geofencingEvent.getGeofenceTransition();
		String eventName = null;
		// Test that the reported transition was of interest.
		if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
				|| geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

			if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
				eventName = "enterregions";
			}
			if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
				eventName = "exitregions";
			}
			// Get the geofences that were triggered. A single event can trigger
			// multiple geofences.
			List<Geofence> triggeringGeofences = geofencingEvent
					.getTriggeringGeofences();
			event.put(
					"regions",
					getGeofenceTransitionDetails(this, geofenceTransition,
							triggeringGeofences).toString());
			try {
				triggerEvent(eventName, event);
			} catch (Error err) {
				System.out
						.println("Error triggering proxy fire method: " + err);
			}

		} else {
			// Log the error.
			try {
				Log.e(TAG,
						getString(
								TiRHelper
										.getApplicationResource("string.geofence_transition_invalid_type"),
								geofenceTransition));
				
				
			} catch (ResourceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Invalid geofence transition type");
		}
	}

	@Kroll.method
	public void triggerEvent(String eventName, HashMap<String, String> event) {

		GeoFenceEvent geoEvent = new GeoFenceEvent(eventName, event);
		EventBus.getDefault().post(geoEvent);

	}

	/**
	 * Gets transition details and returns them as a formatted string.
	 * 
	 * @param context
	 *            The app context.
	 * @param geofenceTransition
	 *            The ID of the geofence transition.
	 * @param triggeringGeofences
	 *            The geofence(s) triggered.
	 * @return The transition details formatted as String.
	 */
	private ArrayList<String> getGeofenceTransitionDetails(Context context,
			int geofenceTransition, List<Geofence> triggeringGeofences) {

		String geofenceTransitionString = null;
		try {
			geofenceTransitionString = getTransitionString(geofenceTransition);
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Get the Ids of each geofence that was triggered.
		ArrayList<String> triggeringGeofencesIdsList = new ArrayList<String>();
		for (Geofence geofence : triggeringGeofences) {
			HashMap<String, String> event = new HashMap<String, String>();
			event.put("identifier", geofence.getRequestId());
			triggeringGeofencesIdsList.add(gson.toJson(event));
		}

		return triggeringGeofencesIdsList;
	}

	/**
	 * Maps geofence transition types to their human-readable equivalents.
	 * 
	 * @param transitionType
	 *            A transition type constant defined in Geofence
	 * @return A String indicating the type of transition
	 * @throws ResourceNotFoundException
	 */
	private String getTransitionString(int transitionType)
			throws ResourceNotFoundException {
		switch (transitionType) {
		case Geofence.GEOFENCE_TRANSITION_ENTER:
			return getString(TiRHelper
					.getApplicationResource("string.geofence_transition_entered"));
		case Geofence.GEOFENCE_TRANSITION_EXIT:
			return getString(TiRHelper
					.getApplicationResource("string.geofence_transition_exited"));
		default:
			return getString(TiRHelper
					.getApplicationResource("string.unknown_geofence_transition"));
		}
	}
}