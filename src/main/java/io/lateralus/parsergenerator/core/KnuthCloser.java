package io.lateralus.parsergenerator.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;

/**
 * Calculates the closure as defined by Knuth.
 */
public class KnuthCloser implements Closer {

	private final Grammar grammar;

	public KnuthCloser(Grammar grammar) {
		this.grammar = grammar;
	}

	public Set<Item> closure(Set<Item> items) {
		Deque<Item> workList = new ArrayDeque<>(items);

		// Here I previously also used a LinkedHashSet to keep insertion order. I don't remember why I did this. So I
		// removed it. Test to make sure that it is ok.
		Set<Item> closure = new /*Linked*/HashSet<>();
		while (!workList.isEmpty()) {
			Item item = workList.pop();
			closure.add(item);

			Symbol expectedSymbol = item.getExpectedSymbol();

			// If there is an expected symbol (i.e. the dot pointer is before the last symbol in the rhs of the rule)
			// and the symbol is non-terminal we must add items for all production / lookahead combinations to our
			// closure. Note that we add it to the work list here, so that if the first symbol of the rhs is a
			// non-terminal we recursively close over them as well.
			if (expectedSymbol != null && !expectedSymbol.isTerminal()) {
				Set<Terminal> lookaheadSet = determineLookahead(item);
				Set<Production> productions = grammar.getProductions((NonTerminal)expectedSymbol);

				for (Production production : productions) {
					Item newItem = new Item(production, lookaheadSet, 0);
					workList.add(newItem);
				}
			}
		}

		return closure;
	}

	private Set<Terminal> determineLookahead(Item item) {
		int nextPosition = item.getPosition() + 1;
		List<Symbol> rhs = item.getProduction().getRhs();

		// If the next position is equal to the length of the rhs the lookahead is the lookahead of the item.
		if (nextPosition == rhs.size()) {
			return item.getLookahead();
		}

		// Create a sublist that holds the symbols after the dot pointer in the item. Note that since the sublist is
		// basically a view on a list this is a cheap operation. Alternatively we could have the iteration in
		// calculateFirstSetForRemainingSymbols start at the next position.
		List<Symbol> remainingSymbols = rhs.subList(nextPosition, rhs.size());
		Set<Terminal> lookahead = grammar.calculateFirstSetForRemainingSymbols(remainingSymbols);

		if (lookahead.remove(EPSILON)) {
			lookahead.addAll(item.getLookahead());
		}

		return lookahead;
	}
}
