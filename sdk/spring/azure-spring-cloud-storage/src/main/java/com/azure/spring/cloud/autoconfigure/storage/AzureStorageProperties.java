// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author Warren Zhu
 */
@Validated
@ConfigurationProperties("spring.cloud.azure.storage")
public class AzureStorageProperties {

    @NotEmpty
    @Pattern(regexp = "^[a-z0-9]{3,24}$",
        message = "must be between 3 and 24 characters in length and use numbers and lower-case letters only")
    private String account;

    private String accessKey;
    
    private String resourceGroup;

    /**
     *
     * @return The account.
     */
    public String getAccount() {
        return account;
    }

    /**
     *
     * @param account The account.
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     *
     * @return The access key.
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     *
     * @param accessKey The access key.
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     *
     * @return The resource group.
     */
    public String getResourceGroup() {
        return resourceGroup;
    }

    /**
     *
     * @param resourceGroup The resource group
     */
    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }
}
