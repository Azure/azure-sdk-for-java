// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.CosmosExcludedRegions;
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
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link LocationCache}
 */
public class LocationCacheTest {
    private final static URI DefaultEndpoint = createUrl("https://default.documents.azure.com");
    private final static URI DefaultRegionalEndpoint = createUrl("https://location1.documents.azure.com");
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

    // The purpose of validateExcludedRegions is the following:
    //  1. Test with various combinations of excludedRegions such as:
    //      a) list with excludedRegions which is a sub-list of preferredRegions
    //      b) list with excludedRegions which is a sub-list of preferredRegions and has duplicates
    //      c) list with excludedRegions which is not a sub-list of preferredRegions and has no duplicates
    //      d) list with excludedRegions which is not a sub-list of preferredRegions and has duplicates
    //      e) list which is null
    //      f) list which is empty
    //      g) whether exclude region works well when preferred region list is empty - in such a case, region exclusion
    //         should apply to account-level regions
    //  2. todo: the dataProvider hard codes the list of available read and available write regions - this can be avoided
    //        a) according to the hardcoding - available read regions: location1, location2; available write regions: location1, location2, location3
    @DataProvider(name = "excludedRegionsTestConfigs")
    public Object[][] excludedRegionsTestConfigs() {
        // Parameters passed:
        //      1. List of regions to exclude on the client / ConnectionPolicy
        //      2. List of regions to exclude on the request
        //      3. The request itself
        //      4. Expected applicable regions for request
        //      6. Is preferred region list empty
        return new Object[][] {
            {
                new HashSet<>(Arrays.asList("location1", "location2")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location1", "location2")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint, Location4Endpoint)),
                true
            },
            {
                new HashSet<>(Arrays.asList("location1", "location2", "location7")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location1", "location2", "location7")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint, Location4Endpoint)),
                true
            },
            {
                null,
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false
            },
            {
                null,
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint, Location4Endpoint)),
                true
            },
            {
                new HashSet<>(),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false
            },
            {
                new HashSet<>(),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint, Location4Endpoint)),
                true
            },
            {
                new HashSet<>(Arrays.asList("location5, location10, location10")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location5, location10, location10")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint, Location4Endpoint)),
                true
            },
            {
                new HashSet<>(Arrays.asList("location1, location2, location10")),
                Arrays.asList("location2"),
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location1, location2, location10")),
                Arrays.asList("location2"),
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location3Endpoint, Location4Endpoint)),
                true
            },
            {
                new HashSet<>(Arrays.asList("location1", "location2")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location1", "location2")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint)),
                true
            },
            {
                new HashSet<>(Arrays.asList("location1", "location2", "location7")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location1", "location2", "location7")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location3Endpoint)),
                true
            },
            {
                null,
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false
            },
            {
                null,
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                true
            },
            {
                new HashSet<>(),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false
            },
            {
                new HashSet<>(),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                true
            },
            {
                new HashSet<>(Arrays.asList("location5, location10, location10")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location5, location10, location10")),
                null,
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                true
            },
            {
                new HashSet<>(Arrays.asList("location1, location2, location10")),
                Arrays.asList("location2"),
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location3Endpoint)),
                false
            },
            {
                new HashSet<>(Arrays.asList("location1, location2, location10")),
                Arrays.asList("location2"),
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location3Endpoint)),
                true
            }
        };
    }

    @DataProvider(name = "effectivePreferredRegionTestConfigs")
    public Object[][] effectivePreferredRegionTestConfigs() {
        // Parameters passed:
        //      1. The request itself
        //      2. Expected applicable read regions
        //      3. Expected applicable write regions
        //      4. Is preferred region list empty
        //      5. Is default endpoint also a regional endpoint
        return new Object[][] {
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint, Location4Endpoint)),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                true,
                false
            },
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint, Location4Endpoint)),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                true,
                false
            },
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(DefaultRegionalEndpoint)),
                new HashSet<>(Arrays.asList(DefaultRegionalEndpoint)),
                true,
                true
            },
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(DefaultRegionalEndpoint)),
                new HashSet<>(Arrays.asList(DefaultRegionalEndpoint)),
                true,
                true
            },
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false,
                false
            },
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false,
                false
            },
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Read,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false,
                true
            },
            {
                RxDocumentServiceRequest.create(
                    mockDiagnosticsClientContext(),
                    OperationType.Create,
                    ResourceType.Document),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                new HashSet<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint)),
                false,
                true
            }
        };
    }

    @Test(groups = "long", dataProvider = "paramsProvider")
    public void validateAsync(boolean useMultipleWriteEndpoints,
                              boolean endpointDiscoveryEnabled,
                              boolean isPreferredListEmpty) throws Exception {

        List<URI> accountLevelWriteEndpoints
            = new ArrayList<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint));
        List<URI> accountLevelReadEndpoints
            = new ArrayList<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint, Location4Endpoint));
        List<URI> preferredWriteRegionsIfPreferredLocationsIsntEmpty
            = new ArrayList<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint));
        List<URI> preferredReadRegionsIfPreferredLocationsIsntEmpty
            = new ArrayList<>(Arrays.asList(Location1Endpoint, Location2Endpoint, Location3Endpoint));

        validateLocationCacheAsync(useMultipleWriteEndpoints,
            endpointDiscoveryEnabled,
            isPreferredListEmpty,
            preferredReadRegionsIfPreferredLocationsIsntEmpty,
            preferredWriteRegionsIfPreferredLocationsIsntEmpty,
            accountLevelReadEndpoints,
            accountLevelWriteEndpoints);
    }

    @Test(groups = "long")
    public void validateWriteEndpointOrderWithClientSideDisableMultipleWriteLocation()  throws Exception {
        this.initialize(false, true, false);
        assertThat(this.cache.getWriteEndpoints().get(0)).isEqualTo(new RegionalRoutingContext(LocationCacheTest.Location1Endpoint));
        assertThat(this.cache.getWriteEndpoints().get(1)).isEqualTo(new RegionalRoutingContext(LocationCacheTest.Location2Endpoint));
        assertThat(this.cache.getWriteEndpoints().get(2)).isEqualTo(new RegionalRoutingContext(LocationCacheTest.Location3Endpoint));
    }

    @Test(groups = "unit", dataProvider = "excludedRegionsTestConfigs")
    public void validateExcludedRegions(
        Set<String> excludedRegionsOnClient,
        List<String> excludedRegionsOnRequest,
        RxDocumentServiceRequest request,
        Set<URI> expectedApplicableEndpoints,
        boolean isPreferredLocationListEmpty) {

        this.initialize(true, true, isPreferredLocationListEmpty);
        AtomicReference<CosmosExcludedRegions> cosmosExcludedRegionsAtomicReference = new AtomicReference<>(
            new CosmosExcludedRegions(new HashSet<>()));
        Supplier<CosmosExcludedRegions> excludedRegionsSupplier = () -> cosmosExcludedRegionsAtomicReference.get();

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(this.cache);

        connectionPolicy.setExcludedRegionsSupplier(excludedRegionsSupplier);

        if (excludedRegionsOnClient == null) {
            assertThatThrownBy(() -> cosmosExcludedRegionsAtomicReference.set(new CosmosExcludedRegions(excludedRegionsOnClient)))
                .isInstanceOf(IllegalArgumentException.class);
        } else {

            cosmosExcludedRegionsAtomicReference.set(new CosmosExcludedRegions(excludedRegionsOnClient));

            request.requestContext.setExcludeRegions(excludedRegionsOnRequest);

            if (request.isReadOnlyRequest()) {
                List<RegionalRoutingContext> applicableReadEndpoints = cache.getApplicableReadRegionRoutingContexts(request);
                assertThat(applicableReadEndpoints.size()).isEqualTo(expectedApplicableEndpoints.size());
                expectedApplicableEndpoints.forEach(endpoint -> assertThat(expectedApplicableEndpoints.contains(endpoint)).isTrue());
            } else {
                List<RegionalRoutingContext> applicableWriteEndpoints = cache.getApplicableWriteRegionRoutingContexts(request);
                assertThat(applicableWriteEndpoints.size()).isEqualTo(expectedApplicableEndpoints.size());
                expectedApplicableEndpoints.forEach(endpoint -> assertThat(expectedApplicableEndpoints.contains(endpoint)).isTrue());
            }
        }
    }

    @Test(groups = "unit", dataProvider = "effectivePreferredRegionTestConfigs")
    public void validateEffectivePreferredRegions(
        RxDocumentServiceRequest request,
        Set<URI> expectedApplicableReadEndpoints,
        Set<URI> expectedApplicableWriteEndpoints,
        boolean isPreferredLocationsListEmpty,
        boolean isDefaultEndpointAlsoRegionalEndpoint) {

        this.initialize(true, true, isPreferredLocationsListEmpty, isDefaultEndpointAlsoRegionalEndpoint);
        List<RegionalRoutingContext> applicableReadEndpoints = cache.getApplicableReadRegionRoutingContexts(request);
        List<RegionalRoutingContext> applicableWriteEndpoints = cache.getApplicableWriteRegionRoutingContexts(request);

        if (request.isReadOnlyRequest()) {
            assertThat(applicableReadEndpoints.size()).isEqualTo(expectedApplicableReadEndpoints.size());
            expectedApplicableReadEndpoints.forEach(endpoint -> assertThat(expectedApplicableReadEndpoints.contains(endpoint)).isTrue());
        } else {
            assertThat(applicableWriteEndpoints.size()).isEqualTo(expectedApplicableWriteEndpoints.size());
            expectedApplicableWriteEndpoints.forEach(endpoint -> assertThat(expectedApplicableWriteEndpoints.contains(endpoint)).isTrue());
        }
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
                        createDatabaseAccountLocation("location3", LocationCacheTest.Location3Endpoint.toString()),
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
            boolean isPreferredLocationsListEmpty) {

        this.initialize(useMultipleWriteLocations, enableEndpointDiscovery, isPreferredLocationsListEmpty, false);
    }

    private void initialize(boolean useMultipleWriteLocations,
                            boolean enableEndpointDiscovery,
                            boolean isPreferredLocationsListEmpty,
                            boolean isDefaultEndpointAlsoRegionalEndpoint) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(enableEndpointDiscovery);
        connectionPolicy.setMultipleWriteRegionsEnabled(useMultipleWriteLocations);

        this.mockedClient = new DatabaseAccountManagerInternalMock();
        this.databaseAccount = LocationCacheTest.createDatabaseAccount(useMultipleWriteLocations);

        this.preferredLocations = isPreferredLocationsListEmpty ?
            new UnmodifiableList<>(Collections.emptyList()) :
            new UnmodifiableList<>(ImmutableList.of("location1", "location2", "location3"));

        connectionPolicy.setPreferredRegions(this.preferredLocations);

        if (isDefaultEndpointAlsoRegionalEndpoint) {
            this.cache = new LocationCache(
                connectionPolicy,
                LocationCacheTest.DefaultRegionalEndpoint,
                configs);
        } else {
            this.cache = new LocationCache(
                connectionPolicy,
                LocationCacheTest.DefaultEndpoint,
                configs);
        }

        this.cache.onDatabaseAccountRead(this.databaseAccount);
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
            boolean isPreferredListEmpty,
            List<URI> preferredReadEndpointsIfPreferredRegionsSetOnClient,
            List<URI> preferredWriteEndpointsIfPreferredRegionsSetOnClient,
            List<URI> accountLevelReadEndpoints,
            List<URI> accountLevelWriteEndpoints) throws Exception {

        int maxReadLocationIndex = isPreferredListEmpty ?
            accountLevelReadEndpoints.size() : preferredReadEndpointsIfPreferredRegionsSetOnClient.size();
        int maxWriteLocationIndex = isPreferredListEmpty ?
            accountLevelWriteEndpoints.size() : preferredWriteEndpointsIfPreferredRegionsSetOnClient.size();

        for (int writeLocationIndex = 0; writeLocationIndex < maxWriteLocationIndex; writeLocationIndex++) {
            for (int readLocationIndex = 0; readLocationIndex < maxReadLocationIndex; readLocationIndex++) {
                this.initialize(
                        useMultipleWriteLocations,
                        endpointDiscoveryEnabled,
                        isPreferredListEmpty);

                UnmodifiableList<RegionalRoutingContext> currentWriteEndpoints = this.cache.getWriteEndpoints();
                UnmodifiableList<RegionalRoutingContext> currentReadEndpoints = this.cache.getReadEndpoints();
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

                accountLevelWriteEndpoints = toStream(this.databaseAccount.getWritableLocations())
                    .map(databaseAccountLocation -> writeEndpointByLocation.get(databaseAccountLocation.getName()))
                    .collect(Collectors.toList());

                accountLevelReadEndpoints = toStream(this.databaseAccount.getReadableLocations())
                    .map(databaseAccountLocation -> readEndpointByLocation.get(databaseAccountLocation.getName()))
                    .collect(Collectors.toList());

                List<String> preferredRegionsWhenClientLevelPreferredRegionsIsEmpty = this.cache.getEffectivePreferredLocations();

                URI[] preferredAvailableWriteEndpoints, preferredAvailableReadEndpoints;

                if (isPreferredListEmpty) {
                    preferredAvailableWriteEndpoints = toStream(preferredRegionsWhenClientLevelPreferredRegionsIsEmpty).skip(writeLocationIndex)
                        .filter(location -> writeEndpointByLocation.containsKey(location))
                        .map(location -> writeEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URI[0]);

                    preferredAvailableReadEndpoints = toStream(preferredRegionsWhenClientLevelPreferredRegionsIsEmpty).skip(readLocationIndex)
                        .filter(location -> readEndpointByLocation.containsKey(location))
                        .map(location -> readEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URI[0]);

                } else {
                    preferredAvailableWriteEndpoints = toStream(this.preferredLocations).skip(writeLocationIndex)
                        .filter(location -> writeEndpointByLocation.containsKey(location))
                        .map(location -> writeEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URI[0]);

                    preferredAvailableReadEndpoints = toStream(this.preferredLocations).skip(readLocationIndex)
                        .filter(location -> readEndpointByLocation.containsKey(location))
                        .map(location -> readEndpointByLocation.get(location))
                        .collect(Collectors.toList()).toArray(new URI[0]);
                }

                this.validateEndpointRefresh(
                        useMultipleWriteLocations,
                        endpointDiscoveryEnabled,
                        isPreferredListEmpty,
                        preferredAvailableWriteEndpoints,
                        preferredAvailableReadEndpoints,
                        preferredRegionsWhenClientLevelPreferredRegionsIsEmpty,
                        preferredRegionsWhenClientLevelPreferredRegionsIsEmpty,
                        accountLevelWriteEndpoints,
                        accountLevelReadEndpoints,
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
            boolean isPreferredListEmpty,
            URI[] preferredAvailableWriteEndpointsOrAccountLevelWriteEndpoints,
            URI[] preferredAvailableReadEndpointsOrAccountLevelReadEndpoints,
            List<String> preferredAvailableWriteRegionsOrAccountLevelWriteEndpoints,
            List<String> preferredAvailableReadRegionsOrAccountLevelReadEndpoints,
            List<URI> accountLevelWriteEndpoints,
            List<URI> accountLevelReadEndpoints,
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
        if (!this.preferredLocations.isEmpty() || isPreferredListEmpty) {
            String mostPreferredReadLocationName = (isPreferredListEmpty && endpointDiscoveryEnabled) ? preferredAvailableReadRegionsOrAccountLevelReadEndpoints.get(0) :
                this.preferredLocations.stream()
                    .filter(location -> toStream(databaseAccount.getReadableLocations())
                            .anyMatch(readLocation -> readLocation.getName().equals(location)))
                    .findFirst().orElse(null);

            URI mostPreferredReadEndpoint = LocationCacheTest.EndpointByLocation.get(mostPreferredReadLocationName);
            isMostPreferredLocationUnavailableForRead = preferredAvailableReadEndpointsOrAccountLevelReadEndpoints.length == 0 ?
                    true : (!areEqual(preferredAvailableReadEndpointsOrAccountLevelReadEndpoints[0], mostPreferredReadEndpoint));

            if (isPreferredListEmpty && endpointDiscoveryEnabled) {
                isMostPreferredLocationUnavailableForRead = !areEqual(preferredAvailableReadEndpointsOrAccountLevelReadEndpoints[0], accountLevelReadEndpoints.get(0));
            }

            String mostPreferredWriteLocationName = (isPreferredListEmpty && endpointDiscoveryEnabled) ? preferredAvailableWriteRegionsOrAccountLevelWriteEndpoints.get(0) :this.preferredLocations.stream()
                    .filter(location -> toStream(databaseAccount.getWritableLocations())
                            .anyMatch(writeLocation -> writeLocation.getName().equals(location)))
                    .findFirst().orElse(null);

            URI mostPreferredWriteEndpoint = LocationCacheTest.EndpointByLocation.get(mostPreferredWriteLocationName);

            if (useMultipleWriteLocations) {
                isMostPreferredLocationUnavailableForWrite = preferredAvailableWriteEndpointsOrAccountLevelWriteEndpoints.length == 0 ?
                        true : (!areEqual(preferredAvailableWriteEndpointsOrAccountLevelWriteEndpoints[0], mostPreferredWriteEndpoint));

                if (isPreferredListEmpty && endpointDiscoveryEnabled) {
                    isMostPreferredLocationUnavailableForWrite = !areEqual(preferredAvailableWriteEndpointsOrAccountLevelWriteEndpoints[0], accountLevelWriteEndpoints.get(0));
                }
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
        UnmodifiableList<RegionalRoutingContext> writeEndpoints = this.cache.getWriteEndpoints();

        assertThat(new RegionalRoutingContext(firstAvailableWriteEndpoint)).isEqualTo(writeEndpoints.get(0));
        assertThat(new RegionalRoutingContext(secondAvailableWriteEndpoint)).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Document, true));
        assertThat(new RegionalRoutingContext(firstAvailableWriteEndpoint)).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Document, false));

        // Writes to other resource types should be directed to first/second write getEndpoint
        assertThat(new RegionalRoutingContext(firstWriteEnpoint)).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Database, false));
        assertThat(new RegionalRoutingContext(secondWriteEnpoint)).isEqualTo(this.resolveEndpointForWriteRequest(ResourceType.Database, true));

        // Reads should be directed to available read endpoints regardless of resource type
        assertThat(new RegionalRoutingContext(firstAvailableReadEndpoint)).isEqualTo(this.resolveEndpointForReadRequest(true));
        assertThat(new RegionalRoutingContext(firstAvailableReadEndpoint)).isEqualTo(this.resolveEndpointForReadRequest(false));
    }

    private RegionalRoutingContext resolveEndpointForReadRequest(boolean masterResourceType) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Read,
                masterResourceType ? ResourceType.Database : ResourceType.Document);
        return this.cache.resolveServiceEndpoint(request);
    }

    private RegionalRoutingContext resolveEndpointForWriteRequest(ResourceType resourceType, boolean useAlternateWriteEndpoint) {
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

    // ========================================================================
    // Region name normalization integration tests
    // ========================================================================

    private static final URI WestUS3Endpoint = createUrl("https://westus3.documents.azure.com");
    private static final URI EastUSEndpoint = createUrl("https://eastus.documents.azure.com");
    private static final URI NorthEuropeEndpoint = createUrl("https://northeurope.documents.azure.com");

    private static DatabaseAccount createDatabaseAccountWithRealRegions() {
        return ModelBridgeUtils.createDatabaseAccount(
            // read endpoints (server returns canonical names)
            ImmutableList.of(
                createDatabaseAccountLocation("West US 3", WestUS3Endpoint.toString()),
                createDatabaseAccountLocation("East US", EastUSEndpoint.toString()),
                createDatabaseAccountLocation("North Europe", NorthEuropeEndpoint.toString())),
            // write endpoints
            ImmutableList.of(
                createDatabaseAccountLocation("West US 3", WestUS3Endpoint.toString()),
                createDatabaseAccountLocation("East US", EastUSEndpoint.toString()),
                createDatabaseAccountLocation("North Europe", NorthEuropeEndpoint.toString())),
            true);
    }

    private LocationCache createCacheWithRealRegions(List<String> preferredRegions) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);
        connectionPolicy.setPreferredRegions(preferredRegions);

        LocationCache locationCache = new LocationCache(
            connectionPolicy,
            DefaultEndpoint,
            configs);

        locationCache.onDatabaseAccountRead(createDatabaseAccountWithRealRegions());
        return locationCache;
    }

    @Test(groups = "unit")
    public void preferredRegions_lowercaseShouldMatchCanonical() {
        // Customer passes "west us 3" (all lowercase) instead of "West US 3"
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("west us 3", "east us", "north europe"));

        UnmodifiableList<RegionalRoutingContext> readEndpoints = locationCache.getReadEndpoints();
        assertThat(readEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(WestUS3Endpoint);
        assertThat(readEndpoints.get(1).getGatewayRegionalEndpoint()).isEqualTo(EastUSEndpoint);
        assertThat(readEndpoints.get(2).getGatewayRegionalEndpoint()).isEqualTo(NorthEuropeEndpoint);
    }

    @Test(groups = "unit")
    public void preferredRegions_noSpacesShouldMatchCanonical() {
        // Customer passes "westus3" (no spaces) instead of "West US 3"
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("westus3", "eastus", "northeurope"));

        UnmodifiableList<RegionalRoutingContext> readEndpoints = locationCache.getReadEndpoints();
        assertThat(readEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(WestUS3Endpoint);
        assertThat(readEndpoints.get(1).getGatewayRegionalEndpoint()).isEqualTo(EastUSEndpoint);
        assertThat(readEndpoints.get(2).getGatewayRegionalEndpoint()).isEqualTo(NorthEuropeEndpoint);
    }

    @Test(groups = "unit")
    public void preferredRegions_uppercaseShouldMatchCanonical() {
        // Customer passes "WEST US 3" (all uppercase)
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("WEST US 3", "EAST US", "NORTH EUROPE"));

        UnmodifiableList<RegionalRoutingContext> readEndpoints = locationCache.getReadEndpoints();
        assertThat(readEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(WestUS3Endpoint);
        assertThat(readEndpoints.get(1).getGatewayRegionalEndpoint()).isEqualTo(EastUSEndpoint);
        assertThat(readEndpoints.get(2).getGatewayRegionalEndpoint()).isEqualTo(NorthEuropeEndpoint);
    }

    @Test(groups = "unit")
    public void excludeRegions_lowercaseNoSpacesShouldExclude() {
        // Preferred regions in canonical form, exclude region with no spaces
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));

        AtomicReference<CosmosExcludedRegions> excludedRef = new AtomicReference<>(
            new CosmosExcludedRegions(new HashSet<>(Arrays.asList("westus3"))));

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(locationCache);
        connectionPolicy.setExcludedRegionsSupplier(excludedRef::get);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        List<RegionalRoutingContext> applicableEndpoints =
            locationCache.getApplicableReadRegionRoutingContexts(request);

        // "westus3" should exclude "West US 3"
        assertThat(applicableEndpoints.stream()
            .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
            .collect(Collectors.toList()))
            .doesNotContain(WestUS3Endpoint);
        assertThat(applicableEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(EastUSEndpoint);
    }

    @Test(groups = "unit")
    public void excludeRegions_mixedCasingShouldExclude() {
        // Exclude with weird casing: "EAST us", "north EUROPE"
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));

        AtomicReference<CosmosExcludedRegions> excludedRef = new AtomicReference<>(
            new CosmosExcludedRegions(new HashSet<>(Arrays.asList("EAST us", "north EUROPE"))));

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(locationCache);
        connectionPolicy.setExcludedRegionsSupplier(excludedRef::get);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        List<RegionalRoutingContext> applicableEndpoints =
            locationCache.getApplicableReadRegionRoutingContexts(request);

        // Only West US 3 should remain
        assertThat(applicableEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(WestUS3Endpoint);
        assertThat(applicableEndpoints.stream()
            .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
            .collect(Collectors.toList()))
            .doesNotContain(EastUSEndpoint)
            .doesNotContain(NorthEuropeEndpoint);
    }

    @Test(groups = "unit")
    public void excludeRegions_requestLevelNoSpacesShouldExclude() {
        // Request-level exclude with space-stripped names
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request.requestContext.setExcludeRegions(Arrays.asList("eastus"));

        List<RegionalRoutingContext> applicableEndpoints =
            locationCache.getApplicableReadRegionRoutingContexts(request);

        assertThat(applicableEndpoints.stream()
            .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
            .collect(Collectors.toList()))
            .doesNotContain(EastUSEndpoint);
        assertThat(applicableEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(WestUS3Endpoint);
    }

    // ========================================================================
    // Unknown region tests — simulates a new Azure region rolled out server-side
    // that is NOT yet in the SDK's static REGION_NAME_TO_REGION_ID_MAPPINGS map.
    // Customer passes the canonically correct name; routing should still work.
    // ========================================================================

    private static final URI PlutoCentralEndpoint = createUrl("https://plutocentral.documents.azure.com");
    private static final URI MarsSouthEndpoint = createUrl("https://marssouth.documents.azure.com");

    private static DatabaseAccount createDatabaseAccountWithUnknownRegions() {
        return ModelBridgeUtils.createDatabaseAccount(
            // Server returns canonical names for new regions not in SDK's static map
            ImmutableList.of(
                createDatabaseAccountLocation("Pluto Central", PlutoCentralEndpoint.toString()),
                createDatabaseAccountLocation("Mars South", MarsSouthEndpoint.toString()),
                createDatabaseAccountLocation("East US", EastUSEndpoint.toString())),
            ImmutableList.of(
                createDatabaseAccountLocation("Pluto Central", PlutoCentralEndpoint.toString()),
                createDatabaseAccountLocation("Mars South", MarsSouthEndpoint.toString()),
                createDatabaseAccountLocation("East US", EastUSEndpoint.toString())),
            true);
    }

    private LocationCache createCacheWithUnknownRegions(List<String> preferredRegions) {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);
        connectionPolicy.setPreferredRegions(preferredRegions);

        LocationCache locationCache = new LocationCache(
            connectionPolicy,
            DefaultEndpoint,
            configs);

        locationCache.onDatabaseAccountRead(createDatabaseAccountWithUnknownRegions());
        return locationCache;
    }

    @Test(groups = "unit")
    public void unknownRegion_canonicalNameShouldRouteCorrectly() {
        // Customer passes the exact canonical name for a new region not in the static map.
        // Server also returns the same canonical name. Routing should work via toLowerCase match.
        LocationCache locationCache = createCacheWithUnknownRegions(
            Arrays.asList("Pluto Central", "Mars South", "East US"));

        UnmodifiableList<RegionalRoutingContext> readEndpoints = locationCache.getReadEndpoints();
        assertThat(readEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(PlutoCentralEndpoint);
        assertThat(readEndpoints.get(1).getGatewayRegionalEndpoint()).isEqualTo(MarsSouthEndpoint);
        assertThat(readEndpoints.get(2).getGatewayRegionalEndpoint()).isEqualTo(EastUSEndpoint);
    }

    @Test(groups = "unit")
    public void unknownRegion_lowercaseShouldRouteCorrectly() {
        // Customer passes lowercase for a new region not in the static map.
        // Should match via CaseInsensitiveMap after toLowerCase.
        LocationCache locationCache = createCacheWithUnknownRegions(
            Arrays.asList("pluto central", "mars south", "east us"));

        UnmodifiableList<RegionalRoutingContext> readEndpoints = locationCache.getReadEndpoints();
        assertThat(readEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(PlutoCentralEndpoint);
        assertThat(readEndpoints.get(1).getGatewayRegionalEndpoint()).isEqualTo(MarsSouthEndpoint);
        assertThat(readEndpoints.get(2).getGatewayRegionalEndpoint()).isEqualTo(EastUSEndpoint);
    }

    @Test(groups = "unit")
    public void unknownRegion_uppercaseShouldRouteCorrectly() {
        // Customer passes UPPERCASE for a new region not in the static map.
        LocationCache locationCache = createCacheWithUnknownRegions(
            Arrays.asList("PLUTO CENTRAL", "MARS SOUTH", "EAST US"));

        UnmodifiableList<RegionalRoutingContext> readEndpoints = locationCache.getReadEndpoints();
        assertThat(readEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(PlutoCentralEndpoint);
        assertThat(readEndpoints.get(1).getGatewayRegionalEndpoint()).isEqualTo(MarsSouthEndpoint);
        assertThat(readEndpoints.get(2).getGatewayRegionalEndpoint()).isEqualTo(EastUSEndpoint);
    }

    @Test(groups = "unit")
    public void unknownRegion_excludeWithCanonicalNameShouldWork() {
        // Exclude an unknown region using its canonical name — should still be excluded
        LocationCache locationCache = createCacheWithUnknownRegions(
            Arrays.asList("Pluto Central", "Mars South", "East US"));

        AtomicReference<CosmosExcludedRegions> excludedRef = new AtomicReference<>(
            new CosmosExcludedRegions(new HashSet<>(Arrays.asList("Pluto Central"))));

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(locationCache);
        connectionPolicy.setExcludedRegionsSupplier(excludedRef::get);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        List<RegionalRoutingContext> applicableEndpoints =
            locationCache.getApplicableReadRegionRoutingContexts(request);

        // "Pluto Central" should be excluded — first endpoint should be Mars South
        assertThat(applicableEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(MarsSouthEndpoint);
        assertThat(applicableEndpoints.stream()
            .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
            .collect(Collectors.toList()))
            .doesNotContain(PlutoCentralEndpoint);
    }

    @Test(groups = "unit")
    public void unknownRegion_excludeWithDifferentCasingShouldWork() {
        // Exclude an unknown region using different casing — equalsIgnoreCase should handle it
        LocationCache locationCache = createCacheWithUnknownRegions(
            Arrays.asList("Pluto Central", "Mars South", "East US"));

        AtomicReference<CosmosExcludedRegions> excludedRef = new AtomicReference<>(
            new CosmosExcludedRegions(new HashSet<>(Arrays.asList("PLUTO CENTRAL"))));

        ConnectionPolicy connectionPolicy = ReflectionUtils.getConnectionPolicy(locationCache);
        connectionPolicy.setExcludedRegionsSupplier(excludedRef::get);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);

        List<RegionalRoutingContext> applicableEndpoints =
            locationCache.getApplicableReadRegionRoutingContexts(request);

        assertThat(applicableEndpoints.get(0).getGatewayRegionalEndpoint()).isEqualTo(MarsSouthEndpoint);
        assertThat(applicableEndpoints.stream()
            .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
            .collect(Collectors.toList()))
            .doesNotContain(PlutoCentralEndpoint);
    }
    // ========================================================================
    // Exclude-region normalization cache tests — exercises the per-LocationCache
    // ConcurrentHashMap<rawCustomerString, normalizedForm> fast path used by
    // getApplicableRegionRoutingContexts on the per-request hot path. Asserts on
    // cache occupancy via reflection to prove that:
    //   - repeated invocations with the same exclude string reuse the cached entry
    //   - distinct strings each get their own entry
    //   - growth halts at the documented cap and the fallback still normalizes correctly
    // ========================================================================

    @SuppressWarnings("unchecked")
    private static java.util.concurrent.ConcurrentHashMap<String, String> getExcludeRegionCache(LocationCache locationCache) {
        try {
            java.lang.reflect.Field field = LocationCache.class.getDeclaredField("rawToNormalizedExcludeRegionCache");
            field.setAccessible(true);
            return (java.util.concurrent.ConcurrentHashMap<String, String>) field.get(locationCache);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not read rawToNormalizedExcludeRegionCache via reflection", e);
        }
    }

    private static int getExcludeRegionCacheCap() {
        try {
            java.lang.reflect.Field field = LocationCache.class.getDeclaredField("EXCLUDE_REGION_CACHE_MAX_SIZE");
            field.setAccessible(true);
            return (int) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not read EXCLUDE_REGION_CACHE_MAX_SIZE via reflection", e);
        }
    }

    private static List<RegionalRoutingContext> requestWithExcludeRegions(LocationCache cache, List<String> excludeRegions) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(), OperationType.Read, ResourceType.Document);
        request.requestContext.setExcludeRegions(excludeRegions);
        return cache.getApplicableReadRegionRoutingContexts(request);
    }

    @Test(groups = "unit")
    public void excludeRegionCache_populatesOnFirstCall() {
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));
        java.util.concurrent.ConcurrentHashMap<String, String> cache = getExcludeRegionCache(locationCache);
        assertThat(cache).isEmpty();

        requestWithExcludeRegions(locationCache, Arrays.asList("West US 3"));

        assertThat(cache).hasSize(1);
        assertThat(cache.get("West US 3")).isEqualTo("westus3");
    }

    @Test(groups = "unit")
    public void excludeRegionCache_reusesCachedEntryOnRepeatedCall() {
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));
        java.util.concurrent.ConcurrentHashMap<String, String> cache = getExcludeRegionCache(locationCache);

        for (int i = 0; i < 50; i++) {
            requestWithExcludeRegions(locationCache, Arrays.asList("West US 3", "East US"));
        }

        // Cache size should equal the number of distinct customer-supplied strings, not the
        // number of normalize calls.
        assertThat(cache).hasSize(2);
        assertThat(cache).containsEntry("West US 3", "westus3");
        assertThat(cache).containsEntry("East US", "eastus");
    }

    @Test(groups = "unit")
    public void excludeRegionCache_distinctStringsGetDistinctEntries() {
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));
        java.util.concurrent.ConcurrentHashMap<String, String> cache = getExcludeRegionCache(locationCache);

        // Same normalized result, different raw inputs - each raw input gets its own entry.
        requestWithExcludeRegions(locationCache, Arrays.asList("West US 3"));
        requestWithExcludeRegions(locationCache, Arrays.asList("WEST US 3"));
        requestWithExcludeRegions(locationCache, Arrays.asList("westus3"));
        requestWithExcludeRegions(locationCache, Arrays.asList("west-us-3"));

        assertThat(cache).hasSize(4);
        assertThat(cache.values()).containsOnly("westus3");
    }

    @Test(groups = "unit")
    public void excludeRegionCache_haltsAtCapAndFallsBackToDirectNormalize() {
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));
        java.util.concurrent.ConcurrentHashMap<String, String> cache = getExcludeRegionCache(locationCache);
        int cap = getExcludeRegionCacheCap();

        // Fill the cache exactly to the cap with synthetic unique exclude-region strings.
        // None of these match real account regions, so routing is unaffected - we're just
        // exercising the cache-population path.
        for (int i = 0; i < cap; i++) {
            requestWithExcludeRegions(locationCache, Arrays.asList("synthetic-region-" + i));
        }
        assertThat(cache).hasSize(cap);

        // Push past the cap with a region that DOES match (East US in no-space form).
        // Cache must NOT grow, AND routing must still correctly exclude East US (fallback
        // to direct normalize must produce the same answer the cache would have).
        List<RegionalRoutingContext> applicable = requestWithExcludeRegions(
            locationCache, Arrays.asList("eastus"));
        assertThat(cache).hasSize(cap);
        assertThat(applicable.stream()
            .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
            .collect(Collectors.toList()))
            .doesNotContain(EastUSEndpoint);
    }

    @Test(groups = "unit")
    public void excludeRegionCache_skipsNullExcludeEntries() {
        LocationCache locationCache = createCacheWithRealRegions(
            Arrays.asList("West US 3", "East US", "North Europe"));
        java.util.concurrent.ConcurrentHashMap<String, String> cache = getExcludeRegionCache(locationCache);

        List<String> withNull = new ArrayList<>();
        withNull.add(null);
        withNull.add("eastus");
        withNull.add(null);

        // Must not NPE and must not insert a null key into the cache.
        List<RegionalRoutingContext> applicable = requestWithExcludeRegions(locationCache, withNull);

        assertThat(cache).hasSize(1).containsKey("eastus");
        assertThat(cache).doesNotContainKey(null);
        assertThat(applicable.stream()
            .map(RegionalRoutingContext::getGatewayRegionalEndpoint)
            .collect(Collectors.toList()))
            .doesNotContain(EastUSEndpoint);
    }
}
