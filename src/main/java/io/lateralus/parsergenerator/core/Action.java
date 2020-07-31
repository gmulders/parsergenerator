package io.lateralus.parsergenerator.core;

import java.util.Objects;

/**
 * Represents an action in the action table.
 *
 * The parser will be looking at the top of the state stack and from this and the next token read it will determine what
 * action to do, on the stack, next. There are three options;
 * - SHIFT; shifts the token and the given state to the stack
 * - REDUCE; removes length(production.rhs) states and symbols from the stack and replaces them with the lhs NonTerminal
 *   of the production
 * - ACCEPT; parsing is done and the given syntax is accepted
 */
public class Action {

	private final ActionType actionType;
	private final Production production;
	private final State state;

	public static Action shift(State state) {
		return new Action(ActionType.SHIFT, null, state);
	}

	public static Action reduce(Production production) {
		return new Action(ActionType.REDUCE, production, null);
	}

	public static Action accept() {
		return new Action(ActionType.ACCEPT, null, null);
	}

	private Action(ActionType actionType, Production production, State state) {
		this.actionType = actionType;
		this.production = production;
		this.state = state;
	}

	@Override
	public String toString() {
		String value;
		switch (actionType) {
			case SHIFT:
				value = state.toString();
				break;
			case ACCEPT:
				value = "";
				break;
			case REDUCE:
				value = production.toString();
				break;
			default:
				value = "ERROR";
		}
		return actionType + "(" + value + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Action action = (Action) o;
		return actionType == action.actionType &&
				Objects.equals(production, action.production) &&
				state.equals(action.state);
	}

	@Override
	public int hashCode() {
		return Objects.hash(actionType, production, state);
	}

	public ActionType getActionType() {
		return actionType;
	}

	public Production getProduction() {
		return production;
	}

	public State getState() {
		return state;
	}
}
