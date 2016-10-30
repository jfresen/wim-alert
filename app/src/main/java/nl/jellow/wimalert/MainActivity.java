package nl.jellow.wimalert;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import nl.jellow.wimalert.net.Api;
import nl.jellow.wimalert.util.Dialogs;
import nl.jellow.wimalert.util.Prefs;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	@Bind(R.id.buildtime)
	protected TextView mBuildtime;

	@Bind(R.id.grantAccess)
	protected ViewGroup mGrantAccess;

	@Bind(R.id.enableTrackingSubtext)
	protected TextView mTrackingEnabledSubtext;
	@Bind(R.id.enableTrackingSwitch)
	protected Switch mTrackingEnabledSwitch;

	@Bind(R.id.trackedUserSubtext)
	protected TextView mTrackedUserSubtext;

	@Bind(R.id.regexSubtext)
	protected TextView mRegexSubtext;

	@Bind(R.id.startTimeSubtext)
	protected TextView mStartTimeSubtext;

	@Bind(R.id.endTimeSubtext)
	protected TextView mEndTimeSubtext;

	@Bind(R.id.webAddressSubtext)
	protected TextView mWebAddressSubtext;

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mHandler = new Handler(Looper.getMainLooper());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		mBuildtime.setText(getString(R.string.buildtime, BuildConfig.BUILD_TIME));
		fillTrackingEnabledSubtext();
		fillTrackedUserSubtext();
		fillRegexSubtext();
		mTrackingEnabledSwitch.setChecked(App.isTrackingEnabled());
		fillStartTimeSubtext();
		fillEndTimeSubtext();
		fillWebAddressSubtext();
	}

	@Override
	protected void onResume() {
		super.onResume();
		showHideGrantAccess();
	}

	private void showHideGrantAccess() {
		if (mGrantAccess != null) {
			final boolean granted = App.isNotificationAccessGranted();
			mGrantAccess.setVisibility(granted ? View.GONE : View.VISIBLE);
		}
	}

	private void fillTrackedUserSubtext() {
		final String trackedUser = App.getTrackedUserName();
		if (!TextUtils.isEmpty(trackedUser)) {
			final String text = getString(R.string.track_user_setting_text_nonempty, trackedUser);
			mTrackedUserSubtext.setText(text);
		} else {
			mTrackedUserSubtext.setText(R.string.track_user_setting_text_empty);
		}
	}

	private void fillRegexSubtext() {
		final String regex = App.getRegexTrigger();
		if (!TextUtils.isEmpty(regex)) {
			final String text = getString(R.string.track_regex_text_nonempty, regex);
			mRegexSubtext.setText(text);
		} else {
			mRegexSubtext.setText(R.string.track_regex_text_empty);
		}
	}

	private void fillTrackingEnabledSubtext() {
		if (App.isTrackingEnabled()) {
			mTrackingEnabledSubtext.setText(R.string.track_enabled_setting_on);
		} else {
			mTrackingEnabledSubtext.setText(R.string.track_enabled_setting_off);
		}
	}

	private void fillStartTimeSubtext() {
		final int start = App.getStartTime();
		mStartTimeSubtext.setText(formatTimeStamp(start));
	}

	private void fillEndTimeSubtext() {
		final int end = App.getEndTime();
		mEndTimeSubtext.setText(formatTimeStamp(end));
	}

	private void fillWebAddressSubtext() {
		final String webAddress = App.getWebAddress();
		if (!TextUtils.isEmpty(webAddress)) {
			mWebAddressSubtext.setText(webAddress);
		} else {
			mWebAddressSubtext.setText(R.string.settings_web_address_empty);
		}
	}

	private void setTrackedUserName(final CharSequence userName) {
		if (TextUtils.isEmpty(userName)) {
			Log.v(TAG, "Resetting tracked user");
			Prefs.get().remove(App.PREF_TRACKED_USER_NAME);
			return;
		}
		final String trackedUser = userName.toString();
		Log.v(TAG, "Setting tracked user to '" + trackedUser + "'");
		Prefs.get().setString(App.PREF_TRACKED_USER_NAME, trackedUser);
	}

	private void setRegexTrigger(final CharSequence regex) {
		if (TextUtils.isEmpty(regex)) {
			Log.v(TAG, "Resetting regex");
			Prefs.get().remove(App.PREF_REGEX_TRIGGER);
			return;
		}
		final String regexString = regex.toString();
		Log.v(TAG, "Setting regular expression to '" + regexString + "'");
		Prefs.get().setString(App.PREF_REGEX_TRIGGER, regexString);
	}

	private void setTrackingEnabled(final boolean enabled) {
		Log.v(TAG, (enabled ? "En" : "Dis") + "abling tracking");
		Prefs.get().setBoolean(App.PREF_TRACKING_ENABLED, enabled);
	}

	private void setWebAddress(final CharSequence webAddress) {
		if (TextUtils.isEmpty(webAddress)) {
			Log.v(TAG, "Resetting web address");
			Prefs.get().remove(App.PREF_ALARM_WEB_ADDRESS);
			return;
		}
		final String webAddressString = webAddress.toString();
		Log.v(TAG, "Setting web address to " + webAddressString);
		Prefs.get().setString(App.PREF_ALARM_WEB_ADDRESS, webAddressString);
	}

	@OnClick(R.id.setupButton)
	protected void onSetupClicked() {
	}

	@OnClick(R.id.trackedUserSetting)
	protected void onTrackedUserSettingClicked() {
		showTrackedUserDialog();
	}

	@OnCheckedChanged(R.id.enableTrackingSwitch)
	protected void onEnableTracking(final boolean enabled) {
		setTrackingEnabled(enabled);
		fillTrackingEnabledSubtext();
	}

	@OnClick(R.id.enableTracking)
	protected void onEnableTrackingClicked() {
		mTrackingEnabledSwitch.toggle();
	}

	@OnClick(R.id.grantAccess)
	protected void onGrantAccessClicked() {
		final String intentAction;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			intentAction = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
		} else {
			intentAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
		}
		startActivity(new Intent(intentAction));
	}

	@OnClick(R.id.regex)
	protected void onRegexClicked() {
		showRegexDialog();
	}

	@OnClick(R.id.startTimeSetting)
	protected void onStartTimeClicked() {
		final Dialogs.SimpleTimePicker picker = Dialogs.SimpleTimePicker.getInstance(App.PREF_START_TIME);
		picker.show(getSupportFragmentManager(), "start-time-picker");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				final Dialog dialog = picker.getDialog();
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(final DialogInterface dialog) {
						fillStartTimeSubtext();
					}
				});
			}
		});
	}

	@OnClick(R.id.endTimeSetting)
	protected void onEndTimeClicked() {
		final Dialogs.SimpleTimePicker picker = Dialogs.SimpleTimePicker.getInstance(App.PREF_END_TIME);
		picker.show(getSupportFragmentManager(), "end-time-picker");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				final Dialog dialog = picker.getDialog();
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(final DialogInterface dialog) {
						fillEndTimeSubtext();
					}
				});
			}
		});
	}

	private void showTrackedUserDialog() {
		new EnterUserNameDialog().show(getSupportFragmentManager(), "username-dialog");
	}

	private void showRegexDialog() {
		new EnterRegexDialog().show(getSupportFragmentManager(), "regex-dialog");
	}

	private Calendar getUtcCalendar(final long millis) {
		final TimeZone utc = TimeZone.getTimeZone("UTC");
		final Calendar cal = Calendar.getInstance(utc, Locale.US);
		cal.setTimeInMillis(millis);
		return cal;
	}

	private String formatTimeStamp(final int timeInMinutes) {
		final int hours = timeInMinutes / 60;
		final int minutes = timeInMinutes - hours * 60;
		final String format = getString(R.string.settings_time_format);
		return String.format(Locale.US, format, hours, minutes);
	}

	@OnClick(R.id.turnAlarmOn)
	protected void onTurnAlarmOnClicked() {
		Log.e(TAG, "Turning alarm on");
		Api.Send("on");
	}

	@OnClick(R.id.turnAlarmOff)
	protected void onTurnAlarmOffClicked() {
		Log.e(TAG, "Turning alarm off");
		Api.Send("off");
	}

	@OnClick(R.id.webAddressSetting)
	protected void onSetIpAddressClicked() {
		Log.i(TAG, "Setting IP address...");
		new EnterWebAddressDialog().show(getSupportFragmentManager(), "ip-address-dialog");
	}

	public static class EnterUserNameDialog extends Dialogs.TextInputDialog {
		@Override
		public void configureDialog(final AlertDialog.Builder builder) {
			builder.setTitle(R.string.track_user_dialog_title);
		}

		@Override
		public void configureEditText(final EditText editText) {
			final CharSequence title = getText(R.string.track_user_setting_title);
			editText.setContentDescription(title);
			editText.setHint(title);
			editText.setInputType(EditorInfo.TYPE_CLASS_TEXT |
					EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME);
			editText.setText(App.getTrackedUserName());
		}

		@Override
		public void onPositiveButtonClicked(final Editable text) {
			getMainActivity().setTrackedUserName(text);
			getMainActivity().fillTrackedUserSubtext();
		}
	}

	public static class EnterRegexDialog extends Dialogs.TextInputDialog {

		@Override
		public void configureDialog(final AlertDialog.Builder builder) {
			builder.setTitle(R.string.track_regex_title);
		}

		@Override
		public void configureEditText(final EditText editText) {
			final CharSequence title = getText(R.string.track_regex_title);
			editText.setContentDescription(title);
			editText.setHint(title);
			editText.setInputType(EditorInfo.TYPE_CLASS_TEXT);
			editText.setText(App.getRegexTrigger());
		}

		@Override
		public void onPositiveButtonClicked(final Editable text) {
			getMainActivity().setRegexTrigger(text);
			getMainActivity().fillRegexSubtext();
		}

	}

	public static class EnterWebAddressDialog extends Dialogs.TextInputDialog {

		@Override
		public void configureDialog(final AlertDialog.Builder builder) {
			builder.setTitle(R.string.settings_web_address);
		}

		@Override
		public void configureEditText(final EditText editText) {
			final CharSequence title = getText(R.string.settings_web_address_title);
			editText.setContentDescription(title);
			editText.setHint(R.string.settings_web_address_hint);
			editText.setInputType(EditorInfo.TYPE_CLASS_TEXT |
					EditorInfo.TYPE_TEXT_VARIATION_URI);
			editText.setText(App.getWebAddress());
		}

		@Override
		public void onPositiveButtonClicked(final Editable text) {
			getMainActivity().setWebAddress(text);
			getMainActivity().fillWebAddressSubtext();
		}

	}

}
