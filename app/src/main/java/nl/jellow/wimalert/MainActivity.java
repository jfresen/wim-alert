package nl.jellow.wimalert;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.innovattic.lib.android.BaseAppCompatActivity;
import com.innovattic.lib.util.Mutable;
import com.innovattic.lib.util.Prefs;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseAppCompatActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	@Bind(R.id.buildtime)
	protected TextView mBuildtime;

	@Bind(R.id.trackedUserSubtext)
	protected TextView mTrackedUserSubtext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		mBuildtime.setText(BuildConfig.BUILD_TIME);
		fillTrackedUserSubtext();
	}

	private void fillTrackedUserSubtext() {
		final String trackedUser = Prefs.get().getString(App.PREF_TRACKED_USER_NAME, null);
		if (!TextUtils.isEmpty(trackedUser)) {
			final String text = getString(R.string.track_user_setting_text_nonempty, trackedUser);
			mTrackedUserSubtext.setText(text);
		} else {
			mTrackedUserSubtext.setText(R.string.track_user_setting_text_empty);
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

	@OnClick(R.id.turnAlarmOn)
	protected void onTurnAlarmOnClicked() {
		Log.e(TAG, "Turning alarm on");
	}

	@OnClick(R.id.turnAlarmOff)
	protected void onTurnAlarmOffClicked() {
		Log.e(TAG, "Turning alarm off");
	}

	@OnClick(R.id.trackedUserSetting)
	protected void onTrackedUserSettingClicked() {
		showTrackedUserDialog();
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

		// Inflate the TextInputLayout in which the user can enter the password
		final LayoutInflater inflater = dialog.getLayoutInflater();
		@SuppressLint("InflateParams")
		final View content = inflater.inflate(R.layout.dialog_input, null);
		inputField.value = (EditText) content.findViewById(R.id.input_text);

		dialog.setView(content);
		dialog.show();
	}

}
