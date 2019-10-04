package com.azure.ai.inkrecognizer;

import com.azure.ai.inkrecognizer.model.InkRecognitionRoot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * The InkRecognizerAsyncClient communicates with the service using default configuration settings or settings provided
 * by the caller. Service results are returned asynchronously.
 * @author Microsoft
 * @version 1.0
 */
public final class InkRecognizerAsyncClient {

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

    InkRecognizerAsyncClient(
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
     * @return An InkRecognitionResult containing status codes, error messages (if applicable) and the recognition
     * results in a hierarchy.
     */
    public Mono<Response<InkRecognitionRoot>> recognizeInk(
            Iterable<InkStroke> strokes
    ) {
        return Mono.fromCallable(() -> recognizeInkHelper(
                strokes,
                this.unit,
                this.unitMultiple,
                this.applicationKind,
                this.language
        ));
    }

    /**
     * Synchronously sends data to the service and generates a tree structure containing the recognition results.
     * @param strokes The ink strokes to recognize.
     * @param language The IETF BCP 47 language code (for ex. en-US, en-GB, hi-IN etc.) for the strokes. This is only
     * needed when the language is different from the default set when the client was instantiated.
     * @return An InkRecognitionResult containing status codes, error messages (if applicable) and the recognition
     * results in a hierarchy.
     */
    public Mono<Response<InkRecognitionRoot>> recognizeInk(
            Iterable<InkStroke> strokes,
            String language
    ) {
        return Mono.fromCallable(() -> recognizeInkHelper(
                strokes,
                this.unit,
                this.unitMultiple,
                this.applicationKind,
                language
        ));
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
     */
    public Mono<Response<InkRecognitionRoot>> recognizeInk(
            Iterable<InkStroke> strokes,
            InkPointUnit unit,
            float multiple,
            ApplicationKind applicationKind,
            String language
    ) {
        return Mono.fromCallable(() -> recognizeInkHelper(
                strokes,
                unit,
                multiple,
                applicationKind,
                language
        ));
    }

    private Response<InkRecognitionRoot> recognizeInkHelper(
            Iterable<InkStroke> strokes,
            InkPointUnit unit,
            float multiple,
            ApplicationKind applicationKind,
            String language
    ) throws Exception {

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

    InkRecognizerAsyncClient setCredentials(InkRecognizerCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    InkRecognizerAsyncClient setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    InkRecognizerAsyncClient setRetryCount(int retries) {
        this.retryCount = retries > 0 ? retries : 3;
        return this;
    }

    InkRecognizerAsyncClient setRetryTimeout(int timeout) {
        this.retryTimeout = timeout > 0 ? timeout : 300;
        return this;
    }

    InkRecognizerAsyncClient setApplicationKind(ApplicationKind kind) {
        this.applicationKind = Utils.getValueOrDefault(kind, ApplicationKind.MIXED);
        return this;
    }

    InkRecognizerAsyncClient setLanguage(String language) {
        this.language = Utils.getValueOrDefault(language, "en-US");
        return this;
    }

    InkRecognizerAsyncClient setUnit(InkPointUnit unit) {
        this.unit = Utils.getValueOrDefault(unit, InkPointUnit.MM);
        return this;
    }

    InkRecognizerAsyncClient setUnitMultiple(float multiple) {
        this.unitMultiple = multiple > 0 ? multiple : 1.0f;
        return this;
    }

    InkRecognizerAsyncClient setServiceVersion(ServiceVersion version) {
        this.serviceVersion = Utils.getValueOrDefault(version, ServiceVersion.PREVIEW_1_0_0);
        return this;
    }

}