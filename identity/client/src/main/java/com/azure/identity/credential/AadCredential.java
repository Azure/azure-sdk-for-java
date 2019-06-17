// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.TokenCredential;

import java.util.ArrayList;
import java.util.List;

/**
 * The base class for credentials that acquires a token from AAD.
 *
 * @param <T> a subtype of this class
 */
abstract class AadCredential<T extends AadCredential<T>> implements TokenCredential {

    private String clientId;

    private String tenantId;

    /**
     * Creates an AadCredential.
     */
    AadCredential() {
    }

    /**
     * @return the client ID for authenticating to AAD.
     */
    public String clientId() {
        return clientId;
    }

    /**
     * Sets the client ID for authentication to AAD.
     * @param clientId the client ID for authentication
     * @return the credential itself
     */
    @SuppressWarnings("unchecked")
    public T clientId(String clientId) {
        this.clientId = clientId;
        return (T) this;
    }

    /**
     * @return the tenant ID for authenticating to AAD.
     */
    public String tenantId() {
        return tenantId;
    }

    /**
     * Sets the tenant ID for authenticating to AAD.
     * @param tenantId the tenant for authenticating to AAD
     * @return the credential itself
     */
    @SuppressWarnings("unchecked")
    public T tenantId(String tenantId) {
        this.tenantId = tenantId;
        return (T) this;
    }

    /**
     * Validates tenant ID and client ID are provided.
     * @throws IllegalArgumentException if any required parameter is missing.
     */
    protected void validate() {
        List<String> missing = new ArrayList<>();
        if (clientId == null) {
            missing.add("clientId");
        }
        if (tenantId == null) {
            missing.add("tenantId");
        }
        if (missing.size() > 0) {
            throw new IllegalArgumentException("Must provide non-null values for "
                + String.join(", ", missing) + " properties in " + this.getClass().getSimpleName());
        }
    }
}
