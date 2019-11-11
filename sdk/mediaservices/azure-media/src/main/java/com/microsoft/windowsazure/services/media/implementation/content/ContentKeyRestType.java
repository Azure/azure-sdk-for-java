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
public class ContentKeyRestType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    /** The created. */
    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    /** The last modified. */
    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    /** The content key type. */
    @XmlElement(name = "ContentKeyType", namespace = Constants.ODATA_DATA_NS)
    private Integer contentKeyType;

    /** The encrypted content key. */
    @XmlElement(name = "EncryptedContentKey", namespace = Constants.ODATA_DATA_NS)
    private String encryptedContentKey;

    /** The name. */
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    /** The protection key id. */
    @XmlElement(name = "ProtectionKeyId", namespace = Constants.ODATA_DATA_NS)
    private String protectionKeyId;

    /** The protection key type. */
    @XmlElement(name = "ProtectionKeyType", namespace = Constants.ODATA_DATA_NS)
    private Integer protectionKeyType;

    /** The checksum. */
    @XmlElement(name = "Checksum", namespace = Constants.ODATA_DATA_NS)
    private String checksum;
    
    /** The authorization policy id . */
    @XmlElement(name = "AuthorizationPolicyId", namespace = Constants.ODATA_DATA_NS)
    private String authorizationPolicyId;


    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id to set
     * @return the content key rest type
     */
    public ContentKeyRestType setId(String id) {
        this.id = id;
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
     *            the created to set
     * @return the content key rest type
     */
    public ContentKeyRestType setCreated(Date created) {
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
     *            the lastModified to set
     * @return the content key rest type
     */
    public ContentKeyRestType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Gets the content key type.
     * 
     * @return the content key type
     */
    public Integer getContentKeyType() {
        return contentKeyType;
    }

    /**
     * Sets the content key type.
     * 
     * @param contentKeyType
     *            the new content key type
     * @return the content key rest type
     */
    public ContentKeyRestType setContentKeyType(Integer contentKeyType) {
        this.contentKeyType = contentKeyType;
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
     * @return the content key rest type
     */
    public ContentKeyRestType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the checksum.
     * 
     * @param checksum
     *            the new checksum
     * @return the content key rest type
     */
    public ContentKeyRestType setChecksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    /**
     * Gets the checksum.
     * 
     * @return the checksum
     */
    public String getChecksum() {
        return this.checksum;
    }

    /**
     * Sets the protection key type.
     * 
     * @param protectionKeyType
     *            the new protection key type
     * @return the content key rest type
     */
    public ContentKeyRestType setProtectionKeyType(Integer protectionKeyType) {
        this.protectionKeyType = protectionKeyType;
        return this;
    }

    /**
     * Gets the protection key type.
     * 
     * @return the protection key type
     */
    public Integer getProtectionKeyType() {
        return this.protectionKeyType;
    }

    /**
     * Sets the protection key id.
     * 
     * @param protectionKeyId
     *            the new protection key id
     * @return the content key rest type
     */
    public ContentKeyRestType setProtectionKeyId(String protectionKeyId) {
        this.protectionKeyId = protectionKeyId;
        return this;
    }

    /**
     * Gets the protection key id.
     * 
     * @return the protection key id
     */
    public String getProtectionKeyId() {
        return this.protectionKeyId;
    }

    /**
     * Sets the encrypted content key.
     * 
     * @param encryptedContentKey
     *            the encrypted content key
     * @return the content key rest type
     */
    public ContentKeyRestType setEncryptedContentKey(String encryptedContentKey) {
        this.encryptedContentKey = encryptedContentKey;
        return this;
    }

    /**
     * Gets the encrypted content key.
     * 
     * @return the encrypted content key
     */
    public String getEncryptedContentKey() {
        return this.encryptedContentKey;
    }
    

    /**
     * Sets the authorization policy id.
     * 
     * @param authorizationPolicyId
     *            the authorization policy id
     * @return the content key rest type
     */
    public ContentKeyRestType setAuthorizationPolicyId(String authorizationPolicyId) {
        this.authorizationPolicyId = authorizationPolicyId;
        return this;
    }

    /**
     * Gets the the authorization policy id.
     * 
     * @return the authorization policy id
     */
    public String getAuthorizationPolicyId() {
        return authorizationPolicyId;
    }
}
