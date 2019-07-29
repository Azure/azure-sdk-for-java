// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.BridgeUtils;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.internal.DatabaseAccount;
import com.azure.data.cosmos.internal.DatabaseAccountLocation;
import com.azure.data.cosmos.internal.DatabaseAccountManagerInternal;
import com.azure.data.cosmos.internal.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.azure.data.cosmos.BridgeUtils.createDatabaseAccountLocation;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocationCache}
 */
public class LocationCacheTest {
    private final static URL DefaultEndpoint = createUrl("https://default.documents.azure.com");
    private final static URL Location1Endpoint = createUrl("https://location1.documents.azure.com");
    private final static URL Location2Endpoint = createUrl("https://location2.documents.azure.com");
    private final static URL Location3Endpoint = createUrl("https://location3.documents.azure.com");
    private final static URL Location4Endpoint = createUrl("https://location4.documents.azure.com");

    private static HashMap<String, URL> EndpointByLocation = new HashMap<>();

    static {
        EndpointByLocation.put("location1", LocationCacheTest.Location1Endpoint);
        EndpointByLocation.put("location2", LocationCacheTest.Location2Endpoint);
        EndpointByLocation.put("location3", LocationCacheTest.Location3Endpoint);
        EndpointByLocation.put("location4", LocationCacheTest.Location4Endpoint);
    }

    private final Configs configs = new Configs() {
        @Override
        public int getUnavailableLocationsExpirationTimeInSeconds() {
            return 3;
        }
    };

    private UnmodifiableList<String> preferredLocations;
    private DatabaseAccount databaseAccount;
    private LocationCache cache;
    private GlobalEndpointManager endpointManager;
    private DatabaseAccountManagerInternalMock mockedClient;

    @DataProvider(name = "paramsProvider")
    public Object[][] paramsProvider() {
        // provides all possible combinations for
        // useMultipleWriteEndpoints, endpointDiscoveryEnabled, isPreferredListEmpty
        List<Object[]> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            boolean useMultipleWriteEndpoints = (i & 1) > 0;
            boolean endpointDiscoveryEnabled = (i & 2) > 0;
            boolean isPreferredListEmpty = (i & 4) > 0;
            list.add(new Object[]{useMultipleWriteEndpoints, endpointDiscoveryEnabled, isPreferredListEmpty});
        }

