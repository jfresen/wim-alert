package nl.jellow.wimalert;

import android.annotation.SuppressLint;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import nl.jellow.wimalert.util.Dialogs;
import nl.jellow.wimalert.util.Mutable;
import nl.jellow.wimalert.util.Prefs;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	@Bind(R.id.buildtime)
	protected TextView mBuildtime;

	@Bind(R.id.grantAccess)
	protected ViewGroup mGrantAccess;

	@Bind(R.id.trackedUserSubtext)
	protected TextView mTrackedUserSubtext;

	@Bind(R.id.enableTrackingSubtext)
	protected TextView mTrackingEnabledSubtext;
	@Bind(R.id.enableTrackingSwitch)
	protected Switch mTrackingEnabledSwitch;

	@Bind(R.id.startTimeSubtext)
	protected TextView mStartTimeSubtext;

	@Bind(R.id.endTimeSubtext)
	protected TextView mEndTimeSubtext;

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mHandler = new Handler(Looper.getMainLooper());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		mBuildtime.setText(BuildConfig.BUILD_TIME);
		fillTrackedUserSubtext();
		fillTrackingEnabledSubtext();
		mTrackingEnabledSwitch.setChecked(App.isTrackingEnabled());
		fillStartTimeSubtext();
		fillEndTimeSubtext();
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

	private void setTrackingEnabled(final boolean enabled) {
		Log.v(TAG, (enabled ? "En" : "Dis") + "abling tracking");
		Prefs.get().setBoolean(App.PREF_TRACKING_ENABLED, enabled);
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
		final Mutable<EditText> inputField = new Mutable<>(null);
		// Create the dialog
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.track_user_dialog_title);
		builder.setNegativeButton(R.string.label_cancel, Dialogs.DISMISS_ON_CLICK);
		builder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final Editable text = inputField.value.getText();
				setTrackedUserName(text);
				fillTrackedUserSubtext();
				dialog.dismiss();
			}
		});
		final AlertDialog dialog = builder.create();

		// Inflate the TextInputLayout in which the username can be entered
		final String trackedUserName = App.getTrackedUserName();
		final LayoutInflater inflater = dialog.getLayoutInflater();
		@SuppressLint("InflateParams")
		final View content = inflater.inflate(R.layout.dialog_input, null);
		inputField.value = (EditText) content.findViewById(R.id.input_text);
		inputField.value.setText(trackedUserName);
		inputField.value.setSelection(trackedUserName == null ? 0 : trackedUserName.length());
		inputField.value.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
				return true;
			}
		});

		dialog.setView(content);
		dialog.show();
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
	}

	@OnClick(R.id.turnAlarmOff)
	protected void onTurnAlarmOffClicked() {
		Log.e(TAG, "Turning alarm off");
	}

}
