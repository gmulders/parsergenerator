package io.lateralus.parsergenerator.test;

import org.gertje.abacus.nodevisitors.NodeVisitor;
import org.gertje.abacus.nodevisitors.VisitingException;
import org.gertje.abacus.token.Token;
import org.gertje.abacus.types.Type;

/**
 * Node that represents an addition.
 */
public class AddNode extends AbstractExpressionNode implements BinaryOperationNode {

	private ExpressionNode lhs;
	private ExpressionNode rhs;

	/**
	 * Constructor
	 */
	public AddNode(ExpressionNode lhs, ExpressionNode rhs, Token token) {
		super (7, token);

		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public Type getType() {
		if (lhs.getType() == rhs.getType()) {
			return lhs.getType();
		}

		if (Type.isNumber(lhs.getType()) && Type.isNumber(rhs.getType())) {
			return Type.DECIMAL;
		}

		if (lhs.getType() == null) {
			return rhs.getType();
		}

		return lhs.getType();
	}

	@Override
	public boolean getIsConstant() {
		return false;
	}

	@Override
	public <R, X extends VisitingException> R accept(NodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}

	public ExpressionNode getLhs() {
		return lhs;
	}

	public void setLhs(ExpressionNode lhs) {
		this.lhs = lhs;
	}

	public ExpressionNode getRhs() {
		return rhs;
	}

	public void setRhs(ExpressionNode rhs) {
		this.rhs = rhs;
	}
}