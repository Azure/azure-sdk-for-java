// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.http.HttpClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SmsAsyncClientTests extends SmsTestBase {
    private List<String> to;
    private SmsAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncClientUsingConnectionString(HttpClient httpClient) {
        to = new ArrayList<String>();
        to.add(SMS_SERVICE_PHONE_NUMBER);
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(asyncClient);
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, null);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        to = new ArrayList<>();
        to.add(SMS_SERVICE_PHONE_NUMBER);
        SmsClientBuilder builder = getSmsClientWithToken(httpClient, tokenCredential);
        asyncClient = setupAsyncClient(builder, "sendSmsUsingTokenCredential");
        assertNotNull(asyncClient);
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, null);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToMultipleUsers(HttpClient httpClient) {
        to = new ArrayList<String>();
        to.add(SMS_SERVICE_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "sendToMultipleUsers");
        // Action & Assert
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, null);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToSingleUser(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "sendToSingleUser");
        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE, null);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToSingleUserWithOptions(HttpClient httpClient) {

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "sendToSingleUserWithOptions");
        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToMultipleUsersWithOptions(HttpClient httpClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");
        to = new ArrayList<String>();
        to.add(SMS_SERVICE_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "sendToMultipleUsersWithOptions");
        // Action & Assert
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, options);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromFakeNumber(HttpClient httpClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "sendFromFakeNumber");
        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send("+155512345678", SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        StepVerifier.create(response)
            .expectErrorMatches(exception ->
                ((HttpResponseException) exception).getResponse().getStatusCode() == 400
            ).verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromUnauthorizedNumber(HttpClient httpClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "sendFromUnauthorizedNumber");
        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send("+18007342577", SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        StepVerifier.create(response)
            .expectErrorMatches(exception ->
                ((HttpResponseException) exception).getResponse().getStatusCode() == 404
            ).verify();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendIdempotencyCheck(HttpClient httpClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "sendIdempotencyCheck");
        // Action & Assert
        Mono<SmsSendResult> response1 = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        StepVerifier.create(response1)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
        assertTrue(response1.block().isSuccessful());
        Mono<SmsSendResult> response2 = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        StepVerifier.create(response2)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
        assertTrue(response2.block().isSuccessful());
        assertNotEquals(response1.block().getMessageId(), response2.block().getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToIncorrectPhoneNumber(HttpClient httpClient) {
        to = new ArrayList<String>();
        to.add("+155512345678");
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendToIncorrectPhoneNumber");
        assertNotNull(asyncClient);
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, null);
        assertNotNull(response);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
        Iterable<SmsSendResult> smsSendResults = response.block();
        for (SmsSendResult result : smsSendResults) {
            assertFalse(result.isSuccessful());
            assertEquals(result.getHttpStatusCode(), 400);
        }
    }

    private SmsAsyncClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

}
