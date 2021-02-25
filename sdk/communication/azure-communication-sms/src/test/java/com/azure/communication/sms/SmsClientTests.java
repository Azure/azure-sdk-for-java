// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;


<<<<<<< HEAD
import com.azure.communication.sms.models.SmsSendOptions;
=======
>>>>>>> 94f7a8b318 (draft of the implementation)
import com.azure.communication.sms.models.SmsSendResult;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.http.HttpClient;


import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD

=======
>>>>>>> 94f7a8b318 (draft of the implementation)
import static org.junit.jupiter.api.Assertions.*;

public class SmsClientTests extends SmsTestBase {
    private List<String> to;
<<<<<<< HEAD
    private SmsClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncClientUsingConnectionString(HttpClient httpClient) {


        to = new ArrayList<String>();
        to.add(TO_PHONE_NUMBER);
=======
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
>>>>>>> 94f7a8b318 (draft of the implementation)
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(client);


<<<<<<< HEAD
        Iterable<SmsSendResult> response = client.send(FROM_PHONE_NUMBER, to, MESSAGE, null, Context.NONE);
        assertNotNull(response);


        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());

        }


    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToIncorrectPhoneNumber(HttpClient httpClient) {


        to = new ArrayList<String>();
        to.add(FAIL_PHONE_NUMBER);
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        client = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(client);


        Iterable<SmsSendResult> response = client.send(FROM_PHONE_NUMBER, to, MESSAGE, null, Context.NONE);
        assertNotNull(response);


        for (SmsSendResult r : response) {
            assertFalse(r.isSuccessful());

        }


    }



    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToMultipleUsers(HttpClient httpClient) {

        to = new ArrayList<String>();
        to.add(TO_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "send");

        // Action & Assert

        Iterable<SmsSendResult> response = client.send(FROM_PHONE_NUMBER, to, MESSAGE);
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
        to.add(TO_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "send");

        // Action & Assert

        Iterable<SmsSendResult> response = client.send(FROM_PHONE_NUMBER, to, MESSAGE, options, Context.NONE);
        assertNotNull(response);


=======
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


>>>>>>> 94f7a8b318 (draft of the implementation)
        for (SmsSendResult r : response) {
            assertTrue(r.isSuccessful());

        }

    }

<<<<<<< HEAD
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToSingleUser(HttpClient httpClient) {


        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "send");

        // Action & Assert

        SmsSendResult response = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);
        assertNotNull(response);
        assertTrue(response.isSuccessful());

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToSingleUserWithOptions(HttpClient httpClient) {

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        to = new ArrayList<String>();
        to.add(TO_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        client = setupAsyncClient(builder, "send");

        // Action & Assert

        SmsSendResult response = client.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options, Context.NONE);
        assertNotNull(response);
        assertTrue(response.isSuccessful());

    }


    private SmsClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

=======

    private SmsClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

>>>>>>> 94f7a8b318 (draft of the implementation)

}
