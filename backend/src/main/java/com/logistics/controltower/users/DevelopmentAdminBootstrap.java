package com.logistics.controltower.users;

import com.logistics.controltower.security.Role;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevelopmentAdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final boolean enabled;
    private final String email;
    private final String password;

    public DevelopmentAdminBootstrap(UserRepository users, PasswordEncoder encoder,
                                     @Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
                                     @Value("${app.bootstrap-admin.email:}") String email,
                                     @Value("${app.bootstrap-admin.password:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (email.isBlank() || password.length() < 12 || password.length() > 72) {
            throw new IllegalStateException("Enabled development admin bootstrap requires an email and a 12-72 character password");
        }
        if (!users.existsByEmailIgnoreCase(email)) {
            users.save(new UserEntity(email, "Development Administrator", encoder.encode(password),
                    Set.of(Role.ADMIN)));
        }
    }
}
