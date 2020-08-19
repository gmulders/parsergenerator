package io.lateralus.parsergenerator.core;

/**
 * Abstract class that represents a Symbol. There are two subclasses; {@link Terminal} and {@link NonTerminal}.
 *
 * The reason that was chosen for subclassing instead of only a boolean or a {@code SymbolType} is that now the compiler
 * helps finding mistakes.
 */
public abstract class Symbol {

    private final boolean isTerminal;

    private final String name;

    public Symbol(boolean isTerminal, String name) {
        this.isTerminal = isTerminal;
        this.name = name;
    }

    public abstract boolean isVanishable();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol)) return false;
        Symbol symbol = (Symbol) o;
        return name.equals(symbol.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public String getName() {
        return name;
    }
}
