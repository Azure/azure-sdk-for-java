// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SmsClientTests extends SmsTestBase {
    private SmsClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        assumeTrue(shouldEnableSmsTests());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsUsingConnectionString(HttpClient httpClient) {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsUsingConnectionStringSync");
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertHappyPath(sendResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        SmsClientBuilder  builder = getSmsClientWithToken(httpClient, tokenCredential);
        client = setupSyncClient(builder, "sendSmsUsingTokenCredentialSync");
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertHappyPath(sendResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToGroup(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsToGroupSync");

        // Action & Assert
        Iterable<SmsSendResult> sendResults = client.send(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE);
        for (SmsSendResult result : sendResults) {
            assertHappyPath(result);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToGroupWithOptions(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsToGroupWithOptionsSync");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");
        // Action & Assert
        Response<Iterable<SmsSendResult>> sendResults = client.sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE, options, Context.NONE);
        for (SmsSendResult result : sendResults.getValue()) {
            assertHappyPath(result);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToSingleNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsToSingleNumberSync");

        // Action & Assert
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertHappyPath(sendResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsToSingleNumberWithOptions(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsToSingleNumberWithOptionsSync");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        // Action & Assert
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
        assertHappyPath(sendResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromFakeNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendFromFakeNumberSync");

        // Action & Assert
        try {
            client.send("+15550000000", TO_PHONE_NUMBER, MESSAGE);
        } catch (Exception exception) {
            assertEquals(400, ((HttpResponseException) exception).getResponse().getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromUnauthorizedNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendFromUnauthorizedNumberSync");

        // Action & Assert
        try {
            SmsSendResult response = client.send("+18007342577", TO_PHONE_NUMBER, MESSAGE);
        } catch (Exception exception) {
            assertNotNull(((HttpResponseException) exception).getResponse().getStatusCode());
            assertEquals(401, ((HttpResponseException) exception).getResponse().getStatusCode());
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToFakePhoneNumber(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendToFakePhoneNumberSync");

        // Action & Assert
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, "+15550000000", MESSAGE);
        assertFalse(sendResult.isSuccessful());
        assertEquals(sendResult.getHttpStatusCode(), 400);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendTwoMessages(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendTwoMessagesSync");

        // Action & Assert
        SmsSendResult firstResponse = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        SmsSendResult secondResponse = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertNotEquals(firstResponse.getMessageId(), secondResponse.getMessageId());
        assertHappyPath(firstResponse);
        assertHappyPath(secondResponse);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void checkForRepeatabilityOptions(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "checkForRepeatabilityOptions");
        // Action & Assert
        Response<Iterable<SmsSendResult>> response = client.sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE, null, Context.NONE);
        String bodyRequest = new String(response.getRequest().getBody().blockLast().array());
        assertTrue(bodyRequest.contains("repeatabilityRequestId"));
        assertTrue(bodyRequest.contains("repeatabilityFirstSent"));
    }

    private SmsClient setupSyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    private void assertHappyPath(SmsSendResult sendResult) {
        assertTrue(sendResult.isSuccessful());
        assertEquals(sendResult.getHttpStatusCode(), 202);
        assertNotNull(sendResult.getMessageId());
    }
}
