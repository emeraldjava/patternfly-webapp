package fr.pgervaise.patternfly.datatable.core;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Philippe Gervaise
 *
 */
public class CSVExporter {

	public final String NAME = "CSV";

	private String newLine = null;
	private String fieldSeparator = null;
	private boolean quoteFields = true;
	private String quoteString = null;
	private DateFormat dateFormat = null;
	private NumberFormat numberFormat = null;
	private NumberFormat decimalFormat = null;
	private ArrayList<String> noQuoteForList = null;
	private Map<String, CSVExporterCallback> callbackMap = null;

	public CSVExporter() {
		newLine = "\r\n";
		fieldSeparator = ";";
		quoteString = "\"";
		noQuoteForList = new ArrayList<String>();
		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);
		decimalFormat = NumberFormat.getNumberInstance();
		callbackMap = new HashMap<String, CSVExporterCallback>();
	}

	public String getNewLine() {
		return newLine;
	}
	
	public CSVExporter setNewLine(String newLine) {
		this.newLine = newLine;
		
		return this;
	}
	
	public String getFieldSeparator() {
		return fieldSeparator;
	}
	
	public CSVExporter setFieldSeparator(String fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
		
		return this;
	}

	public boolean isQuoteFields() {
		return quoteFields;
	}

	public boolean isQuoted(String fieldName, Object fieldValue) {
		if (!isQuoteFields())
			return false;
		
		if (noQuoteForList.contains(fieldName))
			return false;
		
		return true;
	}

	public CSVExporter setQuoteFields(boolean quoteFields) {
		this.quoteFields = quoteFields;
		
		return this;
	}

	public String getQuoteString() {
		return quoteString;
	}

	public CSVExporter setQuoteString(String quoteString) {
		this.quoteString = quoteString;
		
		return this;
	}

	public CSVExporter setNoQuoteFor(String fieldName) {
		this.noQuoteForList.add(fieldName);
		
		return this;
	}
	
	public CSVExporterCallback getValueCallbackFor(String fieldName) {
		return callbackMap.get(fieldName);
	}
	
	/**
	 * 
	 * @param fieldName
	 * @param callback
	 */
	public CSVExporter setValueCallbackFor(String fieldName, CSVExporterCallback callback) {
		if (callback != null)
			callbackMap.put(fieldName, callback);
		
		return this;
	}

	/**
	 * 
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public String getCSVValue(String fieldName, Object value) {
		CSVExporterCallback callback = callbackMap.get(fieldName);

		if (callback != null)
			value = callback.adjustValue(fieldName, value);

		if (value instanceof Date) {
			if (dateFormat != null)
				value = dateFormat.format(value);
		} else if (value instanceof Number) {
			if (value instanceof Double || value instanceof Float || value instanceof BigDecimal)
				value = decimalFormat.format(value);
			else
				value = numberFormat.format(value);
		}

		String stringValue = value == null ? "" : value.toString();

		if (!isQuoted(fieldName, value))
			return stringValue;

		return getQuotedValue(stringValue);
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	private String getQuotedValue(Object value) {
		return quoteString + (value == null ? "" : value.toString()) + quoteString;
	}

	/**
	 * 
	 * @param dataTable
	 * @return
	 */
	public <VIEW> String doExport(DataTable<VIEW> dataTable) {
		StringBuilder csvBuffer = new StringBuilder();
		// Entêtes
		boolean first = true;

		for (DataTableColumn column : dataTable.getColumns()) {
			if (!first)
				csvBuffer.append(this.getFieldSeparator());
			csvBuffer.append(this.getQuoteString());
			csvBuffer.append(column.getLabel());
			csvBuffer.append(this.getQuoteString());
			first = false;
		}
		
		csvBuffer.append(this.getNewLine());

		for (VIEW view : dataTable.getResults()) {
			first = true;
			for (DataTableColumn column : dataTable.getColumns()) {
				if (!first)
					csvBuffer.append(this.getFieldSeparator());
				
				// Ajout de la valeur !
				String propertyName = column.getId();
				
				int pointIndex = propertyName.indexOf('.');

				if (pointIndex >= 0)
					propertyName = propertyName.substring(pointIndex + 1);

				Object result = null;

				try {
					Method method = view.getClass().getMethod("get" + Character.toUpperCase(propertyName.charAt(0))
						+ propertyName.substring(1));

					result = method.invoke(view);
				} catch (Exception e) {
					// e.printStackTrace();
					result = "#!ERROR";
				}

				csvBuffer.append(this.getCSVValue(propertyName, result));

				first = false;
			}
			csvBuffer.append(this.getNewLine());
		}

		return csvBuffer.toString();
	}

	/**
	 * 
	 * @author Philippe Gervaise
	 *
	 */
	public static class QuoteWithEgualCallback implements CSVExporterCallback {
		private CSVExporter csvExporter;

		public QuoteWithEgualCallback(CSVExporter csvExporter) {
			this.csvExporter = csvExporter;
		}

		@Override
		public Object adjustValue(String fieldName, Object value) {
			String stringValue = csvExporter.getQuotedValue(value);

			if (stringValue.length() > csvExporter.getQuoteString().length() * 2)
				return "=" + stringValue;

			return stringValue;
		}
	}
}
