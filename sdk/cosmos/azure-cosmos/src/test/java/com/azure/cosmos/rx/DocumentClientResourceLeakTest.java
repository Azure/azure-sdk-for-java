// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.guava27.Strings;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentClientResourceLeakTest extends TestSuiteBase {

    private static final int TIMEOUT = 2400000;
    private static final int MAX_NUMBER = 1000;

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncContainer createdCollection;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public DocumentClientResourceLeakTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(enabled = false, groups = {"emulator"}, timeOut = TIMEOUT)
    public void resourceLeak() throws Exception {

        System.gc();
        TimeUnit.SECONDS.sleep(10);
        long usedMemoryInBytesBefore = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        for (int i = 0; i < MAX_NUMBER; i++) {
            logger.info("CLIENT {}", i);
            CosmosAsyncClient client = this.getClientBuilder().buildAsyncClient();
            try {
                logger.info("creating document");
                createDocument(client.getDatabase(createdDatabase.getId()).getContainer(createdCollection.getId()),
                    getDocumentDefinition());
            } finally {
                logger.info("closing client");
                client.close();
            }
        }

        System.gc();
        TimeUnit.SECONDS.sleep(10);

        long usedMemoryInBytesAfter = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        logger.info("Memory delta: {} - {} = {} MB",
            usedMemoryInBytesAfter / (double)ONE_MB,
            usedMemoryInBytesBefore / (double)ONE_MB,
            (usedMemoryInBytesAfter - usedMemoryInBytesBefore) / (double)ONE_MB);

        assertThat(usedMemoryInBytesAfter - usedMemoryInBytesBefore).isLessThan(300 * ONE_MB);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_DocumentClientResourceLeakTest() {
        CosmosAsyncClient client = this.getClientBuilder().buildAsyncClient();
        try {
            createdDatabase = getSharedCosmosDatabase(client);
            createdCollection = getSharedMultiPartitionCosmosContainer(client);
        } finally {
            client.close();
        }
    }

    private InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        return new InternalObjectNode(Strings.lenientFormat(
            "{\"id\":\"%s\",\"mypk\":\"%s\",\"sgmts\":[[6519456,1471916863],[2498434,1455671440]]}", uuid, uuid
        ));
    }
}
