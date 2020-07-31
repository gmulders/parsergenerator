package io.lateralus.parsergenerator.codegenerator;

import java.nio.file.Path;

/**
 * TODO: This should probably become an interface; see what I did for the lexergenerator.
 */
public class CodeGenerator {

	private final Path path;


	public CodeGenerator(Path path) {
		this.path = path;
	}

	public void outputParser() {
		// Generate code
	}
}
