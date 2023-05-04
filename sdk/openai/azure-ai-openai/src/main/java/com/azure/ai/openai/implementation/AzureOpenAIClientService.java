// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.OpenAIServiceBase;
import com.azure.core.annotation.*;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for OpenAIClient to be used by the proxy service to perform REST calls.
 */
@Host("{endpoint}/openai")
@ServiceInterface(name = "OpenAIClient")
interface AzureOpenAIClientService extends OpenAIServiceBase {
    @Post("/deployments/{deploymentId}/embeddings")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(
        value = ClientAuthenticationException.class,
        code = {401})
    @UnexpectedResponseExceptionType(
        value = ResourceNotFoundException.class,
        code = {404})
    @UnexpectedResponseExceptionType(
        value = ResourceModifiedException.class,
        code = {409})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<BinaryData>> getEmbeddings(
        @HostParam("endpoint") String endpoint,
        @QueryParam("api-version") String apiVersion,
        @PathParam("deploymentId") String deploymentId,
        @HeaderParam("accept") String accept,
        @BodyParam("application/json") BinaryData embeddingsOptions,
        RequestOptions requestOptions,
        Context context);

    @Post("/deployments/{deploymentId}/completions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(
        value = ClientAuthenticationException.class,
        code = {401})
    @UnexpectedResponseExceptionType(
        value = ResourceNotFoundException.class,
        code = {404})
    @UnexpectedResponseExceptionType(
        value = ResourceModifiedException.class,
        code = {409})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<BinaryData>> getCompletions(
        @HostParam("endpoint") String endpoint,
        @QueryParam("api-version") String apiVersion,
        @PathParam("deploymentId") String deploymentId,
        @HeaderParam("accept") String accept,
        @BodyParam("application/json") BinaryData completionsOptions,
        RequestOptions requestOptions,
        Context context);

    @Post("/deployments/{deploymentId}/chat/completions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(
        value = ClientAuthenticationException.class,
        code = {401})
    @UnexpectedResponseExceptionType(
        value = ResourceNotFoundException.class,
        code = {404})
    @UnexpectedResponseExceptionType(
        value = ResourceModifiedException.class,
        code = {409})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<BinaryData>> getChatCompletions(
        @HostParam("endpoint") String endpoint,
        @QueryParam("api-version") String apiVersion,
        @PathParam("deploymentId") String deploymentId,
        @HeaderParam("accept") String accept,
        @BodyParam("application/json") BinaryData chatCompletionsOptions,
        RequestOptions requestOptions,
        Context context);
}
