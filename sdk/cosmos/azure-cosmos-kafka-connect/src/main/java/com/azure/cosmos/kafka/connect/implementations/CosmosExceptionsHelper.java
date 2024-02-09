// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementations;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;

public class CosmosExceptionsHelper {
    public static boolean isTransientFailure(int statusCode, int substatusCode) {
        return statusCode == HttpConstants.StatusCodes.GONE
            || statusCode == HttpConstants.StatusCodes.SERVICE_UNAVAILABLE
            || statusCode == HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR
            || statusCode == HttpConstants.StatusCodes.REQUEST_TIMEOUT
            || (statusCode == HttpConstants.StatusCodes.NOTFOUND && substatusCode == HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);

    }

    public static boolean isTransientFailure(Exception e) {
        if (e instanceof CosmosException) {
            return isTransientFailure(((CosmosException) e).getStatusCode(), ((CosmosException) e).getSubStatusCode());
        }

        return false;
    }
}
