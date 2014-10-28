package com.vaguehope.lookfar.servlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaguehope.lookfar.model.DataStore;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTailServletTest {

	@Mock private DataStore dataStore;
	private UpdateTailServlet undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new UpdateTailServlet(this.dataStore);
	}

	@Test
	public void itDoesSomething () throws Exception {
		// TODO
	}

}
