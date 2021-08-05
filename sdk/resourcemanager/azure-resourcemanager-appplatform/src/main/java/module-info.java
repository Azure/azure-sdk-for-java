// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.resourcemanager.appplatform {
    requires transitive com.azure.resourcemanager.resources;
    requires com.azure.storage.file.share;

    // export public APIs of appplatform
    exports com.azure.resourcemanager.appplatform;
    exports com.azure.resourcemanager.appplatform.fluent;
    exports com.azure.resourcemanager.appplatform.fluent.models;
    exports com.azure.resourcemanager.appplatform.models;

    // open packages specifically for azure core and jackson
    opens com.azure.resourcemanager.appplatform.fluent.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
    opens com.azure.resourcemanager.appplatform.models to
        com.azure.core,
        com.fasterxml.jackson.databind;
}
