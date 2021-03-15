// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.samples.models;

/**
 * A model class that will be used to send events with in the sample code.
 * It will be serialized into Json by default when being sent to EG.
 */
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
