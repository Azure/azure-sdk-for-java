// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.deploymentmanager.implementation;

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
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.deploymentmanager.fluent.ServicesClient;
import com.azure.resourcemanager.deploymentmanager.fluent.models.ServiceResourceInner;
import java.util.List;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in ServicesClient. */
public final class ServicesClientImpl implements ServicesClient {
    private final ClientLogger logger = new ClientLogger(ServicesClientImpl.class);

    /** The proxy service used to perform REST calls. */
    private final ServicesService service;

    /** The service client containing this operation class. */
    private final AzureDeploymentManagerImpl client;

    /**
     * Initializes an instance of ServicesClientImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    ServicesClientImpl(AzureDeploymentManagerImpl client) {
        this.service = RestProxy.create(ServicesService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for AzureDeploymentManagerServices to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "AzureDeploymentManag")
    private interface ServicesService {
        @Headers({"Content-Type: application/json"})
        @Put(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DeploymentManager"
                + "/serviceTopologies/{serviceTopologyName}/services/{serviceName}")
        @ExpectedResponses({201})
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<ServiceResourceInner>> createOrUpdate(
            @HostParam("$host") String endpoint,
            @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("serviceTopologyName") String serviceTopologyName,
            @PathParam("serviceName") String serviceName,
            @QueryParam("api-version") String apiVersion,
            @BodyParam("application/json") ServiceResourceInner serviceInfo,
            @HeaderParam("Accept") String accept,
            Context context);

        @Headers({"Content-Type: application/json"})
        @Get(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DeploymentManager"
                + "/serviceTopologies/{serviceTopologyName}/services/{serviceName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<ServiceResourceInner>> get(
            @HostParam("$host") String endpoint,
            @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("serviceTopologyName") String serviceTopologyName,
            @PathParam("serviceName") String serviceName,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("Accept") String accept,
            Context context);

        @Headers({"Content-Type: application/json"})
        @Delete(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DeploymentManager"
                + "/serviceTopologies/{serviceTopologyName}/services/{serviceName}")
        @ExpectedResponses({200, 204})
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<Void>> delete(
            @HostParam("$host") String endpoint,
            @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("serviceTopologyName") String serviceTopologyName,
            @PathParam("serviceName") String serviceName,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("Accept") String accept,
            Context context);

        @Headers({"Content-Type: application/json"})
        @Get(
            "/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DeploymentManager"
                + "/serviceTopologies/{serviceTopologyName}/services")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<List<ServiceResourceInner>>> list(
            @HostParam("$host") String endpoint,
            @PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("serviceTopologyName") String serviceTopologyName,
            @QueryParam("api-version") String apiVersion,
            @HeaderParam("Accept") String accept,
            Context context);
    }

    /**
     * Synchronously creates a new service or updates an existing service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param serviceInfo The service object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the resource representation of a service in a service topology.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<ServiceResourceInner>> createOrUpdateWithResponseAsync(
        String resourceGroupName, String serviceTopologyName, String serviceName, ServiceResourceInner serviceInfo) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        if (serviceName == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceName is required and cannot be null."));
        }
        if (serviceInfo == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceInfo is required and cannot be null."));
        } else {
            serviceInfo.validate();
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .createOrUpdate(
                            this.client.getEndpoint(),
                            this.client.getSubscriptionId(),
                            resourceGroupName,
                            serviceTopologyName,
                            serviceName,
                            this.client.getApiVersion(),
                            serviceInfo,
                            accept,
                            context))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Synchronously creates a new service or updates an existing service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param serviceInfo The service object.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the resource representation of a service in a service topology.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<ServiceResourceInner>> createOrUpdateWithResponseAsync(
        String resourceGroupName,
        String serviceTopologyName,
        String serviceName,
        ServiceResourceInner serviceInfo,
        Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        if (serviceName == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceName is required and cannot be null."));
        }
        if (serviceInfo == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceInfo is required and cannot be null."));
        } else {
            serviceInfo.validate();
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service
            .createOrUpdate(
                this.client.getEndpoint(),
                this.client.getSubscriptionId(),
                resourceGroupName,
                serviceTopologyName,
                serviceName,
                this.client.getApiVersion(),
                serviceInfo,
                accept,
                context);
    }

    /**
     * Synchronously creates a new service or updates an existing service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param serviceInfo The service object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the resource representation of a service in a service topology.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<ServiceResourceInner> createOrUpdateAsync(
        String resourceGroupName, String serviceTopologyName, String serviceName, ServiceResourceInner serviceInfo) {
        return createOrUpdateWithResponseAsync(resourceGroupName, serviceTopologyName, serviceName, serviceInfo)
            .flatMap(
                (Response<ServiceResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Synchronously creates a new service or updates an existing service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param serviceInfo The service object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the resource representation of a service in a service topology.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceResourceInner createOrUpdate(
        String resourceGroupName, String serviceTopologyName, String serviceName, ServiceResourceInner serviceInfo) {
        return createOrUpdateAsync(resourceGroupName, serviceTopologyName, serviceName, serviceInfo).block();
    }

    /**
     * Synchronously creates a new service or updates an existing service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param serviceInfo The service object.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the resource representation of a service in a service topology.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ServiceResourceInner> createOrUpdateWithResponse(
        String resourceGroupName,
        String serviceTopologyName,
        String serviceName,
        ServiceResourceInner serviceInfo,
        Context context) {
        return createOrUpdateWithResponseAsync(
                resourceGroupName, serviceTopologyName, serviceName, serviceInfo, context)
            .block();
    }

    /**
     * Gets the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<ServiceResourceInner>> getWithResponseAsync(
        String resourceGroupName, String serviceTopologyName, String serviceName) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        if (serviceName == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceName is required and cannot be null."));
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .get(
                            this.client.getEndpoint(),
                            this.client.getSubscriptionId(),
                            resourceGroupName,
                            serviceTopologyName,
                            serviceName,
                            this.client.getApiVersion(),
                            accept,
                            context))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Gets the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<ServiceResourceInner>> getWithResponseAsync(
        String resourceGroupName, String serviceTopologyName, String serviceName, Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        if (serviceName == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceName is required and cannot be null."));
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service
            .get(
                this.client.getEndpoint(),
                this.client.getSubscriptionId(),
                resourceGroupName,
                serviceTopologyName,
                serviceName,
                this.client.getApiVersion(),
                accept,
                context);
    }

    /**
     * Gets the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<ServiceResourceInner> getAsync(
        String resourceGroupName, String serviceTopologyName, String serviceName) {
        return getWithResponseAsync(resourceGroupName, serviceTopologyName, serviceName)
            .flatMap(
                (Response<ServiceResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Gets the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ServiceResourceInner get(String resourceGroupName, String serviceTopologyName, String serviceName) {
        return getAsync(resourceGroupName, serviceTopologyName, serviceName).block();
    }

    /**
     * Gets the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ServiceResourceInner> getWithResponse(
        String resourceGroupName, String serviceTopologyName, String serviceName, Context context) {
        return getWithResponseAsync(resourceGroupName, serviceTopologyName, serviceName, context).block();
    }

    /**
     * Deletes the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<Void>> deleteWithResponseAsync(
        String resourceGroupName, String serviceTopologyName, String serviceName) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        if (serviceName == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceName is required and cannot be null."));
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .delete(
                            this.client.getEndpoint(),
                            this.client.getSubscriptionId(),
                            resourceGroupName,
                            serviceTopologyName,
                            serviceName,
                            this.client.getApiVersion(),
                            accept,
                            context))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Deletes the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<Void>> deleteWithResponseAsync(
        String resourceGroupName, String serviceTopologyName, String serviceName, Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        if (serviceName == null) {
            return Mono.error(new IllegalArgumentException("Parameter serviceName is required and cannot be null."));
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service
            .delete(
                this.client.getEndpoint(),
                this.client.getSubscriptionId(),
                resourceGroupName,
                serviceTopologyName,
                serviceName,
                this.client.getApiVersion(),
                accept,
                context);
    }

    /**
     * Deletes the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Void> deleteAsync(String resourceGroupName, String serviceTopologyName, String serviceName) {
        return deleteWithResponseAsync(resourceGroupName, serviceTopologyName, serviceName)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Deletes the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String resourceGroupName, String serviceTopologyName, String serviceName) {
        deleteAsync(resourceGroupName, serviceTopologyName, serviceName).block();
    }

    /**
     * Deletes the service.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param serviceName The name of the service resource.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(
        String resourceGroupName, String serviceTopologyName, String serviceName, Context context) {
        return deleteWithResponseAsync(resourceGroupName, serviceTopologyName, serviceName, context).block();
    }

    /**
     * Lists the services in the service topology.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of services.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<List<ServiceResourceInner>>> listWithResponseAsync(
        String resourceGroupName, String serviceTopologyName) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(
                context ->
                    service
                        .list(
                            this.client.getEndpoint(),
                            this.client.getSubscriptionId(),
                            resourceGroupName,
                            serviceTopologyName,
                            this.client.getApiVersion(),
                            accept,
                            context))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Lists the services in the service topology.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of services.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<List<ServiceResourceInner>>> listWithResponseAsync(
        String resourceGroupName, String serviceTopologyName, Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (this.client.getSubscriptionId() == null) {
            return Mono
                .error(
                    new IllegalArgumentException(
                        "Parameter this.client.getSubscriptionId() is required and cannot be null."));
        }
        if (resourceGroupName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (serviceTopologyName == null) {
            return Mono
                .error(new IllegalArgumentException("Parameter serviceTopologyName is required and cannot be null."));
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service
            .list(
                this.client.getEndpoint(),
                this.client.getSubscriptionId(),
                resourceGroupName,
                serviceTopologyName,
                this.client.getApiVersion(),
                accept,
                context);
    }

    /**
     * Lists the services in the service topology.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of services.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<List<ServiceResourceInner>> listAsync(String resourceGroupName, String serviceTopologyName) {
        return listWithResponseAsync(resourceGroupName, serviceTopologyName)
            .flatMap(
                (Response<List<ServiceResourceInner>> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Lists the services in the service topology.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of services.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<ServiceResourceInner> list(String resourceGroupName, String serviceTopologyName) {
        return listAsync(resourceGroupName, serviceTopologyName).block();
    }

    /**
     * Lists the services in the service topology.
     *
     * @param resourceGroupName The name of the resource group. The name is case insensitive.
     * @param serviceTopologyName The name of the service topology .
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the list of services.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<ServiceResourceInner>> listWithResponse(
        String resourceGroupName, String serviceTopologyName, Context context) {
        return listWithResponseAsync(resourceGroupName, serviceTopologyName, context).block();
    }
}
