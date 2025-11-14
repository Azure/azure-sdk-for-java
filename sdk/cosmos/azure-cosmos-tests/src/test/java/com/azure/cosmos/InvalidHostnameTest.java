/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.testng.SkipException;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import javax.net.ssl.SSLHandshakeException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class InvalidHostnameTest extends TestSuiteBase {
    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public InvalidHostnameTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, timeOut = TIMEOUT)
    public void gatewayConnectionFailsWhenHostnameIsInvalid() throws Exception {
        gatewayConnectionFailsWhenHostnameIsInvalidCore(null);
        gatewayConnectionFailsWhenHostnameIsInvalidCore(false);
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, timeOut = TIMEOUT)
    public void gatewayConnectionFailsWhenHostnameIsInvalidEvenWhenHostnameValidationIsDisabled() throws Exception {
        gatewayConnectionFailsWhenHostnameIsInvalidCore(true);
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, timeOut = TIMEOUT)
    public void directConnectionSucceedsWhenHostnameIsInvalidAndHostnameValidationIsDisabled() throws Exception {
        directConnectionTestCore(true);
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, timeOut = TIMEOUT)
    public void directConnectionFailsWhenHostnameIsInvalidAndHostnameValidationIsNotSet() throws Exception {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(1024);
        buf.writeInt(42);
        // no release() on purpose
        System.gc();
        Thread.sleep(2000); // give GC & leak detector time

        directConnectionFailsWhenHostnameIsInvalidCore(null);
    }

    @Test(groups = { "fast", "fi-multi-master", "multi-region" }, timeOut = TIMEOUT)
    public void directConnectionFailsWhenHostnameIsInvalidAndHostnameValidationIsEnabled() throws Exception {
        directConnectionFailsWhenHostnameIsInvalidCore(false);
    }

    private void directConnectionFailsWhenHostnameIsInvalidCore(Boolean disableHostnameValidation) throws Exception {
        try {
            directConnectionTestCore(disableHostnameValidation);
            fail("The test should have failed with invalid hostname when hostname "
                + "validation is enabled or not set.");
        } catch (InternalServerErrorException cosmosException) {
            assertThat(cosmosException.getStatusCode()).isEqualTo(500);
            assertThat(cosmosException.getSubStatusCode())
                .isEqualTo(HttpConstants.SubStatusCodes.INVALID_RESULT);
            assertThat(cosmosException).hasCauseInstanceOf(RuntimeException.class);
            RuntimeException runtimeException = (RuntimeException)cosmosException.getCause();
            assertThat(runtimeException).hasCauseInstanceOf(GoneException.class);
            GoneException goneException = (GoneException)runtimeException.getCause();
            assertThat(goneException).hasCauseInstanceOf(SSLHandshakeException.class);
            logger.info("Expected exception was thrown", cosmosException);
        }
    }

    private void directConnectionTestCore(Boolean disableHostnameValidation) throws Exception {
        CosmosDatabase createdDatabase = null;
        CosmosClient client = null;
        CosmosClientBuilder builder = getClientBuilder();

        if (builder.getEndpoint().contains("localhost")) {
            throw new SkipException("This test is irrelevant for emulator");
        }

        if (builder.getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("This test is only relevant for direct mode");
        }

        try {
            if (disableHostnameValidation != null) {
                System.setProperty("COSMOS.HOSTNAME_VALIDATION_DISABLED", disableHostnameValidation.toString());
            } else {
                System.clearProperty("COSMOS.HOSTNAME_VALIDATION_DISABLED");
            }
            Configs.resetIsHostnameValidationDisabledForTests();

            client = builder.buildClient();

            TransportClient originalTransportClient = ReflectionUtils.getTransportClient(client);

            ReflectionUtils.setTransportClient(
                client,
                new HostnameInvalidationTransportClient(originalTransportClient));

            String dbName = CosmosDatabaseForTest.generateId();
            createdDatabase = createSyncDatabase(client, dbName);
            createdDatabase.createContainer(
                "TestContainer",
                "/id",
                ThroughputProperties.createManualThroughput(400));
            CosmosContainer createdContainer = client.getDatabase(dbName).getContainer("TestContainer");
            ObjectNode newObject = Utils.getSimpleObjectMapper().createObjectNode();
            newObject.put("id", UUID.randomUUID().toString());
            createdContainer.upsertItem(newObject);
        }
        finally {
            if (createdDatabase != null) {
                safeDeleteSyncDatabase(createdDatabase);
            }

            if (client != null) {
                safeCloseSyncClient(client);
            }

            System.clearProperty("COSMOS.HOSTNAME_VALIDATION_DISABLED");
            Configs.resetIsHostnameValidationDisabledForTests();
        }
    }

    private void gatewayConnectionFailsWhenHostnameIsInvalidCore(Boolean disableHostnameValidation) throws Exception {
        CosmosDatabase createdDatabase = null;
        CosmosClient client = null;
        CosmosClientBuilder builder = getClientBuilder();

        if (builder.getEndpoint().contains("localhost")) {
            throw new SkipException("This test is irrelevant for emulator");
        }

        try {
            if (disableHostnameValidation != null) {
                System.setProperty("COSMOS.HOSTNAME_VALIDATION_DISABLED", disableHostnameValidation.toString());
            } else {
                System.clearProperty("COSMOS.HOSTNAME_VALIDATION_DISABLED");
            }
            Configs.resetIsHostnameValidationDisabledForTests();

            URI uri = URI.create(builder.getEndpoint());
            InetAddress address = InetAddress.getByName(uri.getHost());
            URI uriWithInvalidHostname = new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                address.getHostAddress(), // Use the DNS-resolved IP-address as new hostname - this is invalid form TLS cert perspective
                uri.getPort(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment()
            );
            builder.endpoint(uriWithInvalidHostname.toString());
            client = builder.buildClient();
            String dbName = CosmosDatabaseForTest.generateId();
            createdDatabase = createSyncDatabase(client, dbName);
            fail("The attempt to connect to the Gateway endpoint to create a database "
                + "should have failed due to invalid hostname.");
        } catch (RuntimeException e) {
            assertThat(e).hasCauseInstanceOf(CosmosException.class);
            CosmosException cosmosException = (CosmosException)e.getCause();
            assertThat(cosmosException.getStatusCode()).isEqualTo(503);
            assertThat(cosmosException.getSubStatusCode())
                .isEqualTo(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);
            assertThat(cosmosException).hasCauseInstanceOf(SSLHandshakeException.class);
            logger.info("Expected exception was thrown", cosmosException);
        }
        finally {
            if (createdDatabase != null) {
                safeDeleteSyncDatabase(createdDatabase);
            }

            if (client != null) {
                safeCloseSyncClient(client);
            }

            System.clearProperty("COSMOS.HOSTNAME_VALIDATION_DISABLED");
            Configs.resetIsHostnameValidationDisabledForTests();
        }
    }

    private static class HostnameInvalidationTransportClient extends TransportClient {
        private final TransportClient inner;

        public HostnameInvalidationTransportClient(TransportClient transportClient) {
            this.inner = transportClient;
        }

        @Override
        public void close() throws Exception {
            this.inner.close();
        }

        @Override
        public Mono<StoreResponse> invokeStoreAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
            URI uri = physicalAddress.getURI();
            InetAddress address = null;
            try {
                address = InetAddress.getByName(uri.getHost());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            String uriWithInvalidHostname;
            try {
                uriWithInvalidHostname = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    address.getHostAddress(), // Use the DNS-resolved IP-address as new hostname - this is invalid form TLS cert perspective
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
                ).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            Uri ipBasedAddress = Uri.create(uriWithInvalidHostname);

            logger.info("Changed physical address '{}' into '{}'.", physicalAddress, ipBasedAddress);

            return this
                .inner
                .invokeStoreAsync(ipBasedAddress, request)
                .onErrorMap(t -> new RuntimeException(t));
        }

        @Override
        public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {
            this.inner.configureFaultInjectorProvider(injectorProvider);
        }

        @Override
        public GlobalEndpointManager getGlobalEndpointManager() {
            return this.inner.getGlobalEndpointManager();
        }

        @Override
        public ProactiveOpenConnectionsProcessor getProactiveOpenConnectionsProcessor() {
            return this.inner.getProactiveOpenConnectionsProcessor();
        }

        @Override
        public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
            this.inner.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
        }

        @Override
        public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
            this.inner.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
        }
    }
}
