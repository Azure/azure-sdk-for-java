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
import com.azure.communication.email.models.EmailAttachmentType;


import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

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

    public void sendEmailToSingleRecipient() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailToSingleRecipient
        EmailAddress emailAddress = new EmailAddress("<recipient-email-address>");

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage("<sender-email-address>", content)
            .setRecipients(emailRecipients);

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

        EmailRecipients emailRecipients = new EmailRecipients(toAddressList)
            .setCc(ccAddressList)
            .setBcc(bccAddressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailMessage emailMessage = new EmailMessage("<sender-email-address>", content)
            .setRecipients(emailRecipients);

        SendEmailResult response = emailClient.send(emailMessage);
        System.out.println("Message Id: " + response.getMessageId());
        // END: readme-sample-sendEmailToMultipleRecipients
    }


    public void sendEmailWithAttachment() {
        EmailClient emailClient = createEmailClientWithConnectionString();

        // BEGIN: readme-sample-sendEmailWithAttachment
        File file = new File("C:/attachment.txt");

        byte[] fileContent = null;
        try {
            fileContent = Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            System.out.println(e);
        }

        String b64file = Base64.getEncoder().encodeToString(fileContent);

        EmailAddress emailAddress = new EmailAddress("<recipient-email-address>");

        ArrayList<EmailAddress> addressList = new ArrayList<>();
        addressList.add(emailAddress);

        EmailRecipients emailRecipients = new EmailRecipients(addressList);

        EmailContent content = new EmailContent("test subject")
            .setPlainText("test message");

        EmailAttachment attachment = new EmailAttachment("attachment.txt", EmailAttachmentType.TXT, b64file);

        ArrayList<EmailAttachment> attachmentList = new ArrayList<>();
        attachmentList.add(attachment);

        EmailMessage emailMessage = new EmailMessage("<sender-email-address>", content)
            .setRecipients(emailRecipients)
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
