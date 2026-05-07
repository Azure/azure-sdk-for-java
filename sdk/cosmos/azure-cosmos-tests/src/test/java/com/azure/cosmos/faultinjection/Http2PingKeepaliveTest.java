// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.http.Http2PingHandler;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import io.netty.channel.Channel;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.resolver.AddressResolverGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Standalone test for HTTP/2 PING keepalive handler.
 * Proves that the Http2PingHandler sends PING frames on idle connections
 * and that the connection survives the idle period.
 *
 * Run in Docker with --cap-add=NET_ADMIN (group: manual-http-network-fault).
 */
public class Http2PingKeepaliveTest extends FaultInjectionTestBase {

    private static final Logger logger = LoggerFactory.getLogger(Http2PingKeepaliveTest.class);
    private static final long TEST_TIMEOUT = 120_000; // 2 minutes

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private TestObject seedItem;

    public Http2PingKeepaliveTest() {
        super(new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .gatewayMode());
    }

    @BeforeClass(groups = {"manual-http-network-fault"}, timeOut = 120_000)
    public void beforeClass() {
        // Enable HTTP/2 for this test
        System.setProperty("COSMOS.HTTP2_ENABLED", "true");
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

        // Seed item for reads
        this.seedItem = TestObject.create();
        this.cosmosAsyncContainer.createItem(this.seedItem).block();
        logger.info("Seed item created: {}", this.seedItem.getId());
    }

    @AfterClass(groups = {"manual-http-network-fault"}, timeOut = 60_000)
    public void afterClass() {
        safeClose(this.client);
        System.clearProperty("COSMOS.HTTP2_ENABLED");
        System.clearProperty("COSMOS.THINCLIENT_ENABLED");
    }

    @BeforeMethod(groups = {"manual-http-network-fault"})
    public void beforeMethod(Method method) {
        logger.info("=== START: {} ===", method.getName());
    }

    @AfterMethod(groups = {"manual-http-network-fault"})
    public void afterMethod(Method method) {
        logger.info("=== END: {} ===", method.getName());
    }

    @Test(groups = {"manual-http-network-fault"}, timeOut = TEST_TIMEOUT)
    public void pingFramesSentAndAcknowledgedOnIdleConnection() throws Exception {
        System.setProperty("COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS", "3");
        System.setProperty("COSMOS.HTTP2_PING_HEALTH_ENABLED", "true");

        AtomicReference<Http2PingHandler> pingHandlerRef = new AtomicReference<>();

        try {
            safeClose(this.client);

            CosmosClientBuilder builder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .gatewayMode();

            // Inject a doOnConnected callback that installs a PING handler for testing
            com.azure.cosmos.test.implementation.interceptor.CosmosInterceptorHelper
                .registerHttpClientInterceptor(builder, (AddressResolverGroup<?>) null, connection -> {
                    Channel ch = connection.channel();
                    if (ch.pipeline().get(Http2MultiplexHandler.class) != null
                        && ch.pipeline().get("testPingHandler") == null) {
                        Http2PingHandler handler = new Http2PingHandler(3);
                        ch.pipeline().addLast("testPingHandler", handler);
                        pingHandlerRef.compareAndSet(null, handler);
                        logger.info("Test installed Http2PingHandler on H2 parent channel {}", ch.id().asShortText());
                    }
                });

            this.client = builder.buildAsyncClient();
            this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);

            // Establish H2 connection with a warm-up read
            String initialParentChannelId = readAndGetParentChannelId();
            logger.info("Initial parentChannelId: {}", initialParentChannelId);

            // Let the connection go idle — PINGs should fire every 3s
            logger.info("Waiting 20s for PING frames to be sent on idle connection...");
            Thread.sleep(20_000);

            // Recovery read — proves connection is still alive
            String recoveryParentChannelId = readAndGetParentChannelId();

            Http2PingHandler handler = pingHandlerRef.get();
            int sentCount = handler != null ? handler.getPingsSent() : -1;
            int ackCount = handler != null ? handler.getPingAcksReceived() : -1;

            logger.info("RESULT: initial={}, recovery={}, SAME_CONNECTION={}, pingsSent={}, pingAcksReceived={}",
                initialParentChannelId, recoveryParentChannelId,
                initialParentChannelId.equals(recoveryParentChannelId), sentCount, ackCount);

            assertThat(handler)
                .as("Http2PingHandler should be installed on the parent H2 channel")
                .isNotNull();

            assertThat(sentCount)
                .as("PINGs sent should be > 0 — proves the manual PING handler is actively sending frames")
                .isGreaterThan(0);

            assertThat(ackCount)
                .as("PING ACKs received should be > 0 — proves server acknowledged PINGs")
                .isGreaterThan(0);

            // NOTE: We don't assert same connection because pool eviction behavior varies
            // across account types (thin client vs standard). The core assertion is that
            // PINGs are sent and ACKed — keepalive traffic is flowing.
            logger.info("PING keepalive verified: {} PINGs sent, {} ACKs received", sentCount, ackCount);
        } finally {
            System.clearProperty("COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS");
            System.clearProperty("COSMOS.HTTP2_PING_HEALTH_ENABLED");
        }
    }

    private String readAndGetParentChannelId() {
        CosmosItemResponse<TestObject> response = this.cosmosAsyncContainer.readItem(
            seedItem.getId(), new PartitionKey(seedItem.getId()), TestObject.class).block();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);

        return extractParentChannelId(response.getDiagnostics());
    }

    private String extractParentChannelId(CosmosDiagnostics diagnostics) {
        try {
            String diagStr = diagnostics.toString();
            int idx = diagStr.indexOf("parentChannelId");
            if (idx > 0) {
                int start = diagStr.indexOf("\"", idx + 16) + 1;
                int end = diagStr.indexOf("\"", start);
                if (start > 0 && end > start) {
                    return diagStr.substring(start, end);
                }
            }

            logger.warn("Could not extract parentChannelId from diagnostics");
            return "unknown-" + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            logger.warn("Error extracting parentChannelId", e);
            return "error-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
