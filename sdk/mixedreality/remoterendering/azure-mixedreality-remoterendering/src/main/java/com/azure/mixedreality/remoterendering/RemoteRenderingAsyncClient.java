// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImpl;
import com.azure.mixedreality.remoterendering.implementation.models.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@ServiceClient(builder = RemoteRenderingClientBuilder.class, isAsync = true)
public class RemoteRenderingAsyncClient {
    private final UUID accountId;
    private final MixedRealityRemoteRenderingImpl impl;

    RemoteRenderingAsyncClient(MixedRealityRemoteRenderingImpl impl, UUID accountId) {
        this.accountId = accountId;
        this.impl = impl;
    }

    /**
     * Creates a new rendering session.
     *
     * @param sessionId An ID uniquely identifying the rendering session for the given account. The ID is case
     *     sensitive, can contain any combination of alphanumeric characters including hyphens and underscores, and
     *     cannot contain more than 256 characters.
     * @param body Settings of the session to be created.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateSessionResponse> createSession(String sessionId, CreateSessionSettings body, Context context) {
        return impl.createSessionWithResponseAsync(accountId, sessionId, body, context);
    }

    /**
     * Gets properties of a particular rendering session.
     *
     * @param sessionId An ID uniquely identifying the rendering session for the given account. The ID is case
     *     sensitive, can contain any combination of alphanumeric characters including hyphens and underscores, and
     *     cannot contain more than 256 characters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return properties of a particular rendering session.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SessionProperties>> getSession(String sessionId, Context context) {
        return impl.getSessionWithResponseAsync(accountId, sessionId, context);
    }

    /**
     * Updates a particular rendering session.
     *
     * @param sessionId An ID uniquely identifying the rendering session for the given account. The ID is case
     *     sensitive, can contain any combination of alphanumeric characters including hyphens and underscores, and
     *     cannot contain more than 256 characters.
     * @param body Settings of the session to be updated.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SessionProperties>> updateSession(String sessionId, UpdateSessionSettings body, Context context) {
        return impl.updateSessionWithResponseAsync(accountId, sessionId, body, context);
    }

    /**
     * Stops a particular rendering session.
     *
     * @param sessionId An ID uniquely identifying the rendering session for the given account. The ID is case
     *     sensitive, can contain any combination of alphanumeric characters including hyphens and underscores, and
     *     cannot contain more than 256 characters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StopSessionResponse> stopSession(String sessionId, Context context) {
        return impl.stopSessionWithResponseAsync(accountId, sessionId, context);
    }

    /**
     * Get a list of all rendering sessions. Returns a batch of sessions- if more sessions are available the @nextLink
     * property contains the URL where the next batch of sessions can be requested.
     * See {@link #listNextSessionsPage(String nextLink, Context context)}
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all rendering sessions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SessionProperties>> listSessions(Context context) {
        return impl.listSessionsSinglePageAsync(accountId, context);
    }

    /**
     * Get the next page of rendering sessions.
     *
     * @param nextLink The nextLink parameter.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of a list sessions request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SessionProperties>> listNextSessionsPage(String nextLink, Context context) {
        return impl.listSessionsNextSinglePageAsync(nextLink, context);
    }


    /**
     * Starts a conversion using an asset stored in an Azure Blob Storage account. If the remote rendering account has
     * been linked with the storage account no Shared Access Signatures (storageContainerReadListSas,
     * storageContainerWriteSas) for storage access need to be provided. Documentation how to link your Azure Remote
     * Rendering account with the Azure Blob Storage account can be found in the
     * [documentation](https://docs.microsoft.com/azure/remote-rendering/how-tos/create-an-account#link-storage-accounts).
     *
     * <p>All files in the input container starting with the blobPrefix will be retrieved to perform the conversion. To
     * cut down on conversion times only necessary files should be available under the blobPrefix.
     *
     * @param conversionId An ID uniquely identifying the conversion for the given account. The ID is case sensitive,
     *     can contain any combination of alphanumeric characters including hyphens and underscores, and cannot contain
     *     more than 256 characters.
     * @param body Request to start a conversion.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateConversionResponse> startConversion(String conversionId, CreateConversionSettings body, Context context) {
        return impl.createConversionWithResponseAsync(accountId, conversionId, body, context);
    }

    /**
     * Gets the status of a previously created asset conversion.
     *
     * @param conversionId An ID uniquely identifying the conversion for the given account. The ID is case sensitive,
     *     can contain any combination of alphanumeric characters including hyphens and underscores, and cannot contain
     *     more than 256 characters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the status of a previously created asset conversion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetConversionResponse> getConversion(String conversionId, Context context) {
        return impl.getConversionWithResponseAsync(accountId, conversionId, context);
    }

    /**
     * Gets a list of all conversions. Returns a batch of conversions- if more conversions are available the @nextLink
     * property contains the URL where the next batch of conversions can be requested.
     * See {@link #listNextConversionsPage(String nextLink, Context context)}
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all conversions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<Conversion>> listConversions(Context context) {
        return impl.listConversionsSinglePageAsync(accountId, context);
    }

    /**
     * Get the next page of conversions.
     *
     * @param nextLink The nextLink parameter.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<Conversion>> listNextConversionsPage(String nextLink, Context context) {
        return impl.listConversionsNextSinglePageAsync(nextLink, context);
    }
}
