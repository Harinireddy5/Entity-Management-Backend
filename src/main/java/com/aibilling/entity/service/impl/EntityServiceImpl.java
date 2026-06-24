package com.aibilling.entity.service.impl;

import com.aibilling.common.enums.Status;
import com.aibilling.common.service.impl.BaseServiceImpl;
import com.aibilling.entity.model.Entity;
import com.aibilling.entity.model.EntityCategory;
import com.aibilling.entity.model.EntityDetails;
import com.aibilling.entity.model.EntityTypeMapping;
import com.aibilling.entity.repository.EntityRepository;
import com.aibilling.entity.service.EntityService;
import com.aibilling.exception.ResourceNotFoundException;
import com.aibilling.setup.model.EntityType;
import com.aibilling.setup.repository.EntityTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing Entity aggregate roots.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class EntityServiceImpl extends BaseServiceImpl<Entity> implements EntityService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EntityServiceImpl.class);

    private final EntityRepository entityRepository;
    private final EntityTypeRepository entityTypeRepository;

    public EntityServiceImpl(EntityRepository entityRepository, EntityTypeRepository entityTypeRepository) {
        super(entityRepository, "Entity");
        this.entityRepository = entityRepository;
        this.entityTypeRepository = entityTypeRepository;
    }

    @Override
    @Transactional
    public Entity create(Entity entity) {
        throw new UnsupportedOperationException("Entity creation requires type codes. Use create(entity, entityTypeCodes) instead.");
    }

    @Override
    @Transactional
    public Entity update(UUID id, Entity entity) {
        throw new UnsupportedOperationException("Entity update requires type codes. Use update(id, entity, entityTypeCodes) instead.");
    }

    @Override
    @Transactional
    public Entity create(Entity entity, List<String> entityTypeCodes) {
        log.info("Creating entity with category: {} and types: {}", entity.getEntityCategory(), entityTypeCodes);

        entity.setStatus(Status.ACTIVE);

        // Link details bidirectional mapping
        if (entity.getDetails() != null) {
            entity.getDetails().setEntity(entity);
        }

        // Map and link entity types
        if (entityTypeCodes != null && !entityTypeCodes.isEmpty()) {
            entity.setEntityTypes(new HashSet<>());
            for (String code : entityTypeCodes) {
                EntityType entityType = entityTypeRepository.findByCodeAndStatus(code, Status.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException("EntityType", "code", code));

                EntityTypeMapping mapping = new EntityTypeMapping();
                mapping.setEntityType(entityType);
                mapping.setStatus(Status.ACTIVE);
                entity.addEntityType(mapping);
            }
        }

        Entity saved = entityRepository.save(entity);
        log.info("Entity created with id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Entity update(UUID id, Entity updatedEntityData, List<String> entityTypeCodes) {
        log.info("Updating entity id={} with category: {} and types: {}", id, updatedEntityData.getEntityCategory(), entityTypeCodes);

        Entity existing = findById(id);

        existing.setEntityCategory(updatedEntityData.getEntityCategory());
        existing.setStatus(updatedEntityData.getStatus());

        // Update details based on category
        EntityDetails existingDetails = existing.getDetails();
        EntityDetails newDetails = updatedEntityData.getDetails();

        if (existingDetails == null) {
            existingDetails = new EntityDetails();
            existingDetails.setEntity(existing);
            existing.setDetails(existingDetails);
        }

        if (updatedEntityData.getEntityCategory() == EntityCategory.ORGANIZATION) {
            existingDetails.setFullName(null);
            existingDetails.setIdentificationType(null);
            existingDetails.setIdentificationNumber(null);
            if (newDetails != null) {
                existingDetails.setOrganizationName(newDetails.getOrganizationName());
                existingDetails.setTin(newDetails.getTin());
            }
        } else if (updatedEntityData.getEntityCategory() == EntityCategory.PERSON) {
            existingDetails.setOrganizationName(null);
            existingDetails.setTin(null);
            if (newDetails != null) {
                existingDetails.setFullName(newDetails.getFullName());
                existingDetails.setIdentificationType(newDetails.getIdentificationType());
                existingDetails.setIdentificationNumber(newDetails.getIdentificationNumber());
            }
        }

        // Update entity types
        existing.getEntityTypes().clear();
        if (entityTypeCodes != null && !entityTypeCodes.isEmpty()) {
            for (String code : entityTypeCodes) {
                EntityType entityType = entityTypeRepository.findByCodeAndStatus(code, Status.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException("EntityType", "code", code));

                EntityTypeMapping mapping = new EntityTypeMapping();
                mapping.setEntityType(entityType);
                mapping.setStatus(Status.ACTIVE);
                existing.addEntityType(mapping);
            }
        }

        Entity saved = entityRepository.save(existing);
        log.info("Entity updated with id={}", saved.getId());
        return saved;
    }
}
