// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.AuthenticationType;
import com.azure.storage.blob.implementation.models.CreateSessionConfiguration;
import com.azure.storage.blob.implementation.models.CreateSessionResponse;
import com.azure.storage.blob.implementation.models.SessionCredentials;
import reactor.core.publisher.Mono;

/**
 * Package-private client for creating sessions via the CreateSession REST API.
 * Follows the same constructor pattern as {@link com.azure.storage.blob.BlobContainerClient}:
 * takes an {@link HttpPipeline} (bearer-only, no SessionPolicy) and builds an
 * {@link AzureBlobStorageImpl} internally.
 */
final class BlobSessionClient {

    private final AzureBlobStorageImpl azureBlobStorage;
    private final String containerName;

    BlobSessionClient(HttpPipeline bearerPipeline, String url, BlobServiceVersion serviceVersion,
        String containerName) {
        this.azureBlobStorage = new AzureBlobStorageImplBuilder().pipeline(bearerPipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.containerName = containerName;
    }

    Mono<StorageSessionCredential> createSessionAsync() {
        CreateSessionConfiguration config
            = new CreateSessionConfiguration().setAuthenticationType(AuthenticationType.HMAC);

        return azureBlobStorage.getContainers()
            .createSessionWithResponseAsync(containerName, config, null, null)
            .map(this::toCredential);
    }

    StorageSessionCredential createSessionSync() {
        CreateSessionConfiguration config
            = new CreateSessionConfiguration().setAuthenticationType(AuthenticationType.HMAC);

        Response<CreateSessionResponse> response = azureBlobStorage.getContainers()
            .createSessionWithResponse(containerName, config, null, null, Context.NONE);
        return toCredential(response);
    }

    private StorageSessionCredential toCredential(Response<CreateSessionResponse> response) {
        CreateSessionResponse session = response.getValue();
        SessionCredentials creds = session.getCredentials();
        return new StorageSessionCredential(creds.getSessionToken(), creds.getSessionKey(), session.getExpiration());
    }
}
