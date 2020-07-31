package io.lateralus.parsergenerator.core;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Multimaps.toMultimap;
import static io.lateralus.parsergenerator.core.Terminal.EOF;
import static io.lateralus.parsergenerator.core.Terminal.EPSILON;
import static java.util.function.Predicate.not;

/**
 * Represents a context free grammar.
 *
 * A grammar (G) is a set of terminals (N), non-terminals (Σ disjoint from N), production rules (P each in the form
 * N -> (Σ∪N)* ) and a NonTerminal S that is the sentence symbol (aka start symbol).
 */
public class Grammar {

	private static final Set<Terminal> EPSILON_SET = Set.of(EPSILON);

	private final Set<Terminal> terminals;

	private final Set<NonTerminal> nonTerminals;

	private final SetMultimap<NonTerminal, Production> productions;

	private final NonTerminal sentenceSymbol;

	public static Grammar createAugmentedGrammar(List<Production> productions) throws GrammarException {
		Set<Symbol> symbols = productions.stream()
				.flatMap(production -> Stream.concat(Stream.of(production.getLhs()), production.getRhs().stream()))
				.collect(Collectors.toSet());

		if (symbols.contains(NonTerminal.START)) {
			throw new GrammarException("The rules should not contain the augmentation rule (S' -> <start symbol>)");
		}

		Set<Terminal> terminals = symbols.stream()
				.filter(Symbol::isTerminal)
				.map(symbol -> (Terminal)symbol)
				.collect(Collectors.toSet());

		Set<NonTerminal> nonTerminals = symbols.stream()
				.filter(not(Symbol::isTerminal))
				.map(symbol -> (NonTerminal)symbol)
				.collect(Collectors.toSet());

		final Production startProduction = new Production(NonTerminal.START, List.of(productions.get(0).getLhs()));

		nonTerminals.add(NonTerminal.START);

		SetMultimap<NonTerminal, Production> augmentedProductionsMap = productions.stream()
						.collect(toMultimap(
								Production::getLhs,
								Function.identity(),
								MultimapBuilder.hashKeys().hashSetValues()::build));

		augmentedProductionsMap.put(NonTerminal.START, startProduction);

		return new Grammar(terminals, nonTerminals, augmentedProductionsMap, NonTerminal.START);
	}

	private Grammar(Set<Terminal> terminals, Set<NonTerminal> nonTerminals,
	                SetMultimap<NonTerminal, Production> productions, NonTerminal sentenceSymbol) {
		this.sentenceSymbol = sentenceSymbol;
		this.terminals = terminals;
		this.nonTerminals = nonTerminals;
		this.productions = productions;
	}

