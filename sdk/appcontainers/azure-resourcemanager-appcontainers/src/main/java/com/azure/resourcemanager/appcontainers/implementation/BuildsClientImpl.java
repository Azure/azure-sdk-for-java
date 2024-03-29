// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appcontainers.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.polling.PollResult;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.appcontainers.fluent.BuildsClient;
import com.azure.resourcemanager.appcontainers.fluent.models.BuildResourceInner;
import java.nio.ByteBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to all the operations defined in BuildsClient.
 */
public final class BuildsClientImpl implements BuildsClient {
    /**
     * The proxy service used to perform REST calls.
     */
    private final BuildsService service;

    /**
     * The service client containing this operation class.
     */
    private final ContainerAppsApiClientImpl client;

    /**
     * Initializes an instance of BuildsClientImpl.
     * 
     * @param client the instance of the service client containing this operation class.
     */
    BuildsClientImpl(ContainerAppsApiClientImpl client) {
        this.service = RestProxy.create(BuildsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for ContainerAppsApiClientBuilds to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "ContainerAppsApiClie")
    public interface BuildsService {
        @Headers({ "Content-Type: application/json" })
        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.App/builders/{builderName}/builds/{buildName}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<BuildResourceInner>> get(@HostParam("$host") String endpoint,
            @QueryParam("api-version") String apiVersion, @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName, @PathParam("builderName") String builderName,
            @PathParam("buildName") String buildName, @HeaderParam("Accept") String accept, Context context);

        @Headers({ "Content-Type: application/json" })
        @Put("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.App/builders/{builderName}/builds/{buildName}")
        @ExpectedResponses({ 200, 201 })
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<Flux<ByteBuffer>>> createOrUpdate(@HostParam("$host") String endpoint,
            @QueryParam("api-version") String apiVersion, @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName, @PathParam("builderName") String builderName,
            @PathParam("buildName") String buildName, @BodyParam("application/json") BuildResourceInner buildEnvelope,
            @HeaderParam("Accept") String accept, Context context);

        @Headers({ "Content-Type: application/json" })
        @Delete("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.App/builders/{builderName}/builds/{buildName}")
        @ExpectedResponses({ 202, 204 })
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<Flux<ByteBuffer>>> delete(@HostParam("$host") String endpoint,
            @QueryParam("api-version") String apiVersion, @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName, @PathParam("builderName") String builderName,
            @PathParam("buildName") String buildName, @HeaderParam("Accept") String accept, Context context);
    }

    /**
     * Get a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a BuildResource along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<BuildResourceInner>> getWithResponseAsync(String resourceGroupName, String builderName,
        String buildName) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono.error(new IllegalArgumentException(
                "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (builderName == null) {
            return Mono.error(new IllegalArgumentException("Parameter builderName is required and cannot be null."));
        }
        if (buildName == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildName is required and cannot be null."));
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.get(this.client.getEndpoint(), this.client.getApiVersion(),
                this.client.getSubscriptionId(), resourceGroupName, builderName, buildName, accept, context))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Get a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a BuildResource along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<BuildResourceInner>> getWithResponseAsync(String resourceGroupName, String builderName,
        String buildName, Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono.error(new IllegalArgumentException(
                "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (builderName == null) {
            return Mono.error(new IllegalArgumentException("Parameter builderName is required and cannot be null."));
        }
        if (buildName == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildName is required and cannot be null."));
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service.get(this.client.getEndpoint(), this.client.getApiVersion(), this.client.getSubscriptionId(),
            resourceGroupName, builderName, buildName, accept, context);
    }

    /**
     * Get a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a BuildResource on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<BuildResourceInner> getAsync(String resourceGroupName, String builderName, String buildName) {
        return getWithResponseAsync(resourceGroupName, builderName, buildName)
            .flatMap(res -> Mono.justOrEmpty(res.getValue()));
    }

    /**
     * Get a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a BuildResource along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BuildResourceInner> getWithResponse(String resourceGroupName, String builderName, String buildName,
        Context context) {
        return getWithResponseAsync(resourceGroupName, builderName, buildName, context).block();
    }

    /**
     * Get a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a BuildResource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BuildResourceInner get(String resourceGroupName, String builderName, String buildName) {
        return getWithResponse(resourceGroupName, builderName, buildName, Context.NONE).getValue();
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information pertaining to an individual build along with {@link Response} on successful completion of
     * {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<Flux<ByteBuffer>>> createOrUpdateWithResponseAsync(String resourceGroupName,
        String builderName, String buildName, BuildResourceInner buildEnvelope) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono.error(new IllegalArgumentException(
                "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (builderName == null) {
            return Mono.error(new IllegalArgumentException("Parameter builderName is required and cannot be null."));
        }
        if (buildName == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildName is required and cannot be null."));
        }
        if (buildEnvelope == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildEnvelope is required and cannot be null."));
        } else {
            buildEnvelope.validate();
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.createOrUpdate(this.client.getEndpoint(), this.client.getApiVersion(),
                this.client.getSubscriptionId(), resourceGroupName, builderName, buildName, buildEnvelope, accept,
                context))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information pertaining to an individual build along with {@link Response} on successful completion of
     * {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<Flux<ByteBuffer>>> createOrUpdateWithResponseAsync(String resourceGroupName,
        String builderName, String buildName, BuildResourceInner buildEnvelope, Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono.error(new IllegalArgumentException(
                "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (builderName == null) {
            return Mono.error(new IllegalArgumentException("Parameter builderName is required and cannot be null."));
        }
        if (buildName == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildName is required and cannot be null."));
        }
        if (buildEnvelope == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildEnvelope is required and cannot be null."));
        } else {
            buildEnvelope.validate();
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service.createOrUpdate(this.client.getEndpoint(), this.client.getApiVersion(),
            this.client.getSubscriptionId(), resourceGroupName, builderName, buildName, buildEnvelope, accept, context);
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link PollerFlux} for polling of information pertaining to an individual build.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    private PollerFlux<PollResult<BuildResourceInner>, BuildResourceInner> beginCreateOrUpdateAsync(
        String resourceGroupName, String builderName, String buildName, BuildResourceInner buildEnvelope) {
        Mono<Response<Flux<ByteBuffer>>> mono
            = createOrUpdateWithResponseAsync(resourceGroupName, builderName, buildName, buildEnvelope);
        return this.client.<BuildResourceInner, BuildResourceInner>getLroResult(mono, this.client.getHttpPipeline(),
            BuildResourceInner.class, BuildResourceInner.class, this.client.getContext());
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link PollerFlux} for polling of information pertaining to an individual build.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    private PollerFlux<PollResult<BuildResourceInner>, BuildResourceInner> beginCreateOrUpdateAsync(
        String resourceGroupName, String builderName, String buildName, BuildResourceInner buildEnvelope,
        Context context) {
        context = this.client.mergeContext(context);
        Mono<Response<Flux<ByteBuffer>>> mono
            = createOrUpdateWithResponseAsync(resourceGroupName, builderName, buildName, buildEnvelope, context);
        return this.client.<BuildResourceInner, BuildResourceInner>getLroResult(mono, this.client.getHttpPipeline(),
            BuildResourceInner.class, BuildResourceInner.class, context);
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of information pertaining to an individual build.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<PollResult<BuildResourceInner>, BuildResourceInner> beginCreateOrUpdate(String resourceGroupName,
        String builderName, String buildName, BuildResourceInner buildEnvelope) {
        return this.beginCreateOrUpdateAsync(resourceGroupName, builderName, buildName, buildEnvelope).getSyncPoller();
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of information pertaining to an individual build.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<PollResult<BuildResourceInner>, BuildResourceInner> beginCreateOrUpdate(String resourceGroupName,
        String builderName, String buildName, BuildResourceInner buildEnvelope, Context context) {
        return this.beginCreateOrUpdateAsync(resourceGroupName, builderName, buildName, buildEnvelope, context)
            .getSyncPoller();
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information pertaining to an individual build on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<BuildResourceInner> createOrUpdateAsync(String resourceGroupName, String builderName, String buildName,
        BuildResourceInner buildEnvelope) {
        return beginCreateOrUpdateAsync(resourceGroupName, builderName, buildName, buildEnvelope).last()
            .flatMap(this.client::getLroFinalResultOrError);
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information pertaining to an individual build on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<BuildResourceInner> createOrUpdateAsync(String resourceGroupName, String builderName, String buildName,
        BuildResourceInner buildEnvelope, Context context) {
        return beginCreateOrUpdateAsync(resourceGroupName, builderName, buildName, buildEnvelope, context).last()
            .flatMap(this.client::getLroFinalResultOrError);
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information pertaining to an individual build.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BuildResourceInner createOrUpdate(String resourceGroupName, String builderName, String buildName,
        BuildResourceInner buildEnvelope) {
        return createOrUpdateAsync(resourceGroupName, builderName, buildName, buildEnvelope).block();
    }

    /**
     * Create a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param buildEnvelope Resource create or update parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return information pertaining to an individual build.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BuildResourceInner createOrUpdate(String resourceGroupName, String builderName, String buildName,
        BuildResourceInner buildEnvelope, Context context) {
        return createOrUpdateAsync(resourceGroupName, builderName, buildName, buildEnvelope, context).block();
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<Flux<ByteBuffer>>> deleteWithResponseAsync(String resourceGroupName, String builderName,
        String buildName) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono.error(new IllegalArgumentException(
                "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (builderName == null) {
            return Mono.error(new IllegalArgumentException("Parameter builderName is required and cannot be null."));
        }
        if (buildName == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildName is required and cannot be null."));
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.delete(this.client.getEndpoint(), this.client.getApiVersion(),
                this.client.getSubscriptionId(), resourceGroupName, builderName, buildName, accept, context))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<Flux<ByteBuffer>>> deleteWithResponseAsync(String resourceGroupName, String builderName,
        String buildName, Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono.error(new IllegalArgumentException(
                "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (builderName == null) {
            return Mono.error(new IllegalArgumentException("Parameter builderName is required and cannot be null."));
        }
        if (buildName == null) {
            return Mono.error(new IllegalArgumentException("Parameter buildName is required and cannot be null."));
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service.delete(this.client.getEndpoint(), this.client.getApiVersion(), this.client.getSubscriptionId(),
            resourceGroupName, builderName, buildName, accept, context);
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link PollerFlux} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    private PollerFlux<PollResult<Void>, Void> beginDeleteAsync(String resourceGroupName, String builderName,
        String buildName) {
        Mono<Response<Flux<ByteBuffer>>> mono = deleteWithResponseAsync(resourceGroupName, builderName, buildName);
        return this.client.<Void, Void>getLroResult(mono, this.client.getHttpPipeline(), Void.class, Void.class,
            this.client.getContext());
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link PollerFlux} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    private PollerFlux<PollResult<Void>, Void> beginDeleteAsync(String resourceGroupName, String builderName,
        String buildName, Context context) {
        context = this.client.mergeContext(context);
        Mono<Response<Flux<ByteBuffer>>> mono
            = deleteWithResponseAsync(resourceGroupName, builderName, buildName, context);
        return this.client.<Void, Void>getLroResult(mono, this.client.getHttpPipeline(), Void.class, Void.class,
            context);
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<PollResult<Void>, Void> beginDelete(String resourceGroupName, String builderName,
        String buildName) {
        return this.beginDeleteAsync(resourceGroupName, builderName, buildName).getSyncPoller();
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the {@link SyncPoller} for polling of long-running operation.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<PollResult<Void>, Void> beginDelete(String resourceGroupName, String builderName,
        String buildName, Context context) {
        return this.beginDeleteAsync(resourceGroupName, builderName, buildName, context).getSyncPoller();
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A {@link Mono} that completes when a successful response is received.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Void> deleteAsync(String resourceGroupName, String builderName, String buildName) {
        return beginDeleteAsync(resourceGroupName, builderName, buildName).last()
            .flatMap(this.client::getLroFinalResultOrError);
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A {@link Mono} that completes when a successful response is received.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Void> deleteAsync(String resourceGroupName, String builderName, String buildName, Context context) {
        return beginDeleteAsync(resourceGroupName, builderName, buildName, context).last()
            .flatMap(this.client::getLroFinalResultOrError);
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String resourceGroupName, String builderName, String buildName) {
        deleteAsync(resourceGroupName, builderName, buildName).block();
    }

    /**
     * Delete a BuildResource.
     * 
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param builderName The name of the builder.
     * @param buildName The name of a build.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String resourceGroupName, String builderName, String buildName, Context context) {
        deleteAsync(resourceGroupName, builderName, buildName, context).block();
    }
}
