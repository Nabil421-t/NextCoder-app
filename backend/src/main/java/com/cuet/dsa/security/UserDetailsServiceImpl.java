package com.cuet.dsa.security;

import com.cuet.dsa.entity.Session;
import com.cuet.dsa.entity.User;
import com.cuet.dsa.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * LOGIN FLOW (email/password)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + email));

        return mapToUserDetails(user);
    }

    /**
     * JWT FLOW (userId)
     */
    @Transactional(readOnly = true)
    public CustomUserDetails loadUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + userId));

        return mapToUserDetails(user);
    }
    @Transactional(readOnly = true)
    public CustomUserDetails  loadUserBySession(Session session) {
        User user = session.getUser();
        return mapToUserDetails(user);

    }
    /**
     * Mapper → converts DB User → Security User
     */
    private CustomUserDetails mapToUserDetails(User user) {

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );
    }
}