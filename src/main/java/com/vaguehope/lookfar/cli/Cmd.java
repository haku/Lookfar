package com.vaguehope.lookfar.cli;

import java.util.Queue;

public interface Cmd<T> {
	String arg0 ();

	int argCount ();

	void invoke (T context, String cmd, Queue<String> args) throws Exception;
}