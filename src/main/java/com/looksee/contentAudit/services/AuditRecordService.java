package com.looksee.contentAudit.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.looksee.contentAudit.models.repository.AuditRecordRepository;
import com.looksee.contentAudit.models.repository.AuditRepository;
import com.looksee.contentAudit.models.Audit;
import com.looksee.contentAudit.models.AuditRecord;
import com.looksee.contentAudit.models.DesignSystem;
import com.looksee.contentAudit.models.DomainAuditRecord;
import com.looksee.contentAudit.models.Label;
import com.looksee.contentAudit.models.PageAuditRecord;
import com.looksee.contentAudit.models.PageState;
import com.looksee.contentAudit.models.UXIssueMessage;

/**
 * Contains business logic for interacting with and managing audits
 *
 */
@Service
public class AuditRecordService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(AuditRecordService.class);
	
	@Autowired
	private AuditRecordRepository audit_record_repo;
	
	@Autowired
	private AuditRepository audit_repo;
	
	@Autowired
	private PageStateService page_state_service;
	
	@Retryable
	public AuditRecord save(AuditRecord audit) {
		assert audit != null;

		return audit_record_repo.save(audit);
	}

	public Optional<AuditRecord> findById(long id) {
		return audit_record_repo.findById(id);
	}
	
	public AuditRecord findByKey(String key) {
		return audit_record_repo.findByKey(key);
	}


	public List<AuditRecord> findAll() {
		// TODO Auto-generated method stub
		return IterableUtils.toList(audit_record_repo.findAll());
	}
	
	@Retryable
	public void addAudit(long audit_record_id, long audit_id) {
		//check if audit already exists for page state
		Optional<Audit> audit = audit_repo.getAuditForAuditRecord(audit_record_id, audit_id);
		if(!audit.isPresent()) {
			audit_record_repo.addAudit(audit_record_id, audit_id);
		}
	}
	
	@Retryable
	public void addAudit(long audit_record_id, Audit audit) {
		//check if audit already exists for page state
		Optional<Audit> audit_record = audit_repo.getAuditForAuditRecord(audit_record_id, audit.getKey());
		if(!audit_record.isPresent()) {
			audit_record_repo.addAudit(audit_record_id, audit.getId());
		}
	}
	
	public Set<Audit> getAllAuditsAndIssues(long audit_id) {		
		return audit_repo.getAllAuditsForPageAuditRecord(audit_id);
	}
	
	public Optional<DomainAuditRecord> findMostRecentDomainAuditRecord(long id) {
		return audit_record_repo.findMostRecentDomainAuditRecord(id);
	}
	
	public Optional<PageAuditRecord> findMostRecentPageAuditRecord(String page_url) {
		assert page_url != null;
		assert !page_url.isEmpty();
		
		return audit_record_repo.getMostRecentPageAuditRecord(page_url);
	}
	
	public Set<Audit> findMostRecentAuditsForPage(String page_url) {
		assert page_url != null;
		assert !page_url.isEmpty();
		
		//get most recent page state
		PageState page_state = page_state_service.findByUrl(page_url);
		return audit_repo.getMostRecentAuditsForPage(page_state.getKey());
		//return audit_record_repo.findMostRecentDomainAuditRecord(page_url);
	}
	
	public Optional<PageAuditRecord> getMostRecentPageAuditRecord(String url) {
		assert url != null;
		assert !url.isEmpty();
		
		return audit_record_repo.getMostRecentPageAuditRecord(url);
	}

	@Deprecated
	public PageState getPageStateForAuditRecord(String page_audit_key) {
		assert page_audit_key != null;
		assert !page_audit_key.isEmpty();
		
		return audit_record_repo.getPageStateForAuditRecord(page_audit_key);
	}

	public Set<Audit> getAllContentAuditsForDomainRecord(long id) {
		return audit_repo.getAllContentAuditsForDomainRecord(id);
	}

	public Set<Audit> getAllInformationArchitectureAuditsForDomainRecord(long id) {
		return audit_repo.getAllInformationArchitectureAuditsForDomainRecord(id);
	}

	public Set<Audit> getAllAccessibilityAuditsForDomainRecord(long id) {
		return audit_repo.getAllAccessibilityAuditsForDomainRecord(id);
	}

	public Set<Audit> getAllAestheticAuditsForDomainRecord(long id) {
		return audit_repo.getAllAestheticsAuditsForDomainRecord(id);
	}

	public Set<Audit> getAllContentAudits(long audit_record_id) {
		return audit_repo.getAllContentAudits(audit_record_id);
	}

	public Set<Audit> getAllInformationArchitectureAudits(long id) {
		return audit_repo.getAllInformationArchitectureAudits(id);
	}

	public Set<Audit> getAllAccessibilityAudits(Long id) {
		return audit_repo.getAllAccessibilityAudits(id);
	}

	public Set<Audit> getAllAestheticAudits(long id) {
		return audit_repo.getAllAestheticsAudits(id);
	}

	public Set<UXIssueMessage> getIssues(long audit_record_id) {
		return audit_record_repo.getIssues(audit_record_id);
	}

	public Set<PageState> getPageStatesForDomainAuditRecord(long audit_record_id) {
		return audit_record_repo.getPageStatesForDomainAuditRecord(audit_record_id);
	}

	public void addPageToAuditRecord(long audit_record_id, long page_state_id) {
		audit_record_repo.addPageToAuditRecord( audit_record_id, page_state_id );		
	}

	public long getIssueCountBySeverity(long id, String severity) {
		return audit_record_repo.getIssueCountBySeverity(id, severity);
	}

	public int getPageAuditCount(long domain_audit_id) {
		return audit_record_repo.getPageAuditRecordCount(domain_audit_id);
	}

	public Set<Audit> getAllAudits(long id) {
		return audit_repo.getAllAudits(id);
	}

	public boolean isDomainAuditComplete(AuditRecord audit_record) {		
		//audit_record should now have a domain audit record
		//get all page audit records for domain audit

		Set<PageAuditRecord> page_audits = audit_record_repo.getAllPageAudits(audit_record.getId());
		if(audit_record.getDataExtractionProgress() < 1.0) {
			return false;
		}
		//check all page audit records. If all are complete then the domain is also complete
		for(PageAuditRecord audit : page_audits) {
			if(!audit.isComplete()) {
				return false;
			}
		}
		
		return true;
	}

	public Optional<DomainAuditRecord> getDomainAuditRecordForPageRecord(long id) {
		return audit_record_repo.getDomainForPageAuditRecord(id);
	}

	public Set<Label> getLabelsForImageElements(long id) {
		return audit_record_repo.getLabelsForImageElements(id);
	}

	public Optional<DesignSystem> getDesignSystem(long audit_record_id) {
		return audit_record_repo.getDesignSystem(audit_record_id);
	}

	/**
	 * Retrieves {@link PageState} with given URL for {@link DomainAuditRecord}  
	 * @param audit_record_id
	 * @param current_url
	 * @return
	 */
	public PageState findPageWithUrl(long audit_record_id, String url) {
		return audit_record_repo.findPageWithUrl(audit_record_id, url);
	}
	
	/**
	 * Retrieves {@link PageState} with given URL for {@link DomainAuditRecord}  
	 * @param audit_record_id
	 * @param current_url
	 * @return
	 */
	public AuditRecord findPageWithId(long audit_record_id, long page_id) {
		return audit_record_repo.findPageWithId(audit_record_id, page_id);
	}
	

	/**
	 * Retrieves an {@link AuditRecord} for the page with the given id
	 * @param id
	 * @return
	 */
	public PageAuditRecord getAuditRecord(long id) {
		
		return audit_record_repo.getAuditRecord(id);
	}
}
