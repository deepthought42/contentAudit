package com.looksee.contentAudit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.looksee.models.audit.Audit;
import com.looksee.models.enums.AuditName;

public class AuditControllerUnitTest {

	@Test
	public void acknowledgeInvalidMessageReturnsOkStatusAndReasonBody() throws Exception {
		AuditController controller = new AuditController();
		Method method = AuditController.class.getDeclaredMethod("acknowledgeInvalidMessage", String.class);
		method.setAccessible(true);

		@SuppressWarnings("unchecked")
		ResponseEntity<String> response = (ResponseEntity<String>) method.invoke(controller, "Invalid pageAuditId");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Invalid pageAuditId", response.getBody());
	}

	@Test
	public void auditAlreadyExistsReturnsTrueWhenAuditNameMatches() throws Exception {
		AuditController controller = new AuditController();
		Method method = AuditController.class.getDeclaredMethod("auditAlreadyExists", Set.class, AuditName.class);
		method.setAccessible(true);

		Audit existing = mock(Audit.class);
		when(existing.getName()).thenReturn(AuditName.ALT_TEXT);

		Audit other = mock(Audit.class);
		when(other.getName()).thenReturn(AuditName.PARAGRAPHING);

		Set<Audit> audits = new HashSet<>();
		audits.add(existing);
		audits.add(other);

		Boolean result = (Boolean) method.invoke(controller, audits, AuditName.ALT_TEXT);

		assertTrue(result);
	}

	@Test
	public void auditAlreadyExistsReturnsFalseWhenNoAuditNameMatches() throws Exception {
		AuditController controller = new AuditController();
		Method method = AuditController.class.getDeclaredMethod("auditAlreadyExists", Set.class, AuditName.class);
		method.setAccessible(true);

		Audit existing = mock(Audit.class);
		when(existing.getName()).thenReturn(AuditName.READING_COMPLEXITY);

		Set<Audit> audits = new HashSet<>();
		audits.add(existing);

		Boolean result = (Boolean) method.invoke(controller, audits, AuditName.ALT_TEXT);

		assertFalse(result);
	}
}
