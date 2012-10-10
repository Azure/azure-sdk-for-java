/**
 * Copyright 2012 Microsoft Corporation
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
package com.microsoft.windowsazure.services.media.models;

import java.util.Date;

/**
 * The Class UpdateLocatorOptions.
 */
public class UpdateLocatorOptions {

    /** The expiration date time. */
    private Date expirationDateTime;

    /** The start time. */
    private Date startTime;

    /**
     * Gets the expiration date time.
     * 
     * @return the expiration date time
     */
    public Date getExpirationDateTime() {
        return expirationDateTime;
    }

    /**
     * Sets the expiration date time.
     * 
     * @param expirationDateTime
     *            the expiration date time
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setExpirationDateTime(Date expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
        return this;
    }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     * 
     * @param startTime
     *            the start time
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

}