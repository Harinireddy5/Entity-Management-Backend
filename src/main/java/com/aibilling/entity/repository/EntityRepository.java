package com.aibilling.entity.repository;

import com.aibilling.common.repository.BaseRepository;
import com.aibilling.entity.model.Entity;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for Entity aggregate root.
 */
@Repository
public interface EntityRepository extends BaseRepository<Entity> {

    @Query(value = "SELECT e FROM Entity e LEFT JOIN e.details ed " +
            "WHERE (:query IS NULL OR :query = '' " +
            "  OR LOWER(ed.organizationName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "  OR LOWER(ed.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "  OR EXISTS (SELECT 1 FROM Account a WHERE a.entity = e AND LOWER(a.accountName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "  OR EXISTS (SELECT 1 FROM Account a JOIN Contact c ON c.account = a WHERE a.entity = e AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')))) " +
            ")")
    Page<Entity> searchGlobalEntities(@Param("query") String query, Pageable pageable);
}
