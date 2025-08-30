// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import static com.azure.ai.openai.implementation.OpenAIUtils.addAzureVersionToRequestOptions;

import com.azure.ai.openai.responses.implementation.NonAzureResponsesClientImpl;
import com.azure.ai.openai.responses.implementation.OpenAIServerSentEvents;
import com.azure.ai.openai.responses.implementation.ResponsesClientImpl;
import com.azure.ai.openai.responses.implementation.accesshelpers.CreateResponsesRequestAccessHelper;
import com.azure.ai.openai.responses.models.CreateResponseRequestAccept;
import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestIncludable;
import com.azure.ai.openai.responses.models.DeleteResponseResponse;
import com.azure.ai.openai.responses.models.ListInputItemsRequestOrder;
import com.azure.ai.openai.responses.models.ResponsesInputItemList;
import com.azure.ai.openai.responses.models.ResponsesItem;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesStreamEvent;
import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import java.nio.ByteBuffer;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Initializes a new instance of the asynchronous ResponsesClient type.
 */
@ServiceClient(builder = ResponsesClientBuilder.class, isAsync = true)
public final class ResponsesAsyncClient {

    @Generated
    private final ResponsesClientImpl serviceClient;

    private final NonAzureResponsesClientImpl nonAzureServiceClient;

    /**
     * Initializes an instance of ResponsesAsyncClient class.
     *
     * @param serviceClient the service client implementation.
     */
    ResponsesAsyncClient(ResponsesClientImpl serviceClient) {
        this.serviceClient = serviceClient;
        this.nonAzureServiceClient = null;
    }

    /**
     * Initializes an instance of ResponsesAsyncClient class for NonAzure Implementation.
     *
     * @param serviceClient the service client implementation.
     */
    ResponsesAsyncClient(NonAzureResponsesClientImpl serviceClient) {
        this.serviceClient = null;
        this.nonAzureServiceClient = serviceClient;
    }

