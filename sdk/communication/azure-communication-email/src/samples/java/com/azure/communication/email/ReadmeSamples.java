// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.email;

import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailContent;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailRecipients;
import com.azure.communication.email.models.SendEmailResult;
import com.azure.communication.email.models.SendStatusResult;
import com.azure.communication.email.models.EmailAttachment;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
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

    public void sendEmailSimple() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailSimple
        SendEmailResult response = emailClient.send(
            "<sender-email-address>",
            "recipient-email-address",
            "test subject",
            "<h1>test message</h1>"
        );

        System.out.println("Message Id: " + response.getMessageId());
        // END: readme-sample-sendEmailSimple
    }

    public void sendEmailToSingleRecipient() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailToSingleRecipient
        EmailAddress emailAddress = new EmailAddress("<recipient-email-address>");

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients()
            .setTo(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage("<sender-email-address>", content, emailRecipients);

        SendEmailResult response = emailClient.send(emailMessage);
        System.out.println("Message Id: " + response.getMessageId());
        // END: readme-sample-sendEmailToSingleRecipient
    }

    public void sendEmailToMultipleRecipients() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailToMultipleRecipients
        EmailAddress emailAddress = new EmailAddress("<recipient-email-address>");
        EmailAddress emailAddress2 = new EmailAddress("<recipient-2-email-address>");

        ArrayList<EmailAddress> toAddressList = new ArrayList<>();
        toAddressList.add(emailAddress);
        toAddressList.add(emailAddress2);

        ArrayList<EmailAddress> ccAddressList = new ArrayList<>();
        ccAddressList.add(emailAddress);

        ArrayList<EmailAddress> bccAddressList = new ArrayList<>();
        bccAddressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients()
            .setTo(toAddressList)
            .setCc(ccAddressList)
            .setBcc(bccAddressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage("<sender-email-address>", content, emailRecipients);

        SendEmailResult response = emailClient.send(emailMessage);
        System.out.println("Message Id: " + response.getMessageId());
        // END: readme-sample-sendEmailToMultipleRecipients
    }


    public void sendEmailWithAttachment() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailWithAttachment
        EmailAddress emailAddress = new EmailAddress("<recipient-email-address>");

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients()
            .setTo(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        BinaryData attachmentContent = BinaryData.fromFile(new File("C:/attachment.txt").toPath());
        EmailAttachment attachment = new EmailAttachment("attachment.txt", "TXT", attachmentContent);

        ArrayList<EmailAttachment> attachmentList = new ArrayList<>();
        attachmentList.add(attachment);

        EmailMessage emailMessage = new EmailMessage("<sender-email-address>", content, emailRecipients)
            .setAttachments(attachmentList);

        SendEmailResult response = emailClient.send(emailMessage);
        System.out.println("Message Id: " + response.getMessageId());
        // END: readme-sample-sendEmailWithAttachment
    }


    public void getMessageStatus() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-getMessageStatus
        SendStatusResult response = emailClient.getSendStatus("<sent-message-id>");
        System.out.println("Status: " + response.getStatus());
        // END: readme-sample-getMessageStatus
    }
}
