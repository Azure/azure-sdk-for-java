// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.EmbeddingsOptions;
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
import reactor.core.publisher.Mono;

/**
 * Implementation for calling Non-Azure OpenAI service
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

        // OpenAI has model ID in request body
        BinaryData embeddingsOptionsUpdated = BinaryData.fromObject(
            embeddingsOptions.toObject(EmbeddingsOptions.class)
                .setModel(modelId)
        );

        return FluxUtil.withContext(
            context ->
                service.getEmbeddings(
                    OPEN_AI_ENDPOINT,
                    accept,
                    embeddingsOptionsUpdated,
                    requestOptions,
                    context));
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

        // OpenAI has model ID in request body
        BinaryData embeddingsOptionsUpdated = BinaryData.fromObject(
            embeddingsOptions.toObject(EmbeddingsOptions.class)
                .setModel(modelId)
        );

        return service.getEmbeddingsSync(
            OPEN_AI_ENDPOINT,
            accept,
            embeddingsOptionsUpdated,
            requestOptions,
            Context.NONE);
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

        // OpenAI has model ID in request body
        BinaryData completionsOptionsUpdated = BinaryData.fromObject(
            completionsOptions.toObject(CompletionsOptions.class)
                .setModel(modelId)
        );

        return FluxUtil.withContext(
            context ->
                service.getCompletions(
                    OPEN_AI_ENDPOINT,
                    accept,
                    completionsOptionsUpdated,
                    requestOptions,
                    context));
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

        // OpenAI has model ID in request body
        BinaryData completionsOptionsUpdated = BinaryData.fromObject(
            completionsOptions.toObject(CompletionsOptions.class)
                .setModel(modelId)
        );
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

        // OpenAI has model ID in request body
        BinaryData chatCompletionsOptionsUpdated = BinaryData.fromObject(
            chatCompletionsOptions.toObject(ChatCompletionsOptions.class)
                .setModel(modelId)
        );

        return FluxUtil.withContext(
            context ->
                service.getChatCompletions(
                    OPEN_AI_ENDPOINT,
                    accept,
                    chatCompletionsOptionsUpdated,
                    requestOptions,
                    context));
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

        // OpenAI has model ID in request body
        BinaryData chatCompletionsOptionsUpdated = BinaryData.fromObject(
            chatCompletionsOptions.toObject(ChatCompletionsOptions.class)
                .setModel(modelId)
        );

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
}
