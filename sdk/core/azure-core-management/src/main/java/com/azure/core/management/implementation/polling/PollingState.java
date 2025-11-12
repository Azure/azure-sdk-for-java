// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

/**
 * A type representing state of Azure Resource Manager (ARM) long-running-operation (LRO)
 * and it's polling.
 */
public final class PollingState implements JsonSerializable<PollingState> {
    @JsonIgnore
    private static final ClientLogger LOGGER = new ClientLogger(PollingState.class);
    @JsonIgnore
    private static final String KEY = "3c7cac4f-acbb-4671-b6b4-edf2d3010041";
    @JsonIgnore
    private SerializerAdapter serializerAdapter;

    @JsonProperty(value = "pollingType", required = true)
    private PollingType pollingType;
    @JsonProperty(value = "lroResponseStatusCode", required = true)
    private int lroResponseStatusCode;
    @JsonProperty(value = "lroRequestMethod", required = true)
    private HttpMethod lroRequestMethod;
    @JsonProperty(value = "lroOperationUri", required = true)
    private URL lroOperationUri;
    @JsonProperty(value = "pollDelay")
    private Duration pollDelay;
    @JsonProperty(value = "asyncOperationData")
    private AzureAsyncOperationData azureAsyncOperationData;
    @JsonProperty(value = "locationData")
    private LocationData locationData;
    @JsonProperty(value = "provisioningData")
    private ProvisioningStateData provisioningStateData;
    @JsonProperty(value = "synchronouslySucceededLroData")
    private SynchronouslySucceededLroData synchronouslySucceededLroData;
    @JsonProperty(value = "synchronouslyFailedLroData")
    private SynchronouslyFailedLroData synchronouslyFailedLroData;
    @JsonProperty(value = "lastResponseBody")
    private String lastResponseBody;

    PollingState() {
    }

    /**
     * Creates PollingState from the request-response of a long-running-operation api call.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param lroHttpRequest the http request that initiated the LRO api call
     * @param lroResponseStatusCode the response status code of LRO init api call
     * @param lroResponseHeaders the response headers of LRO init api call
     * @param lroResponseBody the response body of LRO init api call
     * @return the PollingState
     * @throws IllegalArgumentException if the http method is not PUT, PATCH, POST or DELETE
     */
    public static PollingState create(SerializerAdapter serializerAdapter, HttpRequest lroHttpRequest,
        int lroResponseStatusCode, HttpHeaders lroResponseHeaders, String lroResponseBody) {
        final HttpMethod httpMethod = lroHttpRequest.getHttpMethod();
        if (httpMethod != HttpMethod.PUT
            && httpMethod != HttpMethod.PATCH
            && httpMethod != HttpMethod.POST
            && httpMethod != HttpMethod.DELETE) {
            throw new IllegalArgumentException(
                "Long-running-operation supported only" + " for PUT, PATCH, POST or DELETE verb.");
        }
        PollingState pollingState = new PollingState(serializerAdapter, lroHttpRequest.getHttpMethod(),
            lroHttpRequest.getUrl(), lroResponseStatusCode, getRetryAfter(lroResponseHeaders), lroResponseBody);
        switch (pollingState.lroResponseStatusCode) {
            case 200:
                return pollingState.initializeDataFor200StatusCode(lroResponseHeaders, lroResponseBody);

            case 201:
                return pollingState.initializeDataFor201StatusCode(lroResponseHeaders, lroResponseBody);

            case 202:
                return pollingState.initializeDataFor202StatusCode(lroResponseHeaders, lroResponseBody);

            case 204:
                return pollingState.initializeDataFor204StatusCode();

            default:
                return pollingState.initializeDataForUnknownStatusCode(lroResponseHeaders, lroResponseBody);
        }
    }

