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
 * This type maps the XML returned in the odata ATOM serialization
 * for ErrorDetail entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobNotificationSubscriptionType implements MediaServiceDTO {

    /** The message. */
    @XmlElement(name = "NotificationEndPointId", namespace = Constants.ODATA_DATA_NS)
    protected String notificationEndPointId;

    /** The time stamp. */
    @XmlElement(name = "TargetJobState", namespace = Constants.ODATA_DATA_NS)
    protected int targetJobState;

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public String getNotificationEndPointId() {
        return this.notificationEndPointId;
    }

    /**
     * Sets the code.
     * 
     * @param code
     *            the id to set
     * @return the error detail type
     */
    public JobNotificationSubscriptionType setNotificationEndPointId(String notificationEndPointId) {
        this.notificationEndPointId = notificationEndPointId;
        return this;
    }

    /**
     * Gets the message.
     * 
     * @return the message
     */
    public int getTargetJobState() {
        return targetJobState;
    }

    /**
     * Sets the message.
     * 
     * @param message
     *            the message to set
     * @return the error detail type
     */
    public JobNotificationSubscriptionType setTargetJobState(int targetJobState) {
        this.targetJobState = targetJobState;
        return this;
    }

}
