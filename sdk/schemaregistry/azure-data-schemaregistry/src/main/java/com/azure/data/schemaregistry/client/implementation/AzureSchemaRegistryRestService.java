// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.data.schemaregistry.client.implementation.models.CreateGroupResponse;
import com.azure.data.schemaregistry.client.implementation.models.CreateSchemaResponse;
import com.azure.data.schemaregistry.client.implementation.models.GetIdBySchemaContentResponse;
import com.azure.data.schemaregistry.client.implementation.models.GetLatestSchemaResponse;
import com.azure.data.schemaregistry.client.implementation.models.GetSchemaByIdResponse;
import com.azure.data.schemaregistry.client.implementation.models.GetSchemaVersionResponse;
import com.azure.data.schemaregistry.client.implementation.models.GetSchemaVersionsResponse;
import com.azure.data.schemaregistry.client.implementation.models.GetSchemasByGroupResponse;
import com.azure.data.schemaregistry.client.implementation.models.SchemaGroup;
import com.azure.data.schemaregistry.client.implementation.models.SchemaId;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Initializes a new instance of the AzureSchemaRegistryRestService type.
 */
public final class AzureSchemaRegistryRestService {
    /**
     * The proxy service used to perform REST calls.
     */
    private final AzureSchemaRegistryRestServiceService service;

    /**
     * server parameter.
     */
    private String host;

