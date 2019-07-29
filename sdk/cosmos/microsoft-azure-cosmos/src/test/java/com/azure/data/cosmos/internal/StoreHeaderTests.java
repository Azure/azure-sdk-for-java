// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

public class StoreHeaderTests extends TestSuiteBase {

    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public StoreHeaderTests(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void validateStoreHeader() {
        Document docDefinition1 = getDocumentDefinition();
        Document responseDoc1 = createDocument(client, createdDatabase.id(), createdCollection.id(), docDefinition1);
        Assert.assertNotNull(responseDoc1.selfLink());
        Assert.assertNotNull(responseDoc1.get("_attachments"));

        Document docDefinition2 = getDocumentDefinition();
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setHeader("x-ms-exclude-system-properties", "true");
        Document responseDoc2 = createDocument(client, createdDatabase.id(), createdCollection.id(), docDefinition2, requestOptions);
        Assert.assertNull(responseDoc2.selfLink());
        Assert.assertNull(responseDoc2.get("_attachments"));
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();

        createdDatabase = SHARED_DATABASE;
        createdCollection = SHARED_MULTI_PARTITION_COLLECTION;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
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