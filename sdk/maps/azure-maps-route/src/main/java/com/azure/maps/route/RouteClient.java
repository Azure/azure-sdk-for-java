// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import java.util.List;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.route.models.ErrorResponseException;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeOptions;
import com.azure.maps.route.models.RouteRangeResult;

/** Initializes a new instance of the synchronous RouteClient type. */
@ServiceClient(builder = RouteClientBuilder.class)
public final class RouteClient {
    @Generated private final RouteAsyncClient asyncClient;

    /**
     * Initializes an instance of Routes client.
     *
     * @param serviceClient the service client implementation.
     */
    RouteClient(RouteAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Request Route Matrix
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginRequestRouteMatrix(RouteMatrixOptions options) {
        return this.asyncClient.beginRequestRouteMatrix(options).getSyncPoller();
    }

    /**
     * Request Route Matrix
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginRequestRouteMatrix(
            RouteMatrixOptions options,
            Context context) {
        return this.asyncClient.beginRequestRouteMatrix(options, context).getSyncPoller();
    }

    /**
     * Get Route Matrix Batch
     *
     * @param matrixId Matrix id received after the Matrix Route request was accepted successfully.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(String matrixId) {
        return this.asyncClient.beginGetRouteMatrix(matrixId).getSyncPoller();
    }

    /**
     * Get Route Matrix Batch
     *
     * @param matrixId Matrix id received after the Matrix Route request was accepted successfully.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(String matrixId, Context context) {
        return this.asyncClient.beginGetRouteMatrix(matrixId, context).getSyncPoller();
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouteDirections getRouteDirections(RouteDirectionsOptions options) {
        return this.asyncClient.getRouteDirections(options).block();
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouteDirections> getRouteDirectionsWithResponse(
            RouteDirectionsOptions options, Context context) {
        return this.asyncClient.getRouteDirectionsWithResponse(options, context).block();
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouteDirections getRouteDirectionsWithAdditionalParameters(
            RouteDirectionsOptions options, RouteDirectionsParameters parameters) {
        return this.asyncClient.getRouteDirectionsWithAdditionalParameters(options, parameters).block();
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouteDirections> getRouteDirectionsWithAdditionalParametersWithResponse(
            RouteDirectionsOptions options, RouteDirectionsParameters parameters, Context context) {
        return this.asyncClient.getRouteDirectionsWithAdditionalParametersWithResponse(
            options, parameters, context).block();
    }

    /**
     * Get Route Range
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouteRangeResult getRouteRange(RouteRangeOptions options) {
        return this.asyncClient.getRouteRange(options).block();
    }

    /**
     * Get Route Range
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouteRangeResult> getRouteRangeWithResponse(
            RouteRangeOptions options, Context context) {
        return this.asyncClient.getRouteRangeWithResponse(options, context).block();
    }

    /**
     * Get Route Directions Batch
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult>
            beginRequestRouteDirectionsBatch(List<RouteDirectionsOptions> optionsList) {
        return this.beginRequestRouteDirectionsBatch(optionsList, null);
    }

    /**
     * Get Route Directions Batch
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult>
            beginRequestRouteDirectionsBatch(List<RouteDirectionsOptions> optionsList, Context context) {
        return this.asyncClient
            .beginRequestRouteDirectionsBatch(optionsList, context).getSyncPoller();
    }

    /**
     * Get Route Directions Batch Id
     *
     * @param batchId the batch id received from a previous call to route directions batch.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginGetRouteDirectionsBatch(
            String batchId) {
        return this.beginGetRouteDirectionsBatch(batchId, null);
    }

    /**
     * Get Route Directions Batch Id
     *
     * @param batchId the batch id received from a previous call to route directions batch.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginGetRouteDirectionsBatch(
            String batchId, Context context) {
        return this.asyncClient.beginGetRouteDirectionsBatch(batchId, context).getSyncPoller();
    }
}
