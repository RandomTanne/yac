package ch.gibb.yac.controllers;

import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import ch.gibb.yac.security.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    AuthenticationManager authenticationManager;

    PersonRepository personRepository;

    PasswordEncoder encoder;

    JwtUtil jwtUtils;

    public AuthController(AuthenticationManager authenticationManager, PersonRepository personRepository, PasswordEncoder encoder, JwtUtil jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.personRepository = personRepository;
        this.jwtUtils = jwtUtils;
        this.encoder = encoder;
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody Person person) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        person.getUsername(),
                        person.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtils.generateToken(userDetails.getUsername());
    }

    @PostMapping("/signup")
    public String registerUser(@RequestBody Person person) {
        if (personRepository.existsByUsername(person.getUsername())) {
            return "Error: Username is already taken!";
        }

        Person newUser = new Person(
                null,
                UUID.randomUUID().toString(),
                encoder.encode(person.getPassword())
        );

        personRepository.save(newUser);
        return newUser.getUsername();
    }
}