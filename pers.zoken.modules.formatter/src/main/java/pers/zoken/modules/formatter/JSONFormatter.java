package pers.zoken.modules.formatter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONFormatter {
	/**
	 * build index. key = label level, value = jsonobject.
	 * 
	 * @param indexMap
	 * @param indexhead
	 * @param obj
	 * @param indexKeys
	 */
	public static void buildJsonIndex(Map<String, JSONObject> indexMap,
			String indexhead, JSONObject obj, String[] indexKeys) {
		Set<String> indexheadset = new HashSet<String>();
		for (String indexKey : indexKeys) {
			if (obj.containsKey(indexKey)) {
				String newindexhead = indexhead + "/" + obj.getString(indexKey);
				indexMap.put(newindexhead, obj);
				indexheadset.add(newindexhead);
			}
		}
		// when no key mapped, then set indexhead to next level.
		if (indexheadset.size() <= 0) {
			indexheadset.add(indexhead);
		}

		// search jsonobject and jsonarray. deep build.
		for (Object jsonkey : obj.keySet()) {
			Object value = obj.get(jsonkey);
			if (value instanceof JSONArray) {
				JSONArray value_array = (JSONArray) value;
				for (int i = 0; i < value_array.size(); i++) {
					Object childobj = value_array.get(i);
					if (childobj instanceof JSONObject) {
						for (String newindexhead : indexheadset) {
							buildJsonIndex(indexMap, newindexhead,
									(JSONObject) childobj, indexKeys);
						}
					}
				}
			} else if (value instanceof JSONObject) {
				for (String newindexhead : indexheadset) {
					buildJsonIndex(indexMap, newindexhead, (JSONObject) value,
							indexKeys);
				}
			}
		}
	}

	/**
	 * build json k - v index. using key label array and value label.
	 * 
	 * @param obj
	 * @param keyLabels
	 * @param valueLabel
	 * @return
	 */
	public static Map<String, Object> getJsonValueMapping(JSONObject obj,
			String[] keyLabels, String valueLabel) {
		Map<String, JSONObject> jsonindex = new HashMap<String, JSONObject>();
		buildJsonIndex(jsonindex, "", obj, keyLabels);
		Map<String, Object> jsonValueMapping = new HashMap<String, Object>();
		for (Entry<String, JSONObject> jsonindexEntry : jsonindex.entrySet()) {
			if (jsonindexEntry.getValue().containsKey(valueLabel)) {
				jsonValueMapping.put(jsonindexEntry.getKey(), jsonindexEntry
						.getValue().get(valueLabel));
			}
		}
		return jsonValueMapping;
	}

	public static void main(String[] args) {

	}
}
