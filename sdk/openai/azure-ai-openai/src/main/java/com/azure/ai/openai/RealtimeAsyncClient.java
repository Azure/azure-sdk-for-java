package com.azure.ai.openai;

import com.azure.ai.openai.implementation.LoggingUtils;
import com.azure.ai.openai.implementation.RealtimesImpl;
import com.azure.ai.openai.implementation.websocket.ClientEndpointConfiguration;
import com.azure.ai.openai.implementation.websocket.CloseReason;
import com.azure.ai.openai.implementation.websocket.RealtimeClientState;
import com.azure.ai.openai.implementation.websocket.WebSocketClient;
import com.azure.ai.openai.implementation.websocket.WebSocketClientNettyImpl;
import com.azure.ai.openai.implementation.websocket.WebSocketSession;
import com.azure.ai.openai.models.realtime.RealtimeClientEvent;
import com.azure.ai.openai.models.realtime.RealtimeServerEvent;
import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class RealtimeAsyncClient implements Closeable {

    // logging
    private ClientLogger logger;
    private final AtomicReference<ClientLogger> loggerReference = new AtomicReference<>();

    // client state
    private final ClientState clientState = new ClientState();
    // state on close
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();
    // state on stop by user
    private final AtomicBoolean isStoppedByUser = new AtomicBoolean();
    private final AtomicReference<Sinks.Empty<Void>> isStoppedByUserSink = new AtomicReference<>();

    // websocket client
    private final WebSocketClient webSocketClient;
    private WebSocketSession webSocketSession;

    // client specifics
    private final ClientEndpointConfiguration clientEndpointConfiguration;
    private final String applicationId;

    // retry
    private final Retry sendMessageRetrySpec;

    RealtimeAsyncClient(
            WebSocketClient webSocketClient, ClientEndpointConfiguration cec, String applicationId, RetryStrategy retryStrategy) {
        updateLogger(applicationId, null);

        this.webSocketClient = webSocketClient == null ? new WebSocketClientNettyImpl() : webSocketClient;
        this.clientEndpointConfiguration = cec;
        this.applicationId = applicationId;
        this.sendMessageRetrySpec = Retry.from(signals -> {
            AtomicInteger retryCount = new AtomicInteger(0);
            return signals.concatMap(s -> {
                Mono<Retry.RetrySignal> ret = Mono.error(s.failure());
                // TODO jpalvarezl: replace `SendMessageFailedException` with the OpenAI Realtime type for this
//                if (s.failure() instanceof SendMessageFailedException) {
//                    if (((SendMessageFailedException) s.failure()).isTransient()) {
//                        int retryAttempt = retryCount.incrementAndGet();
//                        if (retryAttempt <= retryStrategy.getMaxRetries()) {
//                            ret = Mono.delay(retryStrategy.calculateRetryDelay(retryAttempt)).then(Mono.just(s));
//                        }
//                    }
//                }
                return ret;
            });
        });

        // TODO jpalvarezl: remove this:
        this.serviceClient = null;
    }

    @Override
    public void close() throws IOException {

    }

// --------------- Code gen stuff --------------------------------

    @Generated
    private final RealtimesImpl serviceClient;

    /**
     * Initializes an instance of RealtimeAsyncClient class.
     *
     * @param serviceClient the service client implementation.
     */
    @Generated
    RealtimeAsyncClient(RealtimesImpl serviceClient) {
        this.webSocketClient = null;
        this.clientEndpointConfiguration = null;
        this.applicationId = null;
        this.sendMessageRetrySpec = null;
        this.serviceClient = serviceClient;
    }

    public Mono<Void> start() {
        return this.start(null);
    }

    public Mono<Void>start(Runnable postStartTask) {
        if(clientState.get() == RealtimeClientState.CLOSED) {
            return Mono.error(
                    logger.logExceptionAsError(new IllegalStateException("Failed to start. Client is CLOSED.")));
        }

        return Mono.defer(() -> {
            logger.atInfo().addKeyValue("currentClientState", clientState.get()).log("Start client called.");

            isStoppedByUser.set(false);
            isStoppedByUserSink.set(null);

            boolean success = clientState.changeStateOn(RealtimeClientState.STOPPED, RealtimeClientState.CONNECTING);
            if (!success) {
                return Mono.error(
                        logger.logExceptionAsError(new IllegalStateException("Failed to start. Client is not STOPPED.")));
            } else {
                if (postStartTask != null) {
                    postStartTask.run();
                }

                return Mono.empty();
            }
        })// .then( handle TokenCredential retrieval);
                .then(Mono.<Void>fromRunnable( () -> {
                    this.webSocketSession = webSocketClient.connectToServer(this.clientEndpointConfiguration, loggerReference,
                            this::handleMessage, this::handleSessionOpen, this::handleSessionClose);

                })).subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> {
                    // stop if error, do not send StoppedEvent when it fails at start(), which would have exception thrown
                    handleClientStop(false);
                });
    }

    private void handleMessage(Object message) {
        System.out.println((String) message);
        // TODO jpalvarezl: implement this
    }

    private void handleSessionOpen(WebSocketSession session) {
        logger.atVerbose().log("Session opened");

        clientState.changeState(RealtimeClientState.CONNECTED);

        // TODO jpalvarezl: implement this
    }

    private void handleSessionClose(CloseReason closeReason) {
        logger.atVerbose().addKeyValue("code", closeReason.getCloseCode()).log("Session closed");
        //TODO jpavarezl: implement this
    }

    private void handleClientStop(boolean sendStoppedEvent) {
        clientState.changeState(RealtimeClientState.STOPPED);
        // TODO jpalvarezl: implement this
    }


