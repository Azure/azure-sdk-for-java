// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.SasProtocol;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Code snippets for {@link QueueServiceSasSignatureValues}.
 */
public class QueueServiceSasSignatureValuesJavaDocCodeSnippets {
    public void generateSasExpiryTime() {
        // BEGIN: com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("my-account", "my-key");

        // The expiry time and permissions are required to create a valid SAS
        // if a stored access policy identifier is not set.
        QueueServiceSasQueryParameters sasQueryParameters = new QueueServiceSasSignatureValues()
            .setPermissions(QueueSasPermission.parse("rau"))
            .setProtocol(SasProtocol.HTTPS_ONLY)
            .setExpiryTime(OffsetDateTime.now().plus(Duration.ofDays(2)))
            .generateSasQueryParameters(credential);
        // END: com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential
    }

    public void generateWithStoredAccessPolicy() {
        // BEGIN: com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters.identifier#StorageSharedKeyCredential
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("my-account", "my-key");

        // The shared access policy, "read-write-user" exists in the storage account. The SAS generated from this has
        // the same duration and permissions as the policy.
        // The expiry and permissions should not be set explicitly.
        QueueServiceSasQueryParameters sasQueryParameters = new QueueServiceSasSignatureValues()
            .setIdentifier("read-write-user")
            .setProtocol(SasProtocol.HTTPS_ONLY)
            .generateSasQueryParameters(credential);
        // END: com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters.identifier#StorageSharedKeyCredential
    }
}
