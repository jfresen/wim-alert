package nl.jellow.wimalert.net;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;

import nl.jellow.wimalert.App;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jelle on 30-10-2016.
 */

public class Api {

	private static final String TAG = App.TAG;

	public interface RequestBuilderConfigurator {
		Request.Builder configure(Request.Builder builder);
	}

	public static void send(final String endpoint) {
		send(endpoint, null);
	}

	public static void send(final String endpoint, final RequestBuilderConfigurator configurator) {
		final String ip = App.getWebAddress();
		if (TextUtils.isEmpty(ip)) {
			Log.w(TAG, "URL of the lamp is not set");
		}
		final String url = String.format("http://%1$s/%2$s", ip, endpoint);
		final OkHttpClient client = new OkHttpClient();
		Request.Builder builder = new Request.Builder().url(url);
		if (configurator != null) {
			builder = configurator.configure(builder);
		}
		final Request request = builder.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				Log.e(TAG, "Failed to make call to " + url);
			}
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				Log.v(TAG, "Successfully made call to " + url);
			}
		});
	}

	public static byte[] base64(final String str) {
		return base64(str.getBytes());
	}

	public static byte[] base64(final byte[] unescapedData) {
		return Base64.encode(unescapedData, Base64.NO_PADDING | Base64.NO_WRAP);
	}

}
