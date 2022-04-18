// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.sdk.template.two {
    // All public packages should be exported unconditionally so that all consumers may access their public APIs.
    exports com.azure.sdk.template.two;

    // Implementation packages that contain service request or response models need to be opened to azure-core
    // and Jackson Databind to allow their reflective operations to perform correctly.
    opens com.azure.sdk.template.two.implementation to com.azure.core, com.fasterxml.jackson.databind;
}
