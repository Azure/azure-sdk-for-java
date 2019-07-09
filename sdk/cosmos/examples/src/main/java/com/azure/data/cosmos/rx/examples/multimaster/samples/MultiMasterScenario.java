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

package com.azure.data.cosmos.rx.examples.multimaster.samples;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.rx.examples.multimaster.ConfigurationManager;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiMasterScenario {

    private final static Logger logger = LoggerFactory.getLogger(MultiMasterScenario.class);

    final private String accountEndpoint;
    final private String accountKey;
    final private List<Worker> workers;
    final private ConflictWorker conflictWorker;

    public MultiMasterScenario() {
        this.accountEndpoint = ConfigurationManager.getAppSettings().getProperty("endpoint");
        this.accountKey = ConfigurationManager.getAppSettings().getProperty("key");

        String databaseName = ConfigurationManager.getAppSettings().getProperty("databaseName");
        String manualCollectionName = ConfigurationManager.getAppSettings().getProperty("manualCollectionName");
        String lwwCollectionName = ConfigurationManager.getAppSettings().getProperty("lwwCollectionName");
        String udpCollectionName = ConfigurationManager.getAppSettings().getProperty("udpCollectionName");
        String basicCollectionName = ConfigurationManager.getAppSettings().getProperty("basicCollectionName");
        String regionsAsString = ConfigurationManager.getAppSettings().getProperty("regions");
        Preconditions.checkNotNull(regionsAsString, "regions is required");
        String[] regions = regionsAsString.split(";");
        Preconditions.checkArgument(regions.length > 0, "at least one region is required");
        Preconditions.checkNotNull(accountEndpoint, "accountEndpoint is required");
        Preconditions.checkNotNull(accountKey, "accountKey is required");
        Preconditions.checkNotNull(databaseName, "databaseName is required");
        Preconditions.checkNotNull(manualCollectionName, "manualCollectionName is required");
        Preconditions.checkNotNull(lwwCollectionName, "lwwCollectionName is required");
        Preconditions.checkNotNull(udpCollectionName, "udpCollectionName is required");
        Preconditions.checkNotNull(basicCollectionName, "basicCollectionName is required");

        this.workers = new ArrayList<>();
        this.conflictWorker = new ConflictWorker(databaseName, basicCollectionName, manualCollectionName, lwwCollectionName, udpCollectionName);

        for (String region : regions) {
            ConnectionPolicy policy = new ConnectionPolicy();
            policy.usingMultipleWriteLocations(true);
            policy.preferredLocations(Collections.singletonList(region));

            AsyncDocumentClient client =
                    new AsyncDocumentClient.Builder()
                            .withMasterKeyOrResourceToken(this.accountKey)
                            .withServiceEndpoint(this.accountEndpoint)
                            .withConsistencyLevel(ConsistencyLevel.EVENTUAL)
                            .withConnectionPolicy(policy).build();


            workers.add(new Worker(client, databaseName, basicCollectionName));

            conflictWorker.addClient(client);
        }
    }

    public void initialize() throws Exception {
        this.conflictWorker.initialize();
        logger.info("Initialized collections.");
    }

    public void runBasic() throws Exception {
        logger.info("\n####################################################");
        logger.info("Basic Active-Active");
        logger.info("####################################################");

        logger.info("1) Starting insert loops across multiple regions ...");

        List<Mono<Void>> basicTask = new ArrayList<>();

        int documentsToInsertPerWorker = 100;

        for (Worker worker : this.workers) {
            basicTask.add(worker.runLoopAsync(documentsToInsertPerWorker));
        }

        Mono.when(basicTask).block();

        basicTask.clear();

        logger.info("2) Reading from every region ...");

        int expectedDocuments = this.workers.size() * documentsToInsertPerWorker;
        for (Worker worker : this.workers) {
            basicTask.add(worker.readAllAsync(expectedDocuments));
        }

        Mono.when(basicTask).block();

        basicTask.clear();

        logger.info("3) Deleting all the documents ...");

        this.workers.get(0).deleteAll();

        logger.info("####################################################");
    }

    public void runManualConflict() throws Exception {
        logger.info("\n####################################################");
        logger.info("Manual Conflict Resolution");
        logger.info("####################################################");

        this.conflictWorker.runManualConflict();
        logger.info("####################################################");
    }

    public void runLWW() throws Exception {
        logger.info("\n####################################################");
        logger.info("LWW Conflict Resolution");
        logger.info("####################################################");

        this.conflictWorker.runLWWConflict();
        logger.info("####################################################");
    }

    public void runUDP() throws Exception {
        logger.info("\n####################################################");
        logger.info("UDP Conflict Resolution");
        logger.info("####################################################");

        this.conflictWorker.runUDPConflict();
        logger.info("####################################################");
    }

    public void shutdown() {
        conflictWorker.shutdown();
        for(Worker worker: this.workers) {
            worker.shutdown();
        }
    }
}
