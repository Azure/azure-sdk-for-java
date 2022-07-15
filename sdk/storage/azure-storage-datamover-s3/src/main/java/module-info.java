// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.datamover.s3 {
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.core;
    requires com.azure.storage.common;
    exports com.azure.storage.datamover.s3;
}
