package com.aibilling.entity.repository;

import com.aibilling.common.repository.BaseRepository;
import com.aibilling.entity.model.Entity;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Entity aggregate root.
 */
@Repository
public interface EntityRepository extends BaseRepository<Entity> {
}
