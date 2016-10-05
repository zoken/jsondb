package com.fline.modules.dao.jsonparser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public class JsonResourceManager {
	private final Map<String, JSONObject> jsonIndex = new HashMap<String, JSONObject>();
	private static Logger LOG = Logger.getLogger(JsonResourceManager.class);
	private static final String TABLE_RESOURCE = "_JSONTABLE_";
	private static final String QUERY_RESOURCE = "_JSONQUERY_";

	private static JsonResourceManager instance = new JsonResourceManager();

	private JSONObject loadJsonObject(String resourcename) {
		JSONObject obj = jsonIndex.get(resourcename);
		if (obj != null) {
			LOG.warn("repeat load json resource : " + resourcename);
			return obj;
		}
		try {
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream(resourcename);
			if (is != null) {
				byte[] buf = new byte[is.available()];
				is.read(buf);
				is.close();
				JSONObject resource = JSONObject.fromObject(new String(buf)
						.replaceAll("\\r\\n", " ").replaceAll("\\n", " ")
						.replaceAll("\\t", ""));
				return resource;

			} else {
				throw new Exception("resource does not exists.");
			}
		} catch (Exception e) {
			LOG.error("load json resource failed.", e);
			return null;
		}
	}

	private String getResourcePath(String resourcetype, String resourcename) {
		return resourcetype + resourcename;
	}

	public JSONObject getJSON(String resourcetype, String resourcename) {
		String resourcepath = getResourcePath(resourcetype, resourcename);
		JSONObject obj = jsonIndex.get(resourcepath);
		if (obj == null) {
			String[] splits = resourcename.split("/");
			String namespace = splits[0];
			JSONObject objs = loadJsonObject(namespace);
			@SuppressWarnings("unchecked")
			Set<String> resourcekeys = objs.keySet();
			for (String resourcekey : resourcekeys) {
				jsonIndex
						.put(getResourcePath(resourcetype, namespace + "/"
								+ resourcekey), objs.getJSONObject(resourcekey));
			}
			obj = jsonIndex.get(resourcepath);
		}
		return obj;
	}

	public JSONObject getQueryJSON(String resourcename) {
		return getJSON(QUERY_RESOURCE, resourcename);
	}

	public JSONObject getTableJSON(String resourcename) {
		return getJSON(TABLE_RESOURCE, resourcename);
		// String resourcepath = getResourcePath(TABLE_RESOURCE, resourcename);
		// JSONObject obj = jsonIndex.get(resourcepath);
		// if (obj == null) {
		// // load and reget.
		// obj = loadJsonObject(resourcename);
		// jsonIndex.put(resourcepath, obj);
		// }
		// return obj;
	}

	public static JsonResourceManager getInstance() {
		return instance;
	}
}
