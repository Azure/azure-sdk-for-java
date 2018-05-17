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

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.ClientUnderTestBuilder;
import com.microsoft.azure.cosmosdb.rx.TestSuiteBase;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

public class SessionTest extends TestSuiteBase {
    protected static final int TIMEOUT = 20000;

    public final static String DATABASE_ID = getDatabaseId(SessionTest.class);
    private final AsyncDocumentClient.Builder clientBuilder;

    private AsyncDocumentClient houseKeepingClient;
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private RxDocumentClientUnderTest client;

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }

    @Factory(dataProvider = "clientBuilders")
    public SessionTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
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

        client = new ClientUnderTestBuilder(clientBuilder).build();
    }

    @AfterClass(groups = { "internal" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(houseKeepingClient, createdDatabase.getId());
        safeClose(houseKeepingClient);

        client.close();
    }

    @BeforeMethod(groups = { "internal" }, timeOut = SETUP_TIMEOUT)
    public void beforeTest() {
        client.httpRequests.clear();
    }

    @Test(groups = { "internal" }, timeOut = TIMEOUT)
    public void testSessionConsistency_ReadYourWrites() {
        RxDocumentClientImpl clientUnderTest = Mockito.spy(client);

        List<String> capturedRequestSessionTokenList = Collections.synchronizedList(new ArrayList<String>());
        List<String> capturedResponseSessionTokenList = Collections.synchronizedList(new ArrayList<String>());

        clientUnderTest.readCollection(getCollectionLink(), null).toBlocking().single();
        clientUnderTest.createDocument(getCollectionLink(), new Document(), null, false).toBlocking().single();

        setupSpySession(capturedRequestSessionTokenList, capturedResponseSessionTokenList, clientUnderTest, client);

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
            final RxDocumentClientImpl origClient) {

        Mockito.reset(spyClient);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
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
    public void testSessionTokenInDocumentRead() {
        Document document = new Document();
        document.setId("1");
        document.set("pk", "pk");
        document = client.createDocument(getCollectionLink(), document, null, false).toBlocking().single()
                .getResource();

        final String documentLink = document.getSelfLink();
        client.readDocument(documentLink, null).toBlocking().single()
                .getResource();

        List<HttpClientRequest<ByteBuf>> documentReadHttpRequests = client.httpRequests.stream()
                .filter(r -> r.getMethod() == HttpMethod.GET)
                .filter(r -> r.getUri().contains(documentLink))
                .collect(Collectors.toList());

        assertThat(documentReadHttpRequests).hasSize(1);
        assertThat(documentReadHttpRequests.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNotEmpty();
    }

    @Test(groups = { "internal" }, timeOut = TIMEOUT)
    public void testSessionTokenRemovedForMasterResource() {
        client.readCollection(getCollectionLink(), null).toBlocking().single();

        List<HttpClientRequest<ByteBuf>> collectionReadHttpRequests = client.httpRequests.stream()
                .filter(r -> r.getMethod() == HttpMethod.GET)
                .filter(r -> r.getUri().contains(getCollectionLink()))
                .collect(Collectors.toList());

        assertThat(collectionReadHttpRequests).hasSize(1);
        assertThat(collectionReadHttpRequests.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
    }
}