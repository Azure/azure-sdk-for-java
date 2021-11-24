// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.keyvault {
    requires transitive com.azure.resourcemanager.resources;
    requires transitive com.azure.security.keyvault.keys;
    requires transitive com.azure.security.keyvault.secrets;
    requires transitive com.azure.resourcemanager.authorization;

    // export public APIs of keyvault
    exports com.azure.resourcemanager.keyvault;
    exports com.azure.resourcemanager.keyvault.fluent;
    exports com.azure.resourcemanager.keyvault.fluent.models;
    exports com.azure.resourcemanager.keyvault.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.keyvault.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.keyvault.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
