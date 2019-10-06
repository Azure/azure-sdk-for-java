// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

import com.microsoft.azure.cognitiveservices.inkrecognizer.model.InkRecognitionRoot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.concurrent.TimeUnit;

/**
 * The InkRecognizerClient communicates with the service using default configuration settings or settings provided by
 * the caller. Service results are returned synchronously.
 * @author Microsoft
 * @version 1.0
 */
public final class InkRecognizerClient {

    private InkRecognizerCredentials credentials;
    private String endpoint;
    private int retryCount;
    private int retryTimeout;
    private ApplicationKind applicationKind;
    private String language;
    private InkPointUnit unit;
    private float unitMultiple;
    private ServiceVersion serviceVersion;
    private final OkHttpClient httpClient;

    InkRecognizerClient(
        String endpoint,
        InkRecognizerCredentials credentials) {
        this.credentials = credentials;
        this.endpoint = endpoint;
        httpClient = new OkHttpClient.Builder()
            .build();
    }

    /**
     * Synchronously sends data to the service and generates a tree structure containing the recognition results.
     * @param strokes The ink strokes to recognize.
     * @return An InkRecognitionResult containing status codes, error messages (if applicable) and the recognition
     * results in a hierarchy.
     * @throws Exception Exception thrown while trying to recognize ink
     */
    public Response<InkRecognitionRoot> recognizeInk(
        Iterable<InkStroke> strokes) throws Exception {
        return recognizeInk(
            strokes,
            this.unit,
            this.unitMultiple,
            this.applicationKind,
            this.language
        );
    }

    /**
     * Synchronously sends data to the service and generates a tree structure containing the recognition results.
     * @param strokes The ink strokes to recognize.
     * @param language The IETF BCP 47 language code (for ex. en-US, en-GB, hi-IN etc.) for the strokes. This is only
     * needed when the language is different from the default set when the client was instantiated.
     * @return An InkRecognitionResult containing status codes, error messages (if applicable) and the recognition
     * results in a hierarchy.
     * @throws Exception Exception thrown while trying to recognize ink
     */
    public Response<InkRecognitionRoot> recognizeInk(
        Iterable<InkStroke> strokes,
        String language) throws Exception {
        return recognizeInk(
            strokes,
            this.unit,
            this.unitMultiple,
            this.applicationKind,
            language
        );
    }

    /**
     * Synchronously sends data to the service and generates a tree structure containing the recognition results.
     * @param strokes The ink strokes to recognize.
     * @param unit The physical unit for the points in the stroke.
     * @param multiple A multiplier applied to the unit value to indicate the true unit being used. This allows the
     * caller to specify values in a fraction or multiple of a unit.
     * @param applicationKind The domain of the application (Writing or Drawing. The default is "Mixed").
     * @param language The IETF BCP 47 language code (for ex. en-US, en-GB, hi-IN etc.) for the strokes. This is only
     * needed when the language is different from the default set when the client was instantiated.
     * @return A InkRecognitionResult containing status codes, error messages (if applicable) and the recognition
     * results in a hierarchy.
     * @throws Exception Exception thrown while trying to recognize ink
     */
    public Response<InkRecognitionRoot> recognizeInk(
        Iterable<InkStroke> strokes,
        InkPointUnit unit,
        float multiple,
        ApplicationKind applicationKind,
        String language) throws Exception {

        String requestJSON = Utils.createJSONForRequest(strokes, unit, multiple, applicationKind, language);

        MediaType mediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = httpClient.newBuilder()
            .connectTimeout(retryTimeout, TimeUnit.MILLISECONDS)
            .build();
        RequestBody body = RequestBody.create(requestJSON, mediaTypeJSON);
        Request request = new Request.Builder()
            .url(endpoint + serviceVersion.toString())
            .put(body)
            .build();
        request = credentials.SetRequestCredentials(request);

        okhttp3.Response response = null;
        for (int retryAttempt = 0; retryAttempt < retryCount; ++retryAttempt) {
            try {
                response = client.newCall(request).execute();
                if (response.code() == 200) {
                    // Successful response
                    String responseString = response.body().string();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonResponse = objectMapper.readValue(responseString, JsonNode.class);
                    return new Response<>(response.code(), responseString, new InkRecognitionRoot(jsonResponse.get("recognitionUnits")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        throw new Exception("Request unsuccessful" + response);
    }

    InkRecognizerClient setCredentials(InkRecognizerCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    InkRecognizerClient setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    InkRecognizerClient setRetryCount(int retries) {
        this.retryCount = retries > 0 ? retries : 3;
        return this;
    }

    InkRecognizerClient setRetryTimeout(int timeout) {
        this.retryTimeout = timeout > 0 ? timeout : 300;
        return this;
    }

    InkRecognizerClient setApplicationKind(ApplicationKind kind) {
        this.applicationKind = Utils.getValueOrDefault(kind, ApplicationKind.MIXED);
        return this;
    }

    InkRecognizerClient setLanguage(String language) {
        this.language = Utils.getValueOrDefault(language, "en-US");
        return this;
    }

    InkRecognizerClient setUnit(InkPointUnit unit) {
        this.unit = Utils.getValueOrDefault(unit, InkPointUnit.MM);
        return this;
    }

    InkRecognizerClient setUnitMultiple(float multiple) {
        this.unitMultiple = multiple > 0 ? multiple : 1.0f;
        return this;
    }

    InkRecognizerClient setServiceVersion(ServiceVersion version) {
        this.serviceVersion = Utils.getValueOrDefault(version, ServiceVersion.PREVIEW_1_0_0);
        return this;
    }

}
