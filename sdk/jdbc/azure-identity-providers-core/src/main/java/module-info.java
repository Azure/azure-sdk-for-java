// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

module com.azure.identity.providers.core {
    requires com.azure.identity;
    requires static mysql.connector.j;
    requires static org.postgresql.jdbc;
    requires transitive java.sql;

    exports com.azure.identity.providers.mysql;
    exports com.azure.identity.providers.postgresql;
}
