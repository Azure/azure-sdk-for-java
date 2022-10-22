// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import org.junit.jupiter.api.Test;

abstract class DatabaseTypeTest {

    abstract void databaseTypeConstructor();
    abstract void databasePluginAvailable();

    @Test
    void testDatabaseTypeConstructor() {
        databaseTypeConstructor();
    }

    @Test
    void testDatabasePluginAvailable() {
        databasePluginAvailable();
    }

}
