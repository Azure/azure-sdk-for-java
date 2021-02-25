// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;


<<<<<<< HEAD
import com.azure.communication.sms.models.SmsSendOptions;
=======
>>>>>>> 94f7a8b318 (draft of the implementation)
import com.azure.communication.sms.models.SmsSendResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.http.HttpClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;

<<<<<<< HEAD
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
        to.add(TO_PHONE_NUMBER);
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(asyncClient);
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(FROM_PHONE_NUMBER, to, MESSAGE, null);
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
        to.add(TO_PHONE_NUMBER);
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "send");

        // Action & Assert

        Mono<Iterable<SmsSendResult>> response = asyncClient.send(FROM_PHONE_NUMBER, to, MESSAGE, null);
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
        asyncClient = setupAsyncClient(builder, "send");

        // Action & Assert

        Mono<SmsSendResult> response = asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, null);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
=======
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SmsAsyncClientTests extends SmsTestBase {
    private List<String> to;
    private String from;
    private String message;
    private SmsAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
>>>>>>> 94f7a8b318 (draft of the implementation)
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
<<<<<<< HEAD
    public void sendToSingleUserWithOptions(HttpClient httpClient) {

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");


        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "send");

        // Action & Assert

        Mono<SmsSendResult> response = asyncClient.send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE, options);
=======
    public void createAsyncClientUsingConnectionString(HttpClient httpClient) {

        from = "+18335102092";
        to = new ArrayList<String>();
        to.add("+18336388593");
        message = "hello";
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(asyncClient);

        Mono<List<SmsSendResult>> response = asyncClient.send(from, to, message, null);
>>>>>>> 94f7a8b318 (draft of the implementation)
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();


    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
<<<<<<< HEAD
    public void sendToMultipleUsersWithOptions(HttpClient httpClient) {

        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("New Tag");

        to = new ArrayList<String>();
        to.add(TO_PHONE_NUMBER);
=======
    public void send(HttpClient httpClient) {

        from = "+18335102092";
        to = new ArrayList<String>();
        to.add("+18336388593");
        message = "hello";
>>>>>>> 94f7a8b318 (draft of the implementation)
        // Arrange
        SmsClientBuilder builder = getSmsClient(httpClient);
        asyncClient = setupAsyncClient(builder, "send");

        // Action & Assert

<<<<<<< HEAD
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(FROM_PHONE_NUMBER, to, MESSAGE, options);
=======
        Mono<List<SmsSendResult>> response = asyncClient.send(from, to, message, null);
>>>>>>> 94f7a8b318 (draft of the implementation)
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }

<<<<<<< HEAD
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendToIncorrectPhoneNumber(HttpClient httpClient) {

        to = new ArrayList<String>();
        to.add(FAIL_PHONE_NUMBER);
        SmsClientBuilder builder = getSmsClientUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createAsyncSmsClientUsingConnectionString");
        assertNotNull(asyncClient);
        Mono<Iterable<SmsSendResult>> response = asyncClient.send(FROM_PHONE_NUMBER, to, MESSAGE, null);
        assertNotNull(response);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item);
            })
            .verifyComplete();
    }






    private SmsAsyncClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }


=======

    private SmsAsyncClient setupAsyncClient(SmsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }


>>>>>>> 94f7a8b318 (draft of the implementation)
}
