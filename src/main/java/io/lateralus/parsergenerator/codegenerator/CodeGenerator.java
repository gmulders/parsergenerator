package io.lateralus.parsergenerator.codegenerator;

import io.lateralus.parsergenerator.core.definition.ParserDefinition;

import java.util.Set;

/**
 * Represents a code codegenerator.
 * @param <T> The type of the properties object
 */
public interface CodeGenerator<T, S> {

	void setProperties(T properties);

	Set<SourceFile<S>> generate(ParserDefinition lexerDefinition) throws CodeGenerationException;
}
