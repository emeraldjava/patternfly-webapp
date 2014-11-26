package fr.pgervaise.patternfly.datatable.core;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public interface CSVExporterCallback {
	public Object adjustValue(String fieldName, Object value);
}
