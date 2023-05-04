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

// TODO ideally we parametrise the version, but somehow it doesn't work
//@Host("{endpoint}/{api-version}")
@Host("{endpoint}/v1")
@ServiceInterface(name = "OpenAIClient")
interface OpenAIClientService extends OpenAIServiceBase {
    @Post("/embeddings")
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

    @Post("/completions")
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

    @Post("/chat/completions")
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



