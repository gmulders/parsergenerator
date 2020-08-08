package io.lateralus.parsergenerator.core;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import io.lateralus.parsergenerator.codegenerator.CodeGenerator;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;

public class ParserBuilder {

	public static void main(String[] args) throws GrammarParserException, GrammarException {

		// Canonical Collection :
		//State 0 :
		//S' -> .E , [$]
		//E -> .T plus E , [$]
		//E -> .T , [$]
		//T -> .F times T , [plus]
		//T -> .F , [plus]
		//T -> .F , [$]
		//T -> .F times T , [$]
		//F -> .id , [times]
		//F -> .id , [$]
		//F -> .id , [plus]
		//F -> .left E right , [times]
		//F -> .left E right , [$]
		//F -> .left E right , [plus]
		//
		//State 1 :
		//E -> T. , [$]
		//E -> T .plus E , [$]
		//
		//State 2 :
		//S' -> E. , [$]
		//
		//State 3 :
		//F -> left .E right , [times]
		//F -> left .E right , [$]
		//F -> left .E right , [plus]
		//E -> .T plus E , [right]
		//E -> .T , [right]
		//T -> .F times T , [plus]
		//T -> .F , [plus]
		//T -> .F , [right]
		//T -> .F times T , [right]
		//F -> .id , [times]
		//F -> .id , [right]
		//F -> .id , [plus]
		//F -> .left E right , [times]
		//F -> .left E right , [right]
		//F -> .left E right , [plus]
		//
		//State 4 :
		//T -> F .times T , [plus]
		//T -> F. , [plus]
		//T -> F .times T , [$]
		//T -> F. , [$]
		//
		//State 5 :
		//F -> id. , [times]
		//F -> id. , [$]
		//F -> id. , [plus]
		//
		//State 6 :
		//E -> T plus .E , [$]
		//E -> .T plus E , [$]
		//E -> .T , [$]
		//T -> .F times T , [plus]
		//T -> .F , [plus]
		//T -> .F , [$]
		//T -> .F times T , [$]
		//F -> .id , [times]
		//F -> .id , [$]
		//F -> .id , [plus]
		//F -> .left E right , [times]
		//F -> .left E right , [$]
		//F -> .left E right , [plus]
		//
		//State 7 :
		//E -> T .plus E , [right]
		//E -> T. , [right]
		//
		//State 8 :
		//F -> left E .right , [times]
		//F -> left E .right , [$]
		//F -> left E .right , [plus]
		//
		//State 9 :
		//F -> left .E right , [times]
		//F -> left .E right , [right]
		//F -> left .E right , [plus]
		//E -> .T plus E , [right]
		//E -> .T , [right]
		//T -> .F times T , [plus]
		//T -> .F , [plus]
		//T -> .F , [right]
		//T -> .F times T , [right]
		//F -> .id , [times]
		//F -> .id , [right]
		//F -> .id , [plus]
		//F -> .left E right , [times]
		//F -> .left E right , [right]
		//F -> .left E right , [plus]
		//
		//State 10 :
		//T -> F .times T , [plus]
		//T -> F. , [plus]
		//T -> F .times T , [right]
		//T -> F. , [right]
		//
		//State 11 :
		//F -> id. , [times]
		//F -> id. , [right]
		//F -> id. , [plus]
		//
		//State 12 :
		//T -> F times .T , [plus]
		//T -> F times .T , [$]
		//T -> .F times T , [plus]
		//T -> .F , [plus]
		//T -> .F , [$]
		//T -> .F times T , [$]
		//F -> .id , [times]
		//F -> .id , [$]
		//F -> .id , [plus]
		//F -> .left E right , [times]
		//F -> .left E right , [$]
		//F -> .left E right , [plus]
		//
		//State 13 :
		//E -> T plus E. , [$]
		//
		//State 14 :
		//E -> T plus .E , [right]
		//E -> .T plus E , [right]
		//E -> .T , [right]
		//T -> .F times T , [plus]
		//T -> .F , [plus]
		//T -> .F , [right]
		//T -> .F times T , [right]
		//F -> .id , [times]
		//F -> .id , [right]
		//F -> .id , [plus]
		//F -> .left E right , [times]
		//F -> .left E right , [right]
		//F -> .left E right , [plus]
		//
		//State 15 :
		//F -> left E right. , [$]
		//F -> left E right. , [plus]
		//F -> left E right. , [times]
		//
		//State 16 :
		//F -> left E .right , [times]
		//F -> left E .right , [right]
		//F -> left E .right , [plus]
		//
		//State 17 :
		//T -> F times .T , [plus]
		//T -> F times .T , [right]
		//T -> .F times T , [plus]
		//T -> .F , [plus]
		//T -> .F , [right]
		//T -> .F times T , [right]
		//F -> .id , [times]
		//F -> .id , [right]
		//F -> .id , [plus]
		//F -> .left E right , [times]
		//F -> .left E right , [right]
		//F -> .left E right , [plus]
		//
		//State 18 :
		//T -> F times T. , [plus]
		//T -> F times T. , [$]
		//
		//State 19 :
		//E -> T plus E. , [right]
		//
		//State 20 :
		//F -> left E right. , [right]
		//F -> left E right. , [plus]
		//F -> left E right. , [times]
		//
		//State 21 :
		//T -> F times T. , [plus]
		//T -> F times T. , [right]


		String grammarString =
				"E -> T + E\n" +
				"E -> T\n" +
				"T -> F * T\n" +
				"T -> F\n" +
				"F -> ( E )\n" +
				"F -> a";

        // This grammar string does not work yet; we need to add some sort of annotation so that we know what the name
		// for the node that we will generate should be.
//		String grammarString =
//				"E (Plus) -> T plus E\n" +
//				"E -> T\n" +
//				"T (Product) -> F times T\n" +
//				"T -> F\n" +
//				"F (Factor) -> left E right\n" +
//				"F -> id\n";

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

		// Take the grammar and determine the canonical collection
		Set<State> canonicalCollection = createCanonicalCollection(grammar);
		// From the canonical collection we create the goto table and the action table.
		Table<State, NonTerminal, State> gotoTable = buildGotoTable(canonicalCollection);
		Table<State, Terminal, Action> actionTable = buildActionTable(canonicalCollection);

		canonicalCollection.forEach(System.out::println);
		System.out.println();
		gotoTable.cellSet().forEach(cell -> System.out.println(cell.getRowKey() + " + " + cell.getColumnKey() + " --> " + cell.getValue()));
		System.out.println();
		actionTable.cellSet().forEach(cell -> System.out.println(cell.getRowKey() + " + " + cell.getColumnKey() + " --> " + cell.getValue()));

		CodeGenerator codeGenerator = new CodeGenerator(Path.of(args[0]));
		codeGenerator.outputParser();
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

	protected static Set<State> createCanonicalCollection(Grammar grammar) {
		// Note to self: previously I used a LinkedHashSet here so that I kept the insertion order. I don't fully
		// remember why I did this, but I think it was because I used a rather sloppy technique to get the nextState if
		// it was already contained in the canonical collection. See the commented code below in this method. I replaced
		// this with a map that holds the transitions. Test to be sure!!!!
		Set<State> canonicalCollection = new /*Linked*/HashSet<>();
		Table<State, Symbol, State> transitions = HashBasedTable.create();

		Deque<State> workList = new ArrayDeque<>();

		// Determine the state from which to start
		State startState = new State(closure(grammar, createStartKernel(grammar)));
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
				State nextState = new State(closure(grammar, kernel));

				if (canonicalCollection.add(nextState)) {
					workList.push(nextState);
				}

				transitions.put(currentState, expectedSymbol, nextState);

				//if (isNew) {
				//	workList.push(nextState);
				//	currentState.getTransitions().put(expectedSymbol, nextState);
				//} else {
				//	State nextStateOriginal = canonicalCollection.stream()
				//			.filter(Predicate.isEqual(nextState))
				//			.findFirst()
				//			.orElseThrow();
				//	currentState.getTransitions().put(expectedSymbol, nextStateOriginal);
				//}
			}
		}

