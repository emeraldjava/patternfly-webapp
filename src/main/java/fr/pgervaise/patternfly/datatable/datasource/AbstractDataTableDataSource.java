package fr.pgervaise.patternfly.datatable.datasource;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.pgervaise.patternfly.datatable.core.DataTable;
import fr.pgervaise.patternfly.datatable.core.DataTableColumn;
import fr.pgervaise.patternfly.datatable.core.DataTableFilter;

/**
 * 
 * @author pgervaise
 *
 */
public abstract class AbstractDataTableDataSource<VIEW> implements DataTableDataSource<VIEW> {
	
	public final static Logger logger = LoggerFactory.getLogger(AbstractDataTableDataSource.class);

	protected DataTable<VIEW> dataTable;

	/**
	 * 
	 * @param dataTable
	 */
	public AbstractDataTableDataSource(DataTable<VIEW> dataTable) {
		this.dataTable = dataTable;
	}

	/**
	 * 
	 * @param dataTable
	 * @param viewResult
	 * @param orderByColumn
	 */
	protected void sortViewResults(List<? extends Object> viewResult, DataTableColumn orderByColumn) {
		String orderById = orderByColumn.getId();
		
		if (orderById != null) {
			int pointIndex = orderById.indexOf('.');
			
			if (pointIndex >= 0)
				orderById = orderById.substring(pointIndex + 1);

			final String finalOrderById = orderById;
			Integer sensTri = dataTable.getSensTri();

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
	protected void filterViewResults(List<VIEW> viewResults, List<DataTableFilter> filters) {
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
							case GREATER : if (viewValue.compareTo(filterValue) < 0) filterOk = false; break;
							case GREATER_OR_EQUAL : if (viewValue.compareTo(filterValue) <= 0) filterOk = false; break;
							case LOWER : if (viewValue.compareTo(filterValue) >= 0) filterOk = false; break;
							case LOWER_OR_EQUAL : if (viewValue.compareTo(filterValue) > 0) filterOk = false; break;
							default :
								logger.warn("Erreur dans le filtrage virtuel ('" + filter.getLabel() + "'), opérateur non supporté => " + filter.getOperator());
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
}
