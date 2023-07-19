// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

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
import io.netty.handler.ssl.SslContext;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionStateListenerTest {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionStateListenerTest.class);

    private static final AtomicInteger randomPort = new AtomicInteger(1000);
    private static int port = 8082;
    private static String serverAddressPrefix = "rntbd://localhost:";
    private static Random random = new Random();

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
        boolean markUnhealthyWhenServerShutdown) throws ExecutionException, InterruptedException {

        // using a random generated server port
        int serverPort = port + randomPort.getAndIncrement();
        TcpServer server = TcpServerFactory.startNewRntbdServer(serverPort);
        // Inject fake response
        server.injectServerResponse(responseType);

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        connectionPolicy.setTcpConnectionEndpointRediscoveryEnabled(isTcpConnectionEndpointRediscoveryEnabled);

        GlobalAddressResolver addressResolver = Mockito.mock(GlobalAddressResolver.class);

        SslContext sslContext = SslContextUtils.CreateSslContext("client.jks", false);

        Configs config = Mockito.mock(Configs.class);
        Mockito.doReturn(sslContext).when(config).getSslContext();

        RntbdTransportClient client = new RntbdTransportClient(
            config,
            connectionPolicy,
            new UserAgentContainer(),
            addressResolver,
            null,
            null);

        RxDocumentServiceRequest req =
            RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                "dbs/fakedb/colls/fakeColls",
                getDocumentDefinition(), new HashMap<>());
        req.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("fakeCollectionId","fakePartitionKeyRangeId"));

        Uri targetUri = new Uri(serverAddressPrefix + serverPort);
        try {
            client.invokeResourceOperationAsync(targetUri, req).block();
        } catch (Exception e) {
            //  no op here
        }

        if (markUnhealthy) {
            assertThat(targetUri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);
            TcpServerFactory.shutdownRntbdServer(server);
            assertThat(targetUri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);

        } else {
            assertThat(targetUri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Connected);

            TcpServerFactory.shutdownRntbdServer(server);
            if (markUnhealthyWhenServerShutdown) {
                assertThat(targetUri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Unhealthy);
            } else {
                assertThat(targetUri.getHealthStatus()).isEqualTo(Uri.HealthStatus.Connected);
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
