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
package com.microsoft.azure.cosmosdb.rx;

import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.rx.proxy.HttpProxyServer;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

/**
 * This class help to test proxy host feature scenarios where user can provide proxy
 * host server during AsyncDocumentClient initialization and all its request will
 * go through that particular host. 
 *
 */
public class ProxyHostTest extends TestSuiteBase{

    public final static String DATABASE_ID = getDatabaseId(DocumentCrudTest.class);
    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    private AsyncDocumentClient client;
    private Builder clientBuilder;
    private final String PROXY_HOST = "localhost";
    private final int PROXY_PORT = 8080;
    private HttpProxyServer httpProxyServer;

    @Factory(dataProvider = "clientBuilders")
    public ProxyHostTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());
        httpProxyServer = new HttpProxyServer();
        httpProxyServer.start();
    }

    /**
     * This test will try to create document via http proxy server and validate it.
     * 
     * @throws Exception
     */
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocumentWithValidHttpProxy() throws Exception {
        AsyncDocumentClient clientWithRightProxy = null;
        try {
            ConnectionPolicy connectionPolicy =new ConnectionPolicy();
            connectionPolicy.setProxy(PROXY_HOST, PROXY_PORT);
            clientWithRightProxy = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                    .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
            Document docDefinition = getDocumentDefinition();
            Observable<ResourceResponse<Document>> createObservable = clientWithRightProxy
                    .createDocument(getCollectionLink(), docDefinition, null, false);
            ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                    .withId(docDefinition.getId())
                    .build();
            validateSuccess(createObservable, validator);
        } finally {
            safeClose(clientWithRightProxy);
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
        httpProxyServer.shutDown();
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
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