    /**
     * Creates a model response.
     * <p><strong>Request Body Schema</strong></p>
     *
     * <pre>
     * {@code
     * {
     *     model: String(o1/o1-2024-12-17/o1-preview/o1-preview-2024-09-12/o1-mini/o1-mini-2024-09-12/gpt-4o/gpt-4o-2024-11-20/gpt-4o-2024-08-06/gpt-4o-2024-05-13/gpt-4o-audio-preview/gpt-4o-audio-preview-2024-10-01/gpt-4o-audio-preview-2024-12-17/gpt-4o-mini-audio-preview/gpt-4o-mini-audio-preview-2024-12-17/chatgpt-4o-latest/gpt-4o-mini/gpt-4o-mini-2024-07-18/gpt-4-turbo/gpt-4-turbo-2024-04-09/gpt-4-0125-preview/gpt-4-turbo-preview/gpt-4-1106-preview/gpt-4-vision-preview/gpt-4/gpt-4-0314/gpt-4-0613/gpt-4-32k/gpt-4-32k-0314/gpt-4-32k-0613/gpt-3.5-turbo/gpt-3.5-turbo-16k/gpt-3.5-turbo-0301/gpt-3.5-turbo-0613/gpt-3.5-turbo-1106/gpt-3.5-turbo-0125/gpt-3.5-turbo-16k-0613) (Required)
     *     input (Required): [
     *          (Required){
     *             type: String(message/file_search_call/code_interpreter_call/function_call/function_call_output/computer_call/computer_call_output) (Required)
     *             id: String (Required)
     *         }
     *     ]
     *     previous_response_id: String (Optional)
     *     include (Optional): [
     *         String(message.output_text.logprobs/file_search_call.results) (Optional)
     *     ]
     *     tools (Optional): [
     *          (Optional){
     *             type: String(code_interpreter/function/file_search/web_search/computer-preview) (Required)
     *         }
     *     ]
     *     instructions: String (Optional)
     *     reasoning_effort: String(low/medium/high) (Optional)
     *     modalities (Optional): [
     *         String(text/audio) (Optional)
     *     ]
     *     text (Optional): {
     *         format (Optional): {
     *             type: String(text/json_object/json_schema) (Required)
     *         }
     *         stop: BinaryData (Optional)
     *     }
     *     audio (Optional): {
     *         voice: String(alloy/ash/ballad/coral/echo/sage/shimmer/verse) (Required)
     *         format: String(wav/mp3/flac/opus/pcm16) (Required)
     *     }
     *     tool_choice: BinaryData (Optional)
     *     temperature: Double (Optional)
     *     top_p: Double (Optional)
     *     top_logprobs: Integer (Optional)
     *     presence_penalty: Double (Optional)
     *     frequency_penalty: Double (Optional)
     *     max_completion_tokens: Integer (Optional)
     *     truncation: String(auto/disabled) (Optional)
     *     user: String (Optional)
     *     service_tier: String(auto/default) (Optional)
     *     metadata (Optional): {
     *         String: String (Required)
     *     }
     *     parallel_tool_calls: Boolean (Optional)
     *     stream: Boolean (Optional)
     *     store: Boolean (Optional)
     * }
     * }
     * </pre>
     *
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>
     * {@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     created_at: long (Required)
     *     status: String(completed/in_progress/failed/incomplete) (Required)
     *     error (Required): {
     *         message: String (Required)
     *         type: String (Required)
     *         param: String (Required)
     *         code: String (Required)
     *     }
     *     incomplete_details (Required): {
     *         reason: String(max_output_tokens/content_filter) (Required)
     *     }
     *     input (Required): [
     *          (Required){
     *         }
     *     ]
     *     instructions: String (Required)
     *     max_output_tokens: Integer (Required)
     *     model: String (Required)
     *     output (Required): [
     *          (Required){
     *             type: String(message/file_search_call/code_interpreter_call/function_call/function_call_output/computer_call/computer_call_output) (Required)
     *             id: String (Required)
     *         }
     *     ]
     *     parallel_tool_calls: boolean (Required)
     *     previous_response_id: String (Required)
     *     reasoning_effort: String(low/medium/high) (Required)
     *     store: boolean (Required)
     *     temperature: double (Required)
     *     text (Required): {
     *         stop (Optional): [
     *             String (Optional)
     *         ]
     *         format (Required): {
     *             type: String(text/json_object/json_schema) (Required)
     *         }
     *     }
     *     tool_choice: BinaryData (Required)
     *     tools (Required): [
     *          (Required){
     *             type: String(code_interpreter/function/file_search/web_search/computer-preview) (Required)
     *         }
     *     ]
     *     top_p: double (Required)
     *     truncation: String(auto/disabled) (Required)
     *     usage (Required): {
     *         input_tokens: int (Required)
     *         output_tokens: int (Required)
     *         total_tokens: int (Required)
     *         output_tokens_details (Required): {
     *             reasoning_tokens: int (Required)
     *         }
     *     }
     *     user: String (Required)
     *     metadata (Required): {
     *         String: String (Required)
     *     }
     * }
     * }
     * </pre>
     *
     * @param accept The accept parameter. Allowed values: "application/json", "text/event-stream".
     * @param requestBody The requestBody parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<BinaryData>> createResponseWithResponse(String accept, BinaryData requestBody,
        RequestOptions requestOptions) {
        if (nonAzureServiceClient != null) {
            return nonAzureServiceClient.createResponseWithResponseAsync(accept, requestBody, requestOptions);
        } else {
            addAzureVersionToRequestOptions(serviceClient.getEndpoint(), requestOptions,
                serviceClient.getServiceVersion());
            return this.serviceClient.createResponseWithResponseAsync(accept, requestBody, requestOptions);
        }
    }

    /**
     * Retrieves a model response with the given ID.
     * <p><strong>Query Parameters</strong></p>
     * <table border="1">
     * <caption>Query Parameters</caption>
     * <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     * <tr><td>include</td><td>List&lt;String&gt;</td><td>No</td><td>Specifies additional output data to include in the
     * model response. In the form of "," separated string.</td></tr>
     * </table>
     * You can add these to a request with {@link RequestOptions#addQueryParam}
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>
     * {@code
     * {
     *     id: String (Required)
     *     object: String (Required)
     *     created_at: long (Required)
     *     status: String(completed/in_progress/failed/incomplete) (Required)
     *     error (Required): {
     *         message: String (Required)
     *         type: String (Required)
     *         param: String (Required)
     *         code: String (Required)
     *     }
     *     incomplete_details (Required): {
     *         reason: String(max_output_tokens/content_filter) (Required)
     *     }
     *     input (Required): [
     *          (Required){
     *         }
     *     ]
     *     instructions: String (Required)
     *     max_output_tokens: Integer (Required)
     *     model: String (Required)
     *     output (Required): [
     *          (Required){
     *             type: String(message/file_search_call/code_interpreter_call/function_call/function_call_output/computer_call/computer_call_output) (Required)
     *             id: String (Required)
     *         }
     *     ]
     *     parallel_tool_calls: boolean (Required)
     *     previous_response_id: String (Required)
     *     reasoning_effort: String(low/medium/high) (Required)
     *     store: boolean (Required)
     *     temperature: double (Required)
     *     text (Required): {
     *         stop (Optional): [
     *             String (Optional)
     *         ]
     *         format (Required): {
     *             type: String(text/json_object/json_schema) (Required)
     *         }
     *     }
     *     tool_choice: BinaryData (Required)
     *     tools (Required): [
     *          (Required){
     *             type: String(code_interpreter/function/file_search/web_search/computer-preview) (Required)
     *         }
     *     ]
     *     top_p: double (Required)
     *     truncation: String(auto/disabled) (Required)
     *     usage (Required): {
     *         input_tokens: int (Required)
     *         output_tokens: int (Required)
     *         total_tokens: int (Required)
     *         output_tokens_details (Required): {
     *             reasoning_tokens: int (Required)
     *         }
     *     }
     *     user: String (Required)
     *     metadata (Required): {
     *         String: String (Required)
     *     }
     * }
     * }
     * </pre>
     *
     * @param responseId The ID of the response to retrieve.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<BinaryData>> getResponseWithResponse(String responseId, RequestOptions requestOptions) {
        if (nonAzureServiceClient != null) {
            return nonAzureServiceClient.getResponseWithResponseAsync(responseId, requestOptions);
        } else {
            addAzureVersionToRequestOptions(serviceClient.getEndpoint(), requestOptions,
                serviceClient.getServiceVersion());
            return this.serviceClient.getResponseWithResponseAsync(responseId, requestOptions);
        }
    }

    /**
     * Returns a list of input items for a given response.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>
     * {@code
     * {
     *     object: String (Required)
     *     data (Required): [
     *          (Required){
     *             type: String(message/file_search_call/code_interpreter_call/function_call/function_call_output/computer_call/computer_call_output) (Required)
     *             id: String (Required)
     *         }
     *     ]
     *     first_id: String (Required)
     *     last_id: String (Required)
     *     has_more: boolean (Required)
     * }
     * }
     * </pre>
     *
     * @param responseId The ID of the response to retrieve.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<BinaryData>> listInputItemsWithResponse(String responseId, RequestOptions requestOptions) {
        if (nonAzureServiceClient != null) {
            return nonAzureServiceClient.listInputItemsWithResponseAsync(responseId, requestOptions);
        } else {
            addAzureVersionToRequestOptions(serviceClient.getEndpoint(), requestOptions,
                serviceClient.getServiceVersion());
            return this.serviceClient.listInputItemsWithResponseAsync(responseId, requestOptions);
        }
    }

    /**
     * Retrieves a model response with the given ID.
     *
     * @param responseId The ID of the response to retrieve.
     * @param includables The includables parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResponsesResponse> getResponse(String responseId, List<CreateResponsesRequestIncludable> includables) {
        // Generated convenience method for getResponseWithResponse
        RequestOptions requestOptions = new RequestOptions();
        if (includables != null) {
            for (CreateResponsesRequestIncludable paramItemValue : includables) {
                if (paramItemValue != null) {
                    requestOptions.addQueryParam("include[]", paramItemValue.toString(), false);
                }
            }
        }
        return getResponseWithResponse(responseId, requestOptions).flatMap(FluxUtil::toMono)
            .map(protocolMethodData -> protocolMethodData.toObject(ResponsesResponse.class));
    }

    /**
     * Retrieves a model response with the given ID.
     *
     * @param responseId The ID of the response to retrieve.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResponsesResponse> getResponse(String responseId) {
        // Generated convenience method for getResponseWithResponse
        RequestOptions requestOptions = new RequestOptions();
        return getResponseWithResponse(responseId, requestOptions).flatMap(FluxUtil::toMono)
            .map(protocolMethodData -> protocolMethodData.toObject(ResponsesResponse.class));
    }

    /**
     * Creates a model response.
     *
     * @param accept The accept parameter.
     * @param requestBody The requestBody parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<ResponsesResponse> createResponse(CreateResponseRequestAccept accept,
        CreateResponsesRequest requestBody) {
        // Generated convenience method for createResponseWithResponse
        RequestOptions requestOptions = new RequestOptions();
        return createResponseWithResponse(accept.toString(), BinaryData.fromObject(requestBody), requestOptions)
            .flatMap(FluxUtil::toMono)
            .map(protocolMethodData -> protocolMethodData.toObject(ResponsesResponse.class));
    }

    /**
     * Creates a model response.
     *
     * @param requestBody The requestBody parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResponsesResponse> createResponse(CreateResponsesRequest requestBody) {
        RequestOptions requestOptions = new RequestOptions();
        CreateResponsesRequestAccessHelper.setStream(requestBody, false);
        return createResponseWithResponse(CreateResponseRequestAccept.APPLICATION_JSON.toString(),
            BinaryData.fromObject(requestBody), requestOptions).flatMap(FluxUtil::toMono)
                .map(protocolMethodData -> protocolMethodData.toObject(ResponsesResponse.class));
    }

    /**
     * Creates a model response.
     *
     * @param requestBody The requestBody parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResponsesResponse> createResponse(CreateResponsesRequest requestBody, RequestOptions requestOptions) {
        CreateResponsesRequestAccessHelper.setStream(requestBody, false);
        return createResponseWithResponse(CreateResponseRequestAccept.APPLICATION_JSON.toString(),
            BinaryData.fromObject(requestBody), requestOptions).flatMap(FluxUtil::toMono)
                .map(protocolMethodData -> protocolMethodData.toObject(ResponsesResponse.class));
    }

    /**
     * Creates a model response.
     *
     * @param requestBody The requestBody parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ResponsesStreamEvent> createResponseStream(CreateResponsesRequest requestBody) {
        RequestOptions requestOptions = new RequestOptions();
        CreateResponsesRequestAccessHelper.setStream(requestBody, true);
        Flux<ByteBuffer> response = createResponseWithResponse(CreateResponseRequestAccept.TEXT_EVENT_STREAM.toString(),
            BinaryData.fromObject(requestBody), requestOptions).flatMapMany(it -> it.getValue().toFluxByteBuffer());
        return new OpenAIServerSentEvents(response).getEvents();
    }

    /**
     * Creates a model response.
     *
     * @param requestBody The requestBody parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ResponsesStreamEvent> createResponseStream(CreateResponsesRequest requestBody,
        RequestOptions requestOptions) {
        CreateResponsesRequestAccessHelper.setStream(requestBody, true);
        Flux<ByteBuffer> response = createResponseWithResponse(CreateResponseRequestAccept.TEXT_EVENT_STREAM.toString(),
            BinaryData.fromObject(requestBody), requestOptions).flatMapMany(it -> it.getValue().toFluxByteBuffer());
        return new OpenAIServerSentEvents(response).getEvents();
    }

    /**
     * Deletes a response by ID.
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>
     * {@code
     * {
     *     object: String (Required)
     *     id: String (Required)
     *     deleted: boolean (Required)
     * }
     * }
     * </pre>
     *
     * @param responseId The responseId parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<Response<BinaryData>> deleteResponseWithResponse(String responseId, RequestOptions requestOptions) {
        if (nonAzureServiceClient != null) {
            return nonAzureServiceClient.deleteResponseWithResponseAsync(responseId, requestOptions);
        } else {
            addAzureVersionToRequestOptions(serviceClient.getEndpoint(), requestOptions,
                serviceClient.getServiceVersion());
            return this.serviceClient.deleteResponseWithResponseAsync(responseId, requestOptions);
        }
    }

    /**
     * Deletes a response by ID.
     *
     * @param responseId The responseId parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeleteResponseResponse> deleteResponse(String responseId, RequestOptions requestOptions) {
        return deleteResponseWithResponse(responseId, requestOptions).flatMap(FluxUtil::toMono)
            .map(protocolMethodData -> protocolMethodData.toObject(DeleteResponseResponse.class));
    }

    /**
     * Deletes a response by ID.
     *
     * @param responseId The responseId parameter.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DeleteResponseResponse> deleteResponse(String responseId) {
        RequestOptions requestOptions = new RequestOptions();
        return deleteResponseWithResponse(responseId, requestOptions).flatMap(FluxUtil::toMono)
            .map(protocolMethodData -> protocolMethodData.toObject(DeleteResponseResponse.class));
    }

    /**
     * Returns a list of input items for a given response.
     *
     * @param responseId The ID of the response to retrieve.
     * @param limit A limit on the number of objects to be returned. Limit can range between 1 and 100, and the
     * default is 20.
     * @param order Sort order by the `created_at` timestamp of the objects. `asc` for ascending order and`desc`
     * for descending order.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ResponsesItem> listInputItems(String responseId, Integer limit, ListInputItemsRequestOrder order) {
        RequestOptions requestOptions = new RequestOptions();
        if (limit != null) {
            requestOptions.addQueryParam("limit", String.valueOf(limit), false);
        }
        if (order != null) {
            requestOptions.addQueryParam("order", order.toString(), false);
        }

        return new PagedFlux<>(() -> {
            Mono<Response<BinaryData>> responseMono = listInputItemsWithResponse(responseId, requestOptions);
            return responseMono.map(response -> {
                ResponsesInputItemList pagedItems = response.getValue().toObject(ResponsesInputItemList.class);
                return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    pagedItems.getData(), pagedItems.isHasMore() ? pagedItems.getLastId() : null,
                    response.getHeaders());
            });
        }, nextLink -> {
            RequestOptions nextPageRequestOptions = new RequestOptions();
            if (limit != null) {
                nextPageRequestOptions.addQueryParam("limit", String.valueOf(limit), false);
            }
            if (order != null) {
                nextPageRequestOptions.addQueryParam("order", order.toString(), false);
            }
            // nextLink is always define, as it being `null` is the break condition for the loop
            nextPageRequestOptions.addQueryParam("after", nextLink, false);

            Mono<Response<BinaryData>> responseMono = listInputItemsWithResponse(responseId, nextPageRequestOptions);
            return responseMono.map(response -> {
                ResponsesInputItemList pagedItems = response.getValue().toObject(ResponsesInputItemList.class);
                return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    pagedItems.getData(), pagedItems.isHasMore() ? pagedItems.getLastId() : null,
                    response.getHeaders());
            });
        });
    }

    /**
     * Returns a list of input items for a given response.
     *
     * @param responseId The ID of the response to retrieve.
     * @param limit A limit on the number of objects to be returned. Limit can range between 1 and 100, and the
     * default is 20.
     * @param order Sort order by the `created_at` timestamp of the objects. `asc` for ascending order and`desc`
     * for descending order.
     * @param after A cursor for use in pagination. `after` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your
     * subsequent call can include after=obj_foo in order to fetch the next page of the list.
     * @param before A cursor for use in pagination. `before` is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo, your
     * subsequent call can include before=obj_foo in order to fetch the previous page of the list.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<ResponsesInputItemList> listInputItems(String responseId, Integer limit,
        ListInputItemsRequestOrder order, String after, String before) {
        // Generated convenience method for listInputItemsWithResponse
        RequestOptions requestOptions = new RequestOptions();
        if (limit != null) {
            requestOptions.addQueryParam("limit", String.valueOf(limit), false);
        }
        if (order != null) {
            requestOptions.addQueryParam("order", order.toString(), false);
        }
        if (after != null) {
            requestOptions.addQueryParam("after", after, false);
        }
        if (before != null) {
            requestOptions.addQueryParam("before", before, false);
        }
        return listInputItemsWithResponse(responseId, requestOptions).flatMap(FluxUtil::toMono)
            .map(protocolMethodData -> protocolMethodData.toObject(ResponsesInputItemList.class));
    }

    /**
     * Returns a list of input items for a given response.
     *
     * @param responseId The ID of the response to retrieve.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response body on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    private Mono<ResponsesInputItemList> listInputItems(String responseId) {
        // Generated convenience method for listInputItemsWithResponse
        RequestOptions requestOptions = new RequestOptions();
        return listInputItemsWithResponse(responseId, requestOptions).flatMap(FluxUtil::toMono)
            .map(protocolMethodData -> protocolMethodData.toObject(ResponsesInputItemList.class));
    }
}
