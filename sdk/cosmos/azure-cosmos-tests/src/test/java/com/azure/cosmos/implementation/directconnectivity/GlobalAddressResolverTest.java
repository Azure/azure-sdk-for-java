// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;


import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosContainerProactiveInitConfigBuilder;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.CosmosContainerIdentity;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class GlobalAddressResolverTest {

    private HttpClient httpClient;
    private GlobalEndpointManager endpointManager;
    private IAuthorizationTokenProvider authorizationTokenProvider;
    private UserAgentContainer userAgentContainer;
    private RxCollectionCache collectionCache;
    private GatewayServiceConfigurationReader serviceConfigReader;
    private RxPartitionKeyRangeCache routingMapProvider;
    private ConnectionPolicy connectionPolicy;
    private URI urlforRead1;
    private URI urlforRead2;
    private URI urlforRead3;

    private URI urlforWrite1;
    private URI urlforWrite2;
    private URI urlforWrite3;

    @BeforeClass(groups = "unit")
    public void before_GlobalAddressResolverTest() throws Exception {
        urlforRead1 = new URI("http://testRead1.com/");
        urlforRead2 = new URI("http://testRead2.com/");
        urlforRead3 = new URI("http://testRead3.com/");
        urlforWrite1 = new URI("http://testWrite1.com/");
        urlforWrite2 = new URI("http://testWrite2.com/");
        urlforWrite3 = new URI("http://testWrite3.com/");

        connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setReadRequestsFallbackEnabled(true);
        httpClient = Mockito.mock(HttpClient.class);
        endpointManager = Mockito.mock(GlobalEndpointManager.class);

        List<URI> readEndPointList = new ArrayList<>();
        readEndPointList.add(urlforRead1);
        readEndPointList.add(urlforRead2);
        readEndPointList.add(urlforRead3);
        UnmodifiableList<URI> readList = new UnmodifiableList<>(readEndPointList);

        List<URI> writeEndPointList = new ArrayList<>();
        writeEndPointList.add(urlforWrite1);
        writeEndPointList.add(urlforWrite2);
        writeEndPointList.add(urlforWrite3);
        UnmodifiableList<URI> writeList = new UnmodifiableList<>(writeEndPointList);

        Mockito.when(endpointManager.getReadEndpoints()).thenReturn(readList);
        Mockito.when(endpointManager.getWriteEndpoints()).thenReturn(writeList);

        authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionCache = Mockito.mock(RxCollectionCache.class);
        Mockito.when(collectionCache.resolveCollectionAsync(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Mono.just(new Utils.ValueHolder<>(collectionDefinition)));
        routingMapProvider = Mockito.mock(RxPartitionKeyRangeCache.class);
        userAgentContainer = Mockito.mock(UserAgentContainer.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

    }

    @Test(groups = "unit")
    public void resolveAsync() throws Exception {

        GlobalAddressResolver globalAddressResolver = new GlobalAddressResolver(mockDiagnosticsClientContext(), httpClient, endpointManager, Protocol.HTTPS, authorizationTokenProvider, collectionCache, routingMapProvider,
                userAgentContainer,
                serviceConfigReader, connectionPolicy, null);
        RxDocumentServiceRequest request;
        request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                OperationType.Read,
                "dbs/db/colls/coll/docs/doc1",
                ResourceType.Document);

        Set<URI> urlsBeforeResolve = globalAddressResolver.addressCacheByEndpoint.keySet();
        assertThat(urlsBeforeResolve.size()).isEqualTo(5);
        assertThat(urlsBeforeResolve.contains(urlforRead3)).isFalse();//Last read will be removed from addressCacheByEndpoint after 5 endpoints
        assertThat(urlsBeforeResolve.contains(urlforRead2)).isTrue();

        URI testUrl = new URI("http://Test.com/");
        Mockito.when(endpointManager.resolveServiceEndpoint(ArgumentMatchers.any())).thenReturn(testUrl);
        globalAddressResolver.resolveAsync(request, true);
        Set<URI> urlsAfterResolve = globalAddressResolver.addressCacheByEndpoint.keySet();
        assertThat(urlsAfterResolve.size()).isEqualTo(5);
        assertThat(urlsAfterResolve.contains(urlforRead2)).isFalse();//Last read will be removed from addressCacheByEndpoint after 5 endpoints
        assertThat(urlsBeforeResolve.contains(testUrl)).isTrue();//New endpoint will be added in addressCacheByEndpoint
    }

    @Test(groups = "unit")
    public void submitOpenConnectionTasksAndInitCaches() {
        GlobalAddressResolver globalAddressResolver =
                new GlobalAddressResolver(
                        mockDiagnosticsClientContext(),
                        httpClient,
                        endpointManager,
                        Protocol.HTTPS,
                        authorizationTokenProvider,
                        collectionCache,
                        routingMapProvider,
                        userAgentContainer,
                        serviceConfigReader,
                        connectionPolicy,
                        null);
        GlobalAddressResolver.EndpointCache endpointCache = new GlobalAddressResolver.EndpointCache();
        GatewayAddressCache gatewayAddressCache = Mockito.mock(GatewayAddressCache.class);
        endpointCache.addressCache = gatewayAddressCache;
        globalAddressResolver.addressCacheByEndpoint.clear();
        globalAddressResolver.addressCacheByEndpoint.put(urlforRead1, endpointCache);
        globalAddressResolver.addressCacheByEndpoint.put(urlforRead2, endpointCache);

        AddressInformation addressInformation = new AddressInformation(true, true, "https://be1.west-us.com:8080", Protocol.TCP);

        Mockito
                .when(endpointManager.getReadEndpoints())
                .thenReturn(new UnmodifiableList<URI>(Arrays.asList(urlforRead1, urlforRead2)));

        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.setId("TestColl");
        documentCollection.setResourceId("IXYFAOHEBPM=");
        documentCollection.setSelfLink("dbs/testDb/colls/TestColl");

        PartitionKeyRange range = new PartitionKeyRange(
                "0",
                PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
                PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey);
        List<PartitionKeyRange> partitionKeyRanges = new ArrayList<>();
        partitionKeyRanges.add(range);

        Mockito
                .when(collectionCache.resolveByNameAsync(null, documentCollection.getSelfLink(), null))
                .thenReturn(Mono.just(documentCollection));

        Mockito
                .when(routingMapProvider.tryGetOverlappingRangesAsync(
                        null,
                        documentCollection.getResourceId(),
                        PartitionKeyInternalHelper.FullRange,
                        true,
                        null))
                .thenReturn(Mono.just(new Utils.ValueHolder<>(partitionKeyRanges)));

        // Set up GatewayAddressCache.openConnectionAndInitCaches behavior
        List<PartitionKeyRangeIdentity> ranges = new ArrayList<>();
        for (PartitionKeyRange partitionKeyRange : partitionKeyRanges) {
            ranges.add(new PartitionKeyRangeIdentity(documentCollection.getResourceId(), partitionKeyRange.getId()));
        }

        List<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>> collectionToAddresses = new ArrayList<>();

        collectionToAddresses.add(new ImmutablePair<>(new ImmutablePair<>("coll1", documentCollection), addressInformation));

        List<OpenConnectionResponse> openConnectionResponses = new ArrayList<>();
        OpenConnectionResponse response1 = new OpenConnectionResponse(new Uri("http://localhost:8081"), true,null, 1);
        OpenConnectionResponse response2 = new OpenConnectionResponse(new Uri("http://localhost:8082"), false, new IllegalStateException("Test"), 0);

        openConnectionResponses.add(response1);
        openConnectionResponses.add(response2);

        Mockito
                .when(gatewayAddressCache.resolveAddressesAndInitCaches(Mockito.anyString(), Mockito.any(DocumentCollection.class), Mockito.any()))
                        .thenReturn(Flux.fromIterable(collectionToAddresses));

        Mockito
                .when(gatewayAddressCache.submitOpenConnectionTask(addressInformation, documentCollection, Configs.getMinConnectionPoolSizePerEndpoint()))
                .thenReturn(Mono.empty());

        CosmosContainerProactiveInitConfig proactiveContainerInitConfig = new CosmosContainerProactiveInitConfigBuilder(Arrays.asList(new CosmosContainerIdentity("testDb", "TestColl")))
                .setProactiveConnectionRegionsCount(1)
                .build();

        StepVerifier.create(globalAddressResolver.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig))
                .expectComplete()
                .verify();

        Mockito
                .verify(collectionCache, Mockito.times(1))
                .resolveByNameAsync(null, documentCollection.getSelfLink(), null);
        Mockito
                .verify(routingMapProvider, Mockito.times(1))
                .tryGetOverlappingRangesAsync(
                        null,
                        documentCollection.getResourceId(),
                        PartitionKeyInternalHelper.FullRange,
                        true,
                        null);
        Mockito
                .verify(gatewayAddressCache, Mockito.times(1))
                .resolveAddressesAndInitCaches(Mockito.anyString(), Mockito.any(DocumentCollection.class), Mockito.any());

        Mockito
                .verify(gatewayAddressCache, Mockito.times(1))
                .submitOpenConnectionTask(addressInformation, documentCollection, Configs.getMinConnectionPoolSizePerEndpoint());
    }
}
