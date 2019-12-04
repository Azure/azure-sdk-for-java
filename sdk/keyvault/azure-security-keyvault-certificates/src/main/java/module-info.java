// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.certificates {

    requires transitive com.azure.core;

    exports com.azure.security.keyvault.certificates.models;

    opens com.azure.security.keyvault.certificates to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.certificates.implementation to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.certificates.models to com.fasterxml.jackson.databind;
}
