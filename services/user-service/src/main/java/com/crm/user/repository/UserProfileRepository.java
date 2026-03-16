package com.crm.user.repository;

import com.crm.user.entity.UserProfile;
import com.crm.user.enums.CrmRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByAuthUserId(Long authUserId);
    Optional<UserProfile> findByEmail(String email);
    List<UserProfile> findByTeamId(Long teamId);
    List<UserProfile> findByCrmRole(CrmRole crmRole);
    boolean existsByEmail(String email);
    boolean existsByAuthUserId(Long authUserId);
}
