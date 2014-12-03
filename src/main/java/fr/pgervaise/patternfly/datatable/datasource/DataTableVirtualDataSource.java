package fr.pgervaise.patternfly.datatable.datasource;

import java.util.ArrayList;
import java.util.List;

import fr.pgervaise.patternfly.datatable.core.DataTable;
import fr.pgervaise.patternfly.datatable.core.DataTableColumn;
import fr.pgervaise.patternfly.datatable.core.DataTableFilter;
import fr.pgervaise.patternfly.datatable.core.DataTableResultMapper;


/**
 * 
 * @author pgervaise
 *
 */
public abstract class DataTableVirtualDataSource<VIEW> extends AbstractDataTableDataSource<VIEW> {

	public abstract List<? extends Object> getResults();

	/**
	 * 
	 * @param dataTable
	 */
	public DataTableVirtualDataSource(DataTable<VIEW> dataTable) {
		super(dataTable);
	}

	@Override
	public void doQuery() {
		List<? extends Object> results = getResults();

		boolean mapped = false;

		DataTableColumn orderByColumn = null;
		DataTableResultMapper<VIEW> resultMapper = dataTable.getResultMapper();
		List<DataTableColumn> columns = dataTable.getColumns();

		Integer pageIndex = dataTable.getPageIndex();
		Integer modeTri = dataTable.getModeTri();
		Integer resultsPerPage = dataTable.getResultsPerPage();
		List<DataTableFilter> filters = dataTable.getFilters();

		// Ordonnancement de la vue
		if (modeTri != null && modeTri > 0) {

			if (resultMapper != null) {
				mapped = true;
				results = resultMapper.map(dataTable, results);
			}

			orderByColumn = columns.get(modeTri - 1);

			if (!orderByColumn.isVirtualSort())
				sortViewResults(results, orderByColumn);
		}

		List<VIEW> mappedResults = resultMapper != null && !mapped ? resultMapper.map(dataTable, results) : (List<VIEW>) results;

		if (filters.size() > 0)
			filterViewResults(mappedResults, filters);

		if (orderByColumn != null && orderByColumn.isVirtualSort())
			sortViewResults(results, orderByColumn);

		Integer resultCount = mappedResults.size();

		List<VIEW> resultsView = new ArrayList<VIEW>();

		if (results != null) {
			if (resultsPerPage == null)
				resultsView.addAll(mappedResults);
			else {
				for (int i = 0; i < resultsPerPage; i++) {
					int index = resultsPerPage * (pageIndex - 1) + i;
					
					if (index >= mappedResults.size())
						break;
	
					resultsView.add(mappedResults.get(resultsPerPage * (pageIndex - 1) + i));
				}
			}
		}
		
		dataTable.setResultCount(resultCount);
		dataTable.setResults(resultsView);
	}

	@Override
	public boolean isNoCount() {
		return false;
	}
}
