package com.phodev.andtools.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支持选择的Adapter
 * 
 * @author sky
 * 
 * @param <K>
 * @param <T>
 */
public abstract class SelectableAdapter<K, T> extends InnerBaseAdapter<T> {
	public boolean setChecked(int position, boolean isChecked) {
		T t = getData(position);
		return innerSetChecked(position, t, isChecked);
	}

	/**
	 * 设置Item被选中
	 * 
	 * @param t
	 * @param isChecked
	 */
	public boolean setCheckedById(T t, boolean isChecked) {
		int position = mData.indexOf(t);
		if (position < 0) {
			return false;
		}
		return innerSetChecked(position, t, isChecked);
	}

	private boolean broadcasting = false;

	private boolean innerSetChecked(int position, T t, boolean isChecked) {
		if (mSelectInterceptor != null && mSelectInterceptor.interceptSelect(t)) {
			return false;
		}
		if (t != null) {
			if (isChecked) {
				selectmap.put(getItemCheckRecordKey(t), t);
			} else {
				selectmap.remove(getItemCheckRecordKey(t));
			}
			if (broadcasting) {
				return true;
			}
			broadcasting = true;
			onSelectChanged(position, isChecked);
			if (mChangedListener != null) {
				mChangedListener.onSelectChanged(this, position, t, isChecked);
			}
			broadcasting = false;
		}
		return true;
	}

	/**
	 * 选中所有
	 */
	public void checkAll() {
		if (mData != null) {
			int size = mData.size();
			for (int i = 0; i < size; i++) {
				T t = mData.get(i);
				if (t != null) {
					selectmap.put(getItemCheckRecordKey(t), t);
					onSelectChanged(i, true);
				}
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 取消所有选中状态
	 * 
	 * @param notifySelectHolder
	 */
	public void unCheckAll(boolean notifySelectHolder) {
		if (notifySelectHolder && mData != null) {
			int size = mData.size();
			for (int i = 0; i < size; i++) {
				T t = mData.get(i);
				if (t != null) {
					onSelectChanged(i, false);
				}
			}
		}
		selectmap.clear();
		notifyDataSetChanged();
	}

	public List<T> getChecked() {
		if (mData == null || mData.size() <= 0 || selectmap.size() <= 0) {
			return null;
		}
		List<T> result = new ArrayList<T>();
		for (T t : mData) {
			if (selectmap.containsValue(t)) {
				result.add(t);
			}
		}
		return result;
	}

	public int getCheckedCount() {
		return selectmap.size();
	}

	/**
	 * 被选中的Item会被放到一个Map<Key,ItemData>,该方法返回之作为这个map中的key
	 * 
	 * @param t
	 * @return
	 */
	protected abstract K getItemCheckRecordKey(T t);

	protected final Map<K, T> selectmap = new HashMap<K, T>();

	/**
	 * 是否被选中
	 * 
	 * @param key
	 * @return
	 */
	public boolean isCheckedKey(K key) {
		return selectmap.get(key) != null;
	}

	public boolean isCheckedData(T t) {
		return selectmap.get(getItemCheckRecordKey(t)) != null;
	}

	protected void onSelectChanged(int position, boolean isSelected) {
	}

	private boolean mSelectModel = false;
	private boolean mBroadcasting = false;

	public void setSelectModel(boolean selectMode) {
		if (mSelectModel == selectMode) {
			return;
		}
		this.mSelectModel = selectMode;
		if (!mBroadcasting) {
			mBroadcasting = true;
			onSelectModelChanged(selectMode);
			mBroadcasting = false;
		}
		if (!mSelectModel) {
			selectmap.clear();
		}
	}

	public boolean isSelectModel() {
		return mSelectModel;
	}

	protected abstract void onSelectModelChanged(boolean selectModel);

	public interface OnSelectChangedListener<Data> {
		public void onSelectChanged(SelectableAdapter<?, Data> adapter,
				int position, Data item, boolean checked);
	}

	public interface SelectInterceptor<D> {
		public boolean interceptSelect(D data);
	}

	private SelectInterceptor<T> mSelectInterceptor;

	public void setSelectInterceptor(SelectInterceptor<T> interceptor) {
		mSelectInterceptor = interceptor;
	}

	private OnSelectChangedListener<T> mChangedListener;

	public void setSelectChangedListener(OnSelectChangedListener<T> l) {
		mChangedListener = l;
	}
}
