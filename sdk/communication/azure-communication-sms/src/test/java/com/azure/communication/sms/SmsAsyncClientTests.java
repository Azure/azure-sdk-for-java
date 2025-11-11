// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.SmsDeliveryReport;
import com.azure.communication.sms.implementation.models.MessagingConnectOptions;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SmsAsyncClientTests extends SmsTestBase {
    private SmsAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        assumeTrue(shouldEnableSmsTests());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsUsingConnectionString(HttpClient httpClient) {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsUsingConnectionString");
        assertNotNull(asyncClient);
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE))
                .assertNext(this::assertHappyPath)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        SmsClientBuilder builder = getSmsClientWithToken(httpClient, tokenCredential);
        asyncClient = setupAsyncClient(builder, "sendSmsUsingTokenCredential");
        assertNotNull(asyncClient);
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE))
                .assertNext(this::assertHappyPath)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToGroup(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsToGroup");

        // Action & Assert
        StepVerifier
                .create(asyncClient.send(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE))
                .assertNext((Iterable<SmsSendResult> sendResults) -> {
                    for (SmsSendResult result : sendResults) {
                        assertHappyPath(result);
                    }
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToGroupWithOptions(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsToGroupWithOptions");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        // Action & Assert
        StepVerifier
                .create(asyncClient.sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER),
                        MESSAGE, options))
                .assertNext((Response<Iterable<SmsSendResult>> response) -> {
                    for (SmsSendResult result : response.getValue()) {
                        assertHappyPath(result);
                    }
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToSingleNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsToSingleNumber");

        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        StepVerifier.create(response).assertNext(this::assertHappyPath).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToSingleNumberWithOptions(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsToSingleNumberWithOptions");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        // Action & Assert
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options))
                .assertNext(this::assertHappyPath)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToSingleNumberWithContext(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsToSingleNumberWithOptions");
        SmsSendOptions options = new SmsSendOptions();
        Context context = new Context("context_key", "context_value");

        // Action & Assert
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options, context))
                .assertNext(this::assertHappyPath)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromFakeNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendFromFakeNumber");
        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send("+155512345678", TO_PHONE_NUMBER, MESSAGE);
        StepVerifier.create(response)
                .expectErrorMatches(
                        exception -> ((HttpResponseException) exception).getResponse().getStatusCode() == 401)
                .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromUnauthorizedNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendFromUnauthorizedNumber");

        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send("+18007342577", TO_PHONE_NUMBER, MESSAGE);
        StepVerifier.create(response)
                .expectErrorMatches(
                        exception -> ((HttpResponseException) exception).getResponse().getStatusCode() == 401)
                .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendTwoMessages(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendTwoMessages");

        // Action & Assert
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE)).assertNext(firstResult -> {
            StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE))
                    .assertNext((SmsSendResult secondResult) -> {
                        assertNotEquals(firstResult.getMessageId(), secondResult.getMessageId());
                        assertHappyPath(firstResult);
                        assertHappyPath(secondResult);
                    });
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToNullNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsToSingleNumber");

        // Action & Assert
        String to = null;
        Mono<SmsSendResult> response = asyncClient.send(FROM_PHONE_NUMBER, to, MESSAGE);
        StepVerifier.create(response).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsFromNullNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsFromNullNumber");

        // Action & Assert
        Mono<SmsSendResult> response = asyncClient.send(null, TO_PHONE_NUMBER, MESSAGE);
        StepVerifier.create(response).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void checkForRepeatabilityOptions(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "checkForRepeatabilityOptions");

        StepVerifier.create(
                asyncClient
                        .sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE,
                                null,
                                Context.NONE)
                        .flatMap(requestResponse -> {
                            return requestResponse.getRequest().getBody().last();
                        }))
                .assertNext(bodyBuff -> {
                    String bodyRequest = StandardCharsets.UTF_8.decode(bodyBuff).toString();
                    assertTrue(bodyRequest.contains("repeatabilityRequestId"));
                    assertTrue(bodyRequest.contains("repeatabilityFirstSent"));
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsWithDeliveryReportTimeoutAsync(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsWithDeliveryReportTimeoutAsync");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setDeliveryReportTimeoutInSeconds(60);
        options.setTag("TimeoutTestAsync");

        // Action & Assert
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options))
                .assertNext(this::assertHappyPath)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsWithMessagingConnectAsync(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsWithMessagingConnectAsync");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);

        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("TestPartnerAsync");

        Map<String, Object> partnerParams = new HashMap<>();
        partnerParams.put("apiKey", "test-api-key-async-123");
        messagingConnect.setPartnerParams(partnerParams);
        options.setMessagingConnect(messagingConnect);

        // Action & Assert
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options))
                .expectErrorMatches(throwable -> throwable instanceof HttpResponseException
                        && ((HttpResponseException) throwable).getResponse().getStatusCode() == 400
                        && throwable.getMessage().contains("Partner is invalid"))
                .verify();
    }

    @Test
    public void sendSmsWithMessagingConnectValidationAsync_MissingPartnerParams() {
        // Arrange - use a simple mock client without recording
        SmsAsyncClient simpleClient = new SmsClientBuilder()
                .connectionString("endpoint=https://example.communication.azure.com/;accesskey=fake_key")
                .buildAsyncClient();

        SmsSendOptions options = new SmsSendOptions();
        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("TestPartnerAsync"); // partnerParams is missing/null
        options.setMessagingConnect(messagingConnect);

        // Action & Assert
        StepVerifier.create(simpleClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("MessagingConnect partnerParams cannot be null or empty"))
                .verify();
    }

    @Test
    public void sendSmsWithMessagingConnectValidationAsync_MissingPartner() {
        // Arrange - use a simple mock client without recording
        SmsAsyncClient simpleClient = new SmsClientBuilder()
                .connectionString("endpoint=https://example.communication.azure.com/;accesskey=fake_key")
                .buildAsyncClient();

        SmsSendOptions options = new SmsSendOptions();
        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        // Partner is missing, but partnerParams is provided
        Map<String, Object> partnerParams = new HashMap<>();
        partnerParams.put("apiKey", "test-api-key-async-123");
        messagingConnect.setPartnerParams(partnerParams);
        options.setMessagingConnect(messagingConnect);

        // Action & Assert
        StepVerifier.create(simpleClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("MessagingConnect partner cannot be null or empty"))
                .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestSerializationWithNewFieldsAsync(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsRequestSerializationWithNewFieldsAsync");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setDeliveryReportTimeoutInSeconds(180);
        options.setTag("SerializationTestAsync");

        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("SerializationPartnerAsync");

        Map<String, Object> partnerParams = new HashMap<>();
        partnerParams.put("apiKey", "serialization-test-key-async");
        messagingConnect.setPartnerParams(partnerParams);
        options.setMessagingConnect(messagingConnect);

        // Action & Assert
        StepVerifier
                .create(asyncClient.sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), MESSAGE,
                        options,
                        Context.NONE))
                .expectErrorMatches(throwable -> throwable instanceof HttpResponseException
                        && ((HttpResponseException) throwable).getResponse().getStatusCode() == 400
                        && throwable.getMessage().contains("Partner is invalid"))
                .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getDeliveryReport(HttpClient httpClient) {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "getDeliveryReportAsync");

        // First send a message to get a message ID
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);

        Mono<SmsDeliveryReport> deliveryReportMono = asyncClient
                .send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options).flatMap(sendResult -> {
                    assertNotNull(sendResult.getMessageId());
                    // First attempt should fail with 404 immediately after SMS submission
                    return asyncClient.getDeliveryReport(sendResult.getMessageId())
                            .onErrorResume(HttpResponseException.class, ex -> {
                                // Expect 404 initially as delivery report is not ready yet
                                assertEquals(404, ex.getResponse().getStatusCode());
                                // Wait 10 seconds and try again when delivery report should be available
                                return Mono.delay(Duration.ofSeconds(10))
                                        .then(asyncClient.getDeliveryReport(sendResult.getMessageId()));
                            });
                });

        // After waiting, delivery report should be available
        StepVerifier.create(deliveryReportMono).expectNextCount(1).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getDeliveryReportWithResponse(HttpClient httpClient) {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "getDeliveryReportWithResponseAsync");

        // First send a message to get a message ID
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);

        Mono<Response<SmsDeliveryReport>> deliveryReportMono = asyncClient
                .send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options).flatMap(sendResult -> {
                    assertNotNull(sendResult.getMessageId());
                    // First attempt should fail with 404 immediately after SMS submission
                    return asyncClient.getDeliveryReportWithResponse(sendResult.getMessageId(), Context.NONE)
                            .onErrorResume(HttpResponseException.class, ex -> {
                                // Expect 404 initially as delivery report is not ready yet
                                assertEquals(404, ex.getResponse().getStatusCode());
                                // Wait 10 seconds and try again when delivery report should be available
                                return Mono.delay(Duration.ofSeconds(10))
                                        .then(asyncClient.getDeliveryReportWithResponse(sendResult.getMessageId(),
                                                Context.NONE));
                            });
                });

        // After waiting, delivery report should be available
        StepVerifier.create(deliveryReportMono).expectNextCount(1).verifyComplete();
    }

    @Test
    public void getDeliveryReportWithNullMessageId() {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(null);
        asyncClient = setupAsyncClient(builder, "getDeliveryReportNullMessageIdAsync");

        StepVerifier.create(asyncClient.getDeliveryReport(null)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    public void getDeliveryReportWithEmptyMessageId() {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(null);
        asyncClient = setupAsyncClient(builder, "getDeliveryReportEmptyMessageIdAsync");

        StepVerifier.create(asyncClient.getDeliveryReport("")).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    public void getOptOutsAsyncClient() {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(null);
        asyncClient = setupAsyncClient(builder, "getOptOutsAsyncClientTest");

        OptOutsAsyncClient optOutsAsyncClient = asyncClient.getOptOutsAsyncClient();
        assertNotNull(optOutsAsyncClient);

        // Verify that calling getOptOutsAsyncClient multiple times returns the same
        // instance
        OptOutsAsyncClient optOutsAsyncClient2 = asyncClient.getOptOutsAsyncClient();
        assertEquals(optOutsAsyncClient, optOutsAsyncClient2);
    }

    private SmsAsyncClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    private void assertHappyPath(SmsSendResult sendResult) {
        assertTrue(sendResult.isSuccessful());
        assertEquals(sendResult.getHttpStatusCode(), 202);
        assertNotNull(sendResult.getMessageId());
    }
}
