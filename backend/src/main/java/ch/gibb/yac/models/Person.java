package ch.gibb.yac.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The person model used for authentication.
 * @author Manuel Möri
 * @version 2.0
 * @since 2025-04-22
 */
@Entity
public class Person implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * The username of the person
     */
    private String username;

    /**
     * The hashed password of the person
     */
    @NotNull(message = "Password must not be null") @Length(min = 12, message = "Password must be at least 12 characters long") private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public Person() {}

    public Person(Long id, String username, String password) {
        this.username = username;
        this.password = password;
    }
}
