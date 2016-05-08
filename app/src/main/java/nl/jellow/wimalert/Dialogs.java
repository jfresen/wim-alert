package nl.jellow.wimalert;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TimePicker;

import com.innovattic.lib.util.Prefs;

import java.util.Locale;

/**
 * Created by Jelle on 12-1-2016.
 */
public class Dialogs {

	public static final DialogInterface.OnClickListener DISMISS_ON_CLICK = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			if (dialog != null) {
				dialog.dismiss();
			}
		}
	};

	public static class SimpleTimePicker extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		private static final String TAG = SimpleTimePicker.class.getSimpleName();

		private static final String ARG_PREF_KEY = "pref-key";
		private static final String ARG_HOURS = "hours";
		private static final String ARG_MINUTES = "minutes";

		private String mPreferenceKey;
		private int mHours;
		private int mMinutes;

		public static SimpleTimePicker getInstance(final String preferenceKey) {

			final int time = Prefs.get().getInt(preferenceKey, 0);
			final int hours = time / 60;
			final int minutes = time - hours * 60;

			final SimpleTimePicker fragment = new SimpleTimePicker();
			final Bundle args = new Bundle();
			args.putString(ARG_PREF_KEY, preferenceKey);
			args.putInt(ARG_HOURS, hours);
			args.putInt(ARG_MINUTES, minutes);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			final Bundle args = getArguments();
			mPreferenceKey = args.getString(ARG_PREF_KEY, null);
			mHours = args.getInt(ARG_HOURS, 0);
			mMinutes = args.getInt(ARG_MINUTES, 0);
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new TimePickerDialog(getActivity(), this, mHours, mMinutes, true);
		}

		@Override
		public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
			Log.i(TAG, "Setting time span " + mPreferenceKey + ": hour=" + hourOfDay + ", minute=" + minute);
			Prefs.get().setInt(mPreferenceKey, hourOfDay * 60 + minute);
		}

	}

}
