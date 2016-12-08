package cn.rfidcn.scheduledjob.model;

import java.util.Date;

public class StormStats {

	String job;
	long lastOkTransactionTime;
	int reAttemptCounts;
	Date lastOkTransactionTimestamp;
	
	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}
	
	public long getLastOkTransactionTime() {
		return lastOkTransactionTime;
	}
	public void setLastOkTransactionTime(long lastOkTransactionTime) {
		this.lastOkTransactionTime = lastOkTransactionTime;
	}
	public int getReAttemptCounts() {
		return reAttemptCounts;
	}
	public void setReAttemptCounts(int reAttemptCounts) {
		this.reAttemptCounts = reAttemptCounts;
	}
	public Date getLastOkTransactionTimestamp() {
		return lastOkTransactionTimestamp;
	}
	public void setLastOkTransactionTimestamp(Date lastOkTransactionTimestamp) {
		this.lastOkTransactionTimestamp = lastOkTransactionTimestamp;
	}
	@Override
	public String toString() {
		return "StormStats [job=" + job + ", reAttemptCounts="
				+ reAttemptCounts + ", lastOkTransactionTimestamp="
				+ lastOkTransactionTimestamp + "]";
	}
	
	
	
	
	
}
