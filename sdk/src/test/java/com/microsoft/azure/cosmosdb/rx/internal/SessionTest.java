/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.EndpointManager;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.QueryCompatibilityMode;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.TestConfigurations;
import com.microsoft.azure.cosmosdb.rx.TestSuiteBase;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import rx.Observable;
import rx.observers.TestSubscriber;

public class SessionTest extends TestSuiteBase {
    protected static final int TIMEOUT = 20000;

    public final static String DATABASE_ID = getDatabaseId(SessionTest.class);

    private AsyncDocumentClient houseKeepingClient;
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private RxDocumentClientImpl client1;
    private RxDocumentClientImpl client2;

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }

    public class DocumentClientUnderTest extends RxDocumentClientImpl {

        private RxGatewayStoreModel rxGatewayStoreModelSpy;

        public DocumentClientUnderTest(URI serviceEndpoint, String masterKey) {
            super(serviceEndpoint, masterKey, new ConnectionPolicy(), ConsistencyLevel.Session, -1);
        }

        RxGatewayStoreModel createRxGatewayProxy(ConnectionPolicy connectionPolicy, ConsistencyLevel consistencyLevel,
                QueryCompatibilityMode queryCompatibilityMode, String masterKey, Map<String, String> resourceTokens,
                UserAgentContainer userAgentContainer, EndpointManager globalEndpointManager,
                CompositeHttpClient<ByteBuf, ByteBuf> rxClient) {

            this.rxGatewayStoreModelSpy = Mockito
                    .spy(new RxGatewayStoreModel(connectionPolicy, consistencyLevel, queryCompatibilityMode, masterKey,
                            resourceTokens, userAgentContainer, globalEndpointManager, rxClient));

            return this.rxGatewayStoreModelSpy;
        }

        public RxGatewayStoreModel getRxGatewayStoreModelSpy() {
            return this.rxGatewayStoreModelSpy;
        }
    }

    @BeforeClass(groups = { "internal" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        houseKeepingClient = createGatewayRxDocumentClient().build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(houseKeepingClient, d);

        DocumentCollection cl = new DocumentCollection();
        cl.setId(UUID.randomUUID().toString());
        createdCollection = createCollection(houseKeepingClient, createdDatabase.getId(), cl);
    }

    @AfterClass(groups = { "internal" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(houseKeepingClient, createdDatabase.getId());
        safeClose(houseKeepingClient);
    }

    @BeforeTest(groups = { "internal" }, timeOut = SETUP_TIMEOUT)
    public void beforeTest() {
        client1 = (RxDocumentClientImpl) createGatewayRxDocumentClient().build();
        client2 = (RxDocumentClientImpl) createGatewayRxDocumentClient().build();
    }

    @AfterTest(groups = { "internal" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterTest() {
        client1.close();
        client2.close();
    }

    @Test(groups = { "internal" }, timeOut = TIMEOUT)
    public void testSessionConsistency_ReadYourWrites() throws DocumentClientException {
        RxDocumentClientImpl clientUnderTest = Mockito.spy(client1);

        List<String> capturedRequestSessionTokenList = Collections.synchronizedList(new ArrayList<String>());
        List<String> capturedResponseSessionTokenList = Collections.synchronizedList(new ArrayList<String>());

        clientUnderTest.readCollection(getCollectionLink(), null).toBlocking().single();
        clientUnderTest.createDocument(getCollectionLink(), new Document(), null, false).toBlocking().single();

        setupSpySession(capturedRequestSessionTokenList, capturedResponseSessionTokenList, clientUnderTest, client1);

        for (int i = 0; i < 10; i++) {

            Document documentCreated = clientUnderTest.createDocument(getCollectionLink(), new Document(), null, false)
                    .toBlocking().single().getResource();

            assertThat(capturedRequestSessionTokenList).hasSize(3 * i + 1);
            assertThat(capturedRequestSessionTokenList.get(3 * i + 0)).isNotEmpty();

            clientUnderTest.readDocument(documentCreated.getSelfLink(), null).toBlocking().single();

            assertThat(capturedRequestSessionTokenList).hasSize(3 * i + 2);
            assertThat(capturedRequestSessionTokenList.get(3 * i + 1)).isNotEmpty();

            clientUnderTest.readDocument(documentCreated.getSelfLink(), null).toBlocking().single();

            assertThat(capturedRequestSessionTokenList).hasSize(3 * i + 3);
            assertThat(capturedRequestSessionTokenList.get(3 * i + 2)).isNotEmpty();
        }
    }

    private void setupSpySession(final List<String> capturedRequestSessionTokenList,
            final List<String> capturedResponseSessionTokenList, RxDocumentClientImpl spyClient,
            final RxDocumentClientImpl origClient) throws DocumentClientException {

        Mockito.reset(spyClient);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                RxDocumentServiceRequest req = (RxDocumentServiceRequest) args[0];
                RxDocumentServiceResponse resp = (RxDocumentServiceResponse) args[1];

                capturedRequestSessionTokenList.add(req.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN));
                capturedResponseSessionTokenList
                        .add(resp.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN));

                origClient.captureSessionToken(req, resp);

                return null;
            }
        }).when(spyClient).captureSessionToken(Mockito.any(RxDocumentServiceRequest.class),
                Mockito.any(RxDocumentServiceResponse.class));
    }

    @Test(groups = { "internal" }, timeOut = TIMEOUT)
    public void testSessionTokenInDocumentRead() throws Exception {
        DocumentClientUnderTest client = new DocumentClientUnderTest(new URI(TestConfigurations.HOST),
                TestConfigurations.MASTER_KEY);

        ArgumentCaptor<RxDocumentServiceRequest> request = ArgumentCaptor.forClass(RxDocumentServiceRequest.class);

        Document document = new Document();
        document.setId("1");
        document.set("pk", "pk");
        document = client.createDocument(getCollectionLink(), document, null, false).toBlocking().single()
                .getResource();

        client.readDocument(document.getSelfLink(), null).toBlocking().single()
                .getResource();

        Mockito.verify(client.getRxGatewayStoreModelSpy(), Mockito.times(3)).processMessage(request.capture());
        List<RxDocumentServiceRequest> allValues = request.getAllValues();
        assertThat(allValues.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNullOrEmpty();
        assertThat(allValues.get(1).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNullOrEmpty();
    }
    
    @Test(groups = { "internal" }, timeOut = TIMEOUT)
    public void testSessionTokenRemovedForMasterResource() throws Exception {
        DocumentClientUnderTest client = new DocumentClientUnderTest(new URI(TestConfigurations.HOST),
                TestConfigurations.MASTER_KEY);

        client.readCollection(getCollectionLink(), null).toBlocking().single();

        ArgumentCaptor<RxDocumentServiceRequest> request = ArgumentCaptor.forClass(RxDocumentServiceRequest.class);

        Mockito.verify(client.getRxGatewayStoreModelSpy(), Mockito.times(1)).processMessage(request.capture());
        List<RxDocumentServiceRequest> allValues = request.getAllValues();
        assertThat(allValues.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
    }
}