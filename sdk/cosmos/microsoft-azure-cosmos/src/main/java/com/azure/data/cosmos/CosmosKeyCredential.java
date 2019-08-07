// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

/**
 * Cosmos Key Credential is used to store key credentials, in order to support dynamic key rotation.
 * Singleton instance should be used to support multiple keys.
 * SDK insures to use the updated key provided in the same singleton instance which was used when building {@link CosmosClient}
 */
public class CosmosKeyCredential {

    private String key;

    //  Stores key's hashcode for performance improvements
    private int keyHashCode;

    public CosmosKeyCredential(String key) {
        this.key = key;
        this.keyHashCode = key.hashCode();
    }

    public String key() {
        return key;
    }

    public CosmosKeyCredential key(String key) {
        this.key = key;
        this.keyHashCode = key.hashCode();
        return this;
    }

    public int keyHashCode() {
        return this.keyHashCode;
    }
}
