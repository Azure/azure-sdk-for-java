/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.azure.documentdb.Attachment;
import com.microsoft.azure.documentdb.Conflict;
import com.microsoft.azure.documentdb.ConnectionMode;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DatabaseAccount;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.FeedResponsePage;
import com.microsoft.azure.documentdb.MediaOptions;
import com.microsoft.azure.documentdb.MediaResponse;
import com.microsoft.azure.documentdb.Offer;
import com.microsoft.azure.documentdb.Permission;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.SqlQuerySpec;
import com.microsoft.azure.documentdb.StoredProcedure;
import com.microsoft.azure.documentdb.StoredProcedureResponse;
import com.microsoft.azure.documentdb.Trigger;
import com.microsoft.azure.documentdb.User;
import com.microsoft.azure.documentdb.UserDefinedFunction;
import com.microsoft.azure.documentdb.rx.internal.RxDocumentClientImpl;
import com.microsoft.azure.documentdb.rx.internal.RxWrapperDocumentClientImpl;

import rx.Observable;

/**
 * Provides a client-side logical representation of the Azure DocumentDB
 * database service. This async client is used to configure and execute requests
 * against the service.
 * 
 * <p>
 * {@link AsyncDocumentClient} async APIs return <a href="https://github.com/ReactiveX/RxJava">rxJava</a>'s {@code
 * Observable}, and so you can use rxJava {@link Observable} functionalities.
 * <STRONG>The async {@link Observable} based APIs perform the requested operation only after
 * subscription.</STRONG>
 * 
 * <p>
 * The service client encapsulates the endpoint and credentials used to access
 * the DocumentDB service.
 * 
 * To instantiate you can use the {@link Builder}
 * <pre>
 * {@code
 *   AsyncDocumentClient client = new AsyncDocumentClient.Builder()
 *           .withServiceEndpoint(serviceEndpoint)
 *           .withMasterKey(masterKey)
 *           .withConnectionPolicy(ConnectionPolicy.GetDefault())
 *           .withConsistencyLevel(ConsistencyLevel.Session)
 *           .build();
 * }
 * </pre>
 */
public interface AsyncDocumentClient {

    /**
     * Helper class to build {@link AsyncDocumentClient} instances
     * as logical representation of the Azure DocumentDB database service.
     * 
     * <pre>
     * {@code
     *   AsyncDocumentClient client = new AsyncDocumentClient.Builder()
     *           .withServiceEndpoint(serviceEndpoint)
     *           .withMasterKey(masterKey)
     *           .withConnectionPolicy(ConnectionPolicy.GetDefault())
     *           .withConsistencyLevel(ConsistencyLevel.Session)
     *           .build();
     * }
     * </pre>
     */
    public static class Builder {

        private String masterKey;
        private ConnectionPolicy connectionPolicy;
        private ConsistencyLevel desiredConsistencyLevel;
        private URI serviceEndpoint;
        private int eventLoopSize = -1;
        private int separateComputationPoolSize = -1;

        public Builder withServiceEndpoint(String serviceEndpoint) {
            try {
                this.serviceEndpoint = new URI(serviceEndpoint);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            return this;
        }

        public Builder withMasterKey(String masterKey) {
            this.masterKey = masterKey;
            return this;
        }

        public Builder withConsistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
            this.desiredConsistencyLevel = desiredConsistencyLevel;
            return this;
        }
        
        /**
         * NOTE: This is experimental.
         * If sets, modifies the event loop size and the computation pool size.
         * 
         * As a rule of thumb (eventLoopSize + separateComputationPoolSize) ~ #
         * CPU cores. If you have 8 CPU cores, eventLoopSize = 5, and
         * separateComputationPoolSize = 3 is a logical choice for better throughput.
         * 
         * Computation intensive work, e.g., authentication token generation, is
         * performed on the computation scheduler. If
         * separateComputationPoolSize is set to 0, computation will take place
         * on the observable subscription thread.
         * 
         * @param eventLoopSize the size of the event loop (the number of event loop threads).
         * @param separateComputationPoolSize the size the thread pool backing computation scheduler up.
         * @return Builder
         */
        Builder withWorkers(int eventLoopSize, int separateComputationPoolSize) {
            ifThrowIllegalArgException(eventLoopSize <= 0, "invalid event loop size");
            ifThrowIllegalArgException(separateComputationPoolSize < 0, "invalid computation scheduler pool size");
            this.eventLoopSize = eventLoopSize;
            this.separateComputationPoolSize = separateComputationPoolSize;
            return this;
        }

