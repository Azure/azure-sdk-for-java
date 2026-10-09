// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ThinClientConnectivityConfig#shouldUseThinClientStoreModel(boolean, boolean, Boolean, Boolean, RxDocumentServiceRequest)}.
 *
 * <p>These tests pin the exact wiring of the routing gate so that the probe-health bit
 * actually flips traffic between the thin-client store model and the gateway-V1 store model.
 * Prior to extracting the helper, this gate was buried inside {@code RxDocumentClientImpl}
 * and exercised only via end-to-end tests, which made probe-fallback regressions hard to
 * catch in CI.
 */
public class ThinClientRoutingGateTests {

    private static RxDocumentServiceRequest mockDocumentRequest(OperationType op) {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.Document);
        Mockito.when(request.getOperationType()).thenReturn(op);
        Mockito.when(request.isChangeFeedRequest()).thenReturn(false);
        Mockito.when(request.isAllVersionsAndDeletesChangeFeedMode()).thenReturn(false);
        return request;
    }

    @Test(groups = "unit")
    public void allConditionsTrue_routesToThinClient() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isTrue();
    }

    @Test(groups = "unit")
    public void probeUnhealthy_routesToGatewayV1() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        // Probe says proxy is down — even with thin-client enabled and read locations present,
        // the SDK must fall back to Gateway V1 until the next GREEN cycle restores routing.
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.FALSE, request)).isFalse();
    }

    @Test(groups = "unit")
    public void probeRenderedNoDecision_routesToGatewayV1() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        // Implicit path (COSMOS.THINCLIENT_ENABLED unset): a null probe decision means the probe
        // has not yet rendered a verdict (not wired / no cycle completed). Without an affirmative
        // GREEN verdict the SDK must NOT route to Gateway V2 -- traffic stays on Gateway V1 until
        // the probe greenlights.
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, null, request)).isFalse();
    }

    @Test(groups = "unit")
    public void explicitOptIn_bypassesUnhealthyProbe_routesToThinClient() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        // An explicit opt-in (COSMOS.THINCLIENT_ENABLED=true) is a hard contract: even an explicit
        // FALSE probe decision is bypassed and the request still routes to the thin-client model.
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, true, Boolean.FALSE, request)).isTrue();
    }

    @Test(groups = "unit")
    public void thinClientDisabled_routesToGatewayV1() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(false, true, null, Boolean.TRUE, request)).isFalse();
    }

    @Test(groups = "unit")
    public void noThinClientReadLocations_routesToGatewayV1() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, false, null, Boolean.TRUE, request)).isFalse();
    }

    @Test(groups = "unit")
    public void nonDocumentResource_routesToGatewayV1() {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.DocumentCollection);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.Read);
        // Metadata-style reads (DocumentCollection, etc.) must continue through gateway V1
        // even when probe is GREEN and thin-client is enabled.
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isFalse();
    }

    @Test(groups = "unit")
    public void documentQuery_routesToThinClient() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Query);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isTrue();
    }

    @Test(groups = "unit")
    public void batchOperation_routesToThinClient() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Batch);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isTrue();
    }

    @Test(groups = "unit")
    public void allVersionsAndDeletesChangeFeed_routesToGatewayV1() {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.Document);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.ReadFeed);
        Mockito.when(request.isChangeFeedRequest()).thenReturn(true);
        Mockito.when(request.isAllVersionsAndDeletesChangeFeedMode()).thenReturn(true);
        // AllVersionsAndDeletes change feed must NOT go through the proxy.
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isFalse();
    }

    @Test(groups = "unit")
    public void incrementalChangeFeed_routesToThinClient() {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.Document);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.ReadFeed);
        Mockito.when(request.isChangeFeedRequest()).thenReturn(true);
        Mockito.when(request.isAllVersionsAndDeletesChangeFeedMode()).thenReturn(false);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isTrue();
    }

    // --- QueryPlan + Stored Procedure routing to Gateway V2 (PR #47759) ---

    @Test(groups = "unit")
    public void executeStoredProcedure_onStoredProcedureResource_routesToThinClient() {
        // Sproc execute lives on ResourceType.StoredProcedure, not Document. The gate must
        // make a carve-out via isExecuteStoredProcedureBasedRequest() so the request still
        // reaches the proxy and gets routed to Gateway V2.
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.StoredProcedure);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.ExecuteJavaScript);
        Mockito.when(request.isExecuteStoredProcedureBasedRequest()).thenReturn(true);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isTrue();
    }

    @Test(groups = "unit")
    public void nonExecuteStoredProcedureResource_routesToGatewayV1() {
        // CRUD on the StoredProcedure resource (create/replace/delete sproc definition) must
        // continue to flow through Gateway V1 — only the execute path is proxied.
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.StoredProcedure);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.Create);
        Mockito.when(request.isExecuteStoredProcedureBasedRequest()).thenReturn(false);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isFalse();
    }

    @Test(groups = "unit")
    public void queryPlanOperation_routesToThinClient() {
        // QueryPlan is fetched on the Document resource with a dedicated operation type.
        // The gate explicitly enumerates OperationType.QueryPlan so plan retrieval is proxied.
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.Document);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.QueryPlan);
        Mockito.when(request.isExecuteStoredProcedureBasedRequest()).thenReturn(false);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.TRUE, request)).isTrue();
    }

    @Test(groups = "unit")
    public void queryPlanOperation_probeUnhealthy_routesToGatewayV1() {
        // Probe fallback must also gate QueryPlan traffic — when the proxy is unhealthy,
        // plan fetches must fall back to Gateway V1 just like document reads.
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.Document);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.QueryPlan);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.FALSE, request)).isFalse();
    }

    @Test(groups = "unit")
    public void executeStoredProcedure_probeUnhealthy_routesToGatewayV1() {
        // Sproc execute must also respect probe health — even with the resource-type carve-out,
        // a RED probe forces fallback to Gateway V1.
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.StoredProcedure);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.ExecuteJavaScript);
        Mockito.when(request.isExecuteStoredProcedureBasedRequest()).thenReturn(true);
        assertThat(ThinClientConnectivityConfig.shouldUseThinClientStoreModel(true, true, null, Boolean.FALSE, request)).isFalse();
    }
}
