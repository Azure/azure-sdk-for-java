// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.models;

/**
 * Algorithms for use with client-side encryption support in Azure Cosmos DB.
 *
 */
public class CosmosEncryptionAlgorithm {
    /**
     * Authenticated Encryption algorithm based on https://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05
     */
    public static final String AEAD_AES_256_CBC_HMAC_SHA256 = "AEAD_AES_256_CBC_HMAC_SHA256";
}
