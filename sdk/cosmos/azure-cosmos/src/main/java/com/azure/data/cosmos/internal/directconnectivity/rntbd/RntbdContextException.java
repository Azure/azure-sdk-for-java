// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosError;
import com.azure.data.cosmos.internal.directconnectivity.TransportException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public final class RntbdContextException extends TransportException {

    final private CosmosError cosmosError;
    final private Map<String, Object> responseHeaders;
    final private HttpResponseStatus status;

    RntbdContextException(HttpResponseStatus status, ObjectNode details, Map<String, Object> responseHeaders) {

        super(status + ": " + details, null);

        this.cosmosError = BridgeInternal.createCosmosError(details);
        this.responseHeaders = responseHeaders;
        this.status = status;
    }

    public CosmosError getCosmosError() {
        return cosmosError;
    }

    public Map<String, Object> getResponseHeaders() {
        return responseHeaders;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}