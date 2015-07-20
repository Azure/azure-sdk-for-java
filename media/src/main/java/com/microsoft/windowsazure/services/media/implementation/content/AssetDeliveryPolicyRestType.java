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
public class AssetDeliveryPolicyRestType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;
    
    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "AssetDeliveryProtocol", namespace = Constants.ODATA_DATA_NS)
    private Integer assetDeliveryProtocol;
    
    @XmlElement(name = "AssetDeliveryPolicyType", namespace = Constants.ODATA_DATA_NS)
    private Integer assetDeliveryPolicyType;
    
    @XmlElement(name = "AssetDeliveryConfiguration", namespace = Constants.ODATA_DATA_NS)
    private String assetDeliveryConfiguration;

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
    public AssetDeliveryPolicyRestType setId(String id) {
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
    public AssetDeliveryPolicyRestType setCreated(Date created) {
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
    public AssetDeliveryPolicyRestType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * @return the alternateId
     */
    public Integer getAssetDeliveryProtocol() {
        return assetDeliveryProtocol;
    }

    /**
     * @param alternateId
     *            the alternateId to set
     */
    public AssetDeliveryPolicyRestType setAssetDeliveryProtocol(Integer assetDeliveryProtocol) {
        this.assetDeliveryProtocol = assetDeliveryProtocol;
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
    public AssetDeliveryPolicyRestType setName(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * @return the asset delivery policy type
     */
    public Integer getAssetDeliveryPolicyType() {
        return assetDeliveryPolicyType;
    }

    /**
     * @param assetDeliveryPolicyType
     *            the asset delivery policy type
     */
    public AssetDeliveryPolicyRestType setAssetDeliveryPolicyType(Integer assetDeliveryPolicyType) {
        this.assetDeliveryPolicyType = assetDeliveryPolicyType;
        return this;
    }
    
    /**
     * @return the asset delivery configuration
     */
    public String getAssetDeliveryConfiguration() {
        return assetDeliveryConfiguration;
    }

    /**
     * @param assetDeliveryConfiguration
     *            the asset delivery configuration to set
     */
    public AssetDeliveryPolicyRestType setAssetDeliveryConfiguration(String assetDeliveryConfiguration) {
        this.assetDeliveryConfiguration = assetDeliveryConfiguration;
        return this;
    }

    
}
