// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class ExcludeRegionWithFaultInjectionTests extends TestSuiteBase {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredRegions;
    private Function<List<String>, List<String>> chooseLastTwoRegions = (regions) -> chooseLastTwoRegions(regions);
    private Function<List<String>, List<String>> chooseFirstTwoRegions = (regions) -> chooseFirstTwoRegions(regions);
    private Function<List<String>, List<String>> chooseFirstRegion = (regions) -> chooseKthRegion(regions, 1);
    private Function<List<String>, List<String>> chooseSecondRegion = (regions) -> chooseKthRegion(regions, 2);
    private Function<List<String>, List<String>> chooseThirdRegion = (regions) -> chooseKthRegion(regions, 3);
    private Function<List<String>, List<String>> chooseLastRegion = (regions) -> chooseLastRegion(regions);

    @Factory(dataProvider = "clientBuilderSolelyDirectWithSessionConsistency")
    public ExcludeRegionWithFaultInjectionTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @BeforeClass(groups = {"multi-master"})
    public void beforeClass() {
        this.cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.cosmosAsyncClient);

        // todo: do not hardcode here and adapt this test suite to make use of 2 preferred regions
        this.preferredRegions = Arrays.asList("East US", "South Central US", "West US");
    }

    @DataProvider(name = "readSessionNotAvailableTestConfigs")
    public Object[][] readSessionNotAvailableTestConfigs() {

        return new Object[][] {
            {
                // fault injection regions
                this.chooseLastTwoRegions,
                new ExpectedResult(
                    HttpConstants.StatusCodes.NOTFOUND,
                    HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                    // expected contacted regions
                    this.chooseLastTwoRegions.apply(this.preferredRegions)),
                // exclude regions
                this.chooseFirstRegion
            },
            {
                this.chooseFirstTwoRegions,
                new ExpectedResult(
                    HttpConstants.StatusCodes.NOTFOUND,
                    HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                    this.chooseFirstTwoRegions.apply(this.preferredRegions)),
                this.chooseThirdRegion
            },
            {
                this.chooseFirstRegion,
                new ExpectedResult(
                    HttpConstants.StatusCodes.OK,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    Arrays.asList(
                        this.chooseFirstRegion.apply(this.preferredRegions).get(0),
                        this.chooseThirdRegion.apply(this.preferredRegions).get(0))),
                this.chooseSecondRegion
            },
            {
                // fault injection regions
                this.chooseThirdRegion,
                new ExpectedResult(
                    HttpConstants.StatusCodes.NOTFOUND,
                    HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                    Arrays.asList(
                        this.chooseThirdRegion.apply(this.preferredRegions).get(0))),
                this.chooseFirstTwoRegions
            }
        };
    }

    // todo:
    //  1. inject fault for several op types
    @DataProvider(name = "regionExclusionReadAfterCreateTestConfigs")
    public Object[] regionExclusionReadAfterCreateTestConfigs() {
        return new Object[] {
            new MutationTestConfig()
                .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                // applied to the preferred regions
                .withRegionExclusionMutator(this.chooseLastRegion)
                .withExpectedResultBeforeMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.OK,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    this.chooseLastTwoRegions.apply(this.preferredRegions)
                ))
                .withExpectedResultAfterMutation(new ExpectedResult(
                HttpConstants.StatusCodes.NOTFOUND,
                HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                this.chooseFirstTwoRegions.apply(this.preferredRegions)
            )),
            new MutationTestConfig()
                .withChooseInitialExclusionRegions(this.chooseSecondRegion)
                .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                // applied to the preferred regions
                .withRegionExclusionMutator(this.chooseFirstTwoRegions)
                .withExpectedResultBeforeMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.OK,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    Arrays.asList(
                        this.chooseFirstRegion.apply(this.preferredRegions).get(0),
                        this.chooseThirdRegion.apply(this.preferredRegions).get(0))
                ))
                .withExpectedResultAfterMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.OK,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    this.chooseLastRegion.apply(this.preferredRegions)
            )),
            new MutationTestConfig()
                .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                // applied to the preferred regions
                .withRegionExclusionMutator(this.chooseFirstRegion)
                .withExpectedResultBeforeMutation(new ExpectedResult(
                        HttpConstants.StatusCodes.OK,
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        Arrays.asList(this.chooseFirstRegion.apply(this.preferredRegions).get(0))
                    )
                )
                .withExpectedResultAfterMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.OK,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    Arrays.asList(this.chooseSecondRegion.apply(this.preferredRegions).get(0))
                )
            ),
            new MutationTestConfig()
                .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                // applied to the preferred regions
                .withRegionExclusionMutator(this.chooseLastRegion)
                .withExpectedResultBeforeMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                    HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                    this.chooseSecondRegion.apply(this.preferredRegions)
                ))
                .withExpectedResultAfterMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                    HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                    this.chooseFirstRegion.apply(this.preferredRegions)
            )),
            new MutationTestConfig()
                .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                // applied to the preferred regions
                .withRegionExclusionMutator(this.chooseLastRegion)
                .withExpectedResultBeforeMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    this.chooseSecondRegion.apply(this.preferredRegions)
                ))
                .withExpectedResultAfterMutation(new ExpectedResult(
                    HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    this.chooseFirstRegion.apply(this.preferredRegions)
            ))
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "readSessionNotAvailableTestConfigs", enabled = true)
    public void readSessionNotAvailable_withExcludeRegion_test(
        Function<List<String>, List<String>> chooseFaultInjectionRegions,
        ExpectedResult expectedResult,
        Function<List<String>, List<String>> chooseExclusionRegions) {

        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(2));

        CosmosAsyncClient clientWithPreferredRegions = null;

        try {
            clientWithPreferredRegions = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .endpointDiscoveryEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.cosmosAsyncClient).getConsistencyLevel())
                .preferredRegions(this.preferredRegions)
                .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED).build())
                .directMode()
                .buildAsyncClient();

            CosmosAsyncContainer containerForClientWithPreferredRegions = clientWithPreferredRegions
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

            TestItem createdItem = TestItem.createNewItem();
            containerForClientWithPreferredRegions.createItem(createdItem).block();

            List<FaultInjectionRule> readSessionNotAvailableRules = buildReadSessionNotAvailableRules(
                chooseFaultInjectionRegions.apply(this.preferredRegions) , FaultInjectionOperationType.READ_ITEM);

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(containerForClientWithPreferredRegions, readSessionNotAvailableRules)
                .block();

            try {

                CosmosItemResponse<TestItem> itemResponse = containerForClientWithPreferredRegions.readItem(
                    createdItem.getId(),
                    new PartitionKey(createdItem.getId()),
                    new CosmosItemRequestOptions().setExcludedRegions(chooseExclusionRegions.apply(this.preferredRegions)),
                    TestItem.class).block();

                validateResponse(
                    new OperationExecutionResult<TestItem>(
                        itemResponse,
                        null),
                    expectedResult);

            } catch (Exception exception) {

                if (exception instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                    validateResponse(
                        new OperationExecutionResult<TestItem>(
                            null,
                            cosmosException),
                        expectedResult);
                } else {
                    fail("A CosmosException instance should have been thrown.");
                }
            }

        } finally {
            System.clearProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED");
            safeCloseAsync(clientWithPreferredRegions);
        }
    }

    // todo: remove region hardcoding
    @Test(groups = { "multi-master" }, dataProvider = "regionExclusionReadAfterCreateTestConfigs")
    public void regionExclusionMutationOnClient_readAfterCreate_test(MutationTestConfig mutationTestConfig) throws InterruptedException {
        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(2));

        CosmosAsyncClient clientWithPreferredRegions = null;

        List<String> excludeRegions = mutationTestConfig.chooseInitialExclusionRegions.apply(this.preferredRegions);

        try {
            clientWithPreferredRegions = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .endpointDiscoveryEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.cosmosAsyncClient).getConsistencyLevel())
                .preferredRegions(this.preferredRegions)
                .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED).build())
                .directMode()
                .excludeRegions(excludeRegions)
                .buildAsyncClient();

            CosmosAsyncContainer containerForClientWithPreferredRegions = clientWithPreferredRegions
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

            TestItem createdItem = TestItem.createNewItem();

            Thread.sleep(5_000);

            containerForClientWithPreferredRegions.createItem(createdItem).block();

            List<String> faultInjectionRegions =
                mutationTestConfig.chooseFaultInjectionRegions.apply(this.preferredRegions);

            List<FaultInjectionRule> readSessionNotAvailableRules = buildFaultInjectionRules(
                faultInjectionRegions,
                mutationTestConfig.faultInjectionOperationType,
                mutationTestConfig.faultInjectionServerErrorType);

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(containerForClientWithPreferredRegions, readSessionNotAvailableRules)
                .block();

            try {

                CosmosItemResponse<TestItem> itemResponse = containerForClientWithPreferredRegions.readItem(
                    createdItem.getId(),
                    new PartitionKey(createdItem.getId()),
                    new CosmosItemRequestOptions(),
                    TestItem.class).block();

                validateResponse(
                    new OperationExecutionResult<TestItem>(
                        itemResponse,
                        null),
                    mutationTestConfig.expectedResultBeforeMutation);

            } catch (Exception exception) {

                if (exception instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                    // todo: add exception based tests
                    validateResponse(
                        new OperationExecutionResult<TestItem>(
                            null,
                            cosmosException),
                        mutationTestConfig.expectedResultBeforeMutation);
                } else {
                    fail("A CosmosException instance should have been thrown.");
                }
            }

            Function<List<String>, List<String>> regionExclusionMutators = mutationTestConfig.regionExclusionMutator;

            try {

                List<String> mutatedExcludedRegions = regionExclusionMutators.apply(this.preferredRegions);

                clientWithPreferredRegions.setExcludeRegions(mutatedExcludedRegions);

                CosmosItemResponse<TestItem> itemResponse = containerForClientWithPreferredRegions.readItem(
                    createdItem.getId(),
                    new PartitionKey(createdItem.getId()),
                    new CosmosItemRequestOptions(),
                    TestItem.class).block();

                validateResponse(
                    new OperationExecutionResult<TestItem>(
                        itemResponse,
                        null),
                    mutationTestConfig.expectedResultAfterMutation);

            } catch (Exception exception) {

                if (exception instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                    // todo: add exception based tests
                    validateResponse(
                        new OperationExecutionResult<TestItem>(
                            null,
                            cosmosException),
                        mutationTestConfig.expectedResultAfterMutation);
                } else {
                    fail("A CosmosException instance should have been thrown.");
                }
            }

        } finally {
            System.clearProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED");
            safeCloseAsync(clientWithPreferredRegions);
        }
    }

    @AfterClass(groups = {"multi-master"})
    public void afterClass() {
        safeCloseAsync(this.cosmosAsyncClient);
    }

    private static List<FaultInjectionRule> buildFaultInjectionRules(
        List<String> faultInjectionRegions,
        FaultInjectionOperationType faultInjectionOperationType,
        FaultInjectionServerErrorType faultInjectionServerErrorType) {

        switch (faultInjectionServerErrorType) {
            case READ_SESSION_NOT_AVAILABLE:
                return buildReadSessionNotAvailableRules(faultInjectionRegions, faultInjectionOperationType);
            case INTERNAL_SERVER_ERROR:
                return buildInternalServerErrorRules(faultInjectionRegions, faultInjectionOperationType);
            case SERVICE_UNAVAILABLE:
                return buildServiceUnavailableRules(faultInjectionRegions, faultInjectionOperationType);
            default:
                throw new IllegalArgumentException("Unsupported fault-injection server error type");
        }
    }

    private static List<FaultInjectionRule> buildServiceUnavailableRules(List<String> faultInjectionRegions, FaultInjectionOperationType faultInjectionOperationType) {
        List<FaultInjectionRule> serviceUnavailableRules = new ArrayList<>();

        FaultInjectionServerErrorResult serviceUnavailableServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
            .build();

        FaultInjectionRuleBuilder serviceUnavailableRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-serviceUnavailable-" + UUID.randomUUID());

        for (String faultInjectionRegion : faultInjectionRegions) {
            FaultInjectionCondition faultInjectionConditionForRegion = new FaultInjectionConditionBuilder()
                .operationType(faultInjectionOperationType)
                .region(faultInjectionRegion)
                .build();

            FaultInjectionRule serviceUnavailableRule = serviceUnavailableRuleBuilder
                .condition(faultInjectionConditionForRegion)
                .result(serviceUnavailableServerErrorResult)
                .duration(Duration.ofMinutes(10))
                .build();

            serviceUnavailableRules.add(serviceUnavailableRule);
        }

        return serviceUnavailableRules;

    }

    private static List<FaultInjectionRule> buildInternalServerErrorRules(List<String> faultInjectionRegions, FaultInjectionOperationType faultInjectionOperationType) {
        List<FaultInjectionRule> internalServerErrorRules = new ArrayList<>();

        FaultInjectionServerErrorResult internalServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
            .build();

        FaultInjectionRuleBuilder internalServerErrorRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-internalServerError-" + UUID.randomUUID());

        for (String faultInjectionRegion : faultInjectionRegions) {
            FaultInjectionCondition faultInjectionConditionForRegion = new FaultInjectionConditionBuilder()
                .operationType(faultInjectionOperationType)
                .region(faultInjectionRegion)
                .build();

            FaultInjectionRule internalServerErrorRule = internalServerErrorRuleBuilder
                .condition(faultInjectionConditionForRegion)
                .result(internalServerErrorResult)
                .duration(Duration.ofMinutes(10))
                .build();

            internalServerErrorRules.add(internalServerErrorRule);
        }

        return internalServerErrorRules;

    }

    private static List<FaultInjectionRule> buildReadSessionNotAvailableRules(List<String> faultInjectionRegions, FaultInjectionOperationType faultInjectionOperationType) {

        List<FaultInjectionRule> readSessionNotAvailableRules = new ArrayList<>();

        FaultInjectionServerErrorResult readSessionNotAvailableServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        FaultInjectionRuleBuilder readSessionNotAvailableRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-readSessionNotAvailable-" + UUID.randomUUID());

        for (String faultInjectionRegion : faultInjectionRegions) {
            FaultInjectionCondition faultInjectionConditionForRegion = new FaultInjectionConditionBuilder()
                .operationType(faultInjectionOperationType)
                .region(faultInjectionRegion)
                .build();

            FaultInjectionRule readSessionNotAvailableRule = readSessionNotAvailableRuleBuilder
                .condition(faultInjectionConditionForRegion)
                .result(readSessionNotAvailableServerErrorResult)
                .duration(Duration.ofMinutes(10))
                .build();

            readSessionNotAvailableRules.add(readSessionNotAvailableRule);
        }

        return readSessionNotAvailableRules;
    }

    private static List<String> chooseLastTwoRegions(List<String> regions) {
        assertThat(regions).isNotNull();
        assertThat(regions.size()).isGreaterThanOrEqualTo(2);

        int regionsCount = regions.size();

        return regions.subList(regionsCount - 2, regionsCount);
    }

    private static List<String> chooseFirstTwoRegions(List<String> regions) {
        assertThat(regions).isNotNull();
        assertThat(regions.size()).isGreaterThanOrEqualTo(2);

        return regions.subList(0, 2);
    }

    private static List<String> chooseKthRegion(List<String> regions, int k) {
        int regionCount = regions == null ? 0 : regions.size();

        if (regionCount == 0) {
            return new ArrayList<>();
        }

        if (k < 1 || k > regionCount) {
            throw new IllegalArgumentException("Choose a 1-indexed value which is within the boundary of the size of the list.");
        }

        return Arrays.asList(regions.get(k - 1));
    }

    private static List<String> chooseLastRegion(List<String> regions) {
        int regionCount = regions == null ? 0 : regions.size();

        if (regionCount == 0) {
            return new ArrayList<>();
        }

        return Arrays.asList(regions.get(regionCount - 1));
    }

    private static <T> void validateResponse(OperationExecutionResult<T> operationExecutionResult, ExpectedResult expectedResult) {

        if (operationExecutionResult.cosmosItemResponse != null) {

            CosmosItemResponse<T> cosmosItemResponse = operationExecutionResult.cosmosItemResponse;

            assertThat(cosmosItemResponse.getStatusCode()).isEqualTo(expectedResult.expectedStatusCode);

            Set<String> actualContactedRegionNames = cosmosItemResponse.getDiagnostics().getContactedRegionNames();

            assertThat(actualContactedRegionNames.size()).isEqualTo(expectedResult.expectedContactedRegionNames.size());

            for (String expectedContactedRegionName : expectedResult.expectedContactedRegionNames) {
                assertThat(actualContactedRegionNames.contains(expectedContactedRegionName.toLowerCase(Locale.ROOT))).isTrue();
            }

        } else if (operationExecutionResult.cosmosException != null) {
            CosmosException cosmosException = operationExecutionResult.cosmosException;

            assertThat(cosmosException.getStatusCode()).isEqualTo(expectedResult.expectedStatusCode);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(expectedResult.expectedSubStatusCode);

            Set<String> actualContactedRegionNames = cosmosException.getDiagnostics().getContactedRegionNames();

            assertThat(actualContactedRegionNames.size()).isEqualTo(expectedResult.expectedContactedRegionNames.size());

            for (String expectedContactedRegionName : expectedResult.expectedContactedRegionNames) {
                assertThat(actualContactedRegionNames.contains(expectedContactedRegionName.toLowerCase(Locale.ROOT))).isTrue();
            }
        }
    }

    private static class OperationExecutionResult<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;

        OperationExecutionResult(CosmosItemResponse<T> cosmosItemResponse, CosmosException cosmosException) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = cosmosException;
        }
    }

    private static class ExpectedResult {
        private final int expectedStatusCode;
        private final int expectedSubStatusCode;
        private final List<String> expectedContactedRegionNames;

        ExpectedResult(
            int expectedStatusCode,
            int expectedSubStatusCode,
            List<String> expectedContactedRegionNames) {

            this.expectedStatusCode = expectedStatusCode;
            this.expectedSubStatusCode = expectedSubStatusCode;
            this.expectedContactedRegionNames = expectedContactedRegionNames;
        }
    }

    private static class MutationTestConfig {
        private Function<List<String>, List<String>> chooseFaultInjectionRegions
            = (regions) -> new ArrayList<>();
        private Function<List<String>, List<String>> chooseInitialExclusionRegions
            = (regions) -> new ArrayList<>();
        private FaultInjectionOperationType faultInjectionOperationType = FaultInjectionOperationType.READ_ITEM;
        private FaultInjectionServerErrorType faultInjectionServerErrorType = FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE;
        private Function<List<String>, List<String>> regionExclusionMutator
            = (regions) -> new ArrayList<>();
        private ExpectedResult expectedResultBeforeMutation = null;
        private ExpectedResult expectedResultAfterMutation = null;

        public MutationTestConfig withChooseFaultInjectionRegions(
            Function<List<String>, List<String>> chooseFaultInjectionRegions) {
            this.chooseFaultInjectionRegions = chooseFaultInjectionRegions;
            return this;
        }

        public MutationTestConfig withChooseInitialExclusionRegions(
            Function<List<String>, List<String>> chooseInitialExclusionRegions) {
            this.chooseInitialExclusionRegions = chooseInitialExclusionRegions;
            return this;
        }

        public MutationTestConfig withFaultInjectionOperationType(FaultInjectionOperationType faultInjectionOperationType) {
            this.faultInjectionOperationType = faultInjectionOperationType;
            return this;
        }

        public MutationTestConfig withFaultInjectionServerErrorType(FaultInjectionServerErrorType faultInjectionServerErrorType) {
            this.faultInjectionServerErrorType = faultInjectionServerErrorType;
            return this;
        }

        public MutationTestConfig withRegionExclusionMutator(
            Function<List<String>, List<String>> regionExclusionMutator) {
            this.regionExclusionMutator = regionExclusionMutator;
            return this;
        }

        public MutationTestConfig withExpectedResultBeforeMutation(ExpectedResult expectedResultBeforeMutation) {
            this.expectedResultBeforeMutation = expectedResultBeforeMutation;
            return this;
        }

        public MutationTestConfig withExpectedResultAfterMutation(ExpectedResult expectedResultAfterMutation) {
            this.expectedResultAfterMutation = expectedResultAfterMutation;
            return this;
        }
    }
}
