// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Declares a module for Azure Mixed Reality Authentication.
 */
module com.azure.mixedreality.authentication {
    requires transitive com.azure.core;

    exports com.azure.mixedreality.authentication;

    opens com.azure.mixedreality.authentication.implementation.models
        to com.fasterxml.jackson.databind;
    exports com.azure.mixedreality.authentication.implementation.models
        to com.fasterxml.jackson.databind, com.azure.core;
}
