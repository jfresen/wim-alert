package nl.jellow.wimalert.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import nl.jellow.wimalert.MainActivity;
import nl.jellow.wimalert.R;

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

	public static abstract class TextInputDialog extends DialogFragment {

		protected MainActivity getMainActivity() {
			final FragmentActivity activity = getActivity();
			if (activity instanceof MainActivity) {
				return (MainActivity) activity;
			}
			return null;
		}

		public abstract void configureDialog(AlertDialog.Builder builder);
		public abstract void configureEditText(EditText editText);
		public abstract void onPositiveButtonClicked(Editable text);

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Mutable<EditText> inputField = new Mutable<>(null);
			// Create the dialog
			final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			configureDialog(builder);
			builder.setNegativeButton(R.string.label_cancel, Dialogs.DISMISS_ON_CLICK);
			builder.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					onPositiveButtonClicked(inputField.value.getText());
					dialog.dismiss();
				}
			});
			final AlertDialog dialog = builder.create();

			// Inflate the TextInputLayout in which the username can be entered
			final LayoutInflater inflater = dialog.getLayoutInflater();
			@SuppressLint("InflateParams")
			final View content = inflater.inflate(R.layout.dialog_input, null);
			inputField.value = (EditText) content.findViewById(R.id.input_text);
			configureEditText(inputField.value);
			final CharSequence text = inputField.value.getText();
			inputField.value.setSelection(text == null ? 0 : text.length());
			inputField.value.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
					dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
					return true;
				}
			});

			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			dialog.setView(content);
			return dialog;
		}

	}

}
