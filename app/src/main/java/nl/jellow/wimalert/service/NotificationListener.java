package nl.jellow.wimalert.service;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import nl.jellow.wimalert.App;
import nl.jellow.wimalert.ViewHierarchy;

/**
 * Created by Jelle on 7-5-2016.
 */
public class NotificationListener extends NotificationListenerService {

	private static final String TAG = NotificationListener.class.getSimpleName();

	@Override
	public void onNotificationPosted(final StatusBarNotification sbn) {
		super.onNotificationPosted(sbn);
		if (!isActive()) {
			return;
		}
		if (!"com.whatsapp".equals(sbn.getPackageName())) {
			return;
		}
		final String needle = App.getTrackedUserName();
		if (needle == null) {
			return;
		}
		final Notification notification = sbn.getNotification();
		if (notification.bigContentView == null) {
			return;
		}
		final LinearLayout ll = new LinearLayout(this);
		final View root = notification.bigContentView.apply(this, ll);
		if (root == null) {
			return;
		}
		Log.i(TAG, "=== WHATSAPP MESSAGE ===");
		final View titleView = root.findViewById(android.R.id.title);
		boolean multipleMessages = false;
		if (titleView instanceof TextView) {
			final CharSequence title = ((TextView) titleView).getText();
			if (needle.equals(title)) {
				// Ring the alarm!!!!
				Log.i(TAG, "Found tracked username in title view");
				fireAlarm();
				return;
			}
		}
		// now traverse the view hierarchy and check all textviews
		ViewHierarchy.traverse(root, new ViewHierarchy.ViewCallback<TextView>(TextView.class) {
			@Override
			public boolean doWithView(@NonNull TextView view) {
				final CharSequence text = view.getText();
				if (text == null) {
					return true;
				}
				final String name = text.toString().split(":")[0];
				if (needle.equals(name)) {
					// Ring the alarm!!!!
					Log.i(TAG, "Found tracked username in other text view");
					fireAlarm();
					return false;
				}
				return true;
			}
		});
	}

	@Override
	public void onNotificationRemoved(final StatusBarNotification sbn) {
		super.onNotificationRemoved(sbn);
	}

	private boolean isActive() {
		if (!App.isTrackingEnabled()) {
			return false;
		}

		final Calendar calendar = Calendar.getInstance();
		final int currHour = calendar.get(Calendar.HOUR_OF_DAY);
		final int currMinute = calendar.get(Calendar.MINUTE);
		final int currTime = currHour * 60 + currMinute;

		final int startTime = App.getStartTime();
		final int endTime = App.getEndTime();

		if (startTime == endTime) {
			return true;
		} else if (startTime < endTime) {
			return startTime <= currTime && currTime <= endTime;
		} else {
			return startTime <= currTime || currTime <= endTime;
		}
	}

	private void fireAlarm() {
		Log.e(TAG, "ALARM ON!!!! " + App.getTrackedUserName() + " sent a message!");
	}

	private void turnOffAlarm() {
		Log.i(TAG, "ALARM OFF");
	}

}
