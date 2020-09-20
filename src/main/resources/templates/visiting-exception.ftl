package ${parserPackageName}.visitor;

import ${parserPackageName}.nodes.Node;

public class VisitingException extends Exception {

	private final Node node;

	public VisitingException(String message, Node node, Exception cause) {
		super(message, cause);
		this.node = node;
	}

	public VisitingException(String message, Node node) {
		super(message);
		this.node = node;
	}

	public Node getNode() {
		return node;
	}
}
