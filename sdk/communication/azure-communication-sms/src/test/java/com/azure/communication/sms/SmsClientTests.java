// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.*;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SmsClientTests extends SmsTestBase {
    private List<String> to;
    private SmsClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createSyncClientUsingConnectionString(HttpClient httpClient) {

        to = new ArrayList<String>();
        to.add(SMS_SERVICE_PHONE_NUMBER);

        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupAsyncClient(builder, "createSyncClientUsingConnectionString");
        assertNotNull(client);
        Iterable<SmsSendResult> response = client.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, null, Context.NONE);
        assertNotNull(response);
        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());
            assertNull(r.getErrorMessage());

        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void builderNoEndpoint(HttpClient httpClient) {

        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        builder
            .endpoint(null)
            .httpClient(new NoOpHttpClient());
        assertThrows(Exception.class, () -> {
            builder.buildAsyncClient();
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendSmsUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        to = new ArrayList<>();
        to.add(SMS_SERVICE_PHONE_NUMBER);
        SmsClientBuilder  builder = getSmsClientWithToken(httpClient, tokenCredential);
        client = setupAsyncClient(builder, "sendSmsUsingTokenCredential");
        assertNotNull(client);
        Iterable<SmsSendResult> response = client.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, null, Context.NONE);
        assertNotNull(response);
        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());

        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToIncorrectPhoneNumber(HttpClient httpClient) {

        to = new ArrayList<String>();
        to.add("+155512345678");

        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupAsyncClient(builder, "sendToIncorrectPhoneNumber");
        assertNotNull(client);
        Iterable<SmsSendResult> response = client.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, null, Context.NONE);
        assertNotNull(response);
        for (SmsSendResult r : response) {
            assertFalse(r.isSuccessful());

        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromFakeNumber(HttpClient httpClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "sendToSingleUserWithOptions");

        // Action & Assert
        try {
            SmsSendResult response = client.send("+155512345678", SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        } catch (Exception exception) {
            assertEquals(400, ((HttpResponseException) exception).getResponse().getStatusCode());
        }


    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendFromUnauthorizedNumber(HttpClient httpClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "sendFromUnauthorizedNumber");

        // Action & Assert
        try {
            SmsSendResult response = client.send("+18007342577", SMS_SERVICE_PHONE_NUMBER, MESSAGE, options, Context.NONE);
        } catch (Exception exception) {
            assertEquals(404, ((HttpResponseException) exception).getResponse().getStatusCode());
        }


    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToMultipleUsers(HttpClient httpClient) {

        to = new ArrayList<String>();
        to.add(SMS_SERVICE_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "sendToMultipleUsers");
        // Action & Assert
        Iterable<SmsSendResult> response = client.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE);
        assertNotNull(response);
        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());

        }
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
        client = setupAsyncClient(builder, "sendToMultipleUsersWithOptions");
        // Action & Assert
        Iterable<SmsSendResult> response = client.send(SMS_SERVICE_PHONE_NUMBER, to, MESSAGE, options, Context.NONE);
        assertNotNull(response);
        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());

        }

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToSingleUser(HttpClient httpClient) {
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "sendToSingleUser");
        // Action & Assert
        SmsSendResult response = client.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE);
        assertNotNull(response);
        assertTrue(response.isSuccessful());

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendIdempotencyCheck(HttpClient httpClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "sendToSingleUserWithOptions");

        // Action & Assert
        SmsSendResult response1 = client.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        assertTrue(response1.isSuccessful());
        SmsSendResult response2 = client.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE, options);
        assertTrue(response2.isSuccessful());
        assertNotEquals(response1.getMessageId(), response2.getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToSingleUserWithOptions(HttpClient httpClient) {

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");
        to = new ArrayList<String>();
        to.add(SMS_SERVICE_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "sendToSingleUserWithOptions");
        // Action & Assert
        SmsSendResult response = client.send(SMS_SERVICE_PHONE_NUMBER, SMS_SERVICE_PHONE_NUMBER, MESSAGE, options, Context.NONE);
        assertNotNull(response);
        assertTrue(response.isSuccessful());

    }


    private SmsClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
