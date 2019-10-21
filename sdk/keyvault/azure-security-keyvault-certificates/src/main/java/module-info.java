// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.certificates {

    requires transitive com.azure.core;
    requires org.apache.commons.codec;

    exports com.azure.security.keyvault.certificates.models;
    exports com.azure.security.keyvault.certificates.models.webkey;

    opens com.azure.security.keyvault.certificates to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.certificates.implementation to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.certificates.models to com.fasterxml.jackson.databind;
    opens com.azure.security.keyvault.certificates.models.webkey to com.fasterxml.jackson.databind;
}
