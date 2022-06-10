// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.implementation;

/**
 * INTERNAL CLASS.
 *
 * The constants use for polling related operations.
 */
public final class PollingConstants {
    /**
     * The header used for location based polling or sending the final result URL.
     */
    public static final String LOCATION = "Location";
    /**
     * The key for storing the request URL in the polling context.
     */
    public static final String REQUEST_URL = "requestURL";
    /**
     * The key for storing the request HTTP method in the polling context.
     */
    public static final String HTTP_METHOD = "httpMethod";
    /**
     * The key for storing the final resource location in the polling context.
     */
    public static final String RESOURCE_LOCATION = "resourceLocation";
    /**
     * The header used for sending the time in seconds to wait until next poll.
     */
    public static final String RETRY_AFTER = "Retry-After";
    /**
     * The key for storing the response body from the last poll operation.
     */
    public static final String POLL_RESPONSE_BODY = "pollResponseBody";
    /**
     * The polling status indicating the long running operation has not started.
     */
    public static final String STATUS_NOT_STARTED = "NotStarted";
    /**
     * The polling status indicating the long running operation is in progress.
     */
    public static final String STATUS_IN_PROGRESS = "InProgress";
    /**
     * The polling status indicating the long running operation is running.
     */
    public static final String STATUS_RUNNING = "Running";
    /**
     * The polling status indicating the long running operation has failed.
     */
    public static final String STATUS_FAILED = "Failed";
    /**
     * The polling status indicating the long running operation has succeeded.
     */
    public static final String STATUS_SUCCEEDED = "Succeeded";
}
