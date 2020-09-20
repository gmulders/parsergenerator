package ${parserPackageName};

import ${parserPackageName}.nodes.Node;

import static ${parserPackageName}.Action.ActionType.ACCEPT;
import static ${parserPackageName}.Action.ActionType.REDUCE;
import static ${parserPackageName}.Action.ActionType.SHIFT;

public class Action {

	public final ActionType type;
	public final Reduction reduction;
	public final int size;
	public final int state;
	public final int productionId;

	public static Action accept() {
		return new Action(ACCEPT, null, -1, -1, -1);
	}

	public static Action reduce(Reduction reduction, int size, int productionId) {
		return new Action(REDUCE, reduction, size, productionId, -1);
	}

	public static Action shift(int state) {
		return new Action(SHIFT, null, -1, -1, state);
	}

	private Action(ActionType type, Reduction reduction, int size, int productionId, int state) {
		this.type = type;
		this.reduction = reduction;
		this.size = size;
		this.productionId = productionId;
		this.state = state;
	}

	public enum ActionType {
		SHIFT,
		REDUCE,
		ACCEPT
	}

	public interface Reduction {
		<T extends Node> T reduce(Object[] items);
	}
}
