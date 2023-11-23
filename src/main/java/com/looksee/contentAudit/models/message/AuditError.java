package com.looksee.contentAudit.models.message;

import com.looksee.contentAudit.models.enums.AuditCategory;

public class AuditError extends Message {
	private String error_message;
	private AuditCategory audit_category;
	private double progress;
	private long audit_record_id;
	
	public AuditError(long accountId, 
					  long auditRecordId, 
					  String error_message,
					  AuditCategory category, 
					  double progress
	) {
		super(accountId);
		setAuditRecordId(auditRecordId);
		setErrorMessage(error_message);
		setAuditCategory(category);
		setProgress(progress);
	}

	public String getErrorMessage() {
		return error_message;
	}

	public void setErrorMessage(String error_message) {
		this.error_message = error_message;
	}

	public AuditCategory getAuditCategory() {
		return audit_category;
	}

	public void setAuditCategory(AuditCategory audit_category) {
		this.audit_category = audit_category;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public long getAuditRecordId() {
		return audit_record_id;
	}

	public void setAuditRecordId(long audit_record_id) {
		this.audit_record_id = audit_record_id;
	}

}
