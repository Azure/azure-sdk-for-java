// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.models.EventPosition;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import reactor.core.publisher.Mono;

/**
 * Sample program to convert IoT Hub connection string to Event Hubs connection string.
 */
public class IotHubToEventHubConnectionStringSample {

    private static final String HOSTNAME = "hostname";
    private static final String SHARED_ACCESS_KEY_NAME = "sharedaccesskeyname";
    private static final String SHARED_ACCESS_KEY = "sharedaccesskey";
    private static final String HASH_ALGORITHM = "HmacSHA256";
    private static final String IOT_HUB_CONNECTION_STRING = "";

    /**
     * Main method to invoke this demo about how to convert an IoT Hub connection string to Event Hub connection string.
     *
     * @param args Ignored args.
     * @throws Exception If the conversion failed.
     */
    public static void main(String[] args) throws Exception {
        // Extract the key value pairs from IoT Hub connection string to get
        // hostname, shared access key name and shared access key
        Map<String, String> connectionProperties = extractKeyValuePairs(IOT_HUB_CONNECTION_STRING);
        String iotHubName = connectionProperties.get(HOSTNAME).split("\\.")[0];

        // Get the SAS token credential
        TokenCredential tokenCredential = getTokenCredential(connectionProperties);

        // Create an event hub client with above credential and set the CBS auth type to SAS
        // and entity path to "/messages/events"
        EventHubConsumerClient syncClient = new EventHubClientBuilder()
            .credential(connectionProperties.get(HOSTNAME), iotHubName, tokenCredential,
                CbsAuthorizationType.SHARED_ACCESS_SIGNATURE)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .entityPath("/messages/events")
            .buildConsumerClient();

        String redirectHostName = null;
        try {
            // Try to receive an event from the client which will throw link redirect exception
            syncClient
                .receiveFromPartition("0", 1, EventPosition.earliest())
                .forEach(pe -> System.out.println("Received an event));"));
        } catch (Exception ex) {
            // Extract the redirect hostname from the exception
            if (ex instanceof AmqpException) {
                AmqpException amqpException = (AmqpException) ex;
                if (amqpException.getContext() instanceof LinkErrorContext) {
                    LinkErrorContext linkErrorContext = (LinkErrorContext) amqpException.getContext();
                    redirectHostName = linkErrorContext.getRedirectHostName();
                }
            } else {
                throw ex;
            }
        }
        System.out.println("Redirect host is " + redirectHostName);

        // Construct the event hub connection string using the redirect hostname
        String eventHubConnectionString = String
            .format("Endpoint=sb://%s/;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
                redirectHostName, connectionProperties.get(SHARED_ACCESS_KEY_NAME),
                connectionProperties.get(SHARED_ACCESS_KEY), iotHubName);
        System.out.println("Event Hub Connection String = " + eventHubConnectionString);
    }

    private static TokenCredential getTokenCredential(Map<String, String> connectionProperties) {
        return tokenRequestContext -> {
            try {
                String sasToken = generateSasToken(connectionProperties);
                return Mono.just(new AccessToken(sasToken, OffsetDateTime.now().plusDays(2)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Mono.empty();
        };
    }

    private static String generateSasToken(Map<String, String> connectionProperties) throws Exception {
        long expiry = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + 3600;
        String encodedUri = URLEncoder.encode(connectionProperties.get(HOSTNAME), StandardCharsets.UTF_8.toString());
        int ttl = Long.valueOf(expiry).intValue();
        String sig = generateSharedAccessSignature(connectionProperties.get(HOSTNAME),
            connectionProperties.get(SHARED_ACCESS_KEY));

        return String.format("SharedAccessSignature sr=%s&sig=%s&se=%s&skn="
            + "%s", encodedUri, sig, ttl, connectionProperties.get(SHARED_ACCESS_KEY_NAME));
    }

    private static Map<String, String> extractKeyValuePairs(String iotHubConnectionString) {
        String hostname = null;
        String sharedAccessKeyName = null;
        String sharedAccessKey = null;

        Map<String, String> connectionProperties = new HashMap<>();

        for (String part : iotHubConnectionString.trim().split(";")) {
            String[] keyValueSplits = part.split("=", 2);
            if (keyValueSplits.length == 2) {
                if (keyValueSplits[0].toLowerCase().equals(HOSTNAME)) {
                    hostname = keyValueSplits[1].endsWith("/") ? keyValueSplits[1].substring(0,
                        keyValueSplits[1].length() - 1) : keyValueSplits[1];
                    connectionProperties.put(HOSTNAME, hostname);
                }
                if (keyValueSplits[0].toLowerCase().equals(SHARED_ACCESS_KEY_NAME)) {
                    sharedAccessKeyName = keyValueSplits[1];
                    connectionProperties.put(SHARED_ACCESS_KEY_NAME, sharedAccessKeyName);
                }
                if (keyValueSplits[0].toLowerCase().equals(SHARED_ACCESS_KEY)) {
                    sharedAccessKey = keyValueSplits[1];
                    connectionProperties.put(SHARED_ACCESS_KEY, sharedAccessKey);
                }
            }
        }
        if (connectionProperties.size() != 3) {
            throw new IllegalArgumentException("Invalid IoT Hub connection string");
        }
        return connectionProperties;
    }

    private static String generateSharedAccessSignature(String resource, String sharedAccessKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(sharedAccessKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, HASH_ALGORITHM);

        final Mac hmac;
        hmac = Mac.getInstance(HASH_ALGORITHM);
        hmac.init(secretKeySpec);

        final String utf8Encoding = UTF_8.name();
        final OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plus(3600, ChronoUnit.SECONDS);
        final String expiresOnEpochSeconds = Long.toString(expiresOn.toEpochSecond());
        final String audienceUri = URLEncoder.encode(resource, utf8Encoding);
        final String secretToSign = audienceUri + "\n" + expiresOnEpochSeconds;
        final byte[] signatureBytes = hmac.doFinal(secretToSign.getBytes(utf8Encoding));
        String signature = URLEncoder.encode(Base64.getEncoder().encodeToString(signatureBytes), utf8Encoding);
        return signature;
    }
}


