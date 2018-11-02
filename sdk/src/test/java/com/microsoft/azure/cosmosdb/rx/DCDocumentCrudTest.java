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

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import rx.Observable;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Document Creation end to end through direct https.
 */
public class DCDocumentCrudTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(DCDocumentCrudTest.class);

    private static Database createdDatabase;
    private static DocumentCollection createdCollection;

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    @DataProvider
    public static Object[][] directClientBuilder() {
        return new Object[][] { { createDCBuilder() } };
    }

    static protected AsyncDocumentClient.Builder createDCBuilder() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DirectHttps);
        return new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKey(TestConfigurations.MASTER_KEY).withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.Session).withExperimentalDirectModeEnabled(true);
    }

    @Factory(dataProvider = "directClientBuilder")
    public DCDocumentCrudTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    /**
     * Tests document creation through direct https.
     * @throws Exception
     */
    @Test(groups = { "direct" }, timeOut = TIMEOUT)
    public void createDocument() throws Exception {
        Document docDefinition = getDocumentDefinition();

        Observable<ResourceResponse<Document>> createObservable = client
                .createDocument(getCollectionLink(), docDefinition, null, false);

        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId())
                .build();

        validateSuccess(createObservable, validator, TIMEOUT);
    }

    @BeforeClass(groups = { "direct" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        RequestOptions options = new RequestOptions();
        options.setOfferThroughput(50000);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition(), options);
    }

    @AfterClass(groups = { "direct" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private String getCollectionLink() {
        return String.format("/dbs/%s/colls/%s", createdDatabase.getId(), createdCollection.getId());
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid + System.currentTimeMillis()));
        return doc;
    }
}
