package nl.jellow.wimalert.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.Adler32;

import nl.jellow.wimalert.App;

import static android.content.SharedPreferences.Editor;

/**
 * Created by Jelle on 12-11-2014.
 */
public class Prefs
{

	// For storing Set<String> values on pre-honeycomb versions,
	// we sometimes throw our own ClassCastException.
	private static final String CLASS_CAST_EXCEPTION_MSG =
			"java.lang.String cannot be cast to java.lang.Set";

	// For storing Set<String> values on pre-honeycomb versions, we add a prefix
	// and a hash to detect that a String-pref is actually a Set<String>-pref.
	private static final String PREFIX = "java.util.Set[";
	private static final int HASH_SIZE = 8;

	// For storing Set<String> values on pre-honeycomb versions, we store all String's in
	// a comma-separated list which means we have to escape comma's from the String's.
	private static final char ESCAPE_CHAR = '\\';
	private static final char SEPARATOR_CHAR = ',';
	private static final String ESCAPE_STR = String.valueOf(ESCAPE_CHAR);
	private static final String SEPARATOR_STR = String.valueOf(SEPARATOR_CHAR);
	private static final String ESCAPED_ESCAPE_CHAR = "\\\\";
	private static final String ESCAPED_SEPARATOR = "\\,";

	private final SharedPreferences mPrefs;
	private final boolean IS_API_9;
	private final boolean IS_API_11;

	private static Context sContext = null;
	private static String sName = null;
	private static int sMode = -1;

