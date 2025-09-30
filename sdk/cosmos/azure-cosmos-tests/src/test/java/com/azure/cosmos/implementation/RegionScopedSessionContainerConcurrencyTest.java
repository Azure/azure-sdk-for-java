package com.azure.cosmos.implementation;

import com.azure.cosmos.GatewayTestUtils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Fail.fail;

public class RegionScopedSessionContainerConcurrencyTest {

    private static final Logger logger = LoggerFactory.getLogger(RegionScopedSessionContainerConcurrencyTest.class);

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

    private static final int REGION_ID_SATELLITE_ONE = 2;
    private static final int REGION_ID_SATELLITE_TWO = 7;
    private static final double REGRESSION_EMISSION_PROBABILITY = 0.05;
    private static final long REGRESSION_MAX_DELTA = 10;

    @DataProvider
    public Object[][] concurrentSetAndResolveTokensDataArgs() {
        return new Object[][] {
            // For small expected insertion counts, the bloom filter is more likely to have false positives
            // which increases the likelihood of region-specific LSNs to not be present in the region-scoped session container
            // This increases the likelihood of the global session token to be resolved
            {
                "Low expected insertion count => more false positives => more global session token resolutions",
                "1000",  // bloomFilterExpectedInsertionCount
                "0.1",    // bloomFilterExpectedFfpRate
                new URI[] { EAST_US, EAST_US2, CENTRAL_US },
                0.5,
                5000,
                5000
            },
            {
                "Low expected insertion count => more false positives => more global session token resolutions",
                "1000",  // bloomFilterExpectedInsertionCount
                "0.1",    // bloomFilterExpectedFfpRate
                new URI[] { EAST_US2, EAST_US, CENTRAL_US },
                0.5,
                5000,
                5000
            },
            {
                "Low expected insertion count => more false positives => more global session token resolutions",
                "1000",  // bloomFilterExpectedInsertionCount
                "0.1",    // bloomFilterExpectedFfpRate
                new URI[] { EAST_US2, CENTRAL_US, EAST_US },
                0.5,
                5000,
                5000
            },
            {
                "High expected insertion count => less false positives => more region-specific session token resolutions",
                "1000000",  // bloomFilterExpectedInsertionCount
                "0.001",    // bloomFilterExpectedFfpRate
                new URI[] { EAST_US2, CENTRAL_US, EAST_US },
                0.5,
                5000,
                5000
            },
            {
                "High expected insertion count => less false positives => more region-specific session token resolutions",
                "1000000",  // bloomFilterExpectedInsertionCount
                "0.001",    // bloomFilterExpectedFfpRate
                new URI[] { EAST_US, EAST_US2, CENTRAL_US },
                0.5,
                5000,
                5000
            },
        };
    }

