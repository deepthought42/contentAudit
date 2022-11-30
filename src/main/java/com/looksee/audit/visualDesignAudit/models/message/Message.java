package com.looksee.audit.visualDesignAudit.models.message;

/**
 * Core Message object that defines global fields that are to be used by all Message objects
 */
public abstract class Message {
	private long account_id;
	private long domain_id;
	private long domain_audit_record_id;
	
	public Message(){
		setAccountId(-1);
	}
	
	/**
	 * 
	 * @param account_id
	 * @param audit_record_id TODO
	 * @param domain eg. example.com
	 */
	public Message(long account_id, long audit_record_id, long domain_id){
		setAccountId(account_id);
		setDomainAuditRecordId(audit_record_id);
		setDomainId(domain_id);
	}
	
	public long getAccountId() {
		return account_id;
	}

	protected void setAccountId(long account_id) {
		this.account_id = account_id;
	}

	public long getDomainAuditRecordId() {
		return domain_audit_record_id;
	}

	public void setDomainAuditRecordId(long audit_record_id) {
		this.domain_audit_record_id = audit_record_id;
	}

	public long getDomainId() {
		return domain_id;
	}

	public void setDomainId(long domain_id) {
		this.domain_id = domain_id;
	}
}
