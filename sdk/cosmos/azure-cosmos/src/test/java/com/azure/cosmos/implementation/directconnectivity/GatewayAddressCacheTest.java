// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConfigsBuilder;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpClientUnderTestWrapper;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.TestSuiteBase;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpClientConfig;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.AssertionsForClassTypes;
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

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    @Test(groups = { "direct" }, dataProvider = "targetPartitionsKeyRangeListAndCollectionLinkParams", timeOut = TIMEOUT)
    public void getServerAddressesViaGateway(List<String> partitionKeyRangeIds,
                                             String collectionLink,
                                             Protocol protocol) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        // ask gateway for the addresses
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                serviceEndpoint,
                protocol,
                authorizationTokenProvider,
                null,
                getHttpClient(configs),
                false);
        for (int i = 0; i < 2; i++) {
            RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                    collectionLink + "/docs/",
                    getDocumentDefinition(), new HashMap<>());
            if (i == 1) {
                req.forceCollectionRoutingMapRefresh = true; //testing address api with x-ms-collectionroutingmap-refresh true
            }
            Mono<List<Address>> addresses = cache.getServerAddressesViaGatewayAsync(
                req, createdCollection.getResourceId(), partitionKeyRangeIds, false);

            PartitionReplicasAddressesValidator validator = new PartitionReplicasAddressesValidator.Builder()
                .withProtocol(protocol)
                .replicasOfPartitions(partitionKeyRangeIds)
                .build();

            validateSuccess(addresses, validator, TIMEOUT);
        }
    }

    @Test(groups = { "direct" }, dataProvider = "protocolProvider", timeOut = TIMEOUT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void getMasterAddressesViaGatewayAsync(Protocol protocol) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        // ask gateway for the addresses
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(), serviceEndpoint,
                                                            protocol,
                                                            authorizationTokenProvider,
                                                            null,
                                                            getHttpClient(configs),
                                                            false);
        for (int i = 0; i < 2; i++) {
            RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                    "/dbs",
                    new Database(), new HashMap<>());
            if (i == 1) {
                req.forceCollectionRoutingMapRefresh = true; //testing address api with x-ms-collectionroutingmap-refresh true
            }
            Mono<List<Address>> addresses = cache.getMasterAddressesViaGatewayAsync(req, ResourceType.Database,
                null, "/dbs/", false, false, null);

            PartitionReplicasAddressesValidator validator = new PartitionReplicasAddressesValidator.Builder()
                .withProtocol(protocol)
                .replicasOfSamePartition()
                .build();

            validateSuccess(addresses, validator, TIMEOUT);
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

    @Test(groups = { "direct" }, dataProvider = "targetPartitionsKeyRangeAndCollectionLinkParams", timeOut = TIMEOUT)
    public void tryGetAddresses_ForDataPartitions(String partitionKeyRangeId, String collectionLink, Protocol protocol) throws Exception {
        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(), serviceEndpoint,
                                                            protocol,
                                                            authorizationTokenProvider,
                                                            null,
                                                            getHttpClient(configs),
                                                            false);

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

    @Test(groups = { "direct" }, dataProvider = "targetPartitionsKeyRangeAndCollectionLinkParams", timeOut = TIMEOUT)
    public void tryGetAddress_OnConnectionEvent_Refresh(String partitionKeyRangeId, String collectionLink, Protocol protocol) throws Exception {

        Configs configs = ConfigsBuilder.instance().withProtocol(protocol).build();
        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        GatewayAddressCache cache = new GatewayAddressCache(
            mockDiagnosticsClientContext(),
            serviceEndpoint,
            protocol,
            authorizationTokenProvider,
            null,
            httpClientWrapper.getSpyHttpClient(),
            true);

        RxDocumentServiceRequest req =
            RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                collectionLink,
                new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), partitionKeyRangeId);
        boolean forceRefreshPartitionAddresses = false;

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs =
            cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

        ArrayList<AddressInformation> addressInfosFromCache =
            Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

        assertThat(httpClientWrapper.capturedRequests)
            .describedAs("getAddress will read addresses from gateway")
            .asList().hasSize(1);

        httpClientWrapper.capturedRequests.clear();

        // for the second request with the same partitionkeyRangeIdentity, the address result should be fetched from the cache
        getSuccessResult(cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses), TIMEOUT);
        assertThat(httpClientWrapper.capturedRequests)
            .describedAs("getAddress should read from cache")
            .asList().hasSize(0);

        httpClientWrapper.capturedRequests.clear();

        // Now emulate onConnectionEvent happened, and the address should be removed from the cache
        cache.updateAddresses(addressInfosFromCache.get(0).getServerKey());
        getSuccessResult(cache.tryGetAddresses(req, partitionKeyRangeIdentity, forceRefreshPartitionAddresses), TIMEOUT);
        assertThat(httpClientWrapper.capturedRequests)
            .describedAs("getAddress will read addresses from gateway after onConnectionEvent")
            .asList().hasSize(1);
    }

    @DataProvider(name = "openAsyncTargetAndTargetPartitionsKeyRangeAndCollectionLinkParams")
    public Object[][] openAsyncTargetAndPartitionsKeyRangeTargetAndCollectionLinkParams() {
        return new Object[][] {
                // openAsync target partition key range ids, target partition key range id, collection link
                { ImmutableList.of("0", "1"), "0", getNameBasedCollectionLink() },
                { ImmutableList.of("0", "1"), "1", getNameBasedCollectionLink() },
                { ImmutableList.of("0", "1"), "1", getCollectionSelfLink() },
        };
    }

    @Test(groups = { "direct" },
            dataProvider = "openAsyncTargetAndTargetPartitionsKeyRangeAndCollectionLinkParams",
            timeOut = TIMEOUT)
    public void tryGetAddresses_ForDataPartitions_AddressCachedByOpenAsync_NoHttpRequest(
            List<String> allPartitionKeyRangeIds,
            String partitionKeyRangeId, String collectionLink) throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                            serviceEndpoint,
                                                            Protocol.HTTPS,
                                                            authorizationTokenProvider,
                                                            null,
                                                            httpClientWrapper.getSpyHttpClient(),
                                                            false);

        String collectionRid = createdCollection.getResourceId();

        List<PartitionKeyRangeIdentity> pkriList = allPartitionKeyRangeIds.stream().map(
                pkri -> new PartitionKeyRangeIdentity(collectionRid, pkri)).collect(Collectors.toList());

        cache.openAsync(createdCollection, pkriList).block();

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
            String collectionLink) throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        GatewayAddressCache cache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                            serviceEndpoint,
                                                            Protocol.HTTPS,
                                                            authorizationTokenProvider,
                                                            null,
                                                            httpClientWrapper.getSpyHttpClient(),
                                                            false);

        String collectionRid = createdCollection.getResourceId();

        List<PartitionKeyRangeIdentity> pkriList = allPartitionKeyRangeIds.stream().map(
                pkri -> new PartitionKeyRangeIdentity(collectionRid, pkri)).collect(Collectors.toList());

        cache.openAsync(createdCollection, pkriList).block();

        assertThat(httpClientWrapper.capturedRequests).asList().hasSize(1);
        httpClientWrapper.capturedRequests.clear();

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                        collectionLink,
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, partitionKeyRangeId);
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs = cache.tryGetAddresses(req, partitionKeyRangeIdentity, true);
        ArrayList<AddressInformation> addressInfosFromCache = Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

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
            String collectionLink) throws Exception {
        Configs configs = new Configs();
        HttpClientUnderTestWrapper httpClientWrapper = getHttpClientUnderTestWrapper(configs);

        URI serviceEndpoint = new URI(TestConfigurations.HOST);
        IAuthorizationTokenProvider authorizationTokenProvider = (RxDocumentClientImpl) client;

        int suboptimalRefreshTime = 2;

        GatewayAddressCache origCache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                                serviceEndpoint,
                                                                Protocol.HTTPS,
                                                                authorizationTokenProvider,
                                                                null,
                                                                httpClientWrapper.getSpyHttpClient(),
                                                                suboptimalRefreshTime,
                                                                false);

        String collectionRid = createdCollection.getResourceId();

        List<PartitionKeyRangeIdentity> pkriList = allPartitionKeyRangeIds.stream().map(
                pkri -> new PartitionKeyRangeIdentity(collectionRid, pkri)).collect(Collectors.toList());

        origCache.openAsync(createdCollection, pkriList).block();

        assertThat(httpClientWrapper.capturedRequests).asList().hasSize(1);
        httpClientWrapper.capturedRequests.clear();

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                        collectionLink,
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, partitionKeyRangeId);
        Mono<Utils.ValueHolder<AddressInformation[]>> addressesInfosFromCacheObs = origCache.tryGetAddresses(req, partitionKeyRangeIdentity, true);
        ArrayList<AddressInformation> addressInfosFromCache = Lists.newArrayList(getSuccessResult(addressesInfosFromCacheObs, TIMEOUT).v);

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
                                                            false);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "M");
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
                                                            false);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "M");
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
                                                            false);

        RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Database,
                        "/dbs",
                        new Database(), new HashMap<>());

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "M");

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

        int refreshPeriodInSeconds = 10;

        GatewayAddressCache origCache = new GatewayAddressCache(mockDiagnosticsClientContext(),
                                                                serviceEndpoint,
                                                                Protocol.HTTPS,
                                                                authorizationTokenProvider,
                                                                null,
                                                                clientWrapper.getSpyHttpClient(),
                                                                refreshPeriodInSeconds,
                                                                false);

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

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "M");

        // request master partition info to ensure it is cached.
        AddressInformation[] expectedAddresses = spyCache.tryGetAddresses(req,
                partitionKeyRangeIdentity,
                false)
                .block().v;

        assertThat(clientWrapper.capturedRequests).asList().hasSize(1);
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
                                                                false);

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

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(createdCollection.getResourceId(), "M");

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
                                       PartitionReplicasAddressesValidator validator, long timeout) {
        TestSubscriber<List<Address>> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(timeout, TimeUnit.MILLISECONDS);
        testSubscriber.assertNoErrors();
        testSubscriber.assertComplete();
        testSubscriber.assertValueCount(1);
        validator.validate(testSubscriber.values().get(0));
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
