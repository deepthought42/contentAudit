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
import com.looksee.contentAudit.mapper.Body;
import com.looksee.contentAudit.models.Audit;
import com.looksee.contentAudit.models.AuditRecord;
import com.looksee.contentAudit.models.ImageAltTextAudit;
import com.looksee.contentAudit.models.ImageAudit;
import com.looksee.contentAudit.models.ImagePolicyAudit;
import com.looksee.contentAudit.models.PageState;
import com.looksee.contentAudit.models.ParagraphingAudit;
import com.looksee.contentAudit.models.ReadabilityAudit;
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
	private ImageAudit image_audit;

	@Autowired
	private ImagePolicyAudit image_policy_audit;
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ResponseEntity<String> receiveMessage(@RequestBody Body body) throws JsonMappingException, JsonProcessingException, ExecutionException, InterruptedException {
	 
		Body.Message message = body.getMessage();
		String data = message.getData();
	    String target = !data.isEmpty() ? new String(Base64.getDecoder().decode(data)) : "";
        log.warn("page audit msg received = "+target);

	    ObjectMapper input_mapper = new ObjectMapper();
	    PageAuditMessage audit_record_msg = input_mapper.readValue(target, PageAuditMessage.class);
	    
	   // JsonMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

	    try {
	    	//page_audit_record_service.findById(audit_record_msg.getPageAuditId()).get();
	    	log.warn("page audit id = "+audit_record_msg.getPageAuditId());
	    	AuditRecord audit_record = audit_record_service.findById(audit_record_msg.getPageAuditId()).get();
			PageState page = page_state_service.findById(audit_record_msg.getPageId()).get();
			
			page.setElements(page_state_service.getElementStates(page.getId()));
			log.warn("evaluating "+page.getElements().size()+" element state for content audit with page ID :: "+page.getId());
			
			/*
			AuditProgressUpdate audit_update = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																		audit_record_msg.getDomainAuditRecordId(), 
																		0.05, 
																		"checking images for alt text", 
																		AuditCategory.CONTENT,
																		AuditLevel.PAGE, 
																		audit_record_msg.getDomainId(), 
																		audit_record_msg.getPageAuditId());

			String audit_record_json = mapper.writeValueAsString(audit_update);
			audit_record_topic.publish(audit_record_json);
			  */
			try {
				Audit alt_text_audit = image_alt_text_auditor.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), alt_text_audit.getId());

				/*
				AuditProgressUpdate audit_update2 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																			audit_record_msg.getDomainAuditRecordId(), 
																			(2.0 / 6.0), 
																			"Reviewing content for readability",
																			AuditCategory.CONTENT, 
																			AuditLevel.PAGE, 
																			audit_record_msg.getDomainId(), 
																			audit_record_msg.getPageAuditId());

				
				audit_record_json = mapper.writeValueAsString(audit_update2);
				log.warn("sending audit update :: "+audit_record_json);
					
				audit_record_topic.publish(audit_record_json);	
				*/
			} catch (Exception e) {
				/*
				AuditError audit_err = new AuditError(audit_record_msg.getAccountId(), 
													  audit_record_msg.getDomainAuditRecordId(),
													  "An error occurred while reviewing images for uniqueness", 
													  AuditCategory.CONTENT,
													  (2.0 / 6.0),
													  audit_record_msg.getDomainId());
				
				audit_record_json = mapper.writeValueAsString(audit_err);
				error_topic.publish(audit_record_json);
*/
				e.printStackTrace();
			}

			try {
				Audit readability_audit = readability_auditor.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), readability_audit.getId());

				/*
				AuditProgressUpdate audit_update3 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																			audit_record_msg.getDomainAuditRecordId(), 
																			(3.0 / 6.0), 
																			"Reviewing paragraph length", 
																			AuditCategory.CONTENT,
																			AuditLevel.PAGE, 
																			audit_record_msg.getDomainId(), 
																			audit_record_msg.getPageAuditId());

				 
				 audit_record_json = mapper.writeValueAsString(audit_update3);
				 log.warn("readability audit :: " +audit_record_json);
					
				 audit_record_topic.publish(audit_record_json);
				 */
			} catch (Exception e) {
				/*
				AuditError audit_err = new AuditError(audit_record_msg.getAccountId(), 
													  audit_record_msg.getDomainAuditRecordId(),
													  "An error occurred while reviewing images for uniqueness", 
													  AuditCategory.CONTENT,
													  (3.0 / 6.0),
													  audit_record_msg.getDomainId());
				
				audit_record_json = mapper.writeValueAsString(audit_err);
				error_topic.publish(audit_record_json);
				*/
				e.printStackTrace();
			}

			try {
				Audit paragraph_audit = paragraph_auditor.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), paragraph_audit.getId());

				
				/*
				AuditProgressUpdate audit_update4 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																			audit_record_msg.getDomainAuditRecordId(),
																			(4.0/6.0), 
																			"Content Audit Compelete!", 
																			AuditCategory.CONTENT,
																			AuditLevel.PAGE, 
																			audit_record_msg.getDomainId(),
																			audit_record_msg.getPageAuditId());

				audit_record_json = mapper.writeValueAsString(audit_update4);
				log.warn("paragraph audit message :: " +audit_record_json);
				audit_record_topic.publish(audit_record_json);
				*/
			} catch (Exception e) {
				/*
				AuditError audit_err = new AuditError(audit_record_msg.getAccountId(), 
													  audit_record_msg.getDomainAuditRecordId(),
													  "An error occurred while reviewing images for uniqueness", 
													  AuditCategory.CONTENT,
													  (4.0 / 6.0),
													  audit_record_msg.getDomainId());
				audit_record_json = mapper.writeValueAsString(audit_err);
				error_topic.publish(audit_record_json);
				*/
				e.printStackTrace();
			}

			try {
				Audit image_copyright_audit = image_audit.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), image_copyright_audit.getId());

				/*
				AuditProgressUpdate audit_update5 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																			audit_record_msg.getDomainAuditRecordId(),
																			(5.0 / 6.0), 
																			"Reviewing images for uniqueness", 
																			AuditCategory.CONTENT,
																			AuditLevel.PAGE, 
																			audit_record_msg.getDomainId(), 
																			audit_record_msg.getPageAuditId());

				
				audit_record_json = mapper.writeValueAsString(audit_update5);
				log.warn("image copyright audit :: "+audit_record_json);
				audit_record_topic.publish(audit_record_json);
				 */
			} catch (Exception e) {
				/*
				AuditError audit_err = new AuditError(audit_record_msg.getAccountId(), 
													  audit_record_msg.getDomainAuditRecordId(),
													  "An error occurred while reviewing images for uniqueness", 
													  AuditCategory.CONTENT,
													  (5.0 / 6.0),
													  audit_record_msg.getDomainId());
				
				audit_record_json = mapper.writeValueAsString(audit_err);
				error_topic.publish(audit_record_json);
				*/
				e.printStackTrace();
			}
			
			try {
				Audit image_policy_result = image_policy_audit.execute(page, audit_record, null);
				audit_record_service.addAudit(audit_record_msg.getPageAuditId(), image_policy_result.getId());

				/*
				AuditProgressUpdate audit_update6 = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																			audit_record_msg.getDomainAuditRecordId(),
																			1, 
																			"Reviewing images for compliance with domain policy", 
																			AuditCategory.CONTENT,
																			AuditLevel.PAGE, 
																			audit_record_msg.getDomainId(), 
																			audit_record_msg.getPageAuditId());

				audit_record_json = mapper.writeValueAsString(audit_update6);
				
				log.warn("iamge policy audit :: " + audit_record_json);
				audit_record_topic.publish(audit_record_json);
				 */
			} catch (Exception e) {
				/*
				AuditError audit_err = new AuditError(audit_record_msg.getAccountId(), 
													  audit_record_msg.getDomainAuditRecordId(),
													  "An error occurred while reviewing images for uniqueness", 
													  AuditCategory.CONTENT,
													  1.0,
													  audit_record_msg.getDomainId());
				
				audit_record_json = mapper.writeValueAsString(audit_err);
				error_topic.publish(audit_record_json);
				*/
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			log.error("exception caught during content audit");
			e.printStackTrace();
			log.error("-------------------------------------------------------------");
			log.error("-------------------------------------------------------------");
			log.error("THERE WAS AN ISSUE DURING CONTENT AUDIT");
			log.error("-------------------------------------------------------------");
			log.error("-------------------------------------------------------------");
		}
	    
	    /*
		AuditProgressUpdate audit_update = new AuditProgressUpdate(audit_record_msg.getAccountId(),
																	audit_record_msg.getDomainAuditRecordId(),
																	1.0, 
																	"Content Audit Compelete!",
																	AuditCategory.CONTENT, 
																	AuditLevel.PAGE, 
																	audit_record_msg.getDomainId(), 
																	audit_record_msg.getPageAuditId());

		String audit_record_json = mapper.writeValueAsString(audit_update);
		error_topic.publish(audit_record_json);
	     */
	    
	    return new ResponseEntity<String>("Successfully completed content audit", HttpStatus.OK);
	}
}
// [END run_pubsub_handler]
// [END cloudrun_pubsub_handler]