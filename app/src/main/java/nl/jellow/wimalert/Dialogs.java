package nl.jellow.wimalert;

import android.content.DialogInterface;

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

}
