// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

module com.azure.identity.providers.mysql {
    requires com.azure.identity;
    requires com.azure.identity.providers.core;
    requires mysql.connector.j;

    exports com.azure.identity.providers.mysql;
}
