// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.eventhub;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getCheckpointStorageAccount() {
        return checkpointStorageAccount;
    }

    public void setCheckpointStorageAccount(String checkpointStorageAccount) {
        this.checkpointStorageAccount = checkpointStorageAccount;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getCheckpointAccessKey() {
        return checkpointAccessKey;
    }

    public void setCheckpointAccessKey(String checkpointAccessKey) {
        this.checkpointAccessKey = checkpointAccessKey;
    }

    public String getCheckpointContainer() {
        return checkpointContainer;
    }

    public void setCheckpointContainer(String checkpointContainer) {
        this.checkpointContainer = checkpointContainer;
    }

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(namespace) && !StringUtils.hasText(connectionString)) {
            throw new IllegalArgumentException("Either 'spring.cloud.azure.eventhub.namespace' or "
                + "'spring.cloud.azure.eventhub.connection-string' should be provided");
        }
    }
}
