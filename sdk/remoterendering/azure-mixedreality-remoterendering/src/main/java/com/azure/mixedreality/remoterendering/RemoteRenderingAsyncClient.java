// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImpl;
import com.azure.mixedreality.remoterendering.implementation.models.CreateConversionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.SessionProperties;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionInputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionOutputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.UpdateSessionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.CreateSessionSettings;
import com.azure.mixedreality.remoterendering.models.AssetConversion;
import com.azure.mixedreality.remoterendering.models.AssetConversionStatus;
import com.azure.mixedreality.remoterendering.models.BeginSessionOptions;
import com.azure.mixedreality.remoterendering.models.AssetConversionOptions;
import com.azure.mixedreality.remoterendering.models.RemoteRenderingServiceError;
import com.azure.mixedreality.remoterendering.models.RenderingSession;
import com.azure.mixedreality.remoterendering.models.RenderingSessionSize;
import com.azure.mixedreality.remoterendering.models.RenderingSessionStatus;
import com.azure.mixedreality.remoterendering.models.UpdateSessionOptions;
import reactor.core.publisher.Mono;
import com.azure.core.util.polling.PollerFlux;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.FluxUtil.monoError;

/** A builder for creating a new instance of the MixedRealityRemoteRendering type. */
@ServiceClient(builder = RemoteRenderingClientBuilder.class, isAsync = true)
public final class RemoteRenderingAsyncClient {
    private static final Duration DEFAULT_POLLER_TIME = Duration.ofSeconds(10);

    private final ClientLogger logger = new ClientLogger(RemoteRenderingAsyncClient.class);

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
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the rendering session.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RenderingSession, RenderingSession> beginSession(String sessionId) {
        return beginSession(sessionId, new BeginSessionOptions());
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RenderingSession, RenderingSession> beginSession(String sessionId, BeginSessionOptions options) {
        return beginSessionInternal(sessionId, options, Context.NONE);
    }

    PollerFlux<RenderingSession, RenderingSession> beginSessionInternal(String sessionId, BeginSessionOptions options, Context context) {
        Objects.requireNonNull(sessionId, "'sessionId' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        if (sessionId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sessionId' cannot be an empty string."));
        }

