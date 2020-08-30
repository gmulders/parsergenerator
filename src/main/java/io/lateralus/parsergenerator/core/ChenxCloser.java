package io.lateralus.parsergenerator.core;

import com.google.common.collect.Sets;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;
import static java.util.function.Predicate.not;

/**
 * Calculates the closure as discussed in the dissertation of Xin Chen (Measuring and extending LR(1) parser generation)
 */
public class ChenxCloser implements Closer {

	private final Grammar grammar;

	private final Map<Symbol, Boolean> canVanishMap;

	private ChenxCloser(Grammar grammar, Map<Symbol, Boolean> canVanishMap) {
		this.grammar = grammar;
		this.canVanishMap = canVanishMap;
	}

	@Override
	public Set<Item> closure(Set<Item> items) {
		Deque<Item> workList = new ArrayDeque<>(items);

		Set<Item> closure = new HashSet<>();
		while (!workList.isEmpty()) {
			Item item = workList.pop();
			closure.add(item);

			Symbol expectedSymbol = item.getExpectedSymbol();

			// If there is an expected symbol (i.e. the dot pointer is before the last symbol in the rhs of the rule)
			// and the symbol is non-terminal we must add items for all production / lookahead combinations to our
			// closure. Note that we add it to the work list here, so that if the first symbol of the rhs is a
			// non-terminal we recursively close over them as well.
			if (expectedSymbol == null || expectedSymbol.isTerminal()) {
				continue;
			}

			Set<Terminal> lookaheadSet = determineContext(item);
			Set<Production> productions = grammar.getProductions((NonTerminal)expectedSymbol);

			for (Production production : productions) {
				Optional<Item> optionalExistingItemForProduction = closure.stream()
						.filter(closureItem -> closureItem.getPosition() == 0 && closureItem.getProduction().equals(production))
						.findAny();

				if (optionalExistingItemForProduction.isPresent()) {
					Item existingItemForProduction = optionalExistingItemForProduction.get();
					if (existingItemForProduction.getLookahead().containsAll(lookaheadSet)) {
						continue;
					}
					closure.remove(existingItemForProduction);
					lookaheadSet = Sets.union(lookaheadSet, existingItemForProduction.getLookahead());
				}
				Item newItem = new Item(production, lookaheadSet, 0);
				workList.add(newItem);
			}
		}

		return closure;
	}

	private Set<Terminal> determineContext(Item item) {
		int nextPosition = item.getPosition() + 1;
		List<Symbol> rhs = item.getProduction().getRhs();

		// If the next position is equal to the length of the rhs the lookahead is the lookahead of the item.
		if (nextPosition == rhs.size()) {
			return item.getLookahead();
		}

		List<Symbol> remainingSymbols = rhs.subList(nextPosition, rhs.size());
		Set<Terminal> tHeads = determineTHeads(remainingSymbols);

		if (tHeads.isEmpty()) {
			return item.getLookahead();
		}

		if (tHeads.remove(EPSILON)) {
			tHeads.addAll(item.getLookahead());
		}

		return tHeads;
	}

	private Set<Terminal> determineTHeads(List<Symbol> remainingSymbols) {
		Set<Terminal> tHeads = new HashSet<>();
		Set<NonTerminal> heads = new HashSet<>();

		insertAlphaToHeads(remainingSymbols, heads, tHeads);

		for (NonTerminal nonTerminal : heads) {
			Set<Production> productions = grammar.getProductions(nonTerminal);
			for (Production production : productions) {
				List<Symbol> rhs = production.getRhs();
				if (rhs.size() == 1 && rhs.contains(EPSILON)) {
					continue;
				}
				insertStringToHeads(production.getRhs(), heads, tHeads);
			}
		}
		return tHeads;
	}

	private void insertAlphaToHeads(List<Symbol> string, Set<NonTerminal> heads, Set<Terminal> tHeads) {
		if (insertStringToHeads(string, heads, tHeads)) {
			tHeads.add(EPSILON);
		}
	}

	private boolean insertStringToHeads(List<Symbol> string, Set<NonTerminal> heads, Set<Terminal> tHeads) {
		for (Symbol symbol : string) {
			if (canVanish(symbol)) {
				heads.add((NonTerminal)symbol);
				continue;
			}
			if (symbol.isTerminal()) {
				tHeads.add((Terminal)symbol);
			} else {
				heads.add((NonTerminal)symbol);
			}
			return false;
		}
		return true;
	}

	private boolean canVanish(Symbol symbol) {
		Boolean value = canVanishMap.get(symbol);
		return value != null && value;
	}

	public static Builder builder(Grammar grammar) {
		return new Builder(grammar);
	}

	public static class Builder {

		private final Grammar grammar;

		Map<Symbol, Boolean> canVanishMap = new HashMap<>();

		public Builder(Grammar grammar) {
			this.grammar = grammar;
		}

		public ChenxCloser build() {
			fillCanVanishMap();
			return new ChenxCloser(grammar, canVanishMap);
		}

		private void fillCanVanishMap() {
			grammar.getTerminals()
					.forEach(terminal -> canVanishMap.put(terminal, false));
			canVanishMap.put(EPSILON, true);

			boolean newFound = true;
			while(newFound) {
				newFound = false;

				Set<NonTerminal> nonTerminals = getNonVanishableNonTerminals();
				for (NonTerminal lhs : nonTerminals) {
					for (Production production : grammar.getProductions(lhs)) {
						if (canVanish(production.getRhs())) {
							canVanishMap.put(lhs, true);
							newFound = true;
						}
					}
				}
			}
		}

		private Set<NonTerminal> getNonVanishableNonTerminals() {
			return grammar.getNonTerminals().stream()
					.filter(not(this::canVanish))
					.collect(Collectors.toSet());
		}

		private boolean canVanish(List<Symbol> string) {
			return string.stream()
					.allMatch(this::canVanish);
		}

		private boolean canVanish(Symbol symbol) {
			Boolean value = canVanishMap.get(symbol);
			return value != null && value;
		}
	}
}
