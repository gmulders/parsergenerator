package ${parserPackageName}.nodes;

import ${lexerPackageName}.Token;
import ${parserPackageName}.visitor.NodeVisitor;
import ${parserPackageName}.visitor.VisitingException;

public class ${node.className} <#if node.baseClassName??>extends ${node.baseClassName} </#if><#if node.isBinaryNode>implements BinaryOperationNode </#if>{

<#list node.parameterList as parameter>
	private final ${parameter.type} ${parameter.name};
</#list>

	public ${node.className}(<#list node.parameterList as parameter>${parameter.type} ${parameter.name}<#if parameter_has_next>, </#if></#list>) {
<#list node.parameterList as parameter>
		this.${parameter.name} = ${parameter.name};
</#list>
	}

	@Override
	public Token getToken() {
		return ${node.firstTokenName};
	}

	@Override
	public <R, X extends VisitingException> R accept(NodeVisitor<R, X> visitor) throws X {
		return visitor.visit(this);
	}<#list node.parameterList as parameter>

<#if node.isBinaryNode && parameter.name == "lhs" || parameter.name == "rhs">	@Override
</#if>
	public ${parameter.type} get${parameter.nameUpperCase}() {
		return ${parameter.name};
	}</#list>
}
