package com.looksee.contentAudit.models.message;

import lombok.Getter;
import lombok.Setter;

/**
 * Message that is used by auditors to initiate and perform audits
 */
public class PageAuditMessage extends Message {
	@Getter
	@Setter
	private long pageAuditId;

	@Getter
	@Setter
	private long pageId;
	
	public PageAuditMessage() {}
	
	public PageAuditMessage(long account_id,
							long domain_id,
							long domain_audit_id,
							long page_audit_id, 
							long page_id
	) {
		super(account_id);
		setPageAuditId(page_audit_id);
		setPageId(page_id);
	}	
}
