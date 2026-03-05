package com.looksee.contentAudit.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParagraphingAuditUnitTest {

	@Test
	public void calculateParagraphScoreRewardsFiveOrFewerSentences() {
		assertEquals(1, ParagraphingAudit.calculateParagraphScore(1).getPoints());
		assertEquals(1, ParagraphingAudit.calculateParagraphScore(5).getPoints());
	}

	@Test
	public void calculateParagraphScorePenalizesMoreThanFiveSentences() {
		assertEquals(0, ParagraphingAudit.calculateParagraphScore(6).getPoints());
		assertEquals(0, ParagraphingAudit.calculateParagraphScore(10).getPoints());
	}
}
