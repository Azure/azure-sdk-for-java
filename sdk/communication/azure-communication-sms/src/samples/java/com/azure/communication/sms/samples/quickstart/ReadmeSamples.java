// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import java.util.ArrayList;
import java.util.Arrays;
import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.SmsSendResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

/**
 * Hello world!
 */
public class ReadmeSamples {


    public void createSmsClient() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        //Enter your azureKeyCredential
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("SECRET");
        // Instantiate the http client
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();
        // Create a new SmsClientBuilder to instantiate an SmsClient
        SmsClientBuilder smsClientBuilder = new SmsClientBuilder();
        // Set the endpoint, access key, and the HttpClient
        smsClientBuilder.endpoint(endpoint)
            .credential(azureKeyCredential)
            .httpClient(httpClient);
        // Build a new SmsClient
        SmsClient smsClient = smsClientBuilder.buildClient();
    }

    public void createSmsClientWithConnectionString() {
        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";
        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
            .httpClient(httpClient)
            .buildClient();
    }

    public void sendMessageToOneRecipient(SmsClient smsClient) {
        //Send an sms to only one phone number
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("Tag"); /* Optional */
        // Send the message to a list of  phone Numbers and check the response for a messages ids
        SmsSendResult response = smsClient.send(
            "<from-phone-number>",
            "<to-phone-number>",
            "your message",
            options /* Optional */);
        System.out.println("MessageId: " + response.getMessageId());
    }

    public void sendMessageToMultipleRecipients(SmsClient smsClient) {
        //Send an sms to multiple phone numbers
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("Tag"); /* Optional */
        // Send the message to a list of  phone Numbers and check the response for a messages ids
        Iterable<SmsSendResult> responseMultiplePhones = smsClient.send(
            "<from-phone-number>",
            new ArrayList<String>(Arrays.asList("<to-phone-number1>", "<to-phone-number2>")),
            "your message",
            options /* Optional */,
            null);
        for (SmsSendResult messageResponseItem : responseMultiplePhones) {
            System.out.println("MessageId sent to " + messageResponseItem.getTo() + ": " + messageResponseItem.getMessageId());
        }
    }


}
