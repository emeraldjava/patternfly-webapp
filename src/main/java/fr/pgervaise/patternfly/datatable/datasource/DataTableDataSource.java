package fr.pgervaise.patternfly.datatable.datasource;

import java.util.List;

import fr.pgervaise.patternfly.datatable.core.DataTable;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public interface DataTableDataSource<V> {

	public void doQuery();

	/**
	 * Datasource can communicate how much results in total ?<br>
	 * Example : filtering can return > 1 million results. That number can be shown even
	 * if we show the first 1000 results ?
	 * @return
	 */
	public boolean isNoCount();
}
