// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

@SuppressWarnings("SameParameterValue")
public class FaultInjectionWithAvailabilityStrategyTests extends TestSuiteBase {
    private static final int PHYSICAL_PARTITION_COUNT = 3;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(FaultInjectionWithAvailabilityStrategyTests.class);

    private final static Integer NO_QUERY_PAGE_SUB_STATUS_CODE = 9999;
    private final static Duration ONE_SECOND_DURATION = Duration.ofSeconds(1);
    private final static Duration TWO_SECOND_DURATION = Duration.ofSeconds(2);
    private final static Duration THREE_SECOND_DURATION = Duration.ofSeconds(3);

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

    private final static BiConsumer<Integer, Integer> validateStatusCodeIs429TooManyRequests =
        (statusCode, subStatusCode) -> assertThat(statusCode).isEqualTo(HttpConstants.StatusCodes.TOO_MANY_REQUESTS);

    private final static Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover =
        (ctx) -> {
            assertThat(ctx).isNotNull();
            if (ctx != null) {
                assertThat(ctx.getDiagnostics()).isNotNull();
                assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                assertThat(ctx.getContactedRegionNames().size()).isEqualTo(2);
            }
        };

    private Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion = null;


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

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectQueryPlanTransitTimeoutIntoFirstRegionOnly = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectGatewayTransitTimeoutIntoFirstRegionOnly = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectRequestRateTooLargeIntoFirstRegionOnly = null;

    private BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectRequestRateTooLargeIntoAllRegions = null;

    private Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForAllRegions = null;

    private Consumer<CosmosDiagnosticsContext> validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion = null;

    private String FIRST_REGION_NAME = null;
    private String SECOND_REGION_NAME = null;

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

            FIRST_REGION_NAME = this.writeableRegions.get(0).toLowerCase(Locale.ROOT);
            SECOND_REGION_NAME = this.writeableRegions.get(1).toLowerCase(Locale.ROOT);

            this.validateDiagnosticsContextHasDiagnosticsForAllRegions =
                (ctx) -> {
                    assertThat(ctx).isNotNull();
                    if (ctx != null) {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(this.writeableRegions.size());
                        assertThat(ctx.getContactedRegionNames().size()).isEqualTo(this.writeableRegions.size());
                    }
                };

