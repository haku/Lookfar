package com.vaguehope.lookfar.cli;

import java.util.List;
import java.util.Queue;

public interface Cmd<T> {
	String arg0 ();

	int argCount ();

	List<String> description ();

	Cmd<T> isAliasOf();

	void invoke (T context, String cmd, Queue<String> args) throws Exception;
}
