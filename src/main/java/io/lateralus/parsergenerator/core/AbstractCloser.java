package io.lateralus.parsergenerator.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;

public abstract class AbstractCloser implements Closer {

	protected final Grammar grammar;

	public AbstractCloser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public Set<Item> closure(Set<Item> items) {
		Map<LR0Item, Set<Terminal>> closure = new HashMap<>();
		Deque<LR0Item> workList = new ArrayDeque<>();
		for (Item item : items) {
			LR0Item lr0Item = new LR0Item(item);
			workList.add(lr0Item);
			closure.put(lr0Item, item.getLookahead());
		}

		while (!workList.isEmpty()) {
			LR0Item lr0item = workList.pop();
			Symbol expectedSymbol = lr0item.getExpectedSymbol();

			// If there is an expected symbol (i.e. the dot pointer is before the last symbol in the rhs of the rule)
			// and the symbol is non-terminal we must add items for all production / lookahead combinations to our
			// closure. Note that we add it to the work list here, so that if the first symbol of the rhs is a
			// non-terminal we recursively close over them as well.
			if (expectedSymbol == null || expectedSymbol.isTerminal()) {
				continue;
			}

			Set<Terminal> lookaheadSet = determineLookahead(lr0item.production, closure.get(lr0item), lr0item.position);
			Set<Production> productions = grammar.getProductions((NonTerminal)expectedSymbol);
			for (Production production : productions) {
				LR0Item newItem = new LR0Item(production, 0);
				boolean isNew = !closure.containsKey(newItem);
				Set<Terminal> currentLookaheadSet = closure.computeIfAbsent(newItem, item -> new HashSet<>());
				if (currentLookaheadSet.addAll(lookaheadSet) || isNew) {
					workList.add(newItem);
				}
			}
		}

		return closure.entrySet().stream()
				.map(entry -> new Item(entry.getKey().production, entry.getValue(), entry.getKey().position))
				.collect(Collectors.toSet());
	}

	protected abstract Set<Terminal> determineLookahead(Production production, Set<Terminal> lookahead, int position);

	private static class LR0Item {
		private final Production production;
		private final int position;

		public LR0Item(Production production, int position) {
			this.production = production;
			this.position = position;
		}

		private LR0Item(Item item) {
			this.production = item.getProduction();
			this.position = item.getPosition();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LR0Item that = (LR0Item) o;
			return position == that.position &&
					production.equals(that.production);
		}

		@Override
		public int hashCode() {
			return Objects.hash(production, position);
		}

		public Symbol getExpectedSymbol() {
			if (production.getRhs().size() == position) {
				return null;
			}
			Symbol expectedSymbol = production.getRhs().get(position);
			// Don't return EPSILON, since it is not a real non-terminal; the lexer will never return an "epsilon" token.
			if (expectedSymbol == EPSILON) {
				return null;
			}
			return expectedSymbol;
		}
	}
}
