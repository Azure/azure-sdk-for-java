// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;


import com.azure.communication.sms.models.SmsSendResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.http.HttpClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SmsAsyncClientTests extends SmsTestBase {
    private List<String> to;
    private String from;
    private String message;
    private SmsAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncClientUsingConnectionString(HttpClient httpClient) {

        from = "+18335102092";
        to = new ArrayList<String>();
        to.add("+18336388593");
        message = "hello";
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(asyncClient);

        Mono<List<SmsSendResult>> response = asyncClient.send(from, to, message, null);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();


    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void send(HttpClient httpClient) {

        from = "+18335102092";
        to = new ArrayList<String>();
        to.add("+18336388593");
        message = "hello";
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "send");

        // Action & Assert

        Mono<List<SmsSendResult>> response = asyncClient.send(from, to, message, null);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }


    private SmsAsyncClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }


}
