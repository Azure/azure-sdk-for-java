// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class NationalCloudSample {

    public static void main(String[] args) {
        final String endpoint = "endpoint";
        final String authenticationScope = "scope";
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE_US_GOVERNMENT);
        TokenCredential credentials = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();

        ContainerRegistryAsyncClient asyncClient = new ContainerRegistryClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .authenticationScope(authenticationScope)
            .buildAsyncClient();

        asyncClient.listRepositoryNames().subscribe(name -> System.out.println(name));
    }
}
