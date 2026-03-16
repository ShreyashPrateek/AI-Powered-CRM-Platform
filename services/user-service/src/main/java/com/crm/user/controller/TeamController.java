package com.crm.user.controller;

import com.crm.user.dto.TeamDto;
import com.crm.user.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public List<TeamDto.Response> getAll(@RequestParam(required = false) Long departmentId) {
        return departmentId != null
            ? teamService.findByDepartment(departmentId)
            : teamService.findAll();
    }

    @GetMapping("/{id}")
    public TeamDto.Response getById(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TeamDto.Response> create(@Valid @RequestBody TeamDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(req));
    }

    @PatchMapping("/{id}")
    public TeamDto.Response update(@PathVariable Long id, @Valid @RequestBody TeamDto.UpdateRequest req) {
        return teamService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
