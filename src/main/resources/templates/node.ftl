package ${parserPackageName}.nodes;

import ${lexerPackageName}.Token;
import ${parserPackageName}.visitor.NodeVisitor;
import ${parserPackageName}.visitor.VisitingException;

public interface Node {

	Token getToken();

	<R, X extends VisitingException> R accept(NodeVisitor<R, X> visitor) throws X;
}
