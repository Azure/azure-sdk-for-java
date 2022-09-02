// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.autoconfigure.implementation.jdbc.JdbcConnectionString;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertNull;

abstract class AbstractJdbcConnectionStringTest {


    abstract void connectionStringWithNonValueProperties();
    abstract void connectionStringWithMultipleProperties();
    abstract void connectionStringWithoutProperties();
    abstract void connectionStringWittInvalidProperties();
    abstract void inconsistentPropertiesTest();
    abstract void consistentPropertiesTest();
    abstract void enhanceConnectionStringWithoutProperties();
    abstract void enhanceConnectionStringWithProperties();


    @Test
    void testConnectionStringWithNonValueProperties() {
        connectionStringWithNonValueProperties();
    }

    @Test
    void testConnectionStringWithMultipleProperties() {
        connectionStringWithMultipleProperties();
    }

    @Test
    void testConnectionStringWithoutProperties() {
        connectionStringWithoutProperties();
    }

    @Test
    void testConnectionStringWittInvalidProperties() {
        connectionStringWittInvalidProperties();
    }

    @Test
    void testInconsistentPropertiesTest() {
        inconsistentPropertiesTest();
    }

    @Test
    void testConsistentPropertiesTest() {
        consistentPropertiesTest();
    }

    @Test
    void invalidConnectionString() {
        String connectionString = "jdbc:mysqx://host";
        JdbcConnectionString resolve = JdbcConnectionString.resolve(connectionString);
        assertNull(resolve);
    }

    @Test
    void testEnhanceConnectionStringWithoutProperties() {
        enhanceConnectionStringWithoutProperties();
    }

    @Test
    void testEnhanceConnectionStringWithProperties() {
        enhanceConnectionStringWithProperties();
    }
}
