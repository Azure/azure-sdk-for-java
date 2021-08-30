// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This is for testing the modularization of module com.azure.security.keyvault.jca
 */
module com.azure.security.keyvault.jca.test {
    requires com.azure.security.keyvault.jca;
    requires com.azure.security.keyvault.jca.implementation.certificates;
}
