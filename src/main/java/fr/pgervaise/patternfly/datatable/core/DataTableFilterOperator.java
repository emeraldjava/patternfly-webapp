package fr.pgervaise.patternfly.datatable.core;

import fr.pgervaise.patternfly.datatable.core.DataTableFilter.Operator;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DataTableFilterOperator {

	private Operator operator;
	private String label;

	public DataTableFilterOperator(Operator operator) {
		this.operator = operator;
		this.label = operator.getText();
	}

	public DataTableFilterOperator(Operator operator, String label) {
		this.operator = operator;
		this.label = label;
	}
	
	public Operator getOperator() {
		return operator;
	}

	public String getLabel() {
		return label;
	}
}
