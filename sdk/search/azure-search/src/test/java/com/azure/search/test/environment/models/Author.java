// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Author {
    @JsonProperty(value = "FirstName")
    private String firstName;

    @JsonProperty(value = "LastName")
    private String lastName;

    public String firstName() {
        return this.firstName;
    }

    public Author firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String lastName() {
        return this.lastName;
    }

    public Author lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
}
