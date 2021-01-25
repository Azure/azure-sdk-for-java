// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;
import java.util.Objects;

/**
 * Credential to support accessing communication resource with resource access key
 */
public final class CommunicationClientCredential {
    private final String accessKay;

    /**
     * Requires resource access key to create the credential
     *
     * @param accessKey resource access key as provided by Azure in Base64 format
     */
    public CommunicationClientCredential(String accessKey) {
        Objects.requireNonNull(accessKey, "'accessKey' cannot be null");
        this.accessKay = accessKey;
    }

    /**
     * Get access key of this credential
     * @return access key of this credential
     */
    public String getAccessKay() {
        return accessKay;
    }

}
