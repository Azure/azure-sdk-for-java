// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email;

import com.azure.communication.email.models.*;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Timeout;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class EmailAsyncClientTests extends EmailTestBase {

    private EmailAsyncClient emailAsyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void sendEmailToSingleRecipient(HttpClient httpClient) {
        emailAsyncClient = getEmailAsyncClient(httpClient);

        EmailMessage message = new EmailMessage()
            .setSenderAddress(SENDER_ADDRESS)
            .setToRecipients(RECIPIENT_ADDRESS)
            .setSubject("test subject")
            .setBodyHtml("<h1>test message</h1>");

        StepVerifier.create(emailAsyncClient.beginSend(message).last())
            .assertNext(response -> {
                assertEquals(response.getValue().getStatus(), EmailSendStatus.SUCCEEDED);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void sendEmailToMultipleRecipients(HttpClient httpClient) {
        emailAsyncClient = getEmailAsyncClient(httpClient);

        EmailMessage message = new EmailMessage()
            .setSenderAddress(SENDER_ADDRESS)
            .setSubject("test subject")
            .setBodyPlainText("test message")
            .setToRecipients(RECIPIENT_ADDRESS, SECOND_RECIPIENT_ADDRESS)
            .setCcRecipients(RECIPIENT_ADDRESS)
            .setBccRecipients(RECIPIENT_ADDRESS);

        StepVerifier.create(emailAsyncClient.beginSend(message).last())
            .assertNext(response -> {
                assertEquals(response.getValue().getStatus(), EmailSendStatus.SUCCEEDED);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void sendEmailWithAttachment(HttpClient httpClient) {
        emailAsyncClient = getEmailAsyncClient(httpClient);

        EmailAttachment attachment = new EmailAttachment(
            "attachment.txt",
            "text/plain",
            BinaryData.fromString("test").toBytes()
        );

        EmailMessage message = new EmailMessage()
            .setSenderAddress(SENDER_ADDRESS)
            .setToRecipients(RECIPIENT_ADDRESS)
            .setSubject("test subject")
            .setBodyHtml("<h1>test message</h1>")
            .setAttachments(attachment);

        StepVerifier.create(emailAsyncClient.beginSend(message).last())
            .assertNext(response -> {
                assertEquals(response.getValue().getStatus(), EmailSendStatus.SUCCEEDED);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void sendEmailWithInlineAttachment(HttpClient httpClient) {
        emailAsyncClient = getEmailAsyncClient(httpClient);

        EmailAttachment attachment = new EmailAttachment(
            "inlineimage.jpg",
            "image/jpeg",
            BinaryData.fromString("test").toBytes()
        );
        attachment.setContentId("inline_image");

        EmailMessage message = new EmailMessage()
            .setSenderAddress(SENDER_ADDRESS)
            .setToRecipients(RECIPIENT_ADDRESS)
            .setSubject("test subject")
            .setBodyHtml("<h1>test message<img src=\"cid:inline_image\"></h1>")
            .setAttachments(attachment);

        StepVerifier.create(emailAsyncClient.beginSend(message).last())
            .assertNext(response -> {
                assertEquals(response.getValue().getStatus(), EmailSendStatus.SUCCEEDED);
            })
            .verifyComplete();
    }
}
