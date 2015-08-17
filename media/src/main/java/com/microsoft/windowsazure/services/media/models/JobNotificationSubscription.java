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

/**
 * The Class JobNotificationSubscription.
 */
public class JobNotificationSubscription {

    /** The notification end point id. */
    private final String notificationEndPointId;

    /** The target job state. */
    private final TargetJobState targetJobState;

    /**
     * Instantiates a new job notification subscription.
     * 
     * @param uuid
     *            the notification end point id
     * @param targetJobState
     *            the target job state
     */
    public JobNotificationSubscription(String notificationEndPointId,
            TargetJobState targetJobState) {
        this.notificationEndPointId = notificationEndPointId;
        this.targetJobState = targetJobState;
    }

    /**
     * Gets the notification end point.
     * 
     * @return the code
     */
    public String getNotificationEndPointId() {
        return this.notificationEndPointId;
    }

    /**
     * Gets the message.
     * 
     * @return the message
     */
    public TargetJobState getTargetJobState() {
        return this.targetJobState;
    }
}
