// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.digitaltwins.codegen {
    requires transitive com.azure.core;

    exports com.azure.digitaltwins.codegen;

    opens com.azure.digitaltwins.codegen to com.fasterxml.jackson.databind;
}
