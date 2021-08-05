// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.pojos;

import java.util.List;
import java.util.UUID;

public class Person {
    public String name;

    public String id;

    public City city;

    public double income;

    public List<Person> children;

    public int age;

    public Pet pet;

    public UUID guid;

    public Person() {
    }

    public Person(String name, City city, double income, List<Person> children, int age, Pet pet, UUID guid) {
        this.name = name;
        this.city = city;
        this.income = income;
        this.children = children;
        this.age = age;
        this.pet = pet;
        this.guid = guid;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for property 'city'.
     *
     * @return Value for property 'city'.
     */
    public City getCity() {
        return city;
    }

    /**
     * Getter for property 'age'.
     *
     * @return Value for property 'age'.
     */
    public int getAge() {
        return age;
    }

    /**
     * Getter for property 'guid'.
     *
     * @return Value for property 'guid'.
     */
    public UUID getGuid() {
        return guid;
    }
}
