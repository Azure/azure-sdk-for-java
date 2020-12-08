// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.ConflictResolutionPolicy;
import com.azure.cosmos.models.CosmosConflictProperties;
import com.azure.cosmos.models.CosmosConflictRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosConflictsTest {
    private static Logger logger = LoggerFactory.getLogger(CosmosConflictsTest.class);
    private String sprocBody;
    private CosmosAsyncClient globalClient;
    private List<CosmosAsyncClient> regionalClients;

    @BeforeClass(groups = {"multi-master"})
    public void before_ConflictTests() throws Exception {
        sprocBody = IOUtils.toString(
            getClass().getClassLoader().getResourceAsStream("conflict-resolver-sproc"), "UTF-8");
        globalClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .endpointDiscoveryEnabled(false)
            .directMode()
            .buildAsyncClient();

        GlobalEndpointManager globalEndpointManager =
            ReflectionUtils.getGlobalEndpointManager((RxDocumentClientImpl) globalClient.getContextClient());
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        Iterator<DatabaseAccountLocation> locationIterator = databaseAccount.getWritableLocations().iterator();
        regionalClients = new ArrayList<>();
        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            CosmosAsyncClient regionalClient = new CosmosClientBuilder()
                .endpoint(accountLocation.getEndpoint())
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .endpointDiscoveryEnabled(false)
                .directMode()
                .buildAsyncClient();
            regionalClients.add(regionalClient);
        }
    }

    @Test(groups = {"multi-master"})
    public void conflictDefaultLWW() throws InterruptedException {
        String conflictId = "conflict";
        if (this.regionalClients.size() > 1) {
            List<CosmosAsyncContainer> containers = new ArrayList<>();
            warmingUpClient(containers, "TestDB", "TestColl");

            for (int i = 0; i < containers.size(); i++) {
                int finalI = i;
                List<CosmosAsyncContainer> finalContainers = containers;
                new Thread(() -> {
                    ConflictTestPojo conflictObject = new ConflictTestPojo();
                    conflictObject.setId(conflictId);
                    conflictObject.setMypk(conflictId);
                    conflictObject.setRegionId(finalI);
                    tryInsertDocumentTest(finalContainers.get(finalI), conflictObject).block();
                }).start();
            }
            Thread.sleep(10000); // Wait for conflict item to replicate

            Iterator<FeedResponse<CosmosConflictProperties>> iterator =
                containers.get(0).readAllConflicts(new CosmosQueryRequestOptions()).byPage().toIterable().iterator();
            List<ConflictTestPojo> testPojos = new ArrayList<>();
            while (iterator.hasNext()) {
                for (CosmosConflictProperties conflict : iterator.next().getResults()) {
                    testPojos.add(conflict.getItem(ConflictTestPojo.class));
                }
            }
            assertThat(testPojos.size()).isEqualTo(0);

            CosmosItemResponse<ConflictTestPojo> itemResponse = containers.get(0).readItem(conflictId,
                new PartitionKey(conflictId), null, ConflictTestPojo.class).block();

            //Verify delete should always win
            for (int i = 0; i < containers.size(); i++) {
                int finalI = i;
                if (i == 0) {
                    new Thread(() -> {
                        tryReplaceDocumentTest(containers.get(finalI), itemResponse.getItem()).block();
                    }).start();
                } else {
                    new Thread(() -> {
                        tryDeleteDocumentTest(containers.get(finalI), itemResponse.getItem().getId()).block();
                    }).start();
                }
            }
            Thread.sleep(10000); // Wait for conflict item to replicate

            try {
                containers.get(0).readItem(conflictId, new PartitionKey(conflictId), null, ConflictTestPojo.class).block();
                fail("Delete should always win in conflict scenerio");
            } catch (CosmosException ex) {
                assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
            }
        } else {
            fail("Unable to find multi master account");
        }
    }

    @Test(groups = {"multi-master"})
    public void conflictCustomLWW() throws InterruptedException {
        if (this.regionalClients.size() > 1) {
            CosmosAsyncDatabase database = this.regionalClients.get(0).getDatabase("TestDB");
            //getSharedCosmosDatabase(this.regionalClients.get(0));
            CosmosContainerProperties containerProperties = new CosmosContainerProperties("conflictCustomLWWContainer"
                , "/mypk");
            ConflictResolutionPolicy resolutionPolicy = ConflictResolutionPolicy.createLastWriterWinsPolicy(
                "/regionId");
            containerProperties.setConflictResolutionPolicy(resolutionPolicy);
            database.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();
            Thread.sleep(5000); //waiting for container to get available across multi region

            try {
                List<CosmosAsyncContainer> containers = new ArrayList<>();
                warmingUpClient(containers, database.getId(), containerProperties.getId());

                //Creating conflict by creating item in every region simultaneously
                String conflictId = "conflict";
                for (int i = 0; i < containers.size(); i++) {
                    int finalI = i;
                    new Thread(() -> {
                        ConflictTestPojo conflictObject = new ConflictTestPojo();
                        conflictObject.setId(conflictId);
                        conflictObject.setMypk(conflictId);
                        conflictObject.setRegionId(finalI);
                        tryInsertDocumentTest(containers.get(finalI), conflictObject).block();
                    }).start();
                }
                Thread.sleep(10000); // Wait for conflict item to replicate

                Iterator<FeedResponse<CosmosConflictProperties>> iterator =
                    containers.get(0).readAllConflicts(new CosmosQueryRequestOptions()).byPage().toIterable().iterator();
                List<ConflictTestPojo> testPojos = new ArrayList<>();
                while (iterator.hasNext()) {
                    for (CosmosConflictProperties conflict : iterator.next().getResults()) {
                        testPojos.add(conflict.getItem(ConflictTestPojo.class));
                    }
                }
                //There should be no conflict
                assertThat(testPojos.size()).isEqualTo(0);

                CosmosItemResponse<ConflictTestPojo> itemResponse = containers.get(0).readItem(conflictId,
                    new PartitionKey(conflictId), null, ConflictTestPojo.class).block();
                //Higher regionId item should win.
                assertThat(itemResponse.getItem().getRegionId()).isEqualTo(containers.size() - 1);

                //Verify delete should always win
                for (int i = 0; i < containers.size(); i++) {
                    int finalI = i;
                    if (i == 0) {
                        new Thread(() -> {
                            tryReplaceDocumentTest(containers.get(finalI), itemResponse.getItem()).block();
                        }).start();
                    } else {
                        new Thread(() -> {
                            tryDeleteDocumentTest(containers.get(finalI), itemResponse.getItem().getId()).block();
                        }).start();
                    }
                }
                Thread.sleep(10000); // Wait for conflict item to replicate

                try {
                    containers.get(0).readItem(conflictId, new PartitionKey(conflictId), null,
                        ConflictTestPojo.class).block();
                    fail("Delete should always win in conflict scenerio");
                } catch (CosmosException ex) {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                }
            } finally {
                database.getContainer(containerProperties.getId()).delete().block();
            }
        } else {
            fail("Unable to find multi master account");
        }
    }

    @Test(groups = {"simple"})
    public void conflictCustomSproc() throws InterruptedException {
        if (this.regionalClients.size() > 1) {
            CosmosAsyncDatabase database = this.regionalClients.get(0).getDatabase("TestDB");
            //getSharedCosmosDatabase(this.regionalClients.get(0));

            //Creating container with sproc as conflict resolver
            String sprocId = "conflictCustomSproc";
            CosmosContainerProperties containerProperties = new CosmosContainerProperties("conflictSprocContainer",
                "/mypk");
            CosmosAsyncStoredProcedure storedProcedure =
                database.getContainer(containerProperties.getId()).getScripts().getStoredProcedure(sprocId);
            ConflictResolutionPolicy resolutionPolicy = ConflictResolutionPolicy.createCustomPolicy(storedProcedure);
            containerProperties.setConflictResolutionPolicy(resolutionPolicy);
            database.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();
            Thread.sleep(5000); //waiting for container to get available across multi region

            try {
                //create the sproc
                CosmosAsyncContainer asyncContainer = database.getContainer(containerProperties.getId());
                CosmosStoredProcedureProperties procedureProperties = new CosmosStoredProcedureProperties(sprocId,
                    sprocBody);
                asyncContainer.getScripts().createStoredProcedure(procedureProperties).block();

                List<CosmosAsyncContainer> containers = new ArrayList<>();
                warmingUpClient(containers, database.getId(), containerProperties.getId());

                //Creating conflict by creating item in every region simultaneously
                String conflictId = "conflict";
                for (int i = 0; i < containers.size(); i++) {
                    int finalI = i;
                    new Thread(() -> {
                        ConflictTestPojo conflictObject = new ConflictTestPojo();
                        conflictObject.setId(conflictId);
                        conflictObject.setMypk(conflictId);
                        conflictObject.setRegionId(finalI);
                        tryInsertDocumentTest(containers.get(finalI), conflictObject).block();
                    }).start();
                }
                Thread.sleep(10000); // Wait for conflict item to replicate

                Iterator<FeedResponse<CosmosConflictProperties>> iterator =
                    containers.get(0).readAllConflicts(new CosmosQueryRequestOptions()).byPage().toIterable().iterator();
                List<ConflictTestPojo> testPojos = new ArrayList<>();
                while (iterator.hasNext()) {
                    for (CosmosConflictProperties conflict : iterator.next().getResults()) {
                        testPojos.add(conflict.getItem(ConflictTestPojo.class));
                    }
                }
                assertThat(testPojos.size()).isEqualTo(0);

                CosmosItemResponse<ConflictTestPojo> itemResponse = containers.get(0).readItem(conflictId,
                    new PartitionKey(conflictId), null, ConflictTestPojo.class).block();
                //Lower regionId item should win as per sproc.
                assertThat(itemResponse.getItem().getRegionId()).isEqualTo(0);

                //Verify delete should always win
                for (int i = 0; i < containers.size(); i++) {
                    int finalI = i;
                    if (i == 0) {
                        new Thread(() -> {
                            tryReplaceDocumentTest(containers.get(finalI), itemResponse.getItem()).block();
                        }).start();
                    } else {
                        new Thread(() -> {
                            tryDeleteDocumentTest(containers.get(finalI), itemResponse.getItem().getId()).block();
                        }).start();
                    }
                }
                Thread.sleep(10000); // Wait for conflict item to replicate

                try {
                    containers.get(0).readItem(conflictId, new PartitionKey(conflictId), null,
                        ConflictTestPojo.class).block();
                    fail("Delete should always win in conflict scenerio");
                } catch (CosmosException ex) {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.NOTFOUND);
                }
            } finally {
                database.getContainer(containerProperties.getId()).delete().block();
            }
        } else {
            fail("Unable to find multi master account");
        }
    }

    @Test(groups = {"simple"})
    public void conflictNonExistingCustomSproc() throws InterruptedException {
        if (this.regionalClients.size() > 1) {
            CosmosAsyncDatabase database = this.regionalClients.get(0).getDatabase("TestDB");
            //getSharedCosmosDatabase(this.regionalClients.get(0));

            //Creating container with sproc as conflict resolver
            String sprocId = "conflictNonExistingCustomSproc";
            CosmosContainerProperties containerProperties = new CosmosContainerProperties(
                "conflictNonExistingSprocContainer",
                "/mypk");
            CosmosAsyncStoredProcedure storedProcedure =
                database.getContainer(containerProperties.getId()).getScripts().getStoredProcedure(sprocId);
            ConflictResolutionPolicy resolutionPolicy = ConflictResolutionPolicy.createCustomPolicy(storedProcedure);
            containerProperties.setConflictResolutionPolicy(resolutionPolicy);
            database.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400)).block();
            Thread.sleep(5000); //waiting for container to get available across multi region

            try {
                List<CosmosAsyncContainer> containers = new ArrayList<>();
                warmingUpClient(containers, database.getId(), containerProperties.getId());

                String conflictId = "conflict";
                for (int i = 0; i < containers.size(); i++) {
                    int finalI = i;
                    new Thread(() -> {
                        ConflictTestPojo conflictObject = new ConflictTestPojo();
                        conflictObject.setId(conflictId);
                        conflictObject.setMypk(conflictId);
                        conflictObject.setRegionId(finalI);
                        tryInsertDocumentTest(containers.get(finalI), conflictObject).block();
                    }).start();
                }
                Thread.sleep(10000); // Wait for conflict item to replicate

                List<ConflictTestPojo> testPojos = new ArrayList<>();
                List<String> conflictIds = new ArrayList<>();
                Iterator<FeedResponse<CosmosConflictProperties>> iterator = null;
                for (int i = 1; i <= 4; i++) {
                    //Testing readAllConflicts()
                    iterator =
                        containers.get(0).readAllConflicts(new CosmosQueryRequestOptions()).byPage().toIterable().iterator();

                    while (iterator.hasNext()) {
                        for (CosmosConflictProperties conflict : iterator.next().getResults()) {
                            conflictIds.add(conflict.getId());
                            testPojos.add(conflict.getItem(ConflictTestPojo.class));
                        }
                    }
                    if (testPojos.size() == 0) {
                        logger.error("Conflict on {} create has not reflected yet, retrying after 5 sec",
                            containers.get(0).getId());
                        Thread.sleep(5000); // retry after 5 sec
                    } else {
                        break;
                    }
                }
                //We will see some conflicts, as there is no sproc to resolve
                assertThat(testPojos.size()).isEqualTo(containers.size() - 1);

                //Testing queryConflicts(String query, CosmosQueryRequestOptions cosmosQueryRequestOptions)
                testPojos.clear();
                String query = String.format("SELECT * from c where c.id in (%s)",
                    Strings.join(conflictIds.stream().map(s -> "'" + s + "'").collect(Collectors.toList())).with(","));
                CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
                options.setPartitionKey(new PartitionKey(conflictId));
                iterator = containers.get(0).queryConflicts(query, options).byPage().toIterable().iterator();
                while (iterator.hasNext()) {
                    for (CosmosConflictProperties conflict : iterator.next().getResults()) {
                        testPojos.add(conflict.getItem(ConflictTestPojo.class));
                    }
                }
                //We will see some conflicts, as there is no sproc to resolve
                assertThat(testPojos.size()).isEqualTo(containers.size() - 1);


                //Testing queryConflicts(String query)
                testPojos.clear();
                iterator = containers.get(0).queryConflicts(query).byPage().toIterable().iterator();
                while (iterator.hasNext()) {
                    for (CosmosConflictProperties conflict : iterator.next().getResults()) {
                        testPojos.add(conflict.getItem(ConflictTestPojo.class));
                    }
                }
                //We will see some conflicts, as there is no sproc to resolve
                assertThat(testPojos.size()).isEqualTo(containers.size() - 1);

                for (String id : conflictIds) {
                    CosmosConflictRequestOptions requestOptions =
                        new CosmosConflictRequestOptions(new PartitionKey(conflictId));
                    containers.get(0).getConflict(id).delete(requestOptions).block();
                }
                Thread.sleep(5000); // Wait for conflict item to replicate

                iterator =
                    containers.get(0).readAllConflicts(new CosmosQueryRequestOptions()).byPage().toIterable().iterator();
                testPojos.clear();
                while (iterator.hasNext()) {
                    for (CosmosConflictProperties conflict : iterator.next().getResults()) {
                        conflictIds.add(conflict.getId());
                        testPojos.add(conflict.getItem(ConflictTestPojo.class));
                    }
                }
                //Making sure all conflicts are deleted
                assertThat(testPojos.size()).isEqualTo(0);

                CosmosItemResponse<ConflictTestPojo> itemResponse = containers.get(0).readItem(conflictId,
                    new PartitionKey(conflictId), null, ConflictTestPojo.class).block();
                //Verifying during conflict on create we will have one winner item saved
                assertThat(itemResponse.getItem().getId()).isEqualTo(conflictId);
            } finally {
                database.getContainer(containerProperties.getId()).delete().block();
            }
        } else {
            fail("Unable to find multi master account");
        }
    }

    private Mono<CosmosItemResponse<InternalObjectNode>> tryInsertDocument(CosmosAsyncContainer container,
                                                                           InternalObjectNode internalObjectNode) {
        return container.createItem(internalObjectNode, new PartitionKey(internalObjectNode.getId()),
            new CosmosItemRequestOptions())
            .onErrorResume(e -> {
                if (hasCosmosConflictException(e, 409)) {
                    return Mono.empty();
                } else {
                    return Mono.error(e);
                }
            });
    }

    private Mono<CosmosItemResponse<ConflictTestPojo>> tryInsertDocumentTest(CosmosAsyncContainer container,
                                                                             ConflictTestPojo test) {

        return container.createItem(test, new PartitionKey(test.getId()), new CosmosItemRequestOptions())
            .onErrorResume(e -> {
                if (hasCosmosConflictException(e, 409)) {
                    return Mono.empty();
                } else {
                    return Mono.error(e);
                }
            });
    }

    private Mono<CosmosItemResponse<ConflictTestPojo>> tryReplaceDocumentTest(CosmosAsyncContainer container,
                                                                              ConflictTestPojo test) {
        return container.replaceItem(test, test.getId(), new PartitionKey(test.getId()), new CosmosItemRequestOptions())
            .onErrorResume(e -> {
                if (hasCosmosConflictException(e, 409)) {
                    return Mono.empty();
                } else {
                    return Mono.error(e);
                }
            });
    }

    private Mono<CosmosItemResponse<Object>> tryDeleteDocumentTest(CosmosAsyncContainer container, String id) {
        return container.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions())
            .onErrorResume(e -> {
                if (hasCosmosConflictException(e, 409)) {
                    return Mono.empty();
                } else {
                    return Mono.error(e);
                }
            });
    }

    private boolean hasCosmosConflictException(Throwable e, int statusCode) {
        if (e instanceof CosmosException) {
            CosmosException cosmosException = (CosmosException) e;
            return cosmosException.getStatusCode() == statusCode;
        }

        return false;
    }

    private InternalObjectNode getInternalObjectNode() {
        InternalObjectNode internalObjectNode = new InternalObjectNode();
        String uuid = UUID.randomUUID().toString();
        internalObjectNode.setId(uuid);
        BridgeInternal.setProperty(internalObjectNode, "mypk", uuid);
        BridgeInternal.setProperty(internalObjectNode, "regionId", 0);
        return internalObjectNode;
    }

    private ConflictTestPojo getTest() {
        ConflictTestPojo test = new ConflictTestPojo();
        String uuid = UUID.randomUUID().toString();
        test.setId(uuid);
        test.setMypk(uuid);
        test.setRegionId(0);
        return test;
    }

    private void warmingUpClient(List<CosmosAsyncContainer> asyncContainers, String dbId, String containerId) throws InterruptedException {
        for (CosmosAsyncClient asyncClient : this.regionalClients) {
            CosmosAsyncContainer container =
                asyncClient.getDatabase(dbId).getContainer(containerId);
            ConflictTestPojo warmUpItem = getTest();
            for (int i = 1; i <= 4; i++) {
                try {
                    container.createItem(warmUpItem).block();
                    asyncContainers.add(container);
                    break;
                } catch (CosmosException ex) {
                    logger.error("Container {} create has not reflected yet, retrying after 5 sec", containerId);
                    Thread.sleep(5000);//retry again after 5 sec
                }
            }
            container.readItem(warmUpItem.getId(), new PartitionKey(warmUpItem.getId()), null,
                ConflictTestPojo.class).block();
        }
    }
}
