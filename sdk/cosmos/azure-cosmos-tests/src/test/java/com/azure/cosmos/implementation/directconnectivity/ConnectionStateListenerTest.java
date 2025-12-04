// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosNettyLeakDetectorFactory;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetryInfo;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.RequestResponseType;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.SslContextUtils;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.TcpServer;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.TcpServerFactory;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConnectionStateListener;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConnectionStateListenerMetrics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestManager;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import io.netty.handler.ssl.SslContext;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionStateListenerTest {

    private static final AtomicInteger randomPort = new AtomicInteger(1000);
    private static final int port = 8082;
    private static final String serverAddressPrefix = "rntbd://localhost:";

    private volatile AutoCloseable disableNettyLeakDetectionScope;

    @BeforeClass(groups = "unit")
    public void beforeClass_DisableNettyLeakDetection() {
        this.disableNettyLeakDetectionScope = CosmosNettyLeakDetectorFactory.createDisableLeakDetectionScope();
    }

    @AfterClass(groups = "unit", alwaysRun = true)
    public void afterClass_ReactivateNettyLeakDetection() throws Exception {
        if (this.disableNettyLeakDetectionScope != null) {
            this.disableNettyLeakDetectionScope.close();
        }
    }

    @DataProvider(name = "connectionStateListenerConfigProvider")
    public Object[][] connectionStateListenerConfigProvider() {
        return new Object[][]{
            // isTcpConnectionEndpointRediscoveryEnabled, serverResponseType, replicaStatusUpdated, replicaStatusUpdated when server shutdown
            {true, RequestResponseType.CHANNEL_FIN, true, false},
            {false, RequestResponseType.CHANNEL_FIN, false, false},
            {true, RequestResponseType.CHANNEL_RST, true, false},
            {false, RequestResponseType.CHANNEL_RST, false, false},
            {true, RequestResponseType.NONE, false, true}, // the request will be timed out, but the connection will be active. When tcp server shutdown, the connection will be closed gracefully
            {false, RequestResponseType.NONE, false, false},
        };
    }

    @DataProvider(name = "connectionStateListenerExceptionProvider")
    public Object[][] connectionStateListenerExceptionProvider() {
        return new Object[][]{
            // exception, canConnectionStateListenerHandleException
            { new IOException(), true },
            { new ClosedChannelException(), true },
            { new RntbdRequestManager.UnhealthyChannelException("Test"), true },
            { new NotFoundException(), false },
            { new GoneException(), false }
        };
    }

    @Test(groups = { "unit" }, dataProvider = "connectionStateListenerConfigProvider")
    public void connectionStateListener_OnConnectionEvent(
        boolean isTcpConnectionEndpointRediscoveryEnabled,
        RequestResponseType responseType,
        boolean markUnhealthy,
        boolean markUnhealthyWhenServerShutdown) throws Exception {

        try (AutoCloseable disableNettyLeakDetectionScope = CosmosNettyLeakDetectorFactory.createDisableLeakDetectionScope()) {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
            connectionPolicy.setTcpConnectionEndpointRediscoveryEnabled(isTcpConnectionEndpointRediscoveryEnabled);

            GlobalAddressResolver addressResolver = Mockito.mock(GlobalAddressResolver.class);

            SslContext sslContext = SslContextUtils.CreateSslContext("client.jks", false);

            Configs config = Mockito.mock(Configs.class);
            Mockito.doReturn(sslContext).when(config).getSslContext(false, false);

            ClientTelemetry clientTelemetry = Mockito.mock(ClientTelemetry.class);
            ClientTelemetryInfo clientTelemetryInfo = new ClientTelemetryInfo(
                "testMachine",
                "testClient",
                "testProcess",
                "testApp",
                ConnectionMode.DIRECT,
                "test-cdb-account",
                "Test Region 1",
                "Linux",
                false,
                Arrays.asList("Test Region 1", "Test Region 2"));

            Mockito.when(clientTelemetry.getClientTelemetryInfo()).thenReturn(clientTelemetryInfo);

            RntbdTransportClient client = new RntbdTransportClient(
                config,
                connectionPolicy,
                new UserAgentContainer(),
                addressResolver,
                clientTelemetry,
                null);

            RxDocumentServiceRequest req =
                RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                    "dbs/fakedb/colls/fakeColls",
                    getDocumentDefinition(), new HashMap<>());
            req.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(new URI("https://localhost:8080"));

            req.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("fakeCollectionId", "fakePartitionKeyRangeId"));

            // Validate connectionStateListener will always track the latest Uri
            List<Uri> targetUris = new ArrayList<>();
            int serverPort = port + randomPort.getAndIncrement();
            String serverAddress = serverAddressPrefix + serverPort;

            targetUris.add(new Uri(serverAddress));
            targetUris.add(new Uri(serverAddress));

            for (Uri uri : targetUris) {
                // using a random generated server port
                TcpServer server = TcpServerFactory.startNewRntbdServer(serverPort);
                // Inject fake response
                server.injectServerResponse(responseType);

                try {
                    client.invokeResourceOperationAsync(uri, req).block();
                } catch (Exception e) {
                    //  no op here
                }

                if (markUnhealthy) {
                    assertThat(uri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);
                    TcpServerFactory.shutdownRntbdServer(server);
                    assertThat(uri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);

                } else {
                    assertThat(uri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Connected);

                    TcpServerFactory.shutdownRntbdServer(server);
                    if (markUnhealthyWhenServerShutdown) {
                        assertThat(uri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);
                    } else {
                        assertThat(uri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Connected);
                    }
                }
            }
        }
    }

    @Test(groups = { "unit" }, dataProvider = "connectionStateListenerExceptionProvider")
    public void connectionStateListenerOnException(Exception exception, boolean canHandle) {
        RntbdEndpoint endpointMock = Mockito.mock(RntbdEndpoint.class);
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessorMock = Mockito.mock(ProactiveOpenConnectionsProcessor.class);
        AddressSelector addressSelectorMock = Mockito.mock(AddressSelector.class);

        Uri testRequestUri = new Uri("http://127.0.0.1:1");
        testRequestUri.setConnected();
        RntbdConnectionStateListener connectionStateListener = new RntbdConnectionStateListener(endpointMock, proactiveOpenConnectionsProcessorMock, addressSelectorMock);

        connectionStateListener.onBeforeSendRequest(testRequestUri);
        connectionStateListener.onException(exception);
        RntbdConnectionStateListenerMetrics metrics = connectionStateListener.getMetrics();
        if (canHandle) {
            assertThat(metrics.getLastActionableContext().getRight()).isEqualTo(1);
            assertThat(testRequestUri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);
        } else {
            assertThat(metrics.getLastActionableContext()).isNull();
            assertThat(testRequestUri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Connected);
        }
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , uuid, uuid));
        return doc;
    }
}
