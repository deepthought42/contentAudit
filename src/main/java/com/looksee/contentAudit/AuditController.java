package com.looksee.contentAudit;

/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// [START cloudrun_pubsub_handler]
// [START run_pubsub_handler]
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.looksee.contentAudit.models.dto.PageAuditDto;
import com.looksee.contentAudit.models.enums.ExecutionStatus;
import com.looksee.contentAudit.gcp.PubSubAuditUpdatePublisherImpl;
import com.looksee.contentAudit.mapper.Body;
import com.looksee.contentAudit.models.Audit;
import com.looksee.contentAudit.models.AuditRecord;
import com.looksee.contentAudit.models.ImageAltTextAudit;
import com.looksee.contentAudit.models.ImageAudit;
import com.looksee.contentAudit.models.ImagePolicyAudit;
import com.looksee.contentAudit.models.PageState;
import com.looksee.contentAudit.models.ParagraphingAudit;
import com.looksee.contentAudit.models.ReadabilityAudit;
import com.looksee.contentAudit.models.enums.AuditCategory;
import com.looksee.contentAudit.models.enums.AuditLevel;
import com.looksee.contentAudit.models.enums.AuditName;
import com.looksee.contentAudit.models.message.AuditProgressUpdate;
import com.looksee.contentAudit.models.message.PageAuditMessage;
import com.looksee.contentAudit.services.AuditRecordService;
import com.looksee.contentAudit.services.MessageBroadcaster;
import com.looksee.contentAudit.services.PageStateService;
import com.looksee.utils.AuditUtils;

// PubsubController consumes a Pub/Sub message.
@RestController
public class AuditController {
	private static Logger log = LoggerFactory.getLogger(AuditController.class);

	@Autowired
	private AuditRecordService audit_record_service;
	
	@Autowired
	private PageStateService page_state_service;
	
	@Autowired
	private ImageAltTextAudit image_alt_text_auditor;

	@Autowired
	private ParagraphingAudit paragraph_auditor;

	@Autowired
	private ReadabilityAudit readability_auditor;

	@Autowired
	private PubSubAuditUpdatePublisherImpl audit_update_topic;
	
	@Autowired
	private ImageAudit image_audit;

	@Autowired
	private ImagePolicyAudit image_policy_audit;
	
