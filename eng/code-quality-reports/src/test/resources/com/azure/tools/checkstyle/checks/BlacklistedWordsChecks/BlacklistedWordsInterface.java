// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.tools.checkstyle.checks;

import java.net.HttpURLConnection;
import java.time.Duration;

/**
 * Tests for variables in an interface.
 */
public interface BlacklistedWordsInterface {

    int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    /**
     * Computes the delay between each retry.
     *
     * @param retryAttempts The number of retry attempts completed so far.
     * @return The delay duration before the next retry.
     */
    Duration calculateRetryDelay(int retryAttempts);
}
