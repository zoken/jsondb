package com.fline.modules.dao.jsonparser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import net.sf.json.JSONObject;

public class SQLResource {
	private JSONObject resource = null;
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public SQLResource(String resourcename) {
		resource = JsonResourceManager.getInstance().getQueryJSON(resourcename);
	}

	/**
	 * This method will be used for generating sql. with input jsonobject.
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public String getNormalSql(JSONObject obj) throws Exception {
		if (resource == null) {
			throw new Exception(
					"Bad Resource. Table Resource could not be loaded.");
		} else {
			String normalsql = resource.getString("sql");
			// two load param method.
			JSONObject params = resource.getJSONObject("params");
			// replace #@now@#
			normalsql = normalsql.replace("#@now@#", sdf.format(new Date()));
			if (params.isNullObject() == false) {
				int paramsize = params.keySet().size();
				for (int i = 0; i < paramsize; i++) {
					normalsql = normalsql.replaceFirst("\\?",
							obj.getString(params.getString(String.valueOf(i))));
				}
			} else {
				@SuppressWarnings("unchecked")
				Set<String> keys = obj.keySet();
				for (String key : keys) {
					normalsql = normalsql.replace("#" + key + "#",
							obj.getString(key));
				}
			}
			return normalsql;
		}
	}
}
