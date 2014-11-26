package fr.pgervaise.patternfly.datatable.core;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DataTableFilterValue {

	private String value;
	private String label;

	public DataTableFilterValue(String valueAndLabel) {
		this.value = valueAndLabel;
		this.label = valueAndLabel;
	}

	public DataTableFilterValue(String value, String label) {
		this.value = value;
		this.label = label;
	}
	
	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}
}
