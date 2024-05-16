/*
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

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;

/**
 * The Class TaskHistoricalEvent.
 */
public class TaskHistoricalEvent {

    /** The code. */
    private final String code;

    /** The message. */
    private final String message;

    /** The time stamp. */
    private final Date timeStamp;

    /**
     * Instantiates a new task historical event.
     * 
     * @param code
     *            the code
     * @param message
     *            the message
     * @param timeStamp
     *            the time stamp
     */
    public TaskHistoricalEvent(String code, String message, Date timeStamp) {
        this.code = code;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Gets the message.
     * 
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the time stamp.
     * 
     * @return the time stamp
     */
    public Date getTimeStamp() {
        return this.timeStamp;
    }
}
