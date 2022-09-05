// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

public class PostgreSqlJdbcConnectionStringTest extends AbstractJdbcConnectionStringTest {

    @Override
    DatabaseType getDatabaseType() {
        return DatabaseType.POSTGRESQL;
    }

}
