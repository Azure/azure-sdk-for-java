// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.monitor.logingestion {
    requires transitive com.azure.core;
    requires com.azure.identity;
    exports com.azure.monitor.logingestion;

    opens com.azure.monitor.logingestion to com.fasterxml.jackson.databind;

}
