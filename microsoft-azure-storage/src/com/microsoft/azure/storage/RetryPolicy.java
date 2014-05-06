/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package com.microsoft.azure.storage;

import java.net.HttpURLConnection;
import java.util.Date;

import com.microsoft.azure.storage.core.Utility;

/**
 * Abstract class that represents a retry policy.
 */
public abstract class RetryPolicy implements RetryPolicyFactory {

    /**
     * Represents the default client backoff interval, in milliseconds.
     */
    public static final int DEFAULT_CLIENT_BACKOFF = 1000 * 30;

    /**
     * Represents the default client retry count.
     */
    public static final int DEFAULT_CLIENT_RETRY_COUNT = 3;

    /**
     * Represents the default maximum backoff interval, in milliseconds.
     */
    public static final int DEFAULT_MAX_BACKOFF = 1000 * 90;

    /**
     * Represents the default minimum backoff interval, in milliseconds.
     */
    public static final int DEFAULT_MIN_BACKOFF = 1000 * 3;

    /**
     * Represents the realized backoff interval, in milliseconds.
     */
    protected int deltaBackoffIntervalInMs;

    /**
     * Represents the maximum retries that the retry policy should attempt.
     */
    protected int maximumAttempts;

    /**
     * Represents the time of the last request attempt to the primary location.
     */
    protected Date lastPrimaryAttempt = null;

    /**
     * Represents the time of the last request attempt to the secondary location.
     */
    protected Date lastSecondaryAttempt = null;

    /**
     * Creates an instance of the <code>RetryPolicy</code> class.
     */
    public RetryPolicy() {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>RetryPolicy</code> class using the specified delta backoff and maximum retry
     * attempts.
     * 
     * @param deltaBackoff
     *            The backoff interval, in milliseconds, between retries.
     * @param maxAttempts
     *            The maximum number of retry attempts.
     */
    public RetryPolicy(final int deltaBackoff, final int maxAttempts) {
        this.deltaBackoffIntervalInMs = deltaBackoff;
        this.maximumAttempts = maxAttempts;
    }

    /**
     * Determines whether the operation should be retried and specifies the interval until the next retry.
     * 
     * @param retryContext
     *            A {@link RetryContext} object that indicates the number of retries, last request's results, whether
     *            the next retry should happen in the primary or secondary location, and specifies the location mode.
     * @param operationContext
     *            An {@link OperationContext} object for tracking the current operation.
     * @return
     *         A {@link RetryInfo} object that indicates whether the next retry will happen in the primary or secondary
     *         location, and specifies the location mode. If <code>null</code>, the operation will not be retried.
     */
    public abstract RetryInfo evaluate(RetryContext retryContext, OperationContext operationContext);

    /**
     * Determines the time of the last attempt to a storage location and returns a <code>boolean</code> that specifies
     * if a request that was sent to the secondary location failed with 404.
     * 
     * @param retryContext
     *            A {@link RetryContext} object that indicates the number of retries, last request's results, whether
     *            the next retry should happen in the primary or secondary location, and specifies the location mode.
     * @return
     *         <code>true</code> if a request sent to the secondary location fails with 404 (Not Found).
     *         <code>false</code> otherwise.
     */
    protected boolean evaluateLastAttemptAndSecondaryNotFound(RetryContext retryContext) {
        Utility.assertNotNull("retryContext", retryContext);

        // Retry interval of a request to a location must take the time spent sending requests
        // to other locations into account. For example, assume a request was sent to the primary
        // location first, then to the secondary, and then to the primary again. If it
        // was supposed to wait 10 seconds between requests to the primary and the request to
        // the secondary took 3 seconds in total, retry interval should only be 7 seconds. This is because,
        // in total, the requests will be 10 seconds apart from the primary locations' point of view.
        // For this calculation, current instance of the retry policy stores the time of the last
        // request to a specific location.
        if (retryContext.getLastRequestResult().getTargetLocation() == StorageLocation.PRIMARY) {
            this.lastPrimaryAttempt = retryContext.getLastRequestResult().getStopDate();
        }
        else {
            this.lastSecondaryAttempt = retryContext.getLastRequestResult().getStopDate();
        }

        // If a request sent to the secondary location fails with 404 (Not Found), it is possible
        // that the the asynchronous geo-replication for the resource has not completed. So, in case of 404 only in the secondary
        // location, the failure should still be retried.
        return (retryContext.getLastRequestResult().getTargetLocation() == StorageLocation.SECONDARY)
                && (retryContext.getLastRequestResult().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Determines the {@link RetryInfo} object that indicates whether the next retry will happen in the primary or
     * secondary location, and specifies the location mode.
     * 
     * @param retryContext
     *            A {@link RetryContext} object that indicates the number of retries, last request's results, whether
     *            the next retry should happen in the primary or secondary location, and specifies the location mode.
     * 
     * @param secondaryNotFound
     *            A <code>boolean</code> representing whether a request sent to the secondary location failed with 404
     *            (Not Found)
     * @param retryInterval
     *            Backoff Interval.
     * @return
     *         A reference to the {@link RetryInfo} object that indicates whether the next retry will happen in the
     *         primary or secondary location, and specifies the location mode.
     */
    protected RetryInfo evaluateRetryInfo(final RetryContext retryContext, final boolean secondaryNotFound,
            final long retryInterval) {
        RetryInfo retryInfo = new RetryInfo(retryContext);

        // Moreover, in case of 404 when trying the secondary location, instead of retrying on the
        // secondary, further requests should be sent only to the primary location, as it most
        // probably has a higher chance of succeeding there.
        if (secondaryNotFound && (retryContext.getLocationMode() != LocationMode.SECONDARY_ONLY)) {
            retryInfo.setUpdatedLocationMode(LocationMode.PRIMARY_ONLY);
            retryInfo.setTargetLocation(StorageLocation.PRIMARY);
        }

        // Now is the time to calculate the exact retry interval. ShouldRetry call above already
        // returned back how long two requests to the same location should be apart from each other.
        // However, for the reasons explained above, the time spent between the last attempt to
        // the target location and current time must be subtracted from the total retry interval
        // that ShouldRetry returned.
        Date lastAttemptTime = retryInfo.getTargetLocation() == StorageLocation.PRIMARY ? this.lastPrimaryAttempt
                : this.lastSecondaryAttempt;
        if (lastAttemptTime != null) {
            long sinceLastAttempt = (new Date().getTime() - lastAttemptTime.getTime() > 0) ? new Date().getTime()
                    - lastAttemptTime.getTime() : 0;
            retryInfo.setRetryInterval((int) (retryInterval - sinceLastAttempt));
        }
        else {
            retryInfo.setRetryInterval(0);
        }

        return retryInfo;
    }
}
