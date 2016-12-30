package com.vaguehope.lookfar.android.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaguehope.lookfar.android.R;
import com.vaguehope.lookfar.android.util.TimeHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class UpdateAdapter extends BaseAdapter {

	private static final Set<String> OK_STATUSES = new HashSet<String>(Arrays.asList("OK", "PENDING"));

	private final List<Update> data = new ArrayList<Update>();
	private final List<Update> filteredData = new ArrayList<Update>();
	private final LayoutInflater layoutInflater;
	private boolean showAll;

	public UpdateAdapter (final Context context) {
		this.layoutInflater = LayoutInflater.from(context);
	}

	public void setData (final List<Update> newData) {
		this.data.clear();
		this.data.addAll(newData);
		updateFiltered();
		notifyDataSetChanged();
	}

	public void setShowAll (final boolean showAll) {
		this.showAll = showAll;
		updateFiltered();
		notifyDataSetChanged();
	}

	private void updateFiltered () {
		this.filteredData.clear();
		for (Update u : this.data) {
			if (this.showAll || !OK_STATUSES.contains(u.getFlag())) this.filteredData.add(u);
		}
	}

	public Update getUpdate (final int position) {
		if (this.filteredData == null) return null;
		if (position >= this.filteredData.size()) return null;
		return this.filteredData.get(position);
	}

	@Override
	public int getCount () {
		return this.filteredData == null ? 0 : this.filteredData.size();
	}

	@Override
	public Object getItem (final int position) {
		return getUpdate(position);
	}

	@Override
	public long getItemId (final int position) {
		return position;
	}

	@Override
	public View getView (final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		RowView rowView;
		if (view == null) {
			view = this.layoutInflater.inflate(R.layout.updatelistrow, null);
			rowView = new RowView(view);
			view.setTag(rowView);
		}
		else {
			rowView = (RowView) view.getTag();
		}
		rowView.setItem(this.filteredData.get(position));
		return view;
	}

	private static class RowView {

		private final TextView node;
		private final TextView flag;
		private final TextView key;
		private final TextView updated;
		private final TextView value;

		public RowView (final View view) {
			this((TextView) view.findViewById(R.id.txtNode),
					(TextView) view.findViewById(R.id.txtFlag),
					(TextView) view.findViewById(R.id.txtKey),
					(TextView) view.findViewById(R.id.txtUpdated),
					(TextView) view.findViewById(R.id.txtValue));
		}

		public RowView (final TextView node, final TextView flag, final TextView key, final TextView updated, final TextView value) {
			this.node = node;
			this.flag = flag;
			this.key = key;
			this.updated = updated;
			this.value = value;
		}

		public void setItem (final Update item) {
			this.node.setText(item.getNode());
			this.flag.setText(item.getFlag());
			this.key.setText(item.getKey());
			this.updated.setText(TimeHelper.humanTimeSpan(item.getUpdated(), System.currentTimeMillis()));
			this.value.setText(item.getValue());
		}
	}

}
