// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import java.util.ArrayList;
import java.util.List;

import com.azure.communication.common.PhoneNumber;
import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.communication.sms.models.SendSmsResponse;
import com.azure.communication.sms.models.SendSmsResponseItem;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;

/**
 * Hello world!
 *
 */
public class ReadmeSamples {
    public static void main(String[] args) {

        // Your can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessKey = "SECRET";

        // Instantiate the http client
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        // Create a new SmsClientBuilder to instantiate an SmsClient
        SmsClientBuilder smsClientBuilder = new SmsClientBuilder();

        // Set the endpoint, access key, and the HttpClient
        smsClientBuilder.endpoint(endpoint)
            .accessKey(accessKey)
            .httpClient(httpClient);

        // Build a new SmsClient
        SmsClient smsClient = smsClientBuilder.buildClient();

        //Send an sms to one phone number
        List<PhoneNumber> to = new ArrayList<PhoneNumber>();
        to.add(new PhoneNumber("<to-phone-number>"));

        // SendSmsOptions is an optional field. It can be used
        // to enable a delivery report to the Azure Event Grid
        SendSmsOptions options = new SendSmsOptions();
        options.setEnableDeliveryReport(true);

        // Send the message to a list of  phone Nunbers and check the response for a messages ids
        PagedIterable<SendSmsResponseItem> response = smsClient.sendMessage(
            new PhoneNumber("<leased-phone-number>"),
            to,
            "your message",null,
            options /* Optional */);

        for (SendSmsResponseItem messageResponseItem
            : response) {
            System.out.println("MessageId: " + messageResponseItem.getMessageId());
        }

        //Send an sms to multiple phone numbers
        List<PhoneNumber> toMultiplePhones = new ArrayList<PhoneNumber>();
        to.add(new PhoneNumber("<to-phone-number1>"));
        to.add(new PhoneNumber("<to-phone-number2>"));

        // Send the message to a list of  phone Numbers and check the response for a messages ids
        PagedIterable<SendSmsResponseItem> responseMultiplePhones = smsClient.sendMessage(
            new PhoneNumber("<leased-phone-number>"),
            toMultiplePhones,
            "your message",null,
            options /* Optional */);

        for (SendSmsResponseItem messageResponseItem
            : responseMultiplePhones) {
            System.out.println("MessageId sent to " + messageResponseItem.getTo() + ": " + messageResponseItem.getMessageId());
        }





    }

    public void createCommunicationIdentityClientWithConnectionString() {
        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        // Your can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";

        SmsClient smsClient = new SmsClientBuilder()
            .connectionString(connectionString)
            .httpClient(httpClient)
            .buildClient();
    }
}
