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
package com.microsoft.azure.cosmosdb.rx.internal;

import static com.microsoft.azure.cosmosdb.BridgeInternal.documentFromObject;
import static com.microsoft.azure.cosmosdb.BridgeInternal.toDatabaseAccount;
import static com.microsoft.azure.cosmosdb.BridgeInternal.toFeedResponsePage;
import static com.microsoft.azure.cosmosdb.BridgeInternal.toResourceResponse;
import static com.microsoft.azure.cosmosdb.BridgeInternal.toStoredProcedureResponse;
import static org.apache.commons.io.FileUtils.ONE_MB;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.AccessConditionType;
import com.microsoft.azure.cosmosdb.Attachment;
import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.ChangeFeedOptions;
import com.microsoft.azure.cosmosdb.Conflict;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DatabaseAccount;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedOptionsBase;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.JsonSerializable;
import com.microsoft.azure.cosmosdb.MediaOptions;
import com.microsoft.azure.cosmosdb.MediaReadMode;
import com.microsoft.azure.cosmosdb.MediaResponse;
import com.microsoft.azure.cosmosdb.Offer;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.StoredProcedure;
import com.microsoft.azure.cosmosdb.StoredProcedureResponse;
import com.microsoft.azure.cosmosdb.Trigger;
import com.microsoft.azure.cosmosdb.Undefined;
import com.microsoft.azure.cosmosdb.User;
import com.microsoft.azure.cosmosdb.UserDefinedFunction;
import com.microsoft.azure.cosmosdb.internal.BaseAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.internal.EndpointManager;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.PathParser;
import com.microsoft.azure.cosmosdb.internal.Paths;
import com.microsoft.azure.cosmosdb.internal.QueryCompatibilityMode;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.RuntimeConstants;
import com.microsoft.azure.cosmosdb.internal.SessionContainer;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxClientCollectionCache;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxPartitionKeyRangeCache;
import com.microsoft.azure.cosmosdb.rx.internal.query.DocumentQueryExecutionContextFactory;
import com.microsoft.azure.cosmosdb.rx.internal.query.IDocumentQueryClient;
import com.microsoft.azure.cosmosdb.rx.internal.query.IDocumentQueryExecutionContext;
import com.microsoft.azure.cosmosdb.rx.internal.query.Paginator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.RxEventLoopProvider;
import io.reactivex.netty.channel.SingleNioLoopProvider;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.pipeline.PipelineConfiguratorComposite;
import io.reactivex.netty.pipeline.ssl.SSLEngineFactory;
import io.reactivex.netty.protocol.http.HttpObjectAggregationConfigurator;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClientBuilder;
import io.reactivex.netty.protocol.http.client.HttpClientPipelineConfigurator;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class RxDocumentClientImpl implements AsyncDocumentClient, IAuthorizationTokenProvider {

    // we may have ~14K continuation token from a single partition
    private final static int MAX_REQUEST_HEADER_SIZE = 32 * 1024;

    private final Logger logger = LoggerFactory.getLogger(RxDocumentClientImpl.class);
    private final String masterKey;
    private final URI serviceEndpoint;
    private final ConnectionPolicy connectionPolicy;
    private final SessionContainer sessionContainer;
    private final ConsistencyLevel consistencyLevel;
    private final BaseAuthorizationTokenProvider authorizationTokenProvider;
    private final RxClientCollectionCache collectionCache;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final RxGatewayStoreModel gatewayProxy;
    private Map<String, String> resourceTokens;
    /**
     * Compatibility mode: Allows to specify compatibility mode used by client when
     * making query requests. Should be removed when application/sql is no longer
     * supported.
     */
    private final QueryCompatibilityMode queryCompatibilityMode = QueryCompatibilityMode.Default;
    private final CompositeHttpClient<ByteBuf, ByteBuf> rxClient;
    private final EndpointManager globalEndpointManager;
    private RetryPolicy retryPolicy;
    private static final ObjectMapper mapper = new ObjectMapper();

    public RxDocumentClientImpl(URI serviceEndpoint, String masterKey, ConnectionPolicy connectionPolicy,
            ConsistencyLevel consistencyLevel, int eventLoopSize) {

        logger.info(
                "Initializing DocumentClient with"
                        + " serviceEndpoint [{}], ConnectionPolicy [{}], ConsistencyLevel [{}]",
                        serviceEndpoint, connectionPolicy, consistencyLevel);

        this.masterKey = masterKey;
        this.serviceEndpoint = serviceEndpoint;

        if (connectionPolicy != null) {
            this.connectionPolicy = connectionPolicy;
        } else {
            this.connectionPolicy = new ConnectionPolicy();
        }

        this.sessionContainer = new SessionContainer(this.serviceEndpoint.getHost());
        this.consistencyLevel = consistencyLevel;

        UserAgentContainer userAgentContainer = new UserAgentContainer();

        String userAgentSuffix = this.connectionPolicy.getUserAgentSuffix();
        if (userAgentSuffix != null && userAgentSuffix.length() > 0) {
            userAgentContainer.setSuffix(userAgentSuffix);
        }

        if (eventLoopSize <= 0) {
            int cpuCount = Runtime.getRuntime().availableProcessors();
            eventLoopSize = cpuCount;
            logger.debug(
                    "Auto configuring eventLoop size CPU cores [{}], eventLoopSize [{}]",
                    cpuCount, eventLoopSize);
        }

        logger.debug("EventLoop size [{}]", eventLoopSize);

        synchronized (RxDocumentClientImpl.class) {
            SingleNioLoopProvider rxEventLoopProvider = new SingleNioLoopProvider(1, eventLoopSize);
            RxEventLoopProvider oldEventLoopProvider = RxNetty.useEventLoopProvider(rxEventLoopProvider);
            this.rxClient = httpClientBuilder().build();
            RxNetty.useEventLoopProvider(oldEventLoopProvider);
        }

        this.authorizationTokenProvider = new BaseAuthorizationTokenProvider(this.masterKey);

        this.globalEndpointManager = new GlobalEndpointManager(this);

        this.retryPolicy = new RetryPolicy(this.globalEndpointManager, this.connectionPolicy);

        this.gatewayProxy = createRxGatewayProxy(this.connectionPolicy, consistencyLevel, this.queryCompatibilityMode,
                this.masterKey, this.resourceTokens, userAgentContainer, this.globalEndpointManager, this.rxClient);

        this.collectionCache = new RxClientCollectionCache(this.gatewayProxy, this, this.retryPolicy);
        
        this.partitionKeyRangeCache = new RxPartitionKeyRangeCache(
                RxDocumentClientImpl.this, 
                collectionCache);

        if (this.connectionPolicy.getConnectionMode() == ConnectionMode.DirectHttps) {
            throw new UnsupportedOperationException("Direct Https is not supported");
        }   
    }

    RxGatewayStoreModel createRxGatewayProxy(ConnectionPolicy connectionPolicy,
                                             ConsistencyLevel consistencyLevel,
                                             QueryCompatibilityMode queryCompatibilityMode,
                                             String masterKey,
                                             Map<String, String> resourceTokens,
                                             UserAgentContainer userAgentContainer,
                                             EndpointManager globalEndpointManager,
                                             CompositeHttpClient<ByteBuf, ByteBuf> rxClient) {
        return new RxGatewayStoreModel(connectionPolicy,
                consistencyLevel, queryCompatibilityMode, masterKey,
                resourceTokens, userAgentContainer, globalEndpointManager, rxClient);
    }

    private CompositeHttpClientBuilder<ByteBuf, ByteBuf> httpClientBuilder() {
        class DefaultSSLEngineFactory implements SSLEngineFactory {
            private final SslContext sslContex;

            private DefaultSSLEngineFactory() {
                try {
                    SslProvider sslProvider = SslContext.defaultClientProvider();
                    sslContex = SslContextBuilder.forClient().sslProvider(sslProvider).build();
                } catch (SSLException e) {
                    throw new IllegalStateException("Failed to create default SSL context", e);
                }
            }

            @Override
            public SSLEngine createSSLEngine(ByteBufAllocator allocator) {
                return sslContex.newEngine(allocator);
            }
        }

        CompositeHttpClientBuilder<ByteBuf, ByteBuf> builder = new CompositeHttpClientBuilder<ByteBuf, ByteBuf>()
                .withSslEngineFactory(new DefaultSSLEngineFactory())
                .withMaxConnections(connectionPolicy.getMaxPoolSize())
                .withIdleConnectionsTimeoutMillis(this.connectionPolicy.getIdleConnectionTimeoutInMillis())
                .pipelineConfigurator(createClientPipelineConfigurator());

        RxClient.ClientConfig config = new RxClient.ClientConfig.Builder()
                .readTimeout(connectionPolicy.getRequestTimeoutInMillis(), TimeUnit.MILLISECONDS).build();
        return builder.config(config);
    }

    private PipelineConfigurator createClientPipelineConfigurator() {
        PipelineConfigurator clientPipelineConfigurator = new PipelineConfiguratorComposite(
                new HttpClientPipelineConfigurator<ByteBuf, ByteBuf>(
                        HttpClientPipelineConfigurator.MAX_INITIAL_LINE_LENGTH_DEFAULT,
                        MAX_REQUEST_HEADER_SIZE,
                        HttpClientPipelineConfigurator.MAX_CHUNK_SIZE_DEFAULT,
                        true),
                new HttpObjectAggregationConfigurator((int)(ONE_MB * 2)));
        return clientPipelineConfigurator;
    }

    @Override
    public URI getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    @Override
    public URI getWriteEndpoint() {
        return this.globalEndpointManager.getWriteEndpoint();
    }

    @Override
    public URI getReadEndpoint() {
        return this.globalEndpointManager.getReadEndpoint();
    }

    @Override
    public ConnectionPolicy getConnectionPolicy() {
        return this.connectionPolicy;
    }

    @Override
    public Observable<ResourceResponse<Database>> createDatabase(Database database, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> createDatabaseInternal(database, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Database>> createDatabaseInternal(Database database, RequestOptions options) {
        try {

            if (database == null) {
                throw new IllegalArgumentException("Database");
            }

            logger.debug("Creating a Database. id: [{}]", database.getId());
            validateResource(database);

            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Create,
                    ResourceType.Database, Paths.DATABASES_ROOT, database, requestHeaders);

            return this.create(request).map(response -> toResourceResponse(response, Database.class));
        } catch (Exception e) {
            logger.debug("Failure in creating a database. due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Database>> deleteDatabase(String databaseLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteDatabaseInternal(databaseLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Database>> deleteDatabaseInternal(String databaseLink, RequestOptions options) {
        try {
            if (StringUtils.isEmpty(databaseLink)) {
                throw new IllegalArgumentException("databaseLink");
            }

            logger.debug("Deleting a Database. databaseLink: [{}]", databaseLink);
            String path = Utils.joinPath(databaseLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.Database, path, requestHeaders);

            return this.delete(request).map(response -> toResourceResponse(response, Database.class));
        } catch (Exception e) {
            logger.debug("Failure in deleting a database. due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Database>> readDatabase(String databaseLink, RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readDatabaseInternal(databaseLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<Database>> readDatabaseInternal(String databaseLink, RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(databaseLink)) {
                throw new IllegalArgumentException("databaseLink");
            }

            logger.debug("Reading a Database. databaseLink: [{}]", databaseLink);
            String path = Utils.joinPath(databaseLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Database, path, requestHeaders);
            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request).map(response -> toResourceResponse(response, Database.class));
        } catch (Exception e) {
            logger.debug("Failure in reading a database. due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<Database>> readDatabases(FeedOptions options) {
        return readFeed(options, ResourceType.Database, Database.class, Paths.DATABASES_ROOT);
    }

    private String parentResourceLinkToQueryLink(String parentResouceLink, ResourceType resourceTypeEnum) {
        switch (resourceTypeEnum) {
        case Database:
            return Paths.DATABASES_ROOT;

        case DocumentCollection:
            return Utils.joinPath(parentResouceLink, Paths.COLLECTIONS_PATH_SEGMENT);

        case Document:
            return Utils.joinPath(parentResouceLink, Paths.DOCUMENTS_PATH_SEGMENT);

        case Offer:
            return Paths.OFFERS_ROOT;

        case User:
            return Utils.joinPath(parentResouceLink, Paths.USERS_PATH_SEGMENT);

        case Permission:
            return Utils.joinPath(parentResouceLink, Paths.PERMISSIONS_PATH_SEGMENT);

        case Attachment:
            return Utils.joinPath(parentResouceLink, Paths.ATTACHMENTS_PATH_SEGMENT);

        case StoredProcedure:
            return Utils.joinPath(parentResouceLink, Paths.STORED_PROCEDURES_PATH_SEGMENT);

        case Trigger:
            return Utils.joinPath(parentResouceLink, Paths.TRIGGERS_PATH_SEGMENT);

        case UserDefinedFunction:
            return Utils.joinPath(parentResouceLink, Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT);            

        default:
            throw new IllegalArgumentException("resource type not supported");
        }
    }

    private <T extends Resource> Observable<FeedResponse<T>> createQuery(
            String parentResourceLink, 
            SqlQuerySpec sqlQuery,
            FeedOptions options, 
            Class<T> klass,
            ResourceType resourceTypeEnum) {

        String queryResourceLink = parentResourceLinkToQueryLink(parentResourceLink, resourceTypeEnum);

        UUID activityId = Utils.randomUUID();
        IDocumentQueryClient queryClient = DocumentQueryClientImpl(RxDocumentClientImpl.this);
        Observable<? extends IDocumentQueryExecutionContext<T>> executionContext = 
                DocumentQueryExecutionContextFactory.createDocumentQueryExecutionContextAsync(queryClient, resourceTypeEnum, klass, sqlQuery , options, queryResourceLink, false, activityId);
        return executionContext.single().flatMap(ex -> {
            return ex.executeAsync();
        });
    }


    @Override
    public Observable<FeedResponse<Database>> queryDatabases(String query, FeedOptions options) {
        return queryDatabases(new SqlQuerySpec(query), options);
    }


    @Override
    public Observable<FeedResponse<Database>> queryDatabases(SqlQuerySpec querySpec, FeedOptions options) {
        return createQuery(Paths.DATABASES_ROOT, querySpec, options, Database.class, ResourceType.Database);
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> createCollection(String databaseLink,
            DocumentCollection collection, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> this.createCollectionInternal(databaseLink, collection, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<DocumentCollection>> createCollectionInternal(String databaseLink,
            DocumentCollection collection, RequestOptions options) {
        try {
            if (StringUtils.isEmpty(databaseLink)) {
                throw new IllegalArgumentException("databaseLink");
            }
            if (collection == null) {
                throw new IllegalArgumentException("collection");
            }

            logger.debug("Creating a Collection. databaseLink: [{}], Collection id: [{}]", databaseLink,
                    collection.getId());
            validateResource(collection);

            String path = Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Create,
                    ResourceType.DocumentCollection, path, collection, requestHeaders);
            return this.create(request).map(response -> toResourceResponse(response, DocumentCollection.class));
        } catch (Exception e) {
            logger.debug("Failure in creating a collection. due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> replaceCollection(DocumentCollection collection,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceCollectionInternal(collection, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<DocumentCollection>> replaceCollectionInternal(DocumentCollection collection,
            RequestOptions options) {
        try {
            if (collection == null) {
                throw new IllegalArgumentException("collection");
            }

            logger.debug("Replacing a Collection. id: [{}]", collection.getId());
            validateResource(collection);

            String path = Utils.joinPath(collection.getSelfLink(), null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.DocumentCollection, path, collection, requestHeaders);

            // TODO: .Net has some logic for updating session token which we don't
            // have here

            return this.replace(request).map(response -> toResourceResponse(response, DocumentCollection.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a collection. due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> deleteCollection(String collectionLink,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteCollectionInternal(collectionLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<DocumentCollection>> deleteCollectionInternal(String collectionLink,
            RequestOptions options) {
        try {
            if (StringUtils.isEmpty(collectionLink)) {
                throw new IllegalArgumentException("collectionLink");
            }

            logger.debug("Deleting a Collection. collectionLink: [{}]", collectionLink);
            String path = Utils.joinPath(collectionLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.DocumentCollection, path, requestHeaders);
            return this.delete(request).map(response -> toResourceResponse(response, DocumentCollection.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a collection, due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    private Observable<RxDocumentServiceResponse> delete(RxDocumentServiceRequest request) {
        populateHeaders(request, HttpConstants.HttpMethods.DELETE);
        applySessionToken(request);

        return gatewayProxy.delete(request).doOnNext(response -> {
            if (request.getResourceType() != ResourceType.DocumentCollection) {
                captureSessionToken(request, response);
            } else {
                clearToken(request, response);
            }
        });
    }

    private Observable<RxDocumentServiceResponse> read(RxDocumentServiceRequest request) {

        populateHeaders(request, HttpConstants.HttpMethods.GET);
        applySessionToken(request);
        return gatewayProxy.processMessage(request).doOnNext(response -> {
            this.captureSessionToken(request, response);
        });
    }

    Observable<RxDocumentServiceResponse> readFeed(RxDocumentServiceRequest request) {
        populateHeaders(request, HttpConstants.HttpMethods.GET);
        if (!request.isChangeFeedRequest()) {
            applySessionToken(request);
        }

        return gatewayProxy.processMessage(request)
                .doOnNext(response -> {
                    captureSessionToken(request, response);
                });
    }

    private Observable<RxDocumentServiceResponse> query(RxDocumentServiceRequest request) {
        populateHeaders(request, HttpConstants.HttpMethods.POST);
        applySessionToken(request);

        return this.gatewayProxy.processMessage(request)
                .doOnNext(response -> { captureSessionToken(request, response);
        });
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> readCollection(String collectionLink,
            RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readCollectionInternal(collectionLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<DocumentCollection>> readCollectionInternal(String collectionLink,
            RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {

        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (StringUtils.isEmpty(collectionLink)) {
                throw new IllegalArgumentException("collectionLink");
            }

            logger.debug("Reading a Collection. collectionLink: [{}]", collectionLink);
            String path = Utils.joinPath(collectionLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.DocumentCollection, path, requestHeaders);
            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request).map(response -> toResourceResponse(response, DocumentCollection.class));
        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in reading a collection, due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<DocumentCollection>> readCollections(String databaseLink, FeedOptions options) {

        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return readFeed(options, ResourceType.DocumentCollection, DocumentCollection.class,
                Utils.joinPath(databaseLink, Paths.COLLECTIONS_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<DocumentCollection>> queryCollections(String databaseLink, String query,
            FeedOptions options) {
        return createQuery(databaseLink, new SqlQuerySpec(query), options, DocumentCollection.class, ResourceType.DocumentCollection);
    }

    @Override
    public Observable<FeedResponse<DocumentCollection>> queryCollections(String databaseLink,
            SqlQuerySpec querySpec, FeedOptions options) {
        return createQuery(databaseLink, querySpec, options, DocumentCollection.class, ResourceType.DocumentCollection);
    }

    private static String serializeProcedureParams(Object[] objectArray) {
        String[] stringArray = new String[objectArray.length];

        for (int i = 0; i < objectArray.length; ++i) {
            Object object = objectArray[i];
            if (object instanceof JsonSerializable) {
                stringArray[i] = ((JsonSerializable) object).toJson();
            } else if (object instanceof JSONObject) {
                stringArray[i] = object.toString();
            } else {

                // POJO, number, String or Boolean
                try {
                    stringArray[i] = mapper.writeValueAsString(object);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Can't serialize the object into the json string", e);
                }
            }
        }

        return String.format("[%s]", StringUtils.join(stringArray, ","));
    }

    private static void validateResource(Resource resource) {
        if (!StringUtils.isEmpty(resource.getId())) {
            if (resource.getId().indexOf('/') != -1 || resource.getId().indexOf('\\') != -1 ||
                    resource.getId().indexOf('?') != -1 || resource.getId().indexOf('#') != -1) {
                throw new IllegalArgumentException("Id contains illegal chars.");
            }

            if (resource.getId().endsWith(" ")) {
                throw new IllegalArgumentException("Id ends with a space.");
            }
        }
    }

    private Map<String, String> getRequestHeaders(RequestOptions options) {
        if (options == null)
            return null;

        Map<String, String> headers = new HashMap<>();

        if (options.getAccessCondition() != null) {
            if (options.getAccessCondition().getType() == AccessConditionType.IfMatch) {
                headers.put(HttpConstants.HttpHeaders.IF_MATCH, options.getAccessCondition().getCondition());
            } else {
                headers.put(HttpConstants.HttpHeaders.IF_NONE_MATCH, options.getAccessCondition().getCondition());
            }
        }

        if (options.getConsistencyLevel() != null) {
            headers.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, options.getConsistencyLevel().name());
        }

        if (options.getIndexingDirective() != null) {
            headers.put(HttpConstants.HttpHeaders.INDEXING_DIRECTIVE, options.getIndexingDirective().name());
        }

        if (options.getPostTriggerInclude() != null && options.getPostTriggerInclude().size() > 0) {
            String postTriggerInclude = StringUtils.join(options.getPostTriggerInclude(), ",");
            headers.put(HttpConstants.HttpHeaders.POST_TRIGGER_INCLUDE, postTriggerInclude);
        }

        if (options.getPreTriggerInclude() != null && options.getPreTriggerInclude().size() > 0) {
            String preTriggerInclude = StringUtils.join(options.getPreTriggerInclude(), ",");
            headers.put(HttpConstants.HttpHeaders.PRE_TRIGGER_INCLUDE, preTriggerInclude);
        }

        if (options.getSessionToken() != null && !options.getSessionToken().isEmpty()) {
            headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, options.getSessionToken());
        }

        if (options.getResourceTokenExpirySeconds() != null) {
            headers.put(HttpConstants.HttpHeaders.RESOURCE_TOKEN_EXPIRY,
                    String.valueOf(options.getResourceTokenExpirySeconds()));
        }

        if (options.getOfferThroughput() != null && options.getOfferThroughput() >= 0) {
            headers.put(HttpConstants.HttpHeaders.OFFER_THROUGHPUT, options.getOfferThroughput().toString());
        } else if (options.getOfferType() != null) {
            headers.put(HttpConstants.HttpHeaders.OFFER_TYPE, options.getOfferType());
        }

        if (options.getPartitionKey() != null) {
            headers.put(HttpConstants.HttpHeaders.PARTITION_KEY, options.getPartitionKey().toString());
        }

        if (options.isPopulateQuotaInfo()) {
            headers.put(HttpConstants.HttpHeaders.POPULATE_QUOTA_INFO, String.valueOf(true));
        }

        return headers;
    }

    private Map<String, String> getFeedHeaders(FeedOptionsBase options) {
        return BridgeInternal.getFeedHeaders(options);
    }

    private Map<String, String> getMediaHeaders(MediaOptions options) {
        Map<String, String> requestHeaders = new HashMap<>();

        if (options == null || StringUtils.isEmpty(options.getContentType())) {
            requestHeaders.put(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.OCTET_STREAM);
        }

        if (options != null) {
            if (!StringUtils.isEmpty(options.getContentType())) {
                requestHeaders.put(HttpConstants.HttpHeaders.CONTENT_TYPE, options.getContentType());
            }

            if (!StringUtils.isEmpty(options.getSlug())) {
                requestHeaders.put(HttpConstants.HttpHeaders.SLUG, options.getSlug());
            }
        }
        return requestHeaders;
    }

    private Single<RxDocumentServiceRequest> addPartitionKeyInformation(RxDocumentServiceRequest request, Document document,
            RequestOptions options) {

        Single<DocumentCollection> collectionObs = this.collectionCache.resolveCollectionAsync(request);
        return collectionObs
                .map(collection -> {
            addPartitionKeyInformation(request, document, options, collection);
            return request;
        });
    }

    private Single<RxDocumentServiceRequest> addPartitionKeyInformation(RxDocumentServiceRequest request, Document document, RequestOptions options,
            Single<DocumentCollection> collectionObs) {
        
        return collectionObs.map(collection -> {
            addPartitionKeyInformation(request, document, options, collection);
            return request;
        });
    }
    
    private void addPartitionKeyInformation(RxDocumentServiceRequest request, Document document, RequestOptions options,
            DocumentCollection collection) {
        PartitionKeyDefinition partitionKeyDefinition = collection.getPartitionKey();

        PartitionKeyInternal partitionKeyInternal = null;
        if (options != null && options.getPartitionKey() != null) {
            partitionKeyInternal = options.getPartitionKey().getInternalPartitionKey();
        } else if (partitionKeyDefinition == null || partitionKeyDefinition.getPaths().size() == 0) {
            // For backward compatibility, if collection doesn't have partition key defined, we assume all documents
            // have empty value for it and user doesn't need to specify it explicitly.
            partitionKeyInternal = PartitionKeyInternal.getEmpty();
        } else if (document != null) {
            partitionKeyInternal = extractPartitionKeyValueFromDocument(document, partitionKeyDefinition);
        } else {
            throw new UnsupportedOperationException("PartitionKey value must be supplied for this operation.");
        }

        request.getHeaders().put(HttpConstants.HttpHeaders.PARTITION_KEY, escapeNonAscii(partitionKeyInternal.toJson()));        
    }

    private static String escapeNonAscii(String partitionKeyJson) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partitionKeyJson.length(); i++) {
            int val = partitionKeyJson.charAt(i);
            if (val > 127) {
                sb.append("\\u" + String.format("%04X", val));
            } else {
                sb.append(partitionKeyJson.charAt(i));
            }
        }
        return sb.toString();
    }

    private static PartitionKeyInternal extractPartitionKeyValueFromDocument(
            Document document,
            PartitionKeyDefinition partitionKeyDefinition) {
        if (partitionKeyDefinition != null) {
            String path = partitionKeyDefinition.getPaths().iterator().next();
            Collection<String> parts = PathParser.getPathParts(path);
            if (parts.size() >= 1) {
                Object value = document.getObjectByPath(parts);
                if (value == null || value.getClass() == JSONObject.class) {
                    value = Undefined.Value();
                }

                return PartitionKeyInternal.fromObjectArray(Arrays.asList(value), false);
            }
        }

        return null;
    }
    
    private Single<RxDocumentServiceRequest> getCreateDocumentRequest(String documentCollectionLink, Object document,
            RequestOptions options, boolean disableAutomaticIdGeneration, OperationType operationType) {

        if (StringUtils.isEmpty(documentCollectionLink)) {
            throw new IllegalArgumentException("documentCollectionLink");
        }
        if (document == null) {
            throw new IllegalArgumentException("document");
        }

        Document typedDocument = documentFromObject(document, mapper);

        RxDocumentClientImpl.validateResource(typedDocument);

        if (typedDocument.getId() == null && !disableAutomaticIdGeneration) {
            // We are supposed to use GUID. Basically UUID is the same as GUID
            // when represented as a string.
            typedDocument.setId(UUID.randomUUID().toString());
        }
        String path = Utils.joinPath(documentCollectionLink, Paths.DOCUMENTS_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options);

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType, ResourceType.Document, path,
                typedDocument, requestHeaders);


        Single<DocumentCollection> collectionObs = this.collectionCache.resolveCollectionAsync(request);
        return addPartitionKeyInformation(request, typedDocument, options, collectionObs);
    }

    private void populateHeaders(RxDocumentServiceRequest request, String httpMethod) {
        if (this.masterKey != null) {     
            request.getHeaders().put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        }

        if (this.masterKey != null || this.resourceTokens != null) {
            String resourceName = request.getResourceFullName();

            String authorization = this.getUserAuthorizationToken(
                    resourceName, request.getResourceType(), httpMethod,
                    request.getHeaders(), AuthorizationTokenType.PrimaryMasterKey);
            try {
                authorization = URLEncoder.encode(authorization, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Failed to encode authtoken.", e);
            }
            request.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorization);
        }

        if ((HttpConstants.HttpMethods.POST.equals(httpMethod) || HttpConstants.HttpMethods.PUT.equals(httpMethod))
                && !request.getHeaders().containsKey(HttpConstants.HttpHeaders.CONTENT_TYPE)) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE, RuntimeConstants.MediaTypes.JSON);
        }

        if (!request.getHeaders().containsKey(HttpConstants.HttpHeaders.ACCEPT)) {
            request.getHeaders().put(HttpConstants.HttpHeaders.ACCEPT, RuntimeConstants.MediaTypes.JSON);
        }
    }

    @Override
    public String getUserAuthorizationToken(String resourceAddress, ResourceType resourceType, String requestVerb,
            Map<String, String> headers, AuthorizationTokenType tokenType) {
        if (masterKey != null) {
            return this.authorizationTokenProvider.generateKeyAuthorizationSignature(requestVerb, resourceAddress,
                    resourceType, headers);
        } else {
            assert resourceTokens != null;
            return this.authorizationTokenProvider.getAuthorizationTokenUsingResourceTokens(resourceTokens, resourceAddress,
                    resourceAddress);
        }
    }

    private void applySessionToken(RxDocumentServiceRequest request) {
        Map<String, String> headers = request.getHeaders();
        if (headers != null && !StringUtils.isEmpty(headers.get(HttpConstants.HttpHeaders.SESSION_TOKEN))) {
            if (request.getResourceType().isMasterResource()) {
                headers.remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
            }
            return; // User is explicitly controlling the session.
        }

        String requestConsistency = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);
        boolean sessionConsistency = this.consistencyLevel == ConsistencyLevel.Session
                || (!StringUtils.isEmpty(requestConsistency)
                        && StringUtils.equalsIgnoreCase(requestConsistency, ConsistencyLevel.Session.toString()));
        if (!sessionConsistency || request.getResourceType().isMasterResource()) {
            return; // Only apply the session token in case of session consistency
        }

        // Apply the ambient session.
        if (!StringUtils.isEmpty(request.getResourceAddress())) {
            String sessionToken = this.sessionContainer.resolveGlobalSessionToken(request);

            if (!StringUtils.isEmpty(sessionToken)) {
                headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
            }
        }
    }

    void captureSessionToken(RxDocumentServiceRequest request, RxDocumentServiceResponse response) {
        this.sessionContainer.setSessionToken(request, response);
    }

    void clearToken(RxDocumentServiceRequest request, RxDocumentServiceResponse response) {
        this.sessionContainer.clearToken(request);
    }

    private Observable<RxDocumentServiceResponse> create(RxDocumentServiceRequest request) {
        populateHeaders(request, HttpConstants.HttpMethods.POST);
        applySessionToken(request);
        return gatewayProxy.processMessage(request)
                .doOnNext(response -> { captureSessionToken(request, response); });
    }

    private Observable<RxDocumentServiceResponse> upsert(RxDocumentServiceRequest request) {

        populateHeaders(request, HttpConstants.HttpMethods.POST);
        applySessionToken(request);
        Map<String, String> headers = request.getHeaders();
        // headers can never be null, since it will be initialized even when no
        // request options are specified,
        // hence using assertion here instead of exception, being in the private
        // method
        assert (headers != null);
        headers.put(HttpConstants.HttpHeaders.IS_UPSERT, "true");
        
        return gatewayProxy.processMessage(request)
        .doOnNext(response -> {
            captureSessionToken(request, response);
        });
    }

    private Observable<RxDocumentServiceResponse> replace(RxDocumentServiceRequest request) {
        populateHeaders(request, HttpConstants.HttpMethods.PUT);
        applySessionToken(request);
        return gatewayProxy.replace(request)
                .doOnNext(response -> {
                    captureSessionToken(request, response);
                });
    }

    @Override
    public Observable<ResourceResponse<Document>> createDocument(String collectionLink, Object document,
            RequestOptions options, boolean disableAutomaticIdGeneration) {

        IDocumentClientRetryPolicy requestRetryPolicy = this.retryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink);
        }

        return ObservableHelper.inlineIfPossibleAsObs(() -> createDocumentInternal(collectionLink, document, options, disableAutomaticIdGeneration), requestRetryPolicy);
    }
    
    
    private Observable<ResourceResponse<Document>> createDocumentInternal(String collectionLink, Object document,
            RequestOptions options, final boolean disableAutomaticIdGeneration) {

        try {
            logger.debug("Creating a Document. collectionLink: [{}]", collectionLink);

            Single<RxDocumentServiceRequest> requestObs = getCreateDocumentRequest(collectionLink, document,
                    options, disableAutomaticIdGeneration, OperationType.Create);

            Observable<RxDocumentServiceResponse> responseObservable = requestObs
                    .toObservable()
                    .flatMap(req -> { 
                        return create(req);
                    });

            Observable<ResourceResponse<Document>> createObservable = 
                    responseObservable
                    .map(serviceResponse -> {
                        return toResourceResponse(serviceResponse, Document.class);
                    });

            return createObservable;

        } catch (Exception e) {
            logger.debug("Failure in creating a document due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Document>> upsertDocument(String collectionLink, Object document,
            RequestOptions options, boolean disableAutomaticIdGeneration) {

        IDocumentClientRetryPolicy requestRetryPolicy = this.retryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink);
        }
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertDocumentInternal(collectionLink, document, options, disableAutomaticIdGeneration), requestRetryPolicy);
    }

    private Observable<ResourceResponse<Document>> upsertDocumentInternal(String collectionLink, Object document,
            RequestOptions options, boolean disableAutomaticIdGeneration) {
        try {
            logger.debug("Upserting a Document. collectionLink: [{}]", collectionLink);

            Observable<RxDocumentServiceRequest> reqObs = getCreateDocumentRequest(collectionLink, document,
                    options, disableAutomaticIdGeneration, OperationType.Upsert).toObservable();

            Observable<RxDocumentServiceResponse> responseObservable = reqObs.flatMap(req -> upsert(req));
            return responseObservable
                    .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));

        } catch (Exception e) {
            logger.debug("Failure in upserting a document due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Document>> replaceDocument(String documentLink, Object document,
            RequestOptions options) {

        IDocumentClientRetryPolicy requestRetryPolicy = this.retryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            String collectionLink = Utils.getCollectionName(documentLink);
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink);
        }
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceDocumentInternal(documentLink, document, options), requestRetryPolicy);
    }

    private Observable<ResourceResponse<Document>> replaceDocumentInternal(String documentLink, Object document,
            RequestOptions options) {
        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            if (document == null) {
                throw new IllegalArgumentException("document");
            }

            Document typedDocument = documentFromObject(document, mapper);

            return this.replaceDocumentInternal(documentLink, typedDocument, options);

        } catch (Exception e) {
            logger.debug("Failure in replacing a document due to [{}]", e.getMessage());
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Document>> replaceDocument(Document document, RequestOptions options) {
        IDocumentClientRetryPolicy requestRetryPolicy = this.retryPolicy.getRequestPolicy();
        if (options == null || options.getPartitionKey() == null) {
            String collectionLink = document.getSelfLink();
            requestRetryPolicy = new PartitionKeyMismatchRetryPolicy(collectionCache, requestRetryPolicy, collectionLink);
        }
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceDocumentInternal(document, options), requestRetryPolicy);
    }

    private Observable<ResourceResponse<Document>> replaceDocumentInternal(Document document, RequestOptions options) {

        try {
            if (document == null) {
                throw new IllegalArgumentException("document");
            }

            return this.replaceDocumentInternal(document.getSelfLink(), document, options);

        } catch (Exception e) {
            logger.debug("Failure in replacing a database due to [{}]", e.getMessage());
            return Observable.error(e);
        }
    }

    private Observable<ResourceResponse<Document>> replaceDocumentInternal(String documentLink, Document document,
            RequestOptions options) throws DocumentClientException {

        if (document == null) {
            throw new IllegalArgumentException("document");
        }

        logger.debug("Replacing a Document. documentLink: [{}]", documentLink);
        final String path = Utils.joinPath(documentLink, null);
        final Map<String, String> requestHeaders = getRequestHeaders(options);
        final RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                ResourceType.Document, path, document, requestHeaders);

        validateResource(document);

        Single<DocumentCollection> collectionObs = collectionCache.resolveCollectionAsync(request);
        Single<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, document, options, collectionObs);

        return requestObs.toObservable().flatMap(req -> replace(request)
                .map(resp -> toResourceResponse(resp, Document.class)) );
    }
    
    @Override
    public Observable<ResourceResponse<Document>> deleteDocument(String documentLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteDocumentInternal(documentLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Document>> deleteDocumentInternal(String documentLink, RequestOptions options) {
        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            logger.debug("Deleting a Document. documentLink: [{}]", documentLink);
            String path = Utils.joinPath(documentLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.Document, path, requestHeaders);

            Single<DocumentCollection> collectionObs = collectionCache.resolveCollectionAsync(request);

            Single<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, null, options, collectionObs);

            return requestObs.toObservable().flatMap(req -> this.delete(req)
                    .map(serviceResponse -> toResourceResponse(serviceResponse, Document.class)));

        } catch (Exception e) {
            logger.debug("Failure in deleting a document due to [{}]", e.getMessage());
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Document>> readDocument(String documentLink, RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readDocumentInternal(documentLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<Document>> readDocumentInternal(String documentLink, RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(documentLink)) {
                throw new IllegalArgumentException("documentLink");
            }

            logger.debug("Reading a Document. documentLink: [{}]", documentLink);
            String path = Utils.joinPath(documentLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Document, path, requestHeaders);


            Single<DocumentCollection> collectionObs = this.collectionCache.resolveCollectionAsync(request);

            Single<RxDocumentServiceRequest> requestObs = addPartitionKeyInformation(request, null, options, collectionObs);

            return requestObs.toObservable().flatMap(req -> {
                if (retryPolicyInstance != null) {
                    retryPolicyInstance.onBeforeSendRequest(request);
                }
                return this.read(request).map(serviceResponse -> toResourceResponse(serviceResponse, Document.class));
            });
            
        } catch (Exception e) {
            logger.debug("Failure in reading a document due to [{}]", e.getMessage());
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<Document>> readDocuments(String collectionLink, FeedOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }
        
        return readFeedCollectionChild(options, ResourceType.Document, Document.class,
                Utils.joinPath(collectionLink, Paths.DOCUMENTS_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<Document>> queryDocuments(String collectionLink, String query,
            FeedOptions options) {
        return queryDocuments(collectionLink, new SqlQuerySpec(query), options);
    }

    /**
     * @param rxDocumentClientImpl
     * @return
     */
    private IDocumentQueryClient DocumentQueryClientImpl(RxDocumentClientImpl rxDocumentClientImpl) {

        return new IDocumentQueryClient () {

            @Override
            public RxCollectionCache getCollectionCache() {
                return RxDocumentClientImpl.this.collectionCache;
            }

            @Override
            public RxPartitionKeyRangeCache getPartitionKeyRangeCache() {
                return RxDocumentClientImpl.this.partitionKeyRangeCache;
            }

            @Override
            public IRetryPolicyFactory getRetryPolicyFactory() {
                return RxDocumentClientImpl.this.retryPolicy;
            }

            @Override
            public ConsistencyLevel getDefaultConsistencyLevelAsync() {
                // TODO Auto-generated method stub
                return RxDocumentClientImpl.this.consistencyLevel;
            }

            @Override
            public ConsistencyLevel getDesiredConsistencyLevelAsync() {
                // TODO Auto-generated method stub
                return RxDocumentClientImpl.this.consistencyLevel;
            }

            @Override
            public Single<RxDocumentServiceResponse> executeQueryAsync(RxDocumentServiceRequest request) {
                return RxDocumentClientImpl.this.query(request).map(r -> (RxDocumentServiceResponse)r).toSingle();
            }

            @Override
            public QueryCompatibilityMode getQueryCompatibilityMode() {
                // TODO Auto-generated method stub
                return QueryCompatibilityMode.Default;
            }

            @Override
            public Single<RxDocumentServiceResponse> readFeedAsync(RxDocumentServiceRequest request) {
                // TODO Auto-generated method stub
                return null;
            } 
        };
    }

    @Override
    public Observable<FeedResponse<Document>> queryDocuments(String collectionLink, SqlQuerySpec querySpec,
            FeedOptions options) {
        return createQuery(collectionLink, querySpec, options, Document.class, ResourceType.Document);
    }

    @Override
    public Observable<FeedResponse<Document>> queryDocumentChangeFeed(final String collectionLink,
            final ChangeFeedOptions changeFeedOptions) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        ChangeFeedQueryImpl<Document> changeFeedQueryImpl = new ChangeFeedQueryImpl<Document>(this, ResourceType.Document, 
                Document.class, collectionLink, changeFeedOptions);

        return changeFeedQueryImpl.executeAsync();
    }

    @Override
    public Observable<FeedResponse<PartitionKeyRange>> readPartitionKeyRanges(final String collectionLink,
            FeedOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.PartitionKeyRange, PartitionKeyRange.class,
                Utils.joinPath(collectionLink, Paths.PARTITION_KEY_RANGE_PATH_SEGMENT));        
    }

    private RxDocumentServiceRequest getStoredProcedureRequest(String collectionLink, StoredProcedure storedProcedure,
            RequestOptions options, OperationType operationType) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }
        if (storedProcedure == null) {
            throw new IllegalArgumentException("storedProcedure");
        }

        validateResource(storedProcedure);

        String path = Utils.joinPath(collectionLink, Paths.STORED_PROCEDURES_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType, ResourceType.StoredProcedure,
                path, storedProcedure, requestHeaders);
        return request;
    }

    private RxDocumentServiceRequest getUserDefinedFunctionRequest(String collectionLink, UserDefinedFunction udf,
            RequestOptions options, OperationType operationType) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }
        if (udf == null) {
            throw new IllegalArgumentException("udf");
        }

        validateResource(udf);

        String path = Utils.joinPath(collectionLink, Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getRequestHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType,
                ResourceType.UserDefinedFunction, path, udf, requestHeaders);
        return request;
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> createStoredProcedure(String collectionLink,
            StoredProcedure storedProcedure, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> createStoredProcedureInternal(collectionLink, storedProcedure, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<StoredProcedure>> createStoredProcedureInternal(String collectionLink,
            StoredProcedure storedProcedure, RequestOptions options) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            logger.debug("Creating a StoredProcedure. collectionLink: [{}], storedProcedure id [{}]",
                    collectionLink, storedProcedure.getId());
            RxDocumentServiceRequest request = getStoredProcedureRequest(collectionLink, storedProcedure, options,
                    OperationType.Create);

            return this.create(request).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in creating a StoredProcedure due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> upsertStoredProcedure(String collectionLink,
            StoredProcedure storedProcedure, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertStoredProcedureInternal(collectionLink, storedProcedure, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<StoredProcedure>> upsertStoredProcedureInternal(String collectionLink,
            StoredProcedure storedProcedure, RequestOptions options) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            logger.debug("Upserting a StoredProcedure. collectionLink: [{}], storedProcedure id [{}]",
                    collectionLink, storedProcedure.getId());
            RxDocumentServiceRequest request = getStoredProcedureRequest(collectionLink, storedProcedure, options,
                    OperationType.Upsert);

            return this.upsert(request).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in upserting a StoredProcedure due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> replaceStoredProcedure(StoredProcedure storedProcedure,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceStoredProcedureInternal(storedProcedure, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<StoredProcedure>> replaceStoredProcedureInternal(StoredProcedure storedProcedure,
            RequestOptions options) {
        try {

            if (storedProcedure == null) {
                throw new IllegalArgumentException("storedProcedure");
            }
            logger.debug("Replacing a StoredProcedure. storedProcedure id [{}]", storedProcedure.getId());

            RxDocumentClientImpl.validateResource(storedProcedure);

            String path = Utils.joinPath(storedProcedure.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.StoredProcedure, path, storedProcedure, requestHeaders);
            return this.replace(request).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a StoredProcedure due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> deleteStoredProcedure(String storedProcedureLink,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteStoredProcedureInternal(storedProcedureLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<StoredProcedure>> deleteStoredProcedureInternal(String storedProcedureLink,
            RequestOptions options) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            if (StringUtils.isEmpty(storedProcedureLink)) {
                throw new IllegalArgumentException("storedProcedureLink");
            }

            logger.debug("Deleting a StoredProcedure. storedProcedureLink [{}]", storedProcedureLink);
            String path = Utils.joinPath(storedProcedureLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.StoredProcedure, path, requestHeaders);

            return this.delete(request).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in deleting a StoredProcedure due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> readStoredProcedure(String storedProcedureLink,
            RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readStoredProcedureInternal(storedProcedureLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<StoredProcedure>> readStoredProcedureInternal(String storedProcedureLink,
            RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {

        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {

            if (StringUtils.isEmpty(storedProcedureLink)) {
                throw new IllegalArgumentException("storedProcedureLink");
            }

            logger.debug("Reading a StoredProcedure. storedProcedureLink [{}]", storedProcedureLink);
            String path = Utils.joinPath(storedProcedureLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.StoredProcedure, path, requestHeaders);

            if (retryPolicyInstance != null){
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.read(request).map(response -> toResourceResponse(response, StoredProcedure.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in reading a StoredProcedure due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<StoredProcedure>> readStoredProcedures(String collectionLink,
            FeedOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.StoredProcedure, StoredProcedure.class,
                Utils.joinPath(collectionLink, Paths.STORED_PROCEDURES_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink, String query,
            FeedOptions options) {
        return queryStoredProcedures(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<StoredProcedure>> queryStoredProcedures(String collectionLink,
            SqlQuerySpec querySpec, FeedOptions options) {
        return createQuery(collectionLink, querySpec, options, StoredProcedure.class, ResourceType.StoredProcedure);
    }

    @Override
    public Observable<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink,
            Object[] procedureParams) {
        return this.executeStoredProcedure(storedProcedureLink, null, procedureParams);
    }

    @Override
    public Observable<StoredProcedureResponse> executeStoredProcedure(String storedProcedureLink,
            RequestOptions options, Object[] procedureParams) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> executeStoredProcedureInternal(storedProcedureLink, options, procedureParams), retryPolicy.getRequestPolicy());
    }

    private Observable<StoredProcedureResponse> executeStoredProcedureInternal(String storedProcedureLink,
            RequestOptions options, Object[] procedureParams) {

        try {
            logger.debug("Executing a StoredProcedure. storedProcedureLink [{}]", storedProcedureLink);
            String path = Utils.joinPath(storedProcedureLink, null);

            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put(HttpConstants.HttpHeaders.ACCEPT, RuntimeConstants.MediaTypes.JSON);
            if (options != null) {
                if (options.getPartitionKey() != null) {
                    requestHeaders.put(HttpConstants.HttpHeaders.PARTITION_KEY,
                            options.getPartitionKey().toString());
                }
                if (options.isScriptLoggingEnabled()) {
                    requestHeaders.put(HttpConstants.HttpHeaders.SCRIPT_ENABLE_LOGGING, String.valueOf(true));
                }
            }

            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.ExecuteJavaScript,
                    ResourceType.StoredProcedure, path,
                    procedureParams != null ? RxDocumentClientImpl.serializeProcedureParams(procedureParams) : "",
                            requestHeaders);
            Observable<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, options).toObservable();
            return reqObs.flatMap(req -> create(request).map(response -> toStoredProcedureResponse(response)));

        } catch (Exception e) {
            logger.debug("Failure in executing a StoredProcedure due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Trigger>> createTrigger(String collectionLink, Trigger trigger,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> createTriggerInternal(collectionLink, trigger, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Trigger>> createTriggerInternal(String collectionLink, Trigger trigger,
            RequestOptions options) {
        try {

            logger.debug("Creating a Trigger. collectionLink [{}], trigger id [{}]", collectionLink,
                    trigger.getId());
            RxDocumentServiceRequest request = getTriggerRequest(collectionLink, trigger, options,
                    OperationType.Create);
            return this.create(request).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a Trigger due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Trigger>> upsertTrigger(String collectionLink, Trigger trigger,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertTriggerInternal(collectionLink, trigger, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Trigger>> upsertTriggerInternal(String collectionLink, Trigger trigger,
            RequestOptions options) {
        try {

            logger.debug("Upserting a Trigger. collectionLink [{}], trigger id [{}]", collectionLink,
                    trigger.getId());
            RxDocumentServiceRequest request = getTriggerRequest(collectionLink, trigger, options,
                    OperationType.Upsert);
            return this.upsert(request).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in upserting a Trigger due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    private RxDocumentServiceRequest getTriggerRequest(String collectionLink, Trigger trigger, RequestOptions options,
            OperationType operationType) {
        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("trigger");
        }

        RxDocumentClientImpl.validateResource(trigger);

        String path = Utils.joinPath(collectionLink, Paths.TRIGGERS_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType, ResourceType.Trigger, path,
                trigger, requestHeaders);
        return request;
    }

    @Override
    public Observable<ResourceResponse<Trigger>> replaceTrigger(Trigger trigger, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceTriggerInternal(trigger, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Trigger>> replaceTriggerInternal(Trigger trigger, RequestOptions options) {

        try {
            if (trigger == null) {
                throw new IllegalArgumentException("trigger");
            }

            logger.debug("Replacing a Trigger. trigger id [{}]", trigger.getId());
            RxDocumentClientImpl.validateResource(trigger);

            String path = Utils.joinPath(trigger.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.Trigger, path, trigger, requestHeaders);
            return this.replace(request).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a Trigger due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Trigger>> deleteTrigger(String triggerLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteTriggerInternal(triggerLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Trigger>> deleteTriggerInternal(String triggerLink, RequestOptions options) {
        try {
            if (StringUtils.isEmpty(triggerLink)) {
                throw new IllegalArgumentException("triggerLink");
            }

            logger.debug("Deleting a Trigger. triggerLink [{}]", triggerLink);
            String path = Utils.joinPath(triggerLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.Trigger, path, requestHeaders);
            return this.delete(request).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a Trigger due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Trigger>> readTrigger(String triggerLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> readTriggerInternal(triggerLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Trigger>> readTriggerInternal(String triggerLink, RequestOptions options) {

        try {
            if (StringUtils.isEmpty(triggerLink)) {
                throw new IllegalArgumentException("triggerLink");
            }

            logger.debug("Reading a Trigger. triggerLink [{}]", triggerLink);
            String path = Utils.joinPath(triggerLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Trigger, path, requestHeaders);
            return this.read(request).map(response -> toResourceResponse(response, Trigger.class));

        } catch (Exception e) {
            logger.debug("Failure in reading a Trigger due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<Trigger>> readTriggers(String collectionLink, FeedOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.Trigger, Trigger.class,
                Utils.joinPath(collectionLink, Paths.TRIGGERS_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<Trigger>> queryTriggers(String collectionLink, String query,
            FeedOptions options) {
        return queryTriggers(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<Trigger>> queryTriggers(String collectionLink, SqlQuerySpec querySpec,
            FeedOptions options) {
        return createQuery(collectionLink, querySpec, options, Trigger.class, ResourceType.Trigger);
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> createUserDefinedFunction(String collectionLink,
            UserDefinedFunction udf, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> createUserDefinedFunctionInternal(collectionLink, udf, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<UserDefinedFunction>> createUserDefinedFunctionInternal(String collectionLink,
            UserDefinedFunction udf, RequestOptions options) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            logger.debug("Creating a UserDefinedFunction. collectionLink [{}], udf id [{}]", collectionLink,
                    udf.getId());
            RxDocumentServiceRequest request = getUserDefinedFunctionRequest(collectionLink, udf, options,
                    OperationType.Create);

            return this.create(request).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in creating a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> upsertUserDefinedFunction(String collectionLink,
            UserDefinedFunction udf, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertUserDefinedFunctionInternal(collectionLink, udf, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<UserDefinedFunction>> upsertUserDefinedFunctionInternal(String collectionLink,
            UserDefinedFunction udf, RequestOptions options) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            logger.debug("Upserting a UserDefinedFunction. collectionLink [{}], udf id [{}]", collectionLink,
                    udf.getId());
            RxDocumentServiceRequest request = getUserDefinedFunctionRequest(collectionLink, udf, options,
                    OperationType.Upsert);
            return this.upsert(request).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in upserting a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunction(UserDefinedFunction udf,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceUserDefinedFunctionInternal(udf, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunctionInternal(UserDefinedFunction udf,
            RequestOptions options) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (udf == null) {
                throw new IllegalArgumentException("udf");
            }

            logger.debug("Replacing a UserDefinedFunction. udf id [{}]", udf.getId());
            validateResource(udf);

            String path = Utils.joinPath(udf.getSelfLink(), null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.UserDefinedFunction, path, udf, requestHeaders);
            return this.replace(request).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in replacing a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> deleteUserDefinedFunction(String udfLink,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteUserDefinedFunctionInternal(udfLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<UserDefinedFunction>> deleteUserDefinedFunctionInternal(String udfLink,
            RequestOptions options) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (StringUtils.isEmpty(udfLink)) {
                throw new IllegalArgumentException("udfLink");
            }

            logger.debug("Deleting a UserDefinedFunction. udfLink [{}]", udfLink);
            String path = Utils.joinPath(udfLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.UserDefinedFunction, path, requestHeaders);
            return this.delete(request).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in deleting a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> readUserDefinedFunction(String udfLink,
            RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readUserDefinedFunctionInternal(udfLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<UserDefinedFunction>> readUserDefinedFunctionInternal(String udfLink,
            RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {
        // we are using an observable factory here
        // observable will be created fresh upon subscription
        // this is to ensure we capture most up to date information (e.g.,
        // session)
        try {
            if (StringUtils.isEmpty(udfLink)) {
                throw new IllegalArgumentException("udfLink");
            }

            logger.debug("Reading a UserDefinedFunction. udfLink [{}]", udfLink);
            String path = Utils.joinPath(udfLink, null);
            Map<String, String> requestHeaders = this.getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.UserDefinedFunction, path, requestHeaders);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.read(request).map(response -> toResourceResponse(response, UserDefinedFunction.class));

        } catch (Exception e) {
            // this is only in trace level to capture what's going on
            logger.debug("Failure in reading a UserDefinedFunction due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<UserDefinedFunction>> readUserDefinedFunctions(String collectionLink,
            FeedOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.UserDefinedFunction, UserDefinedFunction.class,
                Utils.joinPath(collectionLink, Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink,
            String query, FeedOptions options) {
        return queryUserDefinedFunctions(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<UserDefinedFunction>> queryUserDefinedFunctions(String collectionLink,
            SqlQuerySpec querySpec, FeedOptions options) {
        return createQuery(collectionLink, querySpec, options, UserDefinedFunction.class, ResourceType.UserDefinedFunction);
    }


    @Override
    public Observable<ResourceResponse<Attachment>> createAttachment(String documentLink, Attachment attachment,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> createAttachmentInternal(documentLink, attachment, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Attachment>> createAttachmentInternal(String documentLink, Attachment attachment,
            RequestOptions options) {

        try {
            logger.debug("Creating a Attachment. documentLink [{}], attachment id [{}]", documentLink,
                    attachment.getId());
            Observable<RxDocumentServiceRequest> reqObs = getAttachmentRequest(documentLink, attachment, options,
                    OperationType.Create).toObservable();
            return reqObs.flatMap(req -> create(req).map(response -> toResourceResponse(response, Attachment.class)));

        } catch (Exception e) {
            logger.debug("Failure in creating a Attachment due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }


    @Override
    public Observable<ResourceResponse<Attachment>> upsertAttachment(String documentLink, Attachment attachment,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertAttachmentInternal(documentLink, attachment, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Attachment>> upsertAttachmentInternal(String documentLink, Attachment attachment,
            RequestOptions options) {

        try {
            logger.debug("Upserting a Attachment. documentLink [{}], attachment id [{}]", documentLink,
                    attachment.getId());
            Observable<RxDocumentServiceRequest> reqObs = getAttachmentRequest(documentLink, attachment, options,
                    OperationType.Upsert).toObservable();
            return reqObs.flatMap(req -> upsert(req).map(response -> toResourceResponse(response, Attachment.class)));

        } catch (Exception e) {
            logger.debug("Failure in upserting a Attachment due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Attachment>> replaceAttachment(Attachment attachment, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceAttachmentInternal(attachment, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Attachment>> replaceAttachmentInternal(Attachment attachment, RequestOptions options) {
        try {
            if (attachment == null) {
                throw new IllegalArgumentException("attachment");
            }

            logger.debug("Replacing a Attachment. attachment id [{}]", attachment.getId());
            RxDocumentClientImpl.validateResource(attachment);

            String path = Utils.joinPath(attachment.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.Attachment, path, attachment, requestHeaders);
            Observable<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, options).toObservable();
            return reqObs.flatMap(req -> replace(request).map(response -> toResourceResponse(response, Attachment.class)));
        } catch (Exception e) {
            logger.debug("Failure in replacing a Attachment due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Attachment>> deleteAttachment(String attachmentLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteAttachmentInternal(attachmentLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Attachment>> deleteAttachmentInternal(String attachmentLink, RequestOptions options) {

        try {
            if (StringUtils.isEmpty(attachmentLink)) {
                throw new IllegalArgumentException("attachmentLink");
            }

            logger.debug("Deleting a Attachment. attachmentLink [{}]", attachmentLink);
            String path = Utils.joinPath(attachmentLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.Attachment, path, requestHeaders);
            
            Observable<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, options).toObservable();
            return reqObs.flatMap(req -> delete(req).map(resp -> toResourceResponse(resp, Attachment.class)));
            
        } catch (Exception e) {
            logger.debug("Failure in deleting a Attachment due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Attachment>> readAttachment(String attachmentLink, RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readAttachmentInternal(attachmentLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<Attachment>> readAttachmentInternal(String attachmentLink, RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (StringUtils.isEmpty(attachmentLink)) {
                throw new IllegalArgumentException("attachmentLink");
            }

            logger.debug("Reading a Attachment. attachmentLink [{}]", attachmentLink);
            String path = Utils.joinPath(attachmentLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Attachment, path, requestHeaders);
            Observable<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, options).toObservable();

            return reqObs.flatMap(req -> {

                if (retryPolicyInstance != null) {
                    retryPolicyInstance.onBeforeSendRequest(request);
                }

                return read(request).map(response -> toResourceResponse(response, Attachment.class)); 
            });

        } catch (Exception e) {
            logger.debug("Failure in reading a Attachment due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<Attachment>> readAttachments(String documentLink, FeedOptions options) {

        if (StringUtils.isEmpty(documentLink)) {
            throw new IllegalArgumentException("documentLink");
        }

        return readFeedCollectionChild(options, ResourceType.Attachment, Attachment.class,
                Utils.joinPath(documentLink, Paths.ATTACHMENTS_PATH_SEGMENT));
    }

    @Override
    public Observable<MediaResponse> readMedia(String mediaLink) {
        if (StringUtils.isEmpty(mediaLink)) {
            throw new IllegalArgumentException("mediaLink");
        }

        String targetPath = Utils.joinPath(mediaLink, null);
        return ObservableHelper.inlineIfPossibleAsObs(() -> readMediaInternal(targetPath), retryPolicy.getRequestPolicy());
    }

    private Observable<MediaResponse> readMediaInternal(String mediaLink) {
        logger.debug("Reading a Media. mediaLink [{}]", mediaLink);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read, ResourceType.Media, mediaLink, null);
        request.setIsMedia(true);

        // Media is strong consistent always -> no need of session handling
        populateHeaders(request, HttpConstants.HttpMethods.GET);
        return gatewayProxy.processMessage(request).map(response ->
                BridgeInternal.toMediaResponse(response, this.connectionPolicy.getMediaReadMode() == MediaReadMode.Buffered));
    }

    @Override
    public Observable<MediaResponse> updateMedia(String mediaLink, InputStream mediaStream, MediaOptions options) {
        if (StringUtils.isEmpty(mediaLink)) {
            throw new IllegalArgumentException("mediaLink");
        }
        if (mediaStream == null) {
            throw new IllegalArgumentException("mediaStream");
        }

        String targetPath = Utils.joinPath(mediaLink, null);
        return ObservableHelper.inlineIfPossibleAsObs(() -> updateMediaInternal(targetPath, mediaStream, options), retryPolicy.getRequestPolicy());
    }

    private Observable<MediaResponse> updateMediaInternal(String mediaLink, InputStream mediaStream, MediaOptions options) {
        logger.debug("Updating a Media. mediaLink [{}]", mediaLink);
        Map<String, String> requestHeaders = this.getMediaHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace, ResourceType.Media,
                mediaLink,
                mediaStream,
                requestHeaders);
        request.setIsMedia(true);

        // Media is strong consistent always -> need of session handling
        populateHeaders(request, HttpConstants.HttpMethods.PUT);
        return gatewayProxy.processMessage(request).map(response ->
                BridgeInternal.toMediaResponse(response, this.connectionPolicy.getMediaReadMode() == MediaReadMode.Buffered));
    }

    @Override
    public Observable<FeedResponse<Attachment>> queryAttachments(String documentLink, String query,
            FeedOptions options) {
        return queryAttachments(documentLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<Attachment>> queryAttachments(String documentLink, SqlQuerySpec querySpec,
            FeedOptions options) {
        return createQuery(documentLink, querySpec, options, Attachment.class, ResourceType.Attachment);
    }

    private Single<RxDocumentServiceRequest> getAttachmentRequest(String documentLink, Attachment attachment,
            RequestOptions options, OperationType operationType) {
        if (StringUtils.isEmpty(documentLink)) {
            throw new IllegalArgumentException("documentLink");
        }
        if (attachment == null) {
            throw new IllegalArgumentException("attachment");
        }

        RxDocumentClientImpl.validateResource(attachment);

        String path = Utils.joinPath(documentLink, Paths.ATTACHMENTS_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType, ResourceType.Attachment, path,
                attachment, requestHeaders);
        return addPartitionKeyInformation(request, null, options);
    }

    @Override
    public Observable<ResourceResponse<Attachment>> createAttachment(String documentLink, InputStream mediaStream,
            MediaOptions options, RequestOptions requestOptions) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> createAttachmentInternal(documentLink, mediaStream, options, requestOptions), retryPolicyInstance);
    }

    private Observable<ResourceResponse<Attachment>> createAttachmentInternal(String documentLink, InputStream mediaStream,
            MediaOptions options, RequestOptions requestOptions) {

        try {
            logger.debug("Creating a Attachment. attachmentLink [{}]", documentLink);
            Observable<RxDocumentServiceRequest> reqObs = getAttachmentRequest(documentLink, mediaStream, options,
                    requestOptions, OperationType.Create).toObservable();
            return reqObs.flatMap(req -> create(req).map(response -> toResourceResponse(response, Attachment.class)));

        } catch (Exception e) {
            logger.debug("Failure in creating a Attachment due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Attachment>> upsertAttachment(String documentLink, InputStream mediaStream,
            MediaOptions options, RequestOptions requestOptions) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertAttachmentInternal(documentLink, mediaStream, options, requestOptions), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Attachment>> upsertAttachmentInternal(String documentLink, InputStream mediaStream,
            MediaOptions options, RequestOptions requestOptions) {

        try {
            logger.debug("Upserting a Attachment. attachmentLink [{}]", documentLink);
            Observable<RxDocumentServiceRequest> reqObs = getAttachmentRequest(documentLink, mediaStream, options,
                    requestOptions, OperationType.Upsert).toObservable();
            return reqObs.flatMap(req -> upsert(req).map(response -> toResourceResponse(response, Attachment.class)));

        } catch (Exception e) {
            logger.debug("Failure in upserting a Attachment due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    private Single<RxDocumentServiceRequest> getAttachmentRequest(String documentLink, InputStream mediaStream,
            MediaOptions options, RequestOptions requestOptions, OperationType operationType) {
        if (StringUtils.isEmpty(documentLink)) {
            throw new IllegalArgumentException("documentLink");
        }
        if (mediaStream == null) {
            throw new IllegalArgumentException("mediaStream");
        }
        String path = Utils.joinPath(documentLink, Paths.ATTACHMENTS_PATH_SEGMENT);
        Map<String, String> requestHeaders = this.getMediaHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType, ResourceType.Attachment, path,
                mediaStream, requestHeaders);
        request.setIsMedia(true);
        return addPartitionKeyInformation(request, null, requestOptions);
    }

    @Override
    public Observable<ResourceResponse<Conflict>> readConflict(String conflictLink, RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readConflictInternal(conflictLink, options, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<Conflict>> readConflictInternal(String conflictLink, RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {

        try {
            if (StringUtils.isEmpty(conflictLink)) {
                throw new IllegalArgumentException("conflictLink");
            }

            logger.debug("Reading a Conflict. conflictLink [{}]", conflictLink);
            String path = Utils.joinPath(conflictLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Conflict, path, requestHeaders);
            Observable<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, options).toObservable();

            return reqObs.flatMap(req -> {
                if (retryPolicyInstance != null) {
                    retryPolicyInstance.onBeforeSendRequest(request);
                }
                return this.read(request).map(response -> toResourceResponse(response, Conflict.class));
            });

        } catch (Exception e) {
            logger.debug("Failure in reading a Conflict due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<Conflict>> readConflicts(String collectionLink, FeedOptions options) {

        if (StringUtils.isEmpty(collectionLink)) {
            throw new IllegalArgumentException("collectionLink");
        }

        return readFeed(options, ResourceType.Conflict, Conflict.class,
                Utils.joinPath(collectionLink, Paths.CONFLICTS_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<Conflict>> queryConflicts(String collectionLink, String query,
            FeedOptions options) {
        return queryConflicts(collectionLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<Conflict>> queryConflicts(String collectionLink, SqlQuerySpec querySpec,
            FeedOptions options) {
        return createQuery(collectionLink, querySpec, options, Conflict.class, ResourceType.Conflict);
    }

    @Override
    public Observable<ResourceResponse<Conflict>> deleteConflict(String conflictLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteConflictInternal(conflictLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<Conflict>> deleteConflictInternal(String conflictLink, RequestOptions options) {

        try {
            if (StringUtils.isEmpty(conflictLink)) {
                throw new IllegalArgumentException("conflictLink");
            }

            logger.debug("Deleting a Conflict. conflictLink [{}]", conflictLink);
            String path = Utils.joinPath(conflictLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Conflict, path, requestHeaders);
            Observable<RxDocumentServiceRequest> reqObs = addPartitionKeyInformation(request, null, options).toObservable();
            return reqObs.flatMap(req -> this.delete(request).map(response -> toResourceResponse(response, Conflict.class)));

        } catch (Exception e) {
            logger.debug("Failure in deleting a Conflict due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<User>> createUser(String databaseLink, User user, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> createUserInternal(databaseLink, user, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<User>> createUserInternal(String databaseLink, User user, RequestOptions options) {
        try {
            logger.debug("Creating a User. databaseLink [{}], user id [{}]", databaseLink, user.getId());
            RxDocumentServiceRequest request = getUserRequest(databaseLink, user, options, OperationType.Create);
            return this.create(request).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a User due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<User>> upsertUser(String databaseLink, User user, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertUserInternal(databaseLink, user, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<User>> upsertUserInternal(String databaseLink, User user, RequestOptions options) {
        try {
            logger.debug("Upserting a User. databaseLink [{}], user id [{}]", databaseLink, user.getId());
            RxDocumentServiceRequest request = getUserRequest(databaseLink, user, options, OperationType.Upsert);
            return this.upsert(request).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in upserting a User due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    private RxDocumentServiceRequest getUserRequest(String databaseLink, User user, RequestOptions options,
            OperationType operationType) {
        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }
        if (user == null) {
            throw new IllegalArgumentException("user");
        }

        RxDocumentClientImpl.validateResource(user);

        String path = Utils.joinPath(databaseLink, Paths.USERS_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType, ResourceType.User, path, user,
                requestHeaders);
        return request;
    }

    @Override
    public Observable<ResourceResponse<User>> replaceUser(User user, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceUserInternal(user, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<User>> replaceUserInternal(User user, RequestOptions options) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("user");
            }
            logger.debug("Replacing a User. user id [{}]", user.getId());
            RxDocumentClientImpl.validateResource(user);

            String path = Utils.joinPath(user.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.User, path, user, requestHeaders);
            return this.replace(request).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a User due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }


    public Observable<ResourceResponse<User>> deleteUser(String userLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deleteUserInternal(userLink, options), retryPolicy.getRequestPolicy());
    }

    private Observable<ResourceResponse<User>> deleteUserInternal(String userLink, RequestOptions options) {

        try {
            if (StringUtils.isEmpty(userLink)) {
                throw new IllegalArgumentException("userLink");
            }
            logger.debug("Deleting a User. userLink [{}]", userLink);
            String path = Utils.joinPath(userLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.User, path, requestHeaders);
            return this.delete(request).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a User due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }
    @Override
    public Observable<ResourceResponse<User>> readUser(String userLink, RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readUserInternal(userLink, options, retryPolicyInstance), retryPolicyInstance);        
    }

    private Observable<ResourceResponse<User>> readUserInternal(String userLink, RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(userLink)) {
                throw new IllegalArgumentException("userLink");
            }
            logger.debug("Reading a User. userLink [{}]", userLink);
            String path = Utils.joinPath(userLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.User, path, requestHeaders);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request).map(response -> toResourceResponse(response, User.class));

        } catch (Exception e) {
            logger.debug("Failure in reading a User due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<User>> readUsers(String databaseLink, FeedOptions options) {

        if (StringUtils.isEmpty(databaseLink)) {
            throw new IllegalArgumentException("databaseLink");
        }

        return readFeed(options, ResourceType.User, User.class,
                Utils.joinPath(databaseLink, Paths.USERS_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<User>> queryUsers(String databaseLink, String query, FeedOptions options) {
        return queryUsers(databaseLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<User>> queryUsers(String databaseLink, SqlQuerySpec querySpec,
            FeedOptions options) {
        return createQuery(databaseLink, querySpec, options, User.class, ResourceType.User);
    }

    @Override
    public Observable<ResourceResponse<Permission>> createPermission(String userLink, Permission permission,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> createPermissionInternal(userLink, permission, options), retryPolicy.getRequestPolicy());        
    }

    private Observable<ResourceResponse<Permission>> createPermissionInternal(String userLink, Permission permission,
            RequestOptions options) {

        try {
            logger.debug("Creating a Permission. userLink [{}], permission id [{}]", userLink, permission.getId());
            RxDocumentServiceRequest request = getPermissionRequest(userLink, permission, options,
                    OperationType.Create);
            return this.create(request).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in creating a Permission due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Permission>> upsertPermission(String userLink, Permission permission,
            RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> upsertPermissionInternal(userLink, permission, options), retryPolicy.getRequestPolicy());        
    }

    private Observable<ResourceResponse<Permission>> upsertPermissionInternal(String userLink, Permission permission,
            RequestOptions options) {

        try {
            logger.debug("Upserting a Permission. userLink [{}], permission id [{}]", userLink, permission.getId());
            RxDocumentServiceRequest request = getPermissionRequest(userLink, permission, options,
                    OperationType.Upsert);
            return this.upsert(request).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in upserting a Permission due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    private RxDocumentServiceRequest getPermissionRequest(String userLink, Permission permission,
            RequestOptions options, OperationType operationType) {
        if (StringUtils.isEmpty(userLink)) {
            throw new IllegalArgumentException("userLink");
        }
        if (permission == null) {
            throw new IllegalArgumentException("permission");
        }

        RxDocumentClientImpl.validateResource(permission);

        String path = Utils.joinPath(userLink, Paths.PERMISSIONS_PATH_SEGMENT);
        Map<String, String> requestHeaders = getRequestHeaders(options);
        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(operationType, ResourceType.Permission, path,
                permission, requestHeaders);
        return request;
    }

    @Override
    public Observable<ResourceResponse<Permission>> replacePermission(Permission permission, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replacePermissionInternal(permission, options), retryPolicy.getRequestPolicy());        
    }

    private Observable<ResourceResponse<Permission>> replacePermissionInternal(Permission permission, RequestOptions options) {
        try {
            if (permission == null) {
                throw new IllegalArgumentException("permission");
            }
            logger.debug("Replacing a Permission. permission id [{}]", permission.getId());
            RxDocumentClientImpl.validateResource(permission);

            String path = Utils.joinPath(permission.getSelfLink(), null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.Permission, path, permission, requestHeaders);
            return this.replace(request).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing a Permission due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Permission>> deletePermission(String permissionLink, RequestOptions options) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> deletePermissionInternal(permissionLink, options), retryPolicy.getRequestPolicy());        
    }

    private Observable<ResourceResponse<Permission>> deletePermissionInternal(String permissionLink, RequestOptions options) {

        try {
            if (StringUtils.isEmpty(permissionLink)) {
                throw new IllegalArgumentException("permissionLink");
            }
            logger.debug("Deleting a Permission. permissionLink [{}]", permissionLink);
            String path = Utils.joinPath(permissionLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Delete,
                    ResourceType.Permission, path, requestHeaders);
            return this.delete(request).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in deleting a Permission due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Permission>> readPermission(String permissionLink, RequestOptions options) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readPermissionInternal(permissionLink, options, retryPolicyInstance), retryPolicyInstance);        
    }

    private Observable<ResourceResponse<Permission>> readPermissionInternal(String permissionLink, RequestOptions options, IDocumentClientRetryPolicy retryPolicyInstance ) {
        try {
            if (StringUtils.isEmpty(permissionLink)) {
                throw new IllegalArgumentException("permissionLink");
            }
            logger.debug("Reading a Permission. permissionLink [{}]", permissionLink);
            String path = Utils.joinPath(permissionLink, null);
            Map<String, String> requestHeaders = getRequestHeaders(options);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Permission, path, requestHeaders);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }
            return this.read(request).map(response -> toResourceResponse(response, Permission.class));

        } catch (Exception e) {
            logger.debug("Failure in reading a Permission due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<Permission>> readPermissions(String userLink, FeedOptions options) {

        if (StringUtils.isEmpty(userLink)) {
            throw new IllegalArgumentException("userLink");
        }

        return readFeed(options, ResourceType.Permission, Permission.class,
                Utils.joinPath(userLink, Paths.PERMISSIONS_PATH_SEGMENT));
    }

    @Override
    public Observable<FeedResponse<Permission>> queryPermissions(String userLink, String query,
            FeedOptions options) {
        return queryPermissions(userLink, new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<Permission>> queryPermissions(String userLink, SqlQuerySpec querySpec,
            FeedOptions options) {
        return createQuery(userLink, querySpec, options, Permission.class, ResourceType.Permission);
    }

    @Override
    public Observable<ResourceResponse<Offer>> replaceOffer(Offer offer) {
        return ObservableHelper.inlineIfPossibleAsObs(() -> replaceOfferInternal(offer), retryPolicy.getRequestPolicy());        
    }

    private Observable<ResourceResponse<Offer>> replaceOfferInternal(Offer offer) {
        try {
            if (offer == null) {
                throw new IllegalArgumentException("offer");
            }
            logger.debug("Replacing an Offer. offer id [{}]", offer.getId());
            RxDocumentClientImpl.validateResource(offer);

            String path = Utils.joinPath(offer.getSelfLink(), null);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Replace,
                    ResourceType.Offer, path, offer, null);
            return this.replace(request).map(response -> toResourceResponse(response, Offer.class));

        } catch (Exception e) {
            logger.debug("Failure in replacing an Offer due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<ResourceResponse<Offer>> readOffer(String offerLink) {
        IDocumentClientRetryPolicy retryPolicyInstance = retryPolicy.getRequestPolicy();
        return ObservableHelper.inlineIfPossibleAsObs(() -> readOfferInternal(offerLink, retryPolicyInstance), retryPolicyInstance);
    }

    private Observable<ResourceResponse<Offer>> readOfferInternal(String offerLink, IDocumentClientRetryPolicy retryPolicyInstance) {
        try {
            if (StringUtils.isEmpty(offerLink)) {
                throw new IllegalArgumentException("offerLink");
            }
            logger.debug("Reading an Offer. offerLink [{}]", offerLink);
            String path = Utils.joinPath(offerLink, null);
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.Offer, path, null);

            if (retryPolicyInstance != null) {
                retryPolicyInstance.onBeforeSendRequest(request);
            }

            return this.read(request).map(response -> toResourceResponse(response, Offer.class));

        } catch (Exception e) {
            logger.debug("Failure in reading an Offer due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    @Override
    public Observable<FeedResponse<Offer>> readOffers(FeedOptions options) {
        return readFeed(options, ResourceType.Offer, Offer.class,
                Utils.joinPath(Paths.OFFERS_PATH_SEGMENT, null));
    }
    
    private <T extends Resource> Observable<FeedResponse<T>> readFeedCollectionChild(FeedOptions options, ResourceType resourceType,
            Class<T> klass, String resourceLink) {
        if (options == null) {
            options = new FeedOptions();
        }

        int maxPageSize = options.getMaxItemCount() != null ? options.getMaxItemCount() : -1;

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setPartitionKey(options.getPartitionKey());

        Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> {
            Map<String, String> requestHeaders = new HashMap<>();
            if (continuationToken != null) {
                requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
            }
            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Integer.toString(pageSize));
            return RxDocumentServiceRequest.create(OperationType.ReadFeed,
                    resourceType, resourceLink, requestHeaders);
        };

        Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = request -> {
            return ObservableHelper.inlineIfPossibleAsObs(() -> {
                Single<DocumentCollection> collectionObs = this.collectionCache.resolveCollectionAsync(request);
                Single<RxDocumentServiceRequest> requestObs = this.addPartitionKeyInformation(request, null, requestOptions, collectionObs);

                return requestObs.toObservable().flatMap(req -> this.readFeed(req)
                        .map(response -> toFeedResponsePage(response, klass)));
            }, retryPolicy.getRequestPolicy());
        };

        return Paginator.getPaginatedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, maxPageSize);
    }

    private <T extends Resource> Observable<FeedResponse<T>> readFeed(FeedOptions options, ResourceType resourceType, Class<T> klass, String resourceLink) {
        if (options == null) {
            options = new FeedOptions();
        }
        
        int maxPageSize = options.getMaxItemCount() != null ? options.getMaxItemCount() : -1;
        
        Func2<String, Integer, RxDocumentServiceRequest> createRequestFunc = (continuationToken, pageSize) -> {
            Map<String, String> requestHeaders = new HashMap<>(10);
            if (continuationToken != null) {
                requestHeaders.put(HttpConstants.HttpHeaders.CONTINUATION, continuationToken);
            }
            requestHeaders.put(HttpConstants.HttpHeaders.PAGE_SIZE, Integer.toString(pageSize));
            return RxDocumentServiceRequest.create(OperationType.ReadFeed,
                    resourceType, resourceLink, requestHeaders);
        };

        Func1<RxDocumentServiceRequest, Observable<FeedResponse<T>>> executeFunc = request -> {
            return ObservableHelper.inlineIfPossibleAsObs(() -> readFeed(request).map(response -> toFeedResponsePage(response, klass)), 
                    retryPolicy.getRequestPolicy());
        };

        return Paginator.getPaginatedQueryResultAsObservable(options, createRequestFunc, executeFunc, klass, maxPageSize);
    }
    
    @Override
    public Observable<FeedResponse<Offer>> queryOffers(String query, FeedOptions options) {
        return queryOffers(new SqlQuerySpec(query), options);
    }

    @Override
    public Observable<FeedResponse<Offer>> queryOffers(SqlQuerySpec querySpec, FeedOptions options) {
        return createQuery(null, querySpec, options, Offer.class, ResourceType.Offer);
    }

    @Override
    public Observable<DatabaseAccount> getDatabaseAccount() {
        return ObservableHelper.inlineIfPossibleAsObs(() -> getDatabaseAccountInternal(), retryPolicy.getRequestPolicy());
    }

    private Observable<DatabaseAccount> getDatabaseAccountInternal() {
        try {
            logger.debug("Getting Database Account");
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.DatabaseAccount, "", // path
                    null);
            return this.read(request).map(response -> toDatabaseAccount(response));

        } catch (Exception e) {
            logger.debug("Failure in getting Database Account due to [{}]", e.getMessage(), e);
            return Observable.error(e);
        }
    }

    public Observable<DatabaseAccount> getDatabaseAccountFromEndpoint(URI endpoint) {
        return Observable.defer(() -> {
            RxDocumentServiceRequest request = RxDocumentServiceRequest.create(OperationType.Read,
                    ResourceType.DatabaseAccount, "", null);
            this.populateHeaders(request, HttpConstants.HttpMethods.GET);

            request.setEndpointOverride(endpoint);
            return this.gatewayProxy.read(request).doOnError(e -> {
                String message = String.format("Failed to retrieve database account information. %s",
                            e.getCause() != null
                                    ? e.getCause().toString()
                                    : e.toString());
                logger.warn(message);
            }).map(rsp -> rsp.getResource(DatabaseAccount.class)).onErrorReturn(error -> null);
        });
    }

    @Override
    public void close() {
        this.globalEndpointManager.close();
        try {
            this.rxClient.shutdown();
        } catch (Exception e) {
            logger.warn("Failure in shutting down rxClient", e);
        }
    }
}
