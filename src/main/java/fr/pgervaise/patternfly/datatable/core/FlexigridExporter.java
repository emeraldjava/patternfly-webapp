package fr.pgervaise.patternfly.datatable.core;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.pgervaise.patternfly.datatable.view.AbstractView;
import fr.pgervaise.patternfly.datatable.view.ViewMetaData;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class FlexigridExporter {

	private DateFormat dateFormat = null;

	public FlexigridExporter() {
		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}
	
	public DateFormat getDateFormat() {
		return dateFormat;
	}
	
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Object getViewValue(Object view, String propertyName) {

		try {
			Method method = view.getClass().getMethod("get" + Character.toUpperCase(propertyName.charAt(0))
				+ propertyName.substring(1));

			return method.invoke(view);
		} catch (Exception e) {
			// todo
			return null;
		}
	}

	public <VIEW> String doExport(DataTable<VIEW> dataTable) {
		JSONArray rows = new JSONArray();

		for (VIEW view : dataTable.getResults()) {
			JSONObject cell = new JSONObject();
			
			for (DataTableColumn column : dataTable.getColumns()) {
				
				String propertyName = column.getId();
				
				int pointIndex = propertyName.indexOf('.');

				if (pointIndex >= 0)
					propertyName = propertyName.substring(pointIndex + 1);

				Object value = getViewValue(view, propertyName);

				if (value == null)
					value = "";

				if (value instanceof Date && column.getFormat() != null) {
					value = new SimpleDateFormat(column.getFormat()).format(value);
				}

				if (value instanceof Date) {

					if (dateFormat != null)
						value = dateFormat.format(value);
				} /* else if (value instanceof Number) {
					if (value instanceof Double || value instanceof Float || value instanceof BigDecimal)
						value = decimalFormat.format(value);
					else
						value = numberFormat.format(value);
				} */

				if (value instanceof String && !column.isHtml()) {
					value = value.toString().replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
				}

				cell.put(column.getId(), value);
			}

			JSONObject row = new JSONObject();

			Object idValue = getViewValue(view, "code");

			if (idValue == null)
				idValue = getViewValue(view, "id");

			if (idValue != null)
				row.put("id", idValue);

			if (dataTable.getViewLink() != null) {
				if (Boolean.FALSE.equals(getViewValue(view, "canBeViewed")))
					cell.put("flex_view_link", false);
			}

			if (dataTable.getModifyLink() != null) {
				if (Boolean.FALSE.equals(getViewValue(view, "canBeModified")))
					cell.put("flex_modify_link", false);
			}

			if (dataTable.getDeleteLink() != null) {
				if (Boolean.FALSE.equals(getViewValue(view, "canBeDeleted")))
					cell.put("flex_delete_link", false);
			}

			if (dataTable.getChangeStatusLink() != null) {
				if (Boolean.FALSE.equals(getViewValue(view, "canChangeStatus"))) {
					cell.put("flex_change_status_link", false);
				} else {
					Object codeStatusValue = getViewValue(view, "rawCodeStatus");

					if (codeStatusValue != null)
						cell.put("flex_change_status_link", codeStatusValue);
				}
			}

			// Recherche meta données
			if (view instanceof AbstractView && ((AbstractView) view).getViewMetaData() != null) {
				ViewMetaData viewMetaData = ((AbstractView) view).getViewMetaData();

				JSONObject rowMetaData = new JSONObject();

				if (viewMetaData.getStyleClass() != null)
					rowMetaData.put("styleClass", viewMetaData.getStyleClass());

				row.put("rowMetaData", rowMetaData);
			}

			row.put("cell", cell);
			rows.add(row);
		}

		JSONObject obj = new JSONObject();
		obj.put("page", dataTable.getPageIndex());
		obj.put("total", dataTable.getResultCount());
		obj.put("rows", rows);

		
		// return new String(obj.toString().getBytes(), Charset.forName("UTF-8"));

		try {
			return new String(obj.toString().getBytes("UTF-8"), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
