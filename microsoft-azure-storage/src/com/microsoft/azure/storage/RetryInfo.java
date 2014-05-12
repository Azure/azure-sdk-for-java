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
public class RetryInfo {

    /**
     * The location that the next retry should target.
     */
    private StorageLocation targetLocation;

    /**
     * The location mode for subsequent retries.
     */
    private LocationMode updatedLocationMode;

    /**
     * The interval in milliseconds until the next retry. The minimum back-off interval is 3 seconds.
     */
    private int retryInterval = 3000;

    /**
     * Initializes a new instance of the {@link RetryInfo} class.
     */
    public RetryInfo() {
        this.targetLocation = StorageLocation.PRIMARY;
        this.updatedLocationMode = LocationMode.PRIMARY_ONLY;
    }

    /**
     * Initializes a new instance of the {@link "RetryInfo"} class.
     * 
     * @param retryContext
     *            The {@link RetryContext} object that was passed in to the retry policy.
     */
    public RetryInfo(RetryContext retryContext) {
        Utility.assertNotNull("retryContext", retryContext);
        this.targetLocation = retryContext.getNextLocation();
        this.updatedLocationMode = retryContext.getLocationMode();
    }

    /**
     * Gets the interval in milliseconds until the next retry. The minimum back-off interval is 3 seconds.
     * 
     * @return An <code>int</code> which represents the retry interval (in milliseconds).
     */
    public int getRetryInterval() {
        return this.retryInterval;
    }

    /**
     * Gets the location that the next retry should target.
     * 
     * @return A {@link StorageLocation} object which represents the location for the next retry.
     */
    public final StorageLocation getTargetLocation() {
        return this.targetLocation;
    }

    /**
     * Gets the location mode for subsequent retries.
     * 
     * @return A {@link LocationMode} object which represents the location mode for subsequent retries.
     */
    public LocationMode getUpdatedLocationMode() {
        return this.updatedLocationMode;
    }

    /**
     * Sets the interval in milliseconds until the next retry. The minimum back-off interval is 3 seconds.
     * 
     * @param retryInterval
     *        An <code>int</code> which represents the retry interval to set.
     */
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = (retryInterval > 0 ? retryInterval : 0);
    }

    /**
     * Sets the location that the next retry should target.
     * 
     * @param targetLocation
     *        A {@link StorageLocation} object which represents the location to set.
     */
    public void setTargetLocation(StorageLocation targetLocation) {
        this.targetLocation = targetLocation;
    }

    /**
     * Sets the location mode for subsequent retries.
     * 
     * @param updatedLocationMode
     *        A {@link LocationMode} object which represents the the location mode to set.
     */
    public void setUpdatedLocationMode(LocationMode updatedLocationMode) {
        this.updatedLocationMode = updatedLocationMode;
    }

    /**
     * Returns a string that represents the current {@link "RetryInfo"} instance.
     * 
     * @return A <code>String</code> which represents the current <code>RetryInfo</code> instance.
     */
    @Override
    public String toString() {
        return String.format(Utility.LOCALE_US, "(%s,%s)", this.targetLocation, this.retryInterval);
    }

}
