package nl.jellow.wimalert.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import nl.jellow.wimalert.R;

/**
 * A superclass for adapters that adapt a list of items of a certain class.
 *
 * Created by Jelle on 26-11-2016.
 * @author Jelle Fresen &lt;jellefresen@gmail.com&gt;
 */
public abstract class ArrayRecyclerViewAdapter<T, VH extends ArrayRecyclerViewAdapter.ViewHolder>
		extends RecyclerView.Adapter<VH> {

	public interface OnClickListener<T> {
		void onItemClicked(int position, T data);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		@Nullable
		@Bind(R.id.clickHandler)
		protected View clickHandler;

		public ViewHolder(final View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}

	private final List<T> mItems = new ArrayList<>();
	private final List<OnClickListener<T>> mClickListeners = new ArrayList<>();

	/**
	 * Constructs an empty ArrayRecyclerViewAdapter.
	 */
	public ArrayRecyclerViewAdapter() {
	}

	/**
	 * Constructs an ArrayRecyclerViewAdapter with the given list of items.
	 *
	 * @param items The list of items
	 */
	public ArrayRecyclerViewAdapter(final Collection<T> items) {
		this();
		mItems.addAll(items);
	}

	/**
	 * Returns the layout id of the item layout inflated by this adapter.
	 *
	 * @param viewType The type of the view
	 * @return A layout id
	 */
	@LayoutRes
	protected abstract int getLayoutId(final int viewType);

	/**
	 * Returns an instance of the view holder of type {@link VH}.
	 *
	 * @param view The inflated item view
	 * @return A ViewHolder object
	 */
	@NonNull
	protected abstract VH createViewHolder(final View view, final int viewType);

	protected abstract void onBindViewHolder(final VH holder, final int position, final T object);

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	@Override
	public VH onCreateViewHolder(final ViewGroup parent, final int viewType) {
		final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		final View view = inflater.inflate(getLayoutId(viewType), parent, false);
		return createViewHolder(view, viewType);
	}

	@Override
	public void onBindViewHolder(final VH holder, final int position) {
		final T t = mItems.get(position);
		onBindViewHolder(holder, position, t);

		// Handle clicks
		if (holder.clickHandler != null) {
			holder.clickHandler.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					int position = holder.getAdapterPosition();
					for (OnClickListener<T> mClickListener : mClickListeners) {
						mClickListener.onItemClicked(position, t);
					}
				}
			});
		}
	}

	public void addOnClickListener(final OnClickListener<T> listener) {
		mClickListeners.add(listener);
	}

	public void removeOnClickListener(final OnClickListener<T> listener) {
		mClickListeners.remove(listener);
	}

	public void setItems(final Collection<T> items) {
		final int oldSize = mItems.size();
		mItems.clear();
		if (items != null) {
			mItems.addAll(items);
		}
		final int newSize = mItems.size();
		if (oldSize != 0 || newSize != 0) {
			notifyDataSetChanged();
		}
	}

	public void addItem(final T item) {
		addItem(mItems.size(), item);
	}

	public void addItem(final int index, final T item) {
		mItems.add(index, item);
		notifyItemInserted(index);
	}

	public void addItems(final Collection<T> items) {
		final int sizeBefore = mItems.size();
		mItems.addAll(items);
		notifyItemRangeInserted(sizeBefore, items.size());
	}

	public void removeItem(final T item) {
		final int index = mItems.indexOf(item);
		if (index >= 0) {
			removeItem(index);
		}
	}

	public void removeItem(final int index) {
		mItems.remove(index);
		notifyItemRemoved(index);
	}

}
