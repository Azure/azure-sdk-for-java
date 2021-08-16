// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MixedRealityStsClientTests extends MixedRealityStsClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private MixedRealityStsClient client;

    private void initializeClient(HttpClient httpClient) {
        client = new MixedRealityStsClientBuilder()
            .accountId(super.getAccountId())
            .accountDomain(super.getAccountDomain())
            .pipeline(super.getHttpPipeline(httpClient))
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getToken(HttpClient httpClient) {
        // arrange
        initializeClient(httpClient);

        // act
        AccessToken actual = this.client.getToken();

        // assert
        assertNotNull(actual);
        assertNotNull(actual.getToken());
        assertNotNull(actual.getExpiresAt());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    public void getTokenWithResponse(HttpClient httpClient) {
        // arrange
        initializeClient(httpClient);

        // act
        Response<AccessToken> actualResponse = this.client.getTokenWithResponse(Context.NONE);

        // assert
        assertNotNull(actualResponse);
        assertEquals(200, actualResponse.getStatusCode());

        // act
        AccessToken actual = actualResponse.getValue();

        // assert
        assertNotNull(actual);
        assertNotNull(actual.getToken());
        assertNotNull(actual.getExpiresAt());
    }
}
