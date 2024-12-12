package com.example.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
public class Person extends Unit{
    @Setter
    private boolean status;

    @JsonCreator
    public Person(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name){
        super(id, name);
    }

    @Override
    public String toString() {
        return String.format("%s{id= %s, name= %s, status= %b}", Person.class.getSimpleName(), id, name, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return Objects.equals(getId(), person.getId())
                && Objects.equals(getName(), person.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
