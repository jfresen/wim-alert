package nl.jellow.wimalert.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jelle on 7-5-2016.
 */
public class ViewHierarchy {

	private static final String TAG = ViewHierarchy.class.getSimpleName();

	public static abstract class ViewCallback<T extends View> {

		private final Class<T> mClass;

		public ViewCallback(@NonNull final Class<T> clazz) {
			mClass = clazz;
		}

		/**
		 * Perform an action with the given view. Use the return value to indicate if traversal
		 * should stop.
		 *
		 * @param view The view that is being traversed right now
		 * @return {@code true} if traversing should continue,
		 *         {@code false} if traversing should stop
		 */
		public abstract boolean doWithView(@NonNull final T view);

		public void onTraverseDeeper() {
		}

		public void onTraverseShallower() {
		}
	}

	public static <T extends View> boolean traverse(@Nullable final View view, @Nullable final ViewCallback<T> callback) {
		if (view == null) {
			return false;
		}

		// First apply the callback on the view itself, if the classes match
		if (callback != null && callback.mClass.isAssignableFrom(view.getClass())) {
			if (!callback.doWithView(callback.mClass.cast(view))) {
				return false;
			}
		}

		// Then recurse into its childs:
		boolean keepTraversing = true;
		if (view instanceof ViewGroup) {
			final ViewGroup parent = (ViewGroup)view;
			if (callback != null) {
				callback.onTraverseDeeper();
			}
			for (int i = 0; i < parent.getChildCount() && keepTraversing; i++) {
				if (!traverse(parent.getChildAt(i), callback)) {
					keepTraversing = false;
				}
			}
			if (callback != null) {
				callback.onTraverseShallower();
			}
		}
		return keepTraversing;
	}

}
