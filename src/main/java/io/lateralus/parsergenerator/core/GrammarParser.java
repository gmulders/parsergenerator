package io.lateralus.parsergenerator.core;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;

public class GrammarParser {

	private static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");
	private static final Splitter PRODUCTION_SPLITTER = Splitter.on(Pattern.compile("->")).trimResults().omitEmptyStrings();
	private static final Splitter PRODUCTION_OR_SPLITTER = Splitter.on(Pattern.compile("\\|")).trimResults().omitEmptyStrings();
	private static final Splitter SYMBOL_SPLITTER = Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings();

	private GrammarParser() {
	}

	private static boolean isValidSymbol(String symbol) {
		Matcher matcher = SYMBOL_PATTERN.matcher(symbol);
		return matcher.matches();
	}

	private static Symbol convertToSymbol(String symbol, Set<String> nonTerminals) throws GrammarParserException {
		if (symbol.equals(EPSILON.getName())) {
			return EPSILON;
		}
		if (!isValidSymbol(symbol)) {
			throw new GrammarParserException("The symbol '" + symbol + "' is not a valid symbol");
		}
		if (nonTerminals.contains(symbol)) {
			return new NonTerminal(symbol);
		}
		return new Terminal(symbol);
	}

	public static Grammar.Builder builderFrom(String grammar) throws GrammarParserException {
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
				List<List<String>> currentRhs = productionMap.computeIfAbsent(lhs, a -> new ArrayList<>());
				currentRhs.addAll(rhs);
			}
		} catch (IOException e) {
			throw new GrammarParserException("Could not parse the grammar: \n" + grammar + "\n\n");
		}

		Grammar.Builder builder = Grammar.builder();

		for (String lhs : nonTerminals) {
			if (!isValidSymbol(lhs)) {
				throw new GrammarParserException("The symbol '" + lhs + "' is not a valid symbol");
			}
			NonTerminal nonTerminal = new NonTerminal(lhs);
			for (List<String> rhs : productionMap.get(lhs)) {
				List<Symbol> symbols = new ArrayList<>();
				for (String symbolName : rhs) {
					symbols.add(convertToSymbol(symbolName, nonTerminals));
				}
				builder.addProduction(nonTerminal, symbols);
			}
		}

		return builder;
	}
}
