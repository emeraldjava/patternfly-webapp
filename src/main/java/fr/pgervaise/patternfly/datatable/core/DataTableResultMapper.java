package fr.pgervaise.patternfly.datatable.core;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Philippe Gervaise
 *
 * @param <VIEW>
 */
public interface DataTableResultMapper<VIEW> {
	public List<VIEW> map(DataTable<VIEW> dataTable, List<? extends Object> results);
	public Map<String, Object> getParameters();
}
