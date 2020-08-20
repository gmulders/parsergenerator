package io.lateralus.parsergenerator.core;

/**
 * Represents a non-terminal {@link Symbol}
 */
public class NonTerminal extends Symbol {

    public static final NonTerminal START = new NonTerminal("S'");

    private boolean canVanish;

    public NonTerminal(String name) {
        super(false, name);
        this.canVanish = false;
    }

    @Override
    public boolean canVanish() {
        return canVanish;
    }

    public void markVanishable() {
        canVanish = true;
    }
}
