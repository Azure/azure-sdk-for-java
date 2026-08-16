// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccountManagerInternal;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This test file will cover various scenarios of disaster recovery.
 */
public class GlobalEndPointManagerTest {

    protected static final int TIMEOUT = 6000000;
    DatabaseAccountManagerInternal databaseAccountManagerInternal;

    static String dbAccountJson1 = "{\"_self\":\"\",\"id\":\"testaccount\",\"_rid\":\"testaccount.documents.azure.com\",\"media\":\"//media/\",\"addresses\":\"//addresses/\"," +
        "\"_dbs\":\"//dbs/\",\"writableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"}]," +
        "\"readableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"},{\"name\":\"East Asia\"," +
        "\"databaseAccountEndpoint\":\"https://testaccount-eastasia.documents.azure.com:443/\"}],\"enableMultipleWriteLocations\":false,\"userReplicationPolicy\":{\"asyncReplication\":false," +
        "\"minReplicaSetSize\":3,\"maxReplicasetSize\":4},\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\"},\"systemReplicationPolicy\":{\"minReplicaSetSize\":3," +
        "\"maxReplicasetSize\":4},\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1},\"queryEngineConfiguration\":\"{\\\"maxSqlQueryInputLength\\\":262144," +
        "\\\"maxJoinsPerSqlQuery\\\":5,\\\"maxLogicalAndPerSqlQuery\\\":500,\\\"maxLogicalOrPerSqlQuery\\\":500,\\\"maxUdfRefPerSqlQuery\\\":10,\\\"maxInExpressionItemsCount\\\":16000," +
        "\\\"queryMaxInMemorySortDocumentCount\\\":500,\\\"maxQueryRequestTimeoutFraction\\\":0.9,\\\"sqlAllowNonFiniteNumbers\\\":false,\\\"sqlAllowAggregateFunctions\\\":true," +
        "\\\"sqlAllowSubQuery\\\":true,\\\"sqlAllowScalarSubQuery\\\":true,\\\"allowNewKeywords\\\":true,\\\"sqlAllowLike\\\":false,\\\"sqlAllowGroupByClause\\\":true," +
        "\\\"maxSpatialQueryCells\\\":12,\\\"spatialMaxGeometryPointCount\\\":256,\\\"sqlAllowTop\\\":true,\\\"enableSpatialIndexing\\\":true}\"}\n";


    static String dbAccountJson2 = "{\"_self\":\"\",\"id\":\"testaccount\",\"_rid\":\"testaccount.documents.azure.com\",\"media\":\"//media/\"," +
        "\"addresses\":\"//addresses/\",\"_dbs\":\"//dbs/\",\"writableLocations\":[{\"name\":\"East Asia\",\"databaseAccountEndpoint\":\"https://testaccount-eastasia.documents.azure" +
        ".com:443/\"}],\"readableLocations\":[{\"name\":\"East Asia\",\"databaseAccountEndpoint\":\"https://testaccount-eastasia.documents.azure.com:443/\"}]," +
        "\"enableMultipleWriteLocations\":false,\"userReplicationPolicy\":{\"asyncReplication\":false,\"minReplicaSetSize\":3,\"maxReplicasetSize\":5}," +
        "\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Eventual\"},\"systemReplicationPolicy\":{\"minReplicaSetSize\":3,\"maxReplicasetSize\":5}," +
        "\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1},\"queryEngineConfiguration\":\"{\\\"maxSqlQueryInputLength\\\":262144,\\\"maxJoinsPerSqlQuery\\\":5," +
        "\\\"maxLogicalAndPerSqlQuery\\\":500,\\\"maxLogicalOrPerSqlQuery\\\":500,\\\"maxUdfRefPerSqlQuery\\\":10,\\\"maxInExpressionItemsCount\\\":16000," +
        "\\\"queryMaxInMemorySortDocumentCount\\\":500,\\\"maxQueryRequestTimeoutFraction\\\":0.9,\\\"sqlAllowNonFiniteNumbers\\\":false,\\\"sqlAllowAggregateFunctions\\\":true," +
        "\\\"sqlAllowSubQuery\\\":true,\\\"sqlAllowScalarSubQuery\\\":true,\\\"allowNewKeywords\\\":true,\\\"sqlAllowLike\\\":false,\\\"sqlAllowGroupByClause\\\":true," +
        "\\\"maxSpatialQueryCells\\\":12,\\\"spatialMaxGeometryPointCount\\\":256,\\\"sqlAllowTop\\\":true,\\\"enableSpatialIndexing\\\":false}\"}";

    static String dbAccountJson3 = "{\"_self\":\"\",\"id\":\"testaccount\",\"_rid\":\"testaccount.documents.azure.com\",\"media\":\"//media/\"," +
        "\"addresses\":\"//addresses/\",\"_dbs\":\"//dbs/\",\"writableLocations\":[{\"name\":\"West US\",\"databaseAccountEndpoint\":\"https://testaccount-westus.documents.azure" +
        ".com:443/\"}],\"readableLocations\":[{\"name\":\"West US\",\"databaseAccountEndpoint\":\"https://testaccount-westus.documents.azure.com:443/\"}]," +
        "\"enableMultipleWriteLocations\":false,\"userReplicationPolicy\":{\"asyncReplication\":false,\"minReplicaSetSize\":3,\"maxReplicasetSize\":4}," +
        "\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\"},\"systemReplicationPolicy\":{\"minReplicaSetSize\":3,\"maxReplicasetSize\":4}," +
        "\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1},\"queryEngineConfiguration\":\"{\\\"maxSqlQueryInputLength\\\":262144,\\\"maxJoinsPerSqlQuery\\\":5," +
        "\\\"maxLogicalAndPerSqlQuery\\\":500,\\\"maxLogicalOrPerSqlQuery\\\":500,\\\"maxUdfRefPerSqlQuery\\\":10,\\\"maxInExpressionItemsCount\\\":16000," +
        "\\\"queryMaxInMemorySortDocumentCount\\\":500,\\\"maxQueryRequestTimeoutFraction\\\":0.9,\\\"sqlAllowNonFiniteNumbers\\\":false,\\\"sqlAllowAggregateFunctions\\\":true," +
        "\\\"sqlAllowSubQuery\\\":true,\\\"sqlAllowScalarSubQuery\\\":true,\\\"allowNewKeywords\\\":true,\\\"sqlAllowLike\\\":false,\\\"sqlAllowGroupByClause\\\":true," +
        "\\\"maxSpatialQueryCells\\\":12,\\\"spatialMaxGeometryPointCount\\\":256,\\\"sqlAllowTop\\\":true,\\\"enableSpatialIndexing\\\":true}\"}";

    static String dbAccountJson4 = "{\"_self\":\"\",\"id\":\"testaccount\",\"_rid\":\"testaccount.documents.azure.com\",\"media\":\"//media/\",\"addresses\":\"//addresses/\"," +
        "\"_dbs\":\"//dbs/\",\"writableLocations\":[{\"name\":\"East US\",\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"},{\"name\":\"East Asia\"," +
        "\"databaseAccountEndpoint\":\"https://testaccount-eastasia.documents.azure.com:443/\"}],\"readableLocations\":[{\"name\":\"East US\"," +
        "\"databaseAccountEndpoint\":\"https://testaccount-eastus.documents.azure.com:443/\"},{\"name\":\"East Asia\",\"databaseAccountEndpoint\":\"https://testaccount-eastasia.documents" +
        ".azure.com:443/\"}],\"enableMultipleWriteLocations\":true,\"userReplicationPolicy\":{\"asyncReplication\":false,\"minReplicaSetSize\":3,\"maxReplicasetSize\":4}," +
        "\"userConsistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\"},\"systemReplicationPolicy\":{\"minReplicaSetSize\":3,\"maxReplicasetSize\":4}," +
        "\"readPolicy\":{\"primaryReadCoefficient\":1,\"secondaryReadCoefficient\":1},\"queryEngineConfiguration\":\"{\\\"maxSqlQueryInputLength\\\":262144,\\\"maxJoinsPerSqlQuery\\\":5," +
        "\\\"maxLogicalAndPerSqlQuery\\\":500,\\\"maxLogicalOrPerSqlQuery\\\":500,\\\"maxUdfRefPerSqlQuery\\\":10,\\\"maxInExpressionItemsCount\\\":16000," +
        "\\\"queryMaxInMemorySortDocumentCount\\\":500,\\\"maxQueryRequestTimeoutFraction\\\":0.9,\\\"sqlAllowNonFiniteNumbers\\\":false,\\\"sqlAllowAggregateFunctions\\\":true," +
        "\\\"sqlAllowSubQuery\\\":true,\\\"sqlAllowScalarSubQuery\\\":true,\\\"allowNewKeywords\\\":true,\\\"sqlAllowLike\\\":false,\\\"sqlAllowGroupByClause\\\":true," +
        "\\\"maxSpatialQueryCells\\\":12,\\\"spatialMaxGeometryPointCount\\\":256,\\\"sqlAllowTop\\\":true,\\\"enableSpatialIndexing\\\":true}\"}\n" +
        "0\n" +
        "\n";

    @BeforeClass(groups = "unit")
    public void setup() throws Exception {
        databaseAccountManagerInternal = Mockito.mock(DatabaseAccountManagerInternal.class);
    }

    /**
     * Test for refresh location cache on connectivity issue with no preferred region
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void refreshLocationAsyncForConnectivityIssue() throws Exception {
        GlobalEndpointManager globalEndPointManager = getGlobalEndPointManager();
        DatabaseAccount databaseAccount = new DatabaseAccount(dbAccountJson2);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        globalEndPointManager.markEndpointUnavailableForRead(new URI(("https://testaccount-eastus.documents.azure.com:443/")));
        globalEndPointManager.refreshLocationAsync(null, false).block(); // Cache will be refreshed as there is no preferred active region remaining
        LocationCache locationCache = this.getLocationCache(globalEndPointManager);
        Assert.assertEquals(locationCache.getReadEndpoints().size(), 2, "Read endpoints should have 2 values");

        Map<String, RegionalRoutingContext> availableReadEndpointByLocation = this.getAvailableReadEndpointByLocation(locationCache);
        Assert.assertEquals(availableReadEndpointByLocation.size(), 2);

        Assert.assertTrue(availableReadEndpointByLocation.keySet().contains("East Asia"));
        Assert.assertTrue(availableReadEndpointByLocation.keySet().contains("East US"));

        AtomicBoolean isRefreshing = getIsRefreshing(globalEndPointManager);
        AtomicBoolean isRefreshInBackground = getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());
        Assert.assertTrue(isRefreshInBackground.get());

        databaseAccount = new DatabaseAccount(dbAccountJson3);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        globalEndPointManager.markEndpointUnavailableForRead(new URI(("https://testaccount-eastasia.documents.azure.com:443/")));
        globalEndPointManager.refreshLocationAsync(null, false).block();// Cache will be refreshed as there is no preferred active region remaining
        locationCache = this.getLocationCache(globalEndPointManager);
        Assert.assertEquals(locationCache.getReadEndpoints().size(), 1);

        availableReadEndpointByLocation = this.getAvailableReadEndpointByLocation(locationCache);
        Assert.assertTrue(availableReadEndpointByLocation.keySet().contains("West US"));

        isRefreshing = this.getIsRefreshing(globalEndPointManager);
        isRefreshInBackground = this.getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());
        Assert.assertTrue(isRefreshInBackground.get());
        LifeCycleUtils.closeQuietly(globalEndPointManager);
    }

    /**
     * Test for refresh location cache in background on network failure,
     * switching to different preferredLocation region
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void refreshLocationAsyncForConnectivityIssueWithPreferredRegions() throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        List<String> preferredRegions = new ArrayList<>();
        preferredRegions.add("East US");
        preferredRegions.add("East Asia");
        connectionPolicy.setPreferredRegions(preferredRegions);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);
        DatabaseAccount databaseAccount = new DatabaseAccount(dbAccountJson1);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));
        GlobalEndpointManager globalEndPointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        globalEndPointManager.init();

        LocationCache locationCache = getLocationCache(globalEndPointManager);
        databaseAccount = new DatabaseAccount(dbAccountJson2);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        globalEndPointManager.markEndpointUnavailableForRead(new URI(("https://testaccount-eastus.documents.azure.com:443/")));
        globalEndPointManager.refreshLocationAsync(null, false).block(); // Refreshing location cache due to region outage, moving from East US to East Asia

        locationCache = this.getLocationCache(globalEndPointManager);
        Assert.assertEquals(locationCache.getReadEndpoints().size(), 2); //Cache will not refresh immediately, other preferred region East Asia is still active

        Map<String, RegionalRoutingContext> availableReadEndpointByLocation = this.getAvailableReadEndpointByLocation(locationCache);
        Assert.assertEquals(availableReadEndpointByLocation.size(), 2);
        Assert.assertTrue(availableReadEndpointByLocation.keySet().iterator().next().equalsIgnoreCase("East Asia"));

        AtomicBoolean isRefreshing = getIsRefreshing(globalEndPointManager);
        AtomicBoolean isRefreshInBackground = getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());
        Assert.assertTrue(isRefreshInBackground.get());

        databaseAccount = new DatabaseAccount(dbAccountJson3);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        globalEndPointManager.markEndpointUnavailableForRead(new URI(("https://testaccount-eastasia.documents.azure.com:443/")));
        globalEndPointManager.refreshLocationAsync(null, false).block();// Making eastasia unavailable

        locationCache = this.getLocationCache(globalEndPointManager);

        availableReadEndpointByLocation = this.getAvailableReadEndpointByLocation(locationCache);
        Assert.assertTrue(availableReadEndpointByLocation.keySet().contains("West US"));// Cache will be refreshed as both the preferred region are unavailable now

        isRefreshing = this.getIsRefreshing(globalEndPointManager);
        isRefreshInBackground = this.getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());
        Assert.assertTrue(isRefreshInBackground.get());
        LifeCycleUtils.closeQuietly(globalEndPointManager);
    }

    /**
     * Test for refresh location cache on write forbidden
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void refreshLocationAsyncForWriteForbidden() throws Exception {
        GlobalEndpointManager globalEndPointManager = getGlobalEndPointManager();
        DatabaseAccount databaseAccount = new DatabaseAccount(dbAccountJson2);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        globalEndPointManager.markEndpointUnavailableForWrite(new URI(("https://testaccount-eastus.documents.azure.com:443/")));
        globalEndPointManager.refreshLocationAsync(null, true).block(); // Refreshing location cache due to write forbidden, moving from East US to East Asia

        LocationCache locationCache = this.getLocationCache(globalEndPointManager);
        Assert.assertEquals(locationCache.getReadEndpoints().size(), 1);

        Map<String, RegionalRoutingContext> availableWriteEndpointByLocation = this.getAvailableWriteEndpointByLocation(locationCache);
        Assert.assertTrue(availableWriteEndpointByLocation.keySet().contains("East Asia"));

        AtomicBoolean isRefreshing = getIsRefreshing(globalEndPointManager);
        AtomicBoolean isRefreshInBackground = getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());
        Assert.assertTrue(isRefreshInBackground.get());

        databaseAccount = new DatabaseAccount(dbAccountJson3);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        globalEndPointManager.markEndpointUnavailableForWrite(new URI(("https://testaccount-eastasia.documents.azure.com:443/")));
        globalEndPointManager.refreshLocationAsync(null, true).block();// Refreshing location cache due to write forbidden, moving from East Asia to West US

        locationCache = this.getLocationCache(globalEndPointManager);
        Assert.assertEquals(locationCache.getReadEndpoints().size(), 1);

        availableWriteEndpointByLocation = this.getAvailableWriteEndpointByLocation(locationCache);
        Assert.assertTrue(availableWriteEndpointByLocation.keySet().contains("West US"));

        isRefreshing = this.getIsRefreshing(globalEndPointManager);
        isRefreshInBackground = this.getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());
        Assert.assertTrue(isRefreshInBackground.get());
        LifeCycleUtils.closeQuietly(globalEndPointManager);
    }

    /**
     * Test for background refresh in multi-master: timer must keep running
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void backgroundRefreshForMultiMaster() throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);
        DatabaseAccount databaseAccount = new DatabaseAccount(dbAccountJson4);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));
        GlobalEndpointManager globalEndPointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        globalEndPointManager.init();

        // Background refresh timer must keep running even for multi-master accounts where
        // shouldRefreshEndpoints() returns false. This ensures topology changes (e.g.,
        // multi-write <-> single-write transitions) are detected.
        AtomicBoolean isRefreshInBackground = getRefreshInBackground(globalEndPointManager);
        Assert.assertTrue(isRefreshInBackground.get());
        LifeCycleUtils.closeQuietly(globalEndPointManager);
    }

    /**
     * Validates that a multi-master account's background refresh timer detects a topology
     * change from multi-write to single-write. Without the fix in refreshLocationPrivateAsync,
     * the timer stops after init and the transition is never detected.
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void backgroundRefreshDetectsTopologyChangeForMultiMaster() throws Exception {
        // Start with a multi-writer account (dbAccountJson4: MW, East US + East Asia)
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);
        DatabaseAccount multiWriterAccount = new DatabaseAccount(dbAccountJson4);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any()))
            .thenReturn(Flux.just(multiWriterAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint())
            .thenReturn(new URI("https://testaccount.documents.azure.com:443"));

        GlobalEndpointManager globalEndPointManager = new GlobalEndpointManager(
            databaseAccountManagerInternal, connectionPolicy, new Configs());
        setBackgroundRefreshLocationTimeIntervalInMS(globalEndPointManager, 500);
        setBackgroundRefreshJitterMaxInSeconds(globalEndPointManager, 0);
        globalEndPointManager.init();

        // Verify multi-writer state: 2 write regions available
        LocationCache locationCache = this.getLocationCache(globalEndPointManager);
        Map<String, RegionalRoutingContext> availableWriteEndpoints = this.getAvailableWriteEndpointByLocation(locationCache);
        Assert.assertEquals(availableWriteEndpoints.size(), 2, "Expected 2 write regions for multi-writer account");
        Assert.assertTrue(availableWriteEndpoints.containsKey("East US"));
        Assert.assertTrue(availableWriteEndpoints.containsKey("East Asia"));

        // Transition to single-writer account (dbAccountJson1: SW, East US only for writes)
        DatabaseAccount singleWriterAccount = new DatabaseAccount(dbAccountJson1);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any()))
            .thenReturn(Flux.just(singleWriterAccount));

        // Wait for background refresh to detect the topology change (jitter disabled for test)
        Thread.sleep(2000);

        // Verify single-writer state: write endpoints updated to reflect single-writer topology
        locationCache = this.getLocationCache(globalEndPointManager);
        availableWriteEndpoints = this.getAvailableWriteEndpointByLocation(locationCache);
        Assert.assertEquals(availableWriteEndpoints.size(), 1, "Expected 1 write region after transition to single-writer");
        Assert.assertTrue(availableWriteEndpoints.containsKey("East US"));

        LifeCycleUtils.closeQuietly(globalEndPointManager);
    }

    /**
     * Test for background refresh cycle,
     */
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void startRefreshLocationTimerAsync() throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true);
        DatabaseAccount databaseAccount = new DatabaseAccount(dbAccountJson1);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));
        GlobalEndpointManager globalEndPointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        setBackgroundRefreshLocationTimeIntervalInMS(globalEndPointManager, 1000);
        setBackgroundRefreshJitterMaxInSeconds(globalEndPointManager, 0);
        globalEndPointManager.init();

        databaseAccount = new DatabaseAccount(dbAccountJson2);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        Thread.sleep(2000);

        LocationCache locationCache = this.getLocationCache(globalEndPointManager);
        Map<String, RegionalRoutingContext> availableReadEndpointByLocation = this.getAvailableReadEndpointByLocation(locationCache);
        Assert.assertEquals(availableReadEndpointByLocation.size(), 1);
        Assert.assertTrue(availableReadEndpointByLocation.keySet().iterator().next().equalsIgnoreCase("East Asia"));

        AtomicBoolean isRefreshing = getIsRefreshing(globalEndPointManager);
        AtomicBoolean isRefreshInBackground = getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());

        databaseAccount = new DatabaseAccount(dbAccountJson3);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        Thread.sleep(2000);
        locationCache = this.getLocationCache(globalEndPointManager);

        availableReadEndpointByLocation = this.getAvailableReadEndpointByLocation(locationCache);
        Assert.assertTrue(availableReadEndpointByLocation.keySet().contains("West US"));

        isRefreshing = this.getIsRefreshing(globalEndPointManager);
        isRefreshInBackground = this.getRefreshInBackground(globalEndPointManager);
        Assert.assertFalse(isRefreshing.get());
        LifeCycleUtils.closeQuietly(globalEndPointManager);
    }

    private LocationCache getLocationCache(GlobalEndpointManager globalEndPointManager) throws Exception {
        Field locationCacheField = GlobalEndpointManager.class.getDeclaredField("locationCache");
        locationCacheField.setAccessible(true);
        LocationCache locationCache = (LocationCache) locationCacheField.get(globalEndPointManager);
        return locationCache;
    }

    private Map<String, RegionalRoutingContext> getAvailableWriteEndpointByLocation(LocationCache locationCache) throws Exception {
        Field locationInfoField = LocationCache.class.getDeclaredField("locationInfo");
        locationInfoField.setAccessible(true);
        Object locationInfo = locationInfoField.get(locationCache);

        Class<?> DatabaseAccountLocationsInfoClass = Class.forName("com.azure.cosmos.implementation.routing.LocationCache$DatabaseAccountLocationsInfo");
        Field availableWriteEndpointByLocationField = DatabaseAccountLocationsInfoClass.getDeclaredField("availableWriteRegionalRoutingContextsByRegionName");
        availableWriteEndpointByLocationField.setAccessible(true);
        Field availableReadEndpointByLocationField = DatabaseAccountLocationsInfoClass.getDeclaredField("availableReadRegionalRoutingContextsByRegionName");
        availableReadEndpointByLocationField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, RegionalRoutingContext> map = (Map<String, RegionalRoutingContext>) availableWriteEndpointByLocationField.get(locationInfo);
        return map;
    }

    private Map<String, RegionalRoutingContext> getAvailableReadEndpointByLocation(LocationCache locationCache) throws Exception {
        Field locationInfoField = LocationCache.class.getDeclaredField("locationInfo");
        locationInfoField.setAccessible(true);
        Object locationInfo = locationInfoField.get(locationCache);

        Class<?> DatabaseAccountLocationsInfoClass = Class.forName("com.azure.cosmos.implementation.routing.LocationCache$DatabaseAccountLocationsInfo");
        Field availableReadEndpointByLocationField = DatabaseAccountLocationsInfoClass.getDeclaredField("availableReadRegionalRoutingContextsByRegionName");
        availableReadEndpointByLocationField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, RegionalRoutingContext> map = (Map<String, RegionalRoutingContext>) availableReadEndpointByLocationField.get(locationInfo);
        return map;
    }

    private AtomicBoolean getIsRefreshing(GlobalEndpointManager globalEndPointManager) throws Exception {
        Field isRefreshingField = GlobalEndpointManager.class.getDeclaredField("isRefreshing");
        isRefreshingField.setAccessible(true);
        AtomicBoolean isRefreshing = (AtomicBoolean) isRefreshingField.get(globalEndPointManager);
        return isRefreshing;
    }

    private AtomicBoolean getRefreshInBackground(GlobalEndpointManager globalEndPointManager) throws Exception {
        Field isRefreshInBackgroundField = GlobalEndpointManager.class.getDeclaredField("refreshInBackground");
        isRefreshInBackgroundField.setAccessible(true);
        AtomicBoolean isRefreshInBackground = (AtomicBoolean) isRefreshInBackgroundField.get(globalEndPointManager);
        return isRefreshInBackground;
    }

    private void setBackgroundRefreshLocationTimeIntervalInMS(GlobalEndpointManager globalEndPointManager, int millSec) throws Exception {
        Field backgroundRefreshLocationTimeIntervalInMSField = GlobalEndpointManager.class.getDeclaredField("backgroundRefreshLocationTimeIntervalInMS");
        backgroundRefreshLocationTimeIntervalInMSField.setAccessible(true);
        backgroundRefreshLocationTimeIntervalInMSField.setInt(globalEndPointManager, millSec);
    }

    private void setBackgroundRefreshJitterMaxInSeconds(GlobalEndpointManager globalEndPointManager, int seconds) throws Exception {
        Field jitterField = GlobalEndpointManager.class.getDeclaredField("backgroundRefreshJitterMaxInSeconds");
        jitterField.setAccessible(true);
        jitterField.setInt(globalEndPointManager, seconds);
    }

    private GlobalEndpointManager getGlobalEndPointManager() throws Exception {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setEndpointDiscoveryEnabled(true);
        connectionPolicy.setMultipleWriteRegionsEnabled(true); // currently without this proper, background refresh will not work
        DatabaseAccount databaseAccount = new DatabaseAccount(dbAccountJson1);
        Mockito.when(databaseAccountManagerInternal.getDatabaseAccountFromEndpoint(ArgumentMatchers.any())).thenReturn(Flux.just(databaseAccount));
        Mockito.when(databaseAccountManagerInternal.getServiceEndpoint()).thenReturn(new URI("https://testaccount.documents.azure.com:443"));
        GlobalEndpointManager globalEndPointManager = new GlobalEndpointManager(databaseAccountManagerInternal, connectionPolicy, new Configs());
        globalEndPointManager.init();

        LocationCache locationCache = getLocationCache(globalEndPointManager);
        Assert.assertEquals(locationCache.getReadEndpoints().size(), 2, "Read endpoints should have 2 values");

        Map<String, RegionalRoutingContext> availableWriteEndpointByLocation = this.getAvailableWriteEndpointByLocation(locationCache);
        Map<String, RegionalRoutingContext> availableReadEndpointByLocation = this.getAvailableReadEndpointByLocation(locationCache);
        Assert.assertEquals(availableWriteEndpointByLocation.size(), 1);
        Assert.assertEquals(availableReadEndpointByLocation.size(), 2);
        Assert.assertTrue(availableWriteEndpointByLocation.keySet().contains("East US"));
        Assert.assertTrue(availableReadEndpointByLocation.keySet().contains("East US"));
        Assert.assertTrue(availableReadEndpointByLocation.keySet().contains("East Asia"));
        return globalEndPointManager;
    }
}
