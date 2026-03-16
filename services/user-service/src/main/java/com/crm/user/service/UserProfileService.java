package com.crm.user.service;

import com.crm.user.dto.UserProfileDto;
import com.crm.user.entity.Team;
import com.crm.user.entity.UserProfile;
import com.crm.user.event.UserEvent;
import com.crm.user.event.UserEventPublisher;
import com.crm.user.exception.DuplicateResourceException;
import com.crm.user.exception.ResourceNotFoundException;
import com.crm.user.repository.TeamRepository;
import com.crm.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final TeamRepository teamRepository;
    private final UserEventPublisher eventPublisher;

    public List<UserProfileDto.Response> findAll() {
        return userProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserProfileDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public UserProfileDto.Response findByAuthUserId(Long authUserId) {
        return toResponse(userProfileRepository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile not found for authUserId: " + authUserId)));
    }

    public List<UserProfileDto.Response> findByTeam(Long teamId) {
        return userProfileRepository.findByTeamId(teamId).stream().map(this::toResponse).toList();
    }

    public List<UserProfileDto.Response> findByRole(String role) {
        return userProfileRepository.findByCrmRole(
            com.crm.user.enums.CrmRole.valueOf(role.toUpperCase())
        ).stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserProfileDto.Response create(UserProfileDto.CreateRequest req) {
        if (userProfileRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered: " + req.email());
        }
        if (userProfileRepository.existsByAuthUserId(req.authUserId())) {
            throw new DuplicateResourceException("Profile already exists for authUserId: " + req.authUserId());
        }

        UserProfile profile = UserProfile.builder()
            .authUserId(req.authUserId())
            .fullName(req.fullName())
            .email(req.email())
            .phone(req.phone())
            .jobTitle(req.jobTitle())
            .avatarUrl(req.avatarUrl())
            .crmRole(req.crmRole())
            .team(resolveTeam(req.teamId()))
            .build();

        UserProfile saved = userProfileRepository.save(profile);
        eventPublisher.publish(buildEvent("USER_CREATED", saved));
        return toResponse(saved);
    }

    @Transactional
    public UserProfileDto.Response update(Long id, UserProfileDto.UpdateRequest req) {
        UserProfile profile = getOrThrow(id);
        if (req.fullName() != null) profile.setFullName(req.fullName());
        if (req.phone() != null) profile.setPhone(req.phone());
        if (req.jobTitle() != null) profile.setJobTitle(req.jobTitle());
        if (req.avatarUrl() != null) profile.setAvatarUrl(req.avatarUrl());
        if (req.crmRole() != null) profile.setCrmRole(req.crmRole());
        if (req.teamId() != null) profile.setTeam(resolveTeam(req.teamId()));

        UserProfile saved = userProfileRepository.save(profile);
        eventPublisher.publish(buildEvent("USER_UPDATED", saved));
        return toResponse(saved);
    }

    @Transactional
    public void deactivate(Long id) {
        UserProfile profile = getOrThrow(id);
        profile.setActive(false);
        userProfileRepository.save(profile);
        eventPublisher.publish(buildEvent("USER_DEACTIVATED", profile));
    }

    private UserProfile getOrThrow(Long id) {
        return userProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile not found: " + id));
    }

    private Team resolveTeam(Long teamId) {
        if (teamId == null) return null;
        return teamRepository.findById(teamId)
            .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
    }

    private UserEvent buildEvent(String type, UserProfile p) {
        return UserEvent.builder()
            .eventType(type)
            .userId(p.getId())
            .authUserId(p.getAuthUserId())
            .email(p.getEmail())
            .crmRole(p.getCrmRole())
            .teamId(p.getTeam() != null ? p.getTeam().getId() : null)
            .occurredAt(Instant.now())
            .build();
    }

    private UserProfileDto.Response toResponse(UserProfile p) {
        return UserProfileDto.Response.builder()
            .id(p.getId())
            .authUserId(p.getAuthUserId())
            .fullName(p.getFullName())
            .email(p.getEmail())
            .phone(p.getPhone())
            .jobTitle(p.getJobTitle())
            .avatarUrl(p.getAvatarUrl())
            .crmRole(p.getCrmRole())
            .teamId(p.getTeam() != null ? p.getTeam().getId() : null)
            .teamName(p.getTeam() != null ? p.getTeam().getName() : null)
            .active(p.isActive())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }
}
