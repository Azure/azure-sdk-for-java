// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;

/**
 * @author Warren Zhu
 */
@Validated
@ConfigurationProperties("spring.cloud.azure.eventhub")
public class AzureEventHubProperties {

    private String namespace;

    private String connectionString;

    @Pattern(regexp = "^[a-z0-9]{3,24}$",
        message = "must be between 3 and 24 characters in length and use numbers and lower-case letters only")
    private String checkpointStorageAccount;

    private String checkpointAccessKey;

    private String checkpointContainer;

    /**
     *
     * @return The namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     *
     * @param namespace The namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     *
     * @return The checkpointStorageAccount.
     */
    public String getCheckpointStorageAccount() {
        return checkpointStorageAccount;
    }

    /**
     *
     * @param checkpointStorageAccount The checkpointStorageAccount.
     */
    public void setCheckpointStorageAccount(String checkpointStorageAccount) {
        this.checkpointStorageAccount = checkpointStorageAccount;
    }

    /**
     *
     * @return The connectionString.
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     *
     * @param connectionString The connectionString.
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     *
     * @return The checkpointAccessKey.
     */
    public String getCheckpointAccessKey() {
        return checkpointAccessKey;
    }

    /**
     *
     * @param checkpointAccessKey The checkpointAccessKey.
     */
    public void setCheckpointAccessKey(String checkpointAccessKey) {
        this.checkpointAccessKey = checkpointAccessKey;
    }

    /**
     *
     * @return The checkpointContainer.
     */
    public String getCheckpointContainer() {
        return checkpointContainer;
    }

    /**
     *
     * @param checkpointContainer The checkpointContainer.
     */
    public void setCheckpointContainer(String checkpointContainer) {
        this.checkpointContainer = checkpointContainer;
    }

}
