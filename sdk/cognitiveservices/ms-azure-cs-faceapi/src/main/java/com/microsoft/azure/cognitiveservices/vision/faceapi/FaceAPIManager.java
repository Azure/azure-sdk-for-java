/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.faceapi;

import com.microsoft.azure.cognitiveservices.vision.faceapi.implementation.FaceAPIImpl;
import com.microsoft.azure.cognitiveservices.vision.faceapi.models.AzureRegions;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Entry point to Azure Cognitive Services Face API manager.
 */
public class FaceAPIManager {
    /**
     * Initializes an instance of Face API client.
     *
     * @param region Supported Azure regions for Cognitive Services endpoints.
     * @param subscriptionKey the Face API key
     * @return the Face API client
     */
    public static FaceAPI authenticate(AzureRegions region, String subscriptionKey) {
        return authenticate("https://{AzureRegion}.api.cognitive.microsoft.com/face/v1.0/", subscriptionKey)
                .withAzureRegion(region);
    }

    /**
     * Initializes an instance of Face API client.
     *
     * @param baseUrl the base URL of the service
     * @param subscriptionKey the Face API key
     * @return the Face API client
     */
    public static FaceAPI authenticate(String baseUrl, final String subscriptionKey) {
        ServiceClientCredentials serviceClientCredentials = new ServiceClientCredentials() {
            @Override
            public void applyCredentialsFilter(OkHttpClient.Builder builder) {
                builder.addNetworkInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request request = null;
                                Request original = chain.request();
                                // Request customization: add request headers
                                Request.Builder requestBuilder = original.newBuilder()
                                        .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey);
                                request = requestBuilder.build();
                                return chain.proceed(request);
                            }
                        });
            }
        };
        return authenticate(baseUrl, serviceClientCredentials);
    }

    /**
     * Initializes an instance of Face API client.
     *
     * @param region Supported Azure regions for Cognitive Services endpoints.
     * @param credentials the management credentials for Azure
     * @return the Face API client
     */
    public static FaceAPI authenticate(AzureRegions region, ServiceClientCredentials credentials) {
        return authenticate("https://{AzureRegion}.api.cognitive.microsoft.com/face/v1.0/", credentials)
                .withAzureRegion(region);
    }

    /**
     * Initializes an instance of Face API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @return the Face API client
     */
    public static FaceAPI authenticate(String baseUrl, ServiceClientCredentials credentials) {
        return new FaceAPIImpl(baseUrl, credentials);
    }

    /**
     * Initializes an instance of Face API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the Face API client
     */
    public static FaceAPI authenticate(RestClient restClient) {
        return new FaceAPIImpl(restClient);
    }
}
