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
 * Wrapper DTO for Media Services access policies.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AccessPolicyType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "DurationInMinutes", namespace = Constants.ODATA_DATA_NS)
    private Double durationInMinutes;

    @XmlElement(name = "Permissions", namespace = Constants.ODATA_DATA_NS)
    private Integer permissions;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public AccessPolicyType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created
     *            the created to set
     */
    public AccessPolicyType setCreated(Date created) {
        this.created = created;
        return this;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified
     *            the lastModified to set
     */
    public AccessPolicyType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public AccessPolicyType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the durationInMinutes
     */
    public Double getDurationInMinutes() {
        return durationInMinutes;
    }

    /**
     * @param durationInMinutes
     *            the durationInMinutes to set
     */
    public AccessPolicyType setDurationInMinutes(double durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
        return this;
    }

    /**
     * @return the permissions
     */
    public Integer getPermissions() {
        return permissions;
    }

    /**
     * @param permissions
     *            the permissions to set
     */
    public AccessPolicyType setPermissions(Integer permissions) {
        this.permissions = permissions;
        return this;
    }

}
