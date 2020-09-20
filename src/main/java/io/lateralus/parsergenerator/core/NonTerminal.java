package io.lateralus.parsergenerator.core;

/**
 * Represents a non-terminal {@link Symbol}
 */
public class NonTerminal extends Symbol {

    public static final NonTerminal START = new NonTerminal("Root");

    public NonTerminal(String name) {
        super(false, name);
    }
}
