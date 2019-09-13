// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import android.util.DisplayMetrics;

/**
 * This class is used to create instances of the InkRecognizerClient and InkRecognizerAsyncClient classes
 * @author Microsoft
 * @version 1.0
 */
public final class InkRecognizerClientBuilder {

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

    public InkRecognizerClientBuilder() {
    }

    /**
     * Used to set the application key for communicating with the service
     * @param credentials The application key received after signing up for the service.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder credentials(InkRecognizerCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Used to set the endpoint url for communicating with the service
     * @param endpoint The URL of the service endpoint to communicate with.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * @param retries The number of times a retry should be attempted before reporting an error.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder retryCount(int retries) {
        this.retryCount = retries;
        return this;
    }

    /**
     * @param timeout The amount of time to wait before attempting a retry.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder retryTimeout(int timeout) {
        this.retryTimeout = timeout;
        return this;
    }

    /**
     * @param kind The domain of the application (Writing or Drawing. The default is "Mixed").
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder applicationKind(ApplicationKind kind) {
        this.applicationKind = kind;
        return this;
    }

    /**
     * @param language The IETF BCP 47 language code (for ex. en-US, en-GB, hi-IN etc.)
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder language(String language) {
        this.language = language;
        return this;
    }

    /**
     * @param unit The physical unit used for the ink points. The points are assumed to be in pixels if the values
     * isn't specified.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder unit(InkPointUnit unit) {
        this.unit = unit;
        return this;
    }

    /**
     * @param multiple The multiple to apply to the unit if it has been scaled. The points are assumed to be in pixels
     * if the values isn't specified.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder unitMultiple(float multiple) {
        this.unitMultiple = multiple;
        return this;
    }

    /**
     * @param version The version of the service to use.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder serviceVersion(ServiceVersion version) {
        this.serviceVersion = version;
        return this;
    }

    /**
     * @param displayMetrics The displayMetrics of the client screen.
     * @return The current InkRecognizerClientBuilder instance.
     */
    public InkRecognizerClientBuilder displayMetrics(DisplayMetrics displayMetrics) {
        this.displayMetrics = displayMetrics;
        return this;
    }

    /**
     * Used to retrieve a new instance of the InkRecognizerClient class.
     * @return An InkRecognizerClient instance.
     * @throws Exception
     */
    public InkRecognizerClient buildClient() throws Exception {
        validateFields();
        return new InkRecognizerClient(endpoint, credentials)
                .setCredentials(credentials)
                .setEndpoint(endpoint)
                .setRetryCount(retryCount)
                .setRetryTimeout(retryTimeout)
                .setApplicationKind(applicationKind)
                .setLanguage(language)
                .setUnit(unit)
                .setUnitMultiple(unitMultiple)
                .setServiceVersion(serviceVersion)
                .setDisplayMetrics(displayMetrics);
    }

    /**
     * Used to retrieve a new instance of the InkRecognizerAsyncClient class.
     * @return An InkRecognizerAsyncClient instance.
     * @throws Exception
     */
    public InkRecognizerAsyncClient buildAsyncClient() throws Exception {
        validateFields();
        return new InkRecognizerAsyncClient(endpoint, credentials)
                .setCredentials(credentials)
                .setEndpoint(endpoint)
                .setRetryCount(retryCount)
                .setRetryTimeout(retryTimeout)
                .setApplicationKind(applicationKind)
                .setLanguage(language)
                .setUnit(unit)
                .setUnitMultiple(unitMultiple)
                .setServiceVersion(serviceVersion)
                .setDisplayMetrics(displayMetrics);
    }

    private void validateFields() throws Exception {
        if (credentials == null || endpoint == null || displayMetrics == null) {
            throw new Exception("Required fields not set in the request");
        }
    }

}