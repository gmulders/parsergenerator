package io.lateralus.parsergenerator.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a state in a state machine that is built from the {@link Grammar}.
 *
 * The states and the state machine are created as part of the canonical collection (CC) and are used as a state-machine
 * by the parser.
 */
public class State {

	private final Set<Item> items;

	private final Map<Symbol, State> transitions = new HashMap<>();

	public State(Set<Item> items) {
		this.items = items;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof State)) return false;

		State state = (State) o;

		return items.equals(state.items);
	}

	@Override
	public int hashCode() {
		return items.hashCode();
	}

	@Override
	public String toString() {
		String itemsString = items.stream()
				.map(Item::toString)
				.collect(Collectors.joining(", "));
		return "{" + itemsString + "}";
	}

	public Set<Item> getItems() {
		return items;
	}

	public Map<Symbol, State> getTransitions() {
		return transitions;
	}
}
