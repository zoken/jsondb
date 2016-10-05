package com.fline.modules.jobmanager.objects;

public enum StatusEnum {
	Job_Initing(10, 0.05, "Job Initing"), Job_Executing(20, 0.06,
			"Job Executing"), Job_Failed(30, 1.0, "Job Failed."), Job_Success(
			40, 1.0, "Job Success."),Job_Finished(50, 1.0, "Job Finished");
	private int id;
	private double progress;
	private String description;

	private StatusEnum(int id, double progress, String description) {
		this.id = id;
		this.progress = progress;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}
}
