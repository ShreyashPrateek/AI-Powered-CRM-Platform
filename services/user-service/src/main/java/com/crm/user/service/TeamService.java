package com.crm.user.service;

import com.crm.user.dto.TeamDto;
import com.crm.user.entity.Department;
import com.crm.user.entity.Team;
import com.crm.user.exception.DuplicateResourceException;
import com.crm.user.exception.ResourceNotFoundException;
import com.crm.user.repository.DepartmentRepository;
import com.crm.user.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;

    public List<TeamDto.Response> findAll() {
        return teamRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<TeamDto.Response> findByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId).stream().map(this::toResponse).toList();
    }

    public TeamDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public TeamDto.Response create(TeamDto.CreateRequest req) {
        if (teamRepository.existsByName(req.name())) {
            throw new DuplicateResourceException("Team already exists: " + req.name());
        }
        Department dept = departmentRepository.findById(req.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.departmentId()));

        Team team = Team.builder()
            .name(req.name())
            .description(req.description())
            .department(dept)
            .managerAuthId(req.managerAuthId())
            .build();
        return toResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamDto.Response update(Long id, TeamDto.UpdateRequest req) {
        Team team = getOrThrow(id);
        if (req.name() != null) team.setName(req.name());
        if (req.description() != null) team.setDescription(req.description());
        if (req.managerAuthId() != null) team.setManagerAuthId(req.managerAuthId());
        if (req.departmentId() != null) {
            Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.departmentId()));
            team.setDepartment(dept);
        }
        return toResponse(teamRepository.save(team));
    }

    @Transactional
    public void delete(Long id) {
        teamRepository.delete(getOrThrow(id));
    }

    private Team getOrThrow(Long id) {
        return teamRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    private TeamDto.Response toResponse(Team t) {
        return TeamDto.Response.builder()
            .id(t.getId())
            .name(t.getName())
            .description(t.getDescription())
            .departmentId(t.getDepartment().getId())
            .departmentName(t.getDepartment().getName())
            .managerAuthId(t.getManagerAuthId())
            .memberCount(t.getMembers().size())
            .createdAt(t.getCreatedAt())
            .updatedAt(t.getUpdatedAt())
            .build();
    }
}
