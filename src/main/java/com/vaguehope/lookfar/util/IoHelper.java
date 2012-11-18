package com.vaguehope.lookfar.util;

import java.io.IOException;
import java.net.Socket;

public class IoHelper {

	private IoHelper () {
		throw new AssertionError();
	}

	public static void closeQuietly (Socket sock) {
		try {
			if (sock != null) sock.close();
		}
		catch (IOException e) { /* Ignore. */ }
	}

}
