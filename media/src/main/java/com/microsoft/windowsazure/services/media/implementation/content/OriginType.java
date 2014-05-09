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

/**
 * This type maps the XML returned in the odata ATOM serialization for Asset
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class OriginType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    /** The name. */
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    /** The description. */
    @XmlElement(name = "Description", namespace = Constants.ODATA_DATA_NS)
    private String description;

    /** The hostName. */
    @XmlElement(name = "HostName", namespace = Constants.ODATA_DATA_NS)
    private String hostName;

    /** The created. */
    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    /** The last modified. */
    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    /** The state. */
    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    private String state;

    /** The reserved units. */
    @XmlElement(name = "ReservedUnits", namespace = Constants.ODATA_DATA_NS)
    private int reservedUnits;

    /** The settings. */
    @XmlElement(name = "Settings", namespace = Constants.ODATA_DATA_NS)
    private String settings;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id
     * @return the origin type
     */
    public OriginType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name to set
     * @return the origin type
     */
    public OriginType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the description
     * @return the origin type
     */
    public OriginType setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the host name.
     * 
     * @return the host name
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the host name.
     * 
     * @param host
     *            name the host name to set
     * @return the origin type
     */
    public OriginType setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the created.
     * 
     * @param created
     *            the created
     * @return the origin type
     */
    public OriginType setCreated(Date created) {
        this.created = created;
        return this;
    }

    /**
     * Gets the last modified.
     * 
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified.
     * 
     * @param lastModified
     *            the last modified
     * @return the origin type
     */
    public OriginType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the state
     * @return the origin type
     */
    public OriginType setState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public String getState() {
        return this.state;
    }

    /**
     * Sets the reserved units.
     * 
     * @param reservedUnits
     *            the reserved units
     * @return the origin type
     */
    public OriginType setReservedUnits(int reservedUnits) {
        this.reservedUnits = reservedUnits;
        return this;
    }

    /**
     * Gets the reserved units.
     * 
     * @return the reserved units
     */
    public int getReservedUnits() {
        return this.reservedUnits;
    }

    /**
     * Gets the settings.
     * 
     * @return the settings
     */
    public String getSettings() {
        return this.settings;
    }

    /**
     * Sets the settings.
     * 
     * @param settings
     *            the settings
     * @return the origin type
     */
    public OriginType setSettings(String settings) {
        this.settings = settings;
        return this;
    }
}
