// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.email;

import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailAttachment;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;

public class ReadmeSamples {

    public EmailClient createEmailClientUsingAzureKeyCredential() {
        // BEGIN: readme-sample-createEmailClientUsingAzureKeyCredential
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        EmailClient emailClient = new EmailClientBuilder()
            .endpoint(endpoint)
            .credential(azureKeyCredential)
            .buildClient();
        // END: readme-sample-createEmailClientUsingAzureKeyCredential

        return emailClient;
    }

    public EmailClient createEmailClientWithConnectionString() {
        // BEGIN: readme-sample-createEmailClientWithConnectionString
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        EmailClient emailClient = new EmailClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: readme-sample-createEmailClientWithConnectionString

        return emailClient;
    }

    public EmailClient createEmailClientWithAAD() {
        // BEGIN: readme-sample-createEmailClientWithAAD
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<resource-name>.communication.azure.com/";

        EmailClient emailClient = new EmailClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createEmailClientWithAAD

        return emailClient;
    }

    public void sendEmailToSingleRecipient() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailToSingleRecipient
        EmailMessage message = new EmailMessage()
            .setSenderAddress("<sender-email-address>")
            .setToRecipients("<recipient-email-address>")
            .setSubject("test subject")
            .setBodyPlainText("test message");

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();

        System.out.println("Operation Id: " + response.getValue().getId());
        // END: readme-sample-sendEmailToSingleRecipient
    }

    public void sendEmailToMultipleRecipients() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailToMultipleRecipients
        EmailMessage message = new EmailMessage()
            .setSenderAddress("<sender-email-address>")
            .setSubject("test subject")
            .setBodyPlainText("test message")
            .setToRecipients("<recipient-email-address>", "<recipient-2-email-address>")
            .setCcRecipients("<cc-recipient-email-address>")
            .setBccRecipients("<bcc-recipient-email-address>");

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();

        System.out.println("Operation Id: " + response.getValue().getId());
        // END: readme-sample-sendEmailToMultipleRecipients
    }

    public void sendEmailToMultipleRecipientsWithOptions() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailToMultipleRecipientsWithOptions
        EmailAddress toAddress1 = new EmailAddress("<recipient-email-address>")
            .setDisplayName("Recipient");

        EmailAddress toAddress2 = new EmailAddress("<recipient-2-email-address>")
            .setDisplayName("Recipient 2");

        EmailMessage message = new EmailMessage()
            .setSenderAddress("<sender-email-address>")
            .setSubject("test subject")
            .setBodyPlainText("test message")
            .setToRecipients(toAddress1, toAddress2);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();

        System.out.println("Operation Id: " + response.getValue().getId());
        // END: readme-sample-sendEmailToMultipleRecipientsWithOptions
    }


    public void sendEmailWithAttachment() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailWithAttachment
        BinaryData attachmentContent = BinaryData.fromFile(new File("C:/attachment.txt").toPath());
        EmailAttachment attachment = new EmailAttachment(
            "attachment.txt",
            "text/plain",
            attachmentContent
        );
        
        EmailMessage message = new EmailMessage()
            .setSenderAddress("<sender-email-address>")
            .setToRecipients("<recipient-email-address>")
            .setSubject("test subject")
            .setBodyPlainText("test message")
            .setAttachments(attachment);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();

        System.out.println("Operation Id: " + response.getValue().getId());
        // END: readme-sample-sendEmailWithAttachment
    }

    public void sendEmailWithInlineAttachment() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailWithInlineAttachment
        BinaryData attachmentContent = BinaryData.fromFile(new File("C:/attachment.txt").toPath());
        EmailAttachment attachment = new EmailAttachment(
            "inlineimage.jpg",
            "image/jpeg",
            BinaryData.fromString("test")
        );
        attachment.setContentId("inline_image");
        
        EmailMessage message = new EmailMessage()
            .setSenderAddress("<sender-email-address>")
            .setToRecipients("<recipient-email-address>")
            .setSubject("test subject")
            .setBodyHtml("<h1>test message<img src=\"cid:inline_image\"></h1>")
            .setAttachments(attachment);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();

        System.out.println("Operation Id: " + response.getValue().getId());
        // END: readme-sample-sendEmailWithInlineAttachment
    }
}
