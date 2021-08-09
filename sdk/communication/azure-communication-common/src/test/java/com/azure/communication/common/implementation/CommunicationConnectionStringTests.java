// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CommunicationConnectionStringTests {
    @Test
    public void constructWithNullConnectionStringShouldThrow() {
        assertThrows(NullPointerException.class, () ->
            new CommunicationConnectionString(null), "Should throw on null connectionString");
    }

    @Test
    public void constructWithEmptyConnectionStringShouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            new CommunicationConnectionString(""), "Should throw on empty connectionString");
    }

    @Test
    public void constructWithoutAccessKeyStringShouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            new CommunicationConnectionString("endpoint=https://invalid/;accessKey"), "Should throw on invalid connectionString");
    }

    @Test
    public void constructWithoutEndpointStringShouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            new CommunicationConnectionString("endpoint;accessKey=xyz"), "Should throw on invalid connectionString");
    }

    @Test
    public void constructWithIllegalParameterShouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            new CommunicationConnectionString("endpoint=;illegalparameter=xyz"), "Should throw on illegal parameter");
    }

    @Test
    public void constructWithValidConnectionString() {
        CommunicationConnectionString result =  new CommunicationConnectionString("endpoint=https://valid/;accessKey=xyz");

        assertNotNull(result.getAccessKey());
        assertNotNull(result.getEndpoint());
    }
}
