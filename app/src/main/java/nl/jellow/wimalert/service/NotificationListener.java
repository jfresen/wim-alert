package nl.jellow.wimalert.service;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.regex.Pattern;

import nl.jellow.wimalert.App;
import nl.jellow.wimalert.R;
import nl.jellow.wimalert.net.Api;
import nl.jellow.wimalert.util.ViewHierarchy;

/**
 * Created by Jelle on 7-5-2016.
 */
public class NotificationListener extends NotificationListenerService {

	private static final String TAG = NotificationListener.class.getSimpleName();

	private Pattern mRegex;

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
		final String regexString = App.getRegexTrigger();
		if (TextUtils.isEmpty(regexString)) {
			mRegex = null;
		} else if (mRegex == null || !mRegex.pattern().equals(regexString)) {
			mRegex = Pattern.compile(regexString);
		}

		Log.i(TAG, "=== WHATSAPP MESSAGE ===");
//		Debug.logD(TAG, ViewHierarchy.dumpViewHierarchy(root, this));
		final View titleView = root.findViewById(android.R.id.title);
		if (titleView instanceof TextView) {
			final CharSequence title = ((TextView) titleView).getText();
			if (needle.equals(title)) {
				Log.i(TAG, "Found tracked username in title view");
				if (mRegex != null) {
					// Still need to find a message matching the regex
					ViewHierarchy.traverse(root, new ViewHierarchy.ViewCallback<TextView>(TextView.class) {
						@Override
						public boolean doWithView(@NonNull TextView view) {
							if (messageMatchesRegex(view)) {
								Log.i(TAG, "Found matching message in a text view with text " + view.getText());
								fireAlarm();
								return false;
							} else {
								Log.v(TAG, "No match: " + view.getText());
							}
							return true;
						}
					});
				} else {
					// Ring the alarm!!!!
					Log.i(TAG, "No regex given, accept the name match");
					fireAlarm();
				}
				return;
			}
		}
		// now traverse the view hierarchy and check all textviews
		ViewHierarchy.traverse(root, new ViewHierarchy.ViewCallback<TextView>(TextView.class) {
			@Override
			public boolean doWithView(@NonNull TextView view) {
				if (!isMessageView(view)) {
					return true;
				}
				final CharSequence text = view.getText();
				if (text == null) {
					return true;
				}
				final String[] split = text.toString().split(":", 2);
				final String name = split[0];
				if (needle.equals(name)) {
					Log.i(TAG, "Found tracked username in other text view");
					if (mRegex != null) {
						if (split.length > 0 && matchesRegex(split[1])) {
							Log.i(TAG, "Found matching message in a text view with text " + split[1]);
							fireAlarm();
							return false;
						} else {
							final String msg = split.length > 0 ? split[1] : text.toString();
							Log.v(TAG, "No match: " + msg);
						}
						return true;
					} else {
						// Ring the alarm!!!!
						Log.i(TAG, "No regex given, accept the name match");
						fireAlarm();
						return false;
					}
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

	private boolean messageMatchesRegex(@NonNull final TextView view) {
		// Skip the title view with the person's name
		final int id = view.getId();
		if (id == android.R.id.title || id == R.id.action0) {
			return false;
		}
		return isMessageView(view) && matchesRegex(view.getText());
	}

	private boolean matchesRegex(final CharSequence text) {
		if (text == null) {
			return mRegex == null;
		}
		if (mRegex != null) {
			return mRegex.matcher(text).find();
		}
		return true;
	}

	private boolean isMessageView(@NonNull final TextView view) {
		final int id = view.getId();
		if (id == android.R.id.title ||
				id == R.id.action0 ||
				id == R.id.text) {
			return false;
		}
		return view.getVisibility() == View.VISIBLE;
	}

	private void fireAlarm() {
		Log.e(TAG, "ALARM ON!!!! " + App.getTrackedUserName() + " sent a message!");
		Api.Send("on");
	}

	private void turnOffAlarm() {
		Log.i(TAG, "ALARM OFF");
		Api.Send("off");
	}

}
