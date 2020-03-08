// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

/**
 * Cosmos Key Credential is used to store key credentials, in order to support dynamic key rotation.
 * Singleton instance should be used to support multiple keys.
 * Azure client library for Cosmos ensures to use the updated key provided in the same singleton instance
 * which was used when building {@link CosmosAsyncClient}
 */
public class CosmosKeyCredential {

    private String key;

    //  Stores key's hashcode for performance improvements
    private int keyHashCode;

    /**
     * Instantiates a new Cosmos key credential.
     *
     * @param key the key
     */
    public CosmosKeyCredential(String key) {
        this.key = key;
        this.keyHashCode = key.hashCode();
    }

    /**
     * Returns the key stored in Cosmos Key Credential
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key to be used in CosmosKeyCredential
     *
     * @param key key to be used in CosmosKeyCredential
     * @return current CosmosKeyCredential
     */
    public CosmosKeyCredential setKey(String key) {
        this.key = key;
        this.keyHashCode = key.hashCode();
        return this;
    }

    /**
     * CosmosKeyCredential stores the computed hashcode of the key for performance improvements.
     *
     * @return hashcode of the key
     */
    int getKeyHashCode() {
        return this.keyHashCode;
    }
}
