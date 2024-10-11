package com.azure.ai.openai;


import com.azure.ai.openai.models.realtime.RealtimeClientEvent;
import com.azure.ai.openai.models.realtime.RealtimeServerEvent;
import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.ai.openai.implementation.RealtimesImpl;
import java.util.List;

/**
 * Initializes a new instance of the synchronous OpenAIClient type.
 */
@ServiceClient(builder = OpenAIClientBuilder.class)
public final class RealtimeClient {

    @Generated
    private final RealtimesImpl serviceClient;

    /**
     * Initializes an instance of RealtimeClient class.
     *
     * @param serviceClient the service client implementation.
     */
    @Generated
    RealtimeClient(RealtimesImpl serviceClient) {
        this.serviceClient = serviceClient;
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
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> startRealtimeSessionWithResponse(BinaryData requestMessages,
                                                                 RequestOptions requestOptions) {
        return this.serviceClient.startRealtimeSessionWithResponse(requestMessages, requestOptions);
    }

    /**
     * Starts a real-time conversation session.
     *
     * @param requestMessages The requestMessages parameter.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response.
     */
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<RealtimeServerEvent> startRealtimeSession(List<RealtimeClientEvent> requestMessages) {
        // Generated convenience method for startRealtimeSessionWithResponse
        RequestOptions requestOptions = new RequestOptions();
        return startRealtimeSessionWithResponse(BinaryData.fromObject(requestMessages), requestOptions).getValue()
                .toObject(TYPE_REFERENCE_LIST_REALTIME_SERVER_EVENT);
    }

    @Generated
    private static final TypeReference<List<RealtimeServerEvent>> TYPE_REFERENCE_LIST_REALTIME_SERVER_EVENT
            = new TypeReference<List<RealtimeServerEvent>>() {
    };
}
