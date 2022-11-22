package com.looksee.audit.contentAudit.models;

import com.looksee.audit.contentAudit.models.enums.AuditLevel;
import com.looksee.audit.contentAudit.models.enums.ExecutionStatus;

/**
 * Record detailing an set of {@link Audit audits}.
 */
public class PageAuditRecord extends AuditRecord {

	private long elements_found;
	private long elements_reviewed;
	
	public PageAuditRecord() {
		setKey(generateKey());
	}
	
	/**
	 * Constructor
	 * @param audits TODO
	 * @param page_state TODO
	 * @param is_part_of_domain_audit TODO
	 * @param audit_stats {@link AuditStats} object with statics for audit progress
	 * @pre audits != null
	 * @pre page_state != null
	 * @pre status != null;
	 */
	public PageAuditRecord(
			ExecutionStatus status, 
			boolean is_part_of_domain_audit
	) {
		assert status != null;
		
		setStatus(status);
		setLevel( AuditLevel.PAGE);
		setKey(generateKey());
	}

	public String generateKey() {
		return "pageauditrecord:"+org.apache.commons.codec.digest.DigestUtils.sha256Hex( System.currentTimeMillis() + " " );
	}

	public long getElementsFound() {
		return elements_found;
	}

	public void setElementsFound(long elements_found) {
		this.elements_found = elements_found;
	}

	public long getElementsReviewed() {
		return elements_reviewed;
	}

	public void setElementsReviewed(long elements_reviewed) {
		this.elements_reviewed = elements_reviewed;
	}
}
