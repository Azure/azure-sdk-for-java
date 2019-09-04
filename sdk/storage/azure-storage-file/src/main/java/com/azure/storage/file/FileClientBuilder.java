// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpPipeline;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the
 * {@link FileClient FileClients}, {@link FileAsyncClient FileAsyncClients}, {@link DirectoryClient DirectoryClients},
 * and {@link DirectoryAsyncClient DirectoryAsyncClients}. Calling {@link FileClientBuilder#buildFileClient() buildFileClient},
 * {@link FileClientBuilder#buildFileAsyncClient() buildFileAsyncClient},
 * {@link FileClientBuilder#buildDirectoryClient() buildDirectoryClient}, or
 * {@link FileClientBuilder#buildDirectoryAsyncClient() buildDirectoryAsyncClient} constructs an instance of {@link FileClient},
 * {@link FileAsyncClient}, {@link DirectoryClient}, or {@link DirectoryAsyncClient} respectively.
 *
 * <p>The client needs the endpoint of the Azure Storage File service, name of the share, and authorization credential.
 * {@link FileClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * {@link FileClientBuilder#shareName(String)}, {@link FileClientBuilder#resourcePath(String)} and a {@link SASTokenCredential} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous File Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.fileClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous File Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.directoryClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a {@code SASTokenCredential} they may
 * be set using {@link FileClientBuilder#credential(SASTokenCredential) credential}.</p>
 *
 * {@codesnippet com.azure.storage.file.fileClient.instantiation.credential}
 *
 * {@codesnippet com.azure.storage.file.fileAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage File service must be used. Set the SharedKeyCredential with
 * {@link FileClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous File Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.directoryClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous File Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.directoryAsyncClient.instantiation.connectionstring}
 *
 * @see FileClient
 * @see FileAsyncClient
 * @see SASTokenCredential
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {FileClient.class, FileAsyncClient.class, DirectoryClient.class,
    DirectoryAsyncClient.class})
public class FileClientBuilder extends BaseFileClientBuilder<FileClientBuilder> {

    private final ClientLogger logger = new ClientLogger(FileClientBuilder.class);

    private String shareName;
    private String shareSnapshot;
    private String resourcePath;

    /**
     * Creates a builder instance that is able to configure and construct {@link FileClient FileClients}
     * and {@link FileAsyncClient FileAsyncClients}.
     */
    public FileClientBuilder() { }

    private AzureFileStorageImpl constructImpl() {
        Objects.requireNonNull(shareName);
        Objects.requireNonNull(resourcePath);

        if (!super.hasCredential()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Credentials are required for authorization"));
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
     * Creates a {@link DirectoryAsyncClient} based on options set in the builder. Every time {@code buildFileAsyncClient()} is
     * called a new instance of {@link DirectoryAsyncClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link FileClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link DirectoryAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ShareAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code shareName} is {@code null} or {@code shareName} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public DirectoryAsyncClient buildDirectoryAsyncClient() {
        return new DirectoryAsyncClient(constructImpl(), shareName, resourcePath, shareSnapshot);
    }

    /**
     * Creates a {@link DirectoryClient} based on options set in the builder. Every time {@code buildDirectoryClient()} is
     * called a new instance of {@link DirectoryClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link FileClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link DirectoryClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A DirectoryClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint}, {@code shareName} or {@code directoryPath} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public DirectoryClient buildDirectoryClient() {
        return new DirectoryClient(this.buildDirectoryAsyncClient());
    }

    /**
     * Creates a {@link FileAsyncClient} based on options set in the builder. Every time {@code buildFileAsyncClient()} is
     * called a new instance of {@link FileAsyncClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link FileClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link FileAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ShareAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code shareName} is {@code null} or the (@code resourcePath) is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public FileAsyncClient buildFileAsyncClient() {

        return new FileAsyncClient(constructImpl(), shareName, resourcePath, shareSnapshot);
    }

    /**
     * Creates a {@link FileClient} based on options set in the builder. Every time {@code buildFileClient()} is
     * called a new instance of {@link FileClient} is created.
     *
     * <p>
     * If {@link FileClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link FileClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link FileClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A FileClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint}, {@code shareName} or {@code resourcePath} is {@code null}.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public FileClient buildFileClient() {
        return new FileClient(this.buildFileAsyncClient());
    }

    /**
     * Sets the endpoint for the Azure Storage File instance that the client will interact with.
     *
     * <p>The first path segment, if the endpoint contains path segments, will be assumed to be the name of the share
     * that the client will interact with. Rest of the path segments should be the path of the file.
     * It mush end up with the file name if more segments exist.</p>
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQueryParameters(Map)} in an
     * attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage File instance to send service requests to and receive responses from.
     * @return the updated FileClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is an invalid URL
     */
    @Override
    public FileClientBuilder endpoint(String endpoint) {
        try {
            URL fullURL = new URL(endpoint);
            super.endpoint = fullURL.getProtocol() + "://" + fullURL.getHost();

            // Attempt to get the share name and file path from the URL passed
            String[] pathSegments = fullURL.getPath().split("/");
            int length = pathSegments.length;
            this.shareName = length >= 2 ? pathSegments[1] : this.shareName;
            String[] filePathParams = length >= 3 ? Arrays.copyOfRange(pathSegments, 2, length) : null;
            this.resourcePath = filePathParams != null ? String.join("/", filePathParams) : this.resourcePath;

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential sasTokenCredential = SASTokenCredential.fromQueryParameters(Utility.parseQueryString(fullURL.getQuery()));
            if (sasTokenCredential != null) {
                super.credential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage File endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the share that the constructed clients will interact with
     *
     * @param shareName Name of the share
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code shareName} is {@code null}.
     */
    public FileClientBuilder shareName(String shareName) {
        this.shareName = shareName;
        return this;
    }

    /**
     * Sets the shareSnapshot that the constructed clients will interact with. This shareSnapshot must be linked to the share
     * that has been specified in the builder.
     *
     * @param snapshot Identifier of the shareSnapshot
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code shareSnapshot} is {@code null}.
     */
    public FileClientBuilder snapshot(String snapshot) {
        this.shareSnapshot = snapshot;
        return this;
    }

    /**
     * Sets the file that the constructed clients will interact with
     *
     * @param resourcePath Path of the file (or directory).
     * @return the updated FileClientBuilder object
     * @throws NullPointerException If {@code resourcePath} is {@code null}.
     */
    public FileClientBuilder resourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }
}
