package com.looksee.audit.contentAudit.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import com.looksee.audit.contentAudit.models.ColorContrastIssueMessage;



@Repository
public interface ColorContrastIssueMessageRepository extends Neo4jRepository<ColorContrastIssueMessage, Long>  {
	
}
