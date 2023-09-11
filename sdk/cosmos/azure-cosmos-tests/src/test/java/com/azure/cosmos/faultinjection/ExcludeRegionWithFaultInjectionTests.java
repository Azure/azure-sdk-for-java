// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
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
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private Function<List<String>, List<String>> chooseAllRegions = Function.identity();
    private Map<String, String> readRegionMap;
    private Map<String, String> writeRegionMap;

    @Factory(dataProvider = "clientBuilderSolelyDirectWithSessionConsistency")
    public ExcludeRegionWithFaultInjectionTests(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

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
    }

    // todo: add scenarios for failover on 503s after fabian's ClientRetryPolicy improvement
    @DataProvider(name = "regionExclusionReadAfterCreateTestConfigs")
    public Object[] regionExclusionReadAfterCreateTestConfigs() {

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

        if (this.preferredRegions.size() == 2) {
            return new Object[] {
                new MutationTestConfig()
                    .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                    .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .withDataPlaneOperationExecutor(readItemCallback)
                    // applied to the preferred regions
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
                )),
                new MutationTestConfig()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .withDataPlaneOperationExecutor(readItemCallback)
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
                    .withDataPlaneOperationExecutor(readItemCallback)
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
                    .withDataPlaneOperationExecutor(readItemCallback)
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
        } else if (this.preferredRegions.size() == 3) {
            return new Object[] {
                new MutationTestConfig()
                    .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                    .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                    .withFaultInjectionOperationType(FaultInjectionOperationType.READ_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .withDataPlaneOperationExecutor(readItemCallback)
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
                    .withDataPlaneOperationExecutor(readItemCallback)
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
                    .withDataPlaneOperationExecutor(readItemCallback)
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
                    .withDataPlaneOperationExecutor(readItemCallback)
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
                    .withDataPlaneOperationExecutor(readItemCallback)
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

        throw new IllegalStateException("This test suite is tested for 2 or 3 preferred regions");
    }

    @DataProvider(name = "regionExclusionQueryAfterCreateTestConfigs")
    public Object[] regionExclusionQueryAfterCreateTestConfigs() {
        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> queryItemCallback =
            (params) -> {

                TestItem alreadyCreatedItem = params.createdItem;

                String query = String.format("SELECT * FROM c WHERE c.id = '%s'", alreadyCreatedItem.getId());
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();

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

        if (this.preferredRegions.size() == 2) {
            return new Object[] {
                new MutationTestConfig()
                    .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                    .withChooseFaultInjectionRegions(this.chooseFirstRegion)
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .withDataPlaneOperationExecutor(queryItemCallback)
                    // applied to the preferred regions
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
                )),
                new MutationTestConfig()
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    // applied to the preferred regions
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
                new MutationTestConfig()
                    .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                    .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                    .withDataPlaneOperationExecutor(queryItemCallback)
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
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                    .withDataPlaneOperationExecutor(queryItemCallback)
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
        } else if (this.preferredRegions.size() == 3) {
            return new Object[] {
                new MutationTestConfig()
                    .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                    .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .withDataPlaneOperationExecutor(queryItemCallback)
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
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    .withDataPlaneOperationExecutor(queryItemCallback)
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
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.READ_SESSION_NOT_AVAILABLE)
                    // applied to the preferred regions
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
                new MutationTestConfig()
                    .withChooseInitialExclusionRegions(this.chooseFirstRegion)
                    .withChooseFaultInjectionRegions(this.chooseFirstTwoRegions)
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                    .withDataPlaneOperationExecutor(queryItemCallback)
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
                    .withFaultInjectionOperationType(FaultInjectionOperationType.QUERY_ITEM)
                    .withFaultInjectionServerErrorType(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                    .withDataPlaneOperationExecutor(queryItemCallback)
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

        throw new IllegalStateException("This test suite is tested for 2 or 3 preferred regions");
    }

    @DataProvider(name = "regionExclusionWriteAfterCreateTestConfigs")
    public Object[] regionExclusionWriteAfterCreateTestConfigs() {

        Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> createAnotherItemCallback =
            (params) -> {

                String newDocumentId = UUID.randomUUID().toString();

                try {

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer
                        .createItem(
                            new TestItem(newDocumentId, newDocumentId, newDocumentId),
                            new PartitionKey(newDocumentId),
                            null)
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

                try {

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer
                        .replaceItem(
                           alreadyCreatedItem,
                           alreadyCreatedItem.getId(),
                           new PartitionKey(alreadyCreatedItem.getId()))
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

                try {

                    CosmosItemResponse<?> response = params.cosmosAsyncContainer
                        .deleteItem(
                            alreadyCreatedItem.getId(),
                            new PartitionKey(alreadyCreatedItem.getId()))
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
                
                try {
                    
                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer.upsertItem(
                        alreadyCreatedItem, 
                        new PartitionKey(alreadyCreatedItem.getId()),
                        null).block();
                    
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

                try {

                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer.upsertItem(
                        newItem,
                        new PartitionKey(newItem.getId()),
                        null).block();

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

                if (params.nonIdempotentWriteRetriesEnabled) {
                    params.options.setNonIdempotentWriteRetryPolicy(
                        true, true
                    );
                }

                try {
                    CosmosItemResponse<TestItem> response = params.cosmosAsyncContainer.patchItem(
                        params.createdItem.getId(),
                        new PartitionKey(params.createdItem.getId()),
                        patchOperations,
                        params.options,
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

        if (this.preferredRegions.size() == 2) {
            return new Object[] {
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
                        HttpConstants.StatusCodes.NOTFOUND,
                        HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE,
                        this.chooseFirstRegion.apply(this.preferredRegions)
                )),
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
            };
        } else if (this.preferredRegions.size() == 3) {
            return new Object[] {
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
            };
        }

        throw new IllegalStateException("This test suite is tested for 2 or 3 preferred regions");
    }

    @Test(groups = { "multi-master" }, dataProvider = "regionExclusionReadAfterCreateTestConfigs")
    public void regionExclusionMutationOnClient_readAfterCreate_test(MutationTestConfig mutationTestConfig) throws InterruptedException {
        execute(mutationTestConfig);
    }

    @Test(groups = { "multi-master" }, dataProvider = "regionExclusionQueryAfterCreateTestConfigs")
    public void regionExclusionMutationOnClient_queryAfterCreate_test(MutationTestConfig mutationTestConfig) throws InterruptedException {
        execute(mutationTestConfig);
    }

    @Test(groups = { "multi-master" }, dataProvider = "regionExclusionWriteAfterCreateTestConfigs")
    public void regionExclusionMutationOnClient_writeAfterCreate_test(MutationTestConfig mutationTestConfig) throws InterruptedException {
        execute(mutationTestConfig);
    }

    private void execute(MutationTestConfig mutationTestConfig) throws InterruptedException {
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

            OperationExecutionResult<?> operationExecutionResultBeforeMutation = dataPlaneOperationExecutor.apply(params);
            validateResponse(operationExecutionResultBeforeMutation, mutationTestConfig.expectedResultBeforeMutation);

            Function<List<String>, List<String>> regionExclusionMutators = mutationTestConfig.regionExclusionMutator;

            List<String> mutatedExcludedRegions = regionExclusionMutators.apply(this.preferredRegions);
            clientWithPreferredRegions.setExcludeRegions(mutatedExcludedRegions);

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

    private static class OperationExecutionResult<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;
        private final FeedResponse<T> feedResponse;

        OperationExecutionResult(FeedResponse<T> feedResponse) {
            this.feedResponse = feedResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
        }

        OperationExecutionResult(CosmosItemResponse<T> cosmosItemResponse) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = null;
            this.feedResponse = null;
        }

        OperationExecutionResult(CosmosException cosmosException) {
            this.cosmosException = cosmosException;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
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
        private Function<ItemOperationInvocationParameters, OperationExecutionResult<?>> dataPlaneOperationExecutor = null;
        private ExpectedResult expectedResultBeforeMutation = null;
        private ExpectedResult expectedResultAfterMutation = null;
        private boolean nonIdempotentWritesEnabled = false;

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
    }

    private static class ItemOperationInvocationParameters {
        public boolean nonIdempotentWriteRetriesEnabled;
        public CosmosPatchItemRequestOptions options;
        public CosmosAsyncContainer cosmosAsyncContainer;
        public TestItem createdItem;
    }
}
