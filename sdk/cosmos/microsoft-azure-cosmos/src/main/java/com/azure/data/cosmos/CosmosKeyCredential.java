// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

/**
 * Cosmos Key Credential is used to store key credentials, in order to support dynamic key rotation.
 * Singleton instance should be used to support multiple keys.
 * SDK insures to use the updated key provided in the same singleton instance which was used when building {@link CosmosClient}
 */
public class CosmosKeyCredential {

    private String masterKey;

    public CosmosKeyCredential(String masterKey) {
        this.masterKey = masterKey;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }
}
