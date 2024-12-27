// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

public class PerPartitionAutomaticFailoverTests extends TestSuiteBase {

    private CosmosAsyncDatabase sharedDatabase;
    private CosmosAsyncContainer sharedSinglePartitionContainer;
    private AccountLevelLocationContext accountLevelLocationReadableLocationContext;
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor COSMOS_CLIENT_BUILDER_ACCESSOR
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    BiConsumer<ResponseWrapper<?>, ExpectedResponseCharacteristics> validateExpectedResponseCharacteristics = (responseWrapper, expectedResponseCharacteristics) -> {
        assertThat(responseWrapper).isNotNull();

        Utils.ValueHolder<CosmosDiagnostics> cosmosDiagnosticsValueHolder = new Utils.ValueHolder<>();

        if (responseWrapper.batchResponse != null) {

            CosmosBatchResponse cosmosBatchResponse = responseWrapper.batchResponse;

            assertThat(cosmosBatchResponse.getDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = cosmosBatchResponse.getDiagnostics();
        } else if (responseWrapper.cosmosItemResponse != null) {

            CosmosItemResponse<?> cosmosItemResponse = responseWrapper.cosmosItemResponse;

            assertThat(cosmosItemResponse.getDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = cosmosItemResponse.getDiagnostics();
        } else if (responseWrapper.feedResponse != null) {

            FeedResponse<?> feedResponse = responseWrapper.feedResponse;

            assertThat(feedResponse.getCosmosDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = feedResponse.getCosmosDiagnostics();
        } else if (responseWrapper.cosmosException != null) {

            CosmosException cosmosException = responseWrapper.cosmosException;

            assertThat(cosmosException.getDiagnostics()).isNotNull();
            cosmosDiagnosticsValueHolder.v = cosmosException.getDiagnostics();
        } else {
            throw new AssertionError("One of batchResponse, cosmosItemResponse, feedResponse or cosmosException should be populated!");
        }

        assertThat(cosmosDiagnosticsValueHolder.v).isNotNull();
        CosmosDiagnostics cosmosDiagnostics = cosmosDiagnosticsValueHolder.v;

        assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames()).isNotNull();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames()).isNotEmpty();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getContactedRegionNames().size()).isEqualTo(expectedResponseCharacteristics.expectedRegionsContactedCount);

        assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getRetryCount()).isGreaterThanOrEqualTo(expectedResponseCharacteristics.expectedMinRetryCount);
        assertThat(cosmosDiagnostics.getDiagnosticsContext().getRetryCount()).isLessThanOrEqualTo(expectedResponseCharacteristics.expectedMaxRetryCount);

