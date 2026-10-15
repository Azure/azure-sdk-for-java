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

    /** 500/0 from server. */
    INTERNAL_SERVER_ERROR,

    /** 500/102 ConnectionResetByDownstreamService returned for an address refresh request. */
    CONNECTION_RESET_BY_DOWNSTREAM_SERVICE,

    /** 500/1021 ComputeInternalError returned for an address refresh request. */
    COMPUTE_INTERNAL_ERROR,

    /** 500/3010 PartitionFailoverErrorCode returned for an address refresh request. */
    PARTITION_FAILOVER_ERROR_CODE,

    /**
     * 503/0 returned for an address refresh request. This is distinct from {@link #SERVICE_UNAVAILABLE},
     * which represents an SDK-normalized 503/21008.
     */
    SERVICE_UNAVAILABLE_WITH_UNKNOWN_SUBSTATUS,

    /** 503/1022 LeaseNotFound returned for an address refresh request. */
    SERVICE_UNAVAILABLE_LEASE_NOT_FOUND,

    /** 503/20006 Channel_Closed returned for an address refresh request. */
    CHANNEL_CLOSED,

    /** 503/21004 Server_CompletingPartitionMigrationExceededRetryLimit returned for an address refresh request. */
    SERVER_COMPLETING_PARTITION_MIGRATION_EXCEEDED_RETRY_LIMIT,

    /** 503/21007 Server_ReadQuorumNotMet returned for an address refresh request. */
    SERVER_READ_QUORUM_NOT_MET,

    /** 429 from server */
    TOO_MANY_REQUEST,

    /** 404-1002 from server */
    READ_SESSION_NOT_AVAILABLE,

    /** 404-1003 from server */
    OWNER_RESOURCE_NOT_EXISTS,

    /** 404-1013 from server */
    COLLECTION_NOT_AVAILABLE_FOR_READ,

    /** 408 from server */
    TIMEOUT,

    /** 408/0 RequestTimeout returned for an address refresh request without direct-transport 410 wrapping. */
    REQUEST_TIMEOUT,

    /** 410-1008 from server */
    PARTITION_IS_MIGRATING,

    /** 410-1007 from server */
    PARTITION_IS_SPLITTING,

    /** Response delay, when it is over request timeout, can simulate transit timeout */
    RESPONSE_DELAY,

    /** simulate high channel acquisition, when it is over connection timeout, can simulate connectionTimeoutException */
    CONNECTION_DELAY,
    /** Simulate an SDK-normalized service unavailable response (503/21008). */
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
    PARTITION_IS_GONE,

    /** 410-1022 from server */
    LEASE_NOT_FOUND;
}
