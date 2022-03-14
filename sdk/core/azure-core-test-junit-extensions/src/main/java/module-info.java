// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.test.junit.extensions {
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    exports com.azure.core.test.junit.extensions;
    exports com.azure.core.test.junit.extensions.annotation;

    opens com.azure.core.test.junit.extensions to org.junit.platform.commons;
    opens com.azure.core.test.junit.extensions.annotation to org.junit.platform.commons;
}
