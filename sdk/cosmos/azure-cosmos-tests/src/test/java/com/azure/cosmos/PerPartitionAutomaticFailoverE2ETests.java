// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
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
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.timeout.ReadTimeoutException;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;

public class PerPartitionAutomaticFailoverE2ETests extends TestSuiteBase {

    private CosmosAsyncDatabase sharedDatabase;
    private CosmosAsyncContainer sharedSinglePartitionContainer;
    private AccountLevelLocationContext accountLevelLocationReadableLocationContext;
    private static final ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor COSMOS_CLIENT_BUILDER_ACCESSOR
        = ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private static final Set<ConnectionMode> ALL_CONNECTION_MODES = new HashSet<>();
    private static final Set<ConnectionMode> ONLY_DIRECT_MODE = new HashSet<>();
    private static final Set<ConnectionMode> ONLY_GATEWAY_MODE = new HashSet<>();

    private static final CosmosEndToEndOperationLatencyPolicyConfig THREE_SEC_E2E_TIMEOUT_POLICY = new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(3)).build();

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
    public PerPartitionAutomaticFailoverE2ETests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"multi-region"})
    public void beforeClass() {
        CosmosAsyncClient cosmosAsyncClient = getClientBuilder().buildAsyncClient();

        this.sharedDatabase = getSharedCosmosDatabase(cosmosAsyncClient);
        this.sharedSinglePartitionContainer = getSharedSinglePartitionCosmosContainer(cosmosAsyncClient);

        ONLY_GATEWAY_MODE.add(ConnectionMode.GATEWAY);
        ONLY_DIRECT_MODE.add(ConnectionMode.DIRECT);

        ALL_CONNECTION_MODES.add(ConnectionMode.DIRECT);
        ALL_CONNECTION_MODES.add(ConnectionMode.GATEWAY);

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

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(0)
            .setShouldFinalResponseHaveSuccess(false)
            .setExpectedRegionsContactedCount(1);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverForRequestTimeout = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(1)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(2);

        ExpectedResponseCharacteristics expectedResponseCharacteristicsAfterFailover = new ExpectedResponseCharacteristics()
            .setExpectedMinRetryCount(0)
            .setExpectedMaxRetryCount(0)
            .setShouldFinalResponseHaveSuccess(true)
            .setExpectedRegionsContactedCount(1);

        return new Object[][]{
            {
                "Test failover handling for CREATE when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for REPLACE when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for UPSERT when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for DELETE when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for PATCH when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for BATCH when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for CREATE when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for REPLACE when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for UPSERT when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for PATCH when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for BATCH when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for CREATE when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for REPLACE when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for UPSERT when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for PATCH when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for BATCH when FORBIDDEN / FORBIDDEN_WRITEFORBIDDEN is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.FORBIDDEN,
                HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailover,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for REPLACE when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for UPSERT when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for PATCH when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for BATCH when REQUEST_TIMEOUT / UNKNOWN is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.UNKNOWN,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForRequestTimeout,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                false,
                ALL_CONNECTION_MODES
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH when REQUEST_TIMEOUT / GATEWAY_ENDPOINT_READ_TIMEOUT is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.REQUEST_TIMEOUT,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointReadTimeout,
                expectedResponseCharacteristicsAfterFailover,
                true,
                true,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for CREATE when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH when SERVICE_UNAVAILABLE / GATEWAY_ENDPOINT_UNAVAILABLE is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverForGatewayEndpointUnavailable,
                expectedResponseCharacteristicsAfterFailover,
                true,
                false,
                false,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for CREATE with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for REPLACE with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for UPSERT with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for DELETE with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for PATCH with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for BATCH with e2e timeout and when GONE / SERVER_GENERATED_410 is injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_410,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_DIRECT_MODE
            },
            {
                "Test failover handling for CREATE with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is injected into first preferred region for a specific server partition.",
                OperationType.Create,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.CREATED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for REPLACE with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 and delay too is injected into first preferred region for a specific server partition.",
                OperationType.Replace,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for UPSERT with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 and delay too is injected into first preferred region for a specific server partition.",
                OperationType.Upsert,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for DELETE with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 and delay too is injected into first preferred region for a specific server partition.",
                OperationType.Delete,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.NOT_MODIFIED,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for PATCH with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is and delay too injected into first preferred region for a specific server partition.",
                OperationType.Patch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            },
            {
                "Test failover handling for BATCH with e2e timeout and when SERVICE_UNAVAILABLE / SERVER_GENERATED_503 is and delay too injected into first preferred region for a specific server partition.",
                OperationType.Batch,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                HttpConstants.SubStatusCodes.SERVER_GENERATED_503,
                HttpConstants.StatusCodes.OK,
                expectedResponseCharacteristicsBeforeFailoverWhenE2ETimeoutSet,
                expectedResponseCharacteristicsAfterFailover,
                false,
                false,
                true,
                ONLY_GATEWAY_MODE
            }
        };
    }

    // testPpafWithWriteFailoverWithEligibleErrorStatusCodes does the following:
    // for DIRECT connection mode,
    //  an availability failure (410, 503, 408) or write forbidden failure (403/3) is injected
    //  for a given partitionKeyRange and region through mocking
    //  the first operation execution for a given operation type is expected to see failures and then failover (403/3s & 503s & 408s (not e2e timeout hit) are retried and e2e time out hit (408:20008) just see the operation fail)
    //  the second operation execution should see the request go straight away to the failed over region - caveat is when e2e timeout is hit, only after x failures does a failover happen
    // for GATEWAY connection mode,
    //  an availability failure (503, 408), write forbidden failure (403/3) and I/O failures are injected
    //  for a given region through mocking
    //  the first operation execution for a given operation type is expected to see failures and then failover (403/3s & 503s & 408s (not e2e timeout hit) are retried and e2e time out hit (408:20008) just see the operation fail)
    //  the second operation execution should see the request go straight away to the failed over region - caveat is when e2e timeout is hit, only after x failures does a failover happen
    @Test(groups = {"multi-region"}, dataProvider = "ppafTestConfigsWithWriteOps")
    public void testPpafWithWriteFailoverWithEligibleErrorStatusCodes(
        String testType,
        OperationType operationType,
        int errorStatusCodeToMockFromPartitionInUnhealthyRegion,
        int errorSubStatusCodeToMockFromPartitionInUnhealthyRegion,
        int successStatusCode,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsBeforeFailover,
        ExpectedResponseCharacteristics expectedResponseCharacteristicsAfterFailover,
        boolean shouldThrowNetworkError,
        boolean shouldThrowReadTimeoutExceptionWhenNetworkError,
        boolean shouldUseE2ETimeout,
        Set<ConnectionMode> allowedConnectionModes) {

        ConnectionPolicy connectionPolicy = COSMOS_CLIENT_BUILDER_ACCESSOR.getConnectionPolicy(getClientBuilder());
        ConnectionMode connectionMode = connectionPolicy.getConnectionMode();

        if (!allowedConnectionModes.contains(connectionMode)) {
            throw new SkipException(String.format("Test with type : %s not eligible for specified connection mode %s.", testType, connectionMode));
        }

        if (connectionMode == ConnectionMode.DIRECT) {

            TransportClient transportClientMock = Mockito.mock(TransportClient.class);
            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {

                if (shouldUseE2ETimeout) {
                    System.setProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF", "2");
                }

                System.setProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED", "true");

                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();

                // todo: evaluate whether Batch operation needs op-level e2e timeout and availability strategy
                if (operationType.equals(OperationType.Batch) && shouldUseE2ETimeout) {
                    cosmosClientBuilder.endToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY);
                }

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

                assertThat(partitionKeyRangesForContainer).isNotNull();
                assertThat(partitionKeyRangesForContainer.v).isNotNull();
                assertThat(partitionKeyRangesForContainer.v.size()).isGreaterThanOrEqualTo(1);

                PartitionKeyRange partitionKeyRangeWithIssues = partitionKeyRangesForContainer.v.get(0);

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

                String regionWithIssues = preferredRegions.get(0);
                RegionalRoutingContext regionalRoutingContextWithIssues = new RegionalRoutingContext(new URI(readableRegionNameToEndpoint.get(regionWithIssues)));

                ReflectionUtils.setTransportClient(storeReader, transportClientMock);
                ReflectionUtils.setTransportClient(consistencyWriter, transportClientMock);

                setupTransportClientToReturnSuccessResponse(transportClientMock, constructStoreResponse(operationType, successStatusCode));

                CosmosException cosmosException = createCosmosException(
                    errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                    errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

                setupTransportClientToThrowCosmosException(
                    transportClientMock,
                    partitionKeyRangeWithIssues,
                    regionalRoutingContextWithIssues,
                    cosmosException);

                TestObject testItem = TestObject.create();

                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation = resolveDataPlaneOperation(operationType);

                OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
                operationInvocationParamsWrapper.asyncContainer = asyncContainer;
                operationInvocationParamsWrapper.createdTestItem = testItem;
                operationInvocationParamsWrapper.itemRequestOptions = shouldUseE2ETimeout ? new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosItemRequestOptions();
                operationInvocationParamsWrapper.patchItemRequestOptions = shouldUseE2ETimeout ? new CosmosPatchItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosPatchItemRequestOptions();

                if (shouldUseE2ETimeout) {

                    int iterationsToRun = Configs.getAllowedE2ETimeoutHitCountForPPAF();

                    for (int i = 1; i <= iterationsToRun + 1; i++) {
                        ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                        this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                    }
                } else {
                    ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                    this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                }

                ResponseWrapper<?> responseAfterFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseAfterFailover, expectedResponseCharacteristicsAfterFailover);
            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                System.clearProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF");
                System.clearProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED");
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }

        if (connectionMode == ConnectionMode.GATEWAY) {
            HttpClient mockedHttpClient = Mockito.mock(HttpClient.class);
            List<String> preferredRegions = this.accountLevelLocationReadableLocationContext.serviceOrderedReadableRegions;
            Map<String, String> readableRegionNameToEndpoint = this.accountLevelLocationReadableLocationContext.regionNameToEndpoint;
            Utils.ValueHolder<CosmosAsyncClient> cosmosAsyncClientValueHolder = new Utils.ValueHolder<>();

            try {

                if (shouldUseE2ETimeout) {
                    System.setProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF", "2");
                }

                System.setProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED", "true");

                CosmosClientBuilder cosmosClientBuilder = getClientBuilder();

                // todo: evaluate whether Batch operation needs op-level e2e timeout and availability strategy
                if (operationType.equals(OperationType.Batch) && shouldUseE2ETimeout) {
                    cosmosClientBuilder.endToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY);
                }

                CosmosAsyncClient asyncClient = cosmosClientBuilder.buildAsyncClient();
                cosmosAsyncClientValueHolder.v = asyncClient;

                CosmosAsyncContainer asyncContainer = asyncClient
                    .getDatabase(this.sharedDatabase.getId())
                    .getContainer(this.sharedSinglePartitionContainer.getId());

                // populates collection cache and pkrange cache
                asyncContainer.getFeedRanges().block();

                RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(asyncClient);

                RxStoreModel rxStoreModel = ReflectionUtils.getGatewayProxy(rxDocumentClient);

                GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
                DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();

                assertThat(preferredRegions).isNotNull();
                assertThat(preferredRegions.size()).isGreaterThanOrEqualTo(1);

                String regionWithIssues = preferredRegions.get(0);
                URI locationEndpointWithIssues = new URI(readableRegionNameToEndpoint.get(regionWithIssues) + "dbs/" + this.sharedDatabase.getId() + "/colls/" + this.sharedSinglePartitionContainer.getId() + "/docs");

                ReflectionUtils.setGatewayHttpClient(rxStoreModel, mockedHttpClient);

                setupHttpClientToReturnSuccessResponse(mockedHttpClient, operationType, databaseAccount, successStatusCode);

                CosmosException cosmosException = createCosmosException(
                    errorStatusCodeToMockFromPartitionInUnhealthyRegion,
                    errorSubStatusCodeToMockFromPartitionInUnhealthyRegion);

                setupHttpClientToThrowCosmosException(
                    mockedHttpClient,
                    locationEndpointWithIssues,
                    cosmosException,
                    shouldThrowNetworkError,
                    shouldThrowReadTimeoutExceptionWhenNetworkError,
                    shouldUseE2ETimeout);

                TestObject testItem = TestObject.create();

                Function<OperationInvocationParamsWrapper, ResponseWrapper<?>> dataPlaneOperation = resolveDataPlaneOperation(operationType);

                OperationInvocationParamsWrapper operationInvocationParamsWrapper = new OperationInvocationParamsWrapper();
                operationInvocationParamsWrapper.asyncContainer = asyncContainer;
                operationInvocationParamsWrapper.createdTestItem = testItem;
                operationInvocationParamsWrapper.itemRequestOptions = shouldUseE2ETimeout ? new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosItemRequestOptions();
                operationInvocationParamsWrapper.patchItemRequestOptions = shouldUseE2ETimeout ? new CosmosPatchItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(THREE_SEC_E2E_TIMEOUT_POLICY) : new CosmosPatchItemRequestOptions();

                if (shouldUseE2ETimeout) {

                    int iterationsToRun = Configs.getAllowedE2ETimeoutHitCountForPPAF();

                    for (int i = 1; i <= iterationsToRun + 1; i++) {
                        ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                        this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                    }
                } else {
                    ResponseWrapper<?> responseBeforeFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);

                    assertThat(responseBeforeFailover).isNotNull();
                    this.validateExpectedResponseCharacteristics.accept(responseBeforeFailover, expectedResponseCharacteristicsBeforeFailover);
                }

                ResponseWrapper<?> responseAfterFailover = dataPlaneOperation.apply(operationInvocationParamsWrapper);
                this.validateExpectedResponseCharacteristics.accept(responseAfterFailover, expectedResponseCharacteristicsAfterFailover);
            } catch (Exception e) {
                Assertions.fail("The test ran into an exception {}", e);
            } finally {
                System.clearProperty("COSMOS.E2E_TIMEOUT_ERROR_HIT_THRESHOLD_FOR_PPAF");
                System.clearProperty("COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED");
                safeClose(cosmosAsyncClientValueHolder.v);
            }
        }
    }

    private void setupTransportClientToThrowCosmosException(
        TransportClient transportClientMock,
        PartitionKeyRange partitionKeyRange,
        RegionalRoutingContext regionalRoutingContextToRoute,
        CosmosException cosmosException) {

        Mockito.when(
                transportClientMock.invokeResourceOperationAsync(
                    Mockito.any(),
                    Mockito.argThat(argument ->
                        argument.requestContext.resolvedPartitionKeyRange
                            .getId()
                            .equals(partitionKeyRange.getId()) &&
                            argument.requestContext.regionalRoutingContextToRoute.equals(regionalRoutingContextToRoute))))
            .thenReturn(Mono.error(cosmosException));
    }

    private void setupHttpClientToThrowCosmosException(
        HttpClient httpClientMock,
        URI locationEndpointToRoute,
        CosmosException cosmosException,
        boolean shouldThrowNetworkError,
        boolean shouldThrowReadTimeoutExceptionWhenNetworkError,
        boolean shouldForceE2ETimeout) {

        if (shouldForceE2ETimeout) {
            Mockito.when(
                    httpClientMock.send(
                        Mockito.argThat(argument -> {
                            URI uri = argument.uri();
                            return uri.toString().contains(locationEndpointToRoute.toString());
                        }), Mockito.any(Duration.class)))
                .thenReturn(Mono.delay(Duration.ofSeconds(10)).flatMap(aLong -> Mono.error(cosmosException)));

            return;
        }

        if (shouldThrowNetworkError) {
            if (shouldThrowReadTimeoutExceptionWhenNetworkError) {
                Mockito.when(
                        httpClientMock.send(
                            Mockito.argThat(argument -> {
                                URI uri = argument.uri();
                                return uri.toString().contains(locationEndpointToRoute.toString());
                            }), Mockito.any(Duration.class)))
                    .thenReturn(Mono.error(new ReadTimeoutException()));
            } else {
                Mockito.when(
                        httpClientMock.send(
                            Mockito.argThat(argument -> {
                                URI uri = argument.uri();
                                return uri.toString().contains(locationEndpointToRoute.toString());
                            }), Mockito.any(Duration.class)))
                    .thenReturn(Mono.error(new SocketTimeoutException()));
            }

            return;
        }

        // simulates regional failover with error being bubbled up by RxGatewayStoreModel which uses the mocked HttpClient
        Mockito.when(
                httpClientMock.send(
                    Mockito.argThat(argument -> {
                        URI uri = argument.uri();
                        return uri.toString().contains(locationEndpointToRoute.toString());
                    }), Mockito.any(Duration.class)))
            .thenReturn(Mono.error(cosmosException));
    }

    private void setupTransportClientToReturnSuccessResponse(
        TransportClient transportClientMock,
        StoreResponse storeResponse) {

        Mockito.when(transportClientMock.invokeResourceOperationAsync(Mockito.any(), Mockito.any())).thenReturn(Mono.just(storeResponse));
    }

    private void setupHttpClientToReturnSuccessResponse(HttpClient httpClientMock, OperationType operationType, DatabaseAccount databaseAccount, int statusCode) {

        Mockito
            .when(httpClientMock.send(Mockito.argThat(argument -> {

                if (argument == null) {
                    return false;
                }

            URI uri = argument.uri();
            String uriStr = uri.toString();

            // basically a DatabaseAccount call
            return !uriStr.contains("docs") &&
                !uriStr.contains("dbs") &&
                !uriStr.contains("colls") &&
                !uri.toString().contains("pkranges");
        }), Mockito.any(Duration.class)))
            .thenReturn(Mono.just(createResponse(statusCode, operationType, ResourceType.DatabaseAccount, databaseAccount, getTestPojoObject())));

        Mockito
            .when(httpClientMock.send(Mockito.argThat(argument -> {

                if (argument == null) {
                    return false;
                }

                URI uri = argument.uri();
                String uriStr = uri.toString();

                // basically a Document call
                return uriStr.contains("docs");
            }), Mockito.any(Duration.class)))
            .thenReturn(Mono.just(createResponse(statusCode, operationType, ResourceType.Document, databaseAccount, getTestPojoObject())));
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
                    null,
                    new StringBuilder()));
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
                    TestObject createdTestObject = paramsWrapper.createdTestItem;
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
                    TestObject createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> upsertItemResponse = asyncContainer.upsertItem(
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
                    TestObject createdTestObject = TestObject.create();
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> createItemResponse = asyncContainer.createItem(
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
                    TestObject createdTestObject = paramsWrapper.createdTestItem;
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
                    TestObject createdTestObject = paramsWrapper.createdTestItem;
                    CosmosPatchItemRequestOptions patchItemRequestOptions = (CosmosPatchItemRequestOptions) paramsWrapper.patchItemRequestOptions;

                    CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/number", 555);

                    try {

                        CosmosItemResponse<TestObject> patchItemResponse = asyncContainer.patchItem(
                                createdTestObject.getId(),
                                new PartitionKey(createdTestObject.getId()),
                                patchOperations,
                                patchItemRequestOptions,
                                TestObject.class)
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
                    TestObject createdTestObject = paramsWrapper.createdTestItem;
                    CosmosItemRequestOptions itemRequestOptions = paramsWrapper.itemRequestOptions;

                    try {

                        CosmosItemResponse<TestObject> deleteItemResponse = asyncContainer.replaceItem(
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

                    TestObject testObject = TestObject.create();
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

                        FeedResponse<TestObject> feedResponseFromChangeFeed = asyncContainer.queryChangeFeed(
                                CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(paramsWrapper.feedRangeToDrainForChangeFeed == null ? FeedRange.forFullRange() : paramsWrapper.feedRangeToDrainForChangeFeed),
                                TestObject.class)
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
        public TestObject createdTestItem;
        public CosmosItemRequestOptions itemRequestOptions;
        public CosmosQueryRequestOptions queryRequestOptions;
        public CosmosItemRequestOptions patchItemRequestOptions;
        public FeedRange feedRangeToDrainForChangeFeed;
        public FeedRange feedRangeForQuery;
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

    private HttpResponse createResponse(int statusCode, OperationType operationType, ResourceType resourceType, DatabaseAccount databaseAccount, TestPojo testPojo) {
        HttpResponse httpResponse = new HttpResponse() {
            @Override
            public int statusCode() {
                return statusCode;
            }

            @Override
            public String headerValue(String name) {
                return null;
            }

            @Override
            public HttpHeaders headers() {
                return new HttpHeaders();
            }

            @Override
            public Mono<ByteBuf> body() {
                try {

                    if (resourceType == ResourceType.DatabaseAccount) {
                        return Mono.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT, databaseAccount.toJson()));
                    }

                    if (operationType == OperationType.Batch) {
                        FakeBatchResponse fakeBatchResponse = new FakeBatchResponse();

                        fakeBatchResponse
                            .seteTag("1")
                            .setStatusCode(HttpConstants.StatusCodes.OK)
                            .setSubStatusCode(HttpConstants.SubStatusCodes.UNKNOWN)
                            .setRequestCharge(1.0d)
                            .setResourceBody(getTestPojoObject())
                            .setRetryAfterMilliseconds("1");

                        return Mono.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT,
                            OBJECT_MAPPER.writeValueAsString(Arrays.asList(fakeBatchResponse))));
                    }

                    return Mono.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT,
                        OBJECT_MAPPER.writeValueAsString(testPojo)));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }

            @Override
            public Mono<String> bodyAsString() {
                try {

                    if (resourceType == ResourceType.DatabaseAccount) {
                        return Mono.just(databaseAccount.toJson());
                    }

                    if (operationType == OperationType.Batch) {
                        FakeBatchResponse fakeBatchResponse = new FakeBatchResponse();

                        fakeBatchResponse
                            .seteTag("1")
                            .setStatusCode(HttpConstants.StatusCodes.OK)
                            .setSubStatusCode(HttpConstants.SubStatusCodes.UNKNOWN)
                            .setRequestCharge(1.0d)
                            .setResourceBody(getTestPojoObject())
                            .setRetryAfterMilliseconds("1");

                        return Mono.just(OBJECT_MAPPER.writeValueAsString(Arrays.asList(fakeBatchResponse)));
                    }

                    return Mono.just(OBJECT_MAPPER.writeValueAsString(testPojo));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }
        };

        try {
            return httpResponse.withRequest(new HttpRequest(HttpMethod.POST, TestConfigurations.HOST, 443));
        } catch (URISyntaxException e) {
            return httpResponse;
        }
    }
}
