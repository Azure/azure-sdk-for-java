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
 * Serialization for the AssetFile entity
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class AssetFileType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "ContentFileSize", namespace = Constants.ODATA_DATA_NS)
    private Long contentFileSize;

    @XmlElement(name = "ParentAssetId", namespace = Constants.ODATA_DATA_NS)
    private String parentAssetId;

    @XmlElement(name = "EncryptionVersion", namespace = Constants.ODATA_DATA_NS)
    private String encryptionVersion;

    @XmlElement(name = "EncryptionScheme", namespace = Constants.ODATA_DATA_NS)
    private String encryptionScheme;

    @XmlElement(name = "IsEncrypted", namespace = Constants.ODATA_DATA_NS)
    private Boolean isEncrypted;

    @XmlElement(name = "EncryptionKeyId", namespace = Constants.ODATA_DATA_NS)
    private String encryptionKeyId;

    @XmlElement(name = "InitializationVector", namespace = Constants.ODATA_DATA_NS)
    private String initializationVector;

    @XmlElement(name = "IsPrimary", namespace = Constants.ODATA_DATA_NS)
    private Boolean isPrimary;

    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    @XmlElement(name = "MimeType", namespace = Constants.ODATA_DATA_NS)
    private String mimeType;

    @XmlElement(name = "ContentChecksum", namespace = Constants.ODATA_DATA_NS)
    private String contentChecksum;

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
    public AssetFileType setId(String id) {
        this.id = id;
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
    public AssetFileType setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the contentFileSize
     */
    public Long getContentFileSize() {
        return contentFileSize;
    }

    /**
     * @param contentFileSize
     *            the contentFileSize to set
     */
    public AssetFileType setContentFileSize(Long contentFileSize) {
        this.contentFileSize = contentFileSize;
        return this;
    }

    /**
     * @return the parentAssetId
     */
    public String getParentAssetId() {
        return parentAssetId;
    }

    /**
     * @param parentAssetId
     *            the parentAssetId to set
     */
    public AssetFileType setParentAssetId(String parentAssetId) {
        this.parentAssetId = parentAssetId;
        return this;
    }

    /**
     * @return the encryptionVersion
     */
    public String getEncryptionVersion() {
        return encryptionVersion;
    }

    /**
     * @param encryptionVersion
     *            the encryptionVersion to set
     */
    public AssetFileType setEncryptionVersion(String encryptionVersion) {
        this.encryptionVersion = encryptionVersion;
        return this;
    }

    /**
     * @return the encryptionScheme
     */
    public String getEncryptionScheme() {
        return encryptionScheme;
    }

    /**
     * @param encryptionScheme
     *            the encryptionScheme to set
     */
    public AssetFileType setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
        return this;
    }

    /**
     * @return the isEncrypted
     */
    public Boolean getIsEncrypted() {
        return isEncrypted;
    }

    /**
     * @param isEncrypted
     *            the isEncrypted to set
     */
    public AssetFileType setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
        return this;
    }

    /**
     * @return the encryptionKeyId
     */
    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }

    /**
     * @param encryptionKeyId
     *            the encryptionKeyId to set
     */
    public AssetFileType setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
        return this;
    }

    /**
     * @return the initializationVector
     */
    public String getInitializationVector() {
        return initializationVector;
    }

    /**
     * @param initializationVector
     *            the initializationVector to set
     */
    public AssetFileType setInitializationVector(String initializationVector) {
        this.initializationVector = initializationVector;
        return this;
    }

    /**
     * @return the isPrimary
     */
    public Boolean getIsPrimary() {
        return isPrimary;
    }

    /**
     * @param isPrimary
     *            the isPrimary to set
     */
    public AssetFileType setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
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
    public AssetFileType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
    public AssetFileType setCreated(Date created) {
        this.created = created;
        return this;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType
     *            the mimeType to set
     */
    public AssetFileType setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    /**
     * @return the contentChecksum
     */
    public String getContentChecksum() {
        return contentChecksum;
    }

    /**
     * @param contentChecksum
     *            the contentChecksum to set
     */
    public AssetFileType setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
        return this;
    }
}
