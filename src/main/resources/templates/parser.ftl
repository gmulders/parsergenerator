package ${parserPackageName};

import ${lexerPackageName}.Lexer;
import ${lexerPackageName}.LexerException;
import ${lexerPackageName}.Token;
import ${parserPackageName}.nodes.Node;

import java.util.ArrayDeque;
import java.util.Deque;

import static ${parserPackageName}.Action.ActionType.ACCEPT;
import static ${parserPackageName}.Action.ActionType.REDUCE;
import static ${parserPackageName}.Action.ActionType.SHIFT;

public class Parser {

	private static Action[][] ACTION_TABLE;
	private static int[][] GOTO_TABLE;

	private final Lexer lexer;

	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	public Node parse() throws ParserException {
		Deque<Integer> stateStack = new ArrayDeque<>();
		Deque<Object> nodeStack = new ArrayDeque<>();

		Token token = nextToken();

		while (true) {
			int state = stateStack.peek();
			Action action = ACTION_TABLE[state][token.getTokenType().ordinal()];
			if (action == null) {
				throw new ParserException("");
			}

			if (action.type == SHIFT) {
				nodeStack.push(token);
				stateStack.push(action.state);
				token = nextToken();
			} else if (action.type == REDUCE) {
				Object[] nodes = popNodes(nodeStack, action.size);
				nodeStack.push(action.reduction.reduce(nodes));
				state = popStates(stateStack, action.size);
				stateStack.push(GOTO_TABLE[state][action.productionId]);
			} else if (action.type == ACCEPT) {
				return (Node)nodeStack.pop();
			}
		}
	}

	private Token nextToken() throws ParserException {
		try {
			return lexer.nextToken();
		} catch (LexerException e) {
			throw new ParserException("LexerException during parse: ", e);
		}
	}

	private static Object[] popNodes(Deque<Object> stack, int size) {
		Object[] nodes = new Object[size];
		for (int i = 0; i < size; i++) {
			nodes[i] = stack.pop();
		}
		return nodes;
	}

	private static int popStates(Deque<Integer> stack, int size) {
		for (int i = 0; i < size; i++) {
			stack.pop();
		}
		return stack.peek();
	}
}
