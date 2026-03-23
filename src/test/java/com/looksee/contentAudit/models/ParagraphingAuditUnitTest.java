package com.looksee.contentAudit.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.looksee.models.audit.Score;

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

	@Test
	public void calculateParagraphScoreReturnsOneForZeroSentences() {
		Score score = ParagraphingAudit.calculateParagraphScore(0);
		assertEquals(1, score.getPoints());
		assertEquals(1, score.getMaxPoints());
	}

	@Test
	public void calculateParagraphScoreReturnsZeroForManySentences() {
		Score score = ParagraphingAudit.calculateParagraphScore(100);
		assertEquals(0, score.getPoints());
		assertEquals(1, score.getMaxPoints());
	}

	@Test
	public void calculateParagraphScoreBoundaryAtFive() {
		Score atFive = ParagraphingAudit.calculateParagraphScore(5);
		assertEquals(1, atFive.getPoints());
		assertEquals(1, atFive.getMaxPoints());

		Score atSix = ParagraphingAudit.calculateParagraphScore(6);
		assertEquals(0, atSix.getPoints());
		assertEquals(1, atSix.getMaxPoints());
	}

	@Test
	public void calculateParagraphScoreMaxPointsAlwaysOne() {
		assertEquals(1, ParagraphingAudit.calculateParagraphScore(0).getMaxPoints());
		assertEquals(1, ParagraphingAudit.calculateParagraphScore(3).getMaxPoints());
		assertEquals(1, ParagraphingAudit.calculateParagraphScore(5).getMaxPoints());
		assertEquals(1, ParagraphingAudit.calculateParagraphScore(6).getMaxPoints());
		assertEquals(1, ParagraphingAudit.calculateParagraphScore(50).getMaxPoints());
	}
}
