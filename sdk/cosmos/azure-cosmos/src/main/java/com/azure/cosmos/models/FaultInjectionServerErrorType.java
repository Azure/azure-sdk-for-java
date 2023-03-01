// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/***
 * Fault injection server error type.
 */
public enum FaultInjectionServerErrorType {

    /** 410 from server */
    SERVER_GONE,
    /** 449 from server */
    SERVER_RETRY_WITH,
    /** 500 from server */
    INTERNAL_SERVER_ERROR,
    /** 429 from server */
    TOO_MANY_REQUEST,
    /** 404-1002 from server */
    READ_SESSION_NOT_AVAILABLE,
    /** 408 from server */
    SERVER_TIMEOUT,
    /** Response delay, when it is over request timeout, can simulate transit timeout */
    SERVER_RESPONSE_DELAY,
    /** simulate high channel acquisition, when it is over connection timeout, can simulate connectionTimeoutException */
    SERVER_CONNECTION_DELAY
}
