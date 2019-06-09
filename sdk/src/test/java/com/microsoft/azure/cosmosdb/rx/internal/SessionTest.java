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

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;

import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionTest extends TestSuiteBase {
    protected static final int TIMEOUT = 20000;

    private Database createdDatabase;
    private DocumentCollection createdCollection;
    private String collectionId = "+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~";
    private SpyClientUnderTestFactory.SpyBaseClass<HttpClientRequest<ByteBuf>> spyClient;
    private AsyncDocumentClient houseKeepingClient;
    private ConnectionMode connectionMode;
    private RequestOptions options;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public SessionTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "sessionTestArgProvider")
    public Object[] sessionTestArgProvider() {
        return new Object[] {
                // boolean indicating whether requests should be name based or not
                true,
                false
        };
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        createdDatabase = SHARED_DATABASE;

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        DocumentCollection collection = new DocumentCollection();
        collection.setId(collectionId);
        collection.setPartitionKey(partitionKeyDef);

        createdCollection = createCollection(createGatewayHouseKeepingDocumentClient().build(), createdDatabase.getId(),
                collection, null);
        houseKeepingClient = clientBuilder.build();
        connectionMode = houseKeepingClient.getConnectionPolicy().getConnectionMode();

        if (connectionMode == ConnectionMode.Direct) {
            spyClient = SpyClientUnderTestFactory.createDirectHttpsClientUnderTest(clientBuilder);
        } else {
            spyClient = SpyClientUnderTestFactory.createClientUnderTest(clientBuilder);
        }
        options = new RequestOptions();
        options.setPartitionKey(PartitionKey.None);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteCollection(houseKeepingClient, createdCollection);
        safeClose(houseKeepingClient);
        safeClose(spyClient);
        
    }

    @BeforeMethod(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeTest(Method method) {
        super.beforeMethod(method);
        spyClient.clearCapturedRequests();
    }

    private List<String> getSessionTokensInRequests() {
        return spyClient.getCapturedRequests().stream()
                .map(r -> r.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).collect(Collectors.toList());
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionConsistency_ReadYourWrites(boolean isNameBased) {
        spyClient.readCollection(getCollectionLink(isNameBased), null).toBlocking().single();
        spyClient.createDocument(getCollectionLink(isNameBased), new Document(), null, false).toBlocking().single();

        spyClient.clearCapturedRequests();
        
        for (int i = 0; i < 10; i++) {
            Document documentCreated = spyClient.createDocument(getCollectionLink(isNameBased), new Document(), null, false)
                    .toBlocking().single().getResource();

            // We send session tokens on Writes in Gateway mode
            if (connectionMode == ConnectionMode.Gateway) {
                assertThat(getSessionTokensInRequests()).hasSize(3 * i + 1);
                assertThat(getSessionTokensInRequests().get(3 * i + 0)).isNotEmpty();
            }

            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), options).toBlocking().single();

            assertThat(getSessionTokensInRequests()).hasSize(3 * i + 2);
            assertThat(getSessionTokensInRequests().get(3 * i + 1)).isNotEmpty();

            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), options).toBlocking().single();

            assertThat(getSessionTokensInRequests()).hasSize(3 * i + 3);
            assertThat(getSessionTokensInRequests().get(3 * i + 2)).isNotEmpty();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenInDocumentRead(boolean isNameBased) throws UnsupportedEncodingException {
        Document document = new Document();
        document.setId(UUID.randomUUID().toString());
        document.set("pk", "pk");
        document = spyClient.createDocument(getCollectionLink(isNameBased), document, null, false).toBlocking().single()
                .getResource();

        final String documentLink = getDocumentLink(document, isNameBased);
        spyClient.readDocument(documentLink, options).toBlocking().single()
                .getResource();

        List<HttpClientRequest<ByteBuf>> documentReadHttpRequests = spyClient.getCapturedRequests().stream()
                .filter(r -> r.getMethod() == HttpMethod.GET)
                .filter(r -> {
                    try {
                        return URLDecoder.decode(r.getUri().replaceAll("\\+", "%2b"), "UTF-8").contains(
                                StringUtils.removeEnd(documentLink, "/"));
                    } catch (UnsupportedEncodingException e) {
                        return false;
                    }
                }).collect(Collectors.toList());

        // Direct mode may make more than one call (multiple replicas)
        assertThat(documentReadHttpRequests.size() >= 1).isTrue();
        assertThat(documentReadHttpRequests.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNotEmpty();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenRemovedForMasterResource(boolean isNameBased) throws UnsupportedEncodingException {
        if (connectionMode == ConnectionMode.Direct) {
            throw new SkipException("Master resource access is only through gateway");
        }
        String collectionLink = getCollectionLink(isNameBased);
        spyClient.readCollection(collectionLink, null).toBlocking().single();

        List<HttpClientRequest<ByteBuf>> collectionReadHttpRequests = spyClient.getCapturedRequests().stream()
                .filter(r -> r.getMethod() == HttpMethod.GET)
                .filter(r -> {
                    try {
                        return URLDecoder.decode(r.getUri().replaceAll("\\+", "%2b"), "UTF-8").contains(
                                StringUtils.removeEnd(collectionLink, "/"));
                    } catch (UnsupportedEncodingException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        assertThat(collectionReadHttpRequests).hasSize(1);
        assertThat(collectionReadHttpRequests.get(0).getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
    }

    private String getCollectionLink(boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId():
            createdCollection.getSelfLink();
    }
    
    private String getDocumentLink(Document doc, boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/docs/" + doc.getId() :
            "dbs/" + createdDatabase.getResourceId() + "/colls/" + createdCollection.getResourceId() + "/docs/" + doc.getResourceId() + "/";
    }
}