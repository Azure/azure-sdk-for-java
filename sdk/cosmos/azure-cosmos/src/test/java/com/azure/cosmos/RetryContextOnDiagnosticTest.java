// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentClientRetryPolicy;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.IRetryPolicyFactory;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Quadruple;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.StoreResponseBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyReader;
import com.azure.cosmos.implementation.directconnectivity.ConsistencyWriter;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.ReplicatedResourceClient;
import com.azure.cosmos.implementation.directconnectivity.StoreClient;
import com.azure.cosmos.implementation.directconnectivity.StoreReader;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.TransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.subscribers.TestSubscriber;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.Utils.getUTF8BytesOrNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class RetryContextOnDiagnosticTest extends TestSuiteBase {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static String exceptionText = "TestException";
    private final static String responseText = "TestResponse";

    private IRetryPolicy retryPolicy;
    private RxDocumentServiceRequest serviceRequest;
    private AddressSelector addressSelector;

    @Test(groups = {"unit"}, timeOut = TIMEOUT * 2)
    public void backoffRetryUtilityExecuteRetry() throws Exception {
        @SuppressWarnings("unchecked")
        Callable<Mono<StoreResponse>> callbackMethod = Mockito.mock(Callable.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read,
            ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        addressSelector = Mockito.mock(AddressSelector.class);
        CosmosException exception = new CosmosException(410, exceptionText);
        Mockito.when(callbackMethod.call()).thenThrow(exception, exception, exception, exception, exception)
            .thenReturn(Mono.just(new StoreResponse(200, new ArrayList<>(), getUTF8BytesOrNull(responseText))));
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeRetry(callbackMethod, retryPolicy);
        StoreResponse response = validateSuccess(monoResponse);

        assertThat(response.getResponseBody()).isEqualTo(getUTF8BytesOrNull(responseText));
        assertThat(retryPolicy.getRetryContext().getRetryCount()).isEqualTo(5);
        assertThat(retryPolicy.getRetryContext().getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryContext().getRetryCount());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT * 2)
    public void backoffRetryUtilityExecuteRetryWithFailure() throws Exception {
        @SuppressWarnings("unchecked")
        Callable<Mono<StoreResponse>> callbackMethod = Mockito.mock(Callable.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read,
            ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        addressSelector = Mockito.mock(AddressSelector.class);
        CosmosException exception = new CosmosException(410, exceptionText);
        Mockito.when(callbackMethod.call()).thenThrow(exception);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            ((TestRetryPolicy) retryPolicy).noRetry = true;
        }, 10, TimeUnit.SECONDS);
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeRetry(callbackMethod, retryPolicy);
        validateFailure(monoResponse);

        assertThat(retryPolicy.getRetryContext().getRetryCount()).isGreaterThanOrEqualTo(5);
        assertThat(retryPolicy.getRetryContext().getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryContext().getRetryCount());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT * 2)
    @SuppressWarnings("unchecked")
    public void backoffRetryUtilityExecuteAsync() {
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> inBackoffAlternateCallbackMethod = Mockito.mock(Function.class);
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> parameterizedCallbackMethod =
            Mockito.mock(Function.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read,
            ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        addressSelector = Mockito.mock(AddressSelector.class);
        CosmosException exception = new CosmosException(410, exceptionText);
        Mono<StoreResponse> exceptionMono = Mono.error(exception);
        Mockito.when(parameterizedCallbackMethod.apply(ArgumentMatchers.any())).thenReturn(exceptionMono, exceptionMono, exceptionMono, exceptionMono, exceptionMono)
            .thenReturn(Mono.just(new StoreResponse(200, new ArrayList<>(), getUTF8BytesOrNull(responseText))));
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeAsync(
            parameterizedCallbackMethod,
            retryPolicy,
            inBackoffAlternateCallbackMethod,
            Duration.ofSeconds(5),
            serviceRequest,
            addressSelector);
        StoreResponse response = validateSuccess(monoResponse);

        assertThat(response.getResponseBody()).isEqualTo(getUTF8BytesOrNull(responseText));
        assertThat(retryPolicy.getRetryContext().getRetryCount()).isEqualTo(5);
        assertThat(retryPolicy.getRetryContext().getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryContext().getRetryCount());
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT * 2)
    @SuppressWarnings("unchecked")
    public void backoffRetryUtilityExecuteAsyncWithFailure() {
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> inBackoffAlternateCallbackMethod = Mockito.mock(Function.class);
        Function<Quadruple<Boolean, Boolean, Duration, Integer>, Mono<StoreResponse>> parameterizedCallbackMethod =
            Mockito.mock(Function.class);
        serviceRequest = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read,
            ResourceType.Document);
        retryPolicy = new TestRetryPolicy();
        CosmosException exception = new CosmosException(410, exceptionText);
        Mono<StoreResponse> exceptionMono = Mono.error(exception);
        Mockito.when(parameterizedCallbackMethod.apply(ArgumentMatchers.any())).thenReturn(exceptionMono);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            ((TestRetryPolicy) retryPolicy).noRetry = true;
        }, 10, TimeUnit.SECONDS);
        Mono<StoreResponse> monoResponse = BackoffRetryUtility.executeAsync(
            parameterizedCallbackMethod,
            retryPolicy,
            inBackoffAlternateCallbackMethod,
            Duration.ofSeconds(5),
            serviceRequest,
            addressSelector);
        validateFailure(monoResponse);

        assertThat(retryPolicy.getRetryContext().getStatusAndSubStatusCodes().size()).isEqualTo(retryPolicy.getRetryContext().getRetryCount());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void retryContextMockTestOnCRUDOperation() throws NoSuchFieldException, IllegalAccessException {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        CosmosAsyncContainer cosmosAsyncContainer = getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
        RxStoreModel mockStoreModel = Mockito.mock(RxStoreModel.class, Mockito.CALLS_REAL_METHODS);
        RxDocumentServiceResponse mockRxDocumentServiceResponse = Mockito.mock(RxDocumentServiceResponse.class);

        Field storeModelField = RxDocumentClientImpl.class.getDeclaredField("storeModel");
        storeModelField.setAccessible(true);
        storeModelField.set(rxDocumentClient, mockStoreModel);

        IRetryPolicyFactory mockRetryFactory = Mockito.mock(IRetryPolicyFactory.class);
        Field resetSessionTokenRetryPolicyField = RxDocumentClientImpl.class.getDeclaredField(
            "resetSessionTokenRetryPolicy");
        resetSessionTokenRetryPolicyField.setAccessible(true);
        resetSessionTokenRetryPolicyField.set(rxDocumentClient, mockRetryFactory);

        TestRetryPolicy retryPolicy = Mockito.mock(TestRetryPolicy.class);
        RetryContext retryContext = Mockito.mock(RetryContext.class);
        Mockito.when(retryPolicy.getRetryContext()).thenReturn(retryContext);
        Mockito.when(retryContext.getRetryCount()).thenReturn(1);

        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        Mockito.when(mockStoreModel.processMessage(ArgumentMatchers.any(RxDocumentServiceRequest.class))).thenReturn(Mono.just(mockRxDocumentServiceResponse));
        Mockito.when(mockRxDocumentServiceResponse.getResource(Document.class)).thenReturn(new Document());
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setPartitionKey(new PartitionKey("TestPk"));
        String itemSelfLink = cosmosAsyncContainer.getLink() + "/docs/TestDoc";
        Mono<ResourceResponse<Document>> responseFlux =
            rxDocumentClient.createDocument(cosmosAsyncContainer.getLink(), new Document(), requestOptions, false);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryContext, Mockito.times(1)).getRetryCount();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        retryContext = Mockito.mock(RetryContext.class);
        Mockito.when(retryPolicy.getRetryContext()).thenReturn(retryContext);
        Mockito.when(retryContext.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.readDocument(itemSelfLink, requestOptions);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryContext, Mockito.times(1)).getRetryCount();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        retryContext = Mockito.mock(RetryContext.class);
        Mockito.when(retryPolicy.getRetryContext()).thenReturn(retryContext);
        Mockito.when(retryContext.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.deleteDocument(itemSelfLink, requestOptions);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryContext, Mockito.times(1)).getRetryCount();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        retryContext = Mockito.mock(RetryContext.class);
        Mockito.when(retryPolicy.getRetryContext()).thenReturn(retryContext);
        Mockito.when(retryContext.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.replaceDocument(itemSelfLink, new Document(), requestOptions);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryContext, Mockito.times(1)).getRetryCount();

        retryPolicy = Mockito.mock(TestRetryPolicy.class);
        retryContext = Mockito.mock(RetryContext.class);
        Mockito.when(retryPolicy.getRetryContext()).thenReturn(retryContext);
        Mockito.when(retryContext.getRetryCount()).thenReturn(1);
        Mockito.when(mockRetryFactory.getRequestPolicy()).thenReturn(retryPolicy);
        responseFlux = rxDocumentClient.upsertDocument(itemSelfLink, new Document(), requestOptions, false);
        validateServiceResponseSuccess(responseFlux);

        Mockito.verify(retryContext, Mockito.times(1)).getRetryCount();

        cosmosClient.close();
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void goneExceptionSuccessScenario() throws JsonProcessingException {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        try {
            CosmosAsyncContainer cosmosAsyncContainer =
                getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
            RxDocumentClientImpl rxDocumentClient =
                (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            TransportClient mockTransportClient = Mockito.mock(TransportClient.class);
            GoneException goneException = new GoneException("Gone Test");

            Mono<StoreResponse> storeResponse = Mono.just(getStoreResponse(201));
            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(goneException), Mono.error(goneException), storeResponse);
            ReflectionUtils.setTransportClient(consistencyWriter, mockTransportClient);

            CosmosContainer cosmosContainer =
                cosmosClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            TestPojo testPojo = getTestPojoObject();

            CosmosItemResponse<TestPojo> createItemResponse = cosmosContainer.createItem(testPojo,
                new PartitionKey(testPojo.getMypk()), new CosmosItemRequestOptions());
            RetryContext retryContext =
                createItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(410);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[1]).isEqualTo(0);

            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(goneException), Mono.error(goneException), Mono.just(getStoreResponse(200)));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            CosmosItemResponse<TestPojo> readItemResponse = cosmosContainer.readItem(testPojo.getId(),
                new PartitionKey(testPojo.getMypk()), requestOptions, TestPojo.class);

            retryContext =
                readItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(410);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[1]).isEqualTo(0);

            mockTransportClient = Mockito.mock(TransportClient.class);
            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(goneException), Mono.error(goneException), Mono.just(getQueryStoreResponse()));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);
            String query = "select * from c";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems(query,
                options, InternalObjectNode.class)
                .iterableByPage()
                .iterator();
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            retryContext =
                feedResponse.getCosmosDiagnostics().getFeedResponseDiagnostics().getClientSideRequestStatisticsList().get(0).getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(410);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[1]).isEqualTo(0);
        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void goneAndThrottlingExceptionSuccessScenario() throws JsonProcessingException {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        try {
            CosmosAsyncContainer cosmosAsyncContainer =
                getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
            RxDocumentClientImpl rxDocumentClient =
                (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            TransportClient mockTransportClient = Mockito.mock(TransportClient.class);
            GoneException goneException = new GoneException("Gone Test");
            CosmosException throttlingException = new CosmosException(429, "ThrottlingException Test");

            Mono<StoreResponse> storeResponse = Mono.just(getStoreResponse(201));
            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(throttlingException), Mono.error(goneException),
                    Mono.error(throttlingException), storeResponse);
            ReflectionUtils.setTransportClient(consistencyWriter, mockTransportClient);

            CosmosContainer cosmosContainer =
                cosmosClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            TestPojo testPojo = getTestPojoObject();

            CosmosItemResponse<TestPojo> createItemResponse = cosmosContainer.createItem(testPojo,
                new PartitionKey(testPojo.getMypk()), new CosmosItemRequestOptions());
            RetryContext retryContext =
                createItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(3);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(1)[0]).isEqualTo(410);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(2)[0]).isEqualTo(429);

            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(goneException), Mono.error(throttlingException), Mono.error(goneException),
                    Mono.just(getStoreResponse(200)));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            CosmosItemResponse<TestPojo> readItemResponse = cosmosContainer.readItem(testPojo.getId(),
                new PartitionKey(testPojo.getMypk()), requestOptions, TestPojo.class);

            retryContext =
                readItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(3);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(410);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(1)[0]).isEqualTo(429);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(2)[0]).isEqualTo(410);

            mockTransportClient = Mockito.mock(TransportClient.class);
            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(goneException), Mono.error(throttlingException), Mono.error(goneException),
                    Mono.error(throttlingException), Mono.just(getQueryStoreResponse()));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);
            String query = "select * from c";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems(query,
                options, InternalObjectNode.class)
                .iterableByPage()
                .iterator();
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            retryContext =
                feedResponse.getCosmosDiagnostics().getFeedResponseDiagnostics().getClientSideRequestStatisticsList().get(0).getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(4);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(410);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(1)[0]).isEqualTo(429);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(2)[0]).isEqualTo(410);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(3)[0]).isEqualTo(429);
        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT * 2)
    @SuppressWarnings("unchecked")
    public void goneExceptionFailureScenario() {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        try {
            CosmosAsyncContainer cosmosAsyncContainer =
                getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
            RxDocumentClientImpl rxDocumentClient =
                (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            TransportClient mockTransportClient = Mockito.mock(TransportClient.class);
            GoneException exception = new GoneException("Gone Test");

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(exception));
            ReflectionUtils.setTransportClient(consistencyWriter, mockTransportClient);

            try {
                CosmosContainer cosmosContainer =
                    cosmosClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
                TestPojo testPojo = getTestPojoObject();
                cosmosContainer.createItem(testPojo,
                    new PartitionKey(testPojo.getMypk()), new CosmosItemRequestOptions());

                fail("Create item should no succeed");
            } catch (CosmosException ex) {
                RetryContext retryContext =
                    ex.getDiagnostics().clientSideRequestStatistics().getRetryContext();
                assertThat(retryContext.getStatusAndSubStatusCodes().size()).isLessThanOrEqualTo(7);
                assertThat(retryContext.getStatusAndSubStatusCodes().size()).isGreaterThanOrEqualTo(6);
                assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(410);
                assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[1]).isEqualTo(0);
            }
        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void sessionNonAvailableExceptionScenario() throws JsonProcessingException {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        try {
            CosmosAsyncContainer cosmosAsyncContainer =
                getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
            RxDocumentClientImpl rxDocumentClient =
                (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            TransportClient mockTransportClient = Mockito.mock(TransportClient.class);
            CosmosException sessionNotFoundException = new CosmosException(404, "Session Test");
            BridgeInternal.setSubStatusCode(sessionNotFoundException, 1002);

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(sessionNotFoundException), Mono.error(sessionNotFoundException),
                    Mono.just(getStoreResponse(201)));
            ReflectionUtils.setTransportClient(consistencyWriter, mockTransportClient);

            CosmosContainer cosmosContainer =
                cosmosClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            TestPojo testPojo = getTestPojoObject();
            CosmosItemResponse<TestPojo> createItemResponse = cosmosContainer.createItem(testPojo,
                new PartitionKey(testPojo.getMypk()), new CosmosItemRequestOptions());
            RetryContext retryContext =
                createItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(404);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[1]).isEqualTo(1002);

            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(sessionNotFoundException), Mono.error(sessionNotFoundException),
                    Mono.just(getStoreResponse(200)));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            CosmosItemResponse<TestPojo> readItemResponse = cosmosContainer.readItem(testPojo.getId(),
                new PartitionKey(testPojo.getMypk()), requestOptions, TestPojo.class);

            retryContext =
                readItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(404);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[1]).isEqualTo(1002);

            mockTransportClient = Mockito.mock(TransportClient.class);
            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(sessionNotFoundException), Mono.error(sessionNotFoundException),
                    Mono.just(getQueryStoreResponse()));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);
            String query = "select * from c";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems(query,
                options, InternalObjectNode.class)
                .iterableByPage()
                .iterator();
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            retryContext =
                feedResponse.getCosmosDiagnostics().getFeedResponseDiagnostics().getClientSideRequestStatisticsList().get(0).getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(404);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[1]).isEqualTo(1002);

        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT * 2)
    @SuppressWarnings("unchecked")
    public void sessionNonAvailableExceptionFailureScenario() {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        try {
            CosmosAsyncContainer cosmosAsyncContainer =
                getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
            RxDocumentClientImpl rxDocumentClient =
                (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            TransportClient mockTransportClient = Mockito.mock(TransportClient.class);
            CosmosException exception = new CosmosException(404, "Session Test");
            exception.setSubStatusCode(1002);

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(exception));
            ReflectionUtils.setTransportClient(consistencyWriter, mockTransportClient);

            try {
                CosmosContainer cosmosContainer =
                    cosmosClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
                TestPojo testPojo = getTestPojoObject();
                cosmosContainer.createItem(testPojo,
                    new PartitionKey(testPojo.getMypk()), new CosmosItemRequestOptions());

                fail("Create item should no succeed");
            } catch (CosmosException ex) {
                RetryContext retryContext =
                    ex.getDiagnostics().clientSideRequestStatistics().getRetryContext();
                // On session not found,  max retry via SessionTokenMismatchRetryPolicy is 105 (0, 5,10,20, 40 , then 50ms
                // up to 5 sec).
                // On single region we will have 3 retry of session policy (One retry from first request, one from
                // client retry policy and last from rename policy.
                assertThat(retryContext.getStatusAndSubStatusCodes().size()).isGreaterThanOrEqualTo(100);
                assertThat(retryContext.getStatusAndSubStatusCodes().size()).isLessThanOrEqualTo(315);
                assertThat(retryContext.getStatusAndSubStatusCodes().get(retryContext.getStatusAndSubStatusCodes().size()-1)[0]).isEqualTo(404);
                assertThat(retryContext.getStatusAndSubStatusCodes().get(retryContext.getStatusAndSubStatusCodes().size()-1)[1]).isEqualTo(1002);
            }
        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void throttlingExceptionScenario() throws JsonProcessingException {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.directMode(DirectConnectionConfig.getDefaultConfig()).buildClient();
        try {
            CosmosAsyncContainer cosmosAsyncContainer =
                getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
            RxDocumentClientImpl rxDocumentClient =
                (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
            StoreClient storeClient = ReflectionUtils.getStoreClient(rxDocumentClient);
            ReplicatedResourceClient replicatedResourceClient =
                ReflectionUtils.getReplicatedResourceClient(storeClient);
            ConsistencyWriter consistencyWriter = ReflectionUtils.getConsistencyWriter(replicatedResourceClient);

            TransportClient mockTransportClient = Mockito.mock(TransportClient.class);
            CosmosException throttlingException = new CosmosException(429, "Throttling Test");

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(throttlingException), Mono.error(throttlingException),
                    Mono.just(getStoreResponse(201)));
            ReflectionUtils.setTransportClient(consistencyWriter, mockTransportClient);

            CosmosContainer cosmosContainer =
                cosmosClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            TestPojo testPojo = getTestPojoObject();
            CosmosItemResponse<TestPojo> createItemResponse = cosmosContainer.createItem(testPojo,
                new PartitionKey(testPojo.getMypk()), new CosmosItemRequestOptions());
            RetryContext retryContext =
                createItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);

            ConsistencyReader consistencyReader = ReflectionUtils.getConsistencyReader(replicatedResourceClient);
            StoreReader storeReader = ReflectionUtils.getStoreReader(consistencyReader);

            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(throttlingException), Mono.error(throttlingException),
                    Mono.just(getStoreResponse(200)));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            CosmosItemResponse<TestPojo> readItemResponse = cosmosContainer.readItem(testPojo.getId(),
                new PartitionKey(testPojo.getMypk()), requestOptions, TestPojo.class);

            retryContext =
                readItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);

            mockTransportClient = Mockito.mock(TransportClient.class);
            Mockito.when(mockTransportClient.invokeResourceOperationAsync(Mockito.any(Uri.class),
                Mockito.any(RxDocumentServiceRequest.class)))
                .thenReturn(Mono.error(throttlingException), Mono.error(throttlingException),
                    Mono.just(getQueryStoreResponse()));
            ReflectionUtils.setTransportClient(storeReader, mockTransportClient);
            String query = "select * from c";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems(query,
                options, InternalObjectNode.class)
                .iterableByPage()
                .iterator();
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            retryContext =
                feedResponse.getCosmosDiagnostics().getFeedResponseDiagnostics().getClientSideRequestStatisticsList().get(0).getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);

        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void throttlingExceptionGatewayModeScenario() {
        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY);
        CosmosClient cosmosClient =
            cosmosClientBuilder.gatewayMode().buildClient();
        try {
            CosmosAsyncContainer cosmosAsyncContainer =
                getSharedMultiPartitionCosmosContainer(cosmosClient.asyncClient());
            RxDocumentClientImpl rxDocumentClient =
                (RxDocumentClientImpl) cosmosClient.asyncClient().getContextClient();
            RxStoreModel rxGatewayStoreModel = ReflectionUtils.getGatewayProxy(rxDocumentClient);

            TestPojo testPojo = getTestPojoObject();
            CosmosContainer cosmosContainer =
                cosmosClient.getDatabase(cosmosAsyncContainer.getDatabase().getId()).getContainer(cosmosAsyncContainer.getId());
            try {
                // to warm up the collection cache
                cosmosContainer.readItem(testPojo.getId(), new PartitionKey(testPojo.getMypk()), TestPojo.class);
            } catch (CosmosException ex) {
                // consuming error
            }

            // Query Plan Caching start
            System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "true");
            String query = "select * from c";
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            options.setPartitionKey(new PartitionKey(testPojo.getMypk()));
            options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            Iterator<FeedResponse<InternalObjectNode>> iterator = cosmosContainer.queryItems(query,
                options, InternalObjectNode.class)
                .iterableByPage()
                .iterator();
            FeedResponse<InternalObjectNode> feedResponse = iterator.next();
            // Query Plan Caching end

            HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
            CosmosException throttlingException = new CosmosException(429, "Throttling Test");

            Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class)))
                .thenReturn(Mono.error(throttlingException), Mono.error(throttlingException),
                    Mono.just(createResponse((201))));
            ReflectionUtils.setGatewayHttpClient(rxGatewayStoreModel, mockHttpClient);

            CosmosItemResponse<TestPojo> createItemResponse = cosmosContainer.createItem(testPojo,
                new PartitionKey(testPojo.getMypk()), new CosmosItemRequestOptions());
            RetryContext retryContext =
                createItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);

            mockHttpClient = Mockito.mock(HttpClient.class);
            Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class)))
                .thenReturn(Mono.error(throttlingException), Mono.error(throttlingException),
                    Mono.just(createResponse((201))));
            ReflectionUtils.setGatewayHttpClient(rxGatewayStoreModel, mockHttpClient);

            CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
            requestOptions.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            CosmosItemResponse<TestPojo> readItemResponse = cosmosContainer.readItem(testPojo.getId(),
                new PartitionKey(testPojo.getMypk()), requestOptions, TestPojo.class);

            retryContext =
                readItemResponse.getDiagnostics().clientSideRequestStatistics().getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);

            mockHttpClient = Mockito.mock(HttpClient.class);
            Mockito.when(mockHttpClient.send(Mockito.any(HttpRequest.class), Mockito.any(Duration.class)))
                .thenReturn(Mono.error(throttlingException), Mono.error(throttlingException),
                    Mono.just(createResponse((201))));
            ReflectionUtils.setGatewayHttpClient(rxGatewayStoreModel, mockHttpClient);

            options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);
            iterator = cosmosContainer.queryItems(query,
                options, InternalObjectNode.class)
                .iterableByPage()
                .iterator();
            feedResponse = iterator.next();
            retryContext =
                feedResponse.getCosmosDiagnostics().getFeedResponseDiagnostics().getClientSideRequestStatisticsList().get(0).getRetryContext();
            assertThat(retryContext.getRetryCount()).isEqualTo(2);
            assertThat(retryContext.getStatusAndSubStatusCodes().get(0)[0]).isEqualTo(429);
            System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "false");
        } finally {
            safeCloseSyncClient(cosmosClient);
        }
    }

    private StoreResponse validateSuccess(Mono<StoreResponse> storeResponse) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        storeResponse.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(60000, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        return testSubscriber.values().get(0);
    }

    private void validateFailure(Mono<StoreResponse> storeResponse) {
        TestSubscriber<StoreResponse> testSubscriber = new TestSubscriber<>();
        storeResponse.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(60000, TimeUnit.MILLISECONDS);
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
    }

    private void validateServiceResponseSuccess(Mono<ResourceResponse<Document>> documentServiceResponseMono) {
        TestSubscriber<ResourceResponse<Document>> testSubscriber = new TestSubscriber<>();
        documentServiceResponseMono.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(60000, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
    }

    private class TestRetryPolicy extends DocumentClientRetryPolicy {
        boolean noRetry;
        RetryContext retryContext;

        public TestRetryPolicy() {
            retryContext = new RetryContext();
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {
            if (noRetry) {
                return Mono.just(ShouldRetryResult.noRetry());
            }
            return Mono.just(ShouldRetryResult.retryAfter(Duration.ofSeconds(2)));
        }

        @Override
        public void onBeforeSendRequest(RxDocumentServiceRequest request) {
            //no implementation needed for mock test
        }

        @Override
        public RetryContext getRetryContext() {
            return retryContext;
        }
    }

    private StoreResponse getStoreResponse(int statusCode) throws JsonProcessingException {
        StoreResponseBuilder storeResponseBuilder =
            StoreResponseBuilder.create().withContent(OBJECT_MAPPER.writeValueAsString(getTestPojoObject()))
                .withStatus(statusCode);
        return storeResponseBuilder.build();
    }

    private StoreResponse getQueryStoreResponse() {
        String queryContent = "{\n" +
            "  \"_rid\": \"IaBwAPRwFTg=\",\n" +
            "  \"Documents\": [\n" +
            "    {\n" +
            "      \"id\": \"id1\",\n" +
            "      \"mypk\": \"id1\",\n" +
            "      \"_rid\": \"IaBwAPRwFTgBAAAAAAAAAA==\",\n" +
            "      \"_self\": \"dbs/IaBwAA==/colls/IaBwAPRwFTg=/docs/IaBwAPRwFTgBAAAAAAAAAA==/\",\n" +
            "      \"_etag\": \"\\\"9700bf23-0000-0a00-0000-604681950000\\\"\",\n" +
            "      \"_attachments\": \"attachments/\",\n" +
            "      \"_ts\": 1615233429\n" +
            "    }\n" +
            "  ],\n" +
            "  \"_count\": 1\n" +
            "}";
        StoreResponseBuilder storeResponseBuilder =
            StoreResponseBuilder.create().withContent(queryContent)
                .withStatus(200);
        return storeResponseBuilder.build();
    }

    private TestPojo getTestPojoObject() {
        TestPojo testPojo = new TestPojo();
        String uuid = UUID.randomUUID().toString();
        testPojo.setId(uuid);
        testPojo.setMypk(uuid);
        return testPojo;
    }

    private HttpResponse createResponse(int statusCode) {
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
            public Flux<ByteBuf> body() {
                try {
                    return Flux.just(ByteBufUtil.writeUtf8(ByteBufAllocator.DEFAULT,
                        OBJECT_MAPPER.writeValueAsString(getTestPojoObject())));
                } catch (JsonProcessingException e) {
                    return Flux.error(e);
                }
            }

            @Override
            public Mono<byte[]> bodyAsByteArray() {
                try {
                    return Mono.just(Utils.getUTF8Bytes(OBJECT_MAPPER.writeValueAsString(getTestPojoObject())));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }

            @Override
            public Mono<String> bodyAsString() {
                try {
                    return Mono.just(OBJECT_MAPPER.writeValueAsString(getTestPojoObject()));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }

            @Override
            public Mono<String> bodyAsString(Charset charset) {
                try {
                    return Mono.just(OBJECT_MAPPER.writeValueAsString(getTestPojoObject()));
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
