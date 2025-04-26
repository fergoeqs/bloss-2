package org.fergoeqs.blps1.services;


import org.fergoeqs.blps1.dto.AuthResponse;
import org.fergoeqs.blps1.dto.LoginRequest;
import org.fergoeqs.blps1.dto.RegisterRequest;
import org.fergoeqs.blps1.models.applicantdb.Applicant;
import org.fergoeqs.blps1.models.employerdb.Employer;
import org.fergoeqs.blps1.models.enums.Role;
import org.fergoeqs.blps1.models.securitydb.User;
import org.fergoeqs.blps1.repositories.applicantdb.ApplicantRepository;
import org.fergoeqs.blps1.repositories.employerdb.EmployerRepository;
import org.fergoeqs.blps1.repositories.securitydb.UserRepository;
import org.fergoeqs.blps1.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployerRepository employerRepository;
    private final ApplicantRepository applicantRepository;
    private final TransactionService transactionService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager, EmployerRepository employerRepository,
            ApplicantRepository applicantRepository, TransactionService transactionService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.employerRepository = employerRepository;
        this.applicantRepository = applicantRepository;
        this.transactionService = transactionService;
    }

    public AuthResponse register(RegisterRequest request) {
        return transactionService.execute(
                "userRegistrationTx",
                30,
                (status) -> {
                    if (userRepository.findByEmail(request.email()).isPresent()) {
                        throw new IllegalArgumentException("User with email " + request.email() + " already exists");
                    }

                    User user = new User();
                    user.setEmail(request.email());
                    user.setPassword(passwordEncoder.encode(request.password()));
                    user.setName(request.name());
                    user.setRole(request.role());
                    User savedUser = userRepository.save(user);

                    if (request.role() == Role.EMPLOYER_CREATOR || request.role() == Role.EMPLOYER_REVIEWER) {
                        validateEmployerRequest(request);
                        Employer employer = createEmployer(request, savedUser.getId());
                        employerRepository.save(employer);
                    } else if (request.role() == Role.USER) {
                        Applicant applicant = createApplicant(request, savedUser.getId());
                        applicantRepository.save(applicant);
                    }

                    String jwtToken = jwtService.generateToken(user);
                    return new AuthResponse(jwtToken);
                }
        );
    }

    private void validateEmployerRequest(RegisterRequest request) {
        if (request.companyName() == null || request.contactInfo() == null) {
            throw new IllegalArgumentException("Company name and contact info are required for employer roles");
        }
    }

    private Employer createEmployer(RegisterRequest request, Long userId) {
        Employer employer = new Employer();
        employer.setCompanyName(request.companyName());
        employer.setContactInfo(request.contactInfo());
        employer.setUserId(userId);
        employer.setRole(request.role());
        return employer;
    }

    private Applicant createApplicant(RegisterRequest request, Long userId) {
        Applicant applicant = new Applicant();
        applicant.setName(request.name());
        applicant.setContactInfo(request.contactInfo());
        applicant.setUserId(userId);
        return applicant;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
            User user = userRepository.findByEmail(request.email()).orElseThrow();
            String jwtToken = jwtService.generateToken(user);
            return new AuthResponse(jwtToken);
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }
}