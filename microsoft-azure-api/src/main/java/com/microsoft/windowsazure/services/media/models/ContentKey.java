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

public class ContentKey {
    private String id;
    private Date created;
    private Date lastModified;
    private ContentKeyType contentKeyType;
    private String encryptedContentKey;
    private String name;
    private String protectionKeyId;
    private String checkSum;
    private ProtectionKeyType protectionKeyType;

    public String getId() {
        return this.id;
    }

    public ContentKey setId(String id) {
        this.id = id;
        return this;
    }

    public ContentKey setCreate(Date created) {
        this.created = created;
        return this;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public ContentKey setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public ContentKey setName(String name) {
        this.name = name;
        return this;
    }

    public ContentKey setCheckSum(String checkSum) {
        this.checkSum = checkSum;
        return this;
    }

    public String getCheckSum() {
        return this.checkSum;
    }

    public ContentKey setProtectionKeyType(ProtectionKeyType protectionKeyType) {
        this.protectionKeyType = protectionKeyType;
        return this;
    }

    public ProtectionKeyType getProtectionKeyType() {
        return this.protectionKeyType;
    }

    public ContentKey setProtectionKeyId(String protectionKeyId) {
        this.protectionKeyId = protectionKeyId;
        return this;
    }

    public String getProtectionKeyId() {
        return this.protectionKeyId;
    }

    public ContentKey setEncryptedContentKey(String encryptedContentKey) {
        this.encryptedContentKey = encryptedContentKey;
        return this;
    }

    public String getEncryptedContentKey() {
        return this.encryptedContentKey;
    }

    public ContentKey setContentKeyType(ContentKeyType contentKeyType) {
        this.contentKeyType = contentKeyType;
        return this;
    }

    public ContentKeyType getContentKeyType() {
        return this.contentKeyType;
    }

    public ContentKey setCreated(Date created) {
        this.created = created;
        return this;
    }

    public Date getCreated() {
        return this.created;
    }

}
