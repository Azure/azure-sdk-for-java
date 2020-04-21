package com.azure.messaging.signalr.implementation.client;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to all the operations defined in
 * WebSocketConnectionApis.
 */
public final class WebSocketConnectionApis {
    /**
     * The proxy service used to perform REST calls.
     */
    private WebSocketConnectionApisService service;

    /**
     * The service client containing this operation class.
     */
    private AzureWebSocketServiceRestAPI client;

    /**
     * Initializes an instance of WebSocketConnectionApis.
     * 
     * @param client the instance of the service client containing this operation class.
     */
    WebSocketConnectionApis(AzureWebSocketServiceRestAPI client) {
        this.service = RestProxy.create(WebSocketConnectionApisService.class, client.getHttpPipeline());
        this.client = client;
    }

    /**
     * The interface defining all the services for
     * AzureWebSocketServiceRestAPIWebSocketConnectionApis to be used by the
     * proxy service to perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "AzureWebSocketServiceRestAPIWebSocketConnectionApis")
    private interface WebSocketConnectionApisService {
        @Post("/ws/api/v1")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToAll(@HostParam("$host") String host, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Post("/ws/api/v1")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToAll(@HostParam("$host") String host, @BodyParam("text/plain") String data, Context context);

        @Post("/ws/api/v1/hubs/{hub}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubConnections(@HostParam("$host") String host, @PathParam("hub") String hub, @BodyParam("text/plain") String data, Context context);

        @Post("/ws/api/v1/hubs/{hub}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubConnections(@HostParam("$host") String host, @PathParam("hub") String hub, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Post("/ws/api/v1/users/{id}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToUser(@HostParam("$host") String host, @PathParam("id") String id, @BodyParam("text/plain") String data, Context context);

        @Post("/ws/api/v1/users/{id}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToUser(@HostParam("$host") String host, @PathParam("id") String id, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Post("/ws/api/v1/hubs/{hub}/users/{id}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubUser(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("id") String id, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Post("/ws/api/v1/hubs/{hub}/users/{id}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubUser(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("id") String id, @BodyParam("text/plain") String data, Context context);

        @Post("/ws/api/v1/connections/{connectionId}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToConnection(@HostParam("$host") String host, @PathParam("connectionId") String connectionId, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Post("/ws/api/v1/connections/{connectionId}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToConnection(@HostParam("$host") String host, @PathParam("connectionId") String connectionId, @BodyParam("text/plain") String data, Context context);

        @Get("/ws/api/v1/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> getConnection(@HostParam("$host") String host, @PathParam("connectionId") String connectionId, Context context);

        @Head("/ws/api/v1/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<SimpleResponse<Boolean>> headConnection(@HostParam("$host") String host, @PathParam("connectionId") String connectionId, Context context);

        @Delete("/ws/api/v1/connections/{connectionId}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> closeConnection(@HostParam("$host") String host, @PathParam("connectionId") String connectionId, @QueryParam("reason") String reason, Context context);

        @Post("/ws/api/v1/hubs/{hub}/connections/{connectionId}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubConnection(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("connectionId") String connectionId, @BodyParam("text/plain") String data, Context context);

        @Post("/ws/api/v1/hubs/{hub}/connections/{connectionId}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubConnection(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("connectionId") String connectionId, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Get("/ws/api/v1/hubs/{hub}/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> getHubConnection(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("connectionId") String connectionId, Context context);

        @Head("/ws/api/v1/hubs/{hub}/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<SimpleResponse<Boolean>> headHubConnection(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("connectionId") String connectionId, Context context);

        @Delete("/ws/api/v1/hubs/{hub}/connections/{connectionId}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> closeHubConnection(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("connectionId") String connectionId, @QueryParam("reason") String reason, Context context);

        @Post("/ws/api/v1/groups/{group}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToGroup(@HostParam("$host") String host, @PathParam("group") String group, @BodyParam("text/plain") String data, Context context);

        @Post("/ws/api/v1/groups/{group}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToGroup(@HostParam("$host") String host, @PathParam("group") String group, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Post("/ws/api/v1/hubs/{hub}/groups/{group}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubGroup(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @BodyParam("text/plain") String data, Context context);

        @Post("/ws/api/v1/hubs/{hub}/groups/{group}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> sendToHubGroup(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @BodyParam("application/octet-stream") Flux<ByteBuffer> data, @HeaderParam("Content-Length") long contentLength, Context context);

        @Put("/ws/api/v1/groups/{group}/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> addConnection(@HostParam("$host") String host, @PathParam("group") String group, @PathParam("connectionId") String connectionId, Context context);

        @Delete("/ws/api/v1/groups/{group}/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> removeConnection(@HostParam("$host") String host, @PathParam("group") String group, @PathParam("connectionId") String connectionId, Context context);

        @Get("/ws/api/v1/groups/{group}/users/{user}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> getUser(@HostParam("$host") String host, @PathParam("group") String group, @PathParam("user") String user, Context context);

        @Head("/ws/api/v1/groups/{group}/users/{user}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<SimpleResponse<Boolean>> headUser(@HostParam("$host") String host, @PathParam("group") String group, @PathParam("user") String user, Context context);

        @Put("/ws/api/v1/groups/{group}/users/{user}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> addUser(@HostParam("$host") String host, @PathParam("group") String group, @PathParam("user") String user, @QueryParam("ttl") Integer ttl, Context context);

        @Delete("/ws/api/v1/groups/{group}/users/{user}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> removeUserFromGroup(@HostParam("$host") String host, @PathParam("group") String group, @PathParam("user") String user, Context context);

        @Delete("/ws/api/v1/users/{user}/groups")
        @ExpectedResponses({200, 202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> removeUser(@HostParam("$host") String host, @PathParam("user") String user, Context context);

        @Put("/ws/api/v1/hubs/{hub}/groups/{group}/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> addHubConnection(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @PathParam("connectionId") String connectionId, Context context);

        @Delete("/ws/api/v1/hubs/{hub}/groups/{group}/connections/{connectionId}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> removeHubConnection(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @PathParam("connectionId") String connectionId, Context context);

        @Get("/ws/api/v1/hubs/{hub}/groups/{group}/users/{user}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> getHubUser(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @PathParam("user") String user, Context context);

        @Head("/ws/api/v1/hubs/{hub}/groups/{group}/users/{user}")
        @ExpectedResponses({200, 400, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<SimpleResponse<Boolean>> headHubUser(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @PathParam("user") String user, Context context);

        @Put("/ws/api/v1/hubs/{hub}/groups/{group}/users/{user}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> addHubUser(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @PathParam("user") String user, @QueryParam("ttl") Integer ttl, Context context);

        @Delete("/ws/api/v1/hubs/{hub}/groups/{group}/users/{user}")
        @ExpectedResponses({202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> removeHubUser(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("group") String group, @PathParam("user") String user, Context context);

        @Delete("/ws/api/v1/hubs/{hub}/users/{user}/groups")
        @ExpectedResponses({200, 202, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> removeUserFromHub(@HostParam("$host") String host, @PathParam("hub") String hub, @PathParam("user") String user, Context context);
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponseAsync(Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToAll(this.client.getHost(), data, contentLength, context));
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponseAsync(Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToAll(this.client.getHost(), data, contentLength, context);
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAllAsync(Flux<ByteBuffer> data, long contentLength) {
        return sendToAllWithResponseAsync(data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(Flux<ByteBuffer> data, long contentLength) {
        sendToAllAsync(data, contentLength).block();
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponseAsync(String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToAll(this.client.getHost(), data, context));
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponseAsync(String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToAll(this.client.getHost(), data, context);
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAllAsync(String data) {
        return sendToAllWithResponseAsync(data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Broadcast content inside request body to all the connected to endpoint "/ws/client".
     * 
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(String data) {
        sendToAllAsync(data).block();
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionsWithResponseAsync(String hub, Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubConnections(this.client.getHost(), hub, data, contentLength, context));
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionsWithResponseAsync(String hub, Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubConnections(this.client.getHost(), hub, data, contentLength, context);
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubConnectionsAsync(String hub, Flux<ByteBuffer> data, long contentLength) {
        return sendToHubConnectionsWithResponseAsync(hub, data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubConnections(String hub, Flux<ByteBuffer> data, long contentLength) {
        sendToHubConnectionsAsync(hub, data, contentLength).block();
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionsWithResponseAsync(String hub, String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubConnections(this.client.getHost(), hub, data, context));
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionsWithResponseAsync(String hub, String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubConnections(this.client.getHost(), hub, data, context);
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubConnectionsAsync(String hub, String data) {
        return sendToHubConnectionsWithResponseAsync(hub, data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Broadcast content inside request body to all the connected connections in the same hub.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubConnections(String hub, String data) {
        sendToHubConnectionsAsync(hub, data).block();
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUserWithResponseAsync(String id, Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToUser(this.client.getHost(), id, data, contentLength, context));
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUserWithResponseAsync(String id, Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToUser(this.client.getHost(), id, data, contentLength, context);
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToUserAsync(String id, Flux<ByteBuffer> data, long contentLength) {
        return sendToUserWithResponseAsync(id, data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToUser(String id, Flux<ByteBuffer> data, long contentLength) {
        sendToUserAsync(id, data, contentLength).block();
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUserWithResponseAsync(String id, String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToUser(this.client.getHost(), id, data, context));
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUserWithResponseAsync(String id, String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToUser(this.client.getHost(), id, data, context);
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToUserAsync(String id, String data) {
        return sendToUserWithResponseAsync(id, data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param id The user Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToUser(String id, String data) {
        sendToUserAsync(id, data).block();
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubUserWithResponseAsync(String hub, String id, Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubUser(this.client.getHost(), hub, id, data, contentLength, context));
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubUserWithResponseAsync(String hub, String id, Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubUser(this.client.getHost(), hub, id, data, contentLength, context);
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubUserAsync(String hub, String id, Flux<ByteBuffer> data, long contentLength) {
        return sendToHubUserWithResponseAsync(hub, id, data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubUser(String hub, String id, Flux<ByteBuffer> data, long contentLength) {
        sendToHubUserAsync(hub, id, data, contentLength).block();
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubUserWithResponseAsync(String hub, String id, String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubUser(this.client.getHost(), hub, id, data, context));
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubUserWithResponseAsync(String hub, String id, String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (id == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubUser(this.client.getHost(), hub, id, data, context);
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubUserAsync(String hub, String id, String data) {
        return sendToHubUserWithResponseAsync(hub, id, data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific user.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param id The user Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubUser(String hub, String id, String data) {
        sendToHubUserAsync(hub, id, data).block();
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnectionWithResponseAsync(String connectionId, Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToConnection(this.client.getHost(), connectionId, data, contentLength, context));
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnectionWithResponseAsync(String connectionId, Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToConnection(this.client.getHost(), connectionId, data, contentLength, context);
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToConnectionAsync(String connectionId, Flux<ByteBuffer> data, long contentLength) {
        return sendToConnectionWithResponseAsync(connectionId, data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToConnection(String connectionId, Flux<ByteBuffer> data, long contentLength) {
        sendToConnectionAsync(connectionId, data, contentLength).block();
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnectionWithResponseAsync(String connectionId, String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToConnection(this.client.getHost(), connectionId, data, context));
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnectionWithResponseAsync(String connectionId, String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToConnection(this.client.getHost(), connectionId, data, context);
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToConnectionAsync(String connectionId, String data) {
        return sendToConnectionWithResponseAsync(connectionId, data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param connectionId The connection Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToConnection(String connectionId, String data) {
        sendToConnectionAsync(connectionId, data).block();
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getConnectionWithResponseAsync(String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getConnection(this.client.getHost(), connectionId, context));
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getConnectionWithResponseAsync(String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.getConnection(this.client.getHost(), connectionId, context);
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> getConnectionAsync(String connectionId) {
        return getConnectionWithResponseAsync(connectionId)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void getConnection(String connectionId) {
        getConnectionAsync(connectionId).block();
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headConnectionWithResponseAsync(String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.headConnection(this.client.getHost(), connectionId, context));
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headConnectionWithResponseAsync(String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.headConnection(this.client.getHost(), connectionId, context);
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> headConnectionAsync(String connectionId) {
        return headConnectionWithResponseAsync(connectionId)
            .flatMap((SimpleResponse<Boolean> res) -> {
                if (res.getValue() != null) {
                    return Mono.just(res.getValue());
                } else {
                    return Mono.empty();
                }
            });
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean headConnection(String connectionId) {
        return headConnectionAsync(connectionId).block();
    }

    /**
     * Close the client connection.
     * 
     * @param connectionId 
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnectionWithResponseAsync(String connectionId, String reason) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.closeConnection(this.client.getHost(), connectionId, reason, context));
    }

    /**
     * Close the client connection.
     * 
     * @param connectionId 
     * @param context The context to associate with this operation.
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnectionWithResponseAsync(String connectionId, Context context, String reason) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.closeConnection(this.client.getHost(), connectionId, reason, context);
    }

    /**
     * Close the client connection.
     * 
     * @param connectionId 
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> closeConnectionAsync(String connectionId, String reason) {
        return closeConnectionWithResponseAsync(connectionId, reason)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Close the client connection.
     * 
     * @param connectionId 
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void closeConnection(String connectionId, String reason) {
        closeConnectionAsync(connectionId, reason).block();
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionWithResponseAsync(String hub, String connectionId, Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubConnection(this.client.getHost(), hub, connectionId, data, contentLength, context));
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionWithResponseAsync(String hub, String connectionId, Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubConnection(this.client.getHost(), hub, connectionId, data, contentLength, context);
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubConnectionAsync(String hub, String connectionId, Flux<ByteBuffer> data, long contentLength) {
        return sendToHubConnectionWithResponseAsync(hub, connectionId, data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubConnection(String hub, String connectionId, Flux<ByteBuffer> data, long contentLength) {
        sendToHubConnectionAsync(hub, connectionId, data, contentLength).block();
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionWithResponseAsync(String hub, String connectionId, String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubConnection(this.client.getHost(), hub, connectionId, data, context));
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubConnectionWithResponseAsync(String hub, String connectionId, String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubConnection(this.client.getHost(), hub, connectionId, data, context);
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubConnectionAsync(String hub, String connectionId, String data) {
        return sendToHubConnectionWithResponseAsync(hub, connectionId, data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to the specific connection.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param connectionId The connection Id.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubConnection(String hub, String connectionId, String data) {
        sendToHubConnectionAsync(hub, connectionId, data).block();
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getHubConnectionWithResponseAsync(String hub, String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getHubConnection(this.client.getHost(), hub, connectionId, context));
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getHubConnectionWithResponseAsync(String hub, String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.getHubConnection(this.client.getHost(), hub, connectionId, context);
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> getHubConnectionAsync(String hub, String connectionId) {
        return getHubConnectionWithResponseAsync(hub, connectionId)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void getHubConnection(String hub, String connectionId) {
        getHubConnectionAsync(hub, connectionId).block();
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headHubConnectionWithResponseAsync(String hub, String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.headHubConnection(this.client.getHost(), hub, connectionId, context));
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headHubConnectionWithResponseAsync(String hub, String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.headHubConnection(this.client.getHost(), hub, connectionId, context);
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> headHubConnectionAsync(String hub, String connectionId) {
        return headHubConnectionWithResponseAsync(hub, connectionId)
            .flatMap((SimpleResponse<Boolean> res) -> {
                if (res.getValue() != null) {
                    return Mono.just(res.getValue());
                } else {
                    return Mono.empty();
                }
            });
    }

    /**
     * Check if the connection with the given connectionId exists.
     * 
     * @param hub 
     * @param connectionId 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean headHubConnection(String hub, String connectionId) {
        return headHubConnectionAsync(hub, connectionId).block();
    }

    /**
     * Close the client connection.
     * 
     * @param hub 
     * @param connectionId 
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeHubConnectionWithResponseAsync(String hub, String connectionId, String reason) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.closeHubConnection(this.client.getHost(), hub, connectionId, reason, context));
    }

    /**
     * Close the client connection.
     * 
     * @param hub 
     * @param connectionId 
     * @param context The context to associate with this operation.
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeHubConnectionWithResponseAsync(String hub, String connectionId, Context context, String reason) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.closeHubConnection(this.client.getHost(), hub, connectionId, reason, context);
    }

    /**
     * Close the client connection.
     * 
     * @param hub 
     * @param connectionId 
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> closeHubConnectionAsync(String hub, String connectionId, String reason) {
        return closeHubConnectionWithResponseAsync(hub, connectionId, reason)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Close the client connection.
     * 
     * @param hub 
     * @param connectionId 
     * @param reason 
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void closeHubConnection(String hub, String connectionId, String reason) {
        closeHubConnectionAsync(hub, connectionId, reason).block();
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToGroupWithResponseAsync(String group, Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToGroup(this.client.getHost(), group, data, contentLength, context));
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToGroupWithResponseAsync(String group, Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToGroup(this.client.getHost(), group, data, contentLength, context);
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToGroupAsync(String group, Flux<ByteBuffer> data, long contentLength) {
        return sendToGroupWithResponseAsync(group, data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToGroup(String group, Flux<ByteBuffer> data, long contentLength) {
        sendToGroupAsync(group, data, contentLength).block();
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToGroupWithResponseAsync(String group, String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToGroup(this.client.getHost(), group, data, context));
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToGroupWithResponseAsync(String group, String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToGroup(this.client.getHost(), group, data, context);
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToGroupAsync(String group, String data) {
        return sendToGroupWithResponseAsync(group, data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToGroup(String group, String data) {
        sendToGroupAsync(group, data).block();
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubGroupWithResponseAsync(String hub, String group, Flux<ByteBuffer> data, long contentLength) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubGroup(this.client.getHost(), hub, group, data, contentLength, context));
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubGroupWithResponseAsync(String hub, String group, Flux<ByteBuffer> data, long contentLength, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubGroup(this.client.getHost(), hub, group, data, contentLength, context);
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubGroupAsync(String hub, String group, Flux<ByteBuffer> data, long contentLength) {
        return sendToHubGroupWithResponseAsync(hub, group, data, contentLength)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data 
     * @param contentLength null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubGroup(String hub, String group, Flux<ByteBuffer> data, long contentLength) {
        sendToHubGroupAsync(hub, group, data, contentLength).block();
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubGroupWithResponseAsync(String hub, String group, String data) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.sendToHubGroup(this.client.getHost(), hub, group, data, context));
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToHubGroupWithResponseAsync(String hub, String group, String data, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (data == null) {
            return Mono.error(new IllegalArgumentException("Parameter data is required and cannot be null."));
        }
        return service.sendToHubGroup(this.client.getHost(), hub, group, data, context);
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToHubGroupAsync(String hub, String group, String data) {
        return sendToHubGroupWithResponseAsync(hub, group, data)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Send content inside request body to a group of connections.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param data simple string.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToHubGroup(String hub, String group, String data) {
        sendToHubGroupAsync(hub, group, data).block();
    }

    /**
     * Add a connection to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addConnectionWithResponseAsync(String group, String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.addConnection(this.client.getHost(), group, connectionId, context));
    }

    /**
     * Add a connection to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addConnectionWithResponseAsync(String group, String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.addConnection(this.client.getHost(), group, connectionId, context);
    }

    /**
     * Add a connection to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addConnectionAsync(String group, String connectionId) {
        return addConnectionWithResponseAsync(group, connectionId)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Add a connection to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addConnection(String group, String connectionId) {
        addConnectionAsync(group, connectionId).block();
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeConnectionWithResponseAsync(String group, String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.removeConnection(this.client.getHost(), group, connectionId, context));
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeConnectionWithResponseAsync(String group, String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.removeConnection(this.client.getHost(), group, connectionId, context);
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeConnectionAsync(String group, String connectionId) {
        return removeConnectionWithResponseAsync(group, connectionId)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeConnection(String group, String connectionId) {
        removeConnectionAsync(group, connectionId).block();
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getUserWithResponseAsync(String group, String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getUser(this.client.getHost(), group, user, context));
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getUserWithResponseAsync(String group, String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.getUser(this.client.getHost(), group, user, context);
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> getUserAsync(String group, String user) {
        return getUserWithResponseAsync(group, user)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void getUser(String group, String user) {
        getUserAsync(group, user).block();
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headUserWithResponseAsync(String group, String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.headUser(this.client.getHost(), group, user, context));
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headUserWithResponseAsync(String group, String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.headUser(this.client.getHost(), group, user, context);
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> headUserAsync(String group, String user) {
        return headUserWithResponseAsync(group, user)
            .flatMap((SimpleResponse<Boolean> res) -> {
                if (res.getValue() != null) {
                    return Mono.just(res.getValue());
                } else {
                    return Mono.empty();
                }
            });
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean headUser(String group, String user) {
        return headUserAsync(group, user).block();
    }

    /**
     * Add a user to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addUserWithResponseAsync(String group, String user, Integer ttl) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.addUser(this.client.getHost(), group, user, ttl, context));
    }

    /**
     * Add a user to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addUserWithResponseAsync(String group, String user, Context context, Integer ttl) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.addUser(this.client.getHost(), group, user, ttl, context);
    }

    /**
     * Add a user to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addUserAsync(String group, String user, Integer ttl) {
        return addUserWithResponseAsync(group, user, ttl)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Add a user to the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addUser(String group, String user, Integer ttl) {
        addUserAsync(group, user, ttl).block();
    }

    /**
     * Remove a user from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserFromGroupWithResponseAsync(String group, String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.removeUserFromGroup(this.client.getHost(), group, user, context));
    }

    /**
     * Remove a user from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserFromGroupWithResponseAsync(String group, String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.removeUserFromGroup(this.client.getHost(), group, user, context);
    }

    /**
     * Remove a user from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeUserFromGroupAsync(String group, String user) {
        return removeUserFromGroupWithResponseAsync(group, user)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Remove a user from the target group.
     * 
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeUserFromGroup(String group, String user) {
        removeUserFromGroupAsync(group, user).block();
    }

    /**
     * Remove a user from all groups.
     * 
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserWithResponseAsync(String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.removeUser(this.client.getHost(), user, context));
    }

    /**
     * Remove a user from all groups.
     * 
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserWithResponseAsync(String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.removeUser(this.client.getHost(), user, context);
    }

    /**
     * Remove a user from all groups.
     * 
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeUserAsync(String user) {
        return removeUserWithResponseAsync(user)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Remove a user from all groups.
     * 
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeUser(String user) {
        removeUserAsync(user).block();
    }

    /**
     * Add a connection to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addHubConnectionWithResponseAsync(String hub, String group, String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.addHubConnection(this.client.getHost(), hub, group, connectionId, context));
    }

    /**
     * Add a connection to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addHubConnectionWithResponseAsync(String hub, String group, String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.addHubConnection(this.client.getHost(), hub, group, connectionId, context);
    }

    /**
     * Add a connection to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addHubConnectionAsync(String hub, String group, String connectionId) {
        return addHubConnectionWithResponseAsync(hub, group, connectionId)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Add a connection to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addHubConnection(String hub, String group, String connectionId) {
        addHubConnectionAsync(hub, group, connectionId).block();
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeHubConnectionWithResponseAsync(String hub, String group, String connectionId) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.removeHubConnection(this.client.getHost(), hub, group, connectionId, context));
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeHubConnectionWithResponseAsync(String hub, String group, String connectionId, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (connectionId == null) {
            return Mono.error(new IllegalArgumentException("Parameter connectionId is required and cannot be null."));
        }
        return service.removeHubConnection(this.client.getHost(), hub, group, connectionId, context);
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeHubConnectionAsync(String hub, String group, String connectionId) {
        return removeHubConnectionWithResponseAsync(hub, group, connectionId)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Remove a connection from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param connectionId Target connection Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeHubConnection(String hub, String group, String connectionId) {
        removeHubConnectionAsync(hub, group, connectionId).block();
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getHubUserWithResponseAsync(String hub, String group, String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getHubUser(this.client.getHost(), hub, group, user, context));
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> getHubUserWithResponseAsync(String hub, String group, String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.getHubUser(this.client.getHost(), hub, group, user, context);
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> getHubUserAsync(String hub, String group, String user) {
        return getHubUserWithResponseAsync(hub, group, user)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void getHubUser(String hub, String group, String user) {
        getHubUserAsync(hub, group, user).block();
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headHubUserWithResponseAsync(String hub, String group, String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.headHubUser(this.client.getHost(), hub, group, user, context));
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> headHubUserWithResponseAsync(String hub, String group, String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.headHubUser(this.client.getHost(), hub, group, user, context);
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> headHubUserAsync(String hub, String group, String user) {
        return headHubUserWithResponseAsync(hub, group, user)
            .flatMap((SimpleResponse<Boolean> res) -> {
                if (res.getValue() != null) {
                    return Mono.just(res.getValue());
                } else {
                    return Mono.empty();
                }
            });
    }

    /**
     * Check whether a user exists in the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean headHubUser(String hub, String group, String user) {
        return headHubUserAsync(hub, group, user).block();
    }

    /**
     * Add a user to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addHubUserWithResponseAsync(String hub, String group, String user, Integer ttl) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.addHubUser(this.client.getHost(), hub, group, user, ttl, context));
    }

    /**
     * Add a user to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addHubUserWithResponseAsync(String hub, String group, String user, Context context, Integer ttl) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.addHubUser(this.client.getHost(), hub, group, user, ttl, context);
    }

    /**
     * Add a user to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addHubUserAsync(String hub, String group, String user, Integer ttl) {
        return addHubUserWithResponseAsync(hub, group, user, ttl)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Add a user to the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param ttl Specifies the seconds that the user exists in the group. If not set, the user lives in the group forever.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void addHubUser(String hub, String group, String user, Integer ttl) {
        addHubUserAsync(hub, group, user, ttl).block();
    }

    /**
     * Remove a user from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeHubUserWithResponseAsync(String hub, String group, String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.removeHubUser(this.client.getHost(), hub, group, user, context));
    }

    /**
     * Remove a user from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeHubUserWithResponseAsync(String hub, String group, String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (group == null) {
            return Mono.error(new IllegalArgumentException("Parameter group is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.removeHubUser(this.client.getHost(), hub, group, user, context);
    }

    /**
     * Remove a user from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeHubUserAsync(String hub, String group, String user) {
        return removeHubUserWithResponseAsync(hub, group, user)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Remove a user from the target group.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param group Target group name, which length should be greater than 0 and less than 1025.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeHubUser(String hub, String group, String user) {
        removeHubUserAsync(hub, group, user).block();
    }

    /**
     * Remove a user from all groups.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserFromHubWithResponseAsync(String hub, String user) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.removeUserFromHub(this.client.getHost(), hub, user, context));
    }

    /**
     * Remove a user from all groups.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param user Target user Id.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserFromHubWithResponseAsync(String hub, String user, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (hub == null) {
            return Mono.error(new IllegalArgumentException("Parameter hub is required and cannot be null."));
        }
        if (user == null) {
            return Mono.error(new IllegalArgumentException("Parameter user is required and cannot be null."));
        }
        return service.removeUserFromHub(this.client.getHost(), hub, user, context);
    }

    /**
     * Remove a user from all groups.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeUserFromHubAsync(String hub, String user) {
        return removeUserFromHubWithResponseAsync(hub, user)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Remove a user from all groups.
     * 
     * @param hub Target hub name, which should start with alphabetic characters and only contain alpha-numeric characters or underscore.
     * @param user Target user Id.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void removeUserFromHub(String hub, String user) {
        removeUserFromHubAsync(hub, user).block();
    }
}
