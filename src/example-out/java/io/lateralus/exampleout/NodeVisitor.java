package io.lateralus.parsergenerator.test;

import org.gertje.abacus.nodes.AddNode;
import org.gertje.abacus.nodes.FactorNode;
import org.gertje.abacus.nodes.IntegerNode;
import org.gertje.abacus.nodes.MultiplyNode;

public interface NodeVisitor<R, X extends VisitingException> {
	R visit(AddNode node) throws X;
	R visit(FactorNode node) throws X;
	R visit(IntegerNode node) throws X;
	R visit(MultiplyNode node) throws X;
}