        public Builder withConnectionPolicy(ConnectionPolicy connectionPolicy) {
            this.connectionPolicy = connectionPolicy;
            return this;
        }
        
        private void ifThrowIllegalArgException(boolean value, String error) {
            if (value) {
                throw new IllegalArgumentException(error);
            }
        }
        
        public AsyncDocumentClient build() {
            
            ifThrowIllegalArgException(this.serviceEndpoint == null, "cannot build client without service endpoint");
            ifThrowIllegalArgException(this.masterKey == null, "cannot build client without masterKey");

            if (connectionPolicy != null && ConnectionMode.DirectHttps != connectionPolicy.getConnectionMode()) {
                return new RxDocumentClientImpl(serviceEndpoint, masterKey, connectionPolicy, desiredConsistencyLevel,
                        eventLoopSize, separateComputationPoolSize);
            } else {
                
                ifThrowIllegalArgException(this.eventLoopSize != -1, "eventLoopSize is not applicable in direct mode");
                ifThrowIllegalArgException(this.separateComputationPoolSize != -1, "separateComputationPoolSize is not applicable in direct mode");
                
                // fall back to RX wrapper with blocking IO
                return new RxWrapperDocumentClientImpl(
                        new DocumentClient(serviceEndpoint.toString(), masterKey, connectionPolicy, desiredConsistencyLevel));
            }
        }
    }

    /**
     * Gets the default service endpoint as passed in by the user during construction.
     *
     * @return the service endpoint URI
     */
    URI getServiceEndpoint();

    /**
     * Gets the current write endpoint chosen based on availability and preference.
     *
     * @return the write endpoint URI
     */
    URI getWriteEndpoint();

    /**
     * Gets the current read endpoint chosen based on availability and preference.
     *
     * @return the read endpoint URI
     */
    URI getReadEndpoint();

    /**
     * Gets the connection policy
     * 
     * @return the connection policy
     */
    ConnectionPolicy getConnectionPolicy();

    /**
     * Creates a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created database.
     * In case of failure the {@link Observable} will error.
     * 
     * @param database the database.
     * @param options  the request options.
     * @return an {@link Observable} containing the single resource response with the created database or an error.
     */
    Observable<ResourceResponse<Database>> createDatabase(Database database, RequestOptions options);

    /**
     * Deletes a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the deleted database.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the deleted database or an error.
     */
    Observable<ResourceResponse<Database>> deleteDatabase(String databaseLink, RequestOptions options);

    /**
     * Reads a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read database.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the read database or an error.
     */
    Observable<ResourceResponse<Database>> readDatabase(String databaseLink, RequestOptions options);

    /**
     * Reads all databases.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the read databases.
     * In case of failure the {@link Observable} will error.
     * 
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of read databases or an error.
     */
    Observable<FeedResponsePage<Database>> readDatabases(FeedOptions options);

    /**
     * Query for databases.
     * 
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the read databases.
     * In case of failure the {@link Observable} will error.
     * 
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of read databases or an error.
     */
    Observable<FeedResponsePage<Database>> queryDatabases(String query, FeedOptions options);

