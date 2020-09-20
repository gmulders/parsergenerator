package io.lateralus.parsergenerator.codegenerator;

/**
 * Represents a source file that can be generated.
 */
public interface SourceFile<S> {

	String getName();

	S getContents();
}
