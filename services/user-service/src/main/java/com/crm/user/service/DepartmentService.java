package com.crm.user.service;

import com.crm.user.dto.DepartmentDto;
import com.crm.user.entity.Department;
import com.crm.user.exception.DuplicateResourceException;
import com.crm.user.exception.ResourceNotFoundException;
import com.crm.user.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentDto.Response> findAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DepartmentDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public DepartmentDto.Response create(DepartmentDto.CreateRequest req) {
        if (departmentRepository.existsByName(req.name())) {
            throw new DuplicateResourceException("Department already exists: " + req.name());
        }
        Department dept = Department.builder()
            .name(req.name())
            .description(req.description())
            .build();
        return toResponse(departmentRepository.save(dept));
    }

    @Transactional
    public DepartmentDto.Response update(Long id, DepartmentDto.UpdateRequest req) {
        Department dept = getOrThrow(id);
        if (req.name() != null) dept.setName(req.name());
        if (req.description() != null) dept.setDescription(req.description());
        return toResponse(departmentRepository.save(dept));
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.delete(getOrThrow(id));
    }

    private Department getOrThrow(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private DepartmentDto.Response toResponse(Department d) {
        return DepartmentDto.Response.builder()
            .id(d.getId())
            .name(d.getName())
            .description(d.getDescription())
            .teamCount(d.getTeams().size())
            .createdAt(d.getCreatedAt())
            .updatedAt(d.getUpdatedAt())
            .build();
    }
}
