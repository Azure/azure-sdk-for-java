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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ConnectionStateListenerTest {

    private static int port = 8082;
    private static String serverUriString = "rntbd://localhost:" + port;
    private static final Logger logger = LoggerFactory.getLogger(ConnectionStateListenerTest.class);

    @DataProvider(name = "connectionStateListenerConfigProvider")
    public Object[][] connectionStateListenerConfigProvider() {
        return new Object[][]{
            // isTcpConnectionEndpointRediscoveryEnabled, serverResponseType, GlobalAddressResolver.updateAddresses() called times
            {true, RequestResponseType.CHANNEL_FIN, 1},
            {false, RequestResponseType.CHANNEL_FIN, 0},
            {true, RequestResponseType.CHANNEL_RST, 0},
            {false, RequestResponseType.CHANNEL_RST, 0},
        };
    }

    @Test(groups = { "unit" }, dataProvider = "connectionStateListenerConfigProvider")
    public void connectionStateListener_OnConnectionEvent(
        boolean isTcpConnectionEndpointRediscoveryEnabled,
        RequestResponseType responseType,
        int times) throws ExecutionException, InterruptedException {

        TcpServer server = TcpServerFactory.startNewRntbdServer(port);
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
            addressResolver);

        RxDocumentServiceRequest req =
            RxDocumentServiceRequest.create(mockDiagnosticsClientContext(), OperationType.Create, ResourceType.Document,
                "dbs/fakedb/colls/fakeColls",
                getDocumentDefinition(), new HashMap<>());
        req.setPartitionKeyRangeIdentity(new PartitionKeyRangeIdentity("fakeCollectionId","fakePartitionKeyRangeId"));

        Uri targetUri = new Uri(serverUriString);
        try {
            client.invokeStoreAsync(targetUri, req).block();
        } catch (Exception e) {
            logger.info("expected failed request with reason {}", e);
        }
        finally {
            Mockito.verify(addressResolver, Mockito.times(times)).updateAddresses(Mockito.any(), Mockito.any());
        }

        TcpServerFactory.shutdownRntbdServer(server);
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
