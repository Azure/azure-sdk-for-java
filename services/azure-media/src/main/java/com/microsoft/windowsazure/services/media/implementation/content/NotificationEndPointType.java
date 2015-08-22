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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The type of notification end point.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NotificationEndPointType", namespace = Constants.ODATA_DATA_NS)
public class NotificationEndPointType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    /** The name. */
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    /** The created. */
    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    /** The end point type. */
    @XmlElement(name = "EndPointType", namespace = Constants.ODATA_DATA_NS)
    private int endPointType;

    /** The end point address. */
    @XmlElement(name = "EndPointAddress", namespace = Constants.ODATA_DATA_NS)
    private String endPointAddress;

    /**
     * Gets the id.
     * 
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            id the id to set
     * @return the notification end point type
     */
    public NotificationEndPointType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            name the name to set
     * @return the notification end point type
     */
    public NotificationEndPointType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return this.created;
    }

    /**
     * Sets the created.
     * 
     * @param created
     *            the created
     * @return the notification end point type
     */
    public NotificationEndPointType setCreated(Date created) {
        this.created = created;
        return this;
    }

    /**
     * Gets the end point type.
     * 
     * @return the end point type
     */
    public int getEndPointType() {
        return this.endPointType;
    }

    /**
     * Sets the end point type.
     * 
     * @param endpointType
     *            the endpoint type
     * @return the notification end point type
     */
    public NotificationEndPointType setEndPointType(int endpointType) {
        this.endPointType = endpointType;
        return this;
    }

    /**
     * Gets the end point address.
     * 
     * @return the end point address
     */
    public String getEndPointAddress() {
        return this.endPointAddress;
    }

    /**
     * Sets the end point address.
     * 
     * @param endpointAddress
     *            the endpoint address
     * @return the notification end point type
     */
    public NotificationEndPointType setEndPointAddress(String endpointAddress) {
        this.endPointAddress = endpointAddress;
        return this;
    }

}