	/**
	 * TODO: this should probably be calculated once and cached. Or from a static create function and then as a
	 *   constructor argument. Also replace this method with something like firstSet(Symbol), that simply returns the
	 *   firstSet for a given Symbol which is the only way to use the firstSet anyway.
	 * @return
	 */
	public Map<Symbol, Set<Terminal>> calculateFirstSets() {
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
				Set<Terminal> firstSetsForRhs = calculateFirstSetsForRhs(production.getRhs(), firstSets, 0);
				isChanged |= firstSets.get(production.getLhs()).addAll(firstSetsForRhs);
			}
		}

		return firstSets;
	}

	public static Set<Terminal> calculateFirstSetsForRhs(List<Symbol> rhs, Map<Symbol, Set<Terminal>> firstSets, int startIndex) {
		int i = startIndex;
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

	private Map<NonTerminal, Set<Terminal>> calculateFollowSets(Map<Symbol, Set<Terminal>> firstSets) {
		Map<NonTerminal, Set<Terminal>> followSets = new HashMap<>();

		for (NonTerminal nonTerminal : nonTerminals) {
			followSets.put(nonTerminal, new HashSet<>());
		}

		followSets.put(new NonTerminal("S'"), Set.of(EOF));

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

	public static Builder builder() {
		return new Builder();
	}

	public NonTerminal getSentenceSymbol() {
		return sentenceSymbol;
	}

	public Set<Production> getProductions(NonTerminal lhs) {
		return productions.get(lhs);
	}

	/**
	 * TODO: split into separate class; The logic in this class is specific to the syntax used to represent a grammar.
	 *   Also keep the builder pattern, to do the work from createAugmentedGrammar() and to precompute some static
	 *   grammar dependent things such as the first and follow sets.
	 */
	public static class Builder {

		private static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");
		private static final Splitter PRODUCTION_SPLITTER = Splitter.on(Pattern.compile("->")).trimResults().omitEmptyStrings();
		private static final Splitter PRODUCTION_OR_SPLITTER = Splitter.on(Pattern.compile("\\|")).trimResults().omitEmptyStrings();
		private static final Splitter SYMBOL_SPLITTER = Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings();

		private final List<Production> productions = new ArrayList<>();

		public Builder addProduction(Production production) {
			productions.add(production);
			return this;
		}

		public Builder addProduction(NonTerminal lhs, List<Symbol> rhs) {
			addProduction(new Production(lhs, rhs));
			return this;
		}

		private static boolean validateSymbol(String symbol) {
			Matcher matcher = SYMBOL_PATTERN.matcher(symbol);
			return matcher.matches();
		}

		private static Symbol convertToSymbol(String symbol, Set<String> nonTerminals) throws GrammarParserException {
			if (symbol.equals(EPSILON.getName())) {
				return EPSILON;
			}
			if (!validateSymbol(symbol)) {
				throw new GrammarParserException("The symbol '" + symbol + "' is not a valid symbol");
			}
			if (nonTerminals.contains(symbol)) {
				return new NonTerminal(symbol);
			}
			return new Terminal(symbol);
		}

		public Builder from(String grammar) throws GrammarParserException {
			// Note the use of LinkedHashSet to maintain the insertion order.
			Set<String> nonTerminals = new LinkedHashSet<>();
			Map<String, List<List<String>>> productionMap = new HashMap<>();

			try (BufferedReader reader = new BufferedReader(new StringReader(grammar))) {
				String line;
				while ((line = reader.readLine()) != null) {
					List<String> parts = PRODUCTION_SPLITTER.splitToList(line);
					if (parts.size() != 2) {
						throw new GrammarParserException("Expect <TERMINAL> -> <SYMBOL>* (| <SYMBOL>)*");
					}
					String lhs = parts.get(0);
					List<List<String>> rhs = PRODUCTION_OR_SPLITTER.splitToList(parts.get(1)).stream()
							.map(rhsPart -> SYMBOL_SPLITTER.splitToList(rhsPart).stream()
									.map(String::strip)
									.collect(Collectors.toList())
							)
							.collect(Collectors.toList());
					nonTerminals.add(lhs);
					productionMap.put(lhs, rhs);
				}
			} catch (IOException e) {
				throw new GrammarParserException("Could not parse the grammar: \n" + grammar + "\n\n");
			}

			for (String lhs : nonTerminals) {
				if (!validateSymbol(lhs)) {
					throw new GrammarParserException("The symbol '" + lhs + "' is not a valid symbol");
				}
				NonTerminal nonTerminal = new NonTerminal(lhs);
				for (List<String> rhs : productionMap.get(lhs)) {
					List<Symbol> symbols = new ArrayList<>();
					for (String symbolName : rhs) {
						symbols.add(convertToSymbol(symbolName, nonTerminals));
					}
					addProduction(nonTerminal, symbols);
				}
			}

			return this;
		}

		public Grammar build() throws GrammarException {
			return Grammar.createAugmentedGrammar(productions);
		}

		public static class GrammarParserException extends Exception {
			public GrammarParserException(String s) {
				super(s);
			}
		}
	}
}
