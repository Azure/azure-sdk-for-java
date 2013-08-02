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
 * This type maps the URI.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "NotificationEndPointType", namespace = Constants.ODATA_DATA_NS)
public class NotificationEndPointType implements MediaServiceDTO {
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    @XmlElement(name = "EndpointType", namespace = Constants.ODATA_DATA_NS)
    private int endpointType;

    @XmlElement(name = "EndpointAddress", namespace = Constants.ODATA_DATA_NS)
    private String endpointAddress;

    /**
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            id
     *            the id to set
     */
    public NotificationEndPointType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            name
     *            the name to set
     */
    public NotificationEndPointType setName(String name) {
        this.name = name;
        return this;
    }

    public Date getCreated() {
        return this.created;
    }

    public NotificationEndPointType setCreated(Date created) {
        this.created = created;
        return this;
    }

    public int getEndPointType() {
        return this.endpointType;
    }

    public NotificationEndPointType setEndPointType(int endpointType) {
        this.endpointType = endpointType;
        return this;
    }

    public String getEndPointAddress() {
        return this.endpointAddress;
    }

    public NotificationEndPointType setEndPointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
        return this;
    }

}
