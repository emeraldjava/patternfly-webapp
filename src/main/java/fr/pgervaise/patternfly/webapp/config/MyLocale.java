package fr.pgervaise.patternfly.webapp.config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * 
 * @author pgervaise
 *
 */
public class MyLocale extends LocaleChangeInterceptor {

	public MyLocale() {
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException {
		return super.preHandle(request, response, handler);
	}
}
