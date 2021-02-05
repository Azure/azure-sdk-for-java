// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImpl;
import com.azure.mixedreality.remoterendering.implementation.models.CreateConversionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.SessionProperties;
import com.azure.mixedreality.remoterendering.models.*;
import com.azure.mixedreality.remoterendering.models.internal.ModelTranslator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.azure.core.util.polling.PollerFlux;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@ServiceClient(builder = RemoteRenderingClientBuilder.class, isAsync = true)
public final class RemoteRenderingAsyncClient {
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
     * @param options Settings for the session to be created.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the rendering session.
     */
    public PollerFlux<RenderingSession, RenderingSession> beginSession(String sessionId, CreateSessionOptions options) {
        return beginSessionInternal(sessionId, options, Context.NONE, r -> ModelTranslator.fromGenerated(r.getValue()), s -> s.getStatus());
    }

    public PollerFlux<RenderingSession, RenderingSession> beginSession(String sessionId) {
        return beginSession(sessionId, new CreateSessionOptions());
    }

    public PollerFlux<Response<RenderingSession>, Response<RenderingSession>> beginSessionWithResponse(String sessionId, CreateSessionOptions options, Context context) {
        return beginSessionInternal(sessionId, options, context, r -> ModelTranslator.fromGenerated(r), s -> s.getValue().getStatus());
    }

