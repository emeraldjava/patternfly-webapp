package fr.pgervaise.patternfly.datatable.core;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
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

import fr.pgervaise.patternfly.datatable.core.DataTableFilter.Operator;
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
	 * @param id Short ID of Datatable (ex: "def") for using several datatables in one HTML view
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
	 * Initialisation
	 * @param request
	 */
	public void init(HttpServletRequest request) {
		init(request, true);
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
	 * Get a parameter for THIS datatable (i.e. against internal ID)
	 * @param request
	 * @param name
	 * @return
	 */
	public String getRequestParam(HttpServletRequest request, String name) {
		return request.getParameter(name + "_" + id);
	}

	/**
	 * Initialisation
	 * @param request
	 * @param entityManager
	 */
	public void init(HttpServletRequest request, boolean doQuery) {
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
			String filterValue = getFilterValue(request, filter);
			if (filterValue == null || filterValue.trim().equals(""))
				continue ;
			filter.setValue(filterValue);
			filtered = true;

			// Check if Operator set
			Operator operator = getFilterOperator(request, filter);
			if (operator != null)
				filter.setOperator(operator);

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
				dataSource.doQuery();
			} else {
				logger.warn("No datasource !");
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

	/**
	 * 
	 * @param request
	 * @param dataTableFilter
	 * @return
	 */
	public String getFilterValue(HttpServletRequest request, DataTableFilter dataTableFilter) {
		String id = dataTableFilter.getId();

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
	 * @param request
	 * @param dataTableFilter
	 * @return
	 */
	public Operator getFilterOperator(HttpServletRequest request, DataTableFilter dataTableFilter) {
		String id = dataTableFilter.getId();

		for (int i = 0; i < filters.size(); i++) {
			if (id.equals(filters.get(i).getId())) {
				String operatorValue = getParameter(request, "filter_" + (i + 1) + "_op");

				if (operatorValue != null && operatorValue.trim().equals(""))
					operatorValue = null;
				
				if (operatorValue != null) {
					// convert to an operator
					for (DataTableFilterOperator dataTableFilterOperator : dataTableFilter.getAcceptedOperators()) {
						if (dataTableFilterOperator.getOperator() != null)
							if (operatorValue.equals(dataTableFilterOperator.getOperator().getId().toString()))
								return dataTableFilterOperator.getOperator();
					}
				}

				return null;
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

	/**
	 * Determine if this datatable got a visible filter (not hidden)
	 * @return
	 */
	public Boolean hasVisibleFilter() {
		for (DataTableFilter dataTableFilter : filters) {
			if (!dataTableFilter.isHidden())
				return true;
		}

		return false;
	}
	
	/**
	 * Determine if filter bar is visible
	 * @return
	 */
	public boolean isFilterBarVisible() {
		return false;
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
				sortLink.append("filter_" + filterIndex + "_");
				sortLink.append(id);
				sortLink.append("=");
				sortLink.append(filterLoop.getValue() == null ? "" : URLEncoder.encode(filterLoop.getValue().toString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }

			if (filterLoop.getAcceptedOperators() != null && filterLoop.getAcceptedOperators().size() > 0) {
				sortLink.append("&filter_" + filterIndex + "_op_");
				sortLink.append(id);
				sortLink.append("=");
				sortLink.append(filterLoop.getOperator() == null ? "" : filterLoop.getOperator().getId().toString());
			}
		}

		// Prend la page en cours
		sortLink.append("&pageIndex_");
		sortLink.append(id);
		sortLink.append("=");
		sortLink.append(pageIndex == null ? "" : pageIndex.toString());

		// Ajoute le mode d'export
		try {
			sortLink.append("&modeExport_");
			sortLink.append(id);
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
	 * Result for CURRENT view and not ALL results
	 * @param results
	 */
	public DataTable<VIEW> setResults(List<VIEW> results) {
		this.results = results;

		return this;
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
	 * 
	 * @param resultCount
	 */
	public void setResultCount(Integer resultCount) {
		this.resultCount = resultCount;
	}
	
	/**
	 * Nombre de résultats avant limitation du nombre affiché. Permet de savoir
	 * si le filtre ramène plus de résultats que le nombre réellement affiché.
	 * @return
	 */
	public Integer getResultRealCount() {
		return resultRealCount;
	}
	
	/**
	 * Nombre de résultats avant limitation du nombre affiché. Permet de savoir
	 * si le filtre ramène plus de résultats que le nombre réellement affiché.
	 * @param resultRealCount
	 */
	public void setResultRealCount(Integer resultRealCount) {
		this.resultRealCount = resultRealCount;
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
	
	/**
	 * 
	 */
	public Integer getMaxResults() {
		return maxResults;
	}
}
