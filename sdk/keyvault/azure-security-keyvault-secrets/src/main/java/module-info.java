// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.secrets {
    requires transitive com.azure.core;

    exports com.azure.security.keyvault.secrets;
    exports com.azure.security.keyvault.secrets.models;

    opens com.azure.security.keyvault.secrets to com.azure.core;
    opens com.azure.security.keyvault.secrets.models to com.azure.core;
    opens com.azure.security.keyvault.secrets.implementation.models to com.azure.core;
}