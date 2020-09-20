package io.lateralus.parsergenerator.core.definition;

import io.lateralus.parsergenerator.core.Grammar;

public class ParserDefinition {

	private final Grammar grammar;

	public ParserDefinition(Grammar grammar) {
		this.grammar = grammar;
	}

	public Grammar getGrammar() {
		return grammar;
	}
}
