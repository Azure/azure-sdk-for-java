// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


/**
 * This module info will be deployed instead of the one under src/main/java.
 */
module com.azure.security.keyvault.jca {
    requires java.logging;

    exports com.azure.security.keyvault.jca;
    exports com.azure.security.keyvault.jca.implementation.model;
}
