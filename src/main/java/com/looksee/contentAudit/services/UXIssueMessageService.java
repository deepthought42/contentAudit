package com.looksee.contentAudit.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.looksee.contentAudit.models.ColorContrastIssueMessage;
import com.looksee.contentAudit.models.ElementState;
import com.looksee.contentAudit.models.UXIssueMessage;
import com.looksee.contentAudit.models.repository.ColorContrastIssueMessageRepository;
import com.looksee.contentAudit.models.repository.UXIssueMessageRepository;

import lombok.Synchronized;

@Service
public class UXIssueMessageService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(UXIssueMessageService.class);

	@Autowired
	private UXIssueMessageRepository issue_message_repo;
	
	@Autowired
	private ColorContrastIssueMessageRepository contrast_issue_message_repo;
	
	@Retryable
	public UXIssueMessage save(UXIssueMessage ux_issue) {
		return issue_message_repo.save(ux_issue);
	}
	
	public ColorContrastIssueMessage saveColorContrast(ColorContrastIssueMessage ux_issue) {
		return contrast_issue_message_repo.save(ux_issue);
	}

	/**
	 * Find {@link UXIssueMessage} with a given key
	 * @param key used for identifying {@link UXIssueMessage}
	 * 
	 * @return updated {@link UXIssueMessage} object
	 * 
	 * @pre key != null
	 * @pre !key.isEmpty()
	 */
	public UXIssueMessage findByKey(String key) {
		assert key != null;
		assert !key.isEmpty();
		
		return issue_message_repo.findByKey(key);
	}
	
	/**
	 * Add recommendation string to observation with a given key
	 * 
	 * @param key for finding observation to be updated
	 * @param recommendation to be added to observation
	 * 
	 * @return updated UXIssueMessage record
	 * 
	 * @pre key != null
	 * @pre !key.isEmpty()
	 * @pre priority != null
	 * @pre priority.isEmpty()
	 */
	public UXIssueMessage addRecommendation(String key, String recommendation) {
		assert key != null;
		assert !key.isEmpty();
		assert recommendation != null;
		assert !recommendation.isEmpty();
		
		UXIssueMessage observation = findByKey(key);
    	return save(observation);
	}

	/**
	 * Update priority of observation with a given key
	 * 
	 * @param key for finding observation to be updated
	 * @param priority to be set on observation
	 * @return updated UXIssueMessage record
	 * 
	 * @pre key != null
	 * @pre !key.isEmpty()
	 * @pre priority != null
	 * @pre priority.isEmpty()
	 */
	public UXIssueMessage updatePriority(String key, String priority) {
		assert key != null;
		assert !key.isEmpty();
		assert priority != null;
		assert !priority.isEmpty();
		
		UXIssueMessage observation = findByKey(key);
    	//observation.setPriority(Priority.create(priority));
    	return save(observation);	
	}

	public ElementState getElement(long id) {
		return issue_message_repo.getElement(id);
	}

	public Iterable<UXIssueMessage> saveAll(List<UXIssueMessage> issue_messages) {
		return issue_message_repo.saveAll(issue_messages);
		
	}

	public ElementState getGoodExample(long issue_id) {
		return issue_message_repo.getGoodExample(issue_id);
	}

	@Retryable
	@Synchronized
	public void addElement(long issue_id, long element_id) {
		issue_message_repo.addElement(issue_id, element_id);
	}

	@Retryable
	@Synchronized
	public void addPage(long issue_id, long page_id) {
		issue_message_repo.addPage(issue_id, page_id);
	}
}
