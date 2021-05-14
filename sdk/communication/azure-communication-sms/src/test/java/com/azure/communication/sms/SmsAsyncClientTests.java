// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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
            .assertNext(sendResult -> {
                assertHappyPath(sendResult);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        SmsClientBuilder  builder = getSmsClientWithToken(httpClient, tokenCredential);
        asyncClient = setupAsyncClient(builder, "sendSmsUsingTokenCredential");
        assertNotNull(asyncClient);
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE))
            .assertNext(sendResult -> {
                assertHappyPath(sendResult);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToGroup(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendSmsToGroup");

        // Action & Assert
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE))
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
        StepVerifier.create(asyncClient.sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE, options))
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
        StepVerifier.create(response)
            .assertNext(sendResult -> {
                assertHappyPath(sendResult);
            })
            .verifyComplete();
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
            .assertNext((SmsSendResult sendResult) -> {
                assertHappyPath(sendResult);
            })
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
            .expectErrorMatches(exception ->
                ((HttpResponseException) exception).getResponse().getStatusCode() == 400).verify();
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
        .expectErrorMatches(exception ->
               ((HttpResponseException) exception).getResponse().getStatusCode() == 401).verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToFakePhoneNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendToFakePhoneNumber");
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(FROM_PHONE_NUMBER, Arrays.asList("+15550000000"), MESSAGE);

        // Action & Assert
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendTwoMessages(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "sendTwoMessages");

        // Action & Assert
        StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE))
            .assertNext(firstResult -> {
                StepVerifier.create(asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE))
                    .assertNext((SmsSendResult secondResult) -> {
                        assertNotEquals(firstResult.getMessageId(), secondResult.getMessageId());
                        assertHappyPath(firstResult);
                        assertHappyPath(secondResult);
                    });
            })
            .verifyComplete();
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
        String from = null;
        Mono<SmsSendResult> response = asyncClient.send(from, TO_PHONE_NUMBER, MESSAGE);
        StepVerifier.create(response).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void checkForRepeatabilityOptions(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "checkForRepeatabilityOptions");

        StepVerifier.create(
            asyncClient.sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE, null, Context.NONE)
            .flatMap(requestResponse -> {
                return requestResponse.getRequest().getBody().last();
            })
        ).assertNext(bodyBuff -> {
            String bodyRequest = new String(bodyBuff.array());
            assertTrue(bodyRequest.contains("repeatabilityRequestId"));
            assertTrue(bodyRequest.contains("repeatabilityFirstSent"));
        })
        .verifyComplete();
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
