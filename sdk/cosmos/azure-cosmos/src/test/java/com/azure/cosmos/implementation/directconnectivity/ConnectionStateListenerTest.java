// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.TcpServerFactory;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.TcpServer;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.RequestResponseType;
import com.azure.cosmos.implementation.directconnectivity.TcpServerMock.SslContextUtils;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import io.netty.handler.ssl.SslContext;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ConnectionStateListenerTest {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionStateListenerTest.class);

    private static final AtomicInteger randomPort = new AtomicInteger(1000);
    private static int port = 8082;
    private static String serverAddressPrefix = "rntbd://localhost:";
    private static Random random = new Random();

    @DataProvider(name = "connectionStateListenerConfigProvider")
    public Object[][] connectionStateListenerConfigProvider() {
        return new Object[][]{
            // isTcpConnectionEndpointRediscoveryEnabled, serverResponseType, updateAddresses() called times on request, updateAddresses() called times when server shutdown
            {true, RequestResponseType.CHANNEL_FIN, 1, 0},
            {false, RequestResponseType.CHANNEL_FIN, 0, 0},
            {true, RequestResponseType.CHANNEL_RST, 0, 0},
            {false, RequestResponseType.CHANNEL_RST, 0, 0},
            {true, RequestResponseType.NONE, 0, 1}, // the request will be timed out, but the connection will be active. When tcp server shutdown, the connection will be closed gracefully
            {false, RequestResponseType.NONE, 0, 0},
        };
    }

    @Test(groups = { "unit" }, dataProvider = "connectionStateListenerConfigProvider")
    public void connectionStateListener_OnConnectionEvent(
        boolean isTcpConnectionEndpointRediscoveryEnabled,
        RequestResponseType responseType,
        int timesOnRequest,
        int timesOnServerShutdown) throws ExecutionException, InterruptedException {

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
            logger.info("expected failed request with reason {}", e);
        }

        Mockito.verify(addressResolver, Mockito.times(timesOnRequest)).updateAddresses(Mockito.any());

        Mockito.clearInvocations(addressResolver);

        TcpServerFactory.shutdownRntbdServer(server);
        Mockito.verify(addressResolver, Mockito.times(timesOnServerShutdown)).updateAddresses(Mockito.any());
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
