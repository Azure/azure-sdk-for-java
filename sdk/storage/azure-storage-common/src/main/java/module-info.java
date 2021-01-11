// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.storage.common {
    requires transitive com.azure.core;

    exports com.azure.storage.common;
    exports com.azure.storage.common.sas;
    exports com.azure.storage.common.policy;

    exports com.azure.storage.common.implementation to // FIXME this should not be a long-term solution
        com.azure.data.tables,
        com.azure.storage.internal.avro,
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography,
        com.azure.storage.blob.batch,
        com.azure.storage.file.share,
        com.azure.storage.file.datalake,
        com.azure.storage.queue,
        com.azure.storage.blob.nio,
        com.azure.storage.blob.changefeed;

    exports com.azure.storage.common.implementation.credentials to // FIXME this should not be a long-term solution
        com.azure.data.tables,
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography,
        com.azure.storage.file.share,
        com.azure.storage.file.datalake,
        com.azure.storage.queue;

    exports com.azure.storage.common.implementation.policy to // FIXME this should not be a long-term solution
        com.azure.data.tables,
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography,
        com.azure.storage.file.share,
        com.azure.storage.file.datalake,
        com.azure.storage.queue;

    exports com.azure.storage.common.implementation.connectionstring to // FIXME this should not be a long-term solution
        com.azure.data.tables,
        com.azure.storage.blob,
        com.azure.storage.blob.cryptography,
        com.azure.storage.file.share,
        com.azure.storage.file.datalake,
        com.azure.storage.queue;
}
