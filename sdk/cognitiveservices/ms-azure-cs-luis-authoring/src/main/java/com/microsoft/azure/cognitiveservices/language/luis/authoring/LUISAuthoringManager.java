/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.language.luis.authoring;

import com.microsoft.azure.cognitiveservices.language.luis.authoring.implementation.LUISAuthoringClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;

/**
 * Entry point to Azure Cognitive Services Language Understanding (LUIS) Authoring manager.
 */
public class LUISAuthoringManager {
    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param endpointAPI the endpoint API
     * @param luisAuthoringKey the Language Understanding (LUIS) Authoring API key (see https://www.luis.ai)
     * @return the Language Understanding Authoring API client
     */
    public static LUISAuthoringClient authenticate(EndpointAPI endpointAPI, String luisAuthoringKey) {
        return authenticate(String.format("https://%s/luis/authoring/v3.0-preview", endpointAPI.toString()), luisAuthoringKey)
            .withEndpoint(endpointAPI.toString());
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param baseUrl the base URL of the service
     * @param luisAuthoringKey the Language Understanding (LUIS) Authoring API key (see https://www.luis.ai)
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringClient authenticate(String baseUrl, final String luisAuthoringKey) {
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
                                        .addHeader("Ocp-Apim-Subscription-Key", luisAuthoringKey);
                                request = requestBuilder.build();
                                return chain.proceed(request);
                            }
                        });
            }
        };
        String endpointAPI = null;
        try {
            URI uri = new URI(baseUrl);
            endpointAPI = uri.getHost();
        } catch (Exception e) {
            endpointAPI = EndpointAPI.US_WEST.toString();
        }
        return authenticate(baseUrl, serviceClientCredentials)
            .withEndpoint(endpointAPI);
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param endpointAPI the endpoint API
     * @param credentials the management credentials for Azure
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringClient authenticate(EndpointAPI endpointAPI, ServiceClientCredentials credentials) {
        return authenticate(String.format("https://%s/luis/authoring/v3.0-preview", endpointAPI), credentials)
            .withEndpoint(endpointAPI.toString());
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringClient authenticate(String baseUrl, ServiceClientCredentials credentials) {
        return new LUISAuthoringClientImpl(baseUrl, credentials);
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Authoring API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the Language Understanding (LUIS) Authoring API client
     */
    public static LUISAuthoringClient authenticate(RestClient restClient) {
        return new LUISAuthoringClientImpl(restClient);
    }
}
