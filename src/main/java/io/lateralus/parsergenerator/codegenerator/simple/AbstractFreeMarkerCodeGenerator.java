package io.lateralus.parsergenerator.codegenerator.simple;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.lateralus.parsergenerator.codegenerator.CodeGenerationException;
import io.lateralus.parsergenerator.codegenerator.CodeGenerator;

import java.io.IOException;

/**
 * Abstract base class for a code generator that uses FreeMarker templates.
 */
public abstract class AbstractFreeMarkerCodeGenerator<T, S> implements CodeGenerator<T, S> {

	private Configuration configuration;

	public AbstractFreeMarkerCodeGenerator(TemplateLoader templateLoader) {
		createConfiguration(templateLoader);
	}

	public AbstractFreeMarkerCodeGenerator(Configuration configuration) {
		this.configuration = configuration;
	}

	private void createConfiguration(TemplateLoader templateLoader) {
		configuration = new Configuration(Configuration.VERSION_2_3_28);
		configuration.setTemplateLoader(templateLoader);
		configuration.setDefaultEncoding("UTF-8");
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
		configuration.setWrapUncheckedExceptions(true);
	}

	protected Template determineTemplate(String templateName) throws CodeGenerationException {
		try {
			return configuration.getTemplate(templateName);
		} catch (IOException e) {
			throw new CodeGenerationException("Could not load template:", e);
		}
	}
}
