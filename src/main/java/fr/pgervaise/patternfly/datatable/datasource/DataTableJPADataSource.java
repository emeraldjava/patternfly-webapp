package fr.pgervaise.patternfly.datatable.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import fr.pgervaise.patternfly.datatable.core.DataTable;
import fr.pgervaise.patternfly.datatable.core.DataTableColumn;
import fr.pgervaise.patternfly.datatable.core.DataTableFilter;
import fr.pgervaise.patternfly.datatable.core.DataTableResultMapper;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DataTableJPADataSource<VIEW> extends AbstractDataTableDataSource<VIEW> {

    private EntityManager entityManager;
    private String queryTemplate = null;
    private String queryCountTemplate = null;
    private boolean noCount = false;

    /**
     * 
     * @param dataTable
     * @param entityManager
     */
    public DataTableJPADataSource(DataTable<VIEW> dataTable, EntityManager entityManager) {
    	super(dataTable);

        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

	@Override
	public void doQuery() {
		StringBuilder queryString = new StringBuilder();

		int filterIndex = 0;

		Map<String, Object> parameters = new HashMap<String, Object>();

		List<DataTableFilter> virtualFilters = new ArrayList<DataTableFilter>();

		for (DataTableFilter filter : dataTable.getFilters()) {
			filterIndex++;
			StringBuilder filterName = new StringBuilder(filter.getId() == null ? "FILTER_ID" : filter.getId());

			// Suppression de tout caractère non autorisé
			for (int i = 0; i < filterName.length(); i++) {
				char c = filterName.charAt(i);
				if (Character.isLetterOrDigit(c) || c == '.')
					continue ;
				filterName = filterName.deleteCharAt(i);
			}

			Object filterValue = null;
			
			try {
				filterValue = filter.getTypedValue();
			} catch (Exception e) {
				return ;
			}

			if (filterValue == null || filterValue.toString().trim().length() == 0)
				continue ;

			if (filter.isVirtual()) {
				// Un filtre virtuel n'est pas exploité dans la requête
				virtualFilters.add(filter);
				continue ;
			}
			
			if (filter.isSkipOnFilter()) {
				// Le filtre est indiqué comme à laisser de coté
				continue ;
			}

			String filterParamName = "filterValue" + filterIndex;

			queryString.append(" AND ");
			queryString.append(filterName.toString());

			switch (filter.getOperator()) {
				case IS : queryString.append(" = "); break;
				case GREATER : queryString.append(" > "); break;
				case GREATER_OR_EQUAL : queryString.append(" >= "); break;
				case LOWER : queryString.append(" < "); break;
				case LOWER_OR_EQUAL : queryString.append(" <= "); break;
				case CONTAINS : queryString.append(" like "); filterValue = "%" + filterValue + "%"; break;
				case BEGIN_WITH : queryString.append(" like "); filterValue = filterValue + "%"; break;
				case END_WITH : queryString.append(" like "); filterValue = "%" + filterValue; break;
				default : queryString.append(" = ");
			}

			queryString.append(":");
			queryString.append(filterParamName);

			parameters.put(filterParamName, filterValue);
		}

		StringBuilder orderBy = new StringBuilder();

		if (queryTemplate != null
			&& queryTemplate.toLowerCase().indexOf(" order by ") > 0) {
			// Le template possède déjà un order by
			// Extraction de ce dernier avec intégration dans le "order by"
			int orderByIndex = queryTemplate.toLowerCase().indexOf(" order by ");
			orderBy.append(queryTemplate.substring(orderByIndex));
			queryTemplate = queryTemplate.substring(0, orderByIndex);
		}

		DataTableColumn orderByColumn = null;

		Integer modeTri = dataTable.getModeTri();
		Integer sensTri = dataTable.getSensTri();

		List<DataTableColumn> columns = dataTable.getColumns();

		if (modeTri != null && modeTri > 0) {
			orderByColumn = columns.get(modeTri - 1);

			if (!orderByColumn.isVirtualSort()) {
				String orderById = orderByColumn.getId();

				if (orderById != null) {
					orderBy.append(orderBy.length() == 0 ? " ORDER BY " : ", ");
					orderBy.append(orderById);
	
					if (sensTri != null)
						orderBy.append(" ").append(sensTri == 1 ? "ASC" : "DESC");
				}
			}
		}

		// Supprime le premier " AND" si PAS de where
		if (queryTemplate != null
			&& queryTemplate.toLowerCase().indexOf(" where ") < 0
			&& queryString.length() > 4 && queryString.substring(0, 4).equals(" AND"))
		{
			queryString.delete(0, 4);
		}

		StringBuilder countQueryString = new StringBuilder();
		
		countQueryString.append(queryString);

		if (queryTemplate != null) {
			if (queryCountTemplate == null) {
				queryString.insert(0, queryTemplate);
				countQueryString = new StringBuilder(queryString);
				countQueryString.insert(0, "SELECT COUNT(*) ");
			} else {
				countQueryString = new StringBuilder(queryString);
				queryString.insert(0, queryTemplate);
				countQueryString.insert(0, queryCountTemplate);
			}

			queryString.append(orderBy);
		} else {
			queryString.insert(0, "FROM " + dataTable.getViewClass().getSimpleName());
			countQueryString = new StringBuilder(queryString);
			countQueryString.insert(0, "SELECT COUNT(*) ");
			queryString.append(orderBy);
		}

		Integer resultsPerPage = dataTable.getResultsPerPage();
		Integer maxResults = dataTable.getMaxResults();
		Integer pageIndex = dataTable.getPageIndex();
		DataTableResultMapper<VIEW> resultMapper = dataTable.getResultMapper();

		Integer resultCount = null;
		Integer resultRealCount = null;

		if (!isNoCount() && countQueryString.length() > 0 && virtualFilters.size() == 0) {
			// Un comptage ne peut être effectué que s'il n'existe pas de filtre
			// virtuel
			Query countQuery = entityManager.createQuery(countQueryString.toString());
			
			for (String paramKey : parameters.keySet())
				countQuery.setParameter(paramKey, parameters.get(paramKey));

			// logger.debug("Requete de comptage: " + countQueryString.toString());

			Object count = countQuery.getSingleResult();

			if (count != null) {
				try {
					resultCount = Integer.valueOf(count.toString());
				} catch (Exception e) { }
			}
		}

		if (maxResults != null && resultCount != null && resultCount > maxResults) {
			resultRealCount = resultCount;
			resultCount = maxResults;
		}

		Query query = entityManager.createQuery(queryString.toString());
		
		if (resultsPerPage != null && virtualFilters.size() == 0) {
			// Il est précisé un resultat maximum par page, il y donc une sorte de curseur
			// Par contre cela ne peut pas être appliqué dans le cadre d'un filtre virtuel
			
			// Vérification aussi que firstResult est en accord entre pageIndex et resultCount
			Integer queryPageIndex = pageIndex;

			if (resultCount != null && resultsPerPage != null) {
				Integer maxPageCount = ((resultCount - 1) / resultsPerPage) + 1;
	
				if (queryPageIndex > maxPageCount) {
					// La page demandée est au delà du nombre de pages attendues
					// Réajustement de celle-ci
					queryPageIndex = maxPageCount;
				}
			}

			query.setMaxResults(resultsPerPage);
			query.setFirstResult(resultsPerPage * (queryPageIndex - 1));
		} else if (maxResults != null) {
			query.setMaxResults(maxResults);
		}

		for (String paramKey : parameters.keySet())
			query.setParameter(paramKey, parameters.get(paramKey));

		List<Object> results = query.getResultList();

		/*
		if (results.size() > 1)
			results = new ArrayList<Object>(new LinkedHashSet<Object>(results));
			*/

		List<VIEW> viewResults = resultMapper != null ? resultMapper.map(dataTable, results) : (List<VIEW>) results;

		if (virtualFilters.size() > 0) {
			// Filtrage virtuel
			filterViewResults(viewResults, virtualFilters);
			resultCount = viewResults.size();
		}

		if (resultCount == null)
			resultCount = viewResults.size();

		if (orderByColumn != null && orderByColumn.isVirtualSort())
			sortViewResults(viewResults, orderByColumn);

		dataTable.setResults(viewResults);
		dataTable.setResultCount(resultCount);
		dataTable.setResultRealCount(resultRealCount);
	}

    /**
     * 
     * @param noCount
     */
    public void setNoCount(boolean noCount) {
    	this.noCount = noCount;
    }

    /**
     * 
     */
    public boolean isNoCount() {
    	return noCount;
    }
}
