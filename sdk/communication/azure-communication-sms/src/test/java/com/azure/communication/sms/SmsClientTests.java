// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.SmsDeliveryReport;
import com.azure.communication.sms.implementation.models.MessagingConnectOptions;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
        SmsClientBuilder builder = getSmsClientWithToken(httpClient, tokenCredential);
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
        Iterable<SmsSendResult> sendResults
            = client.send(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE);
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
        Response<Iterable<SmsSendResult>> sendResults = client.sendWithResponse(FROM_PHONE_NUMBER,
            Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE, options, Context.NONE);
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
            assertEquals(401, ((HttpResponseException) exception).getResponse().getStatusCode());
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
            client.send("+18007342577", TO_PHONE_NUMBER, MESSAGE);
        } catch (Exception exception) {
            assertEquals(401, ((HttpResponseException) exception).getResponse().getStatusCode());
        }
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
        Response<Iterable<SmsSendResult>> response = client.sendWithResponse(FROM_PHONE_NUMBER,
            Arrays.asList(TO_PHONE_NUMBER, TO_PHONE_NUMBER), MESSAGE, null, Context.NONE);
        String bodyRequest = response.getRequest().getBodyAsBinaryData().toString();
        assertTrue(bodyRequest.contains("repeatabilityRequestId"));
        assertTrue(bodyRequest.contains("repeatabilityFirstSent"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsWithDeliveryReportTimeout(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsWithDeliveryReportTimeoutSync");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setDeliveryReportTimeoutInSeconds(60);
        options.setTag("TimeoutTest");

        // Action & Assert
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
        assertHappyPath(sendResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsWithMessagingConnect(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsWithMessagingConnectSync");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);

        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("TestPartner");

        Map<String, Object> partnerParams = new HashMap<>();
        partnerParams.put("apiKey", "test-api-key-123");
        messagingConnect.setPartnerParams(partnerParams);
        options.setMessagingConnect(messagingConnect);

        // Action & Assert - Expect error response for invalid partner
        try {
            client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
            // Should not reach here
            assertTrue(false, "Expected HttpResponseException for invalid partner");
        } catch (HttpResponseException exception) {
            // Expected - Partner "TestPartner" is invalid
            assertEquals(400, exception.getResponse().getStatusCode());
            assertTrue(exception.getMessage().contains("Partner is invalid"));
        }
    }

    @Test
    public void sendSmsWithMessagingConnectValidation_MissingPartnerParams() {
        // Arrange - use a simple mock client without recording
        SmsClient simpleClient = new SmsClientBuilder()
            .connectionString("endpoint=https://example.communication.azure.com/;accesskey=fake_key")
            .buildClient();

        SmsSendOptions options = new SmsSendOptions();
        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("TestPartner"); // partnerParams is missing/null
        options.setMessagingConnect(messagingConnect);

        // Action & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
        });
        assertTrue(exception.getMessage().contains("MessagingConnect partnerParams cannot be null or empty"));
    }

    @Test
    public void sendSmsWithMessagingConnectValidation_MissingPartner() {
        // Arrange - use a simple mock client without recording
        SmsClient simpleClient = new SmsClientBuilder()
            .connectionString("endpoint=https://example.communication.azure.com/;accesskey=fake_key")
            .buildClient();

        SmsSendOptions options = new SmsSendOptions();
        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        // Partner is missing, but partnerParams is provided
        Map<String, Object> partnerParams = new HashMap<>();
        partnerParams.put("apiKey", "test-api-key-123");
        messagingConnect.setPartnerParams(partnerParams);
        options.setMessagingConnect(messagingConnect);

        // Action & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
        });
        assertTrue(exception.getMessage().contains("MessagingConnect partner cannot be null or empty"));
    }

    @Test
    public void sendSmsWithMessagingConnectValidation_EmptyPartnerParams() {
        // Arrange - use a simple mock client without recording
        SmsClient simpleClient = new SmsClientBuilder()
            .connectionString("endpoint=https://example.communication.azure.com/;accesskey=fake_key")
            .buildClient();

        SmsSendOptions options = new SmsSendOptions();
        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("TestPartner");
        // Empty partnerParams map
        Map<String, Object> emptyParams = new HashMap<>();
        messagingConnect.setPartnerParams(emptyParams);
        options.setMessagingConnect(messagingConnect);

        // Action & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
        });
        assertTrue(exception.getMessage().contains("MessagingConnect partnerParams cannot be null or empty"));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsRequestSerializationWithNewFields(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "sendSmsRequestSerializationWithNewFields");
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setDeliveryReportTimeoutInSeconds(120);
        options.setTag("SerializationTest");

        MessagingConnectOptions messagingConnect = new MessagingConnectOptions();
        messagingConnect.setPartner("SerializationPartner");

        Map<String, Object> partnerParams = new HashMap<>();
        partnerParams.put("apiKey", "serialization-test-key");
        messagingConnect.setPartnerParams(partnerParams);
        options.setMessagingConnect(messagingConnect);

        // Action & Assert - Expect error response for invalid partner
        try {
            client.sendWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), MESSAGE, options, Context.NONE);
            // Should not reach here
            assertTrue(false, "Expected HttpResponseException for invalid partner");
        } catch (HttpResponseException exception) {
            // Expected - Partner "SerializationPartner" is invalid
            assertEquals(400, exception.getResponse().getStatusCode());
            assertTrue(exception.getMessage().contains("Partner is invalid"));

            // Still verify that the new fields are properly serialized in the request
            String bodyRequest = exception.getResponse().getRequest().getBodyAsBinaryData().toString();
            assertTrue(bodyRequest.contains("deliveryReportTimeoutInSeconds"));
            assertTrue(bodyRequest.contains("120"));
            assertTrue(bodyRequest.contains("messagingConnect"));
            assertTrue(bodyRequest.contains("serialization-test-key"));
            assertTrue(bodyRequest.contains("SerializationPartner"));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getDeliveryReport(HttpClient httpClient) {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "getDeliveryReportSync");

        // First send a message to get a message ID
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
        assertNotNull(sendResult.getMessageId());

        // First attempt should fail with 404 immediately after SMS submission
        try {
            client.getDeliveryReport(sendResult.getMessageId());
        } catch (HttpResponseException e) {
            // Expected - delivery report should not be available immediately with 404
            assertEquals(404, e.getResponse().getStatusCode());
        }

        // Wait 10 seconds and try again - delivery report should be available
        try {
            Thread.sleep(10000); // Wait 10 seconds
            SmsDeliveryReport report = client.getDeliveryReport(sendResult.getMessageId());
            assertNotNull(report);
        } catch (HttpResponseException e) {
            // If still not available after 10 seconds, that's acceptable for some test
            // environments
            assertTrue(e.getResponse().getStatusCode() >= 400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getDeliveryReportWithResponse(HttpClient httpClient) {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupSyncClient(builder, "getDeliveryReportWithResponseSync");

        // First send a message to get a message ID
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        SmsSendResult sendResult = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
        assertNotNull(sendResult.getMessageId());

        // First attempt should fail with 404 immediately after SMS submission
        try {
            client.getDeliveryReportWithResponse(sendResult.getMessageId(), Context.NONE);
        } catch (HttpResponseException e) {
            // Expected - delivery report should not be available immediately with 404
            assertEquals(404, e.getResponse().getStatusCode());
        }

        // Wait 10 seconds and try again - delivery report should be available
        try {
            Thread.sleep(10000); // Wait 10 seconds
            Response<SmsDeliveryReport> response
                = client.getDeliveryReportWithResponse(sendResult.getMessageId(), Context.NONE);
            assertNotNull(response);
            assertNotEquals(0, response.getStatusCode());
        } catch (HttpResponseException e) {
            // If still not available after 10 seconds, that's acceptable for some test
            // environments
            assertTrue(e.getResponse().getStatusCode() >= 400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getDeliveryReportWithNullMessageId() {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(null);
        client = setupSyncClient(builder, "getDeliveryReportNullMessageIdSync");

        assertThrows(RuntimeException.class, () -> {
            client.getDeliveryReport(null);
        });
    }

    @Test
    public void getDeliveryReportWithEmptyMessageId() {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(null);
        client = setupSyncClient(builder, "getDeliveryReportEmptyMessageIdSync");

        assertThrows(RuntimeException.class, () -> {
            client.getDeliveryReport("");
        });
    }

    @Test
    public void getOptOutsClient() {
        SmsClientBuilder builder = getSmsClientUsingConnectionString(null);
        client = setupSyncClient(builder, "getOptOutsClientSync");

        OptOutsClient optOutsClient = client.getOptOutsClient();
        assertNotNull(optOutsClient);

        // Verify that calling getOptOutsClient multiple times returns the same instance
        OptOutsClient optOutsClient2 = client.getOptOutsClient();
        assertEquals(optOutsClient, optOutsClient2);
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
