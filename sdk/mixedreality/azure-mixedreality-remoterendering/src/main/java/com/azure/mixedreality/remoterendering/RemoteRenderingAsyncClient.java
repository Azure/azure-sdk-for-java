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
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImpl;
import com.azure.mixedreality.remoterendering.implementation.models.RemoteRenderingsCreateConversionResponse;
import com.azure.mixedreality.remoterendering.implementation.models.CreateConversionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.RemoteRenderingsGetConversionResponse;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** A builder for creating a new instance of the MixedRealityRemoteRendering type. */
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RenderingSession, RenderingSession> beginSession(String sessionId, BeginSessionOptions options) {
        return beginSessionInternal(sessionId, options, Context.NONE, r -> ModelTranslator.fromGenerated(r.getValue()), RenderingSession::getStatus);
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

    PollerFlux<Response<RenderingSession>, Response<RenderingSession>> beginSessionInternal(String sessionId, BeginSessionOptions options, Context context) {
        return beginSessionInternal(sessionId, options, context, ModelTranslator::fromGenerated, s -> s.getValue().getStatus());
    }

    private <T> PollerFlux<T, T> beginSessionInternal(String sessionId, BeginSessionOptions options, Context context, Function<Response<SessionProperties>, T> mapper, Function<T, RenderingSessionStatus> statusgetter) {
        return new PollerFlux<>(
            Duration.ofSeconds(10),
            pollingContext -> impl.getRemoteRenderings().createSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), context).map(mapper),
            pollingContext -> {
                Mono<T> response = impl.getRemoteRenderings().getSessionWithResponseAsync(accountId, sessionId, context).map(mapper);
                return response.map(session -> {
                    final RenderingSessionStatus sessionStatus = statusgetter.apply(session);
                    LongRunningOperationStatus lroStatus = LongRunningOperationStatus.NOT_STARTED;
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
        return impl.getRemoteRenderings().getSessionWithResponseAsync(accountId, sessionId, Context.NONE).map(s -> ModelTranslator.fromGenerated(s.getValue()));
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
        return getSessionInternal(sessionId, Context.NONE);
    }

    Mono<Response<RenderingSession>> getSessionInternal(String sessionId, Context context) {
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
        return impl.getRemoteRenderings().updateSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), Context.NONE).map(s -> ModelTranslator.fromGenerated(s.getValue()));
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
        return updateSessionInternal(sessionId, options, Context.NONE);
    }

    Mono<Response<RenderingSession>> updateSessionInternal(String sessionId, UpdateSessionOptions options, Context context) {
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
        return impl.getRemoteRenderings().stopSessionWithResponseAsync(accountId, sessionId, Context.NONE).then();
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
        return endSessionInternal(sessionId, Context.NONE);
    }

    Mono<Response<Void>> endSessionInternal(String sessionId, Context context) {
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
        return beginConversionInternal(conversionId, options, Context.NONE, c -> ModelTranslator.fromGenerated(c.getValue()), c -> ModelTranslator.fromGenerated(c.getValue()), AssetConversion::getStatus);
    }

    PollerFlux<Response<AssetConversion>, Response<AssetConversion>> beginConversionInternal(String conversionId, AssetConversionOptions options, Context context) {
        return beginConversionInternal(conversionId, options, context, ModelTranslator::fromGenerated, ModelTranslator::fromGenerated, s -> s.getValue().getStatus());
    }

    private <T> PollerFlux<T, T> beginConversionInternal(String conversionId, AssetConversionOptions options, Context context, Function<RemoteRenderingsCreateConversionResponse, T> mapper, Function<RemoteRenderingsGetConversionResponse, T> mapper2, Function<T, AssetConversionStatus> statusgetter) {
        return new PollerFlux<>(
            Duration.ofSeconds(10),
            pollingContext -> impl.getRemoteRenderings().createConversionWithResponseAsync(accountId, conversionId, new CreateConversionSettings(ModelTranslator.toGenerated(options)), context).map(mapper),
            pollingContext -> {
                Mono<T> response = impl.getRemoteRenderings().getConversionWithResponseAsync(accountId, conversionId, context).map(mapper2);
                return response.map(conversion -> {
                    final AssetConversionStatus convStatus = statusgetter.apply(conversion);
                    LongRunningOperationStatus lroStatus = LongRunningOperationStatus.NOT_STARTED;
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
        return impl.getRemoteRenderings().getConversionWithResponseAsync(accountId, conversionId, Context.NONE).map(r -> ModelTranslator.fromGenerated(r.getValue()));
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
        return getConversionInternal(conversionId, Context.NONE);
    }

    Mono<Response<AssetConversion>> getConversionInternal(String conversionId, Context context) {
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
            return new AssetConversion()
                .setId(conversion.getId())
                .setOptions(fromGenerated(conversion.getSettings()))
                .setOutputAssetUrl(conversion.getOutput() != null ? conversion.getOutput().getOutputAssetUri() : null)
                .setError(fromGenerated(conversion.getError()))
                .setConversionStatus(AssetConversionStatus.fromString(conversion.getStatus().toString()))
                .setCreationTime(conversion.getCreationTime());
        }

        private static RenderingSession fromGenerated(SessionProperties sessionProperties) {
            if (sessionProperties == null) {
                return null;
            }
            return new RenderingSession()
                .setId(sessionProperties.getId())
                .setHandshakePort(sessionProperties.getHandshakePort())
                .setElapsedTime(Duration.ofMinutes(sessionProperties.getElapsedTimeMinutes()))
                .setHostname(sessionProperties.getHostname())
                .setMaxLeaseTime(Duration.ofMinutes(sessionProperties.getMaxLeaseTimeMinutes()))
                .setSize(RenderingSessionSize.fromString(sessionProperties.getSize().toString()))
                .setStatus(RenderingSessionStatus.fromString(sessionProperties.getStatus().toString()))
                .setTeraflops(sessionProperties.getTeraflops())
                .setError(fromGenerated(sessionProperties.getError()))
                .setCreationTime(sessionProperties.getCreationTime());
        }

        private static RemoteRenderingServiceError fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.Error error) {
            if (error == null) {
                return null;
            }
            return new RemoteRenderingServiceError()
                .setCode(error.getCode())
                .setMessage(error.getMessage())
                .setTarget(error.getTarget())
                .setInnerError(fromGenerated(error.getInnerError()))
                .setRootErrors((error.getDetails() != null) ? error.getDetails().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()) : null);
        }

        private static AssetConversionOptions fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings settings) {
            if (settings == null) {
                return null;
            }
            return new AssetConversionOptions()
                .inputBlobPrefix(settings.getInputLocation().getBlobPrefix())
                .inputRelativeAssetPath(settings.getInputLocation().getRelativeInputAssetPath())
                .inputStorageContainerReadListSas(settings.getInputLocation().getStorageContainerReadListSas())
                .inputStorageContainerUrl(settings.getInputLocation().getStorageContainerUri())

                .outputAssetFilename(settings.getOutputLocation().getOutputAssetFilename())
                .outputBlobPrefix(settings.getOutputLocation().getBlobPrefix())
                .outputStorageContainerUrl(settings.getOutputLocation().getStorageContainerUri())
                .outputStorageContainerWriteSas(settings.getOutputLocation().getStorageContainerWriteSas());
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
