package io.lateralus.parsergenerator.test;

import java.util.ArrayDeque;
import java.util.Deque;

import static io.lateralus.parsergenerator.test.Action.ActionType.REDUCE;

public class Parser {

	// 1. Initialize the parse stack to contain a single state s0, where s0 is the distinguished initial state of the
	//    parser.
	// 2. Use the state s on top of the parse stack and the current lookahead t to consult the action table entry
	//    action[s][t]:
	//    - If the action table entry is shift s' then push state s' onto the stack and advance the input so that the
	//      lookahead is set to the next token.
	//    - If the action table entry is reduce r and rule r has m symbols in its RHS, then pop m symbols off the parse
	//      stack. Let s' be the state now revealed on top of the parse stack and N be the LHS nonterminal for rule r.
	//      Then consult the goto table and push the state given by goto[s'][N] onto the stack. The lookahead token is
	//      not changed by this step.
	//    - If the action table entry is accept, then terminate the parse with success.
	//    - If the action table entry is error, then signal an error.
	// 3. Repeat step (2) until the parser terminates.

	// token = next token()
	// repeat forever
	//   s = top of stack
	//   if action[s, token] = "shift i" then
	//     push token
	//     push i
	//     token = next token()
	//   else if action[s, token] = "reduce A ::= /beta" then
	//     pop 2 |/beta| symbols
	//     s = top of stack
	//     push A
	//     push goto[s,A]
	//   else if action[s, token] = "accept " then
	//     return
	//   else error()

	private final Lexer lexer;

	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	public Node parse() {
		Token token = lexer.nextToken();

		Deque<Integer> stateStack = new ArrayDeque<>();
		Deque<Node> nodeStack = new ArrayDeque<>();

		while (true) {
			int state = stateStack.peek();
			Action action = actionTable[state][token.ordinal()];
			if (action == null) {
				throw ParserException();
			}
			if (action.type == REDUCE) {
				Node[] nodes = popNodes(nodeStack, action.getSize());
				nodeStack.push(action.reduction.reduce(nodes));
			}
		}

	}

	private static Node[] popNodes(Deque<Node> stack, int size) {
		Node[] nodes = new Node[stack];
		for (int i = 0; i < size; i++) {
			nodes[i] = stack.pop();
		}
		return nodes;
	}

}
