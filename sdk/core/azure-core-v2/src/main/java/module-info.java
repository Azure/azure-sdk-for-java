// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This module contains the core classes for Azure SDK.
 */
module com.azure.v2.core {
    requires transitive io.clientcore.core;

    // public API surface area
    exports com.azure.v2.core.credentials;
    exports com.azure.v2.core.cryptography;
    exports com.azure.v2.core.http.policy;
    exports com.azure.v2.core.http.rest;
    exports com.azure.v2.core.utils;
    exports com.azure.v2.core.utils.io;
    exports com.azure.v2.core.utils.paging;
    exports com.azure.v2.core.utils.serializers;
    exports com.azure.v2.core.traits;
}
