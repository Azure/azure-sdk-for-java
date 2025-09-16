package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import io.netty.channel.ConnectTimeoutException;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URI;

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
}