        return list.toArray(new Object[][]{});
    }

    @Test(groups = "long", dataProvider = "paramsProvider")
    public void validateAsync(boolean useMultipleWriteEndpoints,
                              boolean endpointDiscoveryEnabled,
                              boolean isPreferredListEmpty) throws Exception {
        validateLocationCacheAsync(useMultipleWriteEndpoints,
                endpointDiscoveryEnabled,
                isPreferredListEmpty);
    }

    @Test(groups = "long")
    public void validateWriteEndpointOrderWithClientSideDisableMultipleWriteLocation()  throws Exception {
        this.initialize(false, true, false);
        assertThat(this.cache.getWriteEndpoints().get(0)).isEqualTo(LocationCacheTest.Location1Endpoint);
        assertThat(this.cache.getWriteEndpoints().get(1)).isEqualTo(LocationCacheTest.Location2Endpoint);
        assertThat(this.cache.getWriteEndpoints().get(2)).isEqualTo(LocationCacheTest.Location3Endpoint);
    }

    private static DatabaseAccount createDatabaseAccount(boolean useMultipleWriteLocations) {
        DatabaseAccount databaseAccount = BridgeUtils.createDatabaseAccount(
                // read endpoints
                ImmutableList.of(
                        createDatabaseAccountLocation("location1", LocationCacheTest.Location1Endpoint.toString()),
                        createDatabaseAccountLocation("location2", LocationCacheTest.Location2Endpoint.toString()),
                        createDatabaseAccountLocation("location4", LocationCacheTest.Location4Endpoint.toString())),

                // write endpoints
                ImmutableList.of(
                        createDatabaseAccountLocation("location1", LocationCacheTest.Location1Endpoint.toString()),
                        createDatabaseAccountLocation("location2", LocationCacheTest.Location2Endpoint.toString()),
                        createDatabaseAccountLocation("location3", LocationCacheTest.Location3Endpoint.toString())),
                // if the account supports multi master multi muster
                useMultipleWriteLocations);

        return databaseAccount;
    }

    private void initialize(
            boolean useMultipleWriteLocations,
            boolean enableEndpointDiscovery,
            boolean isPreferredLocationsListEmpty) throws Exception {

        this.mockedClient = new DatabaseAccountManagerInternalMock();
        this.databaseAccount = LocationCacheTest.createDatabaseAccount(useMultipleWriteLocations);

        this.preferredLocations = isPreferredLocationsListEmpty ?
                new UnmodifiableList<>(Collections.emptyList()) :
                new UnmodifiableList<>(ImmutableList.of("location1", "location2", "location3"));

        this.cache = new LocationCache(
                this.preferredLocations,
                LocationCacheTest.DefaultEndpoint,
                enableEndpointDiscovery,
                useMultipleWriteLocations,
                configs);

        this.cache.onDatabaseAccountRead(this.databaseAccount);

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.enableEndpointDiscovery(enableEndpointDiscovery);
        BridgeInternal.setUseMultipleWriteLocations(connectionPolicy, useMultipleWriteLocations);
        connectionPolicy.preferredLocations(this.preferredLocations);

        this.endpointManager = new GlobalEndpointManager(mockedClient, connectionPolicy, configs);
    }

    class DatabaseAccountManagerInternalMock implements DatabaseAccountManagerInternal {
        private final AtomicInteger counter = new AtomicInteger(0);

        private void reset() {
            counter.set(0);
        }

        private int getInvocationCounter() {
            return counter.get();
        }

        @Override
        public Flux<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
            return Flux.just(LocationCacheTest.this.databaseAccount);
        }

        @Override
        public ConnectionPolicy getConnectionPolicy() {
            throw new RuntimeException("not supported");
        }

        @Override
        public URI getServiceEndpoint() {
            try {
                return LocationCacheTest.DefaultEndpoint.toURI();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }

    private static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    private void validateLocationCacheAsync(
            boolean useMultipleWriteLocations,
            boolean endpointDiscoveryEnabled,
            boolean isPreferredListEmpty) throws Exception {
        for (int writeLocationIndex = 0; writeLocationIndex < 3; writeLocationIndex++) {
            for (int readLocationIndex = 0; readLocationIndex < 2; readLocationIndex++) {
                this.initialize(
                        useMultipleWriteLocations,
                        endpointDiscoveryEnabled,
                        isPreferredListEmpty);

                UnmodifiableList<URL> currentWriteEndpoints = this.cache.getWriteEndpoints();
                UnmodifiableList<URL> currentReadEndpoints = this.cache.getReadEndpoints();
                for (int i = 0; i < readLocationIndex; i++) {
                    this.cache.markEndpointUnavailableForRead(createUrl(Iterables.get(this.databaseAccount.getReadableLocations(), i).getEndpoint()));
                    this.endpointManager.markEndpointUnavailableForRead(createUrl(Iterables.get(this.databaseAccount.getReadableLocations(), i).getEndpoint()));;
                }
                for (int i = 0; i < writeLocationIndex; i++) {
                    this.cache.markEndpointUnavailableForWrite(createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), i).getEndpoint()));
                    this.endpointManager.markEndpointUnavailableForWrite(createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), i).getEndpoint()));
                }

                Map<String, URL> writeEndpointByLocation = toStream(this.databaseAccount.getWritableLocations())
                        .collect(Collectors.toMap(i -> i.getName(), i -> createUrl(i.getEndpoint())));

                Map<String, URL> readEndpointByLocation = toStream(this.databaseAccount.getReadableLocations())
                        .collect(Collectors.toMap(i -> i.getName(), i -> createUrl(i.getEndpoint())));

                URL[] preferredAvailableWriteEndpoints = toStream(this.preferredLocations).skip(writeLocationIndex)
                        .filter(location -> writeEndpointByLocation.containsKey(location))
                        .map(location -> writeEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URL[0]);

                URL[] preferredAvailableReadEndpoints = toStream(this.preferredLocations).skip(readLocationIndex)
                        .filter(location -> readEndpointByLocation.containsKey(location))
                        .map(location -> readEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URL[0]);

                this.validateEndpointRefresh(
                        useMultipleWriteLocations,
                        endpointDiscoveryEnabled,
                        preferredAvailableWriteEndpoints,
                        preferredAvailableReadEndpoints,
                        writeLocationIndex > 0);

                this.validateGlobalEndpointLocationCacheRefreshAsync();

                this.validateRequestEndpointResolution(
                        useMultipleWriteLocations,
                        endpointDiscoveryEnabled,
                        preferredAvailableWriteEndpoints,
                        preferredAvailableReadEndpoints);

                // wait for TTL on unavailability info

                TimeUnit.SECONDS.sleep(configs.getUnavailableLocationsExpirationTimeInSeconds() + 1);

                assertThat(currentWriteEndpoints.toArray()).containsExactly(this.cache.getWriteEndpoints().toArray());
                assertThat(currentReadEndpoints.toArray()).containsExactly(this.cache.getReadEndpoints().toArray());
            }
        }
    }

    private void validateEndpointRefresh(
            boolean useMultipleWriteLocations,
            boolean endpointDiscoveryEnabled,
            URL[] preferredAvailableWriteEndpoints,
            URL[] preferredAvailableReadEndpoints,
            boolean isFirstWriteEndpointUnavailable) {

        Utils.ValueHolder<Boolean> canRefreshInBackgroundHolder = new Utils.ValueHolder<>();
        canRefreshInBackgroundHolder.v = false;

        boolean shouldRefreshEndpoints = this.cache.shouldRefreshEndpoints(canRefreshInBackgroundHolder);

        boolean isMostPreferredLocationUnavailableForRead = false;
        boolean isMostPreferredLocationUnavailableForWrite = useMultipleWriteLocations ?
                false : isFirstWriteEndpointUnavailable;
        if (this.preferredLocations.size() > 0) {
            String mostPreferredReadLocationName = this.preferredLocations.stream()
                    .filter(location -> toStream(databaseAccount.getReadableLocations())
                            .anyMatch(readLocation -> readLocation.getName().equals(location)))
                    .findFirst().orElse(null);

            URL mostPreferredReadEndpoint = LocationCacheTest.EndpointByLocation.get(mostPreferredReadLocationName);
            isMostPreferredLocationUnavailableForRead = preferredAvailableReadEndpoints.length == 0 ?
                    true : (!areEqual(preferredAvailableReadEndpoints[0], mostPreferredReadEndpoint));

            String mostPreferredWriteLocationName = this.preferredLocations.stream()
                    .filter(location -> toStream(databaseAccount.getWritableLocations())
                            .anyMatch(writeLocation -> writeLocation.getName().equals(location)))
                    .findFirst().orElse(null);

            URL mostPreferredWriteEndpoint = LocationCacheTest.EndpointByLocation.get(mostPreferredWriteLocationName);

            if (useMultipleWriteLocations) {
                isMostPreferredLocationUnavailableForWrite = preferredAvailableWriteEndpoints.length == 0 ?
                        true : (!areEqual(preferredAvailableWriteEndpoints[0], mostPreferredWriteEndpoint));
            }
        }

        if (!endpointDiscoveryEnabled) {
            assertThat(shouldRefreshEndpoints).isFalse();
        } else {
            assertThat(shouldRefreshEndpoints).isEqualTo(
                    isMostPreferredLocationUnavailableForRead || isMostPreferredLocationUnavailableForWrite);
        }

        if (shouldRefreshEndpoints) {
            assertThat(canRefreshInBackgroundHolder.v).isTrue();
        }
    }

    private boolean areEqual(URL url1, URL url2) {
        return url1.equals(url2);
    }

    private void validateGlobalEndpointLocationCacheRefreshAsync() throws Exception {

        mockedClient.reset();
        List<Mono<Void>> list = IntStream.range(0, 10)
                .mapToObj(index -> this.endpointManager.refreshLocationAsync(null))
                .collect(Collectors.toList());

        Flux.merge(list).then().block();

        assertThat(mockedClient.getInvocationCounter()).isLessThanOrEqualTo(1);
        mockedClient.reset();

        IntStream.range(0, 10)
                .mapToObj(index -> this.endpointManager.refreshLocationAsync(null))
                .collect(Collectors.toList());
        for (Mono completable : list) {
            completable.block();
        }

        assertThat(mockedClient.getInvocationCounter()).isLessThanOrEqualTo(1);
    }

    private void validateRequestEndpointResolution(
            boolean useMultipleWriteLocations,
            boolean endpointDiscoveryEnabled,
            URL[] availableWriteEndpoints,
            URL[] availableReadEndpoints) throws MalformedURLException {
        URL firstAvailableWriteEndpoint;
        URL secondAvailableWriteEndpoint;

        if (!endpointDiscoveryEnabled) {
            firstAvailableWriteEndpoint = LocationCacheTest.DefaultEndpoint;
            secondAvailableWriteEndpoint = LocationCacheTest.DefaultEndpoint;
        } else if (!useMultipleWriteLocations) {
            firstAvailableWriteEndpoint = createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), 0).getEndpoint());
            secondAvailableWriteEndpoint = createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), 1).getEndpoint());
        } else if (availableWriteEndpoints.length > 1) {
            firstAvailableWriteEndpoint = availableWriteEndpoints[0];
            secondAvailableWriteEndpoint = availableWriteEndpoints[1];
        } else if (availableWriteEndpoints.length > 0) {
            firstAvailableWriteEndpoint = availableWriteEndpoints[0];
            Iterator<DatabaseAccountLocation> writeLocationsIterator = databaseAccount.getWritableLocations().iterator();        
            String writeEndpoint = writeLocationsIterator.next().getEndpoint();
            secondAvailableWriteEndpoint = writeEndpoint != firstAvailableWriteEndpoint.toString()
                    ? new URL(writeEndpoint)
                    : new URL(writeLocationsIterator.next().getEndpoint());
        } else {
            firstAvailableWriteEndpoint = LocationCacheTest.DefaultEndpoint;
            secondAvailableWriteEndpoint = LocationCacheTest.DefaultEndpoint;
        }

        URL firstAvailableReadEndpoint;

        if (!endpointDiscoveryEnabled) {
            firstAvailableReadEndpoint = LocationCacheTest.DefaultEndpoint;
        } else if (this.preferredLocations.size() == 0) {
            firstAvailableReadEndpoint = firstAvailableWriteEndpoint;
        } else if (availableReadEndpoints.length > 0) {
            firstAvailableReadEndpoint = availableReadEndpoints[0];
        } else {
            firstAvailableReadEndpoint = LocationCacheTest.EndpointByLocation.get(this.preferredLocations.get(0));
        }

        URL firstWriteEnpoint = !endpointDiscoveryEnabled ?
                LocationCacheTest.DefaultEndpoint :
                    createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), 0).getEndpoint());

        URL secondWriteEnpoint = !endpointDiscoveryEnabled ?
                LocationCacheTest.DefaultEndpoint :
                    createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), 1).getEndpoint());

        // If current write endpoint is unavailable, write endpoints order doesn't change
        // ALL write requests flip-flop between current write and alternate write endpoint
        UnmodifiableList<URL> writeEndpoints = this.cache.getWriteEndpoints();

        assertThat(firstAvailableWriteEndpoint).isEqualTo(writeEndpoints.get(0));
        assertThat(secondAvailableWriteEndpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Document, true));
        assertThat(firstAvailableWriteEndpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Document, false));

        // Writes to other resource types should be directed to first/second write endpoint
        assertThat(firstWriteEnpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Database, false));
        assertThat(secondWriteEnpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Database, true));

        // Reads should be directed to available read endpoints regardless of resource type
        assertThat(firstAvailableReadEndpoint).isEqualTo(this.resolveEndpointForReadRequest(true));
        assertThat(firstAvailableReadEndpoint).isEqualTo(this.resolveEndpointForReadRequest(false));
    }

    private URL resolveEndpointForReadRequest(boolean masterResourceType) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                masterResourceType ? ResourceType.Database : ResourceType.Document);
        return this.cache.resolveServiceEndpoint(request);
    }

    private URL resolveEndpointForWriteRequest(ResourceType resourceType, boolean useAlternateWriteEndpoint) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Create, resourceType);
        request.requestContext.RouteToLocation(useAlternateWriteEndpoint ? 1 : 0, resourceType.isCollectionChild());
        return this.cache.resolveServiceEndpoint(request);
    }

    private RxDocumentServiceRequest CreateRequest(boolean isReadRequest, boolean isMasterResourceType)
    {
        if (isReadRequest) {
            return RxDocumentServiceRequest.create(OperationType.Read, isMasterResourceType ? ResourceType.Database : ResourceType.Document);
        } else {
            return RxDocumentServiceRequest.create(OperationType.Create, isMasterResourceType ? ResourceType.Database : ResourceType.Document);
        }
    }
    private static URL createUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
