// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

/***
 * Fault injection server error type.
 */
public enum FaultInjectionServerErrorType {

    /** 410 from server */
    GONE,

    /** 449 from server */
    RETRY_WITH,

    /** 500 from server */
    INTERNAL_SERVER_ERROR,

    /** 429 from server */
    TOO_MANY_REQUEST,

    /** 404-1002 from server */
    READ_SESSION_NOT_AVAILABLE,

    /** 408 from server */
    TIMEOUT,

    /** 410-1008 from server */
    PARTITION_IS_MIGRATING,

    /** 410-1007 from server */
    PARTITION_IS_SPLITTING,

    /** Response delay, when it is over request timeout, can simulate transit timeout */
    RESPONSE_DELAY,

    /** simulate high channel acquisition, when it is over connection timeout, can simulate connectionTimeoutException */
    CONNECTION_DELAY
}
