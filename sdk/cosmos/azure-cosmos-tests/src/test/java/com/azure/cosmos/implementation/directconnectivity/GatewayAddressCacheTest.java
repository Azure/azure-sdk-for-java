// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.FlakyTestRetryAnalyzer;
import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConfigsBuilder;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpClientUnderTestWrapper;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.OpenConnectionTask;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.AssertionsForClassTypes;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Connected;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.UnhealthyPending;
import static com.azure.cosmos.implementation.directconnectivity.Uri.HealthStatus.Unknown;
import static org.assertj.core.api.Assertions.assertThat;

public class GatewayAddressCacheTest extends TestSuiteBase {
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public GatewayAddressCacheTest(Builder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "targetPartitionsKeyRangeListAndCollectionLinkParams")
    public Object[][] partitionsKeyRangeListAndCollectionLinkParams() {
        return new Object[][] {
                // target partition key range ids, collection link
                { ImmutableList.of("0"), getNameBasedCollectionLink(), Protocol.TCP},
                { ImmutableList.of("0"), getNameBasedCollectionLink(), Protocol.HTTPS},

                { ImmutableList.of("1"), getNameBasedCollectionLink(), Protocol.HTTPS},
                { ImmutableList.of("1"), getCollectionSelfLink(), Protocol.HTTPS},
                { ImmutableList.of("3"), getNameBasedCollectionLink(), Protocol.HTTPS},

                { ImmutableList.of("0", "1"), getNameBasedCollectionLink(), Protocol.HTTPS},
                { ImmutableList.of("1", "3"), getNameBasedCollectionLink(), Protocol.HTTPS},
        };
    }

    @DataProvider(name = "protocolProvider")
    public Object[][] protocolProvider() {
        return new Object[][]{
                { Protocol.HTTPS},
                { Protocol.TCP},
        };
    }


    @DataProvider(name = "replicaValidationArgsProvider")
    public Object[][] replicaValidationArgsProvider() {
        return new Object[][]{
                // replicaValidationIsEnabled, openConnectionsAndInitCaches
                { false, false },
                { false, true },
                { true, false },
                { true, true },
        };
    }

    @DataProvider(name = "isCollectionUnderWarmUpFlowArgsProvider")
    public Object[] isCollectionUnderWarmUpFlowArgsProvider() {
        return new Object[] {
            // isCollectionUnderWarmUpFlow
            true, false
        };
    }

