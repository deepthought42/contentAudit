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
import com.looksee.contentAudit.models.enums.AuditName;
import com.looksee.contentAudit.models.message.AuditProgressUpdate;
import com.looksee.contentAudit.models.message.PageAuditMessage;
import com.looksee.contentAudit.services.AuditRecordService;
import com.looksee.contentAudit.services.PageStateService;

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
		
		page.setElements(page_state_service.getElementStates(page.getId()));
		log.warn("evaluating "+page.getElements().size()+" element state for content audit with page ID :: "+page.getId());
    	Set<Audit> audits = audit_record_service.getAllAudits(audit_record.getId());

    	// ALT TEXT AUDIT
		if(!auditAlreadyExists(audits, AuditName.ALT_TEXT)) {    			
			Audit alt_text_audit = image_alt_text_auditor.execute(page, audit_record, null);
			audit_record_service.addAudit(audit_record_msg.getPageAuditId(), alt_text_audit);
		}
		
		if(!auditAlreadyExists(audits, AuditName.READING_COMPLEXITY)) {    			
			Audit readability_audit = readability_auditor.execute(page, audit_record, null);
			audit_record_service.addAudit(audit_record_msg.getPageAuditId(), readability_audit);
		}
    		
		if(!auditAlreadyExists(audits, AuditName.PARAGRAPHING)) {    			
			Audit paragraph_audit = paragraph_auditor.execute(page, audit_record, null);
			audit_record_service.addAudit(audit_record_msg.getPageAuditId(), paragraph_audit);
		}

		if(!auditAlreadyExists(audits, AuditName.IMAGE_COPYRIGHT)) {    			
			Audit image_copyright_audit = image_audit.execute(page, audit_record, null);
			audit_record_service.addAudit(audit_record_msg.getPageAuditId(), image_copyright_audit);
		}

		if(!auditAlreadyExists(audits, AuditName.IMAGE_POLICY)) {    			
			Audit image_policy_result = image_policy_audit.execute(page, audit_record, null);
			audit_record_service.addAudit(audit_record_msg.getPageAuditId(), image_policy_result);
		}
	
		AuditProgressUpdate audit_update = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																	audit_record_msg.getPageAuditId(),
																	"Completed content audit!");

		String audit_record_json = mapper.writeValueAsString(audit_update);
		audit_update_topic.publish(audit_record_json);

	    return new ResponseEntity<String>("Successfully completed content audit", HttpStatus.OK);
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