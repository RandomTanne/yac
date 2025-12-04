package ch.gibb.yac.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Person model to verify basic functionality and UserDetails implementation.
 */
public class PersonTest {

    @Test
    void shouldCreatePersonWithAllParameters() {
        Long id = 1L;
        String username = "testuser";
        String password = "hashedPassword123";

        Person person = new Person(id, username, password);

        assertEquals(username, person.getUsername());
        assertEquals(password, person.getPassword());
    }

    @Test
    void shouldCreatePersonWithDefaultConstructor() {
        Person person = new Person();

        assertNotNull(person);
    }

    @Test
    void shouldReturnCorrectUsername() {
        String username = "testuser";
        Person person = new Person(1L, username, "password");

        assertEquals(username, person.getUsername());
    }

    @Test
    void shouldReturnCorrectPassword() {
        String password = "hashedPassword123";
        Person person = new Person(1L, "testuser", password);

        assertEquals(password, person.getPassword());
    }

    @Test
    void shouldReturnEmptyAuthoritiesCollection() {
        Person person = new Person(1L, "testuser", "password");

        assertNotNull(person.getAuthorities());
        assertTrue(person.getAuthorities().isEmpty());
    }

    @Test
    void shouldImplementUserDetailsInterface() {
        Person person = new Person(1L, "testuser", "password");

        assertInstanceOf(org.springframework.security.core.userdetails.UserDetails.class, person);
    }

    @Test
    void shouldHandleNullIdInConstructor() {
        Person person = new Person(null, "testuser", "password");

        assertNotNull(person);
    }

    @Test
    void shouldAccountNotExpired() {
        Person person = new Person(1L, "testuser", "password");

        assertTrue(person.isAccountNonExpired());
    }

    @Test
    void shouldAccountNotLocked() {
        Person person = new Person(1L, "testuser", "password");

        assertTrue(person.isAccountNonLocked());
    }

    @Test
    void shouldCredentialsNotExpired() {
        Person person = new Person(1L, "testuser", "password");

        assertTrue(person.isCredentialsNonExpired());
    }

    @Test
    void shouldAccountBeEnabled() {
        Person person = new Person(1L, "testuser", "password");

        assertTrue(person.isEnabled());
    }

    @Test
    void shouldCreateMultiplePersons() {
        Person person1 = new Person(1L, "user1", "password1");
        Person person2 = new Person(2L, "user2", "password2");
        Person person3 = new Person(3L, "user3", "password3");

        assertNotNull(person1);
        assertNotNull(person2);
        assertNotNull(person3);
        assertNotEquals(person1.getUsername(), person2.getUsername());
    }

    @Test
    void shouldHandleSpecialCharactersInUsername() {
        String username = "user@example.com";
        Person person = new Person(1L, username, "password");

        assertEquals(username, person.getUsername());
    }

    @Test
    void shouldHandleUUIDAsUsername() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        Person person = new Person(1L, uuid, "password");

        assertEquals(uuid, person.getUsername());
    }
}
