// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.schemaregistry.serde.common {
    requires com.azure.core;

    exports com.azure.schemaregistry;

    opens com.azure.schemaregistry to com.fasterxml.jackson.databind, com.azure.core;
}
