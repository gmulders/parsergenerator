package io.lateralus.parsergenerator.core;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a production (or rule) that is part of a {@link Grammar}
 */
public class Production {

    private final NonTerminal lhs;

    private final List<Symbol> rhs;

    public Production(NonTerminal lhs, List<Symbol> rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return lhs + " -> " + rhs.stream().map(Symbol::toString).collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Production that = (Production) o;

        if (!lhs.equals(that.lhs)) return false;
        return rhs.equals(that.rhs);
    }

    @Override
    public int hashCode() {
        int result = lhs.hashCode();
        result = 31 * result + rhs.hashCode();
        return result;
    }

    public NonTerminal getLhs() {
        return lhs;
    }

    public List<Symbol> getRhs() {
        return rhs;
    }
}
