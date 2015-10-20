/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.core.pipeline.jersey;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;

import java.util.Arrays;
import java.util.Random;

public class ExponentialRetryPolicy extends RetryPolicy {
    private final int deltaBackoffIntervalInMs;
    private final int maximumAttempts;
    private final Random randRef = new Random();
    private final int resolvedMaxBackoff = DEFAULT_MAX_BACKOFF;
    private final int resolvedMinBackoff = DEFAULT_MIN_BACKOFF;
    private final int[] retryableStatusCodes;

    public ExponentialRetryPolicy(int[] retryableStatusCodes) {
        this(DEFAULT_CLIENT_BACKOFF, DEFAULT_CLIENT_RETRY_COUNT,
                retryableStatusCodes);
    }

    public ExponentialRetryPolicy(int deltaBackoff, int maximumAttempts,
            int[] retryableStatusCodes) {
        this.deltaBackoffIntervalInMs = deltaBackoff;
        this.maximumAttempts = maximumAttempts;
        this.retryableStatusCodes = Arrays.copyOf(retryableStatusCodes,
                retryableStatusCodes.length);
        Arrays.sort(this.retryableStatusCodes);
    }

    @Override
    public boolean shouldRetry(int retryCount, ServiceResponseContext response,
            Exception error) {
        if (response == null) {
            return false;
        }

        if (retryCount >= this.maximumAttempts) {
            return false;
        }

        // Don't retry if not retryable status code
        if (Arrays
                .binarySearch(this.retryableStatusCodes, response.getStatus()) < 0) {
            return false;
        }

        return true;
    }

    @Override
    public int calculateBackoff(int currentRetryCount,
            ServiceResponseContext response, Exception error) {
        // Calculate backoff Interval between 80% and 120% of the desired
        // backoff, multiply by 2^n -1 for
        // exponential
        int incrementDelta = (int) (Math.pow(2, currentRetryCount) - 1);
        int boundedRandDelta = (int) (this.deltaBackoffIntervalInMs * 0.8)
                + this.randRef
                        .nextInt((int) (this.deltaBackoffIntervalInMs * 1.2)
                                - (int) (this.deltaBackoffIntervalInMs * 0.8));
        incrementDelta *= boundedRandDelta;

        // Enforce max / min backoffs
        return Math.min(this.resolvedMinBackoff + incrementDelta,
                this.resolvedMaxBackoff);
    }
}
