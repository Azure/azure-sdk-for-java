// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.secrets {
    requires transitive com.azure.core;

    exports com.azure.security.keyvault.secrets;
    exports com.azure.security.keyvault.secrets.models;

    opens com.azure.security.keyvault.secrets to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.secrets.implementation to com.azure.core, com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.secrets.models to com.azure.core, com.fasterxml.jackson.databind;
    exports com.azure.security.keyvault.secrets.implementation;
}
