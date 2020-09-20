package io.lateralus.parsergenerator.codegenerator.simple;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.lateralus.parsergenerator.codegenerator.CodeGenerationException;
import io.lateralus.parsergenerator.codegenerator.SourceFile;
import io.lateralus.parsergenerator.core.Grammar;
import io.lateralus.parsergenerator.core.NonTerminal;
import io.lateralus.parsergenerator.core.Production;
import io.lateralus.parsergenerator.core.Symbol;
import io.lateralus.parsergenerator.core.definition.ParserDefinition;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.util.function.Predicate.not;

/**
 * Generates a basic Java parser from a parser definition and a configuration.
 */
public class BasicParserCodeGenerator extends AbstractFreeMarkerCodeGenerator<BasicParserCodeGenerator.Properties, String> {

	private Properties properties;

	public BasicParserCodeGenerator() {
		super(new ClassTemplateLoader(BasicParserCodeGenerator.class, "/templates"));
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public Set<SourceFile<String>> generate(ParserDefinition parserDefinition) throws CodeGenerationException {
		// Create a Set for the source files.
		Set<SourceFile<String>> sourceFiles = new HashSet<>();

		sourceFiles.addAll(createNodes(parserDefinition));
		sourceFiles.addAll(createVisitor(parserDefinition));
		sourceFiles.addAll(createParser(parserDefinition));

		return sourceFiles;
	}

	private Set<SourceFile<String>> createParser(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile<String>> result = new HashSet<>();

		Map<String, Object> model = createBaseModel();
		result.add(createSourceFile("action.ftl", "Action.java", "", model));
		result.add(createSourceFile("parser-exception.ftl", "ParserException.java", "", model));
		result.add(createSourceFile("parser.ftl", "Parser.java", "", model));

		return result;
	}

	private Set<SourceFile<String>> createNodes(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile<String>> result = new HashSet<>();

		for (Node node : createNodeDefinitions(parserDefinition)) {
			Map<String, Object> model = createBaseModel();
			model.put("node", node);
			String fileName = node.className + ".java";
			if (!node.isAbstract) {
				result.add(createSourceFile("type-node.ftl", fileName, "nodes", model));
			} else {
				result.add(createSourceFile("abstract-node.ftl", fileName, "nodes", model));
			}
		}

		result.add(createSourceFile("node.ftl", "Node.java", "nodes", createBaseModel()));
		result.add(createSourceFile("base-node.ftl", "BaseNode.java", "nodes", createBaseModel()));
		result.add(createSourceFile("binary-operation-node.ftl", "BinaryOperationNode.java", "nodes", createBaseModel()));
		return result;
	}

	private Set<SourceFile<String>> createVisitor(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile<String>> result = new HashSet<>();
		result.add(createSourceFile("visiting-exception.ftl", "VisitingException.java", "visitor", createBaseModel()));

		Map<String, Object> model = createBaseModel();
		model.put("nodeTypes", createNodeTypeList(parserDefinition));
		result.add(createSourceFile("node-visitor.ftl", "NodeVisitor.java", "visitor", model));

		return result;
	}

	private List<String> createNodeTypeList(ParserDefinition parserDefinition) {
		return createNodeDefinitions(parserDefinition).stream()
				.filter(not(Node::getIsAbstract))
				.map(Node::getClassName)
				.collect(Collectors.toList());
	}

	private Map<String, Object> createBaseModel() {
		Map<String, Object> model = new HashMap<>();
		model.put("parserPackageName", properties.getParserPackageName());
		model.put("lexerPackageName", properties.getLexerPackageName());
		model.put("parserName", properties.getParserName());
		return model;
	}

	private SourceFile<String> createSourceFile(String templateName, String sourceFileName, String subPackage, Map<String, Object> model)
			throws CodeGenerationException {

		Template template = determineTemplate(templateName);

		StringWriter writer = new StringWriter();

		try {
			template.process(model, writer);
		} catch (TemplateException | IOException e) {
			throw new CodeGenerationException("Could not process the template:", e);
		}

		String packageName = properties.getParserPackageName();
		if (subPackage != null) {
			packageName += "." + subPackage;
		}
		String dirName = packageName.replaceAll("\\.", File.separator);

		return new SimpleSourceFile(dirName + File.separator + sourceFileName, writer.toString());
	}

	private Set<Node> createNodeDefinitions(ParserDefinition parserDefinition) {
		Grammar grammar = parserDefinition.getGrammar();
		Collection<Production> productions = grammar.getProductions();

		Set<Node> nodes = new HashSet<>();

		for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
			Set<Production> nonTerminalProductions = grammar.getProductions(nonTerminal);

			String baseName = "BaseNode";
			for (Production production : productions) {
				if (production.getRhs().size() == 1
						&& production.getRhs().get(0) == nonTerminal) {
					baseName = production.getNodeName();
					if (baseName == null) {
						baseName = production.getLhs().getName();
					}
					baseName += "Node";
					break;
				}
			}

			// Voeg eerst een abstracte node toe met de naam van de NonTerminal.
			nodes.add(new Node(nonTerminal.getName() + "Node", baseName, true, false, null, null));
			baseName = nonTerminal.getName() + "Node";

			// Voeg voor elke een productie bij deze non-terminal een node toe.
			for (Production production : nonTerminalProductions) {
				// Skip producties waarvoor geldt dat de rhs == 1 en rhs(0).isNonTerminal
				if (production.getRhs().size() == 1 && !production.getRhs().get(0).isTerminal()) {
					continue;
				}

				String nodeName = production.getNodeName();
				if (nodeName == null) {
					nodeName = nonTerminal.getName();
				}
				nodeName += "Node";
				List<Parameter> parameterList = determineParameters(production);
				String firstTokenName = determineFirstTokenName(production);
				nodes.add(new Node(
						nodeName,
						baseName,
						false,
						production.isBinary(),
						parameterList,
						firstTokenName));
			}
		}

		return nodes;
	}

