// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.models.OptOutResult;
import com.azure.communication.sms.models.OptOutCheckResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TelcoMessagingAsyncClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    // @EnabledIf("shouldEnableSmsTests")
    public void telcoMessagingClientCreation(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        assertNotNull(client);
        assertNotNull(client.getSmsAsyncClient());
        assertNotNull(client.getDeliveryReportsAsyncClient());
        assertNotNull(client.getOptOutsAsyncClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    // @EnabledIf("shouldEnableSmsTests")
    public void sendSmsViaOrganizedClient(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        StepVerifier.create(client.getSmsAsyncClient().send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE))
            .assertNext(response -> {
                assertNotNull(response);
                assertNotNull(response.getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    // @EnabledIf("shouldEnableSmsTests")
    public void addOptOut(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        StepVerifier.create(client.getOptOutsAsyncClient().addOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER))
            .assertNext(results -> {
                assertNotNull(results);
                assertFalse(results.isEmpty());
                OptOutResult result = results.get(0);
                assertNotNull(result.getTo());
                assertEquals(TO_PHONE_NUMBER, result.getTo());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    // @EnabledIf("shouldEnableSmsTests")
    public void addOptOutMultiple(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();
        List<String> recipients = Arrays.asList(TO_PHONE_NUMBER, "+15551234568");

        StepVerifier.create(client.getOptOutsAsyncClient().addOptOut(FROM_PHONE_NUMBER, recipients))
            .assertNext(results -> {
                assertNotNull(results);
                assertEquals(2, results.size());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    // @EnabledIf("shouldEnableSmsTests")
    public void checkOptOut(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        StepVerifier.create(client.getOptOutsAsyncClient().checkOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER))
            .assertNext(results -> {
                assertNotNull(results);
                assertFalse(results.isEmpty());
                OptOutCheckResult result = results.get(0);
                assertNotNull(result.getTo());
                assertEquals(TO_PHONE_NUMBER, result.getTo());
                // isOptedOut could be true, false, or null
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    // @EnabledIf("shouldEnableSmsTests")
    public void removeOptOut(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        StepVerifier.create(client.getOptOutsAsyncClient().removeOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER))
            .assertNext(results -> {
                assertNotNull(results);
                assertFalse(results.isEmpty());
                OptOutResult result = results.get(0);
                assertNotNull(result.getTo());
                assertEquals(TO_PHONE_NUMBER, result.getTo());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    // @EnabledIf("shouldEnableSmsTests")
    public void addOptOutWithResponse(HttpClient httpClient) {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildAsyncClient();

        StepVerifier
            .create(client.getOptOutsAsyncClient()
                .addOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE))
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
                assertNotNull(response.getValue());
                assertFalse(response.getValue().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    public void optOutWithNullFromThrowsException() {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(null).buildAsyncClient();

        StepVerifier.create(client.getOptOutsAsyncClient().addOptOut(null, TO_PHONE_NUMBER))
            .expectError(NullPointerException.class)
            .verify();
    }

    @Test
    public void optOutWithNullToThrowsException() {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(null).buildAsyncClient();

        StepVerifier.create(client.getOptOutsAsyncClient().addOptOut(FROM_PHONE_NUMBER, (String) null))
            .expectError(NullPointerException.class)
            .verify();
    }

    @Test
    public void deliveryReportWithNullMessageIdThrowsException() {
        TelcoMessagingAsyncClient client = getTelcoMessagingClientUsingConnectionString(null).buildAsyncClient();

        StepVerifier.create(client.getDeliveryReportsAsyncClient().getDeliveryReport(null))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    protected TelcoMessagingClientBuilder getTelcoMessagingClientUsingConnectionString(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder();
        builder.connectionString(CONNECTION_STRING);

        if (httpClient != null) {
            builder
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        // addTestProxySanitizer();
        // addTestProxyMatcher();
        return builder;
    }

    protected TelcoMessagingClientBuilder getTelcoMessagingClientWithToken(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder();
        builder
            .endpoint(new com.azure.communication.common.implementation.CommunicationConnectionString(CONNECTION_STRING)
                .getEndpoint())
            .credential(new AzureKeyCredential("test-key"))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        // addTestProxySanitizer();
        // addTestProxyMatcher();
        return builder;
    }
}
