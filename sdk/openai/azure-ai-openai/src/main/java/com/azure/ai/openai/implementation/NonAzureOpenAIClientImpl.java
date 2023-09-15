// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Post;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Implementation for calling Non-Azure OpenAI Service
 */
public final class NonAzureOpenAIClientImpl {
    /** The proxy service used to perform REST calls. */
    private final NonAzureOpenAIClientService service;

    /** The HTTP pipeline to send requests through. */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /** The serializer to serialize an object into a string. */
    private final SerializerAdapter serializerAdapter;

    /**
     * Gets The serializer to serialize an object into a string.
     *
     * @return the serializerAdapter value.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * This is the endpoint that non-azure OpenAI supports. Currently, it has only v1 version.
     */
    public static final String OPEN_AI_ENDPOINT = "https://api.openai.com/v1";

    /**
     * Mapper used to add the `modelId` into the request body for an nonAzure OpenAI request
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * Initializes an instance of OpenAIClient client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     */
    public NonAzureOpenAIClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.service = RestProxy.create(NonAzureOpenAIClientService.class, this.httpPipeline, this.getSerializerAdapter());
    }

    /**
     * The interface defining all the services for OpenAIClient to be used by the proxy service to perform REST calls.
     */
    @Host("{endpoint}")
    @ServiceInterface(name = "OpenAIClient")
    public interface NonAzureOpenAIClientService {
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
            @HeaderParam("accept") String accept,
            @BodyParam("application/json") BinaryData embeddingsOptions,
            RequestOptions requestOptions,
            Context context);

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
        Response<BinaryData> getEmbeddingsSync(
            @HostParam("endpoint") String endpoint,
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
            @HeaderParam("accept") String accept,
            @BodyParam("application/json") BinaryData completionsOptions,
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
        Response<BinaryData> getCompletionsSync(
            @HostParam("endpoint") String endpoint,
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
            @HeaderParam("accept") String accept,
            @BodyParam("application/json") BinaryData chatCompletionsOptions,
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
        Response<BinaryData> getChatCompletionsSync(
            @HostParam("endpoint") String endpoint,
            @HeaderParam("accept") String accept,
            @BodyParam("application/json") BinaryData chatCompletionsOptions,
            RequestOptions requestOptions,
            Context context);

        @Post("/images/generations")
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
        Mono<Response<BinaryData>> generateImage(
            @HostParam("endpoint") String endpoint,
            @HeaderParam("accept") String accept,
            @BodyParam("application/json") BinaryData imageGenerationOptions,
            RequestOptions requestOptions,
            Context context);

        @Post("/images/generations")
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
        Response<BinaryData> generateImageSync(
            @HostParam("endpoint") String endpoint,
            @HeaderParam("accept") String accept,
            @BodyParam("application/json") BinaryData imageGenerationOptions,
            RequestOptions requestOptions,
            Context context);

        @Post("/audio/transcriptions")
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
        Mono<Response<BinaryData>> getAudioTranscriptionAsResponseObject(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranscriptionOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/transcriptions")
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
        Response<BinaryData> getAudioTranscriptionAsResponseObjectSync(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranscriptionOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/transcriptions")
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
        Mono<Response<BinaryData>> getAudioTranscriptionAsPlainText(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranscriptionOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/transcriptions")
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
        Response<BinaryData> getAudioTranscriptionAsPlainTextSync(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranscriptionOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/translations")
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
        Mono<Response<BinaryData>> getAudioTranslationAsResponseObject(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranslationOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/translations")
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
        Response<BinaryData> getAudioTranslationAsResponseObjectSync(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranslationOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/translations")
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
        Mono<Response<BinaryData>> getAudioTranslationAsPlainText(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranslationOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/translations")
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
        Response<BinaryData> getAudioTranslationAsPlainTextSync(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("multipart/form-data") BinaryData audioTranslationOptions,
                RequestOptions requestOptions,
                Context context);
    }

    /**
     * Return the embeddings for a given prompt.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     user: String (Optional)
     *     model: String (Optional)
     *     input (Required): [
     *         String (Required)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     data (Required): [
     *          (Required){
     *             embedding (Required): [
     *                 double (Required)
     *             ]
     *             index: int (Required)
     *         }
     *     ]
     *     usage (Required): {
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param modelId           id of the deployed model.
     * @param embeddingsOptions The configuration information for an embeddings request. Embeddings measure the
     *                          relatedness of text strings and are commonly used for search, clustering,
     *                          recommendations, and other similar scenarios.
     * @param requestOptions    The options to configure the HTTP request before HTTP client sends it.
     * @return representation of the response data from an embeddings request. Embeddings measure the relatedness of
     * text strings and are commonly used for search, clustering, recommendations, and other similar scenarios along
     * with {@link Response} on successful completion of {@link Mono}.
     * @throws HttpResponseException         thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException     thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException     thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getEmbeddingsWithResponseAsync(String modelId,
        BinaryData embeddingsOptions, RequestOptions requestOptions) {
        final String accept = "application/json";

        // modelId is part of the request body in nonAzure OpenAI
        try {
            BinaryData embeddingsOptionsUpdated = addModelIdJson(embeddingsOptions, modelId);
            return FluxUtil.withContext(
                context ->
                    service.getEmbeddings(
                        OPEN_AI_ENDPOINT,
                        accept,
                        embeddingsOptionsUpdated,
                        requestOptions,
                        context));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    /**
     * Return the embeddings for a given prompt.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     user: String (Optional)
     *     model: String (Optional)
     *     input (Required): [
     *         String (Required)
     *     ]
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     data (Required): [
     *          (Required){
     *             embedding (Required): [
     *                 double (Required)
     *             ]
     *             index: int (Required)
     *         }
     *     ]
     *     usage (Required): {
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param modelId           id of the deployed model.
     * @param embeddingsOptions The configuration information for an embeddings request. Embeddings measure the
     *                          relatedness of text strings and are commonly used for search, clustering,
     *                          recommendations, and other similar scenarios.
     * @param requestOptions    The options to configure the HTTP request before HTTP client sends it.
     * @return representation of the response data from an embeddings request. Embeddings measure the relatedness of
     * text strings and are commonly used for search, clustering, recommendations, and other similar scenarios along
     * with {@link Response}.
     * @throws HttpResponseException         thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException     thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException     thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getEmbeddingsWithResponse(String modelId, BinaryData embeddingsOptions,
                                                          RequestOptions requestOptions) {
        final String accept = "application/json";

        // modelId is part of the request body in nonAzure OpenAI
        try {
            BinaryData embeddingsOptionsUpdated = addModelIdJson(embeddingsOptions, modelId);
            return service.getEmbeddingsSync(
                OPEN_AI_ENDPOINT,
                accept,
                embeddingsOptionsUpdated,
                requestOptions,
                Context.NONE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets completions for the provided input prompts. Completions support a wide variety of tasks and generate text
     * that continues from or "completes" provided prompt data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     prompt (Required): [
     *         String (Required)
     *     ]
     *     max_tokens: Integer (Optional)
     *     temperature: Double (Optional)
     *     top_p: Double (Optional)
     *     logit_bias (Optional): {
     *         String: int (Optional)
     *     }
     *     user: String (Optional)
     *     n: Integer (Optional)
     *     logprobs: Integer (Optional)
     *     echo: Boolean (Optional)
     *     stop (Optional): [
     *         String (Optional)
     *     ]
     *     presence_penalty: Double (Optional)
     *     frequency_penalty: Double (Optional)
     *     best_of: Integer (Optional)
     *     stream: Boolean (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     created: int (Required)
     *     choices (Required): [
     *          (Required){
     *             text: String (Required)
     *             index: int (Required)
     *             logprobs (Required): {
     *                 tokens (Required): [
     *                     String (Required)
     *                 ]
     *                 token_logprobs (Required): [
     *                     double (Required)
     *                 ]
     *                 top_logprobs (Required): [
     *                      (Required){
     *                         String: double (Required)
     *                     }
     *                 ]
     *                 text_offset (Required): [
     *                     int (Required)
     *                 ]
     *             }
     *             finish_reason: String(stopped/tokenLimitReached/contentFiltered) (Required)
     *         }
     *     ]
     *     usage (Required): {
     *         completion_tokens: int (Required)
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param modelId            id of the deployed model.
     * @param completionsOptions The configuration information for a completions request. Completions support a wide
     *                           variety of tasks and generate text that continues from or "completes" provided prompt
     *                           data.
     * @param requestOptions     The options to configure the HTTP request before HTTP client sends it.
     * @return completions for the provided input prompts. Completions support a wide variety of tasks and generate text
     * that continues from or "completes" provided prompt data along with {@link Response} on successful completion
     * of {@link Mono}.
     * @throws HttpResponseException         thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException     thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException     thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getCompletionsWithResponseAsync(String modelId,
        BinaryData completionsOptions, RequestOptions requestOptions) {
        final String accept = "application/json";

        // modelId is part of the request body in nonAzure OpenAI
        try {
            BinaryData completionsOptionsUpdated = addModelIdJson(completionsOptions, modelId);
            return FluxUtil.withContext(
                context ->
                    service.getCompletions(
                        OPEN_AI_ENDPOINT,
                        accept,
                        completionsOptionsUpdated,
                        requestOptions,
                        context));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    /**
     * Gets completions for the provided input prompts. Completions support a wide variety of tasks and generate text
     * that continues from or "completes" provided prompt data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     prompt (Required): [
     *         String (Required)
     *     ]
     *     max_tokens: Integer (Optional)
     *     temperature: Double (Optional)
     *     top_p: Double (Optional)
     *     logit_bias (Optional): {
     *         String: int (Optional)
     *     }
     *     user: String (Optional)
     *     n: Integer (Optional)
     *     logprobs: Integer (Optional)
     *     echo: Boolean (Optional)
     *     stop (Optional): [
     *         String (Optional)
     *     ]
     *     presence_penalty: Double (Optional)
     *     frequency_penalty: Double (Optional)
     *     best_of: Integer (Optional)
     *     stream: Boolean (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     created: int (Required)
     *     choices (Required): [
     *          (Required){
     *             text: String (Required)
     *             index: int (Required)
     *             logprobs (Required): {
     *                 tokens (Required): [
     *                     String (Required)
     *                 ]
     *                 token_logprobs (Required): [
     *                     double (Required)
     *                 ]
     *                 top_logprobs (Required): [
     *                      (Required){
     *                         String: double (Required)
     *                     }
     *                 ]
     *                 text_offset (Required): [
     *                     int (Required)
     *                 ]
     *             }
     *             finish_reason: String(stopped/tokenLimitReached/contentFiltered) (Required)
     *         }
     *     ]
     *     usage (Required): {
     *         completion_tokens: int (Required)
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param modelId            id of the deployed model.
     * @param completionsOptions The configuration information for a completions request. Completions support a wide
     *                           variety of tasks and generate text that continues from or "completes" provided prompt data.
     * @param requestOptions     The options to configure the HTTP request before HTTP client sends it.
     * @return completions for the provided input prompts. Completions support a wide variety of tasks and generate text
     * that continues from or "completes" provided prompt data along with {@link Response}.
     * @throws HttpResponseException         thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException     thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException     thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getCompletionsWithResponse(String modelId, BinaryData completionsOptions,
                                                           RequestOptions requestOptions) {
        final String accept = "application/json";

        // modelId is part of the request body in nonAzure OpenAI
        BinaryData completionsOptionsUpdated = null;
        try {
            completionsOptionsUpdated = addModelIdJson(completionsOptions, modelId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return service.getCompletionsSync(
            OPEN_AI_ENDPOINT,
            accept,
            completionsOptionsUpdated,
            requestOptions,
            Context.NONE);
    }

    /**
     * Gets chat completions for the provided chat messages. Completions support a wide variety of tasks and generate
     * text that continues from or "completes" provided prompt data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     messages (Required): [
     *          (Required){
     *             role: String(system/assistant/user) (Required)
     *             content: String (Optional)
     *         }
     *     ]
     *     max_tokens: Integer (Optional)
     *     temperature: Double (Optional)
     *     top_p: Double (Optional)
     *     logit_bias (Optional): {
     *         String: int (Optional)
     *     }
     *     user: String (Optional)
     *     n: Integer (Optional)
     *     stop (Optional): [
     *         String (Optional)
     *     ]
     *     presence_penalty: Double (Optional)
     *     frequency_penalty: Double (Optional)
     *     stream: Boolean (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     created: int (Required)
     *     choices (Required): [
     *          (Required){
     *             message (Optional): {
     *                 role: String(system/assistant/user) (Required)
     *                 content: String (Optional)
     *             }
     *             index: int (Required)
     *             finish_reason: String(stopped/tokenLimitReached/contentFiltered) (Required)
     *             delta (Optional): {
     *                 role: String(system/assistant/user) (Optional)
     *                 content: String (Optional)
     *             }
     *         }
     *     ]
     *     usage (Required): {
     *         completion_tokens: int (Required)
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param modelId                id of the deployed model.
     * @param chatCompletionsOptions The configuration information for a chat completions request. Completions support a
     *                               wide variety of tasks and generate text that continues from or "completes"
     *                               provided prompt data.
     * @param requestOptions         The options to configure the HTTP request before HTTP client sends it.
     * @return chat completions for the provided chat messages. Completions support a wide variety of tasks and generate
     * text that continues from or "completes" provided prompt data along with {@link Response} on successful
     * completion of {@link Mono}.
     * @throws HttpResponseException         thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException     thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException     thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getChatCompletionsWithResponseAsync(String modelId,
        BinaryData chatCompletionsOptions, RequestOptions requestOptions) {
        final String accept = "application/json";

        // modelId is part of the request body in nonAzure OpenAI
        try {
            BinaryData chatCompletionsOptionsUpdated = addModelIdJson(chatCompletionsOptions, modelId);
            return FluxUtil.withContext(
                context ->
                    service.getChatCompletions(
                        OPEN_AI_ENDPOINT,
                        accept,
                        chatCompletionsOptionsUpdated,
                        requestOptions,
                        context));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    /**
     * Gets chat completions for the provided chat messages. Completions support a wide variety of tasks and generate
     * text that continues from or "completes" provided prompt data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     messages (Required): [
     *          (Required){
     *             role: String(system/assistant/user) (Required)
     *             content: String (Optional)
     *         }
     *     ]
     *     max_tokens: Integer (Optional)
     *     temperature: Double (Optional)
     *     top_p: Double (Optional)
     *     logit_bias (Optional): {
     *         String: int (Optional)
     *     }
     *     user: String (Optional)
     *     n: Integer (Optional)
     *     stop (Optional): [
     *         String (Optional)
     *     ]
     *     presence_penalty: Double (Optional)
     *     frequency_penalty: Double (Optional)
     *     stream: Boolean (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     created: int (Required)
     *     choices (Required): [
     *          (Required){
     *             message (Optional): {
     *                 role: String(system/assistant/user) (Required)
     *                 content: String (Optional)
     *             }
     *             index: int (Required)
     *             finish_reason: String(stopped/tokenLimitReached/contentFiltered) (Required)
     *             delta (Optional): {
     *                 role: String(system/assistant/user) (Optional)
     *                 content: String (Optional)
     *             }
     *         }
     *     ]
     *     usage (Required): {
     *         completion_tokens: int (Required)
     *         prompt_tokens: int (Required)
     *         total_tokens: int (Required)
     *     }
     * }
     * }</pre>
     *
     * @param modelId                id of the deployed model.
     * @param chatCompletionsOptions The configuration information for a chat completions request. Completions support a
     *                               wide variety of tasks and generate text that continues from or "completes" provided
     *                               prompt data.
     * @param requestOptions         The options to configure the HTTP request before HTTP client sends it.
     * @return chat completions for the provided chat messages. Completions support a wide variety of tasks and generate
     * text that continues from or "completes" provided prompt data along with {@link Response}.
     * @throws HttpResponseException         thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException     thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException     thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getChatCompletionsWithResponse(String modelId, BinaryData chatCompletionsOptions,
                                                               RequestOptions requestOptions) {
        final String accept = "application/json";

        // modelId is part of the request body in nonAzure OpenAI
        BinaryData chatCompletionsOptionsUpdated = null;
        try {
            chatCompletionsOptionsUpdated = addModelIdJson(chatCompletionsOptions, modelId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return service.getChatCompletionsSync(
            OPEN_AI_ENDPOINT,
            accept,
            chatCompletionsOptionsUpdated,
            requestOptions,
            Context.NONE);
    }

    /**
     * Starts the generation of a batch of images from a text caption.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     prompt: String (Required)
     *     n: Integer (Optional)
     *     size: String(256x256/512x512/1024x1024) (Optional)
     *     user: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     status: String (Required)
     *     error (Optional): {
     *         code: String (Required)
     *         message: String (Required)
     *         target: String (Optional)
     *         details (Optional): [
     *             (recursive schema, see above)
     *         ]
     *         innererror (Optional): {
     *             code: String (Optional)
     *             innererror (Optional): (recursive schema, see innererror above)
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param imageGenerationOptions Represents the request data used to generate images.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return A list of image URLs that were generated based on the prompt sent in the request
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> generateImageWithResponseAsync(
        BinaryData imageGenerationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";

        return service.generateImage(
            OPEN_AI_ENDPOINT,
            accept,
            imageGenerationOptions,
            requestOptions,
            Context.NONE
        );
    }

    /**
     * Starts the generation of a batch of images from a text caption.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     prompt: String (Required)
     *     n: Integer (Optional)
     *     size: String(256x256/512x512/1024x1024) (Optional)
     *     user: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     status: String (Required)
     *     error (Optional): {
     *         code: String (Required)
     *         message: String (Required)
     *         target: String (Optional)
     *         details (Optional): [
     *             (recursive schema, see above)
     *         ]
     *         innererror (Optional): {
     *             code: String (Optional)
     *             innererror (Optional): (recursive schema, see innererror above)
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param imageGenerationOptions Represents the request data used to generate images.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return A list of image URLs that were generated based on the prompt sent in the request
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> generateImageWithResponse(
        BinaryData imageGenerationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";

        return service.generateImageSync(
            OPEN_AI_ENDPOINT,
            accept,
            imageGenerationOptions,
            requestOptions,
            Context.NONE
        );
    }

    /**
     * This method injects the modelId in the request body for requests against nonAzure OpenAI. Unlike Azure OpenAI,
     * the service expects this value in the body of the request, whereas Azure OpenAI passes it as part of the
     * path of the request.
     *
     * @param inputJson JSON submitted by the client
     * @param modelId The LLM model ID to be injected in the JSON
     * @return an updated version of the JSON with the key "model" and its corresponding value "modelId" added
     */
    private static BinaryData addModelIdJson(BinaryData inputJson, String modelId) throws JsonProcessingException {
        JsonNode jsonNode = JSON_MAPPER.readTree(inputJson.toString());
        if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            objectNode.put("model", modelId);
            inputJson = BinaryData.fromBytes(
                objectNode.toString()
                    .getBytes(StandardCharsets.UTF_8));
        }

        return inputJson;
    }

    /**
     * Gets transcribed text and associated metadata from provided spoken audio data. Audio will be transcribed in the
     * written language corresponding to the language it was spoken in.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     language: String (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     text: String (Required)
     *     task: String(transcribe/translate) (Optional)
     *     language: String (Optional)
     *     duration: Double (Optional)
     *     segments (Optional): [
     *          (Optional){
     *             id: int (Required)
     *             start: double (Required)
     *             end: double (Required)
     *             text: String (Required)
     *             temperature: double (Required)
     *             avg_logprob: double (Required)
     *             compression_ratio: double (Required)
     *             no_speech_prob: double (Required)
     *             tokens (Required): [
     *                 int (Required)
     *             ]
     *             seek: int (Required)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param modelId Specifies the model name to use for this request.
     * @param audioTranscriptionOptions The configuration information for an audio transcription request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return transcribed text and associated metadata from provided spoken audio data along with {@link Response} on
     *     successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getAudioTranscriptionAsResponseObjectWithResponseAsync(
            String modelId, BinaryData audioTranscriptionOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getAudioTranscriptionAsResponseObject(
                                OPEN_AI_ENDPOINT,
                                accept,
                                audioTranscriptionOptions,
                                requestOptions,
                                context));
    }

    /**
     * Gets transcribed text and associated metadata from provided spoken audio data. Audio will be transcribed in the
     * written language corresponding to the language it was spoken in.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     language: String (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     text: String (Required)
     *     task: String(transcribe/translate) (Optional)
     *     language: String (Optional)
     *     duration: Double (Optional)
     *     segments (Optional): [
     *          (Optional){
     *             id: int (Required)
     *             start: double (Required)
     *             end: double (Required)
     *             text: String (Required)
     *             temperature: double (Required)
     *             avg_logprob: double (Required)
     *             compression_ratio: double (Required)
     *             no_speech_prob: double (Required)
     *             tokens (Required): [
     *                 int (Required)
     *             ]
     *             seek: int (Required)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param modelId Specifies the model name to use for this request.
     * @param audioTranscriptionOptions The configuration information for an audio transcription request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return transcribed text and associated metadata from provided spoken audio data along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getAudioTranscriptionAsResponseObjectWithResponse(
            String modelId, BinaryData audioTranscriptionOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.getAudioTranscriptionAsResponseObjectSync(
                OPEN_AI_ENDPOINT,
                accept,
                audioTranscriptionOptions,
                requestOptions,
                Context.NONE);
    }

    /**
     * Gets transcribed text and associated metadata from provided spoken audio data. Audio will be transcribed in the
     * written language corresponding to the language it was spoken in.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     language: String (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * String
     * }</pre>
     *
     * @param modelId Specifies the model name to use for this request.
     * @param audioTranscriptionOptions The configuration information for an audio transcription request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return transcribed text and associated metadata from provided spoken audio data along with {@link Response} on
     *     successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getAudioTranscriptionAsPlainTextWithResponseAsync(
            String modelId, BinaryData audioTranscriptionOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getAudioTranscriptionAsPlainText(
                                OPEN_AI_ENDPOINT,
                                accept,
                                audioTranscriptionOptions,
                                requestOptions,
                                context));
    }

    /**
     * Gets transcribed text and associated metadata from provided spoken audio data. Audio will be transcribed in the
     * written language corresponding to the language it was spoken in.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     language: String (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * String
     * }</pre>
     *
     * @param modelId Specifies the model name to use for this request.
     * @param audioTranscriptionOptions The configuration information for an audio transcription request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return transcribed text and associated metadata from provided spoken audio data along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getAudioTranscriptionAsPlainTextWithResponse(
            String modelId, BinaryData audioTranscriptionOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.getAudioTranscriptionAsPlainTextSync(
                OPEN_AI_ENDPOINT,
                accept,
                audioTranscriptionOptions,
                requestOptions,
                Context.NONE);
    }

    /**
     * Gets English language transcribed text and associated metadata from provided spoken audio data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     text: String (Required)
     *     task: String(transcribe/translate) (Optional)
     *     language: String (Optional)
     *     duration: Double (Optional)
     *     segments (Optional): [
     *          (Optional){
     *             id: int (Required)
     *             start: double (Required)
     *             end: double (Required)
     *             text: String (Required)
     *             temperature: double (Required)
     *             avg_logprob: double (Required)
     *             compression_ratio: double (Required)
     *             no_speech_prob: double (Required)
     *             tokens (Required): [
     *                 int (Required)
     *             ]
     *             seek: int (Required)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param deploymentOrModelName Specifies the model name to use for this request.
     * @param audioTranslationOptions The configuration information for an audio translation request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return english language transcribed text and associated metadata from provided spoken audio data along with
     *     {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getAudioTranslationAsResponseObjectWithResponseAsync(
            String deploymentOrModelName, BinaryData audioTranslationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getAudioTranslationAsResponseObject(
                                OPEN_AI_ENDPOINT,
                                accept,
                                audioTranslationOptions,
                                requestOptions,
                                context));
    }

    /**
     * Gets English language transcribed text and associated metadata from provided spoken audio data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     text: String (Required)
     *     task: String(transcribe/translate) (Optional)
     *     language: String (Optional)
     *     duration: Double (Optional)
     *     segments (Optional): [
     *          (Optional){
     *             id: int (Required)
     *             start: double (Required)
     *             end: double (Required)
     *             text: String (Required)
     *             temperature: double (Required)
     *             avg_logprob: double (Required)
     *             compression_ratio: double (Required)
     *             no_speech_prob: double (Required)
     *             tokens (Required): [
     *                 int (Required)
     *             ]
     *             seek: int (Required)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param modelId Specifies the model name to use for this request.
     * @param audioTranslationOptions The configuration information for an audio translation request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return english language transcribed text and associated metadata from provided spoken audio data along with
     *     {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getAudioTranslationAsResponseObjectWithResponse(
            String modelId, BinaryData audioTranslationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.getAudioTranslationAsResponseObjectSync(
                OPEN_AI_ENDPOINT,
                accept,
                audioTranslationOptions,
                requestOptions,
                Context.NONE);
    }

    /**
     * Gets English language transcribed text and associated metadata from provided spoken audio data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * String
     * }</pre>
     *
     * @param modelId Specifies the model name to use for this request.
     * @param audioTranslationOptions The configuration information for an audio translation request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return english language transcribed text and associated metadata from provided spoken audio data along with
     *     {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getAudioTranslationAsPlainTextWithResponseAsync(
            String modelId, BinaryData audioTranslationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
                context ->
                        service.getAudioTranslationAsPlainText(
                                OPEN_AI_ENDPOINT,
                                accept,
                                audioTranslationOptions,
                                requestOptions,
                                context));
    }

    /**
     * Gets English language transcribed text and associated metadata from provided spoken audio data.
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * {
     *     file: byte[] (Required)
     *     response_format: String(json/verbose_json/text/srt/vtt) (Optional)
     *     prompt: String (Optional)
     *     temperature: Double (Optional)
     *     model: String (Optional)
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong>
     *
     * <pre>{@code
     * String
     * }</pre>
     *
     * @param modelId Specifies the model name to use for this request.
     * @param audioTranslationOptions The configuration information for an audio translation request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return english language transcribed text and associated metadata from provided spoken audio data along with
     *     {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getAudioTranslationAsPlainTextWithResponse(
            String modelId, BinaryData audioTranslationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.getAudioTranslationAsPlainTextSync(
                OPEN_AI_ENDPOINT,
                accept,
                audioTranslationOptions,
                requestOptions,
                Context.NONE);
    }
}
