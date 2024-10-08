// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.consumption.implementation;

import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Headers;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
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
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.resourcemanager.consumption.fluent.ReservationRecommendationsClient;
import com.azure.resourcemanager.consumption.fluent.models.ReservationRecommendationInner;
import com.azure.resourcemanager.consumption.models.ReservationRecommendationsListResult;
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to all the operations defined in ReservationRecommendationsClient.
 */
public final class ReservationRecommendationsClientImpl implements ReservationRecommendationsClient {
    /**
     * The proxy service used to perform REST calls.
     */
    private final ReservationRecommendationsService service;

    /**
     * The service client containing this operation class.
     */
    private final ConsumptionManagementClientImpl client;

    /**
     * Initializes an instance of ReservationRecommendationsClientImpl.
     * 
     * @param client the instance of the service client containing this operation class.
     */
    ReservationRecommendationsClientImpl(ConsumptionManagementClientImpl client) {
        this.service = RestProxy.create(ReservationRecommendationsService.class, client.getHttpPipeline(),
            client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for ConsumptionManagementClientReservationRecommendations to be used by
     * the proxy service to perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "ConsumptionManagemen")
    public interface ReservationRecommendationsService {
        @Headers({ "Content-Type: application/json" })
        @Get("/{resourceScope}/providers/Microsoft.Consumption/reservationRecommendations")
        @ExpectedResponses({ 200, 204 })
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<ReservationRecommendationsListResult>> list(@HostParam("$host") String endpoint,
            @QueryParam("$filter") String filter, @QueryParam("api-version") String apiVersion,
            @PathParam(value = "resourceScope", encoded = true) String resourceScope,
            @HeaderParam("Accept") String accept, Context context);

        @Headers({ "Content-Type: application/json" })
        @Get("{nextLink}")
        @ExpectedResponses({ 200, 204 })
        @UnexpectedResponseExceptionType(ManagementException.class)
        Mono<Response<ReservationRecommendationsListResult>> listNext(
            @PathParam(value = "nextLink", encoded = true) String nextLink, @HostParam("$host") String endpoint,
            @HeaderParam("Accept") String accept, Context context);
    }

    /**
     * List of recommendations for purchasing reserved instances.
     * 
     * @param resourceScope The scope associated with reservation recommendations operations. This includes
     * '/subscriptions/{subscriptionId}/' for subscription scope,
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}' for resource group scope,
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}' for BillingAccount scope, and
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}/billingProfiles/{billingProfileId}' for
     * billingProfile scope.
     * @param filter May be used to filter reservationRecommendations by: properties/scope with allowed values
     * ['Single', 'Shared'] and default value 'Single'; properties/resourceType with allowed values ['VirtualMachines',
     * 'SQLDatabases', 'PostgreSQL', 'ManagedDisk', 'MySQL', 'RedHat', 'MariaDB', 'RedisCache', 'CosmosDB',
     * 'SqlDataWarehouse', 'SUSELinux', 'AppService', 'BlockBlob', 'AzureDataExplorer', 'VMwareCloudSimple'] and default
     * value 'VirtualMachines'; and properties/lookBackPeriod with allowed values ['Last7Days', 'Last30Days',
     * 'Last60Days'] and default value 'Last7Days'.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations along with {@link PagedResponse} on successful completion
     * of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<PagedResponse<ReservationRecommendationInner>> listSinglePageAsync(String resourceScope,
        String filter) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (resourceScope == null) {
            return Mono.error(new IllegalArgumentException("Parameter resourceScope is required and cannot be null."));
        }
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.list(this.client.getEndpoint(), filter, this.client.getApiVersion(),
                resourceScope, accept, context))
            .<PagedResponse<ReservationRecommendationInner>>map(res -> new PagedResponseBase<>(res.getRequest(),
                res.getStatusCode(), res.getHeaders(), res.getValue().value(), res.getValue().nextLink(), null))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * List of recommendations for purchasing reserved instances.
     * 
     * @param resourceScope The scope associated with reservation recommendations operations. This includes
     * '/subscriptions/{subscriptionId}/' for subscription scope,
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}' for resource group scope,
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}' for BillingAccount scope, and
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}/billingProfiles/{billingProfileId}' for
     * billingProfile scope.
     * @param filter May be used to filter reservationRecommendations by: properties/scope with allowed values
     * ['Single', 'Shared'] and default value 'Single'; properties/resourceType with allowed values ['VirtualMachines',
     * 'SQLDatabases', 'PostgreSQL', 'ManagedDisk', 'MySQL', 'RedHat', 'MariaDB', 'RedisCache', 'CosmosDB',
     * 'SqlDataWarehouse', 'SUSELinux', 'AppService', 'BlockBlob', 'AzureDataExplorer', 'VMwareCloudSimple'] and default
     * value 'VirtualMachines'; and properties/lookBackPeriod with allowed values ['Last7Days', 'Last30Days',
     * 'Last60Days'] and default value 'Last7Days'.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations along with {@link PagedResponse} on successful completion
     * of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<PagedResponse<ReservationRecommendationInner>> listSinglePageAsync(String resourceScope, String filter,
        Context context) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (resourceScope == null) {
            return Mono.error(new IllegalArgumentException("Parameter resourceScope is required and cannot be null."));
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service
            .list(this.client.getEndpoint(), filter, this.client.getApiVersion(), resourceScope, accept, context)
            .map(res -> new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                res.getValue().value(), res.getValue().nextLink(), null));
    }

    /**
     * List of recommendations for purchasing reserved instances.
     * 
     * @param resourceScope The scope associated with reservation recommendations operations. This includes
     * '/subscriptions/{subscriptionId}/' for subscription scope,
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}' for resource group scope,
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}' for BillingAccount scope, and
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}/billingProfiles/{billingProfileId}' for
     * billingProfile scope.
     * @param filter May be used to filter reservationRecommendations by: properties/scope with allowed values
     * ['Single', 'Shared'] and default value 'Single'; properties/resourceType with allowed values ['VirtualMachines',
     * 'SQLDatabases', 'PostgreSQL', 'ManagedDisk', 'MySQL', 'RedHat', 'MariaDB', 'RedisCache', 'CosmosDB',
     * 'SqlDataWarehouse', 'SUSELinux', 'AppService', 'BlockBlob', 'AzureDataExplorer', 'VMwareCloudSimple'] and default
     * value 'VirtualMachines'; and properties/lookBackPeriod with allowed values ['Last7Days', 'Last30Days',
     * 'Last60Days'] and default value 'Last7Days'.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations as paginated response with {@link PagedFlux}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    private PagedFlux<ReservationRecommendationInner> listAsync(String resourceScope, String filter) {
        return new PagedFlux<>(() -> listSinglePageAsync(resourceScope, filter),
            nextLink -> listNextSinglePageAsync(nextLink));
    }

    /**
     * List of recommendations for purchasing reserved instances.
     * 
     * @param resourceScope The scope associated with reservation recommendations operations. This includes
     * '/subscriptions/{subscriptionId}/' for subscription scope,
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}' for resource group scope,
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}' for BillingAccount scope, and
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}/billingProfiles/{billingProfileId}' for
     * billingProfile scope.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations as paginated response with {@link PagedFlux}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    private PagedFlux<ReservationRecommendationInner> listAsync(String resourceScope) {
        final String filter = null;
        return new PagedFlux<>(() -> listSinglePageAsync(resourceScope, filter),
            nextLink -> listNextSinglePageAsync(nextLink));
    }

    /**
     * List of recommendations for purchasing reserved instances.
     * 
     * @param resourceScope The scope associated with reservation recommendations operations. This includes
     * '/subscriptions/{subscriptionId}/' for subscription scope,
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}' for resource group scope,
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}' for BillingAccount scope, and
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}/billingProfiles/{billingProfileId}' for
     * billingProfile scope.
     * @param filter May be used to filter reservationRecommendations by: properties/scope with allowed values
     * ['Single', 'Shared'] and default value 'Single'; properties/resourceType with allowed values ['VirtualMachines',
     * 'SQLDatabases', 'PostgreSQL', 'ManagedDisk', 'MySQL', 'RedHat', 'MariaDB', 'RedisCache', 'CosmosDB',
     * 'SqlDataWarehouse', 'SUSELinux', 'AppService', 'BlockBlob', 'AzureDataExplorer', 'VMwareCloudSimple'] and default
     * value 'VirtualMachines'; and properties/lookBackPeriod with allowed values ['Last7Days', 'Last30Days',
     * 'Last60Days'] and default value 'Last7Days'.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations as paginated response with {@link PagedFlux}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    private PagedFlux<ReservationRecommendationInner> listAsync(String resourceScope, String filter, Context context) {
        return new PagedFlux<>(() -> listSinglePageAsync(resourceScope, filter, context),
            nextLink -> listNextSinglePageAsync(nextLink, context));
    }

    /**
     * List of recommendations for purchasing reserved instances.
     * 
     * @param resourceScope The scope associated with reservation recommendations operations. This includes
     * '/subscriptions/{subscriptionId}/' for subscription scope,
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}' for resource group scope,
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}' for BillingAccount scope, and
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}/billingProfiles/{billingProfileId}' for
     * billingProfile scope.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ReservationRecommendationInner> list(String resourceScope) {
        final String filter = null;
        return new PagedIterable<>(listAsync(resourceScope, filter));
    }

    /**
     * List of recommendations for purchasing reserved instances.
     * 
     * @param resourceScope The scope associated with reservation recommendations operations. This includes
     * '/subscriptions/{subscriptionId}/' for subscription scope,
     * '/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}' for resource group scope,
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}' for BillingAccount scope, and
     * '/providers/Microsoft.Billing/billingAccounts/{billingAccountId}/billingProfiles/{billingProfileId}' for
     * billingProfile scope.
     * @param filter May be used to filter reservationRecommendations by: properties/scope with allowed values
     * ['Single', 'Shared'] and default value 'Single'; properties/resourceType with allowed values ['VirtualMachines',
     * 'SQLDatabases', 'PostgreSQL', 'ManagedDisk', 'MySQL', 'RedHat', 'MariaDB', 'RedisCache', 'CosmosDB',
     * 'SqlDataWarehouse', 'SUSELinux', 'AppService', 'BlockBlob', 'AzureDataExplorer', 'VMwareCloudSimple'] and default
     * value 'VirtualMachines'; and properties/lookBackPeriod with allowed values ['Last7Days', 'Last30Days',
     * 'Last60Days'] and default value 'Last7Days'.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ReservationRecommendationInner> list(String resourceScope, String filter, Context context) {
        return new PagedIterable<>(listAsync(resourceScope, filter, context));
    }

    /**
     * Get the next page of items.
     * 
     * @param nextLink The URL to get the next list of items.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations along with {@link PagedResponse} on successful completion
     * of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<PagedResponse<ReservationRecommendationInner>> listNextSinglePageAsync(String nextLink) {
        if (nextLink == null) {
            return Mono.error(new IllegalArgumentException("Parameter nextLink is required and cannot be null."));
        }
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        final String accept = "application/json";
        return FluxUtil.withContext(context -> service.listNext(nextLink, this.client.getEndpoint(), accept, context))
            .<PagedResponse<ReservationRecommendationInner>>map(res -> new PagedResponseBase<>(res.getRequest(),
                res.getStatusCode(), res.getHeaders(), res.getValue().value(), res.getValue().nextLink(), null))
            .contextWrite(context -> context.putAll(FluxUtil.toReactorContext(this.client.getContext()).readOnly()));
    }

    /**
     * Get the next page of items.
     * 
     * @param nextLink The URL to get the next list of items.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ManagementException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return result of listing reservation recommendations along with {@link PagedResponse} on successful completion
     * of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<PagedResponse<ReservationRecommendationInner>> listNextSinglePageAsync(String nextLink,
        Context context) {
        if (nextLink == null) {
            return Mono.error(new IllegalArgumentException("Parameter nextLink is required and cannot be null."));
        }
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        final String accept = "application/json";
        context = this.client.mergeContext(context);
        return service.listNext(nextLink, this.client.getEndpoint(), accept, context)
            .map(res -> new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
                res.getValue().value(), res.getValue().nextLink(), null));
    }
}
