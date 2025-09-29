package com.azure.cosmos.implementation;// ...existing imports...
// (imports stay the same)

import com.azure.cosmos.GatewayTestUtils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Fail.fail;

public class RegionScopedSessionContainerConcurrencyTest {

    private static final URI EAST_US = createUrl("https://concurrency-east-us.documents.azure.com");
    private static final URI EAST_US2 = createUrl("https://concurrency-east-us-2.documents.azure.com");
    private static final URI CENTRAL_US = createUrl("https://concurrency-central-us.documents.azure.com");
    private static final String REGION_EAST_US = "eastus";
    private static final String REGION_EAST_US2 = "eastus2";
    private static final String REGION_CENTRAL_US = "centralus";

    private static final String COLLECTION_FULL_NAME = "dbs/db1/colls1/collConcurrency";

    // Use known-good collection rid from other unit tests (was uf4PAK6T-Cw=)
    private static final String COLLECTION_RID = "uf4PAK6T-Cw=";

    // Flag: if true we include OWNER_ID header; if false rely solely on OWNER_FULL_NAME
    private static final boolean INCLUDE_OWNER_ID = true;

    private static final int NUM_PK_RANGES = 5000;
    private static final String[] PK_RANGE_IDS;
    static {
        PK_RANGE_IDS = new String[NUM_PK_RANGES];
        for (int i = 0; i < NUM_PK_RANGES; i++) {
            PK_RANGE_IDS[i] = "range_" + i;
        }
    }

    private static final int REGION_ID_PRIMARY = 2;
    private static final int REGION_ID_SECONDARY = 7;
    private static final double NON_DEFAULT_REGION_PROBABILITY = 0.25;