	@Autowired
	private MessageBroadcaster message_broadcaster;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> receiveMessage(@RequestBody Body body) throws JsonMappingException, JsonProcessingException, ExecutionException, InterruptedException {
		
		Body.Message message = body.getMessage();
		String data = message.getData();
	    String target = !data.isEmpty() ? new String(Base64.getDecoder().decode(data)) : "";
        log.warn("page audit msg received = "+target);

	    ObjectMapper mapper = new ObjectMapper();
	    PageAuditMessage audit_record_msg = mapper.readValue(target, PageAuditMessage.class);
    
    	//page_audit_record_service.findById(audit_record_msg.getPageAuditId()).get();
    	log.warn("page audit id = "+audit_record_msg.getPageAuditId());
    	AuditRecord audit_record = audit_record_service.findById(audit_record_msg.getPageAuditId()).get();
		PageState page = page_state_service.getPageStateForAuditRecord(audit_record.getId());
    	//PageState page = page_state_service.findById(audit_record_msg.getPageId()).get();
		
		page.setElements(page_state_service.getElementStates(page.getId()));
		log.warn("evaluating "+page.getElements().size()+" element state for content audit with page ID :: "+page.getId());
		//String page_url = page.getUrl();
    	Set<Audit> audits = audit_record_service.getAllAudits(audit_record.getId());

    	// ALT TEXT AUDIT
		//try {
    		if(!auditAlreadyExists(audits, AuditName.ALT_TEXT)) {    			
				Audit alt_text_audit = image_alt_text_auditor.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), alt_text_audit);
    		}
    		/*
			AuditProgressUpdate audit_update2 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																		audit_record_msg.getPageAuditId(), 
																		(2.0 / 6.0), 
																		"Reviewing content for readability",
																		AuditCategory.CONTENT, 
																		AuditLevel.PAGE);
			
			String audit_record_json = mapper.writeValueAsString(audit_update2);
			log.warn("sending audit update :: "+audit_record_json);
				
			audit_update_topic.publish(audit_record_json);	
			*/
    	/*
	} catch (Exception e) {
				e.printStackTrace();
				Audit audit = new Audit(AuditCategory.CONTENT,
										 AuditSubcategory.IMAGERY,
										 AuditName.ALT_TEXT,
										 0,
										 null,
										 AuditLevel.PAGE,
										 0,
										 page_url, 
										 "", 
										 "An error occurred while executing audit",
										 true);
				executed_audits.add(audit);
		}
		*/

		//  READING COMPLEXITY AUDIT
	//	try {
    		if(!auditAlreadyExists(audits, AuditName.READING_COMPLEXITY)) {    			
				Audit readability_audit = readability_auditor.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), readability_audit);
    		}
		/*	
		} catch (Exception e) {
			Audit audit = new Audit(AuditCategory.CONTENT,
									 AuditSubcategory.WRITTEN_CONTENT,
									 AuditName.READING_COMPLEXITY,
									 0,
									 null,
									 AuditLevel.PAGE,
									 0, 
									 page_url,
									 "", 
									 "An error occurred while executing audit",
									 false); 
			executed_audits.add(audit);
			e.printStackTrace();
		}
*/
    		
    		
		//try {
    		if(!auditAlreadyExists(audits, AuditName.PARAGRAPHING)) {    			
				Audit paragraph_audit = paragraph_auditor.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), paragraph_audit);
    		}
			
    		/*
			AuditProgressUpdate audit_update4 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																		audit_record_msg.getPageAuditId(),
																		(4.0/6.0), 
																		"Content Audit Compelete!", 
																		AuditCategory.CONTENT,
																		AuditLevel.PAGE);
			
			String audit_record_json = mapper.writeValueAsString(audit_update4);
			log.warn("paragraph audit message :: " +audit_record_json);
			audit_update_topic.publish(audit_record_json);
			*/
    		/*
		} catch (Exception e) {
			Audit audit = new Audit(AuditCategory.CONTENT,
									 AuditSubcategory.WRITTEN_CONTENT, 
									 AuditName.PARAGRAPHING, 
									 0, 
									 new HashSet<>(), 
									 AuditLevel.PAGE, 
									 0, 
									 page_url,
									 "", 
									 "An error occurred while executing audit",
									 false);
			executed_audits.add(audit);
			
			e.printStackTrace();
		}
		*/

		//try {
    		if(!auditAlreadyExists(audits, AuditName.IMAGE_COPYRIGHT)) {    			
				Audit image_copyright_audit = image_audit.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), image_copyright_audit);
    		}
    		
    		/*
			AuditProgressUpdate audit_update5 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																		audit_record_msg.getPageAuditId(),
																		(5.0 / 6.0), 
																		"Reviewing images for uniqueness", 
																		AuditCategory.CONTENT,
																		AuditLevel.PAGE);

			String audit_record_json = mapper.writeValueAsString(audit_update5);
			log.warn("image copyright audit :: "+audit_record_json);
			audit_update_topic.publish(audit_record_json);
			*/
    		/*
		} catch (Exception e) {
			Audit audit = new Audit(AuditCategory.CONTENT,
					 AuditSubcategory.IMAGERY, 
					 AuditName.IMAGE_COPYRIGHT, 
					 0, 
					 new HashSet<>(), 
					 AuditLevel.PAGE, 
					 0, 
					 page_url,
					 "", 
					 "An error occurred while executing audit",
					 false); 
			audits.add(audit);
			e.printStackTrace();
		}
		*/
		
		//try {
    		if(!auditAlreadyExists(audits, AuditName.IMAGE_POLICY)) {    			
				Audit image_policy_result = image_policy_audit.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), image_policy_result);
    		}
    		
    		/*
			AuditProgressUpdate audit_update6 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																		audit_record_msg.getPageAuditId(),
																		1.0, 
																		"Reviewing images for compliance with domain policy", 
																		AuditCategory.CONTENT,
																		AuditLevel.PAGE);

			String audit_record_json = mapper.writeValueAsString(audit_update6);
			
			log.warn("iamge policy audit :: " + audit_record_json);
			audit_update_topic.publish(audit_record_json);
			*/
    		/*
		} catch (Exception e) {
			Audit audit = new Audit(AuditCategory.CONTENT,
					 AuditSubcategory.IMAGERY, 
					 AuditName.IMAGE_POLICY, 
					 0, 
					 new HashSet<>(), 
					 AuditLevel.PAGE, 
					 0, 
					 page_url,
					 "", 
					 "An error occurred while executing audit",
					 false); 

			audits.add(audit);

			e.printStackTrace();
		}*/
		
    		/*
		try {
			executed_audits = executed_audits.stream().map(audit -> audit_service.save(audit)).collect(Collectors.toList());
			executed_audits.stream().forEach(audit -> audit_record_service.addAudit(audit_record_msg.getPageAuditId(), audit.getId()));
		} catch (Exception e) {
			log.error("exception caught during content audit");
			e.printStackTrace();
			log.error("-------------------------------------------------------------");
			log.error("-------------------------------------------------------------");
			log.error("THERE WAS AN ISSUE DURING CONTENT AUDIT");
			log.error("-------------------------------------------------------------");
			log.error("-------------------------------------------------------------");
		    return new ResponseEntity<String>("Error performing content audit", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		*/
	    
		AuditProgressUpdate audit_update = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																	audit_record_msg.getPageAuditId(),
																	1.0, 
																	"Content Audit Compelete!",
																	AuditCategory.CONTENT, 
																	AuditLevel.PAGE);

		String audit_record_json = mapper.writeValueAsString(audit_update);
		audit_update_topic.publish(audit_record_json);
		PageAuditDto audit_dto = builPagedAuditdDto(audit_record_msg.getPageAuditId(), page.getUrl());
		message_broadcaster.sendAuditUpdate(Long.toString( audit_record_msg.getAccountId() ), audit_dto);

	    return new ResponseEntity<String>("Successfully completed content audit", HttpStatus.OK);
	}
	
	/**
	 * Creates an {@linkplain PageAuditDto} using page audit ID and the provided page_url
	 * @param pageAuditId
	 * @param page_url
	 * @return
	 */
	private PageAuditDto builPagedAuditdDto(long pageAuditId, String page_url) {
		//get all audits
		Set<Audit> audits = audit_record_service.getAllAudits(pageAuditId);
		Set<AuditName> audit_labels = new HashSet<AuditName>();
		audit_labels.add(AuditName.TEXT_BACKGROUND_CONTRAST);
		audit_labels.add(AuditName.NON_TEXT_BACKGROUND_CONTRAST);
		audit_labels.add(AuditName.TITLES);
		audit_labels.add(AuditName.IMAGE_COPYRIGHT);
		audit_labels.add(AuditName.IMAGE_POLICY);
		audit_labels.add(AuditName.LINKS);
		audit_labels.add(AuditName.ALT_TEXT);
		audit_labels.add(AuditName.METADATA);
		audit_labels.add(AuditName.READING_COMPLEXITY);
		audit_labels.add(AuditName.PARAGRAPHING);
		audit_labels.add(AuditName.ENCRYPTED);
		//count audits for each category
		//calculate content score
		//calculate aesthetics score
		//calculate information architecture score
		double visual_design_progress = AuditUtils.calculateProgress(AuditCategory.AESTHETICS, 
																 1, 
																 audits, 
																 AuditUtils.getAuditLabels(AuditCategory.AESTHETICS, audit_labels));
		double content_progress = AuditUtils.calculateProgress(AuditCategory.CONTENT, 
																1, 
																audits, 
																audit_labels);
		double info_architecture_progress = AuditUtils.calculateProgress(AuditCategory.INFORMATION_ARCHITECTURE, 
																		1, 
																		audits, 
																		audit_labels);

		double content_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.CONTENT);
		double info_architecture_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.INFORMATION_ARCHITECTURE);
		double visual_design_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.AESTHETICS);
		double a11y_score = AuditUtils.calculateScoreByCategory(audits, AuditCategory.ACCESSIBILITY);

		double data_extraction_progress = 1;
		String message = "";
		ExecutionStatus execution_status = ExecutionStatus.UNKNOWN;
		if(visual_design_progress < 1 || content_progress < 1 || visual_design_progress < 1) {
			execution_status = ExecutionStatus.IN_PROGRESS;
		}
		else {
			execution_status = ExecutionStatus.COMPLETE;
		}
		
		return new PageAuditDto(pageAuditId, 
								page_url, 
								content_score, 
								content_progress, 
								info_architecture_score, 
								info_architecture_progress, 
								a11y_score,
								visual_design_score,
								visual_design_progress,
								data_extraction_progress, 
								message, 
								execution_status);
	}
	
	/**
	 * Checks if the any of the provided {@link Audit audits} have a name that matches 
	 * 		the provided {@linkplain AuditName}
	 * 
	 * @param audits
	 * @param audit_name
	 * 
	 * @return
	 * 
	 * @pre audits != null
	 * @pre audit_name != null
	 */
	private boolean auditAlreadyExists(Set<Audit> audits, AuditName audit_name) {
		assert audits != null;
		assert audit_name != null;
		
		for(Audit audit : audits) {
			if(audit_name.equals(audit.getName())) {
				return true;
			}
		}
		return false;
	}
}
// [END run_pubsub_handler]
// [END cloudrun_pubsub_handler]