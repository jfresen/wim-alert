package nl.jellow.wimalert.util;

import android.annotation.TargetApi;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

import me.zhanghai.android.materialprogressbar.TintableDrawable;

/**
 * Created by Jelle on 4-12-2016.
 */

public class UI {

	// The MaterialProgressBar library defines its own setTint method in TintableDrawable.
	// Tell lint that calling setTint in this case is ok.
	@TargetApi(14)
	public static void setTint(@Nullable final TintableDrawable tintable, final @ColorInt int tintColor) {
		if (tintable != null) {
			tintable.setTint(tintColor);
		}
	}

}