    private <T> PollerFlux<T, T> beginSessionInternal(String sessionId, CreateSessionOptions options, Context context, Function<Response<SessionProperties>, T> mapper, Function<T, SessionStatus> statusgetter) {
        return new PollerFlux<T, T>(
            options.getPollInterval(),
            pollingContext -> {
                return impl.createSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), context).map(mapper);
            },
            pollingContext -> {
                Mono<T> response = impl.getSessionWithResponseAsync(accountId, sessionId, context).map(mapper);
                return response.map(session -> {
                    final SessionStatus sessionStatus = statusgetter.apply(session);
                    LongRunningOperationStatus lroStatus = LongRunningOperationStatus.NOT_STARTED;
                    if (sessionStatus == SessionStatus.STARTING) {
                        lroStatus = LongRunningOperationStatus.IN_PROGRESS;
                    } else if (sessionStatus == SessionStatus.ERROR) {
                        lroStatus = LongRunningOperationStatus.FAILED;
                    } else if (sessionStatus == SessionStatus.READY) {
                        lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    } else if (sessionStatus == SessionStatus.STOPPED) {
                        // TODO Check semantics of STOPPED and USER_CANCELLED is close enough.
                        lroStatus = LongRunningOperationStatus.USER_CANCELLED;
                    } else {
                        // TODO Assert? Throw?
                    }
                    return new PollResponse<T>(lroStatus, session);
                });
            },
            (pollingContext, pollResponse) -> {
                // TODO should re-query for a new Session object
                return impl.stopSessionWithResponseAsync(accountId, sessionId, context).then(Mono.just(pollingContext.getLatestResponse().getValue()));
            },
            pollingContext -> {
                PollResponse<T> response = pollingContext.getLatestResponse();
                return Mono.just(response.getValue());
            }
        );
    }



    /**
     * Gets properties of a particular rendering session.
     *
     * @param sessionId An ID uniquely identifying the rendering session for the given account. The ID is case
     *     sensitive, can contain any combination of alphanumeric characters including hyphens and underscores, and
     *     cannot contain more than 256 characters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the rendering session.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RenderingSession> getSession(String sessionId) {
        return impl.getSessionWithResponseAsync(accountId, sessionId, Context.NONE).map(s -> ModelTranslator.fromGenerated(s.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RenderingSession>> getSessionWithResponse(String sessionId, Context context) {
        return impl.getSessionWithResponseAsync(accountId, sessionId, context).map(s -> ModelTranslator.fromGenerated(s));
    }

    /**
     * Updates a particular rendering session.
     *
     * @param sessionId An ID uniquely identifying the rendering session for the given account. The ID is case
     *     sensitive, can contain any combination of alphanumeric characters including hyphens and underscores, and
     *     cannot contain more than 256 characters.
     * @param options Options for the session to be updated.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the rendering session.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RenderingSession> updateSession(String sessionId, UpdateSessionOptions options) {
        return impl.updateSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), Context.NONE).map(s -> ModelTranslator.fromGenerated(s.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RenderingSession>> updateSessionWithResponse(String sessionId, UpdateSessionOptions options, Context context) {
        return impl.updateSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), context).map(s -> ModelTranslator.fromGenerated(s));
    }

    /**
     * Stops a particular rendering session.
     *
     * @param sessionId An ID uniquely identifying the rendering session for the given account. The ID is case
     *     sensitive, can contain any combination of alphanumeric characters including hyphens and underscores, and
     *     cannot contain more than 256 characters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return nothing on completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopSession(String sessionId) {
        return impl.stopSessionWithResponseAsync(accountId, sessionId, Context.NONE).then();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopSessionWithResponse(String sessionId, Context context) {
        return impl.stopSessionWithResponseAsync(accountId, sessionId, context).map(r -> r);
    }

    /**
     * Get a list of all rendering sessions.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all rendering sessions.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedFlux<RenderingSession> listSessions() {
        return new PagedFlux<RenderingSession>(
            () -> impl.listSessionsSinglePageAsync(accountId, Context.NONE).map(p ->
                new PagedResponseBase<HttpRequest, RenderingSession>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(sessionProperties -> ModelTranslator.fromGenerated(sessionProperties)).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)),
            continuationToken -> impl.listSessionsNextSinglePageAsync(continuationToken, Context.NONE).map(p ->
                new PagedResponseBase<HttpRequest, RenderingSession>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(sessionProperties -> ModelTranslator.fromGenerated(sessionProperties)).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)));
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
     * @param options The conversion options.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the conversion.
     */
    public PollerFlux<Conversion, Conversion> beginConversion(String conversionId, ConversionOptions options) {
        return new PollerFlux<Conversion, Conversion>(
            options.getPollInterval(),
            pollingContext -> {
                return impl.createConversionWithResponseAsync(accountId, conversionId, new CreateConversionSettings(ModelTranslator.toGenerated(options)), Context.NONE)
                    .map(r -> ModelTranslator.fromGenerated(r.getValue()));
            },
            pollingContext -> {
                Mono<Conversion> response = impl.getConversionWithResponseAsync(accountId, conversionId, Context.NONE).map(r -> ModelTranslator.fromGenerated(r.getValue()));
                return response.map(conversion -> {
                    final ConversionStatus convStatus = conversion.getStatus();
                    LongRunningOperationStatus lroStatus = LongRunningOperationStatus.NOT_STARTED;
                    // TODO Check whether semantics of LongRunningOperationStatus.NOT_STARTED matches ConversionStatus.NOT_STARTED.
                    if ((convStatus == ConversionStatus.RUNNING) || (convStatus == ConversionStatus.NOT_STARTED)) {
                        lroStatus = LongRunningOperationStatus.IN_PROGRESS;
                    } else if (convStatus == ConversionStatus.FAILED) {
                        lroStatus = LongRunningOperationStatus.FAILED;
                    } else if (convStatus == ConversionStatus.SUCCEEDED) {
                        lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    } else if (convStatus == ConversionStatus.CANCELLED) {
                        lroStatus = LongRunningOperationStatus.USER_CANCELLED;
                    } else {
                        // TODO Assert? Throw?
                    }
                    return new PollResponse<Conversion>(lroStatus, conversion);
                });
            },
            (pollingContext, pollResponse) ->
                Mono.error(new RuntimeException("Cancellation is not supported."))
            ,
            pollingContext -> {
                PollResponse<Conversion> response = pollingContext.getLatestResponse();
                return Mono.just(response.getValue());
            }
        );
    };

    /**
     * Gets the status of a previously created asset conversion.
     *
     * @param conversionId An ID uniquely identifying the conversion for the given account. The ID is case sensitive,
     *     can contain any combination of alphanumeric characters including hyphens and underscores, and cannot contain
     *     more than 256 characters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the conversion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Conversion> getConversion(String conversionId) {
        return impl.getConversionWithResponseAsync(accountId, conversionId, Context.NONE).map(r -> ModelTranslator.fromGenerated(r.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Conversion>> getConversionWithResponse(String conversionId, Context context) {
        return impl.getConversionWithResponseAsync(accountId, conversionId, context).map(r -> ModelTranslator.fromGenerated(r));
    }

    /**
     * Gets a list of all conversions.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all conversions.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Conversion> listConversions() {
        return new PagedFlux<Conversion>(
            () -> impl.listConversionsSinglePageAsync(accountId, Context.NONE).map(p ->
                new PagedResponseBase<HttpRequest, Conversion>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(conversion -> ModelTranslator.fromGenerated(conversion)).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)),
            continuationToken -> impl.listConversionsNextSinglePageAsync(continuationToken, Context.NONE).map(p ->
                new PagedResponseBase<HttpRequest, Conversion>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(conversion -> ModelTranslator.fromGenerated(conversion)).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)));
    }
}
