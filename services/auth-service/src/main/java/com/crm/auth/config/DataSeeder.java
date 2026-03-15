package com.crm.auth.config;

import com.crm.auth.entity.*;
import com.crm.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner seed() {
        return args -> {
            // Permissions
            var permissions = List.of(
                    "leads:read", "leads:write",
                    "deals:read", "deals:write",
                    "analytics:read",
                    "users:manage"
            );
            permissions.forEach(name ->
                    permissionRepository.findByName(name).orElseGet(() ->
                            permissionRepository.save(Permission.builder().name(name).build())));

            // Roles
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                var all = permissionRepository.findAll();
                return roleRepository.save(Role.builder()
                        .name("ROLE_ADMIN")
                        .permissions(Set.copyOf(all))
                        .build());
            });

            roleRepository.findByName("ROLE_SALES_REP").orElseGet(() -> {
                var perms = Set.of(
                        permissionRepository.findByName("leads:read").orElseThrow(),
                        permissionRepository.findByName("leads:write").orElseThrow(),
                        permissionRepository.findByName("deals:read").orElseThrow(),
                        permissionRepository.findByName("deals:write").orElseThrow()
                );
                return roleRepository.save(Role.builder().name("ROLE_SALES_REP").permissions(perms).build());
            });

            roleRepository.findByName("ROLE_MANAGER").orElseGet(() -> {
                var perms = Set.of(
                        permissionRepository.findByName("leads:read").orElseThrow(),
                        permissionRepository.findByName("leads:write").orElseThrow(),
                        permissionRepository.findByName("deals:read").orElseThrow(),
                        permissionRepository.findByName("deals:write").orElseThrow(),
                        permissionRepository.findByName("analytics:read").orElseThrow()
                );
                return roleRepository.save(Role.builder().name("ROLE_MANAGER").permissions(perms).build());
            });

            // Default admin user
            if (!userRepository.existsByEmail("admin@crm.io")) {
                userRepository.save(User.builder()
                        .email("admin@crm.io")
                        .password(passwordEncoder.encode("Admin@1234"))
                        .fullName("CRM Admin")
                        .roles(Set.of(adminRole))
                        .build());
                log.info("Seeded default admin user: admin@crm.io");
            }
        };
    }
}
