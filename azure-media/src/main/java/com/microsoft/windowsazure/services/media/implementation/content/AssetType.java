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
public class AssetType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    private Integer state;

    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    @XmlElement(name = "AlternateId", namespace = Constants.ODATA_DATA_NS)
    private String alternateId;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "Options", namespace = Constants.ODATA_DATA_NS)
    private Integer options;

    @XmlElement(name = "Uri", namespace = Constants.ODATA_DATA_NS)
    private String uri;    
    
    @XmlElement(name = "StorageAccountName", namespace = Constants.ODATA_DATA_NS)
    private String storageAccountName;

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
    public AssetType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the state
     */
    public Integer getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public AssetType setState(Integer state) {
        this.state = state;
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
    public AssetType setCreated(Date created) {
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
    public AssetType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * @return the alternateId
     */
    public String getAlternateId() {
        return alternateId;
    }

    /**
     * @param alternateId
     *            the alternateId to set
     */
    public AssetType setAlternateId(String alternateId) {
        this.alternateId = alternateId;
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
    public AssetType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the options
     */
    public Integer getOptions() {
        return options;
    }

    /**
     * @param options
     *            the options to set
     */
    public AssetType setOptions(Integer options) {
        this.options = options;
        return this;
    }

    /**
     * @return The Name of the storage account that contains the assetâ€™s blob container.
     */
    public String getStorageAccountName() {
        return storageAccountName;
    }

    /**
     * @param storageAccountName 
     *              Name of the storage account that contains the assetâ€™s blob container.
     */
    public void setStorageAccountName(String storageAccountName) {
        this.storageAccountName = storageAccountName;
    }

    /**
     * @return The URI of the blob storage container of the specified Asset
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param The URI of the blob storage container of the specified Asset
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
