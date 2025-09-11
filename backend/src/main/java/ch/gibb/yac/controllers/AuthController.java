package ch.gibb.yac.controllers;

import ch.gibb.yac.dtos.SignupDTO;
import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import ch.gibb.yac.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * The controller where logging in and signing up are handled.
 * @author Jannik Pulfer
 * @version 2.0
 * @since 2025-04-29
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    AuthenticationManager authenticationManager;

    PersonRepository personRepository;

    PasswordEncoder encoder;

    JwtUtil jwtUtils;

    public AuthController(
            AuthenticationManager authenticationManager,
            PersonRepository personRepository,
            PasswordEncoder encoder,
            JwtUtil jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.personRepository = personRepository;
        this.jwtUtils = jwtUtils;
        this.encoder = encoder;
    }

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Creates a user session by providing a jwt.
     */
    @PostMapping("/signin")
    public ResponseEntity<SignupDTO> authenticateUser(
            @RequestBody @Valid Person person, HttpServletResponse response) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                person.getUsername(), person.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails.getUsername());
        long jwtExpiration = new Date().getTime() + expiration;

        Cookie authCookie = new Cookie("JWT_Token", jwt);
        authCookie.setMaxAge((int) expiration / 1000);
        authCookie.setHttpOnly(true);
        authCookie.setPath("/");
        response.addCookie(authCookie);
        return new ResponseEntity<>(new SignupDTO(jwt, jwtExpiration), HttpStatus.OK);
    }

    /**
     * Registers a new user in the application with a generated UUID as username.
     */
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid Person person) {
        Person newUser =
                new Person(
                        null, UUID.randomUUID().toString(), encoder.encode(person.getPassword()));

        personRepository.save(newUser);
        return new ResponseEntity<>(newUser.getUsername(), HttpStatus.OK);
    }
}
