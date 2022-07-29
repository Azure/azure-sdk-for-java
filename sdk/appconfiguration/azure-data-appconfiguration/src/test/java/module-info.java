// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

open module com.azure.data.appconfiguration {
    exports com.azure.data.appconfiguration;
    exports com.azure.data.appconfiguration.models;

    requires transitive com.azure.core;
    requires transitive com.azure.core.test;
    requires transitive com.azure.identity;
    requires transitive com.azure.http.netty;
    requires java.sql;
    requires transitive org.junit.jupiter.engine;
    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;
}
