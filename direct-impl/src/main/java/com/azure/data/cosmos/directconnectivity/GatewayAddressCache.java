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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.PartitionKeyRange;
import com.azure.data.cosmos.internal.AuthorizationTokenType;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.Exceptions;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.PathsHelper;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.caches.AsyncCache;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GatewayAddressCache implements IAddressCache {
    private final static Logger logger = LoggerFactory.getLogger(GatewayAddressCache.class);
    private final static String protocolFilterFormat = "%s eq %s";
    private final static int DefaultBatchSize = 50;

    private final static int DefaultSuboptimalPartitionForceRefreshIntervalInSeconds = 600;
    private final ServiceConfig serviceConfig = ServiceConfig.getInstance();

    private final String databaseFeedEntryUrl = PathsHelper.generatePath(ResourceType.Database, "", true);
    private final URL serviceEndpoint;
    private final URL addressEndpoint;

    private final AsyncCache<PartitionKeyRangeIdentity, AddressInformation[]> serverPartitionAddressCache;
    private final ConcurrentHashMap<PartitionKeyRangeIdentity, Instant> suboptimalServerPartitionTimestamps;
    private final long suboptimalPartitionForceRefreshIntervalInSeconds;

    private final String protocolScheme;
    private final String protocolFilter;
    private final IAuthorizationTokenProvider tokenProvider;
    private final HashMap<String, String> defaultRequestHeaders;
    private final CompositeHttpClient<ByteBuf, ByteBuf> httpClient;

    private volatile Pair<PartitionKeyRangeIdentity, AddressInformation[]> masterPartitionAddressCache;
    private volatile Instant suboptimalMasterPartitionTimestamp;

    public GatewayAddressCache(
            URL serviceEndpoint,
            Protocol protocol,
            IAuthorizationTokenProvider tokenProvider,
            UserAgentContainer userAgent,
            CompositeHttpClient<ByteBuf, ByteBuf> httpClient,
            long suboptimalPartitionForceRefreshIntervalInSeconds) {
        try {
            this.addressEndpoint = new URL(serviceEndpoint, Paths.ADDRESS_PATH_SEGMENT);
        } catch (MalformedURLException e) {
            logger.error("serviceEndpoint {} is invalid", serviceEndpoint, e);
            assert false;
            throw new IllegalStateException(e);
        }
        this.tokenProvider = tokenProvider;
        this.serviceEndpoint = serviceEndpoint;
        this.serverPartitionAddressCache = new AsyncCache<>();
        this.suboptimalServerPartitionTimestamps = new ConcurrentHashMap<>();
        this.suboptimalMasterPartitionTimestamp = Instant.MAX;

        this.suboptimalPartitionForceRefreshIntervalInSeconds = suboptimalPartitionForceRefreshIntervalInSeconds;

        this.protocolScheme = protocol.scheme();
        this.protocolFilter = String.format(GatewayAddressCache.protocolFilterFormat,
                Constants.Properties.PROTOCOL,
                this.protocolScheme);

        this.httpClient = httpClient;

        if (userAgent == null) {
            userAgent = new UserAgentContainer();
        }

        defaultRequestHeaders = new HashMap<>();
        defaultRequestHeaders.put(HttpConstants.HttpHeaders.USER_AGENT, userAgent.getUserAgent());

        // Set requested API version header for version enforcement.
        defaultRequestHeaders.put(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);
    }

    public GatewayAddressCache(
            URL serviceEndpoint,
            Protocol protocol,
            IAuthorizationTokenProvider tokenProvider,
            UserAgentContainer userAgent,
            CompositeHttpClient<ByteBuf, ByteBuf> httpClient) {
        this(serviceEndpoint,
             protocol,
             tokenProvider,
             userAgent,
             httpClient,
             DefaultSuboptimalPartitionForceRefreshIntervalInSeconds);
    }

    private URL getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    @Override
    public Single<AddressInformation[]> tryGetAddresses(RxDocumentServiceRequest request,
                                                        PartitionKeyRangeIdentity partitionKeyRangeIdentity,
                                                        boolean forceRefreshPartitionAddresses) {

        com.azure.data.cosmos.internal.Utils.checkNotNullOrThrow(request, "request", "");
        com.azure.data.cosmos.internal.Utils.checkNotNullOrThrow(partitionKeyRangeIdentity, "partitionKeyRangeIdentity", "");

        if (StringUtils.equals(partitionKeyRangeIdentity.getPartitionKeyRangeId(),
                PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {

            // if that's master partition return master partition address!
            return this.resolveMasterAsync(request, forceRefreshPartitionAddresses, request.properties).map(r -> r.getRight());
        }

        Instant suboptimalServerPartitionTimestamp = this.suboptimalServerPartitionTimestamps.get(partitionKeyRangeIdentity);

        if (suboptimalServerPartitionTimestamp != null) {
            boolean forceRefreshDueToSuboptimalPartitionReplicaSet = Duration.between(suboptimalServerPartitionTimestamp, Instant.now()).getSeconds()
                    > this.suboptimalPartitionForceRefreshIntervalInSeconds;

            if (forceRefreshDueToSuboptimalPartitionReplicaSet) {
                // Compares the existing value for the specified key with a specified value,
                // and if they are equal, updates the key with a third value.
                Instant newValue = this.suboptimalServerPartitionTimestamps.computeIfPresent(partitionKeyRangeIdentity,
                        (key, oldVal) -> {
                            if (suboptimalServerPartitionTimestamp.equals(oldVal)) {
                                return Instant.MAX;
                            } else {
                                return oldVal;
                            }
                        });

                if (!newValue.equals(suboptimalServerPartitionTimestamp)) {
                    // the value was replaced;
                    forceRefreshPartitionAddresses = true;
                }
            }
        }

        final boolean forceRefreshPartitionAddressesModified = forceRefreshPartitionAddresses;

        if (forceRefreshPartitionAddressesModified) {
            this.serverPartitionAddressCache.refresh(
                    partitionKeyRangeIdentity,
                    () -> this.getAddressesForRangeId(
                            request,
                            partitionKeyRangeIdentity.getCollectionRid(),
                            partitionKeyRangeIdentity.getPartitionKeyRangeId(),
                            true));

            this.suboptimalServerPartitionTimestamps.remove(partitionKeyRangeIdentity);
        }

        Single<AddressInformation[]> addressesObs = this.serverPartitionAddressCache.getAsync(
                partitionKeyRangeIdentity,
                null,
                () -> this.getAddressesForRangeId(
                        request,
                        partitionKeyRangeIdentity.getCollectionRid(),
                        partitionKeyRangeIdentity.getPartitionKeyRangeId(),
                        false));

        return addressesObs.map(
                addresses -> {
                    if (notAllReplicasAvailable(addresses)) {
                        this.suboptimalServerPartitionTimestamps.putIfAbsent(partitionKeyRangeIdentity, Instant.now());
                    }

                    return addresses;
                }).onErrorResumeNext(ex -> {
            CosmosClientException dce = com.azure.data.cosmos.internal.Utils.as(ex, CosmosClientException.class);
            if (dce == null) {
                if (forceRefreshPartitionAddressesModified) {
                    this.suboptimalServerPartitionTimestamps.remove(partitionKeyRangeIdentity);
                }
                return Single.error(ex);
            } else {
                assert dce != null;
                if (Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND) ||
                        Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.GONE) ||
                        Exceptions.isSubStatusCode(dce, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE)) {
                    //remove from suboptimal cache in case the collection+pKeyRangeId combo is gone.
                    this.suboptimalServerPartitionTimestamps.remove(partitionKeyRangeIdentity);
                    return null;
                }
                return Single.error(ex);
            }

        });
    }

    Single<List<Address>> getServerAddressesViaGatewayAsync(
            RxDocumentServiceRequest request,
            String collectionRid,
            List<String> partitionKeyRangeIds,
            boolean forceRefresh) {
        String entryUrl = PathsHelper.generatePath(ResourceType.Document, collectionRid, true);
        HashMap<String, String> addressQuery = new HashMap<>();

        addressQuery.put(HttpConstants.QueryStrings.URL, HttpUtils.urlEncode(entryUrl));

        HashMap<String, String> headers = new HashMap<>(defaultRequestHeaders);
        if (forceRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_REFRESH, Boolean.TRUE.toString());
        }

        addressQuery.put(HttpConstants.QueryStrings.FILTER, HttpUtils.urlEncode(this.protocolFilter));

        addressQuery.put(HttpConstants.QueryStrings.PARTITION_KEY_RANGE_IDS, String.join(",", partitionKeyRangeIds));
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        String token = null;

        token = this.tokenProvider.getUserAuthorizationToken(
                collectionRid,
                ResourceType.Document,
                HttpConstants.HttpMethods.GET,
                headers,
                AuthorizationTokenType.PrimaryMasterKey,
                request.properties);

        if (token == null && request.getIsNameBased()) {
            // User doesn't have rid based resource token. Maybe user has name based.
            String collectionAltLink = PathsHelper.getCollectionPath(request.getResourceAddress());
            token = this.tokenProvider.getUserAuthorizationToken(
                    collectionAltLink,
                    ResourceType.Document,
                    HttpConstants.HttpMethods.GET,
                    headers,
                    AuthorizationTokenType.PrimaryMasterKey,
                    request.properties);
        }

        token = HttpUtils.urlEncode(token);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, token);
        URL targetEndpoint = Utils.setQuery(this.addressEndpoint.toString(), Utils.createQuery(addressQuery));
        String identifier = logAddressResolutionStart(request, targetEndpoint);
        HttpClientRequest<ByteBuf> httpGet = HttpClientRequest.createGet(targetEndpoint.toString());

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.withHeader(entry.getKey(), entry.getValue());
        }

        RxClient.ServerInfo serverInfo = new RxClient.ServerInfo(targetEndpoint.getHost(), targetEndpoint.getPort());
        Observable<HttpClientResponse<ByteBuf>> responseObs = this.httpClient.submit(serverInfo, httpGet);

        Single<RxDocumentServiceResponse> dsrObs = responseObs.toSingle().flatMap(rsp ->
                HttpClientUtils.parseResponseAsync(rsp));
        return dsrObs.map(
                dsr -> {
                    logAddressResolutionEnd(request, identifier);
                    List<Address> addresses = dsr.getQueryResponse(Address.class);
                    return addresses;
                });
    }

    public void dispose() {
        // TODO We will implement this in future once we will move to httpClient to CompositeHttpClient
        //https://msdata.visualstudio.com/CosmosDB/_workitems/edit/340842
    }

    private Single<Pair<PartitionKeyRangeIdentity, AddressInformation[]>> resolveMasterAsync(RxDocumentServiceRequest request, boolean forceRefresh, Map<String, Object> properties) {
        Pair<PartitionKeyRangeIdentity, AddressInformation[]> masterAddressAndRangeInitial = this.masterPartitionAddressCache;

        forceRefresh = forceRefresh ||
                (masterAddressAndRangeInitial != null &&
                        notAllReplicasAvailable(masterAddressAndRangeInitial.getRight()) &&
                        Duration.between(this.suboptimalMasterPartitionTimestamp, Instant.now()).getSeconds() > this.suboptimalPartitionForceRefreshIntervalInSeconds);

        if (forceRefresh || this.masterPartitionAddressCache == null) {
            Single<List<Address>> masterReplicaAddressesObs = this.getMasterAddressesViaGatewayAsync(
                    request,
                    ResourceType.Database,
                    null,
                    databaseFeedEntryUrl,
                    forceRefresh,
                    false,
                    properties);

            return masterReplicaAddressesObs.map(
                    masterAddresses -> {
                        Pair<PartitionKeyRangeIdentity, AddressInformation[]> masterAddressAndRangeRes =
                                this.toPartitionAddressAndRange("", masterAddresses);
                        this.masterPartitionAddressCache = masterAddressAndRangeRes;

                        if (notAllReplicasAvailable(masterAddressAndRangeRes.getRight())
                                && this.suboptimalMasterPartitionTimestamp.equals(Instant.MAX)) {
                            this.suboptimalMasterPartitionTimestamp = Instant.now();
                        } else {
                            this.suboptimalMasterPartitionTimestamp = Instant.MAX;
                        }

                        return masterPartitionAddressCache;
                    })
                    .doOnError(
                            e -> {
                                this.suboptimalMasterPartitionTimestamp = Instant.MAX;
                            });
        } else {
            if (notAllReplicasAvailable(masterAddressAndRangeInitial.getRight())
                    && this.suboptimalMasterPartitionTimestamp.equals(Instant.MAX)) {
                this.suboptimalMasterPartitionTimestamp = Instant.now();
            }

            return Single.just(masterAddressAndRangeInitial);
        }
    }

    private Single<AddressInformation[]> getAddressesForRangeId(
            RxDocumentServiceRequest request,
            String collectionRid,
            String partitionKeyRangeId,
            boolean forceRefresh) {
        Single<List<Address>> addressResponse = this.getServerAddressesViaGatewayAsync(request, collectionRid, Collections.singletonList(partitionKeyRangeId), forceRefresh);

        Single<List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>>> addressInfos =
                addressResponse.map(
                        addresses ->
                                addresses.stream().filter(addressInfo ->
                                        this.protocolScheme.equals(addressInfo.getProtocolScheme()))
                                        .collect(Collectors.groupingBy(
                                                address -> address.getParitionKeyRangeId()))
                                        .values().stream()
                                        .map(groupedAddresses -> toPartitionAddressAndRange(collectionRid, addresses))
                                        .collect(Collectors.toList()));

        Single<List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>>> result = addressInfos.map(addressInfo -> addressInfo.stream()
                .filter(a ->
                        StringUtils.equals(a.getLeft().getPartitionKeyRangeId(), partitionKeyRangeId))
                .collect(Collectors.toList()));

        return result.flatMap(
                list -> {
                    if (list.isEmpty()) {

                        String errorMessage = String.format(
                                RMResources.PartitionKeyRangeNotFound,
                                partitionKeyRangeId,
                                collectionRid);

                        PartitionKeyRangeGoneException e = new PartitionKeyRangeGoneException(errorMessage);
                        BridgeInternal.setResourceAddress(e, collectionRid);

                        return Single.error(e);
                    } else {
                        return Single.just(list.get(0).getRight());
                    }
                });
    }

    Single<List<Address>> getMasterAddressesViaGatewayAsync(
            RxDocumentServiceRequest request,
            ResourceType resourceType,
            String resourceAddress,
            String entryUrl,
            boolean forceRefresh,
            boolean useMasterCollectionResolver,
            Map<String, Object> properties) {
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(HttpConstants.QueryStrings.URL, HttpUtils.urlEncode(entryUrl));
        HashMap<String, String> headers = new HashMap<>(defaultRequestHeaders);

        if (forceRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_REFRESH, Boolean.TRUE.toString());
        }

        if (useMasterCollectionResolver) {
            headers.put(HttpConstants.HttpHeaders.USE_MASTER_COLLECTION_RESOLVER, Boolean.TRUE.toString());
        }

        queryParameters.put(HttpConstants.QueryStrings.FILTER, HttpUtils.urlEncode(this.protocolFilter));
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        String token = this.tokenProvider.getUserAuthorizationToken(
                resourceAddress,
                resourceType,
                HttpConstants.HttpMethods.GET,
                headers,
                AuthorizationTokenType.PrimaryMasterKey,
                properties);

        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, HttpUtils.urlEncode(token));
        URL targetEndpoint = Utils.setQuery(this.addressEndpoint.toString(), Utils.createQuery(queryParameters));
        String identifier = logAddressResolutionStart(request, targetEndpoint);
        HttpClientRequest<ByteBuf> httpGet = HttpClientRequest.createGet(targetEndpoint.toString());

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.withHeader(entry.getKey(), entry.getValue());
        }

        RxClient.ServerInfo serverInfo = new RxClient.ServerInfo(targetEndpoint.getHost(), targetEndpoint.getPort());
        Observable<HttpClientResponse<ByteBuf>> responseObs = this.httpClient.submit(serverInfo, httpGet);

        Single<RxDocumentServiceResponse> dsrObs = responseObs.toSingle().flatMap(rsp ->
                HttpClientUtils.parseResponseAsync(rsp));
        return dsrObs.map(
                dsr -> {
                    logAddressResolutionEnd(request, identifier);
                    List<Address> addresses = dsr.getQueryResponse(Address.class);
                    return addresses;
                });
    }

    private Pair<PartitionKeyRangeIdentity, AddressInformation[]> toPartitionAddressAndRange(String collectionRid, List<Address> addresses) {
        Address address = addresses.get(0);

        AddressInformation[] addressInfos =
                addresses.stream().map(addr ->
                                GatewayAddressCache.toAddressInformation(addr)
                                      ).collect(Collectors.toList()).toArray(new AddressInformation[addresses.size()]);
        return Pair.of(new PartitionKeyRangeIdentity(collectionRid, address.getParitionKeyRangeId()), addressInfos);
    }

    private static AddressInformation toAddressInformation(Address address) {
        return new AddressInformation(true, address.IsPrimary(), address.getPhyicalUri(), address.getProtocolScheme());
    }

    public Completable openAsync(
            DocumentCollection collection,
            List<PartitionKeyRangeIdentity> partitionKeyRangeIdentities) {
        List<Observable<List<Address>>> tasks = new ArrayList<>();
        int batchSize = GatewayAddressCache.DefaultBatchSize;

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Read,
                //    collection.AltLink,
                collection.resourceId(),
                ResourceType.DocumentCollection,
                //       AuthorizationTokenType.PrimaryMasterKey
                Collections.EMPTY_MAP);
        for (int i = 0; i < partitionKeyRangeIdentities.size(); i += batchSize) {

            int endIndex = i + batchSize;
            endIndex = endIndex < partitionKeyRangeIdentities.size()
                    ? endIndex : partitionKeyRangeIdentities.size();

            tasks.add(this.getServerAddressesViaGatewayAsync(
                    request,
                    collection.resourceId(),

                    partitionKeyRangeIdentities.subList(i, endIndex).
                            stream().map(range -> range.getPartitionKeyRangeId()).collect(Collectors.toList()),
                    false).toObservable());
        }

        return Observable.concat(tasks)
                .doOnNext(list -> {
                    List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>> addressInfos = list.stream()
                            .filter(addressInfo -> this.protocolScheme.equals(addressInfo.getProtocolScheme()))
                            .collect(Collectors.groupingBy(address -> address.getParitionKeyRangeId()))
                            .entrySet().stream().map(group -> toPartitionAddressAndRange(collection.resourceId(), group.getValue()))
                            .collect(Collectors.toList());

                    for (Pair<PartitionKeyRangeIdentity, AddressInformation[]> addressInfo : addressInfos) {
                        this.serverPartitionAddressCache.set(
                                new PartitionKeyRangeIdentity(collection.resourceId(), addressInfo.getLeft().getPartitionKeyRangeId()),
                                addressInfo.getRight());
                    }
                }).toCompletable();
    }

    private boolean notAllReplicasAvailable(AddressInformation[] addressInformations) {
        return addressInformations.length < this.serviceConfig.userReplicationPolicy.MaxReplicaSetSize;
    }

    private static String logAddressResolutionStart(RxDocumentServiceRequest request, URL targetEndpointUrl) {
        try {
            if (request.requestContext.clientSideRequestStatistics != null) {
                return request.requestContext.clientSideRequestStatistics.recordAddressResolutionStart(targetEndpointUrl.toURI());
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    private static void logAddressResolutionEnd(RxDocumentServiceRequest request, String identifier) {
        if (request.requestContext.clientSideRequestStatistics != null) {
            request.requestContext.clientSideRequestStatistics.recordAddressResolutionEnd(identifier);
        }
    }
}
