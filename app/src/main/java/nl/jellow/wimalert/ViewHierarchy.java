package nl.jellow.wimalert;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.innovattic.lib.util.Print;

/**
 * Created by Jelle on 7-5-2016.
 */
public class ViewHierarchy {

	private static final String TAG = ViewHierarchy.class.getSimpleName();
	private static final String INDENT = "  ";
	private static final char EOL = '\n';

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

	/**
	 * Returns a string showing info about the given view and all its descendants. The info shown
	 * of each view is the following:
	 * <ul>
	 *     <li>The class name and, if not a class from the Android SDK, the class from the
	 *     Android SDK from which it extends.</li>
	 *     <li>The id of the view, if it has a name</li>
	 *     <li>Its size in pixels</li>
	 *     <li>Its position in pixels</li>
	 *     <li>Its visibility, either VISIBLE, INVISIBLE or GONE</li>
	 *     <li>Its width, in pixels, MATCH_PARENT or WRAP_CONTENT</li>
	 *     <li>For views with LinearLayout.LayoutParams: its weight</li>
	 *     <li>For views with RelativeLayout.LayoutParams: all enabled layout rules</li>
	 * </ul>
	 * This gives a format similar to this:
	 * <pre>
	 * LinearLayout @id/wrapper size=1080x1920 position=0,0 visibility=VISIBLE width=MATCH_PARENT height=MATCH_PARENT
	 *   [0] TextView @id/some_text size=1080x96 position=0,30 visibility=VISIBLE width=MATCH_PARENT height=WRAP_CONTENT weight=0
	 *   [1] CustomImageView (ImageView) size=864x486 position=108,156 visibility=VISIBLE width=WRAP_CONTENT height=WRAP_CONTENT weight=0
	 * </pre>
	 * @param view A View
	 * @return A string representation of the given view's hierarchy
	 */
	@NonNull
	public static String dumpViewHierarchy(@Nullable final View view, @Nullable final Context alternateContext)
	{
		StringBuilder sb = new StringBuilder();
		dumpViewHierarchy(sb, view, alternateContext, "");
		return sb.toString();
	}

	private static void dumpViewHierarchy(@NonNull final StringBuilder sb,
			@Nullable final View view, final @Nullable Context alternateContext, @NonNull String indent)
	{
		if (view == null) {
			return;
		}

		// First get info about the view itself
		dumpViewInfo(sb, view, alternateContext);
		sb.append(EOL);

		// Then recurse into its childs:
		if (view instanceof ViewGroup) {
			indent += INDENT;
			final ViewGroup parent = (ViewGroup)view;
			for (int i = 0; i < parent.getChildCount(); i++) {
				sb.append(indent).append('[').append(i).append("] ");
				dumpViewHierarchy(sb, parent.getChildAt(i), alternateContext, indent);
			}
		}
	}

	/**
	 * Returns a string showing info about the given view and all its ancestors. See {@link
	 * #dumpViewHierarchy(View, Context)} for an overview of the info per view.
	 *
	 * @param view A View
	 * @return A string representation of the given view's ancestry
	 */
	public static String dumpViewAncestry(@Nullable final View view, @Nullable final Context alternatContext)
	{
		StringBuilder sb = new StringBuilder();
		dumpViewAncestry(sb, view, alternatContext, "");
		return sb.toString();
	}

	private static void dumpViewAncestry(@NonNull final StringBuilder sb,
			@Nullable final View view, @Nullable final Context alternateContext, @NonNull String indent)
	{
		if (view == null) {
			return;
		}

		for (View curr = view; curr != null; ) {
			// First get info about the view itself
			sb.append(indent);
			dumpViewInfo(sb, curr, alternateContext);
			sb.append(EOL);
			// Then prepare the data for its parent
			indent += INDENT;
			ViewParent parent = curr.getParent();
			if (parent instanceof View) {
				curr = (View) parent;
			} else {
				curr = null;
			}
		}
	}

