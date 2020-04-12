/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.knowledge.qnamaker;

import com.microsoft.azure.cognitiveservices.knowledge.qnamaker.implementation.QnAMakerRuntimeClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Entry point to Azure Cognitive Services QnA Maker Runtime manager.
 */
public class QnAMakerRuntimeManager {
    /**
     * Initializes an instance of QnA Maker Runtime API client.
     *
     * @param subscriptionKey the QnA Maker Runtime API key
     * @return the QnA Maker Runtime API client
     */
    public static QnAMakerRuntimeClient authenticate(String subscriptionKey) {
        return authenticate("https://{RuntimeEndpoint}/qnamaker", subscriptionKey);
    }

    /**
     * Initializes an instance of QnA Maker Runtime API client.
     *
     * @param baseUrl the base URL of the service
     * @param subscriptionKey the QnA Maker Runtime API key
     * @return the QnA Maker Runtime API client
     */
    public static QnAMakerRuntimeClient authenticate(String baseUrl, final String subscriptionKey) {
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
                                        .addHeader("Authorization", "EndpointKey " + subscriptionKey);
                                request = requestBuilder.build();
                                return chain.proceed(request);
                            }
                        });
            }
        };
        return authenticate(baseUrl, serviceClientCredentials);
    }

    /**
     * Initializes an instance of QnA Maker Runtime API client.
     *
     * @param credentials the management credentials for Azure
     * @param endpoint Supported Cognitive Services endpoints.
     * @return the QnA Maker Runtime API client
     */
    public static QnAMakerRuntimeClient authenticate(ServiceClientCredentials credentials, String endpoint) {
        return authenticate("https://{RuntimeEndpoint}/qnamaker", credentials)
                .withRuntimeEndpoint(endpoint);
    }

    /**
     * Initializes an instance of QnA Maker Runtime API client.
     *
     * @param baseUrl the base URL of the service
     * @param credentials the management credentials for Azure
     * @return the QnA Maker Runtime API client
     */
    public static QnAMakerRuntimeClient authenticate(String baseUrl, ServiceClientCredentials credentials) {
        return new QnAMakerRuntimeClientImpl(baseUrl, credentials);
    }

    /**
     * Initializes an instance of QnA Maker Runtime API client.
     *
     * @param restClient the REST client to connect to Azure.
     * @return the QnA Maker Runtime API client
     */
    public static QnAMakerRuntimeClient authenticate(RestClient restClient) {
        return new QnAMakerRuntimeClientImpl(restClient);
    }
}
