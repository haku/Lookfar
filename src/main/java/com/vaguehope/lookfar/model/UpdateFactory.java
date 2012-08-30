package com.vaguehope.lookfar.model;

import java.util.Date;

import com.vaguehope.lookfar.expire.ExpireParser;
import com.vaguehope.lookfar.expire.ExpireStatus;
import com.vaguehope.lookfar.threshold.ThresholdParser;
import com.vaguehope.lookfar.threshold.ThresholdStatus;

public class UpdateFactory {

	private final ThresholdParser thresholdParser;
	private final ExpireParser expireParser;

	public UpdateFactory (ThresholdParser thresholdParser, ExpireParser expireParser) {
		this.thresholdParser = thresholdParser;
		this.expireParser = expireParser;
	}

	public Update makeUpdate (String node, Date updated, String key, String value, String threshold, String expire) {
		ThresholdStatus thresholdStatus = this.thresholdParser.parseThreshold(threshold).isValid(value);
		ExpireStatus expireStatus = this.expireParser.parseExpire(expire).isValid(updated);
		return new Update(node, updated, key, value, threshold, thresholdStatus, expire, expireStatus);
	}

}
