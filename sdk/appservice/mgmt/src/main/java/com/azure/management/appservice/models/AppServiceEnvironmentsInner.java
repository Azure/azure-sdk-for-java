package com.azure.management.appservice.models;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Patch;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.management.resources.fluentcore.collection.InnerSupportsDelete;
import com.azure.management.resources.fluentcore.collection.InnerSupportsGet;
import com.azure.management.resources.fluentcore.collection.InnerSupportsListing;
import com.azure.management.appservice.AppServiceEnvironmentPatchResource;
import com.azure.management.appservice.DefaultErrorResponseException;
import com.azure.management.appservice.VirtualNetworkProfile;
import java.nio.ByteBuffer;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to all the operations defined in
 * AppServiceEnvironments.
 */
public final class AppServiceEnvironmentsInner implements InnerSupportsGet<AppServiceEnvironmentResourceInner>, InnerSupportsListing<AppServiceEnvironmentResourceInner>, InnerSupportsDelete<Void> {
    /**
     * The proxy service used to perform REST calls.
     */
    private AppServiceEnvironmentsService service;

    /**
     * The service client containing this operation class.
     */
    private WebSiteManagementClientImpl client;

    /**
     * Initializes an instance of AppServiceEnvironmentsInner.
     *
     * @param client the instance of the service client containing this operation class.
     */
    public AppServiceEnvironmentsInner(WebSiteManagementClientImpl client) {
        this.service = RestProxy.create(AppServiceEnvironmentsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for
     * WebSiteManagementClientAppServiceEnvironments to be used by the proxy
     * service to perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "WebSiteManagementClientAppServiceEnvironments")
    private interface AppServiceEnvironmentsService {
        @Get("/subscriptions/{subscriptionId}/providers/Microsoft.Web/hostingEnvironments")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServiceEnvironmentCollectionInner>> list(@HostParam("$host") String host, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServiceEnvironmentCollectionInner>> listByResourceGroup(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServiceEnvironmentResourceInner>> getByResourceGroup(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Put("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        @ExpectedResponses({200, 201, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<Flux<ByteBuffer>>> createOrUpdate(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope, @QueryParam("api-version") String apiVersion);

        @Delete("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        @ExpectedResponses({202, 204})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<Flux<ByteBuffer>>> delete(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @QueryParam("forceDelete") Boolean forceDelete, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Patch("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        @ExpectedResponses({200, 201, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServiceEnvironmentResourceInner>> update(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") AppServiceEnvironmentPatchResource hostingEnvironmentEnvelope, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/capacities/compute")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<StampCapacityCollectionInner>> listCapacities(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/capacities/virtualip")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AddressResponseInner>> getVipInfo(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Post("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/changeVirtualNetwork")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<Flux<ByteBuffer>>> changeVnet(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") VirtualNetworkProfile vnetInfo, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/diagnostics")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<List<HostingEnvironmentDiagnosticsInner>>> listDiagnostics(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/diagnostics/{diagnosticsName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<HostingEnvironmentDiagnosticsInner>> getDiagnosticsItem(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("diagnosticsName") String diagnosticsName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/inboundNetworkDependenciesEndpoints")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<InboundEnvironmentEndpointCollectionInner>> getInboundNetworkDependenciesEndpoints(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolCollectionInner>> listMultiRolePools(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolResourceInner>> getMultiRolePool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Put("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<Flux<ByteBuffer>>> createOrUpdateMultiRolePool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") WorkerPoolResourceInner multiRolePoolEnvelope, @QueryParam("api-version") String apiVersion);

        @Patch("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolResourceInner>> updateMultiRolePool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") WorkerPoolResourceInner multiRolePoolEnvelope, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/instances/{instance}/metricdefinitions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listMultiRolePoolInstanceMetricDefinitions(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("instance") String instance, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/metricdefinitions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listMultiRoleMetricDefinitions(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/skus")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<SkuInfoCollectionInner>> listMultiRolePoolSkus(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default/usages")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<UsageCollectionInner>> listMultiRoleUsages(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/operations")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<List<OperationInner>>> listOperations(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/outboundNetworkDependenciesEndpoints")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<OutboundEnvironmentEndpointCollectionInner>> getOutboundNetworkDependenciesEndpoints(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Post("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/reboot")
        @ExpectedResponses({202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<Response<Void>> reboot(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Post("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/resume")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<Flux<ByteBuffer>>> resume(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/serverfarms")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServicePlanCollectionInner>> listAppServicePlans(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/sites")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WebAppCollectionInner>> listWebApps(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @QueryParam("propertiesToInclude") String propertiesToInclude, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Post("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/suspend")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<Flux<ByteBuffer>>> suspend(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/usages")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<CsmUsageQuotaCollectionInner>> listUsages(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @QueryParam(value = "$filter", encoded = true) String filter, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolCollectionInner>> listWorkerPools(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolResourceInner>> getWorkerPool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Put("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<Flux<ByteBuffer>>> createOrUpdateWorkerPool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") WorkerPoolResourceInner workerPoolEnvelope, @QueryParam("api-version") String apiVersion);

        @Patch("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolResourceInner>> updateWorkerPool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") WorkerPoolResourceInner workerPoolEnvelope, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/instances/{instance}/metricdefinitions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listWorkerPoolInstanceMetricDefinitions(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("instance") String instance, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/metricdefinitions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listWebWorkerMetricDefinitions(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/skus")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<SkuInfoCollectionInner>> listWorkerPoolSkus(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Get("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}/usages")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<UsageCollectionInner>> listWebWorkerUsages(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Put("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        @ExpectedResponses({200, 201, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServiceEnvironmentResourceInner>> beginCreateOrUpdate(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope, @QueryParam("api-version") String apiVersion);

        @Delete("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}")
        @ExpectedResponses({202, 204})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<Response<Void>> beginDelete(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @QueryParam("forceDelete") Boolean forceDelete, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Post("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/changeVirtualNetwork")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WebAppCollectionInner>> beginChangeVnet(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") VirtualNetworkProfile vnetInfo, @QueryParam("api-version") String apiVersion);

        @Put("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/multiRolePools/default")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolResourceInner>> beginCreateOrUpdateMultiRolePool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") WorkerPoolResourceInner multiRolePoolEnvelope, @QueryParam("api-version") String apiVersion);

        @Post("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/resume")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WebAppCollectionInner>> beginResume(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Post("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/suspend")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WebAppCollectionInner>> beginSuspend(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("subscriptionId") String subscriptionId, @QueryParam("api-version") String apiVersion);

        @Put("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Web/hostingEnvironments/{name}/workerPools/{workerPoolName}")
        @ExpectedResponses({200, 202})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolResourceInner>> beginCreateOrUpdateWorkerPool(@HostParam("$host") String host, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("name") String name, @PathParam("workerPoolName") String workerPoolName, @PathParam("subscriptionId") String subscriptionId, @BodyParam("application/json") WorkerPoolResourceInner workerPoolEnvelope, @QueryParam("api-version") String apiVersion);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServiceEnvironmentCollectionInner>> listNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServiceEnvironmentCollectionInner>> listByResourceGroupNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<StampCapacityCollectionInner>> listCapacitiesNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<InboundEnvironmentEndpointCollectionInner>> getInboundNetworkDependenciesEndpointsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolCollectionInner>> listMultiRolePoolsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listMultiRolePoolInstanceMetricDefinitionsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listMultiRoleMetricDefinitionsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<SkuInfoCollectionInner>> listMultiRolePoolSkusNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<UsageCollectionInner>> listMultiRoleUsagesNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<OutboundEnvironmentEndpointCollectionInner>> getOutboundNetworkDependenciesEndpointsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<AppServicePlanCollectionInner>> listAppServicePlansNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WebAppCollectionInner>> listWebAppsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<CsmUsageQuotaCollectionInner>> listUsagesNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<WorkerPoolCollectionInner>> listWorkerPoolsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listWorkerPoolInstanceMetricDefinitionsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<ResourceMetricDefinitionCollectionInner>> listWebWorkerMetricDefinitionsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<SkuInfoCollectionInner>> listWorkerPoolSkusNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

        @Get("{nextLink}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(DefaultErrorResponseException.class)
        Mono<SimpleResponse<UsageCollectionInner>> listWebWorkerUsagesNext(@PathParam(value = "nextLink", encoded = true) String nextLink);
    }

    /**
     * Description for Get all App Service Environments for a subscription.
     *
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AppServiceEnvironmentResourceInner>> listSinglePageAsync() {
        return service.list(this.client.getHost(), this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get all App Service Environments for a subscription.
     *
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AppServiceEnvironmentResourceInner> listAsync() {
        return new PagedFlux<>(
                () -> listSinglePageAsync(),
                nextLink -> listNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get all App Service Environments for a subscription.
     *
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AppServiceEnvironmentResourceInner> list() {
        return new PagedIterable<>(listAsync());
    }

    /**
     * Description for Get all App Service Environments in a resource group.
     *
     * @param resourceGroupName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AppServiceEnvironmentResourceInner>> listByResourceGroupSinglePageAsync(String resourceGroupName) {
        return service.listByResourceGroup(this.client.getHost(), resourceGroupName, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get all App Service Environments in a resource group.
     *
     * @param resourceGroupName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AppServiceEnvironmentResourceInner> listByResourceGroupAsync(String resourceGroupName) {
        return new PagedFlux<>(
                () -> listByResourceGroupSinglePageAsync(resourceGroupName),
                nextLink -> listByResourceGroupNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get all App Service Environments in a resource group.
     *
     * @param resourceGroupName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AppServiceEnvironmentResourceInner> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    /**
     * Description for Get the properties of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<AppServiceEnvironmentResourceInner>> getByResourceGroupWithResponseAsync(String resourceGroupName, String name) {
        return service.getByResourceGroup(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Get the properties of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AppServiceEnvironmentResourceInner> getByResourceGroupAsync(String resourceGroupName, String name) {
        return getByResourceGroupWithResponseAsync(resourceGroupName, name)
                .flatMap((SimpleResponse<AppServiceEnvironmentResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Get the properties of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AppServiceEnvironmentResourceInner getByResourceGroup(String resourceGroupName, String name) {
        return getByResourceGroupAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Flux<ByteBuffer>>> createOrUpdateWithResponseAsync(String resourceGroupName, String name, AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope) {
        return service.createOrUpdate(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), hostingEnvironmentEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AppServiceEnvironmentResourceInner> createOrUpdateAsync(String resourceGroupName, String name, AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope) {
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = createOrUpdateWithResponseAsync(resourceGroupName, name, hostingEnvironmentEnvelope);
        return client.<AppServiceEnvironmentResourceInner, AppServiceEnvironmentResourceInner>getLroResultAsync(response, client.getHttpPipeline(), AppServiceEnvironmentResourceInner.class, AppServiceEnvironmentResourceInner.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AppServiceEnvironmentResourceInner createOrUpdate(String resourceGroupName, String name, AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope) {
        return createOrUpdateAsync(resourceGroupName, name, hostingEnvironmentEnvelope).block();
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param forceDelete
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Flux<ByteBuffer>>> deleteWithResponseAsync(String resourceGroupName, String name, Boolean forceDelete) {
        return service.delete(this.client.getHost(), resourceGroupName, name, forceDelete, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param forceDelete
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteAsync(String resourceGroupName, String name, Boolean forceDelete) {
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = deleteWithResponseAsync(resourceGroupName, name, forceDelete);
        return client.<Void, Void>getLroResultAsync(response, client.getHttpPipeline(), Void.class, Void.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param forceDelete
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String resourceGroupName, String name, Boolean forceDelete) {
        deleteAsync(resourceGroupName, name, forceDelete).block();
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteAsync(String resourceGroupName, String name) {
        final Boolean forceDelete = null;
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = deleteWithResponseAsync(resourceGroupName, name, forceDelete);
        return client.<Void, Void>getLroResultAsync(response, client.getHttpPipeline(), Void.class, Void.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String resourceGroupName, String name) {
        final Boolean forceDelete = null;
        deleteAsync(resourceGroupName, name, forceDelete).block();
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope ARM resource for a app service environment.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<AppServiceEnvironmentResourceInner>> updateWithResponseAsync(String resourceGroupName, String name, AppServiceEnvironmentPatchResource hostingEnvironmentEnvelope) {
        return service.update(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), hostingEnvironmentEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope ARM resource for a app service environment.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AppServiceEnvironmentResourceInner> updateAsync(String resourceGroupName, String name, AppServiceEnvironmentPatchResource hostingEnvironmentEnvelope) {
        return updateWithResponseAsync(resourceGroupName, name, hostingEnvironmentEnvelope)
                .flatMap((SimpleResponse<AppServiceEnvironmentResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope ARM resource for a app service environment.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AppServiceEnvironmentResourceInner update(String resourceGroupName, String name, AppServiceEnvironmentPatchResource hostingEnvironmentEnvelope) {
        return updateAsync(resourceGroupName, name, hostingEnvironmentEnvelope).block();
    }

    /**
     * Description for Get the used, available, and total worker capacity an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<StampCapacityInner>> listCapacitiesSinglePageAsync(String resourceGroupName, String name) {
        return service.listCapacities(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get the used, available, and total worker capacity an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<StampCapacityInner> listCapacitiesAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> listCapacitiesSinglePageAsync(resourceGroupName, name),
                nextLink -> listCapacitiesNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get the used, available, and total worker capacity an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<StampCapacityInner> listCapacities(String resourceGroupName, String name) {
        return new PagedIterable<>(listCapacitiesAsync(resourceGroupName, name));
    }

    /**
     * Description for Get IP addresses assigned to an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<AddressResponseInner>> getVipInfoWithResponseAsync(String resourceGroupName, String name) {
        return service.getVipInfo(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Get IP addresses assigned to an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddressResponseInner> getVipInfoAsync(String resourceGroupName, String name) {
        return getVipInfoWithResponseAsync(resourceGroupName, name)
                .flatMap((SimpleResponse<AddressResponseInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Get IP addresses assigned to an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddressResponseInner getVipInfo(String resourceGroupName, String name) {
        return getVipInfoAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Move an App Service Environment to a different VNET.
     *
     * @param resourceGroupName
     * @param name
     * @param vnetInfo Specification for using a Virtual Network.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Flux<ByteBuffer>>> changeVnetWithResponseAsync(String resourceGroupName, String name, VirtualNetworkProfile vnetInfo) {
        return service.changeVnet(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), vnetInfo, this.client.getApiVersion());
    }

    /**
     * Description for Move an App Service Environment to a different VNET.
     *
     * @param resourceGroupName
     * @param name
     * @param vnetInfo Specification for using a Virtual Network.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WebAppCollectionInner> changeVnetAsync(String resourceGroupName, String name, VirtualNetworkProfile vnetInfo) {
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = changeVnetWithResponseAsync(resourceGroupName, name, vnetInfo);
        return client.<WebAppCollectionInner, WebAppCollectionInner>getLroResultAsync(response, client.getHttpPipeline(), WebAppCollectionInner.class, WebAppCollectionInner.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Move an App Service Environment to a different VNET.
     *
     * @param resourceGroupName
     * @param name
     * @param vnetInfo Specification for using a Virtual Network.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WebAppCollectionInner changeVnet(String resourceGroupName, String name, VirtualNetworkProfile vnetInfo) {
        return changeVnetAsync(resourceGroupName, name, vnetInfo).block();
    }

    /**
     * Description for Get diagnostic information for an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<List<HostingEnvironmentDiagnosticsInner>>> listDiagnosticsWithResponseAsync(String resourceGroupName, String name) {
        return service.listDiagnostics(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Get diagnostic information for an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<HostingEnvironmentDiagnosticsInner>> listDiagnosticsAsync(String resourceGroupName, String name) {
        return listDiagnosticsWithResponseAsync(resourceGroupName, name)
                .flatMap((SimpleResponse<List<HostingEnvironmentDiagnosticsInner>> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Get diagnostic information for an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<HostingEnvironmentDiagnosticsInner> listDiagnostics(String resourceGroupName, String name) {
        return listDiagnosticsAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Get a diagnostics item for an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param diagnosticsName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<HostingEnvironmentDiagnosticsInner>> getDiagnosticsItemWithResponseAsync(String resourceGroupName, String name, String diagnosticsName) {
        return service.getDiagnosticsItem(this.client.getHost(), resourceGroupName, name, diagnosticsName, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Get a diagnostics item for an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param diagnosticsName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<HostingEnvironmentDiagnosticsInner> getDiagnosticsItemAsync(String resourceGroupName, String name, String diagnosticsName) {
        return getDiagnosticsItemWithResponseAsync(resourceGroupName, name, diagnosticsName)
                .flatMap((SimpleResponse<HostingEnvironmentDiagnosticsInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Get a diagnostics item for an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param diagnosticsName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public HostingEnvironmentDiagnosticsInner getDiagnosticsItem(String resourceGroupName, String name, String diagnosticsName) {
        return getDiagnosticsItemAsync(resourceGroupName, name, diagnosticsName).block();
    }

    /**
     * Description for Get the network endpoints of all inbound dependencies of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<InboundEnvironmentEndpointInner>> getInboundNetworkDependenciesEndpointsSinglePageAsync(String resourceGroupName, String name) {
        return service.getInboundNetworkDependenciesEndpoints(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get the network endpoints of all inbound dependencies of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<InboundEnvironmentEndpointInner> getInboundNetworkDependenciesEndpointsAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> getInboundNetworkDependenciesEndpointsSinglePageAsync(resourceGroupName, name),
                nextLink -> getInboundNetworkDependenciesEndpointsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get the network endpoints of all inbound dependencies of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<InboundEnvironmentEndpointInner> getInboundNetworkDependenciesEndpoints(String resourceGroupName, String name) {
        return new PagedIterable<>(getInboundNetworkDependenciesEndpointsAsync(resourceGroupName, name));
    }

    /**
     * Description for Get all multi-role pools.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<WorkerPoolResourceInner>> listMultiRolePoolsSinglePageAsync(String resourceGroupName, String name) {
        return service.listMultiRolePools(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get all multi-role pools.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<WorkerPoolResourceInner> listMultiRolePoolsAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> listMultiRolePoolsSinglePageAsync(resourceGroupName, name),
                nextLink -> listMultiRolePoolsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get all multi-role pools.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<WorkerPoolResourceInner> listMultiRolePools(String resourceGroupName, String name) {
        return new PagedIterable<>(listMultiRolePoolsAsync(resourceGroupName, name));
    }

    /**
     * Description for Get properties of a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WorkerPoolResourceInner>> getMultiRolePoolWithResponseAsync(String resourceGroupName, String name) {
        return service.getMultiRolePool(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Get properties of a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> getMultiRolePoolAsync(String resourceGroupName, String name) {
        return getMultiRolePoolWithResponseAsync(resourceGroupName, name)
                .flatMap((SimpleResponse<WorkerPoolResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Get properties of a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner getMultiRolePool(String resourceGroupName, String name) {
        return getMultiRolePoolAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Flux<ByteBuffer>>> createOrUpdateMultiRolePoolWithResponseAsync(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return service.createOrUpdateMultiRolePool(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), multiRolePoolEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> createOrUpdateMultiRolePoolAsync(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = createOrUpdateMultiRolePoolWithResponseAsync(resourceGroupName, name, multiRolePoolEnvelope);
        return client.<WorkerPoolResourceInner, WorkerPoolResourceInner>getLroResultAsync(response, client.getHttpPipeline(), WorkerPoolResourceInner.class, WorkerPoolResourceInner.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner createOrUpdateMultiRolePool(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return createOrUpdateMultiRolePoolAsync(resourceGroupName, name, multiRolePoolEnvelope).block();
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WorkerPoolResourceInner>> updateMultiRolePoolWithResponseAsync(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return service.updateMultiRolePool(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), multiRolePoolEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> updateMultiRolePoolAsync(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return updateMultiRolePoolWithResponseAsync(resourceGroupName, name, multiRolePoolEnvelope)
                .flatMap((SimpleResponse<WorkerPoolResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner updateMultiRolePool(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return updateMultiRolePoolAsync(resourceGroupName, name, multiRolePoolEnvelope).block();
    }

    /**
     * Description for Get metric definitions for a specific instance of a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param instance
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listMultiRolePoolInstanceMetricDefinitionsSinglePageAsync(String resourceGroupName, String name, String instance) {
        return service.listMultiRolePoolInstanceMetricDefinitions(this.client.getHost(), resourceGroupName, name, instance, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get metric definitions for a specific instance of a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param instance
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ResourceMetricDefinitionInner> listMultiRolePoolInstanceMetricDefinitionsAsync(String resourceGroupName, String name, String instance) {
        return new PagedFlux<>(
                () -> listMultiRolePoolInstanceMetricDefinitionsSinglePageAsync(resourceGroupName, name, instance),
                nextLink -> listMultiRolePoolInstanceMetricDefinitionsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get metric definitions for a specific instance of a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param instance
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ResourceMetricDefinitionInner> listMultiRolePoolInstanceMetricDefinitions(String resourceGroupName, String name, String instance) {
        return new PagedIterable<>(listMultiRolePoolInstanceMetricDefinitionsAsync(resourceGroupName, name, instance));
    }

    /**
     * Description for Get metric definitions for a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listMultiRoleMetricDefinitionsSinglePageAsync(String resourceGroupName, String name) {
        return service.listMultiRoleMetricDefinitions(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get metric definitions for a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ResourceMetricDefinitionInner> listMultiRoleMetricDefinitionsAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> listMultiRoleMetricDefinitionsSinglePageAsync(resourceGroupName, name),
                nextLink -> listMultiRoleMetricDefinitionsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get metric definitions for a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ResourceMetricDefinitionInner> listMultiRoleMetricDefinitions(String resourceGroupName, String name) {
        return new PagedIterable<>(listMultiRoleMetricDefinitionsAsync(resourceGroupName, name));
    }

    /**
     * Description for Get available SKUs for scaling a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SkuInfoInner>> listMultiRolePoolSkusSinglePageAsync(String resourceGroupName, String name) {
        return service.listMultiRolePoolSkus(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get available SKUs for scaling a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SkuInfoInner> listMultiRolePoolSkusAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> listMultiRolePoolSkusSinglePageAsync(resourceGroupName, name),
                nextLink -> listMultiRolePoolSkusNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get available SKUs for scaling a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SkuInfoInner> listMultiRolePoolSkus(String resourceGroupName, String name) {
        return new PagedIterable<>(listMultiRolePoolSkusAsync(resourceGroupName, name));
    }

    /**
     * Description for Get usage metrics for a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UsageInner>> listMultiRoleUsagesSinglePageAsync(String resourceGroupName, String name) {
        return service.listMultiRoleUsages(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get usage metrics for a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<UsageInner> listMultiRoleUsagesAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> listMultiRoleUsagesSinglePageAsync(resourceGroupName, name),
                nextLink -> listMultiRoleUsagesNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get usage metrics for a multi-role pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<UsageInner> listMultiRoleUsages(String resourceGroupName, String name) {
        return new PagedIterable<>(listMultiRoleUsagesAsync(resourceGroupName, name));
    }

    /**
     * Description for List all currently running operations on the App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<List<OperationInner>>> listOperationsWithResponseAsync(String resourceGroupName, String name) {
        return service.listOperations(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for List all currently running operations on the App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<OperationInner>> listOperationsAsync(String resourceGroupName, String name) {
        return listOperationsWithResponseAsync(resourceGroupName, name)
                .flatMap((SimpleResponse<List<OperationInner>> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for List all currently running operations on the App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<OperationInner> listOperations(String resourceGroupName, String name) {
        return listOperationsAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Get the network endpoints of all outbound dependencies of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<OutboundEnvironmentEndpointInner>> getOutboundNetworkDependenciesEndpointsSinglePageAsync(String resourceGroupName, String name) {
        return service.getOutboundNetworkDependenciesEndpoints(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get the network endpoints of all outbound dependencies of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<OutboundEnvironmentEndpointInner> getOutboundNetworkDependenciesEndpointsAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> getOutboundNetworkDependenciesEndpointsSinglePageAsync(resourceGroupName, name),
                nextLink -> getOutboundNetworkDependenciesEndpointsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get the network endpoints of all outbound dependencies of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<OutboundEnvironmentEndpointInner> getOutboundNetworkDependenciesEndpoints(String resourceGroupName, String name) {
        return new PagedIterable<>(getOutboundNetworkDependenciesEndpointsAsync(resourceGroupName, name));
    }

    /**
     * Description for Reboot all machines in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rebootWithResponseAsync(String resourceGroupName, String name) {
        return service.reboot(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Reboot all machines in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rebootAsync(String resourceGroupName, String name) {
        return rebootWithResponseAsync(resourceGroupName, name)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Description for Reboot all machines in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void reboot(String resourceGroupName, String name) {
        rebootAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Resume an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Flux<ByteBuffer>>> resumeWithResponseAsync(String resourceGroupName, String name) {
        return service.resume(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Resume an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WebAppCollectionInner> resumeAsync(String resourceGroupName, String name) {
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = resumeWithResponseAsync(resourceGroupName, name);
        return client.<WebAppCollectionInner, WebAppCollectionInner>getLroResultAsync(response, client.getHttpPipeline(), WebAppCollectionInner.class, WebAppCollectionInner.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Resume an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WebAppCollectionInner resume(String resourceGroupName, String name) {
        return resumeAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Get all App Service plans in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AppServicePlanInner>> listAppServicePlansSinglePageAsync(String resourceGroupName, String name) {
        return service.listAppServicePlans(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get all App Service plans in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AppServicePlanInner> listAppServicePlansAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> listAppServicePlansSinglePageAsync(resourceGroupName, name),
                nextLink -> listAppServicePlansNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get all App Service plans in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AppServicePlanInner> listAppServicePlans(String resourceGroupName, String name) {
        return new PagedIterable<>(listAppServicePlansAsync(resourceGroupName, name));
    }

    /**
     * Description for Get all apps in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param propertiesToInclude
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SiteInner>> listWebAppsSinglePageAsync(String resourceGroupName, String name, String propertiesToInclude) {
        return service.listWebApps(this.client.getHost(), resourceGroupName, name, propertiesToInclude, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get all apps in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param propertiesToInclude
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SiteInner> listWebAppsAsync(String resourceGroupName, String name, String propertiesToInclude) {
        return new PagedFlux<>(
                () -> listWebAppsSinglePageAsync(resourceGroupName, name, propertiesToInclude),
                nextLink -> listWebAppsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get all apps in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SiteInner> listWebAppsAsync(String resourceGroupName, String name) {
        final String propertiesToInclude = null;
        return new PagedFlux<>(
                () -> listWebAppsSinglePageAsync(resourceGroupName, name, propertiesToInclude),
                nextLink -> listWebAppsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get all apps in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param propertiesToInclude
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SiteInner> listWebApps(String resourceGroupName, String name, String propertiesToInclude) {
        return new PagedIterable<>(listWebAppsAsync(resourceGroupName, name, propertiesToInclude));
    }

    /**
     * Description for Get all apps in an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SiteInner> listWebApps(String resourceGroupName, String name) {
        final String propertiesToInclude = null;
        return new PagedIterable<>(listWebAppsAsync(resourceGroupName, name, propertiesToInclude));
    }

    /**
     * Description for Suspend an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Flux<ByteBuffer>>> suspendWithResponseAsync(String resourceGroupName, String name) {
        return service.suspend(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Suspend an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WebAppCollectionInner> suspendAsync(String resourceGroupName, String name) {
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = suspendWithResponseAsync(resourceGroupName, name);
        return client.<WebAppCollectionInner, WebAppCollectionInner>getLroResultAsync(response, client.getHttpPipeline(), WebAppCollectionInner.class, WebAppCollectionInner.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Suspend an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WebAppCollectionInner suspend(String resourceGroupName, String name) {
        return suspendAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Get global usage metrics of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param filter
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<CsmUsageQuotaInner>> listUsagesSinglePageAsync(String resourceGroupName, String name, String filter) {
        return service.listUsages(this.client.getHost(), resourceGroupName, name, filter, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get global usage metrics of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param filter
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CsmUsageQuotaInner> listUsagesAsync(String resourceGroupName, String name, String filter) {
        return new PagedFlux<>(
                () -> listUsagesSinglePageAsync(resourceGroupName, name, filter),
                nextLink -> listUsagesNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get global usage metrics of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CsmUsageQuotaInner> listUsagesAsync(String resourceGroupName, String name) {
        final String filter = null;
        return new PagedFlux<>(
                () -> listUsagesSinglePageAsync(resourceGroupName, name, filter),
                nextLink -> listUsagesNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get global usage metrics of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param filter
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CsmUsageQuotaInner> listUsages(String resourceGroupName, String name, String filter) {
        return new PagedIterable<>(listUsagesAsync(resourceGroupName, name, filter));
    }

    /**
     * Description for Get global usage metrics of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<CsmUsageQuotaInner> listUsages(String resourceGroupName, String name) {
        final String filter = null;
        return new PagedIterable<>(listUsagesAsync(resourceGroupName, name, filter));
    }

    /**
     * Description for Get all worker pools of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<WorkerPoolResourceInner>> listWorkerPoolsSinglePageAsync(String resourceGroupName, String name) {
        return service.listWorkerPools(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get all worker pools of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<WorkerPoolResourceInner> listWorkerPoolsAsync(String resourceGroupName, String name) {
        return new PagedFlux<>(
                () -> listWorkerPoolsSinglePageAsync(resourceGroupName, name),
                nextLink -> listWorkerPoolsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get all worker pools of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<WorkerPoolResourceInner> listWorkerPools(String resourceGroupName, String name) {
        return new PagedIterable<>(listWorkerPoolsAsync(resourceGroupName, name));
    }

    /**
     * Description for Get properties of a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WorkerPoolResourceInner>> getWorkerPoolWithResponseAsync(String resourceGroupName, String name, String workerPoolName) {
        return service.getWorkerPool(this.client.getHost(), resourceGroupName, name, workerPoolName, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Get properties of a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> getWorkerPoolAsync(String resourceGroupName, String name, String workerPoolName) {
        return getWorkerPoolWithResponseAsync(resourceGroupName, name, workerPoolName)
                .flatMap((SimpleResponse<WorkerPoolResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Get properties of a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner getWorkerPool(String resourceGroupName, String name, String workerPoolName) {
        return getWorkerPoolAsync(resourceGroupName, name, workerPoolName).block();
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Flux<ByteBuffer>>> createOrUpdateWorkerPoolWithResponseAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return service.createOrUpdateWorkerPool(this.client.getHost(), resourceGroupName, name, workerPoolName, this.client.getSubscriptionId(), workerPoolEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> createOrUpdateWorkerPoolAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        Mono<SimpleResponse<Flux<ByteBuffer>>> response = createOrUpdateWorkerPoolWithResponseAsync(resourceGroupName, name, workerPoolName, workerPoolEnvelope);
        return client.<WorkerPoolResourceInner, WorkerPoolResourceInner>getLroResultAsync(response, client.getHttpPipeline(), WorkerPoolResourceInner.class, WorkerPoolResourceInner.class)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult);
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner createOrUpdateWorkerPool(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return createOrUpdateWorkerPoolAsync(resourceGroupName, name, workerPoolName, workerPoolEnvelope).block();
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WorkerPoolResourceInner>> updateWorkerPoolWithResponseAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return service.updateWorkerPool(this.client.getHost(), resourceGroupName, name, workerPoolName, this.client.getSubscriptionId(), workerPoolEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> updateWorkerPoolAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return updateWorkerPoolWithResponseAsync(resourceGroupName, name, workerPoolName, workerPoolEnvelope)
                .flatMap((SimpleResponse<WorkerPoolResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner updateWorkerPool(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return updateWorkerPoolAsync(resourceGroupName, name, workerPoolName, workerPoolEnvelope).block();
    }

    /**
     * Description for Get metric definitions for a specific instance of a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param instance
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listWorkerPoolInstanceMetricDefinitionsSinglePageAsync(String resourceGroupName, String name, String workerPoolName, String instance) {
        return service.listWorkerPoolInstanceMetricDefinitions(this.client.getHost(), resourceGroupName, name, workerPoolName, instance, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get metric definitions for a specific instance of a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param instance
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ResourceMetricDefinitionInner> listWorkerPoolInstanceMetricDefinitionsAsync(String resourceGroupName, String name, String workerPoolName, String instance) {
        return new PagedFlux<>(
                () -> listWorkerPoolInstanceMetricDefinitionsSinglePageAsync(resourceGroupName, name, workerPoolName, instance),
                nextLink -> listWorkerPoolInstanceMetricDefinitionsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get metric definitions for a specific instance of a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param instance
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ResourceMetricDefinitionInner> listWorkerPoolInstanceMetricDefinitions(String resourceGroupName, String name, String workerPoolName, String instance) {
        return new PagedIterable<>(listWorkerPoolInstanceMetricDefinitionsAsync(resourceGroupName, name, workerPoolName, instance));
    }

    /**
     * Description for Get metric definitions for a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listWebWorkerMetricDefinitionsSinglePageAsync(String resourceGroupName, String name, String workerPoolName) {
        return service.listWebWorkerMetricDefinitions(this.client.getHost(), resourceGroupName, name, workerPoolName, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get metric definitions for a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ResourceMetricDefinitionInner> listWebWorkerMetricDefinitionsAsync(String resourceGroupName, String name, String workerPoolName) {
        return new PagedFlux<>(
                () -> listWebWorkerMetricDefinitionsSinglePageAsync(resourceGroupName, name, workerPoolName),
                nextLink -> listWebWorkerMetricDefinitionsNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get metric definitions for a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ResourceMetricDefinitionInner> listWebWorkerMetricDefinitions(String resourceGroupName, String name, String workerPoolName) {
        return new PagedIterable<>(listWebWorkerMetricDefinitionsAsync(resourceGroupName, name, workerPoolName));
    }

    /**
     * Description for Get available SKUs for scaling a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SkuInfoInner>> listWorkerPoolSkusSinglePageAsync(String resourceGroupName, String name, String workerPoolName) {
        return service.listWorkerPoolSkus(this.client.getHost(), resourceGroupName, name, workerPoolName, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get available SKUs for scaling a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SkuInfoInner> listWorkerPoolSkusAsync(String resourceGroupName, String name, String workerPoolName) {
        return new PagedFlux<>(
                () -> listWorkerPoolSkusSinglePageAsync(resourceGroupName, name, workerPoolName),
                nextLink -> listWorkerPoolSkusNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get available SKUs for scaling a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<SkuInfoInner> listWorkerPoolSkus(String resourceGroupName, String name, String workerPoolName) {
        return new PagedIterable<>(listWorkerPoolSkusAsync(resourceGroupName, name, workerPoolName));
    }

    /**
     * Description for Get usage metrics for a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UsageInner>> listWebWorkerUsagesSinglePageAsync(String resourceGroupName, String name, String workerPoolName) {
        return service.listWebWorkerUsages(this.client.getHost(), resourceGroupName, name, workerPoolName, this.client.getSubscriptionId(), this.client.getApiVersion()).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Description for Get usage metrics for a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<UsageInner> listWebWorkerUsagesAsync(String resourceGroupName, String name, String workerPoolName) {
        return new PagedFlux<>(
                () -> listWebWorkerUsagesSinglePageAsync(resourceGroupName, name, workerPoolName),
                nextLink -> listWebWorkerUsagesNextSinglePageAsync(nextLink));
    }

    /**
     * Description for Get usage metrics for a worker pool of an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<UsageInner> listWebWorkerUsages(String resourceGroupName, String name, String workerPoolName) {
        return new PagedIterable<>(listWebWorkerUsagesAsync(resourceGroupName, name, workerPoolName));
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<AppServiceEnvironmentResourceInner>> beginCreateOrUpdateWithResponseAsync(String resourceGroupName, String name, AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope) {
        return service.beginCreateOrUpdate(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), hostingEnvironmentEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AppServiceEnvironmentResourceInner> beginCreateOrUpdateAsync(String resourceGroupName, String name, AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope) {
        return beginCreateOrUpdateWithResponseAsync(resourceGroupName, name, hostingEnvironmentEnvelope)
                .flatMap((SimpleResponse<AppServiceEnvironmentResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Create or update an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param hostingEnvironmentEnvelope App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AppServiceEnvironmentResourceInner beginCreateOrUpdate(String resourceGroupName, String name, AppServiceEnvironmentResourceInner hostingEnvironmentEnvelope) {
        return beginCreateOrUpdateAsync(resourceGroupName, name, hostingEnvironmentEnvelope).block();
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param forceDelete
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> beginDeleteWithResponseAsync(String resourceGroupName, String name, Boolean forceDelete) {
        return service.beginDelete(this.client.getHost(), resourceGroupName, name, forceDelete, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param forceDelete
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> beginDeleteAsync(String resourceGroupName, String name, Boolean forceDelete) {
        return beginDeleteWithResponseAsync(resourceGroupName, name, forceDelete)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> beginDeleteAsync(String resourceGroupName, String name) {
        final Boolean forceDelete = null;
        return beginDeleteWithResponseAsync(resourceGroupName, name, forceDelete)
                .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @param forceDelete
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void beginDelete(String resourceGroupName, String name, Boolean forceDelete) {
        beginDeleteAsync(resourceGroupName, name, forceDelete).block();
    }

    /**
     * Description for Delete an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void beginDelete(String resourceGroupName, String name) {
        final Boolean forceDelete = null;
        beginDeleteAsync(resourceGroupName, name, forceDelete).block();
    }

    /**
     * Description for Move an App Service Environment to a different VNET.
     *
     * @param resourceGroupName
     * @param name
     * @param vnetInfo Specification for using a Virtual Network.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WebAppCollectionInner>> beginChangeVnetWithResponseAsync(String resourceGroupName, String name, VirtualNetworkProfile vnetInfo) {
        return service.beginChangeVnet(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), vnetInfo, this.client.getApiVersion());
    }

    /**
     * Description for Move an App Service Environment to a different VNET.
     *
     * @param resourceGroupName
     * @param name
     * @param vnetInfo Specification for using a Virtual Network.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WebAppCollectionInner> beginChangeVnetAsync(String resourceGroupName, String name, VirtualNetworkProfile vnetInfo) {
        return beginChangeVnetWithResponseAsync(resourceGroupName, name, vnetInfo)
                .flatMap((SimpleResponse<WebAppCollectionInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Move an App Service Environment to a different VNET.
     *
     * @param resourceGroupName
     * @param name
     * @param vnetInfo Specification for using a Virtual Network.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WebAppCollectionInner beginChangeVnet(String resourceGroupName, String name, VirtualNetworkProfile vnetInfo) {
        return beginChangeVnetAsync(resourceGroupName, name, vnetInfo).block();
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WorkerPoolResourceInner>> beginCreateOrUpdateMultiRolePoolWithResponseAsync(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return service.beginCreateOrUpdateMultiRolePool(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), multiRolePoolEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> beginCreateOrUpdateMultiRolePoolAsync(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return beginCreateOrUpdateMultiRolePoolWithResponseAsync(resourceGroupName, name, multiRolePoolEnvelope)
                .flatMap((SimpleResponse<WorkerPoolResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Create or update a multi-role pool.
     *
     * @param resourceGroupName
     * @param name
     * @param multiRolePoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner beginCreateOrUpdateMultiRolePool(String resourceGroupName, String name, WorkerPoolResourceInner multiRolePoolEnvelope) {
        return beginCreateOrUpdateMultiRolePoolAsync(resourceGroupName, name, multiRolePoolEnvelope).block();
    }

    /**
     * Description for Resume an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WebAppCollectionInner>> beginResumeWithResponseAsync(String resourceGroupName, String name) {
        return service.beginResume(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Resume an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WebAppCollectionInner> beginResumeAsync(String resourceGroupName, String name) {
        return beginResumeWithResponseAsync(resourceGroupName, name)
                .flatMap((SimpleResponse<WebAppCollectionInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Resume an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WebAppCollectionInner beginResume(String resourceGroupName, String name) {
        return beginResumeAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Suspend an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WebAppCollectionInner>> beginSuspendWithResponseAsync(String resourceGroupName, String name) {
        return service.beginSuspend(this.client.getHost(), resourceGroupName, name, this.client.getSubscriptionId(), this.client.getApiVersion());
    }

    /**
     * Description for Suspend an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WebAppCollectionInner> beginSuspendAsync(String resourceGroupName, String name) {
        return beginSuspendWithResponseAsync(resourceGroupName, name)
                .flatMap((SimpleResponse<WebAppCollectionInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Suspend an App Service Environment.
     *
     * @param resourceGroupName
     * @param name
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WebAppCollectionInner beginSuspend(String resourceGroupName, String name) {
        return beginSuspendAsync(resourceGroupName, name).block();
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<WorkerPoolResourceInner>> beginCreateOrUpdateWorkerPoolWithResponseAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return service.beginCreateOrUpdateWorkerPool(this.client.getHost(), resourceGroupName, name, workerPoolName, this.client.getSubscriptionId(), workerPoolEnvelope, this.client.getApiVersion());
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WorkerPoolResourceInner> beginCreateOrUpdateWorkerPoolAsync(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return beginCreateOrUpdateWorkerPoolWithResponseAsync(resourceGroupName, name, workerPoolName, workerPoolEnvelope)
                .flatMap((SimpleResponse<WorkerPoolResourceInner> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Description for Create or update a worker pool.
     *
     * @param resourceGroupName
     * @param name
     * @param workerPoolName
     * @param workerPoolEnvelope Worker pool of an App Service Environment ARM resource.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WorkerPoolResourceInner beginCreateOrUpdateWorkerPool(String resourceGroupName, String name, String workerPoolName, WorkerPoolResourceInner workerPoolEnvelope) {
        return beginCreateOrUpdateWorkerPoolAsync(resourceGroupName, name, workerPoolName, workerPoolEnvelope).block();
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AppServiceEnvironmentResourceInner>> listNextSinglePageAsync(String nextLink) {
        return service.listNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AppServiceEnvironmentResourceInner>> listByResourceGroupNextSinglePageAsync(String nextLink) {
        return service.listByResourceGroupNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<StampCapacityInner>> listCapacitiesNextSinglePageAsync(String nextLink) {
        return service.listCapacitiesNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<InboundEnvironmentEndpointInner>> getInboundNetworkDependenciesEndpointsNextSinglePageAsync(String nextLink) {
        return service.getInboundNetworkDependenciesEndpointsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<WorkerPoolResourceInner>> listMultiRolePoolsNextSinglePageAsync(String nextLink) {
        return service.listMultiRolePoolsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listMultiRolePoolInstanceMetricDefinitionsNextSinglePageAsync(String nextLink) {
        return service.listMultiRolePoolInstanceMetricDefinitionsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listMultiRoleMetricDefinitionsNextSinglePageAsync(String nextLink) {
        return service.listMultiRoleMetricDefinitionsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SkuInfoInner>> listMultiRolePoolSkusNextSinglePageAsync(String nextLink) {
        return service.listMultiRolePoolSkusNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UsageInner>> listMultiRoleUsagesNextSinglePageAsync(String nextLink) {
        return service.listMultiRoleUsagesNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<OutboundEnvironmentEndpointInner>> getOutboundNetworkDependenciesEndpointsNextSinglePageAsync(String nextLink) {
        return service.getOutboundNetworkDependenciesEndpointsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<AppServicePlanInner>> listAppServicePlansNextSinglePageAsync(String nextLink) {
        return service.listAppServicePlansNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SiteInner>> listWebAppsNextSinglePageAsync(String nextLink) {
        return service.listWebAppsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<CsmUsageQuotaInner>> listUsagesNextSinglePageAsync(String nextLink) {
        return service.listUsagesNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<WorkerPoolResourceInner>> listWorkerPoolsNextSinglePageAsync(String nextLink) {
        return service.listWorkerPoolsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listWorkerPoolInstanceMetricDefinitionsNextSinglePageAsync(String nextLink) {
        return service.listWorkerPoolInstanceMetricDefinitionsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<ResourceMetricDefinitionInner>> listWebWorkerMetricDefinitionsNextSinglePageAsync(String nextLink) {
        return service.listWebWorkerMetricDefinitionsNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<SkuInfoInner>> listWorkerPoolSkusNextSinglePageAsync(String nextLink) {
        return service.listWorkerPoolSkusNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }

    /**
     * Get the next page of items.
     *
     * @param nextLink null
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws DefaultErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedResponse<UsageInner>> listWebWorkerUsagesNextSinglePageAsync(String nextLink) {
        return service.listWebWorkerUsagesNext(nextLink).map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().value(),
                res.getValue().nextLink(),
                null));
    }
}
