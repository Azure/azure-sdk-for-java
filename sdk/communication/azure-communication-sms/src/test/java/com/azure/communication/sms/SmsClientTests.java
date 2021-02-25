// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;


import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.http.HttpClient;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SmsClientTests extends SmsTestBase {
    private List<String> to;
    private String from;
    private String body;
    private SmsClient client;

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
        body = "hello";
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(client);


        List<SmsSendResult> response = client.send(from, to, body, null, Context.NONE);
        assertNotNull(response);


        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());

        }


    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void send(HttpClient httpClient) {

        from = "+18335102092";
        to = new ArrayList<String>();
        to.add("+18336388593");
        body = "hello";
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "send");

        // Action & Assert

        List<SmsSendResult> response = client.send(from, to, body, null, Context.NONE);
        assertNotNull(response);


        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());

        }

    }


    private SmsClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }


}
