package com.fline.modules.jobmanager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.fline.modules.dao.executor.MySQL_Instance;
import com.fline.modules.dao.jsonparser.QueryResource;
import com.fline.modules.jobmanager.objects.StatusEnum;

public class StatusManager {
	// DB - STATUS TABLE - COLUMNS
	public static final String JOB_NAME = "jobname";
	public static final String STATUS_NAME = "statusname";
	public static final String STATUS_DESCRIPTION = "statusdescription";
	public static final String PROGRESS = "progress";
	public static final String CREATED_TIME = "createdtime";

	// RESOURCES
	private static final String ADD_STATUS_RESOURCE = "JobManager.json/addStatus";
	private static final String QUERY_LATEST_STATUS_RESOURCE = "JobManager.json/queryLatestStatusByJobName";

	private static final DateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// Mem store.
	Map<String, JSONObject> job2status = new HashMap<String, JSONObject>();

	private static Logger LOG = Logger.getLogger(StatusManager.class);
	private static StatusManager instance = new StatusManager();

	public static StatusManager getInstance() {
		return instance;
	}

	/**
	 * create job status. init created time, updated time,status and so on.
	 * 
	 * @param jobname
	 * @throws Exception
	 */
	public void createJobStatus(String jobname) throws Exception {
		JSONObject statusobj = new JSONObject();
		statusobj.put(JOB_NAME, jobname);
		statusobj.put(STATUS_NAME, StatusEnum.Job_Initing.getDescription());
		statusobj.put(STATUS_DESCRIPTION,
				StatusEnum.Job_Initing.getDescription());
		statusobj.put(PROGRESS, StatusEnum.Job_Initing.getProgress());
		statusobj.put(CREATED_TIME, sdf.format(new Date()));
		storeStatus(jobname, statusobj);
	}

	/**
	 * query status from db by jobname.
	 * 
	 * @param jobname
	 * @return
	 * @throws Exception
	 */
	public JSONObject getJobStatus(String jobname) throws Exception {
		JSONObject result = null;
		if (useMemStore) {
			result = job2status.get(jobname);
		}
		if (result == null && useDBStore) {
			try {
				QueryResource queryLatestStatusResource = new QueryResource(
						QUERY_LATEST_STATUS_RESOURCE);
				JSONObject queryparam = new JSONObject();
				queryparam.put(JOB_NAME, jobname);
				String sql = queryLatestStatusResource.getQuerySql(queryparam);
				JSONArray results = MySQL_Instance.getInstance().executeQuery(
						sql);
				if (results.size() > 0) {
					return results.getJSONObject(0);
				} else {
					return new JSONObject();
				}
			} catch (Exception e) {
				LOG.error(e);
				return null;
			}
		} else if (result != null) {
			return result;
		} else {
			return new JSONObject();
		}
	}

	/**
	 * update progress by jobname.
	 * 
	 * @param jobname
	 * @param progress
	 * 
	 */
	public boolean updateProgress(String jobname, double progress,
			String statusname, String status_description) {
		if (progress > 1.0 || progress <= 0) {
			String error_message = "input param invalid. progress must <=1.0 && >0";
			LOG.error(error_message);
			return false;
		}
		JSONObject statusobj = new JSONObject();
		statusobj.put(JOB_NAME, jobname);
		statusobj.put(STATUS_NAME, statusname);
		statusobj.put(STATUS_DESCRIPTION, status_description);
		statusobj.put(PROGRESS, progress);
		statusobj.put(CREATED_TIME, sdf.format(new Date()));
		return storeStatus(jobname, statusobj);
	}

	/**
	 * finish job.
	 * 
	 * @param jobname
	 * @throws Exception
	 */
	public boolean finishJob(String jobname) {
		JSONObject statusobj = new JSONObject();
		statusobj.put(JOB_NAME, jobname);
		statusobj.put(STATUS_NAME, StatusEnum.Job_Finished.getDescription());
		statusobj.put(STATUS_DESCRIPTION,
				StatusEnum.Job_Finished.getDescription());
		statusobj.put(PROGRESS, StatusEnum.Job_Finished.getProgress());
		statusobj.put(CREATED_TIME, sdf.format(new Date()));
		return storeStatus(jobname, statusobj);
	}

	private boolean storeStatus(String jobname, JSONObject statusobj) {
		if (useMemStore) {
			job2status.put(jobname, statusobj);
		}
		if (useDBStore) {
			try {
				QueryResource addResource = new QueryResource(
						ADD_STATUS_RESOURCE);
				String sql = addResource.getQuerySql(statusobj);
				String id = MySQL_Instance.getInstance().executeInsert(sql);
				LOG.debug("insert jobstatus. new record . id = " + id);
			} catch (Exception e) {
				LOG.error(e);
				return false;
			}
		}
		return true;
	}

	private boolean useMemStore = true;
	private boolean useDBStore = false;

	public void setUseMemStore(boolean useMemStore) {
		this.useMemStore = useMemStore;
	}

	public boolean isUseMemStore() {
		return useMemStore;
	}

	public boolean isUseDBStore() {
		return useDBStore;
	}

	public void setUseDBStore(boolean useDBStore) {
		this.useDBStore = useDBStore;
	}
}
