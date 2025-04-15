// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.keyvault.certificates {
    requires transitive com.azure.core;

    exports com.azure.security.keyvault.certificates;
    exports com.azure.security.keyvault.certificates.models;

    opens com.azure.security.keyvault.certificates to com.azure.core;
    opens com.azure.security.keyvault.certificates.models to com.azure.core;
    opens com.azure.security.keyvault.certificates.implementation.models to com.azure.core;
}
