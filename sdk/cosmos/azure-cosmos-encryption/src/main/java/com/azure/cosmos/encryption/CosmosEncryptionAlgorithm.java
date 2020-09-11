// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

/**
 * Algorithms for use with client-side encryption support in Azure Cosmos DB.
 *
 */
public class CosmosEncryptionAlgorithm {
    /**
     * Authenticated Encryption algorithm based on https://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05
     */
    public static final String AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED = "AEAes256CbcHmacSha256Randomized";
}

// TODO: moderakh enum string type?
