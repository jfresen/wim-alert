package nl.jellow.wimalert.util;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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
	public static String dumpViewHierarchy(@Nullable final View view, @Nullable Context resourceContext)
	{
		StringBuilder sb = new StringBuilder();
		dumpViewHierarchy(sb, view, "", resourceContext);
		return sb.toString();
	}

	private static void dumpViewHierarchy(@NonNull final StringBuilder sb,
			@Nullable final View view, @NonNull String indent, @Nullable Context c)
	{
		if (view == null) {
			return;
		}

		// First get info about the view itself
		dumpViewInfo(sb, view, c);
		sb.append(EOL);

		// Then recurse into its childs:
		if (view instanceof ViewGroup) {
			indent += INDENT;
			final ViewGroup parent = (ViewGroup)view;
			for (int i = 0; i < parent.getChildCount(); i++) {
				sb.append(indent).append('[').append(i).append("] ");
				dumpViewHierarchy(sb, parent.getChildAt(i), indent, c);
			}
		}
	}

	private static void dumpViewInfo(@NonNull final StringBuilder sb, @NonNull final View view,
			@Nullable final Context c)
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
		final Resources r = c == null ? view.getResources() : c.getResources();
		final String resName = printResourceName(r, view.getId(), false, false);
		if (resName != null) {
			sb.append(" id=").append(resName);
		}

		// Add the bounds
		final int w = view.getWidth(), h = view.getHeight();
		final int x = view.getLeft(), y = view.getTop();
		sb.append(" size=").append(w).append('x').append(h);
		sb.append(" position=").append(x).append(',').append(y);

		// Visibility
		sb.append(" visibility=").append(printVisibility(view.getVisibility()));

		// Layout params
		ViewGroup.LayoutParams params = view.getLayoutParams();
		sb.append(" width=").append(printLayoutSize(params.width));
		sb.append(" height=").append(printLayoutSize(params.height));
		if (params instanceof LinearLayout.LayoutParams) {
			LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams)params;
			sb.append(" weight=").append(llp.weight);
		} else if (params instanceof RelativeLayout.LayoutParams) {
			RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)params;
			final int[] rules = rlp.getRules();
			for (int i = 0; i < rules.length; i++) {
				if (rules[i] != 0) {
					sb.append(' ').append(printLayoutRule(i)).append('=');
					sb.append(printLayoutRuleAnchor(r, rules[i]));
				}
			}
		}

		// Text in case of TextViews
		if (view instanceof TextView) {
			sb.append(" text=\"").append(((TextView) view).getText()).append('"');
		}
	}

	/**
	 * Returns if the given class descriptor describes a class from the Android package. A class is
	 * considered to be from Android if the fully qualified class name starts with "android".
	 *
	 * @param clazz A class descriptor
	 * @return {@code true} if the class is from an Android package
	 */
	private static boolean isFromAndroidPackage(@Nullable final Class<?> clazz)
	{
		if (clazz == null || clazz.getPackage() == null || clazz.getPackage().getName() == null) {
			return false;
		}
		return clazz.getPackage().getName().startsWith("android");
	}

	/**
	 * Returns if the given class descriptor is describing the Java root class Object.
	 *
	 * @param clazz A class descriptor
	 * @return {@code true} if clazz equals {@code Object.class}
	 */
	private static boolean isObject(@Nullable final Class<?> clazz)
	{
		return Object.class.equals(clazz);
	}

	/**
	 * Returns a string representation of the given view visibility, which must be a value as
	 * returned by {@link View#getVisibility()}.
	 *
	 * @param visibility A visibility value
	 * @return VISIBLE, INVISIBLE or GONE
	 */
	@NonNull
	public static String printVisibility(final int visibility)
	{
		switch (visibility) {
			case View.VISIBLE:   return "VISIBLE";
			case View.INVISIBLE: return "INVISIBLE";
			case View.GONE:      return "GONE";
			default: return Integer.toString(visibility);
		}
	}

	/**
	 * Returns a string representation of the given attribute, which must be a layout attribute
	 * from RelativeLayout.LayoutParams. Note that this concerns the attribute name, not the value.
	 *
	 * @param verb An identifier of a layout rule from RelativeLayout.LayoutParams
	 * @return ABOVE, ALIGN_BASELINE, ALIGN_BOTTOM, etc.
	 */
	@NonNull
	public static String printLayoutRule(final int verb)
	{
		switch (verb) {
			case RelativeLayout.ABOVE:               return "ABOVE";
			case RelativeLayout.ALIGN_BASELINE:      return "ALIGN_BASELINE";
			case RelativeLayout.ALIGN_BOTTOM:        return "ALIGN_BOTTOM";
			case RelativeLayout.ALIGN_END:           return "ALIGN_END";
			case RelativeLayout.ALIGN_LEFT:          return "ALIGN_LEFT";
			case RelativeLayout.ALIGN_PARENT_BOTTOM: return "ALIGN_PARENT_BOTTOM";
			case RelativeLayout.ALIGN_PARENT_END:    return "ALIGN_PARENT_END";
			case RelativeLayout.ALIGN_PARENT_LEFT:   return "ALIGN_PARENT_LEFT";
			case RelativeLayout.ALIGN_PARENT_RIGHT:  return "ALIGN_PARENT_RIGHT";
			case RelativeLayout.ALIGN_PARENT_START:  return "ALIGN_PARENT_START";
			case RelativeLayout.ALIGN_PARENT_TOP:    return "ALIGN_PARENT_TOP";
			case RelativeLayout.ALIGN_RIGHT:         return "ALIGN_RIGHT";
			case RelativeLayout.ALIGN_START:         return "ALIGN_START";
			case RelativeLayout.ALIGN_TOP:           return "ALIGN_TOP";
			case RelativeLayout.BELOW:               return "BELOW";
			case RelativeLayout.CENTER_HORIZONTAL:   return "CENTER_HORIZONTAL";
			case RelativeLayout.CENTER_IN_PARENT:    return "CENTER_IN_PARENT";
			case RelativeLayout.CENTER_VERTICAL:     return "CENTER_VERTICAL";
			case RelativeLayout.END_OF:              return "END_OF";
			case RelativeLayout.LEFT_OF:             return "LEFT_OF";
			case RelativeLayout.RIGHT_OF:            return "RIGHT_OF";
			case RelativeLayout.START_OF:            return "START_OF";
			default: return Integer.toString(verb);
		}
	}

	/**
	 * Prints the anchor of a layout rule, which is TRUE or FALSE in case of boolean rules, or a
	 * resource name/id in case of anchoring rules. Note that if the given anchor is the value of
	 * an anchoring rule, but it is set to 'nothing' (i.e., 0), it will still be interpreted as
	 * FALSE.
	 *
	 * @param res A Resources object to lookup a resource name. If null, will return {@code null}
	 *        if the given anchor is neither TRUE or FALSE.
	 * @param anchor The anchor of a layout rule. Called an 'anchor' regardless of whether or not
	 *        it is really from an anchoring rule.
	 * @return TRUE, FALSE, {@code null} or a resource id.
	 */
	@Nullable
	public static String printLayoutRuleAnchor(@Nullable final Resources res, final int anchor)
	{
		switch (anchor) {
			case RelativeLayout.TRUE: return "TRUE";
			case 0:                   return "FALSE";
		}
		return res == null ? null : printResourceName(res, anchor);
	}

	/**
	 * Returns a string representation of the given layout parameter, which must be a value of
	 * either layout_width or layout_height.
	 *
	 * @param value The value of layout_width or layout_height
	 * @return MATCH_PARENT, WRAP_CONTENT or the given value
	 */
	@NonNull
	public static String printLayoutSize(final int value)
	{
		switch (value) {
			case MATCH_PARENT: return "MATCH_PARENT";
			case WRAP_CONTENT: return "WRAP_CONTENT";
			default: return Integer.toString(value);
		}
	}

	/**
	 * Same as {@link #printResourceName(Resources, int, boolean, boolean) resId(r, id, false, true)}.
	 *
	 * @param r A Resources object
	 * @param id A resource id
	 * @return A string with the resource name for the given resource id, e.g.
	 *         {@code @string/some_string}.
	 */
	@Nullable
	public static String printResourceName(@NonNull final Resources r, final int id)
	{
		return printResourceName(r, id, false, true);
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
	public static String printResourceName(@NonNull final Resources r, final int id,
			final boolean addPackage, final boolean fallback)
	{
		try {
			final String name = r.getResourceTypeName(id) + "/" + r.getResourceEntryName(id);
			if (addPackage) {
				return "@" + r.getResourcePackageName(id) + ":" + name;
			} else {
				return "@" + name;
			}
		} catch (final Resources.NotFoundException nfe) {
			if (fallback) {
				return printHex(id);
			} else {
				return null;
			}
		}
	}

	/**
	 * Format the given integer as a hex string, prefixed with "0x" and padded to 8 digits, for
	 * example 0x0043E9A9.
	 *
	 * @param i An int
	 * @return A hex string, e.g., 0x0043E9A9
	 */
	public static String printHex(final int i)
	{
		return String.format(Locale.US, "0x%08X", i);
	}

}