    @Test(groups = { "direct" }, dataProvider = "targetPartitionsKeyRangeListAndCollectionLinkParams", timeOut = TIMEOUT)
    public void getServerAddressesViaGateway(List<String> partitionKeyRangeIds,
                                             String collectionLink,
                                             Protocol protocol) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        // ask gateway for the addresses
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                serviceEndpoint,
                protocol,
                authorizationTokenProvider,
                null,
                httpClientWrapper.getSpyHttpClient(),
                null,
                null,
                ConnectionPolicy.getDefaultPolicy(),
                null,
                null);

        for (int i = 0; i < 2; i++) {
            RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                    collectionLink + "/docs/",
                    getDocumentDefinition(), new HashMap<>());
            req.requestContext.cosmosDiagnostics = req.createCosmosDiagnostics();
            if (i == 1) {
                req.forceCollectionRoutingMapRefresh = true; //testing address api with x-ms-collectionroutingmap-refresh true
            }
            Mono<List<Address>> addresses = cache.getServerAddressesViaGatewayAsync(
                req, createdCollection.getResourceId(), partitionKeyRangeIds, false);

            PartitionReplicasAddressesValidator validator = new PartitionReplicasAddressesValidator.Builder()
                .withProtocol(protocol)
                .replicasOfPartitions(partitionKeyRangeIds)
                .build();

            validateSuccess(addresses, validator, httpClientWrapper, req, i, TIMEOUT);
        }
    }

    @Test(groups = { "direct" }, dataProvider = "protocolProvider", timeOut = TIMEOUT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void getMasterAddressesViaGatewayAsync(Protocol protocol) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        // ask gateway for the addresses
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(), serviceEndpoint,
                                                            protocol,
                                                            authorizationTokenProvider,
                                                            null,
                                                            httpClientWrapper.getSpyHttpClient(),
                                                            null,
                                                            null,
                                                            ConnectionPolicy.getDefaultPolicy(),
                                                            null,
                                                            null);

        for (int i = 0; i < 2; i++) {
            RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                    "/dbs",
                    new Database(), new HashMap<>());
            req.requestContext.cosmosDiagnostics = req.createCosmosDiagnostics();
            if (i == 1) {
                req.forceCollectionRoutingMapRefresh = true; //testing address api with x-ms-collectionroutingmap-refresh true
            }
            Mono<List<Address>> addresses = cache.getMasterAddressesViaGatewayAsync(req, ResourceType.Database,
                null, "/dbs/", false, false, null);

            PartitionReplicasAddressesValidator validator = new PartitionReplicasAddressesValidator.Builder()
                .withProtocol(protocol)
                .replicasOfSamePartition()
                .build();

            validateSuccess(addresses, validator, httpClientWrapper, req, i, TIMEOUT);
        }
    }

    @DataProvider(name = "targetPartitionsKeyRangeAndCollectionLinkParams")
    public Object[][] partitionsKeyRangeAndCollectionLinkParams() {
        return new Object[][] {
                // target partition key range ids, collection link, protocol
                { "0", getNameBasedCollectionLink(), Protocol.TCP},
                { "0", getNameBasedCollectionLink(), Protocol.HTTPS},

                { "1", getNameBasedCollectionLink(), Protocol.HTTPS} ,
                { "1", getCollectionSelfLink(), Protocol.HTTPS},
                { "3", getNameBasedCollectionLink(), Protocol.HTTPS},
        };
    }

    @Test(groups = { "direct" }, dataProvider = "targetPartitionsKeyRangeAndCollectionLinkParams", timeOut = TIMEOUT, retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void tryGetAddresses_ForDataPartitions(String partitionKeyRangeId, String collectionLink, Protocol protocol) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(), serviceEndpoint,
                                                            protocol,
                                                            authorizationTokenProvider,
                                                            null,
                                                            getHttpClient(configs),
                                                            null,
                                                            null,
                                                            ConnectionPolicy.getDefaultPolicy(),
                                                            proactiveOpenConnectionsProcessorMock,
                                                            null);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                        collectionLink,
                       new Database(), new HashMap<>());

        String collectionRid = createdCollection.getResourceId();

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, partitionKeyRangeId);
        boolean forceRefreshPartitionAddresses = false;
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs = cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

        ArrayList<AddressInformation> addressInfosFromCache =
            Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        Mono<List<Address>> masterAddressFromGatewayObs = cache.getServerAddressesViaGatewayAsync(req,
                collectionRid, ImmutableList.of(partitionKeyRangeId), false);
        List<Address> expectedAddresses = getSuccessResult(masterAddressFromGatewayObs, TIMEOUT);

        assertSameAs(addressInfosFromCache, expectedAddresses);
    }

    @DataProvider(name = "openAsyncTargetAndTargetPartitionsKeyRangeAndCollectionLinkParams")
    public Object[][] openAsyncTargetAndPartitionsKeyRangeTargetAndCollectionLinkParams() {
        return new Object[][] {
                // openAsync target partition key range ids, target partition key range id, collection link, is collection under warm up flow
                { ImmutableList.of("0", "1"), "0", getNameBasedCollectionLink(), true },
                { ImmutableList.of("0", "1"), "1", getNameBasedCollectionLink(), true },
                { ImmutableList.of("0", "1"), "1", getCollectionSelfLink(), false },
        };
    }

    @Test(groups = { "direct" },
            dataProvider = "openAsyncTargetAndTargetPartitionsKeyRangeAndCollectionLinkParams",
            timeOut = TIMEOUT)
    public void tryGetAddresses_ForDataPartitions_AddressCachedByOpenAsync_NoHttpRequest(
            List<String> allPartitionKeyRangeIds,
            String partitionKeyRangeId,
            String collectionLink,
            boolean isCollectionUnderWarmUpFlow) throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                            serviceEndpoint,
                                                            Protocol.HTTPS,
                                                            authorizationTokenProvider,
                                                            null,
                                                            httpClientWrapper.getSpyHttpClient(),
                                                            null,
                                                            null,
                                                            ConnectionPolicy.getDefaultPolicy(),
                                                            proactiveOpenConnectionsProcessorMock,
                                                            null);

        String collectionRid = createdCollection.getResourceId();

        List<PartitionKeyRangeIdentity> pkriList = allPartitionKeyRangeIds.stream().map(
                pkri -> new PartitionKeyRangeIdentity(collectionRid, pkri)).collect(Collectors.toList());

        cache.resolveAddressesAndInitCaches(
                collectionLink,
                createdCollection,
                pkriList)
                .blockLast();

        assertThat(httpClientWrapper.capturedRequests).asList().hasSize(1);
        httpClientWrapper.capturedRequests.clear();

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                        collectionLink,
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, partitionKeyRangeId);
        boolean forceRefreshPartitionAddresses = false;
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs = cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);
        ArrayList<AddressInformation> addressInfosFromCache = Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        // no new request is made
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("no http request: addresses already cached by openAsync")
                .asList().hasSize(0);

        Mono<List<Address>> masterAddressFromGatewayObs = cache.getServerAddressesViaGatewayAsync(req,
                collectionRid, ImmutableList.of(partitionKeyRangeId), false);
        List<Address> expectedAddresses = getSuccessResult(masterAddressFromGatewayObs, TIMEOUT);

        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getServerAddressesViaGatewayAsync will read addresses from gateway")
                .asList().hasSize(1);

        assertSameAs(addressInfosFromCache, expectedAddresses);
    }

    @Test(groups = { "direct" },
            dataProvider = "openAsyncTargetAndTargetPartitionsKeyRangeAndCollectionLinkParams",
            timeOut = TIMEOUT)
    public void tryGetAddresses_ForDataPartitions_ForceRefresh(
            List<String> allPartitionKeyRangeIds,
            String partitionKeyRangeId,
            String collectionLink,
            boolean isCollectionUnderWarmUpFlow) throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);
        Uri addressUriMock = Mockito.mock(Uri.class);
        OpenConnectionTask dummyOpenConnectionsTask = new OpenConnectionTask("", serviceEndpoint, addressUriMock, Configs.getMinConnectionPoolSizePerEndpoint());

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                            serviceEndpoint,
                                                            Protocol.HTTPS,
                                                            authorizationTokenProvider,
                                                            null,
                                                            httpClientWrapper.getSpyHttpClient(),
                                                            null,
                                                            null,
                                                            ConnectionPolicy.getDefaultPolicy(),
                                                            proactiveOpenConnectionsProcessorMock,
                                                            null);

        String collectionRid = createdCollection.getResourceId();

        List<PartitionKeyRangeIdentity> pkriList = allPartitionKeyRangeIds.stream().map(
                pkri -> new PartitionKeyRangeIdentity(collectionRid, pkri)).collect(Collectors.toList());

        Mockito.when(proactiveOpenConnectionsProcessorMock.submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(dummyOpenConnectionsTask);

        cache.resolveAddressesAndInitCaches(collectionLink, createdCollection, pkriList).blockLast();

        assertThat(httpClientWrapper.capturedRequests).asList().hasSize(1);
        httpClientWrapper.capturedRequests.clear();

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                        collectionLink,
                        new Database(), new HashMap<>());

        if (isCollectionUnderWarmUpFlow) {
            Mockito
                .when(proactiveOpenConnectionsProcessorMock.isCollectionRidUnderOpenConnectionsFlow(Mockito.anyString()))
                .thenReturn(true);
        } else {
            Mockito
                .when(proactiveOpenConnectionsProcessorMock.isCollectionRidUnderOpenConnectionsFlow(Mockito.anyString()))
                .thenReturn(false);
        }

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, partitionKeyRangeId);
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs = cache.tryGetAddresses(req, partitionKeyRangeIdentity, true);
        ArrayList<AddressInformation> addressInfosFromCache = Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        // isCollectionRidUnderOpenConnectionsFlow is called 6 times
        // 1. as forceRefreshPartitionAddresses = true, this invokes isCollectionRidUnderOpenConnectionsFlow eventually
        // refreshing collectionRid->addresses map maintained by proactiveOpenConnectionsProcessor to track containers / addresses
        // under connection warm up flow
        // 2. replica validation will get triggered in case of unhealthyPending / unknown addresses, replica validation will do a
        // submitOpenConnectionTaskOutsideLoop for each of these addresses but before that it will also do
        // isCollectionRidUnderOpenConnectionsFlow check to determine the no. of connections to open
        // 3. it needs to be checked if the replicas (up for validation) with Unknown health status are used by a container which
        // is part of the warm up flow - isCollectionRidUnderOpenConnectionsFlow is called here again for each replica
        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(6))
                .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());

        if (isCollectionUnderWarmUpFlow) {
            // if the collection is under the warm up flow and the replica health status is Unknown / UnhealthyPending
            // status then trigger replica validation for this replica address
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.atLeastOnce())
                   .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
        } else {
            // if the collection is not under the warm up flow and the replica health status is Unknown
            // then don't trigger replica validation for this replica address
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.never())
                   .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
        }

        Mockito.clearInvocations(proactiveOpenConnectionsProcessorMock);
        // no new request is made
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("force refresh fetched from gateway")
                .asList().hasSize(1);

        Mono<List<Address>> masterAddressFromGatewayObs = cache.getServerAddressesViaGatewayAsync(req,
                collectionRid, ImmutableList.of(partitionKeyRangeId), false);
        List<Address> expectedAddresses = getSuccessResult(masterAddressFromGatewayObs, TIMEOUT);

        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getServerAddressesViaGatewayAsync will read addresses from gateway")
                .asList().hasSize(2);

        assertSameAs(addressInfosFromCache, expectedAddresses);
    }

    @Test(groups = { "direct" },
            dataProvider = "openAsyncTargetAndTargetPartitionsKeyRangeAndCollectionLinkParams",
            timeOut = TIMEOUT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void tryGetAddresses_ForDataPartitions_Suboptimal_Refresh(
            List<String> allPartitionKeyRangeIds,
            String partitionKeyRangeId,
            String collectionLink,
            boolean isCollectionUnderWarmUpFlow) throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);
        Uri addressUriMock = Mockito.mock(Uri.class);
        OpenConnectionTask dummyOpenConnectionsTask = new OpenConnectionTask("", serviceEndpoint, addressUriMock, Configs.getMinConnectionPoolSizePerEndpoint());

        int suboptimalRefreshTime = 2;

        GatewayAddressCache origCache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                                serviceEndpoint,
                                                                Protocol.HTTPS,
                                                                authorizationTokenProvider,
                                                                null,
                                                                httpClientWrapper.getSpyHttpClient(),
                                                                suboptimalRefreshTime,
                                                                null,
                                                                null,
                                                                ConnectionPolicy.getDefaultPolicy(),
                                                                proactiveOpenConnectionsProcessorMock,
                                                                null);

        String collectionRid = createdCollection.getResourceId();

        List<PartitionKeyRangeIdentity> pkriList = allPartitionKeyRangeIds.stream().map(
                pkri -> new PartitionKeyRangeIdentity(collectionRid, pkri)).collect(Collectors.toList());

        Mockito.when(proactiveOpenConnectionsProcessorMock.submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(dummyOpenConnectionsTask);

        origCache.resolveAddressesAndInitCaches(collectionLink, createdCollection, pkriList).blockLast();

        assertThat(httpClientWrapper.capturedRequests).asList().hasSize(1);
        httpClientWrapper.capturedRequests.clear();

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                        collectionLink,
                        new Database(), new HashMap<>());

        if (isCollectionUnderWarmUpFlow) {
            Mockito
                .when(proactiveOpenConnectionsProcessorMock.isCollectionRidUnderOpenConnectionsFlow(Mockito.anyString()))
                .thenReturn(true);
        } else {
            Mockito
                .when(proactiveOpenConnectionsProcessorMock.isCollectionRidUnderOpenConnectionsFlow(Mockito.anyString()))
                .thenReturn(false);
        }

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, partitionKeyRangeId);
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs = origCache.tryGetAddresses(req, partitionKeyRangeIdentity, true);
        ArrayList<AddressInformation> addressInfosFromCache = Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        // isCollectionRidUnderOpenConnectionsFlow called 6 times
        // 1. as forceRefreshPartitionAddresses = true, this invokes isCollectionRidUnderOpenConnectionsFlow eventually
        // refreshing collectionRid->addresses map maintained by proactiveOpenConnectionsProcessor to track containers / addresses
        // under connection warm up flow
        // 2. replica validation will get triggered in case of unhealthyPending / unknown addresses, replica validation will do a
        // submitOpenConnectionTaskOutsideLoop for each of these addresses but before that it will also do
        // isCollectionRidUnderOpenConnectionsFlow check to determine the no. of connections to open
        // 3. it needs to be checked if the replicas (up for validation) with Unknown health status are used by a container which
        // is part of the warm up flow - isCollectionRidUnderOpenConnectionsFlow is called here again for each replica
        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(6))
               .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());
        if (isCollectionUnderWarmUpFlow) {
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.atLeastOnce())
                   .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
        } else {
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.never())
                   .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
        }

        // no new request is made
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("force refresh fetched from gateway")
                .asList().hasSize(1);

        GatewayAddressCache spyCache = Mockito.spy(origCache);

        final AtomicInteger fetchCounter = new AtomicInteger(0);
        Mockito.doAnswer(new Answer<Mono<List<Address>>>() {
            @Override
            public Mono<List<Address>> answer(InvocationOnMock invocationOnMock) throws Throwable {

                RxDocumentServiceRequest req = invocationOnMock.getArgument(0, RxDocumentServiceRequest.class);
                String collectionRid = invocationOnMock.getArgument(1, String.class);
                List<String> partitionKeyRangeIds = invocationOnMock.getArgument(2, List.class);
                boolean forceRefresh = invocationOnMock.getArgument(3, Boolean.class);

                int cnt = fetchCounter.getAndIncrement();

                if (cnt == 0) {
                    Mono<List<Address>> res = origCache.getServerAddressesViaGatewayAsync(req,
                            collectionRid,
                            partitionKeyRangeIds,
                            forceRefresh);

                    // remove one replica
                    return res.map(list -> removeOneReplica(list));
                }

                return origCache.getServerAddressesViaGatewayAsync(req,
                        collectionRid,
                        partitionKeyRangeIds,
                        forceRefresh);
            }
        }).when(spyCache).getServerAddressesViaGatewayAsync(ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.anyBoolean());

        httpClientWrapper.capturedRequests.clear();

        // force refresh to replace existing with sub-optimal addresses
        addressesInfosFromCacheObs = spyCache.tryGetAddresses(req, partitionKeyRangeIdentity, true);
        Utils.ValueHolder<AddressInformation[]> suboptimalAddresses = getSuccessResult(addressesInfosFromCacheObs, TIMEOUT);
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getServerAddressesViaGatewayAsync will read addresses from gateway")
                .asList().hasSize(1);
        httpClientWrapper.capturedRequests.clear();

        // relaxes one replica being down
        assertThat(suboptimalAddresses.v.length).isLessThanOrEqualTo((ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize - 1));
        assertThat(suboptimalAddresses.v.length).isGreaterThanOrEqualTo(ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize - 2);
        assertThat(fetchCounter.get()).isEqualTo(1);

        // no refresh, use cache
        addressesInfosFromCacheObs = spyCache.tryGetAddresses(req, partitionKeyRangeIdentity, false);
        suboptimalAddresses = getSuccessResult(addressesInfosFromCacheObs, TIMEOUT);
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getServerAddressesViaGatewayAsync will read addresses from gateway")
                .asList().hasSize(0);
        AssertionsForClassTypes.assertThat(suboptimalAddresses.v).hasSize(ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize - 1);
        assertThat(fetchCounter.get()).isEqualTo(1);

        // wait for refresh time
        TimeUnit.SECONDS.sleep(suboptimalRefreshTime + 1);

        addressesInfosFromCacheObs = spyCache.tryGetAddresses(req, partitionKeyRangeIdentity, false);
        Utils.ValueHolder<AddressInformation[]> addresses = getSuccessResult(addressesInfosFromCacheObs, TIMEOUT);
        AssertionsForClassTypes.assertThat(addresses.v).hasSize(ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize);
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getServerAddressesViaGatewayAsync will read addresses from gateway")
                .asList().hasSize(1);
        assertThat(fetchCounter.get()).isEqualTo(2);
    }

    @Test(groups = { "direct" }, dataProvider = "protocolProvider",timeOut = TIMEOUT)
    public void tryGetAddresses_ForMasterPartition(Protocol protocol) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                            serviceEndpoint,
                                                            protocol,
                                                            authorizationTokenProvider,
                                                            null,
                                                            getHttpClient(configs),
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity("M");
        boolean forceRefreshPartitionAddresses = false;
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs = cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

        ArrayList<AddressInformation> addressInfosFromCache = Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        Mono<List<Address>> masterAddressFromGatewayObs = cache.getMasterAddressesViaGatewayAsync(req, ResourceType.Database,
                null, "/dbs/", false, false, null);
        List<Address> expectedAddresses = getSuccessResult(masterAddressFromGatewayObs, TIMEOUT);

        assertSameAs(addressInfosFromCache, expectedAddresses);
    }

    @DataProvider(name = "refreshTime")
    public Object[][] refreshTime() {
        return new Object[][] {
                // refresh time, wait before doing tryGetAddresses
                { 60, 1 },
                { 1, 2 },
        };
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT, dataProvider = "refreshTime")
    public void tryGetAddresses_ForMasterPartition_MasterPartitionAddressAlreadyCached_NoNewHttpRequest(
            int suboptimalPartitionForceRefreshIntervalInSeconds,
            int waitTimeInBetweenAttemptsInSeconds
            ) throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper clientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;


        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                            serviceEndpoint,
                                                            Protocol.HTTPS,
                                                            authorizationTokenProvider,
                                                            null,
                                                            clientWrapper.getSpyHttpClient(),
                                                            suboptimalPartitionForceRefreshIntervalInSeconds,
                                                            null,
                                                            null,
                                                            ConnectionPolicy.getDefaultPolicy(),
                                                            null,
                                                            null);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity("M");
        boolean forceRefreshPartitionAddresses = false;

        // request master partition info to ensure it is cached.
        AddressInformation[] expectedAddresses = cache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                forceRefreshPartitionAddresses).block().v;

        assertThat(clientWrapper.capturedRequests).asList().hasSize(1);
        clientWrapper.capturedRequests.clear();


        TimeUnit.SECONDS.sleep(waitTimeInBetweenAttemptsInSeconds);

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = cache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                forceRefreshPartitionAddresses);

        AddressInformation[] actualAddresses = getSuccessResult(addressesObs, TIMEOUT).v;

        assertExactlyEqual(actualAddresses, expectedAddresses);

        // the cache address is used. no new http request is sent
        assertThat(clientWrapper.capturedRequests).asList().hasSize(0);
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void tryGetAddresses_ForMasterPartition_ForceRefresh() throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper clientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                            serviceEndpoint,
                                                            Protocol.HTTPS,
                                                            authorizationTokenProvider,
                                                            null,
                                                            clientWrapper.getSpyHttpClient(),
                                                            null,
                                                            null,
                                                            ConnectionPolicy.getDefaultPolicy(),
                                                            null,
                                                            null);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity("M");

        // request master partition info to ensure it is cached.
        AddressInformation[] expectedAddresses = cache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                false)
                .block().v;

        assertThat(clientWrapper.capturedRequests).asList().hasSize(1);
        clientWrapper.capturedRequests.clear();

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = cache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                true);

        AddressInformation[] actualAddresses = getSuccessResult(addressesObs, TIMEOUT).v;

        assertExactlyEqual(actualAddresses, expectedAddresses);

        // the cache address is used. no new http request is sent
        assertThat(clientWrapper.capturedRequests).asList().hasSize(1);
    }

    private static List<Address> removeOneReplica(List<Address> addresses) {
        addresses.remove(0);
        return addresses;
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void tryGetAddresses_SuboptimalMasterPartition_NotStaleEnough_NoRefresh() throws Exception {
        Configs configs = new Configs();
        Instant start = Instant.now();
        HttpClientUnderTestWrapper clientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        String apiType = ApiType.SQL.toString();

        int refreshPeriodInSeconds = 10;

        GatewayAddressCache origCache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                                serviceEndpoint,
                                                                Protocol.HTTPS,
                                                                authorizationTokenProvider,
                                                                null,
                                                                clientWrapper.getSpyHttpClient(),
                                                                refreshPeriodInSeconds,
                                                                ApiType.SQL,
                                                                null,
                                                                ConnectionPolicy.getDefaultPolicy(),
                                                                null,
                                                                null);

        GatewayAddressCache spyCache = Mockito.spy(origCache);

        final AtomicInteger getMasterAddressesViaGatewayAsyncInvocation = new AtomicInteger(0);
        Mockito.doAnswer(new Answer<Mono<List<Address>>>() {
            @Override
            public Mono<List<Address>> answer(InvocationOnMock invocationOnMock) throws Throwable {

                RxDocumentServiceRequest request = invocationOnMock.getArgument(0, RxDocumentServiceRequest.class);
                ResourceType resourceType = invocationOnMock.getArgument(1, ResourceType.class);
                String resourceAddress = invocationOnMock.getArgument(2, String.class);
                String entryUrl = invocationOnMock.getArgument(3, String.class);
                boolean forceRefresh = invocationOnMock.getArgument(4, Boolean.class);
                boolean useMasterCollectionResolver = invocationOnMock.getArgument(5, Boolean.class);

                int cnt = getMasterAddressesViaGatewayAsyncInvocation.getAndIncrement();

                if (cnt == 0) {
                    Mono<List<Address>> res = origCache.getMasterAddressesViaGatewayAsync(
                            request,
                            resourceType,
                            resourceAddress,
                            entryUrl,
                            forceRefresh,
                            useMasterCollectionResolver,
                            null);

                    // remove one replica
                    return res.map(list -> removeOneReplica(list));
                }

                return origCache.getMasterAddressesViaGatewayAsync(
                        request,
                        resourceType,
                        resourceAddress,
                        entryUrl,
                        forceRefresh,
                        useMasterCollectionResolver,
                        null);
                }
            }).when(spyCache).getMasterAddressesViaGatewayAsync(ArgumentMatchers.any(RxDocumentServiceRequest.class), ArgumentMatchers.any(ResourceType.class), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any());


        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity("M");

        // request master partition info to ensure it is cached.
        AddressInformation[] expectedAddresses = spyCache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                false)
                .block().v;

        assertThat(clientWrapper.capturedRequests).asList().hasSize(1);
        assertThat(clientWrapper.capturedRequests.get(0).headers().toMap().get(HttpConstants.HttpHeaders.API_TYPE)).isEqualTo(apiType);
        clientWrapper.capturedRequests.clear();

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = spyCache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                false);

        AddressInformation[] actualAddresses = getSuccessResult(addressesObs, TIMEOUT).v;

        assertExactlyEqual(actualAddresses, expectedAddresses);

        // the cache address is used. no new http request is sent
        assertThat(clientWrapper.capturedRequests).asList().hasSize(0);

        Instant end = Instant.now();
        assertThat(end.minusSeconds(refreshPeriodInSeconds)).isBefore(start);
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void tryGetAddresses_SuboptimalMasterPartition_Stale_DoRefresh() throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper clientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        int refreshPeriodInSeconds = 1;

        GatewayAddressCache origCache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                                serviceEndpoint,
                                                                Protocol.HTTPS,
                                                                authorizationTokenProvider,
                                                                null,
                                                                clientWrapper.getSpyHttpClient(),
                                                                refreshPeriodInSeconds,
                                                                null,
                                                                null,
                                                                ConnectionPolicy.getDefaultPolicy(),
                                                                null,
                                                                null);

        GatewayAddressCache spyCache = Mockito.spy(origCache);

        final AtomicInteger getMasterAddressesViaGatewayAsyncInvocation = new AtomicInteger(0);
        Mockito.doAnswer(new Answer<Mono<List<Address>>>() {
            @Override
            public Mono<List<Address>> answer(InvocationOnMock invocationOnMock) throws Throwable {

                System.out.print("fetch");

                RxDocumentServiceRequest request = invocationOnMock.getArgument(0, RxDocumentServiceRequest.class);
                ResourceType resourceType = invocationOnMock.getArgument(1, ResourceType.class);
                String resourceAddress = invocationOnMock.getArgument(2, String.class);
                String entryUrl = invocationOnMock.getArgument(3, String.class);
                boolean forceRefresh = invocationOnMock.getArgument(4, Boolean.class);
                boolean useMasterCollectionResolver = invocationOnMock.getArgument(5, Boolean.class);

                int cnt = getMasterAddressesViaGatewayAsyncInvocation.getAndIncrement();

                if (cnt == 0) {
                    Mono<List<Address>> res = origCache.getMasterAddressesViaGatewayAsync(
                            request,
                            resourceType,
                            resourceAddress,
                            entryUrl,
                            forceRefresh,
                            useMasterCollectionResolver,
                            null);

                    // remove one replica
                    return res.map(list -> removeOneReplica(list));
                }

                return origCache.getMasterAddressesViaGatewayAsync(
                        request,
                        resourceType,
                        resourceAddress,
                        entryUrl,
                        forceRefresh,
                        useMasterCollectionResolver,
                        null);
            }
        }).when(spyCache).getMasterAddressesViaGatewayAsync(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any());

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity("M");

        // request master partition info to ensure it is cached.
        AddressInformation[] subOptimalAddresses = spyCache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                false)
                .block().v;

        assertThat(getMasterAddressesViaGatewayAsyncInvocation.get()).isEqualTo(1);
        AssertionsForClassTypes.assertThat(subOptimalAddresses).hasSize(ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize - 1);

        Instant start = Instant.now();
        TimeUnit.SECONDS.sleep(refreshPeriodInSeconds + 1);
        Instant end = Instant.now();
        assertThat(end.minusSeconds(refreshPeriodInSeconds)).isAfter(start);

        assertThat(clientWrapper.capturedRequests).asList().hasSize(1);
        clientWrapper.capturedRequests.clear();

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = spyCache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                false);


        AddressInformation[] actualAddresses = getSuccessResult(addressesObs, TIMEOUT).v;
        // the cache address is used. no new http request is sent
        assertThat(clientWrapper.capturedRequests).asList().hasSize(1);
        assertThat(getMasterAddressesViaGatewayAsyncInvocation.get()).isEqualTo(2);
        AssertionsForClassTypes.assertThat(actualAddresses).hasSize(ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize);

        List<Address> fetchedAddresses = origCache.getMasterAddressesViaGatewayAsync(req, ResourceType.Database,
                null, "/dbs/", false, false, null).block();

        assertSameAs(ImmutableList.copyOf(actualAddresses),  fetchedAddresses);
    }

    @SuppressWarnings("unchecked")
    @Test(groups = { "direct" }, dataProvider = "replicaValidationArgsProvider", timeOut = TIMEOUT)
    public void tryGetAddress_replicaValidationTests(boolean replicaValidationEnabled, boolean submitOpenConnectionTasksAndInitCaches) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(Protocol.TCP).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);
        Uri addressUriMock = Mockito.mock(Uri.class);
        OpenConnectionTask dummyOpenConnectionsTask = new OpenConnectionTask("", serviceEndpoint, addressUriMock, Configs.getMinConnectionPoolSizePerEndpoint());

        if (replicaValidationEnabled) {
            System.setProperty("COSMOS.REPLICA_ADDRESS_VALIDATION_ENABLED", "true");
            assertThat(Configs.isReplicaAddressValidationEnabled()).isTrue();
        } else {
            System.setProperty("COSMOS.REPLICA_ADDRESS_VALIDATION_ENABLED", "false");
            assertThat(Configs.isReplicaAddressValidationEnabled()).isFalse();
        }

        GatewayAddressCache cache = new GatewayAddressCache(
                mockDiagnosticsClientContext(),
                serviceEndpoint,
                Protocol.TCP,
                authorizationTokenProvider,
                null,
                httpClientWrapper.getSpyHttpClient(),
                null,
                null,
                ConnectionPolicy.getDefaultPolicy(),
                proactiveOpenConnectionsProcessorMock,
                null);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(
                        mockDiagnosticsClientContext(),
                        OperationType.Create,
                        ResourceType.Document,
                        getCollectionSelfLink(),
                        new Database(),
                        new HashMap<>());

        if (submitOpenConnectionTasksAndInitCaches) {
            List<PartitionKeyRangeIdentity> pkriList = Arrays.asList(new PartitionKeyRangeIdentity("0"));
            cache.resolveAddressesAndInitCaches(createdCollection.getSelfLink(), createdCollection, pkriList).blockLast();
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(1))
                    .recordCollectionRidsAndUrisUnderOpenConnectionsAndInitCaches(Mockito.any(), Mockito.any());
            Mockito.clearInvocations(proactiveOpenConnectionsProcessorMock);
            httpClientWrapper.capturedRequests.clear();
            Mockito.when(proactiveOpenConnectionsProcessorMock.isCollectionRidUnderOpenConnectionsFlow(Mockito.anyString())).thenReturn(true);
        }

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "0");
        boolean forceRefreshPartitionAddresses = true;

        Mockito.when(proactiveOpenConnectionsProcessorMock.submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt()))
                .thenReturn(dummyOpenConnectionsTask);

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs =
                cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

        ArrayList<AddressInformation> addressInfosFromCache =
                Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getAddress will read addresses from gateway")
                .asList().hasSize(1);

        if (replicaValidationEnabled) {
            ArgumentCaptor<Uri> openConnectionArguments = ArgumentCaptor.forClass(Uri.class);
            ArgumentCaptor<URI> serviceEndpointArguments = ArgumentCaptor.forClass(URI.class);

            if (submitOpenConnectionTasksAndInitCaches) {

                // If submitOpenConnectionTasksAndInitCaches is called, then replica validation will also include for unknown status
                // isCollectionRidUnderOpenConnectionsFlow called 6 times
                // 1. as forceRefreshPartitionAddresses = true, this invokes isCollectionRidUnderOpenConnectionsFlow eventually
                // refreshing collectionRid->addresses map maintained by proactiveOpenConnectionsProcessor to track containers / addresses
                // under connection warm up flow
                // 2. replica validation will get triggered in case of unhealthyPending / unknown addresses, replica validation will do a
                // submitOpenConnectionTaskOutsideLoop for each of these addresses but before that it will also do
                // isCollectionRidUnderOpenConnectionsFlow check to determine the no. of connections to open
                // 3. it needs to be checked if the replicas (up for validation) with Unknown health status are used by a container which
                // is part of the warm-up flow - isCollectionRidUnderOpenConnectionsFlow is called here again (called up to 4 times - 1 for each replica)
                // for a given physical partition
                Mockito
                    .verify(proactiveOpenConnectionsProcessorMock, Mockito.atLeastOnce())
                    .submitOpenConnectionTaskOutsideLoop(Mockito.any(), serviceEndpointArguments.capture(), openConnectionArguments.capture(), Mockito.anyInt());
                assertThat(openConnectionArguments.getAllValues()).hasSize(addressInfosFromCache.size());
                Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(6))
                        .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());
            } else {
                // Open connection will only be called for unhealthyPending status address
                Mockito
                    .verify(proactiveOpenConnectionsProcessorMock, Mockito.times(0))
                    .submitOpenConnectionTaskOutsideLoop(Mockito.any(), serviceEndpointArguments.capture(), openConnectionArguments.capture(), Mockito.anyInt());
                // isCollectionRidUnderOpenConnectionsFlow called once
                // 1. as forceRefreshPartitionAddresses = true, this invokes isCollectionRidUnderOpenConnectionsFlow eventually
                // refreshing collectionRid->addresses map maintained by proactiveOpenConnectionsProcessor to track containers / addresses
                // under connection warm up flow
                // 2. replica validation will get triggered only in case of unhealthyPending addresses (if no connection warm up is used at all),
                Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(1))
                        .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());
            }
        } else {
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.never())
                    .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(1))
                    .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());
        }

        httpClientWrapper.capturedRequests.clear();
        Mockito.clearInvocations(proactiveOpenConnectionsProcessorMock);

        // Mark one of the uri as unhealthy, one as unknown, others as connected
        // and then force refresh the addresses again, make sure the health status of the uri is reserved
        assertThat(addressInfosFromCache.size()).isGreaterThan(2);
        Uri unknownAddressUri = null;
        Uri unhealthyAddressUri = null;
        for (int i = 0; i < addressInfosFromCache.size(); i++) {
            if (i == 0) {
                unknownAddressUri = addressInfosFromCache.get(0).getPhysicalUri();
                continue;
            }
            if (i == 1) {
                unhealthyAddressUri = addressInfosFromCache.get(1).getPhysicalUri();
                unhealthyAddressUri.setUnhealthy();
            } else {
                addressInfosFromCache.get(i).getPhysicalUri().setConnected();
            }
        }

        ArrayList<AddressInformation> refreshedAddresses =
                Lists.newArrayList(getSuccessResult(cache.tryGetAddresses(req, partitionKeyRangeIdentity, true), TIMEOUT).v);
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getAddress will read addresses from gateway")
                .asList().hasSize(1);
        assertThat(refreshedAddresses).hasSize(addressInfosFromCache.size()).containsAll(addressInfosFromCache);

        // validate connected status will be reserved
        // validate unhealthy status will change into unhealthyPending status
        // Validate openConnection will be called for addresses in unhealthyPending status
        // Validate openConnection will be called for addresses in unknown status if submitOpenConnectionTasksAndInitCaches is called
        for (AddressInformation addressInformation : refreshedAddresses) {
            if (addressInformation.getPhysicalUri().equals(unknownAddressUri)) {
                assertThat(addressInformation.getPhysicalUri().getHealthStatus()).isEqualTo(Unknown);
            } else if (addressInformation.getPhysicalUri().equals(unhealthyAddressUri)) {
                assertThat(addressInformation.getPhysicalUri().getHealthStatus()).isEqualTo(UnhealthyPending);
            } else {
                assertThat(addressInformation.getPhysicalUri().getHealthStatus()).isEqualTo(Connected);
            }
        }

        if (replicaValidationEnabled) {
            ArgumentCaptor<Uri> openConnectionArguments = ArgumentCaptor.forClass(Uri.class);
            ArgumentCaptor<URI> serviceEndpointArguments = ArgumentCaptor.forClass(URI.class);

            Mockito
                .verify(proactiveOpenConnectionsProcessorMock, Mockito.atLeastOnce())
                .submitOpenConnectionTaskOutsideLoop(Mockito.any(), serviceEndpointArguments.capture(), openConnectionArguments.capture(), Mockito.anyInt());
            if (submitOpenConnectionTasksAndInitCaches) {
                assertThat(openConnectionArguments.getAllValues()).containsExactlyElementsOf(Arrays.asList(unhealthyAddressUri, unknownAddressUri));
            } else {
                assertThat(openConnectionArguments.getAllValues()).containsExactly(unhealthyAddressUri);
            }

        } else {
            Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.never())
                    .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
        }

        System.clearProperty("COSMOS.REPLICA_ADDRESS_VALIDATION_ENABLED");
    }

    @Test(groups = { "direct" },  timeOut = TIMEOUT)
    public void tryGetAddress_failedEndpointTests() throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(Protocol.TCP).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);

        GatewayAddressCache cache = new GatewayAddressCache(
                mockDiagnosticsClientContext(),
                serviceEndpoint,
                Protocol.TCP,
                authorizationTokenProvider,
                null,
                httpClientWrapper.getSpyHttpClient(),
                null,
                null,
                ConnectionPolicy.getDefaultPolicy(),
                proactiveOpenConnectionsProcessorMock,
                null);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(
                        mockDiagnosticsClientContext(),
                        OperationType.Create,
                        ResourceType.Document,
                        getCollectionSelfLink(),
                        new Database(),
                        new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "0");
        boolean forceRefreshPartitionAddresses = false;

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs =
                cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

        ArrayList<AddressInformation> addressInfosFromCache =
                Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getAddress will read addresses from gateway")
                .asList().hasSize(1);

        // Mark all the uris in connected status
        // Setup request failedEndpoints, and then refresh addresses again(with forceRefresh = false), confirm the failed endpoint uri is marked as unhealthy
        httpClientWrapper.capturedRequests.clear();
        Mockito.clearInvocations(proactiveOpenConnectionsProcessorMock);
        for (AddressInformation address : addressInfosFromCache) {
            address.getPhysicalUri().setConnected();
        }

        req.requestContext.getFailedEndpoints().add(addressInfosFromCache.get(0).getPhysicalUri());

        ArrayList<AddressInformation> refreshedAddresses =
                Lists.newArrayList(getSuccessResult(cache.tryGetAddresses(req, partitionKeyRangeIdentity, false), TIMEOUT).v);
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getAddress will read from cache")
                .asList().hasSize(0);
        assertThat(refreshedAddresses).hasSize(addressInfosFromCache.size()).containsAll(addressInfosFromCache);
        assertThat(refreshedAddresses.get(0).getPhysicalUri().getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);
    }

    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void tryGetAddress_unhealthyStatus_forceRefresh() throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(Protocol.TCP).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);

        GatewayAddressCache cache = new GatewayAddressCache(
                mockDiagnosticsClientContext(),
                serviceEndpoint,
                Protocol.TCP,
                authorizationTokenProvider,
                null,
                httpClientWrapper.getSpyHttpClient(),
                null,
                null,
                ConnectionPolicy.getDefaultPolicy(),
                proactiveOpenConnectionsProcessorMock,
                null);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(
                        mockDiagnosticsClientContext(),
                        OperationType.Create,
                        ResourceType.Document,
                        getCollectionSelfLink(),
                        new Database(),
                        new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "0");
        boolean forceRefreshPartitionAddresses = false;

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs =
                cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

        ArrayList<AddressInformation> addressInfosFromCache =
                Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        // isCollectionRidUnderOpenConnectionsFlow is not called since forceRefreshPartitionAddresses=false
        // and replicaValidationScope just has 'Unhealthy'
        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(0))
                .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());

        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(0))
                .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());

        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getAddress will read addresses from gateway")
                .asList().hasSize(1);

        Mockito.clearInvocations(proactiveOpenConnectionsProcessorMock);
        httpClientWrapper.capturedRequests.clear();

        // mark one of the uri as unhealthy, and validate the address cache will be refreshed after 1 min
        Uri unhealthyAddressUri = addressInfosFromCache.get(0).getPhysicalUri();
        unhealthyAddressUri.setUnhealthy();
        Field lastTransitionToUnhealthyTimestampField = Uri.class.getDeclaredField("lastTransitionToUnhealthyTimestamp");
        lastTransitionToUnhealthyTimestampField.setAccessible(true);
        lastTransitionToUnhealthyTimestampField.set(unhealthyAddressUri, Instant.now().minusMillis(Duration.ofMinutes(1).toMillis()));

        // using forceRefresh false
        // but as there is one address has been stuck in unhealthy status for more than 1 min,
        // so after getting the addresses, it will refresh the cache
        ArrayList<AddressInformation> cachedAddresses =
                Lists.newArrayList(getSuccessResult(cache.tryGetAddresses(req, partitionKeyRangeIdentity, false), TIMEOUT).v);

        // since the refresh will happen asynchronously in the background, wait here some time for it to happen
        Thread.sleep(500);

        // validate the cache will be refreshed
        assertThat(httpClientWrapper.capturedRequests)
                .describedAs("getAddress will read addresses from gateway")
                .asList().hasSize(1);
        assertThat(cachedAddresses).hasSize(addressInfosFromCache.size()).containsAll(addressInfosFromCache);

        // isCollectionRidUnderOpenConnectionsFlow called 2 times
        // 1. forceRefreshPartitionAddresses = false but when addresses are unhealthy for long enough, the SDK does a forceRefresh=true for addresses
        // which refreshes collectionRid->addresses map maintained by proactiveOpenConnectionsProcessor to track containers / addresses
        // under connection warm up flow, the refresh of this map only happens if isCollectionRidUnderOpenConnectionsFlow is true
        // 2. replica validation will get triggered in case of unhealthyPending addresses, replica validation will do a
        // submitOpenConnectionTaskOutsideLoop for each of these addresses but before that it will also do
        // isCollectionRidUnderOpenConnectionsFlow check to determine the no. of connections to open
        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(2))
                .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());

        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(1))
                .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
    }

    @Test(groups = {"direct"}, timeOut = 2 * TIMEOUT)
    public void tryGetAddress_repeatedlySetUnhealthyStatus_forceRefresh() throws InterruptedException, URISyntaxException {
        Configs configs = ConfigsBuilder.instance().withProtocol(Protocol.TCP).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);

        GatewayAddressCache cache = new GatewayAddressCache(
            mockDiagnosticsClientContext(),
            serviceEndpoint,
            Protocol.TCP,
            authorizationTokenProvider,
            null,
            httpClientWrapper.getSpyHttpClient(),
            null,
            null,
            ConnectionPolicy.getDefaultPolicy(),
            proactiveOpenConnectionsProcessorMock,
            null);

        RxDocumentServiceRequest req =
            RxDocumentServiceRequest.create(
                mockDiagnosticsClientContext(),
                OperationType.Create,
                ResourceType.Document,
                getCollectionSelfLink(),
                new Database(),
                new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "0");
        boolean forceRefreshPartitionAddresses = false;

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs =
            cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

        ArrayList<AddressInformation> addressInfosFromCache =
            Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        // isCollectionRidUnderOpenConnectionsFlow is not called since forceRefreshPartitionAddresses=false
        // and replicaValidationScope just has 'Unhealthy'
        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(0))
               .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());

        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(0))
               .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());

        assertThat(httpClientWrapper.capturedRequests)
            .describedAs("getAddress will read addresses from gateway")
            .asList().hasSize(1);

        Mockito.clearInvocations(proactiveOpenConnectionsProcessorMock);
        httpClientWrapper.capturedRequests.clear();

        // mark one of the uri as unhealthy, and validate the address cache will be refreshed after 1 min
        Uri unhealthyAddressUri = addressInfosFromCache.get(0).getPhysicalUri();
        unhealthyAddressUri.setUnhealthy();
        Thread.sleep(60 * 1000);
        // setting uri as Unhealthy on a still Unhealthy uri should still cause address refresh to be triggerred
        unhealthyAddressUri.setUnhealthy();
        // using forceRefresh false
        // but as there is one address has been stuck in unhealthy status for more than 1 min,
        // so after getting the addresses, it will refresh the cache
        ArrayList<AddressInformation> cachedAddresses =
            Lists.newArrayList(getSuccessResult(cache.tryGetAddresses(req, partitionKeyRangeIdentity, false), TIMEOUT).v);

        // since the refresh will happen asynchronously in the background, wait here some time for it to happen
        Thread.sleep(500);

        // validate the cache will be refreshed
        assertThat(httpClientWrapper.capturedRequests)
            .describedAs("getAddress will read addresses from gateway")
            .asList().hasSize(1);
        assertThat(cachedAddresses).hasSize(addressInfosFromCache.size()).containsAll(addressInfosFromCache);

        // isCollectionRidUnderOpenConnectionsFlow called twice
        // 1. forceRefreshPartitionAddresses = false but when addresses are unhealthy for long enough, the SDK does a forceRefresh=true for addresses
        // which refreshes collectionRid->addresses map maintained by proactiveOpenConnectionsProcessor to track containers / addresses
        // under connection warm up flow, the refresh of this map only happens if isCollectionRidUnderOpenConnectionsFlow is true
        // 2. replica validation will get triggered in case of unhealthyPending / unknown addresses, replica validation will do a
        // submitOpenConnectionTaskOutsideLoop for each of these addresses but before that it will also do
        // isCollectionRidUnderOpenConnectionsFlow check to determine the no. of connections to open
        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(2))
               .isCollectionRidUnderOpenConnectionsFlow(Mockito.any());

        Mockito.verify(proactiveOpenConnectionsProcessorMock, Mockito.times(1))
               .submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test(groups = { "direct" }, dataProvider = "isCollectionUnderWarmUpFlowArgsProvider", timeOut = TIMEOUT)
    public void validateReplicaAddressesTests(boolean isCollectionUnderWarmUpFlow) throws URISyntaxException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Configs configs = ConfigsBuilder.instance().withProtocol(Protocol.TCP).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);
        Uri addressUriMock = Mockito.mock(Uri.class);
        OpenConnectionTask dummyOpenConnectionsTask = new OpenConnectionTask("", serviceEndpoint, addressUriMock, Configs.getMinConnectionPoolSizePerEndpoint());

        GatewayAddressCache cache = new GatewayAddressCache(
                mockDiagnosticsClientContext(),
                serviceEndpoint,
                Protocol.TCP,
                authorizationTokenProvider,
                null,
                httpClientWrapper.getSpyHttpClient(),
                null,
                null,
                ConnectionPolicy.getDefaultPolicy(),
                proactiveOpenConnectionsProcessorMock,
                null);

        Mockito.when(proactiveOpenConnectionsProcessorMock.submitOpenConnectionTaskOutsideLoop(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(dummyOpenConnectionsTask);

        Method validateReplicaAddressesMethod =
            GatewayAddressCache.class.getDeclaredMethod("validateReplicaAddresses", new Class[] { String.class, AddressInformation[].class });
        validateReplicaAddressesMethod.setAccessible(true);

        // connected status
        AddressInformation address1 = new AddressInformation(true, true, "rntbd://127.0.0.1:1", Protocol.TCP);
        address1.getPhysicalUri().setConnected();

        // remain in unknown status
        AddressInformation address2 = new AddressInformation(true, false, "rntbd://127.0.0.1:2", Protocol.TCP);

        // unhealthy status
        AddressInformation address3 = new AddressInformation(true, false, "rntbd://127.0.0.1:3", Protocol.TCP);
        address3.getPhysicalUri().setUnhealthy();

        // unhealthy pending status
        AddressInformation address4 = new AddressInformation(true, false, "rntbd://127.0.0.1:4", Protocol.TCP);
        AtomicReference<Uri.HealthStatusAndDiagnosticStringTuple> healthStatus = ReflectionUtils.getHealthStatus(address4.getPhysicalUri());
        healthStatus.set(new Uri.HealthStatusAndDiagnosticStringTuple(new URI(address4.getPhysicalUri().getURIAsString()), UnhealthyPending));

        // Set the replica validation scope
        Set<Uri.HealthStatus> replicaValidationScopes = ReflectionUtils.getReplicaValidationScopes(cache);
        replicaValidationScopes.add(Unknown);
        replicaValidationScopes.add(UnhealthyPending);

        if (isCollectionUnderWarmUpFlow) {
            Mockito
                .when(proactiveOpenConnectionsProcessorMock.isCollectionRidUnderOpenConnectionsFlow(Mockito.anyString()))
                .thenReturn(true);
        } else {
            Mockito
                .when(proactiveOpenConnectionsProcessorMock.isCollectionRidUnderOpenConnectionsFlow(Mockito.anyString()))
                .thenReturn(false);
        }

        validateReplicaAddressesMethod
            .invoke(
                cache,
                new Object[]{ createdCollection.getResourceId(), new AddressInformation[]{ address1, address2, address3, address4 }}) ;

        // Validate openConnection will only be called for address in unhealthyPending status
        ArgumentCaptor<Uri> openConnectionArguments = ArgumentCaptor.forClass(Uri.class);
        ArgumentCaptor<URI> serviceEndpointArguments = ArgumentCaptor.forClass(URI.class);

        if (isCollectionUnderWarmUpFlow) {
            Mockito
                .verify(proactiveOpenConnectionsProcessorMock, Mockito.times(2))
                .submitOpenConnectionTaskOutsideLoop(Mockito.any(), serviceEndpointArguments.capture(), openConnectionArguments.capture(), Mockito.anyInt());

            // when collection is under warm up flow both Unknown and UnhealthyPending are in the scope
            // for replica validation
            // address4 is in an UnhealthyPending state (prioritize UnhealthyPending for replica validation)
            // and address2 is in an Unknown state
            assertThat(openConnectionArguments.getAllValues()).containsExactlyElementsOf(
                Arrays.asList(address4, address2)
                      .stream()
                      .map(addressInformation -> addressInformation.getPhysicalUri())
                      .collect(Collectors.toList()));
        } else {
            // when collection is not under warm up flow only UnhealthyPending is in the scope
            // for replica validation
            // address4 is in an UnhealthyPending state (prioritize UnhealthyPending for replica validation)
            // and address2 is in an Unknown state (excluded from replica validation)
            Mockito
                .verify(proactiveOpenConnectionsProcessorMock, Mockito.times(1))
                .submitOpenConnectionTaskOutsideLoop(Mockito.any(), serviceEndpointArguments.capture(), openConnectionArguments.capture(), Mockito.anyInt());

            // Only address4 is in an UnhealthyPending state
            assertThat(openConnectionArguments.getAllValues()).containsExactlyElementsOf(
                Arrays.asList(address4)
                      .stream()
                      .map(addressInformation -> addressInformation.getPhysicalUri())
                      .collect(Collectors.toList()));
        }
    }

    @SuppressWarnings("rawtypes")
    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void mergeAddressesTests() throws URISyntaxException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Configs configs = ConfigsBuilder.instance().withProtocol(Protocol.TCP).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        GatewayAddressCache cache = new GatewayAddressCache(
                mockDiagnosticsClientContext(),
                serviceEndpoint,
                Protocol.TCP,
                authorizationTokenProvider,
                null,
                httpClientWrapper.getSpyHttpClient(),
                null,
                null,
                ConnectionPolicy.getDefaultPolicy(),
                null,
                null);

        // connected status
        AddressInformation address1 = new AddressInformation(true, true, "rntbd://127.0.0.1:1", Protocol.TCP);
        address1.getPhysicalUri().setConnected();

        // unhealthyStatus
        AddressInformation address2 = new AddressInformation(true, false, "rntbd://127.0.0.1:2", Protocol.TCP);
        address2.getPhysicalUri().setUnhealthy();

        AddressInformation address3 = new AddressInformation(true, false, "rntbd://127.0.0.1:3", Protocol.TCP);
        AddressInformation address4 = new AddressInformation(true, false, "rntbd://127.0.0.1:4", Protocol.TCP);
        AddressInformation address5 = new AddressInformation(true, false, "rntbd://127.0.0.1:5", Protocol.TCP);
        AddressInformation address6 = new AddressInformation(true, false, "rntbd://127.0.0.1:6", Protocol.TCP);


        AddressInformation[] cachedAddresses = new AddressInformation[] { address1, address2, address3, address4 };
        // when decide to whether to use cached addressInformation, it will compare physical uri, protocol, isPrimary
        AddressInformation address7 = new AddressInformation(true, true, "rntbd://127.0.0.1:2", Protocol.TCP);

        AddressInformation[] newAddresses = new AddressInformation[] {
                new AddressInformation(true, true, "rntbd://127.0.0.1:1", Protocol.TCP),
                address7,
                address5,
                address6 };

        Method mergeAddressesMethod =
                GatewayAddressCache.class.getDeclaredMethod(
                        "mergeAddresses",
                        new Class[] { AddressInformation[].class, AddressInformation[].class });
        mergeAddressesMethod.setAccessible(true);
        AddressInformation[] mergedAddresses =
                (AddressInformation[]) mergeAddressesMethod.invoke(cache, new Object[]{ newAddresses, cachedAddresses });

        assertThat(mergedAddresses).hasSize(newAddresses.length)
                .containsExactly(address1, address7, address5, address6);
    }

    public static void assertSameAs(List<AddressInformation> actual, List<Address> expected) {
        assertThat(actual).asList().hasSize(expected.size());
        for(int i = 0; i < expected.size(); i++) {
            assertEqual(actual.get(i), expected.get(i));
        }
    }

    private static void assertEqual(AddressInformation actual, Address expected) {
        assertThat(actual.getPhysicalUri().getURIAsString()).isEqualTo(expected.getPhyicalUri().replaceAll("/+$", "/"));
        assertThat(actual.getProtocolScheme()).isEqualTo(expected.getProtocolScheme().toLowerCase());
        assertThat(actual.isPrimary()).isEqualTo(expected.isPrimary());
    }

    private static void assertEqual(AddressInformation actual, AddressInformation expected) {
        assertThat(actual.getPhysicalUri()).isEqualTo(expected.getPhysicalUri());
        assertThat(actual.getProtocolName()).isEqualTo(expected.getProtocolName());
        assertThat(actual.isPrimary()).isEqualTo(expected.isPrimary());
        assertThat(actual.isPublic()).isEqualTo(expected.isPublic());
    }

    public static void assertExactlyEqual(AddressInformation[] actual, AddressInformation[] expected) {
        assertExactlyEqual(Arrays.asList(actual), Arrays.asList(expected));
    }

    public static void assertExactlyEqual(List<AddressInformation> actual, List<AddressInformation> expected) {
        assertThat(actual).asList().hasSize(expected.size());
        for(int i = 0; i < expected.size(); i++) {
            assertEqual(actual.get(i), expected.get(i));
        }
    }

    public static<T> T getSuccessResult(Mono<T> observable, long timeout) {
        TestSubscriber<T> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        return testSubscriber.values().get(0);
    }

    public static void validateSuccess(Mono<List<Address>> observable,
                                       PartitionReplicasAddressesValidator validator,
                                       HttpClientUnderTestWrapper httpClient,
                                       RxDocumentServiceRequest serviceRequest,
                                       int requestIndex,
                                       long timeout) {
        TestSubscriber<List<Address>> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
        // Verifying activity id is being set in header on address call to gateway.
        String addressResolutionActivityId =
            BridgeInternal.getClientSideRequestStatics(serviceRequest.requestContext.cosmosDiagnostics).getAddressResolutionStatistics().keySet().iterator().next();
        assertThat(httpClient.capturedRequests.get(requestIndex).headers().value(HttpConstants.HttpHeaders.ACTIVITY_ID)).isNotNull();
        assertThat(httpClient.capturedRequests.get(requestIndex).headers().value(HttpConstants.HttpHeaders.ACTIVITY_ID)).isEqualTo(addressResolutionActivityId);
    }

    @BeforeClass(groups = { "direct" }, timeOut = SETUP_TIMEOUT)
    public void before_GatewayAddressCacheTest() {
        client = clientBuilder().build();
        createdDatabase = SHARED_DATABASE;

        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(30000);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition(), options);
    }

    @AfterClass(groups = { "direct" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(client, createdCollection);
        safeClose(client);
    }

    static protected DocumentCollection getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId("mycol");
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    private HttpClient getHttpClient(Configs configs) {
        return HttpClient.createFixed(new HttpClientConfig(configs));
    }

    private HttpClientUnderTestWrapper getHttpClientUnderTestWrapper(Configs configs) {
        HttpClient origHttpClient = getHttpClient(configs);
        return new HttpClientUnderTestWrapper(origHttpClient);
    }

    public String getNameBasedCollectionLink() {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId();
    }

    public String getCollectionSelfLink() {
        return createdCollection.getSelfLink();
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }
}
