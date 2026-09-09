// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.RequestOptions;
import com.azure.messaging.webpubsub.chat.implementation.WebPubSubChatServiceClientImpl;
import com.azure.messaging.webpubsub.chat.implementation.models.GenerateClientTokenResponse;
import com.azure.messaging.webpubsub.chat.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.chat.models.WebPubSubClientAccessToken;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class WebPubSubClientAccessTokenFactory {
    private static final List<String> CHAT_ROLES = Arrays.asList("webpubsub.getGroupState", "webpubsub.setGroupState");

    static WebPubSubClientAccessToken create(WebPubSubChatServiceClientImpl serviceClient,
        AzureKeyCredential keyCredential, GetClientAccessTokenOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        Duration expiresAfter = validateExpiresAfter(options.getExpiresAfter());
        String token;
        if (keyCredential == null) {
            token = serviceClient
                .generateClientTokenWithResponse(serviceClient.getHub(), createRequestOptions(options, expiresAfter))
                .getValue()
                .toObject(GenerateClientTokenResponse.class)
                .getToken();
        } else {
            token = WebPubSubTokenGenerator.generateToken(createAudience(serviceClient), options.getUserId(),
                CHAT_ROLES, expiresAfter, keyCredential);
        }
        return createResult(token, serviceClient);
    }

    static Mono<WebPubSubClientAccessToken> createAsync(WebPubSubChatServiceClientImpl serviceClient,
        AzureKeyCredential keyCredential, GetClientAccessTokenOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        Duration expiresAfter = validateExpiresAfter(options.getExpiresAfter());
        if (keyCredential == null) {
            return serviceClient
                .generateClientTokenWithResponseAsync(serviceClient.getHub(),
                    createRequestOptions(options, expiresAfter))
                .map(response -> response.getValue().toObject(GenerateClientTokenResponse.class).getToken())
                .map(token -> createResult(token, serviceClient));
        }
        return Mono.fromCallable(() -> create(serviceClient, keyCredential, options));
    }

    private static RequestOptions createRequestOptions(GetClientAccessTokenOptions options, Duration expiresAfter) {
        RequestOptions requestOptions = new RequestOptions();
        if (options.getUserId() != null) {
            requestOptions.addQueryParam("userId", options.getUserId());
        }
        requestOptions.addQueryParam("minutesToExpire", String.valueOf(expiresAfter.toMinutes()));
        CHAT_ROLES.forEach(role -> requestOptions.addQueryParam("role", role));
        return requestOptions;
    }

    private static Duration validateExpiresAfter(Duration expiresAfter) {
        Objects.requireNonNull(expiresAfter, "'expiresAfter' cannot be null.");
        long minutes = expiresAfter.toMinutes();
        if (minutes < 1 || minutes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "'expiresAfter' must be between 1 minute and " + Integer.MAX_VALUE + " minutes.");
        }
        return expiresAfter;
    }

    private static String createAudience(WebPubSubChatServiceClientImpl serviceClient) {
        String endpoint = serviceClient.getEndpoint();
        return endpoint + (endpoint.endsWith("/") ? "" : "/") + "client/hubs/" + serviceClient.getHub();
    }

    private static WebPubSubClientAccessToken createResult(String token, WebPubSubChatServiceClientImpl serviceClient) {
        String clientUrl = createAudience(serviceClient).replaceFirst("http", "ws");
        return new WebPubSubClientAccessToken(token, clientUrl + "?access_token=" + token);
    }

    private WebPubSubClientAccessTokenFactory() {
    }
}
