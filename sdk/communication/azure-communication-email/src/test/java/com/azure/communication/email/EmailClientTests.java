// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email;

import com.azure.communication.email.models.*;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EmailClientTests extends EmailTestBase {

    private EmailClient emailClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendEmailToSingleRecipient(HttpClient httpClient) {
        emailClient = getEmailClient(httpClient);

        EmailAddress emailAddress = new EmailAddress(RECIPIENT_ADDRESS);

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage(SENDER_ADDRESS, content)
            .setRecipients(emailRecipients);

        SendEmailResult response = emailClient.send(emailMessage);
        assertNotNull(response.getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendEmailToMultipleRecipients(HttpClient httpClient) {
        emailClient = getEmailClient(httpClient);

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

        SendEmailResult response = emailClient.send(emailMessage);
        assertNotNull(response.getMessageId());
    }
    //
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void sendEmailWithAttachment(HttpClient httpClient) {
        emailClient = getEmailClient(httpClient);

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

        SendEmailResult response = emailClient.send(emailMessage);
        assertNotNull(response.getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getMessageStatus(HttpClient httpClient) {
        emailClient = getEmailClient(httpClient);

        EmailAddress emailAddress = new EmailAddress(RECIPIENT_ADDRESS);

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage(SENDER_ADDRESS, content)
            .setRecipients(emailRecipients);

        SendEmailResult sendEmailResult = emailClient.send(emailMessage);
        SendStatusResult sendStatusResult = emailClient.getSendStatus(sendEmailResult.getMessageId());
        assertNotNull(sendStatusResult.getStatus());
    }
}