	private static void dumpViewInfo(@NonNull final StringBuilder sb, @NonNull final View view, @Nullable final Context alternateContext)
	{
		// Get the View class and the android View class from which the View class is an extension
		final Class<?> clazz = view.getClass();
		Class<?> aClazz = clazz;
		while (!isFromAndroidPackage(aClazz) && !isObject(aClazz)) {
			aClazz = aClazz.getSuperclass();
		}

		// Start the info with these class names
		sb.append(clazz.getSimpleName());
		if (!clazz.equals(aClazz)) {
			sb.append(" (").append(aClazz.getSimpleName()).append(')');
		}

		// Add the resource id, if any
		Resources r = view.getResources();
		if (r == null && alternateContext != null) {
			Log.d(TAG, "View resources are null, trying alternateContext's resources...");
			r = alternateContext.getResources();
			if (r == null) {
				Log.w(TAG, "Both view resources and alternateContext resources are null");
			}
		}
		final String resName = resourceName(r, view.getId(), false, true);
		if (resName != null) {
			sb.append(" id=").append(resName);
		}

		// Add the bounds
		final int w = view.getWidth(), h = view.getHeight();
		final int x = view.getLeft(), y = view.getTop();
		sb.append(" size=").append(w).append('x').append(h);
		sb.append(" position=").append(x).append(',').append(y);

		// Visibility
		sb.append(" visibility=").append(Print.visibility(view.getVisibility()));

		// Layout params
		ViewGroup.LayoutParams params = view.getLayoutParams();
		sb.append(" width=").append(Print.layoutSize(params.width));
		sb.append(" height=").append(Print.layoutSize(params.height));
		if (params instanceof LinearLayout.LayoutParams) {
			LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams)params;
			sb.append(" weight=").append(llp.weight);
		} else if (params instanceof RelativeLayout.LayoutParams) {
			RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)params;
			final int[] rules = rlp.getRules();
			for (int i = 0; i < rules.length; i++) {
				if (rules[i] != 0) {
					sb.append(' ').append(Print.layoutRule(i)).append('=');
					sb.append(Print.layoutRuleAnchor(r, rules[i]));
				}
			}
		}

		// Text, in case of TextViews
		if (view instanceof TextView) {
			sb.append(" text=").append(((TextView) view).getText());
		}
	}

	private static boolean isFromAndroidPackage(@Nullable final Class<?> clazz)
	{
		if (clazz == null || clazz.getPackage() == null || clazz.getPackage().getName() == null) {
			return false;
		}
		return clazz.getPackage().getName().startsWith("android");
	}

	private static boolean isObject(@Nullable final Class<?> clazz)
	{
		return Object.class.equals(clazz);
	}

	@Nullable
	private static String ruleAnchorToString(@NonNull final Resources res, final int anchor)
	{
		switch (anchor) {
			case -1: return "TRUE";
			case  0: return "FALSE";
		}
		return Print.resourceName(res, anchor);
	}

	/**
	 * Returns a string with the resource name for the given resource id. The format can be
	 * influenced with the addPackage and fallback parameters. If addPackage is {@code true}, the
	 * format is @package:type/entry. If addPackage is {@code false}, the format is @type/entry.
	 * If the resource cannot be found, the fallback parameter decides what is returned. When
	 * fallback is {@code false}, null is returned. If fallback is {@code true}, the resource id
	 * is converted to an 8-digit hexadecimal string, e.g. 0x07F1442E.
	 *
	 * @param r A Resources object
	 * @param id A resource id
	 * @param addPackage Whether or not to include the package name
	 * @param fallback If {@code true}, returns a string with the resource id in hex if the
	 *        resource cannot be found, if {@code false}, returns null if the resource cannot be
	 *        found.
	 * @return A string with the resource name for the given resource id, e.g.
	 *         {@code @string/some_string}.
	 */
	@Nullable
	public static String resourceName(@Nullable final Resources r, final int id,
			final boolean addPackage, final boolean fallback)
	{
		try {
			if (r != null) {
				final String name = r.getResourceTypeName(id) + "/" + r.getResourceEntryName(id);
				final String packageName = r.getResourcePackageName(id);
				if (addPackage || "android".equals(packageName)) {
					return "@" + r.getResourcePackageName(id) + ":" + name;
				} else {
					return "@" + name;
				}
			}
		} catch (Resources.NotFoundException ignored) {
		}
		if (fallback) {
			return Print.hex(id);
		} else {
			return null;
		}
	}

}
