package com.vaguehope.lookfar.model;

import com.vaguehope.lookfar.expire.ExpireStatus;
import com.vaguehope.lookfar.threshold.ThresholdStatus;

public enum UpdateFlag {

	PENDING,
	OK,
	EXPIRED,
	INVALID,
	WARNING, ;

	public static UpdateFlag fromThresholdAndExpire (final ThresholdStatus t, final ExpireStatus e) {
		if ((t == ThresholdStatus.UNDEFINED || t == ThresholdStatus.OK) && e == ExpireStatus.OK) return OK;
		if (e == ExpireStatus.EXPIRED) return EXPIRED;
		if (e == ExpireStatus.PENDING) return PENDING;
		if (t == ThresholdStatus.EXCEEDED) return WARNING;
		if (t == ThresholdStatus.INVALID) return INVALID;
		return WARNING;
	}

}
