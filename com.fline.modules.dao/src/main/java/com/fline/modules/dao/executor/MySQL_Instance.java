package com.fline.modules.dao.executor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.fline.modules.dao.annotation.DBColumn;

/**
 * instance will read config from resourcepath named as 'mysql.properties'
 * 
 * @author Fline_FDP
 * 
 */
public class MySQL_Instance {
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static MySQL_Instance instance = new MySQL_Instance();
	private static Logger LOG = Logger.getLogger(MySQL_Instance.class);
	private Properties properties = new Properties();
	private Connection con = null;
	private String url = null;
	private String username = null;
	private String passwd = null;
	private long lastUseTime = 0;
	private long config_timegap = 0;
	private String autoReconnectTime;

	public static MySQL_Instance getInstance() {
		return instance;
	}

	private void loadPrivateMysqlProperties(String configpath)
			throws IOException {
		InputStream is = MySQL_Instance.class.getClassLoader()
				.getResourceAsStream(configpath);
		if (is != null) {
			properties.load(is);
			url = properties.getProperty("url");
			username = properties.getProperty("username");
			passwd = properties.getProperty("passwd");
			autoReconnectTime = properties.getProperty("autoReconnectTime");
			is.close();
		}
	}

	private void loadIbatsMysqlProperties() throws IOException {
		InputStream is = MySQL_Instance.class
				.getClassLoader()
				.getResourceAsStream(
						"applicationContext-tp-datasource-dbcp.extensionpoint.properties");
		if (is != null) {
			properties.load(is);
			url = properties.getProperty("jdbc.url");
			username = properties.getProperty("jdbc.username");
			passwd = properties.getProperty("jdbc.password");
			is.close();
		}
	}

