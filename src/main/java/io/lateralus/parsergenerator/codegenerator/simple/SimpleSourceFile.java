package io.lateralus.parsergenerator.codegenerator.simple;

import io.lateralus.parsergenerator.codegenerator.SourceFile;

/**
 * Straightforward implementation of a {@link SourceFile}.
 */
public class SimpleSourceFile implements SourceFile<String> {

	private final String name;

	private final String contents;

	public SimpleSourceFile(final String name, final String contents) {
		this.name = name;
		this.contents = contents;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContents() {
		return contents;
	}
}
