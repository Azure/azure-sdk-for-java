// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in ModelsRepository. */
public final class ModelsRepositoryImpl {
    /** The proxy service used to perform REST calls. */
    private final ModelsRepositoryService service;

    /** The service client containing this operation class. */
    private final ModelsRepositoryAPIImpl client;

    /**
     * Initializes an instance of ModelsRepositoryImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    ModelsRepositoryImpl(ModelsRepositoryAPIImpl client) {
        this.service =
                RestProxy.create(ModelsRepositoryService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for ModelsRepositoryAPI to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "ModelsRepositoryAPI")
    private interface ModelsRepositoryService {
        @Get("{path}")
        @ExpectedResponses({200})
        Mono<String> getById(
                @HostParam("$host") String host,
                @HeaderParam("traceparent") String traceparent,
                @HeaderParam("tracestate") String tracestate,
                @PathParam("id") String id,
                @QueryParam("api-version") String apiVersion,
                Context context);
    }

    /**
     * Retrieves a dtmi model. Status codes: * 200 OK * 400 Bad Request * InvalidArgument
     *
     * @param path The path of the dtmi. The id is unique within the service and case sensitive.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getByIdWithResponseAsync(
            String path, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(
                    new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (path == null) {
            return Mono.error(new IllegalArgumentException("Parameter id is required and cannot be null."));
        }

        String traceparentInternal = null;
        String traceparent = traceparentInternal;
        String tracestateInternal = null;
        String tracestate = tracestateInternal;
        return service.getById(
                this.client.getHost(), traceparent, tracestate, path, this.client.getApiVersion(), context);
    }
}
