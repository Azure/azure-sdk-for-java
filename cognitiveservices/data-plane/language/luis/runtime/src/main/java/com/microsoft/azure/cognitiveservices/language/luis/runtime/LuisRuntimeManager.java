/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.language.luis.runtime;

import com.microsoft.azure.cognitiveservices.language.luis.runtime.implementation.LuisRuntimeAPIImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;

/**
 * Entry point to Azure Cognitive Services Language Understanding (LUIS) Runtime manager.
 */
public class LuisRuntimeManager {
    /**
     * Initializes an instance of Language Understanding (LUIS) Runtime API client.
     *
     * @param endpointAPI the endpoint API
     * @param luisAuthoringKey the Language Understanding (LUIS) Authoring API key (see https://www.luis.ai)
     * @return the Language Understanding Runtime API client
     */
    public static LuisRuntimeAPI authenticate(EndpointAPI endpointAPI, String luisAuthoringKey) {
        return authenticate(String.format("https://%s/luis/v2.0/", endpointAPI.toString()), luisAuthoringKey)
            .withEndpoint(endpointAPI.toString());
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Runtime API client.
     *
     * @param baseUrl the base URL of the service
     * @param luisAuthoringKey the Language Understanding (LUIS) Authoring API key (see https://www.luis.ai)
     * @return the Language Understanding (LUIS) Runtime API client
     */
    public static LuisRuntimeAPI authenticate(String baseUrl, final String luisAuthoringKey) {
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
     * Initializes an instance of Language Understanding (LUIS) Runtime API client.
     *
     * @param endpointAPI the endpoint API
     * @param credentials the management credentials for Azure
     * @return the Language Understanding (LUIS) Runtime API client
     */
    public static LuisRuntimeAPI authenticate(EndpointAPI endpointAPI, ServiceClientCredentials credentials) {
        return authenticate(String.format("https://%s/luis/v2.0/", endpointAPI.toString()), credentials)
            .withEndpoint(endpointAPI.toString());
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Runtime API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @return the Language Understanding (LUIS) Runtime API client
     */
    public static LuisRuntimeAPI authenticate(String baseUrl, ServiceClientCredentials credentials) {
        String endpointAPI = null;
        try {
            URI uri = new URI(baseUrl);
            endpointAPI = uri.getHost();
        } catch (Exception e) {
            endpointAPI = EndpointAPI.US_WEST.toString();
        }
        return new LuisRuntimeAPIImpl(baseUrl, credentials)
            .withEndpoint(endpointAPI);
    }

    /**
     * Initializes an instance of Language Understanding (LUIS) Runtime API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the Language Understanding (LUIS) Runtime API client
     */
    public static LuisRuntimeAPI authenticate(RestClient restClient) {
        return new LuisRuntimeAPIImpl(restClient);
    }
}
