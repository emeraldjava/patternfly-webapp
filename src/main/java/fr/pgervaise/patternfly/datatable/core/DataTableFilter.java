package fr.pgervaise.patternfly.datatable.core;

import java.util.List;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DataTableFilter {

	public enum Type {
		STRING,
		NUMBER
	};
	
	public enum Operator {
		IS, // =
		GREATER,
		GREATER_OR_EQUAL,
		LOWER,
		LOWER_OR_EQUAL,
		CONTAINS, // like %X%
		BEGIN_WITH, // like x%
		END_WITH // like %x
	}

	protected String label = "FILTER_LABEL";
	// protected Type type = Type.STRING;
	protected Integer width = null;
	protected String id = null;
	protected Object value = null;
	protected boolean valueSet = false;
	protected Class<?> type = null;
	protected Operator operator = Operator.IS;
	private boolean newLine = false;
	private Integer size;
	private Integer colSpan = 1;
	private boolean hidden = false;

	/**
	 * Détermine si l'opérateur de recherche est virtuel. A savoir qu'il
	 * ne peut pas être utilisé en tant que critère de recherche dans une requête
	 */
	private boolean virtual;
	
	/**
	 * Détermine si le fitre ne sera pas utilisé lors de l'opération de filtrage
	 */
	private boolean skipOnFilter;

	protected List<DataTableFilterValue> acceptedValues = null;

	public DataTableFilter(String label) {
		setLabel(label);
		setId(label);
	}

	/**
	 * Libellé associé au filtre
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Libellé associé au filtre
	 * @param label
	 * @return
	 */
	public DataTableFilter setLabel(String label) {
		this.label = label;
		
		return this;
	}

	public Class<?> getType() {
		return type;
	}

	public DataTableFilter setType(Class<?> type) {
		this.type = type;

		return this;
	}

	/**
	 * Largeur du libellé du filtre dans le formulaire
	 * @return
	 */
	public Integer getWidth() {
		return width;
	}
	
	/**
	 * Largeur du libellé du filtre dans le formulaire
	 * @param width
	 * @return
	 */
	public DataTableFilter setWidth(Integer width) {
		this.width = width;
		
		return this;
	}
	
	/**
	 * L'identifiant du filtre
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * L'identifiant du filtre
	 * @param id
	 * @return
	 */
	public DataTableFilter setId(String id) {
		this.id = id;
		
		return this;
	}

	/**
	 * Récupère la valeur du filtre
	 * @return
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Récupère la valeur du filtre selon le type déclaré
	 * @return
	 */
	public Object getTypedValue() {
		if (type == null)
			return value;

		if (value == null)
			return null;

		if (type == Integer.class)
			return Integer.valueOf(value.toString());

		if (type == Long.class)
			return Long.valueOf(value.toString());

		if (type == String.class)
			return value.toString();
		
		return value;
	}

	/**
	 * Positionne la valeur du filtre
	 * @param value
	 */
	public DataTableFilter setValue(Object value) {
		this.value = value;
		this.valueSet = true;

		return this;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isValueSet() {
		return valueSet;
	}

	/**
	 * 
	 * @param valueSet
	 */
	public void setValueSet(boolean valueSet) {
		this.valueSet = valueSet;
	}

	/**
	 * Opérateur utilisé pour la recherche
	 * @return
	 */
	public Operator getOperator() {
		return operator;
	}
	
	/**
	 * Opérateur utilisé pour la recherche
	 * @param operator
	 * @return
	 */
	public DataTableFilter setOperator(Operator operator) {
		this.operator = operator;
		
		return this;
	}

	/**
	 * Récupère la liste des valeurs acceptées (type COMBOBOX)
	 * @return
	 */
	public List<DataTableFilterValue> getAcceptedValues() {
		return acceptedValues;
	}

	/**
	 * Positionne la liste des valeurs acceptées (type COMBOBOX)
	 * @param acceptedValues
	 * @return
	 */
	public DataTableFilter setAcceptedValues(List<DataTableFilterValue> acceptedValues) {
		this.acceptedValues = acceptedValues;
		
		return this;
	}

	/**
	 * Est-ce que le filtre est sur une nouvelle ligne ?
	 * @return
	 */
	public boolean isNewLine() {
		return newLine;
	}

	/**
	 * Positionne le filtre sur une nouvelle ligne lors de l'affichage du formulaire des filtres
	 * @return
	 */
	public DataTableFilter setNewLine() {
		newLine = true;
		
		return this;
	}
	
	public Integer getSize() {
		return size;
	}
	
	public DataTableFilter setSize(Integer size) {
		this.size = size;
		
		return this;
	}

	/**
	 * Précise si l'opérateur de recherche est virtuel. A savoir qu'il
	 * ne peut pas être utilisé en tant que critère de recherche dans une requête SQL
	 * @return
	 */
	public boolean isVirtual() {
		return virtual;
	}

	/**
	 * Précise si l'opérateur de recherche est virtuel. A savoir qu'il
	 * ne peut pas être utilisé en tant que critère de recherche dans une requête SQL
	 * @param virtual
	 * @return
	 */
	public DataTableFilter setVirtual(boolean virtual) {
		this.virtual = virtual;
		
		return this;
	}
	
	/**
	 * Précise si l'opérateur est pris réellement pris en compte lors d'un filtrage
	 * @return
	 */
	public boolean isSkipOnFilter() {
		return skipOnFilter;
	}
	
	/**
	 * Précise si l'opérateur est pris réellement pris en compte lors d'un filtrage
	 * @param skipOnFilter
	 */
	public DataTableFilter setSkipOnFilter(boolean skipOnFilter) {
		this.skipOnFilter = skipOnFilter;
		
		return this;
	}
	
	public Integer getColSpan() {
		return colSpan;
	}

	public DataTableFilter setColSpan(Integer colSpan) {
		this.colSpan = colSpan;
		
		return this;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public DataTableFilter setHidden(boolean hidden) {
		this.hidden = hidden;
		
		return this;
	}

	public String getParametreName() {
		return null;
	}
}
