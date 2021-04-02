// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.digitaltwins.parser {
    requires transitive com.azure.core;

    exports com.azure.digitaltwins.parser;

    opens com.azure.digitaltwins.parser to com.fasterxml.jackson.databind;
    opens com.azure.digitaltwins.parser.implementation to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.digitaltwins.parser.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}
