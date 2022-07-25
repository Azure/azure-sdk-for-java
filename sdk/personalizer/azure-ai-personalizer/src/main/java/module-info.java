// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.ai.personalizer {
    requires transitive com.azure.core;

    exports com.azure.ai.personalizer;

    opens com.azure.ai.personalizer to com.fasterxml.jackson.databind;
    opens com.azure.ai.personalizer.implementation to com.fasterxml.jackson.databind;
    opens com.azure.ai.personalizer.models to com.fasterxml.jackson.databind, com.azure.core;
}
