package fr.pgervaise.patternfly.datatable.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;

import fr.pgervaise.patternfly.datatable.service.ValeurReferenceService;
import fr.pgervaise.patternfly.datatable.view.AbstractView;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DefaultDataTableResultMapper<VIEW extends AbstractView<?>> implements DataTableResultMapper<VIEW> {

	private Map<String, Object> parameters = new HashMap<String, Object>();

	/**
	 * 
	 * @param mappedClass
	 */
	public DefaultDataTableResultMapper() {
	}

	/**
	 * 
	 * @param clazz
	 * @param valeurReferenceService
	 * @param model
	 */
	public DefaultDataTableResultMapper(ValeurReferenceService valeurReferenceService, Model model) {
		parameters.put("valeurReferenceService", valeurReferenceService);
		parameters.put("model", model);
	}

	@Override
	public List<VIEW> map(DataTable<VIEW> dataTable, List<? extends Object> results) {
		Class<?> viewClass = dataTable.getViewClass();

		List<VIEW> list = new ArrayList<VIEW>();

		int colCount = 1;
		Object[] resultRow = null;
		Constructor<?> constructor = null;

		if (results.size() > 0) {
			if (results.get(0) instanceof Object[]) {
				colCount = ((Object[]) results.get(0)).length;
			}

			resultRow = new Object[colCount];

			// Choix du bon constructeur
			if (constructor == null) {
				Constructor<?>[] constructorArray = viewClass.getConstructors();

				if (constructorArray.length > 1)
					throw new IllegalArgumentException("La classe " + viewClass.getSimpleName() + " possède plusieurs contructeurs");

				for (Constructor<?> c : viewClass.getConstructors()) {
					constructor = c;
					break;
				}
			}
		}

		for (Object result : results) {
			if (colCount == 1) {
				resultRow[0] = result;
			} else {
				resultRow = (Object[]) result;
			}

			Class<?>[] argTypes = constructor.getParameterTypes();
			Object[] args = new Object[argTypes.length];
			
			int index = 0;

			for (index = 0; index < resultRow.length; index++)
				args[index] = resultRow[index];

			for (; index < args.length; index++) {
				if (argTypes[index].isAssignableFrom(ValeurReferenceService.class))
					args[index] = getValeurReferenceService();
				if (argTypes[index].isAssignableFrom(Model.class))
					args[index] = getModel();
			}

			VIEW view = null;

			try {
				view = (VIEW) constructor.newInstance((Object[]) args);
				view.setMapper(this);
				view.init();
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}

			list.add(view);
		}

		return list;
	}

	/**
	 * 
	 * @return
	 */
	public ValeurReferenceService getValeurReferenceService() {
		return (ValeurReferenceService) parameters.get("valeurReferenceService");
	}

	/**
	 * 
	 * @return
	 */
	public Model getModel() {
		return (Model) parameters.get("model");
	}

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}
}