package fr.pgervaise.patternfly.datatable.datasource;

import java.util.List;

import fr.pgervaise.patternfly.datatable.core.DataTable;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public interface DataTableDataSource<V> {
	public List<? extends Object> getResults(DataTable<V> dataTable);
}
