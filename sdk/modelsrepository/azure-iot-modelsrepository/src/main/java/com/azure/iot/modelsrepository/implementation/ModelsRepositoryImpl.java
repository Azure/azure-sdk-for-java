// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.annotation.*;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.implementation.models.ErrorResponseException;
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to all the operations defined in ModelsRepository.
 */
public final class ModelsRepositoryImpl {
    /**
     * The proxy service used to perform REST calls.
     */
    private final ModelsRepositoryService service;

    /**
     * The service client containing this operation class.
     */
    private final ModelsRepositoryAPIImpl client;

    /**
     * Initializes an instance of ModelsRepositoryImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    ModelsRepositoryImpl(ModelsRepositoryAPIImpl client) {
        this.service =
            RestProxy.create(ModelsRepositoryService.class, client.getHttpPipeline());
        this.client = client;
    }

    /**
     * Retrieves a dtmi model. Status codes: * 200 OK * 400 Bad Request * InvalidArgument
     *
     * @param path    The path of the dtmi. The id is unique within the service and case sensitive.
     * @param context The context to associate with this operation.
     * @return any object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws RuntimeException         all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<byte[]> getModelFromPathWithResponseAsync(
        String path, Context context) {
        if (this.client.getHost() == null) {
            return Mono.error(
                new IllegalArgumentException("Parameter this.client.getHost() is required and cannot be null."));
        }
        if (path == null) {
            return Mono.error(new IllegalArgumentException("Parameter 'path' is required and cannot be null."));
        }

        return service.getModelFromPath(
            this.client.getHost(), path, context);
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
        @UnexpectedResponseExceptionType(ErrorResponseException.class)
        Mono<byte[]> getModelFromPath(
            @HostParam("$host") String host,
            @PathParam("path") String path,
            Context context);
    }
}
