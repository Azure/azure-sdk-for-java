// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

module com.azure.identity.extensions {
    requires com.azure.identity;
    requires static mysql.connector.j;
    requires static org.postgresql.jdbc;
    requires transitive java.sql;

    exports com.azure.identity.extensions.mysql;
    exports com.azure.identity.extensions.postgresql;
}
