package com.looksee.contentAudit.models.dto;

public class PageBuiltMessage {
	private String account_id;
	private long page_id;
	private long domain_id;
	private long domain_audit_id;
	
	public PageBuiltMessage() {}
	
	public PageBuiltMessage(long page_id, long domain_audit_id) {
		setPageId(page_id);
		setDomainAuditId(domain_audit_id);
	}
	
	public long getPageId() {
		return page_id;
	}
	public void setPageId(long page_id) {
		this.page_id = page_id;
	}
	public long getDomainAuditId() {
		return domain_audit_id;
	}
	public void setDomainAuditId(long domain_audit_id) {
		this.domain_audit_id = domain_audit_id;
	}

	public String getAccountId() {
		return account_id;
	}

	public void setAccountId(String account_id) {
		this.account_id = account_id;
	}

	public long getDomainId() {
		return domain_id;
	}

	public void setDomainId(long domain_id) {
		this.domain_id = domain_id;
	}
}
