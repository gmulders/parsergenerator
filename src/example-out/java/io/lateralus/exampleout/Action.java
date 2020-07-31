package io.lateralus.parsergenerator.test;

public class Action {

	public final ActionType type;
	public final Reduction reduction;
	public final int size;
	public final int state;

	private Action(ActionType type, Reduction reduction, int size, int state) {
		this.type = type;
		this.reduction = reduction;
		this.size = size;
		this.state = state;
	}

	public enum ActionType {
		SHIFT,
		REDUCE,
		ACCEPT
	}

	public interface Reduction {
		<T> T reduce(Node[] nodes);
	}
}
