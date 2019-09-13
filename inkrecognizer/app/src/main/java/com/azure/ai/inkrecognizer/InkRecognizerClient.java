// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

/**
 * The InkRecognizerClient communicates with the service using default configuration settings
 * or settings provided by the caller. Service results are returned synchronously.
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
    private DisplayMetrics displayMetrics;
    private final OkHttpClient httpClient;

    InkRecognizerClient(
            String endpoint,
            InkRecognizerCredentials credentials
    ) {
        this.credentials = credentials;
        this.endpoint = endpoint;
        httpClient = new OkHttpClient.Builder()
                .build();
    }

    /**
     * Synchronously sends data to the service and generates a tree structure containing the recognition results.
     * @param strokes The ink strokes to recognize.
     * @return An InkRecognitionResult containing status codes, error messages (if applicable) and the recognition results
     * in a hierarchy.
     * @throws Exception
     */
    public Response<InkRecognitionRoot> recognizeInk(
            Iterable<InkStroke> strokes
    ) throws Exception {
        return recognizeInk(
                strokes,
                this.unit,
                this.unitMultiple,
                this.applicationKind,
                this.language,
                this.displayMetrics
        );
    }

    /**
     * Synchronously sends data to the service and generates a tree structure containing the recognition results.
     * @param strokes The ink strokes to recognize.
     * @param language The IETF BCP 47 language code (for ex. en-US, en-GB, hi-IN etc.) for the strokes. This is only
     * needed when the language is different from the default set when the client was instantiated.
     * @return An InkRecognitionResult containing status codes, error messages (if applicable) and the recognition results
     * in a hierarchy.
     * @throws Exception
     */
    public Response<InkRecognitionRoot> recognizeInk(
            Iterable<InkStroke> strokes,
            String language
    ) throws Exception {
        return recognizeInk(
                strokes,
                this.unit,
                this.unitMultiple,
                this.applicationKind,
                language,
                this.displayMetrics
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
     * @param displayMetrics A structure describing general information about the display, such as xdpi, ydpi, where the
     * application using this SDK will run on.
     * @return A InkRecognitionResult containing status codes, error messages (if applicable) and the recognition results
     * in a hierarchy.
     * @throws Exception
     */
    public Response<InkRecognitionRoot> recognizeInk(
            Iterable<InkStroke> strokes,
            InkPointUnit unit,
            float multiple,
            ApplicationKind applicationKind,
            String language,
            DisplayMetrics displayMetrics
    ) throws Exception {

        String requestJSON = Utils.createJSONForRequest(strokes, unit, multiple, applicationKind, language, displayMetrics);

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

        for (int retryAttempt = 0; retryAttempt < retryCount; ++retryAttempt) {
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (response.code() != 200) {
                    // If last attempt failed, return the error
                    if (retryAttempt == retryCount - 1) {
                        throw new Exception("Request unsuccessful: " + response);
                    } else {
                        continue;
                    }
                }
                // Successful response
                String responseString = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readValue(responseString, JsonNode.class);
                return new Response<>(response.code(), responseString, new InkRecognitionRoot(jsonResponse.get("recognitionUnits"), unit, displayMetrics));
            } catch (Exception e) {
            }
        }

        throw new Exception("Request unsuccessful");
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
        this.unit = Utils.getValueOrDefault(unit, InkPointUnit.PIXEL);
        return this;
    }

    InkRecognizerClient setUnitMultiple(float multiple) {
        this.unitMultiple = Utils.getValueOrDefault(multiple, 1.0f);
        return this;
    }

    InkRecognizerClient setServiceVersion(ServiceVersion version) {
        this.serviceVersion = Utils.getValueOrDefault(version, ServiceVersion.PREVIEW_1_0_0);
        return this;
    }

    InkRecognizerClient setDisplayMetrics(DisplayMetrics displayMetrics) {
        this.displayMetrics = displayMetrics;
        return this;
    }

}
