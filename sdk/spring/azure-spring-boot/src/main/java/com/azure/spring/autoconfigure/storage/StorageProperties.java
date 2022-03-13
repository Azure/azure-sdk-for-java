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

    /**
     * Gets the account name.
     *
     * @return the account name
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the account name.
     *
     * @param accountName the account name
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * Gets the Blob endpoint.
     *
     * @return the Blob endpoint
     */
    public String getBlobEndpoint() {
        return blobEndpoint;
    }

    /**
     * Sets the Blob endpoint.
     *
     * @param blobEndpoint the Blob endpoint
     */
    public void setBlobEndpoint(String blobEndpoint) {
        this.blobEndpoint = blobEndpoint;
    }

    /**
     * Gets the File endpoint.
     *
     * @return the File endpoint
     */
    public String getFileEndpoint() {
        return fileEndpoint;
    }

    /**
     * Sets the File endpoint.
     *
     * @param fileEndpoint the File endpoint
     */
    public void setFileEndpoint(String fileEndpoint) {
        this.fileEndpoint = fileEndpoint;
    }

    /**
     * Gets the account key.
     *
     * @return the account key
     */
    public String getAccountKey() {
        return accountKey;
    }

    /**
     * Sets the account key.
     *
     * @param accountKey the account key
     */
    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

}
