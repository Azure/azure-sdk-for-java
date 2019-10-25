/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.customvision.training;

import com.microsoft.azure.cognitiveservices.vision.customvision.training.implementation.CustomVisionTrainingClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;

/**
 * Entry point to Azure Cognitive Services Custom Vision Training manager.
 */
public class CustomVisionTrainingManager {
    /**
     * Initializes an instance of Custom Vision Training API client.
     *
     * @param apiKey the Custom Vision Training API key
     * @return the Computer Vision Training API client
     */
    public static CustomVisionTrainingClient authenticate(String apiKey) {
        return authenticate("https://southcentralus.api.cognitive.microsoft.com/customvision/v2.1/Training/", apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Training API client.
     *
     * @param baseUrl the base URL of the service
     * @param apiKey the Custom Vision Training API key
     * @return the Custom Vision Training API client
     */
    public static CustomVisionTrainingClient authenticate(String baseUrl, final String apiKey) {
        ServiceClientCredentials serviceClientCredentials = new ServiceClientCredentials() {
            @Override
            public void applyCredentialsFilter(OkHttpClient.Builder builder) {
            }
        };
        return authenticate(baseUrl, serviceClientCredentials, apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Training API client.
     *
     * @param credentials the management credentials for Azure
     * @param apiKey the Custom Vision Training API key
     * @return the Computer Vision Training API client
     */
    public static CustomVisionTrainingClient authenticate(ServiceClientCredentials credentials, final String apiKey) {
        return authenticate("https://southcentralus.api.cognitive.microsoft.com/customvision/v2.1/Training/", credentials, apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Training API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @param apiKey the Custom Vision Training API key
     * @return the Custom Vision Training API client
     */
    public static CustomVisionTrainingClient authenticate(String baseUrl, ServiceClientCredentials credentials, final String apiKey) {
        return new CustomVisionTrainingClientImpl(baseUrl, credentials).withApiKey(apiKey);
    }

    /**
     * Initializes an instance of Custom Vision Training API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @param apiKey the Custom Vision Training API key
     * @return the Custom Vision Training API client
     */
    public static CustomVisionTrainingClient authenticate(RestClient restClient, final String apiKey) {
        return new CustomVisionTrainingClientImpl(restClient).withApiKey(apiKey);
    }
}
