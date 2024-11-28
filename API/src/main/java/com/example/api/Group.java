package com.example.api;

import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Group extends Unit {
    @Getter
    private final List<Person> personInGroupList;
    @Getter
    private final Person admin;

    public Group(String id, String name, List<Person> personInGroupList, Person admin) {
        super(id, name);
        this.personInGroupList = personInGroupList;
        this.admin = admin;
    }

    @Override
    public String toString() {
        return String.format("%s{id= %s, name= %s, personInGroupList= %s, admin= %s}", Group.class.getSimpleName(),
                id, name, personInGroupList, admin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group group)) return false;
        return Objects.equals(getId(), group.getId())
                && Objects.equals(getName(), group.getName())
                && comparePersonInGroupList(personInGroupList, group.getPersonInGroupList());
    }

    private boolean comparePersonInGroupList(List<Person> personInGroupList, List<Person> personInGroupListCheck) {
       personInGroupList.sort(Comparator.comparing(Person::getId));
       personInGroupListCheck.sort(Comparator.comparing(Person::getId));
       return personInGroupList.equals(personInGroupListCheck);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }
}
