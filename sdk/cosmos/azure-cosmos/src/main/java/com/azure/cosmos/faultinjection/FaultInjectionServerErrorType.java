// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.util.Beta;

/***
 * Fault injection server error type.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum FaultInjectionServerErrorType {

    /** 410 from server */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SERVER_GONE,

    /** 449 from server */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SERVER_RETRY_WITH,

    /** 500 from server */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    INTERNAL_SERVER_ERROR,

    /** 429 from server */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SERVER_TOO_MANY_REQUEST,

    /** 404-1002 from server */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SERVER_READ_SESSION_NOT_AVAILABLE,

    /** 408 from server */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SERVER_TIMEOUT,

    /** Response delay, when it is over request timeout, can simulate transit timeout */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SERVER_RESPONSE_DELAY,

    /** simulate high channel acquisition, when it is over connection timeout, can simulate connectionTimeoutException */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SERVER_CONNECTION_DELAY
}
