/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.contentmoderator;

import com.microsoft.azure.cognitiveservices.vision.contentmoderator.implementation.ContentModeratorClientImpl;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.AzureRegionBaseUrl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Entry point to Azure Cognitive Services Content Moderator manager.
 */
public class ContentModeratorManager {
    /**
     * Initializes an instance of Content Moderator API client.
     *
     * @param baseUrl sets Supported Azure regions for Content Moderator endpoints.
     * @param subscriptionKey the Content Moderator API key
     * @return the Content Moderator API client
     */
    public static ContentModeratorClient authenticate(AzureRegionBaseUrl baseUrl, String subscriptionKey) {
        return authenticate("https://{baseUrl}/", subscriptionKey)
                .withBaseUrl(baseUrl);
    }

    /**
     * Initializes an instance of Content Moderator API client.
     *
     * @param baseUrl the base URL of the service
     * @param subscriptionKey the Content Moderator API key
     * @return the Content Moderator API client
     */
    public static ContentModeratorClient authenticate(String baseUrl, final String subscriptionKey) {
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
     * Initializes an instance of Content Moderator API client.
     *
     * @param baseUrl Supported Azure regions for Cognitive Services endpoints.
     * @param credentials the management credentials for Azure
     * @return the Content Moderator API client
     */
    public static ContentModeratorClient authenticate(AzureRegionBaseUrl baseUrl, ServiceClientCredentials credentials) {
        return authenticate("https://{baseUrl}/", credentials)
                .withBaseUrl(baseUrl);
    }

    /**
     * Initializes an instance of Content Moderator API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @return the Content Moderator API client
     */
    public static ContentModeratorClient authenticate(String baseUrl, ServiceClientCredentials credentials) {
        return new ContentModeratorClientImpl(baseUrl, credentials);
    }

    /**
     * Initializes an instance of Content Moderator API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the Text Analytics API client
     */
    public static ContentModeratorClient authenticate(RestClient restClient) {
        return new ContentModeratorClientImpl(restClient);
    }
}
