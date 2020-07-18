// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption.api;

// TODO: enum string type?

/**
 * Algorithms for use with client-side encryption support in Azure Cosmos DB.
 */
public class CosmosEncryptionAlgorithm {

    /**
     * Authenticated Encryption algorithm based on https://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05
     */
    public static final String AEAes256CbcHmacSha256Randomized = "AEAes256CbcHmacSha256Randomized";

}
