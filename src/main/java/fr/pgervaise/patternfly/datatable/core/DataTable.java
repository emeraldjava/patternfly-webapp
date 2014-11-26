package fr.pgervaise.patternfly.datatable.core;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.pgervaise.patternfly.datatable.datasource.DataTableDataSource;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DataTable<VIEW> {

	public static final Logger logger = LoggerFactory.getLogger(DataTable.class);

	public static final String CSV = "csv";
	public static final String MODE_FLEXIGRID = "flexigrid";
	public static final String MODE_FLEXIGRID_UPDATE = "flexigrid-update";
	public static final String MODE_BOOTSTRAP_DYNAMIC = "bootstrap-dyn";
	public static final String MODE_BOOTSTRAP_DYNAMIC_UPDATE = "bootstrap-dyn-update";
	public static final String MODE_STATIC = "static";

	public static final Integer TRI_ASC = 1;
	public static final Integer TRI_DESC = 2;

	private Map<String, String> params = new LinkedHashMap<String, String>();
	private List<DataTableFilter> filters = new ArrayList<DataTableFilter>();
	private List<DataTableColumn> columns = new ArrayList<DataTableColumn>();
	// private List<DataTableResult> result = new

	private String id = null;
	private String sortLink = null;
	private String sortLinkGenPart = null;
	private String viewLink = null;
	private String modifyLink = null;
	private String deleteLink = null;
	private String changeStatusLink = null;
	
	private Integer pageIndex = 1;
	private Integer pageCount = 1;
	private Integer resultRealCount = null;
	private Integer resultCount = null;
	private Integer resultFirstIndex = null;
	private Integer resultLastIndex = null;

	private Integer resultsPerPage = 10;

	private String queryCountTemplate = null;
	private String queryTemplate = null;

	private List<VIEW> results = new ArrayList<VIEW>();
	
	private DataTableResultMapper<VIEW> resultMapper;

	private Class<VIEW> viewClass;

	private Integer modeTri = 0; // pas de colonne triée par défaut
	private Integer sensTri = 1;
	
	private String modeExportDefault = MODE_STATIC;
	private String modeExport = modeExportDefault;

	// Exports autorisés
	private String availableExporters = null;
	
	// Datasource personnalisée
	private DataTableDataSource dataSource = null;
	
	private String title = null;
	private boolean filtered;
	private Boolean showFilters = null;
	
	private boolean firstView = false;
	private boolean doQueryOnFirstView = false;
	private boolean noCount = false;
	
	private Integer maxResults = 1000;

	private Map<String, DataTableParameterModifier> dataTableParameterModifierMap = new HashMap<String, DataTableParameterModifier>();
	
	/**
	 * 
	 * @param viewClass
	 */
	public DataTable(Class<VIEW> viewClass) {
		this.viewClass = viewClass;
		// this.id = new UUIDGenerator().generateUUID();
		this.id = "_def";
	}

	/**
	 * 
	 * @param viewClass
	 * @param id
	 */
	public DataTable(Class<VIEW> viewClass, String id) {
		this.viewClass = viewClass;
		this.id = id;
	}

	/**
	 * @return 
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id 
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Retourne la classe de la vue
	 * @return
	 */
	public Class<VIEW> getViewClass() {
		return viewClass;
	}

	public DataTableDataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(DataTableDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void init(HttpServletRequest request) {
		init(request, null);
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 
	 * @param request
	 * @param parameterName
	 * @return
	 */
	private Integer getRequestParameterAsInteger(HttpServletRequest request, String parameterName) {
		if (getRequestParam(request, parameterName) == null)
			return null;
		
		String value = getRequestParam(request, parameterName);
		
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			
		}

		return null;
	}

	/**
	 * Initialisation SANS requête en BDD
	 * @param request
	 */
	public void initWithoutQuery(HttpServletRequest request) {
		init(request, null, false);
	}

	/**
	 * Initialisation avec requête en BDD
	 * @param request
	 * @param entityManager
	 */
	public void init(HttpServletRequest request, EntityManager entityManager) {
		boolean doQuery = true;

		String tempModeExport = getModeExport(request);

		if (tempModeExport == null)
			tempModeExport = modeExport;

		if (tempModeExport == null)
			doQuery = false;
		
		init(request, entityManager, doQuery);
	}

	/**
	 * Obtention d'un paramètre avec possibilité d'en modifier la valeur
	 * @param paramKey
	 * @return
	 */
	private String getParameter(HttpServletRequest request, String paramKey) {
		String value = getRequestParam(request, paramKey);

		DataTableParameterModifier dataTableParameterModifier = dataTableParameterModifierMap.get(paramKey);

		if (dataTableParameterModifier != null)
			value = dataTableParameterModifier.modify(value);

		return value;
	}

	/**
	 * Ajout un modificateur de paramètre
	 * @param paramKey
	 * @param dataTableParameterModifier
	 */
	public void setParameterModifier(String paramKey, DataTableParameterModifier dataTableParameterModifier) {
		dataTableParameterModifierMap.put(paramKey, dataTableParameterModifier);
	}

	/**
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public String getRequestParam(HttpServletRequest request, String name) {
		name = name + (id == null ? "" : id);
		return request.getParameter(name);
	}

	/**
	 * Initialisation
	 * @param request
	 * @param entityManager
	 */
	public void init(HttpServletRequest request, EntityManager entityManager, boolean doQuery) {
		String tempModeExport = getModeExport(request);

		if (tempModeExport != null)
			modeExport = tempModeExport;

		if (modeExport != null && !modeExport.equals(MODE_STATIC))
			resultsPerPage = null;
		
		if (tempModeExport == null || (getRequestParam(request, "firstView") != null && "1".equals(getRequestParam(request, "firstView")))) {
			firstView = true;
		}

		String showFiltersParam = getRequestParam(request, "showFilters");

		if ("1".equals(showFiltersParam))
			showFilters = true;

		if ("0".equals(showFiltersParam))
			showFilters = false;

		// Ni 0 ou ni 1 => reste à NULL


		Integer pageIndex = null;

		if (MODE_FLEXIGRID_UPDATE.equals(modeExport)) {
			pageIndex = getRequestParameterAsInteger(request, "page");
		} else {
			pageIndex = getRequestParameterAsInteger(request, "pageIndex");
		}

		if (pageIndex != null)
			setPageIndex(pageIndex);

		Integer tempResultsPerPage = getRequestParameterAsInteger(request, "rp");

		if (tempResultsPerPage != null)
			resultsPerPage = tempResultsPerPage;

		Integer modeTri = getModeTri(request);
		if (modeTri != null)
			setModeTri(modeTri);
		
		Integer sensTri = getSensTri(request);
		if (sensTri != null)
			setSensTri(sensTri);

		for (DataTableFilter filter : filters) {
			if (filter.isHidden())
				continue ;
			if (filter.isValueSet())
				continue ;
			String filterValue = getFilterValue(request, filter.getId());
			if (filterValue == null || filterValue.trim().equals(""))
				continue ;
			filter.setValue(filterValue);
			filtered = true;
			
			if (showFilters == null)
				showFilters = true;
		}

		genSortLink();

		boolean canDoQuery = true;

		if (doQuery && firstView && !doQueryOnFirstView) {
			canDoQuery = false;
			showFilters = true;
		}

		if (doQuery && canDoQuery) {
			if (dataSource != null) {
				doQuery(dataSource);
			} else if (entityManager != null) {
				doQuery(entityManager);
			}
		}

		if (resultCount != null && resultsPerPage != null) {
			pageCount = ((resultCount - 1) / resultsPerPage) + 1;
		}
		
		if (getPageIndex() > getPageCount())
			setPageIndex(getPageCount());
		
		if (resultCount != null) {
			if (resultsPerPage == null) {
				resultFirstIndex = 1;
				resultLastIndex = resultCount;
			} else {
				resultFirstIndex = ((getPageIndex() - 1) * resultsPerPage) + 1;
				resultLastIndex = resultFirstIndex + resultsPerPage;
				if (resultLastIndex >= resultCount)
					resultLastIndex = resultCount;
			}
		}
	}

	public String getFilterValue(HttpServletRequest request, String id) {
		
		for (int i = 0; i < filters.size(); i++) {
			if (id.equals(filters.get(i).getId())) {
				String value = getParameter(request, "filter_" + (i + 1));

				if (value != null && value.trim().equals(""))
					value = null;

				return value;
			}
		}

		return null;
	}
	
	/**
	 * 
	 * @param dataTableFilter
	 * @return
	 */
	public String getFilterParameterName(DataTableFilter dataTableFilter) {
		if (dataTableFilter.getParametreName() != null)
			return dataTableFilter.getParametreName();

		int indexOf = filters.indexOf(dataTableFilter);
		
		if (indexOf < 0)
			return null;

		return "filter_" + (indexOf + 1);
	}

	/**
	 * 
	 * @param dataTableFilter
	 * @return
	 */
	public String getFilterParameterNameById(String id) {
		DataTableFilter dataTableFilter = getFilterById(id);

		if (dataTableFilter == null)
			return null;

		if (dataTableFilter.getParametreName() != null)
			return dataTableFilter.getParametreName();

		int indexOf = filters.indexOf(dataTableFilter);
		
		if (indexOf < 0)
			return null;

		return "filter_" + (indexOf + 1) + (this.id == null ? "" : this.id);
	}

	/**
	 * Retourne un filtre selon son ID
	 * @param id
	 * @return
	 */
	public DataTableFilter getFilterById(String id) {
		if (id == null || filters == null)
			return null;

		for (DataTableFilter filter : filters) {
			if (id.equals(filter.getId()))
				return filter;
		}

		return null;
	}

	public List<DataTableFilter> getFilters() {
		return filters;
	}

	public DataTable<VIEW> addFilter(DataTableFilter filter) {
		filters.add(filter);

		if (filter.getId() == null)
			filter.setId("filter_" + (filters.size() + 1));

		return this;
	}
	
	public DataTable<VIEW> removeFilterById(String filterId) {
		if (filterId == null)
			return this;

		Iterator<DataTableFilter> it = filters.iterator();
		
		while (it.hasNext()) {
			DataTableFilter filter = it.next();
			if (filterId.equals(filter.getId())) {
				it.remove();
				break;
			}
		}

		return this;
	}

	public String getParam(String name) {
		return params.get(name);
	}

	public Map<String, String> getParams() {
		return params;
	}
	
	public DataTable<VIEW> addParam(String name, String value) {
		params.put(name, value);
		
		return this;
	}

	public boolean hasColumn(String columnId) {
		int pointIndex = columnId.indexOf('.');
		if (pointIndex >= 0)
			columnId = columnId.substring(pointIndex);
		else
			columnId = "." + columnId;

		for (DataTableColumn column : columns) {
			if (("." + column.getId()).endsWith(columnId)) {
				return true;
			}
		}

		return false;
	}

	public List<DataTableColumn> getColumns() {
		return columns;
	}

	/**
	 * 
	 * @param columnId
	 * @return
	 */
	public DataTableColumn getColumnById(String columnId) {
		for (DataTableColumn column : columns) {
			if (column.getId().equals(columnId))
				return column;
		}
		
		return null;
	}

	public DataTable<VIEW> addColumn(DataTableColumn column) {
		columns.add(column);

		return this;
	}

	
	/**
	 * 
	 */
	private void genSortLink() {
		StringBuilder sortLink = new StringBuilder();

		// Prend les filtres
		int filterIndex = 0;

		for (DataTableFilter filterLoop : filters) {
			filterIndex++;

			if (filterLoop.isHidden())
				continue ;

			if (filterLoop.getValue() == null)
				continue ;

			sortLink.append("&");

			try {
				sortLink.append("filter_" + filterIndex);
				sortLink.append(id == null ? "" : id);
				sortLink.append("=");
				sortLink.append(filterLoop.getValue() == null ? "" : URLEncoder.encode(filterLoop.getValue().toString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }

		}

		// Prend la page en cours
		sortLink.append("&pageIndex");
		sortLink.append(id == null ? "" : id);
		sortLink.append("=");
		sortLink.append(pageIndex == null ? "" : pageIndex.toString());

		// Ajoute le mode d'export
		try {
			sortLink.append("&modeExport");
			sortLink.append(id == null ? "" : id);
			sortLink.append("=");
			sortLink.append(modeExport == null ? "" : URLEncoder.encode(modeExport, "UTF-8"));
		} catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }

		// Ajoute les paramètres fixes
		for (String paramName : params.keySet()) {
			String paramValue = params.get(paramName);
			
			if (paramValue == null)
				continue ;

			sortLink.append("&");
			try {
				sortLink.append(URLEncoder.encode(paramName, "UTF-8"));
				sortLink.append("=");
				sortLink.append(URLEncoder.encode(paramValue, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		sortLinkGenPart = sortLink.deleteCharAt(0).toString();
	}

	@SuppressWarnings ("unchecked")
	public Class<VIEW> getTypeParameterClass()
	{
	    Type type = getClass().getGenericSuperclass();
	    ParameterizedType paramType = (ParameterizedType) type;
	    return (Class<VIEW>) paramType.getActualTypeArguments()[0];
	}

	private void sortViewResults(List<? extends Object> viewResult, DataTableColumn orderByColumn) {
		String orderById = orderByColumn.getId();
		
		if (orderById != null) {
			int pointIndex = orderById.indexOf('.');
			
			if (pointIndex >= 0)
				orderById = orderById.substring(pointIndex + 1);

			final String finalOrderById = orderById;

			Collections.sort(viewResult, new Comparator<Object>() {
				public int compare(Object o1, Object o2) {

					if (sensTri == 2) {
						Object o3 = o1;
						o1 = o2;
						o2 = o3;
					}

					if (o1 == o2)
						return 0;

					if (o1 == null)
						return -1;

					if (o2 == null)
						return 1;

					if (o1 != null && o2 != null && o1.getClass() == o2.getClass()) {
						String methodName = "get" +
							Character.toUpperCase(finalOrderById.charAt(0)) +
							finalOrderById.substring(1);

						try {
							Method m = o1.getClass().getMethod(methodName);

							o1 = m.invoke(o1);
							o2 = m.invoke(o2);

							if (o1 == o2)
								return 0;

							if (o1 == null)
								return -1;

							if (o1 != null)
								return ((Comparable<Object>) o1).compareTo(o2);

						} catch (Exception e) {
							return 0;
						}
					}

					return 0;
				};
			});
		}
	}

	/**
	 * Filtrage (sur place) d'une liste de résultats d'une vue
	 * @param viewResults
	 */
	private void filterViewResults(List<VIEW> viewResults, List<DataTableFilter> filters) {
		if (viewResults.size() == 0)
			return ;

		Class<?> viewClass = viewResults.get(0).getClass();

		// Association, pour chaque filtre, du getter de la vue
		Map<DataTableFilter, Method> filterMethodMapping = new HashMap<DataTableFilter, Method>();

		for (DataTableFilter filter : filters) {
			if (filter.isSkipOnFilter())
				continue ;
			String id = filter.getId();
			String getterName = "get" + Character.toUpperCase(id.charAt(0)) + (id.length() > 1 ? id.substring(1) : "");
			Method viewMethod = null;
			try { 
				viewMethod = viewClass.getMethod(getterName, null);
			} catch (NoSuchMethodException e) {
				logger.warn("Méthode non trouvée : " + getterName + "() dans la classe " + viewClass);
				
				viewResults.clear();
				return ;
			}

			filterMethodMapping.put(filter, viewMethod);
		}

		Iterator<VIEW> viewIterator = viewResults.iterator();

		while (viewIterator.hasNext()) {
			// On récupère la valeur de la vue
			VIEW view = viewIterator.next();

			boolean filterOk = true;

			for (DataTableFilter filter : filters) {
				Method viewMethod = filterMethodMapping.get(filter);
				
				if (viewMethod == null)
					continue ;
				
				Object viewValueObject = null;
				try {
					viewValueObject = viewMethod.invoke(view, null);
				} catch (Exception e) {
					logger.warn("Erreur dans l'appel de la méthode " + viewMethod.getName() + "() dans un objet de classe " + viewClass, e);
					viewResults.clear();

					return ;
				}

				if (filter.getValue() != null) {
					if (viewValueObject == null)
						filterOk = false;
					else {
						String viewValue = viewValueObject.toString();
						String filterValue = filter.getValue().toString();
	
						switch (filter.getOperator()) {
							case IS : if (!viewValue.equals(filterValue)) filterOk = false; break;
							case BEGIN_WITH : if (!viewValue.startsWith(filterValue)) filterOk = false; break;
							case END_WITH : if (!viewValue.endsWith(filterValue)) filterOk = false; break;
							case CONTAINS : if (!viewValue.contains(filterValue)) filterOk = false; break;
							case GREATER : if (viewValue.compareTo(filterValue) > 0) filterOk = false; break;
							case GREATER_OR_EQUAL : if (viewValue.compareTo(filterValue) >= 0) filterOk = false; break;
							case LOWER : if (viewValue.compareTo(filterValue) < 0) filterOk = false; break;
							case LOWER_OR_EQUAL : if (viewValue.compareTo(filterValue) <= 0) filterOk = false; break;
							default :
								logger.warn("Erreur dans le filtrage virtuel ('" + filter.label + "'), opérateur non supporté => " + filter.getOperator());
								viewResults.clear();
								return ;
						}
					}

					if (!filterOk) {
						// Le filtre n'est pas satisfait on arrête
						break;
					}
				}
			}
			
			if (!filterOk)
				viewIterator.remove();
				
		}
		
	}

	/**
	 * 
	 * @param dataSource
	 */
	private void doQuery(DataTableDataSource<VIEW> dataSource) {
		List<? extends Object> results = dataSource.getResults(this);

		boolean mapped = false;

		DataTableColumn orderByColumn = null;

		// Ordonnancement de la vue
		if (modeTri != null && modeTri > 0) {

			if (resultMapper != null) {
				mapped = true;
				results = resultMapper.map(this, results);
			}

			orderByColumn = columns.get(modeTri - 1);

			if (!orderByColumn.isVirtualSort())
				sortViewResults(results, orderByColumn);
		}

		List<VIEW> mappedResults = resultMapper != null && !mapped ? resultMapper.map(this, results) : (List<VIEW>) results;

		if (filters.size() > 0)
			filterViewResults(mappedResults, filters);

		if (orderByColumn != null && orderByColumn.isVirtualSort())
			sortViewResults(results, orderByColumn);

		resultCount = mappedResults.size();

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

		setResults(resultsView);
	}

	/**
	 * 
	 * @param entityManager
	 */
	private void doQuery(EntityManager entityManager) {
		StringBuilder queryString = new StringBuilder();

		int filterIndex = 0;

		Map<String, Object> parameters = new HashMap<String, Object>();

		List<DataTableFilter> virtualFilters = new ArrayList<DataTableFilter>();

		for (DataTableFilter filter : filters) {
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
			queryString.insert(0, "FROM " + viewClass.getSimpleName());
			countQueryString = new StringBuilder(queryString);
			countQueryString.insert(0, "SELECT COUNT(*) ");
			queryString.append(orderBy);
		}

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

		List<VIEW> viewResults = resultMapper != null ? resultMapper.map(this, results) : (List<VIEW>) results;
		
		if (virtualFilters.size() > 0) {
			// Filtrage virtuel
			filterViewResults(viewResults, virtualFilters);
			resultCount = viewResults.size();
		}

		if (resultCount == null)
			resultCount = viewResults.size();

		if (orderByColumn != null && orderByColumn.isVirtualSort())
			sortViewResults(viewResults, orderByColumn);

		setResults(viewResults);
	}

	public String getSortLink() {
		return ((sortLink != null && sortLink.indexOf('?') >= 0) ? "&" : "?") + sortLinkGenPart;
	}

	/*
	public DataTable setSortLink(String sortLinkBegin, HttpServletRequest request) {
		this.sortLink = sortLinkBegin + "?";
		
		return this;
	} */
	
	/**
	 * 
	 * @return
	 */
	public String getViewLink() {
		return viewLink;
	}
	
	/**
	 * 
	 * @param viewLink
	 */
	public DataTable<VIEW> setViewLink(String viewLink) {
		this.viewLink = viewLink;
		
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public String getModifyLink() {
		return modifyLink;
	}

	/**
	 * 
	 * @param modifyLink
	 * @return
	 */
	public DataTable<VIEW> setModifyLink(String modifyLink) {
		this.modifyLink = modifyLink;
		
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDeleteLink() {
		return deleteLink;
	}
	
	/**
	 * 
	 * @param deleteLink
	 * @return
	 */
	public DataTable<VIEW> setDeleteLink(String deleteLink) {
		this.deleteLink = deleteLink;
		
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public String getChangeStatusLink() {
		return changeStatusLink;
	}

	/**
	 * Positionne un lien permettant de changer une ligne de status (0 -> 1, 1 -> 0)
	 * @param changeStatusLink
	 * @return
	 */
	public DataTable<VIEW> setChangeStatusLink(String changeStatusLink) {
		this.changeStatusLink = changeStatusLink;

		return this;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getPageIndex() {
		return pageIndex;
	}

	/**
	 * 
	 * @param pageIndex
	 * @return
	 */
	public DataTable<VIEW> setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;

		return this;
	}

	public Integer getPageCount() {
		return pageCount;
	}
	
	public DataTable<VIEW> setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
		
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<VIEW> getResults() {
		return results;
	}

	/**
	 * 
	 * @param results
	 */
	public DataTable<VIEW> setResults(List<VIEW> results) {
		this.results = results;

		return this;
	}
	
	public String getQueryCountTemplate() {
		return queryCountTemplate;
	}
	
	public void setQueryCountTemplate(String queryCountTemplate) {
		this.queryCountTemplate = queryCountTemplate;
	}

	/**
	 * 
	 * @return
	 */
	public String getQueryTemplate() {
		return queryTemplate;
	}

	/**
	 * 
	 * @param queryTemplate
	 */
	public void setQueryTemplate(String queryTemplate) {
		this.queryTemplate = queryTemplate;
	}
	
	/**
	 * 
	 * @return
	 */
	public DataTableResultMapper<VIEW> getResultMapper() {
		return resultMapper;
	}

	/**
	 * 
	 * @param resultMapper
	 */
	public void setResultMapper(DataTableResultMapper<VIEW> resultMapper) {
		this.resultMapper = resultMapper;
	}
	
	/**
	 * Retourne l'index de la colonne actuellement triée
	 * @return
	 */
	public Integer getModeTri() {
		return modeTri;
	}
	
	/**
	 * Pour un nom de colonne retourne l'index de cette dernière dans le cas d'un tri
	 * @param modeTri
	 * @return
	 */
	public Integer getModeTri(String modeTri) {
		if (modeTri == null)
			return null;

		int index = 1;

		for (DataTableColumn column : columns) {
			if (modeTri.equals(column.getId())) {
				return index;
			}
			index++;
		}
		
		return null;
	}
	
	/**
	 * Retourne, à partir d'une requête web, l'index de la colonne sur laquelle trier ou
	 * NULL si pas de tri
	 * @param request Requête HTTP
	 * @return Index de la colonne ou NULL si pas de tri
	 */
	public Integer getModeTri(HttpServletRequest request) {
		String modeExport = getModeExport(request);

		if (MODE_FLEXIGRID_UPDATE.equals(modeExport)) {
			String sortName = getRequestParam(request, "sortname");

			if (sortName != null)
				return getModeTri(sortName);
		} else {
			Integer modeTri = getRequestParameterAsInteger(request, "modeTri");

			if (modeTri != null)
				return modeTri;
			else {
				// Recherche si de type String
				String modeTriAsString = getRequestParam(request, "modeTri");
				if (modeTriAsString != null && !modeTriAsString.trim().equals("")) {
					return getModeTri(modeTriAsString);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Positionne l'index de la colonne sur lequel il y aura le tri
	 * @param modeTri
	 */
	public void setModeTri(Integer modeTri) {
		this.modeTri = modeTri;
	}
	
	/**
	 * Positionne le nom de la colonne sur laquelle il y aura le tri
	 * (sera converti en index)
	 * @param modeTri
	 */
	public void setModeTri(String modeTri) {
		this.modeTri = getModeTri(modeTri);
	}

	public Integer getSensTri() {
		return sensTri;
	}
	
	/**
	 * Retourne le sens de tri d'un paramètre de type String
	 * @param sensTri 0 ou 1 ou ASC ou DESC
	 * @return
	 */
	public Integer getSensTri(String sensTri) {
		if (sensTri != null && "desc".equals(sensTri.toLowerCase()))
			return 2;
		
		// ASC par défaut
		return 1;
	}
	
	/**
	 * Retourne le sens de tri d'une requête web
	 * @param request
	 * @return
	 */
	public Integer getSensTri(HttpServletRequest request) {
		String modeExport = getModeExport(request);
		
		if (MODE_FLEXIGRID.equals(modeExport)) {
			return getSensTri(getRequestParam(request, "sortorder"));
		} else {
			Integer sensTri = getRequestParameterAsInteger(request, "sensTri");

			if (sensTri != null)
				setSensTri(sensTri);
			else {
				// Recherche si de type string
				String sensTriAsString = getRequestParam(request, "sensTri");
				if (sensTriAsString != null && !sensTriAsString.trim().equals("")) {
					setSensTri(sensTriAsString);
				}
			}
		}

		return null;
	}
	
	public void setSensTri(Integer sensTri) {
		this.sensTri = sensTri;
	}
	
	public void setSensTri(String sensTri) {
		this.sensTri = getSensTri(sensTri);
	}

	/**
	 * Nombre de résultats. Attention une limite de 1000 résultats est appliqué
	 * par défaut.
	 * @return
	 */
	public Integer getResultCount() {
		return resultCount;
	}
	
	/**
	 * Nombre de résultats avant limitation du nombre affiché. Permet de savoir
	 * si le filtre ramène plus de résultats que le nombre réellement affiché.
	 * @return
	 */
	public Integer getResultRealCount() {
		return resultRealCount;
	}
	
	public String getAvailableExporters() {
		return availableExporters;
	}

	public void setAvailableExporters(String availableExporters) {
		this.availableExporters = availableExporters;
	}

	public String exportToCSV() {
		return new CSVExporter().doExport(this);
	}

	public String exportToCSV(CSVExporter csvExporter) {
		return csvExporter.doExport(this);
	}
	
	public String getModeExport() {
		return modeExport;
	}

	/**
	 * Retourne le mode d'export d'une requête
	 * @param request
	 * @return
	 */
	public String getModeExport(HttpServletRequest request) {
		String modeExport = getRequestParam(request, "modeExport");
		
		if (modeExport != null && modeExport.trim().equals(""))
			modeExport = null;
		
		return modeExport;
	}

	public void setModeExport(String modeExport) {
		this.modeExport = modeExport;
	}
	
	/**
	 * Détermine si la table est actuellement filtrée
	 * @return
	 */
	public boolean isFiltered() {
		return filtered;
	}

	/**
	 * Détermine s'il s'agit du premier affichage du tableau
	 * @return
	 */
	public boolean isFirstView() {
		return firstView;
	}
	
	/**
	 * Détermine s'il on fait une requête dès le premier affichage du formulaire (sans filtre)
	 * @return
	 */
	public boolean isDoQueryOnFirstView() {
		return doQueryOnFirstView;
	}
	
	/**
	 * Détermine s'il on fait une requête dès le premier affichage du formulaire (sans filtre)
	 * @param doQueryOnFirstView
	 */
	public void setDoQueryOnFirstView(boolean doQueryOnFirstView) {
		this.doQueryOnFirstView = doQueryOnFirstView;
	}

	/**
	 * Détermine s'il faut afficher les filtres
	 * @return
	 */
	public Boolean isShowFilters() {
		return showFilters;
	}
	
	/**
	 * Force l'affichage des filtres
	 * @param showFilters
	 */
	public void setShowFilters(Boolean showFilters) {
		this.showFilters = showFilters;
	}
	
	/**
	 * Nombre de résultats par page
	 * @return
	 */
	public Integer getResultsPerPage() {
		return resultsPerPage;
	}
	
	/**
	 * Nombre de résultats par page
	 * @param resultsPerPage
	 */
	public void setResultsPerPage(Integer resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}
	
	/**
	 * Index du premier résultat
	 * @return
	 */
	public Integer getResultFirstIndex() {
		return resultFirstIndex;
	}
	
	/**
	 * Index du dernier résultat
	 * @return
	 */
	public Integer getResultLastIndex() {
		return resultLastIndex;
	}

	/**
	 * @return 
	 */
	public boolean isNoCount() {
		return noCount;
	}

	/**
	 * @param noCount 
	 */
	public void setNoCount(boolean noCount) {
		this.noCount = noCount;
	}
}
