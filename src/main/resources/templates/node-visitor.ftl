package ${parserPackageName}.visitor;

<#list nodeTypes as nodeType>
import ${parserPackageName}.nodes.${nodeType};
</#list>

public interface NodeVisitor<R, X extends VisitingException> {
<#list nodeTypes as nodeType>
	R visit(${nodeType} node) throws X;
</#list>
}
