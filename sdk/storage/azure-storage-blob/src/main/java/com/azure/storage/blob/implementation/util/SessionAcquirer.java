// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.AuthenticationType;
import com.azure.storage.blob.implementation.models.CreateSessionConfiguration;
import com.azure.storage.blob.implementation.models.CreateSessionResponse;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Issues CreateSession requests on behalf of a single client, through that client's own {@link HttpPipeline}.
 * <p>
 * The session cache is shared by every client using the same {@code SessionContext}, but session acquisition is
 * not: each client's {@link SessionAuthenticationPolicy} owns an acquirer bound to the pipeline that contains it, so
 * CreateSession runs through the customer's configured policies, retry, tracing, logging, and transport.
 * <p>
 * CreateSession requests are tagged with {@link #SESSION_ACQUISITION_CONTEXT_KEY} so that pipeline policies can
 * recognize them, for example to keep the session key out of HTTP logs or to authenticate with an acquisition
 * credential override.
 */
final class SessionAcquirer {
    /**
     * {@link Context} key set on CreateSession requests issued by an acquirer.
     */
    static final String SESSION_ACQUISITION_CONTEXT_KEY = "azure-storage-blob-session-acquisition";

    private static final ClientLogger LOGGER = new ClientLogger(SessionAcquirer.class);

    private final String endpoint;
    private final BlobServiceVersion serviceVersion;
    private final String accountName;
    private final StorageBearerTokenChallengeAuthorizationPolicy acquisitionBearerPolicy;
    private volatile AzureBlobStorageImpl azureBlobStorage;

    /**
     * Creates an acquirer that is not yet bound to a pipeline.
     *
     * @param endpoint The account endpoint used for CreateSession and to decide which requests may use sessions.
     * @param serviceVersion The service version used for CreateSession.
     * @param accountName The account name resolved at client construction, used for signing and cache identity.
     * @param acquisitionBearerPolicy Optional bearer policy that overrides the client's credential for CreateSession.
     */
    SessionAcquirer(String endpoint, BlobServiceVersion serviceVersion, String accountName,
        StorageBearerTokenChallengeAuthorizationPolicy acquisitionBearerPolicy) {
        if (CoreUtils.isNullOrEmpty(accountName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'accountName' cannot be null or empty."));
        }
        this.endpoint
            = BuilderHelper.getSessionEndpoint(Objects.requireNonNull(endpoint, "'endpoint' cannot be null."));
        this.serviceVersion = serviceVersion != null ? serviceVersion : BlobServiceVersion.getLatest();
        this.accountName = accountName;
        this.acquisitionBearerPolicy = acquisitionBearerPolicy;
    }

    /**
     * Binds the acquirer to the pipeline that contains its {@link SessionAuthenticationPolicy}. Must be called before
     * the pipeline is used.
     *
     * @param pipeline The client's pipeline.
     * @return This acquirer.
     */
    SessionAcquirer bind(HttpPipeline pipeline) {
        this.azureBlobStorage
            = new AzureBlobStorageImplBuilder().pipeline(Objects.requireNonNull(pipeline, "'pipeline' cannot be null."))
                .url(endpoint)
                .version(serviceVersion.getVersion())
                .buildClient();
        return this;
    }

    /**
     * Determines whether a request targets this acquirer's account endpoint.
     *
     * @param request The request.
     * @return Whether the request targets the acquirer's endpoint.
     */
    boolean isRequestForSessionEndpoint(HttpRequest request) {
        try {
            return endpoint.equalsIgnoreCase(BuilderHelper.getSessionEndpoint(request.getUrl().toString()));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    String getAccountName() {
        return accountName;
    }

    StorageBearerTokenChallengeAuthorizationPolicy getAcquisitionBearerPolicy() {
        return acquisitionBearerPolicy;
    }

    /**
     * Sends a CreateSession request for the container through the bound pipeline.
     *
     * @param containerName The container to create a session for.
     * @param context The context of the request that triggered session acquisition.
     * @return The CreateSession response.
     */
    Mono<Response<CreateSessionResponse>> createSessionAsync(String containerName, Context context) {
        AzureBlobStorageImpl client = azureBlobStorage;
        if (client == null) {
            return Mono.error(LOGGER.logExceptionAsError(
                new IllegalStateException("The session acquirer has not been bound to a pipeline.")));
        }

        CreateSessionConfiguration config
            = new CreateSessionConfiguration().setAuthenticationType(AuthenticationType.HMAC);
        Context acquisitionContext = context.addData(SESSION_ACQUISITION_CONTEXT_KEY, true);
        return client.getContainers()
            .createSessionWithResponseAsync(containerName, config, null, null, acquisitionContext);
    }

    /**
     * Determines whether a pipeline call is a CreateSession request issued by an acquirer.
     *
     * @param context The pipeline call context.
     * @return Whether the call is a session acquisition request.
     */
    static boolean isAcquisitionRequest(HttpPipelineCallContext context) {
        return context.getData(SESSION_ACQUISITION_CONTEXT_KEY).isPresent();
    }
}
