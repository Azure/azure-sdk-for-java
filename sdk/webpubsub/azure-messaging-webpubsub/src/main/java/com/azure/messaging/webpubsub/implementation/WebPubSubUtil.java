// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/**
 * Utility class for centralizing token extraction code.
 */
public final class WebPubSubUtil {
    private static final ClientLogger LOGGER = new ClientLogger(WebPubSubUtil.class);
    private static final String TOKEN = "token";

    /**
     * Extracts the token from BinaryData response of generateClientToken API.
     * @param binaryData The response BinaryData.
     * @return The token extracted from the JSON payload of the generateClientToken API.
     */
    public static String getToken(BinaryData binaryData) {
        try {
            JsonParser parser = JsonFactory.builder().build().createParser(binaryData.toString());
            while (!parser.isClosed()) {
                JsonToken jsonToken = parser.nextToken();
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    String fieldName = parser.getCurrentName();
                    if (TOKEN.equals(fieldName)) {
                        // move parser forward to get value of "token"
                        parser.nextToken();
                        return parser.getValueAsString();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.logThrowableAsError(new IllegalStateException("Unable to find token in the response", e));
        }
        return null;
    }

    /**
     * Creates a new instance of {@link WebPubSubClientAccessToken}.
     * @param token The JWT token.
     * @param endpoint The Web PubSub endpoint.
     * @param hub The name of the hub.
     * @return A new instance of {@link WebPubSubClientAccessToken}.
     */
    public static WebPubSubClientAccessToken createToken(String token, String endpoint, String hub) {
        endpoint = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        // The endpoint should always be http or https and client endpoint should be ws or wss respectively.
        final String clientEndpoint = endpoint.replaceFirst("http", "ws");
        final String clientUrl = clientEndpoint + "client/hubs/" + hub;
        final String url = clientUrl + "?access_token=" + token;
        return new WebPubSubClientAccessToken(token, url);
    }

    private WebPubSubUtil() {
        // no instances
    }

}
