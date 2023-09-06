package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.GlobalEndpointManager;
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

    @Factory(dataProvider = "clientBuilderSolelyDirectWithSessionConsistency")
    public ExcludeRegionWithFaultInjectionTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    @BeforeClass(groups = {"multi-master"})
    public void beforeClass() {
        this.cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();

        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.cosmosAsyncClient);

        // todo: do not hardcode here and adapt this test suite to make use of 2 preferred regions
        this.preferredRegions = Arrays.asList("East US", "South Central US", "West US");
    }

    @DataProvider(name = "readSessionNotAvailableTestConfigs")
    public Object[][] readSessionNotAvailableTestConfigs() {

        Function<List<String>, List<String>> chooseLastTwoRegions = (regions) -> chooseLastTwoRegions(regions);
        Function<List<String>, List<String>> chooseFirstTwoRegions = (regions) -> chooseFirstTwoRegions(regions);
        Function<List<String>, List<String>> chooseFirstRegion = (regions) -> chooseKthRegion(regions, 1);
        Function<List<String>, List<String>> chooseSecondRegion = (regions) -> chooseKthRegion(regions, 2);
        Function<List<String>, List<String>> chooseThirdRegion = (regions) -> chooseKthRegion(regions, 3);

        return new Object[][] {
            {
                // fault injection regions
                chooseLastTwoRegions, 
                new ExpectedResult(
                    HttpConstants.StatusCodes.NOTFOUND,
                    HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                    // expected contacted regions
                    chooseLastTwoRegions.apply(this.preferredRegions)), 
                // exclude regions
                chooseFirstRegion
            },
            {
                chooseFirstTwoRegions, 
                new ExpectedResult(
                    HttpConstants.StatusCodes.NOTFOUND,
                    HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                    chooseFirstTwoRegions.apply(this.preferredRegions)), 
                chooseThirdRegion
            },
            {
                chooseFirstRegion,
                new ExpectedResult(
                    HttpConstants.StatusCodes.OK,
                    HttpConstants.SubStatusCodes.UNKNOWN,
                    Arrays.asList(
                        chooseFirstRegion.apply(this.preferredRegions).get(0),
                        chooseThirdRegion.apply(this.preferredRegions).get(0))),
                chooseSecondRegion
            }
        };
    }

    @Test(groups = {"multi-master"}, dataProvider = "readSessionNotAvailableTestConfigs", enabled = true)
    public void readSessionNotAvailable_withExcludeRegion_test(
        Function<List<String>, List<String>> chooseFaultInjectionRegions,
        ExpectedResult expectedResult,
        Function<List<String>, List<String>> chooseExclusionRegions) {

        CosmosAsyncClient clientWithPreferredRegions = null;

        try {
            clientWithPreferredRegions = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
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

    @AfterClass(groups = {"multi-master"})
    public void afterClass() {
        safeCloseAsync(this.cosmosAsyncClient);
    }

    private static List<FaultInjectionRule> buildReadSessionNotAvailableRules(List<String> preferredRegions, FaultInjectionOperationType faultInjectionOperationType) {

        List<FaultInjectionRule> readSessionNotAvailableRules = new ArrayList<>();

        FaultInjectionServerErrorResult readSessionNotAvailableServerErrorResult = FaultInjectionResultBuilders
            .getResultBuilder(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
            .build();

        FaultInjectionRuleBuilder readSessionNotAvailableRuleBuilder = new FaultInjectionRuleBuilder("serverErrorRule-readSessionNotAvailable-" + UUID.randomUUID());

        for (String preferredRegion : preferredRegions) {
            FaultInjectionCondition faultInjectionConditionForRegion = new FaultInjectionConditionBuilder()
                .operationType(faultInjectionOperationType)
                .region(preferredRegion)
                .build();

            FaultInjectionRule readSessionNotAvailableRule = readSessionNotAvailableRuleBuilder
                .condition(faultInjectionConditionForRegion)
                .result(readSessionNotAvailableServerErrorResult)
                .duration(Duration.ofSeconds(10))
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
}
