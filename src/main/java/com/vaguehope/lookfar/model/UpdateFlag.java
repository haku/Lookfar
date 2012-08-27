package com.vaguehope.lookfar.model;

import com.vaguehope.lookfar.threshold.ThresholdStatus;

public enum UpdateFlag {

	OK,
	EXPIRED,
	INVALID,
	WARNING,
	;

	public static UpdateFlag fromThreshold (ThresholdStatus thresholdStatus) {
		switch (thresholdStatus) {
			case UNDEFINED:
			case OK:
				return OK;

			case INVALID:
				return INVALID;

			case EXCEEDED:
			default:
				return WARNING;
		}
	}

}
