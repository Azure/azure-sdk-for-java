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
package com.microsoft.azure.storage;

import java.net.URI;

public class Tenant {
    private String tenantName;
    private String accountName;
    private String accountKey;
    private URI blobServiceEndpoint;
    private URI fileServiceEndpoint;
    private URI queueServiceEndpoint;
    private URI tableServiceEndpoint;
    private URI blobServiceSecondaryEndpoint;
    private URI fileServiceSecondaryEndpoint;
    private URI queueServiceSecondaryEndpoint;
    private URI tableServiceSecondaryEndpoint;
    private Integer blobHttpsPortOverride = null;
    private Integer fileHttpsPortOverride = null;
    private Integer queueHttpsPortOverride = null;
    private Integer tableHttpsPortOverride = null;

    public String getTenantName() {
        return this.tenantName;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getAccountKey() {
        return this.accountKey;
    }

    public URI getBlobServiceEndpoint() {
        return this.blobServiceEndpoint;
    }

    public URI getFileServiceEndpoint() {
        return this.fileServiceEndpoint;
    }

    public URI getQueueServiceEndpoint() {
        return this.queueServiceEndpoint;
    }

    public URI getTableServiceEndpoint() {
        return this.tableServiceEndpoint;
    }

    public URI getBlobServiceSecondaryEndpoint() {
        return this.blobServiceSecondaryEndpoint;
    }

    public URI getFileServiceSecondaryEndpoint() {
        return this.fileServiceSecondaryEndpoint;
    }

    public URI getQueueServiceSecondaryEndpoint() {
        return this.queueServiceSecondaryEndpoint;
    }

    public URI getTableServiceSecondaryEndpoint() {
        return this.tableServiceSecondaryEndpoint;
    }

    public Integer getBlobHttpsPortOverride() {
        return this.blobHttpsPortOverride;
    }

    public Integer getFileHttpsPortOverride() {
        return this.fileHttpsPortOverride;
    }

    public Integer getQueueHttpsPortOverride() {
        return this.queueHttpsPortOverride;
    }

    public Integer getTableHttpsPortOverride() {
        return this.tableHttpsPortOverride;
    }
    
    void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    void setBlobServiceEndpoint(URI blobServiceEndpoint) {
        this.blobServiceEndpoint = blobServiceEndpoint;
    }

    void setFileServiceEndpoint(URI fileServiceEndpoint) {
        this.fileServiceEndpoint = fileServiceEndpoint;
    }

    void setQueueServiceEndpoint(URI queueServiceEndpoint) {
        this.queueServiceEndpoint = queueServiceEndpoint;
    }

    void setTableServiceEndpoint(URI tableServiceEndpoint) {
        this.tableServiceEndpoint = tableServiceEndpoint;
    }

    void setBlobServiceSecondaryEndpoint(URI blobServiceSecondaryEndpoint) {
        this.blobServiceSecondaryEndpoint = blobServiceSecondaryEndpoint;
    }

    void setFileServiceSecondaryEndpoint(URI fileServiceSecondaryEndpoint) {
        this.fileServiceSecondaryEndpoint = fileServiceSecondaryEndpoint;
    }

    void setQueueServiceSecondaryEndpoint(URI queueServiceSecondaryEndpoint) {
        this.queueServiceSecondaryEndpoint = queueServiceSecondaryEndpoint;
    }

    void setTableServiceSecondaryEndpoint(URI tableServiceSecondaryEndpoint) {
        this.tableServiceSecondaryEndpoint = tableServiceSecondaryEndpoint;
    }

    void setBlobHttpsPortOverride(Integer blobHttpsPortOverride) {
        this.blobHttpsPortOverride = blobHttpsPortOverride;
    }

    void setFileHttpsPortOverride(Integer fileHttpsPortOverride) {
        this.fileHttpsPortOverride = fileHttpsPortOverride;
    }

    void setQueueHttpsPortOverride(Integer queueHttpsPortOverride) {
        this.queueHttpsPortOverride = queueHttpsPortOverride;
    }

    void setTableHttpsPortOverride(Integer tableHttpsPortOverride) {
        this.tableHttpsPortOverride = tableHttpsPortOverride;
    }

    public void assertSecondaryEndpoint() {
        if ((this.getBlobServiceSecondaryEndpoint() == null) || (this.getQueueServiceSecondaryEndpoint() == null)
                || (this.getTableServiceSecondaryEndpoint() == null)) {
            throw new IllegalStateException("Secondary endpoints are not defined for target tenant");
        }
    }
}
