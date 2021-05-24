// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.credential.TokenCredential
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.resourcemanager.storage.generated.fluent.models.BlobContainerInner
import com.azure.resourcemanager.storage.generated.implementation.StorageManagementClientBuilder
import com.azure.resourcemanager.storage.generated.implementation.StorageManagementClientImpl
import com.azure.resourcemanager.storage.generated.models.PublicAccess

class ImmutableStorageWithVersioningTest extends APISpec {

    BlobContainerClient containerClient;


    def setup() {

        TokenCredential credential = new EnvironmentCredentialBuilder().build()
//        StorageManager manager = StorageManager.authenticate(credential, new AzureProfile(AzureEnvironment.AZURE))

        StorageManagementClientImpl client = new StorageManagementClientBuilder()
            .buildClient()

        def resourceGroupName = "XClient"
        def inner = new BlobContainerInner().withPublicAccess(PublicAccess.CONTAINER)
//        client.getBlobContainers().createWithResponse(resourceGroupName, env.primaryAccount.name, generateContainerName())

    }

}
