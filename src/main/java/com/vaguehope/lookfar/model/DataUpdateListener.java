package com.vaguehope.lookfar.model;

import java.util.Map;

public interface DataUpdateListener {

	void onUpdate (String node, Map<String, String> data);

}
