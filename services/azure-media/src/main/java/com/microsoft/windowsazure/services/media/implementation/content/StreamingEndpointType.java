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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;


/**
 * This type maps the XML returned in the odata ATOM serialization for Asset
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamingEndpointType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;
    
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;
    
    @XmlElement(name = "Description", namespace = Constants.ODATA_DATA_NS)
    private String description;

    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;
    
    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    private String state;

    @XmlElement(name = "HostName", namespace = Constants.ODATA_DATA_NS)
    private String hostName;

    @XmlElement(name = "ScaleUnits", namespace = Constants.ODATA_DATA_NS)
    private Integer scaleUnits;
    
    @XmlElement(name = "CdnEnabled", namespace = Constants.ODATA_DATA_NS)
    private boolean cdnEnabled;
    
    @XmlElementWrapper(name = "CustomHostNames", namespace = Constants.ODATA_DATA_NS)
    @XmlElement(name = "CustomHostName", namespace = Constants.ODATA_DATA_NS)
    private List<String> customHostName;
    
    @XmlElement(name = "AccessControl", namespace = Constants.ODATA_DATA_NS)
    private StreamingEndpointAccessControlType streamingEndpointAccessControl;

    @XmlElement(name = "CacheControl", namespace = Constants.ODATA_DATA_NS)
    private StreamingEndpointCacheControlType streamingEndpointCacheControl;
    
    @XmlElement(name = "CrossSiteAccessPolicies", namespace = Constants.ODATA_DATA_NS)
    private CrossSiteAccessPoliciesType crossSiteAccessPolicies;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getScaleUnits() {
        return scaleUnits;
    }

    public void setScaleUnits(int scaleUnits) {
        this.scaleUnits = scaleUnits;
    }

    public List<String> getCustomHostName() {
        return customHostName;
    }

    public void setCustomHostName(List<String> customHostName) {
        this.customHostName = customHostName;
    }

    public boolean isCdnEnabled() {
        return cdnEnabled;
    }

    public void setCdnEnabled(boolean cdnEnabled) {
        this.cdnEnabled = cdnEnabled;
    }

    public StreamingEndpointAccessControlType getAccessControl() {
        return streamingEndpointAccessControl;
    }

    public void setAccessControl(StreamingEndpointAccessControlType streamingEndpointAccessControl) {
        this.streamingEndpointAccessControl = streamingEndpointAccessControl;
    }

    public StreamingEndpointCacheControlType getCacheControl() {
        return streamingEndpointCacheControl;
    }

    public void setCacheControl(StreamingEndpointCacheControlType streamingEndpointCacheControl) {
        this.streamingEndpointCacheControl = streamingEndpointCacheControl;
    }

    public CrossSiteAccessPoliciesType getCrossSiteAccessPolicies() {
        return crossSiteAccessPolicies;
    }

    public void setCrossSiteAccessPolicies(CrossSiteAccessPoliciesType crossSiteAccessPolicies) {
        this.crossSiteAccessPolicies = crossSiteAccessPolicies;
    }
}
