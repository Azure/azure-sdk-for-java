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
    public void generateSas() {
        // BEGIN: com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential("my-account", "my-key");
        QueueServiceSasQueryParameters sasQueryParameters = new QueueServiceSasSignatureValues()
            .setPermissions(QueueSasPermission.parse("rau"))
            .setProtocol(SasProtocol.HTTPS_ONLY)
            .setExpiryTime(OffsetDateTime.now().plus(Duration.ofDays(2)))
            .generateSasQueryParameters(credential);
        // END: com.azure.storage.queue.queueServiceSasSignatureValues.generateSasQueryParameters#StorageSharedKeyCredential
    }
}
