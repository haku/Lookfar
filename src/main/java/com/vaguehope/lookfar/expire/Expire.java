package com.vaguehope.lookfar.expire;

import java.util.Date;

public interface Expire {

	ExpireStatus isValid (Date updated);

}
