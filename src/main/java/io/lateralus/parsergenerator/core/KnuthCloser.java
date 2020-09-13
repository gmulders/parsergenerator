package io.lateralus.parsergenerator.core;

import java.util.List;
import java.util.Set;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;

/**
 * Calculates the closure as defined by Knuth.
 */
public class KnuthCloser extends AbstractCloser {

	public KnuthCloser(Grammar grammar) {
		super(grammar);
	}

	protected Set<Terminal> determineLookahead(Production production, Set<Terminal> lookahead, int position) {
		int nextPosition = position + 1;
		List<Symbol> rhs = production.getRhs();

		// If the next position is equal to the length of the rhs the lookahead is the lookahead of the item.
		if (nextPosition == rhs.size()) {
			return lookahead;
		}

		// Create a sublist that holds the symbols after the dot pointer in the item. Note that since the sublist is
		// basically a view on a list this is a cheap operation. Alternatively we could have the iteration in
		// calculateFirstSetForRemainingSymbols start at the next position.
		List<Symbol> remainingSymbols = rhs.subList(nextPosition, rhs.size());
		Set<Terminal> extraLookahead = grammar.calculateFirstSetForRemainingSymbols(remainingSymbols);

		if (extraLookahead.remove(EPSILON)) {
			extraLookahead.addAll(lookahead);
		}

		return extraLookahead;
	}
}
