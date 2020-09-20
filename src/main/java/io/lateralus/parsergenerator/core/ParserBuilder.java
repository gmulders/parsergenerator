package io.lateralus.parsergenerator.core;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import io.lateralus.parsergenerator.codegenerator.CodeGenerationException;
import io.lateralus.parsergenerator.codegenerator.CodeGenerator;
import io.lateralus.parsergenerator.codegenerator.simple.BasicParserCodeGenerator;
import io.lateralus.parsergenerator.core.definition.ParserDefinition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParserBuilder {

	public static void main(String[] args) throws GrammarParserException, GrammarException, CodeGenerationException {

		// https://zaa.ch/jison/try/usf/index.html
		// %%
		//
		//E
		//    : T X
		//    ;
		//X
		//    : plus T X
		//    | %empty
		//    ;
		//
		//T
		//    : F Y
		//    ;
		//
		//Y
		//    : times F Y
		//    | %empty
		//    ;
		//
		//F   : left E right
		//    | id
		//    ;

		// 0
		//$accept -> .E $end #lookaheads= $end
		//E -> .T X #lookaheads= $end
		//T -> .F Y #lookaheads= $end plus
		//F -> .left E right #lookaheads= $end plus times
		//F -> .id #lookaheads= $end plus times
		// 1
		//$accept -> E .$end #lookaheads= $end
		// 2
		//E -> T .X #lookaheads= $end
		//X -> .plus T X #lookaheads= $end
		//X -> . #lookaheads= $end
		// 3
		//T -> F .Y #lookaheads= $end plus
		//Y -> .times F Y #lookaheads= $end plus
		//Y -> . #lookaheads= $end plus
		// 4
		//F -> left .E right #lookaheads= $end plus times
		//E -> .T X #lookaheads= right
		//T -> .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 5
		//F -> id . #lookaheads= $end plus times
		// 6
		//E -> T X . #lookaheads= $end
		// 7
		//X -> plus .T X #lookaheads= $end
		//T -> .F Y #lookaheads= $end plus
		//F -> .left E right #lookaheads= $end plus times
		//F -> .id #lookaheads= $end plus times
		// 8
		//T -> F Y . #lookaheads= $end plus
		// 9
		//Y -> times .F Y #lookaheads= $end plus
		//F -> .left E right #lookaheads= $end plus times
		//F -> .id #lookaheads= $end plus times
		// 10
		//F -> left E .right #lookaheads= $end plus times
		// 11
		//E -> T .X #lookaheads= right
		//X -> .plus T X #lookaheads= right
		//X -> . #lookaheads= right
		// 12
		//T -> F .Y #lookaheads= plus right
		//Y -> .times F Y #lookaheads= plus right
		//Y -> . #lookaheads= plus right
		// 13
		//F -> left .E right #lookaheads= plus right times
		//E -> .T X #lookaheads= right
		//T -> .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 14
		//F -> id . #lookaheads= plus right times
		// 15
		//X -> plus T .X #lookaheads= $end
		//X -> .plus T X #lookaheads= $end
		//X -> . #lookaheads= $end
		// 16
		//Y -> times F .Y #lookaheads= $end plus
		//Y -> .times F Y #lookaheads= $end plus
		//Y -> . #lookaheads= $end plus
		// 17
		//F -> left E right . #lookaheads= $end plus times
		// 18
		//E -> T X . #lookaheads= right
		// 19
		//X -> plus .T X #lookaheads= right
		//T -> .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 20
		//T -> F Y . #lookaheads= plus right
		// 21
		//Y -> times .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 22
		//F -> left E .right #lookaheads= plus right times
		// 23
		//X -> plus T X . #lookaheads= $end
		// 24
		//Y -> times F Y . #lookaheads= $end plus
		// 25
		//X -> plus T .X #lookaheads= right
		//X -> .plus T X #lookaheads= right
		//X -> . #lookaheads= right
		// 26
		//Y -> times F .Y #lookaheads= plus right
		//Y -> .times F Y #lookaheads= plus right
		//Y -> . #lookaheads= plus right
		// 27
		//F -> left E right . #lookaheads= plus right times
		// 28
		//X -> plus T X . #lookaheads= right
		// 29
		//Y -> times F Y . #lookaheads= plus right

		// %%
		//E
		//    : E plus T
		//    | T
		//    ;
		//T
		//    : T times F
		//    | F
		//    ;
		//F
		//    : left E right
		//    | id
		//    ;

//		String grammarString =
//				"E -> T plus E\n" +
//				"E -> T\n" +
//				"T -> F times T\n" +
//				"T -> F\n" +
//				"F -> left E right\n" +
//				"F -> id";

//		String grammarString =
//				"E -> E plus T\n" +
//				"E -> T\n" +
//				"T -> T times F\n" +
//				"T -> F\n" +
//				"F -> left E right\n" +
//				"F -> id\n";

        // This grammar string does not work yet; we need to add some sort of annotation so that we know what the name
		// for the node that we will generate should be.
		String grammarString =
				"Expression -> Term\n" +
				"Expression -> Expression(lhs) plus Term(rhs) : Plus binary\n" +
				"Term -> Factor\n" +
				"Term -> Term(lhs) times Factor(rhs) : Product binary\n" +
				"Factor -> left Expression right : Paren\n" +
				"Factor -> id : Number\n";

//		String grammarString =
//				"E -> T X\n" +
//				"X -> plus T X | ε\n" +
//				"T -> F Y\n" +
//				"Y -> times F Y | ε\n" +
//				"F -> left E right | id";

		// First parse the input grammar into an internal representation
		Grammar grammar = GrammarParser
				.builderFrom(grammarString)
				.build();

//		Closer closer = new KnuthCloser(grammar);
		Closer closer = ChenxCloser.builder(grammar).build();

		for (Symbol symbol : grammar.getNonTerminals()) {
			System.out.println(symbol + " -> " + grammar.firstSet(symbol));
		}
		System.out.println();

		// Take the grammar and determine the canonical collection
		Set<State> canonicalCollection = createCanonicalCollection(grammar, closer);

		int i = 0;
		for (State state : canonicalCollection) {
			System.out.println(String.format("%02d", i++) + " " + state);
		}

		// From the canonical collection we create the goto table and the action table.
		Table<State, NonTerminal, State> gotoTable = buildGotoTable(canonicalCollection);
		Table<State, Terminal, Action> actionTable = buildActionTable(canonicalCollection);

		System.out.println();
		gotoTable.cellSet().forEach(cell -> System.out.println(cell.getRowKey() + " + " + cell.getColumnKey() + " --> " + cell.getValue()));

		System.out.println();
		actionTable.cellSet().forEach(cell -> System.out.println(cell.getRowKey() + " + " + cell.getColumnKey() + " --> " + cell.getValue()));

		ParserDefinition parserDefinition = new ParserDefinition(grammar);
		CodeGenerator<BasicParserCodeGenerator.Properties, String> codeGenerator = new BasicParserCodeGenerator();
		codeGenerator.setProperties(new BasicParserCodeGenerator.Properties("Super", "test.parser", "test.lexer"));
		codeGenerator.generate(parserDefinition)
				.forEach(f -> {
					System.out.println("=============== " + f.getName() + " ===============");
					System.out.println(f.getContents());
					try {
						Path path = Path.of("src/test-out/java/", f.getName()).toAbsolutePath();
						Files.createDirectories(path.getParent());
						Files.write(path, f.getContents().getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
	}

	private static Table<State, Terminal, Action> buildActionTable(Set<State> canonicalCollection) {
		Table<State, Terminal, Action> actionTable = HashBasedTable.create();
		for (State state : canonicalCollection) {

			for (Symbol symbol : state.getTransitions().keySet()) {
				if (symbol.isTerminal()) {
					updateActionTable(actionTable, state, (Terminal)symbol,
							Action.shift(state.getTransitions().get(symbol)));
				}
			}

			for (Item item : state.getItems()) {
				if (item.getExpectedSymbol() != null) {
					continue;
				}

				if (item.getProduction().getLhs() == NonTerminal.START) {
					updateActionTable(actionTable, state, Terminal.EOF, Action.accept());
				} else {
					updateActionTable(actionTable, state, item.getLookahead(), Action.reduce(item.getProduction()));
				}
			}
		}
		return actionTable;
	}

	private static void updateActionTable(Table<State, Terminal, Action> actionTable, State state, Set<Terminal> terminals, Action action) {
		for (Terminal terminal : terminals) {
			updateActionTable(actionTable, state, terminal, action);
		}
	}

	private static void updateActionTable(Table<State, Terminal, Action> actionTable, State state, Terminal terminal, Action action) {
		Action currentAction = actionTable.get(state, terminal);
		if (currentAction != null) {
			throw new IllegalStateException("Grammar leads to a " + currentAction.getActionType() + "-" +
					action.getActionType() + " conflict. State: " + state + ", terminal: " + terminal);
		}
		actionTable.put(state, terminal, action);
	}

	protected static Table<State, NonTerminal, State> buildGotoTable(Set<State> canonicalCollection) {
		Table<State, NonTerminal, State> gotoTable = HashBasedTable.create();

		for (State state : canonicalCollection) {
			for (Map.Entry<Symbol, State> entry : state.getTransitions().entrySet()) {
				if (entry.getKey().isTerminal()) {
					continue;
				}
				gotoTable.put(state, (NonTerminal)entry.getKey(), entry.getValue());
			}
		}

		return gotoTable;
	}

	protected static Set<State> createCanonicalCollection(Grammar grammar, Closer closer) {
		// Note to self: previously I used a LinkedHashSet here so that I kept the insertion order. I don't fully
		// remember why I did this, but I think it was because I used a rather sloppy technique to get the nextState if
		// it was already contained in the canonical collection. See the commented code below in this method. I replaced
		// this with a map that holds the transitions. Test to be sure!!!!
		Set<State> canonicalCollection = Collections.newSetFromMap(new IdentityHashMap<>());
		Map<State, State> internedStates = new HashMap<>();

		Deque<State> workList = new ArrayDeque<>();

		// Determine the state from which to start
		State startState = new State(closer.closure(createStartKernel(grammar)));
		internedStates.put(startState, startState);
		canonicalCollection.add(startState);
		workList.push(startState);

		while (!workList.isEmpty()) {
			State currentState = workList.pop();

			// Find all new kernels
			SetMultimap<Symbol, Item> kernels = HashMultimap.create();
			for (Item item : currentState.getItems()) {
				Symbol expectedSymbol = item.getExpectedSymbol();
				if (expectedSymbol != null) {
					kernels.put(expectedSymbol, item);
				}
			}

			for (Symbol expectedSymbol : kernels.keySet()) {
				Set<Item> kernelBase = kernels.get(expectedSymbol);

				// Create new items for the kernel (increase the dot pointer).
				Set<Item> kernel = kernelBase.stream()
						.map(item -> new Item(item.getProduction(), item.getLookahead(), item.getPosition() + 1))
						.collect(Collectors.toSet());

				// Create the next state by taking the closure of the kernel
				State nextState = internedStates.computeIfAbsent(new State(closer.closure(kernel)), Function.identity());

				if (canonicalCollection.add(nextState)) {
					workList.push(nextState);
				}
				currentState.getTransitions().put(expectedSymbol, nextState);
			}
		}
		return canonicalCollection;
	}

	private static Set<Item> createStartKernel(Grammar grammar) {
		Set<Production> startProductions = grammar.getProductions(grammar.getSentenceSymbol());
		// Note that while we do a stream here and collect to a set, we only expect the set of "start productions" to
		// contain one element.
		return startProductions.stream()
				.map(startProduction -> new Item(startProduction, Set.of(Terminal.EOF), 0))
				.collect(Collectors.toSet());
	}
}