		for (Table.Cell<State, Symbol, State> cell : transitions.cellSet()) {
			cell.getRowKey().getTransitions().put(cell.getColumnKey(), cell.getValue());
		}

		return canonicalCollection;
	}

	private static Set<Item> createStartKernel(Grammar grammar) {
		Set<Production> startProductions = grammar.getProductions(grammar.getSentenceSymbol());
		// Note that while we do a stream here and collect to a set, we only expect the set of "start productions" to
		// contain one element.
		return startProductions.stream()
				.map(startProduction -> new Item(startProduction, Terminal.EOF, 0))
				.collect(Collectors.toSet());
	}

	private static Set<Item> closure(Grammar grammar, Set<Item> items) {
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
				Set<Terminal> lookaheadSet = determineLookahead(grammar, item);
				Set<Production> productions = grammar.getProductions((NonTerminal)expectedSymbol);

				for (Production production : productions) {
					for (Terminal lookahead : lookaheadSet) {
						Item newItem = new Item(production, lookahead, 0);
						workList.add(newItem);
					}
				}
			}
		}

		return closure;
	}

	private static Set<Terminal> determineLookahead(Grammar grammar, Item item) {
		int nextPosition = item.getPosition() + 1;
		List<Symbol> rhs = item.getProduction().getRhs();

		// If the next position is equal to the length of the rhs the lookahead is the lookahead of the item.
		if (nextPosition == rhs.size()) {
			return Set.of(item.getLookahead());
		}

		// Create a sublist that holds the symbols after the dot pointer in the item. Note that since the sublist is
		// basically a view on a list this is a cheap operation. Alternatively we could have the iteration in
		// calculateFirstSetForRemainingSymbols start at the next position.
		List<Symbol> remainingSymbols = rhs.subList(nextPosition, rhs.size());
		Set<Terminal> lookahead = grammar.calculateFirstSetForRemainingSymbols(remainingSymbols);

		if (lookahead.remove(EPSILON)) {
			lookahead.add(item.getLookahead());
		}

		return lookahead;
	}

}
