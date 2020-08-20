package io.lateralus.parsergenerator.core;

import java.util.Set;
import java.util.stream.Collectors;

import static io.lateralus.parsergenerator.core.Terminal.EPSILON;

/**
 * Represents an LR(1) item of which a {@link State} is made of.
 *
 * A LR(1) item consists of a production with a dot-pointer (representing the current "position") and a lookahead
 * symbol.
 */
public class Item {

    private final Production production;

    private final Set<Terminal> lookahead;

    private final int position;

    public Item(Production production, Set<Terminal> lookahead, int position) {
        this.production = production;
        this.lookahead = lookahead;
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;

        Item item = (Item) o;

        if (position != item.position) return false;
        if (!production.equals(item.production)) return false;
        return lookahead.equals(item.lookahead);
    }

    @Override
    public int hashCode() {
        int result = production.hashCode();
        result = 31 * result + lookahead.hashCode();
        result = 31 * result + position;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[")
                .append(production.getLhs().getName())
                .append(" ->");

        int i = 0;
        for (Symbol symbol : production.getRhs()) {
            builder.append(" ");
            if (i++ == position) {
                builder.append("\u2022 ");
            }
            builder.append(symbol.getName());
        }
        if (i == position) {
            builder.append(" \u2022");
        }
        builder.append(", {");

        String lookaheadString = lookahead.stream()
                .map(Symbol::getName)
                .collect(Collectors.joining(", "));

        builder.append(lookaheadString)
                .append("}]");

        return builder.toString();
    }

    public Production getProduction() {
        return production;
    }

    public Set<Terminal> getLookahead() {
        return lookahead;
    }

    public int getPosition() {
        return position;
    }

    public Symbol getExpectedSymbol() {
        if (production.getRhs().size() == position) {
            return null;
        }
        Symbol expectedSymbol = production.getRhs().get(position);
        // Don't return EPSILON, since it is not a real non-terminal; the lexer will never return an "epsilon" token.
        if (expectedSymbol == EPSILON) {
            return null;
        }
        return expectedSymbol;
    }
}
