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
package com.microsoft.azure.storage;

import com.microsoft.azure.storage.core.Utility;

/**
 * Represents the context for a retry of a request made against the storage services.
 */
public final class RetryContext {
    /**
     * The location that the next retry should target.
     */
    private final StorageLocation nextLocation;

    /**
     * The location mode for subsequent retries.
     */
    private final LocationMode locationMode;

    /**
     * The number of retries for the given operation.
     */
    private final int currentRetryCount;

    /**
     * The last request's results.
     */
    private final RequestResult lastRequestResult;

    /**
     * Creates an instance of the <code>RequestResult</code> class.
     * 
     * @param currentRetryCount
     *            An <code>int</code> which represents the number of retries for the given operation.
     * @param lastRequestResult
     *            A {@link RequestResult} object which represents the last request's results.
     * @param nextLocation
     *            A {@link StorageLocation} object which represents the location mode for subsequent retries.
     * @param locationMode
     *            A {@link LocationMode} value which represents the location mode for subsequent retries.
     */
    public RetryContext(int currentRetryCount, RequestResult lastRequestResult, StorageLocation nextLocation,
            LocationMode locationMode) {
        this.currentRetryCount = currentRetryCount;
        this.lastRequestResult = lastRequestResult;
        this.nextLocation = nextLocation;
        this.locationMode = locationMode;
    }

    /**
     * Gets the number of retries for the given operation.
     * 
     * @return An <code>int</code> which represents the number of retries for the given operation.
     */
    public int getCurrentRetryCount() {
        return this.currentRetryCount;
    }

    /**
     * Gets the last request's results.
     * 
     * @return A {@link RequestResult} object which represents the last request's results.
     */
    public RequestResult getLastRequestResult() {
        return this.lastRequestResult;
    }

    /**
     * Gets the location mode for subsequent retries.
     * 
     * @return A {@link LocationMode} value which represents the location mode for subsequent retries.
     */
    public LocationMode getLocationMode() {
        return this.locationMode;
    }

    /**
     * Gets the location that the next retry should target.
     * 
     * @return A {@link StorageLocation} object which represents the location for subsequent retries.
     */
    public StorageLocation getNextLocation() {
        return this.nextLocation;
    }

    /**
     * Returns a string that represents the current {@link RetryContext} instance.
     * 
     * @return An <code>String</code> which represents the current {@link RetryContext} instance.
     */
    @Override
    public String toString() {
        return String.format(Utility.LOCALE_US, "(%s,%s)", this.currentRetryCount, this.locationMode);
    }

}
