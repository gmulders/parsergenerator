package io.lateralus.parsergenerator.core;

public class StateTerminal {

	private final State state;
	private final Terminal terminal;

	public StateTerminal(State state, Terminal terminal) {
		this.state = state;
		this.terminal = terminal;
	}

	@Override
	public String toString() {
		return state + " + " + terminal;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StateTerminal that = (StateTerminal) o;

		if (!state.equals(that.state)) return false;
		return terminal.equals(that.terminal);
	}

	@Override
	public int hashCode() {
		int result = state.hashCode();
		result = 31 * result + terminal.hashCode();
		return result;
	}
}
