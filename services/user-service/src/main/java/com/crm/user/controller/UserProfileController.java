package com.crm.user.controller;

import com.crm.user.dto.UserProfileDto;
import com.crm.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public List<UserProfileDto.Response> getAll(
        @RequestParam(required = false) Long teamId,
        @RequestParam(required = false) String role
    ) {
        if (teamId != null) return userProfileService.findByTeam(teamId);
        if (role != null) return userProfileService.findByRole(role);
        return userProfileService.findAll();
    }

    @GetMapping("/{id}")
    public UserProfileDto.Response getById(@PathVariable Long id) {
        return userProfileService.findById(id);
    }

    @GetMapping("/by-auth/{authUserId}")
    public UserProfileDto.Response getByAuthUserId(@PathVariable Long authUserId) {
        return userProfileService.findByAuthUserId(authUserId);
    }

    @PostMapping
    public ResponseEntity<UserProfileDto.Response> create(@Valid @RequestBody UserProfileDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.create(req));
    }

    @PatchMapping("/{id}")
    public UserProfileDto.Response update(@PathVariable Long id, @Valid @RequestBody UserProfileDto.UpdateRequest req) {
        return userProfileService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userProfileService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
