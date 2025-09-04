package ch.gibb.yac.repositories;

import ch.gibb.yac.models.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long> {
  Person findByUsername(String uuid);
}
