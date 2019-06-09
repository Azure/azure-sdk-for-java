/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.internal.directconnectivity;


import com.azure.data.cosmos.internal.GlobalEndpointManager;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import com.azure.data.cosmos.internal.caches.RxPartitionKeyRangeCache;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.directconnectivity.GatewayAddressCache;
import com.azure.data.cosmos.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.data.cosmos.directconnectivity.Protocol;
import com.azure.data.cosmos.internal.routing.CollectionRoutingMap;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternalHelper;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Completable;
import rx.Single;
import rx.functions.Action0;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalAddressResolverTest {

    private CompositeHttpClient<ByteBuf, ByteBuf> httpClient;
    private GlobalEndpointManager endpointManager;
    private IAuthorizationTokenProvider authorizationTokenProvider;
    private UserAgentContainer userAgentContainer;
    private RxCollectionCache collectionCache;
    private GatewayServiceConfigurationReader serviceConfigReader;
    private RxPartitionKeyRangeCache routingMapProvider;
    private ConnectionPolicy connectionPolicy;
    private URL urlforRead1;
    private URL urlforRead2;
    private URL urlforRead3;

    private URL urlforWrite1;
    private URL urlforWrite2;
    private URL urlforWrite3;

    @BeforeClass(groups = "unit")
    public void setup() throws Exception {
        urlforRead1 = new URL("http://testRead1.com/");
        urlforRead2 = new URL("http://testRead2.com/");
        urlforRead3 = new URL("http://testRead3.com/");
        urlforWrite1 = new URL("http://testWrite1.com/");
        urlforWrite2 = new URL("http://testWrite2.com/");
        urlforWrite3 = new URL("http://testWrite3.com/");

        connectionPolicy = new ConnectionPolicy();
        connectionPolicy.enableReadRequestsFallback(true);
        httpClient = Mockito.mock(CompositeHttpClient.class);
        endpointManager = Mockito.mock(GlobalEndpointManager.class);

        List<URL> readEndPointList = new ArrayList<>();
        readEndPointList.add(urlforRead1);
        readEndPointList.add(urlforRead2);
        readEndPointList.add(urlforRead3);
        UnmodifiableList readList = new UnmodifiableList(readEndPointList);

        List<URL> writeEndPointList = new ArrayList<>();
        writeEndPointList.add(urlforWrite1);
        writeEndPointList.add(urlforWrite2);
        writeEndPointList.add(urlforWrite3);
        UnmodifiableList writeList = new UnmodifiableList(writeEndPointList);

        Mockito.when(endpointManager.getReadEndpoints()).thenReturn(readList);
        Mockito.when(endpointManager.getWriteEndpoints()).thenReturn(writeList);

        authorizationTokenProvider = Mockito.mock(IAuthorizationTokenProvider.class);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.id(UUID.randomUUID().toString());
        collectionCache = Mockito.mock(RxCollectionCache.class);
        Mockito.when(collectionCache.resolveCollectionAsync(Matchers.any(RxDocumentServiceRequest.class))).thenReturn(Single.just(collectionDefinition));
        routingMapProvider = Mockito.mock(RxPartitionKeyRangeCache.class);
        userAgentContainer = Mockito.mock(UserAgentContainer.class);
        serviceConfigReader = Mockito.mock(GatewayServiceConfigurationReader.class);

    }

    @Test(groups = "unit")
    public void resolveAsync() throws Exception {

        GlobalAddressResolver globalAddressResolver = new GlobalAddressResolver(httpClient, endpointManager, Protocol.HTTPS, authorizationTokenProvider, collectionCache, routingMapProvider,
                userAgentContainer,
                serviceConfigReader, connectionPolicy);
        RxDocumentServiceRequest request;
        request = RxDocumentServiceRequest.createFromName(
                OperationType.Read,
                "dbs/db/colls/coll/docs/doc1",
                ResourceType.Document);

        Set<URL> urlsBeforeResolve = globalAddressResolver.addressCacheByEndpoint.keySet();
        assertThat(urlsBeforeResolve.size()).isEqualTo(5);
        assertThat(urlsBeforeResolve.contains(urlforRead3)).isFalse();//Last read will be removed from addressCacheByEndpoint after 5 endpoints
        assertThat(urlsBeforeResolve.contains(urlforRead2)).isTrue();

        URL testUrl = new URL("http://Test.com/");
        Mockito.when(endpointManager.resolveServiceEndpoint(Matchers.any(RxDocumentServiceRequest.class))).thenReturn(testUrl);
        globalAddressResolver.resolveAsync(request, true);
        Set<URL> urlsAfterResolve = globalAddressResolver.addressCacheByEndpoint.keySet();
        assertThat(urlsAfterResolve.size()).isEqualTo(5);
        assertThat(urlsAfterResolve.contains(urlforRead2)).isFalse();//Last read will be removed from addressCacheByEndpoint after 5 endpoints
        assertThat(urlsBeforeResolve.contains(testUrl)).isTrue();//New endpoint will be added in addressCacheByEndpoint
    }

    @Test(groups = "unit")
    public void openAsync() throws Exception {
        GlobalAddressResolver globalAddressResolver = new GlobalAddressResolver(httpClient, endpointManager, Protocol.HTTPS, authorizationTokenProvider, collectionCache, routingMapProvider,
                userAgentContainer,
                serviceConfigReader, connectionPolicy);
        Map<URL, GlobalAddressResolver.EndpointCache> addressCacheByEndpoint = Mockito.spy(globalAddressResolver.addressCacheByEndpoint);
        GlobalAddressResolver.EndpointCache endpointCache = new GlobalAddressResolver.EndpointCache();
        GatewayAddressCache gatewayAddressCache = Mockito.mock(GatewayAddressCache.class);
        AtomicInteger numberOfTaskCompleted = new AtomicInteger(0);
        endpointCache.addressCache = gatewayAddressCache;
        globalAddressResolver.addressCacheByEndpoint.clear();
        globalAddressResolver.addressCacheByEndpoint.put(urlforRead1, endpointCache);
        globalAddressResolver.addressCacheByEndpoint.put(urlforRead2, endpointCache);


        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.id("TestColl");
        documentCollection.resourceId("IXYFAOHEBPM=");
        CollectionRoutingMap collectionRoutingMap = Mockito.mock(CollectionRoutingMap.class);
        PartitionKeyRange range = new PartitionKeyRange("0", PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
                PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey);
        List<PartitionKeyRange> partitionKeyRanges = new ArrayList<>();
        partitionKeyRanges.add(range);
        Mockito.when(collectionRoutingMap.getOrderedPartitionKeyRanges()).thenReturn(partitionKeyRanges);
        Single<CollectionRoutingMap> collectionRoutingMapSingle = Single.just(collectionRoutingMap);
        Mockito.when(routingMapProvider.tryLookupAsync(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(collectionRoutingMapSingle);

        List<PartitionKeyRangeIdentity> ranges = new ArrayList<>();
        for (PartitionKeyRange partitionKeyRange : (List<PartitionKeyRange>) collectionRoutingMap.getOrderedPartitionKeyRanges()) {
            PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(documentCollection.resourceId(), partitionKeyRange.id());
            ranges.add(partitionKeyRangeIdentity);
        }
        Completable completable = Completable.fromAction(new Action0() {
            @Override
            public void call() {
                numberOfTaskCompleted.getAndIncrement();
            }
        });
        Mockito.when(gatewayAddressCache.openAsync(documentCollection, ranges)).thenReturn(completable);

        globalAddressResolver.openAsync(documentCollection).await();
        assertThat(numberOfTaskCompleted.get()).isEqualTo(2);
    }
}
