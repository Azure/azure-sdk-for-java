package com.azure.cosmos.implementation.directconnectivity.MockServer;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.directconnectivity.GlobalAddressResolver;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;

public class ConnectionStateListenerTest {
    private final int port= 8082;
    private static EventExecutor serverExecutor = new DefaultEventExecutor();

    @BeforeMethod
    public void before_ConnectionStateListenerTest() {
        RntbdServerMock server = new RntbdServerMock(8082);
        serverExecutor.execute(() -> {
            try {
                server.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (SSLException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void test_normalConnectionStateListener() throws SSLException {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        GlobalAddressResolver addressResolver = Mockito.mock(GlobalAddressResolver.class);

        SslContext sslContext = SslContextBuilder.forClient().build();
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

        Uri targetUri = new Uri("rntbd://localhost:8082");
        client.invokeStoreAsync(targetUri, req).block();
        //testSubscriber.assertComplete();
    }


    // change to use Json
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
