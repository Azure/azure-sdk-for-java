/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.search.videosearch;

import com.microsoft.azure.cognitiveservices.search.videosearch.implementation.BingVideoSearchAPIImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Entry point to Azure Cognitive Services Bing Video Search manager.
 */
public class BingVideoSearchManager {
    /**
     * Initializes an instance of Bing Video Search API client.
     *
     * @param subscriptionKey the Bing Search API key
     * @return the Bing Video Search API client
     */
    public static BingVideoSearchAPI authenticate(String subscriptionKey) {
        return authenticate("https://api.cognitive.microsoft.com/bing/v7.0/", subscriptionKey);
    }

    /**
     * Initializes an instance of Bing Video Search API client.
     *
     * @param baseUrl the URL of the service
     * @param subscriptionKey the Bing Search API key
     * @return the Bing Video Search API client
     */
    public static BingVideoSearchAPI authenticate(String baseUrl, final String subscriptionKey) {
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
     * Initializes an instance of Bing Video Search API client.
     *
     * @param credentials the management credentials for Azure
     * @return the Bing Video Search API client
     */
    public static BingVideoSearchAPI authenticate(ServiceClientCredentials credentials) {
        return authenticate("https://api.cognitive.microsoft.com/bing/v7.0/", credentials);
    }

    /**
     * Initializes an instance of Bing Video Search API client.
     *
     * @param baseUrl the base URL of the
     * @param credentials the management credentials for Azure
     * @return the Bing Video Search API client
     */
    public static BingVideoSearchAPI authenticate(String baseUrl, ServiceClientCredentials credentials) {
        return new BingVideoSearchAPIImpl(baseUrl, credentials);
    }

    /**
     * Initializes an instance of Bing Video Search API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the Bing Video Search API client
     */
    public static BingVideoSearchAPI authenticate(RestClient restClient) {
        return new BingVideoSearchAPIImpl(restClient);
    }
}
