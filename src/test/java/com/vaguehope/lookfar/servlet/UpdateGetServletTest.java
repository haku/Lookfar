package com.vaguehope.lookfar.servlet;

import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.collect.Lists;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Update;
import com.vaguehope.lookfar.threshold.ThresholdStatus;

@RunWith(MockitoJUnitRunner.class)
public class UpdateGetServletTest {

	@Mock private DataStore dataStore;
	private MockHttpServletRequest req;
	private MockHttpServletResponse resp;
	private UpdateGetServlet undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new UpdateGetServlet(this.dataStore);
		this.req = new MockHttpServletRequest();
		this.resp = new MockHttpServletResponse();
	}

	@Test
	public void itSerialisesSingleUpdate () throws Exception {
		List<Update> update = givenSomeUpdates(1);
		this.undertest.doGet(this.req, this.resp);
		String expected = "[{\"node\":\"node0\",\"updated\":" + update.get(0).getUpdated().getTime() + ",\"key\":\"key0\",\"value\":\"value0\",\"threshold\":\"==0\",\"flag\":\"WARNING\"}]";
		JSONAssert.assertEquals(expected, this.resp.getContentAsString(), false);
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private List<Update> givenSomeUpdates (int n) throws SQLException {
		List<Update> nodes = Lists.newArrayList();
		for (int i = 0; i < n; i++) {
			nodes.add(new Update("node" + i, new Date(), "key" + i, "value" + i, "==0", ThresholdStatus.EXCEEDED));
		}
		when(this.dataStore.getAllUpdates()).thenReturn(nodes);
		return nodes;
	}

}
