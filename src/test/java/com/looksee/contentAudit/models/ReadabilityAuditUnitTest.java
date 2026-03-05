package com.looksee.contentAudit.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Test;

import com.looksee.models.ElementState;
import com.looksee.models.audit.Score;

public class ReadabilityAuditUnitTest {

	@Test
	public void calculateSentenceScoreReturnsFullPointsForShortSentence() {
		Score score = ReadabilityAudit.calculateSentenceScore("This sentence has only a few words.");

		assertEquals(2, score.getPoints());
		assertEquals(2, score.getMaxPoints());
		assertTrue(score.getIssueMessages().isEmpty());
	}

	@Test
	public void calculateSentenceScoreReturnsPartialPointsForMediumSentence() {
		Score score = ReadabilityAudit.calculateSentenceScore(
			"This sentence has more than ten words but remains short enough to avoid the longest penalty tier.");

		assertEquals(1, score.getPoints());
		assertEquals(2, score.getMaxPoints());
	}

	@Test
	public void calculateSentenceScoreReturnsNoPointsForLongSentence() {
		Score score = ReadabilityAudit.calculateSentenceScore(
			"This sentence intentionally includes enough additional words to ensure it exceeds twenty words and is scored in the lowest readability sentence scoring category.");

		assertEquals(0, score.getPoints());
		assertEquals(2, score.getMaxPoints());
	}

	@Test
	public void calculateParagraphScoreReturnsExpectedValues() {
		assertEquals(1, ReadabilityAudit.calculateParagraphScore(5).getPoints());
		assertEquals(0, ReadabilityAudit.calculateParagraphScore(6).getPoints());
	}

	@Test
	public void privateHelpersHandleNullAndConsumerLabels() throws Exception {
		ReadabilityAudit audit = new ReadabilityAudit();

		Method countWordsMethod = ReadabilityAudit.class.getDeclaredMethod("countWords", String.class);
		countWordsMethod.setAccessible(true);
		assertEquals(0, ((Integer) countWordsMethod.invoke(null, (String) null)).intValue());
		assertEquals(0, ((Integer) countWordsMethod.invoke(null, "   ")).intValue());
		assertEquals(3, ((Integer) countWordsMethod.invoke(null, "alpha beta gamma")).intValue());

		Method xpathContainsMethod = ReadabilityAudit.class.getDeclaredMethod("xpathContains", String.class, String.class);
		xpathContainsMethod.setAccessible(true);
		assertTrue((Boolean) xpathContainsMethod.invoke(null, "/html/body/div", "body"));

		Method consumerTypeMethod = ReadabilityAudit.class.getDeclaredMethod("getConsumerType", String.class);
		consumerTypeMethod.setAccessible(true);
		assertEquals("the average consumer", consumerTypeMethod.invoke(audit, new Object[] { null }));
		assertEquals("users with a College education", consumerTypeMethod.invoke(audit, "College"));
	}

	@Test
	public void generateIssueDescriptionIncludesElementTextAndEducationAudience() throws Exception {
		ReadabilityAudit audit = new ReadabilityAudit();
		ElementState element = mock(ElementState.class);
		when(element.getAllText()).thenReturn("Complex sample content");

		Method method = ReadabilityAudit.class.getDeclaredMethod("generateIssueDescription", ElementState.class, String.class, String.class);
		method.setAccessible(true);

		String description = (String) method.invoke(audit, element, "difficult", "HS");

		assertTrue(description.contains("Complex sample content"));
		assertTrue(description.contains("difficult"));
		assertTrue(description.contains("users with a HS education"));
	}

	@Test
	public void getPointsForEducationLevelUsesExpectedBands() throws Exception {
		ReadabilityAudit audit = new ReadabilityAudit();
		Method method = ReadabilityAudit.class.getDeclaredMethod("getPointsForEducationLevel", double.class, String.class);
		method.setAccessible(true);

		assertEquals(4, ((Integer) method.invoke(audit, 95.0d, null)).intValue());
		assertEquals(3, ((Integer) method.invoke(audit, 95.0d, "Advanced")).intValue());
		assertEquals(2, ((Integer) method.invoke(audit, 65.0d, "HS")).intValue());
		assertEquals(3, ((Integer) method.invoke(audit, 45.0d, "Advanced")).intValue());
		assertEquals(0, ((Integer) method.invoke(audit, 20.0d, "HS")).intValue());
	}
}
