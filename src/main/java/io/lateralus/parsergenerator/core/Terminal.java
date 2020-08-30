package io.lateralus.parsergenerator.core;

/**
 * Represents a terminal {@link Symbol}
 */
public class Terminal extends Symbol {

    public static final Terminal EPSILON = new Terminal("ε");
    public static final Terminal EOF = new Terminal("$");

    public Terminal(String name) {
        super(true, name);
    }
}
