// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.namespaces.models;

public class User {
    private String firstName;
    private String lastName;

    public User() {

    }
    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return "User{"
            + "firstName='" + firstName + '\''
            + ", lastName='" + lastName + '\''
            + '}';
    }
}
