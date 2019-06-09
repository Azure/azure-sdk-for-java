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

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosClientBuilder;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import org.testng.SkipException;
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
    private CosmosClientBuilder clientBuilder;
    private CosmosClient client;

    private CosmosDatabase createdDatabase;    
    private CosmosContainer createdCollection;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public DocumentClientResourceLeakTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    //TODO : FIX tests
    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void resourceLeak() throws Exception {
        //TODO FIXME DANOBLE this test doesn't pass on RNTBD
        if (clientBuilder.getConfigs().getProtocol() == Protocol.Tcp) {
            throw new SkipException("RNTBD");
        }
        System.gc();
        TimeUnit.SECONDS.sleep(10);
        long usedMemoryInBytesBefore = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());


        for (int i = 0; i < MAX_NUMBER; i++) {
            client = clientBuilder.build();
            logger.info("client {}", i);
            try {
                logger.info("creating doc...");
                createDocument(client.getDatabase(createdDatabase.getId()).getContainer(createdCollection.getId()), getDocumentDefinition());
            } finally {
                logger.info("closing client...");
                client.close();
            }
        }
        System.gc();
        TimeUnit.SECONDS.sleep(10);
        long usedMemoryInBytesAfter = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        assertThat(usedMemoryInBytesAfter - usedMemoryInBytesBefore).isLessThan(200 * ONE_MB);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = getSharedCosmosDatabase(client);
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    private CosmosItemSettings getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        CosmosItemSettings doc = new CosmosItemSettings(String.format("{ "
                                                          + "\"id\": \"%s\", "
                                                          + "\"mypk\": \"%s\", "
                                                          + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                          + "}"
                , uuid, uuid));
        return doc;
    }
}
