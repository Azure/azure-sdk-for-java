// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MixedRealityStsAsyncClientTests extends MixedRealityStsClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private MixedRealityStsAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getToken(HttpClient httpClient) {
        // arrange
        client = getClientBuilder(httpClient).buildAsyncClient();

        // act
        StepVerifier.create(client.getToken())
            .assertNext(actual -> {
                // assert
                assertNotNull(actual);
                assertNotNull(actual.getToken());
                assertNotNull(actual.getExpiresAt());
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getTokenWithResponse(HttpClient httpClient) {
        // arrange
        client = getClientBuilder(httpClient).buildAsyncClient();

        // act
        StepVerifier.create(client.getTokenWithResponse())
            .assertNext(actualResponse -> {
                // assert
                assertNotNull(actualResponse);
                assertEquals(200, actualResponse.getStatusCode());

                // act
                AccessToken actual = actualResponse.getValue();

                // assert
                assertNotNull(actual);
                assertNotNull(actual.getToken());
                assertNotNull(actual.getExpiresAt());
            })
            .verifyComplete();
    }
}
