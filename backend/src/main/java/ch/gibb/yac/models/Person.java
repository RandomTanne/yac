package ch.gibb.yac.models;

import jakarta.persistence.*;

@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    private String pwHash;
}
