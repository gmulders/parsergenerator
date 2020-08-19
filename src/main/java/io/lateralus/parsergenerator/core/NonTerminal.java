package io.lateralus.parsergenerator.core;

/**
 * Represents a non-terminal {@link Symbol}
 */
public class NonTerminal extends Symbol {

    public static final NonTerminal START = new NonTerminal("S'");

    private final boolean isVanishable;

    public NonTerminal(String name) {
        super(false, name);
        this.isVanishable = false;
    }

    public NonTerminal(String name, boolean isVanishable) {
        super(false, name);
        this.isVanishable = isVanishable;
    }

    @Override
    public boolean isVanishable() {
        return isVanishable;
    }
}
