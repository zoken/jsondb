package pers.zoken.modules.formatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XmlFormatter {
	/**
	 * trans xml to json format
	 * 
	 * @param xmlstr
	 * @return
	 * @throws DocumentException
	 */
	public static JSONObject transXml2Json(String xmlstr)
			throws DocumentException {
		Document doc = DocumentHelper.parseText(xmlstr);
		Element ele = doc.getRootElement();
		JSONObject obj = new JSONObject();
		parseElement(ele, obj);
		return obj;
	}

	private static void parseElement(Element ele, JSONObject obj) {
		// handle attribute
		for (int i = 0; i < ele.attributeCount(); i++) {
			Attribute attr = ele.attribute(i);
			obj.put(attr.getName(), attr.getValue());
		}
		// handle text
		if (ele.getTextTrim().isEmpty() == false) {
			obj.put("_textvalue_", ele.getTextTrim());
		}
		// handle nodes - first getEleNames
		Set<String> eleNameSet = new HashSet<String>();
		for (Object childobj : ele.elements()) {
			if (childobj instanceof Element) {
				Element childele = (Element) childobj;
				eleNameSet.add(childele.getName());
			}
		}
		// every Name corresponding to a JSONArray
		for (String eleName : eleNameSet) {
			JSONArray eleArray = new JSONArray();
			for (Object childobj : ele.elements(eleName)) {
				if (childobj instanceof Element) {
					JSONObject childJsonObj = new JSONObject();
					parseElement((Element) childobj, childJsonObj);
					eleArray.add(childJsonObj);
				}
			}
			obj.put(eleName, eleArray);
		}
	}

	public static JSONObject transXml2Json(String filepath, String fileencoding)
			throws IOException, DocumentException {
		File f = new File(filepath);
		FileInputStream fis = new FileInputStream(f);
		byte[] buf = new byte[fis.available()];
		fis.read(buf);
		fis.close();
		String xmlstr = new String(buf, fileencoding);
		return transXml2Json(xmlstr);
	}

	public static void main(String[] args) throws Exception {
		JSONObject obj = transXml2Json("./testresource/testfile.xml", "utf-8");
		System.out.println(obj);
		Map<String, Object> jsonvaluemapping = JSONFormatter
				.getJsonValueMapping(obj, new String[] { "name" },
						"_textvalue_");
		for (String indexMapKey : jsonvaluemapping.keySet()) {
			System.out.println(indexMapKey + "\t"
					+ jsonvaluemapping.get(indexMapKey));
		}
	}
}
