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
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.Document;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.RetryAnalyzer;
import com.azure.data.cosmos.directconnectivity.Protocol;

import reactor.core.publisher.Mono;

import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.apache.commons.io.FileUtils.ONE_MB;

public class VeryLargeDocumentQueryTest extends TestSuiteBase {
    private final static int TIMEOUT = 60000;
    private final static int SETUP_TIMEOUT = 60000;
    private Database createdDatabase;
    private CosmosContainer createdCollection;

    private CosmosClient client;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public VeryLargeDocumentQueryTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, retryAnalyzer = RetryAnalyzer.class)
    public void queryLargeDocuments() throws InterruptedException {
        int cnt = 5;
        for(int i = 0; i < cnt; i++) {
            createLargeDocument();
        }

        try {
            FeedOptions options = new FeedOptions();
            options.enableCrossPartitionQuery(true);
            validateQuerySuccess(createdCollection.queryItems("SELECT * FROM r", options),
                new FeedResponseListValidator.Builder().totalSize(cnt).build());
        } catch (Throwable error) {
            if (this.clientBuilder.getConfigs().getProtocol() == Protocol.TCP) {
                String message = String.format("DIRECT TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.getDesiredConsistencyLevel());
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
    }

    private void createLargeDocument() throws InterruptedException {
        Document docDefinition = getDocumentDefinition();

        //Keep size as ~ 1.999MB to account for size of other props
        int size = (int) (ONE_MB * 1.999);
        docDefinition.set("largeString", StringUtils.repeat("x", size));

        Mono<CosmosItemResponse> createObservable = createdCollection.createItem(docDefinition, new CosmosItemRequestOptions());

        CosmosResponseValidator<CosmosItemResponse> validator = new CosmosResponseValidator.Builder<CosmosItemResponse>()
                .withId(docDefinition.id())
                .build();

        validateSuccess(createObservable, validator);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = 2 * SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "}"
                , uuid, uuid));
        return doc;
    }

    public String getCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.id(), createdCollection.id());
    }
}
