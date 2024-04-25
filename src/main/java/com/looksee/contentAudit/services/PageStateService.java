package com.looksee.contentAudit.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.looksee.contentAudit.models.ElementState;
import com.looksee.contentAudit.models.PageState;
import com.looksee.contentAudit.models.enums.ElementClassification;
import com.looksee.contentAudit.models.repository.ElementStateRepository;
import com.looksee.contentAudit.models.repository.PageStateRepository;

import io.github.resilience4j.retry.annotation.Retry;



/**
 * Service layer object for interacting with {@link PageState} database layer
 *
 */
@Service
@Retry(name = "neoforj")
public class PageStateService {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(PageStateService.class.getName());
	
	@Autowired
	private PageStateRepository page_state_repo;
	
	@Autowired
	private ElementStateRepository element_state_repo;

	/**
	 * Save a {@link PageState} object and its associated objects
	 * @param page_state
	 * @return
	 * @throws Exception 
	 * 
	 * @pre page_state != null
	 */
	public PageState save(PageState page_state) throws Exception {
		assert page_state != null;
		
		PageState page_state_record = page_state_repo.findByKey(page_state.getKey());
		
		if(page_state_record == null) {
			log.warn("page state wasn't found in database. Saving new page state to neo4j");

			return page_state_repo.save(page_state);
		}

		return page_state_record;
	}
	
	public PageState findByKey(String page_key) {
		PageState page_state = page_state_repo.findByKey(page_key);
		if(page_state != null){
			page_state.setElements(getElementStates(page_key));
		}
		return page_state;
	}
	
	public List<PageState> findByScreenshotChecksumAndPageUrl(String user_id, String url, String screenshot_checksum){
		return page_state_repo.findByScreenshotChecksumAndPageUrl(url, screenshot_checksum);		
	}
	
	public List<PageState> findByFullPageScreenshotChecksum(String screenshot_checksum){
		return page_state_repo.findByFullPageScreenshotChecksum(screenshot_checksum);		
	}
	
	public PageState findByAnimationImageChecksum(String user_id, String screenshot_checksum){
		return page_state_repo.findByAnimationImageChecksum(user_id, screenshot_checksum);		
	}
	
	public List<ElementState> getElementStates(String page_key){
		assert page_key != null;
		assert !page_key.isEmpty();
		
		return element_state_repo.getElementStates(page_key);
	}
	
	public List<ElementState> getElementStates(long page_state_id){
		return element_state_repo.getElementStates(page_state_id);
	}
	
	public List<PageState> findPageStatesWithForm(long account_id, String url, String page_key) {
		return page_state_repo.findPageStatesWithForm(account_id, url, page_key);
	}

	public Collection<ElementState> getExpandableElements(List<ElementState> elements) {
		List<ElementState> expandable_elements = new ArrayList<>();
		for(ElementState elem : elements) {
			if(ElementClassification.LEAF.equals(elem.getClassification())) {
				expandable_elements.add(elem);
			}
		}
		return expandable_elements;
	}
	
	public List<PageState> findBySourceChecksumForDomain(String url, String src_checksum) {
		return page_state_repo.findBySourceChecksumForDomain(url, src_checksum);
	}

	public PageState findByUrl(String url) {
		assert url != null;
		assert !url.isEmpty();
		
		return page_state_repo.findByUrl(url);
	}

	public Optional<PageState> findById(long page_id) {
		return page_state_repo.findById(page_id);
	}

	public void updateCompositeImageUrl(Long id, String composite_img_url) {
		page_state_repo.updateCompositeImageUrl(id, composite_img_url);
	}

	public void addAllElements(long page_state_id, List<Long> element_ids) {
		page_state_repo.addAllElements(page_state_id, element_ids);
	}

	public PageState findByDomainAudit(long domainAuditRecordId, long page_state_id) {
		return page_state_repo.findByDomainAudit(domainAuditRecordId, page_state_id);
	}

    public PageState findByAuditRecordId(long pageAuditId) {
        return page_state_repo.findByAuditRecordId(pageAuditId);
    }
}
