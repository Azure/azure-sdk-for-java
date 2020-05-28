// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.data.schemaregistry.serde.common {
    requires com.azure.core;

    exports com.azure.data.schemaregistry;

    opens com.azure.data.schemaregistry to com.fasterxml.jackson.databind, com.azure.core;
}
