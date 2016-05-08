package nl.jellow.wimalert;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.innovattic.lib.android.BaseApp;
import com.innovattic.lib.util.Prefs;

/**
 * Created by Jelle on 7-5-2016.
 */
public class App extends BaseApp {

	private static final String TAG = App.class.getSimpleName();

	public static final String PREF_TRACKED_USER_NAME = "tracked-user";
	public static final String PREF_TRACKING_ENABLED = "tracking-enabled";
	public static final String PREF_START_TIME = "start-tracking-time";
	public static final String PREF_END_TIME = "end-tracking-time";

	@Override
	public void onCreate() {
		super.onCreate();
		Prefs.init(this, Context.MODE_PRIVATE);
	}

	public static String getTrackedUserName() {
		return Prefs.get().getString(PREF_TRACKED_USER_NAME, null);
	}

	public static boolean isTrackingEnabled() {
		return Prefs.get().getBoolean(PREF_TRACKING_ENABLED, false);
	}

	public static boolean isNotificationAccessGranted() {
		final Context context = App.getContext();
		final String listeners = Settings.Secure.getString(context.getContentResolver(),
				"enabled_notification_listeners");
		final boolean val = listeners != null && listeners.contains(context.getPackageName());
		Log.i(TAG, "Access to notifications is " + (val ? "" : "not ") + "granted");
		return val;
	}

	public static int getStartTime() {
		return Prefs.get().getInt(PREF_START_TIME, 0);
	}

	public static int getEndTime() {
		return Prefs.get().getInt(PREF_END_TIME, 0);
	}

}
