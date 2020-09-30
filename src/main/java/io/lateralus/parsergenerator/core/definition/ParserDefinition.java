package io.lateralus.parsergenerator.core.definition;

import com.google.common.collect.Table;
import io.lateralus.parsergenerator.core.Action;
import io.lateralus.parsergenerator.core.Grammar;
import io.lateralus.parsergenerator.core.NonTerminal;
import io.lateralus.parsergenerator.core.State;
import io.lateralus.parsergenerator.core.Terminal;

import java.util.List;

public class ParserDefinition {

	private final Grammar grammar;

	private final Table<State, Terminal, Action> actionTable;

	private final Table<State, NonTerminal, State> gotoTable;

	private final List<Terminal> orderedTerminalList;

	public ParserDefinition(Grammar grammar,
			Table<State, Terminal, Action> actionTable,
            Table<State, NonTerminal, State> gotoTable,
            List<Terminal> orderedTerminalList) {
		this.grammar = grammar;
		this.actionTable = actionTable;
		this.gotoTable = gotoTable;
		this.orderedTerminalList = orderedTerminalList;
	}

	public Grammar getGrammar() {
		return grammar;
	}

	public Table<State, Terminal, Action> getActionTable() {
		return actionTable;
	}

	public Table<State, NonTerminal, State> getGotoTable() {
		return gotoTable;
	}

	public List<Terminal> getOrderedTerminalList() {
		return orderedTerminalList;
	}
}
