// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.quantum.jobs {
    exports com.azure.quantum.jobs;

    requires com.azure.core;
    requires com.azure.identity;

    exports com.azure.quantum.jobs.models to com.fasterxml.jackson.databind;
    opens com.azure.quantum.jobs.models to com.fasterxml.jackson.databind;
}
