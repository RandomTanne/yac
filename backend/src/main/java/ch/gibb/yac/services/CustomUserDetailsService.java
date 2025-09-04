package ch.gibb.yac.services;

import ch.gibb.yac.models.Person;
import ch.gibb.yac.repositories.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final PersonRepository personRepository;

  public CustomUserDetailsService(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Person person = personRepository.findByUsername(username);
    if (person == null) {
      throw new UsernameNotFoundException("User Not Found with username: " + username);
    }
    return new org.springframework.security.core.userdetails.User(
        person.getUsername(), person.getPassword(), person.getAuthorities());
  }
}
