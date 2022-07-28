// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.appconfiguration {
    requires java.sql;

    requires transitive com.azure.core;
    requires transitive com.azure.core.test;
    requires transitive com.azure.identity;
    requires transitive com.azure.http.netty;

    exports com.azure.data.appconfiguration;
    exports com.azure.data.appconfiguration.models;
}
