package com.fline.modules.jobmanager;

import com.fline.modules.jobmanager.objects.JobStatus;

public interface FlineJobable {
	public boolean re_execute(JobStatus status);

	public boolean execute();

	public boolean rollback();

	// Job Name must unique in system.
	public String getJobName();
}