	public MySQL_Instance() {
		try {
			loadIbatsMysqlProperties();
			loadPrivateMysqlProperties("mysql.properties");
			if (autoReconnectTime == null) {
				config_timegap = Long.MAX_VALUE;
			} else {
				config_timegap = Long.valueOf(autoReconnectTime);
			}
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, username, passwd);
			lastUseTime = System.currentTimeMillis();
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public MySQL_Instance(String configpath) {
		try {
			loadPrivateMysqlProperties(configpath);
			if (autoReconnectTime == null) {
				config_timegap = Long.MAX_VALUE;
			} else {
				config_timegap = Long.valueOf(autoReconnectTime);
			}
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, username, passwd);
			lastUseTime = System.currentTimeMillis();
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public String writeObj(Object obj) {
		try {
			String sql = MySQL_gen.generateInsertSQL(obj);
			return executeInsert(sql);
		} catch (Exception e) {
			LOG.error(e);
			return null;
		}
	}

	public int executeUpdate(String sql) {
		try {
			if (System.currentTimeMillis() - lastUseTime > config_timegap) {
				// reopen
				con = DriverManager.getConnection(url, username, passwd);
				lastUseTime = System.currentTimeMillis();
			}
			Statement stat = con.createStatement();
			int ret = stat.executeUpdate(sql);
			LOG.debug("successnum :" + ret);
			return ret;
		} catch (Exception e) {
			LOG.error(e);
			return 0;
		}
	}

	public void executeInserts(List<String> sqls) {
		try {
			if (System.currentTimeMillis() - lastUseTime > config_timegap) {
				// reopen
				con = DriverManager.getConnection(url, username, passwd);
				lastUseTime = System.currentTimeMillis();
			}
			Statement stat = con.createStatement();
			for (String sql : sqls) {
				stat.addBatch(sql);
			}
			stat.executeBatch();
			stat.close();
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public String executeInsert(String sql) {
		try {
			if (System.currentTimeMillis() - lastUseTime > config_timegap) {
				// reopen
				con = DriverManager.getConnection(url, username, passwd);
				lastUseTime = System.currentTimeMillis();
			}
			Statement stat = con.createStatement();
			int ret = stat.executeUpdate(sql);
			ResultSet rs = stat.executeQuery("select @@IDENTITY");
			String result = "";
			if (rs.next())
				result = rs.getString("@@IDENTITY");
			rs.close();
			stat.close();
			LOG.debug("successnum :" + ret);
			return result;
		} catch (Exception e) {
			LOG.error(e);
			return null;
		}
	}

	public int executeDelete(String sql) {
		try {
			if (System.currentTimeMillis() - lastUseTime > config_timegap) {
				// reopen
				con = DriverManager.getConnection(url, username, passwd);
				lastUseTime = System.currentTimeMillis();
			}
			Statement stat = con.createStatement();
			int ret = stat.executeUpdate(sql);
			stat.close();
			return ret;
		} catch (Exception e) {
			LOG.error(e);
			return -1;
		}
	}

	/**
	 * query data, and set data to user's object.
	 * 
	 * @param sql
	 * @param result
	 */
	public void executeQuery(String sql, Object result) {
		try {
			if (System.currentTimeMillis() - lastUseTime > config_timegap) {
				// reopen
				con = DriverManager.getConnection(url, username, passwd);
				lastUseTime = System.currentTimeMillis();
			}
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			if (rs.next() == false) {
				return;
			}
			ResultSetMetaData metadata = rs.getMetaData();
			Set<String> columns = new HashSet<String>();
			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				columns.add(metadata.getColumnName(i));
			}
			Field[] fields = result.getClass().getDeclaredFields();
			for (Field field : fields) {
				DBColumn annotation = field.getAnnotation(DBColumn.class);
				String labelstr = annotation == null ? field.getName()
						: annotation.realcolumnname().length() <= 0 ? field
								.getName() : annotation.realcolumnname();
				if (columns.contains(labelstr)) {
					Object value = null;
					if (field.getType().equals(Integer.class)) {
						value = rs.getInt(labelstr);
					} else {
						value = rs.getObject(labelstr);
					}

					String setmethodname = "set"
							+ String.valueOf(field.getName().charAt(0))
									.toUpperCase()
							+ field.getName().substring(1);
					Method setmethod = result.getClass().getMethod(
							setmethodname, field.getType());
					setmethod.invoke(result, value);
				}
			}
			stat.close();
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public JSONArray executeQuery(String sql) {
		JSONArray json_result = new JSONArray();
		try {
			if (System.currentTimeMillis() - lastUseTime > config_timegap) {
				// reopen
				con = DriverManager.getConnection(url, username, passwd);
				lastUseTime = System.currentTimeMillis();
			}
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			Set<String> columns = new HashSet<String>();
			while (rs.next()) {
				ResultSetMetaData metadata = rs.getMetaData();
				if (columns.isEmpty()) {
					for (int i = 1; i <= metadata.getColumnCount(); i++) {
						columns.add(metadata.getColumnLabel(i));
					}
				}
				JSONObject singleresult = new JSONObject();
				for (String column : columns) {
					Object value = rs.getObject(column);
					if (value == null) {
						continue;
					} else {
						if (value instanceof Timestamp) {
							singleresult.put(column, sdf.format(value));
						} else {
							singleresult.put(column, value);
						}
					}
				}
				json_result.add(singleresult);
			}
			stat.close();
		} catch (Exception e) {
			LOG.error(e);
		}
		return json_result;
	}

	/**
	 * query data list, and set data to user's object.
	 * 
	 * @param sql
	 * @param result
	 */
	public <T> List<T> executeQueryList(String sql, T result) {
		List<T> results = new ArrayList<T>();
		try {
			if (System.currentTimeMillis() - lastUseTime > config_timegap) {
				// reopen
				con = DriverManager.getConnection(url, username, passwd);
				lastUseTime = System.currentTimeMillis();
			}
			Field[] fields = result.getClass().getDeclaredFields();
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				ResultSetMetaData metadata = rs.getMetaData();
				Set<String> columns = new HashSet<String>();
				for (int i = 1; i <= metadata.getColumnCount(); i++) {
					columns.add(metadata.getColumnName(i));
				}
				@SuppressWarnings("unchecked")
				T instance = (T) result.getClass().newInstance();
				for (Field field : fields) {
					DBColumn annotation = field.getAnnotation(DBColumn.class);
					String labelstr = annotation == null ? field.getName()
							: annotation.realcolumnname().length() <= 0 ? field
									.getName() : annotation.realcolumnname();
					if (columns.contains(labelstr)) {
						Object value = null;
						if (field.getType().equals(Integer.class)) {
							value = rs.getInt(labelstr);
						} else {
							value = rs.getObject(labelstr);
						}

						String setmethodname = "set"
								+ String.valueOf(field.getName().charAt(0))
										.toUpperCase()
								+ field.getName().substring(1);
						Method setmethod = result.getClass().getMethod(
								setmethodname, field.getType());
						setmethod.invoke(instance, value);
					}
				}
				results.add(instance);
			}
			stat.close();
		} catch (Exception e) {
			LOG.error(e);
		}
		return results;
	}

	public void close() throws SQLException {
		if (con != null) {
			con.close();
		}
	}
}
