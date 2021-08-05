// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.containers.containerregistry {
    requires transitive com.azure.core;
    exports com.azure.containers.containerregistry;
    exports com.azure.containers.containerregistry.models;

    opens com.azure.containers.containerregistry.implementation to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.containers.containerregistry.implementation.models to com.fasterxml.jackson.databind, com.azure.core;

    opens com.azure.containers.containerregistry.models to com.fasterxml.jackson.databind;
}
