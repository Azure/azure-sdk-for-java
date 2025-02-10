// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


module com.azure.cosmos.encryption {

    requires transitive com.azure.cosmos;
    requires java.sql;

    // public API surface area
    exports com.azure.cosmos.encryption;
    exports com.azure.cosmos.encryption.models;
    opens com.azure.cosmos.encryption to com.fasterxml.jackson.databind, com.fasterxml.jackson.module.afterburner;
}
