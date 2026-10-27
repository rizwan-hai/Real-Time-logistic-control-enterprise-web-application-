package com.logistics.controltower.users;

import com.logistics.controltower.audit.AuditService;
import com.logistics.controltower.security.Role;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder, AuditService audit) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Page<UserDtos.UserView> list(Pageable pageable) {
        return users.findAll(pageable).map(UserDtos.UserView::from);
    }

    @Transactional
    public UserDtos.UserView create(UserDtos.CreateUserRequest input) {
        String email = input.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        UserEntity user = users.save(new UserEntity(email, input.displayName(),
                passwordEncoder.encode(input.password()), input.roles()));
        audit.record("USER_CREATED", "SUCCESS", "USER", user.getId(), "roles=" + user.getRoles());
        return UserDtos.UserView.from(user);
    }

    @Transactional(readOnly = true)
    public UserDtos.UserView getByEmail(String email) {
        return UserDtos.UserView.from(find(email));
    }

    @Transactional
    public UserDtos.UserView updateProfile(String email, String displayName) {
        UserEntity user = find(email);
        user.updateProfile(displayName);
        return UserDtos.UserView.from(user);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        UserEntity user = find(email);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        user.changePassword(passwordEncoder.encode(newPassword));
        audit.record("PASSWORD_CHANGED", "SUCCESS", "USER", user.getId(), null);
    }

    @Transactional
    public void setActive(long id, boolean active) {
        UserEntity user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!active && user.getRoles().contains(Role.ADMIN)
                && users.findAll().stream().filter(UserEntity::isActive)
                .filter(candidate -> candidate.getRoles().contains(Role.ADMIN)).count() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot deactivate the last active administrator");
        }
        user.setActive(active);
        audit.record(active ? "ACCOUNT_ACTIVATED" : "ACCOUNT_DEACTIVATED",
                "SUCCESS", "USER", id, null);
    }

    @Transactional
    public void updateRoles(long id, Set<Role> roles) {
        UserEntity user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRoles().contains(Role.ADMIN) && !roles.contains(Role.ADMIN)
                && users.findAll().stream().filter(UserEntity::isActive)
                .filter(candidate -> candidate.getRoles().contains(Role.ADMIN)).count() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove the last active administrator");
        }
        user.setRoles(roles);
        audit.record("USER_ROLES_CHANGED", "SUCCESS", "USER", id, "roles=" + roles);
    }

    private UserEntity find(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
