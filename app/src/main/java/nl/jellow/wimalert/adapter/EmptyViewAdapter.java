package nl.jellow.wimalert.adapter;

import java.util.List;

/**
 * Created by Jelle on 01-12-2016.
 * @author Jelle Fresen &lt;jellefresen@gmail.com&gt;
 */
public abstract class EmptyViewAdapter<T, VH extends ArrayRecyclerViewAdapter.ViewHolder>
		extends ArrayRecyclerViewAdapter<T, VH> {

	// Any random number will do
	public static final int VIEW_TYPE_EMPTY_VIEW = 896708356;

	public EmptyViewAdapter() {
		super();
	}

	public EmptyViewAdapter(final List<T> items) {
		super(items);
	}

	@Override
	public int getItemCount() {
		if (showEmptyView()) {
			return 1;
		} else {
			return super.getItemCount();
		}
	}

	@Override
	public int getItemViewType(final int position) {
		if (showEmptyView()) {
			return VIEW_TYPE_EMPTY_VIEW;
		} else {
			return super.getItemViewType(position);
		}
	}

	@Override
	public void onBindViewHolder(final VH holder, final int position) {
		if (!showEmptyView()) {
			super.onBindViewHolder(holder, position);
		}
	}

	private boolean showEmptyView() {
		return super.getItemCount() == 0;
	}

}
