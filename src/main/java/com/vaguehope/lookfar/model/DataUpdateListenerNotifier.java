package com.vaguehope.lookfar.model;

import java.util.Collection;
import java.util.Map;

public class DataUpdateListenerNotifier implements DataUpdateListener {

	private final Collection<DataUpdateListener> listeners;

	public DataUpdateListenerNotifier (final Collection<DataUpdateListener> listeners) {
		this.listeners = listeners;
	}

	@Override
	public void onUpdate (final String node, final Map<String, String> data) {
		for (final DataUpdateListener l : this.listeners) {
			l.onUpdate(node, data);
		}
	}

}
