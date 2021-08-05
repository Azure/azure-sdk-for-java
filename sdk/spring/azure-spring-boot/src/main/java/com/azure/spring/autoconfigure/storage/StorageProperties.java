// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Storage properties.
 */
@Validated
@ConfigurationProperties("azure.storage")
public class StorageProperties {

    @NotEmpty
    @Pattern(regexp = "^[a-z0-9]{3,24}$",
        message = "must be between 3 and 24 characters in length and use numbers and lower-case letters only")
    private String accountName;

    private String blobEndpoint;

    private String fileEndpoint;

    private String accountKey;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBlobEndpoint() {
        return blobEndpoint;
    }

    public void setBlobEndpoint(String blobEndpoint) {
        this.blobEndpoint = blobEndpoint;
    }

    public String getFileEndpoint() {
        return fileEndpoint;
    }

    public void setFileEndpoint(String fileEndpoint) {
        this.fileEndpoint = fileEndpoint;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

}
