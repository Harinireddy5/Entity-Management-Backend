package com.aibilling.account.repository;

import com.aibilling.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    List<Account> findByEntityIdAndStatusNot(UUID entityId, com.aibilling.common.enums.Status status);
    
    long countByEntityIdAndStatusNot(UUID entityId, com.aibilling.common.enums.Status status);
}
