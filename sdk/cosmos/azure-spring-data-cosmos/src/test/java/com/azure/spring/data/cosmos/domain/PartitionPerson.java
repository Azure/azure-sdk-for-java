// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;

import java.util.List;
import java.util.Objects;

@Container()
public class PartitionPerson {

    private String id;

    private String firstName;

    @PartitionKey
    private Integer zipCode;

    private List<String> hobbies;

    private List<Address> shippingAddresses;

    public PartitionPerson() {
    }

    public PartitionPerson(String id, String firstName, Integer zipCode, List<String> hobbies, List<Address> shippingAddresses) {
        this.id = id;
        this.firstName = firstName;
        this.zipCode = zipCode;
        this.hobbies = hobbies;
        this.shippingAddresses = shippingAddresses;
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

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PartitionPerson that = (PartitionPerson) o;
        return Objects.equals(id, that.id)
            && Objects.equals(firstName, that.firstName)
            && Objects.equals(zipCode, that.zipCode)
            && Objects.equals(hobbies, that.hobbies)
            && Objects.equals(shippingAddresses, that.shippingAddresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, zipCode, hobbies, shippingAddresses);
    }

    @Override
    public String toString() {
        return "PartitionPerson{"
            + "id='"
            + id
            + '\''
            + ", firstName='"
            + firstName
            + '\''
            + ", lastName='"
            + zipCode
            + '\''
            + ", hobbies="
            + hobbies
            + ", shippingAddresses="
            + shippingAddresses
            + '}';
    }
}
