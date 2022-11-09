// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

module com.azure.identity.providers.postgresql {
    requires com.azure.identity;
    requires com.azure.identity.providers.core;
    requires org.postgresql.jdbc;
    requires java.sql;

    exports com.azure.identity.providers.postgresql;
}
