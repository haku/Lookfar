package com.vaguehope.lookfar.model;

import java.util.Date;

import com.google.common.base.Objects;

public class Node {

	private final String node;
	private final Date updated;

	public Node (String node, Date updated) {
		this.node = node;
		this.updated = updated;
	}

	public String getNode () {
		return this.node;
	}

	public Date getUpdated () {
		return this.updated;
	}

	@Override
	public String toString () {
		return Objects.toStringHelper(Node.class)
				.add("node", this.node)
				.add("updated", this.updated)
				.toString();
	}

	@Override
	public boolean equals (Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (!(o instanceof Node)) return false;
		Node that = (Node) o;
		return Objects.equal(this.node, that.node) &&
				Objects.equal(this.updated, that.updated);
	}

	@Override
	public int hashCode () {
		return Objects.hashCode(this.node, this.updated);
	}

}
