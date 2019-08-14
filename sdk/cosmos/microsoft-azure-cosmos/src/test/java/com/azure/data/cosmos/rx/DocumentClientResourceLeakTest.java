// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItemProperties;
import com.google.common.base.Strings;
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

    private CosmosDatabase createdDatabase;
    private CosmosContainer createdCollection;

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
            CosmosClient client = this.clientBuilder().build();
            try {
                logger.info("creating document");
                createDocument(client.getDatabase(createdDatabase.id()).getContainer(createdCollection.id()),
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

        assertThat(usedMemoryInBytesAfter - usedMemoryInBytesBefore).isLessThan(125 * ONE_MB);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        CosmosClient client = this.clientBuilder().build();
        try {
            createdDatabase = getSharedCosmosDatabase(client);
            createdCollection = getSharedMultiPartitionCosmosContainer(client);
        } finally {
            client.close();
        }
    }

    private CosmosItemProperties getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        return new CosmosItemProperties(Strings.lenientFormat(
            "{\"id\":\"%s\",\"mypk\":\"%s\",\"sgmts\":[[6519456,1471916863],[2498434,1455671440]]}", uuid, uuid
        ));
    }
}
