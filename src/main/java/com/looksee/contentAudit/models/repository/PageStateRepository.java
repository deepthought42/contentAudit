package com.looksee.contentAudit.models.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.looksee.contentAudit.models.PageState;

import io.github.resilience4j.retry.annotation.Retry;

/**
 * 
 */
@Repository
@Retry(name = "neoforj")
public interface PageStateRepository extends Neo4jRepository<PageState, Long> {
	@Query("MATCH (:Account{username:$user_id})-[*]->(p:PageState{key:$key}) RETURN p LIMIT 1")
	public PageState findByKeyAndUsername(@Param("user_id") String user_id, @Param("key") String key);

	@Query("MATCH (p:PageState{key:$key}) RETURN p LIMIT 1")
	public PageState findByKey(@Param("key") String key);

	@Deprecated
	@Query("MATCH (:Account{username:$user_id})-[]->(d:Domain{url:$url}) MATCH (d)-[]->(p:PageState) MATCH a=(p)-[h:HAS]->() WHERE $screenshot_checksum IN p.screenshot_checksums RETURN a")
	public List<PageState> findByScreenshotChecksumsContainsForUserAndDomain(@Param("user_id") String user_id, @Param("url") String url, @Param("screenshot_checksum") String checksum );
	
	@Query("MATCH (p:PageState{url:$url})-[h:HAS]->() WHERE $screenshot_checksum IN p.screenshot_checksums RETURN a")
	public List<PageState> findByScreenshotChecksumAndPageUrl(@Param("url") String url, @Param("screenshot_checksum") String checksum );
	
	@Query("MATCH (p:PageState{full_page_checksum:$screenshot_checksum}) MATCH a=(p)-[h:HAS_CHILD]->() RETURN a")
	public List<PageState> findByFullPageScreenshotChecksum(@Param("screenshot_checksum") String checksum );

	@Query("MATCH (:Account{username:$user_id})-[*]->(p:PageState{key:$page_key}) WHERE $screenshot_checksum IN p.animated_image_checksums RETURN p LIMIT 1")
	public PageState findByAnimationImageChecksum(@Param("user_id") String user_id, @Param("screenshot_checksum") String screenshot_checksum);

	@Query("MATCH (a:Account)-[]->(d:Domain{url:$url}) MATCH (d)-[]->(p:PageState) MATCH (p)-[:HAS]->(f:Form{key:$form_key}) WHERE id(account)=$account_id RETURN p")
	public List<PageState> findPageStatesWithForm(@Param("account_id") long account_id, @Param("url") String url, @Param("form_key") String form_key);

	@Query("MATCH (d:Domain{url:$url})-[:HAS]->(ps:PageState{src_checksum:$src_checksum}) MATCH a=(ps)-[h:HAS]->() RETURN a")
	public List<PageState> findBySourceChecksumForDomain(@Param("url") String url, @Param("src_checksum") String src_checksum);

	@Query("ps:PageState{key:$page_state_key}) return p LIMIT 1")
	public PageState getParentPage(@Param("page_state_key") String page_state_key);

	@Query("MATCH (p:PageState{url:$url}) RETURN p ORDER BY p.created_at DESC LIMIT 1")
	public PageState findByUrl(@Param("url") String url);

	@Query("MATCH (ps:PageState) WHERE id(ps)=$id SET ps.fullPageScreenshotUrlComposite = $composite_img_url RETURN ps")
	public void updateCompositeImageUrl(@Param("id") long id, @Param("composite_img_url") String composite_img_url);

	@Query("MATCH (p:PageState) MATCH (element:ElementState) WHERE id(p)=$page_state_id AND id(element) IN $element_id_list MERGE (p)-[:HAS]->(element) RETURN COUNT(element)")
	public long addAllElements(@Param("page_state_id") long page_state_id, @Param("element_id_list") List<Long> element_id_list);

	@Query("MATCH (p:PageState) MATCH (element:ElementState) WHERE id(p)=$page_state_id AND id(element) IN $element_id_list MERGE (p)-[:HAS]->(element) RETURN element")
	public Set<PageState> getPageStatesForDomainAuditRecord(@Param("domain_audit_id") long domain_audit_id);
	
	@Query("MATCH (domain_audit:DomainAuditRecord) with domain_audit WHERE id(domain_audit)=$domain_audit_id MATCH (domain_audit)-[]->(page_audit:PageAuditRecord) MATCH (page_audit)-[]->(page_state:PageState) WHERE id(page_state)=$page_state_id RETURN page_state")
	public PageState findByDomainAudit(@Param("domain_audit_id") long domainAuditRecordId, @Param("page_state_id") long page_state_id);
	
	@Query("MATCH (s:Step) WHERE id(s)=$step_id MATCH (s)-[:ENDS_WITH]->(p:PageState) RETURN p")
	public PageState getEndPageForStep(@Param("step_id") long id);

	@Query("MATCH (page_audit:PageAuditRecord)-[]->(page_state:PageState) WHERE id(page_audit)=$id RETURN page_state")
    public PageState findByAuditRecordId(@Param("id") long pageAuditId);
}
