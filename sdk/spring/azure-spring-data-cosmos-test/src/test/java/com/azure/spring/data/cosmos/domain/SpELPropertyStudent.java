// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;

import java.util.Objects;

@Container(containerName = "${dynamic.collection.name}")
public class SpELPropertyStudent {
    private String id;
    private String firstName;
    private String lastName;

    public SpELPropertyStudent(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public SpELPropertyStudent() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpELPropertyStudent that = (SpELPropertyStudent) o;
        return Objects.equals(id, that.id)
            && Objects.equals(firstName, that.firstName)
            && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }

    @Override
    public String toString() {
        return "SpELPropertyStudent{"
            + "id='"
            + id
            + '\''
            + ", firstName='"
            + firstName
            + '\''
            + ", lastName='"
            + lastName
            + '\''
            + '}';
    }
}
