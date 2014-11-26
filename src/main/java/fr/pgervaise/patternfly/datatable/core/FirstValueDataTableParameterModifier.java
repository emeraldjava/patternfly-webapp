package fr.pgervaise.patternfly.datatable.core;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class FirstValueDataTableParameterModifier<VIEW> implements DataTableParameterModifier {

	private DataTable<VIEW> dataTable;
	private String defaultValue;

	public FirstValueDataTableParameterModifier(DataTable<VIEW> dataTable, String defaultValue) {
		this.dataTable = dataTable;
		this.defaultValue = defaultValue;
	}

	public String modify(String value) {
		if (dataTable.isFirstView())
			return defaultValue;

		return value;
	}
}
