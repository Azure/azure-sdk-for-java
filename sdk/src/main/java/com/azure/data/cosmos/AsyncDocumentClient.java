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
package com.azure.data.cosmos;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.RxDocumentClientImpl;

import rx.Observable;

/**
 * Provides a client-side logical representation of the Azure Cosmos DB
 * database service. This async client is used to configure and execute requests
 * against the service.
 *
 * <p>
 * {@link AsyncDocumentClient} async APIs return <a href="https://github.com/ReactiveX/RxJava">rxJava</a>'s {@code
 * Observable}, and so you can use rxJava {@link Observable} functionality.
 * <STRONG>The async {@link Observable} based APIs perform the requested operation only after
 * subscription.</STRONG>
 *
 * <p>
 * The service client encapsulates the endpoint and credentials used to access
 * the Cosmos DB service.
 * <p>
 * To instantiate you can use the {@link Builder}
 * <pre>
 * {@code
 * ConnectionPolicy connectionPolicy = new ConnectionPolicy();
 * connectionPolicy.connectionMode(ConnectionMode.DIRECT);
 * AsyncDocumentClient client = new AsyncDocumentClient.Builder()
 *         .withServiceEndpoint(serviceEndpoint)
 *         .withMasterKeyOrResourceToken(masterKey)
 *         .withConnectionPolicy(connectionPolicy)
 *         .withConsistencyLevel(ConsistencyLevel.SESSION)
 *         .build();
 * }
 * </pre>
 */
public interface AsyncDocumentClient {

    /**
     * Helper class to build {@link AsyncDocumentClient} instances
     * as logical representation of the Azure Cosmos DB database service.
     *
     * <pre>
     * {@code
     * ConnectionPolicy connectionPolicy = new ConnectionPolicy();
     * connectionPolicy.connectionMode(ConnectionMode.DIRECT);
     * AsyncDocumentClient client = new AsyncDocumentClient.Builder()
     *         .withServiceEndpoint(serviceEndpoint)
     *         .withMasterKeyOrResourceToken(masterKey)
     *         .withConnectionPolicy(connectionPolicy)
     *         .withConsistencyLevel(ConsistencyLevel.SESSION)
     *         .build();
     * }
     * </pre>
     */
    class Builder {

        Configs configs = new Configs();
        ConnectionPolicy connectionPolicy;
        ConsistencyLevel desiredConsistencyLevel;
        List<Permission> permissionFeed;
        String masterKeyOrResourceToken;
        URI serviceEndpoint;
        TokenResolver tokenResolver;

        public Builder withServiceEndpoint(String serviceEndpoint) {
            try {
                this.serviceEndpoint = new URI(serviceEndpoint);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            return this;
        }

        /**
         * New method withMasterKeyOrResourceToken will take either master key or resource token
         * and perform authentication for accessing resource.
         *
         * @param masterKeyOrResourceToken MasterKey or resourceToken for authentication.
         * @return current Builder.
         * @deprecated use {@link #withMasterKeyOrResourceToken(String)} instead.
         */
        @Deprecated
        public Builder withMasterKey(String masterKeyOrResourceToken) {
            this.masterKeyOrResourceToken = masterKeyOrResourceToken;
            return this;
        }

        /**
         * This method will accept the master key , additionally it can also consume
         * resource token too for authentication.
         *
         * @param masterKeyOrResourceToken MasterKey or resourceToken for authentication.
         * @return current Builder.
         */
        public Builder withMasterKeyOrResourceToken(String masterKeyOrResourceToken) {
            this.masterKeyOrResourceToken = masterKeyOrResourceToken;
            return this;
        }

        /**
         * This method will accept the permission list , which contains the
         * resource tokens needed to access resources.
         *
         * @param permissionFeed Permission list for authentication.
         * @return current Builder.
         */
        public Builder withPermissionFeed(List<Permission> permissionFeed) {
            this.permissionFeed = permissionFeed;
            return this;
        }

        public Builder withConsistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
            this.desiredConsistencyLevel = desiredConsistencyLevel;
            return this;
        }

        public Builder withConfigs(Configs configs) {
            this.configs = configs;
            return this;
        }

        public Builder withConnectionPolicy(ConnectionPolicy connectionPolicy) {
            this.connectionPolicy = connectionPolicy;
            return this;
        }

        /**
         * This method will accept tokenResolver which is rx function, it takes arguments<br>
         * T1 requestVerb(STRING),<br>
         * T2 resourceIdOrFullName(STRING),<br>
         * T3 resourceType(com.azure.data.cosmos.internal.ResourceType),<br>
         * T4 request headers(Map<STRING, STRING>)<br>
         *<br>
         * and return<br>
         * R authenticationToken(STRING)<br>
         *
         * @param tokenResolver tokenResolver function for authentication.
         * @return current Builder.
         */
        /*public Builder withTokenResolver(Func4<STRING, STRING, ResourceType, Map<STRING, STRING>, STRING> tokenResolver) {
            this.tokenResolver = tokenResolver;
            return this;
        }*/

