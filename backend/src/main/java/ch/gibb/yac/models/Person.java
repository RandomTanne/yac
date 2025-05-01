package ch.gibb.yac.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private String uuid;

    private String pwHash;
}
