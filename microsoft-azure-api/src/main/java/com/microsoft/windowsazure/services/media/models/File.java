/*
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;

public class File {
    private String id;
    private String name;
    private int contentFileSize;
    private String parentAssetId;
    private String encryptionVersion;
    private String encryptionScheme;
    private Boolean isEncrypted;
    private String encryptionKeyId;
    private String initializationVector;
    private Boolean isPrimary;
    private Date lastModified;
    private Date created;
    private String mimeType;
    private String contentChecksum;

    public String getId() {
        return this.id;
    }

    public File setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public File setName(String name) {
        this.name = name;
        return this;
    }

    public int getContentFileSize() {
        return this.contentFileSize;
    }

    public File setContentFileSize(int contentFileSize) {
        this.contentFileSize = contentFileSize;
        return this;
    }

    public String getParentAssetId() {
        return this.parentAssetId;
    }

    public File setParentAssetId(String parentAssetId) {
        this.parentAssetId = parentAssetId;
        return this;
    }

    public Date getCreated() {
        return this.created;
    }

    public File setCreate(Date created) {
        this.created = created;
        return this;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public File setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public File setEncryptionVersion(String encryptionVersion) {
        this.encryptionVersion = encryptionVersion;
        return this;
    }

    public String getEncryptionVersion() {
        return this.encryptionVersion;
    }

    public File setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
        return this;
    }

    public String getEncryptionScheme() {
        return this.encryptionScheme;
    }

    public File setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
        return this;
    }

    public Boolean getIsEncrypted() {
        return this.isEncrypted;
    }

    public File setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
        return this;
    }

    public String getEncryptionKeyId() {
        return this.encryptionKeyId;
    }

    public File setInitializationVector(String expectedInitializationVector) {
        this.initializationVector = expectedInitializationVector;
        return this;
    }

    public String getInitializationVector() {
        return this.initializationVector;
    }

    public File setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
        return this;
    }

    public Boolean getIsPrimary() {
        return this.isPrimary;
    }

    public File setCreated(Date created) {
        this.created = created;
        return this;
    }

    public File setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public File setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
        return this;
    }

    public String getContentChecksum() {
        return this.contentChecksum;
    }

}
