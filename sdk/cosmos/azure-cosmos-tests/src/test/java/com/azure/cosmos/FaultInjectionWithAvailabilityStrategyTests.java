// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
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
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class FaultInjectionWithAvailabilityStrategyTests extends TestSuiteBase {

    private final static Logger logger = LoggerFactory.getLogger(FaultInjectionWithAvailabilityStrategyTests.class);

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

    @DataProvider(name = "testConfigs_readAfterCreation_with_readSessionNotAvailable")
    public Object[][] testConfigs_readAfterCreation_with_readSessionNotAvailable() {
        final String sameDocumentIdJustCreated = null;

        CosmosRegionSwitchHint noRegionSwitchHint = null;
        ThresholdBasedAvailabilityStrategy defaultAvailabilityStrategy = new ThresholdBasedAvailabilityStrategy();
        ThresholdBasedAvailabilityStrategy noAvailabilityStrategy = null;
        ThresholdBasedAvailabilityStrategy eagerThresholdAvailabilityStrategy = new ThresholdBasedAvailabilityStrategy(
            Duration.ofMillis(5), Duration.ofMillis(10)
        );
        ThresholdBasedAvailabilityStrategy reluctantThresholdAvailabilityStrategy = new ThresholdBasedAvailabilityStrategy(
            Duration.ofSeconds(10), Duration.ofSeconds(1)
        );

        Consumer<CosmosAsyncContainer> injectReadSessionNotAvailableIntoAllRegions =
            (c) -> injectReadSessionNotAvailableError(c, this.writeableRegions);

        Consumer<CosmosAsyncContainer> injectReadSessionNotAvailableIntoFirstRegionOnly =
            (c) -> injectReadSessionNotAvailableError(c, this.getFirstRegion());

        Consumer<CosmosAsyncContainer> injectReadSessionNotAvailableIntoAllExceptFirstRegion =
            (c) -> injectReadSessionNotAvailableError(c, this.getAllRegionsExceptFirst());

        Consumer<CosmosAsyncContainer> injectTransitTimeoutIntoFirstRegionOnly =
            (c) -> injectTransitTimeout(c, this.getFirstRegion());

        Consumer<CosmosAsyncContainer> injectTransitTimeoutIntoAllRegions =
            (c) -> injectTransitTimeout(c, this.writeableRegions);

        Consumer<CosmosAsyncContainer> injectServiceUnavailableIntoFirstRegionOnly =
            (c) -> injectServiceUnavailable(c, this.getFirstRegion());

        Consumer<CosmosAsyncContainer> injectServiceUnavailableIntoAllRegions =
            (c) -> injectServiceUnavailable(c, this.writeableRegions);

        Consumer<CosmosAsyncContainer> injectInternalServerErrorIntoFirstRegionOnly =
            (c) -> injectInternalServerError(c, this.getFirstRegion());

        Consumer<CosmosAsyncContainer> injectInternalServerErrorIntoAllRegions =
            (c) -> injectInternalServerError(c, this.writeableRegions);

        BiConsumer<Integer, Integer> validateStatusCodeIsReadSessionNotAvailableError =
            (statusCode, subStatusCode) -> {
                assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE);
            };

        BiConsumer<Integer, Integer> validateStatusCodeIsOperationCancelled =
            (statusCode, subStatusCode) -> {
                assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.REQUEST_TIMEOUT);
                assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.CLIENT_OPERATION_TIMEOUT);
            };

        BiConsumer<Integer, Integer> validateStatusCodeIsLegitNotFound =
            (statusCode, subStatusCode) -> {
                assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
            };

        BiConsumer<Integer, Integer> validateStatusCodeIs200Ok =
            (statusCode, subStatusCode) -> assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.OK);

        BiConsumer<Integer, Integer> validateStatusCodeIsInternalServerError =
            (statusCode, subStatusCode) -> {
                assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
                assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.UNKNOWN);
            };

        BiConsumer<Integer, Integer> validateStatusCodeIsServiceUnavailable =
            (statusCode, subStatusCode) -> {
                assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE);
                assertThat(subStatusCode).isEqualTo(HttpConstants.SubStatusCodes.SERVER_GENERATED_503);
            };

        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForAllRegions =
            (ctx) -> {
                logger.debug(
                    "Diagnostics Context to evaluate: {}",
                    ctx != null ? ctx.toJson() : "NULL");

                assertThat(ctx).isNotNull();
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo(this.writeableRegions.size());
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(this.writeableRegions.size());
            };

        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion =
            (ctx) -> {
                logger.info(
                    "Diagnostics Context to evaluate: {}",
                    ctx != null ? ctx.toJson() : "NULL");

                assertThat(ctx).isNotNull();
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo (1);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(1);
                assertThat(ctx.getContactedRegionNames().iterator().next())
                    .isEqualTo(this.writeableRegions.get(0).toLowerCase(Locale.ROOT));
            };

        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover =
            (ctx) -> {
                logger.info(
                    "Diagnostics Context to evaluate: {}",
                    ctx != null ? ctx.toJson() : "NULL");

                assertThat(ctx).isNotNull();
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo (1);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
            };

        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButTwoContactedRegions =
            (ctx) -> {
                logger.info(
                    "Diagnostics Context to evaluate: {}",
                    ctx != null ? ctx.toJson() : "NULL");

                assertThat(ctx).isNotNull();
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo (1);
                assertThat(ctx.getDiagnostics().size()).isLessThanOrEqualTo(2);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
            };

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
            // to the operations triggered by teh availability strategy against each region will all timeout because
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
        List<String> applicableRegions) {

        String ruleName = "serverErrorRule-read-session-unavailable-" + UUID.randomUUID();
        FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            FaultInjectionOperationType.READ_ITEM,
            badSessionTokenServerErrorResult
        );
    }

    private static void injectTransitTimeout(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions) {

        String ruleName = "serverErrorRule-transitTimeout-" + UUID.randomUUID();
        FaultInjectionServerErrorResult timeoutResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofSeconds(6))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            FaultInjectionOperationType.READ_ITEM,
            timeoutResult
        );
    }

    private static void injectServiceUnavailable(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions) {

        String ruleName = "serverErrorRule-serviceUnavailable-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            FaultInjectionOperationType.READ_ITEM,
            serviceUnavailableResult
        );
    }

    private static void injectInternalServerError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions) {

        String ruleName = "serverErrorRule-internalServerError-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            FaultInjectionOperationType.READ_ITEM,
            serviceUnavailableResult
        );
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_readAfterCreation_with_readSessionNotAvailable")
    public void readItemAfterCreatingIt(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        String readItemDocumentIdOverride,
        Consumer<CosmosAsyncContainer> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext) {

        logger.info("START {}", testCaseId);

        CosmosAsyncClient clientWithPreferredRegions = buildCosmosClient(this.writeableRegions, regionSwitchHint);
        try {
            String documentId = UUID.randomUUID().toString();
            Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

            CosmosDiagnosticsTest.TestItem createdItem = new CosmosDiagnosticsTest.TestItem(documentId, documentId);
            CosmosAsyncContainer testContainer = clientWithPreferredRegions
                .getDatabase(this.testDatabaseId)
                    .getContainer(this.testContainerId);

            testContainer.createItem(createdItem).block();
            if (faultInjectionCallback != null) {
                faultInjectionCallback.accept(testContainer);
            }

            CosmosEndToEndOperationLatencyPolicyConfigBuilder e2ePolicyBuilder =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(endToEndTimeout)
                    .enable(true);
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                availabilityStrategy != null
                    ? e2ePolicyBuilder.availabilityStrategy(availabilityStrategy).build()
                    : e2ePolicyBuilder.build();

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            if (endToEndOperationLatencyPolicyConfig != null) {
                itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
            }

            try {
                CosmosItemResponse<ObjectNode> response = testContainer
                    .readItem(
                        readItemDocumentIdOverride != null ? readItemDocumentIdOverride : idAndPkValPair.getLeft(),
                        new PartitionKey(idAndPkValPair.getRight()),
                        itemRequestOptions,
                        ObjectNode.class)
                    .block();

                validateStatusCode.accept(response.getStatusCode(), null);

                CosmosDiagnosticsContext diagnosticsContext = null;

                if (response != null && response.getDiagnostics() != null) {
                    diagnosticsContext = response.getDiagnostics().getDiagnosticsContext();
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
        CosmosRegionSwitchHint regionSwitchHint) {

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CosmosDiagnosticsLogger());

        return new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(new SessionRetryOptions(regionSwitchHint))
            .directMode()
            .multipleWriteRegionsEnabled(true)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();
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
}
