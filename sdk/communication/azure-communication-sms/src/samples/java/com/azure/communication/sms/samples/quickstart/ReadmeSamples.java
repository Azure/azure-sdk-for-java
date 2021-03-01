// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import java.util.ArrayList;
import java.util.List;
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

        // Your can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        //Enter your azureKeyCredential
        AzureKeyCredential azureKeyCredential = null;

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

        // Your can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";

        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
            .httpClient(httpClient)
            .buildClient();
    }

    public void sendMessageToOneRecipient(SmsClient smsClient) {
        //Send an sms to only one phone number
        String to = "<to-phone-number>";

        // to enable a delivery report to the Azure Event Grid
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        //addionaly you can ad a tag you wish to identify the messages for this tag.
        options.setTag("Tag"); /* Optional */

        // Send the message to a list of  phone Numbers and check the response for a messages ids
        SmsSendResult response = smsClient.send(
            "<leased-phone-number>",
            to,
            "your message",
            options /* Optional */);

        System.out.println("MessageId: " + response.getMessageId());

    }

    public void sendMessageToMultipleRecipients(SmsClient smsClient) {
        //Send an sms to multiple phone numbers
        List<String> toMultiplePhones = new ArrayList<String>();
        toMultiplePhones.add("<to-phone-number1>");
        toMultiplePhones.add("<to-phone-number2>");

        // to enable a delivery report to the Azure Event Grid
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        //addionaly you can ad a tag you wish to identify the messages for this tag.
        options.setTag("Tag"); /* Optional */

        // Send the message to a list of  phone Numbers and check the response for a messages ids
        Iterable<SmsSendResult> responseMultiplePhones = smsClient.send(
            "<leased-phone-number>",
            toMultiplePhones,
            "your message",
            options /* Optional */,
            null);

        for (SmsSendResult messageResponseItem
            : responseMultiplePhones) {
            System.out.println("MessageId sent to " + messageResponseItem.getTo() + ": " + messageResponseItem.getMessageId());
        }
    }


}
