package ch.gibb.yac.controllers;

import ch.gibb.yac.dtos.SignupDTO;
import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import ch.gibb.yac.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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

    @Value("${jwt.expiration}")
    private long expiration;

    @PostMapping("/signin")
    public ResponseEntity<SignupDTO> authenticateUser(@RequestBody @Valid Person person) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        person.getUsername(),
                        person.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new ResponseEntity<>(new SignupDTO(jwtUtils.generateToken(userDetails.getUsername()), new Date().getTime() + expiration), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody @Valid Person person) {
        if (personRepository.existsByUsername(person.getUsername())) {
            return new ResponseEntity<>("Error: Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        Person newUser = new Person(
                null,
                UUID.randomUUID().toString(),
                encoder.encode(person.getPassword())
        );

        personRepository.save(newUser);
        return new ResponseEntity<>(newUser.getUsername(), HttpStatus.OK);
    }
}