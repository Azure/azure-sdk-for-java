// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Objects;

public class Person {
    @JsonProperty
    private String name;

    @JsonProperty
    private int age;


    public Person() {
    }

    @JsonSetter
    public Person setName(String name) {
        this.name = name;
        return this;
    }

    @JsonGetter
    public String getName() {
        return name;
    }

    @JsonSetter
    public Person setAge(int age) {
        this.age = age;
        return this;
    }

    @JsonGetter
    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Person person = (Person) other;

        return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
