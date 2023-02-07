// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public enum FaultInjectionServerErrorType {
    // limit the error types which are related to the current improvements
    SERVER_GONE,
    SERVER_RETRY_WITH,
    INTERNAL_SERVER_ERROR,
    TOO_MANY_REQUEST,
    NOT_FOUND_READ_SESSION_NOT_AVAILABLE,
    SERVER_TIMEOUT,
    SERVER_DELAY,
    SERVER_CONNECTION_UNRESPONSIVE // this is for high connection acquisition time
}
