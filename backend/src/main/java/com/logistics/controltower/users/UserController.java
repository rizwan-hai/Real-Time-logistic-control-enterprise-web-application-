package com.logistics.controltower.users;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService users;

    public UserController(UserService users) { this.users = users; }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDtos.UserView> list(Pageable pageable) { return users.list(pageable); }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDtos.UserView create(@Valid @RequestBody UserDtos.CreateUserRequest request) {
        return users.create(request);
    }

    @PatchMapping("/users/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public void setActive(@PathVariable long id, @RequestParam boolean active) {
        users.setActive(id, active);
    }

    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public void updateRoles(@PathVariable long id, @Valid @RequestBody UserDtos.RoleUpdateRequest request) {
        users.updateRoles(id, request.roles());
    }

    @GetMapping("/auth/me")
    public UserDtos.UserView current(Authentication authentication) {
        return users.getByEmail(authentication.getName());
    }

    @PutMapping("/auth/me")
    public UserDtos.UserView updateProfile(Authentication authentication,
                                           @Valid @RequestBody UserDtos.ProfileRequest request) {
        return users.updateProfile(authentication.getName(), request.displayName());
    }

    @PostMapping("/auth/change-password")
    public void changePassword(Authentication authentication,
                               @Valid @RequestBody UserDtos.PasswordChangeRequest request) {
        users.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
    }
}
