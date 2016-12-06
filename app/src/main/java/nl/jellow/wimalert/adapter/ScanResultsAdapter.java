package nl.jellow.wimalert.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import me.zhanghai.android.materialprogressbar.IndeterminateProgressDrawable;
import nl.jellow.wimalert.R;
import nl.jellow.wimalert.adapter.ArrayRecyclerViewAdapter.ViewHolder;
import nl.jellow.wimalert.util.UI;

/**
 * Created by Jelle on 01-12-2016.
 * @author Jelle Fresen &lt;jellefresen@gmail.com&gt;
 */
public class ScanResultsAdapter extends EmptyViewAdapter<ScanResult, ViewHolder> {

	//region ViewHolders

	public static class ScanResultViewHolder extends ViewHolder {

		@Bind(R.id.ssid)
		public TextView mSsid;

		public ScanResultViewHolder(final View view) {
			super(view);
		}

	}

	public static class ScanningViewHolder extends ViewHolder {

		@Bind(R.id.progress)
		public ProgressBar mProgress;

		public ScanningViewHolder(final View view) {
			super(view);
			final Context context = itemView.getContext();
			final IndeterminateProgressDrawable drawable = new IndeterminateProgressDrawable(context);
			UI.setTint(drawable, ContextCompat.getColor(context, R.color.colorPrimary));
			mProgress.setIndeterminateDrawable(drawable);
		}

	}

	//endregion

	@Override
	protected int getLayoutId(final int viewType) {
		switch (viewType) {
			case VIEW_TYPE_EMPTY_VIEW:
				return R.layout.item_busy_scanning;
			default: case 0:
				return R.layout.item_scan_result;
		}
	}

	@NonNull
	@Override
	protected ViewHolder createViewHolder(final View view, final int viewType) {
		switch (viewType) {
			case VIEW_TYPE_EMPTY_VIEW:
				return new ScanningViewHolder(view);
			default: case 0:
				return new ScanResultViewHolder(view);
		}
	}

	@Override
	protected void onBindViewHolder(final ViewHolder viewHolder, final int position,
			final ScanResult scanResult) {
		if (!(viewHolder instanceof ScanResultViewHolder)) {
			return;
		}
		final ScanResultViewHolder holder = (ScanResultViewHolder) viewHolder;
		final int signalStrength = WifiManager.calculateSignalLevel(scanResult.level, 100);
		holder.mSsid.setText(scanResult.SSID);
	}

}
