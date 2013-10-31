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

package com.microsoft.windowsazure.services.management.models;

import java.util.Date;

/**
 * The base result class for all the result of service management operation.
 * 
 */
public class UpdateAffinityGroupResult extends OperationResult {

    /** The region. */
    private String region;

    /** The date. */
    private Date date;

    /**
     * Instantiates a new update affinity group result.
     * 
     * @param statusCode
     *            the status code
     * @param requestId
     *            the request id
     */
    public UpdateAffinityGroupResult(int statusCode, String requestId) {
        super(statusCode, requestId);
    }

    /**
     * Sets the region.
     * 
     * @param region
     *            the region
     * @return the update affinity group result
     */
    public UpdateAffinityGroupResult setRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * Sets the date.
     * 
     * @param date
     *            the date
     * @return the update affinity group result
     */
    public UpdateAffinityGroupResult setDate(Date date) {
        this.date = date;
        return this;
    }

    /**
     * Gets the region.
     * 
     * @return the region
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * Gets the date.
     * 
     * @return the date
     */
    public Date getDate() {
        return this.date;
    }
}
