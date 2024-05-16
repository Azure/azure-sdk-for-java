/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.customvision.prediction;

import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.implementation.CustomVisionPredictionClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;

/**
 * Entry point to Azure Cognitive Services Custom Vision Prediction manager.
 */
public class CustomVisionPredictionManager {
    /**
     * Initializes an instance of Custom Vision Prediction API client.
     *
     * @param apiKey the Custom Vision Prediction API key
     * @return the Computer Vision API client
     */
    public static CustomVisionPredictionClient authenticate(String apiKey) {
        return authenticate("https://southcentralus.api.cognitive.microsoft.com/customvision/v2.0/Prediction/", apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Prediction API client.
     *
     * @param baseUrl the base URL of the service
     * @param apiKey the Custom Vision Prediction API key
     * @return the Custom Vision Prediction API client
     */
    public static CustomVisionPredictionClient authenticate(String baseUrl, final String apiKey) {
        ServiceClientCredentials serviceClientCredentials = new ServiceClientCredentials() {
            @Override
            public void applyCredentialsFilter(OkHttpClient.Builder builder) {
            }
        };
        return authenticate(baseUrl, serviceClientCredentials, apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Prediction API client.
     *
     * @param credentials the management credentials for Azure
     * @param apiKey the Custom Vision Prediction API key
     * @return the Computer Vision API client
     */
    public static CustomVisionPredictionClient authenticate(ServiceClientCredentials credentials, final String apiKey) {
        return authenticate("https://southcentralus.api.cognitive.microsoft.com/customvision/v2.0/Prediction/", credentials, apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Prediction API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @param apiKey the Custom Vision Prediction API key
     * @return the Custom Vision Prediction API client
     */
    public static CustomVisionPredictionClient authenticate(String baseUrl, ServiceClientCredentials credentials, final String apiKey) {
        return new CustomVisionPredictionClientImpl(baseUrl, credentials).withApiKey(apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Prediction API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @param apiKey the Custom Vision Prediction API key
     * @return the Custom Vision Prediction API client
     */
    public static CustomVisionPredictionClient authenticate(RestClient restClient, final String apiKey) {
        return new CustomVisionPredictionClientImpl(restClient).withApiKey(apiKey);
    }
}
