// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import java.util.Arrays;
import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SmsSendOptions;
import com.azure.communication.sms.SmsSendResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Context;

public class ReadmeSamples {
    public SmsClient createSmsClientUsingAAD() {
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
        return smsClientBuilder.buildClient();
    }

    public SmsClient createSmsClientWithConnectionString() {
        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";
        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
            .httpClient(httpClient)
            .buildClient();
        return smsClient;
    }

    public void sendMessageToOneRecipient(SmsClient smsClient) {
        // Send the message to a list of  phone Numbers and check the response for a messages ids
        SmsSendResult response = smsClient.send(
            "<from-phone-number>",
            "<to-phone-number>",
            "Hi");

        System.out.println("MessageId: " + response.getMessageId());
    }

    public void sendMessageToGroupWithOptions(SmsClient smsClient) {
        SmsSendOptions options = new SmsSendOptions();
        options.setDeliveryReportEnabled(true);
        options.setTag("Tag");

        Iterable<SmsSendResult> sendResults = smsClient.sendWithResponse(
            "<from-phone-number>",
            Arrays.asList("<to-phone-number1>", "<to-phone-number2>"),
            "Hi",
            options /* Optional */,
            Context.NONE).getValue();

        for (SmsSendResult result : sendResults) {
            System.out.println("Message Id:" + result.getMessageId());
            System.out.println("Status code:" + result.getHttpStatusCode());
            System.out.println("Successful:" + result.isSuccessful());
        }
    }
}
