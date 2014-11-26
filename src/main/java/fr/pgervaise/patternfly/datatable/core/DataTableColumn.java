package fr.pgervaise.patternfly.datatable.core;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class DataTableColumn {

	public enum Order {
		ASCENDANT,
		DESCENDANT
	};

	private Integer width = null;
	private String label = "";
	private String headerComment;
	// private Order order = null;
	private String id;
	private boolean rawView = false;
	private boolean sortable = true;
	private boolean hidden = false;
	private boolean html = false;
	private String format = null;
	private boolean virtualSort = false;

	public DataTableColumn() {
	}
	
	public DataTableColumn(String id, String label) {
		setId(id);
		setLabel(label);
	}

	public Integer getWidth() {
		return width;
	}
	
	public DataTableColumn setWidth(Integer width) {
		this.width = width;
		
		return this;
	}
	
	public String getLabel() {
		return label;
	}

	public DataTableColumn setLabel(String label) {
		this.label = label;
		
		return this;
	}
/*
	public Order getOrder() {
		return order;
	}

	public DataTableColumn setOrder(Order order) {
		this.order = order;

		return this;
	} */
	
	public String getId() {
		return id;
	}

	public DataTableColumn setId(String id) {
		this.id = id;
		
		return this;
	}

	public boolean isRawView() {
		return rawView;
	}

	public DataTableColumn setRawView(boolean rawView) {
		this.rawView = rawView;
		
		return this;
	}
	
	public boolean isSortable() {
		return sortable;
	}

	public DataTableColumn setSortable(boolean sortable) {
		this.sortable = sortable;

		return this;
	}
	
	/**
	 * Tri virtuel. Le tri est effectué APRES le tri effectué la datasource
	 * @return
	 */
	public boolean isVirtualSort() {
		return virtualSort;
	}

	/**
	 * Tri virtuel. Le tri est effectué APRES la récupération via la datasource
	 * @param virtualSort
	 * @return
	 */
	public DataTableColumn setVirtualSort(boolean virtualSort) {
		this.virtualSort = virtualSort;

		return this;
	}

	public boolean isHidden() {
		return hidden;
	}
	
	public DataTableColumn setHidden(boolean hidden) {
		this.hidden = hidden;

		return this;
	}
	
	public boolean isHtml() {
		return html;
	}
	
	public DataTableColumn setHtml(boolean html) {
		this.html = html;

		return this;
	}
	
	public String getFormat() {
		return format;
	}
	
	public DataTableColumn setFormat(String format) {
		this.format = format;
		
		return this;
	}

	/**
	 * @return 
	 */
	public String getHeaderComment() {
		return headerComment;
	}

	/**
	 * @param headerComment 
	 */
	public DataTableColumn setHeaderComment(String headerComment) {
		this.headerComment = headerComment;
		
		return this;
	}	
}
