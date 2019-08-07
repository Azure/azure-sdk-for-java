// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosKeyCredential;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.TokenResolver;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
/**
 * Provides a client-side logical representation of the Azure Cosmos DB
 * database service. This async client is used to configure and execute requests
 * against the service.
 *
 * <p>
 * {@link AsyncDocumentClient} async APIs return <a href="https://github.com/reactor/reactor-core">project reactor</a>'s {@link
 * Flux}, and so you can use project reactor {@link Flux} functionality.
 * <STRONG>The async {@link Flux} based APIs perform the requested operation only after
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
        CosmosKeyCredential cosmosKeyCredential;

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

        public Builder withCosmosKeyCredential(CosmosKeyCredential cosmosKeyCredential) {
            if (cosmosKeyCredential != null && StringUtils.isEmpty(cosmosKeyCredential.key())) {
                throw new IllegalArgumentException("Cannot build client with empty key credential");
            }
            this.cosmosKeyCredential = cosmosKeyCredential;
            return this;
        }

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
                    this.masterKeyOrResourceToken == null && (permissionFeed == null || permissionFeed.isEmpty())
                        && this.tokenResolver == null && this.cosmosKeyCredential == null,
                    "cannot build client without any one of masterKey, " +
                        "resource token, permissionFeed, tokenResolver and cosmos key credential");
            ifThrowIllegalArgException(cosmosKeyCredential != null && StringUtils.isEmpty(cosmosKeyCredential.key()),
                "cannot build client without key credential");

            RxDocumentClientImpl client = new RxDocumentClientImpl(serviceEndpoint,
                                                                   masterKeyOrResourceToken,
                                                                   permissionFeed,
                                                                   connectionPolicy,
                                                                   desiredConsistencyLevel,
                                                                   configs,
                                                                   tokenResolver,
                                                                    cosmosKeyCredential);
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

        public CosmosKeyCredential getCosmosKeyCredential() {
            return cosmosKeyCredential;
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
     * The {@link Flux} upon successful completion will contain a single resource response with the created database.
     * In case of failure the {@link Flux} will error.
     *
     * @param database the database.
     * @param options  the request options.
     * @return an {@link Flux} containing the single resource response with the created database or an error.
     */
    Flux<ResourceResponse<Database>> createDatabase(Database database, RequestOptions options);

    /**
     * Deletes a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the deleted database.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the deleted database or an error.
     */
    Flux<ResourceResponse<Database>> deleteDatabase(String databaseLink, RequestOptions options);

    /**
     * Reads a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read database.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the read database or an error.
     */
    Flux<ResourceResponse<Database>> readDatabase(String databaseLink, RequestOptions options);

    /**
     * Reads all databases.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read databases.
     * In case of failure the {@link Flux} will error.
     *
     * @param options the feed options.
     * @return an {@link Flux} containing one or several feed response pages of read databases or an error.
     */
    Flux<FeedResponse<Database>> readDatabases(FeedOptions options);

    /**
     * Query for databases.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read databases.
     * In case of failure the {@link Flux} will error.
     *
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Flux} containing one or several feed response pages of read databases or an error.
     */
    Flux<FeedResponse<Database>> queryDatabases(String query, FeedOptions options);

    /**
     * Query for databases.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the obtained databases.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec the SQL query specification.
     * @param options   the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained databases or an error.
     */
    Flux<FeedResponse<Database>> queryDatabases(SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the created collection.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param collection   the collection.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the created collection or an error.
     */
    Flux<ResourceResponse<DocumentCollection>> createCollection(String databaseLink, DocumentCollection collection,
                                                                RequestOptions options);

    /**
     * Replaces a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced document collection.
     * In case of failure the {@link Flux} will error.
     *
     * @param collection the document collection to use.
     * @param options    the request options.
     * @return an {@link Flux} containing the single resource response with the replaced document collection or an error.
     */
    Flux<ResourceResponse<DocumentCollection>> replaceCollection(DocumentCollection collection, RequestOptions options);

    /**
     * Deletes a document collection by the collection link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted database.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response for the deleted database or an error.
     */
    Flux<ResourceResponse<DocumentCollection>> deleteCollection(String collectionLink, RequestOptions options);

    /**
     * Reads a document collection by the collection link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read collection.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response with the read collection or an error.
     */
    Flux<ResourceResponse<DocumentCollection>> readCollection(String collectionLink, RequestOptions options);

    /**
     * Reads all document collections in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read collections.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param options      the fee options.
     * @return an {@link Flux} containing one or several feed response pages of the read collections or an error.
     */
    Flux<FeedResponse<DocumentCollection>> readCollections(String databaseLink, FeedOptions options);

    /**
     * Query for document collections in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the obtained collections.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained collections or an error.
     */
    Flux<FeedResponse<DocumentCollection>> queryCollections(String databaseLink, String query, FeedOptions options);

    /**
     * Query for document collections in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the obtained collections.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained collections or an error.
     */
    Flux<FeedResponse<DocumentCollection>> queryCollections(String databaseLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a document.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the created document.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink               the link to the parent document collection.
     * @param document                     the document represented as a POJO or Document object.
     * @param options                      the request options.
     * @param disableAutomaticIdGeneration the flag for disabling automatic id generation.
     * @return an {@link Flux} containing the single resource response with the created document or an error.
     */
    Flux<ResourceResponse<Document>> createDocument(String collectionLink, Object document, RequestOptions options,
                                                    boolean disableAutomaticIdGeneration);

    /**
     * Upserts a document.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the upserted document.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink               the link to the parent document collection.
     * @param document                     the document represented as a POJO or Document object to upsert.
     * @param options                      the request options.
     * @param disableAutomaticIdGeneration the flag for disabling automatic id generation.
     * @return an {@link Flux} containing the single resource response with the upserted document or an error.
     */
    Flux<ResourceResponse<Document>> upsertDocument(String collectionLink, Object document, RequestOptions options,
                                                          boolean disableAutomaticIdGeneration);

    /**
     * Replaces a document using a POJO object.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced document.
     * In case of failure the {@link Flux} will error.
     *
     * @param documentLink the document link.
     * @param document     the document represented as a POJO or Document object.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the replaced document or an error.
     */
    Flux<ResourceResponse<Document>> replaceDocument(String documentLink, Object document, RequestOptions options);

    /**
     * Replaces a document with the passed in document.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced document.
     * In case of failure the {@link Flux} will error.
     *
     * @param document the document to replace (containing the document id).
     * @param options  the request options.
     * @return an {@link Flux} containing the single resource response with the replaced document or an error.
     */
    Flux<ResourceResponse<Document>> replaceDocument(Document document, RequestOptions options);

    /**
     * Deletes a document by the document link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted document.
     * In case of failure the {@link Flux} will error.
     *
     * @param documentLink the document link.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response for the deleted document or an error.
     */
    Flux<ResourceResponse<Document>> deleteDocument(String documentLink, RequestOptions options);

    /**
     * Reads a document by the document link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read document.
     * In case of failure the {@link Flux} will error.
     *
     * @param documentLink the document link.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the read document or an error.
     */
    Flux<ResourceResponse<Document>> readDocument(String documentLink, RequestOptions options);

    /**
     * Reads all documents in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the read documents.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read documents or an error.
     */
    Flux<FeedResponse<Document>> readDocuments(String collectionLink, FeedOptions options);


    /**
     * Query for documents in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the link to the parent document collection.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained document or an error.
     */
    Flux<FeedResponse<Document>> queryDocuments(String collectionLink, String query, FeedOptions options);

    /**
     * Query for documents in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response of the obtained documents.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the link to the parent document collection.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained documents or an error.
     */
    Flux<FeedResponse<Document>> queryDocuments(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Query for documents change feed in a document collection.
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained documents.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink    the link to the parent document collection.
     * @param changeFeedOptions the change feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained documents or an error.
     */
    Flux<FeedResponse<Document>> queryDocumentChangeFeed(String collectionLink,
                                                               ChangeFeedOptions changeFeedOptions);

    /**
     * Reads all partition key ranges in a document collection.
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained partition key ranges.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the link to the parent document collection.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained partition key ranges or an error.
     */
    Flux<FeedResponse<PartitionKeyRange>> readPartitionKeyRanges(String collectionLink, FeedOptions options);

    /**
     * Creates a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the created stored procedure.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink  the collection link.
     * @param storedProcedure the stored procedure to create.
     * @param options         the request options.
     * @return an {@link Flux} containing the single resource response with the created stored procedure or an error.
     */
    Flux<ResourceResponse<StoredProcedure>> createStoredProcedure(String collectionLink, StoredProcedure storedProcedure,
                                                                  RequestOptions options);

    /**
     * Upserts a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the upserted stored procedure.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink  the collection link.
     * @param storedProcedure the stored procedure to upsert.
     * @param options         the request options.
     * @return an {@link Flux} containing the single resource response with the upserted stored procedure or an error.
     */
    Flux<ResourceResponse<StoredProcedure>> upsertStoredProcedure(String collectionLink, StoredProcedure storedProcedure,
                                                                        RequestOptions options);

    /**
     * Replaces a stored procedure.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced stored procedure.
     * In case of failure the {@link Flux} will error.
     *
     * @param storedProcedure the stored procedure to use.
     * @param options         the request options.
     * @return an {@link Flux} containing the single resource response with the replaced stored procedure or an error.
     */
    Flux<ResourceResponse<StoredProcedure>> replaceStoredProcedure(StoredProcedure storedProcedure, RequestOptions options);

    /**
     * Deletes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted stored procedure.
     * In case of failure the {@link Flux} will error.
     *
     * @param storedProcedureLink the stored procedure link.
     * @param options             the request options.
     * @return an {@link Flux} containing the single resource response for the deleted stored procedure or an error.
     */
    Flux<ResourceResponse<StoredProcedure>> deleteStoredProcedure(String storedProcedureLink, RequestOptions options);

    /**
     * READ a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read stored procedure.
     * In case of failure the {@link Flux} will error.
     *
     * @param storedProcedureLink the stored procedure link.
     * @param options             the request options.
     * @return an {@link Flux} containing the single resource response with the read stored procedure or an error.
     */
    Flux<ResourceResponse<StoredProcedure>> readStoredProcedure(String storedProcedureLink, RequestOptions options);

    /**
     * Reads all stored procedures in a document collection link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read stored procedures.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read stored procedures or an error.
     */
    Flux<FeedResponse<StoredProcedure>> readStoredProcedures(String collectionLink, FeedOptions options);

    /**
     * Query for stored procedures in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained stored procedures or an error.
     */
    Flux<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink, String query, FeedOptions options);

    /**
     * Query for stored procedures in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained stored procedures.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained stored procedures or an error.
     */
    Flux<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink, SqlQuerySpec querySpec,
                                                                    FeedOptions options);

    /**
     * Executes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the stored procedure response.
     * In case of failure the {@link Flux} will error.
     *
     * @param storedProcedureLink the stored procedure link.
     * @param procedureParams     the array of procedure parameter values.
     * @return an {@link Flux} containing the single resource response with the stored procedure response or an error.
     */
    Flux<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink, Object[] procedureParams);

    /**
     * Executes a stored procedure by the stored procedure link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the stored procedure response.
     * In case of failure the {@link Flux} will error.
     *
     * @param storedProcedureLink the stored procedure link.
     * @param options             the request options.
     * @param procedureParams     the array of procedure parameter values.
     * @return an {@link Flux} containing the single resource response with the stored procedure response or an error.
     */
    Flux<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink, RequestOptions options,
                                                               Object[] procedureParams);

    /**
     * Creates a trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the created trigger.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param trigger        the trigger.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response with the created trigger or an error.
     */
    Flux<ResourceResponse<Trigger>> createTrigger(String collectionLink, Trigger trigger, RequestOptions options);

    /**
     * Upserts a trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the upserted trigger.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param trigger        the trigger to upsert.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response with the upserted trigger or an error.
     */
    Flux<ResourceResponse<Trigger>> upsertTrigger(String collectionLink, Trigger trigger, RequestOptions options);

    /**
     * Replaces a trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced trigger.
     * In case of failure the {@link Flux} will error.
     *
     * @param trigger the trigger to use.
     * @param options the request options.
     * @return an {@link Flux} containing the single resource response with the replaced trigger or an error.
     */
    Flux<ResourceResponse<Trigger>> replaceTrigger(Trigger trigger, RequestOptions options);

    /**
     * Deletes a trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted trigger.
     * In case of failure the {@link Flux} will error.
     *
     * @param triggerLink the trigger link.
     * @param options     the request options.
     * @return an {@link Flux} containing the single resource response for the deleted trigger or an error.
     */
    Flux<ResourceResponse<Trigger>> deleteTrigger(String triggerLink, RequestOptions options);

    /**
     * Reads a trigger by the trigger link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the read trigger.
     * In case of failure the {@link Flux} will error.
     *
     * @param triggerLink the trigger link.
     * @param options     the request options.
     * @return an {@link Flux} containing the single resource response for the read trigger or an error.
     */
    Flux<ResourceResponse<Trigger>> readTrigger(String triggerLink, RequestOptions options);

    /**
     * Reads all triggers in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read triggers.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read triggers or an error.
     */
    Flux<FeedResponse<Trigger>> readTriggers(String collectionLink, FeedOptions options);

    /**
     * Query for triggers.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained triggers or an error.
     */
    Flux<FeedResponse<Trigger>> queryTriggers(String collectionLink, String query, FeedOptions options);

    /**
     * Query for triggers.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained triggers.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained triggers or an error.
     */
    Flux<FeedResponse<Trigger>> queryTriggers(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the created user defined function.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param udf            the user defined function.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response with the created user defined function or an error.
     */
    Flux<ResourceResponse<UserDefinedFunction>> createUserDefinedFunction(String collectionLink, UserDefinedFunction udf,
                                                                          RequestOptions options);

    /**
     * Upserts a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the upserted user defined function.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param udf            the user defined function to upsert.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response with the upserted user defined function or an error.
     */
    Flux<ResourceResponse<UserDefinedFunction>> upsertUserDefinedFunction(String collectionLink, UserDefinedFunction udf,
                                                                                RequestOptions options);

    /**
     * Replaces a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced user defined function.
     * In case of failure the {@link Flux} will error.
     *
     * @param udf     the user defined function.
     * @param options the request options.
     * @return an {@link Flux} containing the single resource response with the replaced user defined function or an error.
     */
    Flux<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunction(UserDefinedFunction udf, RequestOptions options);

    /**
     * Deletes a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted user defined function.
     * In case of failure the {@link Flux} will error.
     *
     * @param udfLink the user defined function link.
     * @param options the request options.
     * @return an {@link Flux} containing the single resource response for the deleted user defined function or an error.
     */
    Flux<ResourceResponse<UserDefinedFunction>> deleteUserDefinedFunction(String udfLink, RequestOptions options);

    /**
     * READ a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the read user defined function.
     * In case of failure the {@link Flux} will error.
     *
     * @param udfLink the user defined function link.
     * @param options the request options.
     * @return an {@link Flux} containing the single resource response for the read user defined function or an error.
     */
    Flux<ResourceResponse<UserDefinedFunction>> readUserDefinedFunction(String udfLink, RequestOptions options);

    /**
     * Reads all user defined functions in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read user defined functions.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read user defined functions or an error.
     */
    Flux<FeedResponse<UserDefinedFunction>> readUserDefinedFunctions(String collectionLink, FeedOptions options);

    /**
     * Query for user defined functions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    Flux<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink, String query,
                                                                            FeedOptions options);

    /**
     * Query for user defined functions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained user defined functions.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained user defined functions or an error.
     */
    Flux<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink, SqlQuerySpec querySpec,
                                                                            FeedOptions options);

    /**
     * Reads a conflict.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read conflict.
     * In case of failure the {@link Flux} will error.
     *
     * @param conflictLink the conflict link.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the read conflict or an error.
     */
    Flux<ResourceResponse<Conflict>> readConflict(String conflictLink, RequestOptions options);

    /**
     * Reads all conflicts in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read conflicts.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read conflicts or an error.
     */
    Flux<FeedResponse<Conflict>> readConflicts(String collectionLink, FeedOptions options);

    /**
     * Query for conflicts.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained conflicts.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained conflicts or an error.
     */
    Flux<FeedResponse<Conflict>> queryConflicts(String collectionLink, String query, FeedOptions options);

    /**
     * Query for conflicts.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained conflicts.
     * In case of failure the {@link Flux} will error.
     *
     * @param collectionLink the collection link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained conflicts or an error.
     */
    Flux<FeedResponse<Conflict>> queryConflicts(String collectionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Deletes a conflict.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted conflict.
     * In case of failure the {@link Flux} will error.
     *
     * @param conflictLink the conflict link.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response for the deleted conflict or an error.
     */
    Flux<ResourceResponse<Conflict>> deleteConflict(String conflictLink, RequestOptions options);

    /**
     * Creates a user.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the created user.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param user         the user to create.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the created user or an error.
     */
    Flux<ResourceResponse<User>> createUser(String databaseLink, User user, RequestOptions options);

    /**
     * Upserts a user.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the upserted user.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param user         the user to upsert.
     * @param options      the request options.
     * @return an {@link Flux} containing the single resource response with the upserted user or an error.
     */
    Flux<ResourceResponse<User>> upsertUser(String databaseLink, User user, RequestOptions options);

    /**
     * Replaces a user.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced user.
     * In case of failure the {@link Flux} will error.
     *
     * @param user    the user to use.
     * @param options the request options.
     * @return an {@link Flux} containing the single resource response with the replaced user or an error.
     */
    Flux<ResourceResponse<User>> replaceUser(User user, RequestOptions options);

    /**
     * Deletes a user.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted user.
     * In case of failure the {@link Flux} will error.
     *
     * @param userLink the user link.
     * @param options  the request options.
     * @return an {@link Flux} containing the single resource response for the deleted user or an error.
     */
    Flux<ResourceResponse<User>> deleteUser(String userLink, RequestOptions options);

    /**
     * Reads a user.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read user.
     * In case of failure the {@link Flux} will error.
     *
     * @param userLink the user link.
     * @param options  the request options.
     * @return an {@link Flux} containing the single resource response with the read user or an error.
     */
    Flux<ResourceResponse<User>> readUser(String userLink, RequestOptions options);

    /**
     * Reads all users in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read users.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param options      the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read users or an error.
     */
    Flux<FeedResponse<User>> readUsers(String databaseLink, FeedOptions options);

    /**
     * Query for users.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained users.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param query        the query.
     * @param options      the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained users or an error.
     */
    Flux<FeedResponse<User>> queryUsers(String databaseLink, String query, FeedOptions options);

    /**
     * Query for users.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained users.
     * In case of failure the {@link Flux} will error.
     *
     * @param databaseLink the database link.
     * @param querySpec    the SQL query specification.
     * @param options      the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained users or an error.
     */
    Flux<FeedResponse<User>> queryUsers(String databaseLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Creates a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the created permission.
     * In case of failure the {@link Flux} will error.
     *
     * @param userLink   the user link.
     * @param permission the permission to create.
     * @param options    the request options.
     * @return an {@link Flux} containing the single resource response with the created permission or an error.
     */
    Flux<ResourceResponse<Permission>> createPermission(String userLink, Permission permission, RequestOptions options);

    /**
     * Upserts a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the upserted permission.
     * In case of failure the {@link Flux} will error.
     *
     * @param userLink   the user link.
     * @param permission the permission to upsert.
     * @param options    the request options.
     * @return an {@link Flux} containing the single resource response with the upserted permission or an error.
     */
    Flux<ResourceResponse<Permission>> upsertPermission(String userLink, Permission permission, RequestOptions options);

    /**
     * Replaces a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced permission.
     * In case of failure the {@link Flux} will error.
     *
     * @param permission the permission to use.
     * @param options    the request options.
     * @return an {@link Flux} containing the single resource response with the replaced permission or an error.
     */
    Flux<ResourceResponse<Permission>> replacePermission(Permission permission, RequestOptions options);

    /**
     * Deletes a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response for the deleted permission.
     * In case of failure the {@link Flux} will error.
     *
     * @param permissionLink the permission link.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response for the deleted permission or an error.
     */
    Flux<ResourceResponse<Permission>> deletePermission(String permissionLink, RequestOptions options);

    /**
     * Reads a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read permission.
     * In case of failure the {@link Flux} will error.
     *
     * @param permissionLink the permission link.
     * @param options        the request options.
     * @return an {@link Flux} containing the single resource response with the read permission or an error.
     */
    Flux<ResourceResponse<Permission>> readPermission(String permissionLink, RequestOptions options);

    /**
     * Reads all permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link Flux} will error.
     *
     * @param permissionLink the permission link.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read permissions or an error.
     */
    Flux<FeedResponse<Permission>> readPermissions(String permissionLink, FeedOptions options);

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Flux} will error.
     *
     * @param permissionLink the permission link.
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained permissions or an error.
     */
    Flux<FeedResponse<Permission>> queryPermissions(String permissionLink, String query, FeedOptions options);

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Flux} will error.
     *
     * @param permissionLink the permission link.
     * @param querySpec      the SQL query specification.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained permissions or an error.
     */
    Flux<FeedResponse<Permission>> queryPermissions(String permissionLink, SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Replaces an offer.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the replaced offer.
     * In case of failure the {@link Flux} will error.
     *
     * @param offer the offer to use.
     * @return an {@link Flux} containing the single resource response with the replaced offer or an error.
     */
    Flux<ResourceResponse<Offer>> replaceOffer(Offer offer);

    /**
     * Reads an offer.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the read offer.
     * In case of failure the {@link Flux} will error.
     *
     * @param offerLink the offer link.
     * @return an {@link Flux} containing the single resource response with the read offer or an error.
     */
    Flux<ResourceResponse<Offer>> readOffer(String offerLink);

    /**
     * Reads offers.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read offers.
     * In case of failure the {@link Flux} will error.
     *
     * @param options the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read offers or an error.
     */
    Flux<FeedResponse<Offer>> readOffers(FeedOptions options);

    /**
     * Query for offers in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of obtained obtained offers.
     * In case of failure the {@link Flux} will error.
     *
     * @param query   the query.
     * @param options the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained offers or an error.
     */
    Flux<FeedResponse<Offer>> queryOffers(String query, FeedOptions options);

    /**
     * Query for offers in a database.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of obtained obtained offers.
     * In case of failure the {@link Flux} will error.
     *
     * @param querySpec the query specification.
     * @param options   the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained offers or an error.
     */
    Flux<FeedResponse<Offer>> queryOffers(SqlQuerySpec querySpec, FeedOptions options);

    /**
     * Gets database account information.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} upon successful completion will contain a single resource response with the database account.
     * In case of failure the {@link Flux} will error.
     *
     * @return an {@link Flux} containing the single resource response with the database account or an error.
     */
    Flux<DatabaseAccount> getDatabaseAccount();

    /**
     * Close this {@link AsyncDocumentClient} instance and cleans up the resources.
     */
    void close();

}
