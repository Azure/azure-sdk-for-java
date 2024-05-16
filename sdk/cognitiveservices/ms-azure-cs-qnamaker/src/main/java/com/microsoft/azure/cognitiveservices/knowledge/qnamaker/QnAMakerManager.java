/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.knowledge.qnamaker;

import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.implementation.QnAMakerClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Entry point to Azure Cognitive Services QnA Maker manager.
 */
public class QnAMakerManager {
    /**
     * Initializes an instance of QnA Maker API client.
     *
     * @param subscriptionKey the QnA Maker API key
     * @return the QnA Maker API client
     */
    public static QnAMakerClient authenticate(String subscriptionKey) {
        return authenticate("https://{Endpoint}/qnamaker/v5.0-preview.1", subscriptionKey);
    }

    /**
     * Initializes an instance of QnA Maker API client.
     *
     * @param baseUrl the base URL of the service
     * @param subscriptionKey the QnA Maker API key
     * @return the QnA Maker API client
     */
    public static QnAMakerClient authenticate(String baseUrl, final String subscriptionKey) {
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
     * Initializes an instance of QnA Maker API client.
     *
     * @param credentials the management credentials for Azure
     * @param endpoint Supported Cognitive Services endpoints.
     * @return the QnA Maker API client
     */
    public static QnAMakerClient authenticate(ServiceClientCredentials credentials, String endpoint) {
        return authenticate("https://{Endpoint}/qnamaker/v5.0-preview.1", credentials)
                .withEndpoint(endpoint);
    }

    /**
     * Initializes an instance of QnA Maker API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @return the QnA Maker API client
     */
    public static QnAMakerClient authenticate(String baseUrl, ServiceClientCredentials credentials) {
        return new QnAMakerClientImpl(baseUrl, credentials);
    }

    /**
     * Initializes an instance of QnA Maker API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the QnA Maker API client
     */
    public static QnAMakerClient authenticate(RestClient restClient) {
        return new QnAMakerClientImpl(restClient);
    }
}
