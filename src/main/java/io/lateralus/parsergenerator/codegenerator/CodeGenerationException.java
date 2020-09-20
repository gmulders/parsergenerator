package io.lateralus.parsergenerator.codegenerator;

/**
 * Exception that can be thrown from the code generator.
 */
public class CodeGenerationException extends Exception {

	public CodeGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
