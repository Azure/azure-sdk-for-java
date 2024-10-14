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
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to all the operations defined in Realtimes.
 */
public class RealtimesImpl {
    /**
     * The proxy service used to perform REST calls.
     */
    private final RealtimesService service;

    private final String endpoint;

    RealtimesImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String endpoint) {
        this.service
                = RestProxy.create(RealtimesService.class, httpPipeline, serializerAdapter);
        this.endpoint = endpoint;
    }

    /**
     * The interface defining all the services for OpenAIClientRealtimes to be used by the proxy service to perform REST
     * calls.
     */
    @Host("{endpoint}")
    @ServiceInterface(name = "OpenAIClientRealtime")
    public interface RealtimesService {
        @Post("/realtime")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Mono<Response<BinaryData>> startRealtimeSession(@HostParam("endpoint") String endpoint, @HeaderParam("accept") String accept,
                                                        @BodyParam("application/json") BinaryData requestMessages, RequestOptions requestOptions, Context context);

        @Post("/realtime")
        @ExpectedResponses({ 200 })
        @UnexpectedResponseExceptionType(value = ClientAuthenticationException.class, code = { 401 })
        @UnexpectedResponseExceptionType(value = ResourceNotFoundException.class, code = { 404 })
        @UnexpectedResponseExceptionType(value = ResourceModifiedException.class, code = { 409 })
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        Response<BinaryData> startRealtimeSessionSync(@HostParam("endpoint") String endpoint, @HeaderParam("accept") String accept,
                                                      @BodyParam("application/json") BinaryData requestMessages, RequestOptions requestOptions, Context context);
    }

    /**
     * Starts a real-time conversation session.
     * <p><strong>Request Body Schema</strong></p>
     *
     * <pre>{@code
     * [
     *      (Required){
     *         type: String(session.update/input_audio_buffer.append/input_audio_buffer.commit/input_audio_buffer.clear/conversation.item.create/conversation.item.delete/conversation.item.truncate/response.create/response.cancel) (Required)
     *         event_id: String (Optional)
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * [
     *      (Required){
     *         type: String(session.created/session.updated/conversation.created/conversation.item.created/conversation.item.deleted/conversation.item.truncated/response.created/response.done/rate_limits.updated/response.output_item.added/response.output_item.done/response.content_part.added/response.content_part.done/response.audio.delta/response.audio.done/response.audio_transcript.delta/response.audio_transcript.done/response.text.delta/response.text.done/response.function_call_arguments.delta/response.function_call_arguments.done/input_audio_buffer.speech_started/input_audio_buffer.speech_stopped/conversation.item.input_audio_transcription.completed/conversation.item.input_audio_transcription.failed/input_audio_buffer.committed/input_audio_buffer.cleared/error) (Required)
     *         event_id: String (Required)
     *     }
     * ]
     * }</pre>
     *
     * @param requestMessages The requestMessages parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BinaryData>> startRealtimeSessionWithResponseAsync(BinaryData requestMessages,
                                                                            RequestOptions requestOptions) {
        final String accept = "application/json";
        return FluxUtil
                .withContext(context -> service.startRealtimeSession(this.endpoint, accept, requestMessages, requestOptions, context));
    }

    /**
     * Starts a real-time conversation session.
     * <p><strong>Request Body Schema</strong></p>
     *
     * <pre>{@code
     * [
     *      (Required){
     *         type: String(session.update/input_audio_buffer.append/input_audio_buffer.commit/input_audio_buffer.clear/conversation.item.create/conversation.item.delete/conversation.item.truncate/response.create/response.cancel) (Required)
     *         event_id: String (Optional)
     *     }
     * ]
     * }</pre>
     *
     * <p><strong>Response Body Schema</strong></p>
     *
     * <pre>{@code
     * [
     *      (Required){
     *         type: String(session.created/session.updated/conversation.created/conversation.item.created/conversation.item.deleted/conversation.item.truncated/response.created/response.done/rate_limits.updated/response.output_item.added/response.output_item.done/response.content_part.added/response.content_part.done/response.audio.delta/response.audio.done/response.audio_transcript.delta/response.audio_transcript.done/response.text.delta/response.text.done/response.function_call_arguments.delta/response.function_call_arguments.done/input_audio_buffer.speech_started/input_audio_buffer.speech_stopped/conversation.item.input_audio_transcription.completed/conversation.item.input_audio_transcription.failed/input_audio_buffer.committed/input_audio_buffer.cleared/error) (Required)
     *         event_id: String (Required)
     *     }
     * ]
     * }</pre>
     *
     * @param requestMessages The requestMessages parameter.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the response body along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> startRealtimeSessionWithResponse(BinaryData requestMessages,
                                                                 RequestOptions requestOptions) {
        final String accept = "application/json";
        return service.startRealtimeSessionSync(this.endpoint, accept, requestMessages, requestOptions, Context.NONE);
    }
}
