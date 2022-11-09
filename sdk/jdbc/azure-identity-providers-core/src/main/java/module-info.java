// Licensed under the MIT License.
// Copyright (c) Microsoft Corporation. All rights reserved.

module com.azure.identity.providers.core {
    requires transitive com.azure.core;
    requires transitive com.azure.identity;

    exports com.azure.identity.providers.jdbc.implementation.template to
        com.azure.identity.providers.mysql
        ,com.azure.identity.providers.postgresql;
}
