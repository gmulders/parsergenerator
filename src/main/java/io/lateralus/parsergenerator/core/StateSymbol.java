package io.lateralus.parsergenerator.core;

public class StateSymbol {

	private final State state;
	private final Symbol symbol;

	public StateSymbol(State state, Symbol symbol) {
		this.state = state;
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return "StateSymbol{" +
				"state=" + state +
				", symbol=" + symbol +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StateSymbol that = (StateSymbol) o;

		if (!state.equals(that.state)) return false;
		return symbol.equals(that.symbol);
	}

	@Override
	public int hashCode() {
		int result = state.hashCode();
		result = 31 * result + symbol.hashCode();
		return result;
	}

	public State getState() {
		return state;
	}

	public Symbol getSymbol() {
		return symbol;
	}
}
