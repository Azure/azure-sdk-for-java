// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import javax.crypto.SecretKey;

import static com.azure.storage.blob.specialized.cryptography.BlobCryptographyTestBase.MOCK_AES_KEY;

final class EncryptedBlobAsyncClientSpy extends EncryptedBlobAsyncClient {
    EncryptedBlobAsyncClientSpy(EncryptedBlobAsyncClient client) {
        super(client.getHttpPipeline(), client.getAccountUrl(), client.getServiceVersion(), client.getAccountName(),
            client.getContainerName(), client.getBlobName(), client.getSnapshotId(), client.getCustomerProvidedKey(),
            client.getEncryptionScopeInternal(), client.getKeyWrapper(), client.getKeyWrapAlgorithm(),
            client.getVersionId(), client.getEncryptionVersion(), client.isRequiresEncryption(),
            client.getGcmEncryptionRegionLength());
    }

    @Override
    SecretKey generateSecretKey() {
        return MOCK_AES_KEY;
    }
}
