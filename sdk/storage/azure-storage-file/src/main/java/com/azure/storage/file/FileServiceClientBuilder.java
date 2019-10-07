// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * FileServiceClient FileServiceClients} and {@link FileServiceAsyncClient FileServiceAsyncClients}, calling {@link
 * FileServiceClientBuilder#buildClient() buildClient} constructs an instance of FileServiceClient and calling {@link
 * FileServiceClientBuilder#buildAsyncClient() buildFileAsyncClient} constructs an instance of FileServiceAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage File service and authorization credential.
 * {@link FileServiceClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder a
 * SAS token that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous FileService Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous FileService Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a SAS token they may be set using
 * {@link #sasToken(String) sasToken} .</p>
 *
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.credential}
 *
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage File service must be used. Set the SharedKeyCredential with {@link
 * FileServiceClientBuilder#connectionString(String) connectionString}. If the builder has both a SAS token and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous FileService Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous FileService Client with connection string. </strong></p>
 * {@codesnippet com.azure.storage.file.fileServiceAsyncClient.instantiation.connectionstring}
 *
 * @see FileServiceClient
 * @see FileServiceAsyncClient
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {FileServiceClient.class, FileServiceAsyncClient.class})
public final class FileServiceClientBuilder extends BaseFileClientBuilder<FileServiceClientBuilder> {

    private final ClientLogger logger = new ClientLogger(FileServiceClientBuilder.class);

    private String accountName;

    /**
     * Creates a builder instance that is able to configure and construct {@link FileServiceClient FileServiceClients}
     * and {@link FileServiceAsyncClient FileServiceAsyncClients}.
     */
    public FileServiceClientBuilder() {
    }

    private AzureFileStorageImpl constructImpl() {
        if (!super.hasCredential()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Credentials are required for authorization"));
        }

        HttpPipeline pipeline = super.getPipeline();
        if (pipeline == null) {
            pipeline = super.buildPipeline();
        }

        return new AzureFileStorageBuilder()
            .url(super.endpoint)
            .pipeline(pipeline)
            .build();
    }

    /**
     * Creates a {@link FileServiceAsyncClient} based on options set in the builder. Every time this method is called a
     * new instance of {@link FileServiceAsyncClient} is created.
     *
     * <p>
     * If {@link FileServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link FileServiceAsyncClient client}.
     * All other builder settings are ignored.
     * </p>
     *
     * @return A FileServiceAsyncClient with the options set from the builder.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public FileServiceAsyncClient buildAsyncClient() {
        return new FileServiceAsyncClient(constructImpl(), accountName);
    }

    /**
     * Creates a {@link FileServiceClient} based on options set in the builder. Every time {@code buildClient()} is
     * called a new instance of {@link FileServiceClient} is created.
     *
     * <p>
     * If {@link FileServiceClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and {@link
     * FileServiceClientBuilder#endpoint(String) endpoint} are used to create the {@link FileServiceClient client}. All
     * other builder settings are ignored.
     * </p>
     *
     * @return A FileServiceClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link #sasToken(String) SAS token}
     * has been set.
     */
    public FileServiceClient buildClient() {
        return new FileServiceClient(buildAsyncClient());
    }

    /**
     * Sets the endpoint for the Azure Storage File instance that the client will interact with.
     *
     * <p>Query parameters of the endpoint will be parsed in an attempt to generate a SAS token to authenticate
     * requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage File instance to send service requests to and receive responses
     * from.
     * @return the updated FileServiceClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} isn't a proper URL
     */
    @Override
    public FileServiceClientBuilder endpoint(String endpoint) {
        try {
            URL fullUrl = new URL(endpoint);
            super.endpoint = fullUrl.getProtocol() + "://" + fullUrl.getHost();

            this.accountName = Utility.getAccountName(fullUrl);

            // Attempt to get the SAS token from the URL passed
            String sasToken = new FileServiceSasQueryParameters(
                Utility.parseQueryStringSplitValues(fullUrl.getQuery()), false).encode();
            if (!ImplUtils.isNullOrEmpty(sasToken)) {
                super.sasToken(sasToken);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The Azure Storage File Service endpoint url is malformed."));
        }

        return this;
    }

    @Override
    protected Class<FileServiceClientBuilder> getClazz() {
        return FileServiceClientBuilder.class;
    }
}
