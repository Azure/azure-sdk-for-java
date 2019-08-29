// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.file.implementation.AzureFileStorageBuilder;
import com.azure.storage.file.implementation.AzureFileStorageImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link ShareClient ShareClients}
 * and {@link ShareAsyncClient SahreAsyncClients}, calling {@link ShareClientBuilder#buildClient() buildFileClient}
 * constructs an instance of ShareClient and calling {@link ShareClientBuilder#buildAsyncClient() buildFileAsyncClient}
 * constructs an instance of SahreAsyncClient.
 *
 * <p>The client needs the endpoint of the Azure Storage File service, name of the share, and authorization credential.
 * {@link ShareClientBuilder#endpoint(String) endpoint} gives the builder the endpoint and may give the builder the
 * {@link ShareClientBuilder#shareName(String) shareName} and a {@link SASTokenCredential} that authorizes the client.</p>
 *
 * <p><strong>Instantiating a synchronous Share Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.shareClient.instantiation.sastoken}
 *
 * <p><strong>Instantiating an Asynchronous Share Client with SAS token</strong></p>
 * {@codesnippet com.azure.storage.file.shareAsyncClient.instantiation.sastoken}
 *
 * <p>If the {@code endpoint} doesn't contain the query parameters to construct a {@code SASTokenCredential} they may
 * be set using {@link ShareClientBuilder#credential(SASTokenCredential) credential}.</p>
 *
 * {@codesnippet com.azure.storage.file.shareClient.instantiation.credential}
 *
 * {@codesnippet com.azure.storage.file.shareAsyncClient.instantiation.credential}
 *
 * <p>Another way to authenticate the client is using a {@link SharedKeyCredential}. To create a SharedKeyCredential
 * a connection string from the Storage File service must be used. Set the SharedKeyCredential with
 * {@link ShareClientBuilder#connectionString(String) connectionString}. If the builder has both a SASTokenCredential and
 * SharedKeyCredential the SharedKeyCredential will be preferred when authorizing requests sent to the service.</p>
 *
 * <p><strong>Instantiating a synchronous Share Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.shareClient.instantiation.connectionstring}
 *
 * <p><strong>Instantiating an Asynchronous Share Client with connection string.</strong></p>
 * {@codesnippet com.azure.storage.file.shareAsyncClient.instantiation.connectionstring}
 *
 * @see ShareClient
 * @see ShareAsyncClient
 * @see SASTokenCredential
 * @see SharedKeyCredential
 */
@ServiceClientBuilder(serviceClients = {ShareClient.class, ShareAsyncClient.class})
public class ShareClientBuilder extends BaseFileClientBuilder<ShareClientBuilder> {

    private final ClientLogger logger = new ClientLogger(ShareClientBuilder.class);
    private String shareName;
    private String snapshot;

    /**
     * Creates a builder instance that is able to configure and construct {@link ShareClient ShareClients}
     * and {@link ShareAsyncClient ShareAsyncClients}.
     */
    public ShareClientBuilder() { }

    private AzureFileStorageImpl constructImpl() {
        Objects.requireNonNull(shareName);

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
     * Creates a {@link ShareAsyncClient} based on options set in the builder. Every time {@code buildFileAsyncClient()} is
     * called a new instance of {@link ShareAsyncClient} is created.
     *
     * <p>
     * If {@link ShareClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link ShareClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link ShareAsyncClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ShareAsyncClient with the options set from the builder.
     * @throws NullPointerException If {@code shareName} is {@code null}.
     * @throws IllegalArgumentException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public ShareAsyncClient buildAsyncClient() {
        return new ShareAsyncClient(constructImpl(), shareName, snapshot);
    }

    /**
     * Creates a {@link ShareClient} based on options set in the builder. Every time {@code buildFileClient()} is
     * called a new instance of {@link ShareClient} is created.
     *
     * <p>
     * If {@link ShareClientBuilder#pipeline(HttpPipeline) pipeline} is set, then the {@code pipeline} and
     * {@link ShareClientBuilder#endpoint(String) endpoint} are used to create the
     * {@link ShareClient client}. All other builder settings are ignored.
     * </p>
     *
     * @return A ShareClient with the options set from the builder.
     * @throws NullPointerException If {@code endpoint} or {@code shareName} is {@code null}.
     * @throws IllegalStateException If neither a {@link SharedKeyCredential} or {@link SASTokenCredential} has been set.
     */
    public ShareClient buildClient() {
        return new ShareClient(buildAsyncClient());
    }

    /**
     * Sets the endpoint for the Azure Storage File instance that the client will interact with.
     *
     * <p>The first path segment, if the endpoint contains path segments, will be assumed to be the name of the share
     * that the client will interact with.</p>
     *
     * <p>Query parameters of the endpoint will be parsed using {@link SASTokenCredential#fromQueryParameters(Map)} in an
     * attempt to generate a {@link SASTokenCredential} to authenticate requests sent to the service.</p>
     *
     * @param endpoint The URL of the Azure Storage File instance to send service requests to and receive responses from.
     * @return the updated ShareClientBuilder object
     * @throws IllegalArgumentException If {@code endpoint} is {@code null} or is an invalid URL
     */
    @Override
    public ShareClientBuilder endpoint(String endpoint) {
        try {
            URL fullURL = new URL(endpoint);
            super.endpoint = fullURL.getProtocol() + "://" + fullURL.getHost();

            // Attempt to get the share name from the URL passed
            String[] pathSegments = fullURL.getPath().split("/");
            int length = pathSegments.length;
            if (length >= 3) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Cannot accept a URL to a file or directory to construct a file share client"));
            }
            this.shareName = length >= 2 ? pathSegments[1] : this.shareName;

            // Attempt to get the SAS token from the URL passed
            SASTokenCredential sasTokenCredential = SASTokenCredential.fromQueryParameters(Utility.parseQueryString(fullURL.getQuery()));
            if (sasTokenCredential != null) {
                super.credential(sasTokenCredential);
            }
        } catch (MalformedURLException ex) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The Azure Storage File Service endpoint url is malformed."));
        }

        return this;
    }

    /**
     * Sets the share that the constructed clients will interact with
     *
     * @param shareName Name of the share
     * @return the updated ShareClientBuilder object
     * @throws NullPointerException If {@code shareName} is {@code null}.
     */
    public ShareClientBuilder shareName(String shareName) {
        this.shareName = Objects.requireNonNull(shareName);
        return this;
    }

    /**
     * Sets the snapshot that the constructed clients will interact with. This snapshot must be linked to the share
     * that has been specified in the builder.
     *
     * @param snapshot Identifier of the snapshot
     * @return the updated ShareClientBuilder object
     */
    public ShareClientBuilder snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }
}
