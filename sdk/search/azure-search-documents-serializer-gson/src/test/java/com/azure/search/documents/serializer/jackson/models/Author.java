// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.serializer.jackson.models;

import com.google.gson.annotations.SerializedName;

public class Author {
    @SerializedName(value = "FirstName")
    private String firstName;

    @SerializedName(value = "LastName")
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

    public String toString() {
        return String.format("{\"FirstName\":\"%s\",\"LastName\":\"%s\"}",
            firstName, lastName);
    }
}
