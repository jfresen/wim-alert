package nl.jellow.wimalert.net;

import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import nl.jellow.wimalert.R;
import nl.jellow.wimalert.adapter.ArrayRecyclerViewAdapter;
import nl.jellow.wimalert.adapter.ScanResultsAdapter;
import nl.jellow.wimalert.util.Dialogs;

/**
 * Created by Jelle on 6-12-2016.
 */
public class ChooseWifiDialog extends Dialogs.ListDialogFragment<ScanResultsAdapter.ViewHolder>
		implements WifiScanner.ResultListener, ArrayRecyclerViewAdapter.OnClickListener<ScanResult> {

	private static final String ARG_PATTERN = "pattern";
	private static final Pattern ESP_PATTERN = Pattern.compile("ESP_.*");

	private WifiScanner mScanner;
	private ScanResultsAdapter mAdapter;

	@Override
	protected int getDialogLayout() {
		return R.layout.dialog_wifi_networks;
	}

	@Override
	protected RecyclerView.Adapter<ScanResultsAdapter.ViewHolder> onCreateAdapter() {
		mAdapter = new ScanResultsAdapter();
		mAdapter.addOnClickListener(this);
		return mAdapter;
	}

	@Override
	protected void configureDialog(final AlertDialog.Builder builder) {
		super.configureDialog(builder);
		builder.setTitle(R.string.setup_select_esp_wifi);
		if (mScanner == null) {
			mScanner = WifiScanner.startScan(builder.getContext(), this, true);
		}
		builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mScanner != null) {
					mScanner.stopScanning();
					mScanner = null;
				}
			}
		});
	}

	@Override
	public void onScanResult(final List<ScanResult> scanResults) {
		if (mAdapter != null) {
			mAdapter.setItems(sort(filter(scanResults)));
		}
	}

	private List<ScanResult> filter(final List<ScanResult> scanResults) {
		final List<ScanResult> filtered = new ArrayList<>();
		for (ScanResult result : scanResults) {
			if (ESP_PATTERN.matcher(result.SSID).matches()) {
				filtered.add(result);
			}
		}
		return filtered;
	}

	private List<ScanResult> sort(final List<ScanResult> scanResults) {
		Collections.sort(scanResults, SCANRESULT_COMPARATOR);
		return scanResults;
	}

	@Override
	public void onItemClicked(final int position, final ScanResult data) {
		// TODO: connect to the wifi
		dismiss();
		Toast.makeText(getContext(), "Clicked on " + data.SSID, Toast.LENGTH_SHORT).show();
	}

	private static final Comparator<ScanResult> SCANRESULT_COMPARATOR = new Comparator<ScanResult>() {
		@Override
		public int compare(final ScanResult lhs, final ScanResult rhs) {
			return WifiManager.compareSignalLevel(rhs.level, lhs.level);
		}
	};

}
