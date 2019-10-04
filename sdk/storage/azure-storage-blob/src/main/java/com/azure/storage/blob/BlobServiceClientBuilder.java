// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.common.credentials.SASTokenCredential;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Fluent BlobServiceClientBuilder for instantiating a {@link BlobServiceClient} or {@link BlobServiceAsyncClient} using
 * {@link BlobServiceClientBuilder#buildClient()} or {@link BlobServiceClientBuilder#buildAsyncClient()} respectively.
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the endpoint through {@code .endpoint()}, in the format of {@code https://{accountName}.blob.core.windows.net}.
 * <li>the credential through {@code .credential()} or {@code .connectionString()} if the container is not publicly
 * accessible.
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a {@link
 * BlobServiceClient} or {@code .buildAsyncClient()} to create a {@link BlobServiceAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {BlobServiceClient.class, BlobServiceAsyncClient.class})
public final class BlobServiceClientBuilder extends BaseBlobClientBuilder<BlobServiceClientBuilder> {

    private final ClientLogger logger = new ClientLogger(BlobServiceClientBuilder.class);


    /**
     * Creates a builder instance that is able to configure and construct {@link BlobServiceClient BlobServiceClients}
     * and {@link BlobServiceAsyncClient BlobServiceAsyncClients}.
     */
    public BlobServiceClientBuilder() {
    }

    /**
     * @return a {@link BlobServiceClient} created from the configurations in this builder.
     */
    public BlobServiceClient buildClient() {
        return new BlobServiceClient(buildAsyncClient());
    }

    /**
     * @return a {@link BlobServiceAsyncClient} created from the configurations in this builder.
     */
    public BlobServiceAsyncClient buildAsyncClient() {
        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new BlobServiceAsyncClient(new AzureBlobStorageBuilder()
            .url(super.endpoint)
            .pipeline(pipeline)
            .build(), customerProvidedKey, accountName);
    }

    /**
     * Sets the blob service endpoint, additionally parses it for information (SAS token)
     *
     * @param endpoint URL of the service
     * @return the updated BlobServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is a malformed URL.
     */
    public BlobServiceClientBuilder endpoint(String endpoint) {
        try {
            URL url = new URL(endpoint);
            super.endpoint = url.getProtocol() + "://" + url.getAuthority();

            SASTokenCredential sasTokenCredential = SASTokenCredential
                .fromSASTokenString(BlobURLParts.parse(url).getSasQueryParameters().encode());
            if (sasTokenCredential != null) {
                super.credential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage endpoint url is malformed."));
        }

        return this;
    }

    @Override
    protected Class<BlobServiceClientBuilder> getClazz() {
        return BlobServiceClientBuilder.class;
    }

    String endpoint() {
        return super.endpoint;
    }
}
