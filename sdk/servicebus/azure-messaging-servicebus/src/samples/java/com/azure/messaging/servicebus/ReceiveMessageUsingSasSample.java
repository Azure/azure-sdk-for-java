// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

/**
 * Sample showing how to generate Shared Access Signatures for specific resource, an Azure Service Bus Queue or Topic
 * and receive messages.
 */
public class ReceiveMessageUsingSasSample {
    /**
     * Sample showing how to generate Shared Access Signatures for specific resource, an Azure Service Bus Queue or
     * Topic and receive messages.
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {

        // The connection string value can be obtained by:
        // 1. sharedAccesskey, sharedAccesskeyName: They can specific to a resource. Go on portal under your
        //    service bus namespace, navigate to specific queue or topic and  click on "shared  access policies". Here
        //    you can create appropriate resource specific policies.
        // 2. Now generate SAS token using: https://docs.microsoft.com/rest/api/eventhub/generate-sas-token
        // 3. More about SAS Tokens explained: https://docs.microsoft.com/azure/service-bus-messaging/service-bus-sas

        String sharedAccesskeyName = "<Resource Specific Key name from Portal>";
        String sharedAccesskey = "<Resource Specific Key from Portal>";
        String uri = "{fully-qualified-namespace}/<queue-name>";
        String queueName = "<queue-name>";
        String sharedAccessSignature = getSASToken(uri, sharedAccesskeyName, sharedAccesskey);

        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessSignature=" + sharedAccessSignature;

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "sb://{your-namespace}.servicebus.windows.net"
        // "<queue-name>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        // Each message's lock is renewed up to 1 minute.
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .maxAutoLockRenewDuration(Duration.ofMinutes(1))
            .queueName(queueName)
            .buildClient();

        // Try to receive a set of messages from Service Bus 10 times. A batch of messages are returned when 5 messages
        // are received, or the operation timeout has elapsed, whichever occurs first.
        for (int i = 0; i < 10; i++) {

            receiver.receiveMessages(5).stream().forEach(message -> {
                // Process message. The message lock is renewed for up to 1 minute.
                System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(), message.getBody());

                // Messages from the sync receiver MUST be settled explicitly.
                receiver.complete(message);
            });
        }

        // Close the receiver.
        receiver.close();
    }

    /**
     * This method generate SAS Token for given resource uri.
     */
    private static String getSASToken(String resourceUri, String keyName, String key) {
        long epoch = System.currentTimeMillis() / 1000L;
        // NOTE : This is for demo one, Change this for your application specific expiry duration.
        int week = 60 * 60 * 24 * 7;
        String expiry = Long.toString(epoch + week);
        String sasToken = null;
        try {
            String stringToSign = URLEncoder.encode(resourceUri, StandardCharsets.UTF_8.toString()) + "\n" + expiry;
            String signature = getHMAC256(key, stringToSign);
            sasToken = "SharedAccessSignature sr=" + URLEncoder.encode(resourceUri, StandardCharsets.UTF_8.toString())
                + "&sig=" + URLEncoder.encode(signature, StandardCharsets.UTF_8.toString()) + "&se=" + expiry + "&skn="
                + keyName;
        } catch (Exception e) {
            System.err.println(" Could not url encode " + e.getMessage());
        }
        return sasToken;
    }

    private static String getHMAC256(String key, String input) {
        Mac sha256HMAC;
        String hash = null;
        try {
            sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256HMAC.init(secretKey);
            Base64.Encoder encoder = Base64.getEncoder();

            hash = new String(encoder.encode(sha256HMAC.doFinal(input.getBytes(StandardCharsets.UTF_8))));

        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException e) {
            e.printStackTrace();
        }

        return hash;
    }
}
