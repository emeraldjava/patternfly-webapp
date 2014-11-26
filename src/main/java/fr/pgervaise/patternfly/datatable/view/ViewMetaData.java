package fr.pgervaise.patternfly.datatable.view;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class ViewMetaData {

	private String styleClass;

	public ViewMetaData() {
	}

	/**
	 * La classe du style à appliquer à toute une ligne
	 * @return
	 */
	public String getStyleClass() {
		return styleClass;
	}

	/**
	 * La classe du style à appliquer à toute une ligne
	 * @param styleClass
	 */
	public ViewMetaData setStyleClass(String styleClass) {
		this.styleClass = styleClass;
		
		return this;
	}
}