        /**
         * This method will accept functional interface TokenResolver which helps in generation authorization
         * token per request. AsyncDocumentClient can be successfully initialized with this API without passing any MasterKey, ResourceToken or PermissionFeed.
         * @param tokenResolver The tokenResolver
         * @return current Builder.
         */
        public Builder withTokenResolver(TokenResolver tokenResolver) {
            this.tokenResolver = tokenResolver;
            return this;
        }

        private void ifThrowIllegalArgException(boolean value, String error) {
            if (value) {
                throw new IllegalArgumentException(error);
            }
        }

        public AsyncDocumentClient build() {

            ifThrowIllegalArgException(this.serviceEndpoint == null, "cannot build client without service endpoint");
            ifThrowIllegalArgException(
                    this.masterKeyOrResourceToken == null && (permissionFeed == null || permissionFeed.isEmpty()) && this.tokenResolver == null,
                    "cannot build client without any one of masterKey, resource token, permissionFeed and tokenResolver");

            RxDocumentClientImpl client = new RxDocumentClientImpl(serviceEndpoint,
                                                                   masterKeyOrResourceToken,
                                                                   permissionFeed,
                                                                   connectionPolicy,
                                                                   desiredConsistencyLevel,
                                                                   configs,
                                                                   tokenResolver);
            client.init();
            return client;
        }

        public Configs getConfigs() {
            return configs;
        }

        public void setConfigs(Configs configs) {
            this.configs = configs;
        }

        public ConnectionPolicy getConnectionPolicy() {
            return connectionPolicy;
        }

        public void setConnectionPolicy(ConnectionPolicy connectionPolicy) {
            this.connectionPolicy = connectionPolicy;
        }

        public ConsistencyLevel getDesiredConsistencyLevel() {
            return desiredConsistencyLevel;
        }

        public void setDesiredConsistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
            this.desiredConsistencyLevel = desiredConsistencyLevel;
        }

        public List<Permission> getPermissionFeed() {
            return permissionFeed;
        }

        public void setPermissionFeed(List<Permission> permissionFeed) {
            this.permissionFeed = permissionFeed;
        }

        public String getMasterKeyOrResourceToken() {
            return masterKeyOrResourceToken;
        }

        public void setMasterKeyOrResourceToken(String masterKeyOrResourceToken) {
            this.masterKeyOrResourceToken = masterKeyOrResourceToken;
        }

        public URI getServiceEndpoint() {
            return serviceEndpoint;
        }

        public void setServiceEndpoint(URI serviceEndpoint) {
            this.serviceEndpoint = serviceEndpoint;
        }

        public TokenResolver getTokenResolver() {
            return tokenResolver;
        }

