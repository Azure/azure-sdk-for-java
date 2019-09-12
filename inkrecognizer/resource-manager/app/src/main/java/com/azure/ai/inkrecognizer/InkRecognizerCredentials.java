// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import okhttp3.Request;

/**
 * Place holder class for credentials implementation for Java on Android
 * @author Microsoft
 * @version 1.0
 */
public class InkRecognizerCredentials {

    private final String key;

    public InkRecognizerCredentials(String subscriptionKey) {
        key = subscriptionKey;
    }

    /**
     * Sets the required credential on the request.
     * @param request The request to set the credentials on.
     */
    Request SetRequestCredentials(Request request) {
        return request.newBuilder().addHeader("Ocp-Apim-Subscription-Key", key).build();
    }

}
