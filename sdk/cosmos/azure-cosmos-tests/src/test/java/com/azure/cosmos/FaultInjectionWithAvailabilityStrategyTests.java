// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

@SuppressWarnings("SameParameterValue")
public class FaultInjectionWithAvailabilityStrategyTests extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(FaultInjectionWithAvailabilityStrategyTests.class);

    private final static Integer NO_QUERY_PAGE_SUB_STATUS_CODE = 9999;

    private final static String sameDocumentIdJustCreated = null;

    private final static Boolean notSpecifiedWhetherIdempotentWriteRetriesAreEnabled = null;

    private final static CosmosRegionSwitchHint noRegionSwitchHint = null;
    private final static  ThresholdBasedAvailabilityStrategy defaultAvailabilityStrategy = new ThresholdBasedAvailabilityStrategy();
    private final static ThresholdBasedAvailabilityStrategy noAvailabilityStrategy = null;
    private final static ThresholdBasedAvailabilityStrategy eagerThresholdAvailabilityStrategy =
        new ThresholdBasedAvailabilityStrategy(
            Duration.ofMillis(1), Duration.ofMillis(10)
        );
    private final static ThresholdBasedAvailabilityStrategy reluctantThresholdAvailabilityStrategy =
        new ThresholdBasedAvailabilityStrategy(
            Duration.ofSeconds(10), Duration.ofSeconds(1)
        );

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsReadSessionNotAvailableError =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsOperationCancelled =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsLegitNotFound =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIs200Ok =
        (statusCode, subStatusCode) -> assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.OK);

    private final static BiConsumer<Integer, Integer> validateStatusCodeIs201Created =
        (statusCode, subStatusCode) -> assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.CREATED);

    private final static BiConsumer<Integer, Integer> validateStatusCodeIs204NoContent =
        (statusCode, subStatusCode) -> assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NO_CONTENT);

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsInternalServerError =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
        };

    private final static BiConsumer<Integer, Integer> validateStatusCodeIsServiceUnavailable =
        (statusCode, subStatusCode) -> {
            assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
            assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.SERVER_GENERATED_503);
        };

    private final static Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover =
        (ctx) -> {
            logger.info(
                "Diagnostics Context to evaluate: {}",
                ctx != null ? ctx.toJson() : "NULL");

            assertThat(ctx).isNotNull();
            if (ctx != null) {
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
            }
        };

    private final static Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions =
        (ctx) -> {
            logger.info(
                "Diagnostics Context to evaluate: {}",
                ctx != null ? ctx.toJson() : "NULL");

            assertThat(ctx).isNotNull();
            if (ctx != null) {
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                assertThat(ctx.getDiagnostics().size()).isLessThanOrEqualTo(2);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
            }
        };

    private final static BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> noFailureInjection =
        (container, operationType) -> {};

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectReadSessionNotAvailableIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectReadSessionNotAvailableIntoFirstRegionOnly = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectReadSessionNotAvailableIntoAllExceptFirstRegion = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectTransitTimeoutIntoFirstRegionOnly = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectTransitTimeoutIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectServiceUnavailableIntoFirstRegionOnly = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectServiceUnavailableIntoAllRegions = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectInternalServerErrorIntoFirstRegionOnly = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectInternalServerErrorIntoAllRegions = null;

    private Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForAllRegions = null;

    private Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion = null;

    private List<String> writeableRegions;

    private String testDatabaseId;
    private String testContainerId;

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        if (row == null || row.length == 0) {
            return "";
        }

        return (String)row[0];
    }

    @BeforeClass(groups = { "multi-master" })
    public void beforeClass() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode();

        CosmosAsyncClient dummyClient = null;

        try {

            dummyClient = clientBuilder.buildAsyncClient();

            AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(dummyClient);
            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
            GlobalEndpointManager globalEndpointManager =
                ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);

            DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

            Map<String, String> writeRegionMap = this.getRegionMap(databaseAccount, true);

            this.writeableRegions = new ArrayList<>(writeRegionMap.keySet());
            assertThat(this.writeableRegions).isNotNull();
            assertThat(this.writeableRegions.size()).isGreaterThanOrEqualTo(2);

            this.validateDiagnosticsContextHasDiagnosticsForAllRegions =
                (ctx) -> {
                    logger.debug(
                        "Diagnostics Context to evaluate: {}",
                        ctx != null ? ctx.toJson() : "NULL");

                    assertThat(ctx).isNotNull();
                    if (ctx != null) {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(this.writeableRegions.size());
                        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(this.writeableRegions.size());
                    }
                };

            this.validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion =
                (ctx) -> {
                    logger.info(
                        "Diagnostics Context to evaluate: {}",
                        ctx != null ? ctx.toJson() : "NULL");

                    assertThat(ctx).isNotNull();
                    if (ctx != null) {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(ctx.getContactedRegionNames().iterator().next())
                            .isEqualTo(this.writeableRegions.get(0).toLowerCase(Locale.ROOT));
                    }
                };

            this.injectReadSessionNotAvailableIntoAllRegions =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.writeableRegions, operationType);

            this.injectReadSessionNotAvailableIntoFirstRegionOnly =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.getFirstRegion(), operationType);

            this.injectReadSessionNotAvailableIntoAllExceptFirstRegion =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.getAllRegionsExceptFirst(), operationType);

            this.injectTransitTimeoutIntoFirstRegionOnly =
                (c, operationType) -> injectTransitTimeout(c, this.getFirstRegion(), operationType);

            this.injectTransitTimeoutIntoAllRegions =
                (c, operationType) -> injectTransitTimeout(c, this.writeableRegions, operationType);

            this.injectServiceUnavailableIntoFirstRegionOnly =
                (c, operationType) -> injectServiceUnavailable(c, this.getFirstRegion(), operationType);

            this.injectServiceUnavailableIntoAllRegions =
                (c, operationType) -> injectServiceUnavailable(c, this.writeableRegions, operationType);

            this.injectInternalServerErrorIntoFirstRegionOnly =
                (c, operationType) -> injectInternalServerError(c, this.getFirstRegion(), operationType);

            this.injectInternalServerErrorIntoAllRegions =
                (c, operationType) -> injectInternalServerError(c, this.writeableRegions, operationType);

            CosmosAsyncContainer container = this.createTestContainer(dummyClient);
            this.testDatabaseId = container.getDatabase().getId();
            this.testContainerId = container.getId();

            // Creating a container is an async task - especially with multiple regions it can
            // take some time until the container is available in the remote regions as well
            // When the container does not exist yet, you would see 401 for example for point reads etc.
            // So, adding this delay after container creation to minimize risk of hitting these errors
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } finally {
            safeClose(dummyClient);
        }
    }
    @AfterClass(groups = { "multi-master" })
    public void afterClass() {
        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode();

        CosmosAsyncClient dummyClient = null;
        if (testDatabaseId != null) {
            try {

                dummyClient = clientBuilder.buildAsyncClient();
                CosmosAsyncDatabase testDatabase = dummyClient
                    .getDatabase(this.testDatabaseId);

                safeDeleteDatabase(testDatabase);
            } finally {
                safeClose(dummyClient);
            }
        }
    }

    private List<String> getFirstRegion() {
        ArrayList<String> regions = new ArrayList<>();
        regions.add(this.writeableRegions.get(0));
        return regions;
    }

    private List<String> getAllRegionsExceptFirst() {
        ArrayList<String> regions = new ArrayList<>();
        for (int i = 1; i < this.writeableRegions.size(); i++) {
            regions.add(this.writeableRegions.get(i));
        }

        return regions;
    }

    @DataProvider(name = "testConfigs_readAfterCreation")
    public Object[][] testConfigs_readAfterCreation() {
        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Availability Strategy used
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    optional documentId used for reads (instead of the just created doc id) - this can be used to trigger 404/0
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    Diagnostics context validation callback
            // },

            // This test injects 404/1002 across all regions for the read operation after the initial creation
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening
            // It is expected to fail with a 404/1002 - the validation will make sure that cross regional
            // execution via availability strategy was happening (but also failed)
            new Object[] {
                "404-1002_AllRegions_RemotePreferred",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 404/1002 for the read operation after the initial creation into the local region only.
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening
            // The availability strategy has a high threshold - so, it is expected that the successful response
            // as a cross-regional retry triggered by the ClientRetryPolicy of the initial operation finishes the Mono
            // successfully with 200 - OK>
            new Object[] {
                "404-1002_OnlyFirstRegion_RemotePreferred_ReluctantAvailabilityStrategy",
                Duration.ofSeconds(1),
                reluctantThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 404/1002 for the read operation after the initial creation into the local region only.
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening
            // The availability strategy is very aggressive with a short threshold - so, it is expected that the
            // successful response comes from the operation being executed against the second region after hitting
            // threshold.
            new Object[] {
                "404-1002_OnlyFirstRegion_RemotePreferred_EagerAvailabilityStrategy",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // Only injects 404/1002 into secondary region - just ensure that no cross regional execution
            // is even happening
            new Object[] {
                "404-1002_AllExceptFirstRegion_RemotePreferred",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllExceptFirstRegion,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test simulates 404/1002 across all regions for the read operation after the initial creation
            // The region switch hint for 404/1002 is local - meaning many local retries are happening which leads
            // to the operations triggered by the availability strategy against each region will all timeout because
            // they haven't finished the "local retries" before hitting end-to-end timeout
            // It is expected to fail with a 404/1002 - the validation will make sure that cross regional
            // execution via availability strategy was happening (but also failed)
            new Object[] {
                "404-1002_AllRegions_LocalPreferred",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 404/1002 for the read operation after the initial creation into the local region only.
            // The region switch hint for 404/1002 is local - meaning many local retries are happening which leads
            // to the operation triggered against the local region timing out. So, it is expected that the
            // successful response comes from the operation being executed against the second region after hitting
            // threshold.
            new Object[] {
                "404-1002_OnlyFirstRegion_LocalPreferred",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // Only injects 404/1002 into secondary region - just ensure that no cross regional execution
            // is even happening
            new Object[] {
                "404-1002_AllExceptFirstRegion_LocalPreferred",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoAllExceptFirstRegion,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 404/1002 for the read operation after the initial creation into the local region only.
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening
            // No availability strategy so, it is expected that the successful response
            // from a cross-regional retry triggered by the ClientRetryPolicy of the initial operation finishes the Mono
            // successfully with 200 - OK>
            new Object[] {
                "404-1002_OnlyFirstRegion_RemotePreferred_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                null,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok, // First operation will failover from region 1 to region 2 quickly enough
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 404/1002 for the read operation after the initial creation into the local region only.
            // The region switch hint for 404/1002 is local -  meaning many local retries are happening which leads
            // to the operation triggered against the local region timing out.
            // No availability strategy - so, it is expected that we see a timeout (operation cancellation) after
            // e2e timeout, because the local 404/1002 retries are still ongoing and no cross-regional retry
            // is triggered yet.
            new Object[] {
                "404-1002_OnlyFirstRegion_LocalPreferred_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                null,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled, // Too many local retries to allow cross regional failover within e2e timeout
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 404/102 only in the local region. The actual read operation is intentionally for
            // a document id that won't exist - so, expected result is a 404/0
            // The region switch hint for 404/1002 is local - meaning many local retries are happening which leads
            // to the operation triggered against the local region timing out.
            // The goal of this test case is to ensure that non-transient errors (like the 404/0) retrieved from the
            // hedging against the second region will complete the composite Mono (even when the initial operation
            // against the local region is still ongoing).
            new Object[] {
                "Legit404_404-1002_OnlyFirstRegion_LocalPreferred",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                "SomeNonExistingId",
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                // Too many local retries to allow cross regional failover within e2e timeout, but after
                // threshold remote region returns 404/0
                validateStatusCodeIsLegitNotFound,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 404/102 only in the local region. The actual read operation is intentionally for
            // a document id that won't exist - so, expected result is a 404/0
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening
            // Which means the ClientRetryPolicy for the initial operation would failover to the second region and
            // should result in the 404/0 being returned
            new Object[] {
                "Legit404_404-1002_OnlyFirstRegion_RemotePreferred_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                null,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                "SomeNonExistingId",
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsLegitNotFound, // Too many local retries to allow cross regional failover within e2e timeout
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 404/102 only in the local region. The actual read operation is intentionally for
            // a document id that won't exist - so, expected result is a 404/0
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening
            // Which means the cross-regional retry initiated by the ClientRetryPolicy of the initial operation
            // should result in returning 404/0
            new Object[] {
                "Legit404_OnlyFirstRegion_RemotePreferred",
                Duration.ofSeconds(5),
                reluctantThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                "SomeNonExistingId",
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsLegitNotFound, // Too many local retries to allow cross regional failover within e2e timeout
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },


            // This test injects 408 timeouts (as transit timeouts) into all regions.
            // Expected outcome is a 408 after doing cross regional retries via availability strategy
            // against all regions
            new Object[] {
                "408_AllRegions",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 408 timeouts (as transit timeouts) into the local region only.
            // Expected outcome is a successful response from the cross-regional retry from availability strategy
            // against the secondary region.
            new Object[] {
                "408_FirstRegionOnly",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 408 timeouts (as transit timeouts) into all regions.
            // There is no availability strategy - so, expected outcome is a timeout with diagnostics for just
            // the local region
            new Object[] {
                "408_AllRegions_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 408 timeouts (as transit timeouts) into the local region only.
            // There is no availability strategy - the e2e timeouts is very high. This test evaluates whether
            // cross regional retry would eventually kick-in for the 408s even without availability strategy.
            //
            // NOTE - local retries for 408 would basically retry forever in local region
            // even within 30 seconds no cross-regional retry is happening
            //
            new Object[] {
               "408_FirstRegionOnly_NoAvailabilityStrategy_LongE2ETimeout",
               Duration.ofSeconds(90),
               noAvailabilityStrategy,
               noRegionSwitchHint,
               sameDocumentIdJustCreated,
               injectTransitTimeoutIntoFirstRegionOnly,
               validateStatusCodeIs200Ok,
               validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 408 timeouts (as transit timeouts) into the local region only.
            // There is no availability strategy - the e2e timeout is shorter than the NetworkRequestTimeout - so,
            // a timeout is expected with diagnostics only for the local region
            new Object[] {
                "408_FirstRegionOnly_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // No availability strategy exists - expected outcome is a successful response from the cross-regional
            // retry issued in the client retry policy
            new Object[] {
               "503_FirstRegionOnly_NoAvailabilityStrategy",
               Duration.ofSeconds(90),
               noAvailabilityStrategy,
               noRegionSwitchHint,
               sameDocumentIdJustCreated,
               injectServiceUnavailableIntoFirstRegionOnly,
               validateStatusCodeIs200Ok,
               validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // Expected outcome is a successful retry either by the cross-regional retry triggered in the
            // ClientRetryPolicy of the initial execution or the one triggered by the availability strategy
            // whatever happens first
            new Object[] {
                "503_FirstRegionOnly",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },

            // This test injects 503 (Service Unavailable) into all regions.
            // Expected outcome is a timeout due to ongoing retries in both operations triggered by
            // availability strategy. Diagnostics should contain two operations.
            new Object[] {
                "503_AllRegions",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 500 (Internal Server Error) into all regions.
            //
            // Currently, 500 (Internal Server error) is not ever retried. Neither in the Consistency Reader
            // nor ClientRetryPolicy - so, a 500 will immediately fail the Mono and there will only
            // be diagnostics for the first region
            new Object[] {
               "500_FirstRegionOnly_NoAvailabilityStrategy",
               Duration.ofSeconds(90),
               noAvailabilityStrategy,
               noRegionSwitchHint,
               sameDocumentIdJustCreated,
               injectInternalServerErrorIntoFirstRegionOnly,
               validateStatusCodeIsInternalServerError,
               validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 500 (Internal Server Error) into all regions.
            //
            // Currently, 500 (Internal Server error) is not ever retried. Neither in the Consistency Reader
            // nor ClientRetryPolicy - so, a 500 will immediately fail the Mono and there will only
            // be diagnostics for the first region
            new Object[] {
                "500_FirstRegionOnly_DefaultAvailabilityStrategy",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectInternalServerErrorIntoFirstRegionOnly,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 500 (Internal Server Error) into all regions.
            //
            // Currently, 500 (Internal Server error) is not ever retried. Neither in the Consistency Reader
            // nor ClientRetryPolicy - so, a 500 will immediately fail the Mono and there will only
            // be diagnostics for the first region
            new Object[] {
                "500_AllRegions_DefaultAvailabilityStrategy",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                sameDocumentIdJustCreated,
                injectInternalServerErrorIntoAllRegions,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_readAfterCreation")
    public void readAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext) {

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readItemCallback = (params) ->
            new CosmosResponseWrapper(params.container
                .readItem(
                    readItemDocumentIdOverride != null
                        ? readItemDocumentIdOverride
                        : params.idAndPkValuePair.getLeft(),
                    new PartitionKey(params.idAndPkValuePair.getRight()),
                    params.options,
                    ObjectNode.class)
                .block());

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            FaultInjectionOperationType.READ_ITEM,
            readItemCallback,
            faultInjectionCallback,
            validateStatusCode,
            validateDiagnosticsContext);
    }

    @DataProvider(name = "testConfigs_writeAfterCreation")
    public Object[][] testConfigs_writeAfterCreation() {
        final boolean nonIdempotentWriteRetriesEnabled = true;
        final boolean nonIdempotentWriteRetriesDisabled = false;

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> createAnotherItemCallback =
            (params) -> {
                String newDocumentId = UUID.randomUUID().toString();

                return new CosmosResponseWrapper(params.container
                    .createItem(
                        createTestItemAsJson(newDocumentId, params.idAndPkValuePair.getRight()),
                        params.options)
                    .block());
            };

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> replaceItemCallback =
            (params) -> {
                ObjectNode newItem =
                    createTestItemAsJson(params.idAndPkValuePair.getLeft(), params.idAndPkValuePair.getRight());
                newItem.put("newField", UUID.randomUUID().toString());

                return new CosmosResponseWrapper(params.container
                    .replaceItem(
                        newItem,
                        params.idAndPkValuePair.getLeft(),
                        new PartitionKey(params.idAndPkValuePair.getRight()),
                        params.options)
                    .block());
            };

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> deleteItemCallback =
            (params) -> new CosmosResponseWrapper(params.container
                    .deleteItem(
                        params.idAndPkValuePair.getLeft(),
                        new PartitionKey(params.idAndPkValuePair.getRight()),
                        params.options)
                    .block());
        Function<ItemOperationInvocationParameters, CosmosItemResponse<?>> deleteNonExistingItemCallback =
            (params) -> params.container
                    .deleteItem(
                        UUID.randomUUID().toString(),
                        new PartitionKey(params.idAndPkValuePair.getRight()),
                        params.options)
                    .block();

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> upsertExistingItemCallback =
            (params) -> {
                ObjectNode newItem =
                    createTestItemAsJson(params.idAndPkValuePair.getLeft(), params.idAndPkValuePair.getRight());
                newItem.put("newField", UUID.randomUUID().toString());

                return new CosmosResponseWrapper(params.container
                    .upsertItem(
                        newItem,
                        new PartitionKey(params.idAndPkValuePair.getRight()),
                        params.options)
                    .block());
            };

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> upsertAnotherItemCallback =
            (params) -> new CosmosResponseWrapper(params.container
                    .upsertItem(
                        createTestItemAsJson(UUID.randomUUID().toString(), params.idAndPkValuePair.getRight()),
                        new PartitionKey(params.idAndPkValuePair.getRight()),
                        params.options)
                    .block());

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> patchItemCallback =
            (params) -> {
                CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
                patchOperations.add("/newField", UUID.randomUUID().toString());

                if (params.nonIdempotentWriteRetriesEnabled) {
                    params.options.setNonIdempotentWriteRetryPolicy(
                        true, true
                    );
                }

                return new CosmosResponseWrapper(params.container
                    .patchItem(
                        params.idAndPkValuePair.getLeft(),
                        new PartitionKey(params.idAndPkValuePair.getRight()),
                        patchOperations,
                        params.options,
                        ObjectNode.class)
                    .block());
            };

        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Availability Strategy used
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    nonIdempotentWriteRetriesEnabled?
            //    FaultInjectionOperationType
            //    write operation - action to be executed after initial document creation
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    Diagnostics context validation callback
            // },

            // This test injects 503 (Service Unavailable) into the local region only.
            // No availability strategy exists - expected outcome is a successful response from the cross-regional
            // retry issued in the client retry policy
            new Object[] {
                "Create_503_FirstRegionOnly_NoAvailabilityStrategy_WithWriteRetries",
                Duration.ofSeconds(3),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // No availability strategy exists - expected outcome is a 503 because non-idempotent write retries
            // are disabled - and no cross regional retry is happening
            new Object[] {
                "Create_503_FirstRegionOnly_NoAvailabilityStrategy_NoWriteRetries",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from either the cross
            // regional retry in client retry policy of operations against first region - or the hedging
            // against the second region
            new Object[] {
                "Create_503_FirstRegionOnly_WithWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // Default availability strategy exists - expected outcome is a 503 because non-idempotent write retries
            // are disabled - which means no hedging for write operations nor cross regional retry
            new Object[] {
                "Create_503_FirstRegionOnly_NoWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 503 (Service Unavailable) into all regions.
            // Eager availability strategy exists - expected outcome is a 503 - diagnostics should reflect the
            // hedging against second region
            new Object[] {
                "Create_503_AllRegions_WithWriteRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 503 (Service Unavailable) into all regions.
            // Default availability strategy exists - expected outcome is a 503 because non-idempotent write retries
            // are disabled - which means no hedging for write operations nor cross regional retry
            // Same expectation for all write operation types
            new Object[] {
                "Create_503_AllRegions_NoWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "Replace_503_AllRegions_NoWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "Patch_503_AllRegions_NoWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "Delete_503_AllRegions_NoWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "UpsertExisting_503_AllRegions_NoWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "UpsertNew_503_AllRegions_NoWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertAnotherItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            new Object[] {
                "Patch_503_FirstRegionOnly_WithWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },

            // This test injects 503 (Service Unavailable) into the first region only.
            // Default availability strategy exists - expected outcome is a successful response because non-idempotent
            // write retries are enabled which would allow hedging (or cross regional fail-over) to succeed
            // Same expectation for all write operation types
            new Object[] {
                "Delete_503_FirstRegionOnly_WithWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs204NoContent,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },
            new Object[] {
                "Replace_503_FirstRegionOnly_WithWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },
            new Object[] {
                "UpsertNew_503_FirstRegionOnly_WithWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },
            new Object[] {
                "UpsertExisting_503_FirstRegionOnly_WithWriteRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },
            new Object[] {
                "Create_500_FirstRegionOnly_NoAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectInternalServerErrorIntoFirstRegionOnly,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 500 (Internal Service Error) into the first region only.
            // No availability strategy exists, non-idempotent write retries are disabled.
            // No hedging, no cross regional retry in client retry policy --> 500 thrown
            new Object[] {
                "Create_500_FirstRegionOnly_NoAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectInternalServerErrorIntoFirstRegionOnly,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 500 (Internal Service Error) into the first region only.
            // Reluctant availability strategy exists, non-idempotent write retries are enabled.
            // 500 is not applicable for cross regional retry in client retry policy --> so, 500 would be thrown from
            // initial Mono against local region - hedging is not applicable because the 500 happens way before
            // threshold is reached
            new Object[] {
                "Delete_500_FirstRegionOnly_ReluctantAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                reluctantThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectInternalServerErrorIntoFirstRegionOnly,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 500 (Internal Service Error) into the first region only.
            // Default availability strategy exists, non-idempotent write retries are disabled. No hedging
            // (write retries disabled), no cross regional retry in client retry policy for 500 --> 500 thrown
            new Object[] {
                "Delete_500_FirstRegionOnly_DefaultAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectInternalServerErrorIntoFirstRegionOnly,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 500 (Internal Service Error) into all regions.
            // Default availability strategy exists, non-idempotent write retries are enabled. Hedging is enabled
            // but the 500 from the initial operation execution is thrown before threshold is reached
            new Object[] {
                "Patch_500_AllRegions_DefaultAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                reluctantThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectInternalServerErrorIntoAllRegions,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 500 (Internal Service Error) into all regions.
            // Default availability strategy exists, non-idempotent write retries are disabled. So, no hedging or cross
            // regional retries in client retry policy --> 500 thrown
            new Object[] {
                "Patch_500_AllRegions_DefaultAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectInternalServerErrorIntoAllRegions,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects transit timeouts into all regions.
            // Default availability strategy exists, non-idempotent write retries are disabled. So, no hedging or (cross
            // regional) retries after transit timeouts --> Also the timeout (network request timeout) is higher than
            // the e2e timeout --> operations gets cancelled when hitting e2e timeout. Diagnostics should only have
            // data for initial region
            new Object[] {
                "Replace_408_AllRegions_DefaultAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectTransitTimeoutIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects transit timeouts into all regions.
            // Eager availability strategy exists, non-idempotent write retries are enabled. Hedging is enabled,
            // but at e2e timeout operation against both regions have not finished yet -> 408
            // Diagnostics should contain data for original and hedging operation
            new Object[] {
                "Replace_408_AllRegions_DefaultAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectTransitTimeoutIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // No Availability strategy (hedging), no write retries -> operation canceled
            // Diagnostics only in first region due to no hedging
            new Object[] {
                "Replace_408_AllRegions_NoAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectTransitTimeoutIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // No Availability strategy (hedging), operation not finished at e2e timeout - so, cross regional
            // retry in client retry policy has not even been started yet -> 408
            // Diagnostics only in first region due to no hedging or cross regional fail-over being started yet
            new Object[] {
                "Replace_408_AllRegions_NoAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectTransitTimeoutIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // No hedging due to non-idempotent write retries being disabled. Operation not finished at e2e timeout -
            // so, cross regional retry in client retry policy has not even been started yet -> 408
            // Diagnostics only in first region due to no hedging or cross regional fail-over being started yet
            new Object[] {
                "UpsertExisting_408_FirstRegionOnly_DefaultAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // Hedging and non-idempotent write retries enabled. Operation against first region still pending, cross
            // regional retry in client retry policy not yet started.
            // So, successful response from hedging expected.
            // Diagnostics should have data for both operations.
            new Object[] {
                "UpsertExisting_408_FirstRegionOnly_DefaultAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // No hedging because there is no availability strategy, No cross regional retries due to non-idempotent
            // write retries being disabled. Operation not finished at e2e timeout -> 408
            // Diagnostics only in first region
            new Object[] {
                "UpsertNew_408_FirstRegionOnly_NoAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertAnotherItemCallback,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // No hedging/availability strategy, cross regional retry not started yet -> 408,  single diagnostics
            new Object[] {
                "UpsertNew_408_FirstRegionOnly_NoAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(90),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertAnotherItemCallback,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test simulates 404/1002 across all regions for the operation after the initial creation
            // The region switch hint for 404/1002 is local - meaning many local retries are happening which leads
            // to the operations triggered by the availability strategy against each region will all timeout because
            // they haven't finished the "local retries" before hitting end-to-end timeout
            // The validation will make sure that cross regional
            // execution via availability strategy was happening (but also failed)
            new Object[] {
                "Create_404-1002_AllRegions_LocalPreferred_DefaultAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test simulates 404/1002 across all regions for the operation after the initial creation
            // The region switch hint for 404/1002 is local - meaning many local retries are happening which leads
            // to the operations triggered by the availability strategy against each region will all timeout because
            // they haven't finished the "local retries" before hitting end-to-end timeout
            // The validation will make sure that cross regional
            // execution via availability strategy was happening (but also failed)
            new Object[] {
                "Replace_404-1002_AllRegions_LocalPreferred_DefaultAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test simulates 404/1002 across all regions for the operation after the initial creation
            // The region switch hint for 404/1002 is remote - meaning no local retries are happening.
            // Hedging is enabled.
            // Operations against all regions (initial and via hedging) are expected to fail with 404/1002.
            // The validation will make sure that cross regional
            // execution via availability strategy was happening (but also failed)
            new Object[] {
                "Replace_404-1002_AllRegions_RemotePreferred_EagerAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // 404/1022 into all region
            // Availability strategy exists - but no hedging because NonIdempotentWriteRetries are disabled
            // No regional fail-over seen, because local preferred region switch (retying too long locally to allow
            // cross regional retry)
            new Object[] {
                "Replace_404-1002_AllRegions_LocalPreferred_EagerAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // Availability strategy exists - but no hedging because NonIdempotentWriteRetries are disabled
            // Regional fail-over seen, because preferred region switch is remote
            new Object[] {
                "Replace_404-1002_AllRegions_RemotePreferred_EagerAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoAllRegions,
                validateStatusCodeIsReadSessionNotAvailableError,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // 404/1022 into all region
            // Availability strategy exists - but no hedging because NonIdempotentWriteRetries are disabled
            // No regional fail-over seen, because local preferred region switch (retying too long locally to allow
            // cross regional retry)
            new Object[] {
                "Replace_404-1002_FirstRegionOnly_RemotePreferred_EagerAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // 404/1022 into all region
            // Availability strategy exists - but no hedging because NonIdempotentWriteRetries are disabled
            // Regional fail-over seen = remote preferred region switch allows cross-regional retry to happen in
            // client retry policy within the e2e timeout.
            new Object[] {
                "Replace_404-1002_FirstRegionOnly_LocalPreferred_EagerAvailabilityStrategy_NoRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // 404/1022 into local region only
            // Availability strategy exists - hedging or cross regional retry (remote regional preference) would
            // result in successful response.
            new Object[] {
                "Replace_404-1002_FirstRegionOnly_RemotePreferred_EagerAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions
            },

            // 404/1022 into local region only
            // Availability strategy exists - hedging is enabled - no cross regional retry expected (local regional
            // preference results in too many local retries). Should result in successful response form hedging.
            new Object[] {
                "Replace_404-1002_FirstRegionOnly_LocalPreferred_EagerAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // 404/1022 into local region only
            // Availability strategy exists, but threshold is very high. So, hedging is not applicable.
            // Expected to get successful response from cross regional retry - region switch is remote allowing the
            // cross regional retry to finish within e2e timeout.
            new Object[] {
                "Create_404-1002_FirstRegionOnly_RemotePreferred_ReluctantAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                reluctantThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // 404/1022 into local region only
            // Availability strategy exists, hedging is enabled. Region switch is local - meaning the local retries
            // will take so long, that the cross-regional retry in the client retry policy is not applicable.
            // Successful response expected from hedging. Diagnostics should have data for both operations.
            new Object[] {
                "Create_404-1002_FirstRegionOnly_LocalPreferred_EagerAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // 404/1022 into local region only, attempt to delete a non-existing item
            // Availability strategy exists, hedging is enabled. Region switch is local - meaning the local retries
            // will take so long that the initial Mono will hit e2e timeout.
            // Successful response expected from hedging - validating that non=transient errors like 404/0 are
            // terminating the composite Mono. Diagnostics should have data for both operations.
            new Object[] {
                "DeleteNonExistingItem_404-1002_FirstRegionOnly_LocalPreferred_EagerAvailabilityStrategy_WithRetries",
                Duration.ofSeconds(1),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteNonExistingItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsLegitNotFound,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_writeAfterCreation")
    public void writeAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        FaultInjectionOperationType faultInjectionOperationType,
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> actionAfterInitialCreation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext) {

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            nonIdempotentWriteRetriesEnabled,
            faultInjectionOperationType,
            actionAfterInitialCreation,
            faultInjectionCallback,
            validateStatusCode,
            validateDiagnosticsContext);
    }

    @DataProvider(name = "testConfigs_queryAfterCreation")
    public Object[][] testConfigs_queryAfterCreation() {
        BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryReturnsFirstNonEmptyPage = (query, params) -> {

            CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
            CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = ImplementationBridgeHelpers
                .CosmosItemRequestOptionsHelper
                .getCosmosItemRequestOptionsAccessor()
                .getEndToEndOperationLatencyPolicyConfig(params.options);
            queryOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

            CosmosPagedFlux<ObjectNode> queryPagedFlux = params.container.queryItems(
                query,
                queryOptions,
                ObjectNode.class
            );

            List<FeedResponse<ObjectNode>> returnedPages =
                queryPagedFlux.byPage(100).collectList().block();

            for (FeedResponse<ObjectNode> page: returnedPages) {
                if (page.getResults() != null && page.getResults().size() > 0) {
                    return new CosmosResponseWrapper(page);
                }
            }

            return new CosmosResponseWrapper(
                null,
                HttpConstants.StatusCodes.NOTFOUND,
                NO_QUERY_PAGE_SUB_STATUS_CODE);
        };

        Function<ItemOperationInvocationParameters, String> singlePartitionQueryGenerator = (params) ->
            "SELECT * FROM c WHERE c.id = '"
                + params.idAndPkValuePair.getLeft()
                + "' AND c.mypk = '"
                +  params.idAndPkValuePair.getRight()
                + "'";

        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Availability Strategy used
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    Function<ItemOperationInvocationParameters, String> queryGenerator
            //    BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryExecution
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    Diagnostics context validation callback
            // },
            new Object[] {
                "FirstNonEmptyPage_AllGood_NoAvailabilityStrategy",
                Duration.ofSeconds(1),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                singlePartitionQueryGenerator,
                queryReturnsFirstNonEmptyPage,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_queryAfterCreation")
    public void queryAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Function<ItemOperationInvocationParameters, String> queryGenerator,
        BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryExecution,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext) {

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            FaultInjectionOperationType.QUERY_ITEM,
            (params) -> queryExecution.apply(queryGenerator.apply(params), params),
            faultInjectionCallback,
            validateStatusCode,
            validateDiagnosticsContext);
    }

    private static ObjectNode createTestItemAsJson(String id, String pkValue) {
        CosmosDiagnosticsTest.TestItem nextItemToBeCreated =
            new CosmosDiagnosticsTest.TestItem(id, pkValue);

        return OBJECT_MAPPER.valueToTree(nextItemToBeCreated);
    }

    private CosmosAsyncContainer createTestContainer(CosmosAsyncClient clientWithPreferredRegions) {
        String dbId = UUID.randomUUID().toString();
        String containerId = UUID.randomUUID().toString();

        clientWithPreferredRegions.createDatabaseIfNotExists(dbId).block();
        CosmosAsyncDatabase databaseWithSeveralWriteableRegions = clientWithPreferredRegions.getDatabase(dbId);

        // setup db and container and pass their ids accordingly
        // ensure the container has a partition key definition of /mypk

        databaseWithSeveralWriteableRegions
            .createContainerIfNotExists(
                new CosmosContainerProperties(
                    containerId,
                    new PartitionKeyDefinition().setPaths(Arrays.asList("/mypk"))))
            .block();

        return databaseWithSeveralWriteableRegions.getContainer(containerId);
    }

    private static void inject(
        String ruleName,
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType applicableOperationType,
        FaultInjectionServerErrorResult toBeInjectedServerErrorResult) {

        FaultInjectionRuleBuilder ruleBuilder = new FaultInjectionRuleBuilder(ruleName);

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        // inject 404/1002s in all regions
        // configure in accordance with preferredRegions on the client
        for (String region : applicableRegions) {
            FaultInjectionCondition faultInjectionConditionForReads =
                new FaultInjectionConditionBuilder()
                    .operationType(applicableOperationType)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region(region)
                    .build();

            // sustained fault injection
            FaultInjectionRule readSessionUnavailableRule = ruleBuilder
                .condition(faultInjectionConditionForReads)
                .result(toBeInjectedServerErrorResult)
                .duration(Duration.ofSeconds(120))
                .build();

            faultInjectionRules.add(readSessionUnavailableRule);
        }

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(containerWithSeveralWriteableRegions, faultInjectionRules)
            .block();

        logger.info(
            "FAULT INJECTION - Applied rule '{}' for regions '{}'.",
            ruleName,
            String.join(", ", applicableRegions));
    }

    private static void injectReadSessionNotAvailableError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType operationType) {

        String ruleName = "serverErrorRule-read-session-unavailable-" + UUID.randomUUID();
        FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            operationType,
            badSessionTokenServerErrorResult
        );
    }

    private static void injectTransitTimeout(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-transitTimeout-" + UUID.randomUUID();
        FaultInjectionServerErrorResult timeoutResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofSeconds(6))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            timeoutResult
        );
    }

    private static void injectServiceUnavailable(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-serviceUnavailable-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
            .delay(Duration.ofMillis(5))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serviceUnavailableResult
        );
    }

    private static void injectInternalServerError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-internalServerError-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serviceUnavailableResult
        );
    }

    private void execute(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled,
        FaultInjectionOperationType faultInjectionOperationType,
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> actionAfterInitialCreation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext) {

        logger.info("START {}", testCaseId);

        CosmosAsyncClient clientWithPreferredRegions = buildCosmosClient(this.writeableRegions, regionSwitchHint, nonIdempotentWriteRetriesEnabled);
        try {
            String documentId = UUID.randomUUID().toString();
            Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

            CosmosDiagnosticsTest.TestItem createdItem = new CosmosDiagnosticsTest.TestItem(documentId, documentId);
            CosmosAsyncContainer testContainer = clientWithPreferredRegions
                .getDatabase(this.testDatabaseId)
                .getContainer(this.testContainerId);

            testContainer.createItem(createdItem).block();
            if (faultInjectionCallback != null) {
                faultInjectionCallback.accept(testContainer, faultInjectionOperationType);
            }

            CosmosEndToEndOperationLatencyPolicyConfigBuilder e2ePolicyBuilder =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(endToEndTimeout)
                    .enable(true);
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                availabilityStrategy != null
                    ? e2ePolicyBuilder.availabilityStrategy(availabilityStrategy).build()
                    : e2ePolicyBuilder.build();

            CosmosPatchItemRequestOptions itemRequestOptions = new CosmosPatchItemRequestOptions();

            if (endToEndOperationLatencyPolicyConfig != null) {
                itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
            }

            try {

                ItemOperationInvocationParameters params = new ItemOperationInvocationParameters();
                params.container = testContainer;
                params.options = itemRequestOptions;
                params.idAndPkValuePair = idAndPkValPair;
                params.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;

                CosmosResponseWrapper response = actionAfterInitialCreation.apply(params);

                CosmosDiagnosticsContext diagnosticsContext = response.getDiagnosticsContext();

                logger.info(
                    "DIAGNOSTICS CONTEXT: {}",
                    diagnosticsContext != null ? diagnosticsContext.toJson(): "NULL");

                if (response == null) {
                    fail("Response is null");
                } else {
                    validateStatusCode.accept(response.getStatusCode(), null);
                }
                validateDiagnosticsContext.accept(diagnosticsContext);
            } catch (Exception e) {
                if (e instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(e, CosmosException.class);
                    CosmosDiagnosticsContext diagnosticsContext = null;
                    if (cosmosException.getDiagnostics() != null) {
                        diagnosticsContext = cosmosException.getDiagnostics().getDiagnosticsContext();
                    }

                    logger.info("EXCEPTION: ", e);
                    logger.info(
                        "DIAGNOSTICS CONTEXT: {}",
                        diagnosticsContext != null ? diagnosticsContext.toJson(): "NULL");

                    validateStatusCode.accept(cosmosException.getStatusCode(), cosmosException.getSubStatusCode());
                    validateDiagnosticsContext.accept(diagnosticsContext);
                } else {
                    fail("A CosmosException instance should have been thrown.", e);
                }
            }
        } finally {
            safeClose(clientWithPreferredRegions);
        }
    }

    private static CosmosAsyncClient buildCosmosClient(
        List<String> preferredRegions,
        CosmosRegionSwitchHint regionSwitchHint,
        Boolean nonIdempotentWriteRetriesEnabled) {

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CosmosDiagnosticsLogger());

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(new SessionRetryOptions(regionSwitchHint))
            .directMode()
            .multipleWriteRegionsEnabled(true)
            .clientTelemetryConfig(telemetryConfig);

        if (nonIdempotentWriteRetriesEnabled != null) {
            builder.setNonIdempotentWriteRetryPolicy(
                nonIdempotentWriteRetriesEnabled, true);
        }

        return builder.buildAsyncClient();
    }

    private Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }

    private static class CosmosResponseWrapper {
        private final CosmosDiagnosticsContext diagnosticsContext;
        private final Integer statusCode;
        private final Integer subStatusCode;

        public CosmosResponseWrapper(CosmosItemResponse itemResponse) {
            if (itemResponse.getDiagnostics() != null &&
                itemResponse.getDiagnostics().getDiagnosticsContext() != null) {

                this.diagnosticsContext = itemResponse.getDiagnostics().getDiagnosticsContext();
            } else {
                this.diagnosticsContext = null;
            }

            this.statusCode = itemResponse.getStatusCode();
            this.subStatusCode = null;
        }

        public CosmosResponseWrapper(CosmosException exception) {
            if (exception.getDiagnostics() != null &&
                exception.getDiagnostics().getDiagnosticsContext() != null) {

                this.diagnosticsContext = exception.getDiagnostics().getDiagnosticsContext();
            } else {
                this.diagnosticsContext = null;
            }

            this.statusCode = exception.getStatusCode();
            this.subStatusCode = exception.getSubStatusCode();
        }

        public CosmosResponseWrapper(FeedResponse<ObjectNode> feedResponse) {
            if (feedResponse.getCosmosDiagnostics() != null &&
                feedResponse.getCosmosDiagnostics().getDiagnosticsContext() != null) {

                this.diagnosticsContext = feedResponse.getCosmosDiagnostics().getDiagnosticsContext();
            } else {
                this.diagnosticsContext = null;
            }

            this.statusCode = 200;
            this.subStatusCode = 0;
        }

        public CosmosResponseWrapper(CosmosDiagnosticsContext ctx, int statusCode, Integer subStatusCode) {
            this.diagnosticsContext = ctx;
            this.statusCode = statusCode;
            this.subStatusCode = subStatusCode;
        }

        public CosmosDiagnosticsContext getDiagnosticsContext() {
            return this.diagnosticsContext;
        }

        public Integer getStatusCode() {
            return this.statusCode;
        }

        public Integer getSubStatusCode() {
            return this.subStatusCode;
        }

    }

    private static class ItemOperationInvocationParameters {
        public CosmosPatchItemRequestOptions options;
        public CosmosAsyncContainer container;
        public Pair<String, String> idAndPkValuePair;
        public Boolean nonIdempotentWriteRetriesEnabled;
    }
}
