// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.ModelBridgeUtils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.Iterables;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
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
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocationCache}
 */
public class LocationCacheTest {
    private final static URI DefaultEndpoint = createUrl("https://default.documents.azure.com");
    private final static URI Location1Endpoint = createUrl("https://location1.documents.azure.com");
    private final static URI Location2Endpoint = createUrl("https://location2.documents.azure.com");
    private final static URI Location3Endpoint = createUrl("https://location3.documents.azure.com");
    private final static URI Location4Endpoint = createUrl("https://location4.documents.azure.com");

    private static HashMap<String, URI> EndpointByLocation = new HashMap<>();

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

    @AfterClass()
    public void afterClass() {
        LifeCycleUtils.closeQuietly(this.endpointManager);
    }

    private static DatabaseAccount createDatabaseAccount(boolean useMultipleWriteLocations) {
        DatabaseAccount databaseAccount = ModelBridgeUtils.createDatabaseAccount(
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

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(enableEndpointDiscovery);
        connectionPolicy.setMultipleWriteRegionsEnabled(useMultipleWriteLocations);
        connectionPolicy.setPreferredRegions(this.preferredLocations);

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
                return LocationCacheTest.DefaultEndpoint;
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

                UnmodifiableList<URI> currentWriteEndpoints = this.cache.getWriteEndpoints();
                UnmodifiableList<URI> currentReadEndpoints = this.cache.getReadEndpoints();
                for (int i = 0; i < readLocationIndex; i++) {
                    this.cache.markEndpointUnavailableForRead(createUrl(Iterables.get(this.databaseAccount.getReadableLocations(), i).getEndpoint()));
                    this.endpointManager.markEndpointUnavailableForRead(createUrl(Iterables.get(this.databaseAccount.getReadableLocations(), i).getEndpoint()));;
                }
                for (int i = 0; i < writeLocationIndex; i++) {
                    this.cache.markEndpointUnavailableForWrite(createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), i).getEndpoint()));
                    this.endpointManager.markEndpointUnavailableForWrite(createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), i).getEndpoint()));
                }

                Map<String, URI> writeEndpointByLocation = toStream(this.databaseAccount.getWritableLocations())
                        .collect(Collectors.toMap(i -> i.getName(), i -> createUrl(i.getEndpoint())));

                Map<String, URI> readEndpointByLocation = toStream(this.databaseAccount.getReadableLocations())
                        .collect(Collectors.toMap(i -> i.getName(), i -> createUrl(i.getEndpoint())));

                URI[] preferredAvailableWriteEndpoints = toStream(this.preferredLocations).skip(writeLocationIndex)
                        .filter(location -> writeEndpointByLocation.containsKey(location))
                        .map(location -> writeEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URI[0]);

                URI[] preferredAvailableReadEndpoints = toStream(this.preferredLocations).skip(readLocationIndex)
                        .filter(location -> readEndpointByLocation.containsKey(location))
                        .map(location -> readEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URI[0]);

                this.validateEndpointRefresh(
                        useMultipleWriteLocations,
                        endpointDiscoveryEnabled,
                        preferredAvailableWriteEndpoints,
                        preferredAvailableReadEndpoints,
                        readLocationIndex > 0 && !currentReadEndpoints.get(0).equals(DefaultEndpoint),
                        writeLocationIndex > 0,
                        currentReadEndpoints.size() > 1,
                        currentWriteEndpoints.size() > 1
                    );

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
            URI[] preferredAvailableWriteEndpoints,
            URI[] preferredAvailableReadEndpoints,
            boolean isFirstReadEndpointUnavailable,
            boolean isFirstWriteEndpointUnavailable,
            boolean hasMoreThanOneReadEndpoints,
            boolean hasMoreThanOneWriteEndpoints) {

        Utils.ValueHolder<Boolean> canRefreshInBackgroundHolder = new Utils.ValueHolder<>();
        canRefreshInBackgroundHolder.v = false;

        boolean shouldRefreshEndpoints = this.cache.shouldRefreshEndpoints(canRefreshInBackgroundHolder);

        boolean isMostPreferredLocationUnavailableForRead = isFirstReadEndpointUnavailable;
        boolean isMostPreferredLocationUnavailableForWrite = useMultipleWriteLocations ?
                false : isFirstWriteEndpointUnavailable;
        if (this.preferredLocations.size() > 0) {
            String mostPreferredReadLocationName = this.preferredLocations.stream()
                    .filter(location -> toStream(databaseAccount.getReadableLocations())
                            .anyMatch(readLocation -> readLocation.getName().equals(location)))
                    .findFirst().orElse(null);

            URI mostPreferredReadEndpoint = LocationCacheTest.EndpointByLocation.get(mostPreferredReadLocationName);
            isMostPreferredLocationUnavailableForRead = preferredAvailableReadEndpoints.length == 0 ?
                    true : (!areEqual(preferredAvailableReadEndpoints[0], mostPreferredReadEndpoint));

            String mostPreferredWriteLocationName = this.preferredLocations.stream()
                    .filter(location -> toStream(databaseAccount.getWritableLocations())
                            .anyMatch(writeLocation -> writeLocation.getName().equals(location)))
                    .findFirst().orElse(null);

            URI mostPreferredWriteEndpoint = LocationCacheTest.EndpointByLocation.get(mostPreferredWriteLocationName);

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
            if (isMostPreferredLocationUnavailableForRead) {
                assertThat(canRefreshInBackgroundHolder.v).isEqualTo(hasMoreThanOneReadEndpoints);
            } else if (isMostPreferredLocationUnavailableForWrite) {
                assertThat(canRefreshInBackgroundHolder.v).isEqualTo(hasMoreThanOneWriteEndpoints);
            }
        }
    }

    private boolean areEqual(URI url1, URI url2) {
        return url1.equals(url2);
    }

    private void validateGlobalEndpointLocationCacheRefreshAsync() throws Exception {

        mockedClient.reset();
        List<Mono<Void>> list = IntStream.range(0, 10)
                .mapToObj(index -> this.endpointManager.refreshLocationAsync(null, false))
                .collect(Collectors.toList());

        Flux.merge(list).then().block();

        assertThat(mockedClient.getInvocationCounter()).isLessThanOrEqualTo(1);
        mockedClient.reset();

        IntStream.range(0, 10)
                .mapToObj(index -> this.endpointManager.refreshLocationAsync(null, false))
                .collect(Collectors.toList());
        for (Mono<Void> completable : list) {
            completable.block();
        }

        assertThat(mockedClient.getInvocationCounter()).isLessThanOrEqualTo(1);
    }

    private void validateRequestEndpointResolution(
            boolean useMultipleWriteLocations,
            boolean endpointDiscoveryEnabled,
            URI[] availableWriteEndpoints,
            URI[] availableReadEndpoints) throws Exception {
        URI firstAvailableWriteEndpoint;
        URI secondAvailableWriteEndpoint;

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
                    ? new URI(writeEndpoint)
                    : new URI(writeLocationsIterator.next().getEndpoint());
        } else {
            firstAvailableWriteEndpoint = LocationCacheTest.DefaultEndpoint;
            secondAvailableWriteEndpoint = LocationCacheTest.DefaultEndpoint;
        }

        URI firstAvailableReadEndpoint;

        if (!endpointDiscoveryEnabled) {
            firstAvailableReadEndpoint = LocationCacheTest.DefaultEndpoint;
        } else if (this.preferredLocations.size() == 0) {
            firstAvailableReadEndpoint = firstAvailableWriteEndpoint;
        } else if (availableReadEndpoints.length > 0) {
            firstAvailableReadEndpoint = availableReadEndpoints[0];
        } else {
            firstAvailableReadEndpoint = LocationCacheTest.EndpointByLocation.get(this.preferredLocations.get(0));
        }

        URI firstWriteEnpoint = !endpointDiscoveryEnabled ?
                LocationCacheTest.DefaultEndpoint :
                    createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), 0).getEndpoint());

        URI secondWriteEnpoint = !endpointDiscoveryEnabled ?
                LocationCacheTest.DefaultEndpoint :
                    createUrl(Iterables.get(this.databaseAccount.getWritableLocations(), 1).getEndpoint());

        // If current write endpoint is unavailable, write endpoints order doesn't change
        // ALL write requests flip-flop between current write and alternate write endpoint
        UnmodifiableList<URI> writeEndpoints = this.cache.getWriteEndpoints();

        assertThat(firstAvailableWriteEndpoint).isEqualTo(writeEndpoints.get(0));
        assertThat(secondAvailableWriteEndpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Document, true));
        assertThat(firstAvailableWriteEndpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Document, false));

        // Writes to other resource types should be directed to first/second write getEndpoint
        assertThat(firstWriteEnpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Database, false));
        assertThat(secondWriteEnpoint).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Database, true));

        // Reads should be directed to available read endpoints regardless of resource type
        assertThat(firstAvailableReadEndpoint).isEqualTo(this.resolveEndpointForReadRequest(true));
        assertThat(firstAvailableReadEndpoint).isEqualTo(this.resolveEndpointForReadRequest(false));
    }

    private URI resolveEndpointForReadRequest(boolean masterResourceType) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read,
                masterResourceType ? ResourceType.Database : ResourceType.Document);
        return this.cache.resolveServiceEndpoint(request);
    }

    private URI resolveEndpointForWriteRequest(ResourceType resourceType, boolean useAlternateWriteEndpoint) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, resourceType);
        request.requestContext.routeToLocation(useAlternateWriteEndpoint ? 1 : 0, resourceType.isCollectionChild());
        return this.cache.resolveServiceEndpoint(request);
    }

    private RxDocumentServiceRequest CreateRequest(boolean isReadRequest, boolean isMasterResourceType)
    {
        if (isReadRequest) {
            return RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read, isMasterResourceType ? ResourceType.Database : ResourceType.Document);
        } else {
            return RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, isMasterResourceType ? ResourceType.Database : ResourceType.Document);
        }
    }
    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static DatabaseAccountLocation createDatabaseAccountLocation(String name, String endpoint) {
        DatabaseAccountLocation dal = new DatabaseAccountLocation();
        dal.setName(name);
        dal.setEndpoint(endpoint);

        return dal;
    }
}
