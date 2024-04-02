// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosException;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.RetriableException;

public class KafkaCosmosExceptionsHelper {
    public static boolean isTransientFailure(int statusCode, int substatusCode) {
        return statusCode == KafkaCosmosConstants.StatusCodes.GONE
            || statusCode == KafkaCosmosConstants.StatusCodes.SERVICE_UNAVAILABLE
            || statusCode == KafkaCosmosConstants.StatusCodes.INTERNAL_SERVER_ERROR
            || statusCode == KafkaCosmosConstants.StatusCodes.REQUEST_TIMEOUT
            || (statusCode == KafkaCosmosConstants.StatusCodes.NOTFOUND && substatusCode == KafkaCosmosConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);

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
        return statusCode == KafkaCosmosConstants.StatusCodes.GONE
            && (substatusCode == KafkaCosmosConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE
            || substatusCode == KafkaCosmosConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE);
    }

    public static ConnectException convertToConnectException(Throwable throwable, String message) {
        if (KafkaCosmosExceptionsHelper.isTransientFailure(throwable)) {
            return new RetriableException(message, throwable);
        }

        return new ConnectException(message, throwable);
    }

    public static boolean isResourceExistsException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == KafkaCosmosConstants.StatusCodes.CONFLICT;
        }

        return false;
    }

    public static boolean isNotFoundException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == KafkaCosmosConstants.StatusCodes.NOTFOUND;
        }

        return false;
    }

    public static boolean isPreconditionFailedException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == KafkaCosmosConstants.StatusCodes.PRECONDITION_FAILED;
        }

        return false;
    }

    public static boolean isTimeoutException(Throwable throwable) {
        if (throwable instanceof CosmosException) {
            return ((CosmosException) throwable).getStatusCode() == KafkaCosmosConstants.StatusCodes.REQUEST_TIMEOUT;
        }

        return false;
    }
}
