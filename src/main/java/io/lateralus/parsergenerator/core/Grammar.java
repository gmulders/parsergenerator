package io.lateralus.parsergenerator.core;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Multimaps.toMultimap;
import static io.lateralus.parsergenerator.core.NonTerminal.START;
import static io.lateralus.parsergenerator.core.Terminal.EOF;
import static io.lateralus.parsergenerator.core.Terminal.EPSILON;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;

/**
 * Represents a context free grammar.
 *
 * A grammar (G) is a set of terminals (N), non-terminals (Σ disjoint from N), production rules (P each in the form
 * N -> (Σ∪N)* ) and a NonTerminal S that is the sentence symbol (aka start symbol).
 */
public class Grammar {

	private final Set<Symbol> symbols;

	private final Set<Terminal> terminals;

	private final Set<NonTerminal> nonTerminals;

	private final SetMultimap<NonTerminal, Production> productions;

	private final NonTerminal sentenceSymbol;

	private final Map<Symbol, Set<Terminal>> firstSets;

	private final Map<NonTerminal, Set<Terminal>> followSets;

	private Grammar(Set<Symbol> symbols,
	                Set<Terminal> terminals,
	                Set<NonTerminal> nonTerminals,
	                SetMultimap<NonTerminal, Production> productions,
	                NonTerminal sentenceSymbol,
	                Map<Symbol, Set<Terminal>> firstSets,
	                Map<NonTerminal, Set<Terminal>> followSets) {
		this.symbols = symbols;
		this.sentenceSymbol = sentenceSymbol;
		this.terminals = terminals;
		this.nonTerminals = nonTerminals;
		this.productions = productions;
		this.firstSets = firstSets;
		this.followSets = followSets;
	}

	public Set<Terminal> firstSet(Symbol symbol) {
		return firstSets.get(symbol);
	}

	public Set<Terminal> followSet(NonTerminal nonTerminal) {
		return followSets.get(nonTerminal);
	}

	public Set<Terminal> calculateFirstSetForRemainingSymbols(List<Symbol> remainingSymbols) {
		// The function calculateFirstSetForRhs returns a SetView, which we cannot mutate. This is fine for use within
		// the builder (i.e. calculating first sets), but not for where this method is called.
		return new HashSet<>(Builder.calculateFirstSetForRhs(remainingSymbols, firstSets));
	}

	public static Builder builder() {
		return new Builder();
	}

	public NonTerminal getSentenceSymbol() {
		return sentenceSymbol;
	}

	public Set<Production> getProductions(NonTerminal lhs) {
		return productions.get(lhs);
	}

	public Set<Symbol> getSymbols() {
		return symbols;
	}

	public Set<Terminal> getTerminals() {
		return terminals;
	}

	public Set<NonTerminal> getNonTerminals() {
		return nonTerminals;
	}

	/**
	 * Builder to create a {@link Grammar}
	 */
	public static class Builder {
		/**
		 * Set containing only the epsilon symbol
		 */
		private static final Set<Terminal> EPSILON_SET = Set.of(EPSILON);

		private final List<Production> productions = new ArrayList<>();
		private final Map<Symbol, Symbol> internedSymbols = new HashMap<>();

		/**
		 * Interns the symbols in the production and adds a production with the interned symbols to the list.
		 * @param production The production to add
		 * @return The current builder
		 */
		public Builder addProduction(Production production) {
			NonTerminal lhs = (NonTerminal) internedSymbols.computeIfAbsent(production.getLhs(), identity());
			List<Symbol> rhs = new ArrayList<>();
			for (Symbol symbol : production.getRhs()) {
				rhs.add(internedSymbols.computeIfAbsent(symbol, identity()));
			}
			productions.add(new Production(lhs, rhs));
			return this;
		}

		public Builder addProduction(NonTerminal lhs, List<Symbol> rhs) {
			return addProduction(new Production(lhs, rhs));
		}

		public Grammar build() throws GrammarException {
			Set<Symbol> symbols = internedSymbols.keySet();

			// Check that the grammar is not augmented
			if (symbols.contains(START)) {
				throw new GrammarException("The rules should not contain the augmentation rule (S' -> <start symbol>)");
			}

			Set<Terminal> terminals = calculateTerminals(symbols);
			Set<NonTerminal> nonTerminals = calculateNonTerminals(symbols);
			SetMultimap<NonTerminal, Production> productionsMap = calculateProductionMap();

			augmentGrammar(nonTerminals, productionsMap);

			Map<Symbol, Set<Terminal>> firstSets = calculateFirstSets(terminals, nonTerminals, productionsMap);
			Map<NonTerminal, Set<Terminal>> followSets = calculateFollowSets(nonTerminals, productionsMap, firstSets);

			return new Grammar(symbols, terminals, nonTerminals, productionsMap, START, firstSets, followSets);
		}

