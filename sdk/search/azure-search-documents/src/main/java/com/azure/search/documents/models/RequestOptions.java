// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/**
 * Additional parameters for a set of operations.
 */
@Fluent
public final class RequestOptions {
    /*
     * The tracking ID sent with the request to help with debugging.
     */
    @JsonProperty(value = "xMsClientRequestId")
    private UUID clientRequestId;

    /**
     * Get the xMsClientRequestId property: The tracking ID sent with the
     * request to help with debugging.
     *
     * @return the xMsClientRequestId value.
     */
    public UUID getClientRequestId() {
        return this.clientRequestId;
    }

    /**
     * Set the clientRequestId property: The tracking ID sent with the
     * request to help with debugging.
     *
     * @param clientRequestId the clientRequestId value to set.
     * @return the RequestOptions object itself.
     */
    public RequestOptions setClientRequestId(UUID clientRequestId) {
        this.clientRequestId = clientRequestId;
        return this;
    }
}