            this.validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion = (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    assertThat(ctx.getContactedRegionNames().size()).isEqualTo(1);
                    assertThat(ctx.getContactedRegionNames().iterator().next())
                        .isEqualTo(this.writeableRegions.get(0).toLowerCase(Locale.ROOT));
                }
            };

            this.validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion = (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                    assertThat(ctx.getDiagnostics().size()).isLessThanOrEqualTo(2);
                    assertThat(ctx.getContactedRegionNames().size()).isGreaterThanOrEqualTo(1);

                    if (ctx.getContactedRegionNames().size() == 1) {
                        assertThat(ctx.getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                    }
                }
            };

            FeedRange ALL_PARTITIONS = null;

            this.injectReadSessionNotAvailableIntoAllRegions =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.writeableRegions, operationType, ALL_PARTITIONS);

            this.injectReadSessionNotAvailableIntoFirstRegionOnly =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.getFirstRegion(), operationType, ALL_PARTITIONS);

            this.injectReadSessionNotAvailableIntoAllExceptFirstRegion =
                (c, operationType) -> injectReadSessionNotAvailableError(c, this.getAllRegionsExceptFirst(), operationType, ALL_PARTITIONS);

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

            this.injectQueryPlanTransitTimeoutIntoFirstRegionOnly =
                (c, operationType) -> injectGatewayTransitTimeout(
                    c, this.getFirstRegion(), FaultInjectionOperationType.METADATA_REQUEST_QUERY_PLAN);

            this.injectGatewayTransitTimeoutIntoFirstRegionOnly =
                (c, operationType) -> injectGatewayTransitTimeout(
                    c, this.getFirstRegion(), operationType);

            this.injectRequestRateTooLargeIntoFirstRegionOnly =
                (c, operationType) -> injectRequestRateTooLargeError(c, this.getFirstRegion(), operationType);

            this.injectRequestRateTooLargeIntoAllRegions =
                (c, operationType) -> injectRequestRateTooLargeError(c, this.writeableRegions, operationType);

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
                Duration.ofSeconds(10),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                reluctantThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // Only injects 404/1002 into secondary region - just ensure that no cross regional execution
            // is even happening
            new Object[] {
                "404-1002_AllExceptFirstRegion_RemotePreferred",
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                sameDocumentIdJustCreated,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // Only injects 404/1002 into secondary region - just ensure that no cross regional execution
            // is even happening
            new Object[] {
                "404-1002_AllExceptFirstRegion_LocalPreferred",
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                null,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                null,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                null,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
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
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
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
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
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
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                sameDocumentIdJustCreated,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },

            // This test injects 503 (Service Unavailable) into all regions.
            // Expected outcome is a timeout due to ongoing retries in both operations triggered by
            // availability strategy. Diagnostics should contain two operations.
            new Object[] {
                "503_AllRegions",
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
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
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
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
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                sameDocumentIdJustCreated,
                injectInternalServerErrorIntoAllRegions,
                validateStatusCodeIsInternalServerError,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // No availability strategy exists - expected outcome is request failed due to 429
            // SDK does not do cross region retry for 429 by default
            new Object[] {
                "429_FirstRegionOnly_NoAvailabilityStrategy",
                Duration.ofSeconds(90),
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                sameDocumentIdJustCreated,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs429TooManyRequests,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // expected outcome is request will succeed by the hedging request triggered by availability strategy
            new Object[] {
                "429_FirstRegionOnly_EagerThresholdAvailabilityStrategy",
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                sameDocumentIdJustCreated,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },

            // This test injects 429 (Request_Rate_Too_Large) into all regions.
            // Expected outcome is a timeout due to ongoing retries in both operations triggered by
            // availability strategy. Diagnostics should contain two operations.
            new Object[] {
                "429_AllRegions_EagerThresholdAvailabilityStrategy",
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                sameDocumentIdJustCreated,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // GATEWAY
            // -------

            // This test injects Gateway transit timeout into the local region only.
            // Expected outcome is a successful retry by the availability strategy
            new Object[] {
                "GW_408_FirstRegionOnly",
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.GATEWAY,
                sameDocumentIdJustCreated,
                injectGatewayTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_readAfterCreation")
    public void readAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
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
            null,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(FaultInjectionOperationType.READ_ITEM),
            readItemCallback,
            faultInjectionCallback,
            validateStatusCode,
            1,
            ArrayUtils.toArray(validateDiagnosticsContext),
            null,
            null,
            0,
            0,
            false,
            connectionMode);
    }

    @DataProvider(name = "testConfigs_writeAfterCreation")
    public Object[][] testConfigs_writeAfterCreation() {
        final boolean nonIdempotentWriteRetriesEnabled = true;
        final boolean nonIdempotentWriteRetriesDisabled = false;
        final String SECOND_REGION_NAME = writeableRegions.get(1).toLowerCase(Locale.ROOT);
        final Duration NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES = null;

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
                "Create_503_FirstRegionOnly_NoAvailabilityStrategy_WriteRetriesEnabled_WithWriteRetries",
                THREE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // No availability strategy exists - expected outcome is a successful response from the cross-regional
            // issued in the client retry policy
            new Object[] {
                "Create_503_FirstRegionOnly_NoAvailabilityStrategy_WriteRetriesDisabled_withWriteRetries",
                TWO_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from either the cross
            // regional retry in client retry policy of operations against first region - or the hedging
            // against the second region
            new Object[] {
                "Create_503_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },

            // This test injects 503 (Service Unavailable) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from the cross
            // regional retry in client retry policy, but not from the hedging against the second region
            new Object[] {
                "Create_503_FirstRegionOnly_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // This test injects 503 (Service Unavailable) into all regions.
            // Eager availability strategy exists - expected outcome is a 503 - diagnostics should reflect the
            // hedging against second region
            new Object[] {
                "Create_503_AllRegions_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // This test injects 503 (Service Unavailable) into all regions.
            // Default availability strategy exists - expected outcome is a 503 because non-idempotent write retries
            // are disabled - which means no hedging for write operations, but there will be cross regional retry from clientRetryPolicy
            // Same expectation for all write operation types
            new Object[] {
                "Create_503_AllRegions_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },
            new Object[] {
                "Replace_503_AllRegions_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },
            new Object[] {
                "Patch_503_AllRegions_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },
            new Object[] {
                "Delete_503_AllRegions_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },
            new Object[] {
                "UpsertExisting_503_AllRegions_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },
            new Object[] {
                "UpsertNew_503_AllRegions_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertAnotherItemCallback,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },
            new Object[] {
                "Patch_503_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },

            // This test injects 503 (Service Unavailable) into the first region only.
            // Default availability strategy exists - expected outcome is a successful response because non-idempotent
            // write retries are enabled which would allow hedging (or cross regional fail-over) to succeed
            // Same expectation for all write operation types
            new Object[] {
                "Delete_503_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs204NoContent,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            new Object[] {
                "Replace_503_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            new Object[] {
                "UpsertNew_503_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertAnotherItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            new Object[] {
                "UpsertExisting_503_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectServiceUnavailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            new Object[] {
                "Create_500_FirstRegionOnly_NoAvailabilityStrategy_WithRetries",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                ONE_SECOND_DURATION,
                reluctantThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                ONE_SECOND_DURATION,
                reluctantThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                ONE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },

            // 404/1022 into local region only
            // Availability strategy exists - hedging is enabled - no cross regional retry expected (local regional
            // preference results in too many local retries). Should result in successful response form hedging.
            new Object[] {
                "Replace_404-1002_FirstRegionOnly_LocalPreferred_EagerAvailabilityStrategy_WithRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                reluctantThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover
            },

            // 404/1022 into local region only
            // No Availability strategy exists.
            // Expected to get successful response from cross regional retry - region switch is remote allowing the
            // cross regional retry to finish within e2e timeout.
            new Object[] {
                "Create_404-1002_FirstRegionOnly_RemotePreferred_NoAvailabilityStrategy_WithRetries",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                (Consumer<CosmosDiagnosticsContext>)(ctx -> {
                    assertThat(ctx).isNotNull();
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics).isNotNull();
                    assertThat(diagnostics.length).isEqualTo(1);
                    assertThat(diagnostics[0].getClientSideRequestStatistics()).isNotNull();
                    ClientSideRequestStatistics[] clientStats =
                        diagnostics[0].getClientSideRequestStatistics().toArray(new ClientSideRequestStatistics[0]);
                    assertThat(clientStats.length).isEqualTo(1);
                    assertThat(clientStats[0].getResponseStatisticsList()).isNotNull();
                    ClientSideRequestStatistics.StoreResponseStatistics[] storeResponses =
                        clientStats[0].getResponseStatisticsList().toArray(
                            new ClientSideRequestStatistics.StoreResponseStatistics[0]);
                    assertThat(storeResponses.length).isGreaterThanOrEqualTo(2);


                    Instant firstRequestStart = Instant.MAX;
                    Instant firstRequestStartInSecondRegion = Instant.MAX;
                    for (ClientSideRequestStatistics.StoreResponseStatistics currentStoreResponse : storeResponses) {
                        if (currentStoreResponse.getRequestStartTimeUTC().isBefore(firstRequestStart)) {
                            firstRequestStart = currentStoreResponse.getRequestStartTimeUTC();
                        }

                        if (currentStoreResponse.getRegionName().equals(SECOND_REGION_NAME) &&
                            currentStoreResponse.getRequestStartTimeUTC().isBefore(firstRequestStartInSecondRegion)) {

                            firstRequestStartInSecondRegion = currentStoreResponse.getRequestStartTimeUTC();
                        }
                    }

                    logger.info("FirstRequestStart: {}, FirstRequestInSecondReqionStart: {}",
                        firstRequestStart,
                        firstRequestStartInSecondRegion);

                    assertThat(firstRequestStartInSecondRegion.isAfter(firstRequestStart)).isEqualTo(true);
                    assertThat(
                        firstRequestStartInSecondRegion
                            .minus(
                                Configs.getMinRetryTimeInLocalRegionWhenRemoteRegionPreferred().minus(Duration.ofMillis(5)))
                            .isAfter(firstRequestStart)).isEqualTo(true);

                    validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover.accept(ctx);
                })
            },

            // 404/1022 into local region only
            // No availability strategy exists.
            // Expected to get successful response from cross regional retry - region switch is remote allowing the
            // cross regional retry to finish within e2e timeout.
            new Object[] {
                "Create_404-1002_FirstRegionOnly_RemotePreferredWithHighInRegionRetryTime_NoAvailabilityStrategy_WithRetries",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                Duration.ofMillis(600),
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                (Consumer<CosmosDiagnosticsContext>)(ctx -> {
                    assertThat(ctx).isNotNull();
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics).isNotNull();
                    assertThat(diagnostics.length).isEqualTo(1);
                    assertThat(diagnostics[0].getClientSideRequestStatistics()).isNotNull();
                    ClientSideRequestStatistics[] clientStats =
                        diagnostics[0].getClientSideRequestStatistics().toArray(new ClientSideRequestStatistics[0]);
                    assertThat(clientStats.length).isEqualTo(1);
                    assertThat(clientStats[0].getResponseStatisticsList()).isNotNull();
                    ClientSideRequestStatistics.StoreResponseStatistics[] storeResponses =
                        clientStats[0].getResponseStatisticsList().toArray(
                            new ClientSideRequestStatistics.StoreResponseStatistics[0]);
                    assertThat(storeResponses.length).isGreaterThanOrEqualTo(2);


                    Instant firstRequestStart = Instant.MAX;
                    Instant firstRequestStartInSecondRegion = Instant.MAX;
                    for (ClientSideRequestStatistics.StoreResponseStatistics currentStoreResponse : storeResponses) {
                        if (currentStoreResponse.getRequestStartTimeUTC().isBefore(firstRequestStart)) {
                            firstRequestStart = currentStoreResponse.getRequestStartTimeUTC();
                        }

                        if (currentStoreResponse.getRegionName().equals(SECOND_REGION_NAME) &&
                            currentStoreResponse.getRequestStartTimeUTC().isBefore(firstRequestStartInSecondRegion)) {

                            firstRequestStartInSecondRegion = currentStoreResponse.getRequestStartTimeUTC();
                        }
                    }

                    logger.info("FirstRequestStart: {}, FirstRequestInSecondReqionStart: {}",
                        firstRequestStart,
                        firstRequestStartInSecondRegion);

                    assertThat(firstRequestStartInSecondRegion.isAfter(firstRequestStart)).isEqualTo(true);
                    assertThat(
                        firstRequestStartInSecondRegion
                            .minus(Duration.ofMillis(600-5))
                            .isAfter(firstRequestStart)).isEqualTo(true);

                    validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegionButWithRegionalFailover.accept(ctx);
                })
            },

            // 404/1022 into local region only
            // No availability strategy exists.
            // Expected to get 408 because min. in-region wait time is larger than e2e timeout.
            new Object[] {
                "Create_404-1002_FirstRegionOnly_RemotePreferredWithTooHighInRegionRetryTime_NoAvailabilityStrategy_408",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                Duration.ofMillis(1100),
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                (Consumer<CosmosDiagnosticsContext>)(ctx -> {
                    assertThat(ctx).isNotNull();
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics).isNotNull();
                    assertThat(diagnostics.length).isEqualTo(1);
                    assertThat(diagnostics[0].getClientSideRequestStatistics()).isNotNull();
                    ClientSideRequestStatistics[] clientStats =
                        diagnostics[0].getClientSideRequestStatistics().toArray(new ClientSideRequestStatistics[0]);
                    assertThat(clientStats.length).isEqualTo(1);
                    assertThat(clientStats[0].getResponseStatisticsList()).isNotNull();
                    ClientSideRequestStatistics.StoreResponseStatistics[] storeResponses =
                        clientStats[0].getResponseStatisticsList().toArray(
                            new ClientSideRequestStatistics.StoreResponseStatistics[0]);

                    // retry should not have been issued yet. With just single retry
                    // the back-off time will be expanded to the minInRegionRetryWaitTime
                    assertThat(storeResponses.length).isEqualTo(1);

                    validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion.accept(ctx);
                })
            },

            // 404/1022 into local region only
            // Availability strategy exists, hedging is enabled. Region switch is local - meaning the local retries
            // will take so long, that the cross-regional retry in the client retry policy is not applicable.
            // Successful response expected from hedging. Diagnostics should have data for both operations.
            new Object[] {
                "Create_404-1002_FirstRegionOnly_LocalPreferred_EagerAvailabilityStrategy_WithRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
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
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteNonExistingItemCallback,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIsLegitNotFound,
                // no hedging even with availability strategy because nonIdempotentWrites are disabled
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // No availability strategy exists - expected outcome is request fail due to timeout
            // SDK does not do cross region retry for 429 by default
            new Object[] {
                "Create_429_FirstRegionOnly_NoAvailabilityStrategy_WriteRetriesEnabled_WithWriteRetries",
                THREE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from the hedging
            // against the second region
            new Object[] {
                "Create_429_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists but nonIdempotentWriteRetriesDisabled - expected outcome is request will timed out
            new Object[] {
                "Create_429_FirstRegionOnly_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into all regions.
            // Eager availability strategy exists - expected outcome is request timed out
            new Object[] {
                "Create_429_AllRegions_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // No availability strategy exists - expected outcome is request timed out
            // SDK does not do cross region retry for 429 by default
            new Object[] {
                "Upsert_429_FirstRegionOnly_NoAvailabilityStrategy_WriteRetriesEnabled_WithWriteRetries",
                THREE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from the hedging
            // against the second region
            new Object[] {
                "Upsert_429_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists but nonIdempotentWriteRetriesDisabled - expected outcome is request will timed out
            new Object[] {
                "Upsert_429_FirstRegionOnly_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 429 (Request_Rate_Too_Large) into all regions.
            // Eager availability strategy exists - expected outcome is request timed out
            new Object[] {
                "Upsert_429_AllRegions_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.UPSERT_ITEM,
                upsertExistingItemCallback,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // No availability strategy exists - 429 will retry up to 30s or max 9 retries.
            // In the fault injection, we have injected around 500ms delay, so with 3s e2e timeout, the expected outcome is request will time out
            // SDK does not do cross region retry for 429 by default
            new Object[] {
                "Delete_429_FirstRegionOnly_NoAvailabilityStrategy_WriteRetriesEnabled_WithWriteRetries",
                THREE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from the hedging
            // against the second region
            new Object[] {
                "Delete_429_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs204NoContent,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists but nonIdempotentWriteRetriesDisabled - expected outcome is request will timed out
            new Object[] {
                "Delete_429_FirstRegionOnly_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },

            // This test injects 429 (Request_Rate_Too_Large) into all regions.
            // Eager availability strategy exists - expected outcome is request timed out
            new Object[] {
                "Delete_429_AllRegions_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.DELETE_ITEM,
                deleteItemCallback,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // No availability strategy exists - 429 will retry up to 30s or max 9 retries.
            // In the fault injection, we have injected around 500ms delay, so with 3s e2e timeout, the expected outcome is request will time out
            new Object[] {
                "Replace_429_FirstRegionOnly_NoAvailabilityStrategy_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from the hedging
            // against the second region
            new Object[] {
                "Replace_429_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists but nonIdempotentWriteRetriesDisabled - expected outcome is request will timed out
            new Object[] {
                "Replace_429_FirstRegionOnly_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into all regions.
            // Eager availability strategy exists - expected outcome is request timed out
            new Object[] {
                "Replace_429_AllRegions_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.REPLACE_ITEM,
                replaceItemCallback,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // No availability strategy exists - 429 will retry up to 30s or max 9 retries.
            // In the fault injection, we have injected around 500ms delay, so with 3s e2e timeout, the expected outcome is request will time out
            // SDK does not do cross region retry for 429 by default
            new Object[] {
                "Patch_429_FirstRegionOnly_NoAvailabilityStrategy_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from the hedging
            // against the second region
            new Object[] {
                "Patch_429_FirstRegionOnly_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into the local region only.
            // Default availability strategy exists but nonIdempotentWriteRetriesDisabled - expected outcome is request will timed out
            new Object[] {
                "Patch_429_FirstRegionOnly_WriteRetriesDisabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesDisabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForOnlyFirstRegion
            },
            // This test injects 429 (Request_Rate_Too_Large) into all regions.
            // Eager availability strategy exists - expected outcome is request timed out
            new Object[] {
                "Patch_429_AllRegions_WriteRetriesEnabled_WithWriteRetries",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.PATCH_ITEM,
                patchItemCallback,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                validateDiagnosticsContextHasDiagnosticsForAllRegions
            },

            // GATEWAY
            // -------

            // This test injects 503 (Service Unavailable) into the local region only.
            // Default availability strategy exists - expected outcome is successful response from either the cross
            // regional retry in client retry policy of operations against first region - or the hedging
            // against the second region
            new Object[] {
                "GW_Create_GW408_FirstRegionOnly_WithWriteRetries",
                THREE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.GATEWAY,
                NO_CUSTOM_MIN_RETRY_TIME_IN_REGION_FOR_WRITES,
                nonIdempotentWriteRetriesEnabled,
                FaultInjectionOperationType.CREATE_ITEM,
                createAnotherItemCallback,
                injectGatewayTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs201Created,
                validateDiagnosticsContextHasDiagnosticsForOneOrTwoRegionsButAlwaysContactedSecondRegion
            },
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_writeAfterCreation")
    public void writeAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Duration customMinRetryTimeInLocalRegion,
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
            customMinRetryTimeInLocalRegion,
            nonIdempotentWriteRetriesEnabled,
            ArrayUtils.toArray(faultInjectionOperationType),
            actionAfterInitialCreation,
            faultInjectionCallback,
            validateStatusCode,
            1,
            ArrayUtils.toArray(validateDiagnosticsContext),
            null,
            null,
            0,
            0,
            false,
            connectionMode);
    }

    private CosmosResponseWrapper queryReturnsTotalRecordCountCore(
        String query,
        ItemOperationInvocationParameters params,
        int requestedPageSize
    ) {
        return queryReturnsTotalRecordCountCore(query, params, requestedPageSize, false);
    }

    private CosmosResponseWrapper queryReturnsTotalRecordCountCore(
        String query,
        ItemOperationInvocationParameters params,
        int requestedPageSize,
        boolean enforceEmptyPages
    ) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        if (enforceEmptyPages) {
            ImplementationBridgeHelpers
                .CosmosQueryRequestOptionsHelper
                .getCosmosQueryRequestOptionsAccessor()
                .setAllowEmptyPages(queryOptions, true);
        }

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
            queryPagedFlux.byPage(requestedPageSize).collectList().block();

        ArrayList<CosmosDiagnosticsContext> foundCtxs = new ArrayList<>();

        if (returnedPages.isEmpty()) {
            return new CosmosResponseWrapper(
                null,
                HttpConstants.StatusCodes.NOTFOUND,
                NO_QUERY_PAGE_SUB_STATUS_CODE,
                null);
        }

        long totalRecordCount = 0L;
        for (FeedResponse<ObjectNode> page : returnedPages) {
            if (page.getCosmosDiagnostics() != null) {
                System.out.println("lalalalaal, " + page.getCosmosDiagnostics());
                foundCtxs.add(page.getCosmosDiagnostics().getDiagnosticsContext());
            } else {
                System.out.println("oh no, how come there is no diagnostics for the page");
                foundCtxs.add(null);
            }

            if (page.getResults() != null && page.getResults().size() > 0) {
                totalRecordCount += page.getResults().size();
            }
        }

        return new CosmosResponseWrapper(
            foundCtxs.toArray(new CosmosDiagnosticsContext[0]),
            HttpConstants.StatusCodes.OK,
            HttpConstants.SubStatusCodes.UNKNOWN,
            totalRecordCount);
    }

    @DataProvider(name = "testConfigs_queryAfterCreation")
    public Object[][] testConfigs_queryAfterCreation() {

        final int ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE = 10;
        final int NO_OTHER_DOCS_WITH_SAME_PK = 0;
        final int NO_OTHER_DOCS_WITH_SAME_ID = 0;
        final int ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION = PHYSICAL_PARTITION_COUNT * 10;
        final int SINGLE_REGION = 1;
        final int TWO_REGIONS = 2;

        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> injectReadSessionNotAvailableIntoFirstRegionOnlyForSinglePartition =
            (c, operationType) -> injectReadSessionNotAvailableError(c, this.getFirstRegion(), operationType, c.getFeedRanges().block().get(0));

        BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryReturnsTotalRecordCountWithDefaultPageSize = (query, params) ->
            queryReturnsTotalRecordCountCore(query, params, 100);

        BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryReturnsTotalRecordCountWithPageSizeOne = (query, params) ->
            queryReturnsTotalRecordCountCore(query, params, 1);

        BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryReturnsTotalRecordCountWithPageSizeOneAndEmptyPagesEnabled = (query, params) ->
            queryReturnsTotalRecordCountCore(query, params, 1, true);

        BiConsumer<CosmosResponseWrapper, Long> validateExpectedRecordCount = (response, expectedRecordCount) -> {
            if (expectedRecordCount != null) {
                assertThat(response).isNotNull();
                assertThat(response.getTotalRecordCount()).isNotNull();
                assertThat(response.getTotalRecordCount()).isEqualTo(expectedRecordCount);
            }
        };

        Consumer<CosmosResponseWrapper> validateEmptyResults =
            (response) -> validateExpectedRecordCount.accept(response, 0L);

        Consumer<CosmosResponseWrapper> validateExactlyOneRecordReturned =
            (response) -> validateExpectedRecordCount.accept(response, 1L);

        Consumer<CosmosResponseWrapper> validateAllRecordsSameIdReturned =
            (response) -> validateExpectedRecordCount.accept(
                response,
                1L + ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION);

        Consumer<CosmosResponseWrapper> validateAllRecordsSamePartitionReturned =
            (response) -> validateExpectedRecordCount.accept(
                response,
                1L + ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE);

        Function<ItemOperationInvocationParameters, String> singlePartitionQueryGenerator = (params) ->
            "SELECT * FROM c WHERE c.mypk = '"
                +  params.idAndPkValuePair.getRight()
                + "'";

        Function<ItemOperationInvocationParameters, String> singlePartitionWithAggregatesAndOrderByQueryGenerator = (params) ->
            "SELECT DISTINCT c.id FROM c WHERE c.mypk = '"
                +  params.idAndPkValuePair.getRight()
                + "' ORDER BY c.id";

        Function<ItemOperationInvocationParameters, String> singlePartitionEmptyResultQueryGenerator = (params) ->
            "SELECT * FROM c WHERE c.mypk = '"
                +  params.idAndPkValuePair.getRight()
                + "' and c.id = 'NotExistingId'";

        Function<ItemOperationInvocationParameters, String> crossPartitionQueryGenerator = (params) ->
            "SELECT * FROM c WHERE CONTAINS (c.id, '"
                + params.idAndPkValuePair.getLeft()
                + "')";

        Function<ItemOperationInvocationParameters, String> crossPartitionWithAggregatesAndOrderByQueryGenerator = (params) ->
            "SELECT DISTINCT c.id FROM c WHERE CONTAINS (c.id, '"
                + params.idAndPkValuePair.getLeft()
                + "')";

        Function<ItemOperationInvocationParameters, String> crossPartitionEmptyResultQueryGenerator = (params) ->
            "SELECT * FROM c WHERE CONTAINS (c.id, 'NotExistingId')";

        BiConsumer<CosmosDiagnosticsContext, Integer> validateCtxRegions =
            (ctx, expectedNumberOfRegionsContacted) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getContactedRegionNames().size()).isEqualTo(expectedNumberOfRegionsContacted);
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxQueryPlan =
            (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    // Query Plan + at least one query response

                    assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(1);
                    assertThat(diagnostics[0]).isNotNull();
                    assertThat(diagnostics[0].getFeedResponseDiagnostics()).isNull();
                    assertThat(diagnostics[0].getClientSideRequestStatistics()).isNotNull();
                    assertThat(diagnostics[0].getClientSideRequestStatistics().size()).isEqualTo(1);
                    ClientSideRequestStatistics[] clientStats =
                        diagnostics[0].getClientSideRequestStatistics().toArray(new ClientSideRequestStatistics[0]);
                    assertThat(clientStats.length).isEqualTo(1);
                    assertThat(clientStats[0]).isNotNull();
                    assertThat(clientStats[0].getGatewayStatisticsList()).isNotNull();
                    ClientSideRequestStatistics.GatewayStatistics[] gwStats =
                        clientStats[0].getGatewayStatisticsList().toArray(new ClientSideRequestStatistics.GatewayStatistics[0]);
                    assertThat(gwStats.length).isGreaterThanOrEqualTo(1);
                    assertThat(gwStats[0]).isNotNull();
                    assertThat(gwStats[0].getOperationType()).isEqualTo(OperationType.QueryPlan);
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxOnlyFeedResponsesExceptQueryPlan =
            (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    // Query Plan + at least one query response

                    assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(1);

                    // Validate that at most one FeedResponse has query plan diagnostics
                    CosmosDiagnostics[] feedResponseDiagnosticsWithQueryPlan = Arrays.stream(diagnostics)
                          .filter(d -> d.getFeedResponseDiagnostics() != null
                              && d.getFeedResponseDiagnostics().getQueryPlanDiagnosticsContext() != null)
                        .toArray(CosmosDiagnostics[]::new);

                    assertThat(feedResponseDiagnosticsWithQueryPlan.length).isLessThanOrEqualTo(1);

                    int start = 0;
                    if (diagnostics[0].getFeedResponseDiagnostics() == null) {
                        // skip query plan
                        start = 1;
                    }

                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(start + 1);

                    for (int i = start; i < diagnostics.length; i++) {
                        CosmosDiagnostics currentDiagnostics = diagnostics[i];
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getQueryMetricsMap()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics().size()).isGreaterThanOrEqualTo(1);
                    }
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxSingleRegion =
            (ctx) -> validateCtxRegions.accept(ctx, SINGLE_REGION);

        Consumer<CosmosDiagnosticsContext> validateCtxTwoRegions =
            (ctx) -> validateCtxRegions.accept(ctx, TWO_REGIONS);

        Consumer<CosmosDiagnosticsContext> validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse = (ctx) -> {
            CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
            assertThat(diagnostics.length).isEqualTo(3);
            CosmosDiagnostics firstRegionDiagnostics = diagnostics[1];
            assertThat(firstRegionDiagnostics.getFeedResponseDiagnostics()).isNull();
            assertThat(firstRegionDiagnostics.getContactedRegionNames()).isNotNull();
            assertThat(firstRegionDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
            assertThat(firstRegionDiagnostics.getContactedRegionNames().iterator().next())
                .isEqualTo(this.writeableRegions.get(0).toLowerCase(Locale.ROOT));

            CosmosDiagnostics secondRegionDiagnostics = diagnostics[2];
            assertThat(secondRegionDiagnostics.getFeedResponseDiagnostics()).isNotNull();
            assertThat(secondRegionDiagnostics.getContactedRegionNames()).isNotNull();
            assertThat(secondRegionDiagnostics.getContactedRegionNames().size()).isEqualTo(1);
            assertThat(secondRegionDiagnostics.getContactedRegionNames().iterator().next())
                .isEqualTo(this.writeableRegions.get(1).toLowerCase(Locale.ROOT));
            assertThat(secondRegionDiagnostics.getFeedResponseDiagnostics().getQueryPlanDiagnosticsContext()).isNotNull();
            assertThat(secondRegionDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
            assertThat(secondRegionDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics().size()).isEqualTo(1);
        };

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
            //    Expected number of DiagnosticsContext instances - there will be one per page returned form the PagedFlux
            //    Diagnostics context validation callback applied to the first DiagnosticsContext instance
            //    Diagnostics context validation callback applied to the all other DiagnosticsContext instances
            //    Consumer<CosmosResponseWrapper> - callback to validate the response (status codes, total records returned etc.)
            //    numberOfOtherDocumentsWithSameId - number of other documents to be created with the same id-value
            //        (but different pk-value). Mostly used to ensure cross-partition queries have to
            //        touch more than one partition.
            //    numberOfOtherDocumentsWithSamePk - number of documents to be created with the same pk-value
            //        (but different id-value). Mostly used to force a certain number of documents being
            //        returned for single partition queries.
            // },

            // Plain vanilla single partition query. No failure injection and all records will fit into a single page
            new Object[] {
                "DefaultPageSize_SinglePartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                null,
                validateExactlyOneRecordReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple cross partition query. No failure injection and all records returned for a partition will fit
            // into a single page. But there will be one page per partition
            new Object[] {
                "DefaultPageSize_CrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple single partition query. No failure injection but page size set to 1 - so, multiple pages will
            // be returned from the PagedFlux - for each document one page - and the expectation is that there
            // will be as many CosmosDiagnosticsContext instances as pages.
            new Object[] {
                "PageSizeOne_SinglePartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOne,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1 + ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                validateAllRecordsSamePartitionReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // Simple cross partition query. No failure injection but page size set to 1 - so, multiple pages will
            // be returned from the PagedFlux per physical partition - for each document one page - and the
            // expectation is that there will be as many CosmosDiagnosticsContext instances as pages.
            new Object[] {
                "PageSizeOne_CrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOne,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1 + ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple single partition query intended to not return any results. No failure injection and only
            // one empty page expected - with exactly one CosmosDiagnostics instance
            new Object[] {
                "EmptyResults_SinglePartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                singlePartitionEmptyResultQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOne,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan
                ),
                null,
                validateEmptyResults,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple cross partition query intended to not return any results. No failures injected.
            // Empty pages should be skipped (except for the last one) - so, exactly one empty page expected -
            // with exactly one CosmosDiagnostics instance - even when this is a cross-partition query touching all
            // partitions
            new Object[] {
                "EmptyResults_CrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionEmptyResultQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOne,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                // empty pages are skipped except for the last one
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[1].getClientSideRequestStatistics().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                    }
                ),
                null,
                validateEmptyResults,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple cross partition query intended to not return any results. No failures injected.
            // Empty pages should be returned - so, exactly one page per partition expected -
            // with exactly one CosmosDiagnostics instance (plus query plan on very first one)
            new Object[] {
                "EmptyResults_EnableEmptyPageRetrieval_CrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionEmptyResultQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOneAndEmptyPagesEnabled,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                // empty pages are bubbled up
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[1].getClientSideRequestStatistics().size())
                            .isEqualTo(1);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(1);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[0].getClientSideRequestStatistics().size())
                            .isEqualTo(1);
                        assertThat(diagnostics[0].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(1);
                    }
                ),
                validateEmptyResults,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple cross partition query intended to not return any results except on one partition.
            // No failures injected. Empty pages of all but one partition will be skipped, but
            // query metrics and client side request statistics are captured in the merged diagnostics.
            new Object[] {
                "AllButOnePartitionEmptyResults_CrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[1].getClientSideRequestStatistics().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Single partition query with DISTINCT and ORDER BY. No failures injected
            // Expect to get as many pages and diagnostics contexts as there are documents for this PK-value
            new Object[] {
                "AggregatesAndOrderBy_PageSizeOne_SinglePartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                singlePartitionWithAggregatesAndOrderByQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOne,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1 + ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                validateAllRecordsSamePartitionReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // Single partition query with DISTINCT and ORDER BY. No failures injected
            // Only a single document matches the where condition - but this is a cross partition query. Because
            // the single page returned in the CosmosPagedFlux had to peek into all physical partitions to be
            // able to achieve global ordering in the query pipeline a single CosmosDiagnosticsContext instance
            // is returned - but with query metrics and client request statistics for all partitions
            new Object[] {
                "AggregatesAndOrderBy_PageSizeOne_CrossPartitionSingleRecord_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionWithAggregatesAndOrderByQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOne,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[1].getClientSideRequestStatistics().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_PK,
                NO_OTHER_DOCS_WITH_SAME_ID
            },

            // Cross partition query with DISTINCT and ORDER BY. Documents from all partitions meet the where
            // condition but the distinct id value is identical - so, to the application only a single record is
            // returned. Because the page size is 1 we expect as many pages / CosmosDiagnosticsContext instances
            // as there are documents with the same id-value.
            new Object[] {
                "AggregatesAndOrderBy_PageSizeOne_CrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionWithAggregatesAndOrderByQueryGenerator,
                queryReturnsTotalRecordCountWithPageSizeOne,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1 + ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan
                ),
                validateExactlyOneRecordReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Cross partition query with DISTINCT and ORDER BY. Documents from all partitions meet the where
            // condition but the distinct id value is identical - so, to the application only a single record is
            // returned. Because the page size is 1 we expect as many pages / CosmosDiagnosticsContext instances
            // as there are documents with the same id-value.
            new Object[] {
                "AggregatesAndOrderBy_DefaultPageSize_CrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionWithAggregatesAndOrderByQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[1].getClientSideRequestStatistics().size())
                            .isEqualTo(1);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(1);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[0].getClientSideRequestStatistics().size())
                            .isEqualTo(1);
                        assertThat(diagnostics[0].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(1);
                    }
                ),
                validateExactlyOneRecordReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Cross partition query with DISTINCT and ORDER BY. Single document meets the where
            // condition, but queries against all partitions need to be executed. Expect to see a single
            // page and CosmosDiagnosticsContext - but including three request statistics and query metrics.
            new Object[] {
                "AggregatesAndOrderBy_DefaultPageSize_SingleRecordCrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                crossPartitionWithAggregatesAndOrderByQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[1].getClientSideRequestStatistics().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap().size())
                            .isEqualTo(PHYSICAL_PARTITION_COUNT);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple single partition query - 404/1002 injected into all partition of the first region
            // RegionSwitchHint is local - with eager availability strategy - so, the expectation is that the
            // hedging will provide a successful response. There should only be a single CosmosDiagnosticsContext
            // (and page) - but it should have three CosmosDiagnostics instances - first for query plan, second for
            // the attempt in the first region and third one for hedging returning successful response.
            new Object[] {
                "DefaultPageSize_SinglePartition_404-1002_OnlyFirstRegion_LocalPreferred_EagerAvailabilityStrategy",
                Duration.ofSeconds(10),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(3);

                        // Ensure first FeedResponse CosmoDiagnostics has at least requests to first region
                        // (possibly also fail-over to secondary region)
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isGreaterThanOrEqualTo(1);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);

                        // Ensure second FeedResponse CosmoDiagnostics has only requests to second region
                        assertThat(diagnostics[2].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[2].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple cross partition query - 404/1002 injected into all partition of the first region
            // RegionSwitchHint is remote - with reluctant availability strategy - so, the expectation is that the
            // retry on the first region will provide a successful response and no hedging is happening.
            // There should be one CosmosDiagnosticsContext (and page) per partition - each should only have
            // a single CosmosDiagnostics instance contacting both regions.
            new Object[] {
                "DefaultPageSize_CrossPartition_404-1002_OnlyFirstRegion_AllPartitions_RemotePreferred_ReluctantAvailabilityStrategy",
                THREE_SECOND_DURATION,
                reluctantThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                crossPartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(2);

                        // Ensure fail-over happened
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isEqualTo(2);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(1);

                        // Ensure fail-over happened
                        assertThat(diagnostics[0].getContactedRegionNames().size()).isEqualTo(2);
                        assertThat(diagnostics[0].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);
                        assertThat(diagnostics[0].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple cross partition query - 404/1002 injected into only a single partition of the first region
            // RegionSwitchHint is remote - with reluctant availability strategy - so, the expectation is that the
            // retry on the first region will provide a successful response for the one partition and no hedging is
            // happening. There should be one CosmosDiagnosticsContext (and page) per partition - each should only have
            // a single CosmosDiagnostics instance contacting both regions.
            new Object[] {
                "DefaultPageSize_CrossPartition_404-1002_OnlyFirstRegion_SinglePartition_RemotePreferred_ReluctantAvailabilityStrategy",
                ONE_SECOND_DURATION,
                reluctantThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                crossPartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectReadSessionNotAvailableIntoFirstRegionOnlyForSinglePartition,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(2);

                        // Ensure fail-over happened
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isEqualTo(2);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(1);

                        // Ensure no fail-over happened
                        assertThat(diagnostics[0].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[0].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple single partition query - 404/1002 injected into all partition of the first region
            // RegionSwitchHint is local - with eager availability strategy - so, the expectation is that the
            // hedging will provide a successful response. There should only be a single CosmosDiagnosticsContext
            // (and page) - but it should have three CosmosDiagnostics instances - first for query plan, second for
            // the attempt in the first region and third one for hedging returning successful response.
            new Object[] {
                "DefaultPageSize_SinglePartition_503_AllRegions_EagerAvailabilityStrategy",
                Duration.ofSeconds(10),
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectServiceUnavailableIntoAllRegions,
                validateStatusCodeIsServiceUnavailable,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(3);

                        // Ensure first FeedResponse reaches both regions since Clinet Retry
                        // policy should kick in and retry in remote region
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isEqualTo(2);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);

                        // Ensure second FeedResponse CosmoDiagnostics has only requests to second region
                        assertThat(diagnostics[2].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[2].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                null,
                null,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple single partition query. Gateway timeout for query plan retrieval in first region injected.
            // This test case validates that the availability strategy and hedging is also applied for the
            // query plan request. The expectation is that the query plan request in the first region won't finish,
            // the query plan will then be retrieved from the second region but the actual query is executed against the
            // first region.
            new Object[] {
                "DefaultPageSize_SinglePartition_QueryPLanHighLatency_EagerAvailabilityStrategy",
                THREE_SECOND_DURATION,
                reluctantThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectQueryPlanTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxQueryPlan,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isGreaterThanOrEqualTo(3);

                        // Ensure that the query plan has been retrieved from the second region
                        assertThat(diagnostics[0].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[0].getContactedRegionNames().iterator().next()).isEqualTo(FIRST_REGION_NAME);
                        assertThat(diagnostics[0].getClientSideRequestStatistics()).isNotNull();
                        assertThat(diagnostics[0].getClientSideRequestStatistics().size()).isGreaterThanOrEqualTo(1);
                        ClientSideRequestStatistics requestStats = diagnostics[0].getClientSideRequestStatistics().iterator().next();
                        assertThat(requestStats.getGatewayStatisticsList()).isNotNull();
                        assertThat(requestStats.getGatewayStatisticsList().size()).isGreaterThanOrEqualTo(1);
                        assertThat(requestStats.getGatewayStatisticsList().iterator().next().getOperationType()).isEqualTo(OperationType.QueryPlan);
                        assertThat(requestStats.getGatewayStatisticsList().iterator().next().getStatusCode()).isEqualTo(408);

                        // Ensure that the query plan has been retrieved from the second region
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[1].getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                        assertThat(diagnostics[1].getClientSideRequestStatistics()).isNotNull();
                        assertThat(diagnostics[1].getClientSideRequestStatistics().size()).isGreaterThanOrEqualTo(1);
                        requestStats = diagnostics[1].getClientSideRequestStatistics().iterator().next();
                        assertThat(requestStats.getGatewayStatisticsList()).isNotNull();
                        assertThat(requestStats.getGatewayStatisticsList().size()).isGreaterThanOrEqualTo(1);
                        assertThat(requestStats.getGatewayStatisticsList().iterator().next().getOperationType()).isEqualTo(OperationType.QueryPlan);
                        assertThat(requestStats.getGatewayStatisticsList().iterator().next().getStatusCode()).isEqualTo(200);


                        // There possibly is an incomplete diagnostics for the failed query plan retrieval in the first region
                        // Last Diagnostics should be for processed request against the first region with the
                        // query plan retrieved from the second region
                        boolean found = false;
                        for (int i = 2; i < diagnostics.length; i++) {
                            if (diagnostics[i].getFeedResponseDiagnostics() != null &&
                                diagnostics[i].getFeedResponseDiagnostics().getQueryMetricsMap() != null) {

                                found = true;
                                assertThat(diagnostics[i].getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                                assertThat(diagnostics[i].getFeedResponseDiagnostics().getClientSideRequestStatistics().size()).isGreaterThanOrEqualTo(1);
                                assertThat(diagnostics[i].getContactedRegionNames().size()).isEqualTo(1);
                                assertThat(diagnostics[i].getContactedRegionNames().iterator().next()).isEqualTo(FIRST_REGION_NAME);
                            }
                        }

                        assertThat(found).isEqualTo(true);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
            // Simple single partition query - 429/3200 injected into all partition of the first region
            // Eager availability strategy - so, the expectation is that the
            // hedging will provide a successful response. There should only be a single CosmosDiagnosticsContext
            // (and page) - but it should have three CosmosDiagnostics instances - first for query plan, second for
            // the attempt in the first region and third one for hedging returning successful response.
            new Object[] {
                "DefaultPageSize_SinglePartition_429-3200_OnlyFirstRegion_LocalPreferred_EagerAvailabilityStrategy",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(3);

                        // Ensure first FeedResponse CosmoDiagnostics has at least requests to first region
                        // (possibly also fail-over to secondary region)
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isGreaterThanOrEqualTo(1);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);

                        // Ensure second FeedResponse CosmoDiagnostics has only requests to second region
                        assertThat(diagnostics[2].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[2].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
            // Simple single partition query - 429/3200 injected into all regions
            // Eager availability strategy - the expectation is that even with hedging, the request will time out
            new Object[] {
                "DefaultPageSize_SinglePartition_429-3200_AllRegions_LocalPreferred_EagerAvailabilityStrategy",
                TWO_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(3);

                        // Ensure first FeedResponse CosmoDiagnostics has at least requests to first region
                        // (possibly also fail-over to secondary region)
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isGreaterThanOrEqualTo(1);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);

                        // Ensure second FeedResponse CosmoDiagnostics has only requests to second region
                        assertThat(diagnostics[2].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[2].getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .isEqualTo(true);
                    }
                ),
                null,
                null,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // Simple single partition query - 429/3200 injected into first region only
            // no availability strategy - the expectation is that no hedging will happen, the request will time out
            new Object[] {
                "DefaultPageSize_SinglePartition_429-3200_AllRegions_LocalPreferred_noAvailabilityStrategy",
                TWO_SECOND_DURATION,
                noAvailabilityStrategy,
                CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED,
                ConnectionMode.DIRECT,
                singlePartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectRequestRateTooLargeIntoAllRegions,
                validateStatusCodeIsOperationCancelled,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    (ctx) -> {
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics.length).isEqualTo(2);

                        // Ensure first FeedResponse CosmoDiagnostics has at least requests to first region
                        // (possibly also fail-over to secondary region)
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[1].getContactedRegionNames().contains(FIRST_REGION_NAME))
                            .isEqualTo(true);
                        assertThat(diagnostics[1].clientSideRequestStatistics().getResponseStatisticsList().size()).isGreaterThan(1);
                    }
                ),
                null,
                null,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
            // GATEWAY MODE
            // ------------

            // Simple cross partition query - 404/1002 injected into all partition of the first region
            // RegionSwitchHint is remote - with reluctant availability strategy - so, the expectation is that the
            // retry on the first region will provide a successful response and no hedging is happening.
            // There should be one CosmosDiagnosticsContext (and page) per partition - each should only have
            // a single CosmosDiagnostics instance contacting both regions.
            new Object[] {
                "GW_DefaultPageSize_CrossPartition_GW408_EagerAvailabilityStrategy",
                THREE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.GATEWAY,
                crossPartitionQueryGenerator,
                queryReturnsTotalRecordCountWithDefaultPageSize,
                injectGatewayTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxTwoRegions, // query plan 1st region, all queries 2nd region
                    validateCtxQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);

                        // Diagnostics of query attempt in first region not even available yet
                        assertThat(diagnostics.length).isEqualTo(2);

                        // query plan on first region
                        assertThat(diagnostics[0].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[0].getContactedRegionNames().iterator().next()).isEqualTo(FIRST_REGION_NAME);
                    },
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[1].getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics()).isNotNull();
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap()).isNotNull();
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                        ClientSideRequestStatistics[] clientStats =
                            diagnostics[1]
                                .getFeedResponseDiagnostics()
                                .getClientSideRequestStatistics()
                                .toArray(new ClientSideRequestStatistics[0]);
                        assertThat(clientStats.length).isEqualTo(1);
                        for (int i = 0; i < clientStats.length; i++) {
                            assertThat(clientStats[i].getContactedRegionNames()).isNotNull();
                            assertThat(clientStats[i].getContactedRegionNames().size()).isEqualTo(1);
                            assertThat(clientStats[i].getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                            assertThat(clientStats[i].getGatewayStatisticsList()).isNotNull();
                            assertThat(clientStats[i].getResponseStatisticsList()).isNotNull();
                            assertThat(clientStats[i].getResponseStatisticsList().size()).isEqualTo(0);
                        }
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                        assertThat(diagnostics[0].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[0].getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                        assertThat(diagnostics[0].getFeedResponseDiagnostics()).isNotNull();
                        assertThat(diagnostics[0].getFeedResponseDiagnostics().getQueryMetricsMap()).isNotNull();
                        assertThat(diagnostics[0].getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                        ClientSideRequestStatistics[] clientStats =
                            diagnostics[0]
                                .getFeedResponseDiagnostics()
                                .getClientSideRequestStatistics()
                                .toArray(new ClientSideRequestStatistics[0]);
                        assertThat(clientStats.length).isEqualTo(1);
                        for (int i = 0; i < clientStats.length; i++) {
                            assertThat(clientStats[i].getContactedRegionNames()).isNotNull();
                            assertThat(clientStats[i].getContactedRegionNames().size()).isEqualTo(1);
                            assertThat(clientStats[i].getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                            assertThat(clientStats[i].getGatewayStatisticsList()).isNotNull();
                            assertThat(clientStats[i].getResponseStatisticsList()).isNotNull();
                            assertThat(clientStats[i].getResponseStatisticsList().size()).isEqualTo(0);
                        }
                    }
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            }
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_queryAfterCreation")
    public void queryAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Function<ItemOperationInvocationParameters, String> queryGenerator,
        BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryExecution,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> responseValidator,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk) {

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            null,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(FaultInjectionOperationType.QUERY_ITEM),
            (params) -> queryExecution.apply(queryGenerator.apply(params), params),
            faultInjectionCallback,
            validateStatusCode,
            expectedDiagnosticsContextCount,
            firstDiagnosticsContextValidations,
            otherDiagnosticsContextValidations,
            responseValidator,
            numberOfOtherDocumentsWithSameId,
            numberOfOtherDocumentsWithSamePk,
            false,
            connectionMode);
    }

    private CosmosResponseWrapper readManyCore(
        List<Pair<String, String>> tuples,
        ItemOperationInvocationParameters params
    ) {
        CosmosReadManyRequestOptions queryOptions = new CosmosReadManyRequestOptions();

        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = ImplementationBridgeHelpers
            .CosmosItemRequestOptionsHelper
            .getCosmosItemRequestOptionsAccessor()
            .getEndToEndOperationLatencyPolicyConfig(params.options);
        queryOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        List<CosmosItemIdentity> itemIdentities = tuples
            .stream()
            .map(idAndPkPair ->
                new CosmosItemIdentity(new PartitionKey(idAndPkPair.getRight()), idAndPkPair.getLeft()))
            .collect(Collectors.toList());

        FeedResponse<ObjectNode> response = params.container.readMany(
            itemIdentities,
            queryOptions,
            ObjectNode.class
        ).block();

        List<FeedResponse<ObjectNode>> returnedPages = Collections.singletonList(response);

        ArrayList<CosmosDiagnosticsContext> foundCtxs = new ArrayList<>();

        long totalRecordCount = 0L;
        for (FeedResponse<ObjectNode> page : returnedPages) {
            if (page.getCosmosDiagnostics() != null) {
                foundCtxs.add(page.getCosmosDiagnostics().getDiagnosticsContext());
            } else {
                foundCtxs.add(null);
            }

            if (page.getResults() != null && page.getResults().size() > 0) {
                totalRecordCount += page.getResults().size();
            }
        }

        return new CosmosResponseWrapper(
            foundCtxs.toArray(new CosmosDiagnosticsContext[0]),
            HttpConstants.StatusCodes.OK,
            HttpConstants.SubStatusCodes.UNKNOWN,
            totalRecordCount);
    }

    @DataProvider(name = "testConfigs_readManyAfterCreation")
    public Object[][] testConfigs_readManyAfterCreation() {

        final int ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE = 10;
        final int NO_OTHER_DOCS_WITH_SAME_PK = 0;
        final int NO_OTHER_DOCS_WITH_SAME_ID = 0;
        final int ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION = PHYSICAL_PARTITION_COUNT * 10;
        final int SINGLE_REGION = 1;
        final int TWO_REGIONS = 2;

        BiConsumer<CosmosResponseWrapper, Long>  validateExpectedRecordCount = (response, expectedRecordCount) -> {
            if (expectedRecordCount != null) {
                assertThat(response).isNotNull();
                assertThat(response.getTotalRecordCount()).isNotNull();
                assertThat(response.getTotalRecordCount()).isEqualTo(expectedRecordCount);
            }
        };

        Consumer<CosmosResponseWrapper> validateEmptyResults =
            (response) -> validateExpectedRecordCount.accept(response, 0L);

        Consumer<CosmosResponseWrapper> validateExactlyOneRecordReturned =
            (response) -> validateExpectedRecordCount.accept(response, 1L);

        Consumer<CosmosResponseWrapper> validateAllRecordsSameIdReturned =
            (response) -> validateExpectedRecordCount.accept(
                response,
                1L + ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION);

        Consumer<CosmosResponseWrapper> validateAllRecordsSamePartitionReturned =
            (response) -> validateExpectedRecordCount.accept(
                response,
                1L + ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE);

        BiConsumer<CosmosDiagnosticsContext, Integer> validateCtxRegions =
            (ctx, expectedNumberOfRegionsContacted) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getContactedRegionNames().size()).isEqualTo(expectedNumberOfRegionsContacted);
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxOnlyFeedResponsesOrPointReads =
            (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    // Query Plan + at least one query response

                    assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(1);

                    for (CosmosDiagnostics currentDiagnostics : diagnostics) {
                        // ReadMany will either use a point read (when there is exactly one id + pk tuple per partition
                        // or execute a query
                        if (currentDiagnostics.getFeedResponseDiagnostics() == null) {
                            // Point read or failed query request
                            assertThat(currentDiagnostics.getClientSideRequestStatistics()).isNotNull();
                            assertThat(currentDiagnostics.getClientSideRequestStatistics().size()).isEqualTo(1);
                            ClientSideRequestStatistics reqStats =
                                currentDiagnostics.getClientSideRequestStatistics().iterator().next();
                            assertThat(reqStats.getResponseStatisticsList()).isNotNull();
                            List<ClientSideRequestStatistics.StoreResponseStatistics> responseStatistics =
                                reqStats.getResponseStatisticsList().stream().collect(Collectors.toList());
                            assertThat(responseStatistics.size()).isGreaterThanOrEqualTo(1);
                            for (ClientSideRequestStatistics.StoreResponseStatistics responseStats : responseStatistics) {
                                assertThat(responseStats.getRequestResourceType())
                                    .isEqualTo(ResourceType.Document);

                                boolean isError = false;
                                if (responseStats.getStoreResult() != null &
                                    responseStats.getStoreResult().getStoreResponseDiagnostics() != null) {
                                    isError = responseStats.getStoreResult().getStoreResponseDiagnostics().getStatusCode() >= 400;
                                }

                                if (!isError) {
                                    assertThat(responseStats.getRequestOperationType())
                                        .isEqualTo(OperationType.Read);
                                }
                            }
                        } else {
                            // query
                            assertThat(currentDiagnostics.getFeedResponseDiagnostics()).isNotNull();
                            assertThat(currentDiagnostics.getFeedResponseDiagnostics().getQueryMetricsMap()).isNotNull();
                            assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                            assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics().size()).isGreaterThanOrEqualTo(1);
                        }
                    }
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxSingleRegion =
            (ctx) -> validateCtxRegions.accept(ctx, SINGLE_REGION);

        Consumer<CosmosDiagnosticsContext> validateCtxTwoRegions =
            (ctx) -> validateCtxRegions.accept(ctx, TWO_REGIONS);

        Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTupleForSingleDocument = (inputParams) -> {
            ArrayList<Pair<String, String>> tuples = new ArrayList<>();
            tuples.add(inputParams.idAndPkValuePair);
            return tuples;
        };

        Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTuplesForSinglePartition = (inputParams) -> {
            ArrayList<Pair<String, String>> tuples = new ArrayList<>();
            tuples.add(inputParams.idAndPkValuePair);
            for (Pair<String, String> idAndPkValuePair: inputParams.otherDocumentIdAndPkValuePairs) {
                if (idAndPkValuePair.getRight().equals(inputParams.idAndPkValuePair.getRight())) {
                    tuples.add(idAndPkValuePair);
                }
            }
            return tuples;
        };

        Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTuplesForSameIdAcrossMultiplePartitions = (inputParams) -> {
            ArrayList<Pair<String, String>> tuples = new ArrayList<>();
            tuples.add(inputParams.idAndPkValuePair);
            for (Pair<String, String> idAndPkValuePair: inputParams.otherDocumentIdAndPkValuePairs) {
                if (idAndPkValuePair.getLeft().equals(inputParams.idAndPkValuePair.getLeft())) {
                    tuples.add(idAndPkValuePair);
                }
            }
            return tuples;
        };

        Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTupleForSingleDocumentEmptyResult = (inputParams) -> {
            ArrayList<Pair<String, String>> tuples = new ArrayList<>();
            tuples.add(Pair.of("Invalid" + inputParams.idAndPkValuePair.getLeft(), inputParams.idAndPkValuePair.getRight()));
            return tuples;
        };

        Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTuplesForSinglePartitionEmptyResult = (inputParams) -> {
            ArrayList<Pair<String, String>> tuples = new ArrayList<>();
            tuples.add(Pair.of("Invalid" + inputParams.idAndPkValuePair.getLeft(), inputParams.idAndPkValuePair.getRight()));
            for (Pair<String, String> idAndPkValuePair: inputParams.otherDocumentIdAndPkValuePairs) {
                if (idAndPkValuePair.getRight().equals(inputParams.idAndPkValuePair.getRight())) {
                    tuples.add(Pair.of("Invalid" + idAndPkValuePair.getLeft(), idAndPkValuePair.getRight()));
                }
            }
            return tuples;
        };

        BiFunction<ItemOperationInvocationParameters, List<Pair<String, String>>, CosmosResponseWrapper>
            readMany = (inputParams, tuples) -> readManyCore(tuples, inputParams);

        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Availability Strategy used
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTuples,
            //    BiFunction<ItemOperationInvocationParameters, List<Pair<String, String>>, CosmosResponseWrapper> readManyOperation,
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    Expected number of DiagnosticsContext instances - there will be one per page returned form the PagedFlux
            //    Diagnostics context validation callback applied to the first DiagnosticsContext instance
            //    Diagnostics context validation callback applied to the all other DiagnosticsContext instances
            //    Consumer<CosmosResponseWrapper> - callback to validate the response (status codes, total records returned etc.)
            //    numberOfOtherDocumentsWithSameId - number of other documents to be created with the same id-value
            //        (but different pk-value). Mostly used to ensure cross-partition queries have to
            //        touch more than one partition.
            //    numberOfOtherDocumentsWithSamePk - number of documents to be created with the same pk-value
            //        (but different id-value). Mostly used to force a certain number of documents being
            //        returned for single partition queries.
            // },

            // ReadMany with a single id+pk tuple - resulting in point read.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "SingleTuple_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTupleForSingleDocument,
                readMany,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "ManyTuplesSinglePartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartition,
                readMany,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                validateAllRecordsSamePartitionReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query. Page size is one -
            // so, multiple pages are expected to be returned
            // No failure injection and all records will fit into a single page
            new Object[] {
                "ManyTuplesCrossPartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSameIdAcrossMultiplePartitions,
                readMany,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(PHYSICAL_PARTITION_COUNT);
                    }
                ),
                null,
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // ReadMany with a single id+pk tuple - resulting in point read - no results returned (404 mapped to
            // empty FeedResponse). No failure injection and all records will fit into a single page
            new Object[] {
                "SingleTuple_EmptyResult_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTupleForSingleDocumentEmptyResult,
                readMany,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                validateEmptyResults,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "ManyTuplesSinglePartition_EmptyResult_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartitionEmptyResult,
                readMany,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                validateEmptyResults,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "ManyTuplesSinglePartition_408_FirstRegionOnly_EagerAvailabilityStrategy",
                THREE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartition,
                readMany,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(2);
                    }
                ),
                null,
                validateAllRecordsSamePartitionReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "SingleTuple_408_FirstRegionOnly_EagerAvailabilityStrategy",
                THREE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartition,
                readMany,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(2);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query.
            // 404-1002 injected injection, remote region preferred - so client retry policy retry should
            // succeed before hedging even starts. All records will fit into a single page
            new Object[] {
                "ManyTuplesSinglePartition_404-1002_RemotePreferred_FirstRegionOnly_ReluctantAvailabilityStrategy",
                Duration.ofSeconds(10),
                reluctantThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartition,
                readMany,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                validateAllRecordsSamePartitionReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },
            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query.
            // 429-3200 injected only in local region - request will succeed due to hedging request.
            // All records will fit into a single page
            new Object[] {
                "ManyTuplesSinglePartition_429-3200_RemotePreferred_FirstRegionOnly_DefaultAvailabilityStrategy",
                THREE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartition,
                readMany,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                null,
                validateAllRecordsSamePartitionReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },
            // ReadMany with a multiple id+pk tuple for a single partition - resulting in a query.
            // 429-3200 injected only in local region
            // No availability strategy - request will time out
            new Object[] {
                "ManyTuplesSinglePartition_429-3200_RemotePreferred_FirstRegionOnly_NoAvailabilityStrategy",
                THREE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartition,
                readMany,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                null,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },
            // ReadMany with a single id+pk tuple - resulting in a read.
            // 429-3200 injected only in local region - request will succeed due to hedging request.
            // All records will fit into a single page
            new Object[] {
                "SingleTuple_429-3200_RemotePreferred_FirstRegionOnly_DefaultAvailabilityStrategy",
                THREE_SECOND_DURATION,
                defaultAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTupleForSingleDocument,
                readMany,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
            // ReadMany with a single id+pk tuple - resulting in a read.
            // 429-3200 injected only in local region
            // No availability strategy - request will time out
            new Object[] {
                "SingleTuple_429-3200_RemotePreferred_FirstRegionOnly_NoAvailabilityStrategy",
                THREE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                readManyTuplesForSinglePartition,
                readMany,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesOrPointReads,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                null,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_readManyAfterCreation")
    public void readManyAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTuples,
        BiFunction<ItemOperationInvocationParameters, List<Pair<String, String>>, CosmosResponseWrapper> readManyOperation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> responseValidator,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk) {

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            null,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(
                FaultInjectionOperationType.QUERY_ITEM,
                       FaultInjectionOperationType.READ_ITEM
            ),
            (params) -> readManyOperation.apply(params, readManyTuples.apply(params)),
            faultInjectionCallback,
            validateStatusCode,
            expectedDiagnosticsContextCount,
            firstDiagnosticsContextValidations,
            otherDiagnosticsContextValidations,
            responseValidator,
            numberOfOtherDocumentsWithSameId,
            numberOfOtherDocumentsWithSamePk,
            false,
            ConnectionMode.DIRECT);
    }

    private CosmosResponseWrapper readAllReturnsTotalRecordCountCore(
        String partitionKeyValue,
        ItemOperationInvocationParameters params,
        int requestedPageSize,
        boolean enforceEmptyPages
    ) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

        if (enforceEmptyPages) {
            ImplementationBridgeHelpers
                .CosmosQueryRequestOptionsHelper
                .getCosmosQueryRequestOptionsAccessor()
                .setAllowEmptyPages(queryOptions, true);
        }

        CosmosEndToEndOperationLatencyPolicyConfig e2ePolicy = ImplementationBridgeHelpers
            .CosmosItemRequestOptionsHelper
            .getCosmosItemRequestOptionsAccessor()
            .getEndToEndOperationLatencyPolicyConfig(params.options);
        queryOptions.setCosmosEndToEndOperationLatencyPolicyConfig(e2ePolicy);

        CosmosPagedFlux<ObjectNode> readAllPagedFlux;

        if (partitionKeyValue != null){
            readAllPagedFlux = params.container.readAllItems(
                new PartitionKey(partitionKeyValue),
                queryOptions,
                ObjectNode.class
            );
        } else {
            readAllPagedFlux = params.container.readAllItems(
                queryOptions,
                ObjectNode.class
            );
        }

        List<FeedResponse<ObjectNode>> returnedPages =
            readAllPagedFlux.byPage(requestedPageSize).collectList().block();

        ArrayList<CosmosDiagnosticsContext> foundCtxs = new ArrayList<>();

        if (returnedPages.isEmpty()) {
            return new CosmosResponseWrapper(
                null,
                HttpConstants.StatusCodes.NOTFOUND,
                NO_QUERY_PAGE_SUB_STATUS_CODE,
                null);
        }

        long totalRecordCount = 0L;
        for (FeedResponse<ObjectNode> page : returnedPages) {
            if (page.getCosmosDiagnostics() != null) {
                foundCtxs.add(page.getCosmosDiagnostics().getDiagnosticsContext());
            } else {
                foundCtxs.add(null);
            }

            if (page.getResults() != null && page.getResults().size() > 0) {
                totalRecordCount += page.getResults().size();
            }
        }

        return new CosmosResponseWrapper(
            foundCtxs.toArray(new CosmosDiagnosticsContext[0]),
            HttpConstants.StatusCodes.OK,
            HttpConstants.SubStatusCodes.UNKNOWN,
            totalRecordCount);
    }

    @DataProvider(name = "testConfigs_readAllAfterCreation")
    public Object[][] testConfigs_readAllAfterCreation() {

        final int ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE = 10;
        final int NO_OTHER_DOCS_WITH_SAME_PK = 0;
        final int NO_OTHER_DOCS_WITH_SAME_ID = 0;
        final int ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION = PHYSICAL_PARTITION_COUNT * 10;
        final int SINGLE_REGION = 1;
        final int TWO_REGIONS = 2;

        BiConsumer<CosmosResponseWrapper, Long> validateExpectedRecordCount = (response, expectedRecordCount) -> {
            if (expectedRecordCount != null) {
                assertThat(response).isNotNull();
                assertThat(response.getTotalRecordCount()).isNotNull();
                assertThat(response.getTotalRecordCount()).isEqualTo(expectedRecordCount);
            }
        };

        Consumer<CosmosResponseWrapper> validateEmptyResults =
            (response) -> validateExpectedRecordCount.accept(response, 0L);

        Consumer<CosmosResponseWrapper> validateExactlyOneRecordReturned =
            (response) -> validateExpectedRecordCount.accept(response, 1L);

        Consumer<CosmosResponseWrapper> validateAllRecordsSameIdReturned =
            (response) -> validateExpectedRecordCount.accept(
                response,
                1L + ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION);

        Consumer<CosmosResponseWrapper> validateAllRecordsSamePartitionReturned =
            (response) -> validateExpectedRecordCount.accept(
                response,
                1L + ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE);

        BiConsumer<CosmosDiagnosticsContext, Integer> validateCtxRegions =
            (ctx, expectedNumberOfRegionsContacted) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getContactedRegionNames().size()).isEqualTo(expectedNumberOfRegionsContacted);
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxOnlyFeedResponsesOrPointReads =
            (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    // Query Plan + at least one query response

                    assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(1);

                    for (CosmosDiagnostics currentDiagnostics : diagnostics) {
                        assertThat(currentDiagnostics).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getQueryPlanDiagnosticsContext()).isNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getQueryMetricsMap()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics().size()).isGreaterThanOrEqualTo(1);
                    }
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxSingleRegion =
            (ctx) -> validateCtxRegions.accept(ctx, SINGLE_REGION);

        Consumer<CosmosDiagnosticsContext> validateCtxTwoRegions =
            (ctx) -> validateCtxRegions.accept(ctx, TWO_REGIONS);

        Consumer<CosmosDiagnosticsContext> validateCtxQueryPlan =
            (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    // Query Plan + at least one query response

                    assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(1);
                    assertThat(diagnostics[0]).isNotNull();
                    assertThat(diagnostics[0].getFeedResponseDiagnostics()).isNull();
                    assertThat(diagnostics[0].getClientSideRequestStatistics()).isNotNull();
                    assertThat(diagnostics[0].getClientSideRequestStatistics().size()).isEqualTo(1);
                    ClientSideRequestStatistics[] clientStats =
                        diagnostics[0].getClientSideRequestStatistics().toArray(new ClientSideRequestStatistics[0]);
                    assertThat(clientStats.length).isEqualTo(1);
                    assertThat(clientStats[0]).isNotNull();
                    assertThat(clientStats[0].getGatewayStatisticsList()).isNotNull();
                    ClientSideRequestStatistics.GatewayStatistics[] gwStats =
                        clientStats[0].getGatewayStatisticsList().toArray(new ClientSideRequestStatistics.GatewayStatistics[0]);
                    assertThat(gwStats.length).isGreaterThanOrEqualTo(1);
                    assertThat(gwStats[0]).isNotNull();
                    assertThat(gwStats[0].getOperationType()).isEqualTo(OperationType.QueryPlan);
                }
            };

        Consumer<CosmosDiagnosticsContext> validateCtxOnlyFeedResponsesExceptQueryPlan =
            (ctx) -> {
                assertThat(ctx).isNotNull();
                if (ctx != null) {
                    assertThat(ctx.getDiagnostics()).isNotNull();
                    // Query Plan + at least one query response

                    assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                    CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);
                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(1);

                    // Validate that at most one FeedResponse has query plan diagnostics
                    CosmosDiagnostics[] feedResponseDiagnosticsWithQueryPlan = Arrays.stream(diagnostics)
                                                                                     .filter(d -> d.getFeedResponseDiagnostics() != null
                                                                                         && d.getFeedResponseDiagnostics().getQueryPlanDiagnosticsContext() != null)
                                                                                     .toArray(CosmosDiagnostics[]::new);

                    assertThat(feedResponseDiagnosticsWithQueryPlan.length).isLessThanOrEqualTo(1);

                    int start = 0;
                    if (diagnostics[0].getFeedResponseDiagnostics() == null) {
                        // skip query plan
                        start = 1;
                    }

                    assertThat(diagnostics.length).isGreaterThanOrEqualTo(start + 1);

                    for (int i = start; i < diagnostics.length; i++) {
                        CosmosDiagnostics currentDiagnostics = diagnostics[i];
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getQueryMetricsMap()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics().size()).isGreaterThanOrEqualTo(1);
                    }
                }
            };

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readAllDefaultPageSizeEntireContainer =
            (inputParams) -> readAllReturnsTotalRecordCountCore(null, inputParams, 100, false);

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readAllDefaultPageSizeEntireContainerEnforceEmptyPages =
            (inputParams) -> readAllReturnsTotalRecordCountCore(null, inputParams, 100, true);

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readAllDefaultPageSizeSinglePartition =
            (inputParams) -> readAllReturnsTotalRecordCountCore(inputParams.idAndPkValuePair.getRight(), inputParams, 100, false);

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readAllPageSizeOneEntireContainer =
            (inputParams) -> readAllReturnsTotalRecordCountCore(null, inputParams, 1, false);

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readAllPageSizeOneSinglePartition =
            (inputParams) -> readAllReturnsTotalRecordCountCore(inputParams.idAndPkValuePair.getRight(), inputParams, 1, false);

        Consumer<CosmosDiagnosticsContext> validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse = (ctx) -> {
            CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);


            assertThat(diagnostics.length).isGreaterThanOrEqualTo(1);
            int start = 0;
            if (diagnostics[0].getFeedResponseDiagnostics() == null) {
                // First entry is for query plan
                start = 1;
            }

            assertThat(diagnostics.length).isGreaterThanOrEqualTo(start + 1);

            // skip first diagnostics - which is query plan
            for (int i = start; i < diagnostics.length; i++) {
                CosmosDiagnostics currentDiagnostics = diagnostics[i];
                assertThat(currentDiagnostics.getContactedRegionNames()).isNotNull();
                assertThat(currentDiagnostics.getContactedRegionNames().size()).isGreaterThanOrEqualTo(1);
                String regionName = currentDiagnostics.getContactedRegionNames().iterator().next();
                if (regionName.equals(FIRST_REGION_NAME)) {
                    // First region requests should have failed
                    assertThat(currentDiagnostics.getFeedResponseDiagnostics()).isNull();
                } else {
                    assertThat(regionName).isEqualTo(SECOND_REGION_NAME);
                    // Second region requests should have succeeded
                    assertThat(currentDiagnostics.getFeedResponseDiagnostics()).isNotNull();
                    if (start == 1) {
                        assertThat(currentDiagnostics.getFeedResponseDiagnostics().getQueryPlanDiagnosticsContext()).isNotNull();
                    }
                    assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                    assertThat(currentDiagnostics.getFeedResponseDiagnostics().getClientSideRequestStatistics().size()).isEqualTo(1);
                }
            }
        };

        return new Object[][] {
            // CONFIG description
            // new Object[] {
            //    TestId - name identifying the test case
            //    End-to-end timeout
            //    Availability Strategy used
            //    Region switch hint (404/1002 prefer local or remote retries)
            //    Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readAllOperation,
            //    Failure injection callback
            //    Status code/sub status code validation callback
            //    Expected number of DiagnosticsContext instances - there will be one per page returned form the PagedFlux
            //    Diagnostics context validation callback applied to the first DiagnosticsContext instance
            //    Diagnostics context validation callback applied to the all other DiagnosticsContext instances
            //    Consumer<CosmosResponseWrapper> - callback to validate the response (status codes, total records returned etc.)
            //    numberOfOtherDocumentsWithSameId - number of other documents to be created with the same id-value
            //        (but different pk-value). Mostly used to ensure cross-partition queries have to
            //        touch more than one partition.
            //    numberOfOtherDocumentsWithSamePk - number of documents to be created with the same pk-value
            //        (but different id-value). Mostly used to force a certain number of documents being
            //        returned for single partition queries.
            // },

            // ReadAll (entire container) with single doc inserted into single partition only.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "DefaultPageSize_Container_SingleDocument_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainer,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadAll (entire container) with documents inserted into single partition only.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "DefaultPageSize_Container_SinglePartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainer,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                null,
                validateAllRecordsSamePartitionReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // ReadAll (entire container) with single doc inserted - empty pages enabled. So, expected to see as many
            // CosmosDiagnosticsContext instances as physical partitions
            // No failure injection and all records will fit into a single page
            new Object[] {
                "DefaultPageSize_Container_SingleDocumentWithEmptyPages_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainerEnforceEmptyPages,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadAll (entire container) with documents inserted into single partition only. Page size is one - so,
            // multiple pages returned. No failure injection and all records will fit into a single page
            new Object[] {
                "PageSizeOne_Container_SinglePartition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllPageSizeOneEntireContainer,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1 + ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                validateAllRecordsSamePartitionReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // ReadAll (entire container) with documents inserted into single partition only.
            // No failure injection and all records will fit into a single page
            // ReadAll with PartitionKey never will retrieve a query plan
            new Object[] {
                "DefaultPageSize_Partition_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeSinglePartition,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                null,
                validateAllRecordsSamePartitionReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                ENOUGH_DOCS_SAME_PK_TO_EXCEED_PAGE_SIZE
            },

            // ReadAll (entire container) with multiple docs with same "id" inserted across partitions.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "DefaultPageSize_Container_DocsAcrossAllPartitions_AllGood_NoAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainer,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(1);
                    }
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadAll (entire container) with multiple docs for single partition. Injected 503 on first region only.
            // All records per partition will fit into a single page
            new Object[] {
                "DefaultPageSize_Container_DocsAcrossAllPartitions_408_OnlyFirstRegion_EagerAvailabilityStrategy",
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainer,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxQueryPlan,
                    validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(3);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(
                            ctx
                                .getDiagnostics()
                                .stream()
                                .filter(d -> d.getContactedRegionNames().contains(SECOND_REGION_NAME))
                                .count())
                            .isEqualTo(1);
                    }
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadAll (entire container) with multiple docs for single partition. Injected 503 on first region only.
            // All records per partition will fit into a single page
            new Object[] {
                "DefaultPageSize_Partition_DocsAcrossAllPartitions_408_OnlyFirstRegion_EagerAvailabilityStrategy",
                THREE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllPageSizeOneSinglePartition,
                injectTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getContactedRegionNames()).isNotNull();
                        assertThat(ctx.getContactedRegionNames().contains(SECOND_REGION_NAME)).isTrue();
                        assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(1);
                        List<CosmosDiagnostics> secondRegionDiagnostics = ctx
                            .getDiagnostics()
                            .stream()
                            .filter(d -> d.getContactedRegionNames().size() == 1 &&
                                d.getContactedRegionNames().contains(SECOND_REGION_NAME))
                            .collect(Collectors.toList());
                        assertThat(
                            secondRegionDiagnostics
                                .get(0)
                                .getClientSideRequestStatistics()
                        ).isNotNull();
                        ClientSideRequestStatistics[] clientStats =
                            secondRegionDiagnostics.get(0).getClientSideRequestStatistics().toArray(
                                new ClientSideRequestStatistics[0]
                            );
                        assertThat(clientStats.length).isGreaterThanOrEqualTo(1);
                        assertThat(clientStats[0].getResponseStatisticsList()).isNotNull();
                        assertThat(clientStats[0].getResponseStatisticsList().iterator().next().getExcludedRegions()).isEqualTo(FIRST_REGION_NAME);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // ReadAll (entire container) with multiple docs for single partition. Injected 410-1002 on first region only.
            // All records per partition will fit into a single page
            new Object[] {
                "DefaultPageSize_Container_DocsAcrossAllPartitions_410-1002_Local_OnlyFirstRegion_EagerAvailabilityStrategy",
                ONE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainer,
                injectReadSessionNotAvailableIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxQueryPlan,
                    validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(3);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(
                            ctx
                                .getDiagnostics()
                                .stream()
                                .filter(d -> d.getContactedRegionNames().contains(SECOND_REGION_NAME))
                                .count())
                            .isEqualTo(1);
                    }
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
            // ReadAll (entire container) with multiple docs for single partition. Injected 429-3200 on first region only.
            // All records per partition will fit into a single page
            new Object[] {
                "DefaultPageSize_Container_DocsAcrossAllPartitions_429-3200_Local_OnlyFirstRegion_EagerAvailabilityStrategy",
                THREE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainer,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxTwoRegions,
                    validateCtxQueryPlan,
                    validateCtxFirstRegionFailureSecondRegionSuccessfulSingleFeedResponse,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(3);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(
                            ctx
                                .getDiagnostics()
                                .stream()
                                .filter(d -> d.getContactedRegionNames().contains(SECOND_REGION_NAME))
                                .count())
                            .isEqualTo(1);
                    }
                ),
                validateAllRecordsSameIdReturned,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
            // ReadAll (entire container) with multiple docs for single partition. Injected 429-3200 on first region only.
            new Object[] {
                "DefaultPageSize_Container_DocsAcrossAllPartitions_429-3200_Local_OnlyFirstRegion_noAvailabilityStrategy",
                ONE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.DIRECT,
                readAllDefaultPageSizeEntireContainer,
                injectRequestRateTooLargeIntoFirstRegionOnly,
                validateStatusCodeIsOperationCancelled,
                PHYSICAL_PARTITION_COUNT,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isGreaterThanOrEqualTo(2);
                    }
                ),
                ArrayUtils.toArray(
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(
                            ctx
                                .getDiagnostics()
                                .stream()
                                .filter(d -> d.getContactedRegionNames().contains(SECOND_REGION_NAME))
                                .count())
                            .isEqualTo(0);
                    }
                ),
                null,
                ENOUGH_DOCS_OTHER_PK_TO_HIT_EVERY_PARTITION,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            // GATEWAY MODE
            //-------------

            // ReadAll (entire container) with single doc inserted into single partition only.
            // No failure injection and all records will fit into a single page
            new Object[] {
                "GW_DefaultPageSize_Container_SingleDocument_AllGood_NoAvailabilityStrategy",
                THREE_SECOND_DURATION,
                noAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.GATEWAY,
                readAllDefaultPageSizeEntireContainer,
                noFailureInjection,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxSingleRegion,
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        assertThat(ctx.getDiagnostics().size()).isEqualTo(2);
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },

            new Object[] {
                "GW_DefaultPageSize_Container_SingleDocument_GW408_EagerAvailabilityStrategy",
                THREE_SECOND_DURATION,
                eagerThresholdAvailabilityStrategy,
                noRegionSwitchHint,
                ConnectionMode.GATEWAY,
                readAllDefaultPageSizeEntireContainer,
                injectGatewayTransitTimeoutIntoFirstRegionOnly,
                validateStatusCodeIs200Ok,
                1,
                ArrayUtils.toArray(
                    validateCtxQueryPlan,
                    validateCtxOnlyFeedResponsesExceptQueryPlan,
                    (ctx) -> {
                        assertThat(ctx.getContactedRegionNames()).isNotNull();
                        assertThat(ctx.getContactedRegionNames().size()).isGreaterThanOrEqualTo(1);
                        assertThat(ctx.getContactedRegionNames().contains(SECOND_REGION_NAME)).isEqualTo(true);
                        assertThat(ctx.getDiagnostics()).isNotNull();
                        CosmosDiagnostics[] diagnostics = ctx.getDiagnostics().toArray(new CosmosDiagnostics[0]);

                        // Diagnostics of query attempt in first region not even available yet
                        assertThat(diagnostics.length).isEqualTo(2);
                        assertThat(diagnostics[1].getContactedRegionNames().size()).isEqualTo(1);
                        assertThat(diagnostics[1].getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                        assertThat(diagnostics[1].getFeedResponseDiagnostics()).isNotNull();
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getQueryMetricsMap()).isNotNull();
                        assertThat(diagnostics[1].getFeedResponseDiagnostics().getClientSideRequestStatistics()).isNotNull();
                        ClientSideRequestStatistics[] clientStats =
                            diagnostics[1]
                                .getFeedResponseDiagnostics()
                                .getClientSideRequestStatistics()
                                .toArray(new ClientSideRequestStatistics[0]);
                        assertThat(clientStats.length).isEqualTo(PHYSICAL_PARTITION_COUNT);
                        for (int i = 0; i < clientStats.length; i++) {
                            assertThat(clientStats[i].getContactedRegionNames()).isNotNull();
                            assertThat(clientStats[i].getContactedRegionNames().size()).isEqualTo(1);
                            assertThat(clientStats[i].getContactedRegionNames().iterator().next()).isEqualTo(SECOND_REGION_NAME);
                            assertThat(clientStats[i].getGatewayStatisticsList()).isNotNull();
                            assertThat(clientStats[i].getResponseStatisticsList()).isNotNull();
                            assertThat(clientStats[i].getResponseStatisticsList().size()).isEqualTo(0);
                        }
                    }
                ),
                null,
                validateExactlyOneRecordReturned,
                NO_OTHER_DOCS_WITH_SAME_ID,
                NO_OTHER_DOCS_WITH_SAME_PK
            },
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "testConfigs_readAllAfterCreation")
    public void readAllAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readAllOperation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> responseValidator,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk) {

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            null,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(FaultInjectionOperationType.QUERY_ITEM),
            readAllOperation,
            faultInjectionCallback,
            validateStatusCode,
            expectedDiagnosticsContextCount,
            firstDiagnosticsContextValidations,
            otherDiagnosticsContextValidations,
            responseValidator,
            numberOfOtherDocumentsWithSameId,
            numberOfOtherDocumentsWithSamePk,
            true,
            connectionMode);
    }

    private static ObjectNode createTestItemAsJson(String id, String pkValue) {
        CosmosDiagnosticsTest.TestItem nextItemToBeCreated =
            new CosmosDiagnosticsTest.TestItem(id, pkValue);

        return OBJECT_MAPPER.valueToTree(nextItemToBeCreated);
    }

    private CosmosAsyncContainer createTestContainer(CosmosAsyncClient clientWithPreferredRegions) {
        String dbId = UUID.randomUUID().toString();
        return createTestContainer(clientWithPreferredRegions, dbId);
    }

    private CosmosAsyncContainer createTestContainer(CosmosAsyncClient clientWithPreferredRegions, String dbId) {
        String containerId = UUID.randomUUID().toString();

        clientWithPreferredRegions.createDatabaseIfNotExists(dbId).block();
        CosmosAsyncDatabase databaseWithSeveralWriteableRegions = clientWithPreferredRegions.getDatabase(dbId);

        // setup db and container and pass their ids accordingly
        // ensure the container has a partition key definition of /mypk

        databaseWithSeveralWriteableRegions
            .createContainerIfNotExists(
                new CosmosContainerProperties(
                    containerId,
                    new PartitionKeyDefinition().setPaths(Arrays.asList("/mypk"))),
                // for PHYSICAL_PARTITION_COUNT partitions
                ThroughputProperties.createManualThroughput(6_000 * PHYSICAL_PARTITION_COUNT))
            .block();

        return databaseWithSeveralWriteableRegions.getContainer(containerId);
    }

    private static void inject(
        String ruleName,
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType applicableOperationType,
        FaultInjectionServerErrorResult toBeInjectedServerErrorResult,
        FeedRange applicableFeedRange) {

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            applicableOperationType,
            toBeInjectedServerErrorResult,
            applicableFeedRange,
            FaultInjectionConnectionType.DIRECT
        );
    }

    private static void inject(
        String ruleName,
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType applicableOperationType,
        FaultInjectionServerErrorResult toBeInjectedServerErrorResult,
        FeedRange applicableFeedRange,
        FaultInjectionConnectionType connectionType) {

        FaultInjectionRuleBuilder ruleBuilder = new FaultInjectionRuleBuilder(ruleName);

        List<FaultInjectionRule> faultInjectionRules = new ArrayList<>();

        // inject errors in all regions
        // configure in accordance with preferredRegions on the client
        for (String region : applicableRegions) {
            FaultInjectionConditionBuilder conditionBuilder = new FaultInjectionConditionBuilder()
                .operationType(applicableOperationType)
                .connectionType(connectionType)
                .region(region);

            if (applicableFeedRange != null) {
                conditionBuilder = conditionBuilder.endpoints(
                    new FaultInjectionEndpointBuilder(applicableFeedRange)
                        .replicaCount(4)
                        .includePrimary(true)
                        .build()
                );
            }

            FaultInjectionCondition faultInjectionCondition = conditionBuilder.build();

            // sustained fault injection
            FaultInjectionRule readSessionUnavailableRule = ruleBuilder
                .condition(faultInjectionCondition)
                .result(toBeInjectedServerErrorResult)
                .duration(Duration.ofSeconds(120))
                .build();

            faultInjectionRules.add(readSessionUnavailableRule);
        }

        CosmosFaultInjectionHelper
            .configureFaultInjectionRules(containerWithSeveralWriteableRegions, faultInjectionRules)
            .block();

        logger.info(
            "FAULT INJECTION - Applied rule '{}' for regions '{}', operationType '{}'.",
            ruleName,
            String.join(", ", applicableRegions),
            applicableOperationType);
    }

    private static void injectReadSessionNotAvailableError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType operationType,
        FeedRange applicableFeedRange) {

        String ruleName = "serverErrorRule-read-session-unavailable-" + UUID.randomUUID();
        FaultInjectionServerErrorResult badSessionTokenServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            operationType,
            badSessionTokenServerErrorResult,
            applicableFeedRange
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
            timeoutResult,
            null
        );
    }

    private static void injectGatewayTransitTimeout(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-gatewayTransitTimeout-" + UUID.randomUUID();
        FaultInjectionServerErrorResult timeoutResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
            .delay(Duration.ofSeconds(6))
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            timeoutResult,
            null,
            FaultInjectionConnectionType.GATEWAY
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
            serviceUnavailableResult,
            null
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
            serviceUnavailableResult,
            null
        );
    }

    private static void injectRequestRateTooLargeError(
        CosmosAsyncContainer containerWithSeveralWriteableRegions,
        List<String> applicableRegions,
        FaultInjectionOperationType faultInjectionOperationType) {

        String ruleName = "serverErrorRule-requestRateTooLargeError-" + UUID.randomUUID();
        FaultInjectionServerErrorResult serviceUnavailableResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
            .build();

        inject(
            ruleName,
            containerWithSeveralWriteableRegions,
            applicableRegions,
            faultInjectionOperationType,
            serviceUnavailableResult,
            null
        );
    }

    private void execute(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Duration customMinRetryTimeInLocalRegionForWrites,
        Boolean nonIdempotentWriteRetriesEnabled,
        FaultInjectionOperationType[] faultInjectionOperationTypes,
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> actionAfterInitialCreation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> validateResponse,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk,
        boolean clearContainerBeforeExecution,
        ConnectionMode connectionMode) {

        // Test two cases here:
        // - the endToEndOperationLatencyPolicyConfig is being configured on the client only
        // - the endToEndOperationLatencyPolicyConfig is being configured on the request options only
        for (boolean e2eTimeoutPolicyOnClient : Arrays.asList(Boolean.TRUE, Boolean.FALSE)) {
            logger.info("START {}, e2eTimeoutPolicyOnClient {}", testCaseId, e2eTimeoutPolicyOnClient);

            CosmosEndToEndOperationLatencyPolicyConfigBuilder e2ePolicyBuilder =
                new CosmosEndToEndOperationLatencyPolicyConfigBuilder(endToEndTimeout)
                    .enable(true);
            CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig =
                availabilityStrategy != null
                    ? e2ePolicyBuilder.availabilityStrategy(availabilityStrategy).build()
                    : e2ePolicyBuilder.build();

            CosmosAsyncClient clientWithPreferredRegions = null;

            if (endToEndOperationLatencyPolicyConfig != null) {
                clientWithPreferredRegions = buildCosmosClientWithE2ETimeoutPolicy(
                    this.writeableRegions,
                    regionSwitchHint,
                    connectionMode,
                    customMinRetryTimeInLocalRegionForWrites,
                    nonIdempotentWriteRetriesEnabled,
                    endToEndOperationLatencyPolicyConfig);
            } else {
                clientWithPreferredRegions = buildCosmosClientWithoutE2ETimeoutPolicy(
                    this.writeableRegions,
                    regionSwitchHint,
                    connectionMode,
                    customMinRetryTimeInLocalRegionForWrites,
                    nonIdempotentWriteRetriesEnabled);
            }

            try {

                if (clearContainerBeforeExecution) {
                    CosmosAsyncContainer newTestContainer =
                        this.createTestContainer(clientWithPreferredRegions, this.testDatabaseId);
                    this.testContainerId = newTestContainer.getId();
                    // Creating a container is an async task - especially with multiple regions it can
                    // take some time until the container is available in the remote regions as well
                    // When the container does not exist yet, you would see 401 for example for point reads etc.
                    // So, adding this delay after container creation to minimize risk of hitting these errors
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                String documentId = UUID.randomUUID().toString();
                Pair<String, String> idAndPkValPair = new ImmutablePair<>(documentId, documentId);

                CosmosDiagnosticsTest.TestItem createdItem = new CosmosDiagnosticsTest.TestItem(documentId, documentId);
                CosmosAsyncContainer testContainer = clientWithPreferredRegions
                    .getDatabase(this.testDatabaseId)
                    .getContainer(this.testContainerId);

                testContainer.createItem(createdItem).block();

                List<Pair<String, String>> otherIdAndPkValues = new ArrayList<>();
                for (int i = 0; i < numberOfOtherDocumentsWithSameId; i++) {
                    String additionalPK = UUID.randomUUID().toString();
                    testContainer.createItem(new CosmosDiagnosticsTest.TestItem(documentId, additionalPK)).block();
                    otherIdAndPkValues.add(Pair.of(documentId, additionalPK));
                }

                for (int i = 0; i < numberOfOtherDocumentsWithSamePk; i++) {
                    String sharedPK = documentId;
                    String additionalDocumentId = UUID.randomUUID().toString();
                    testContainer.createItem(new CosmosDiagnosticsTest.TestItem(additionalDocumentId, sharedPK)).block();
                    otherIdAndPkValues.add(Pair.of(additionalDocumentId, sharedPK));
                }

                if (faultInjectionCallback != null) {
                    for (FaultInjectionOperationType faultInjectionOperationType: faultInjectionOperationTypes) {
                        faultInjectionCallback.accept(testContainer, faultInjectionOperationType);
                    }
                }

                CosmosPatchItemRequestOptions itemRequestOptions = new CosmosPatchItemRequestOptions();

                if (!e2eTimeoutPolicyOnClient && endToEndOperationLatencyPolicyConfig != null) {
                    itemRequestOptions.setCosmosEndToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
                }

                try {

                    ItemOperationInvocationParameters params = new ItemOperationInvocationParameters();
                    params.container = testContainer;
                    params.options = itemRequestOptions;
                    params.idAndPkValuePair = idAndPkValPair;
                    params.otherDocumentIdAndPkValuePairs = otherIdAndPkValues;
                    params.nonIdempotentWriteRetriesEnabled = nonIdempotentWriteRetriesEnabled;

                    CosmosResponseWrapper response = actionAfterInitialCreation.apply(params);

                    CosmosDiagnosticsContext[] diagnosticsContexts = response.getDiagnosticsContexts();
                    assertThat(diagnosticsContexts).isNotNull();

                    logger.info(
                        "DIAGNOSTICS CONTEXT COUNT: {}",
                        diagnosticsContexts.length);
                    for (CosmosDiagnosticsContext diagnosticsContext: diagnosticsContexts) {
                        logger.info(
                            "DIAGNOSTICS CONTEXT: {}/{} {} {}",
                            diagnosticsContext != null ? diagnosticsContext.getStatusCode() : "n/a",
                            diagnosticsContext != null ? diagnosticsContext.getSubStatusCode() : "n/a",
                            diagnosticsContext != null ? diagnosticsContext.toString() : "n/a",
                            diagnosticsContext != null ? diagnosticsContext.toJson() : "NULL");
                        validateStatusCode.accept(diagnosticsContext.getStatusCode(), diagnosticsContext.getSubStatusCode());
                    }

                    assertThat(diagnosticsContexts.length).isEqualTo(expectedDiagnosticsContextCount);

                    if (response == null) {
                        fail("Response is null");
                    } else {
                        validateStatusCode.accept(response.getStatusCode(), null);
                        if (validateResponse != null) {
                            validateResponse.accept(response);
                        }
                    }

                    for (Consumer<CosmosDiagnosticsContext> ctxValidation : firstDiagnosticsContextValidations) {
                        ctxValidation.accept(diagnosticsContexts[0]);
                    }

                    for (int i = 1; i < diagnosticsContexts.length; i++) {
                        CosmosDiagnosticsContext currentCtx = diagnosticsContexts[i];

                        for (Consumer<CosmosDiagnosticsContext> ctxValidation : otherDiagnosticsContextValidations) {
                            ctxValidation.accept(currentCtx);
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(e, CosmosException.class);
                        CosmosDiagnosticsContext diagnosticsContext = null;
                        if (cosmosException.getDiagnostics() != null) {
                            diagnosticsContext = cosmosException.getDiagnostics().getDiagnosticsContext();
                        }

                        logger.info("EXCEPTION: ", e);
                        logger.info(
                            "DIAGNOSTICS CONTEXT: {} {}",
                            diagnosticsContext != null ? diagnosticsContext.toString() : "n/a",
                            diagnosticsContext != null ? diagnosticsContext.toJson(): "NULL");

                        validateStatusCode.accept(cosmosException.getStatusCode(), cosmosException.getSubStatusCode());
                        if (firstDiagnosticsContextValidations != null) {
                            assertThat(expectedDiagnosticsContextCount).isGreaterThanOrEqualTo(1);
                            for (Consumer<CosmosDiagnosticsContext> ctxValidation : firstDiagnosticsContextValidations) {
                                ctxValidation.accept(diagnosticsContext);
                            }
                        }
                    } else {
                        fail("A CosmosException instance should have been thrown.", e);
                    }
                }
            } finally {
                safeClose(clientWithPreferredRegions);
            }
        }
    }

    private static CosmosAsyncClient buildCosmosClientWithE2ETimeoutPolicy(
        List<String> preferredRegions,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Duration customMinRetryTimeInLocalRegionForWrites,
        Boolean nonIdempotentWriteRetriesEnabled,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {
        return buildCosmosClient(
            preferredRegions,
            regionSwitchHint,
            connectionMode,
            customMinRetryTimeInLocalRegionForWrites,
            nonIdempotentWriteRetriesEnabled,
            endToEndOperationLatencyPolicyConfig);
    }

    private static CosmosAsyncClient buildCosmosClientWithoutE2ETimeoutPolicy(
        List<String> preferredRegions,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Duration customMinRetryTimeInLocalRegionForWrites,
        Boolean nonIdempotentWriteRetriesEnabled) {
        return buildCosmosClient(
            preferredRegions,
            regionSwitchHint,
            connectionMode,
            customMinRetryTimeInLocalRegionForWrites,
            nonIdempotentWriteRetriesEnabled,
            null);
    }

    private static CosmosAsyncClient buildCosmosClient(
        List<String> preferredRegions,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Duration customMinRetryTimeInLocalRegionForWrites,
        Boolean nonIdempotentWriteRetriesEnabled,
        CosmosEndToEndOperationLatencyPolicyConfig endToEndOperationLatencyPolicyConfig) {

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsHandler(new CosmosDiagnosticsLogger());

        CosmosRegionSwitchHint effectiveRegionSwitchHint = regionSwitchHint != null
            ? regionSwitchHint
            : CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED;
        SessionRetryOptionsBuilder retryOptionsBuilder = new SessionRetryOptionsBuilder()
            .regionSwitchHint(effectiveRegionSwitchHint);

        if (customMinRetryTimeInLocalRegionForWrites != null) {
            retryOptionsBuilder.minTimeoutPerRegion(customMinRetryTimeInLocalRegionForWrites);
        }

        CosmosClientBuilder builder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .preferredRegions(preferredRegions)
            .sessionRetryOptions(retryOptionsBuilder.build())
            .multipleWriteRegionsEnabled(true)
            .clientTelemetryConfig(telemetryConfig);

        if (connectionMode == ConnectionMode.GATEWAY) {
            builder.gatewayMode();
        } else {
            builder.directMode();
        }

        if (nonIdempotentWriteRetriesEnabled != null) {
            builder.nonIdempotentWriteRetryOptions(
                new NonIdempotentWriteRetryOptions()
                    .setEnabled(nonIdempotentWriteRetriesEnabled)
                    .setTrackingIdUsed(true));
        }

        if (endToEndOperationLatencyPolicyConfig != null) {
            builder.endToEndOperationLatencyPolicyConfig(endToEndOperationLatencyPolicyConfig);
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
        private final CosmosDiagnosticsContext[] diagnosticsContexts;
        private final Integer statusCode;
        private final Integer subStatusCode;

        private final Long totalRecordCount;

        public CosmosResponseWrapper(CosmosItemResponse<?> itemResponse) {
            if (itemResponse.getDiagnostics() != null &&
                itemResponse.getDiagnostics().getDiagnosticsContext() != null) {
                System.out.println(itemResponse.getDiagnostics());

                this.diagnosticsContexts = ArrayUtils.toArray(itemResponse.getDiagnostics().getDiagnosticsContext());
            } else {
                this.diagnosticsContexts = null;
            }

            this.statusCode = itemResponse.getStatusCode();
            this.subStatusCode = null;
            this.totalRecordCount = itemResponse.getItem() != null ? 1L : 0L;
        }

        public CosmosResponseWrapper(CosmosDiagnosticsContext[] ctxs, int statusCode, Integer subStatusCode, Long totalRecordCount) {
            this.diagnosticsContexts = ctxs;
            this.statusCode = statusCode;
            this.subStatusCode = subStatusCode;
            this.totalRecordCount = totalRecordCount;
        }

        public CosmosDiagnosticsContext[] getDiagnosticsContexts() {
            return this.diagnosticsContexts;
        }

        public Integer getStatusCode() {
            return this.statusCode;
        }

        public Integer getSubStatusCode() {
            return this.subStatusCode;
        }

        public Long getTotalRecordCount() {
            return this.totalRecordCount;
        }
    }

    private static class ItemOperationInvocationParameters {
        public CosmosPatchItemRequestOptions options;
        public CosmosAsyncContainer container;
        public Pair<String, String> idAndPkValuePair;

        public List<Pair<String, String>> otherDocumentIdAndPkValuePairs;
        public Boolean nonIdempotentWriteRetriesEnabled;
    }
}