		private Set<Terminal> calculateTerminals(Set<Symbol> symbols) {
			return symbols.stream()
					.filter(Symbol::isTerminal)
					.map(symbol -> (Terminal)symbol)
					.collect(Collectors.toSet());
		}

		private Set<NonTerminal> calculateNonTerminals(Set<Symbol> symbols) {
			return symbols.stream()
					.filter(not(Symbol::isTerminal))
					.map(symbol -> (NonTerminal) symbol)
					.collect(Collectors.toSet());
		}

		private SetMultimap<NonTerminal, Production> calculateProductionMap() {
			return productions.stream()
					.collect(toMultimap(
							Production::getLhs,
							identity(),
							MultimapBuilder.hashKeys().hashSetValues()::build));
		}

		private void augmentGrammar(Set<NonTerminal> nonTerminals, SetMultimap<NonTerminal, Production> productionsMap) {
			nonTerminals.add(START);
			final Production startProduction = new Production(START, List.of(productions.get(0).getLhs()));
			productionsMap.put(START, startProduction);
		}

		private static Set<Terminal> calculateFirstSetForRhs(List<Symbol> rhs, Map<Symbol, Set<Terminal>> firstSets) {
			int i = 0;
			Set<Terminal> firstBi = firstSets.get(rhs.get(i));
			Set<Terminal> firstSetsForRhs = Sets.difference(firstBi, EPSILON_SET);

			while (firstBi.contains(EPSILON) && i < rhs.size() - 1) {
				firstBi = firstSets.get(rhs.get(i));
				firstSetsForRhs = Sets.union(firstSetsForRhs, Sets.difference(firstBi, EPSILON_SET));
				i++;
			}

			if (firstBi.contains(EPSILON) && i == rhs.size() - 1) {
				firstSetsForRhs = Sets.union(firstSetsForRhs, EPSILON_SET);
			}
			return firstSetsForRhs;
		}

		/**
		 * Calculates the first sets for the grammar
		 * @return A map containing the first set per symbol.
		 */
		private static Map<Symbol, Set<Terminal>> calculateFirstSets(
				Set<Terminal> terminals,
				Set<NonTerminal> nonTerminals,
				SetMultimap<NonTerminal, Production> productions
		) {
			Map<Symbol, Set<Terminal>> firstSets = new HashMap<>();

			for (Terminal terminal : terminals) {
				firstSets.put(terminal, Set.of(terminal));
			}
			for (NonTerminal nonTerminal : nonTerminals) {
				firstSets.put(nonTerminal, new HashSet<>());
			}

			firstSets.put(EOF, Set.of(EOF));

			boolean isChanged = true;

			while (isChanged) {
				isChanged = false;
				for (Production production : productions.values()) {
					Set<Terminal> firstSetForRhs = calculateFirstSetForRhs(production.getRhs(), firstSets);
					isChanged |= firstSets.get(production.getLhs()).addAll(firstSetForRhs);
				}
			}

			return firstSets;
		}

		/**
		 * Calculates the follow sets for the grammar
		 * @return A map containing the follow set per symbol.
		 */
		private static Map<NonTerminal, Set<Terminal>> calculateFollowSets(
				Set<NonTerminal> nonTerminals,
				SetMultimap<NonTerminal, Production> productions,
				Map<Symbol, Set<Terminal>> firstSets
		) {
			Map<NonTerminal, Set<Terminal>> followSets = new HashMap<>();

			for (NonTerminal nonTerminal : nonTerminals) {
				followSets.put(nonTerminal, new HashSet<>());
			}

			followSets.put(START, Set.of(EOF));

			boolean isChanged = true;

			while (isChanged) {
				isChanged = false;

				for (Production rule : productions.values()) {
					Set<Terminal> trailer = new HashSet<>(followSets.get(rule.getLhs()));
					List<Symbol> rhs = rule.getRhs();
					for (int i = rhs.size() - 1; i >= 0; i--) {
						Symbol bi = rhs.get(i);
						Set<Terminal> firstBi = firstSets.get(bi);
						if (bi.isTerminal()) {
							trailer = firstBi;
							continue;
						}
						// else it is a non terminal
						NonTerminal nonTerminalBi = (NonTerminal)bi;
						isChanged |= followSets.get(nonTerminalBi).addAll(trailer);
						if (firstBi.contains(EPSILON)) {
							trailer = Sets.union(trailer, Sets.difference(firstBi, EPSILON_SET));
						} else {
							trailer = firstBi;
						}
					}
				}

			}
			return followSets;
		}
	}
}
