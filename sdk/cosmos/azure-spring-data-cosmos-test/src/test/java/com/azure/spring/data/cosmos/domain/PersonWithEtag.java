// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Version;

import java.util.List;
import java.util.Objects;

@Container()
@CosmosIndexingPolicy()
public class PersonWithEtag {
    private String id;
    private String firstName;

    @PartitionKey
    private String lastName;
    private List<String> hobbies;
    private List<Address> shippingAddresses;
    @Version
    private String etag;

    public PersonWithEtag(String id, String firstName, String lastName, List<String> hobbies, List<Address> shippingAddresses) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hobbies = hobbies;
        this.shippingAddresses = shippingAddresses;
    }

    public PersonWithEtag() {
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

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PersonWithEtag person = (PersonWithEtag) o;
        return Objects.equals(id, person.id)
            && Objects.equals(firstName, person.firstName)
            && Objects.equals(lastName, person.lastName)
            && Objects.equals(hobbies, person.hobbies)
            && Objects.equals(shippingAddresses, person.shippingAddresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, hobbies, shippingAddresses);
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
            + ", hobbies="
            + hobbies
            + ", shippingAddresses="
            + shippingAddresses
            + ", etag='"
            + etag
            + '\''
            + '}';
    }
}