    /**
     * Gets server parameter.
     *
     * @return the host value.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Sets server parameter.
     *
     * @param host the host value.
     * @return the service client itself.
     */
    public AzureSchemaRegistryRestService setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * The HTTP pipeline to send requests through.
     */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Initializes an instance of AzureSchemaRegistryRestService client.
     */
    public AzureSchemaRegistryRestService() {
        this(new HttpPipelineBuilder().policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy()).build());
    }

    /**
     * Initializes an instance of AzureSchemaRegistryRestService client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     */
    public AzureSchemaRegistryRestService(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.service = RestProxy.create(AzureSchemaRegistryRestServiceService.class, this.httpPipeline);
    }

    /**
     * The interface defining all the services for AzureSchemaRegistryRestService to be used by the proxy service to
     * perform REST calls.
     */
    @Host("{$host}")
    @ServiceInterface(name = "AzureSchemaRegistryR")
    private interface AzureSchemaRegistryRestServiceService {
        @Get("/$schemagroups")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<SimpleResponse<List<String>>> getGroups(@HostParam("$host") String host, Context context);

        @Get("/$schemagroups/getSchemaById/{schema-id}")
        @ExpectedResponses({200, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<GetSchemaByIdResponse> getSchemaById(
            @HostParam("$host") String host, @PathParam("schema-id") UUID schemaId, Context context);

        @Get("/$schemagroups/{group-name}")
        @ExpectedResponses({200, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<SimpleResponse<SchemaGroup>> getGroup(
            @HostParam("$host") String host, @PathParam("group-name") String groupName, Context context);

        @Put("/$schemagroups/{group-name}")
        @ExpectedResponses({201, 409})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<CreateGroupResponse> createGroup(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @BodyParam("application/json") SchemaGroup body,
            Context context);

        @Delete("/$schemagroups/{group-name}")
        @ExpectedResponses({204, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> deleteGroup(
            @HostParam("$host") String host, @PathParam("group-name") String groupName, Context context);

        @Get("/$schemagroups/{group-name}/schemas")
        @ExpectedResponses({200, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<GetSchemasByGroupResponse> getSchemasByGroup(
            @HostParam("$host") String host, @PathParam("group-name") String groupName, Context context);

        @Delete("/$schemagroups/{group-name}/schemas")
        @ExpectedResponses({204, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> deleteSchemasByGroup(
            @HostParam("$host") String host, @PathParam("group-name") String groupName, Context context);

        @Post("/$schemagroups/{group-name}/schemas/{schema-name}")
        @ExpectedResponses({200, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<GetIdBySchemaContentResponse> getIdBySchemaContent(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @PathParam("schema-name") String schemaName,
            @HeaderParam("X-Schema-Type") String xSchemaType,
            @BodyParam("application/json") String body,
            Context context);

        @Put("/$schemagroups/{group-name}/schemas/{schema-name}")
        @ExpectedResponses({200, 400})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<CreateSchemaResponse> createSchema(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @PathParam("schema-name") String schemaName,
            @HeaderParam("X-Schema-Type") String xSchemaType,
            @BodyParam("application/json") String body,
            Context context);

        @Get("/$schemagroups/{group-name}/schemas/{schema-name}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<GetLatestSchemaResponse> getLatestSchema(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @PathParam("schema-name") String schemaName,
            Context context);

        @Delete("/$schemagroups/{group-name}/schemas/{schema-name}")
        @ExpectedResponses({204, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> deleteSchema(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @PathParam("schema-name") String schemaName,
            Context context);

        @Get("/$schemagroups/{group-name}/schemas/{schema-name}/versions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<GetSchemaVersionsResponse> getSchemaVersions(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @PathParam("schema-name") String schemaName,
            Context context);

        @Get("/$schemagroups/{group-name}/schemas/{schema-name}/versions/{version-number}")
        @ExpectedResponses({200, 404})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<GetSchemaVersionResponse> getSchemaVersion(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @PathParam("schema-name") String schemaName,
            @PathParam("version-number") int versionNumber,
            Context context);

        @Delete("/$schemagroups/{group-name}/schemas/{schema-name}/versions/{version-number}")
        @ExpectedResponses({204})
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<Void>> deleteSchemaVersion(
            @HostParam("$host") String host,
            @PathParam("group-name") String groupName,
            @PathParam("schema-name") String schemaName,
            @PathParam("version-number") int versionNumber,
            Context context);
    }

    /**
     * Get all schema groups in namespace.
     *
     * @return all schema groups in namespace.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<List<String>>> getGroupsWithResponseAsync() {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getGroups(this.getHost(), context));
    }

    /**
     * Get all schema groups in namespace.
     *
     * @param context The context to associate with this operation.
     * @return all schema groups in namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<List<String>>> getGroupsWithResponseAsync(Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        return service.getGroups(this.getHost(), context);
    }

    /**
     * Get all schema groups in namespace.
     *
     * @return all schema groups in namespace.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<String>> getGroupsAsync() {
        return getGroupsWithResponseAsync()
            .flatMap(
                (SimpleResponse<List<String>> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get all schema groups in namespace.
     *
     * @return all schema groups in namespace.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<String> getGroups() {
        return getGroupsAsync().block();
    }

    /**
     * Get schema by schema ID.
     *
     * @param schemaId schema ID referencing specific schema in registry namespace.
     * @return schema by schema ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemaByIdResponse> getSchemaByIdWithResponseAsync(UUID schemaId) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (schemaId == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaId is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getSchemaById(this.getHost(), schemaId, context));
    }

    /**
     * Get schema by schema ID.
     *
     * @param schemaId schema ID referencing specific schema in registry namespace.
     * @param context The context to associate with this operation.
     * @return schema by schema ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemaByIdResponse> getSchemaByIdWithResponseAsync(UUID schemaId, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (schemaId == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaId is required and cannot be null."));
        }
        return service.getSchemaById(this.getHost(), schemaId, context);
    }

    /**
     * Get schema by schema ID.
     *
     * @param schemaId schema ID referencing specific schema in registry namespace.
     * @return schema by schema ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getSchemaByIdAsync(UUID schemaId) {
        return getSchemaByIdWithResponseAsync(schemaId)
            .flatMap(
                (GetSchemaByIdResponse res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get schema by schema ID.
     *
     * @param schemaId schema ID referencing specific schema in registry namespace.
     * @return schema by schema ID.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getSchemaById(UUID schemaId) {
        return getSchemaByIdAsync(schemaId).block();

    }

    /**
     * Get schema group description in registry namespace.
     *
     * @param groupName schema group.
     * @return schema group description in registry namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<SchemaGroup>> getGroupWithResponseAsync(String groupName) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getGroup(this.getHost(), groupName, context));
    }

    /**
     * Get schema group description in registry namespace.
     *
     * @param groupName schema group.
     * @param context The context to associate with this operation.
     * @return schema group description in registry namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<SchemaGroup>> getGroupWithResponseAsync(String groupName, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return service.getGroup(this.getHost(), groupName, context);
    }

    /**
     * Get schema group description in registry namespace.
     *
     * @param groupName schema group.
     * @return schema group description in registry namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaGroup> getGroupAsync(String groupName) {
        return getGroupWithResponseAsync(groupName)
            .flatMap(
                (SimpleResponse<SchemaGroup> res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get schema group description in registry namespace.
     *
     * @param groupName schema group.
     * @return schema group description in registry namespace.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaGroup getGroup(String groupName) {
        return getGroupAsync(groupName).block();
    }

    /**
     * Create schema group with specified schema type in registry namespace.
     *
     * @param groupName schema group.
     * @param body schema group description.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateGroupResponse> createGroupWithResponseAsync(String groupName, SchemaGroup body) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (body == null) {
            return Mono.error(new IllegalArgumentException("Parameter body is required and cannot be null."));
        } else {
            body.validate();
        }
        return FluxUtil.withContext(context -> service.createGroup(this.getHost(), groupName, body, context));
    }

    /**
     * Create schema group with specified schema type in registry namespace.
     *
     * @param groupName schema group.
     * @param body schema group description.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateGroupResponse> createGroupWithResponseAsync(String groupName, SchemaGroup body, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (body == null) {
            return Mono.error(new IllegalArgumentException("Parameter body is required and cannot be null."));
        } else {
            body.validate();
        }
        return service.createGroup(this.getHost(), groupName, body, context);
    }

    /**
     * Create schema group with specified schema type in registry namespace.
     *
     * @param groupName schema group.
     * @param body schema group description.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createGroupAsync(String groupName, SchemaGroup body) {
        return createGroupWithResponseAsync(groupName, body).flatMap((CreateGroupResponse res) -> Mono.empty());
    }

    /**
     * Create schema group with specified schema type in registry namespace.
     *
     * @param groupName schema group.
     * @param body schema group description.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void createGroup(String groupName, SchemaGroup body) {
        createGroupAsync(groupName, body).block();
    }

    /**
     * Delete schema group in schema registry namespace.
     *
     * @param groupName schema group.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGroupWithResponseAsync(String groupName) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.deleteGroup(this.getHost(), groupName, context));
    }

    /**
     * Delete schema group in schema registry namespace.
     *
     * @param groupName schema group.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteGroupWithResponseAsync(String groupName, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return service.deleteGroup(this.getHost(), groupName, context);
    }

    /**
     * Delete schema group in schema registry namespace.
     *
     * @param groupName schema group.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteGroupAsync(String groupName) {
        return deleteGroupWithResponseAsync(groupName).flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete schema group in schema registry namespace.
     *
     * @param groupName schema group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteGroup(String groupName) {
        deleteGroupAsync(groupName).block();
    }

    /**
     * Returns schema by group name.
     *
     * @param groupName schema group.
     * @return array of String.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemasByGroupResponse> getSchemasByGroupWithResponseAsync(String groupName) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getSchemasByGroup(this.getHost(), groupName, context));
    }

    /**
     * Returns schema by group name.
     *
     * @param groupName schema group.
     * @param context The context to associate with this operation.
     * @return array of String.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemasByGroupResponse> getSchemasByGroupWithResponseAsync(String groupName, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return service.getSchemasByGroup(this.getHost(), groupName, context);
    }

    /**
     * Returns schema by group name.
     *
     * @param groupName schema group.
     * @return array of String.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<String>> getSchemasByGroupAsync(String groupName) {
        return getSchemasByGroupWithResponseAsync(groupName)
            .flatMap(
                (GetSchemasByGroupResponse res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Returns schema by group name.
     *
     * @param groupName schema group.
     * @return array of String.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<String> getSchemasByGroup(String groupName) {
        return getSchemasByGroupAsync(groupName).block();
    }

    /**
     * Deletes all schemas under specified group name.
     *
     * @param groupName schema group.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSchemasByGroupWithResponseAsync(String groupName) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.deleteSchemasByGroup(this.getHost(), groupName, context));
    }

    /**
     * Deletes all schemas under specified group name.
     *
     * @param groupName schema group.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSchemasByGroupWithResponseAsync(String groupName, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        return service.deleteSchemasByGroup(this.getHost(), groupName, context);
    }

    /**
     * Deletes all schemas under specified group name.
     *
     * @param groupName schema group.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSchemasByGroupAsync(String groupName) {
        return deleteSchemasByGroupWithResponseAsync(groupName).flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Deletes all schemas under specified group name.
     *
     * @param groupName schema group.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSchemasByGroup(String groupName) {
        deleteSchemasByGroupAsync(groupName).block();
    }

    /**
     * Get ID for schema with matching byte content and schema type.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @return iD for schema with matching byte content and schema type.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetIdBySchemaContentResponse> getIdBySchemaContentWithResponseAsync(
        String groupName, String schemaName, String xSchemaType, String body) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
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
        if (body == null) {
            return Mono.error(new IllegalArgumentException("Parameter body is required and cannot be null."));
        }
        return FluxUtil.withContext(
            context ->
                service.getIdBySchemaContent(
                    this.getHost(), groupName, schemaName, xSchemaType, body, context));
    }

    /**
     * Get ID for schema with matching byte content and schema type.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @param context The context to associate with this operation.
     * @return iD for schema with matching byte content and schema type.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetIdBySchemaContentResponse> getIdBySchemaContentWithResponseAsync(
        String groupName, String schemaName, String xSchemaType, String body, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
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
        if (body == null) {
            return Mono.error(new IllegalArgumentException("Parameter body is required and cannot be null."));
        }
        return service.getIdBySchemaContent(this.getHost(), groupName, schemaName, xSchemaType, body, context);
    }

    /**
     * Get ID for schema with matching byte content and schema type.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @return iD for schema with matching byte content and schema type.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaId> getIdBySchemaContentAsync(
        String groupName, String schemaName, String xSchemaType, String body) {
        return getIdBySchemaContentWithResponseAsync(groupName, schemaName, xSchemaType, body)
            .flatMap(
                (GetIdBySchemaContentResponse res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get ID for schema with matching byte content and schema type.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @return iD for schema with matching byte content and schema type.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaId getIdBySchemaContent(String groupName, String schemaName, String xSchemaType, String body) {
        return getIdBySchemaContentAsync(groupName, schemaName, xSchemaType, body).block();
    }

    /**
     * Register schema. If schema of specified name does not exist in specified group, schema is created at version 1.
     * If schema of specified name exists already in specified group, schema is created at latest version + 1. If schema
     * with identical content already exists, existing schema's ID is returned.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @return the response.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateSchemaResponse> createSchemaWithResponseAsync(
        String groupName, String schemaName, String xSchemaType, String body) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
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
        if (body == null) {
            return Mono.error(new IllegalArgumentException("Parameter body is required and cannot be null."));
        }
        return FluxUtil.withContext(
            context -> service.createSchema(this.getHost(), groupName, schemaName, xSchemaType, body, context));
    }

    /**
     * Register schema. If schema of specified name does not exist in specified group, schema is created at version 1.
     * If schema of specified name exists already in specified group, schema is created at latest version + 1. If schema
     * with identical content already exists, existing schema's ID is returned.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @param context The context to associate with this operation.
     * @return the response.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateSchemaResponse> createSchemaWithResponseAsync(
        String groupName, String schemaName, String xSchemaType, String body, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
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
        if (body == null) {
            return Mono.error(new IllegalArgumentException("Parameter body is required and cannot be null."));
        }
        return service.createSchema(this.getHost(), groupName, schemaName, xSchemaType, body, context);
    }

    /**
     * Register schema. If schema of specified name does not exist in specified group, schema is created at version 1.
     * If schema of specified name exists already in specified group, schema is created at latest version + 1. If schema
     * with identical content already exists, existing schema's ID is returned.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @return the response.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SchemaId> createSchemaAsync(String groupName, String schemaName, String xSchemaType, String body) {
        return createSchemaWithResponseAsync(groupName, schemaName, xSchemaType, body)
            .flatMap(
                (CreateSchemaResponse res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Register schema. If schema of specified name does not exist in specified group, schema is created at version 1.
     * If schema of specified name exists already in specified group, schema is created at latest version + 1. If schema
     * with identical content already exists, existing schema's ID is returned.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param xSchemaType The xSchemaType parameter.
     * @param body schema content.
     * @return the response.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SchemaId createSchema(String groupName, String schemaName, String xSchemaType, String body) {
        return createSchemaAsync(groupName, schemaName, xSchemaType, body).block();
    }

    /**
     * Get latest version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return latest version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetLatestSchemaResponse> getLatestSchemaWithResponseAsync(String groupName, String schemaName) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.getLatestSchema(this.getHost(), groupName, schemaName, context));
    }

    /**
     * Get latest version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param context The context to associate with this operation.
     * @return latest version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetLatestSchemaResponse> getLatestSchemaWithResponseAsync(
        String groupName, String schemaName, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return service.getLatestSchema(this.getHost(), groupName, schemaName, context);
    }

    /**
     * Get latest version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return latest version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getLatestSchemaAsync(String groupName, String schemaName) {
        return getLatestSchemaWithResponseAsync(groupName, schemaName)
            .flatMap(
                (GetLatestSchemaResponse res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get latest version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return latest version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getLatestSchema(String groupName, String schemaName) {
        return getLatestSchemaAsync(groupName, schemaName).block();
    }

    /**
     * Delete schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSchemaWithResponseAsync(String groupName, String schemaName) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return FluxUtil.withContext(context -> service.deleteSchema(this.getHost(), groupName, schemaName, context));
    }

    /**
     * Delete schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSchemaWithResponseAsync(String groupName, String schemaName, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return service.deleteSchema(this.getHost(), groupName, schemaName, context);
    }

    /**
     * Delete schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSchemaAsync(String groupName, String schemaName) {
        return deleteSchemaWithResponseAsync(groupName, schemaName).flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSchema(String groupName, String schemaName) {
        deleteSchemaAsync(groupName, schemaName).block();
    }

    /**
     * Get list of versions for specified schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return list of versions for specified schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemaVersionsResponse> getSchemaVersionsWithResponseAsync(String groupName, String schemaName) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return FluxUtil.withContext(
            context -> service.getSchemaVersions(this.getHost(), groupName, schemaName, context));
    }

    /**
     * Get list of versions for specified schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param context The context to associate with this operation.
     * @return list of versions for specified schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemaVersionsResponse> getSchemaVersionsWithResponseAsync(
        String groupName, String schemaName, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return service.getSchemaVersions(this.getHost(), groupName, schemaName, context);
    }

    /**
     * Get list of versions for specified schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return list of versions for specified schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<Integer>> getSchemaVersionsAsync(String groupName, String schemaName) {
        return getSchemaVersionsWithResponseAsync(groupName, schemaName)
            .flatMap(
                (GetSchemaVersionsResponse res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get list of versions for specified schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @return list of versions for specified schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<Integer> getSchemaVersions(String groupName, String schemaName) {
        return getSchemaVersionsAsync(groupName, schemaName).block();
    }

    /**
     * Get specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @return specified version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemaVersionResponse> getSchemaVersionWithResponseAsync(
        String groupName, String schemaName, int versionNumber) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return FluxUtil.withContext(
            context -> service.getSchemaVersion(this.getHost(), groupName, schemaName, versionNumber, context));
    }

    /**
     * Get specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @param context The context to associate with this operation.
     * @return specified version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GetSchemaVersionResponse> getSchemaVersionWithResponseAsync(
        String groupName, String schemaName, int versionNumber, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return service.getSchemaVersion(this.getHost(), groupName, schemaName, versionNumber, context);
    }

    /**
     * Get specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @return specified version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getSchemaVersionAsync(String groupName, String schemaName, int versionNumber) {
        return getSchemaVersionWithResponseAsync(groupName, schemaName, versionNumber)
            .flatMap(
                (GetSchemaVersionResponse res) -> {
                    if (res.getValue() != null) {
                        return Mono.just(res.getValue());
                    } else {
                        return Mono.empty();
                    }
                });
    }

    /**
     * Get specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @return specified version of schema.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public String getSchemaVersion(String groupName, String schemaName, int versionNumber) {
        return getSchemaVersionAsync(groupName, schemaName, versionNumber).block();
    }

    /**
     * Delete specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSchemaVersionWithResponseAsync(
        String groupName, String schemaName, int versionNumber) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return FluxUtil.withContext(
            context -> service.deleteSchemaVersion(this.getHost(), groupName, schemaName, versionNumber, context));
    }

    /**
     * Delete specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @param context The context to associate with this operation.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSchemaVersionWithResponseAsync(
        String groupName, String schemaName, int versionNumber, Context context) {
        if (this.getHost() == null) {
            return Mono.error(new IllegalArgumentException("Parameter this.getHost() is required and cannot be null."));
        }
        if (groupName == null) {
            return Mono.error(new IllegalArgumentException("Parameter groupName is required and cannot be null."));
        }
        if (schemaName == null) {
            return Mono.error(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
        }
        return service.deleteSchemaVersion(this.getHost(), groupName, schemaName, versionNumber, context);
    }

    /**
     * Delete specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @return the completion.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSchemaVersionAsync(String groupName, String schemaName, int versionNumber) {
        return deleteSchemaVersionWithResponseAsync(groupName, schemaName, versionNumber)
            .flatMap((Response<Void> res) -> Mono.empty());
    }

    /**
     * Delete specified version of schema.
     *
     * @param groupName schema group.
     * @param schemaName schema name.
     * @param versionNumber version number.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSchemaVersion(String groupName, String schemaName, int versionNumber) {
        deleteSchemaVersionAsync(groupName, schemaName, versionNumber).block();
    }
}