        if (expectedResponseCharacteristics.shouldFinalResponseHaveSuccess) {
            assertThat(cosmosDiagnostics.getDiagnosticsContext()).isNotNull();
            assertThat(cosmosDiagnostics.getDiagnosticsContext().getStatusCode() >= HttpConstants.StatusCodes.OK
                && cosmosDiagnostics.getDiagnosticsContext().getStatusCode() <= HttpConstants.StatusCodes.NOT_MODIFIED).isTrue();
        }
    };

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public PerPartitionAutomaticFailoverTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        CosmosAsyncClient cosmosAsyncClient = getClientBuilder().buildAsyncClient();
        this.sharedDatabase = getSharedCosmosDatabase(cosmosAsyncClient);
        this.sharedSinglePartitionContainer = getSharedSinglePartitionCosmosContainer(cosmosAsyncClient);

        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(cosmosAsyncClient);
        GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccountSnapshot = globalEndpointManager.getLatestDatabaseAccount();

        this.accountLevelLocationReadableLocationContext = getAccountLevelLocationContext(databaseAccountSnapshot, false);
    }

    @DataProvider(name = "ppafTestConfigsWithWriteOps")
    public Object[][] ppafTestConfigsWithWriteOps() {

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailover = new ExpectedResponseCharacteristics()
                .setExpectedMinRetryCount(1)
                .setShouldFinalResponseHaveSuccess(true)
                .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverForRequestTimeout = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(0)
            .setShouldFinalResponseHaveSuccess(false)
            .setExpectedRegionsContactedCount(1);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsAfterFailover = new ExpectedResponseCharacteristics()
                .setExpectedMinRetryCount(0)
                .setExpectedMaxRetryCount(0)
                .setShouldFinalResponseHaveSuccess(true)
                .setExpectedRegionsContactedCount(1);

        return new Object[][]{
            {
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Replace,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Delete,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Patch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Batch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Create,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Replace,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Upsert,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Delete,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Patch,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Batch,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Create,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Replace,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Upsert,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Delete,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Patch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover
            },
            {
                OperationType.Batch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover
            }
        };
    }

    @DataProvider(name = "ppafTestConfigsWithReadOps")
    public Object[][] ppafTestConfigsWithReadOps() {

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailover = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setExpectedRegionsContactedCount(2)
            .setShouldFinalResponseHaveSuccess(true);

        return new Object[][]{
            {
                OperationType.Create,
                OperationType.Read,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                new ExpectedResponseCharacteristics()
                    .setExpectedMinRetryCount(0)
                    .setExpectedRegionsContactedCount(1)
                    .setShouldFinalResponseHaveSuccess(true)
                    .setExpectedMaxRetryCount(0)
            },
            {
                OperationType.Create,
                OperationType.Query,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                new ExpectedResponseCharacteristics()
                    .setExpectedMinRetryCount(0)
                    .setExpectedRegionsContactedCount(2)
                    .setShouldFinalResponseHaveSuccess(true)
                    .setExpectedMaxRetryCount(0)
            },
            {
                OperationType.Create,
                OperationType.ReadFeed,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                new ExpectedResponseCharacteristics()
                    .setExpectedMinRetryCount(0)
                    .setExpectedRegionsContactedCount(1)
                    .setShouldFinalResponseHaveSuccess(true)
                    .setExpectedMaxRetryCount(0)
            }
        };
    }

    @Test(groups = {"multi-region"}, dataProvider = "ppafTestConfigsWithWriteOps")
    public void testPpafWithWriteFailoverWithEligibleErrorStatusCodes(
        OperationType operationType,
        int errorStatusCodeToMockFromPartitionInUnhealthyRegion,
        int errorSubStatusCodeToMockFromPartitionInUnhealthyRegion,
        int successStatusCode,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailover,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsAfterFailover) {

        TransportClient transportClientMock = Mockito.mock(TransportClient.class);
        List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
        Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
        Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

        if (COSMOS_CLIENT_BUILDER_ACCESSOR.getConnectionPolicy(getClientBuilder()).getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("testPpafWithServiceUnavailable does not run in the GATEWAY connectivity mode!");
        }

        try {

            CosmosClientBuilder cosmosClientBuilder = getClientBuilder()
                .perPartitionAutomaticFailoverEnabled(true)
                .preferredRegions(preferredRegions);

            CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
            cosmosAsyncClientValueHolder.v = asyncClient;

            CosmosAsyncContainer asyncContainer = asyncClient
                .getDatabase(this.sharedDatabase.getId())
                .getContainer(this.sharedSinglePartitionContainer.getId());

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);
            Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangesForContainer
                = getPartitionKeyRangesForContainer(asyncContainer, rxDocumentClient).block();

            assertThat(partitionKeyRangesForContainer.v).isNotNull();
            assertThat(partitionKeyRangesForContainer.v.size()).isGreaterThanOrEqualTo(1);

            PartitionKeyRange partitionKeyRangeWithIssues = partitionKeyRangesForContainer.v.get(0);

            assertThat(preferredRegions).isNotNull();
            assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

            String regionWithIssues = preferredRegions.get(0);
            URI locationEndpointWithIssues = new URI(readableRegionNameToEndpoint.get(regionWithIssues));

            ReflectionUtils.setTransportClient(storeReader, transportClientMock);
            ReflectionUtils.setTransportClient(consistencyWriter, transportClientMock);

            setupTransportClientToReturnSuccessResponse(transportClientMock, constructStoreResponse(operationType, successStatusCode));

            CosmosException cosmosException = createCosmosException(
                errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

            setupTransportClientToThrowCosmosException(
                transportClientMock,
                partitionKeyRangeWithIssues,
                locationEndpointWithIssues,
                cosmosException);

            TestItem testItem = TestItem.createNewItem();

            Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation = resolveDataPlaneOperation(operationType);

            OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
            operationInvocationParamsWrapper.asyncContainer = asyncContainer;
            operationInvocationParamsWrapper.createdTestItem = testItem;
            operationInvocationParamsWrapper.itemRequestOptions = new CosmosItemRequestOptions();
            operationInvocationParamsWrapper.patchItemRequestOptions = new CosmosPatchItemRequestOptions();

            ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);

            assertThat(responseBeforeFailover).isNotNull();
            this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);

            ResponseWrapper<?> responseAfterFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
            this.validateExpectedResponseCharacteristics.accept(responseAfterFailover, expectedResponseCharacteristicsAfterFailover);
        } catch (Exception e) {
            Assertions.fail("The test ran into an exception {}", e);
        } finally {
            safeClose(cosmosAsyncClientValueHolder.v);
        }
    }

    @Test(groups = {"multi-region"}, dataProvider = "ppafTestConfigsWithReadOps")
    public void testPpafReadFailoverPostWriteWithEligibleErrorStatusCodes(
        OperationType writeOperationType,
        OperationType readOperationType,
        int errorStatusCodeToMockFromPartitionInUnhealthyRegion,
        int errorSubStatusCodeToMockFromPartitionInUnhealthyRegion,
        int successStatusCodeToMockForReadRequestFromHealthyRegion,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsForWriteOperationBeforeFailover,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsForReadOperationAfterFailover) {

        TransportClientMock transportClientMock = Mockito.mock(TransportClientMock.class);
        List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
        Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
        Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

        if (COSMOS_CLIENT_BUILDER_ACCESSOR.getConnectionPolicy(getClientBuilder()).getConnectionMode() == ConnectionMode.GATEWAY) {
            throw new SkipException("testPpafWithServiceUnavailable does not run in the GATEWAY connectivity mode!");
        }

        try {

            CosmosClientBuilder cosmosClientBuilder = getClientBuilder()
                .perPartitionAutomaticFailoverEnabled(true)
                .preferredRegions(preferredRegions);

            CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
            cosmosAsyncClientValueHolder.v = asyncClient;

            CosmosAsyncContainer asyncContainer = asyncClient
                .getDatabase(this.sharedDatabase.getId())
                .getContainer(this.sharedSinglePartitionContainer.getId());

            RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient = ReflectionUtils.getReplicatedResourceClient(storeClient);

            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            GlobalEndpointManager globalEndpointManager = rxDocumentClient.getGlobalEndpointManager();

            Utils.ValueHolder<List<PartitionKeyRange>> partitionKeyRangesForContainer
                = getPartitionKeyRangesForContainer(asyncContainer, rxDocumentClient).block();

            assertThat(partitionKeyRangesForContainer.v).isNotNull();
            assertThat(partitionKeyRangesForContainer.v.size()).isGreaterThanOrEqualTo(1);

            PartitionKeyRange partitionKeyRangeWithIssues = partitionKeyRangesForContainer.v.get(0);

            assertThat(preferredRegions).isNotNull();
            assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

            String regionWithIssues = preferredRegions.get(0);
            URI locationEndpointWithIssues = new URI(readableRegionNameToEndpoint.get(regionWithIssues));

            ReflectionUtils.setTransportClient(consistencyWriter, transportClientMock);

            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            ReflectionUtils.setTransportClient(storeReader, transportClientMock);

            Mockito.when(transportClientMock.getGlobalEndpointManager()).thenReturn(globalEndpointManager);

            setupTransportClientToReturnSuccessResponse(transportClientMock, constructStoreResponse(readOperationType, successStatusCodeToMockForReadRequestFromHealthyRegion));

            CosmosException cosmosException = createCosmosException(
                errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

            setupTransportClientToThrowCosmosException(
                transportClientMock,
                partitionKeyRangeWithIssues,
                locationEndpointWithIssues,
                cosmosException);

            TestItem testItem = TestItem.createNewItem();

            Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> writeDataPlaneOperationBeforeFailover = resolveDataPlaneOperation(writeOperationType);

            OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
            operationInvocationParamsWrapper.asyncContainer = asyncContainer;
            operationInvocationParamsWrapper.createdTestItem = testItem;
            operationInvocationParamsWrapper.itemRequestOptions = new CosmosItemRequestOptions();

            ResponseWrapper<?> responseBeforeFailover = writeDataPlaneOperationBeforeFailover.apply(operationInvocationParamsWrapper);

            assertThat(responseBeforeFailover).isNotNull();
            this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsForWriteOperationBeforeFailover);

            Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> readDataPlaneOperationAfterFailover = resolveDataPlaneOperation(readOperationType);

            ResponseWrapper<?> responseAfterFailover = readDataPlaneOperationAfterFailover.apply(operationInvocationParamsWrapper);
            this.validateExpectedResponseCharacteristics.accept(responseAfterFailover, expectedResponseCharacteristicsForReadOperationAfterFailover);
        } catch (Exception e) {
            Assertions.fail("The test ran into an exception {}", e);
        } finally {
            safeClose(cosmosAsyncClientValueHolder.v);
        }
    }

    private void setupTransportClientToThrowCosmosException(
        TransportClient transportClientMock,
        PartitionKeyRange partitionKeyRange,
        URI locationEndpointToRoute,
        CosmosException cosmosException) {

        Mockito.when(
            transportClientMock.invokeResourceOperationAsync(
                Mockito.any(),
                Mockito.argThat(argument ->
                    argument.requestContext.resolvedPartitionKeyRange
                        .getId()
                        .equals(partitionKeyRange.getId()) &&
                        argument.requestContext.locationEndpointToRoute.equals(locationEndpointToRoute))))
            .thenReturn(Mono.error(cosmosException));
    }

    private void setupTransportClientToReturnSuccessResponse(
        TransportClient transportClientMock,
        StoreResponse storeResponse) {

        Mockito.when(transportClientMock.invokeResourceOperationAsync(Mockito.any(), Mockito.any())).thenReturn(Mono.just(storeResponse));
    }

    private Mono<Utils.ValueHolder<List<PartitionKeyRange>>> getPartitionKeyRangesForContainer(
        CosmosAsyncContainer cosmosAsyncContainer, RxDocumentClientImpl rxDocumentClient) {
        return Mono.just(cosmosAsyncContainer)
            .flatMap(CosmosAsyncContainer::read)
            .flatMap(containerResponse -> rxDocumentClient
                .getPartitionKeyRangeCache()
                .tryGetOverlappingRangesAsync(
                    null,
                    containerResponse.getProperties().getResourceId(),
                    PartitionKeyInternalHelper.FullRange,
                    false,
                    null));
    }

    private AccountLevelLocationContext getAccountLevelLocationContext(DatabaseAccount databaseAccount, boolean writeOnly) {
        Iterator<DatabaseAccountLocation> locationIterator =
            writeOnly ? databaseAccount.getWritableLocations().iterator() : databaseAccount.getReadableLocations().iterator();

        List<String> serviceOrderedReadableRegions = new ArrayList<>();
        List<String> serviceOrderedWriteableRegions = new ArrayList<>();
        Map<String, String> regionMap = new ConcurrentHashMap<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            regionMap.put(accountLocation.getName(), accountLocation.getEndpoint());

            if (writeOnly) {
                serviceOrderedWriteableRegions.add(accountLocation.getName());
            } else {
                serviceOrderedReadableRegions.add(accountLocation.getName());
            }
        }

        return new AccountLevelLocationContext(
            serviceOrderedReadableRegions,
            serviceOrderedWriteableRegions,
            regionMap);
    }

    private StoreResponse constructStoreResponse(OperationType operationType, int statusCode) throws JsonProcessingException {

        StoreResponseBuilder storeResponseBuilder = StoreResponseBuilder.create()
            .withContent(OBJECT_MAPPER.writeValueAsString(getTestPojoObject()))
            .withStatus(statusCode);

        if (operationType == OperationType.ReadFeed) {
            return storeResponseBuilder
                .withHeader(HttpConstants.HttpHeaders.CONTINUATION, "1")
                .withHeader(HttpConstants.HttpHeaders.E_TAG, "1")
                .build();
        } else if (operationType == OperationType.Batch) {

            FakeBatchResponse fakeBatchResponse = new FakeBatchResponse();

            fakeBatchResponse
                .seteTag("1")
                .setStatusCode(HttpConstants.StatusCodes.OK)
                .setSubStatusCode(HttpConstants.SubStatusCodes.UNKNOWN)
                .setRequestCharge(1.0d)
                .setResourceBody(getTestPojoObject())
                .setRetryAfterMilliseconds("1");

            return storeResponseBuilder
                .withContent(OBJECT_MAPPER.writeValueAsString(Arrays.asList(fakeBatchResponse)))
                .build();
        } else {
            return storeResponseBuilder.build();
        }
    }

    private static class AccountLevelLocationContext {
        private final List<String> serviceOrderedReadableRegions;
        private final List<String> serviceOrderedWriteableRegions;
        private final Map<String, String> regionNameToEndpoint;

        public AccountLevelLocationContext(
            List<String> serviceOrderedReadableRegions,
            List<String> serviceOrderedWriteableRegions,
            Map<String, String> regionNameToEndpoint) {

            this.serviceOrderedReadableRegions = serviceOrderedReadableRegions;
            this.serviceOrderedWriteableRegions = serviceOrderedWriteableRegions;
            this.regionNameToEndpoint = regionNameToEndpoint;
        }
    }

    private TestPojo getTestPojoObject() {
        TestPojo testPojo = new TestPojo();
        String uuid = UUID.randomUUID().toString();
        testPojo.setId(uuid);
        testPojo.setMypk(uuid);
        return testPojo;
    }

    private CosmosException createCosmosException(int statusCode, int subStatusCode) {

        switch (statusCode) {
            case HttpConstants.StatusCodes.GONE:
                return new GoneException("", subStatusCode);
            case HttpConstants.StatusCodes.SERVICE_UNAVAILABLE:
                return new ServiceUnavailableException(null, null, null, null, subStatusCode);
            case HttpConstants.StatusCodes.FORBIDDEN:
                ForbiddenException forbiddenException = new ForbiddenException(null, -1, null, new HashMap<>());
                BridgeInternal.setSubStatusCode(forbiddenException, subStatusCode);
                return forbiddenException;
            case HttpConstants.StatusCodes.REQUEST_TIMEOUT:
                return new RequestTimeoutException("", null, subStatusCode);
            default:
                throw new UnsupportedOperationException(String.format("Uncovered erroneous status code %d", statusCode));
        }
    }

    private Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> resolveDataPlaneOperation(OperationType operationType) {

        switch (operationType) {
            case Read:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> readItemResponse = asyncContainer.readItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions,
                                TestObject.class)
                            .block();

                        return new ResponseWrapper<>(readItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Upsert:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestItem> upsertItemResponse = asyncContainer.upsertItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(upsertItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Create:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = TestItem.createNewItem();
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestItem> createItemResponse = asyncContainer.createItem(
                                createdTestObject,
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(createItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Delete:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<Object> deleteItemResponse = asyncContainer.deleteItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Patch:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosPatchItemRequestOptions patchItemRequestOptions = (CosmosPatchItemRequestOptions) paramsWrapper.patchItemRequestOptions;

                    CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/number", 555);

                    try {

                        CosmosItemResponse<TestItem> patchItemResponse = asyncContainer.patchItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                patchOperations,
                                patchItemRequestOptions,
                                TestItem.class)
                            .block();

                        return new ResponseWrapper<>(patchItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Query:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    CosmosQueryRequestOptions queryRequestOptions = paramsWrapper.queryRequestOptions == null ? new CosmosQueryRequestOptions() : paramsWrapper.queryRequestOptions;
                    queryRequestOptions = paramsWrapper.feedRangeForQuery == null ? queryRequestOptions.setFeedRange(FeedRange.forFullRange()) : queryRequestOptions.setFeedRange(paramsWrapper.feedRangeForQuery);

                    try {

                        FeedResponse<TestObject> queryItemResponse = asyncContainer.queryItems(
                                "SELECT * FROM C",
                                queryRequestOptions,
                                TestObject.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(queryItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Replace:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;
                    TestItem createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestItem> deleteItemResponse = asyncContainer.replaceItem(
                                createdTestObject,
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                itemRequestOptions)
                            .block();

                        return new ResponseWrapper<>(deleteItemResponse);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case Batch:
                return (paramsWrapper) -> {

                    TestItem testObject = TestItem.createNewItem();
                    CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(testObject.getId()));
                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    batch.createItemOperation(testObject);

                    try {
                        CosmosBatchResponse batchResponse = asyncContainer.executeCosmosBatch(batch).block();
                        return new ResponseWrapper<>(batchResponse);
                    } catch (Exception ex) {
                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            case ReadFeed:
                return (paramsWrapper) -> {

                    CosmosAsyncContainer asyncContainer = paramsWrapper.asyncContainer;

                    try {

                        FeedResponse<TestItem> feedResponseFromChangeFeed = asyncContainer.queryChangeFeed(
                                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(paramsWrapper.feedRangeToDrainForChangeFeed == null ? FeedRange.forFullRange() : paramsWrapper.feedRangeToDrainForChangeFeed),
                                TestItem.class)
                            .byPage()
                            .blockLast();

                        return new ResponseWrapper<>(feedResponseFromChangeFeed);
                    } catch (Exception ex) {

                        if (ex instanceof CosmosException) {
                            CosmosException cosmosException = Utils.as(ex, CosmosException.class);
                            return new ResponseWrapper<>(cosmosException);
                        }

                        throw ex;
                    }
                };
            default:
                throw new UnsupportedOperationException(String.format("Operation of type : %s is not supported", operationType));
        }
    }

    private static class ResponseWrapper<T> {

        private final CosmosItemResponse<T> cosmosItemResponse;
        private final CosmosException cosmosException;
        private final FeedResponse<T> feedResponse;
        private final CosmosBatchResponse batchResponse;

        ResponseWrapper(FeedResponse<T> feedResponse) {
            this.feedResponse = feedResponse;
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosItemResponse<T> cosmosItemResponse) {
            this.cosmosItemResponse = cosmosItemResponse;
            this.cosmosException = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosException cosmosException) {
            this.cosmosException = cosmosException;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = null;
        }

        ResponseWrapper(CosmosBatchResponse batchResponse) {
            this.cosmosException = null;
            this.cosmosItemResponse = null;
            this.feedResponse = null;
            this.batchResponse = batchResponse;
        }
    }

    private static class OperationInvocationParamsWrapper {
        public CosmosAsyncContainer asyncContainer;
        public TestItem createdTestItem;
        public CosmosItemRequestOptions itemRequestOptions;
        public CosmosQueryRequestOptions queryRequestOptions;
        public CosmosReadManyRequestOptions readManyRequestOptions;
        public CosmosItemRequestOptions patchItemRequestOptions;
        public FeedRange feedRangeToDrainForChangeFeed;
        public FeedRange feedRangeForQuery;
    }

    static class TransportClientMock extends TransportClient {

        @Override
        protected Mono<StoreResponse> invokeStoreAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
            return null;
        }

        @Override
        public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {}

        @Override
        protected GlobalEndpointManager getGlobalEndpointManager() {
            return null;
        }

        @Override
        public ProactiveOpenConnectionsProcessor getProactiveOpenConnectionsProcessor() {
            return null;
        }

        @Override
        public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {}

        @Override
        public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {}

        @Override
        public void close() throws Exception {}
    }

    private static class ExpectedResponseCharacteristics {

        int expectedRegionsContactedCount = 0;

        int expectedMaxRetryCount = Integer.MAX_VALUE;

        int expectedMinRetryCount = 0;

        boolean shouldFinalResponseHaveSuccess = false;

        public ExpectedResponseCharacteristics setExpectedRegionsContactedCount(int expectedRegionsContactedCount) {
            this.expectedRegionsContactedCount = expectedRegionsContactedCount;
            return this;
        }

        public ExpectedResponseCharacteristics setExpectedMaxRetryCount(int expectedMaxRetryCount) {
            this.expectedMaxRetryCount = expectedMaxRetryCount;
            return this;
        }

        public ExpectedResponseCharacteristics setExpectedMinRetryCount(int expectedMinRetryCount) {
            this.expectedMinRetryCount = expectedMinRetryCount;
            return this;
        }

        public ExpectedResponseCharacteristics setShouldFinalResponseHaveSuccess(boolean shouldFinalResponseHaveSuccess) {
            this.shouldFinalResponseHaveSuccess = shouldFinalResponseHaveSuccess;
            return this;
        }
    }

    private static class FakeBatchResponse {

        private int statusCode;

        private int subStatusCode;

        private double requestCharge;

        private String eTag;

        private Object resourceBody;

        private String retryAfterMilliseconds;

        public int getStatusCode() {
            return statusCode;
        }

        public FakeBatchResponse setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public int getSubStatusCode() {
            return subStatusCode;
        }

        public FakeBatchResponse setSubStatusCode(int subStatusCode) {
            this.subStatusCode = subStatusCode;
            return this;
        }

        public double getRequestCharge() {
            return requestCharge;
        }

        public FakeBatchResponse setRequestCharge(double requestCharge) {
            this.requestCharge = requestCharge;
            return this;
        }

        public String geteTag() {
            return eTag;
        }

        public FakeBatchResponse seteTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public Object getResourceBody() {
            return resourceBody;
        }

        public FakeBatchResponse setResourceBody(Object resourceBody) {
            this.resourceBody = resourceBody;
            return this;
        }

        public String getRetryAfterMilliseconds() {
            return retryAfterMilliseconds;
        }

        public FakeBatchResponse setRetryAfterMilliseconds(String retryAfterMilliseconds) {
            this.retryAfterMilliseconds = retryAfterMilliseconds;
            return this;
        }
    }
}
