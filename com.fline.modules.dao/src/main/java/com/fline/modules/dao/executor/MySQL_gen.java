package com.fline.modules.dao.executor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fline.modules.dao.annotation.DBColumn;
import com.fline.modules.dao.annotation.DBTable;

public class MySQL_gen {
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * generate SQL String from object with annotation {DBColumn and DBTable}
	 * 
	 * @param obj
	 * @return
	 * @throws SQLException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static String generateInsertSQL(Object obj) throws SQLException,
			NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<?> objclass = obj.getClass();
		Field[] fields = objclass.getDeclaredFields();
		DBTable tableanno = objclass.getAnnotation(DBTable.class);
		String sql = "insert into "
				+ (tableanno == null ? objclass.getSimpleName() : tableanno
						.realtablename());
		StringBuilder keyBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();
		keyBuilder.append('(');
		valueBuilder.append('(');
		for (Field field : fields) {
			DBColumn columnanno = field.getAnnotation(DBColumn.class);
			boolean onlyquery = false;
			if (columnanno != null) {
				onlyquery = columnanno.onlyquery();
			}
			if (onlyquery) {
				continue;
			}
			boolean notempty = columnanno == null || columnanno.notempty();
			String getMethodName = "get"
					+ String.valueOf(field.getName().charAt(0)).toUpperCase()
					+ field.getName().substring(1);
			Method getMethod = objclass.getMethod(getMethodName);
			Object value = getMethod.invoke(obj);
			value = value == null ? (columnanno != null ? (columnanno
					.defaultvalue().length() > 0 ? columnanno.defaultvalue()
					: value) : value) : value;
			if (notempty && value == null) {
				throw new IllegalArgumentException(field.getName()
						+ " could not be empty.");
			} else {
				if (value != null) {
					valueBuilder.append('\'');
					// use default value
					if (value instanceof Date) {
						// format
						valueBuilder.append(sdf.format(value));
					} else {
						valueBuilder.append(value.toString());
					}
					valueBuilder.append('\'');
					valueBuilder.append(',');
					String fieldname = (columnanno == null || columnanno
							.realcolumnname().length() <= 0) ? field.getName()
							: columnanno.realcolumnname();
					keyBuilder.append(fieldname);
					keyBuilder.append(',');
				} else {
					// do nothing.
				}
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
