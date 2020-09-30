package ${parserPackageName};

import ${lexerPackageName}.Lexer;
import ${lexerPackageName}.LexerException;
import ${lexerPackageName}.Token;
import ${parserPackageName}.Action.Accept;
import ${parserPackageName}.Action.Reduce;
import ${parserPackageName}.Action.Shift;
import ${parserPackageName}.nodes.*;

import java.util.ArrayDeque;
import java.util.Deque;

public class Parser {

	private static Action[][] ACTION_TABLE = ${actionTableJava};
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

			if (action instanceof Shift) {
				nodeStack.push(token);
				stateStack.push(((Shift)action).state);
				token = nextToken();
			} else if (action instanceof Reduce) {
				Reduce reduce = (Reduce)action;
				Object[] nodes = popNodes(nodeStack, reduce.size);
				nodeStack.push(reduce.reduction.reduce(nodes));
				state = popStates(stateStack, reduce.size);
				stateStack.push(GOTO_TABLE[state][reduce.id]);
			} else if (action instanceof Accept) {
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
