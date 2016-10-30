package nl.jellow.wimalert.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by Jelle on 30-10-2016.
 */

public class WifiScanner {

	public interface ResultListener {
		void onScanResult(List<ScanResult> scanResults);
	}

	private Context mContext;
	private WifiManager mWifiManager;
	private ScanReceiver mScanReceiver;

	private ResultListener mListener;
	private boolean mStopped;
	private boolean mContinuousScan;

	public static WifiScanner startScan(final @NonNull Context context,
			final ResultListener listener) {
		return startScan(context, listener, false);
	}

	public static WifiScanner startScan(final @NonNull Context context,
			final ResultListener listener, final boolean continuous) {
		final WifiScanner scanner = new WifiScanner(context, listener, continuous);
		scanner.startScanning();
		return scanner;
	}

	private WifiScanner(final @NonNull Context context, ResultListener listener, boolean continuous) {
		mContext = context;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		mStopped = false;
		mListener = listener;
		mContinuousScan = continuous;
	}

	public Context getContext() {
		return mContext;
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	private void startScanning() {
		registerBroadcastReceiver();
		mWifiManager.startScan();
	}

	public void stopScanning() {
		mStopped = true;
		unregisterBroadcastReceiver();
	}

	private void onNewScanResults() {
		if (mListener != null) {
			mListener.onScanResult(mWifiManager.getScanResults());
		}
		// Start a new scan immediately
		if (mContinuousScan && !mStopped) {
			mWifiManager.startScan();
		} else {
			unregisterBroadcastReceiver();
		}
	}

	private void registerBroadcastReceiver() {
		if (mScanReceiver != null) {
			return;
		}
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

		mScanReceiver = new ScanReceiver();
		mContext.registerReceiver(mScanReceiver, intentFilter);
	}

	private void unregisterBroadcastReceiver() {
		if (mScanReceiver != null) {
			mContext.unregisterReceiver(mScanReceiver);
			mScanReceiver = null;
		}
	}

	private class ScanReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				onNewScanResults();
			}
		}
	}

}
