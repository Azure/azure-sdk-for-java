/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.models;

public class SetBlobPropertiesOptions extends BlobServiceOptions {
    private String leaseId;
    private String contentType;
    private Long contentLength;
    private String contentEncoding;
    private String contentLanguage;
    private String contentMD5;
    private String cacheControl;
    private String sequenceNumberAction;
    private Long sequenceNumber;
    private AccessCondition accessCondition;

    @Override
    public SetBlobPropertiesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public SetBlobPropertiesOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public SetBlobPropertiesOptions setContentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public SetBlobPropertiesOptions setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public SetBlobPropertiesOptions setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public SetBlobPropertiesOptions setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public SetBlobPropertiesOptions setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public SetBlobPropertiesOptions setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public SetBlobPropertiesOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public String getSequenceNumberAction() {
        return sequenceNumberAction;
    }

    public SetBlobPropertiesOptions setSequenceNumberAction(String sequenceNumberAction) {
        this.sequenceNumberAction = sequenceNumberAction;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public SetBlobPropertiesOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
