// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.implementation;

import java.util.Locale;

/**
 * The enum RequestVerb.
 */
public enum  RequestVerb {

    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    PATCH("PATCH");


    RequestVerb(String stringValue) {
        this.stringValue = stringValue;
        this.lowerCaseStringValue = stringValue.toLowerCase(Locale.ROOT);
    }

    /**
     * Provides lower case name of the verb.
     * e.g, get, put, post, delete, head
     * @return lower case form of the verb
     */
    public String toLowerCase() {
        return lowerCaseStringValue;
    }

    private final String lowerCaseStringValue;
    private final String stringValue;
}
