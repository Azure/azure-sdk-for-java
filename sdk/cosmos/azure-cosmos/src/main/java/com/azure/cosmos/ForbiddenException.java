// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.internal.RMResources;
import com.azure.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.cosmos.internal.http.HttpHeaders;
import com.azure.cosmos.internal.HttpConstants;
import com.azure.cosmos.internal.RMResources;
import com.azure.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.cosmos.internal.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

public class ForbiddenException extends CosmosClientException {
    ForbiddenException() {
        this(RMResources.Forbidden);
    }

    public ForbiddenException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.FORBIDDEN, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    ForbiddenException(String message) {
        this(message, null, null, null);
    }

    ForbiddenException(String message, HttpHeaders headers, String requestUrlString) {
        this(message, null, headers, requestUrlString);
    }

    public ForbiddenException(String message, HttpHeaders headers, URI requestUri) {
        this(message, headers, requestUri != null ? requestUri.toString() : null);
    }

    ForbiddenException(Exception innerException) {
        this(RMResources.Forbidden, innerException, null, null);
    }

    ForbiddenException(String message,
                              Exception innerException,
                              HttpHeaders headers,
                              String requestUrlString) {
        super(String.format("%s: %s", RMResources.Forbidden, message),
                innerException,
                HttpUtils.asMap(headers),
                HttpConstants.StatusCodes.FORBIDDEN,
                requestUrlString);
    }
}
