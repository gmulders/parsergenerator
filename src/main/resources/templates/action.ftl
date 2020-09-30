package ${parserPackageName};

import ${parserPackageName}.nodes.Node;

public abstract class Action {

	public static class Accept extends Action {
		private Accept() {}
	}

	public static class Reduce extends Action {
		public final Reduction reduction;
		public final int size;
		public final int id;

		private Reduce(Reduction reduction, int size, int id) {
			this.reduction = reduction;
			this.size = size;
			this.id = id;
		}
	}

	public static class Shift extends Action {
		public final int state;

		private Shift(int state) {
			this.state = state;
		}
	}

	public static Action accept() {
		return new Accept();
	}

	public static Action reduce(Reduction reduction, int size, int id) {
		return new Reduce(reduction, size, id);
	}

	public static Action shift(int state) {
		return new Shift(state);
	}

	public interface Reduction<T extends Node> {
		T reduce(Object[] items);
	}
}
