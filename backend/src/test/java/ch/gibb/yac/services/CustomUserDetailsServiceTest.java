package ch.gibb.yac.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Tests for CustomUserDetailsService to verify user loading functionality.
 */
public class CustomUserDetailsServiceTest {

    private CustomUserDetailsService customUserDetailsService;

    @Mock private PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customUserDetailsService = new CustomUserDetailsService(personRepository);
    }

    @Test
    void shouldSuccessfullyLoadUserByUsername() {
        String username = "testuser";
        String password = "hashedPassword123";
        Person testPerson = new Person(1L, username, password);

        when(personRepository.findByUsername(username)).thenReturn(testPerson);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        verify(personRepository).findByUsername(username);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String username = "nonexistentuser";

        when(personRepository.findByUsername(username)).thenReturn(null);

        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> customUserDetailsService.loadUserByUsername(username));

        assertTrue(exception.getMessage().contains("User Not Found"));
        assertTrue(exception.getMessage().contains(username));
    }

    @Test
    void shouldLoadUserWithCorrectAuthorities() {
        String username = "testuser";
        String password = "hashedPassword123";
        Person testPerson = new Person(1L, username, password);

        when(personRepository.findByUsername(username)).thenReturn(testPerson);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails.getAuthorities());
        // Person model has no authorities, so it should be empty
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void shouldLoadMultipleDifferentUsers() {
        String username1 = "user1";
        String username2 = "user2";
        String password1 = "password1";
        String password2 = "password2";

        Person person1 = new Person(1L, username1, password1);
        Person person2 = new Person(2L, username2, password2);

        when(personRepository.findByUsername(username1)).thenReturn(person1);
        when(personRepository.findByUsername(username2)).thenReturn(person2);

        UserDetails userDetails1 = customUserDetailsService.loadUserByUsername(username1);
        UserDetails userDetails2 = customUserDetailsService.loadUserByUsername(username2);

        assertEquals(username1, userDetails1.getUsername());
        assertEquals(password1, userDetails1.getPassword());
        assertEquals(username2, userDetails2.getUsername());
        assertEquals(password2, userDetails2.getPassword());

        verify(personRepository).findByUsername(username1);
        verify(personRepository).findByUsername(username2);
    }

    @Test
    void shouldLoadUserWithSpecialCharactersInUsername() {
        String username = "user@example.com";
        String password = "hashedPassword123";
        Person testPerson = new Person(1L, username, password);

        when(personRepository.findByUsername(username)).thenReturn(testPerson);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertEquals(username, userDetails.getUsername());
        verify(personRepository).findByUsername(username);
    }
}
