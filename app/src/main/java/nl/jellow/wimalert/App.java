package nl.jellow.wimalert;

import android.content.Context;

import com.innovattic.lib.android.BaseApp;
import com.innovattic.lib.util.Prefs;

/**
 * Created by Jelle on 7-5-2016.
 */
public class App extends BaseApp {

	public static final String PREF_TRACKED_USER_NAME = "tracked-user";

	@Override
	public void onCreate() {
		super.onCreate();
		Prefs.init(this, Context.MODE_PRIVATE);
	}
}
