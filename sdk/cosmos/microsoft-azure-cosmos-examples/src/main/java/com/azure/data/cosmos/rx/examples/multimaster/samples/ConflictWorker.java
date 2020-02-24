// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx.examples.multimaster.samples;

import com.azure.data.cosmos.AccessCondition;
import com.azure.data.cosmos.AccessConditionType;
import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.internal.Conflict;
import com.azure.data.cosmos.ConflictResolutionPolicy;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.StoredProcedure;
import com.azure.data.cosmos.rx.examples.multimaster.Helpers;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConflictWorker {
    private static Logger logger = LoggerFactory.getLogger(ConflictWorker.class);

    private final Scheduler schedulerForBlockingWork;
    private final List<AsyncDocumentClient> clients;
    private final String basicCollectionUri;
    private final String manualCollectionUri;
    private final String lwwCollectionUri;
    private final String udpCollectionUri;
    private final String databaseName;
    private final String basicCollectionName;
    private final String manualCollectionName;
    private final String lwwCollectionName;
    private final String udpCollectionName;
    private final ExecutorService executor;

    public ConflictWorker(String databaseName, String basicCollectionName, String manualCollectionName, String lwwCollectionName, String udpCollectionName) {
        this.clients = new ArrayList<>();
        this.basicCollectionUri = Helpers.createDocumentCollectionUri(databaseName, basicCollectionName);
        this.manualCollectionUri = Helpers.createDocumentCollectionUri(databaseName, manualCollectionName);
        this.lwwCollectionUri = Helpers.createDocumentCollectionUri(databaseName, lwwCollectionName);
        this.udpCollectionUri = Helpers.createDocumentCollectionUri(databaseName, udpCollectionName);

        this.databaseName = databaseName;
        this.basicCollectionName = basicCollectionName;
        this.manualCollectionName = manualCollectionName;
        this.lwwCollectionName = lwwCollectionName;
        this.udpCollectionName = udpCollectionName;

        this.executor = Executors.newFixedThreadPool(100);
        this.schedulerForBlockingWork = Schedulers.fromExecutor(executor);
    }

    public void addClient(AsyncDocumentClient client) {
        this.clients.add(client);
    }

    private DocumentCollection createCollectionIfNotExists(AsyncDocumentClient createClient, String databaseName, DocumentCollection collection) {
        return Helpers.createCollectionIfNotExists(createClient, this.databaseName, collection)
                .subscribeOn(schedulerForBlockingWork).block();
    }

    private DocumentCollection createCollectionIfNotExists(AsyncDocumentClient createClient, String databaseName, String collectionName) {

        return Helpers.createCollectionIfNotExists(createClient, this.databaseName, this.basicCollectionName)
                .subscribeOn(schedulerForBlockingWork).block();
    }

    private DocumentCollection getCollectionDefForManual(String id) {
        DocumentCollection collection = new DocumentCollection();
        collection.id(id);
        ConflictResolutionPolicy policy = ConflictResolutionPolicy.createCustomPolicy();
        collection.setConflictResolutionPolicy(policy);
        return collection;
    }

    private DocumentCollection getCollectionDefForLastWinWrites(String id, String conflictResolutionPath) {
        DocumentCollection collection = new DocumentCollection();
        collection.id(id);
        ConflictResolutionPolicy policy = ConflictResolutionPolicy.createLastWriterWinsPolicy(conflictResolutionPath);
        collection.setConflictResolutionPolicy(policy);
        return collection;
    }

    private DocumentCollection getCollectionDefForCustom(String id, String storedProc) {
        DocumentCollection collection = new DocumentCollection();
        collection.id(id);
        ConflictResolutionPolicy policy = ConflictResolutionPolicy.createCustomPolicy(storedProc);
        collection.setConflictResolutionPolicy(policy);
        return collection;
    }

    public void initialize() throws Exception {
        AsyncDocumentClient createClient = this.clients.get(0);

        Helpers.createDatabaseIfNotExists(createClient, this.databaseName).subscribeOn(schedulerForBlockingWork).block();

        DocumentCollection basic = createCollectionIfNotExists(createClient, this.databaseName, this.basicCollectionName);

        DocumentCollection manualCollection = createCollectionIfNotExists(createClient,
                Helpers.createDatabaseUri(this.databaseName), getCollectionDefForManual(this.manualCollectionName));

        DocumentCollection lwwCollection = createCollectionIfNotExists(createClient,
                Helpers.createDatabaseUri(this.databaseName), getCollectionDefForLastWinWrites(this.lwwCollectionName, "/regionId"));

        DocumentCollection udpCollection = createCollectionIfNotExists(createClient,
                Helpers.createDatabaseUri(this.databaseName), getCollectionDefForCustom(this.udpCollectionName,
                        String.format("dbs/%s/colls/%s/sprocs/%s", this.databaseName, this.udpCollectionName, "resolver")));

        StoredProcedure lwwSproc = new StoredProcedure();
        lwwSproc.id("resolver");
        lwwSproc.setBody(IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("resolver-storedproc.txt"), "UTF-8"));

        lwwSproc =
                getResource(createClient.upsertStoredProcedure(
                        Helpers.createDocumentCollectionUri(this.databaseName, this.udpCollectionName), lwwSproc, null));

    }

    private <T extends Resource> T getResource(Flux<ResourceResponse<T>> obs) {
        return obs.subscribeOn(schedulerForBlockingWork).single().block().getResource();
    }

    public void runManualConflict() throws Exception {
        logger.info("\r\nInsert Conflict\r\n");
        this.runInsertConflictOnManual();

        logger.info("\r\nUPDATE Conflict\r\n");
        this.runUpdateConflictOnManual();

        logger.info("\r\nDELETE Conflict\r\n");
        this.runDeleteConflictOnManual();
    }

    public void runLWWConflict() throws Exception {
        logger.info("\r\nInsert Conflict\r\n");
        this.runInsertConflictOnLWW();

        logger.info("\r\nUPDATE Conflict\r\n");
        this.runUpdateConflictOnLWW();

        logger.info("\r\nDELETE Conflict\r\n");
        this.runDeleteConflictOnLWW();
    }

    public void runUDPConflict() throws Exception {
        logger.info("\r\nInsert Conflict\r\n");
        this.runInsertConflictOnUdp();

        logger.info("\r\nUPDATE Conflict\r\n");
        this.runUpdateConflictOnUdp();

        logger.info("\r\nDELETE Conflict\r\n");
        this.runDeleteConflictOnUdp();
    }

    public void runInsertConflictOnManual() throws Exception {
        do {
            logger.info("1) Performing conflicting insert across {} regions on {}", this.clients.size(), this.manualCollectionName);

            ArrayList<Flux<Document>> insertTask = new ArrayList<>();

            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                insertTask.add(this.tryInsertDocument(client, this.manualCollectionUri, conflictDocument, index++));
            }

            List<Document> conflictDocuments = Flux.merge(insertTask).collectList().subscribeOn(schedulerForBlockingWork).single().block();

            if (conflictDocuments.size() == this.clients.size()) {
                logger.info("2) Caused {} insert conflicts, verifying conflict resolution", conflictDocuments.size());

                for (Document conflictingInsert : conflictDocuments) {
                    this.validateManualConflict(this.clients, conflictingInsert);
                }
                break;
            } else {
                logger.info("Retrying insert to induce conflicts");
            }
        } while (true);
    }

    public void runUpdateConflictOnManual() throws Exception {
        do {
            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());


            conflictDocument = this.tryInsertDocument(clients.get(0), this.manualCollectionUri, conflictDocument, 0)
                    .singleOrEmpty().block();

            TimeUnit.SECONDS.sleep(1);//1 Second for write to sync.


            logger.info("1) Performing conflicting update across 3 regions on {}", this.manualCollectionName);

            ArrayList<Flux<Document>> updateTask = new ArrayList<>();

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                updateTask.add(this.tryUpdateDocument(client, this.manualCollectionUri, conflictDocument, index++));
            }

            List<Document> conflictDocuments = Flux.merge(updateTask).collectList().single().block();

            if (conflictDocuments.size() > 1) {
                logger.info("2) Caused {} updated conflicts, verifying conflict resolution", conflictDocuments.size());

                for (Document conflictingUpdate : conflictDocuments) {
                    this.validateManualConflict(this.clients, conflictingUpdate);
                }
                break;
            } else {
                logger.info("Retrying update to induce conflicts");
            }
        } while (true);
    }

    public void runDeleteConflictOnManual() throws Exception {
        do {
            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            conflictDocument = this.tryInsertDocument(clients.get(0), this.manualCollectionUri, conflictDocument, 0)
                    .singleOrEmpty().block();

            TimeUnit.SECONDS.sleep(10);//1 Second for write to sync.

            logger.info("1) Performing conflicting delete across 3 regions on {}", this.manualCollectionName);

            ArrayList<Flux<Document>> deleteTask = new ArrayList<>();

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                deleteTask.add(this.tryDeleteDocument(client, this.manualCollectionUri, conflictDocument, index++));
            }

            List<Document> conflictDocuments = Flux.merge(deleteTask).collectList()
                    .subscribeOn(schedulerForBlockingWork)
                    .single().block();

            if (conflictDocuments.size() > 1) {
                logger.info("2) Caused {} delete conflicts, verifying conflict resolution", conflictDocuments.size());

                for (Document conflictingDelete : conflictDocuments) {
                    this.validateManualConflict(this.clients, conflictingDelete);
                }

                break;
            } else {
                logger.info("Retrying update to induce conflicts");
            }
        } while (true);
    }

    public void runInsertConflictOnLWW() throws Exception {
        do {
            logger.info("Performing conflicting insert across 3 regions");

            ArrayList<Flux<Document>> insertTask = new ArrayList<>();

            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                insertTask.add(this.tryInsertDocument(client, this.lwwCollectionUri, conflictDocument, index++));
            }

            List<Document> conflictDocuments = Flux.merge(insertTask).collectList().single().block();


            if (conflictDocuments.size() > 1) {
                logger.info("Inserted {} conflicts, verifying conflict resolution", conflictDocuments.size());

                this.validateLWW(this.clients, conflictDocuments);

                break;
            } else {
                logger.info("Retrying insert to induce conflicts");
            }
        } while (true);
    }

    public void runUpdateConflictOnLWW() throws Exception {
        do {
            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            conflictDocument = this.tryInsertDocument(clients.get(0), this.lwwCollectionUri, conflictDocument, 0)
                    .singleOrEmpty().block();


            TimeUnit.SECONDS.sleep(1); //1 Second for write to sync.

            logger.info("1) Performing conflicting update across {} regions on {}", this.clients.size(), this.lwwCollectionUri);

            ArrayList<Flux<Document>> insertTask = new ArrayList<>();

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                insertTask.add(this.tryUpdateDocument(client, this.lwwCollectionUri, conflictDocument, index++));
            }

            List<Document> conflictDocuments = Flux.merge(insertTask).collectList().single().block();


            if (conflictDocuments.size() > 1) {
                logger.info("2) Caused {} update conflicts, verifying conflict resolution", conflictDocuments.size());

                this.validateLWW(this.clients, conflictDocuments);

                break;
            } else {
                logger.info("Retrying insert to induce conflicts");
            }
        } while (true);
    }

    public void runDeleteConflictOnLWW() throws Exception {
        do {
            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            conflictDocument = this.tryInsertDocument(clients.get(0), this.lwwCollectionUri, conflictDocument, 0)
                    .singleOrEmpty().block();


            TimeUnit.SECONDS.sleep(1); //1 Second for write to sync.

            logger.info("1) Performing conflicting delete across {} regions on {}", this.clients.size(), this.lwwCollectionUri);

            ArrayList<Flux<Document>> insertTask = new ArrayList<>();

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                if (index % 2 == 1) {
                    //We delete from region 1, even though region 2 always win.
                    insertTask.add(this.tryDeleteDocument(client, this.lwwCollectionUri, conflictDocument, index++));
                } else {
                    insertTask.add(this.tryUpdateDocument(client, this.lwwCollectionUri, conflictDocument, index++));
                }
            }

            List<Document> conflictDocuments = Flux.merge(insertTask).collectList().single().block();

            if (conflictDocuments.size() > 1) {
                logger.info("Inserted {} conflicts, verifying conflict resolution", conflictDocuments.size());

                //DELETE should always win. irrespective of LWW.
                this.validateLWW(this.clients, conflictDocuments, true);
                break;
            } else {
                logger.info("Retrying update/delete to induce conflicts");
            }
        } while (true);
    }

    public void runInsertConflictOnUdp() throws Exception {
        do {
            logger.info("1) Performing conflicting insert across 3 regions on {}", this.udpCollectionName);

            ArrayList<Flux<Document>> insertTask = new ArrayList<>();

            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                insertTask.add(this.tryInsertDocument(client, this.udpCollectionUri, conflictDocument, index++));
            }

            List<Document> conflictDocuments = Flux.merge(insertTask).collectList().single().block();


            if (conflictDocuments.size() > 1) {
                logger.info("2) Caused {} insert conflicts, verifying conflict resolution", conflictDocuments.size());

                this.validateUDPAsync(this.clients, conflictDocuments);

                break;
            } else {
                logger.info("Retrying insert to induce conflicts");
            }
        } while (true);
    }

    public void runUpdateConflictOnUdp() throws Exception {
        do {
            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            conflictDocument = this.tryInsertDocument(clients.get(0), this.udpCollectionUri, conflictDocument, 0)
                    .singleOrEmpty().block();

            TimeUnit.SECONDS.sleep(1); //1 Second for write to sync.

            logger.info("1) Performing conflicting update across 3 regions on {}", this.udpCollectionUri);

            ArrayList<Flux<Document>> updateTask = new ArrayList<>();

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                updateTask.add(this.tryUpdateDocument(client, this.udpCollectionUri, conflictDocument, index++));
            }

            List<Document> conflictDocuments = Flux.merge(updateTask).collectList().single().block();


            if (conflictDocuments.size() > 1) {
                logger.info("2) Caused {} update conflicts, verifying conflict resolution", conflictDocuments.size());

                this.validateUDPAsync(this.clients, conflictDocuments);

                break;
            } else {
                logger.info("Retrying update to induce conflicts");
            }
        } while (true);
    }

    public void runDeleteConflictOnUdp() throws Exception {
        do {
            Document conflictDocument = new Document();
            conflictDocument.id(UUID.randomUUID().toString());

            conflictDocument = this.tryInsertDocument(clients.get(0), this.udpCollectionUri, conflictDocument, 0)
                    .singleOrEmpty().block();

            TimeUnit.SECONDS.sleep(1); //1 Second for write to sync.

            logger.info("1) Performing conflicting update/delete across 3 regions on {}", this.udpCollectionUri);

            ArrayList<Flux<Document>> deleteTask = new ArrayList<>();

            int index = 0;
            for (AsyncDocumentClient client : this.clients) {
                if (index % 2 == 1) {
                    //We delete from region 1, even though region 2 always win.
                    deleteTask.add(this.tryDeleteDocument(client, this.udpCollectionUri, conflictDocument, index++));
                } else {
                    deleteTask.add(this.tryUpdateDocument(client, this.udpCollectionUri, conflictDocument, index++));
                }
            }

            List<Document> conflictDocuments = Flux.merge(deleteTask).collectList().single().block();

            if (conflictDocuments.size() > 1) {
                logger.info("2) Caused {} delete conflicts, verifying conflict resolution", conflictDocuments.size());

                //DELETE should always win. irrespective of LWW.
                this.validateUDPAsync(this.clients, conflictDocuments, true);
                break;
            } else {
                logger.info("Retrying update/delete to induce conflicts");
            }
        } while (true);
    }

    private Flux<Document> tryInsertDocument(AsyncDocumentClient client, String collectionUri, Document document, int index) {

        logger.debug("region: {}", client.getWriteEndpoint());
        BridgeInternal.setProperty(document, "regionId", index);
        BridgeInternal.setProperty(document, "regionEndpoint", client.getReadEndpoint());
        return client.createDocument(collectionUri, document, null, false)
                .onErrorResume(e -> {
                    if (hasDocumentClientException(e, 409)) {
                        return Flux.empty();
                    } else {
                        return Flux.error(e);
                    }
                }).map(ResourceResponse::getResource);
    }

    private boolean hasDocumentClientException(Throwable e, int statusCode) {
        if (e instanceof CosmosClientException) {
            CosmosClientException dce = (CosmosClientException) e;
            return dce.statusCode() == statusCode;
        }

        return false;
    }

    private boolean hasDocumentClientExceptionCause(Throwable e) {
        while (e != null) {
            if (e instanceof CosmosClientException) {
                return true;
            }

            e = e.getCause();
        }
        return false;
    }

    private boolean hasDocumentClientExceptionCause(Throwable e, int statusCode) {
        while (e != null) {
            if (e instanceof CosmosClientException) {
                CosmosClientException dce = (CosmosClientException) e;
                return dce.statusCode() == statusCode;
            }

            e = e.getCause();
        }

        return false;
    }

    private Flux<Document> tryUpdateDocument(AsyncDocumentClient client, String collectionUri, Document document, int index) {
        BridgeInternal.setProperty(document, "regionId", index);
        BridgeInternal.setProperty(document, "regionEndpoint", client.getReadEndpoint());

        RequestOptions options = new RequestOptions();
        options.setAccessCondition(new AccessCondition());
        options.getAccessCondition().type(AccessConditionType.IF_MATCH);
        options.getAccessCondition().condition(document.etag());


        return client.replaceDocument(document.selfLink(), document, null).onErrorResume(e -> {

            // pre condition failed
            if (hasDocumentClientException(e, 412)) {
                //Lost synchronously or not document yet. No conflict is induced.
                return Flux.empty();

            }
            return Flux.error(e);
        }).map(ResourceResponse::getResource);
    }

    private Flux<Document> tryDeleteDocument(AsyncDocumentClient client, String collectionUri, Document document, int index) {
        BridgeInternal.setProperty(document, "regionId", index);
        BridgeInternal.setProperty(document, "regionEndpoint", client.getReadEndpoint());

        RequestOptions options = new RequestOptions();
        options.setAccessCondition(new AccessCondition());
        options.getAccessCondition().type(AccessConditionType.IF_MATCH);
        options.getAccessCondition().condition(document.etag());


        return client.deleteDocument(document.selfLink(), options).onErrorResume(e -> {

            // pre condition failed
            if (hasDocumentClientException(e, 412)) {
                //Lost synchronously. No conflict is induced.
                return Flux.empty();

            }
            return Flux.error(e);
        }).map(rr -> document);
    }

    private void validateManualConflict(List<AsyncDocumentClient> clients, Document conflictDocument) throws Exception {
        boolean conflictExists = false;
        for (AsyncDocumentClient client : clients) {
            conflictExists = this.validateManualConflict(client, conflictDocument);
        }

        if (conflictExists) {
            this.deleteConflict(conflictDocument);
        }
    }

    private boolean isDelete(Conflict conflict) {
        return StringUtils.equalsIgnoreCase(conflict.getOperationKind(), "delete");
    }


    private boolean equals(String a, String b) {
        return StringUtils.equals(a, b);
    }

    private boolean validateManualConflict(AsyncDocumentClient client, Document conflictDocument) throws Exception {
        while (true) {
            FeedResponse<Conflict> response = client.readConflicts(this.manualCollectionUri, null)
                    .take(1).single().block();

            for (Conflict conflict : response.results()) {
                if (!isDelete(conflict)) {
                    Document conflictDocumentContent = conflict.getResource(Document.class);
                    if (equals(conflictDocument.id(), conflictDocumentContent.id())) {
                        if (equals(conflictDocument.resourceId(), conflictDocumentContent.resourceId()) &&
                                equals(conflictDocument.etag(), conflictDocumentContent.etag())) {
                            logger.info("Document from Region {} lost conflict @ {}",
                                    conflictDocument.id(),
                                    conflictDocument.getInt("regionId"),
                                    client.getReadEndpoint());
                            return true;
                        } else {
                            try {
                                //Checking whether this is the winner.
                                Document winnerDocument = client.readDocument(conflictDocument.selfLink(), null)
                                        .single().block().getResource();
                                logger.info("Document from region {} won the conflict @ {}",
                                        conflictDocument.getInt("regionId"),
                                        client.getReadEndpoint());
                                return false;
                            }
                            catch (Exception exception) {
                                if (hasDocumentClientException(exception, 404)) {
                                    throw exception;
                                } else {
                                    logger.info(
                                            "Document from region {} not found @ {}",
                                            conflictDocument.getInt("regionId"),
                                            client.getReadEndpoint());
                                }
                            }
                        }
                    }
                } else {
                    if (equals(conflict.getSourceResourceId(), conflictDocument.resourceId())) {
                        logger.info("DELETE conflict found @ {}",
                                client.getReadEndpoint());
                        return false;
                    }
                }
            }

            logger.error("Document {} is not found in conflict feed @ {}, retrying",
                    conflictDocument.id(),
                    client.getReadEndpoint());

            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private void deleteConflict(Document conflictDocument) {
        AsyncDocumentClient delClient = clients.get(0);

        FeedResponse<Conflict> conflicts = delClient.readConflicts(this.manualCollectionUri, null).take(1).single().block();

        for (Conflict conflict : conflicts.results()) {
            if (!isDelete(conflict)) {
                Document conflictContent = conflict.getResource(Document.class);
                if (equals(conflictContent.resourceId(), conflictDocument.resourceId())
                        && equals(conflictContent.etag(), conflictDocument.etag())) {
                    logger.info("Deleting manual conflict {} from region {}",
                            conflict.getSourceResourceId(),
                            conflictContent.getInt("regionId"));
                    delClient.deleteConflict(conflict.selfLink(), null)
                            .single().block();

                }
            } else if (equals(conflict.getSourceResourceId(), conflictDocument.resourceId())) {
                logger.info("Deleting manual conflict {} from region {}",
                        conflict.getSourceResourceId(),
                        conflictDocument.getInt("regionId"));
                delClient.deleteConflict(conflict.selfLink(), null)
                        .single().block();
            }
        }
    }

    private void validateLWW(List<AsyncDocumentClient> clients, List<Document> conflictDocument) throws Exception {
        validateLWW(clients, conflictDocument, false);
    }


    private void validateLWW(List<AsyncDocumentClient> clients, List<Document> conflictDocument, boolean hasDeleteConflict) throws Exception {
        for (AsyncDocumentClient client : clients) {
            this.validateLWW(client, conflictDocument, hasDeleteConflict);
        }
    }

    private void validateLWW(AsyncDocumentClient client, List<Document> conflictDocument, boolean hasDeleteConflict) throws Exception {
        FeedResponse<Conflict> response = client.readConflicts(this.lwwCollectionUri, null)
                .take(1).single().block();

        if (response.results().size() != 0) {
            logger.error("Found {} conflicts in the lww collection", response.results().size());
            return;
        }

        if (hasDeleteConflict) {
            do {
                try {
                    client.readDocument(conflictDocument.get(0).selfLink(), null).single().block();

                    logger.error("DELETE conflict for document {} didnt win @ {}",
                            conflictDocument.get(0).id(),
                            client.getReadEndpoint());

                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (Exception exception) {
                    if (!hasDocumentClientExceptionCause(exception)) {
                        throw exception;
                    }

                    // NotFound
                    if (hasDocumentClientExceptionCause(exception, 404)) {

                        logger.info("DELETE conflict won @ {}", client.getReadEndpoint());
                        return;
                    } else {
                        logger.error("DELETE conflict for document {} didnt win @ {}",
                                conflictDocument.get(0).id(),
                                client.getReadEndpoint());

                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                }
            } while (true);
        }

        Document winnerDocument = null;

        for (Document document : conflictDocument) {
            if (winnerDocument == null ||
                    winnerDocument.getInt("regionId") <= document.getInt("regionId")) {
                winnerDocument = document;
            }
        }

        logger.info("Document from region {} should be the winner",
                winnerDocument.getInt("regionId"));

        while (true) {
            try {
                Document existingDocument = client.readDocument(winnerDocument.selfLink(), null)
                        .single().block().getResource();

                if (existingDocument.getInt("regionId") == winnerDocument.getInt("regionId")) {
                    logger.info("Winner document from region {} found at {}",
                            existingDocument.getInt("regionId"),
                            client.getReadEndpoint());
                    break;
                } else {
                    logger.error("Winning document version from region {} is not found @ {}, retrying...",
                            winnerDocument.getInt("regionId"),
                            client.getWriteEndpoint());
                    TimeUnit.MILLISECONDS.sleep(500);
                }
            } catch (Exception e) {
                logger.error("Winner document from region {} is not found @ {}, retrying...",
                        winnerDocument.getInt("regionId"),
                        client.getWriteEndpoint());
                TimeUnit.MILLISECONDS.sleep(500);
            }
        }
    }

    private void validateUDPAsync(List<AsyncDocumentClient> clients, List<Document> conflictDocument) throws Exception {
        validateUDPAsync(clients, conflictDocument, false);
    }

    private void validateUDPAsync(List<AsyncDocumentClient> clients, List<Document> conflictDocument, boolean hasDeleteConflict) throws Exception {
        for (AsyncDocumentClient client : clients) {
            this.validateUDPAsync(client, conflictDocument, hasDeleteConflict);
        }
    }

    private String documentNameLink(String collectionId, String documentId) {
        return String.format("dbs/%s/colls/%s/docs/%s", databaseName, collectionId, documentId);
    }

    private void validateUDPAsync(AsyncDocumentClient client, List<Document> conflictDocument, boolean hasDeleteConflict) throws Exception {
        FeedResponse<Conflict> response = client.readConflicts(this.udpCollectionUri, null).take(1).single().block();

        if (response.results().size() != 0) {
            logger.error("Found {} conflicts in the udp collection", response.results().size());
            return;
        }

        if (hasDeleteConflict) {
            do {
                try {
                    client.readDocument(
                            documentNameLink(udpCollectionName, conflictDocument.get(0).id()), null)
                            .single().block();

                    logger.error("DELETE conflict for document {} didnt win @ {}",
                            conflictDocument.get(0).id(),
                            client.getReadEndpoint());

                    TimeUnit.MILLISECONDS.sleep(500);

                } catch (Exception exception) {
                    if (hasDocumentClientExceptionCause(exception, 404)) {
                        logger.info("DELETE conflict won @ {}", client.getReadEndpoint());
                        return;
                    } else {
                        logger.error("DELETE conflict for document {} didnt win @ {}",
                                conflictDocument.get(0).id(),
                                client.getReadEndpoint());

                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                }
            } while (true);
        }

        Document winnerDocument = null;

        for (Document document : conflictDocument) {
            if (winnerDocument == null ||
                    winnerDocument.getInt("regionId") <= document.getInt("regionId")) {
                winnerDocument = document;
            }
        }

        logger.info("Document from region {} should be the winner",
                winnerDocument.getInt("regionId"));

        while (true) {
            try {

                Document existingDocument = client.readDocument(
                        documentNameLink(udpCollectionName, winnerDocument.id()), null)
                        .single().block().getResource();

                if (existingDocument.getInt("regionId") == winnerDocument.getInt(
                        ("regionId"))) {
                    logger.info("Winner document from region {} found at {}",
                            existingDocument.getInt("regionId"),
                            client.getReadEndpoint());
                    break;
                } else {
                    logger.error("Winning document version from region {} is not found @ {}, retrying...",
                            winnerDocument.getInt("regionId"),
                            client.getWriteEndpoint());
                    TimeUnit.MILLISECONDS.sleep(500);
                }
            } catch (Exception e) {
                logger.error("Winner document from region {} is not found @ {}, retrying...",
                        winnerDocument.getInt("regionId"),
                        client.getWriteEndpoint());
                TimeUnit.MILLISECONDS.sleep(500);
            }
        }
    }

    public void shutdown() {
        this.executor.shutdown();
        for(AsyncDocumentClient client: clients) {
            client.close();
        }
    }
}
