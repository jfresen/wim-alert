package nl.jellow.wimalert.net;

import android.text.TextUtils;
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

	private static final String TAG = Api.class.getSimpleName();

	public static void Send(final String endpoint) {
		final String ip = App.getIpAddress();
		if (TextUtils.isEmpty(ip)) {
			Log.w(TAG, "IP address of the lamp is not known");
		}
		final String url = String.format("http://%1$s/%2$s", ip, endpoint);
		final OkHttpClient client = new OkHttpClient();
		final Request request = new Request.Builder()
				.url(url)
				.build();
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

}
