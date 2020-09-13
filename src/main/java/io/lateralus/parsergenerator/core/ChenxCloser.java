package io.lateralus.parsergenerator.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;
import static java.util.function.Predicate.not;

/**
 * Calculates the closure as discussed in the dissertation of Xin Chen (Measuring and extending LR(1) parser generation)
 */
public class ChenxCloser extends AbstractCloser {

	private final Map<Symbol, Boolean> canVanishMap;

	private ChenxCloser(Grammar grammar, Map<Symbol, Boolean> canVanishMap) {
		super(grammar);
		this.canVanishMap = canVanishMap;
	}

	protected Set<Terminal> determineLookahead(Item item) {
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
