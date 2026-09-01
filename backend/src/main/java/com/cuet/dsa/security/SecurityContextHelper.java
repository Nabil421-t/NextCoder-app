package com.cuet.dsa.security;

import com.cuet.dsa.entity.User;
import com.cuet.dsa.exception.UnauthorizedException;
import com.cuet.dsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityContextHelper {

    private final UserRepository userRepository;


    public static Long getCurrentUserId() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException(
                    "No authenticated user found");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new UnauthorizedException(
                    "Invalid authenticated principal");
        }

        return userDetails.getId();
    }

    public boolean isAdmin() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            return false;
        }

        return userDetails.getRole() == User.Role.ADMIN;
    }
}