//    /**
//     * Starts a real-time conversation session.
//     * <p><strong>Request Body Schema</strong></p>
//     *
//     * <pre>{@code
//     * [
//     *      (Required){
//     *         type: String(session.update/input_audio_buffer.append/input_audio_buffer.commit/input_audio_buffer.clear/conversation.item.create/conversation.item.delete/conversation.item.truncate/response.create/response.cancel) (Required)
//     *         event_id: String (Optional)
//     *     }
//     * ]
//     * }</pre>
//     *
//     * <p><strong>Response Body Schema</strong></p>
//     *
//     * <pre>{@code
//     * [
//     *      (Required){
//     *         type: String(session.created/session.updated/conversation.created/conversation.item.created/conversation.item.deleted/conversation.item.truncated/response.created/response.done/rate_limits.updated/response.output_item.added/response.output_item.done/response.content_part.added/response.content_part.done/response.audio.delta/response.audio.done/response.audio_transcript.delta/response.audio_transcript.done/response.text.delta/response.text.done/response.function_call_arguments.delta/response.function_call_arguments.done/input_audio_buffer.speech_started/input_audio_buffer.speech_stopped/conversation.item.input_audio_transcription.completed/conversation.item.input_audio_transcription.failed/input_audio_buffer.committed/input_audio_buffer.cleared/error) (Required)
//     *         event_id: String (Required)
//     *     }
//     * ]
//     * }</pre>
//     *
//     * @param requestMessages The requestMessages parameter.
//     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
//     * @throws HttpResponseException thrown if the request is rejected by server.
//     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
//     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
//     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
//     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
//     */
//    @Generated
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<Response<BinaryData>> startRealtimeSessionWithResponse(BinaryData requestMessages,
//                                                                       RequestOptions requestOptions) {
//        return this.serviceClient.startRealtimeSessionWithResponseAsync(requestMessages, requestOptions);
//    }
//
//    /**
//     * Starts a real-time conversation session.
//     *
//     * @param requestMessages The requestMessages parameter.
//     * @throws IllegalArgumentException thrown if parameters fail the validation.
//     * @throws HttpResponseException thrown if the request is rejected by server.
//     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
//     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
//     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
//     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
//     * @return the response body on successful completion of {@link Mono}.
//     */
//    @Generated
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<List<RealtimeServerEvent>> startRealtimeSession(List<RealtimeClientEvent> requestMessages) {
//        // Generated convenience method for startRealtimeSessionWithResponse
//        RequestOptions requestOptions = new RequestOptions();
//        return startRealtimeSessionWithResponse(BinaryData.fromObject(requestMessages), requestOptions)
//                .flatMap(FluxUtil::toMono)
//                .map(protocolMethodData -> protocolMethodData.toObject(TYPE_REFERENCE_LIST_REALTIME_SERVER_EVENT));
//    }
//
//    @Generated
//    private static final TypeReference<List<RealtimeServerEvent>> TYPE_REFERENCE_LIST_REALTIME_SERVER_EVENT
//            = new TypeReference<List<RealtimeServerEvent>>() {
//    };

    private void updateLogger(String applicationId, String connectionId) {
        logger = new ClientLogger(RealtimeAsyncClient.class,
                LoggingUtils.createContextWithConnectionId(applicationId, connectionId));
        loggerReference.set(logger);
    }

    private final class ClientState {
        private final AtomicReference<RealtimeClientState> clientState = new AtomicReference<>(
                RealtimeClientState.STOPPED);

        RealtimeClientState get() {
            return clientState.get();
        }

        RealtimeClientState changeState(RealtimeClientState newState) {
            RealtimeClientState previousState = clientState.getAndSet(newState);
            logger.atInfo()
                    .addKeyValue("currentClientState", newState)
                    .addKeyValue("previousClientState", previousState)
                    .log("Client state changed");
            return previousState;
        }

        boolean changeStateOn(RealtimeClientState expectedCurrentState, RealtimeClientState newState) {
            boolean success = clientState.compareAndSet(expectedCurrentState, newState);
            if (success) {
                logger.atInfo()
                        .addKeyValue("currentClientState", newState)
                        .addKeyValue("previousClientState", expectedCurrentState)
                        .log("Client state changed.");
            }
            return success;
        }
    }
}

