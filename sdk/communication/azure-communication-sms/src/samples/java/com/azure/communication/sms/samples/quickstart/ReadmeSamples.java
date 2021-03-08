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
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {
    public SmsClient createSmsClientUsingAzureKeyCredential() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<resource-name>.communication.azure.com";
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential("<access-key>");

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        SmsClient smsClient = new SmsClientBuilder()
            .endpoint(endpoint)
            .credential(azureKeyCredential)
            .httpClient(httpClient)
            .buildClient();

        return smsClient;
    }

    public SmsClient createSmsClientWithConnectionString() {
        // You can find your connection string from your resource in the Azure Portal
        String connectionString = "https://<resource-name>.communication.azure.com/;<access-key>";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
            .httpClient(httpClient)
            .buildClient();

        return smsClient;
    }

    public SmsClient createSmsClientWithAAD() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        SmsClient smsClient = new SmsClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(httpClient)
            .buildClient();

        return smsClient;
    }

    public void sendMessageToOneRecipient() {
        SmsClient smsClient = createSmsClientUsingAzureKeyCredential();

        SmsSendResult sendResult = smsClient.send(
            "<from-phone-number>",
            "<to-phone-number>",
            "Hi");

        System.out.println("Message Id: " + sendResult.getMessageId());
        System.out.println("Recipient Number: " + sendResult.getTo());
        System.out.println("Send Result Successful:" + sendResult.isSuccessful());
    }

    public void sendMessageToGroupWithOptions() {
        SmsClient smsClient = createSmsClientUsingAzureKeyCredential();

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
            System.out.println("Message Id: " + result.getMessageId());
            System.out.println("Recipient Number: " + result.getTo());
            System.out.println("Send Result Successful:" + result.isSuccessful());
        }
    }

    /**
     * Sample code for troubleshooting
     */
    public void sendSMSTroubleshooting() {
        SmsClient smsClient = createSmsClientUsingAzureKeyCredential();
        try {
            SmsSendResult sendResult = smsClient.send(
                "<from-phone-number>",
                "<to-phone-number>",
                "Hi"
            );
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
