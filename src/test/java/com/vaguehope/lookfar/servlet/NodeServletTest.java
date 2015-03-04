package com.vaguehope.lookfar.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vaguehope.lookfar.auth.PasswdGen;
import com.vaguehope.lookfar.expire.ExpireStatus;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Node;
import com.vaguehope.lookfar.model.Update;
import com.vaguehope.lookfar.threshold.ThresholdStatus;

@RunWith(MockitoJUnitRunner.class)
public class NodeServletTest {

	@Mock private DataStore dataStore;
	@Mock private PasswdGen passwdGen;
	private MockHttpServletRequest req;
	private MockHttpServletResponse resp;
	private NodeServlet undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new NodeServlet(this.dataStore, this.passwdGen);
		this.req = new MockHttpServletRequest();
		this.resp = new MockHttpServletResponse();
	}

	/**
	 * GET /node
	 */
	@Test
	public void itReturnsListOfAllNodes () throws Exception {
		List<Node> nodes = givenSomeNodes(5);
		this.undertest.doGet(this.req, this.resp);
		assertThat(this.resp.getContentAsString(), stringContainsInOrder(getNodeNames(nodes)));
	}

	/**
	 * PUT /node/$nodeName
	 */
	@Test
	public void itCreatesNewNodes () throws Exception {
		this.req.setPathInfo("/my_new_node");
		when(this.passwdGen.makePasswd()).thenReturn("123abc");
		this.undertest.doPut(this.req, this.resp);
		verify(this.dataStore).upsertNode(eq("my_new_node"), startsWith("$2a$"));
		assertThat(this.resp.getContentAsString(), containsString("my_new_node"));
		assertThat(this.resp.getContentAsString(), containsString("123abc"));
		assertEquals(201, this.resp.getStatus());
	}

	/**
	 * DELETE /node/$nodeName
	 */
	@SuppressWarnings("boxing")
	@Test
	public void itDeletesNodes () throws Exception {
		this.req.setPathInfo("/my_node");
		when(this.dataStore.deleteNode("my_node")).thenReturn(1);

		this.undertest.doDelete(this.req, this.resp);

		verify(this.dataStore).deleteNode("my_node");
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * GET /node/$nodeName
	 */
	@Test
	public void itReturnsSingleNode () throws Exception {
		this.req.setPathInfo("/my_node");
		givenSingleNodeWithUpdate("my_node", "some_key", "some value");

		this.undertest.doGet(this.req, this.resp);

		assertThat(this.resp.getContentAsString(), stringContainsInOrder(ImmutableList.of("my_node", "some_key", "some value")));
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * GET /node/$nodeName/$keyName
	 */
	@Test
	public void itReturnsSingleNodeValue () throws Exception {
		this.req.setPathInfo("/my_node/some_key");
		givenSingleNodeWithUpdate("my_node", "some_key", "some value");

		this.undertest.doGet(this.req, this.resp);

		assertEquals("some value", this.resp.getContentAsString());
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * PUT /node/$nodeName/$keyName
	 */
	@Test
	public void itClearsUpdateUpdated () throws Exception {
		this.req.setPathInfo("/my_node/some_key");
		when(this.dataStore.clearUpdateUpdated("my_node", "some_key")).thenReturn(1);

		this.undertest.doPut(this.req, this.resp);

		verify(this.dataStore).clearUpdateUpdated("my_node", "some_key");
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * DELETE /node/$nodeName/$keyName
	 */
	@SuppressWarnings("boxing")
	@Test
	public void itDeletesKey () throws Exception {
		this.req.setPathInfo("/my_node/some_key");
		when(this.dataStore.deleteUpdate("my_node", "some_key")).thenReturn(1);

		this.undertest.doDelete(this.req, this.resp);

		verify(this.dataStore).deleteUpdate("my_node", "some_key");
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * GET /node/$nodeName/$keyName/threshold
	 */
	@Test
	public void itGetsThreshold () throws Exception {
		this.req.setPathInfo("/my_node/some_key/threshold");
		givenSingleNodeWithUpdate("my_node", "some_key", "some value", "==0", null);

		this.undertest.doGet(this.req, this.resp);

		assertEquals("==0", this.resp.getContentAsString());
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * POST /node/$nodeName/$keyName/threshold
	 */
	@SuppressWarnings("boxing")
	@Test
	public void itSetsThreshold () throws Exception {
		this.req.setPathInfo("/my_node/some_key/threshold");
		this.req.setContent("==0".getBytes());
		when(this.dataStore.setThreshold("my_node", "some_key", "==0")).thenReturn(1);

		this.undertest.doPost(this.req, this.resp);

		verify(this.dataStore).setThreshold("my_node", "some_key", "==0");
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * DELETE /node/$nodeName/$keyName/threshold
	 */
	@SuppressWarnings("boxing")
	@Test
	public void itDeletesThreshold () throws Exception {
		this.req.setPathInfo("/my_node/some_key/threshold");
		when(this.dataStore.setThreshold("my_node", "some_key", null)).thenReturn(1);

		this.undertest.doDelete(this.req, this.resp);

		verify(this.dataStore).setThreshold("my_node", "some_key", null);
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * GET /node/$nodeName/$keyName/expire
	 */
	@Test
	public void itGetsExpire () throws Exception {
		this.req.setPathInfo("/my_node/some_key/expire");
		givenSingleNodeWithUpdate("my_node", "some_key", "some value", null, "1h");

		this.undertest.doGet(this.req, this.resp);

		assertEquals("1h", this.resp.getContentAsString());
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * POST /node/$nodeName/$keyName/expire
	 */
	@SuppressWarnings("boxing")
	@Test
	public void itSetsExpire () throws Exception {
		this.req.setPathInfo("/my_node/some_key/expire");
		this.req.setContent("2d".getBytes());
		when(this.dataStore.setExpire("my_node", "some_key", "2d")).thenReturn(1);

		this.undertest.doPost(this.req, this.resp);

		verify(this.dataStore).setExpire("my_node", "some_key", "2d");
		assertEquals(200, this.resp.getStatus());
	}

	/**
	 * DELETE /node/$nodeName/$keyName/expire
	 */
	@SuppressWarnings("boxing")
	@Test
	public void itDeletesExpire () throws Exception {
		this.req.setPathInfo("/my_node/some_key/expire");
		when(this.dataStore.setExpire("my_node", "some_key", null)).thenReturn(1);

		this.undertest.doDelete(this.req, this.resp);

		verify(this.dataStore).setExpire("my_node", "some_key", null);
		assertEquals(200, this.resp.getStatus());
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private List<Node> givenSomeNodes (final int n) throws SQLException {
		List<Node> nodes = Lists.newArrayList();
		for (int i = 0; i < n; i++) {
			nodes.add(new Node("node" + i, new Date()));
		}
		when(this.dataStore.getAllNodes()).thenReturn(nodes);
		return nodes;
	}

	private Update givenSingleNodeWithUpdate (final String nodeName, final String key, final String value) throws Exception {
		return givenSingleNodeWithUpdate(nodeName, key, value, null, null);
	}

	private Update givenSingleNodeWithUpdate (final String nodeName, final String key, final String value, final String threshold, final String expire) throws Exception {
		Update update = new Update(nodeName, new Date(), key, value, threshold, ThresholdStatus.OK, expire, ExpireStatus.OK);
		when(this.dataStore.getUpdate(nodeName, key)).thenReturn(update);
		when(this.dataStore.getUpdates(nodeName)).thenReturn(ImmutableList.of(update));
		return update;
	}

	private static Iterable<String> getNodeNames (final List<Node> nodes) {
		Iterable<String> nodeNames = Collections2.transform(nodes, new Function<Node, String>() {
			@Override
			public String apply (final Node in) {
				return in.getNode();
			}
		});
		return nodeNames;
	}

}
