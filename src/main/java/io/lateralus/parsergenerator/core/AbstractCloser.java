package io.lateralus.parsergenerator.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public abstract class AbstractCloser implements Closer {

	protected final Grammar grammar;

	public AbstractCloser(Grammar grammar) {
		this.grammar = grammar;
	}

	@Override
	public Set<Item> closure(Set<Item> items) {
		Deque<Item> workList = new ArrayDeque<>(items);
		Set<Item> closure = new ClosureSet();
		closure.addAll(items);

		while (!workList.isEmpty()) {
			Item item = workList.pop();
			Symbol expectedSymbol = item.getExpectedSymbol();

			// If there is an expected symbol (i.e. the dot pointer is before the last symbol in the rhs of the rule)
			// and the symbol is non-terminal we must add items for all production / lookahead combinations to our
			// closure. Note that we add it to the work list here, so that if the first symbol of the rhs is a
			// non-terminal we recursively close over them as well.
			if (expectedSymbol == null || expectedSymbol.isTerminal()) {
				continue;
			}

			Set<Terminal> lookaheadSet = determineLookahead(item);
			Set<Production> productions = grammar.getProductions((NonTerminal)expectedSymbol);

			for (Production production : productions) {
				Item newItem = new Item(production, lookaheadSet, 0);
				if (closure.add(newItem)) {
					workList.add(newItem);
				}
			}
		}
		return closure;
	}

	protected abstract Set<Terminal> determineLookahead(Item item);
}
