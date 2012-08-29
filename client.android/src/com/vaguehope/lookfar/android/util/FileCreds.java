package com.vaguehope.lookfar.android.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.vaguehope.lookfar.android.util.HttpHelper.HttpCreds;

public class FileCreds implements HttpCreds {

	private final String user;
	private final String pass;

	public FileCreds (String path) throws IOException {
		File file = new File(path);
		List<String> list = FileHelper.fileToList(file);
		this.user = list.size() >= 1 ? list.get(0) : null;
		this.pass = list.size() >= 2 ? list.get(1) : null;
	}

	@Override
	public String getUser () {
		return this.user;
	}

	@Override
	public String getPass () {
		return this.pass;
	}

}
