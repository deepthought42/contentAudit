package com.looksee.contentAudit.models;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.looksee.contentAudit.models.enums.AuditCategory;
import com.looksee.contentAudit.models.enums.AuditLevel;
import com.looksee.contentAudit.models.enums.AuditName;
import com.looksee.contentAudit.models.enums.AuditSubcategory;
import com.looksee.contentAudit.models.enums.Priority;
import com.looksee.contentAudit.services.AuditService;
import com.looksee.contentAudit.services.UXIssueMessageService;


/**
 * Responsible for executing an audit on the images on a page to determine adherence to alternate text best practices 
 *  for the visual audit category
 */
@Component
public class SVGAltTextAudit implements IExecutablePageStateAudit {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ImageAltTextAudit.class);
	
	@Autowired
	private AuditService audit_service;
	
	@Autowired
	private UXIssueMessageService issue_message_service;
	
	public SVGAltTextAudit() {
		//super(buildBestPractices(), getAdaDescription(), getAuditDescription(), AuditSubcategory.LINKS);
	}

	
	/**
	 * {@inheritDoc}
	 * 
	 * Scores images on a page based on if the image has an "alt" value present, format is valid and the 
	 *   url goes to a location that doesn't produce a 4xx error 
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	@Override
	public Audit execute(PageState page_state, AuditRecord audit_record, DesignSystem design_system) { 
		assert page_state != null;
		
		Set<UXIssueMessage> issue_messages = new HashSet<>();

		Set<String> labels = new HashSet<>();
		labels.add("alt_text");
		labels.add("wcag");
		
		// tags not covered = canvas, video, audio and figure
		List<ElementState> element_states = new ArrayList<>();
		for(ElementState element : page_state.getElements()) {
			if("svg".equalsIgnoreCase(element.getName())) {
				element_states.add(element);
			}
		}
		
		String why_it_matters = "Ensuring SVG elements have <title> and <desc> tags ensures that all users understand the purpose of SVG elements on your site.";
		String ada_compliance = "Your website does not meet the level A ADA compliance requirement for" +
				" ‘Alt’ text within SVG elements.";

		//score each link element
		for(ElementState svg_element : element_states) {
			Document jsoup_doc = Jsoup.parseBodyFragment(svg_element.getAllText(), page_state.getUrl());
			Element title_element = jsoup_doc.getElementsByTag("title").first();
			Element description_element = jsoup_doc.getElementsByTag("desc").first();

			if(title_element == null || title_element.text().isEmpty()){
				String title = "SVG does not have title element";
				String description = "SVG does not have title element";
				
				ElementStateIssueMessage issue_message = new ElementStateIssueMessage(
					Priority.HIGH,
					description, 
					"SVG tag should have a <title> element defined within the SVG",
					null,
					AuditCategory.CONTENT,
					labels,
					ada_compliance,
					title,
					0,
					1);

				issue_message = (ElementStateIssueMessage) issue_message_service.save(issue_message);
				issue_message_service.addElement(issue_message.getId(), svg_element.getId());
				issue_messages.add(issue_message);
			}
			else{
				String title = "SVG has title included!";
				String description = "Well done! This SVG is considered accessible according to WCAG 2.1 section 1.1.1";
				
				ElementStateIssueMessage issue_message = new ElementStateIssueMessage(
																Priority.NONE,
																description,
																"",
																null,
																AuditCategory.CONTENT,
																labels,
																ada_compliance,
																title,
																1,
																1);

				issue_message = (ElementStateIssueMessage) issue_message_service.save(issue_message);
				issue_message_service.addElement(issue_message.getId(), svg_element.getId());
				issue_messages.add(issue_message);
			}

			if(description_element == null || description_element.text().isEmpty()){
				String title = "SVG does not have <desc> element";
				String description = "SVG does not have <desc> element";
				
				ElementStateIssueMessage issue_message = new ElementStateIssueMessage(
					Priority.HIGH,
					description, 
					"SVG tag should have a <desc> element defined within the SVG",
					null,
					AuditCategory.CONTENT,
					labels,
					ada_compliance,
					title,
					0,
					1);

				issue_message = (ElementStateIssueMessage) issue_message_service.save(issue_message);
				issue_message_service.addElement(issue_message.getId(), svg_element.getId());
				issue_messages.add(issue_message);
			}
			else{
				String title = "SVG has description included!";
				String description = "Well done! This SVG is considered accessible according to WCAG 2.1 section 1.1.1";
				
				ElementStateIssueMessage issue_message = new ElementStateIssueMessage(
																Priority.NONE,
																description,
																"",
																null,
																AuditCategory.CONTENT,
																labels,
																ada_compliance,
																title,
																1,
																1);

				issue_message = (ElementStateIssueMessage) issue_message_service.save(issue_message);
				issue_message_service.addElement(issue_message.getId(), svg_element.getId());
				issue_messages.add(issue_message);
			}
		}
		
		int points_earned = 0;
		int max_points = 0;
		for(UXIssueMessage issue_msg : issue_messages) {
			points_earned += issue_msg.getPoints();
			max_points += issue_msg.getMaxPoints();
		}
		
		//log.warn("ALT TEXT AUDIT SCORE ::  "+ points_earned + " / " + max_points);
		String description = "SVG tags should have <title> and <desc> elements and they should not be empty";

		Audit audit = new Audit(AuditCategory.CONTENT,
								 AuditSubcategory.IMAGERY,
								 AuditName.ALT_TEXT,
								 points_earned,
								 null,
								 AuditLevel.PAGE,
								 max_points,
								 page_state.getUrl(),
								 why_it_matters,
								 description,
								 true);
								 
		audit = audit_service.save(audit);
		audit_service.addAllIssues(audit.getId(), issue_messages);
		
		return audit;
	}
}
