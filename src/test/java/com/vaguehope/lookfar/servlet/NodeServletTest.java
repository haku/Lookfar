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
import com.google.common.collect.Lists;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Node;

@RunWith(MockitoJUnitRunner.class)
public class NodeServletTest {

	@Mock private DataStore dataStore;
	private MockHttpServletRequest req;
	private MockHttpServletResponse resp;
	private NodeServlet undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new NodeServlet(this.dataStore);
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
		this.undertest.doPut(this.req, this.resp);
		verify(this.dataStore).upsertNode(eq("my_new_node"), startsWith("$2a$"));
		assertThat(this.resp.getContentAsString(), containsString("my_new_node"));
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

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private List<Node> givenSomeNodes (int n) throws SQLException {
		List<Node> nodes = Lists.newArrayList();
		for (int i = 0; i < n; i++) {
			nodes.add(new Node("node" + i, new Date()));
		}
		when(this.dataStore.getAllNodes()).thenReturn(nodes);
		return nodes;
	}

	private static Iterable<String> getNodeNames (List<Node> nodes) {
		Iterable<String> nodeNames = Collections2.transform(nodes, new Function<Node, String>() {
			@Override
			public String apply (Node in) {
				return in.getNode();
			}
		});
		return nodeNames;
	}

}
