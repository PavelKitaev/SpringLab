package com.example.taskmanager.service;

import com.example.taskmanager.dto.JwtResponseDTO;
import com.example.taskmanager.dto.LoginRequestDTO;
import com.example.taskmanager.dto.SignupRequestDTO;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.RoleName;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.RoleRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.config.security.JwtUtils;
import com.example.taskmanager.config.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public JwtResponseDTO authenticateUser(LoginRequestDTO loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Обновляем время последнего входа
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        logger.info("User {} authenticated successfully", loginRequest.getUsername());

        return new JwtResponseDTO(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    @Transactional
    public void registerUser(SignupRequestDTO signupRequest) {
        logger.info("Registering new user: {}", signupRequest.getUsername());

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            logger.error("Username {} is already taken", signupRequest.getUsername());
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            logger.error("Email {} is already in use", signupRequest.getEmail());
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Создаем нового пользователя
        User user = new User(signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()));

        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> {
                    logger.error("ROLE_USER not found in database");
                    return new RuntimeException("Error: Role USER is not found.");
                });
        roles.add(userRole);

        // Первому пользователю даем роль ADMIN
        if (userRepository.count() == 0) {
            logger.info("First user registration - assigning ADMIN role");
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> {
                        logger.error("ROLE_ADMIN not found in database");
                        return new RuntimeException("Error: Role ADMIN is not found.");
                    });
            roles.add(adminRole);
        }

        user.setRoles(roles);
        userRepository.save(user);

        logger.info("User {} registered successfully with roles: {}",
                signupRequest.getUsername(),
                roles.stream().map(r -> r.getName().name()).collect(Collectors.joining(", ")));
    }
}