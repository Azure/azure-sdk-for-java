// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
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

import java.util.Map;

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
        Mono<Response<BinaryData>> getImageGenerations(
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
        Response<BinaryData> getImageGenerationsSync(
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

        @Post("/audio/speech")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> generateSpeechFromText(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("application/json") BinaryData speechGenerationOptions,
                RequestOptions requestOptions,
                Context context);

        @Post("/audio/speech")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> generateSpeechFromTextSync(
                @HostParam("endpoint") String endpoint,
                @HeaderParam("accept") String accept,
                @BodyParam("application/json") BinaryData speechGenerationOptions,
                RequestOptions requestOptions,
                Context context);

        @Get("/files")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> listFiles(
            @HostParam("endpoint") String endpoint,
            @HeaderParam("accept") String accept,
            RequestOptions requestOptions,
            Context context);

        @Get("/files")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> listFilesSync(
            @HostParam("endpoint") String endpoint,
            @HeaderParam("accept") String accept,
            RequestOptions requestOptions,
            Context context);

        // @Multipart not supported by RestProxy
        @Post("/files")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> uploadFile(
            @HostParam("endpoint") String endpoint,
            @HeaderParam("content-type") String contentType,
            @HeaderParam("accept") String accept,
            @BodyParam("multipart/form-data") BinaryData uploadFileRequest,
            RequestOptions requestOptions,
            Context context);

        // @Multipart not supported by RestProxy
        @Post("/files")
        @ExpectedResponses({ 200, 201 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> uploadFileSync(
            @HostParam("endpoint") String endpoint,
            @HeaderParam("content-type") String contentType,
            @HeaderParam("accept") String accept,
            @BodyParam("multipart/form-data") BinaryData uploadFileRequest,
            RequestOptions requestOptions,
            Context context);

        @Delete("/files/{fileId}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> deleteFile(@HostParam("endpoint") String endpoint,
                                              @PathParam("fileId") String fileId, @HeaderParam("accept") String accept, RequestOptions requestOptions,
                                              Context context);

        @Delete("/files/{fileId}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> deleteFileSync(@HostParam("endpoint") String endpoint, @PathParam("fileId") String fileId,
                                            @HeaderParam("accept") String accept, RequestOptions requestOptions, Context context);

        @Get("/files/{fileId}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> getFile(@HostParam("endpoint") String endpoint, @PathParam("fileId") String fileId,
                                           @HeaderParam("accept") String accept, RequestOptions requestOptions, Context context);

        @Get("/files/{fileId}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> getFileSync(@HostParam("endpoint") String endpoint, @PathParam("fileId") String fileId,
                                         @HeaderParam("accept") String accept, RequestOptions requestOptions, Context context);

        @Get("/files/{fileId}/content")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> getFileContent(@HostParam("endpoint") String endpoint,
                                                  @PathParam("fileId") String fileId, @HeaderParam("accept") String accept, RequestOptions requestOptions,
                                                  Context context);

        @Get("/files/{fileId}/content")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> getFileContentSync(@HostParam("endpoint") String endpoint,
                                                @PathParam("fileId") String fileId, @HeaderParam("accept") String accept, RequestOptions requestOptions,
                                                Context context);

        @Get("/batches")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> listBatches(@HostParam("endpoint") String endpoint,
                                               @HeaderParam("accept") String accept, RequestOptions requestOptions, Context context);

        @Get("/batches")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> listBatchesSync(@HostParam("endpoint") String endpoint,
                                             @HeaderParam("accept") String accept, RequestOptions requestOptions, Context context);

        @Post("/batches")
        @ExpectedResponses({ 200, 201 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> createBatch(@HostParam("endpoint") String endpoint,
                                               @HeaderParam("accept") String accept, @BodyParam("application/json") BinaryData createBatchRequest,
                                               RequestOptions requestOptions, Context context);

        @Post("/batches")
        @ExpectedResponses({ 200, 201 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> createBatchSync(@HostParam("endpoint") String endpoint,
                                             @HeaderParam("accept") String accept, @BodyParam("application/json") BinaryData createBatchRequest,
                                             RequestOptions requestOptions, Context context);

        @Get("/batches/{batchId}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> getBatch(@HostParam("endpoint") String endpoint,
                                            @PathParam("batchId") String batchId, @HeaderParam("accept") String accept, RequestOptions requestOptions,
                                            Context context);

        @Get("/batches/{batchId}")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> getBatchSync(@HostParam("endpoint") String endpoint, @PathParam("batchId") String batchId,
                                          @HeaderParam("accept") String accept, RequestOptions requestOptions, Context context);

        @Post("/batches/{batchId}/cancel")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> cancelBatch(@HostParam("endpoint") String endpoint,
                                               @PathParam("batchId") String batchId, @HeaderParam("accept") String accept, RequestOptions requestOptions,
                                               Context context);

        @Post("/batches/{batchId}/cancel")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> cancelBatchSync(@HostParam("endpoint") String endpoint,
                                             @PathParam("batchId") String batchId, @HeaderParam("accept") String accept, RequestOptions requestOptions,
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
        final BinaryData embeddingsOptionsUpdated = addModelIdJson(embeddingsOptions, modelId);
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
        // modelId is part of the request body in nonAzure OpenAI
        final BinaryData embeddingsOptionsUpdated = addModelIdJson(embeddingsOptions, modelId);
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
        // modelId is part of the request body in nonAzure OpenAI
        final BinaryData completionsOptionsUpdated = addModelIdJson(completionsOptions, modelId);
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
        // modelId is part of the request body in nonAzure OpenAI
        final BinaryData completionsOptionsUpdated = addModelIdJson(completionsOptions, modelId);
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
        final BinaryData chatCompletionsOptionsUpdated = addModelIdJson(chatCompletionsOptions, modelId);
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
        // modelId is part of the request body in nonAzure OpenAI
        final BinaryData chatCompletionsOptionsUpdated = addModelIdJson(chatCompletionsOptions, modelId);
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
    public Mono<Response<BinaryData>> getImageGenerationsWithResponseAsync(String modelId,
        BinaryData imageGenerationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        // modelId is part of the request body in nonAzure OpenAI
        final BinaryData imageGenerationOptionsUpdated = addModelIdJson(imageGenerationOptions, modelId);
        return FluxUtil.withContext(
                context ->
                        service.getImageGenerations(
                                OPEN_AI_ENDPOINT,
                                accept,
                                imageGenerationOptionsUpdated,
                                requestOptions,
                                context));
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
    public Response<BinaryData> getImageGenerationsWithResponse(String modelId,
        BinaryData imageGenerationOptions, RequestOptions requestOptions) {
        final String accept = "application/json";
        final BinaryData imageGenerationOptionsUpdated = addModelIdJson(imageGenerationOptions, modelId);
        return service.getImageGenerationsSync(
            OPEN_AI_ENDPOINT,
            accept,
            imageGenerationOptionsUpdated,
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
    @SuppressWarnings("unchecked")
    public static BinaryData addModelIdJson(BinaryData inputJson, String modelId) {
        Map<String, Object> mapJson = inputJson.toObject(Map.class);
        mapJson.put("model", modelId);
        inputJson = BinaryData.fromObject(mapJson);
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

    /**
     * Generates text-to-speech audio from the input text.
     * <p>
     * <strong>Request Body Schema</strong>
     * </p>
     * <pre>{@code
     * {
     *     input: String (Required)
     *     voice: String(alloy/echo/fable/onyx/nova/shimmer) (Required)
     *     response_format: String(mp3/opus/aac/flac) (Optional)
     *     speed: Double (Optional)
     * }
     * }</pre>
     * <p>
     * <strong>Response Body Schema</strong>
     * </p>
     * <pre>{@code
     * BinaryData
     * }</pre>
     *
     * @param modelId Specifies either the model name to use for this request.
     * @param speechGenerationOptions A representation of the request options that control the behavior of a
     * text-to-speech operation.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> generateSpeechFromTextWithResponseAsync(String modelId,
        BinaryData speechGenerationOptions, RequestOptions requestOptions) {
        final String accept = "application/octet-stream, application/json";
        return FluxUtil.withContext(context -> service.generateSpeechFromText(
                    OPEN_AI_ENDPOINT, accept, speechGenerationOptions, requestOptions, context));
    }

    /**
     * Generates text-to-speech audio from the input text.
     * <p>
     * <strong>Request Body Schema</strong>
     * </p>
     * <pre>{@code
     * {
     *     input: String (Required)
     *     voice: String(alloy/echo/fable/onyx/nova/shimmer) (Required)
     *     response_format: String(mp3/opus/aac/flac) (Optional)
     *     speed: Double (Optional)
     * }
     * }</pre>
     * <p>
     * <strong>Response Body Schema</strong>
     * </p>
     * <pre>{@code
     * BinaryData
     * }</pre>
     *
     * @param speechGenerationOptions A representation of the request options that control the behavior of a
     * text-to-speech operation.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> generateSpeechFromTextWithResponse(BinaryData speechGenerationOptions,
                                                                   RequestOptions requestOptions) {
        final String accept = "application/octet-stream, application/json";
        return service.generateSpeechFromTextSync(OPEN_AI_ENDPOINT, accept, speechGenerationOptions,
                requestOptions, Context.NONE);
    }

    /**
     * Gets a list of previously uploaded files.
     * <p><strong>Query Parameters</strong></p>
     * <table border="1">
     * <caption>Query Parameters</caption>
     * <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     * <tr><td>purpose</td><td>String</td><td>No</td><td>A value that, when provided, limits list results to files
     * matching the corresponding purpose. Allowed values: "fine-tune", "fine-tune-results", "assistants",
     * "assistants_output", "batch", "batch_output", "vision".</td></tr>
     * </table>
     * You can add these to a request with {@link RequestOptions#addQueryParam}
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     data (Required): [
     *          (Required){
     *             object: String (Required)
     *             id: String (Required)
     *             bytes: int (Required)
     *             filename: String (Required)
     *             created_at: long (Required)
     *             purpose: String(fine-tune/fine-tune-results/assistants/assistants_output/batch/batch_output/vision) (Required)
     *             status: String(uploaded/pending/running/processed/error/deleting/deleted) (Optional)
     *             status_details: String (Optional)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return a list of previously uploaded files along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listFilesWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(context -> service.listFiles(OPEN_AI_ENDPOINT, accept, requestOptions, context));
    }

    /**
     * Gets a list of previously uploaded files.
     * <p><strong>Query Parameters</strong></p>
     * <table border="1">
     * <caption>Query Parameters</caption>
     * <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     * <tr><td>purpose</td><td>String</td><td>No</td><td>A value that, when provided, limits list results to files
     * matching the corresponding purpose. Allowed values: "fine-tune", "fine-tune-results", "assistants",
     * "assistants_output", "batch", "batch_output", "vision".</td></tr>
     * </table>
     * You can add these to a request with {@link RequestOptions#addQueryParam}
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     data (Required): [
     *          (Required){
     *             object: String (Required)
     *             id: String (Required)
     *             bytes: int (Required)
     *             filename: String (Required)
     *             created_at: long (Required)
     *             purpose: String(fine-tune/fine-tune-results/assistants/assistants_output/batch/batch_output/vision) (Required)
     *             status: String(uploaded/pending/running/processed/error/deleting/deleted) (Optional)
     *             status_details: String (Optional)
     *         }
     *     ]
     * }
     * }</pre>
     *
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return a list of previously uploaded files along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listFilesWithResponse(RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.listFilesSync(OPEN_AI_ENDPOINT, accept, requestOptions, Context.NONE);
    }

    /**
     * Uploads a file for use by other operations.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     id: String (Required)
     *     bytes: int (Required)
     *     filename: String (Required)
     *     created_at: long (Required)
     *     purpose: String(fine-tune/fine-tune-results/assistants/assistants_output/batch/batch_output/vision) (Required)
     *     status: String(uploaded/pending/running/processed/error/deleting/deleted) (Optional)
     *     status_details: String (Optional)
     * }
     * }</pre>
     *
     * @param uploadFileRequest The uploadFileRequest parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return represents an assistant that can call the model and use tools along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> uploadFileWithResponseAsync(BinaryData uploadFileRequest,
                                                                  RequestOptions requestOptions) {
        final String contentType = "multipart/form-data";
        final String accept = "application/json";
        return FluxUtil.withContext(context -> service.uploadFile(OPEN_AI_ENDPOINT, contentType, accept,
            uploadFileRequest, requestOptions, context));
    }

    /**
     * Uploads a file for use by other operations.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     id: String (Required)
     *     bytes: int (Required)
     *     filename: String (Required)
     *     created_at: long (Required)
     *     purpose: String(fine-tune/fine-tune-results/assistants/assistants_output/batch/batch_output/vision) (Required)
     *     status: String(uploaded/pending/running/processed/error/deleting/deleted) (Optional)
     *     status_details: String (Optional)
     * }
     * }</pre>
     *
     * @param uploadFileRequest The uploadFileRequest parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return represents an assistant that can call the model and use tools along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> uploadFileWithResponse(BinaryData uploadFileRequest, RequestOptions requestOptions) {
        final String contentType = "multipart/form-data";
        final String accept = "application/json";
        return service.uploadFileSync(OPEN_AI_ENDPOINT, contentType, accept, uploadFileRequest, requestOptions,
            Context.NONE);
    }

    /**
     * Delete a previously uploaded file.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     deleted: boolean (Required)
     *     object: String (Required)
     * }
     * }</pre>
     *
     * @param fileId The ID of the file to delete.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return a status response from a file deletion operation along with {@link Response} on successful completion of
     * {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> deleteFileWithResponseAsync(String fileId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.deleteFile(OPEN_AI_ENDPOINT, fileId, accept, requestOptions, context));
    }

    /**
     * Delete a previously uploaded file.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     deleted: boolean (Required)
     *     object: String (Required)
     * }
     * }</pre>
     *
     * @param fileId The ID of the file to delete.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return a status response from a file deletion operation along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> deleteFileWithResponse(String fileId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.deleteFileSync(OPEN_AI_ENDPOINT, fileId, accept, requestOptions, Context.NONE);
    }

    /**
     * Returns information about a specific file. Does not retrieve file content.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     id: String (Required)
     *     bytes: int (Required)
     *     filename: String (Required)
     *     created_at: long (Required)
     *     purpose: String(fine-tune/fine-tune-results/assistants/assistants_output/batch/batch_output/vision) (Required)
     *     status: String(uploaded/pending/running/processed/error/deleting/deleted) (Optional)
     *     status_details: String (Optional)
     * }
     * }</pre>
     *
     * @param fileId The ID of the file to retrieve.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return represents an assistant that can call the model and use tools along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getFileWithResponseAsync(String fileId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.getFile(OPEN_AI_ENDPOINT, fileId, accept, requestOptions, context));
    }

    /**
     * Returns information about a specific file. Does not retrieve file content.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     id: String (Required)
     *     bytes: int (Required)
     *     filename: String (Required)
     *     created_at: long (Required)
     *     purpose: String(fine-tune/fine-tune-results/assistants/assistants_output/batch/batch_output/vision) (Required)
     *     status: String(uploaded/pending/running/processed/error/deleting/deleted) (Optional)
     *     status_details: String (Optional)
     * }
     * }</pre>
     *
     * @param fileId The ID of the file to retrieve.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return represents an assistant that can call the model and use tools along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getFileWithResponse(String fileId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.getFileSync(OPEN_AI_ENDPOINT, fileId, accept, requestOptions, Context.NONE);
    }

    /**
     * Returns information about a specific file. Does not retrieve file content.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * byte[]
     * }</pre>
     *
     * @param fileId The ID of the file to retrieve.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return represent a byte array along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getFileContentWithResponseAsync(String fileId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
            context -> service.getFileContent(OPEN_AI_ENDPOINT, fileId, accept, requestOptions, context));
    }

    /**
     * Returns information about a specific file. Does not retrieve file content.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * byte[]
     * }</pre>
     *
     * @param fileId The ID of the file to retrieve.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return represent a byte array along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getFileContentWithResponse(String fileId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.getFileContentSync(OPEN_AI_ENDPOINT, fileId, accept, requestOptions, Context.NONE);
    }

    /**
     * Gets a list of all batches owned by the Azure OpenAI resource.
     * <p><strong>Query Parameters</strong></p>
     * <table border="1">
     * <caption>Query Parameters</caption>
     * <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     * <tr><td>after</td><td>String</td><td>No</td><td>Identifier for the last event from the previous pagination
     * request.</td></tr>
     * <tr><td>limit</td><td>Integer</td><td>No</td><td>The number of batches to retrieve. The default is 20.</td></tr>
     * </table>
     * You can add these to a request with {@link RequestOptions#addQueryParam}
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     data (Required): [
     *          (Required){
     *             id: String (Required)
     *             object: String (Required)
     *             endpoint: String (Required)
     *             errors (Optional): {
     *                 object: String (Required)
     *                 data (Optional): [
     *                      (Optional){
     *                         code: String (Optional)
     *                         message: String (Optional)
     *                         param: String (Optional)
     *                         line: Integer (Optional)
     *                     }
     *                 ]
     *             }
     *             input_file_id: String (Required)
     *             completion_window: String (Required)
     *             status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *             output_file_id: String (Optional)
     *             error_file_id: String (Optional)
     *             created_at: long (Required)
     *             in_progress_at: Long (Optional)
     *             expires_at: Long (Optional)
     *             finalizing_at: Long (Optional)
     *             completed_at: Long (Optional)
     *             failed_at: Long (Optional)
     *             expired_at: Long (Optional)
     *             cancelling_at: Long (Optional)
     *             cancelled_at: Long (Optional)
     *             request_counts (Optional): {
     *                 total: int (Required)
     *                 completed: int (Required)
     *                 failed: int (Required)
     *             }
     *             metadata (Optional): {
     *                 String: String (Required)
     *             }
     *         }
     *     ]
     *     first_id: String (Required)
     *     last_id: String (Required)
     *     has_more: boolean (Required)
     * }
     * }</pre>
     *
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return a list of all batches owned by the Azure OpenAI resource along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> listBatchesWithResponseAsync(RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.listBatches(OPEN_AI_ENDPOINT, accept, requestOptions, context));
    }

    /**
     * Gets a list of all batches owned by the Azure OpenAI resource.
     * <p><strong>Query Parameters</strong></p>
     * <table border="1">
     * <caption>Query Parameters</caption>
     * <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     * <tr><td>after</td><td>String</td><td>No</td><td>Identifier for the last event from the previous pagination
     * request.</td></tr>
     * <tr><td>limit</td><td>Integer</td><td>No</td><td>The number of batches to retrieve. The default is 20.</td></tr>
     * </table>
     * You can add these to a request with {@link RequestOptions#addQueryParam}
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     object: String (Required)
     *     data (Required): [
     *          (Required){
     *             id: String (Required)
     *             object: String (Required)
     *             endpoint: String (Required)
     *             errors (Optional): {
     *                 object: String (Required)
     *                 data (Optional): [
     *                      (Optional){
     *                         code: String (Optional)
     *                         message: String (Optional)
     *                         param: String (Optional)
     *                         line: Integer (Optional)
     *                     }
     *                 ]
     *             }
     *             input_file_id: String (Required)
     *             completion_window: String (Required)
     *             status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *             output_file_id: String (Optional)
     *             error_file_id: String (Optional)
     *             created_at: long (Required)
     *             in_progress_at: Long (Optional)
     *             expires_at: Long (Optional)
     *             finalizing_at: Long (Optional)
     *             completed_at: Long (Optional)
     *             failed_at: Long (Optional)
     *             expired_at: Long (Optional)
     *             cancelling_at: Long (Optional)
     *             cancelled_at: Long (Optional)
     *             request_counts (Optional): {
     *                 total: int (Required)
     *                 completed: int (Required)
     *                 failed: int (Required)
     *             }
     *             metadata (Optional): {
     *                 String: String (Required)
     *             }
     *         }
     *     ]
     *     first_id: String (Required)
     *     last_id: String (Required)
     *     has_more: boolean (Required)
     * }
     * }</pre>
     *
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return a list of all batches owned by the Azure OpenAI resource along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> listBatchesWithResponse(RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.listBatchesSync(OPEN_AI_ENDPOINT, accept, requestOptions, Context.NONE);
    }

    /**
     * Creates and executes a batch from an uploaded file of requests.
     * Response includes details of the enqueued job including job status.
     * The ID of the result file is added to the response once complete.
     * <p><strong>Request Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     endpoint: String (Required)
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     endpoint: String (Required)
     *     errors (Optional): {
     *         object: String (Required)
     *         data (Optional): [
     *              (Optional){
     *                 code: String (Optional)
     *                 message: String (Optional)
     *                 param: String (Optional)
     *                 line: Integer (Optional)
     *             }
     *         ]
     *     }
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *     output_file_id: String (Optional)
     *     error_file_id: String (Optional)
     *     created_at: long (Required)
     *     in_progress_at: Long (Optional)
     *     expires_at: Long (Optional)
     *     finalizing_at: Long (Optional)
     *     completed_at: Long (Optional)
     *     failed_at: Long (Optional)
     *     expired_at: Long (Optional)
     *     cancelling_at: Long (Optional)
     *     cancelled_at: Long (Optional)
     *     request_counts (Optional): {
     *         total: int (Required)
     *         completed: int (Required)
     *         failed: int (Required)
     *     }
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * @param createBatchRequest The createBatchRequest parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the Batch object along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> createBatchWithResponseAsync(BinaryData createBatchRequest,
                                                                   RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil.withContext(
            context -> service.createBatch(OPEN_AI_ENDPOINT, accept, createBatchRequest, requestOptions, context));
    }

    /**
     * Creates and executes a batch from an uploaded file of requests.
     * Response includes details of the enqueued job including job status.
     * The ID of the result file is added to the response once complete.
     * <p><strong>Request Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     endpoint: String (Required)
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     endpoint: String (Required)
     *     errors (Optional): {
     *         object: String (Required)
     *         data (Optional): [
     *              (Optional){
     *                 code: String (Optional)
     *                 message: String (Optional)
     *                 param: String (Optional)
     *                 line: Integer (Optional)
     *             }
     *         ]
     *     }
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *     output_file_id: String (Optional)
     *     error_file_id: String (Optional)
     *     created_at: long (Required)
     *     in_progress_at: Long (Optional)
     *     expires_at: Long (Optional)
     *     finalizing_at: Long (Optional)
     *     completed_at: Long (Optional)
     *     failed_at: Long (Optional)
     *     expired_at: Long (Optional)
     *     cancelling_at: Long (Optional)
     *     cancelled_at: Long (Optional)
     *     request_counts (Optional): {
     *         total: int (Required)
     *         completed: int (Required)
     *         failed: int (Required)
     *     }
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * @param createBatchRequest The createBatchRequest parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the Batch object along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> createBatchWithResponse(BinaryData createBatchRequest, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.createBatchSync(OPEN_AI_ENDPOINT, accept, createBatchRequest, requestOptions, Context.NONE);
    }

    /**
     * Gets details for a single batch specified by the given batchID.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     endpoint: String (Required)
     *     errors (Optional): {
     *         object: String (Required)
     *         data (Optional): [
     *              (Optional){
     *                 code: String (Optional)
     *                 message: String (Optional)
     *                 param: String (Optional)
     *                 line: Integer (Optional)
     *             }
     *         ]
     *     }
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *     output_file_id: String (Optional)
     *     error_file_id: String (Optional)
     *     created_at: long (Required)
     *     in_progress_at: Long (Optional)
     *     expires_at: Long (Optional)
     *     finalizing_at: Long (Optional)
     *     completed_at: Long (Optional)
     *     failed_at: Long (Optional)
     *     expired_at: Long (Optional)
     *     cancelling_at: Long (Optional)
     *     cancelled_at: Long (Optional)
     *     request_counts (Optional): {
     *         total: int (Required)
     *         completed: int (Required)
     *         failed: int (Required)
     *     }
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * @param batchId The identifier of the batch.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return details for a single batch specified by the given batchID along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> getBatchWithResponseAsync(String batchId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.getBatch(OPEN_AI_ENDPOINT, batchId, accept, requestOptions, context));
    }

    /**
     * Gets details for a single batch specified by the given batchID.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     endpoint: String (Required)
     *     errors (Optional): {
     *         object: String (Required)
     *         data (Optional): [
     *              (Optional){
     *                 code: String (Optional)
     *                 message: String (Optional)
     *                 param: String (Optional)
     *                 line: Integer (Optional)
     *             }
     *         ]
     *     }
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *     output_file_id: String (Optional)
     *     error_file_id: String (Optional)
     *     created_at: long (Required)
     *     in_progress_at: Long (Optional)
     *     expires_at: Long (Optional)
     *     finalizing_at: Long (Optional)
     *     completed_at: Long (Optional)
     *     failed_at: Long (Optional)
     *     expired_at: Long (Optional)
     *     cancelling_at: Long (Optional)
     *     cancelled_at: Long (Optional)
     *     request_counts (Optional): {
     *         total: int (Required)
     *         completed: int (Required)
     *         failed: int (Required)
     *     }
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * @param batchId The identifier of the batch.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return details for a single batch specified by the given batchID along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> getBatchWithResponse(String batchId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.getBatchSync(OPEN_AI_ENDPOINT, batchId, accept, requestOptions, Context.NONE);
    }

    /**
     * Gets details for a single batch specified by the given batchID.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     endpoint: String (Required)
     *     errors (Optional): {
     *         object: String (Required)
     *         data (Optional): [
     *              (Optional){
     *                 code: String (Optional)
     *                 message: String (Optional)
     *                 param: String (Optional)
     *                 line: Integer (Optional)
     *             }
     *         ]
     *     }
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *     output_file_id: String (Optional)
     *     error_file_id: String (Optional)
     *     created_at: long (Required)
     *     in_progress_at: Long (Optional)
     *     expires_at: Long (Optional)
     *     finalizing_at: Long (Optional)
     *     completed_at: Long (Optional)
     *     failed_at: Long (Optional)
     *     expired_at: Long (Optional)
     *     cancelling_at: Long (Optional)
     *     cancelled_at: Long (Optional)
     *     request_counts (Optional): {
     *         total: int (Required)
     *         completed: int (Required)
     *         failed: int (Required)
     *     }
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * @param batchId The identifier of the batch.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return details for a single batch specified by the given batchID along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> cancelBatchWithResponseAsync(String batchId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil
            .withContext(context -> service.cancelBatch(OPEN_AI_ENDPOINT, batchId, accept, requestOptions, context));
    }

    /**
     * Gets details for a single batch specified by the given batchID.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     endpoint: String (Required)
     *     errors (Optional): {
     *         object: String (Required)
     *         data (Optional): [
     *              (Optional){
     *                 code: String (Optional)
     *                 message: String (Optional)
     *                 param: String (Optional)
     *                 line: Integer (Optional)
     *             }
     *         ]
     *     }
     *     input_file_id: String (Required)
     *     completion_window: String (Required)
     *     status: String(validating/failed/in_progress/finalizing/completed/expired/cancelling/cancelled) (Required)
     *     output_file_id: String (Optional)
     *     error_file_id: String (Optional)
     *     created_at: long (Required)
     *     in_progress_at: Long (Optional)
     *     expires_at: Long (Optional)
     *     finalizing_at: Long (Optional)
     *     completed_at: Long (Optional)
     *     failed_at: Long (Optional)
     *     expired_at: Long (Optional)
     *     cancelling_at: Long (Optional)
     *     cancelled_at: Long (Optional)
     *     request_counts (Optional): {
     *         total: int (Required)
     *         completed: int (Required)
     *         failed: int (Required)
     *     }
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     * }
     * }</pre>
     *
     * @param batchId The identifier of the batch.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return details for a single batch specified by the given batchID along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> cancelBatchWithResponse(String batchId, RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.cancelBatchSync(OPEN_AI_ENDPOINT, batchId, accept, requestOptions, Context.NONE);
    }
}
