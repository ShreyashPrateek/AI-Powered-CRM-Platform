package com.crm.user.controller;

import com.crm.user.dto.DepartmentDto;
import com.crm.user.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentDto.Response> getAll() {
        return departmentService.findAll();
    }

    @GetMapping("/{id}")
    public DepartmentDto.Response getById(@PathVariable Long id) {
        return departmentService.findById(id);
    }

    @PostMapping
    public ResponseEntity<DepartmentDto.Response> create(@Valid @RequestBody DepartmentDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(req));
    }

    @PatchMapping("/{id}")
    public DepartmentDto.Response update(@PathVariable Long id, @Valid @RequestBody DepartmentDto.UpdateRequest req) {
        return departmentService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
