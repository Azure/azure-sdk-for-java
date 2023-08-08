// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.directconnectivity.TransportException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public final class RntbdContextException extends TransportException {

    final private CosmosError cosmosError;
    final private Map<String, Object> responseHeaders;
    final private HttpResponseStatus status;

    RntbdContextException(HttpResponseStatus status, ObjectNode details, Map<String, Object> responseHeaders) {

        super(status + ": " + details, null);

        this.cosmosError = new CosmosError(details);
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
