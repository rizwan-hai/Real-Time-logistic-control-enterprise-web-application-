package com.logistics.controltower.users;

import com.logistics.controltower.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

public final class UserDtos {
    private UserDtos() {}

    public record UserView(Long id, String email, String displayName, Set<Role> roles,
                           boolean active, Instant createdAt) {
        static UserView from(UserEntity user) {
            return new UserView(user.getId(), user.getEmail(), user.getDisplayName(),
                    user.getRoles(), user.isActive(), user.getCreatedAt());
        }
    }

    public record CreateUserRequest(@Email @NotBlank String email,
                                    @NotBlank @Size(max = 100) String displayName,
                                    @NotBlank @Size(min = 12, max = 72) String password,
                                    @NotEmpty Set<Role> roles) {}

    public record ProfileRequest(@NotBlank @Size(max = 100) String displayName) {}
    public record PasswordChangeRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 12, max = 72) String newPassword) {}
    public record RoleUpdateRequest(@NotEmpty Set<Role> roles) {}
}
