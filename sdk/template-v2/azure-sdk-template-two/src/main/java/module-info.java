// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.v2.sdk.template.two {
    // All public packages should be exported unconditionally so that all consumers may access their public APIs.
    exports com.azure.v2.sdk.template.two;

    // Implementation packages that contain service request or response models need to be opened to azure-core to allow
    // its reflective operations to perform correctly.
    opens com.azure.v2.sdk.template.two.implementation to com.azure.core;
}
