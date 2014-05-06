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
    private URI queueServiceEndpoint;
    private URI tableServiceEndpoint;
    private URI blobServiceSecondaryEndpoint;
    private URI queueServiceSecondaryEndpoint;
    private URI tableServiceSecondaryEndpoint;

    public String getTenantName() {
        return tenantName;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public URI getBlobServiceEndpoint() {
        return blobServiceEndpoint;
    }

    public URI getQueueServiceEndpoint() {
        return queueServiceEndpoint;
    }

    public URI getTableServiceEndpoint() {
        return tableServiceEndpoint;
    }

    public URI getBlobServiceSecondaryEndpoint() {
        return blobServiceSecondaryEndpoint;
    }

    public URI getQueueServiceSecondaryEndpoint() {
        return queueServiceSecondaryEndpoint;
    }

    public URI getTableServiceSecondaryEndpoint() {
        return tableServiceSecondaryEndpoint;
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

    void setQueueServiceEndpoint(URI queueServiceEndpoint) {
        this.queueServiceEndpoint = queueServiceEndpoint;
    }

    void setTableServiceEndpoint(URI tableServiceEndpoint) {
        this.tableServiceEndpoint = tableServiceEndpoint;
    }

    void setBlobServiceSecondaryEndpoint(URI blobServiceSecondaryEndpoint) {
        this.blobServiceSecondaryEndpoint = blobServiceSecondaryEndpoint;
    }

    void setQueueServiceSecondaryEndpoint(URI queueServiceSecondaryEndpoint) {
        this.queueServiceSecondaryEndpoint = queueServiceSecondaryEndpoint;
    }

    void setTableServiceSecondaryEndpoint(URI tableServiceSecondaryEndpoint) {
        this.tableServiceSecondaryEndpoint = tableServiceSecondaryEndpoint;
    }

    public void assertSecondaryEndpoint() {
        if ((this.getBlobServiceSecondaryEndpoint() == null) || (this.getQueueServiceSecondaryEndpoint() == null)
                || (this.getTableServiceSecondaryEndpoint() == null)) {
            throw new IllegalStateException("Secondary endpoints are not defined for target tenant");
        }
    }
}
