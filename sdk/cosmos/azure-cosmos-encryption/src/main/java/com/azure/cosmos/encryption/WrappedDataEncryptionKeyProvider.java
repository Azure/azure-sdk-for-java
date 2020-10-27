// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.encryption.DataEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;

public class WrappedDataEncryptionKeyProvider {

    private final static String ContainerPartitionKeyPath = "/id";

    private CosmosAsyncContainer keyContainer;

    public WrappedDataEncryptionKeyProvider(CosmosAsyncContainer keyContainer) {
        this.keyContainer = keyContainer;
    }

    public void InitializeAsync() {
        CosmosContainerResponse containerResponse = this.keyContainer.getDatabase().createContainerIfNotExists(
            this.keyContainer.getId(),
            WrappedDataEncryptionKeyProvider.ContainerPartitionKeyPath).block();
        // TODO non blocking

        if (!StringUtils.equals(containerResponse.getProperties().getPartitionKeyDefinition().getPaths().get(0),
            WrappedDataEncryptionKeyProvider.ContainerPartitionKeyPath)) {

            //TODO: validate partition key path
            //            throw new ArgumentException(
            //                $"Provided container {this.keyContainer.Id} did not have the appropriate partition key
            //                definition. " +
            //                $"The container needs to be created with PartitionKeyPath set to
            //                {WrappedDataEncryptionKeyProvider.ContainerPartitionKeyPath}.");
        }
    }

    public DataEncryptionKeyProperties GetDataEncryptionKeyPropertiesAsync(String id) {
        try {
            CosmosItemResponse<DataEncryptionKeyProperties> response = this.keyContainer.readItem(
                id,
                new PartitionKey(id), DataEncryptionKeyProperties.class).block();

            return response.getItem();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
            // TODO: fixme
            //            throw EncryptionExceptionFactory.EncryptionKeyNotFoundException(
            //                $"Failed to retrieve Data Encryption Key with id: '{id}'.",
            //                exception);
        }
    }
}