    /**
     * Create PollingState from the given the context.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param context the context to look for the PollingState
     * @param <T> the type of the poll response.
     * @return the PollingState from the context
     * @throws IllegalArgumentException if the context does not contain a PollingState
     * @throws RuntimeException if there is an error while retrieving the PollingState from the context
     */
    public static <T> PollingState from(SerializerAdapter serializerAdapter, PollingContext<T> context) {
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        String value = context.getData(KEY);
        if (CoreUtils.isNullOrEmpty(value)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The provided context does not contain" + " serialized PollingState."));
        }
        try (JsonReader jsonReader = JsonProviders.createReader(value)) {
            PollingState pollingState = PollingState.fromJson(jsonReader);
            return pollingState.setSerializer(serializerAdapter);
        } catch (IOException ioe) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Failed to deserialize '" + value + "' to PollingState.", ioe));
        }
    }

    /**
     * Create PollingState from the given the string.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param value the string value
     * @return the PollingState
     * @throws IllegalArgumentException if the value is null or empty
     * @throws RuntimeException if there is an error while decoding the string
     */
    public static PollingState from(SerializerAdapter serializerAdapter, String value) {
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        if (CoreUtils.isNullOrEmpty(value)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'value' is required"));
        }

        try (JsonReader jsonReader = JsonProviders.createReader(value)) {
            PollingState pollingState = PollingState.fromJson(jsonReader);
            return pollingState.setSerializer(serializerAdapter);
        } catch (IOException ioe) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to deserialize '" + value, ioe));
        }
    }

    /**
     * Store the current PollingState in the given context.
     *
     * @param context the context to store the PollingState
     * @param <T> the type of the poll response.
     * @throws RuntimeException if there is an error while storing the PollingState in the context
     */
    public <T> void store(PollingContext<T> context) {
        try {
            context.setData(KEY, this.serializerAdapter.serialize(this, SerializerEncoding.JSON));
        } catch (IOException ioe) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to serialize the PollingState.", ioe));
        }
    }

    /**
     * Serializes the current PollingState into a continuation token string that can be used to resume
     * the long-running operation at a later time or in a different process.
     * <p>
     * The continuation token is a Base64-encoded JSON representation of the polling state. It contains
     * all necessary information to reconstruct the poller and continue polling the operation, including:
     * <ul>
     * <li>Operation URL and HTTP method</li>
     * <li>Current provisioning state</li>
     * <li>Polling URLs (Azure-AsyncOperation, Location headers, etc.)</li>
     * <li>Last known response data</li>
     * </ul>
     * <p>
     * <strong>Security Note:</strong> The continuation token contains the operation URL. Ensure tokens
     * are stored securely and transmitted over secure channels.
     * <p>
     * <strong>Compatibility Note:</strong> The token format is internal and may change between SDK versions.
     * Tokens should only be used with the same version of the SDK that generated them.
     *
     * @return A Base64-encoded continuation token string representing the current polling state.
     * @throws RuntimeException if the state cannot be serialized into a token.
     */
    public String toContinuationToken() {
        try {
            String jsonState = this.serializerAdapter.serialize(this, SerializerEncoding.JSON);
            return java.util.Base64.getEncoder()
                .encodeToString(jsonState.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException ioe) {
            throw LOGGER.logExceptionAsError(
                new RuntimeException("Failed to serialize PollingState to continuation token.", ioe));
        }
    }

    /**
     * Deserializes a continuation token string into a PollingState object that can be used to resume
     * a long-running operation.
     * <p>
     * This method is the counterpart to {@link #toContinuationToken()} and reconstructs a PollingState
     * from a previously serialized token. The reconstructed state can then be used to create a new
     * SyncPoller that continues polling from where the previous poller left off.
     *
     * @param continuationToken The Base64-encoded continuation token string, previously obtained from
     *                          {@link #toContinuationToken()}.
     * @param serializerAdapter The serializer for decoding the token. This should be the same serializer
     *                          type used by the service client.
     * @return A PollingState object reconstructed from the continuation token.
     * @throws IllegalArgumentException if the {@code continuationToken} or {@code serializerAdapter} is null or empty.
     * @throws RuntimeException if the token cannot be decoded or deserialized. This may occur if:
     *         <ul>
     *         <li>The token is malformed or corrupted</li>
     *         <li>The token was created with a different SDK version</li>
     *         <li>The token format has changed</li>
     *         </ul>
     */
    public static PollingState fromContinuationToken(String continuationToken, SerializerAdapter serializerAdapter) {
        Objects.requireNonNull(continuationToken, "'continuationToken' cannot be null.");
        Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");

        if (continuationToken.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'continuationToken' cannot be empty."));
        }

        try {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(continuationToken);
            String jsonState = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
            return PollingState.from(serializerAdapter, jsonState);
        } catch (IllegalArgumentException iae) {
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Failed to decode continuation token. The token may be malformed or corrupted.", iae));
        } catch (RuntimeException re) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to deserialize continuation token. "
                + "The token may have been created with a different SDK version.", re));
        }
    }

    /**
     * @return the current status of the long-running-operation.
     */
    public LongRunningOperationStatus getOperationStatus() {
        switch (this.pollingType) {
            case AZURE_ASYNC_OPERATION_POLL:
                return toLongRunningOperationStatus(this.azureAsyncOperationData.getProvisioningState());

            case LOCATION_POLL:
                return toLongRunningOperationStatus(this.locationData.getProvisioningState());

            case PROVISIONING_STATE_POLL:
                return toLongRunningOperationStatus(this.provisioningStateData.getProvisioningState());

            case SYNCHRONOUSLY_SUCCEEDED_LRO_NO_POLL:
                return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

            case SYNCHRONOUSLY_FAILED_LRO_NO_POLL:
                return LongRunningOperationStatus.FAILED;

            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException("Unknown pollingType:" + this.pollingType));
        }
    }

    /**
     * @return the url to poll the status of the long-running-operation.
     */
    URL getPollUrl() {
        switch (this.pollingType) {
            case AZURE_ASYNC_OPERATION_POLL:
                return this.azureAsyncOperationData.getPollUrl();

            case LOCATION_POLL:
                return this.locationData.getPollUrl();

            case PROVISIONING_STATE_POLL:
                return this.provisioningStateData.getPollUrl();

            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("PollUrl not available for the pollingType:" + this.pollingType));
        }
    }

    /**
     * @return the delay in seconds to wait before invoking poll operation.
     */
    public Duration getPollDelay() {
        return this.pollDelay;
    }

    /**
     * @return the error describing failure of a long-running-operation that synchronously failed.
     */
    Error getSynchronouslyFailedLroError() {
        return this.pollingType == PollingType.SYNCHRONOUSLY_FAILED_LRO_NO_POLL
            ? this.synchronouslyFailedLroData
            : null;
    }

    /**
     * @return the error describing failure of a long-running-operation that asynchronously failed.
     */
    Error getPollError() {
        switch (this.pollingType) {
            case AZURE_ASYNC_OPERATION_POLL:
                return this.azureAsyncOperationData.getPollError();

            case LOCATION_POLL:
                return this.locationData.getPollError();

            case PROVISIONING_STATE_POLL:
                return this.provisioningStateData.getPollError();

            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("PollError not available for the pollingType:" + this.pollingType));
        }
    }

    /**
     * @return the necessary information to access the final result of long-running-operation that is
     * successfully (asynchronously or synchronously) completed.
     */
    FinalResult getFinalResult() {
        switch (this.pollingType) {
            case AZURE_ASYNC_OPERATION_POLL:
                return this.azureAsyncOperationData.getFinalResult();

            case LOCATION_POLL:
                return this.locationData.getFinalResult();

            case PROVISIONING_STATE_POLL:
                return this.provisioningStateData.getFinalResult();

            case SYNCHRONOUSLY_SUCCEEDED_LRO_NO_POLL:
                return this.synchronouslySucceededLroData.getFinalResult();

            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("FinalResult not available for the pollingType:" + this.pollingType));
        }
    }

    /**
     * @return the last response body this PollingState received
     */
    public String getLastResponseBody() {
        return this.lastResponseBody;
    }

    /**
     * Update state from the given poll response.
     *
     * @param pollResponseStatusCode the poll response status code
     * @param pollResponseHeaders the poll response headers
     * @param pollResponseBody the poll response body
     * @return the updated PollingState object
     */
    PollingState update(int pollResponseStatusCode, HttpHeaders pollResponseHeaders, String pollResponseBody) {
        switch (this.pollingType) {
            case AZURE_ASYNC_OPERATION_POLL:
                this.azureAsyncOperationData.update(pollResponseStatusCode, pollResponseHeaders, pollResponseBody);
                break;

            case LOCATION_POLL:
                this.locationData.update(pollResponseStatusCode, pollResponseHeaders, pollResponseBody);
                break;

            case PROVISIONING_STATE_POLL:
                this.provisioningStateData.update(pollResponseStatusCode, pollResponseHeaders, pollResponseBody);
                break;

            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("update not available for the pollingType:" + this.pollingType));
        }
        this.pollDelay = getRetryAfter(pollResponseHeaders);
        this.lastResponseBody = pollResponseBody;
        return this;
    }

    /**
     * Set the serializer for decoding and encoding.
     *
     * @param serializer the serializer
     * @return updated PollingState
     */
    private PollingState setSerializer(SerializerAdapter serializer) {
        this.serializerAdapter = serializer;
        return this;
    }

    /**
     * Converts the given value to LongRunningOperationStatus.
     *
     * @param value the value
     * @return LongRunningOperationStatus
     */
    private LongRunningOperationStatus toLongRunningOperationStatus(String value) {
        boolean isCompleted = ProvisioningState.SUCCEEDED.equalsIgnoreCase(value)
            || ProvisioningState.FAILED.equalsIgnoreCase(value)
            || ProvisioningState.CANCELED.equalsIgnoreCase(value);
        if (isCompleted && ProvisioningState.SUCCEEDED.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (isCompleted && ProvisioningState.FAILED.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.FAILED;
        } else if (isCompleted && ProvisioningState.CANCELED.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.USER_CANCELLED;
        } else if (ProvisioningState.IN_PROGRESS.equalsIgnoreCase(value)) {
            return LongRunningOperationStatus.IN_PROGRESS;
        }
        return LongRunningOperationStatus.fromString(value, isCompleted);
    }

    /**
     * @return true if the LRO was initiated using Http Put or Patch verb.
     */
    private boolean isPutOrPatchLro() {
        return this.lroRequestMethod == HttpMethod.PUT || this.lroRequestMethod == HttpMethod.PATCH;
    }

    /**
     * @return true if the LRO was initiated using Http Post or Delete verb.
     */
    private boolean isPostOrDeleteLro() {
        return this.lroRequestMethod == HttpMethod.POST || this.lroRequestMethod == HttpMethod.DELETE;
    }

    /**
     * sets the AzureAsyncOperationData in the PollingState.
     *
     * @param data the data
     * @return updated PollingState
     */
    private PollingState setData(AzureAsyncOperationData data) {
        this.pollingType = PollingType.AZURE_ASYNC_OPERATION_POLL;
        this.azureAsyncOperationData = data;
        return this;
    }

    /**
     * sets the LocationData in the PollingState.
     *
     * @param data the data
     * @return updated PollingState
     */
    private PollingState setData(LocationData data) {
        this.pollingType = PollingType.LOCATION_POLL;
        this.locationData = data;
        return this;
    }

    /**
     * sets the ProvisioningStateData in the PollingState.
     *
     * @param data the data
     * @return updated PollingState
     */
    private PollingState setData(ProvisioningStateData data) {
        this.pollingType = PollingType.PROVISIONING_STATE_POLL;
        this.provisioningStateData = data;
        return this;
    }

    /**
     * sets the SynchronousLroData in the PollingState.
     *
     * @param data the data
     * @return updated PollingState
     */
    private PollingState setData(SynchronouslySucceededLroData data) {
        this.pollingType = PollingType.SYNCHRONOUSLY_SUCCEEDED_LRO_NO_POLL;
        this.synchronouslySucceededLroData = data;
        return this;
    }

    /**
     * sets the LroInitErroredData in the PollingState.
     *
     * @param data the data
     * @return updated PollingState
     */
    private PollingState setData(SynchronouslyFailedLroData data) {
        this.pollingType = PollingType.SYNCHRONOUSLY_FAILED_LRO_NO_POLL;
        this.synchronouslyFailedLroData = data;
        return this;
    }

    /**
     * Initialize the PollingState when the LRO initialization response is 200.
     *
     * @param lroResponseBody the LRO response body
     * @return updated PollingState
     */
    private PollingState initializeDataFor200StatusCode(HttpHeaders lroResponseHeaders, String lroResponseBody) {
        assertStatusCode(200);
        if (this.isPutOrPatchLro()) {
            String value = ProvisioningStateData.tryParseProvisioningState(lroResponseBody);
            if (value != null && !ProvisioningState.SUCCEEDED.equalsIgnoreCase(value)) {
                final URL azAsyncOpUrl = Util.getAzureAsyncOperationUrl(lroResponseHeaders, LOGGER, true);
                if (azAsyncOpUrl == null) {
                    return this.setData(new ProvisioningStateData(this.lroOperationUri, value));
                } else {
                    return this.setData(
                        new AzureAsyncOperationData(this.lroRequestMethod, this.lroOperationUri, azAsyncOpUrl, null));
                }
            } else {
                return this.setData(new SynchronouslySucceededLroData(lroResponseBody));
            }
        } else {
            return this.setData(new SynchronouslySucceededLroData(lroResponseBody));
        }
    }

    /**
     * Initialize the PollingState when the LRO initialization response is 201.
     *
     * @param lroResponseHeaders the LRO response headers
     * @param lroResponseBody the LRO response body
     * @return updated PollingState
     */
    private PollingState initializeDataFor201StatusCode(HttpHeaders lroResponseHeaders, String lroResponseBody) {
        assertStatusCode(201);
        final URL azAsyncOpUrl = Util.getAzureAsyncOperationUrl(lroResponseHeaders, LOGGER, true);
        final URL locationUrl = Util.getLocationUrl(lroResponseHeaders, LOGGER, true);
        if (azAsyncOpUrl != null) {
            if (this.isPostOrDeleteLro()) {
                LOGGER.info("The LRO {}:{}, received StatusCode:201, AzureAsyncOperation:{}. {}", this.lroRequestMethod,
                    this.lroOperationUri, azAsyncOpUrl,
                    "<POST|DELETE, 201, AzureAsyncOperation> combination violate ARM guideline, "
                        + "defaulting to async operation based polling.");
            }
            String value = ProvisioningStateData.tryParseProvisioningState(lroResponseBody);
            if (!ProvisioningState.SUCCEEDED.equalsIgnoreCase(value)) {
                return this.setData(new AzureAsyncOperationData(this.lroRequestMethod, this.lroOperationUri,
                    azAsyncOpUrl, locationUrl));
            } else {
                return this.setData(new SynchronouslySucceededLroData(lroResponseBody));
            }
        }
        if (locationUrl != null) {
            LOGGER.info("The LRO {}:{}, received StatusCode:201, Location:{} without AzureAsyncOperation. {}",
                this.lroRequestMethod, this.lroOperationUri, locationUrl,
                "Location will be ignored on <201, Location, No AzureAsyncOperation> combination.");
        }
        if (this.isPutOrPatchLro()) {
            String value = ProvisioningStateData.tryParseProvisioningState(lroResponseBody);
            if (value != null && !ProvisioningState.SUCCEEDED.equalsIgnoreCase(value)) {
                return this.setData(new ProvisioningStateData(this.lroOperationUri, value));
            } else {
                return this.setData(new SynchronouslySucceededLroData(lroResponseBody));
            }
        } else {
            return this.setData(new SynchronouslySucceededLroData(lroResponseBody));
        }
    }

    /**
     * Initialize the PollingState when the LRO initialization response is 202.
     *
     * @param lroResponseHeaders the LRO response headers
     * @param lroResponseBody the LRO response body
     * @return updated PollingState
     */
    private PollingState initializeDataFor202StatusCode(HttpHeaders lroResponseHeaders, String lroResponseBody) {
        assertStatusCode(202);
        final URL azAsyncOpUrl = Util.getAzureAsyncOperationUrl(lroResponseHeaders, LOGGER, true);
        final URL locationUrl = Util.getLocationUrl(lroResponseHeaders, LOGGER, true);
        if (azAsyncOpUrl != null) {
            return this.setData(
                new AzureAsyncOperationData(this.lroRequestMethod, this.lroOperationUri, azAsyncOpUrl, locationUrl));
        }
        if (locationUrl != null) {
            return this.setData(new LocationData(locationUrl));
        }
        return this.setData(new SynchronouslyFailedLroData(
            "Response with status code 202 does not contain " + "an Azure-AsyncOperation or Location header", 202,
            lroResponseHeaders.toMap(), lroResponseBody));
    }

    /**
     * Initialize the PollingState when the LRO initialization response is 204.
     *
     * @return updated PollingState
     */
    private PollingState initializeDataFor204StatusCode() {
        assertStatusCode(204);
        return this.setData(new SynchronouslySucceededLroData(null /* NoContent */));
    }

    /**
     * Initialize the PollingState when the LRO response status code is unknown.
     *
     * @return updated PollingState
     */
    private PollingState initializeDataForUnknownStatusCode(HttpHeaders lroResponseHeaders, String lroResponseBody) {
        return this.setData(new SynchronouslyFailedLroData("Response StatusCode: " + this.lroResponseStatusCode,
            this.lroResponseStatusCode, lroResponseHeaders.toMap(), lroResponseBody));
    }

    /**
     * Asserts given status code matches with the lro status code.
     *
     * @param statusCode the status code
     */
    private void assertStatusCode(int statusCode) {
        if (this.lroResponseStatusCode != statusCode) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Expected statusCode" + statusCode + "found" + this.lroResponseStatusCode));
        }
    }

    /**
     * Get number of seconds that server want client to wait before making next poll.
     * In ARM the number of seconds to wait before next poll will be communicated via 'Retry-After' header.
     *
     * @param headers the http headers to look for wait duration
     * @return the duration if exists, null otherwise
     */
    private static Duration getRetryAfter(HttpHeaders headers) {
        final String value = headers.getValue(HttpHeaderName.RETRY_AFTER);
        if (value != null) {
            try {
                long retryAfterInSeconds = Long.parseLong(value);
                if (retryAfterInSeconds >= 0) {
                    return Duration.ofSeconds(retryAfterInSeconds);
                }
            } catch (NumberFormatException nfe) {
                LOGGER.logExceptionAsWarning(
                    new IllegalArgumentException("Unable to decode '" + value + "' to Long", nfe));
            }
        }
        return null;
    }

    /**
     * Creates PollingState.
     *
     * @param serializerAdapter the serializer for any encoding and decoding
     * @param lroRequestMethod the http verb used to initialize the LRO operation
     * @param lroOperationUri the endpoint used to initialize the LRO operation
     * @param lroResponseStatusCode the LRO initialization operation status code
     * @param pollDelay the initial delay from the service in response to LRO initialization operation
     * @param lroResponseBody the LRO initialization operation response body
     */
    private PollingState(SerializerAdapter serializerAdapter, HttpMethod lroRequestMethod, URL lroOperationUri,
        int lroResponseStatusCode, Duration pollDelay, String lroResponseBody) {
        this.serializerAdapter = Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null");
        this.lroRequestMethod = Objects.requireNonNull(lroRequestMethod, "'lroRequestMethod' cannot be null");
        this.lroOperationUri = Objects.requireNonNull(lroOperationUri, "'lroOperationUri' cannot be null");
        this.lroResponseStatusCode = lroResponseStatusCode;
        this.pollDelay = pollDelay;
        this.lastResponseBody = lroResponseBody;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("pollingType", Objects.toString(pollingType, null))
            .writeIntField("lroResponseStatusCode", lroResponseStatusCode)
            .writeStringField("lroRequestMethod", Objects.toString(lroRequestMethod, null))
            .writeStringField("lroOperationUri", Objects.toString(lroOperationUri, null))
            .writeStringField("pollDelay", Objects.toString(pollDelay, null))
            .writeJsonField("asyncOperationData", azureAsyncOperationData)
            .writeJsonField("locationData", locationData)
            .writeJsonField("provisioningData", provisioningStateData)
            .writeJsonField("synchronouslySucceededLroData", synchronouslySucceededLroData)
            .writeJsonField("synchronouslyFailedLroData", (JsonSerializable<?>) synchronouslyFailedLroData)
            .writeStringField("lastResponseBody", lastResponseBody)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link PollingState}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link PollingState} that the JSON stream represented, may return null.
     * @throws IOException If a {@link PollingState} fails to be read from the {@code jsonReader}.
     */
    @SuppressWarnings("deprecation")
    public static PollingState fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PollingState pollingState = new PollingState();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("pollingType".equals(fieldName)) {
                    pollingState.pollingType = PollingType.valueOf(reader.getString());
                } else if ("lroResponseStatusCode".equals(fieldName)) {
                    pollingState.lroResponseStatusCode = reader.getInt();
                } else if ("lroRequestMethod".equals(fieldName)) {
                    pollingState.lroRequestMethod = HttpMethod.valueOf(reader.getString());
                } else if ("lroOperationUri".equals(fieldName)) {
                    pollingState.lroOperationUri
                        = reader.getNullable(nonNullReader -> new URL(nonNullReader.getString()));
                } else if ("pollDelay".equals(fieldName)) {
                    pollingState.pollDelay
                        = reader.getNullable(nonNullReader -> Duration.parse(nonNullReader.getString()));
                } else if ("asyncOperationData".equals(fieldName)) {
                    pollingState.azureAsyncOperationData = AzureAsyncOperationData.fromJson(reader);
                } else if ("locationData".equals(fieldName)) {
                    pollingState.locationData = LocationData.fromJson(reader);
                } else if ("provisioningData".equals(fieldName)) {
                    pollingState.provisioningStateData = ProvisioningStateData.fromJson(reader);
                } else if ("synchronouslySucceededLroData".equals(fieldName)) {
                    pollingState.synchronouslySucceededLroData = SynchronouslySucceededLroData.fromJson(reader);
                } else if ("synchronouslyFailedLroData".equals(fieldName)) {
                    pollingState.synchronouslyFailedLroData = SynchronouslyFailedLroData.fromJson(reader);
                } else if ("lastResponseBody".equals(fieldName)) {
                    pollingState.lastResponseBody = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return pollingState;
        });
    }
}
