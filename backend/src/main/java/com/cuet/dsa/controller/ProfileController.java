package com.cuet.dsa.controller;

import com.cuet.dsa.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final SecurityContextHelper securityContextHelper;
    @GetMapping("/api/me/profile")
    public void getProfile() {
        Long userId = securityContextHelper.getCurrentUserId();
        System.out.println(userId);
    }
}
