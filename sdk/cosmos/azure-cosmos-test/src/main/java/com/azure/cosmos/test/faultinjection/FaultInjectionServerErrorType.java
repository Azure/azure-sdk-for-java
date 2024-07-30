// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

/***
 * Fault injection server error type.
 */
public enum FaultInjectionServerErrorType {

    /** 410 from server. Only applicable for direct connection type. */
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
    CONNECTION_DELAY,
    /**
     * Simulate service unavailable(503)
     */
    SERVICE_UNAVAILABLE,
    /**
     * simulate 410-0 due to staled addresses. The exception will only be cleared if a forceRefresh address refresh happened.
     */
    STALED_ADDRESSES_SERVER_GONE,

    /**
     * Simulate 410/1000, container recreate scenario
     */
    NAME_CACHE_IS_STALE,

    /** 410-1002 from server */
    PARTITION_IS_GONE
}
