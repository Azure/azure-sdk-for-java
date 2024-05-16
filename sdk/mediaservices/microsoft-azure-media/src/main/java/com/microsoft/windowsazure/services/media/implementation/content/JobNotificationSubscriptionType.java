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

package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This type maps the XML returned in the odata ATOM serialization for job
 * notification subscription.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobNotificationSubscriptionType implements MediaServiceDTO {

    /** The ID of the notification end point. */
    @XmlElement(name = "NotificationEndPointId", namespace = Constants.ODATA_DATA_NS)
    private String notificationEndPointId;

    /** The target state of the job. */
    @XmlElement(name = "TargetJobState", namespace = Constants.ODATA_DATA_NS)
    private int targetJobState;

    /**
     * Gets the ID of the notification end point.
     * 
     * @return the ID of the notification end point.
     */
    public String getNotificationEndPointId() {
        return this.notificationEndPointId;
    }

    /**
     * Sets the ID of the notification end point.
     * 
     * @param notificationEndPointId
     *            the ID of the notification end point to set
     * @return the job notification subscription type
     */
    public JobNotificationSubscriptionType setNotificationEndPointId(
            String notificationEndPointId) {
        this.notificationEndPointId = notificationEndPointId;
        return this;
    }

    /**
     * Gets the target job state.
     * 
     * @return an integer representing the target job state.
     */
    public int getTargetJobState() {
        return targetJobState;
    }

    /**
     * Sets the target job state.
     * 
     * @param targetJobState
     *            the target job state
     * @return the target job state
     */
    public JobNotificationSubscriptionType setTargetJobState(int targetJobState) {
        this.targetJobState = targetJobState;
        return this;
    }

}
