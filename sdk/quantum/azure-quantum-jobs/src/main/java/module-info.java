// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.quantum.jobs {
    exports com.azure.quantum.jobs;
    exports com.azure.quantum.jobs.models;

    requires transitive com.azure.core;

    opens com.azure.quantum.jobs.models to com.fasterxml.jackson.databind;
}
