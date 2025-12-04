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
    void shouldSuccessfullyAuthenticateUserWithCorrectCredentials() {
        // User Story: Login mit korrekten Daten
        // Als Benutzer möchte ich mich mit meinem Username und meinem Passwort anmelden
        String username = "550e8400-e29b-41d4-a716-446655440000";
        String password = "testpassword";
        String jwt = "test.jwt.token";
        long expiration = 86400000;

        Person person = new Person(1L, username, password);
        User userDetails = new User(username, password, java.util.List.of());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(username)).thenReturn(jwt);

        try {
            var expirationField = AuthController.class.getDeclaredField("expiration");
            expirationField.setAccessible(true);
            expirationField.set(authController, expiration);
        } catch (Exception e) {
            fail("Failed to set expiration field");
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
        assertTrue(cookie.isHttpOnly(), "Cookie should be HttpOnly for security");
    }

    @Test
    void shouldRejectAuthenticationWithIncorrectPassword() {
        // User Story: Login mit falschen Daten
        // Als Benutzer möchte ich nicht, dass man sich in meinen Account mit falschen Logindaten
        // einloggen kann
        Person person = new Person(1L, "550e8400-e29b-41d4-a716-446655440000", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(
                        new org.springframework.security.authentication.BadCredentialsException(
                                "Bad credentials"));

        assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> authController.authenticateUser(person, response));

        verify(response, never()).addCookie(any());
    }

    @Test
    void shouldRejectAuthenticationWithNonexistentUser() {
        // User Story: Login mit falschen Daten
        Person person = new Person(null, "nonexistent-uuid", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(
                        new org.springframework.security.authentication.BadCredentialsException(
                                "User not found"));

        assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> authController.authenticateUser(person, response));
    }

    @Test
    void shouldSuccessfullyRegisterNewUserWithGeneratedUUID() {
        // User Story: Registrierung
        // Als Benutzer möchte ich einen neuen Account erstellen können
        String rawPassword = "MySecurePassword123";
        String encodedPassword = "$2a$12$encodedPasswordHash";
        Person inputPerson = new Person(null, "anyName", rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> result = authController.registerUser(inputPerson);

        // Verify successful registration
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        String generatedUsername = result.getBody();

        // Verify username is UUID format (36 characters with hyphens)
        assertTrue(generatedUsername.length() == 36, "Username should be UUID format");
        assertTrue(
                generatedUsername.matches(
                        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
                "Username should be valid UUID format");

        // Verify password was encoded and saved
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertEquals(encodedPassword, savedPerson.getPassword());
        assertEquals(generatedUsername, savedPerson.getUsername());
    }

    @Test
    void shouldEncodePasswordWhenRegisteringUserAndNotStorePlaintext() {
        // Testobjekt: Password-Anforderung - mindestens 12 Zeichen
        String rawPassword = "MySecurePassword123"; // 21 characters
        String encodedPassword = "$2a$12$encodedPasswordHash";
        Person inputPerson = new Person(null, "ignored", rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authController.registerUser(inputPerson);

        // Verify password encoder was called with plaintext
        verify(passwordEncoder).encode(rawPassword);

        // Verify encoded password is saved, not plaintext
        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        Person savedPerson = personCaptor.getValue();
        assertEquals(encodedPassword, savedPerson.getPassword());
        assertNotEquals(rawPassword, savedPerson.getPassword());
    }

    @Test
    void shouldGenerateUniqueUUIDForEachRegistration() {
        // User Story: Registrierung - UUID wird zufällig generiert
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(personRepository.save(any(Person.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Person person1 = new Person(null, "user1", "password123456");
        Person person2 = new Person(null, "user2", "password123456");
        Person person3 = new Person(null, "user3", "password123456");

        ResponseEntity<String> result1 = authController.registerUser(person1);
        ResponseEntity<String> result2 = authController.registerUser(person2);
        ResponseEntity<String> result3 = authController.registerUser(person3);

        String username1 = result1.getBody();
        String username2 = result2.getBody();
        String username3 = result3.getBody();

        // Verify all usernames are different UUIDs
        assertNotEquals(username1, username2);
        assertNotEquals(username2, username3);
        assertNotEquals(username1, username3);
    }
}
