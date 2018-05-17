/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.language.luis.authoring;

import com.microsoft.azure.cognitiveservices.language.luis.authoring.implementation.LUISAuthoringAPIImpl;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.AzureRegions;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Entry point to Azure Cognitive Services Language Understanding (LUIS) Authoring manager.
 */
public class LUISAuthoringManager {
    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param region Supported Azure regions for Cognitive Services endpoints.
     * @param subscriptionKey the Language Understanding (LUIS) Authoring API key
     * @return the Language Understanding Authoring API client
     */
    public static LUISAuthoringAPI authenticate(AzureRegions region, String subscriptionKey) {
        return authenticate("https://{AzureRegion}.api.cognitive.microsoft.com/luis/api/v2.0/", subscriptionKey)
                .withAzureRegion(region);
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param baseUrl the base URL of the service
     * @param subscriptionKey the Language Understanding (LUIS) Authoring API key
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringAPI authenticate(String baseUrl, final String subscriptionKey) {
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
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param region Supported Azure regions for Cognitive Services endpoints.
     * @param credentials the management credentials for Azure
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringAPI authenticate(AzureRegions region, ServiceClientCredentials credentials) {
        return authenticate("https://{AzureRegion}.api.cognitive.microsoft.com/luis/api/v2.0/", credentials)
                .withAzureRegion(region);
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringAPI authenticate(String baseUrl, ServiceClientCredentials credentials) {
        return new LUISAuthoringAPIImpl(baseUrl, credentials);
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringAPI authenticate(RestClient restClient) {
        return new LUISAuthoringAPIImpl(restClient);
    }
}
