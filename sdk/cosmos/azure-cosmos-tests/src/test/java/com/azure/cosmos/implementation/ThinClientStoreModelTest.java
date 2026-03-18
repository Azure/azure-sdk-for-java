package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import io.netty.channel.ConnectTimeoutException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

public class ThinClientStoreModelTest {
    @Test(groups = "unit")
    public void testThinClientStoreModel() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);
        Mockito.doReturn(new DiagnosticsClientContext.DiagnosticsClientConfig()).when(clientContext).getConfig();
        Mockito
            .doReturn(ImplementationBridgeHelpers
                .CosmosDiagnosticsHelper
                .getCosmosDiagnosticsAccessor()
                .create(clientContext, 1d))
            .when(clientContext).createDiagnostics();

        String sdkGlobalSessionToken = "1#100#1=20#2=5#3=30";
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn(sdkGlobalSessionToken).when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);

        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost:8080")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        // mocking with HTTP/1.1 client, just using this test as basic store model validation. e2e request flow
        // with HTTP/2 will be tested in future PR once the wiring is all connected
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.when(httpClient.send(any(), any())).thenReturn(Mono.error(new ConnectTimeoutException()));

        ThinClientStoreModel storeModel = new ThinClientStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/fakeResourceFullName",
            ResourceType.Document);

        try {
            storeModel.performRequest(dsr).block();
        } catch (Exception e) {
            //no-op
        }
    }

    /**
     * Verifies that additionalHeaders (e.g., workload-id) passed to ThinClientStoreModel's
     * constructor are correctly propagated to the parent RxGatewayStoreModel and injected
     * into outgoing requests via performRequest().
     * <p>
     * This is for the workload-id feature: ThinClientStoreModel extends
     * RxGatewayStoreModel, and the additionalHeaders must flow through the constructor
     * chain so that performRequest() injects them into every request — including
     * metadata requests (collection cache, PKRange cache, etc.).
     */
    @Test(groups = "unit")
    public void testAdditionalHeadersFlowThroughThinClientStoreModel() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);
        Mockito.doReturn(new DiagnosticsClientContext.DiagnosticsClientConfig()).when(clientContext).getConfig();
        Mockito
            .doReturn(ImplementationBridgeHelpers
                .CosmosDiagnosticsHelper
                .getCosmosDiagnosticsAccessor()
                .create(clientContext, 1d))
            .when(clientContext).createDiagnostics();

        String sdkGlobalSessionToken = "1#100#1=20#2=5#3=30";
        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn(sdkGlobalSessionToken).when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost:8080")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        // Capture the HttpRequest sent by performRequest() to verify headers
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        Mockito.when(httpClient.send(requestCaptor.capture(), any()))
            .thenReturn(Mono.error(new ConnectTimeoutException()));

        // Set up additionalHeaders with workload-id
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(HttpConstants.HttpHeaders.WORKLOAD_ID, "15");

        ThinClientStoreModel storeModel = new ThinClientStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            additionalHeaders);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/fakeResourceFullName",
            ResourceType.Document);

        try {
            storeModel.performRequest(dsr).block();
        } catch (Exception e) {
            // Expected — mock HTTP client throws ConnectTimeoutException
        }

        // Verify that the workload-id header was injected into the request
        assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.WORKLOAD_ID))
            .as("workload-id header should be injected into request by performRequest()")
            .isEqualTo("15");
    }

    /**
     * Verifies that ThinClientStoreModel works correctly when additionalHeaders is null
     * (the default case when no workload-id is configured). This ensures backward
     * compatibility — the null case should not throw or inject unexpected headers.
     */
    @Test(groups = "unit")
    public void testNullAdditionalHeadersThinClientStoreModel() throws Exception {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);
        Mockito.doReturn(new DiagnosticsClientContext.DiagnosticsClientConfig()).when(clientContext).getConfig();
        Mockito
            .doReturn(ImplementationBridgeHelpers
                .CosmosDiagnosticsHelper
                .getCosmosDiagnosticsAccessor()
                .create(clientContext, 1d))
            .when(clientContext).createDiagnostics();

        ISessionContainer sessionContainer = Mockito.mock(ISessionContainer.class);
        Mockito.doReturn("1#100#1=20#2=5#3=30").when(sessionContainer).resolveGlobalSessionToken(any());

        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new RegionalRoutingContext(new URI("https://localhost:8080")))
            .when(globalEndpointManager).resolveServiceEndpoint(any());

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        Mockito.when(httpClient.send(any(), any()))
            .thenReturn(Mono.error(new ConnectTimeoutException()));

        // Pass null for additionalHeaders — this is the default case
        ThinClientStoreModel storeModel = new ThinClientStoreModel(
            clientContext,
            sessionContainer,
            ConsistencyLevel.SESSION,
            new UserAgentContainer(),
            globalEndpointManager,
            httpClient,
            null);

        RxDocumentServiceRequest dsr = RxDocumentServiceRequest.createFromName(
            clientContext,
            OperationType.Read,
            "/fakeResourceFullName",
            ResourceType.Document);

        try {
            storeModel.performRequest(dsr).block();
        } catch (Exception e) {
            // Expected — mock HTTP client throws ConnectTimeoutException
        }

        // Verify that no workload-id header was injected
        assertThat(dsr.getHeaders().get(HttpConstants.HttpHeaders.WORKLOAD_ID))
            .as("workload-id header should NOT be present when additionalHeaders is null")
            .isNull();
    }
}