	private List<Parameter> determineParameters(Production production) {
		List<Parameter> result = new ArrayList<>();

		for (int i = 0; i< production.getRhs().size(); i++) {
			Symbol symbol = production.getRhs().get(i);
			String rhsName = production.getRhsNames().get(i);
			if (rhsName == null) {
				rhsName = symbol.getName();
			}
			String typeName = "Token";
			if (!symbol.isTerminal()) {
				typeName = symbol.getName() + "Node";
			}
			result.add(new Parameter(toLowerCamel(rhsName), typeName));
		}

		return result;
	}

	private String determineFirstTokenName(Production production) {
		for (int i = 0; i < production.getRhs().size(); i++) {
			Symbol symbol = production.getRhs().get(i);
			String rhsName = production.getRhsNames().get(i);
			if (symbol.isTerminal()) {
				return rhsName != null ? rhsName : symbol.getName();
			}
		}
		return production.getRhsNames().get(0) + "Node.getToken()";
	}

	private static String toLowerCamel(String name) {
		return UPPER_CAMEL.to(LOWER_CAMEL, name);
	}

	private static String toUpperCamel(String name) {
		return LOWER_CAMEL.to(UPPER_CAMEL, name);
	}

	public static class Properties {
		final private String parserName;
		final private String parserPackageName;
		final private String lexerPackageName;

		public Properties(String parserName, String parserPackageName, String lexerPackageName) {
			this.parserName = parserName;
			this.parserPackageName = parserPackageName;
			this.lexerPackageName = lexerPackageName;
		}

		public String getParserName() {
			return parserName;
		}

		public String getParserPackageName() {
			return parserPackageName;
		}

		public String getLexerPackageName() {
			return lexerPackageName;
		}
	}

	public static class Node {
		private final String className;
		private final String baseClassName;
		private final boolean isAbstract;
		private final boolean isBinaryNode;
		private final List<Parameter> parameterList;
		private final String firstTokenName;

		public Node(String className, String baseClassName, boolean isAbstract, boolean isBinaryNode,
				List<Parameter> parameterList, String firstTokenName) {
			this.className = className;
			this.baseClassName = baseClassName;
			this.isAbstract = isAbstract;
			this.isBinaryNode = isBinaryNode;
			this.parameterList = parameterList;
			this.firstTokenName = firstTokenName;
		}

		public String getClassName() {
			return className;
		}

		public String getBaseClassName() {
			return baseClassName;
		}

		public boolean getIsAbstract() {
			return isAbstract;
		}

		public boolean getIsBinaryNode() {
			return isBinaryNode;
		}

		public List<Parameter> getParameterList() {
			return parameterList;
		}

		public String getFirstTokenName() {
			return firstTokenName;
		}
	}

	public static class Parameter {
		private final String name;
		private final String type;

		public Parameter(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public String getNameUpperCase() {
			return toUpperCamel(name);
		}

		public String getType() {
			return type;
		}
	}
}
