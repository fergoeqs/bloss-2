package org.fergoeqs.blps1.models.securitydb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.fergoeqs.blps1.models.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", schema = "security_schema")
@Getter
@Setter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String email;
        private String password;
        private List<GrantedAuthority> authorities;
        private Role role;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder authorities(List<GrantedAuthority> authorities) {
            this.authorities = authorities;

            if (authorities != null && !authorities.isEmpty()) {
                String roleName = authorities.get(0).getAuthority()
                        .replace("ROLE_", "");
                this.role = Role.valueOf(roleName);
            }
            return this;
        }

        public User build() {
            User user = new User();
            user.setId(this.id);
            user.setEmail(this.email);
            user.setPassword(this.password);
            user.setRole(this.role);
            return user;
        }
    }}