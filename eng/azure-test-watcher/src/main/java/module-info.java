// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.testing {
    requires org.junit.jupiter.api;

    exports com.azure.testing;

    provides org.junit.jupiter.api.extension.Extension with com.azure.testing.AzureTestWatcher;

    uses org.junit.jupiter.api.extension.Extension;
}