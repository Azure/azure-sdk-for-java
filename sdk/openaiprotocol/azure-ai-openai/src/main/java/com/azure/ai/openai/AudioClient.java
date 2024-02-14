package com.azure.ai.openai;

import com.azure.ai.openai.models.AudioTranscription;
import com.azure.ai.openai.models.AudioTranscriptionOptions;
import com.azure.ai.openai.models.AudioTranslation;
import com.azure.ai.openai.models.AudioTranslationOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;

/**
 * The AudioClient class provides a client for interacting with the Azure OpenAI service.
 */
public final class AudioClient {

    private final OpenAIClient openAIClient;
    private final String model;

    AudioClient(OpenAIClient openAIClient, String model) {
        this.openAIClient = openAIClient;
        this.model = model;
    }

    /**
     * Gets transcribed text and associated metadata from provided spoken audio data. Audio will be transcribed in the
     * written language corresponding to the language it was spoken in.
     *
     * @param audioBytes the audio data to transcribe.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return transcribed text and associated metadata from provided spoken audio data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AudioTranscription transcribeAudio(BinaryData audioBytes) {
        return null;
    }

    /**
     * Gets transcribed text and associated metadata from provided spoken audio data. Audio will be transcribed in the
     * written language corresponding to the language it was spoken in.
     *
     * @param audioBytes the audio data to transcribe.
     * @param audioTranscriptionOptions The configuration information for an audio transcription request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return transcribed text and associated metadata from provided spoken audio data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AudioTranscription> transcribeAudioWithResponse(BinaryData audioBytes, AudioTranscriptionOptions audioTranscriptionOptions, RequestOptions requestOptions) {
        return null;
    }

    /**
     * Gets transcribed text and associated metadata from provided spoken audio data. Audio will be transcribed in the
     * written language corresponding to the language it was spoken in.
     *
     * @param requestBody The request body that needs to be sent to the server.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return transcribed text and associated metadata from provided spoken audio data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> transcribeAudioWithResponse(BinaryData requestBody, RequestOptions requestOptions) {
        return null;
    }

    /**
     * Gets English language transcribed text and associated metadata from provided spoken audio data.
     *
     * @param audioBytes the audio data to translate.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return english language transcribed text and associated metadata from provided spoken audio data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AudioTranslation translateAudio(BinaryData audioBytes) {
        return null;
    }

    /**
     * Gets English language transcribed text and associated metadata from provided spoken audio data.
     *
     * @param audioBytes the audio data to translate.
     * @param audioTranslationOptions The configuration information for an audio translation request.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return english language transcribed text and associated metadata from provided spoken audio data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AudioTranslation> translateAudioWithResponse(BinaryData audioBytes, AudioTranslationOptions audioTranslationOptions, RequestOptions requestOptions) {
        return null;
    }

    /**
     * Gets English language transcribed text and associated metadata from provided spoken audio data.
     *
     * @param requestBody The request body that needs to be sent to the server.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return english language transcribed text and associated metadata from provided spoken audio data.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> translateAudioWithResponse(BinaryData requestBody, RequestOptions requestOptions) {
        return null;
    }
}
