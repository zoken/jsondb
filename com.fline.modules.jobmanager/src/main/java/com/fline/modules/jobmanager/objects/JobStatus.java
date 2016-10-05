package com.fline.modules.jobmanager.objects;

import java.util.Date;

import com.fline.modules.dao.annotation.DBTable;

@DBTable(realtablename = "c_project_jobstatus")
public class JobStatus {
	private String jobname;
	private int jobstatus_id;
	private String jobstatus_description;
	private Date createdTime;
	private Date updateTime;
	private double progress;

	public String getJobname() {
		return jobname;
	}

	public void setJobname(String jobname) {
		this.jobname = jobname;
	}

	public String getJobstatus_description() {
		return jobstatus_description;
	}

	public void setJobstatus_description(String jobstatus_description) {
		this.jobstatus_description = jobstatus_description;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public int getJobstatus_id() {
		return jobstatus_id;
	}

	public void setJobstatus_id(int jobstatus_id) {
		this.jobstatus_id = jobstatus_id;
	}

	public void setStatusEnum(StatusEnum status) {
		this.jobstatus_id = status.getId();
		this.progress = status.getProgress();
		this.jobstatus_description = status.getDescription();
	}
}
