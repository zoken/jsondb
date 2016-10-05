package com.fline.modules.jobmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class JobManager {
	private ExecutorService pools = Executors.newCachedThreadPool();
	private static Logger LOG = Logger.getLogger(JobManager.class);
	private static JobManager instance = new JobManager();

	/**
	 * start a FlineJobable job.
	 * 
	 * @param job
	 */
	public void startJob(final FlineJobable job) {
		Thread t = new Thread() {
			public void run() {
				try {
					StatusManager.getInstance().createJobStatus(
							job.getJobName());
					boolean executeSuccessfully = job.execute();
					// roll back.
					if (executeSuccessfully == false) {
						// try again. restart...
						StatusManager.getInstance().createJobStatus(
								job.getJobName());
						boolean rollresult = job.rollback();
						if (rollresult) {
							LOG.debug("task execute failed. roll back successfully.");
						} else {
							LOG.debug("task execute failed and roll back failed.");
						}
					} else {
						LOG.debug("task executed successfully.");
					}
					StatusManager.getInstance().finishJob(job.getJobName());
				} catch (Exception e) {
					LOG.error(e);
				}
			}
		};
		pools.execute(t);
	}

	public static JobManager getInstance() {
		return instance;
	}

}
