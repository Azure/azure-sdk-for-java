// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RxDocumentClientImpl#shouldUseThinClientStoreModel(boolean, boolean, boolean, RxDocumentServiceRequest)}.
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
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, true, true, request)).isTrue();
    }

    @Test(groups = "unit")
    public void probeUnhealthy_routesToGatewayV1() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        // Probe says proxy is down — even with thin-client enabled and read locations present,
        // the SDK must fall back to Gateway V1 until the next GREEN cycle restores routing.
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, true, false, request)).isFalse();
    }

    @Test(groups = "unit")
    public void thinClientDisabled_routesToGatewayV1() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(false, true, true, request)).isFalse();
    }

    @Test(groups = "unit")
    public void noThinClientReadLocations_routesToGatewayV1() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Read);
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, false, true, request)).isFalse();
    }

    @Test(groups = "unit")
    public void nonDocumentResource_routesToGatewayV1() {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.DocumentCollection);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.Read);
        // Metadata-style reads (DocumentCollection, etc.) must continue through gateway V1
        // even when probe is GREEN and thin-client is enabled.
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, true, true, request)).isFalse();
    }

    @Test(groups = "unit")
    public void documentQuery_routesToThinClient() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Query);
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, true, true, request)).isTrue();
    }

    @Test(groups = "unit")
    public void batchOperation_routesToThinClient() {
        RxDocumentServiceRequest request = mockDocumentRequest(OperationType.Batch);
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, true, true, request)).isTrue();
    }

    @Test(groups = "unit")
    public void allVersionsAndDeletesChangeFeed_routesToGatewayV1() {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.Document);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.ReadFeed);
        Mockito.when(request.isChangeFeedRequest()).thenReturn(true);
        Mockito.when(request.isAllVersionsAndDeletesChangeFeedMode()).thenReturn(true);
        // AllVersionsAndDeletes change feed must NOT go through the proxy.
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, true, true, request)).isFalse();
    }

    @Test(groups = "unit")
    public void incrementalChangeFeed_routesToThinClient() {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.when(request.getResourceType()).thenReturn(ResourceType.Document);
        Mockito.when(request.getOperationType()).thenReturn(OperationType.ReadFeed);
        Mockito.when(request.isChangeFeedRequest()).thenReturn(true);
        Mockito.when(request.isAllVersionsAndDeletesChangeFeedMode()).thenReturn(false);
        assertThat(RxDocumentClientImpl.shouldUseThinClientStoreModel(true, true, true, request)).isTrue();
    }
}
