// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 * Metadata about storage of Data Encryption Keys.
 */
public class CosmosDataEncryptionKeyProviderMetadata {

    /**
     * Initializes a new instance of {@link CosmosDataEncryptionKeyProviderMetadata}
     */
    public CosmosDataEncryptionKeyProviderMetadata() {
        this.type = "CosmosContainer";
    }

    /// <summary>
    /// Name of the database that has the key container.
    /// </summary>
    @JsonProperty("databaseName")
    public String databaseName;

    /// <summary>
    /// Name of the key container.
    /// </summary>
    @JsonProperty("containerName")
    public String ContainerName;

    @JsonProperty("type")
    String type;

    @JsonProperty("accountEndpoint")
    URI accountEndpoint; // todo: check Uri
}
