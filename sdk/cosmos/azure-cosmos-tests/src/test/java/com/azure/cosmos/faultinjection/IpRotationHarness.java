// IP Rotation Test Harness
// Tests max-lifetime DNS rotation with FilterableDnsResolverGroup
// Validates: block IP → traffic shifts, unblock IP → traffic rebalances

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.test.faultinjection.FilterableDnsResolverGroup;
import com.azure.cosmos.test.implementation.interceptor.CosmosInterceptorHelper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http2.Http2MultiplexHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IpRotationHarness {

    public static void main(String[] args) throws Exception {
        String endpoint = System.getenv("ACCOUNT_HOST");
        String key = System.getenv("ACCOUNT_KEY");
        String dbId = System.getenv("DB_ID");
        String containerId = System.getenv("CONTAINER_ID");
        String preferredRegion = System.getenv("PREFERRED_REGION");
        int maxLifeSec = Integer.parseInt(System.getenv().getOrDefault("MAX_LIFE_SEC", "60"));
        int runtimeMinutes = Integer.parseInt(System.getenv().getOrDefault("RUNTIME_MIN", "15"));

        System.setProperty("COSMOS.HTTP2_ENABLED", "true");
        System.setProperty("COSMOS.HTTP_CONNECTION_MAX_LIFETIME_ENABLED", "true");
        System.setProperty("COSMOS.HTTP_CONNECTION_MAX_LIFETIME_IN_SECONDS", String.valueOf(maxLifeSec));
        System.setProperty("COSMOS.HTTP2_PING_HEALTH_ENABLED", "true");
        System.setProperty("COSMOS.HTTP2_PING_INTERVAL_IN_SECONDS", "10");

        // Resolve all IPs for the regional endpoint
        URI uri = new URI(endpoint);
        String globalHost = uri.getHost();
        String regionalHost = globalHost.replace(".documents.azure.com",
            "-" + preferredRegion.toLowerCase().replace(" ", "") + ".documents.azure.com");
        InetAddress[] allIps = InetAddress.getAllByName(regionalHost);
        System.out.printf("Regional endpoint: %s%n", regionalHost);
        System.out.printf("Resolved IPs: %d%n", allIps.length);
        for (InetAddress ip : allIps) {
            System.out.printf("  %s%n", ip.getHostAddress());
        }

        // Even with 1 IP at startup, DNS round-robin may return a different IP later.
        // MaxLife rotation forces new DNS lookups, so we proceed regardless.

        // Create FilterableDnsResolverGroup with control reference
        FilterableDnsResolverGroup resolver = new FilterableDnsResolverGroup();

        // Track which IPs connections go to
        ConcurrentHashMap<String, AtomicInteger> ipRequestCounts = new ConcurrentHashMap<>();

        // Build client with resolver injected via interceptor
        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint(endpoint)
            .key(key)
            .preferredRegions(java.util.Arrays.asList(preferredRegion))
            .gatewayMode()
            .contentResponseOnWriteEnabled(true);

        CosmosInterceptorHelper.registerHttpClientInterceptor(builder, resolver, connection -> {
            Channel ch = connection.channel();
            if (ch.pipeline().get(Http2MultiplexHandler.class) != null) {
                InetSocketAddress remote = (InetSocketAddress) ch.remoteAddress();
                if (remote != null) {
                    String ip = remote.getAddress().getHostAddress();
                    ipRequestCounts.computeIfAbsent(ip, k -> new AtomicInteger()).incrementAndGet();
                    System.out.printf("[%s] New H2 connection to IP: %s (channel: %s)%n",
                        Instant.now(), ip, ch.id().asShortText());
                }
            }
        });

        CosmosAsyncClient client = builder.buildAsyncClient();
        CosmosAsyncContainer container = client.getDatabase(dbId).getContainer(containerId);

        // Seed a test item
        TestObject seedItem = new TestObject();
        seedItem.setId("rotation-test-" + System.currentTimeMillis());
        seedItem.setMypk(seedItem.getId());
        container.createItem(seedItem).block();
        System.out.printf("Seeded item: %s%n", seedItem.getId());

        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(Duration.ofMinutes(runtimeMinutes));
        InetAddress ip1 = allIps[0];

        // ================================================================
        // Phase 1: Normal workload (no blocking) — ~1/3 of runtime
        // ================================================================
        Instant phase1End = startTime.plus(Duration.ofMinutes(runtimeMinutes / 3));
        System.out.printf("%n=== PHASE 1: Normal workload (all IPs available) ===%n");
        System.out.printf("Duration: until %s%n", phase1End);

        while (Instant.now().isBefore(phase1End)) {
            try {
                container.readItem(seedItem.getId(),
                    new com.azure.cosmos.models.PartitionKey(seedItem.getId()),
                    TestObject.class).block();
            } catch (Exception e) {
                System.out.printf("[%s] Read failed: %s%n", Instant.now(), e.getMessage());
            }
            Thread.sleep(100); // ~10 RPS
        }

        printIpDistribution("PHASE 1 END", ipRequestCounts);

        // ================================================================
        // Phase 2: Block IP1 — traffic should shift to IP2
        // ================================================================
        System.out.printf("%n=== PHASE 2: Blocking IP %s ===%n", ip1.getHostAddress());
        resolver.blockIp(ip1);
        ipRequestCounts.clear();

        Instant phase2End = Instant.now().plus(Duration.ofMinutes(runtimeMinutes / 3));
        // Wait for maxLife to expire and force new connections
        System.out.printf("Waiting for maxLife (%ds) + jitter to expire...%n", maxLifeSec);

        while (Instant.now().isBefore(phase2End)) {
            try {
                container.readItem(seedItem.getId(),
                    new com.azure.cosmos.models.PartitionKey(seedItem.getId()),
                    TestObject.class).block();
            } catch (Exception e) {
                System.out.printf("[%s] Read failed: %s%n", Instant.now(), e.getMessage());
            }
            Thread.sleep(100);
        }

        printIpDistribution("PHASE 2 END (IP1 blocked)", ipRequestCounts);

        // ================================================================
        // Phase 3: Unblock IP1 — traffic should rebalance
        // ================================================================
        System.out.printf("%n=== PHASE 3: Unblocking IP %s ===%n", ip1.getHostAddress());
        resolver.unblockAll();
        ipRequestCounts.clear();

        while (Instant.now().isBefore(endTime)) {
            try {
                container.readItem(seedItem.getId(),
                    new com.azure.cosmos.models.PartitionKey(seedItem.getId()),
                    TestObject.class).block();
            } catch (Exception e) {
                System.out.printf("[%s] Read failed: %s%n", Instant.now(), e.getMessage());
            }
            Thread.sleep(100);
        }

        printIpDistribution("PHASE 3 END (all IPs unblocked)", ipRequestCounts);

        // Cleanup
        client.close();
        System.out.printf("%nHarness complete at %s%n", Instant.now());
    }

    private static void printIpDistribution(String label, ConcurrentHashMap<String, AtomicInteger> counts) {
        System.out.printf("%n--- %s ---%n", label);
        int total = counts.values().stream().mapToInt(AtomicInteger::get).sum();
        counts.forEach((ip, count) -> {
            double pct = total > 0 ? (count.get() * 100.0 / total) : 0;
            System.out.printf("  IP %-16s : %5d connections (%.1f%%)%n", ip, count.get(), pct);
        });
        System.out.printf("  Total connections: %d%n", total);
    }
}
