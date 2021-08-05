// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
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
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.data.schemaregistry.implementation.models.SchemaId;
import com.azure.data.schemaregistry.implementation.models.SchemasQueryIdByContentResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasGetByIdResponse;
import com.azure.data.schemaregistry.implementation.models.SchemasRegisterResponse;
import com.azure.data.schemaregistry.implementation.models.SerializationType;
import com.azure.data.schemaregistry.implementation.models.ServiceErrorResponseException;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in Schemas. */
public final class Schemas {
    /** The proxy service used to perform REST calls. */
    private final SchemasService service;

    /** The service client containing this operation class. */
    private final AzureSchemaRegistry client;

    /**
     * Initializes an instance of Schemas.
     *
     * @param client the instance of the service client containing this operation class.
     */
    Schemas(AzureSchemaRegistry client) {
        this.service = RestProxy.create(SchemasService.class, client.getHttpPipeline(), client.getSerializerAdapter());
        this.client = client;
    }

    /**
     * The interface defining all the services for AzureSchemaRegistrySchemas to be used by the proxy service to perform
     * REST calls.
     */
    @Host("https://{endpoint}")
    @ServiceInterface(name = "AzureSchemaRegistryS")
    private interface SchemasService {
        @Get("/$schemagroups/getSchemaById/{schema-id}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ServiceErrorResponseException.class)
        Mono<SchemasGetByIdResponse> getById(
                @HostParam("endpoint") String endpoint,
                @PathParam("schema-id") String schemaId,
                @QueryParam("api-version") String apiVersion,
                Context context);

        @Post("/$schemagroups/{group-name}/schemas/{schema-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ServiceErrorResponseException.class)
        Mono<SchemasQueryIdByContentResponse> queryIdByContent(
                @HostParam("endpoint") String endpoint,
                @PathParam("group-name") String groupName,
                @PathParam("schema-name") String schemaName,
                @HeaderParam("Serialization-Type") SerializationType xSchemaType,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") String schemaContent,
                Context context);

        @Put("/$schemagroups/{group-name}/schemas/{schema-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(ServiceErrorResponseException.class)
        Mono<SchemasRegisterResponse> register(
                @HostParam("endpoint") String endpoint,
                @PathParam("group-name") String groupName,
                @PathParam("schema-name") String schemaName,
                @HeaderParam("Serialization-Type") SerializationType xSchemaType,
                @QueryParam("api-version") String apiVersion,
                @BodyParam("application/json") String schemaContent,
                Context context);
    }

    /**
     * Gets a registered schema by its unique ID. Azure Schema Registry guarantees that ID is unique within a namespace.
     *
     * @param schemaId References specific schema in registry namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a registered schema by its unique ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemasGetByIdResponse> getByIdWithResponseAsync(String schemaId) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (schemaId == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaId is required and cannot be null."));
        }
        return FluxUtil.withContext(
                context -> service.getById(this.client.getEndpoint(), schemaId, this.client.getApiVersion(), context));
    }

    /**
     * Gets a registered schema by its unique ID. Azure Schema Registry guarantees that ID is unique within a namespace.
     *
     * @param schemaId References specific schema in registry namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a registered schema by its unique ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getByIdAsync(String schemaId) {
        return getByIdWithResponseAsync(schemaId)
                .flatMap(
                        (SchemasGetByIdResponse res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Gets a registered schema by its unique ID. Azure Schema Registry guarantees that ID is unique within a namespace.
     *
     * @param schemaId References specific schema in registry namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a registered schema by its unique ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getById(String schemaId) {
        return getByIdAsync(schemaId).block();
    }

    /**
     * Gets the ID referencing an existing schema within the specified schema group, as matched by schema content
     * comparison.
     *
     * @param groupName Schema group under which schema is registered. Group's serialization type should match the
     *     serialization type specified in the request.
     * @param schemaName Name of the registered schema.
     * @param xSchemaType Serialization type for the schema being registered.
     * @param schemaContent String representation of the registered schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the ID referencing an existing schema within the specified schema group, as matched by schema content
     *     comparison.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemasQueryIdByContentResponse> queryIdByContentWithResponseAsync(
            String groupName, String schemaName, SerializationType xSchemaType, String schemaContent) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        if (xSchemaType == null) {
            return Mono.error(new IllegalArgumentException("Parameter xSchemaType is required and cannot be null."));
        }
        if (schemaContent == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaContent is required and cannot be null."));
        }
        return FluxUtil.withContext(
                context ->
                        service.queryIdByContent(
                                this.client.getEndpoint(),
                                groupName,
                                schemaName,
                                xSchemaType,
                                this.client.getApiVersion(),
                                schemaContent,
                                context));
    }

    /**
     * Gets the ID referencing an existing schema within the specified schema group, as matched by schema content
     * comparison.
     *
     * @param groupName Schema group under which schema is registered. Group's serialization type should match the
     *     serialization type specified in the request.
     * @param schemaName Name of the registered schema.
     * @param xSchemaType Serialization type for the schema being registered.
     * @param schemaContent String representation of the registered schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the ID referencing an existing schema within the specified schema group, as matched by schema content
     *     comparison.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaId> queryIdByContentAsync(
            String groupName, String schemaName, SerializationType xSchemaType, String schemaContent) {
        return queryIdByContentWithResponseAsync(groupName, schemaName, xSchemaType, schemaContent)
                .flatMap(
                        (SchemasQueryIdByContentResponse res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Gets the ID referencing an existing schema within the specified schema group, as matched by schema content
     * comparison.
     *
     * @param groupName Schema group under which schema is registered. Group's serialization type should match the
     *     serialization type specified in the request.
     * @param schemaName Name of the registered schema.
     * @param xSchemaType Serialization type for the schema being registered.
     * @param schemaContent String representation of the registered schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the ID referencing an existing schema within the specified schema group, as matched by schema content
     *     comparison.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaId queryIdByContent(
            String groupName, String schemaName, SerializationType xSchemaType, String schemaContent) {
        return queryIdByContentAsync(groupName, schemaName, xSchemaType, schemaContent).block();
    }

    /**
     * Register new schema. If schema of specified name does not exist in specified group, schema is created at version
     * 1. If schema of specified name exists already in specified group, schema is created at latest version + 1.
     *
     * @param groupName Schema group under which schema should be registered. Group's serialization type should match
     *     the serialization type specified in the request.
     * @param schemaName Name of schema being registered.
     * @param xSchemaType Serialization type for the schema being registered.
     * @param schemaContent String representation of the schema being registered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return jSON Object received from the registry containing schema identifiers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemasRegisterResponse> registerWithResponseAsync(
            String groupName, String schemaName, SerializationType xSchemaType, String schemaContent) {
        if (this.client.getEndpoint() == null) {
            return Mono.error(
                    new IllegalArgumentException(
                            "Parameter this.client.getEndpoint() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        if (xSchemaType == null) {
            return Mono.error(new IllegalArgumentException("Parameter xSchemaType is required and cannot be null."));
        }
        if (schemaContent == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaContent is required and cannot be null."));
        }
        return FluxUtil.withContext(
                context ->
                        service.register(
                                this.client.getEndpoint(),
                                groupName,
                                schemaName,
                                xSchemaType,
                                this.client.getApiVersion(),
                                schemaContent,
                                context));
    }

    /**
     * Register new schema. If schema of specified name does not exist in specified group, schema is created at version
     * 1. If schema of specified name exists already in specified group, schema is created at latest version + 1.
     *
     * @param groupName Schema group under which schema should be registered. Group's serialization type should match
     *     the serialization type specified in the request.
     * @param schemaName Name of schema being registered.
     * @param xSchemaType Serialization type for the schema being registered.
     * @param schemaContent String representation of the schema being registered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return jSON Object received from the registry containing schema identifiers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaId> registerAsync(
            String groupName, String schemaName, SerializationType xSchemaType, String schemaContent) {
        return registerWithResponseAsync(groupName, schemaName, xSchemaType, schemaContent)
                .flatMap(
                        (SchemasRegisterResponse res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Register new schema. If schema of specified name does not exist in specified group, schema is created at version
     * 1. If schema of specified name exists already in specified group, schema is created at latest version + 1.
     *
     * @param groupName Schema group under which schema should be registered. Group's serialization type should match
     *     the serialization type specified in the request.
     * @param schemaName Name of schema being registered.
     * @param xSchemaType Serialization type for the schema being registered.
     * @param schemaContent String representation of the schema being registered.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ServiceErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return jSON Object received from the registry containing schema identifiers.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaId register(String groupName, String schemaName, SerializationType xSchemaType, String schemaContent) {
        return registerAsync(groupName, schemaName, xSchemaType, schemaContent).block();
    }
}