	private Prefs(final SharedPreferences prefs)
	{
		mPrefs = prefs;
		IS_API_9 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
		IS_API_11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	/**
	 * Return a Prefs object wrapped around the given SharedPreferences
	 *
	 * @param preferences A SharedPreferences object retrieved by your app
	 * @return A Prefs object with which you can easily get and set values
	 */
	public static Prefs with(final SharedPreferences preferences)
	{
		return new Prefs(preferences);
	}

	/**
	 * Returns a Prefs object wrapped around the SharedPreferences from the given Context. The Prefs
	 * class must have been initialized with {@link #init(Context, String, int)} or
	 * {@link #init(String, int)} before this method can be called, or an {@link
	 * IllegalStateException} will be thrown. The name of the retrieved shared preferences
	 * and the mode in which they are opened are supplied by the most recent init call.
	 *
	 * @param context The context to get the shared preferences from.
	 * @throws IllegalStateException If the default name and mode are not set.
	 * @return A Prefs object with which you can easily get and set values
	 */
	public static Prefs with(final Context context)
	{
		if (sMode == -1) {
			throw new IllegalStateException("Prefs class has not yet been properly initialized. " +
					"If you have called init before, make sure the arguments were non-null " +
					"and not -1.");
		}
		if (sName != null) {
			return new Prefs(context.getSharedPreferences(sName, sMode));
		} else {
			final String appClassName = App.getContext().getClass().getCanonicalName();
			return new Prefs(context.getSharedPreferences(appClassName, sMode));
		}
	}

	/**
	 * Returns a Prefs object wrapped around the SharedPreferences from the default Context. The
	 * Prefs class must have been initialized with {@link #init(Context, String,
	 * int)} or {@link #init(String, int)} followed by {@link #setDefaultContext(
	 * Context)} before this method can be called, or an {@link
	 * IllegalStateException} will be thrown. The name of the retrieved shared preferences
	 * and the mode in which they are opened are supplied by the most recent init and/or set calls.
	 *
	 * @throws IllegalStateException If the default context, name and mode are not set.
	 * @return A Prefs object with which you can easily get and set values
	 */
	public static Prefs get()
	{
		if (sContext == null) {
			throw new IllegalStateException("Prefs class has not yet been properly initialized. " +
					"If you have called init before, make sure the arguments were non-null " +
					"and not -1.");
		}
		return Prefs.with(sContext);
	}

	/**
	 * Initializes the Prefs class with the given mode. All SharedPreferences instances will use
	 * this mode. Their name will be the class name of the Application class which must extend
	 * {@link App} and be in use. Note that you can not use {@link #get()} until you set the default
	 * Context as well with {@link #setDefaultContext(Context)}.
	 *
	 * @param mode The default privacy mode of the SharedPreferences. See {@link
	 *        Context#getSharedPreferences(String, int)} for possible values.
	 */
	public static void init(final int mode)
	{
		sMode = mode;
	}

	/**
	 * Initializes the Prefs class with the given name and mode. All SharedPreferences instances
	 * will use these values. Note that you can not use {@link #get()} until you set the default
	 * Context as well with {@link #setDefaultContext(Context)}.
	 *
	 * @param name The default name of the SharedPreferences
	 * @param mode The default privacy mode of the SharedPreferences. See {@link
	 *        Context#getSharedPreferences(String, int)} for possible values.
	 */
	public static void init(@Nullable final String name, final int mode)
	{
		init(mode);
		sName = name;
	}

	/**
	 * Initializes the Prefs class with the given context and mode. All SharedPreferences
	 * instances will be created from this context and with the given mode. Their name will be
	 * the class name of the Application class which must extend {@link App} and be in use.
	 *
	 * @param context The default context from which to get the SharedPreferences
	 * @param mode The default privacy mode of the SharedPreferences. See {@link
	 *        Context#getSharedPreferences(String, int)} for possible values.
	 */
	public static void init(final Context context, final int mode)
	{
		init(mode);
		setDefaultContext(context);
	}

	/**
	 * Initializes the Prefs class with the given context, name and mode. All SharedPreferences
	 * instances will be created from this context and with the given values.
	 *
	 * @param context The default context from which to get the SharedPreferences
	 * @param name The default name of the SharedPreferences
	 * @param mode The default privacy mode of the SharedPreferences. See {@link
	 *        Context#getSharedPreferences(String, int)} for possible values.
	 */
	public static void init(final Context context, final String name, final int mode)
	{
		init(name, mode);
		setDefaultContext(context);
	}

	/**
	 * Sets the default context to use when retrieving a Prefs instance without supplying a Context.
	 *
	 * @param context The default context from which to get the SharedPreferences
	 */
	public static void setDefaultContext(final Context context)
	{
		sContext = context;
	}

	/**
	 * Return the original SharedPreferences object with which this Prefs object was initialized.
	 *
	 * @return The original SharedPreferences
	 */
	public final SharedPreferences sharedPreferences()
	{
		return mPrefs;
	}

	/**
	 * Set and apply a boolean value. To retrieve the value, use the SharedPreferences object with
	 * which this Prefs object was initialized.
	 *
	 * @param key The key with which the value must be stored in the SharedPreferences
	 * @param b The value
	 * @return This Prefs object
	 * @see SharedPreferences#getBoolean(String, boolean)
	 */
	public final Prefs setBoolean(final String key, final boolean b)
	{
		final Editor editor = mPrefs.edit();
		editor.putBoolean(key, b);
		apply(editor);
		return this;
	}

	/**
	 * Set and apply an integer value. To retrieve the value, use the SharedPreferences object with
	 * which this Prefs object was initialized.
	 *
	 * @param key The key with which the value must be stored in the SharedPreferences
	 * @param i The value
	 * @return This Prefs object
	 * @see SharedPreferences#getInt(String, int)
	 */
	public final Prefs setInt(final String key, final int i)
	{
		final Editor editor = mPrefs.edit();
		editor.putInt(key, i);
		apply(editor);
		return this;
	}

	/**
	 * Set and apply a long value. To retrieve the value, use the SharedPreferences object with
	 * which this Prefs object was initialized.
	 *
	 * @param key The key with which the value must be stored in the SharedPreferences
	 * @param l The value
	 * @return This Prefs object
	 * @see SharedPreferences#getLong(String, long)
	 */
	public final Prefs setLong(final String key, final long l)
	{
		final Editor editor = mPrefs.edit();
		editor.putLong(key, l);
		apply(editor);
		return this;
	}

	/**
	 * Set and apply a float value. To retrieve the value, use the SharedPreferences object with
	 * which this Prefs object was initialized.
	 *
	 * @param key The key with which the value must be stored in the SharedPreferences
	 * @param f The value
	 * @return This Prefs object
	 * @see SharedPreferences#getFloat(String, float)
	 */
	public final Prefs setFloat(final String key, final float f)
	{
		final Editor editor = mPrefs.edit();
		editor.putFloat(key, f);
		apply(editor);
		return this;
	}

	/**
	 * Set and apply a String value. To retrieve the value, use the SharedPreferences object with
	 * which this Prefs object was initialized.
	 *
	 * @param key The key with which the value must be stored in the SharedPreferences
	 * @param s The value
	 * @return This Prefs object
	 * @see SharedPreferences#getString(String, String)
	 */
	public final Prefs setString(final String key, final String s)
	{
		final Editor editor = mPrefs.edit();
		editor.putString(key, s);
		apply(editor);
		return this;
	}

	/**
	 * Set and apply a set of strings. To retrieve the value, use {@link #getStringSet(String,
	 * Set)}. This is an API version independent implementation and captures the corner
	 * case where the set of strings was stored on a pre-Honeycomb version and is read on a
	 * post-Honeycomb version.
	 *
	 * @param key The key with which the value must be stored in the SharedPreferences
	 * @param strings The values
	 * @return This Prefs object
	 */
	public final Prefs setStringSet(final String key, final Set<String> strings)
	{
		final Editor editor = mPrefs.edit();
		putStringSet(editor, key, strings);
		apply(editor);
		return this;
	}

	/**
	 * @see SharedPreferences#getBoolean(String, boolean)
	 */
	public final boolean getBoolean(final String key, final boolean defValue)
	{
		return mPrefs.getBoolean(key, defValue);
	}

	/**
	 * @see SharedPreferences#getInt(String, int)
	 */
	public final int getInt(final String key, final int defValue)
	{
		return mPrefs.getInt(key, defValue);
	}

	/**
	 * @see SharedPreferences#getLong(String, long)
	 */
	public final long getLong(final String key, final long defValue)
	{
		return mPrefs.getLong(key, defValue);
	}

	/**
	 * @see SharedPreferences#getFloat(String, float)
	 */
	public final float getFloat(final String key, final float defValue)
	{
		return mPrefs.getFloat(key, defValue);
	}

	/**
	 * @see SharedPreferences#getString(String, String)
	 */
	public final String getString(final String key, final String defValue)
	{
		return mPrefs.getString(key, defValue);
	}

	/**
	 * @see SharedPreferences#getStringSet(String, Set)
	 */
	@SuppressLint("NewApi")
	public final Set<String> getStringSet(final String key, final Set<String> defValues)
	{
		if (IS_API_11) {
			return getStringsHc(key, defValues);
		} else {
			return getStringsPreHc(key, defValues);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private Set<String> getStringsHc(final String key, final Set<String> defValues)
	{
		ClassCastException caught;
		// First, try the new API
		try {
			return mPrefs.getStringSet(key, defValues);
		} catch (ClassCastException e) {
			caught = e;
		}
		// If that fails, try the old API
		try {
			return getStringsPreHc(key, defValues);
		} catch (ClassCastException e) {
			// Throw original exception, the prefs actually
			// contain a key with a value that was not a Set of Strings
			throw caught;
		}
	}

	private Set<String> getStringsPreHc(final String key, final Set<String> defValues)
	{
		// Retrieve the raw value:
		final String value;
		try {
			value = mPrefs.getString(key, null);
		} catch (ClassCastException e) {
			// Throw an exception with a less confusing message
			throw new ClassCastException(CLASS_CAST_EXCEPTION_MSG);
		}
		if (value == null) {
			return defValues;
		}

		// Check the hash. If the hash is not correct, an exception is throwns
		if (value.length() < HASH_SIZE + PREFIX.length()) {
			// This was not a Set of Strings!
			throw new ClassCastException(CLASS_CAST_EXCEPTION_MSG);
		}
		final String storedHash = value.substring(0, HASH_SIZE);
		final String actualHash = getHash(value.substring(HASH_SIZE));
		if (!storedHash.equals(actualHash)) {
			// This was not a Set of Strings!
			throw new ClassCastException(CLASS_CAST_EXCEPTION_MSG);
		}

		// Decode the comma separated list with a simple state machine
		final String escapedStrings = value.substring(HASH_SIZE+PREFIX.length());
		final Set<String> strings = new HashSet<>();
		final StringBuilder buffer = new StringBuilder();
		final char[] raw = escapedStrings.toCharArray();
		boolean escapeNext = false;
		for (int i = 0; i < escapedStrings.length(); i++) {
			if (escapeNext) {
				buffer.append(raw[i]);
				escapeNext = false;
			} else if (raw[i] == ESCAPE_CHAR) {
				escapeNext = true;
			} else if (raw[i] == SEPARATOR_CHAR) {
				strings.add(buffer.toString());
				buffer.delete(0, buffer.length());
			} else {
				buffer.append(raw[i]);
			}
		}
		// After the for loop, the buffer contains the last string
		strings.add(buffer.toString());
		return strings;
	}

	/**
	 * Remove a value from the preferences.
	 *
	 * @param key The key whose value must be removed from the SharedPreferences
	 * @return This Prefs object
	 */
	public final Prefs remove(final String key)
	{
		final Editor editor = mPrefs.edit();
		editor.remove(key);
		apply(editor);
		return this;
	}

	/**
	 * Removes all values from the preferences.
	 *
	 * @return This Prefs object
	 */
	public final Prefs removeAll()
	{
		final Editor editor = mPrefs.edit();
		for (String key : mPrefs.getAll().keySet()) {
			editor.remove(key);
		}
		apply(editor);
		return this;
	}

	@SuppressLint("NewApi")
	private void apply(final Editor editor)
	{
		if (IS_API_9) {
			editor.apply();
		} else {
			editor.commit();
		}
	}

	@SuppressLint("NewApi")
	private void putStringSet(final Editor editor, final String key, final Set<String> strings)
	{
		if (IS_API_11) {
			editor.putStringSet(key, strings);
		} else {
			putStringSetPreHc(editor, key, strings);
		}
	}

	private void putStringSetPreHc(final Editor editor, final String key, final Set<String> strings)
	{
		final StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			if (sb.length() > 0) {
				sb.append(SEPARATOR_CHAR);
			}
			sb.append(escapeCommas(s));
		}
		final String value = PREFIX+sb.toString();
		editor.putString(key, getHash(value)+value);
	}

	private String escapeCommas(String s)
	{
		// First, escape all escape characters:
		s = s.replace(ESCAPE_STR, ESCAPED_ESCAPE_CHAR);
		// Then, escape all commas:
		return s.replace(SEPARATOR_STR, ESCAPED_SEPARATOR);
	}

	private String getHash(final String s)
	{
		// Go for Adler32, which is cheaper then CRC32, but slightly less reliable
		final Adler32 encoder = new Adler32();
		encoder.update(s.getBytes());
		return String.format("%0"+HASH_SIZE+"X", encoder.getValue());
	}

}
