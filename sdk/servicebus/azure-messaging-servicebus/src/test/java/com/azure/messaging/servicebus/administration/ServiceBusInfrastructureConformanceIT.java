// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.TestUtils;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Data-plane half of namespace configuration conformance. The runner must verify the named
 * configuration through Azure management readback before selecting a profile.
 */
@Tag("integration")
public class ServiceBusInfrastructureConformanceIT extends TestProxyTestBase {
    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "GeoDr")
    void geoDrAliasRoutesAfterExternallyVerifiedFailover() {
        completeGeoDrAfterPromotion();
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "GeoReplication")
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_PHASE", matches = "before")
    void asyncGeoReplicationSendsBeforePromotion() {
        createGeoQueue();
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "GeoReplication")
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_PHASE", matches = "after")
    void asyncGeoReplicationRetainsMessageAfterPromotion() {
        verifyReplicatedMessage();
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "GeoReplicationSync")
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_PHASE", matches = "before")
    void syncGeoReplicationSendsBeforePromotion() {
        createGeoQueue();
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "GeoReplicationSync")
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_PHASE", matches = "after")
    void syncGeoReplicationRetainsMessageAfterPromotion() {
        verifyReplicatedMessage();
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "PremiumPartitioned")
    void namespacePartitioningRoutesMessages() {
        roundTripThroughConfiguredNamespace("namespace-partitioning", true);
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "ZoneRedundant")
    void zoneRedundantNamespaceRoutesMessages() {
        roundTripThroughConfiguredNamespace("zone-redundancy");
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "NetworkIsolated")
    void privateEndpointRoutesMessages() {
        roundTripThroughConfiguredNamespace("private-endpoint");
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "NetworkFirewall")
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_NETWORK_PROBE", matches = "allowed")
    void firewallAllowedNetworkRoutesMessages() {
        roundTripThroughConfiguredNamespace("firewall", false, firewallCredential());
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "NetworkFirewall")
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_NETWORK_PROBE", matches = "denied")
    void firewallRejectsSenderOutsideAllowedNetwork() {
        final TokenCredential credential = firewallCredential();
        assertNotNull(credential.getToken(new TokenRequestContext().addScopes("https://servicebus.azure.net/.default"))
            .block(Duration.ofSeconds(20)), "The blocked runner did not obtain a valid Service Bus token.");
        final ServiceBusClientBuilder builder
            = new ServiceBusClientBuilder().credential(TestUtils.getFullyQualifiedDomainName(false), credential)
                .retryOptions(new AmqpRetryOptions().setMaxRetries(0).setTryTimeout(Duration.ofSeconds(10)));
        final boolean[] sent = { false };
        final ServiceBusException rejection = assertThrows(ServiceBusException.class, () -> {
            try (ServiceBusSenderClient sender = builder.sender().queueName("queue-0").buildClient()) {
                sender.sendMessage(new ServiceBusMessage("firewall-denied"));
                sent[0] = true;
            }
        });
        assertFalse(sent[0], "The excluded runner successfully sent a message.");
        assertEquals(ServiceBusFailureReason.UNAUTHORIZED, rejection.getReason(),
            "The failure was not the broker's documented firewall rejection.");
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "NetworkIsolated")
    void disabledPublicAccessRoutesMessagesPrivately() {
        roundTripThroughConfiguredNamespace("private-only");
    }

    @Test
    @LiveOnly
    @EnabledIfEnvironmentVariable(named = "AZURE_SERVICEBUS_CONFORMANCE_CONFIG", matches = "Cmk")
    void customerManagedKeyNamespaceRoutesMessages() {
        roundTripThroughConfiguredNamespace("customer-managed-key");
    }

    private void createGeoQueue() {
        final String queueName = geoQueueName();
        final String namespace = TestUtils.getFullyQualifiedDomainName(false);
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final ServiceBusAdministrationClient administration
            = new ServiceBusAdministrationClientBuilder().credential(namespace, credential).buildClient();
        administration.createQueue(queueName);
        boolean prepared = false;
        try {
            try (ServiceBusSenderClient sender = new ServiceBusClientBuilder().credential(namespace, credential)
                .sender()
                .queueName(queueName)
                .buildClient()) {
                sender.sendMessage(new ServiceBusMessage(geoMessageBody()));
            }
            prepared = true;
        } finally {
            if (!prepared) {
                administration.deleteQueue(queueName);
            }
        }
    }

    private void completeGeoDrAfterPromotion() {
        final String queueName = "geodr-metadata-probe";
        final String namespace = TestUtils.getFullyQualifiedDomainName(false);
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final ServiceBusAdministrationClient administration
            = new ServiceBusAdministrationClientBuilder().credential(namespace, credential).buildClient();
        assertTrue(administration.getQueueExists(queueName), "Geo-DR did not replicate the queue metadata.");
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder().credential(namespace, credential);
        try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
            ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient()) {
            sender.sendMessage(new ServiceBusMessage("geo-dr-alias"));
            final ServiceBusReceivedMessage received
                = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
            assertNotNull(received, "Geo-DR alias did not route to the promoted namespace.");
            assertEquals("geo-dr-alias", received.getBody().toString());
            receiver.complete(received);
        }
    }

    private void verifyReplicatedMessage() {
        final String queueName = geoQueueName();
        final String namespace = TestUtils.getFullyQualifiedDomainName(false);
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final ServiceBusAdministrationClient administration
            = new ServiceBusAdministrationClientBuilder().credential(namespace, credential).buildClient();
        try {
            assertTrue(administration.getQueueExists(queueName), "Geo-Replication did not retain the queue.");
            try (ServiceBusReceiverClient receiver = new ServiceBusClientBuilder().credential(namespace, credential)
                .receiver()
                .queueName(queueName)
                .buildClient()) {
                final ServiceBusReceivedMessage received
                    = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(received, "Message did not survive promotion of the replicated namespace.");
                assertEquals(geoMessageBody(), received.getBody().toString());
                receiver.complete(received);
            }
        } finally {
            if (administration.getQueueExists(queueName)) {
                administration.deleteQueue(queueName);
            }
        }
    }

    private String geoQueueName() {
        final String name = System.getenv("SERVICEBUS_CONFORMANCE_GEO_QUEUE");
        assertNotNull(name, "The same SERVICEBUS_CONFORMANCE_GEO_QUEUE must be supplied to both phases.");
        assertTrue(name.matches("cfm-[a-z0-9-]{1,40}"), "Use a dedicated cfm- queue for the geo scenario.");
        return name;
    }

    private String geoMessageBody() {
        final String body = System.getenv("SERVICEBUS_CONFORMANCE_GEO_MESSAGE");
        assertNotNull(body, "The same SERVICEBUS_CONFORMANCE_GEO_MESSAGE must be supplied to both phases.");
        assertTrue(!body.isEmpty(), "Geo message identity must not be empty.");
        return body;
    }

    private void roundTripThroughConfiguredNamespace(String body) {
        roundTripThroughConfiguredNamespace(body, false);
    }

    private void roundTripThroughConfiguredNamespace(String body, boolean partitioned) {
        roundTripThroughConfiguredNamespace(body, partitioned, new DefaultAzureCredentialBuilder().build());
    }

    private TokenCredential firewallCredential() {
        final String clientId = System.getenv("AZURE_CLIENT_ID");
        assertNotNull(clientId, "Both firewall runners must use the same authorized user-assigned identity.");
        assertFalse(clientId.isEmpty(), "AZURE_CLIENT_ID must identify the authorized user-assigned identity.");
        return new ManagedIdentityCredentialBuilder().clientId(clientId).build();
    }

    private void roundTripThroughConfiguredNamespace(String body, boolean partitioned, TokenCredential credential) {
        final String namespace = TestUtils.getFullyQualifiedDomainName(false);
        final String queueName = testResourceNamer.randomName("infra", 10);
        final ServiceBusAdministrationClient administration
            = new ServiceBusAdministrationClientBuilder().credential(namespace, credential).buildClient();
        administration.createQueue(queueName, new CreateQueueOptions().setPartitioningEnabled(partitioned));
        try {
            if (partitioned) {
                assertTrue(administration.getQueue(queueName).isPartitioningEnabled(),
                    "Premium namespace did not create a partitioned queue.");
            }
            final ServiceBusClientBuilder builder = new ServiceBusClientBuilder().credential(namespace, credential);
            try (ServiceBusSenderClient sender = builder.sender().queueName(queueName).buildClient();
                ServiceBusReceiverClient receiver = builder.receiver().queueName(queueName).buildClient()) {
                final ServiceBusMessage message = new ServiceBusMessage(body);
                if (partitioned) {
                    message.setPartitionKey("conformance-partition");
                }
                sender.sendMessage(message);
                final ServiceBusReceivedMessage received
                    = receiver.receiveMessages(1, Duration.ofSeconds(30)).stream().findFirst().orElse(null);
                assertNotNull(received, "Configured namespace did not deliver the message.");
                assertEquals(body, received.getBody().toString());
                if (partitioned) {
                    assertEquals("conformance-partition", received.getPartitionKey());
                }
                receiver.complete(received);
            }
        } finally {
            administration.deleteQueue(queueName);
        }
    }
}
