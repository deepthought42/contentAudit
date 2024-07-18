package com.looksee.contentAudit.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.looksee.contentAudit.models.Audit;
import com.looksee.contentAudit.models.UXIssueMessage;
import com.looksee.contentAudit.models.repository.AuditRepository;

import io.github.resilience4j.retry.annotation.Retry;

/**
 * Contains business logic for interacting with and managing audits
 *
 */
@Service
@Retry(name = "neoforj")
public class AuditService {
	private static Logger log = LoggerFactory.getLogger(AuditService.class);

	@Autowired
	private AuditRepository audit_repo;
	
	public Audit save(Audit audit) {
		assert audit != null;
		
		return audit_repo.save(audit);
	}

	public Optional<Audit> findById(long id) {
		return audit_repo.findById(id);
	}
	
	public Audit findByKey(String key) {
		return audit_repo.findByKey(key);
	}

	public List<Audit> findAll() {
		// TODO Auto-generated method stub
		return IterableUtils.toList(audit_repo.findAll());
	}

	public Set<UXIssueMessage> getIssues(long audit_id) {
		Set<UXIssueMessage> raw_issue_set = audit_repo.findIssueMessages(audit_id);
		
		return raw_issue_set.parallelStream()
							.filter(issue -> issue.getPoints() != issue.getMaxPoints())
							.distinct()
							.collect(Collectors.toSet());
	}


	public void addAllIssues(long id, List<Long> issue_ids) {
		audit_repo.addAllIssues(id, issue_ids);
	}

	public void addAllIssues(long audit_id, Set<UXIssueMessage> issue_messages) {
		List<Long> issue_ids = issue_messages.stream().map(x -> x.getId()).collect(Collectors.toList());
		addAllIssues(audit_id, issue_ids);
	}
}
