package com.looksee.contentAudit.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.looksee.contentAudit.models.enums.JourneyStatus;
import com.looksee.contentAudit.models.journeys.Journey;

public interface JourneyRepository extends Neo4jRepository<Journey, Long>  {
	
	public Journey findByKey(@Param("key") String key);

	@Query("MATCH (j:Journey) MATCH (s:Step) WHERE id(s)=$step_id AND id(j)=$journey_id MERGE (j)-[:HAS]->(s) RETURN j")
	public Journey addStep(@Param("journey_id") long journey_id, @Param("step_id") long id);

	@Query("MATCH (j:Journey{candidateKey:$candidateKey}) RETURN j")
	public Journey findByCandidateKey(@Param("candidateKey") String candidateKey);
	
	@Query("MATCH (audit:DomainAuditRecord) WHERE id(audit)=$audit_id MATCH (audit)-[*2]->(j:Journey) WHERE j.status=$status RETURN COUNT(j)")
	public int findAllJourneysForDomainAudit(@Param("audit_id") long audit_id, @Param("status") JourneyStatus status);
}
