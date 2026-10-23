// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.TestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Focused identity-only conformance test for a namespace with local authentication disabled.
 */
@Tag("integration")
public class ServiceBusAadOnlyConformanceIT extends TestProxyTestBase {
    @Test
    @LiveOnly
    void identityCanAdministerSendAndReceive() throws InterruptedException {
        final String namespace = TestUtils.getFullyQualifiedDomainName(false);
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final ServiceBusAdministrationClient administration
            = new ServiceBusAdministrationClientBuilder().credential(namespace, credential).buildClient();
        final String queueName = testResourceNamer.randomName("aadonly", 10);
        final long roleDeadline = System.nanoTime() + Duration.ofMinutes(8).toNanos();
        while (true) {
            try {
                administration.createQueue(queueName);
                break;
            } catch (HttpResponseException e) {
                final int status = e.getResponse() == null ? 0 : e.getResponse().getStatusCode();
                if ((status != 401 && status != 403) || System.nanoTime() >= roleDeadline) {
                    throw e;
                }
                Thread.sleep(Duration.ofSeconds(15).toMillis());
            }
        }
        try {
            final ServiceBusClientBuilder builder = new ServiceBusClientBuilder().credential(namespace, credential);
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient()) {
                while (true) {
                    try {
                        sender.sendMessage(new ServiceBusMessage("identity-only"));
                        break;
                    } catch (ServiceBusException e) {
                        if (!ServiceBusFailureReason.UNAUTHORIZED.equals(e.getReason())
                            || System.nanoTime() >= roleDeadline) {
                            throw e;
                        }
                        // A new namespace's data-plane role can lag its management-plane assignment.
                        Thread.sleep(Duration.ofSeconds(15).toMillis());
                    }
                }
                final ServiceBusReceivedMessage received
                    = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(received, "No message arrived through the identity-authenticated connection.");
                assertEquals("identity-only", received.getBody().toString());
                receiver.complete(received);
            }
        } finally {
            administration.deleteQueue(queueName);
        }
    }
}
