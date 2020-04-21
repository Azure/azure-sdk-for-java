package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.signalr.implementation.client.AzureWebSocketServiceRestAPI;
import com.azure.messaging.signalr.implementation.client.HealthApis;
import com.azure.messaging.signalr.implementation.client.WebSocketConnectionApis;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

@ServiceClient(builder = SignalRClientBuilder.class,
    isAsync = true
//    serviceInterfaces = WebSocketConnectionApisService.class // FIXME private interface, can't set it
)
public final class SignalRAsyncClient {
    // FIXME what is the right tracing namespace value?
    static final String SIGNALR_TRACING_NAMESPACE_VALUE = "Microsoft.SignalR";

    private final ClientLogger logger = new ClientLogger(SignalRAsyncClient.class);

    private final AzureWebSocketServiceRestAPI innerClient;
    private final WebSocketConnectionApis dataClient;
    private final HealthApis healthClient;
    private final SignalRServiceVersion serviceVersion;

    SignalRAsyncClient(AzureWebSocketServiceRestAPI innerClient, SignalRServiceVersion serviceVersion) {
        this.innerClient = innerClient;
        this.dataClient = innerClient.webSocketConnectionApis();
        this.healthClient = innerClient.healthApis();
        this.serviceVersion = serviceVersion;
    }

    // FIXME this is a nicer pattern than what we have in existing client libraries
    private Context configureTracing(Context context) {
        return (context == null ? Context.NONE : context)
           .addData(AZ_TRACING_NAMESPACE_KEY, SIGNALR_TRACING_NAMESPACE_VALUE);
    }

    public SignalRHubAsyncClient getHubClient(String hub) {
        return new SignalRHubAsyncClient(innerClient, hub);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SignalRStatus> getStatus() {
        return healthClient.getHealthWithResponseAsync()
            .map(Response::getStatusCode)
            .map(SignalRStatus::new);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(String message) {
        return sendToAll(message, Context.NONE);
    }

    Mono<Response<Void>> sendToAll(String message, Context context) {
        return dataClient.sendToAllWithResponseAsync(message, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending message to all: '{}'", message))
           .doOnSuccess(response -> logger.info("Sent message to all: '{}', response: {}", message, response.getValue()))
           .doOnError(error -> logger.warning("Failed to send message to all: {}, response: {}", message, error));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(byte[] data) {
        return sendToAll(data, Context.NONE);
    }

    Mono<Response<Void>> sendToAll(byte[] data, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return dataClient.sendToAllWithResponseAsync(byteFlux, data.length, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending data to all"))
           .doOnSuccess(response -> logger.info("Sent data to all, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send data to all, response: {}", error));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(String user, String message) {
        return sendToUser(user, message, Context.NONE);
    }

    Mono<Response<Void>> sendToUser(String user, String message, Context context) {
        return dataClient.sendToUserWithResponseAsync(user, message, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending message '{}' to user '{}'", message, user))
           .doOnSuccess(response -> logger.info("Sent message '{}' to user '{}', response: {}", message, response.getValue()))
           .doOnError(error -> logger.warning("Failed to send message to user: {}, response: {}", user, error));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(String user, byte[] data) {
        return sendToUser(user, data, Context.NONE);
    }

    Mono<Response<Void>> sendToUser(String user, byte[] data, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return dataClient.sendToUserWithResponseAsync(user, byteFlux, data.length, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending data to all"))
           .doOnSuccess(response -> logger.info("Sent data to all, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send data to all, response: {}", error));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(String connectionId, String message) {
        return sendToConnection(connectionId, message, Context.NONE);
    }

    Mono<Response<Void>> sendToConnection(String connectionId, String message, Context context) {
        return dataClient.sendToConnectionWithResponseAsync(connectionId, message, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending message '{}' to connection '{}'", message, connectionId))
           .doOnSuccess(response -> logger.info("Sent message '{}' to connection '{}', response: {}", message, response.getValue()))
           .doOnError(error -> logger.warning("Failed to send message to connection: {}, response: {}", connectionId, error));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(String connectionId, byte[] data) {
        return sendToConnection(connectionId, data, Context.NONE);
    }

    Mono<Response<Void>> sendToConnection(String connectionId, byte[] data, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return dataClient.sendToConnectionWithResponseAsync(connectionId, byteFlux, data.length, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending data to connection '{}'", connectionId))
           .doOnSuccess(response -> logger.info("Sent data to connection '{}', response: {}", connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to send data to connection: {}, response: {}", connectionId, error));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(String connectionId) {
        return closeConnection(connectionId, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(String connectionId, String reason) {
        return closeConnection(connectionId, reason, Context.NONE);
    }

    Mono<Response<Void>> closeConnection(String connectionId, String reason, Context context) {
        return dataClient.closeConnectionWithResponseAsync(connectionId, configureTracing(context), reason)
           .doOnSubscribe(ignoredValue -> logger.info("Closing connection ID {} with reason '{}'", connectionId, reason))
           .doOnSuccess(response -> logger.info("Closed connection ID {}, response: {}", connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to close connection ID {}, response: {}", connectionId, error));
    }
}
