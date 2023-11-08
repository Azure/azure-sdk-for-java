// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

module com.azure.identity.extensions {
    requires com.azure.identity;
    requires static mysql.connector.j;
    requires static org.postgresql.jdbc;
    requires static java.sql;

    exports com.azure.identity.extensions.jdbc.mysql;
    exports com.azure.identity.extensions.jdbc.postgresql;
}
