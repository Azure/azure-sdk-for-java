// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.sms.samples.quickstart;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.azure.communication.common.PhoneNumber;
import com.azure.communication.common.CommunicationClientCredential;
import com.azure.communication.sms.SmsClient;
import com.azure.communication.sms.SmsClientBuilder;
import com.azure.communication.sms.models.SendSmsOptions;
import com.azure.communication.sms.models.SendSmsResponse;
import com.azure.core.http.HttpClient;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {

        // Retrieve the Azure Communication SMS Service endpoint for use with the application. 
        // The endpoint string is stored in an environment variable on the machine running the 
        // application called COMMUNICATION_SERVICES_ENDPOINT.
        String endpoint = System.getenv("COMMUNICATION_SERVICES_ENDPOINT");

        // Retrieve the access key string for use with the application. The access key
        // string is stored in an environment variable on the machine running the application 
        // called COMMUNICATION_SERVICES_ACCESS_KEY.
        String accessKey = System.getenv("COMMUNICATION_SERVICES_ACCESS_KEY");

        // Instantiate the http client
        HttpClient httpClient = null; // Your HttpClient

        CommunicationClientCredential credential = null;
        try {
            credential = new CommunicationClientCredential(accessKey);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        } catch (InvalidKeyException e) {
            System.out.println(e.getMessage());
        }
        
        // Create a new SmsClientBuilder to instantiate an SmsClient
        SmsClientBuilder smsClientBuilder = new SmsClientBuilder();

        // Set the endpoint, access key, and the HttpClient
        smsClientBuilder.endpoint(endpoint)
            .credential(credential)
            .httpClient(httpClient);

        // Build a new SmsClient
        SmsClient smsClient = smsClientBuilder.buildClient();

        // Currently Sms services only supports one phone number
        List<PhoneNumber> to = new ArrayList<PhoneNumber>();
        to.add(new PhoneNumber("<to-phone-number>"));

        // SendSmsOptions is an optional field. It can be used
        // to enable a delivery report to the Azure Event Grid
        SendSmsOptions options = new SendSmsOptions();
        options.setEnableDeliveryReport(true);

        // Send the message and check the response for a message id
        SendSmsResponse response = smsClient.sendMessage(
            new PhoneNumber("<leased-phone-number>"), 
            to, 
            "your message",
            options /* Optional */);

        System.out.println("MessageId: " + response.getMessageId());
    }
}
