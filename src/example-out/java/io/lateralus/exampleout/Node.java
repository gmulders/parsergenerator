package io.lateralus.parsergenerator.test;

import org.gertje.abacus.nodevisitors.NodeVisitor;
import org.gertje.abacus.nodevisitors.VisitingException;
import org.gertje.abacus.token.Token;

/**
 * Represents a node in the AST.
 */
public interface Node {

	/**
	 * Accepts the visitor.
	 *
	 * @param visitor The visitor.
	 * @throws X
	 */
	<R, X extends VisitingException> R accept(NodeVisitor<R, X> visitor) throws X;

	/**
	 * Returns the token.
	 * @return the token.
	 */
	Token getToken();

	/**
	 * Returns the type of the node.
	 * @return the type of the node.
	 */
	NodeType getNodeType();

	/**
	 * Returns an unique id for this node.
	 * @return The unique id for this node.
	 */
	long getId();
}