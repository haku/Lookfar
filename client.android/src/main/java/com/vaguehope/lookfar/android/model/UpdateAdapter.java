package com.vaguehope.lookfar.android.model;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vaguehope.lookfar.android.R;
import com.vaguehope.lookfar.android.util.TimeHelper;

public class UpdateAdapter extends BaseAdapter {

	private final List<Update> updates;
	private final LayoutInflater layoutInflater;

	public UpdateAdapter (final Context context, final List<Update> updates) {
		this.updates = updates;
		this.layoutInflater = LayoutInflater.from(context);
	}

	public Update getUpdate (final int position) {
		if (this.updates == null) return null;
		if (position >= this.updates.size()) return null;
		return this.updates.get(position);
	}

	@Override
	public int getCount () {
		return this.updates == null ? 0 : this.updates.size();
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
		rowView.setItem(this.updates.get(position));
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
