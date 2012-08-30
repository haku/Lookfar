package com.vaguehope.lookfar.model;

import com.vaguehope.lookfar.expire.ExpireStatus;
import com.vaguehope.lookfar.threshold.ThresholdStatus;

public enum UpdateFlag {

	OK,
	EXPIRED,
	INVALID,
	WARNING, ;

	public static UpdateFlag fromThresholdAndExpire (ThresholdStatus t, ExpireStatus e) {
		if ((t == ThresholdStatus.UNDEFINED || t == ThresholdStatus.OK)
				&& (e == ExpireStatus.OK)) return OK;
		if (e == ExpireStatus.EXPIRED) return EXPIRED;
		if (t == ThresholdStatus.EXCEEDED) return WARNING;
		if (t == ThresholdStatus.INVALID || e == ExpireStatus.INVALID) return INVALID;
		return WARNING;
	}

}
