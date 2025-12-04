package ch.gibb.yac.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ch.gibb.yac.dtos.SignupDTO;
import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import ch.gibb.yac.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests for AuthController to verify sign-in and sign-up functionality.
 */
public class AuthControllerTest {

    private AuthController authController;

    @Mock private AuthenticationManager authenticationManager;
    @Mock private PersonRepository personRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController =
                new AuthController(
                        authenticationManager, personRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void shouldSuccessfullyAuthenticateUserAndReturnJwt() {
        String username = "testuser";
        String password = "testpassword";
        String jwt = "test.jwt.token";
        long expiration = 86400000;

        Person person = new Person(null, username, password);
        User userDetails = new User(username, password, java.util.List.of());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(username)).thenReturn(jwt);

        // Mock the reflection access to expiration field
        try {
            var expirationField = AuthController.class.getDeclaredField("expiration");
            expirationField.setAccessible(true);
            expirationField.set(authController, expiration);
        } catch (Exception e) {
            fail("Failed to set expiration field: " + e.getMessage());
        }

        ResponseEntity<SignupDTO> result = authController.authenticateUser(person, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(jwt, result.getBody().jwt());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("JWT_Token", cookie.getName());
        assertEquals(jwt, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationFails() {
        Person person = new Person(null, "wronguser", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(
                        new org.springframework.security.authentication.BadCredentialsException(
                                "Bad credentials"));

        assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> authController.authenticateUser(person, response));
    }

    @Test
    void shouldSuccessfullyRegisterNewUser() {
        String password = "encodedPassword123";
        Person inputPerson = new Person(null, "inputusername", "rawpassword");

        when(passwordEncoder.encode("rawpassword")).thenReturn(password);
        when(personRepository.save(any(Person.class)))
                .thenAnswer(
                        invocation -> {
                            Person savedPerson = invocation.getArgument(0);
                            return savedPerson;
                        });

        ResponseEntity<String> result = authController.registerUser(inputPerson);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        // Username should be a UUID
        assertTrue(result.getBody().length() > 0);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertEquals(password, savedPerson.getPassword());
    }

    @Test
    void shouldEncodePasswordWhenRegisteringUser() {
        String rawPassword = "MySecurePassword123";
        String encodedPassword = "$2a$12$encodedPasswordHash";
        Person inputPerson = new Person(null, "user", rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authController.registerUser(inputPerson);

        verify(passwordEncoder).encode(rawPassword);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertEquals(encodedPassword, savedPerson.getPassword());
    }

    @Test
    void shouldGenerateUniqueUsernamesForDifferentRegistrations() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Person person1 = new Person(null, "ignored1", "password");
        Person person2 = new Person(null, "ignored2", "password");

        ResponseEntity<String> result1 = authController.registerUser(person1);
        ResponseEntity<String> result2 = authController.registerUser(person2);

        String username1 = result1.getBody();
        String username2 = result2.getBody();

        assertNotEquals(username1, username2);
    }
}
