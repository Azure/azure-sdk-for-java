// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.models;

/**
 * Algorithms for use with client-side encryption support in Azure Cosmos DB.
 */
public class CosmosEncryptionType {
    /**
     * Randomized encryption uses a method that encrypts data in a less predictable manner. Randomized encryption is more secure.
     */
    public final static  String RANDOMIZED = "Randomized";

    /**
     * Deterministic encryption always generates the same encrypted value for any given plain text value.
     */
    public final static  String DETERMINISTIC = "Deterministic";
}
