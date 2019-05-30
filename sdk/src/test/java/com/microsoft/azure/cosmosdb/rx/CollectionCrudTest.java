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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.microsoft.azure.cosmosdb.DatabaseForTest;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.CompositePath;
import com.microsoft.azure.cosmosdb.CompositePathSortOrder;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IndexingMode;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SpatialSpec;
import com.microsoft.azure.cosmosdb.SpatialType;

import rx.Observable;

public class CollectionCrudTest extends TestSuiteBase {
    private static final int TIMEOUT = 50000;
    private static final int SETUP_TIMEOUT = 20000;
    private static final int SHUTDOWN_TIMEOUT = 20000;
    private final String databaseId = DatabaseForTest.generateId();

    private AsyncDocumentClient client;
    private Database database;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public CollectionCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "collectionCrudArgProvider")
    public Object[][] collectionCrudArgProvider() {
        return new Object[][] {
                // collection name, is name base
                {UUID.randomUUID().toString(), false } ,
                {UUID.randomUUID().toString(), true  } ,

                // with special characters in the name.
                {"+ -_,:.|~" + UUID.randomUUID().toString() + " +-_,:.|~", true  } ,
        };
    }

    private DocumentCollection getCollectionDefinition(String collectionName) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(collectionName);
        collectionDefinition.setPartitionKey(partitionKeyDef);

        return collectionDefinition;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void createCollection(String collectionName, boolean isNameBased) {
        DocumentCollection collectionDefinition = getCollectionDefinition(collectionName);
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client
                .createCollection(getDatabaseLink(database, isNameBased), collectionDefinition, null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .withId(collectionDefinition.getId()).build();
        
        validateSuccess(createObservable, validator);
        safeDeleteAllCollections(client, database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createCollectionWithCompositeIndexAndSpatialSpec() {
        DocumentCollection collection = new DocumentCollection();

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        CompositePath compositePath1 = new CompositePath();
        compositePath1.setPath("/path1");
        compositePath1.setOrder(CompositePathSortOrder.Ascending);
        CompositePath compositePath2 = new CompositePath();
        compositePath2.setPath("/path2");
        compositePath2.setOrder(CompositePathSortOrder.Descending);
        CompositePath compositePath3 = new CompositePath();
        compositePath3.setPath("/path3");
        CompositePath compositePath4 = new CompositePath();
        compositePath4.setPath("/path4");
        compositePath4.setOrder(CompositePathSortOrder.Ascending);
        CompositePath compositePath5 = new CompositePath();
        compositePath5.setPath("/path5");
        compositePath5.setOrder(CompositePathSortOrder.Descending);
        CompositePath compositePath6 = new CompositePath();
        compositePath6.setPath("/path6");
        
        ArrayList<CompositePath> compositeIndex1 = new ArrayList<CompositePath>();
        compositeIndex1.add(compositePath1);
        compositeIndex1.add(compositePath2);
        compositeIndex1.add(compositePath3);
        
        ArrayList<CompositePath> compositeIndex2 = new ArrayList<CompositePath>();
        compositeIndex2.add(compositePath4);
        compositeIndex2.add(compositePath5);
        compositeIndex2.add(compositePath6);
        
        Collection<ArrayList<CompositePath>> compositeIndexes = new ArrayList<ArrayList<CompositePath>>();
        compositeIndexes.add(compositeIndex1);
        compositeIndexes.add(compositeIndex2);
        indexingPolicy.setCompositeIndexes(compositeIndexes);

        SpatialType[] spatialTypes = new SpatialType[] {
                SpatialType.Point,
                SpatialType.LineString,
                SpatialType.Polygon,
                SpatialType.MultiPolygon
                };
        Collection<SpatialSpec> spatialIndexes = new ArrayList<SpatialSpec>();
        for (int index = 0; index < 2; index++) {
            Collection<SpatialType> collectionOfSpatialTypes = new ArrayList<SpatialType>();

            SpatialSpec spec = new SpatialSpec();
            spec.setPath("/path" + index + "/*");

            for (int i = index; i < index + 3; i++) {
                collectionOfSpatialTypes.add(spatialTypes[i]);
            }
            spec.setSpatialTypes(collectionOfSpatialTypes);
            spatialIndexes.add(spec);
        }
        
        indexingPolicy.setSpatialIndexes(spatialIndexes);

        collection.setId(UUID.randomUUID().toString());
        collection.setIndexingPolicy(indexingPolicy);
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client
                .createCollection(database.getSelfLink(), collection, null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .withId(collection.getId())
                .withCompositeIndexes(compositeIndexes)
                .withSpatialIndexes(spatialIndexes)
                .build();
        
        validateSuccess(createObservable, validator);
        safeDeleteAllCollections(client, database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void readCollection(String collectionName, boolean isNameBased) {
        DocumentCollection collectionDefinition = getCollectionDefinition(collectionName);
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(getDatabaseLink(database, isNameBased), collectionDefinition,
                null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();

        Observable<ResourceResponse<DocumentCollection>> readObservable = client.readCollection(getCollectionLink(database, collection, isNameBased), null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(client, database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void readCollection_NameBase(String collectionName, boolean isNameBased) {
        DocumentCollection collectionDefinition = getCollectionDefinition(collectionName);
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(getDatabaseLink(database, isNameBased), collectionDefinition,
                null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();
        
        Observable<ResourceResponse<DocumentCollection>> readObservable = client.readCollection(
                getCollectionLink(database, collection, isNameBased), null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .withId(collection.getId()).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(client, database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void readCollection_DoesntExist(String collectionName, boolean isNameBased) throws Exception {

        Observable<ResourceResponse<DocumentCollection>> readObservable = client
                .readCollection(Utils.getCollectionNameLink(database.getId(), "I don't exist"), null);

        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void deleteCollection(String collectionName, boolean isNameBased) {
        DocumentCollection collectionDefinition = getCollectionDefinition(collectionName);
        
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(getDatabaseLink(database, isNameBased), collectionDefinition, null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();

        Observable<ResourceResponse<DocumentCollection>> deleteObservable = client.deleteCollection(getCollectionLink(database, collection, isNameBased),
                null);

        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, dataProvider = "collectionCrudArgProvider")
    public void replaceCollection(String collectionName, boolean isNameBased)  {
        // create a collection
        DocumentCollection collectionDefinition = getCollectionDefinition(collectionName);
        Observable<ResourceResponse<DocumentCollection>> createObservable = client.createCollection(getDatabaseLink(database, isNameBased), collectionDefinition, null);
        DocumentCollection collection = createObservable.toBlocking().single().getResource();
        // sanity check
        assertThat(collection.getIndexingPolicy().getIndexingMode()).isEqualTo(IndexingMode.Consistent);
        
        // replace indexing mode
        IndexingPolicy indexingMode = new IndexingPolicy();
        indexingMode.setIndexingMode(IndexingMode.Lazy);
        collection.setIndexingPolicy(indexingMode);
        Observable<ResourceResponse<DocumentCollection>> readObservable = client.replaceCollection(collection, null);
        
        // validate
        ResourceResponseValidator<DocumentCollection> validator = new ResourceResponseValidator.Builder<DocumentCollection>()
                .indexingMode(IndexingMode.Lazy).build();
        validateSuccess(readObservable, validator);
        safeDeleteAllCollections(client, database);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void sessionTokenConsistencyCollectionDeleteCreateSameName() {
        AsyncDocumentClient client1 = clientBuilder.build();
        AsyncDocumentClient client2 = clientBuilder.build();
        try {
            String dbId = database.getId();
            String collectionId = "coll";

            DocumentCollection collectionDefinition = new DocumentCollection();
            collectionDefinition.setId(collectionId);
            DocumentCollection collection = createCollection(client1, dbId, collectionDefinition);

            Document document = new Document();
            document.setId("doc");
            document.set("name", "New Document");
            createDocument(client1, dbId, collectionId, document);
            ResourceResponse<Document> readDocumentResponse = client1.readDocument(Utils.getDocumentNameLink(dbId, collectionId, document.getId()), null).toBlocking().single();
            logger.info("Client 1 Read Document Client Side Request Statistics {}", readDocumentResponse.getRequestDiagnosticsString());
            logger.info("Client 1 Read Document Latency {}", readDocumentResponse.getRequestLatency());

            document.set("name", "New Updated Document");
            ResourceResponse<Document> upsertDocumentResponse = client1.upsertDocument(collection.getSelfLink(), document, null,
                    true).toBlocking().single();
            logger.info("Client 1 Upsert Document Client Side Request Statistics {}", upsertDocumentResponse.getRequestDiagnosticsString());
            logger.info("Client 1 Upsert Document Latency {}", upsertDocumentResponse.getRequestLatency());

            //  Delete the existing collection
            deleteCollection(client2, Utils.getCollectionNameLink(dbId, collectionId));
            //  Recreate the collection with the same name but with different client
            createCollection(client2, dbId, collectionDefinition);

            Document newDocument = new Document();
            newDocument.setId("doc");
            newDocument.set("name", "New Created Document");
            createDocument(client2, dbId, collectionId, newDocument);

            readDocumentResponse = client1.readDocument(Utils.getDocumentNameLink(dbId, collectionId, newDocument.getId()), null).toBlocking().single();
            logger.info("Client 2 Read Document Client Side Request Statistics {}", readDocumentResponse.getRequestDiagnosticsString());
            logger.info("Client 2 Read Document Latency {}", readDocumentResponse.getRequestLatency());

            Document readDocument = readDocumentResponse.getResource();

            assertThat(readDocument.getId().equals(newDocument.getId())).isTrue();
            assertThat(readDocument.get("name").equals(newDocument.get("name"))).isTrue();
        } finally {
            safeClose(client1);
            safeClose(client2);
        }
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        database = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, databaseId);
        safeClose(client);
    }

    private static String getDatabaseLink(Database db, boolean isNameLink) {
        return isNameLink ? "dbs/" + db.getId() : db.getSelfLink();
    }

    private static String getCollectionLink(Database db, DocumentCollection documentCollection, boolean isNameLink) {
        return isNameLink ? "dbs/" + db.getId() + "/colls/" + documentCollection.getId() : documentCollection.getSelfLink();
    }
}
