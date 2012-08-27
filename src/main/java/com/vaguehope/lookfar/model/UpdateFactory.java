package com.vaguehope.lookfar.model;

import java.util.Date;

import com.vaguehope.lookfar.threshold.ThresholdParser;
import com.vaguehope.lookfar.threshold.ThresholdStatus;

public class UpdateFactory {

	private final ThresholdParser thresholdParser;

	public UpdateFactory (ThresholdParser thresholdParser) {
		this.thresholdParser = thresholdParser;
	}

	public Update makeUpdate (String node, Date updated, String key, String value, String threshold) {
		ThresholdStatus thresholdStatus = this.thresholdParser.parseThreshold(threshold).isValid(value);
		return new Update(node, updated, key, value, threshold, thresholdStatus);
	}

}
