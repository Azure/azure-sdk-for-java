// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Version;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Container()
@CosmosIndexingPolicy()
public class Person {
    private String id;
    private String firstName;

    @PartitionKey
    private String lastName;
    private List<String> hobbies;
    private List<Address> shippingAddresses;
    private Integer age;
    private Map<String, String> passportIdsByCountry;
    @Version
    private String _etag;

    public Person(String id, String firstName, String lastName, List<String> hobbies, List<Address> shippingAddresses,
                  Integer age, Map<String, String> passportIDsByCountry) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hobbies = hobbies;
        this.shippingAddresses = shippingAddresses;
        this.age = age;
        this.passportIdsByCountry = passportIDsByCountry;
    }

    public Person() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public List<Address> getShippingAddresses() {
        return shippingAddresses;
    }

    public void setShippingAddresses(List<Address> shippingAddresses) {
        this.shippingAddresses = shippingAddresses;
    }

    public String get_etag() {
        return _etag;
    }

    public void set_etag(String _etag) {
        this._etag = _etag;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Map<String, String> getPassportIdsByCountry() {
        return passportIdsByCountry;
    }

    public void setPassportIdsByCountry(Map<String, String> passportIdsByCountry) {
        this.passportIdsByCountry = passportIdsByCountry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(id, person.id)
            && Objects.equals(firstName, person.firstName)
            && Objects.equals(lastName, person.lastName)
            && Objects.equals(hobbies, person.hobbies)
            && Objects.equals(shippingAddresses, person.shippingAddresses)
            && Objects.equals(age, person.age)
            && Objects.equals(passportIdsByCountry, person.passportIdsByCountry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, hobbies, shippingAddresses, age, passportIdsByCountry);
    }

    @Override
    public String toString() {
        return "Person{"
            + "id='"
            + id
            + '\''
            + ", firstName='"
            + firstName
            + '\''
            + ", lastName='"
            + lastName
            + '\''
            + ", age="
            + age
            + ", hobbies="
            + hobbies
            + ", shippingAddresses="
            + shippingAddresses
            + ", passportIdsByCountry="
            + passportIdsByCountry
            + ", _etag='"
            + _etag
            + '\''
            + '}';
    }
}