        return new PollerFlux<>(
            DEFAULT_POLLER_TIME,
            pollingContext -> impl.getRemoteRenderings().createSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), context).map(r -> ModelTranslator.fromGenerated(r.getValue())),
            pollingContext -> {
                Mono<RenderingSession> response = impl.getRemoteRenderings().getSessionWithResponseAsync(accountId, sessionId, context).map(r -> ModelTranslator.fromGenerated(r.getValue()));
                return response.map(session -> {
                    final RenderingSessionStatus sessionStatus = session.getStatus();
                    LongRunningOperationStatus lroStatus;
                    if (sessionStatus == RenderingSessionStatus.STARTING) {
                        lroStatus = LongRunningOperationStatus.IN_PROGRESS;
                    } else if (sessionStatus == RenderingSessionStatus.ERROR) {
                        lroStatus = LongRunningOperationStatus.FAILED;
                    } else if (sessionStatus == RenderingSessionStatus.READY) {
                        lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    } else if (sessionStatus == RenderingSessionStatus.STOPPED) {
                        lroStatus = LongRunningOperationStatus.USER_CANCELLED;
                    } else {
                        lroStatus = LongRunningOperationStatus.FAILED;
                    }
                    return new PollResponse<>(lroStatus, session);
                });
            },
            (pollingContext, pollResponse) -> impl.getRemoteRenderings().stopSessionWithResponseAsync(accountId, sessionId, context).then(Mono.just(pollingContext.getLatestResponse().getValue())),
            pollingContext -> Mono.just(pollingContext.getLatestResponse().getValue())
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
        return getSessionWithResponse(sessionId).map(Response::getValue);
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
    public Mono<Response<RenderingSession>> getSessionWithResponse(String sessionId) {
        try {
            return withContext(context -> getSessionInternal(sessionId, context));
        } catch (RuntimeException exception) {
            return monoError(this.logger, exception);
        }
    }

    Mono<Response<RenderingSession>> getSessionInternal(String sessionId, Context context) {
        Objects.requireNonNull(sessionId, "'sessionId' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        if (sessionId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sessionId' cannot be an empty string."));
        }

        return impl.getRemoteRenderings().getSessionWithResponseAsync(accountId, sessionId, context).map(ModelTranslator::fromGenerated);
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
        return updateSessionWithResponse(sessionId, options).map(Response::getValue);
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
    public Mono<Response<RenderingSession>> updateSessionWithResponse(String sessionId, UpdateSessionOptions options) {
        try {
            return withContext(context -> updateSessionInternal(sessionId, options, context));
        } catch (RuntimeException exception) {
            return monoError(this.logger, exception);
        }
    }

    Mono<Response<RenderingSession>> updateSessionInternal(String sessionId, UpdateSessionOptions options, Context context) {
        Objects.requireNonNull(sessionId, "'sessionId' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        if (sessionId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sessionId' cannot be an empty string."));
        }

        return impl.getRemoteRenderings().updateSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), context).map(ModelTranslator::fromGenerated);
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
    public Mono<Void> endSession(String sessionId) {
        return endSessionWithResponse(sessionId).then();
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
    public Mono<Response<Void>> endSessionWithResponse(String sessionId) {
        try {
            return withContext(context -> endSessionInternal(sessionId, context));
        } catch (RuntimeException exception) {
            return monoError(this.logger, exception);
        }
    }

    Mono<Response<Void>> endSessionInternal(String sessionId, Context context) {
        Objects.requireNonNull(sessionId, "'sessionId' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        if (sessionId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sessionId' cannot be an empty string."));
        }

        return impl.getRemoteRenderings().stopSessionWithResponseAsync(accountId, sessionId, context).map(r -> r);
    }

    /**
     * Get a list of all rendering sessions.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of all rendering sessions.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<RenderingSession> listSessions() {
        return listSessionsInternal(Context.NONE);
    }

    PagedFlux<RenderingSession> listSessionsInternal(Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");

        return new PagedFlux<>(
            () -> impl.getRemoteRenderings().listSessionsSinglePageAsync(accountId, context).map(p ->
                new PagedResponseBase<HttpRequest, RenderingSession>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)),
            continuationToken -> impl.getRemoteRenderings().listSessionsNextSinglePageAsync(continuationToken, context).map(p ->
                new PagedResponseBase<HttpRequest, RenderingSession>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()),
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<AssetConversion, AssetConversion> beginConversion(String conversionId, AssetConversionOptions options) {
        return beginConversionInternal(conversionId, options, Context.NONE);
    }

    PollerFlux<AssetConversion, AssetConversion> beginConversionInternal(String conversionId, AssetConversionOptions options, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");

        return new PollerFlux<>(
            DEFAULT_POLLER_TIME,
            pollingContext -> impl.getRemoteRenderings().createConversionWithResponseAsync(accountId, conversionId, new CreateConversionSettings(ModelTranslator.toGenerated(options)), context).map(c -> ModelTranslator.fromGenerated(c.getValue())),
            pollingContext -> {
                Mono<AssetConversion> response = impl.getRemoteRenderings().getConversionWithResponseAsync(accountId, conversionId, context).map(c -> ModelTranslator.fromGenerated(c.getValue()));
                return response.map(conversion -> {
                    final AssetConversionStatus convStatus = conversion.getStatus();
                    LongRunningOperationStatus lroStatus;
                    if ((convStatus == AssetConversionStatus.RUNNING) || (convStatus == AssetConversionStatus.NOT_STARTED)) {
                        lroStatus = LongRunningOperationStatus.IN_PROGRESS;
                    } else if (convStatus == AssetConversionStatus.FAILED) {
                        lroStatus = LongRunningOperationStatus.FAILED;
                    } else if (convStatus == AssetConversionStatus.SUCCEEDED) {
                        lroStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    } else if (convStatus == AssetConversionStatus.CANCELLED) {
                        lroStatus = LongRunningOperationStatus.USER_CANCELLED;
                    } else {
                        lroStatus = LongRunningOperationStatus.FAILED;
                    }
                    return new PollResponse<>(lroStatus, conversion);
                });
            },
            (pollingContext, pollResponse) -> Mono.error(new RuntimeException("Cancellation is not supported.")),
            pollingContext -> Mono.just(pollingContext.getLatestResponse().getValue())
        );
    }


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
    public Mono<AssetConversion> getConversion(String conversionId) {
        return getConversionWithResponse(conversionId).map(Response::getValue);
    }

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
    public Mono<Response<AssetConversion>> getConversionWithResponse(String conversionId) {
        try {
            return withContext(context -> getConversionInternal(conversionId, context));
        } catch (RuntimeException exception) {
            return monoError(this.logger, exception);
        }
    }

    Mono<Response<AssetConversion>> getConversionInternal(String conversionId, Context context) {
        Objects.requireNonNull(conversionId, "'conversionId' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        if (conversionId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'conversionId' cannot be an empty string."));
        }

        return impl.getRemoteRenderings().getConversionWithResponseAsync(accountId, conversionId, context).map(ModelTranslator::fromGenerated);
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
    public PagedFlux<AssetConversion> listConversions() {
        return listConversionsInternal(Context.NONE);
    }

    PagedFlux<AssetConversion> listConversionsInternal(Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");

        return new PagedFlux<>(
            () -> impl.getRemoteRenderings().listConversionsSinglePageAsync(accountId, context).map(p ->
                new PagedResponseBase<HttpRequest, AssetConversion>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)),
            continuationToken -> impl.getRemoteRenderings().listConversionsNextSinglePageAsync(continuationToken, context).map(p ->
                new PagedResponseBase<HttpRequest, AssetConversion>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)));
    }




    private static class ModelTranslator {

        private static <T, Y> Response<T> fromGenerated(Response<Y> response) {
            if (response == null) {
                return null;
            }
            return new Response<T>() {

                private final T value = fromGeneratedGeneric(response.getValue());

                @Override
                public int getStatusCode() {
                    return response.getStatusCode();
                }

                @Override
                public HttpHeaders getHeaders() {
                    return response.getHeaders();
                }

                @Override
                public HttpRequest getRequest() {
                    return response.getRequest();
                }

                @Override
                public T getValue() {
                    return this.value;
                }
            };
        }

        @SuppressWarnings("unchecked")
        private static <T, Y> T fromGeneratedGeneric(Y value) {
            if (value == null) {
                return null;
            }
            else if (value instanceof com.azure.mixedreality.remoterendering.implementation.models.Conversion) {
                return (T) fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.Conversion) value);
            }
            else if (value instanceof SessionProperties) {
                return (T) fromGenerated((SessionProperties) value);
            }
            else if (value instanceof com.azure.mixedreality.remoterendering.implementation.models.Error) {
                return (T) fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.Error) value);
            }
            else if (value instanceof com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings) {
                return (T) fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings) value);
            }
            else {
                // throw?
                return null;
            }
        }

        private static AssetConversion fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.Conversion conversion) {
            if (conversion == null) {
                return null;
            }
            return new AssetConversion(
                conversion.getId(),
                fromGenerated(conversion.getSettings()),
                conversion.getOutput() != null ? conversion.getOutput().getOutputAssetUri() : null,
                fromGenerated(conversion.getError()),
                AssetConversionStatus.fromString(conversion.getStatus().toString()),
                conversion.getCreationTime());
        }

        private static RenderingSession fromGenerated(SessionProperties sessionProperties) {
            if (sessionProperties == null) {
                return null;
            }
            return new RenderingSession(
                sessionProperties.getId(),
                Optional.ofNullable(sessionProperties.getArrInspectorPort()).orElse(0),
                Optional.ofNullable(sessionProperties.getHandshakePort()).orElse(0),
                Duration.ofMinutes(Optional.ofNullable(sessionProperties.getElapsedTimeMinutes()).orElse(0)),
                sessionProperties.getHostname(),
                Duration.ofMinutes(Optional.ofNullable(sessionProperties.getMaxLeaseTimeMinutes()).orElse(0)),
                RenderingSessionSize.fromString(sessionProperties.getSize().toString()),
                RenderingSessionStatus.fromString(sessionProperties.getStatus().toString()),
                Optional.ofNullable(sessionProperties.getTeraflops()).orElse(0.0f),
                fromGenerated(sessionProperties.getError()),
                sessionProperties.getCreationTime());
        }

        private static RemoteRenderingServiceError fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.Error error) {
            if (error == null) {
                return null;
            }
            return new RemoteRenderingServiceError(
                error.getCode(),
                error.getMessage(),
                error.getTarget(),
                fromGenerated(error.getInnerError()),
                (error.getDetails() != null) ? error.getDetails().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()) : null);
        }

        private static AssetConversionOptions fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings settings) {
            if (settings == null) {
                return null;
            }
            return new AssetConversionOptions()
                .setInputBlobPrefix(settings.getInputLocation().getBlobPrefix())
                .setInputRelativeAssetPath(settings.getInputLocation().getRelativeInputAssetPath())
                .setInputStorageContainerReadListSas(settings.getInputLocation().getStorageContainerReadListSas())
                .setInputStorageContainerUrl(settings.getInputLocation().getStorageContainerUri())

                .setOutputAssetFilename(settings.getOutputLocation().getOutputAssetFilename())
                .setOutputBlobPrefix(settings.getOutputLocation().getBlobPrefix())
                .setOutputStorageContainerUrl(settings.getOutputLocation().getStorageContainerUri())
                .setOutputStorageContainerWriteSas(settings.getOutputLocation().getStorageContainerWriteSas());
        }

        private static ConversionSettings toGenerated(AssetConversionOptions conversionOptions) {
            if (conversionOptions == null) {
                return null;
            }
            return new ConversionSettings(
                new ConversionInputSettings(
                    conversionOptions.getInputStorageContainerUrl(),
                    conversionOptions.getInputRelativeAssetPath())
                    .setStorageContainerReadListSas(conversionOptions.getInputStorageContainerReadListSas())
                    .setBlobPrefix(conversionOptions.getInputBlobPrefix()),
                new ConversionOutputSettings(conversionOptions.getOutputStorageContainerUrl())
                    .setStorageContainerWriteSas(conversionOptions.getOutputStorageContainerWriteSas())
                    .setBlobPrefix(conversionOptions.getOutputBlobPrefix())
                    .setOutputAssetFilename(conversionOptions.getOutputAssetFilename())
            );
        }

        private static UpdateSessionSettings toGenerated(UpdateSessionOptions options) {
            if (options == null) {
                return null;
            }
            return new UpdateSessionSettings((int) options.getMaxLeaseTime().toMinutes());
        }

        private static CreateSessionSettings toGenerated(BeginSessionOptions options) {
            if (options == null) {
                return null;
            }
            return new CreateSessionSettings((int) options.getMaxLeaseTime().toMinutes(), com.azure.mixedreality.remoterendering.implementation.models.SessionSize.fromString(options.getSize().toString()));
        }
    }
}
