// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosExcludedRegions;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.SessionRetryOptionsBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
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
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class ExcludedRegionWithFaultInjectionTests extends FaultInjectionTestBase {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private List<String> preferredRegions;
    private String regionResolvedForDefaultEndpoint;
    private Function<List<String>, List<String>> chooseLastTwoRegions = (regions) -> chooseLastTwoRegions(regions);
    private Function<List<String>, List<String>> chooseFirstTwoRegions = (regions) -> chooseFirstTwoRegions(regions);
    private Function<List<String>, List<String>> chooseFirstRegion = (regions) -> chooseKthRegion(regions, 1);
    private Function<List<String>, List<String>> chooseSecondRegion = (regions) -> chooseKthRegion(regions, 2);
    private Function<List<String>, List<String>> chooseThirdRegion = (regions) -> chooseKthRegion(regions, 3);
    private Function<List<String>, List<String>> chooseLastRegion = (regions) -> chooseLastRegion(regions);
    private Function<List<String>, List<String>> chooseAllRegions = Function.identity();
    private Map<String, String> readRegionMap;
    private Map<String, String> writeRegionMap;

    @Factory(dataProvider = "clientBuilderSolelyDirectWithSessionConsistency")
    public ExcludedRegionWithFaultInjectionTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    // The objective of this test suite is the following:
    //      1. Test for two validation scenarios - where the operation fails with a 404, 503 or a 500 status code or the
    //      operation succeeds with a 200, 201 or 204 status code.
    //      2. Inject faults which could trigger a request to be routed to a different region such as 404/1002 or 503/21008.
    //      3. Validate the expected status of an eventually succeeding or an eventually failing request.
    //      4. Validate the regions contacted when an operation is executed with an initial list of excluded regions and
    //      do one more round of validation when the operation with the same operation type is re-executed after mutating
    //      this list of excluded regions.
    //      5. Validate the regions contacted when an operation is executed with CosmosItemRequestOptions / CosmosQueryRequestOptions
    //      with excluded regions set on the request option. Here, client-level excluded regions is overridden.
    //      6. Repeat the above steps with various operation types such as a query, batch, bulk, point reads and point writes. Choose
    //      various combinations of regions to exclude and regions to inject faults into.
    //      7. Test with the following combination excludedRegions:
    //          a) list with excludedRegions which is a sub-list of preferredRegions
    //          b) list with excludedRegions which is a sub-list of preferredRegions and has duplicates
    //          c) list with excludedRegions which is not a sub-list of preferredRegions and has no duplicates
    //          d) list with excludedRegions which is not a sub-list of preferredRegions and has duplicates
    //          e) list which is null
    //          f) list which is empty
    //          g) list with excludedRegions which is the same as preferredRegions
    // MutationTestConfig is used to encapsulate all test related configs, here is a description of each property:
    //    1. chooseFaultInjectionRegions: list of regions to inject a fault into
    //    2. chooseInitialExclusionRegions: list of regions to exclude configured when building CosmosClient / CosmosAsyncClient
    //    3. faultInjectionOperationType: the operation type for which the fault is to be injected
    //    4. faultInjectionServerErrorType: the type of fault to inject
    //    5. regionExclusionMutator: the list of excluded regions to be set on an existing client thereby mutating previously configured excluded regions
    //    6. dataPlaneOperationExecutor: the callback which executes the data plane operation
    //    7. expectedResultBeforeMutation: the expected result before the excluded regions mutation is done
    //    8. expectedResultAfterMutation: the expected result after the excluded regions mutation is done
    //    9. nonIdempotentWritesEnabled: a boolean flag to denote whether non-idempotent retry of writes are enabled or not
    //    10. patchItemRequestOptionsForCallbackAfterMutation: a CosmosItemRequestOptions instance configured to be set on the data plane operation
    //    after mutation is done. used by patchItemCallback
    //    11. itemRequestOptionsForCallbackAfterMutation: a CosmosItemRequestOptions instance configured to be set on the data plane operation
    //    after mutation is done.
    //    12. queryRequestOptionsForCallbackAfterMutation: a CosmosItemRequestOptions instance configured to be set on the data plane operation
    //    after mutation is done. used by the queryItemCallback
    //    13. bulkExecutionOptions: a CosmosBulkExecutionOptions instance configured to set on the data plane operation after mutation is done
    //    14. batchRequestOptions: a CosmosBatchRequestOptions instance configured to set on the data plane operation after mutation is done
    //    15. perRegionDuplicateCount: no. of times to duplicate a particular region in excludedRegions
    @BeforeClass(groups = {"multi-master"})
    public void beforeClass() {
        this.cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.cosmosAsyncClient);

        AsyncDocumentClient asyncDocumentClient = BridgeInternal.getContextClient(this.cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = asyncDocumentClient.getGlobalEndpointManager();
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

        this.readRegionMap = getRegionMap(databaseAccount, false);
        this.writeRegionMap = getRegionMap(databaseAccount, true);
        this.preferredRegions = this.writeRegionMap.keySet().stream().collect(Collectors.toList());
        this.regionResolvedForDefaultEndpoint = getRegionResolvedForDefaultEndpoint(this.cosmosAsyncContainer, this.preferredRegions);
    }

    @DataProvider(name = "regionExclusionReadAfterCreateTestConfigs")
    public Object[][] regionExclusionReadAfterCreateTestConfigs() {

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> readItemCallback =
            (params) -> {

                TestItem alreadyCreatedItem = params.createdItem;

                try {

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer
                        .readItem(
                            alreadyCreatedItem.getId(),
                            new PartitionKey(alreadyCreatedItem.getId()),
                            TestItem.class)
                        .block();

                    return new OperationExecutionResult<>(response);
                } catch (Exception exception) {

                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Object[][] testConfigs_readAfterCreate = null;

        if (this.preferredRegions.size() == 2) {
            testConfigs_readAfterCreate = new Object[][] {
                {
                    "404/1002_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "404/1002_noRegion_beforeMutation_noExcludeRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator(this.chooseFirstRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            Arrays.asList(this.chooseFirstRegion.apply(this.preferredRegions).get(0)))
                        )
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            Arrays.asList(this.chooseSecondRegion.apply(this.preferredRegions).get(0))
                        )
                    )
                },
                {
                    "404/1002_noRegion_beforeMutation_noExcludeRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
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
                    ))
                },
                {
                    "503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseSecondRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeAllRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator((regions) -> regions)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeIncorrectRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>(Arrays.asList("Non-existent region 1", "Non-existent region 2")))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "500/0_allRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeSecondRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(readItemCallback)
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
                },
                {
                    "500/0_allRegions_beforeMutation_excludeFirstRegion_withDuplicates_afterMutation_excludeSecondRegion_withDuplicates",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withPerRegionDuplicateCount(3)
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
                }
            };

            return addBooleanFlagsToAllTestConfigs(testConfigs_readAfterCreate);

        } else if (this.preferredRegions.size() == 3) {
            testConfigs_readAfterCreate = new Object[][] {
                {
                    "404/1002_firstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
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
                    ))
                },
                {
                    "404/1002_firstTwoRegions_beforeMutation_excludeSecondRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseSecondRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
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
                },
                {
                    "404/1002_noRegions_beforeMutation_noExcludeRegions_afterMutation_excludeFirstRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
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
                },
                {
                    "503/21008_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(readItemCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "500/0_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(readItemCallback)
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
                }
            };

            return addBooleanFlagsToAllTestConfigs(testConfigs_readAfterCreate);
        }

        throw new IllegalStateException("This test suite is tested for 2 or 3 preferred regions");
    }

    @DataProvider(name = "regionExclusionQueryAfterCreateTestConfigs")
    public Object[][] regionExclusionQueryAfterCreateTestConfigs() {
        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> queryItemCallback =
            (params) -> {

                TestItem alreadyCreatedItem = params.createdItem;

                String query = String.format("SELECT * FROM c WHERE c.id = '%s'", alreadyCreatedItem.getId());
                CosmosQueryRequestOptions queryRequestOptions = params.queryRequestOptionsForCallbackAfterMutation != null
                    ? params.queryRequestOptionsForCallbackAfterMutation : new CosmosQueryRequestOptions();

                SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query);

                try {
                    FeedResponse<TestItem> feedResponse = params.cosmosAsyncContainer
                        .queryItems(sqlQuerySpec, queryRequestOptions, TestItem.class)
                        .byPage()
                        .blockFirst();

                    return new OperationExecutionResult<>(feedResponse);
                } catch (Exception exception) {
                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Object[][] testConfig_queryAfterCreate = null;

        if (this.preferredRegions.size() == 2) {
            testConfig_queryAfterCreate = new Object[][] {
                {
                    "404/1002_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(queryItemCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "404/1002_noRegion_beforeMutation_noExcludeRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator(this.chooseFirstRegion)
                        .withDataPlaneOperationExecutor(queryItemCallback)
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
                    )
                },
                {
                    "503/21008_allRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(queryItemCallback)
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
                    ))
                },
                {
                    "503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(queryItemCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeNoRegions_requestOptionsOverride_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(queryItemCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withQueryRequestOptionsForCallbackAfterMutation(
                            new CosmosQueryRequestOptions().setExcludedRegions(this.chooseLastRegion.apply(this.preferredRegions)))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "500/0_allRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeSecondRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(queryItemCallback)
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
                }
            };

            return addBooleanFlagsToAllTestConfigs(testConfig_queryAfterCreate);

        } else if (this.preferredRegions.size() == 3) {
            testConfig_queryAfterCreate = new Object[][] {
                {
                    "404/1002_firstTwoRegions_beforeMutation_excludeSecondRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(queryItemCallback)
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

                },
                {
                    "404/1002_firstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseSecondRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(queryItemCallback)
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
                },
                {
                    "404/1002_firstTwoRegions_beforeMutation_excludeSecondRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator(this.chooseFirstRegion)
                        .withDataPlaneOperationExecutor(queryItemCallback)
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
                },
                {
                    "503/21008_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(queryItemCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "500/0_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(queryItemCallback)
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
                }
            };

            return addBooleanFlagsToAllTestConfigs(testConfig_queryAfterCreate);
        }

        throw new IllegalStateException("This test suite is tested for 2 or 3 preferred regions");
    }

    @DataProvider(name = "regionExclusionWriteAfterCreateTestConfigs")
    public Object[][] regionExclusionWriteAfterCreateTestConfigs() {

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> createAnotherItemCallback =
            (params) -> {

                String newDocumentId = UUID.randomUUID().toString();

                try {

                    CosmosItemRequestOptions itemRequestOptions = params.itemRequestOptionsForCallbackAfterMutation != null ?
                        params.itemRequestOptionsForCallbackAfterMutation : new CosmosItemRequestOptions();

                    itemRequestOptions.setNonIdempotentWriteRetryPolicy(params.nonIdempotentWriteRetriesEnabled, true);

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer
                        .createItem(
                            new TestItem(newDocumentId, newDocumentId, newDocumentId),
                            new PartitionKey(newDocumentId),
                            itemRequestOptions)
                        .block();

                    return new OperationExecutionResult<>(response);
                } catch (Exception exception) {

                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> replaceItemCallback =
            (params) -> {

                TestItem alreadyCreatedItem = params.createdItem;
                alreadyCreatedItem.setProp(UUID.randomUUID().toString());

                CosmosItemRequestOptions itemRequestOptions = params.itemRequestOptionsForCallbackAfterMutation != null ?
                    params.itemRequestOptionsForCallbackAfterMutation : new CosmosItemRequestOptions();

                itemRequestOptions.setNonIdempotentWriteRetryPolicy(params.nonIdempotentWriteRetriesEnabled, true);

                try {

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer
                        .replaceItem(
                           alreadyCreatedItem,
                           alreadyCreatedItem.getId(),
                           new PartitionKey(alreadyCreatedItem.getId()),
                           itemRequestOptions)
                        .block();

                    return new OperationExecutionResult<>(response);
                } catch (Exception exception) {
                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> deleteItemCallback =
            (params) -> {

                TestItem alreadyCreatedItem = params.createdItem;

                CosmosItemRequestOptions itemRequestOptions = params.itemRequestOptionsForCallbackAfterMutation != null ?
                    params.itemRequestOptionsForCallbackAfterMutation : new CosmosItemRequestOptions();

                itemRequestOptions.setNonIdempotentWriteRetryPolicy(params.nonIdempotentWriteRetriesEnabled, true);

                try {

                    CosmosItemResponse<?> response = params.cosmosAsyncContainer
                        .deleteItem(
                            alreadyCreatedItem.getId(),
                            new PartitionKey(alreadyCreatedItem.getId()),
                            itemRequestOptions)
                        .block();

                    return new OperationExecutionResult<>(response);
                } catch (Exception exception) {
                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> upsertExistingItemCallback =
            (params) -> {

                TestItem alreadyCreatedItem = params.createdItem;
                alreadyCreatedItem.setProp(UUID.randomUUID().toString());

                CosmosItemRequestOptions itemRequestOptions = params.itemRequestOptionsForCallbackAfterMutation != null ?
                    params.itemRequestOptionsForCallbackAfterMutation : new CosmosItemRequestOptions();

                itemRequestOptions.setNonIdempotentWriteRetryPolicy(params.nonIdempotentWriteRetriesEnabled, true);

                try {

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer.upsertItem(
                        alreadyCreatedItem,
                        new PartitionKey(alreadyCreatedItem.getId()),
                        itemRequestOptions).block();

                    return new OperationExecutionResult<>(response);

                } catch (Exception exception) {
                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> upsertNonExistingItemCallback =
            (params) -> {

                TestItem newItem = TestItem.createNewItem();

                CosmosItemRequestOptions itemRequestOptions = params.itemRequestOptionsForCallbackAfterMutation != null ?
                params.itemRequestOptionsForCallbackAfterMutation : new CosmosItemRequestOptions();

                itemRequestOptions.setNonIdempotentWriteRetryPolicy(params.nonIdempotentWriteRetriesEnabled, true);

                try {

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer.upsertItem(
                        newItem,
                        new PartitionKey(newItem.getId()),
                        itemRequestOptions).block();

                    return new OperationExecutionResult<>(response);

                } catch (Exception exception) {
                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> patchItemCallback =
            (params) -> {
                CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
                patchOperations.add("/newField", UUID.randomUUID().toString());

                CosmosItemRequestOptions patchItemRequestOptions = params.patchItemRequestOptionsForCallbackAfterMutation != null ?
                    params.patchItemRequestOptionsForCallbackAfterMutation : new CosmosPatchItemRequestOptions();

                patchItemRequestOptions.setNonIdempotentWriteRetryPolicy(params.nonIdempotentWriteRetriesEnabled, true);

                try {
                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer.patchItem(
                        params.createdItem.getId(),
                        new PartitionKey(params.createdItem.getId()),
                        patchOperations,
                        (CosmosPatchItemRequestOptions) patchItemRequestOptions,
                        TestItem.class
                    ).block();

                    return new OperationExecutionResult<>(response);
                } catch (Exception exception) {

                    if (exception instanceof CosmosException) {
                        CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                        return new OperationExecutionResult<>(cosmosException);
                    } else {
                        fail("A CosmosException instance should have been thrown.");
                    }
                }

                return null;
            };

        Object[][] testConfigs_writeAfterCreate = null;

        if (this.preferredRegions.size() == 2) {
            testConfigs_writeAfterCreate = new Object[][] {
                {
                    "create_404/1002_firstRegion_beforeMutation_excludeNoRegions_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withDataPlaneOperationExecutor(createAnotherItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                        HttpConstants.StatusCodes.NOTFOUND,
                        HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                        this.chooseFirstRegion.apply(this.preferredRegions)
                    )),
                },
                {
                    "replace_404/1002_chooseFirstRegion_beforeMutation_excludeNoRegions_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withDataPlaneOperationExecutor(replaceItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator(this.chooseSecondRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    )),
                },
                {
                    "delete_404/1002_allRegions_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(deleteItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                        HttpConstants.StatusCodes.NOTFOUND,
                        HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                        this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "upsertExistingItem_404/1002_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "create_404/1002_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertNonExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "patch_404/1002_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(patchItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "create_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(createAnotherItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "create_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_false",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(createAnotherItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                        .withNonIdempotentWritesEnabled(false)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "create_503/21008_noRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeAllRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                        .withDataPlaneOperationExecutor(createAnotherItemCallback)
                        .withRegionExclusionMutator((regions) -> regions)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            Arrays.asList(this.regionResolvedForDefaultEndpoint)
                    ))
                },
                {
                    "create_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(createAnotherItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                        HttpConstants.StatusCodes.CREATED,
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "replace_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_false",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(replaceItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                        .withNonIdempotentWritesEnabled(false)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                        HttpConstants.StatusCodes.OK,
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "replace_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(replaceItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "replace_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true_requestOptionsOverride_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(replaceItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withItemRequestOptionsForCallbackAfterMutation(
                            new CosmosItemRequestOptions().setExcludedRegions(this.chooseLastRegion.apply(this.preferredRegions)))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    )),
                },
                {
                    "upsertExistingItem_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_false",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withNonIdempotentWritesEnabled(false)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "upsertExistingItem_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "upsertNonExistingItem_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_false",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertNonExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withNonIdempotentWritesEnabled(false)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "upsertNonExistingItem_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertNonExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "delete_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_false",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(deleteItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                        .withNonIdempotentWritesEnabled(false)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NO_CONTENT,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "delete_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(deleteItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NO_CONTENT,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "patch_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_false",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(patchItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                        .withNonIdempotentWritesEnabled(false)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "patch_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(patchItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "patch_503/21008_firstRegion_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions_nonIdempotentWrite_true_requestOptionsOverride_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(patchItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                        .withPatchRequestOptionsForCallbackAfterMutation(
                            new CosmosPatchItemRequestOptions().setExcludedRegions(this.chooseLastRegion.apply(this.preferredRegions)))
                        .withNonIdempotentWritesEnabled(true)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    )),
                }
            };

            addBooleanFlagsToAllTestConfigs(testConfigs_writeAfterCreate);
        } else if (this.preferredRegions.size() == 3) {
            testConfigs_writeAfterCreate = new Object[][] {
                {
                    "create_404/1002_firstRegion_beforeMutation_excludeNoRegions_afterMutation_excludeSecondRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withDataPlaneOperationExecutor(createAnotherItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator(this.chooseSecondRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.CREATE_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                        HttpConstants.StatusCodes.CREATED,
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        Arrays.asList(
                            this.chooseFirstRegion.apply(this.preferredRegions).get(0),
                            this.chooseLastRegion.apply(this.preferredRegions).get(0)
                        )
                    )),
                },
                {
                    "replace_404/1002_firstRegion_beforeMutation_excludeNoRegions_afterMutation_excludeSecondRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withDataPlaneOperationExecutor(replaceItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator(this.chooseSecondRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.REPLACE_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            Arrays.asList(
                                this.chooseFirstRegion.apply(this.preferredRegions).get(0),
                                this.chooseLastRegion.apply(this.preferredRegions).get(0)
                            )
                    )),
                },
                {
                    "delete_404/1002_firstTwoRegions_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(deleteItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.DELETE_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NO_CONTENT,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "upsertExistingItem_404/1002_firstTwoRegions_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                        HttpConstants.StatusCodes.OK,
                        HttpConstants.SubStatusCodes.UNKNOWN,
                        this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "upsertNonExistingItem_404/1002_firstTwoRegions_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(upsertNonExistingItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.UPSERT_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.CREATED,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                },
                {
                    "patch_404/1002_firstTwoRegions_beforeMutation_excludeLastRegion_afterMutation_excludeNoRegion",
                    new MutationTestConfig()
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withChooseInitialExclusionRegions(this.chooseLastRegion)
                        .withDataPlaneOperationExecutor(patchItemCallback)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withFaultInjectionOperationType(FaultInjectionOperationType.PATCH_ITEM)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseAllRegions.apply(this.preferredRegions)
                    )),
                }
            };

            addBooleanFlagsToAllTestConfigs(testConfigs_writeAfterCreate);
        }

        throw new IllegalStateException("This test suite is tested for 2 or 3 preferred regions");
    }

    @DataProvider(name = "regionExclusionBatchTestConfigs")
    public Object[][] regionExclusionBatchTestConfigs() {

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> batchCreateAndReadCallback = (params) -> {

            String documentId = UUID.randomUUID().toString();
            TestItem testItem = new TestItem(documentId, documentId, documentId);

            CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(documentId));

            batch.createItemOperation(testItem);
            batch.readItemOperation(documentId);

            CosmosBatchRequestOptions batchRequestOptions = params.batchRequestOptionsForCallbackAfterMutation != null
                ? params.batchRequestOptionsForCallbackAfterMutation : new CosmosBatchRequestOptions();

            try {

                CosmosBatchResponse batchResponse = params.cosmosAsyncContainer.executeCosmosBatch(batch, batchRequestOptions).block();
                return new OperationExecutionResult<>(batchResponse);

            } catch (Exception exception) {

                if (exception instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                    return new OperationExecutionResult<>(cosmosException);
                }

                fail("A CosmosException instance should have been thrown.");
            }

            return null;
        };

        Object[][] testConfigs_batch = null;

        if (this.preferredRegions.size() == 2) {
            testConfigs_batch =  new Object[][] {
                {
                    "batchCreateAndRead_404/1002_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "batchCreateAndRead_404/1002_noRegion_beforeMutation_noExcludeRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseFirstRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            Arrays.asList(this.chooseFirstRegion.apply(this.preferredRegions).get(0)))
                        )
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            Arrays.asList(this.chooseSecondRegion.apply(this.preferredRegions).get(0))
                        )
                    )
                },
                {
                    "batchCreateAndRead_404/1002_noRegion_beforeMutation_noExcludeRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
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
                    ))
                },
                {
                    "batchCreateAndRead_503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseSecondRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "batchCreateAndRead_503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "batchCreateAndRead_503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeIncorrectRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>(Arrays.asList("Non-existent region 1", "Non-existent region 2")))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "batchCreateAndRead_503/21008_chooseAllRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion_requestOptionsOverride_excludeFirstRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withBatchRequestOptionsForCallbackAfterMutation(
                            new CosmosBatchRequestOptions().setExcludedRegions(this.chooseFirstRegion.apply(this.preferredRegions)))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseLastRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "batchCreateAndRead_500/0_allRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeSecondRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
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
                },
                {
                    "batchCreateAndRead_500/0_allRegions_beforeMutation_excludeFirstRegion_withDuplicates_afterMutation_excludeSecondRegion_withDuplicates",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withPerRegionDuplicateCount(3)
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
                }
            };

            addBooleanFlagsToAllTestConfigs(testConfigs_batch);
        } else if (this.preferredRegions.size() == 3) {
            testConfigs_batch =  new Object[][] {
                {
                    "batchCreateAndRead_404/1002_firstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseLastTwoRegions.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "batchCreateAndRead_404/1002_firstTwoRegions_beforeMutation_excludeSecondRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseSecondRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseFirstTwoRegions)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            Arrays.asList(
                                this.chooseFirstRegion.apply(this.preferredRegions).get(0),
                                this.chooseThirdRegion.apply(this.preferredRegions).get(0))
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            this.chooseLastRegion.apply(this.preferredRegions)
                    )),
                },
                {
                    "batchCreateAndRead_404/1002_noRegions_beforeMutation_noExcludeRegions_afterMutation_excludeFirstRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseFirstRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                                HttpConstants.StatusCodes.OK,
                                HttpConstants.SubStatusCodes.UNKNOWN,
                                2,
                                Arrays.asList(this.chooseFirstRegion.apply(this.preferredRegions).get(0))
                            )
                        )
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            2,
                            Arrays.asList(this.chooseSecondRegion.apply(this.preferredRegions).get(0))
                        )
                    ),
                },
                {
                    "batchCreateAndRead_503/21008_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
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
                    ))
                },
                {
                    "batchCreateAndRead_500/0_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
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
                },
                {
                    "batchCreateAndRead_500/0_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion_requestOptionsOverride_excludeFirstRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(batchCreateAndReadCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withBatchRequestOptionsForCallbackAfterMutation(
                            new CosmosBatchRequestOptions().setExcludedRegions(this.chooseFirstRegion.apply(this.preferredRegions)))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseSecondRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseSecondRegion.apply(this.preferredRegions)
                    ))
                }
            };

            addBooleanFlagsToAllTestConfigs(testConfigs_batch);
        }

        return null;
    }

    @DataProvider(name = "regionExclusionBulkTestConfigs")
    public Object[][] regionExclusionBulkTestConfigs() {

        final int totalCreates = 1;

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> bulkCreateCallback = (params) -> {

            Flux<CosmosItemOperation> createOperationsFlux = Flux.range(0, totalCreates).map(i -> {

                String documentId = UUID.randomUUID().toString();
                TestItem testItem = new TestItem(documentId, documentId, documentId);

                return CosmosBulkOperations.getCreateItemOperation(testItem, new PartitionKey(documentId));

            });

            CosmosBulkExecutionOptions bulkExecutionOptions = params.bulkExecutionOptionsForCallbackAfterMutation != null
                ? params.bulkExecutionOptionsForCallbackAfterMutation : new CosmosBulkExecutionOptions();

            try {

                CosmosBulkOperationResponse<?> bulkOperationResponse = params.cosmosAsyncContainer
                    .executeBulkOperations(createOperationsFlux, bulkExecutionOptions).blockLast();

                if (bulkOperationResponse != null && bulkOperationResponse.getException() != null) {
                    throw bulkOperationResponse.getException();
                }

                return new OperationExecutionResult<>(bulkOperationResponse);

            } catch (Exception exception) {

                if (exception instanceof CosmosException) {
                    CosmosException cosmosException = Utils.as(exception, CosmosException.class);

                    return new OperationExecutionResult<>(cosmosException);
                }

                fail("A CosmosException instance should have been thrown.");
            }

            return null;
        };

        Object[][] testConfigs_bulk = null;

        if (this.preferredRegions.size() == 2) {
            testConfigs_bulk = new Object[][] {
                {
                    "bulkCreate_404/1002_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.NOTFOUND,
                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "bulkCreate_404/1002_noRegion_beforeMutation_noExcludeRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator(this.chooseFirstRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            Arrays.asList(this.chooseFirstRegion.apply(this.preferredRegions).get(0)))
                        )
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            Arrays.asList(this.chooseSecondRegion.apply(this.preferredRegions).get(0))
                        )
                    )
                },
                {
                    "bulkCreate_404/1002_noRegion_beforeMutation_noExcludeRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
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
                    ))
                },
                {
                    "bulkCreate_503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseSecondRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                            HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                            this.chooseFirstRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "bulkCreate_503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeNoRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>())
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "bulkCreate_503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeIncorrectRegions",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>(Arrays.asList("Non-existent region 1", "Non-existent region 2")))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseFirstTwoRegions.apply(this.preferredRegions)
                    ))
                },
                {
                    "bulkCreate_503/21008_firstRegion_beforeMutation_excludeFirstRegion_afterMutation_excludeIncorrectRegions_requestOptionsOverride_excludeFirstRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator((regions) -> new ArrayList<>(Arrays.asList("Non-existent region 1", "Non-existent region 2")))
                        .withBulkExecutionOptionsForCallbackAfterMutation(
                            new CosmosBulkExecutionOptions().setExcludedRegions(this.chooseFirstRegion.apply(this.preferredRegions)))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.OK,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseLastRegion.apply(this.preferredRegions)
                    ))
                },
                {
                    "bulkCreate_500/0_allRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeSecondRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
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
                },
                {
                    "bulkCreate_500/0_allRegions_beforeMutation_excludeFirstRegion_withDuplicates_afterMutation_excludeSecondRegion_withDuplicates",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseAllRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withPerRegionDuplicateCount(3)
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
                }
            };

            addBooleanFlagsToAllTestConfigs(testConfigs_bulk);

        } else if (this.preferredRegions.size() == 3) {
            testConfigs_bulk = new Object[][] {
                {
                    "bulkCreate_404/1002_firstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
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
                    ))
                },
                {
                    "bulkCreate_404/1002_firstTwoRegions_beforeMutation_excludeSecondRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseSecondRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
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
                },
                {
                    "bulkCreate_404/1002_noRegions_beforeMutation_noExcludeRegions_afterMutation_excludeFirstRegion",
                    new MutationTestConfig()
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
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
                },
                {
                    "bulkCreate_503/21008_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
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
                    ))
                },
                {
                    "bulkCreate_500/0_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
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
                },
                {
                    "bulkCreate_500/0_chooseFirstTwoRegions_beforeMutation_excludeFirstRegion_afterMutation_excludeLastRegion_requestOptionsOverride_excludeFirstRegion",
                    new MutationTestConfig()
                        .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                        .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                        .withFaultInjectionOperationType(FaultInjectionOperationType.BATCH_ITEM)
                        .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                        .withDataPlaneOperationExecutor(bulkCreateCallback)
                        .withRegionExclusionMutator(this.chooseLastRegion)
                        .withBulkExecutionOptionsForCallbackAfterMutation(
                            new CosmosBulkExecutionOptions().setExcludedRegions(this.chooseFirstRegion.apply(this.preferredRegions)))
                        .withExpectedResultBeforeMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseSecondRegion.apply(this.preferredRegions)
                        ))
                        .withExpectedResultAfterMutation(new ExpectedResult(
                            HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
                            HttpConstants.SubStatusCodes.UNKNOWN,
                            this.chooseSecondRegion.apply(this.preferredRegions)
                    ))
                }
            };

            addBooleanFlagsToAllTestConfigs(testConfigs_bulk);
        }

        return null;
    }

    @Test(groups = { "multi-master" }, dataProvider = "regionExclusionReadAfterCreateTestConfigs")
    public void regionExclusionMutationOnClient_readAfterCreate_test(String testTitle, MutationTestConfig mutationTestConfig, boolean shouldInjectPreferredRegions) throws InterruptedException {
        logger.info("Test started with title : {}", testTitle);
        execute(mutationTestConfig, shouldInjectPreferredRegions);
    }

    @Test(groups = { "multi-master" }, dataProvider = "regionExclusionQueryAfterCreateTestConfigs")
    public void regionExclusionMutationOnClient_queryAfterCreate_test(String testTitle, MutationTestConfig mutationTestConfig, boolean shouldInjectPreferredRegions) throws InterruptedException {
        logger.info("Test started with title : {}", testTitle);
        execute(mutationTestConfig, shouldInjectPreferredRegions);
    }

    @Test(groups = { "multi-master" }, dataProvider = "regionExclusionWriteAfterCreateTestConfigs")
    public void regionExclusionMutationOnClient_writeAfterCreate_test(String testTitle, MutationTestConfig mutationTestConfig, boolean shouldInjectPreferredRegions) throws InterruptedException {
        logger.info("Test started with title : {}", testTitle);
        execute(mutationTestConfig, shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "regionExclusionBatchTestConfigs")
    public void regionExclusionMutationOnClient_batch_test(String testTitle, MutationTestConfig mutationTestConfig, boolean shouldInjectPreferredRegions) throws InterruptedException {
        logger.info("Test started with title : {}", testTitle);
        execute(mutationTestConfig, shouldInjectPreferredRegions);
    }

    @Test(groups = {"multi-master"}, dataProvider = "regionExclusionBulkTestConfigs")
    public void regionExclusionMutationOnClient_bulk_test(String testTitle, MutationTestConfig mutationTestConfig, boolean shouldInjectPreferredRegions) throws InterruptedException {
        logger.info("Test started with title : {}", testTitle);
        execute(mutationTestConfig, shouldInjectPreferredRegions);
    }

    private void execute(MutationTestConfig mutationTestConfig, boolean shouldInjectPreferredRegions) throws InterruptedException {
        System.setProperty("COSMOS.MAX_RETRIES_IN_LOCAL_REGION_WHEN_REMOTE_REGION_PREFERRED", String.valueOf(2));

        CosmosAsyncClient clientWithPreferredRegions = null;

        List<String> excludedRegions = null;

        AtomicReference<CosmosExcludedRegions> cosmosExcludedRegionsAtomicReference
            = new AtomicReference<>(new CosmosExcludedRegions(new HashSet<>()));

        if (mutationTestConfig.chooseInitialExclusionRegions != null) {
            excludedRegions = mutationTestConfig.chooseInitialExclusionRegions.apply(this.preferredRegions);

            List<String> excludedRegionsCopy = new ArrayList<>();

            if (mutationTestConfig.perRegionDuplicateCount > 1) {

                for (int i = 1; i <= mutationTestConfig.perRegionDuplicateCount - 1; i++) {
                    excludedRegionsCopy.addAll(excludedRegions);
                }

                excludedRegions.addAll(excludedRegionsCopy);
            }

            cosmosExcludedRegionsAtomicReference.set(new CosmosExcludedRegions(new HashSet<>(excludedRegions)));
        }

        try {
            clientWithPreferredRegions = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .endpointDiscoveryEnabled(true)
                .consistencyLevel(BridgeInternal.getContextClient(this.cosmosAsyncClient).getConsistencyLevel())
                .preferredRegions(shouldInjectPreferredRegions ? this.preferredRegions : Collections.emptyList())
                .sessionRetryOptions(new SessionRetryOptionsBuilder().regionSwitchHint(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED).build())
                .excludedRegionsSupplier(cosmosExcludedRegionsAtomicReference::get)
                .directMode()
                .buildAsyncClient();

            CosmosAsyncContainer containerForClientWithPreferredRegions = clientWithPreferredRegions
                .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                .getContainer(this.cosmosAsyncContainer.getId());

            TestItem createdItem = TestItem.createNewItem();

            Thread.sleep(5_000);

            containerForClientWithPreferredRegions.createItem(createdItem).block();

            List<String> faultInjectionRegions =
                mutationTestConfig.chooseFaultInjectionRegions.apply(this.preferredRegions);

            List<FaultInjectionRule> faultInjectionRules = buildFaultInjectionRules(
                faultInjectionRegions,
                mutationTestConfig.faultInjectionOperationType,
                mutationTestConfig.faultInjectionServerErrorType);

            CosmosFaultInjectionHelper
                .configureFaultInjectionRules(containerForClientWithPreferredRegions, faultInjectionRules)
                .block();

            Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> dataPlaneOperationExecutor =
                mutationTestConfig.dataPlaneOperationExecutor;

            ItemOperationInvocationParameters params = new ItemOperationInvocationParameters();

            params.cosmosAsyncContainer = containerForClientWithPreferredRegions;
            params.createdItem = createdItem;
            params.nonIdempotentWriteRetriesEnabled = mutationTestConfig.nonIdempotentWritesEnabled;

            OperationExecutionResult<?> operationExecutionResultBeforeMutation = dataPlaneOperationExecutor.apply(params);
            validateResponse(operationExecutionResultBeforeMutation, mutationTestConfig.expectedResultBeforeMutation);

            Function<List<String>, List<String>> regionExclusionMutators = mutationTestConfig.regionExclusionMutator;

            List<String> mutatedExcludedRegions = regionExclusionMutators.apply(this.preferredRegions);

            if (mutationTestConfig.perRegionDuplicateCount > 1) {

                List<String> excludedRegionsCopy = new ArrayList<>();

                for (int i = 1; i <= mutationTestConfig.perRegionDuplicateCount - 1; i++) {
                    excludedRegionsCopy.addAll(mutatedExcludedRegions);
                }

                mutatedExcludedRegions.addAll(excludedRegionsCopy);
            }

            cosmosExcludedRegionsAtomicReference.set(new CosmosExcludedRegions(new HashSet<>(mutatedExcludedRegions)));

            params.itemRequestOptionsForCallbackAfterMutation = mutationTestConfig.itemRequestOptionsForCallbackAfterMutation;
            params.patchItemRequestOptionsForCallbackAfterMutation = mutationTestConfig.patchItemRequestOptionsForCallbackAfterMutation;
            params.queryRequestOptionsForCallbackAfterMutation = mutationTestConfig.queryRequestOptionsForCallbackAfterMutation;
            params.batchRequestOptionsForCallbackAfterMutation = mutationTestConfig.batchRequestOptionsForCallbackAfterMutation;
            params.bulkExecutionOptionsForCallbackAfterMutation = mutationTestConfig.bulkExecutionOptionsForCallbackAfterMutation;

            OperationExecutionResult<?> operationExecutionResultAfterMutation = dataPlaneOperationExecutor.apply(params);
            validateResponse(operationExecutionResultAfterMutation, mutationTestConfig.expectedResultAfterMutation);

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

        return new ArrayList<>(regions.subList(regionsCount - 2, regionsCount));
    }

    private static List<String> chooseFirstTwoRegions(List<String> regions) {
        assertThat(regions).isNotNull();
        assertThat(regions.size()).isGreaterThanOrEqualTo(2);

        return new ArrayList<>(regions.subList(0, 2));
    }

    private static List<String> chooseKthRegion(List<String> regions, int k) {
        int regionCount = regions == null ? 0 : regions.size();

        if (regionCount == 0) {
            return new ArrayList<>();
        }

        if (k < 1 || k > regionCount) {
            throw new IllegalArgumentException("Choose a 1-indexed value which is within the boundary of the size of the list.");
        }

        return new ArrayList<>(Arrays.asList(regions.get(k - 1)));
    }

    private static List<String> chooseLastRegion(List<String> regions) {
        int regionCount = regions == null ? 0 : regions.size();

        if (regionCount == 0) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(regions.get(regionCount - 1)));
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

        } else if (operationExecutionResult.feedResponse != null) {

            FeedResponse<T> feedResponse = operationExecutionResult.feedResponse;
            CosmosDiagnostics cosmosDiagnostics = feedResponse.getCosmosDiagnostics();

            assertThat(feedResponse.getResults()).isNotNull();
            assertThat(feedResponse.getResults().size()).isEqualTo(1);

            Set<String> actualContactedRegionNames = cosmosDiagnostics.getContactedRegionNames();

            assertThat(actualContactedRegionNames.size()).isEqualTo(expectedResult.expectedContactedRegionNames.size());

            for (String expectedContactedRegionName : expectedResult.expectedContactedRegionNames) {
                assertThat(actualContactedRegionNames.contains(expectedContactedRegionName.toLowerCase(Locale.ROOT))).isTrue();
            }

        } else if (operationExecutionResult.batchResponse != null) {

            CosmosBatchResponse batchResponse = operationExecutionResult.batchResponse;
            CosmosDiagnostics cosmosDiagnostics = batchResponse.getDiagnostics();

            assertThat(batchResponse.getResults()).isNotNull();
            assertThat(batchResponse.getResults().size()).isEqualTo(expectedResult.expectedResultCountInBatch);

            Set<String> actualContactedRegionNames = cosmosDiagnostics.getContactedRegionNames();

            assertThat(actualContactedRegionNames.size()).isEqualTo(expectedResult.expectedContactedRegionNames.size());

            for (String expectedContactedRegionName : expectedResult.expectedContactedRegionNames) {
                assertThat(actualContactedRegionNames.contains(expectedContactedRegionName.toLowerCase(Locale.ROOT))).isTrue();
            }

        } else if (operationExecutionResult.bulkOperationResponse != null) {

            CosmosBulkOperationResponse<?> bulkOperationResponse = operationExecutionResult.bulkOperationResponse;
            CosmosDiagnostics cosmosDiagnostics = bulkOperationResponse.getResponse().getCosmosDiagnostics();

            Set<String> actualContactedRegionNames = cosmosDiagnostics.getContactedRegionNames();

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

    private static Map<String, String> getRegionMap(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());
        }

        return regionMap;
    }

    private static String getRegionResolvedForDefaultEndpoint(CosmosAsyncContainer container, List<String> preferredRegions) {
        TestItem testItem = TestItem.createNewItem();
        CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();
        itemRequestOptions.setExcludedRegions(preferredRegions);

        CosmosItemResponse<TestItem> response = container.createItem(testItem, itemRequestOptions).block();

        Set<String> contactedRegion = response.getDiagnostics().getContactedRegionNames();

        assert contactedRegion.size() == 1;

        return contactedRegion.stream().findFirst().get();
    }

    private static class OperationExecutionResult<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;
        private final FeedResponse<T> feedResponse;
        private final CosmosBatchResponse batchResponse;
        private final CosmosBulkOperationResponse<T> bulkOperationResponse;

        OperationExecutionResult(FeedResponse<T> feedResponse) {
            this.feedResponse = feedResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.batchResponse = null;
            this.bulkOperationResponse = null;
        }

        OperationExecutionResult(CosmosItemResponse<T> cosmosItemResponse) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = null;
            this.feedResponse = null;
            this.batchResponse = null;
            this.bulkOperationResponse = null;
        }

        OperationExecutionResult(CosmosException cosmosException) {
            this.cosmosException = cosmosException;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = null;
            this.bulkOperationResponse = null;
        }

        OperationExecutionResult(CosmosBatchResponse batchResponse) {
            this.batchResponse = batchResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.bulkOperationResponse = null;
        }

        OperationExecutionResult(CosmosBulkOperationResponse<T> bulkOperationResponse) {
            this.bulkOperationResponse = bulkOperationResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }
    }

    private static class ExpectedResult {
        private final int expectedStatusCode;
        private final int expectedSubStatusCode;
        private final int expectedResultCountInBatch;
        private final List<String> expectedContactedRegionNames;

        ExpectedResult(
            int expectedStatusCode,
            int expectedSubStatusCode,
            List<String> expectedContactedRegionNames) {

            this.expectedStatusCode = expectedStatusCode;
            this.expectedSubStatusCode = expectedSubStatusCode;
            this.expectedContactedRegionNames = expectedContactedRegionNames;
            this.expectedResultCountInBatch = -1;
        }

        ExpectedResult(
            int expectedStatusCode,
            int expectedSubStatusCode,
            int expectedResultCountInBatch,
            List<String> expectedContactedRegionNames) {

            this.expectedStatusCode = expectedStatusCode;
            this.expectedSubStatusCode = expectedSubStatusCode;
            this.expectedResultCountInBatch = expectedResultCountInBatch;
            this.expectedContactedRegionNames = expectedContactedRegionNames;
        }
    }

    private Object[][] addBooleanFlagsToAllTestConfigs(Object[][] testConfigs) {
        List<List<Object>> intermediateTestConfigList = new ArrayList<>();
        boolean[] shouldInjectPreferredRegionsFlags = new boolean[]{true, false};

        for (boolean shouldInjectPreferredRegionsFlag : shouldInjectPreferredRegionsFlags) {
            for (Object[] testConfigForSingleTest : testConfigs) {
                List<Object> testConfigForSingleTestAsMutableList = new ArrayList<>(Arrays.asList(testConfigForSingleTest));
                testConfigForSingleTestAsMutableList.add(shouldInjectPreferredRegionsFlag);
                intermediateTestConfigList.add(testConfigForSingleTestAsMutableList);
            }
        }

        testConfigs = intermediateTestConfigList.stream()
            .map(l -> l.stream().toArray(Object[]::new))
            .toArray(Object[][]::new);

        return testConfigs;
    }

    private static class MutationTestConfig {
        private Function<List<String>, List<String>> chooseFaultInjectionRegions
            = (regions) -> new ArrayList<>();
        private Function<List<String>, List<String>> chooseInitialExclusionRegions
            = null;
        private FaultInjectionOperationType faultInjectionOperationType = FaultInjectionOperationType.READ_ITEM;
        private FaultInjectionServerErrorType faultInjectionServerErrorType = FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE;
        private Function<List<String>, List<String>> regionExclusionMutator
            = (regions) -> new ArrayList<>();
        private Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> dataPlaneOperationExecutor = null;
        private ExpectedResult expectedResultBeforeMutation = null;
        private ExpectedResult expectedResultAfterMutation = null;
        private boolean nonIdempotentWritesEnabled = false;
        private CosmosItemRequestOptions patchItemRequestOptionsForCallbackAfterMutation = null;
        private CosmosItemRequestOptions itemRequestOptionsForCallbackAfterMutation = null;
        private CosmosQueryRequestOptions queryRequestOptionsForCallbackAfterMutation = null;
        private CosmosBulkExecutionOptions bulkExecutionOptionsForCallbackAfterMutation = null;
        private CosmosBatchRequestOptions batchRequestOptionsForCallbackAfterMutation = null;
        private int perRegionDuplicateCount = 1;

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

        public MutationTestConfig withDataPlaneOperationExecutor(
            Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> dataPlaneOperationExecutor) {
            this.dataPlaneOperationExecutor = dataPlaneOperationExecutor;
            return this;
        }

        public MutationTestConfig withNonIdempotentWritesEnabled(boolean nonIdempotentWritesEnabled) {
            this.nonIdempotentWritesEnabled = nonIdempotentWritesEnabled;
            return this;
        }

        public MutationTestConfig withItemRequestOptionsForCallbackAfterMutation(CosmosItemRequestOptions itemRequestOptions) {
            this.itemRequestOptionsForCallbackAfterMutation = itemRequestOptions;
            return this;
        }

        public MutationTestConfig withPatchRequestOptionsForCallbackAfterMutation(CosmosItemRequestOptions patchItemRequestOptions) {
            this.patchItemRequestOptionsForCallbackAfterMutation = patchItemRequestOptions;
            return this;
        }

        public MutationTestConfig withQueryRequestOptionsForCallbackAfterMutation(CosmosQueryRequestOptions queryRequestOptionsForCallbackAfterMutation) {
            this.queryRequestOptionsForCallbackAfterMutation = queryRequestOptionsForCallbackAfterMutation;
            return this;
        }

        public MutationTestConfig withBulkExecutionOptionsForCallbackAfterMutation(CosmosBulkExecutionOptions bulkExecutionOptionsForCallbackAfterMutation) {
            this.bulkExecutionOptionsForCallbackAfterMutation = bulkExecutionOptionsForCallbackAfterMutation;
            return this;
        }

        public MutationTestConfig withBatchRequestOptionsForCallbackAfterMutation(CosmosBatchRequestOptions batchRequestOptionsForCallbackAfterMutation) {
            this.batchRequestOptionsForCallbackAfterMutation = batchRequestOptionsForCallbackAfterMutation;
            return this;
        }

        public MutationTestConfig withPerRegionDuplicateCount(int perRegionDuplicateCount) {
            this.perRegionDuplicateCount = perRegionDuplicateCount;
            return this;
        }
    }

    private static class ItemOperationInvocationParameters {
        public boolean nonIdempotentWriteRetriesEnabled;
        public CosmosAsyncContainer cosmosAsyncContainer;
        public TestItem createdItem;
        public CosmosItemRequestOptions itemRequestOptionsForCallbackAfterMutation;
        public CosmosItemRequestOptions patchItemRequestOptionsForCallbackAfterMutation;
        public CosmosQueryRequestOptions queryRequestOptionsForCallbackAfterMutation;
        public CosmosBulkExecutionOptions bulkExecutionOptionsForCallbackAfterMutation;
        public CosmosBatchRequestOptions batchRequestOptionsForCallbackAfterMutation;
    }
}
