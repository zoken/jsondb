package com.fline.modules.dao.jsonparser;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

/**
 * define table for old version. This resource used for insert SQL.
 * 
 * @author Fline_FDP
 * 
 */
public class TableResource {
	public static final String DB_TABLE_DESCRIPTION = "resourcename";
	public static final String DB_FIELDS = "fields";
	public static final String DB_TABLE_NAME = "tablename";
	public static Map<String, JSONObject> DB_TABLES = new HashMap<String, JSONObject>();

	private JSONObject resource = null;

	public TableResource(String resourcename) {
		resource = JsonResourceManager.getInstance().getTableJSON(resourcename);
	}

	public String getInsertSql(JSONObject obj) throws Exception {
		if (resource == null) {
			throw new Exception(
					"Bad Resource. Table Resource could not be loaded.");
		} else {
			String fields = resource.getString(DB_FIELDS);
			String[] keys = fields.split(",");
			String sql = "insert into " + resource.getString(DB_TABLE_NAME);
			StringBuilder keyBuilder = new StringBuilder();
			StringBuilder valueBuilder = new StringBuilder();
			keyBuilder.append('(');
			valueBuilder.append('(');
			for (String key : keys) {
				Object value = obj.get(key);
				if (value == null) {
					continue;
				} else {
					keyBuilder.append(key);
					keyBuilder.append(',');
					valueBuilder.append('\'');
					valueBuilder.append(value);
					valueBuilder.append("\',");
				}
			}
			keyBuilder.setCharAt(keyBuilder.length() - 1, ')');
			valueBuilder.setCharAt(valueBuilder.length() - 1, ')');
			sql = sql + keyBuilder.toString()// delete last
												// ','
					+ " values " + valueBuilder.toString();
			return sql;
		}
	}
}