        public void setTokenResolver(TokenResolver tokenResolver) {
            this.tokenResolver = tokenResolver;
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the read databases.
     * In case of failure the {@link Observable} will error.
     *
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of read databases or an error.
     */
    Observable<FeedResponse<Database>> readDatabases(FeedOptions options);

    /**
     * Query for databases.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the read databases.
     * In case of failure the {@link Observable} will error.
     *
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of read databases or an error.
     */
    Observable<FeedResponse<Database>> queryDatabases(String query, FeedOptions options);

    /**
     * Query for databases.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the obtained databases.
     * In case of failure the {@link Observable} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options   the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained databases or an error.
     */
    Observable<FeedResponse<Database>> queryDatabases(SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a document collection.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the read collections.
     * In case of failure the {@link Observable} will error.
     *
     * @param databaseLink the database link.
     * @param options      the fee options.
     * @return an {@link Observable} containing one or several feed response pages of the read collections or an error.
     */
    Observable<FeedResponse<DocumentCollection>> readCollections(String databaseLink, FeedOptions options);

    /**
     * Query for document collections in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the obtained collections.
     * In case of failure the {@link Observable} will error.
     *
     * @param databaseLink the database link.
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained collections or an error.
     */
    Observable<FeedResponse<DocumentCollection>> queryCollections(String databaseLink, String query, FeedOptions options);

    /**
     * Query for document collections in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the obtained collections.
     * In case of failure the {@link Observable} will error.
     *
     * @param databaseLink the database link.
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained collections or an error.
     */
    Observable<FeedResponse<DocumentCollection>> queryCollections(String databaseLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a document.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the read documents.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read documents or an error.
     */
    Observable<FeedResponse<Document>> readDocuments(String collectionLink, FeedOptions options);


    /**
     * Query for documents in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the link to the parent document collection.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained document or an error.
     */
    Observable<FeedResponse<Document>> queryDocuments(String collectionLink, String query, FeedOptions options);

    /**
     * Query for documents in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the link to the parent document collection.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained documents or an error.
     */
    Observable<FeedResponse<Document>> queryDocuments(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Query for documents change feed in a document collection.
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained documents.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink    the link to the parent document collection.
     * @param changeFeedOptions the change feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained documents or an error.
     */
    Observable<FeedResponse<Document>> queryDocumentChangeFeed(String collectionLink,
                                                               ChangeFeedOptions changeFeedOptions);

    /**
     * Reads all partition key ranges in a document collection.
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained partition key ranges.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the link to the parent document collection.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained partition key ranges or an error.
     */
    Observable<FeedResponse<PartitionKeyRange>> readPartitionKeyRanges(String collectionLink, FeedOptions options);

    /**
     * Creates a stored procedure.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * READ a stored procedure by the stored procedure link.
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the read stored procedures.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read stored procedures or an error.
     */
    Observable<FeedResponse<StoredProcedure>> readStoredProcedures(String collectionLink, FeedOptions options);

    /**
     * Query for stored procedures in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained stored procedures or an error.
     */
    Observable<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink, String query, FeedOptions options);

    /**
     * Query for stored procedures in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained stored procedures or an error.
     */
    Observable<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink, SqlQuerySpec querySpec,
                                                                    FeedOptions options);

    /**
     * Executes a stored procedure by the stored procedure link.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the read triggers.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read triggers or an error.
     */
    Observable<FeedResponse<Trigger>> readTriggers(String collectionLink, FeedOptions options);

    /**
     * Query for triggers.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained triggers or an error.
     */
    Observable<FeedResponse<Trigger>> queryTriggers(String collectionLink, String query, FeedOptions options);

    /**
     * Query for triggers.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained triggers or an error.
     */
    Observable<FeedResponse<Trigger>> queryTriggers(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a user defined function.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * READ a user defined function.
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the read user defined functions.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read user defined functions or an error.
     */
    Observable<FeedResponse<UserDefinedFunction>> readUserDefinedFunctions(String collectionLink, FeedOptions options);

    /**
     * Query for user defined functions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    Observable<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink, String query,
                                                                            FeedOptions options);

    /**
     * Query for user defined functions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    Observable<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink, SqlQuerySpec querySpec,
                                                                            FeedOptions options);

    /**
     * Reads a conflict.
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the read conflicts.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read conflicts or an error.
     */
    Observable<FeedResponse<Conflict>> readConflicts(String collectionLink, FeedOptions options);

    /**
     * Query for conflicts.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained conflicts.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained conflicts or an error.
     */
    Observable<FeedResponse<Conflict>> queryConflicts(String collectionLink, String query, FeedOptions options);

    /**
     * Query for conflicts.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained conflicts.
     * In case of failure the {@link Observable} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained conflicts or an error.
     */
    Observable<FeedResponse<Conflict>> queryConflicts(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Deletes a conflict.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the read users.
     * In case of failure the {@link Observable} will error.
     *
     * @param databaseLink the database link.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read users or an error.
     */
    Observable<FeedResponse<User>> readUsers(String databaseLink, FeedOptions options);

    /**
     * Query for users.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained users.
     * In case of failure the {@link Observable} will error.
     *
     * @param databaseLink the database link.
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained users or an error.
     */
    Observable<FeedResponse<User>> queryUsers(String databaseLink, String query, FeedOptions options);

    /**
     * Query for users.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained users.
     * In case of failure the {@link Observable} will error.
     *
     * @param databaseLink the database link.
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained users or an error.
     */
    Observable<FeedResponse<User>> queryUsers(String databaseLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a permission.
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link Observable} will error.
     *
     * @param permissionLink the permission link.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read permissions or an error.
     */
    Observable<FeedResponse<Permission>> readPermissions(String permissionLink, FeedOptions options);

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Observable} will error.
     *
     * @param permissionLink the permission link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained permissions or an error.
     */
    Observable<FeedResponse<Permission>> queryPermissions(String permissionLink, String query, FeedOptions options);

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Observable} will error.
     *
     * @param permissionLink the permission link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained permissions or an error.
     */
    Observable<FeedResponse<Permission>> queryPermissions(String permissionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Replaces an offer.
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of the read offers.
     * In case of failure the {@link Observable} will error.
     *
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the read offers or an error.
     */
    Observable<FeedResponse<Offer>> readOffers(FeedOptions options);

    /**
     * Query for offers in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of obtained obtained offers.
     * In case of failure the {@link Observable} will error.
     *
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained offers or an error.
     */
    Observable<FeedResponse<Offer>> queryOffers(String query, FeedOptions options);

    /**
     * Query for offers in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Observable} will contain one or several feed response pages of obtained obtained offers.
     * In case of failure the {@link Observable} will error.
     *
     * @param querySpec the query specification.
     * @param options   the feed options.
     * @return an {@link Observable} containing one or several feed response pages of the obtained offers or an error.
     */
    Observable<FeedResponse<Offer>> queryOffers(SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Gets database account information.
     * <p>
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