    @Test(groups = "unit")
    public void concurrentSetAndResolveTokens() throws Exception {
        final int WRITER_THREADS = 2;
        final int READER_THREADS = 12;
        final int ITERATIONS_PER_WRITER = 5000;
        final Duration TEST_TIMEOUT = Duration.ofSeconds(120); // shorter now that logic fixed

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer regionScopedSessionContainer =
            new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        UnmodifiableList<RegionalRoutingContext> endpoints = new UnmodifiableList<>(
            ImmutableList.of(
                new RegionalRoutingContext(EAST_US),
                new RegionalRoutingContext(EAST_US2),
                new RegionalRoutingContext(CENTRAL_US)));

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(EAST_US), Mockito.any()))
            .thenReturn(REGION_EAST_US);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(EAST_US2), Mockito.any()))
            .thenReturn(REGION_EAST_US2);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(CENTRAL_US), Mockito.any()))
            .thenReturn(REGION_CENTRAL_US);

        ExecutorService exec = Executors.newFixedThreadPool(WRITER_THREADS + READER_THREADS);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        Phaser startBarrier = new Phaser(1 + WRITER_THREADS + READER_THREADS);
        CountDownLatch writersDone = new CountDownLatch(WRITER_THREADS);

        AtomicLong[] maxObservedGlobal = new AtomicLong[NUM_PK_RANGES];
        AtomicLong[] maxObservedRegionPrimary = new AtomicLong[NUM_PK_RANGES];
        AtomicLong[] maxObservedRegionSecondary = new AtomicLong[NUM_PK_RANGES];
        boolean[] firstWritten = new boolean[NUM_PK_RANGES];
        OrderedApplier[] orderedAppliers = new OrderedApplier[NUM_PK_RANGES];
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk"));

        // NEW: per-range sequence generators (only for successful resolves) and ordered appliers
        AtomicLong[] sequenceGenerators = new AtomicLong[NUM_PK_RANGES];
        for (int i = 0; i < NUM_PK_RANGES; i++) {
            maxObservedGlobal[i] = new AtomicLong(-1);
            maxObservedRegionPrimary[i] = new AtomicLong(-1);
            maxObservedRegionSecondary[i] = new AtomicLong(-1);
            sequenceGenerators[i] = new AtomicLong(0);
            orderedAppliers[i] = new OrderedApplier(
                i,
                maxObservedGlobal,
                maxObservedRegionPrimary,
                maxObservedRegionSecondary
            );
        }

        // Writers
        for (int w = 0; w < WRITER_THREADS; w++) {
            final int writerIndex = w;
            exec.submit(wrap(errors, () -> {
                startBarrier.arriveAndAwaitAdvance();
                long version = 1;
                long baseOffset = writerIndex * 10_000_000L;
                for (int iter = 1; iter <= ITERATIONS_PER_WRITER; iter++) {
                    int pkIdx = (iter - 1) % NUM_PK_RANGES;
                    String pkRangeId = PK_RANGE_IDS[pkIdx];
                    URI chosen = chooseRegionWithSkew();

                    RxDocumentServiceRequest writeRequest = RxDocumentServiceRequest.create(
                        mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                        COLLECTION_FULL_NAME + "/docs", Utils.getUTF8Bytes("payload"), new HashMap<>());
                    writeRequest.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(chosen);

                    PartitionKeyRange pkRange = new PartitionKeyRange();
                    pkRange.setId(pkRangeId);
                    GatewayTestUtils.setParent(pkRange, ImmutableList.of());
                    writeRequest.requestContext.resolvedPartitionKeyRange = pkRange;
                    writeRequest.setPartitionKeyInternal(ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(pkRangeId)));
                    writeRequest.setPartitionKeyDefinition(partitionKeyDefinition);

                    long globalLsn = baseOffset + iter;
                    long regionPrimaryLsn = globalLsn;
                    long regionSecondaryLsn = globalLsn - (writerIndex % 2);

                    String vector = buildVectorToken(version, globalLsn, regionPrimaryLsn, regionSecondaryLsn);
                    String headerValue = pkRangeId + ":" + vector;

                    Map<String, String> respHeaders = new HashMap<>();
                    respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, headerValue);
                    respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, COLLECTION_FULL_NAME);
                    if (INCLUDE_OWNER_ID) {
                        respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, COLLECTION_RID);
                    }

                    regionScopedSessionContainer.setSessionToken(writeRequest, respHeaders);
                    System.out.println(System.currentTimeMillis() + "=>" + "Writer " + writerIndex + " set token for " + pkRangeId + " => " + headerValue);

                    if (!firstWritten[pkIdx]) {
                        firstWritten[pkIdx] = true;
                        // Minimal debug; switch to logger if desired
                        // System.out.println("First token stored for " + pkRangeId + " => " + headerValue);
                    }

                    if ((iter & 0x3F) == 0) {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(1, 3));
                    }
                }
                writersDone.countDown();
            }));
        }

        // Readers
        for (int r = 0; r < READER_THREADS; r++) {
            exec.submit(wrap(errors, () -> {
                startBarrier.arriveAndAwaitAdvance();
                while (true) {
                    for (int pkIdx = 0; pkIdx < NUM_PK_RANGES; pkIdx++) {
                        String pkRangeId = PK_RANGE_IDS[pkIdx];
                        URI chosen = chooseRegionWithSkew();

                        RxDocumentServiceRequest readRequest = RxDocumentServiceRequest.create(
                            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document,
                            COLLECTION_FULL_NAME + "/docs/doc1", new HashMap<>());
                        readRequest.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(chosen);

                        PartitionKeyRange pkRange = new PartitionKeyRange();
                        pkRange.setId(pkRangeId);
                        GatewayTestUtils.setParent(pkRange, ImmutableList.of());
                        readRequest.requestContext.resolvedPartitionKeyRange = pkRange;
                        readRequest.setPartitionKeyInternal(ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(pkRangeId)));
                        readRequest.setPartitionKeyDefinition(partitionKeyDefinition);

                        long sequence = sequenceGenerators[pkIdx].getAndIncrement();
                        ISessionToken sessionToken = regionScopedSessionContainer.resolvePartitionLocalSessionToken(readRequest, pkRangeId);

                        if (sessionToken != null) {
                            orderedAppliers[pkIdx].submit(sequence, sessionToken);
                        }
                    }

                    if (writersDone.getCount() == 0) {
                        // Single final sweep
                        for (int pkIdx = 0; pkIdx < NUM_PK_RANGES; pkIdx++) {
                            String pkRangeId = PK_RANGE_IDS[pkIdx];
                            RxDocumentServiceRequest finalRead = RxDocumentServiceRequest.create(
                                mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document,
                                COLLECTION_FULL_NAME + "/docs/doc1", new HashMap<>());
                            finalRead.requestContext.regionalRoutingContextToRoute =
                                new RegionalRoutingContext(chooseRegionWithSkew());
                            ISessionToken finalToken =
                                regionScopedSessionContainer.resolvePartitionLocalSessionToken(finalRead, pkRangeId);
                            if (finalToken != null) {
                                maxObservedGlobal[pkIdx].getAndAccumulate(finalToken.getLSN(), Math::max);
                            }
                        }
                        break;
                    }
                    Thread.sleep(1);
                }
            }));
        }

        startBarrier.arriveAndDeregister();

        boolean finished = writersDone.await(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        exec.shutdown();
        exec.awaitTermination(TEST_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

        if (!finished) {
            fail("Writers did not finish within timeout " + TEST_TIMEOUT);
        }
        if (!errors.isEmpty()) {
            List<Throwable> snapshot = new ArrayList<>(errors);
            snapshot.forEach(Throwable::printStackTrace);
            fail("Encountered " + snapshot.size() + " errors. First: " + snapshot.get(0));
        }

        // FINAL DRAIN: ensure all per-range buffered records are applied
        for (int i = 0; i < NUM_PK_RANGES; i++) {
            orderedAppliers[i].finalDrain();
        }

        // Diagnostic: how many ranges ever written
        int writtenCount = 0;
        List<String> notWritten = new ArrayList<>();
        for (int i = 0; i < NUM_PK_RANGES; i++) {
            if (firstWritten[i]) {
                writtenCount++;
            } else {
                notWritten.add(PK_RANGE_IDS[i]);
            }
        }
        if (!notWritten.isEmpty()) {
            fail("No writer recorded first token for ranges: " + notWritten);
        }

        // Check every pk range observed at least once
        List<String> missingObserved = new ArrayList<>();
        for (int i = 0; i < NUM_PK_RANGES; i++) {
            if (maxObservedGlobal[i].get() < 0) {
                missingObserved.add(PK_RANGE_IDS[i]);
            }
        }
        if (!missingObserved.isEmpty()) {
            fail("No token observed for ranges (despite writer writes): " + missingObserved);
        }
    }

    private static URI chooseRegionWithSkew() {
        double d = ThreadLocalRandom.current().nextDouble();
        if (d < NON_DEFAULT_REGION_PROBABILITY) {
            return ThreadLocalRandom.current().nextBoolean() ? EAST_US2 : CENTRAL_US;
        }
        return EAST_US;
    }

    private static String buildVectorToken(long version,
                                           long globalLsn,
                                           long primaryLocal,
                                           long secondaryLocal) {
        return version + "#" + globalLsn + "#" + REGION_ID_PRIMARY + "=" + primaryLocal + "#" + REGION_ID_SECONDARY + "=" + secondaryLocal;
    }

    private static ParsedVector parseVector(String vector) {
        String[] parts = vector.split("#");
        Map<Integer, Long> map = new HashMap<>();
        for (int i = 2; i < parts.length; i++) {
            int eq = parts[i].indexOf('=');
            if (eq < 0) continue;
            try {
                int regionId = Integer.parseInt(parts[i].substring(0, eq));
                long lsn = Long.parseLong(parts[i].substring(eq + 1));
                map.merge(regionId, lsn, Math::max);
            } catch (NumberFormatException ignore) {
                // ignore malformed
            }
        }
        return new ParsedVector(map);
    }

    private static URI createUrl(String raw) {
        try { return new URI(raw); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static <T> Callable<T> wrap(ConcurrentLinkedQueue<Throwable> errors, ThrowingRunnable r) {
        return () -> {
            try { r.run(); } catch (Throwable t) { errors.add(t); }
            return null;
        };
    }

    @FunctionalInterface
    private interface ThrowingRunnable { void run() throws Exception; }

    private static class ParsedVector {
        final Map<Integer, Long> regionIdToLsn;
        ParsedVector(Map<Integer, Long> regionIdToLsn) {
            this.regionIdToLsn = Objects.requireNonNull(regionIdToLsn);
        }
    }

    // NEW: Ordered application support
    private static final class ObservedRecord {
        final long seq;
        final long globalLsn;
        final long primaryLsn;
        final long secondaryLsn;
        ObservedRecord(long seq, long globalLsn, long primaryLsn, long secondaryLsn) {
            this.seq = seq;
            this.globalLsn = globalLsn;
            this.primaryLsn = primaryLsn;
            this.secondaryLsn = secondaryLsn;
        }
    }

    private static final class OrderedApplier {
        private final int pkIdx;
        private final AtomicLong nextToApply = new AtomicLong(0);
        private final ConcurrentHashMap<Long, ObservedRecord> buffer = new ConcurrentHashMap<>();

        // References to shared arrays for assertions
        private final AtomicLong[] maxObservedGlobal;
        private final AtomicLong[] maxObservedRegionPrimary;
        private final AtomicLong[] maxObservedRegionSecondary;

        OrderedApplier(int pkIdx,
                       AtomicLong[] maxObservedGlobal,
                       AtomicLong[] maxObservedRegionPrimary,
                       AtomicLong[] maxObservedRegionSecondary) {
            this.pkIdx = pkIdx;
            this.maxObservedGlobal = maxObservedGlobal;
            this.maxObservedRegionPrimary = maxObservedRegionPrimary;
            this.maxObservedRegionSecondary = maxObservedRegionSecondary;
        }

        void submit(long seq, ISessionToken token) {
            ParsedVector pv = parseVector(token.convertToString());
            long glsn = token.getLSN();
            long primary = pv.regionIdToLsn.getOrDefault(REGION_ID_PRIMARY, -1L);
            long secondary = pv.regionIdToLsn.getOrDefault(REGION_ID_SECONDARY, -1L);

            buffer.put(seq, new ObservedRecord(seq, glsn, primary, secondary));
            drainInOrder();
        }

        void finalDrain() {
            drainInOrder();
        }

        private void drainInOrder() {
            // Try to advance while consecutive sequences are present
            while (true) {
                long expected = nextToApply.get();
                ObservedRecord rec = buffer.remove(expected);
                if (rec == null) {
                    return;
                }
                apply(rec);
                // Advance expected
                nextToApply.compareAndSet(expected, expected + 1);
            }
        }

        private void apply(ObservedRecord rec) {
            // Monotonic assertions now enforced strictly in resolve sequence order
            long prevG = maxObservedGlobal[pkIdx].getAndAccumulate(rec.globalLsn, Math::max);
            if (prevG > rec.globalLsn) {
                fail("Global LSN decreased (seq=" + rec.seq + ") for " + PK_RANGE_IDS[pkIdx] +
                    ": prev=" + prevG + " new=" + rec.globalLsn);
            }
            if (rec.primaryLsn >= 0) {
                long prevP = maxObservedRegionPrimary[pkIdx].getAndAccumulate(rec.primaryLsn, Math::max);
                if (prevP > rec.primaryLsn) {
                    fail("Primary local LSN decreased (seq=" + rec.seq + ") for " + PK_RANGE_IDS[pkIdx] +
                        ": prev=" + prevP + " new=" + rec.primaryLsn);
                }
            }
            if (rec.secondaryLsn >= 0) {
                long prevS = maxObservedRegionSecondary[pkIdx].getAndAccumulate(rec.secondaryLsn, Math::max);
                if (prevS > rec.secondaryLsn) {
                    fail("Secondary local LSN decreased (seq=" + rec.seq + ") for " + PK_RANGE_IDS[pkIdx] +
                        ": prev=" + prevS + " new=" + rec.secondaryLsn);
                }
            }
        }
    }
}
