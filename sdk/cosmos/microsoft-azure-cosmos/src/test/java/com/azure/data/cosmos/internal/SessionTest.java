// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConnectionMode;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.http.HttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

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
    private SpyClientUnderTestFactory.SpyBaseClass<HttpRequest> spyClient;
    private AsyncDocumentClient houseKeepingClient;
    private ConnectionMode connectionMode;
    private RequestOptions options;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public SessionTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
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
        partitionKeyDef.paths(paths);

        DocumentCollection collection = new DocumentCollection();
        collection.id(collectionId);
        collection.setPartitionKey(partitionKeyDef);

        createdCollection = createCollection(createGatewayHouseKeepingDocumentClient().build(), createdDatabase.id(),
                collection, null);
        houseKeepingClient = clientBuilder().build();
        connectionMode = houseKeepingClient.getConnectionPolicy().connectionMode();

        if (connectionMode == ConnectionMode.DIRECT) {
            spyClient = SpyClientUnderTestFactory.createDirectHttpsClientUnderTest(clientBuilder());
        } else {
            spyClient = SpyClientUnderTestFactory.createClientUnderTest(clientBuilder());
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
        spyClient.clearCapturedRequests();
    }

    private List<String> getSessionTokensInRequests() {
        return spyClient.getCapturedRequests().stream()
                .map(r -> r.headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN)).collect(Collectors.toList());
    }

    //FIXME: Test flakes inconsistently with assertion error
    @Ignore
    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionConsistency_ReadYourWrites(boolean isNameBased) {
        spyClient.readCollection(getCollectionLink(isNameBased), null).blockFirst();
        spyClient.createDocument(getCollectionLink(isNameBased), new Document(), null, false).blockFirst();

        spyClient.clearCapturedRequests();
        
        for (int i = 0; i < 10; i++) {
            Document documentCreated = spyClient.createDocument(getCollectionLink(isNameBased), new Document(), null, false)
                    .blockFirst().getResource();

            spyClient.clearCapturedRequests();

            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), options).blockFirst();

            assertThat(getSessionTokensInRequests()).hasSize(1);
            assertThat(getSessionTokensInRequests().get(0)).isNotEmpty();

            spyClient.readDocument(getDocumentLink(documentCreated, isNameBased), options).blockFirst();

            assertThat(getSessionTokensInRequests()).hasSize(2);
            assertThat(getSessionTokensInRequests().get(1)).isNotEmpty();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenInDocumentRead(boolean isNameBased) throws UnsupportedEncodingException {
        Document document = new Document();
        document.id(UUID.randomUUID().toString());
        BridgeInternal.setProperty(document, "pk", "pk");
        document = spyClient.createDocument(getCollectionLink(isNameBased), document, null, false)
                .blockFirst()
                .getResource();

        final String documentLink = getDocumentLink(document, isNameBased);
        spyClient.readDocument(documentLink, options).blockFirst()
                .getResource();

        List<HttpRequest> documentReadHttpRequests = spyClient.getCapturedRequests().stream()
                .filter(r -> r.httpMethod() == HttpMethod.GET)
                .filter(r -> {
                    try {
                        return URLDecoder.decode(r.uri().toString().replaceAll("\\+", "%2b"), "UTF-8").contains(
                                StringUtils.removeEnd(documentLink, "/"));
                    } catch (UnsupportedEncodingException e) {
                        return false;
                    }
                }).collect(Collectors.toList());

        // DIRECT mode may make more than one call (multiple replicas)
        assertThat(documentReadHttpRequests.size() >= 1).isTrue();
        assertThat(documentReadHttpRequests.get(0).headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNotEmpty();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT, dataProvider = "sessionTestArgProvider")
    public void sessionTokenRemovedForMasterResource(boolean isNameBased) throws UnsupportedEncodingException {
        if (connectionMode == ConnectionMode.DIRECT) {
            throw new SkipException("Master resource access is only through gateway");
        }
        String collectionLink = getCollectionLink(isNameBased);
        spyClient.readCollection(collectionLink, null).blockFirst();

        List<HttpRequest> collectionReadHttpRequests = spyClient.getCapturedRequests().stream()
                .filter(r -> r.httpMethod() == HttpMethod.GET)
                .filter(r -> {
                    try {
                        return URLDecoder.decode(r.uri().toString().replaceAll("\\+", "%2b"), "UTF-8").contains(
                                StringUtils.removeEnd(collectionLink, "/"));
                    } catch (UnsupportedEncodingException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        assertThat(collectionReadHttpRequests).hasSize(1);
        assertThat(collectionReadHttpRequests.get(0).headers().value(HttpConstants.HttpHeaders.SESSION_TOKEN)).isNull();
    }

    private String getCollectionLink(boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id():
            createdCollection.selfLink();
    }
    
    private String getDocumentLink(Document doc, boolean isNameBased) {
        return isNameBased ? "dbs/" + createdDatabase.id() + "/colls/" + createdCollection.id() + "/docs/" + doc.id() :
            "dbs/" + createdDatabase.resourceId() + "/colls/" + createdCollection.resourceId() + "/docs/" + doc.resourceId() + "/";
    }
}