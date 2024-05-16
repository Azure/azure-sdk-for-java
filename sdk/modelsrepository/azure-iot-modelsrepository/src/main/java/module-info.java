// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.iot.modelsrepository {
    requires transitive com.azure.core;

    exports com.azure.iot.modelsrepository;

    opens com.azure.iot.modelsrepository to com.fasterxml.jackson.databind;
    opens com.azure.iot.modelsrepository.implementation to com.fasterxml.jackson.databind, com.azure.core;
    opens com.azure.iot.modelsrepository.implementation.models to com.fasterxml.jackson.databind, com.azure.core;
}
