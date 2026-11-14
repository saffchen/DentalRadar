package com.dentalradar.auth;

import com.dentalradar.dto.AuthResponse;
import com.dentalradar.dto.LoginRequest;
import com.dentalradar.dto.RegisterRequest;
import com.dentalradar.exception.ConflictException;
import com.dentalradar.exception.UnauthorizedException;
import com.dentalradar.model.Clinic;
import com.dentalradar.model.Role;
import com.dentalradar.model.UserAccount;
import com.dentalradar.repository.ClinicRepository;
import com.dentalradar.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            ClinicRepository clinicRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.clinicRepository = clinicRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Этот email уже зарегистрирован");
        }
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setDisplayName(request.displayName().trim());
        userRepository.save(user);
        if (request.role() == Role.CLINIC) {
            Clinic clinic = new Clinic();
            clinic.setId(UUID.randomUUID().toString());
            clinic.setOwnerUserId(user.getId());
            clinic.setName(user.getDisplayName());
            clinic.setDescription("");
            clinic.setAddress("");
            clinicRepository.save(clinic);
        }
        return token(user);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Неверный email или пароль"));
        return token(user);
    }

    private AuthResponse token(UserAccount user) {
        return new AuthResponse(
                jwtService.createToken(user),
                user.getRole().name(),
                user.getDisplayName(),
                user.getId()
        );
    }
}
