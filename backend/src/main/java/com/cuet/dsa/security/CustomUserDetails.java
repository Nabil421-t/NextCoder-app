package com.cuet.dsa.security;

import com.cuet.dsa.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String username;
    private final String password;
    private final User.Role role;

    // ── AUTHORITY (ROLE BASED SECURITY) ─────────────────────
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    // ── LOGIN IDENTIFIER ────────────────────────────────────
    // Since you login using EMAIL
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    // ── ACCOUNT STATUS ──────────────────────────────────────
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


    // ── EXTRA HELPER METHODS (VERY IMPORTANT) ───────────────

    public boolean isAdmin() {
        return role == User.Role.ADMIN;
    }

    public boolean isUser() {
        return role == User.Role.USER;
    }
}
