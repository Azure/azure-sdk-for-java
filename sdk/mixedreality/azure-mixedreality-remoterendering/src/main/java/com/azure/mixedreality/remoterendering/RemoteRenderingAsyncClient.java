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
import com.azure.mixedreality.remoterendering.implementation.models.CreateConversionResponse;
import com.azure.mixedreality.remoterendering.implementation.models.CreateConversionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.GetConversionResponse;
import com.azure.mixedreality.remoterendering.implementation.models.SessionProperties;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionInputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionOutputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.UpdateSessionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.CreateSessionSettings;
import com.azure.mixedreality.remoterendering.models.*;
import reactor.core.publisher.Mono;
import com.azure.core.util.polling.PollerFlux;

import java.time.Duration;
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<RenderingSession, RenderingSession> beginSession(String sessionId, CreateSessionOptions options) {
        return beginSessionInternal(sessionId, options, Context.NONE, r -> ModelTranslator.fromGenerated(r.getValue()), RenderingSession::getStatus);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<RenderingSession, RenderingSession> beginSession(String sessionId) {
        return beginSession(sessionId, new CreateSessionOptions());
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<Response<RenderingSession>, Response<RenderingSession>> beginSessionWithResponse(String sessionId, CreateSessionOptions options, Context context) {
        return beginSessionInternal(sessionId, options, context, ModelTranslator::fromGenerated, s -> s.getValue().getStatus());
    }

    private <T> PollerFlux<T, T> beginSessionInternal(String sessionId, CreateSessionOptions options, Context context, Function<Response<SessionProperties>, T> mapper, Function<T, SessionStatus> statusgetter) {
        return new PollerFlux<>(
            options.getPollInterval(),
            pollingContext -> impl.createSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), context).map(mapper),
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
                    return new PollResponse<>(lroStatus, session);
                });
            },
            (pollingContext, pollResponse) -> {
                // TODO should re-query for a new Session object
                return impl.stopSessionWithResponseAsync(accountId, sessionId, context).then(Mono.just(pollingContext.getLatestResponse().getValue()));
            },
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
        return impl.getSessionWithResponseAsync(accountId, sessionId, Context.NONE).map(s -> ModelTranslator.fromGenerated(s.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RenderingSession>> getSessionWithResponse(String sessionId, Context context) {
        return impl.getSessionWithResponseAsync(accountId, sessionId, context).map(ModelTranslator::fromGenerated);
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
        return impl.updateSessionWithResponseAsync(accountId, sessionId, ModelTranslator.toGenerated(options), context).map(ModelTranslator::fromGenerated);
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<RenderingSession> listSessions() {
        return new PagedFlux<>(
            () -> impl.listSessionsSinglePageAsync(accountId, Context.NONE).map(p ->
                new PagedResponseBase<HttpRequest, RenderingSession>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)),
            continuationToken -> impl.listSessionsNextSinglePageAsync(continuationToken, Context.NONE).map(p ->
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<Conversion, Conversion> beginConversion(String conversionId, ConversionOptions options) {
        return beginConversionInternal(conversionId, options, Context.NONE, c -> ModelTranslator.fromGenerated(c.getValue()), c -> ModelTranslator.fromGenerated(c.getValue()), Conversion::getStatus);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public PollerFlux<Response<Conversion>, Response<Conversion>> beginConversionWithResponse(String conversionId, ConversionOptions options, Context context) {
        return beginConversionInternal(conversionId, options, context, ModelTranslator::fromGenerated, ModelTranslator::fromGenerated, s -> s.getValue().getStatus());
    }

    private <T> PollerFlux<T, T> beginConversionInternal(String conversionId, ConversionOptions options, Context context, Function<CreateConversionResponse, T> mapper, Function<GetConversionResponse, T> mapper2, Function<T, ConversionStatus> statusgetter) {
        return new PollerFlux<>(
            options.getPollInterval(),
            pollingContext -> impl.createConversionWithResponseAsync(accountId, conversionId, new CreateConversionSettings(ModelTranslator.toGenerated(options)), context).map(mapper),
            pollingContext -> {
                Mono<T> response = impl.getConversionWithResponseAsync(accountId, conversionId, context).map(mapper2);
                return response.map(conversion -> {
                    final ConversionStatus convStatus = statusgetter.apply(conversion);
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
                    return new PollResponse<>(lroStatus, conversion);
                });
            },
            (pollingContext, pollResponse) ->
                Mono.error(new RuntimeException("Cancellation is not supported."))
            ,
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
    public Mono<Conversion> getConversion(String conversionId) {
        return impl.getConversionWithResponseAsync(accountId, conversionId, Context.NONE).map(r -> ModelTranslator.fromGenerated(r.getValue()));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Conversion>> getConversionWithResponse(String conversionId, Context context) {
        return impl.getConversionWithResponseAsync(accountId, conversionId, context).map(ModelTranslator::fromGenerated);
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
        return new PagedFlux<>(
            () -> impl.listConversionsSinglePageAsync(accountId, Context.NONE).map(p ->
                new PagedResponseBase<HttpRequest, Conversion>(p.getRequest(),
                    p.getStatusCode(),
                    p.getHeaders(),
                    p.getValue().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()),
                    p.getContinuationToken(),
                    null)),
            continuationToken -> impl.listConversionsNextSinglePageAsync(continuationToken, Context.NONE).map(p ->
                new PagedResponseBase<HttpRequest, Conversion>(p.getRequest(),
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
            return new Response<>() {

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
                return (T)fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.Conversion)value);
            }
            else if (value instanceof SessionProperties) {
                return (T)fromGenerated((SessionProperties)value);
            }
            else if (value instanceof com.azure.mixedreality.remoterendering.implementation.models.Error) {
                return (T)fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.Error)value);
            }
            else if (value instanceof com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings) {
                return (T)fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings)value);
            }
            else {
                // throw?
                return null;
            }
        }

        private static Conversion fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.Conversion conversion) {
            if (conversion == null) {
                return null;
            }
            return new Conversion()
                .setId(conversion.getId())
                .setOptions(fromGenerated(conversion.getSettings()))
                .setOutputAssetUrl(conversion.getOutput() != null ? conversion.getOutput().getOutputAssetUri() : null)
                .setError(fromGenerated(conversion.getError()))
                .setConversionStatus(ConversionStatus.fromString(conversion.getStatus().toString()))
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
                .setSessionSize(SessionSize.fromString(sessionProperties.getSize().toString()))
                .setSessionStatus(SessionStatus.fromString(sessionProperties.getStatus().toString()))
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

        private static ConversionOptions fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings settings) {
            if (settings == null) {
                return null;
            }
            return new ConversionOptions()
                .inputBlobPrefix(settings.getInputLocation().getBlobPrefix())
                .inputRelativeAssetPath(settings.getInputLocation().getRelativeInputAssetPath())
                .inputStorageContainerReadListSas(settings.getInputLocation().getStorageContainerReadListSas())
                .inputStorageContainerUrl(settings.getInputLocation().getStorageContainerUri())

                .outputAssetFilename(settings.getOutputLocation().getOutputAssetFilename())
                .outputBlobPrefix(settings.getOutputLocation().getBlobPrefix())
                .outputStorageContainerUrl(settings.getOutputLocation().getStorageContainerUri())
                .outputStorageContainerWriteSas(settings.getOutputLocation().getStorageContainerWriteSas());
        }

        private static ConversionSettings toGenerated(ConversionOptions conversionOptions) {
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
            return new UpdateSessionSettings((int)options.getMaxLeaseTime().toMinutes());
        }

        private static CreateSessionSettings toGenerated(CreateSessionOptions options) {
            if (options == null) {
                return null;
            }
            return new CreateSessionSettings((int)options.getMaxLeaseTime().toMinutes(), com.azure.mixedreality.remoterendering.implementation.models.SessionSize.fromString(options.getSize().toString()));
        }
    }
}
