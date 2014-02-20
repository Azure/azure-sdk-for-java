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
package com.microsoft.windowsazure.storage;

import java.util.Date;

import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Represents the options to use while processing a given request.
 */
public abstract class RequestOptions {

    /**
     * The instance of the {@link RetryPolicyFactory} interface to use for the request.
     */
    private RetryPolicyFactory retryPolicyFactory;

    /**
     * The timeout interval, in milliseconds, to use for the request.
     */
    private Integer timeoutIntervalInMs;

    /**
     * The location mode of the request.
     */
    private LocationMode locationMode;

    /**
     * The maximum execution time, in milliseconds, across all potential retries.
     */
    private Integer maximumExecutionTimeInMs;

    /**
     * Represents the start time, in milliseconds, from the client's perspective.
     */
    private Long operationExpiryTime;

    /**
     * Creates an instance of the <code>RequestOptions</code> class.
     */
    public RequestOptions() {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>RequestOptions</code> class by copying values from another
     * <code>RequestOptions</code> instance.
     * 
     * @param other
     *            A <code>RequestOptions</code> object that represents the request options to copy.
     */
    public RequestOptions(final RequestOptions other) {
        if (other != null) {
            this.setRetryPolicyFactory(other.getRetryPolicyFactory());
            this.setTimeoutIntervalInMs(other.getTimeoutIntervalInMs());
            this.setLocationMode(other.getLocationMode());
            this.setMaximumExecutionTimeInMs(other.getMaximumExecutionTimeInMs());
            this.setOperationExpiryTimeInMs(other.getOperationExpiryTimeInMs());
        }
    }

    /**
     * Populates the default timeout, retry policy, and location mode from client if they are null.
     * 
     * @param options
     *            The input options to copy from when applying defaults
     * @param client
     *            the service client to populate from
     */
    protected static final RequestOptions applyBaseDefaultsInternal(RequestOptions modifiedOptions,
            final ServiceClient client) {
        return applyBaseDefaultsInternal(modifiedOptions, client, true);
    }

    /**
     * Populates the default timeout, retry policy, and location mode from client if they are null.
     * 
     * @param options
     *            The input options to copy from when applying defaults
     * @param client
     *            the service client to populate from
     * @param setStartTime
     *            whether to initialize the startTimeInMs field, or not
     */
    protected static final RequestOptions applyBaseDefaultsInternal(RequestOptions modifiedOptions,
            final ServiceClient client, final boolean setStartTime) {
        Utility.assertNotNull("modifiedOptions", modifiedOptions);
        if (modifiedOptions.getRetryPolicyFactory() == null) {
            modifiedOptions.setRetryPolicyFactory(client.getRetryPolicyFactory());
        }

        if (modifiedOptions.getTimeoutIntervalInMs() == null) {
            modifiedOptions.setTimeoutIntervalInMs(client.getTimeoutInMs());
        }

        if (modifiedOptions.getLocationMode() == null) {
            modifiedOptions.setLocationMode(client.getLocationMode());
        }

        if (modifiedOptions.getMaximumExecutionTimeInMs() == null) {
            modifiedOptions.setMaximumExecutionTimeInMs(client.getMaximumExecutionTimeInMs());
        }

        if (modifiedOptions.getMaximumExecutionTimeInMs() != null
                && modifiedOptions.getOperationExpiryTimeInMs() == null && setStartTime) {
            modifiedOptions.setOperationExpiryTimeInMs(new Date().getTime()
                    + modifiedOptions.getMaximumExecutionTimeInMs());
        }

        return modifiedOptions;
    }

    /**
     * Returns the retry policy to use for this request.
     * 
     * @return An {@link RetryPolicyFactory} object that represents the current retry policy.
     * 
     * @see RetryPolicy
     * @see RetryExponentialRetry
     * @see RetryLinearRetry
     * @see RetryNoRetry
     */
    public final RetryPolicyFactory getRetryPolicyFactory() {
        return this.retryPolicyFactory;
    }

    /**
     * Returns the timeout value for this request. For more information about the timeout, see
     * {@link #setTimeoutIntervalInMs(Integer)}.
     * 
     * @return The current timeout value, in milliseconds, for this request.
     */
    public final Integer getTimeoutIntervalInMs() {
        return this.timeoutIntervalInMs;
    }

    /**
     * Gets the default location mode for this request.
     * 
     * @return A {@link LocationMode} object that represents the location mode for this request.
     */
    public final LocationMode getLocationMode() {
        return this.locationMode;
    }

    /**
     * Returns the maximum execution time for this request. For more information about maximum execution time, see
     * {@link #setMaximumExecutionTimeInMs(Integer)}.
     * 
     * @return The current maximum execution time, in milliseconds, for this request.
     */
    public Integer getMaximumExecutionTimeInMs() {
        return maximumExecutionTimeInMs;
    }

    /**
     * Returns the time at which this operation expires. This is computed by adding the time the operation begins and
     * the maximum execution time and will be null if maximum execution time is null. For more information about maximum
     * execution time, see {@link #setMaximumExecutionTimeInMs(Integer)}.
     * 
     * @return The current operation expiry time, in milliseconds, for this request.
     */
    public Long getOperationExpiryTimeInMs() {
        return this.operationExpiryTime;
    }

    /**
     * Sets the RetryPolicyFactory object to use for this request.
     * 
     * @param retryPolicyFactory
     *            the RetryPolicyFactory object to use when making service requests.
     * 
     * @see RetryPolicy
     * @see RetryExponentialRetry
     * @see RetryLinearRetry
     * @see RetryNoRetry
     */
    public final void setRetryPolicyFactory(final RetryPolicyFactory retryPolicyFactory) {
        this.retryPolicyFactory = retryPolicyFactory;
    }

    /**
     * Sets the timeout to use when making this request.
     * <p>
     * The server timeout interval begins at the time that the complete request has been received by the service, and
     * the server begins processing the response. If the timeout interval elapses before the response is returned to the
     * client, the operation times out. The timeout interval resets with each retry, if the request is retried.
     * 
     * The default timeout interval for a request is the timeout set in the client (by default 90 seconds). You can
     * change the timeout interval on this request by setting this property. You can also change the value at
     * {@link ServiceClient#setTimeoutInMs(int)} so that all subsequent requests made via the service client will use
     * the new timeout interval.
     * 
     * If you are downloading a large blob, you should increase the value of the timeout beyond the default value.
     * 
     * @param timeoutInMs
     *            The timeout, in milliseconds, to use for this request.
     */
    public final void setTimeoutIntervalInMs(final Integer timeoutIntervalInMs) {
        this.timeoutIntervalInMs = timeoutIntervalInMs;
    }

    /**
     * Sets the {@link LocationMode} for this request.
     * 
     * @param locationMode
     *            the locationMode to set
     */
    public final void setLocationMode(final LocationMode locationMode) {
        this.locationMode = locationMode;
    }

    /**
     * Sets the maximum execution time to use when making this request.
     * <p>
     * The maximum execution time interval begins at the time that the client begins building the request. The maximum
     * execution time is checked intermittently while uploading data, downloading data, and before executing retries.
     * The service will continue to upload, download, and retry until the maximum execution time is reached. At that
     * time, any partial uploads or downloads will be cancelled and an exception will be thrown.
     * 
     * The default maximum execution is set in the client and is by default null, indicating no maximum time. You can
     * change the maximum execution time on this request by setting this property. You can also change the value at
     * {@link ServiceClient#setMaximumExecutionTimeInMs(Integer)} so that all subsequent requests made via the service
     * client will use the maximum execution time.
     * 
     * @param maximumExecutionTimeInMs
     *            The maximum execution time, in milliseconds, to use for this request.
     */
    public void setMaximumExecutionTimeInMs(Integer maximumExecutionTimeInMs) {
        this.maximumExecutionTimeInMs = maximumExecutionTimeInMs;
    }

    /**
     * @param operationExpiryTime
     *            the operationExpiryTime to set
     */
    private void setOperationExpiryTimeInMs(final Long operationExpiryTime) {
        this.operationExpiryTime = operationExpiryTime;
    }
}
