package com.vaguehope.lookfar.servlet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.vaguehope.lookfar.model.DataStore;
import com.vaguehope.lookfar.model.Node;

@RunWith(MockitoJUnitRunner.class)
public class NodeServletTest {

	@Mock private DataStore dataStore;

	@Mock private HttpServletRequest req;
	@Mock private HttpServletResponse resp;
	private StringWriter responseWriter;

	private NodeServlet undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new NodeServlet(this.dataStore);
		this.responseWriter = new StringWriter();
		when(this.resp.getWriter()).thenReturn(new PrintWriter(this.responseWriter));
	}

	/**
	 * GET /node
	 */
	@Test
	public void itReturnsListOfAllNodes () throws Exception {
		List<Node> nodes = givenSomeNodes(5);
		this.undertest.doGet(this.req, this.resp);
		assertThat(this.responseWriter.toString(), stringContainsInOrder(getNodeNames(nodes)));
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
