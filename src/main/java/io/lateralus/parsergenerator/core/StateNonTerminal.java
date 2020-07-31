package io.lateralus.parsergenerator.core;

public class StateNonTerminal {

	private final State state;
	private final NonTerminal nonTerminal;

	public StateNonTerminal(State state, NonTerminal nonTerminal) {
		this.state = state;
		this.nonTerminal = nonTerminal;
	}

	@Override
	public String toString() {
		return state + " + " + nonTerminal;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StateNonTerminal that = (StateNonTerminal) o;

		if (!state.equals(that.state)) return false;
		return nonTerminal.equals(that.nonTerminal);
	}

	@Override
	public int hashCode() {
		int result = state.hashCode();
		result = 31 * result + nonTerminal.hashCode();
		return result;
	}
}