    /**
     * Stress / correctness test for {@link RegionScopedSessionContainer} exercising concurrent
     * {@code setSessionToken} (writers) and {@code resolvePartitionLocalSessionToken} (readers)
     * across a large fan-out of physical partition key range ids and multiple region orderings.
     * <p>
     * The test is parameterized via a TestNG {@code @DataProvider}; each invocation ("profile")
     * may vary:
     * <ul>
     *   <li>Bloom filter sizing (expected insertion count & false positive probability)</li>
     *   <li>Endpoint ordering (first element treated as the "primary" for probabilistic routing)</li>
     *   <li>Probability of routing to a non-primary region</li>
     *   <li>Number of writer iterations (synthetic writes)</li>
     *   <li>Number of simulated physical partition key ranges</li>
     * </ul>
     *
     * <h3>Key Behaviors Simulated</h3>
     * <ul>
     *   <li>Writers emit synthetic vector session tokens for each partition range in round-robin fashion.</li>
     *   <li>Each token includes a global LSN and two region-local LSN components (satellite regions),
     *       with a small probability of <em>emitting a regressed (lower) value</em> to mimic out-of-order
     *       or stale arrivals (the container must still maintain monotonic visibility on resolution).</li>
     *   <li>Readers continuously resolve local partition session tokens from randomly (skew) selected regions,
     *       buffering observations per-partition and enforcing in-order application via a per-range
     *       sequence + gap-buffer (OrderedApplier) to avoid false regression assertions due to
     *       thread interleaving.</li>
     *   <li>Bloom filter parameters influence whether region-scoped session tokens or global tokens
     *       are resolved (higher false positive rate → more fallbacks / global resolutions).</li>
     * </ul>
     *
     * <h3>Invariants Asserted Per Physical Partition (pkRangeId)</h3>
     * <ul>
     *   <li>Observed (resolved) global LSN never decreases.</li>
     *   <li>Observed satellite region local LSNs (Region IDs {@code REGION_ID_SATELLITE_ONE} and
     *       {@code REGION_ID_SATELLITE_TWO}) never decrease.</li>
     *   <li>At least one token was written (first write recorded).</li>
     *   <li>At least one token was eventually observed by a reader.</li>
     * </ul>
     *
     * <h3>Ordering Strategy</h3>
     * Resolved tokens are not asserted immediately. Instead each successful resolve gets a strictly
     * increasing per-partition sequence number; records are stored in a concurrent buffer and applied
     * only when all prior sequences have been applied, ensuring logical (resolve) order and eliminating
     * spurious monotonicity failures caused by racing reader threads.
     *
     * <h3>Regression Injection</h3>
     * A small configurable probability (see constants {@code REGRESSION_EMISSION_PROBABILITY} and
     * {@code REGRESSION_MAX_DELTA}) causes writers to emit artificially "regressed" (older) global
     * or local LSN values. The container is expected to merge via max semantics so that externally
     * resolved tokens still exhibit monotonic progression; any decrease detected after ordered
     * application constitutes a real bug in session token capture/merge.
     *
     * <h3>Failure Modes</h3>
     * The test fails fast with descriptive messages if:
     * <ul>
     *   <li>Any per-partition monotonic LSN invariant is violated.</li>
     *   <li>Writers do not finish within the allotted timeout.</li>
     *   <li>No token is ever written or observed for a given partition range.</li>
     *   <li>Unhandled exceptions occur in writer or reader tasks (first one surfaced).</li>
     * </ul>
     *
     * <h3>Resource & Runtime Notes</h3>
     * <ul>
     *   <li>High CPU thread counts (split between writers and readers) can make this test
     *       expensive; adjust {@code iterationsPerWriter} or {@code numPkRanges} for a lighter profile.</li>
     *   <li>Bloom filter system properties are set before each profile run and cleared in {@code finally}.</li>
     *   <li>Logging includes profile parameters and counts of emitted regressions to aid diagnosis.</li>
     * </ul>
     *
     * <h3>Thread Safety & Concurrency Constructs</h3>
     * <ul>
     *   <li>{@link Phaser} acts as a one-time start gate (all worker threads begin simultaneously).</li>
     *   <li>{@link CountDownLatch} tracks completion of all writers.</li>
     *   <li>{@link java.util.concurrent.atomic.AtomicLong} arrays hold per-partition max LSN state.</li>
     *   <li>Ordered application uses a gap-buffer + CAS on a {@code nextToApply} counter.</li>
     * </ul>
     *
     * <h3>Parameters (from DataProvider)</h3>
     * @param profileName human-readable name describing this parameter set (appears in logs / failure messages)
     * @param bloomFilterExpectedInsertionCount expected element count used to size the partition/region Bloom filter (affects false positive rate)
     * @param bloomFilterExpectedFfpRate target (approximate) Bloom filter false positive rate (string form for direct system property injection)
     * @param orderedReadEndpoints array of region endpoints; index 0 is treated as the primary for skewed routing probability calculations
     * @param nonPrimaryRegionProbability probability (0.0–1.0) that a request (read or write) is routed to a non-primary endpoint
     * @param iterationsPerWriter number of synthetic write iterations per writer thread (each iteration targets one partition range)
     * @param numPkRanges number of simulated physical partition key range ids (fan-out); higher values increase concurrency stress
     *
     * @throws Exception on unexpected execution failures or if assertions / invariants fail
     *
     * @implNote Sets and clears system properties
     *           {@code COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT} and
     *           {@code COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE} per invocation.
     *           Emitted regressions are counted but not asserted for minimum quantity (can be added if needed).
     *
     * @see RegionScopedSessionContainer
     * @see com.azure.cosmos.implementation.ISessionToken
     */
    @Test(groups = "unit", dataProvider = "concurrentSetAndResolveTokensDataArgs")
    public void concurrentSetAndResolveTokens(String profileName,
                                              String bloomFilterExpectedInsertionCount,
                                              String bloomFilterExpectedFfpRate,
                                              URI[] orderedReadEndpoints,
                                              double nonPrimaryRegionProbability,
                                              int iterationsPerWriter,
                                              int numPkRanges) throws Exception {

        logger.info("Starting profile=[{}] bloomInsertionCount=[{}] bloomFpp=[{}] endpoints=[{}] nonPrimaryProb=[{}] iterationsPerWriter=[{}] numPkRanges=[{}]",
            profileName,
            bloomFilterExpectedInsertionCount,
            bloomFilterExpectedFfpRate,
            Arrays.toString(orderedReadEndpoints),
            nonPrimaryRegionProbability,
            iterationsPerWriter,
            numPkRanges);

        System.setProperty("COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT", bloomFilterExpectedInsertionCount);
        System.setProperty("COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE", bloomFilterExpectedFfpRate);

        final Duration TEST_TIMEOUT = Duration.ofSeconds(120);

        GlobalEndpointManager globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        RegionScopedSessionContainer regionScopedSessionContainer =
            new RegionScopedSessionContainer("127.0.0.1", false, globalEndpointManagerMock);

        // Build endpoints per ordering
        ImmutableList.Builder<RegionalRoutingContext> endpointBuilder = ImmutableList.builder();
        for (URI u : orderedReadEndpoints) {
            endpointBuilder.add(new RegionalRoutingContext(u));
        }
        UnmodifiableList<RegionalRoutingContext> endpoints =
            new UnmodifiableList<>(endpointBuilder.build());

        Mockito.when(globalEndpointManagerMock.getReadEndpoints()).thenReturn(endpoints);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(EAST_US), Mockito.any()))
            .thenReturn(REGION_EAST_US);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(EAST_US2), Mockito.any()))
            .thenReturn(REGION_EAST_US2);
        Mockito.when(globalEndpointManagerMock.getRegionName(Mockito.eq(CENTRAL_US), Mockito.any()))
            .thenReturn(REGION_CENTRAL_US);
        Mockito.when(globalEndpointManagerMock.canUseMultipleWriteLocations(Mockito.any())).thenReturn(true);

        int totalCores = Configs.getCPUCnt();
        final int WRITER_THREADS = totalCores / 2;
        final int READER_THREADS = totalCores / 2;

        ExecutorService exec = Executors.newFixedThreadPool(WRITER_THREADS + READER_THREADS);
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        Phaser startBarrier = new Phaser(1 + WRITER_THREADS + READER_THREADS);
        CountDownLatch writersDone = new CountDownLatch(WRITER_THREADS);

        AtomicLong[] maxObservedGlobal = new AtomicLong[NUM_PK_RANGES];
        AtomicLong[] maxObservedLocalLsnSatelliteOne = new AtomicLong[NUM_PK_RANGES];
        AtomicLong[] maxObservedLocalLsnSatelliteTwo = new AtomicLong[NUM_PK_RANGES];
        boolean[] firstWritten = new boolean[NUM_PK_RANGES];
        OrderedApplier[] orderedAppliers = new OrderedApplier[NUM_PK_RANGES];
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition().setPaths(ImmutableList.of("/mypk"));

        // NEW: per-range sequence generators (only for successful resolves) and ordered appliers
        AtomicLong[] sequenceGenerators = new AtomicLong[NUM_PK_RANGES];

        AtomicInteger globalLsnRegressionsCount = new AtomicInteger();
        AtomicInteger satelliteOneLocalLsnRegressionsCount = new AtomicInteger();
        AtomicInteger satelliteTwoLocalLsnRegressionsCount = new AtomicInteger();

        try {

            // Starting state of lsn (global and regional) is -1 (no token)
            // Sequence generators help enforce in-order application of observed tokens
            for (int i = 0; i < NUM_PK_RANGES; i++) {
                maxObservedGlobal[i] = new AtomicLong(-1);
                maxObservedLocalLsnSatelliteOne[i] = new AtomicLong(-1);
                maxObservedLocalLsnSatelliteTwo[i] = new AtomicLong(-1);
                sequenceGenerators[i] = new AtomicLong(0);
                orderedAppliers[i] = new OrderedApplier(
                    i,
                    maxObservedGlobal,
                    maxObservedLocalLsnSatelliteOne,
                    maxObservedLocalLsnSatelliteTwo
                );
            }

            // Each writer writes to all ranges in round-robin fashion for x iterations
            // Each write uses a random region (skewed towards default region)
            // Each write increments global LSN by 1 and region-specific LSNs by either 0 or 1
            // Each reader randomly reads all ranges in a loop until writers done
            // Each read uses a random region (skewed towards default region)
            // Each read resolves the token and records observed LSNs (global and region-specific)
            // Finally, after writers done, readers do one more sweep of all ranges to resolve any
            // tokens that may not have been observed, yet
            // Finally we check that for each range the observed LSNs never decreased (globally and regionally)
            for (int w = 0; w < WRITER_THREADS; w++) {
                final int writerIndex = w;
                exec.submit(wrap(errors, () -> {
                    startBarrier.arriveAndAwaitAdvance();
                    long version = 1;
                    long baseOffset = writerIndex * 10_000_000L;
                    for (int iter = 1; iter <= iterationsPerWriter; iter++) {
                        int pkIdx = (iter - 1) % NUM_PK_RANGES;
                        String pkRangeId = PK_RANGE_IDS[pkIdx];

                        RxDocumentServiceRequest writeRequest = RxDocumentServiceRequest.create(
                            mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                            COLLECTION_FULL_NAME + "/docs", Utils.getUTF8Bytes("payload"), new HashMap<>());
                        writeRequest.requestContext.regionalRoutingContextToRoute = chooseRegionWithSkew(endpoints, nonPrimaryRegionProbability);

                        PartitionKeyRange pkRange = new PartitionKeyRange();
                        pkRange.setId(pkRangeId);
                        GatewayTestUtils.setParent(pkRange, ImmutableList.of());
                        writeRequest.requestContext.resolvedPartitionKeyRange = pkRange;

                        // Consistency of mapping crucial PK should always map to same PK Range ID.
                        writeRequest.setPartitionKeyInternal(ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(pkRangeId)));
                        writeRequest.setPartitionKeyDefinition(partitionKeyDefinition);

                        long globalLsn = baseOffset + iter;
                        long satelliteOneLocalLsn = globalLsn;
                        long satelliteTwoLocalLsn = globalLsn - (writerIndex % 2);

                        long possiblyRegressedGlobalLsn = maybeRegress(globalLsn, REGRESSION_EMISSION_PROBABILITY, REGRESSION_MAX_DELTA, globalLsnRegressionsCount);
                        long possiblyRegressedSatelliteOneLocalLsn = maybeRegress(satelliteOneLocalLsn, REGRESSION_EMISSION_PROBABILITY, REGRESSION_MAX_DELTA, satelliteOneLocalLsnRegressionsCount);
                        long possiblyRegressedSatelliteTwoLocalLsn = maybeRegress(satelliteTwoLocalLsn, REGRESSION_EMISSION_PROBABILITY, REGRESSION_MAX_DELTA, satelliteTwoLocalLsnRegressionsCount);

                        // assumption: globalLsn always increases (per writer) and region-specific LSNs keep monotonically increasing
                        // (this test assumes LSNs belonging to the same epoch are always monotonically increasing and therefore assumes there are no service-side regressions)
                        String vector = buildVectorToken(version, possiblyRegressedGlobalLsn, possiblyRegressedSatelliteOneLocalLsn, possiblyRegressedSatelliteTwoLocalLsn);
                        String headerValue = pkRangeId + ":" + vector;

                        Map<String, String> respHeaders = new HashMap<>();
                        respHeaders.put(HttpConstants.HttpHeaders.SESSION_TOKEN, headerValue);
                        respHeaders.put(HttpConstants.HttpHeaders.OWNER_FULL_NAME, COLLECTION_FULL_NAME);
                        if (INCLUDE_OWNER_ID) {
                            respHeaders.put(HttpConstants.HttpHeaders.OWNER_ID, COLLECTION_RID);
                        }

                        regionScopedSessionContainer.setSessionToken(writeRequest, respHeaders);
                        logger.info(System.currentTimeMillis() + "=>" + "Writer " + writerIndex + " set token for " + pkRangeId + " => " + headerValue);

                        if (!firstWritten[pkIdx]) {
                            firstWritten[pkIdx] = true;
                        }

                        Thread.sleep(1);
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

                            RxDocumentServiceRequest readRequest = RxDocumentServiceRequest.create(
                                mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document,
                                COLLECTION_FULL_NAME + "/docs/doc1", new HashMap<>());
                            readRequest.requestContext.regionalRoutingContextToRoute = chooseRegionWithSkew(endpoints, nonPrimaryRegionProbability);

                            PartitionKeyRange pkRange = new PartitionKeyRange();
                            pkRange.setId(pkRangeId);
                            GatewayTestUtils.setParent(pkRange, ImmutableList.of());
                            readRequest.requestContext.resolvedPartitionKeyRange = pkRange;

                            // Consistency of mapping crucial PK should always map to same PK Range ID.
                            readRequest.setPartitionKeyInternal(ModelBridgeInternal.getPartitionKeyInternal(new PartitionKey(pkRangeId)));
                            readRequest.setPartitionKeyDefinition(partitionKeyDefinition);

                            long sequence = sequenceGenerators[pkIdx].getAndIncrement();
                            ISessionToken sessionToken = regionScopedSessionContainer.resolvePartitionLocalSessionToken(readRequest, pkRangeId);

                            if (sessionToken != null) {
                                orderedAppliers[pkIdx].submit(sequence, sessionToken);
                            }
                        }

                        // There is a possibility that for some ranges no read happened after the last write
                        // So after writers are done, we do one final sweep of all ranges to pick any
                        // read stragglers
                        if (writersDone.getCount() == 0) {
                            // Single final sweep
                            for (int pkIdx = 0; pkIdx < NUM_PK_RANGES; pkIdx++) {
                                String pkRangeId = PK_RANGE_IDS[pkIdx];
                                RxDocumentServiceRequest finalRead = RxDocumentServiceRequest.create(
                                    mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document,
                                    COLLECTION_FULL_NAME + "/docs/doc1", new HashMap<>());
                                finalRead.requestContext.regionalRoutingContextToRoute =
                                    chooseRegionWithSkew(endpoints, nonPrimaryRegionProbability);
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

            // All threads start executing once the main thread arrives here (this is done so reads and writed happen concurrently)
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
            List<String> notWritten = new ArrayList<>();
            for (int i = 0; i < NUM_PK_RANGES; i++) {
                if (firstWritten[i]) {
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

            logger.info("Emitted regressions: globalLsnRegressionsCount=[{}] satelliteOneLocalLsnRegressionCount=[{}] satelliteTwoLocalLsnRegressionCount=[{}]",
                globalLsnRegressionsCount.get(), satelliteOneLocalLsnRegressionsCount.get(), satelliteTwoLocalLsnRegressionsCount.get());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            exec.shutdown();
            globalEndpointManagerMock.close();
            System.clearProperty("COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_INSERTION_COUNT");
            System.clearProperty("COSMOS.PK_BASED_BLOOM_FILTER_EXPECTED_FFP_RATE");
        }
    }

    private static RegionalRoutingContext chooseRegionWithSkew(List<RegionalRoutingContext> endpoints, double nonPrimaryProb) {
        if (endpoints.size() <= 1) {
            return endpoints.get(0);
        }
        double d = ThreadLocalRandom.current().nextDouble();
        if (d >= nonPrimaryProb) {
            return endpoints.get(0); // primary per ordering
        }
        int idx = 1 + ThreadLocalRandom.current().nextInt(endpoints.size() - 1);
        return endpoints.get(idx);
    }

    private static String buildVectorToken(long version,
                                           long globalLsn,
                                           long satelliteOneLocalLsn,
                                           long satelliteTwoLocalLsn) {
        return version + "#" + globalLsn + "#" + REGION_ID_SATELLITE_ONE + "=" + satelliteOneLocalLsn + "#" + REGION_ID_SATELLITE_TWO + "=" + satelliteTwoLocalLsn;
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

    private static long maybeRegress(long baseline,
                                     double probability,
                                     long maxDelta,
                                     AtomicInteger counter) {
        if (ThreadLocalRandom.current().nextDouble() < probability && baseline > 0) {
            long delta = 1 + ThreadLocalRandom.current().nextLong(Math.min(maxDelta, baseline));
            counter.incrementAndGet();
            return baseline - delta;
        }
        return baseline;
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
        final long satelliteOneLocalLsn;
        final long satelliteTwoLocalLsn;
        ObservedRecord(long seq, long globalLsn, long satelliteOneLocalLsn, long satelliteTwoLocalLsn) {
            this.seq = seq;
            this.globalLsn = globalLsn;
            this.satelliteOneLocalLsn = satelliteOneLocalLsn;
            this.satelliteTwoLocalLsn = satelliteTwoLocalLsn;
        }
    }

    private static final class OrderedApplier {
        private final int pkIdx;
        private final AtomicLong nextToApply = new AtomicLong(0);
        private final ConcurrentHashMap<Long, ObservedRecord> buffer = new ConcurrentHashMap<>();

        // References to shared arrays for assertions
        private final AtomicLong[] maxObservedGlobalLsn;
        private final AtomicLong[] maxObservedSatelliteOneLocalLsn;
        private final AtomicLong[] maxObservedSatelliteTwoLocalLsn;

        OrderedApplier(int pkIdx,
                       AtomicLong[] maxObservedGlobalLsn,
                       AtomicLong[] maxObservedSatelliteOneLocalLsn,
                       AtomicLong[] maxObservedSatelliteTwoLocalLsn) {
            this.pkIdx = pkIdx;
            this.maxObservedGlobalLsn = maxObservedGlobalLsn;
            this.maxObservedSatelliteOneLocalLsn = maxObservedSatelliteOneLocalLsn;
            this.maxObservedSatelliteTwoLocalLsn = maxObservedSatelliteTwoLocalLsn;
        }

        void submit(long seq, ISessionToken token) {
            ParsedVector pv = parseVector(token.convertToString());
            long glsn = token.getLSN();
            long satelliteOneLocalLsn = pv.regionIdToLsn.getOrDefault(REGION_ID_SATELLITE_ONE, -1L);
            long satelliteTwoLocalLsn = pv.regionIdToLsn.getOrDefault(REGION_ID_SATELLITE_TWO, -1L);

            buffer.put(seq, new ObservedRecord(seq, glsn, satelliteOneLocalLsn, satelliteTwoLocalLsn));
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
            long prevG = maxObservedGlobalLsn[pkIdx].getAndAccumulate(rec.globalLsn, Math::max);
            if (prevG > rec.globalLsn) {
                fail("Global LSN decreased (seq=" + rec.seq + ") for " + PK_RANGE_IDS[pkIdx] +
                    ": prev=" + prevG + " new=" + rec.globalLsn);
            }
            if (rec.satelliteOneLocalLsn >= 0) {
                long prevP = maxObservedSatelliteOneLocalLsn[pkIdx].getAndAccumulate(rec.satelliteOneLocalLsn, Math::max);
                if (prevP > rec.satelliteOneLocalLsn) {
                    fail("Primary local LSN decreased (seq=" + rec.seq + ") for " + PK_RANGE_IDS[pkIdx] +
                        ": prev=" + prevP + " new=" + rec.satelliteOneLocalLsn);
                }
            }
            if (rec.satelliteTwoLocalLsn >= 0) {
                long prevS = maxObservedSatelliteTwoLocalLsn[pkIdx].getAndAccumulate(rec.satelliteTwoLocalLsn, Math::max);
                if (prevS > rec.satelliteTwoLocalLsn) {
                    fail("Secondary local LSN decreased (seq=" + rec.seq + ") for " + PK_RANGE_IDS[pkIdx] +
                        ": prev=" + prevS + " new=" + rec.satelliteTwoLocalLsn);
                }
            }
        }
    }
}
