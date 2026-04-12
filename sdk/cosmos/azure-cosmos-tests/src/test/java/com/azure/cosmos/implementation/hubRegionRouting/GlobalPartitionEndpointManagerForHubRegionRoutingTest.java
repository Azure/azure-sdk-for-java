// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.hubRegionRouting;

import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class GlobalPartitionEndpointManagerForHubRegionRoutingTest {

    private static final String COLLECTION_RID = "dbs/db1/colls/col1";

    private GlobalEndpointManager endpointManager;
    private GlobalPartitionEndpointManagerForHubRegionRouting hubRegionManager;

    @BeforeMethod
    public void setUp() {
        System.setProperty("COSMOS.HUB_REGION_PROCESSING_ENABLED", "true");
        endpointManager = Mockito.mock(GlobalEndpointManager.class);
        hubRegionManager = new GlobalPartitionEndpointManagerForHubRegionRouting(endpointManager);
    }

    @AfterMethod
    public void tearDown() {
        System.clearProperty("COSMOS.HUB_REGION_PROCESSING_ENABLED");
    }

    @Test(groups = {"unit"})
    public void featureFlagDisabled_hubRegionRoutingNotActive() {
        hubRegionManager.resetHubRegionProcessingEnabled(false);

        RxDocumentServiceRequest request = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        Assert.assertFalse(hubRegionManager.isHubRegionRoutingActive(request));
    }

    @Test(groups = {"unit"})
    public void featureFlagEnabled_singleMaster_hubRegionRoutingActive() {
        RxDocumentServiceRequest request = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        Assert.assertTrue(hubRegionManager.isHubRegionRoutingActive(request));
    }

    @Test(groups = {"unit"})
    public void featureFlagEnabled_multiMaster_hubRegionRoutingNotActive() {
        RxDocumentServiceRequest request = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(true);

        Assert.assertFalse(hubRegionManager.isHubRegionRoutingActive(request));
    }

    @Test(groups = {"unit"})
    public void nonDocumentRequest_hubRegionRoutingNotActive() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            "/dbs/db1",
            ResourceType.Database);
        request.requestContext = new DocumentServiceRequestContext();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        Assert.assertFalse(hubRegionManager.isHubRegionRoutingActive(request));
    }

    @Test(groups = {"unit"})
    public void queryPlanRequest_hubRegionRoutingNotActive() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(),
            OperationType.QueryPlan,
            "/dbs/db1/colls/col1/docs/doc1",
            ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        Assert.assertFalse(hubRegionManager.isHubRegionRoutingActive(request));
    }

    @Test(groups = {"unit"})
    public void cacheAndRetrieveHubRegion() throws Exception {
        RxDocumentServiceRequest request = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        URI hubUri = new URI("https://hub-region.cosmos.azure.com");
        RegionalRoutingContext hubContext = new RegionalRoutingContext(hubUri);
        request.requestContext.routeToLocation(hubContext);

        hubRegionManager.cacheHubRegionForPartition(request);

        Assert.assertTrue(hubRegionManager.hasCachedHubRegion(request));
        Assert.assertEquals(1, hubRegionManager.getCacheSize());
    }

    @Test(groups = {"unit"})
    public void tryRouteToCachedHubRegion_warmPath() throws Exception {
        RxDocumentServiceRequest request = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        URI hubUri = new URI("https://hub-region.cosmos.azure.com");
        RegionalRoutingContext hubContext = new RegionalRoutingContext(hubUri);
        request.requestContext.routeToLocation(hubContext);

        hubRegionManager.cacheHubRegionForPartition(request);

        // New request for the same partition
        RxDocumentServiceRequest newRequest = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(newRequest)).thenReturn(false);

        Assert.assertTrue(hubRegionManager.tryRouteToCachedHubRegion(newRequest));
        Assert.assertEquals(hubUri, newRequest.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint());
    }

    @Test(groups = {"unit"})
    public void tryRouteToCachedHubRegion_coldPath() {
        RxDocumentServiceRequest request = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        Assert.assertFalse(hubRegionManager.tryRouteToCachedHubRegion(request));
    }

    @Test(groups = {"unit"})
    public void invalidateCachedHubRegion() throws Exception {
        RxDocumentServiceRequest request = createDocumentReadRequest();
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        URI hubUri = new URI("https://hub-region.cosmos.azure.com");
        request.requestContext.routeToLocation(new RegionalRoutingContext(hubUri));

        hubRegionManager.cacheHubRegionForPartition(request);
        Assert.assertTrue(hubRegionManager.hasCachedHubRegion(request));

        hubRegionManager.invalidateCachedHubRegion(request);
        Assert.assertFalse(hubRegionManager.hasCachedHubRegion(request));
        Assert.assertEquals(0, hubRegionManager.getCacheSize());
    }

    @Test(groups = {"unit"})
    public void perPartitionCaching_differentPartitionsDifferentHubs() throws Exception {
        Mockito.when(endpointManager.canUseMultipleWriteLocations(Mockito.any())).thenReturn(false);

        // Partition 1
        RxDocumentServiceRequest request1 = createDocumentReadRequestWithPartition("0", "100");
        URI hub1 = new URI("https://hub1.cosmos.azure.com");
        request1.requestContext.routeToLocation(new RegionalRoutingContext(hub1));
        hubRegionManager.cacheHubRegionForPartition(request1);

        // Partition 2
        RxDocumentServiceRequest request2 = createDocumentReadRequestWithPartition("100", "200");
        URI hub2 = new URI("https://hub2.cosmos.azure.com");
        request2.requestContext.routeToLocation(new RegionalRoutingContext(hub2));
        hubRegionManager.cacheHubRegionForPartition(request2);

        Assert.assertEquals(2, hubRegionManager.getCacheSize());

        // Verify each partition routes to its own hub
        RxDocumentServiceRequest check1 = createDocumentReadRequestWithPartition("0", "100");
        Assert.assertTrue(hubRegionManager.tryRouteToCachedHubRegion(check1));
        Assert.assertEquals(hub1, check1.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint());

        RxDocumentServiceRequest check2 = createDocumentReadRequestWithPartition("100", "200");
        Assert.assertTrue(hubRegionManager.tryRouteToCachedHubRegion(check2));
        Assert.assertEquals(hub2, check2.requestContext.regionalRoutingContextToRoute.getGatewayRegionalEndpoint());
    }

    @Test(groups = {"unit"})
    public void clearRemovesAllCachedEntries() throws Exception {
        Mockito.when(endpointManager.canUseMultipleWriteLocations(Mockito.any())).thenReturn(false);

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.requestContext.routeToLocation(new RegionalRoutingContext(new URI("https://hub.cosmos.azure.com")));
        hubRegionManager.cacheHubRegionForPartition(request);

        Assert.assertEquals(1, hubRegionManager.getCacheSize());

        hubRegionManager.clear();
        Assert.assertEquals(0, hubRegionManager.getCacheSize());
    }

    @Test(groups = {"unit"})
    public void resetDisablingFeatureClearsCacheAndDeactivates() throws Exception {
        Mockito.when(endpointManager.canUseMultipleWriteLocations(Mockito.any())).thenReturn(false);

        RxDocumentServiceRequest request = createDocumentReadRequest();
        request.requestContext.routeToLocation(new RegionalRoutingContext(new URI("https://hub.cosmos.azure.com")));
        hubRegionManager.cacheHubRegionForPartition(request);

        hubRegionManager.resetHubRegionProcessingEnabled(false);
        Assert.assertFalse(hubRegionManager.isHubRegionProcessingEnabled());
        Assert.assertEquals(0, hubRegionManager.getCacheSize());
    }

    @Test(groups = {"unit"})
    public void nullRequest_noException() {
        Assert.assertFalse(hubRegionManager.isHubRegionRoutingActive(null));
        Assert.assertFalse(hubRegionManager.tryRouteToCachedHubRegion(null));
        Assert.assertFalse(hubRegionManager.hasCachedHubRegion(null));
        hubRegionManager.invalidateCachedHubRegion(null);
        hubRegionManager.cacheHubRegionForPartition(null);
    }

    @Test(groups = {"unit"})
    public void documentWriteRequest_hubRegionRoutingActive() {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(),
            OperationType.Create,
            "/dbs/db1/colls/col1/docs/doc1",
            ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        setPartitionKeyRange(request, "0", "100");
        Mockito.when(endpointManager.canUseMultipleWriteLocations(request)).thenReturn(false);

        Assert.assertTrue(hubRegionManager.isHubRegionRoutingActive(request));
    }

    private RxDocumentServiceRequest createDocumentReadRequest() {
        return createDocumentReadRequestWithPartition("0", "100");
    }

    private RxDocumentServiceRequest createDocumentReadRequestWithPartition(String minInclusive, String maxExclusive) {
        RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(
            mockDiagnosticsClientContext(),
            OperationType.Read,
            "/dbs/db1/colls/col1/docs/doc1",
            ResourceType.Document);
        request.requestContext = new DocumentServiceRequestContext();
        setPartitionKeyRange(request, minInclusive, maxExclusive);
        return request;
    }

    private void setPartitionKeyRange(RxDocumentServiceRequest request, String minInclusive, String maxExclusive) {
        PartitionKeyRange pkRange = new PartitionKeyRange();
        pkRange.setMinInclusive(minInclusive);
        pkRange.setMaxExclusive(maxExclusive);
        pkRange.setId(minInclusive + "-" + maxExclusive);
        request.requestContext.resolvedPartitionKeyRange = pkRange;
        request.requestContext.resolvedCollectionRid = COLLECTION_RID;
    }
}
