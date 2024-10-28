// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;
import reactor.core.Exceptions;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class KafkaCosmosExceptionsHelper {
    public static boolean isTransientFailure(int statusCode, int substatusCode) {
        return statusCode == HttpConstants.StatusCodes.GONE
            || statusCode == HttpConstants.StatusCodes.SERVICE_UNAVAILABLE
            || statusCode == HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR
            || statusCode == HttpConstants.StatusCodes.REQUEST_TIMEOUT
            || (statusCode == HttpConstants.StatusCodes.NOTFOUND && substatusCode == HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);

    }

    public static boolean isTransientFailure(Throwable e) {
        if (e instanceof CosmosException) {
            return isTransientFailure(((CosmosException) e).getStatusCode(), ((CosmosException) e).getSubStatusCode());
        }

        return false;
    }

    public static boolean isFeedRangeGoneException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return isFeedRangeGoneException(
                ((CosmosException) throwable).getStatusCode(),
                ((CosmosException) throwable).getSubStatusCode());
        }

        return false;
    }

    public static boolean isFeedRangeGoneException(int statusCode, int substatusCode) {
        return statusCode == HttpConstants.StatusCodes.GONE
            && (substatusCode == HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE
            || substatusCode == HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE);
    }

    public static ConnectException convertToConnectException(Throwable throwable, String message) {
        if (KafkaCosmosExceptionsHelper.isTransientFailure(throwable)) {
            return new RetriableException(message, throwable);
        }

        return new ConnectException(message, throwable);
    }

    public static boolean isResourceExistsException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == HttpConstants.StatusCodes.CONFLICT;
        }

        return false;
    }

    public static boolean isNotFoundException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == HttpConstants.StatusCodes.NOTFOUND;
        }

        return false;
    }

    public static boolean isPreconditionFailedException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == HttpConstants.StatusCodes.PRECONDITION_FAILED;
        }

        return false;
    }

    public static boolean isTimeoutException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == HttpConstants.StatusCodes.REQUEST_TIMEOUT;
        }

        return false;
    }

    public static boolean isOwnerResourceNotExistsException(Throwable throwable) {
        checkNotNull(throwable, "Throwable should not be null");

        CosmosException cosmosException = Utils.as(Exceptions.unwrap(throwable), CosmosException.class);
        return cosmosException != null
            && cosmosException.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND
            && cosmosException.getSubStatusCode() == HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS;
    }
}
