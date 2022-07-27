// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email;

import com.azure.communication.email.models.*;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EmailAsyncClientTests extends EmailTestBase {

    private EmailAsyncClient emailAsyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendEmailToSingleRecipient(HttpClient httpClient) {
        emailAsyncClient = getEmailAsyncClient(httpClient);

        EmailAddress emailAddress = new EmailAddress(RECIPIENT_ADDRESS);

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage(SENDER_ADDRESS, content)
            .setRecipients(emailRecipients);

        StepVerifier.create(emailAsyncClient.send(emailMessage))
            .assertNext(response -> {
                assertNotNull(response.getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendEmailToMultipleRecipients(HttpClient httpClient) {
        emailAsyncClient = getEmailAsyncClient(httpClient);

        EmailAddress emailAddress = new EmailAddress(RECIPIENT_ADDRESS);
        EmailAddress emailAddress2 = new EmailAddress(SECOND_RECIPIENT_ADDRESS);

        ArrayList<EmailAddress> toAddressList = new ArrayList<>();
        toAddressList.add(emailAddress);
        toAddressList.add(emailAddress2);

        ArrayList<EmailAddress> ccAddressList = new ArrayList<>();
        ccAddressList.add(emailAddress);

        ArrayList<EmailAddress> bccAddressList = new ArrayList<>();
        bccAddressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients(toAddressList)
            .setCc(ccAddressList)
            .setBcc(bccAddressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage(SENDER_ADDRESS, content)
            .setRecipients(emailRecipients);

        StepVerifier.create(emailAsyncClient.send(emailMessage))
            .assertNext(response -> {
                assertNotNull(response.getMessageId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendEmailWithAttachment(HttpClient httpClient) {
        emailAsyncClient = getEmailAsyncClient(httpClient);

        EmailAddress emailAddress = new EmailAddress(RECIPIENT_ADDRESS);

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailAttachment attachment = new EmailAttachment(
            "attachment.txt",
            EmailAttachmentType.TXT,
            "dGVzdA=="
        );

        ArrayList<EmailAttachment> attachmentList = new ArrayList<>();
        attachmentList.add(attachment);

        EmailMessage emailMessage = new EmailMessage(SENDER_ADDRESS, content)
            .setRecipients(emailRecipients)
            .setAttachments(attachmentList);

        StepVerifier.create(emailAsyncClient.send(emailMessage))
            .assertNext(response -> {
                assertNotNull(response.getMessageId());
            })
            .verifyComplete();
    }

//    @ParameterizedTest
//    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
//    public void getMessageStatus(HttpClient httpClient) {
//        emailAsyncClient = getEmailAsyncClient(httpClient);
//
//        EmailAddress emailAddress = new EmailAddress(RECIPIENT_ADDRESS);
//
//        ArrayList<EmailAddress> addressList = new ArrayList<>();
//        addressList.add(emailAddress);
//
//        EmailRecipients emailRecipients = new EmailRecipients(addressList);
//
//        EmailContent content = new EmailContent("test subject")
//            .setPlainText("test message");
//
//        EmailMessage emailMessage = new EmailMessage(SENDER_ADDRESS, content)
//            .setRecipients(emailRecipients);
//
//        StepVerifier.create(emailAsyncClient.send(emailMessage))
//            .assertNext(sendResponse -> {
//                assertNotNull(sendResponse.getMessageId());
//
//                StepVerifier.create(emailAsyncClient.getSendStatus(sendResponse.getMessageId()))
//                    .assertNext(getStatusResponse -> {
//                        assertNotNull(getStatusResponse.getMessageId());
//                    })
//                    .verifyComplete();
//            })
//            .verifyComplete();
//    }
}
