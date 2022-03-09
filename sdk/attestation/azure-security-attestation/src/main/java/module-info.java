// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.security.attestation {
    requires transitive com.azure.core;
    requires com.nimbusds.jose.jwt;
    exports com.azure.security.attestation;
    exports com.azure.security.attestation.models;

    opens com.azure.security.attestation.implementation.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