    /**
     * Query for databases.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the obtained databases.
     * In case of failure the {@link Observable} will error.
     * 
     * @param querySpec the SQL query specification.
     * @param options   the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained databases or an error.
     */
    Observable<FeedResponsePage<Database>> queryDatabases(SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created collection.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param collection   the collection.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the created collection or an error.
     */
    Observable<ResourceResponse<DocumentCollection>> createCollection(String databaseLink, DocumentCollection collection,
            RequestOptions options);

    /**
     * Replaces a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced document collection.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collection the document collection to use.
     * @param options    the request options.
     * @return an {@link Observable} containing the single resource response with the replaced document collection or an error.
     */
    Observable<ResourceResponse<DocumentCollection>> replaceCollection(DocumentCollection collection, RequestOptions options);

    /**
     * Deletes a document collection by the collection link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted database.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response for the deleted database or an error.

     */
    Observable<ResourceResponse<DocumentCollection>> deleteCollection(String collectionLink, RequestOptions options);

    /**
     * Reads a document collection by the collection link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read collection.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response with the read collection or an error.
     */
    Observable<ResourceResponse<DocumentCollection>> readCollection(String collectionLink, RequestOptions options);

    /**
     * Reads all document collections in a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the read collections.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param options      the fee options.
     * @return the feed response with the read collections.
     * @return an {@link Observable} containing one or several feed response pages of the read collections or an error.
     */
    Observable<FeedResponsePage<DocumentCollection>> readCollections(String databaseLink, FeedOptions options);

    /**
     * Query for document collections in a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the obtained collections.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained collections or an error.
     */
    Observable<FeedResponsePage<DocumentCollection>> queryCollections(String databaseLink, String query, FeedOptions options);

    /**
     * Query for document collections in a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the obtained collections.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained collections or an error.
     */
    Observable<FeedResponsePage<DocumentCollection>> queryCollections(String databaseLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a document.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created document.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink               the link to the parent document collection.
     * @param document                     the document represented as a POJO or Document object.
     * @param options                      the request options.
     * @param disableAutomaticIdGeneration the flag for disabling automatic id generation.
     * @return an {@link Observable} containing the single resource response with the created document or an error.

     */
    Observable<ResourceResponse<Document>> createDocument(String collectionLink, Object document, RequestOptions options,
            boolean disableAutomaticIdGeneration);

    /**
     * Upserts a document.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted document.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink               the link to the parent document collection.
     * @param document                     the document represented as a POJO or Document object to upsert.
     * @param options                      the request options.
     * @param disableAutomaticIdGeneration the flag for disabling automatic id generation.
     * @return an {@link Observable} containing the single resource response with the upserted document or an error.
     */
    Observable<ResourceResponse<Document>> upsertDocument(String collectionLink, Object document, RequestOptions options,
            boolean disableAutomaticIdGeneration);

    /**
     * Replaces a document using a POJO object.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced document.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param document     the document represented as a POJO or Document object.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the replaced document or an error.
     */
    Observable<ResourceResponse<Document>> replaceDocument(String documentLink, Object document, RequestOptions options);

    /**
     * Replaces a document with the passed in document.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced document.
     * In case of failure the {@link Observable} will error.
     * 
     * @param document the document to replace (containing the document id).
     * @param options  the request options.
     * @return an {@link Observable} containing the single resource response with the replaced document or an error.
     */
    Observable<ResourceResponse<Document>> replaceDocument(Document document, RequestOptions options);

    /**
     * Deletes a document by the document link. 
     * 
     * After subscription the operation will be performed.
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted document.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response for the deleted document or an error.
     */
    Observable<ResourceResponse<Document>> deleteDocument(String documentLink, RequestOptions options);

    /**
     * Reads a document by the document link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read document.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the read document or an error.
     */
    Observable<ResourceResponse<Document>> readDocument(String documentLink, RequestOptions options);

    /**
     * Reads all documents in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the read documents.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read documents or an error.
     */
    Observable<FeedResponsePage<Document>> readDocuments(String collectionLink, FeedOptions options);


    /**
     * Query for documents in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the link to the parent document collection.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained document or an error.
     */
    Observable<FeedResponsePage<Document>> queryDocuments(String collectionLink, String query, FeedOptions options);

    /**
     * Query for documents in a document collection with a partitionKey
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the link to the parent document collection.
     * @param query          the query.
     * @param options        the feed options.
     * @param partitionKey   the partitionKey.
     * @return an {@link Observable} containing one or several feed response pages of the obtained documents or an error.
     */
    Observable<FeedResponsePage<Document>> queryDocuments(String collectionLink, String query, FeedOptions options,
            Object partitionKey);

    /**
     * Query for documents in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the link to the parent document collection.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained documents or an error.
     */
    Observable<FeedResponsePage<Document>> queryDocuments(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Query for documents in a document collection.
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the link to the parent document collection.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @param partitionKey   the partitionKey.
     * @return an {@link Observable} containing one or several feed response pages of the obtained documents or an error.
     */
    Observable<FeedResponsePage<Document>> queryDocuments(String collectionLink, SqlQuerySpec querySpec, FeedOptions options,
            Object partitionKey);

    /**
     * Creates a stored procedure.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created stored procedure.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink  the collection link.
     * @param storedProcedure the stored procedure to create.
     * @param options         the request options.
     * @return an {@link Observable} containing the single resource response with the created stored procedure or an error.
     */
    Observable<ResourceResponse<StoredProcedure>> createStoredProcedure(String collectionLink, StoredProcedure storedProcedure,
            RequestOptions options);

    /**
     * Upserts a stored procedure.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted stored procedure.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink  the collection link.
     * @param storedProcedure the stored procedure to upsert.
     * @param options         the request options.
     * @return an {@link Observable} containing the single resource response with the upserted stored procedure or an error.
     */
    Observable<ResourceResponse<StoredProcedure>> upsertStoredProcedure(String collectionLink, StoredProcedure storedProcedure,
            RequestOptions options);

    /**
     * Replaces a stored procedure.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced stored procedure.
     * In case of failure the {@link Observable} will error.
     * 
     * @param storedProcedure the stored procedure to use.
     * @param options         the request options.
     * @return an {@link Observable} containing the single resource response with the replaced stored procedure or an error.
     */
    Observable<ResourceResponse<StoredProcedure>> replaceStoredProcedure(StoredProcedure storedProcedure, RequestOptions options);

    /**
     * Deletes a stored procedure by the stored procedure link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted stored procedure.
     * In case of failure the {@link Observable} will error.
     * 
     * @param storedProcedureLink the stored procedure link.
     * @param options             the request options.
     * @return an {@link Observable} containing the single resource response for the deleted stored procedure or an error.
     */
    Observable<ResourceResponse<StoredProcedure>> deleteStoredProcedure(String storedProcedureLink, RequestOptions options);

    /**
     * Read a stored procedure by the stored procedure link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read stored procedure.
     * In case of failure the {@link Observable} will error.
     * 
     * @param storedProcedureLink the stored procedure link.
     * @param options             the request options.
     * @return an {@link Observable} containing the single resource response with the read stored procedure or an error.
     */
    Observable<ResourceResponse<StoredProcedure>> readStoredProcedure(String storedProcedureLink, RequestOptions options);

    /**
     * Reads all stored procedures in a document collection link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read stored procedures.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read stored procedures or an error.
     */
    Observable<FeedResponsePage<StoredProcedure>> readStoredProcedures(String collectionLink, FeedOptions options);

    /**
     * Query for stored procedures in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained stored procedures or an error.
     */
    Observable<FeedResponsePage<StoredProcedure>> queryStoredProcedures(String collectionLink, String query, FeedOptions options);

    /**
     * Query for stored procedures in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained stored procedures or an error.
     */
    Observable<FeedResponsePage<StoredProcedure>> queryStoredProcedures(String collectionLink, SqlQuerySpec querySpec,
            FeedOptions options);

    /**
     * Executes a stored procedure by the stored procedure link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the stored procedure response.
     * In case of failure the {@link Observable} will error.
     * 
     * @param storedProcedureLink the stored procedure link.
     * @param procedureParams     the array of procedure parameter values.
     * @return an {@link Observable} containing the single resource response with the stored procedure response or an error.
     */
    Observable<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink, Object[] procedureParams);

    /**
     * Executes a stored procedure by the stored procedure link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the stored procedure response.
     * In case of failure the {@link Observable} will error.
     * 
     * @param storedProcedureLink the stored procedure link.
     * @param options             the request options.
     * @param procedureParams     the array of procedure parameter values.
     * @return an {@link Observable} containing the single resource response with the stored procedure response or an error.
     */
    Observable<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink, RequestOptions options,
            Object[] procedureParams);

    /**
     * Creates a trigger.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created trigger.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param trigger        the trigger.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response with the created trigger or an error.
     */
    Observable<ResourceResponse<Trigger>> createTrigger(String collectionLink, Trigger trigger, RequestOptions options);

    /**
     * Upserts a trigger.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted trigger.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param trigger        the trigger to upsert.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response with the upserted trigger or an error.
     */
    Observable<ResourceResponse<Trigger>> upsertTrigger(String collectionLink, Trigger trigger, RequestOptions options);

    /**
     * Replaces a trigger.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced trigger.
     * In case of failure the {@link Observable} will error.
     * 
     * @param trigger the trigger to use.
     * @param options the request options.
     * @return an {@link Observable} containing the single resource response with the replaced trigger or an error.
     */
    Observable<ResourceResponse<Trigger>> replaceTrigger(Trigger trigger, RequestOptions options);

    /**
     * Deletes a trigger.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted trigger.
     * In case of failure the {@link Observable} will error.
     * 
     * @param triggerLink the trigger link.
     * @param options     the request options.
     * @return an {@link Observable} containing the single resource response for the deleted trigger or an error.
     */
    Observable<ResourceResponse<Trigger>> deleteTrigger(String triggerLink, RequestOptions options);

    /**
     * Reads a trigger by the trigger link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the read trigger.
     * In case of failure the {@link Observable} will error.
     * 
     * @param triggerLink the trigger link.
     * @param options     the request options.
     * @return an {@link Observable} containing the single resource response for the read trigger or an error.
     */
    Observable<ResourceResponse<Trigger>> readTrigger(String triggerLink, RequestOptions options);

    /**
     * Reads all triggers in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read triggers.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read triggers or an error.
     */
    Observable<FeedResponsePage<Trigger>> readTriggers(String collectionLink, FeedOptions options);

    /**
     * Query for triggers.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained triggers or an error.
     */
    Observable<FeedResponsePage<Trigger>> queryTriggers(String collectionLink, String query, FeedOptions options);

    /**
     * Query for triggers.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained triggers or an error.
     */
    Observable<FeedResponsePage<Trigger>> queryTriggers(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a user defined function.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created user defined function.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param udf            the user defined function.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response with the created user defined function or an error.
     */
    Observable<ResourceResponse<UserDefinedFunction>> createUserDefinedFunction(String collectionLink, UserDefinedFunction udf,
            RequestOptions options);

    /**
     * Upserts a user defined function.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted user defined function.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param udf            the user defined function to upsert.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response with the upserted user defined function or an error.
     */
    Observable<ResourceResponse<UserDefinedFunction>> upsertUserDefinedFunction(String collectionLink, UserDefinedFunction udf,
            RequestOptions options);

    /**
     * Replaces a user defined function.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced user defined function.
     * In case of failure the {@link Observable} will error.
     * 
     * @param udf     the user defined function.
     * @param options the request options.
     * @return an {@link Observable} containing the single resource response with the replaced user defined function or an error.
     */
    Observable<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunction(UserDefinedFunction udf, RequestOptions options);

    /**
     * Deletes a user defined function.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted user defined function.
     * In case of failure the {@link Observable} will error.
     * 
     * @param udfLink the user defined function link.
     * @param options the request options.
     * @return an {@link Observable} containing the single resource response for the deleted user defined function or an error.
     */
    Observable<ResourceResponse<UserDefinedFunction>> deleteUserDefinedFunction(String udfLink, RequestOptions options);

    /**
     * Read a user defined function.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the read user defined function.
     * In case of failure the {@link Observable} will error.
     * 
     * @param udfLink the user defined function link.
     * @param options the request options.
     * @return an {@link Observable} containing the single resource response for the read user defined function or an error.
     */
    Observable<ResourceResponse<UserDefinedFunction>> readUserDefinedFunction(String udfLink, RequestOptions options);

    /**
     * Reads all user defined functions in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read user defined functions.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read user defined functions or an error.
     */
    Observable<FeedResponsePage<UserDefinedFunction>> readUserDefinedFunctions(String collectionLink, FeedOptions options);

    /**
     * Query for user defined functions.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    Observable<FeedResponsePage<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink, String query,
            FeedOptions options);

    /**
     * Query for user defined functions.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    Observable<FeedResponsePage<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink, SqlQuerySpec querySpec,
            FeedOptions options);

    /**
     * Creates an attachment.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created attachment.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param attachment   the attachment to create.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the created attachment or an error.
     */
    Observable<ResourceResponse<Attachment>> createAttachment(String documentLink, Attachment attachment, RequestOptions options);

    /**
     * Upserts an attachment.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted attachment.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param attachment   the attachment to upsert.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the upserted attachment or an error.
     */
    Observable<ResourceResponse<Attachment>> upsertAttachment(String documentLink, Attachment attachment, RequestOptions options);

    /**
     * Replaces an attachment.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced attachment.
     * In case of failure the {@link Observable} will error.
     * 
     * @param attachment the attachment to use.
     * @param options    the request options.
     * @return an {@link Observable} containing the single resource response with the replaced attachment or an error.
     */
    Observable<ResourceResponse<Attachment>> replaceAttachment(Attachment attachment, RequestOptions options);

    /**
     * Deletes an attachment.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted attachment.
     * In case of failure the {@link Observable} will error.
     * 
     * @param attachmentLink the attachment link.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response for the deleted attachment or an error.
     */
    Observable<ResourceResponse<Attachment>> deleteAttachment(String attachmentLink, RequestOptions options);

    /**
     * Reads an attachment.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read attachment.
     * In case of failure the {@link Observable} will error.
     * 
     * @param attachmentLink the attachment link.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response with the read attachment or an error.
     */
    Observable<ResourceResponse<Attachment>> readAttachment(String attachmentLink, RequestOptions options);

    /**
     * Reads all attachments in a document.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read attachments.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read attachments or an error.
     */
    Observable<FeedResponsePage<Attachment>> readAttachments(String documentLink, FeedOptions options);

    /**
     * Query for attachments.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained attachments.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained attachments or an error.
     */
    Observable<FeedResponsePage<Attachment>> queryAttachments(String documentLink, String query, FeedOptions options);

    /**
     * Query for attachments.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained attachments.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained attachments or an error.
     */
    Observable<FeedResponsePage<Attachment>> queryAttachments(String documentLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates an attachment.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created attachment.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param mediaStream  the media stream for creating the attachment.
     * @param options      the media options.
     * @return an {@link Observable} containing the single resource response with the created attachment or an error.
     */
    Observable<ResourceResponse<Attachment>> createAttachment(String documentLink, InputStream mediaStream, MediaOptions options);

    /**
     * Upserts an attachment to the media stream
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted attachment.
     * In case of failure the {@link Observable} will error.
     * 
     * @param documentLink the document link.
     * @param mediaStream  the media stream for upserting the attachment.
     * @param options      the media options.
     * @return an {@link Observable} containing the single resource response with the upserted attachment or an error.
     */
    Observable<ResourceResponse<Attachment>> upsertAttachment(String documentLink, InputStream mediaStream, MediaOptions options);

    /**
     * Reads a media by the media link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single media response.
     * In case of failure the {@link Observable} will error.
     * 
     * @param mediaLink the media link.
     * @return an {@link Observable} containing the single meadia response or an error.
     */
    Observable<MediaResponse> readMedia(String mediaLink);

    /**
     * Updates a media by the media link.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single media response.
     * In case of failure the {@link Observable} will error.
     * 
     * @param mediaLink   the media link.
     * @param mediaStream the media stream to upload.
     * @param options     the media options.
     * @return an {@link Observable} containing the single meadia response or an error.
     */
    Observable<MediaResponse> updateMedia(String mediaLink, InputStream mediaStream, MediaOptions options);

    /**
     * Reads a conflict.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read conflict.
     * In case of failure the {@link Observable} will error.
     * 
     * @param conflictLink the conflict link.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the read conflict or an error.
     */
    Observable<ResourceResponse<Conflict>> readConflict(String conflictLink, RequestOptions options);

    /**
     * Reads all conflicts in a document collection.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read conflicts.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read conflicts or an error.
     */
    Observable<FeedResponsePage<Conflict>> readConflicts(String collectionLink, FeedOptions options);

    /**
     * Query for conflicts.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained conflicts.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained conflicts or an error.
     */
    Observable<FeedResponsePage<Conflict>> queryConflicts(String collectionLink, String query, FeedOptions options);

    /**
     * Query for conflicts.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained conflicts.
     * In case of failure the {@link Observable} will error.
     * 
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained conflicts or an error.
     */
    Observable<FeedResponsePage<Conflict>> queryConflicts(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Deletes a conflict.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted conflict.
     * In case of failure the {@link Observable} will error.
     * 
     * @param conflictLink the conflict link.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response for the deleted conflict or an error.
     */
    Observable<ResourceResponse<Conflict>> deleteConflict(String conflictLink, RequestOptions options);

    /**
     * Creates a user.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created user.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param user         the user to create.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the created user or an error.
     */
    Observable<ResourceResponse<User>> createUser(String databaseLink, User user, RequestOptions options);

    /**
     * Upserts a user.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted user.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param user         the user to upsert.
     * @param options      the request options.
     * @return an {@link Observable} containing the single resource response with the upserted user or an error.
     */
    Observable<ResourceResponse<User>> upsertUser(String databaseLink, User user, RequestOptions options);

    /**
     * Replaces a user.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced user.
     * In case of failure the {@link Observable} will error.
     * 
     * @param user    the user to use.
     * @param options the request options.
     * @return an {@link Observable} containing the single resource response with the replaced user or an error.
     */
    Observable<ResourceResponse<User>> replaceUser(User user, RequestOptions options);

    /**
     * Deletes a user.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted user.
     * In case of failure the {@link Observable} will error.
     * 
     * @param userLink the user link.
     * @param options  the request options.
     * @return an {@link Observable} containing the single resource response for the deleted user or an error.
     */
    Observable<ResourceResponse<User>> deleteUser(String userLink, RequestOptions options);

    /**
     * Reads a user.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read user.
     * In case of failure the {@link Observable} will error.
     * 
     * @param userLink the user link.
     * @param options  the request options.
     * @return an {@link Observable} containing the single resource response with the read user or an error.
     */
    Observable<ResourceResponse<User>> readUser(String userLink, RequestOptions options);

    /**
     * Reads all users in a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read users.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read users or an error.
     */
    Observable<FeedResponsePage<User>> readUsers(String databaseLink, FeedOptions options);

    /**
     * Query for users.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained users.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained users or an error.
     */
    Observable<FeedResponsePage<User>> queryUsers(String databaseLink, String query, FeedOptions options);

    /**
     * Query for users.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained users.
     * In case of failure the {@link Observable} will error.
     * 
     * @param databaseLink the database link.
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained users or an error.
     */
    Observable<FeedResponsePage<User>> queryUsers(String databaseLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a permission.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the created permission.
     * In case of failure the {@link Observable} will error.
     * 
     * @param userLink   the user link.
     * @param permission the permission to create.
     * @param options    the request options.
     * @return an {@link Observable} containing the single resource response with the created permission or an error.
     */
    Observable<ResourceResponse<Permission>> createPermission(String userLink, Permission permission, RequestOptions options);

    /**
     * Upserts a permission.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the upserted permission.
     * In case of failure the {@link Observable} will error.
     * 
     * @param userLink   the user link.
     * @param permission the permission to upsert.
     * @param options    the request options.
     * @return an {@link Observable} containing the single resource response with the upserted permission or an error.
     */
    Observable<ResourceResponse<Permission>> upsertPermission(String userLink, Permission permission, RequestOptions options);

    /**
     * Replaces a permission.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced permission.
     * In case of failure the {@link Observable} will error.
     * 
     * @param permission the permission to use.
     * @param options    the request options.
     * @return an {@link Observable} containing the single resource response with the replaced permission or an error.
     */
    Observable<ResourceResponse<Permission>> replacePermission(Permission permission, RequestOptions options);

    /**
     * Deletes a permission.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response for the deleted permission.
     * In case of failure the {@link Observable} will error.
     * 
     * @param permissionLink the permission link.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response for the deleted permission or an error.
     */
    Observable<ResourceResponse<Permission>> deletePermission(String permissionLink, RequestOptions options);

    /**
     * Reads a permission.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read permission.
     * In case of failure the {@link Observable} will error.
     * 
     * @param permissionLink the permission link.
     * @param options        the request options.
     * @return an {@link Observable} containing the single resource response with the read permission or an error.
     */
    Observable<ResourceResponse<Permission>> readPermission(String permissionLink, RequestOptions options);

    /**
     * Reads all permissions.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link Observable} will error.
     * 
     * @param permissionLink the permission link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read permissions or an error.
     */
    Observable<FeedResponsePage<Permission>> readPermissions(String permissionLink, FeedOptions options);

    /**
     * Query for permissions.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Observable} will error.
     * 
     * @param permissionLink the permission link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained permissions or an error.
     */
    Observable<FeedResponsePage<Permission>> queryPermissions(String permissionLink, String query, FeedOptions options);

    /**
     * Query for permissions.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Observable} will error.
     * 
     * @param permissionLink the permission link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained permissions or an error.
     */
    Observable<FeedResponsePage<Permission>> queryPermissions(String permissionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Replaces an offer.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the replaced offer.
     * In case of failure the {@link Observable} will error.
     * 
     * @param offer the offer to use.
     * @return an {@link Observable} containing the single resource response with the replaced offer or an error.
     */
    Observable<ResourceResponse<Offer>> replaceOffer(Offer offer);

    /**
     * Reads an offer.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the read offer.
     * In case of failure the {@link Observable} will error.
     * 
     * @param offerLink the offer link.
     * @return an {@link Observable} containing the single resource response with the read offer or an error.
     */
    Observable<ResourceResponse<Offer>> readOffer(String offerLink);

    /**
     * Reads offers.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of the read offers.
     * In case of failure the {@link Observable} will error.
     * 
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read offers or an error.
     */
    Observable<FeedResponsePage<Offer>> readOffers(FeedOptions options);

    /**
     * Query for offers in a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of obtained obtained offers.
     * In case of failure the {@link Observable} will error.
     * 
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained offers or an error.
     */
    Observable<FeedResponsePage<Offer>> queryOffers(String query, FeedOptions options);

    /**
     * Query for offers in a database.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} will contain one or several feed response pages of obtained obtained offers.
     * In case of failure the {@link Observable} will error.
     * 
     * @param querySpec the query specification.
     * @param options   the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained offers or an error.
     */
    Observable<FeedResponsePage<Offer>> queryOffers(SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Gets database account information.
     * 
     * After subscription the operation will be performed. 
     * The {@link Observable} upon successful completion will contain a single resource response with the database account.
     * In case of failure the {@link Observable} will error.
     * 
     * @return an {@link Observable} containing the single resource response with the database account or an error.
     */
    Observable<DatabaseAccount> getDatabaseAccount();

    /**
     * Close this {@link AsyncDocumentClient} instance and cleans up the resources.
     */
    void close();

}