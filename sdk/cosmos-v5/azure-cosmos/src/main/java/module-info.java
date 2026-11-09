// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Isolated, nonfunctional Java 17 foundation for the Azure Cosmos DB v5 SDK.
 * No service client or native transport is provided by this module yet.
 */
module com.azure.cosmos {
    requires com.azure.v2.core;
    requires io.clientcore.core;
}
