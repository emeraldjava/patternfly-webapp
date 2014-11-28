package fr.pgervaise.patternfly.webapp.config;

import java.util.Locale;

import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class MySessionLocale extends SessionLocaleResolver {
	
	@Override
	protected Locale getDefaultLocale() {
		return super.getDefaultLocale();
	}
}
