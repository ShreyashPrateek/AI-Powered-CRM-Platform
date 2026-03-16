package com.crm.user.repository;

import com.crm.user.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByDepartmentId(Long departmentId);
    Optional<Team> findByName(String name);
    boolean existsByName(String name);
}
