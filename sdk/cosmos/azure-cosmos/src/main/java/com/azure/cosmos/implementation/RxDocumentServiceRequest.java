// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is core Transport/Connection agnostic request to the Azure Cosmos DB database service.
 */
public class RxDocumentServiceRequest implements Cloneable {

    private final DiagnosticsClientContext clientContext;
    public volatile boolean forcePartitionKeyRangeRefresh;
    public volatile boolean forceCollectionRoutingMapRefresh;
    private String resourceId;
    private final ResourceType resourceType;
    private final Map<String, String> headers;
    private volatile String continuation;
    private boolean isMedia = false;
    private final boolean isNameBased;
    private final OperationType operationType;
    private final String resourceAddress;
    public volatile boolean forceNameCacheRefresh;
    private volatile URI endpointOverride = null;
    private final UUID activityId;

    private volatile String originalSessionToken;
    private volatile PartitionKeyRangeIdentity partitionKeyRangeIdentity;
    private volatile Integer defaultReplicaIndex;

    private boolean isAddressRefresh;

    public DocumentServiceRequestContext requestContext;

    // has the non serialized value of the partition-key
    private PartitionKeyInternal partitionKeyInternal;

    private FeedRangeInternal feedRange;
    private Range<String> effectiveRange;
    private int numberOfItemsInBatchRequest;

    private byte[] contentAsByteArray;

    // NOTE: TODO: these fields are copied from .Net SDK
    // some of these fields are missing from the main java sdk service request
    // so it means most likely the corresponding features are also missing from the main sdk
    // we need to wire this up.
    public boolean UseGatewayMode;

    private volatile boolean isDisposed = false;
    public volatile String entityId;
    public volatile boolean isFeed;
    public volatile AuthorizationTokenType authorizationTokenType;
    public volatile Map<String, Object> properties;
    public String throughputControlGroupName;

    public boolean isReadOnlyRequest() {
        return this.operationType == OperationType.Read
                || this.operationType == OperationType.ReadFeed
                || this.operationType == OperationType.Head
                || this.operationType == OperationType.HeadFeed
                || this.operationType == OperationType.Query
                || this.operationType == OperationType.SqlQuery
                || this.operationType == OperationType.QueryPlan;
    }

    public boolean isReadOnlyScript() {
        String isReadOnlyScript = this.headers.get(HttpConstants.HttpHeaders.IS_READ_ONLY_SCRIPT);
        if(StringUtils.isEmpty(isReadOnlyScript)) {
            return false;
        } else {
            return this.operationType.equals(OperationType.ExecuteJavaScript) && isReadOnlyScript.equalsIgnoreCase(Boolean.TRUE.toString());
        }
    }

    public boolean isReadOnly() {
        return this.isReadOnlyRequest() || this.isReadOnlyScript();
    }

    /**
     * @param operationType          the operation type.
     * @param resourceIdOrFullName   the request id or full name.
     * @param resourceType           the resource type.
     * @param byteContent            the byte content.
     * @param headers                the headers.
     * @param isNameBased            whether request is name based.
     * @param authorizationTokenType the request authorizationTokenType.
     */
    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     String resourceIdOrFullName,
                                     ResourceType resourceType,
                                     byte[] byteContent,
                                     Map<String, String> headers,
                                     boolean isNameBased,
                                     AuthorizationTokenType authorizationTokenType) {
        this(clientContext, operationType, resourceIdOrFullName, resourceType, wrapByteBuffer(byteContent), headers, isNameBased, authorizationTokenType);
    }

    /**
     * @param operationType          the operation type.
     * @param resourceIdOrFullName   the request id or full name.
     * @param resourceType           the resource type.
     * @param byteBuffer             the byte content.
     * @param headers                the headers.
     * @param isNameBased            whether request is name based.
     * @param authorizationTokenType the request authorizationTokenType.
     */
    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     String resourceIdOrFullName,
                                     ResourceType resourceType,
                                     ByteBuffer byteBuffer,
                                     Map<String, String> headers,
                                     boolean isNameBased,
                                     AuthorizationTokenType authorizationTokenType) {
        this.clientContext = clientContext;
        this.operationType = operationType;
        this.forceNameCacheRefresh = false;
        this.resourceType = resourceType;
        this.contentAsByteArray = toByteArray(byteBuffer);
        this.headers = headers != null ? headers : new HashMap<>();
        this.activityId = Utils.randomUUID();
        this.isFeed = false;
        this.isNameBased = isNameBased;
        if (!isNameBased) {
            this.resourceId = resourceIdOrFullName;
        }
        this.resourceAddress = resourceIdOrFullName;
        this.authorizationTokenType = authorizationTokenType;
        this.requestContext = new DocumentServiceRequestContext();
        if (StringUtils.isNotEmpty(this.headers.get(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID)))
            this.partitionKeyRangeIdentity = PartitionKeyRangeIdentity.fromHeader(this.headers.get(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID));
    }

    /**
     * Creates a AbstractDocumentServiceRequest
     *
     * @param operationType     the operation type.
     * @param resourceType      the resource type.
     * @param path              the path.
     * @param headers           the headers
     */
    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     ResourceType resourceType,
                                     String path,
                                     Map<String, String> headers) {
        this.clientContext = clientContext;
        this.requestContext = new DocumentServiceRequestContext();
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.requestContext.sessionToken = null;
        this.headers = headers != null ? headers : new HashMap<>();
        this.activityId = Utils.randomUUID();
        this.isFeed = false;

        if (StringUtils.isNotEmpty(path)) {
            PathInfo pathInfo = new PathInfo(false, null, null, false);
            if (PathsHelper.tryParsePathSegments(path, pathInfo, null)) {
                this.isNameBased = pathInfo.isNameBased;
                this.isFeed = pathInfo.isFeed;
                String resourceIdOrFullName = pathInfo.resourceIdOrFullName;
                if (!this.isNameBased) {
                if (resourceType == ResourceType.Media) {
                    this.resourceId = getAttachmentIdFromMediaId(resourceIdOrFullName);
                } else {
                    this.resourceId = resourceIdOrFullName;
                }

                this.resourceAddress = resourceIdOrFullName;

                    // throw exception when the address parsing fail
                    // do not parse address for offer resource
                    if (StringUtils.isNotEmpty(this.resourceId) && !ResourceId.tryParse(this.resourceId).getLeft()
                            && !resourceType.equals(ResourceType.Offer) && !resourceType.equals(ResourceType.Media)
                            && !resourceType.equals(ResourceType.MasterPartition)
                            && !resourceType.equals(ResourceType.ServerPartition)
                            && !resourceType.equals(ResourceType.DatabaseAccount)
                            && !resourceType.equals(ResourceType.RidRange)) {
                        throw new IllegalArgumentException(
                                String.format(RMResources.InvalidResourceUrlQuery, path, HttpConstants.QueryStrings.URL));
                    }
                } else {
                    this.resourceAddress = resourceIdOrFullName;
                    this.resourceId = null;
                }
            } else {
                throw new IllegalArgumentException(RMResources.NotFound);
            }
        } else {
            this.isNameBased = false;
            this.resourceAddress = path;
        }

        if (StringUtils.isNotEmpty(this.headers.get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID))) {
            this.partitionKeyRangeIdentity = PartitionKeyRangeIdentity
                    .fromHeader(this.headers.get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID));
        }
    }

    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     ResourceType resourceType,
                                     ByteBuffer byteBuffer,
                                     String path,
                                     Map<String, String> headers,
                                     AuthorizationTokenType authorizationTokenType) {
        this(clientContext,
            operationType,
            resourceType,
            path,
            headers);
        this.authorizationTokenType = authorizationTokenType;
        this.contentAsByteArray = toByteArray(byteBuffer);
    }

    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     ResourceType resourceType,
                                     byte[] content,
                                     String path,
                                     Map<String, String> headers,
                                     AuthorizationTokenType authorizationTokenType) {
        this(clientContext, operationType, resourceType, wrapByteBuffer(content), path, headers, authorizationTokenType);
    }

    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     ResourceType resourceType,
                                     String path,
                                     ByteBuffer byteBuffer,
                                     Map<String, String> headers,
                                     AuthorizationTokenType authorizationTokenType) {
        this(clientContext, operationType, resourceType, byteBuffer, path, headers, authorizationTokenType);
    }

    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     ResourceType resourceType,
                                     String path,
                                     byte[] byteContent,
                                     Map<String, String> headers,
                                     AuthorizationTokenType authorizationTokenType) {
        this(clientContext, operationType, resourceType, byteContent, path, headers, authorizationTokenType);
    }

    private RxDocumentServiceRequest(DiagnosticsClientContext clientContext,
                                     OperationType operationType,
                                     ResourceType resourceType,
                                     String relativeUriPath,
                                     Map<String, String> headers,
                                     AuthorizationTokenType authorizationTokenType) {
        this(clientContext, operationType, resourceType, (byte[]) null, relativeUriPath, headers, authorizationTokenType);
    }

    public void setContentBytes(byte[] contentBytes) {
        this.contentAsByteArray = contentBytes;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.contentAsByteArray = toByteArray(byteBuffer);
    }

    /** Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param bytes        the body content.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(
            DiagnosticsClientContext clientContext,
            OperationType operation,
            ResourceType resourceType,
            String relativePath,
            byte[] bytes,
            Map<String, String> headers) {
        return new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath, bytes, headers, AuthorizationTokenType.PrimaryMasterKey);
    }

    /** Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param bytes        the byte array content.
     * @param headers      the request headers.
     * @param authorizationTokenType      the request authorizationTokenType.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  byte[] bytes,
                                                  Map<String, String> headers,
                                                  AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath, bytes, headers, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest with a resource.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param resource     the resource of the request.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Resource resource,
                                                  Map<String, String> headers) {
        return create(clientContext, operation, resourceType, relativePath, resource, headers, (RequestOptions) null);
    }

    /**
     * Creates a DocumentServiceRequest with a resource.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param resource     the resource of the request.
     * @param headers      the request headers.
     * @param options      the request/feed/changeFeed options.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Resource resource,
                                                  Map<String, String> headers,
                                                  Object options) {

        RxDocumentServiceRequest request = new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath,
            ModelBridgeInternal.serializeJsonToByteBuffer(resource), headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
        request.throughputControlGroupName = getThroughputControlGroupName(options);
        return request;
    }

    /**
     * Creates a DocumentServiceRequest with a resource.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param byteBuffer   the resource byteBuffer.
     * @param headers      the request headers.
     * @param options      the request/feed/changeFeed options.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  ByteBuffer byteBuffer,
                                                  Map<String, String> headers,
                                                  Object options) {

        RxDocumentServiceRequest request = new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath,
            byteBuffer, headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
        request.throughputControlGroupName = getThroughputControlGroupName(options);
        return request;
    }

    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Map<String, String> headers,
                                                  Object options,
                                                  ByteBuffer byteBuffer) {

        RxDocumentServiceRequest request = new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath,
            byteBuffer, headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
        request.throughputControlGroupName = getThroughputControlGroupName(options);
        return request;
    }

    /**
     * Creates a DocumentServiceRequest with a body.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param body         the body.
     * @param headers      the request headers.
     * @param options      the request/feed/changeFeed options.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  String body,
                                                  Map<String, String> headers,
                                                  Object options) {
        RxDocumentServiceRequest request = new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath,
            body.getBytes(StandardCharsets.UTF_8), headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
        request.throughputControlGroupName = getThroughputControlGroupName(options);
        return request;
    }

    /**
     * Creates a DocumentServiceRequest with a query.
     *
     * @param resourceType           the resource type.
     * @param relativePath           the relative URI path.
     * @param querySpec              the query.
     * @param queryCompatibilityMode the QueryCompatibilityMode mode.
     * @param headers                the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  SqlQuerySpec querySpec,
                                                  QueryCompatibilityMode queryCompatibilityMode,
                                                  Map<String, String> headers) {
        OperationType operation;
        switch (queryCompatibilityMode) {
        case SqlQuery:
            // The querySpec.getParameters() method always ensure the returned value is non-null
            // hence null check is not required here.
            if (querySpec.getParameters().size() > 0) {
                throw new IllegalArgumentException(
                        String.format("Unsupported argument in query compatibility mode '{%s}'",
                                queryCompatibilityMode.toString()));
            }

            operation = OperationType.SqlQuery;
            return new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath, Utils.getUTF8Bytes(querySpec.getQueryText()), headers, AuthorizationTokenType.PrimaryMasterKey);

        case Default:
        case Query:
        default:
            operation = OperationType.Query;
            return new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath,
                ModelBridgeInternal.serializeJsonToByteBuffer(querySpec), headers, AuthorizationTokenType.PrimaryMasterKey);
        }
    }

    /**
     * Creates a DocumentServiceRequest without body.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Map<String, String> headers) {
        return create(clientContext, operation, resourceType, relativePath, headers, (RequestOptions)null);
    }

    /**
     * Creates a DocumentServiceRequest without body.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param headers      the request headers.
     * @param options      the request/feed/changeFeed options.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Map<String, String> headers,
                                                  Object options) {
        RxDocumentServiceRequest request = new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath, headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
        request.throughputControlGroupName = getThroughputControlGroupName(options);
        return request;
    }

    /**
     * Creates a DocumentServiceRequest without body.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param headers      the request headers.
     * @param authorizationTokenType      the request authorizationTokenType.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Map<String, String> headers,
                                                  AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath, headers, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest without body.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  Resource resource,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Map<String, String> headers) {
        ByteBuffer resourceContent = ModelBridgeInternal.serializeJsonToByteBuffer(resource);
        return new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath, resourceContent, headers, AuthorizationTokenType.PrimaryMasterKey);
    }

    /**
     * Creates a DocumentServiceRequest without body.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param headers      the request headers.
     * @param authorizationTokenType      the request authorizationTokenType.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  Resource resource,
                                                  ResourceType resourceType,
                                                  String relativePath,
                                                  Map<String, String> headers,
                                                  AuthorizationTokenType authorizationTokenType) {
        ByteBuffer resourceContent = ModelBridgeInternal.serializeJsonToByteBuffer(resource);
        return new RxDocumentServiceRequest(clientContext, operation, resourceType, relativePath, resourceContent, headers, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest with a resourceId.
     *
     * @param operation    the operation type.
     * @param resourceId   the resource id.
     * @param resourceType the resource type.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  String resourceId,
                                                  ResourceType resourceType,
                                                  Map<String, String> headers) {
        return new RxDocumentServiceRequest(clientContext, operation, resourceId,resourceType, (ByteBuffer) null, headers, false, AuthorizationTokenType.PrimaryMasterKey) ;
    }

    /**
     * Creates a DocumentServiceRequest with a resourceId.
     *
     * @param operation    the operation type.
     * @param resourceId   the resource id.
     * @param resourceType the resource type.
     * @param headers      the request headers.
     * @param authorizationTokenType      the request authorizationTokenType.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  String resourceId,
                                                  ResourceType resourceType,
                                                  Map<String, String> headers,
                                                  AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(clientContext, operation, resourceId, resourceType, (ByteBuffer) null, headers, false, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest with a resourceId.
     *
     * @param operation    the operation type.
     * @param resourceId   the resource id.
     * @param resourceType the resource type.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  String resourceId,
                                                  ResourceType resourceType,
                                                  Resource resource,
                                                  Map<String, String> headers) {
        ByteBuffer resourceContent = ModelBridgeInternal.serializeJsonToByteBuffer(resource);
        return new RxDocumentServiceRequest(clientContext, operation, resourceId, resourceType, resourceContent, headers, false, AuthorizationTokenType.PrimaryMasterKey);
    }

    /**
     * Creates a DocumentServiceRequest with a resourceId.
     *
     * @param operation    the operation type.
     * @param resourceId   the resource id.
     * @param resourceType the resource type.
     * @param headers      the request headers.
     * @param authorizationTokenType      the request authorizationTokenType.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  String resourceId,
                                                  ResourceType resourceType,
                                                  Resource resource,
                                                  Map<String, String> headers,
                                                  AuthorizationTokenType authorizationTokenType) {
        ByteBuffer resourceContent = ModelBridgeInternal.serializeJsonToByteBuffer(resource);
        return new RxDocumentServiceRequest(clientContext, operation, resourceId, resourceType, resourceContent, headers, false, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest with operationType and resourceType
     * @param operation     the operation type
     * @param resourceType  the resource type
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(DiagnosticsClientContext clientContext,
                                                  OperationType operation,
                                                  ResourceType resourceType) {
        return new RxDocumentServiceRequest(clientContext, operation, resourceType, null, null);
    }

    public static RxDocumentServiceRequest createFromName(DiagnosticsClientContext clientContext,
                                                          OperationType operationType,
                                                          String resourceFullName,
                                                          ResourceType resourceType) {
        return new RxDocumentServiceRequest(clientContext,
                operationType,
                resourceFullName,
                resourceType,
                (ByteBuffer) null,
                new HashMap<>(),
                true,
                AuthorizationTokenType.PrimaryMasterKey
        );
    }

    public static RxDocumentServiceRequest createFromName(
            DiagnosticsClientContext clientContext,
            OperationType operationType,
            String resourceFullName,
            ResourceType resourceType,
            AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(clientContext,
                operationType,
                resourceFullName,
                resourceType,
                (ByteBuffer) null,
                new HashMap<>(),
                true,
                authorizationTokenType
        );
    }

    public static RxDocumentServiceRequest createFromName(
            DiagnosticsClientContext clientContext,
            OperationType operationType,
            Resource resource,
            String resourceFullName,
            ResourceType resourceType) {
        ByteBuffer resourceContent = ModelBridgeInternal.serializeJsonToByteBuffer(resource);
        return new RxDocumentServiceRequest(clientContext,
                operationType,
                resourceFullName,
                resourceType,
                resourceContent,
                new HashMap<>(),
                true,
                AuthorizationTokenType.PrimaryMasterKey
        );
    }

    public static RxDocumentServiceRequest createFromName(
            DiagnosticsClientContext clientContext,
            OperationType operationType,
            Resource resource,
            String resourceFullName,
            ResourceType resourceType,
            AuthorizationTokenType authorizationTokenType) {
        ByteBuffer resourceContent = ModelBridgeInternal.serializeJsonToByteBuffer(resource);
        return new RxDocumentServiceRequest(clientContext,
                operationType,
                resourceFullName,
                resourceType,
                resourceContent,
                new HashMap<>(),
                true,
                authorizationTokenType
        );
    }

    static String getAttachmentIdFromMediaId(String mediaId) {
        // '/' was replaced with '-'.
        byte[] buffer = Utils.Base64Decoder.decode(mediaId.replace('-', '/').getBytes(StandardCharsets.UTF_8));

        final int resoureIdLength = 20;
        String attachmentId;

        if (buffer.length > resoureIdLength) {
            // We are cuting off the storage index.
            byte[] newBuffer = new byte[resoureIdLength];
            System.arraycopy(buffer, 0, newBuffer, 0, resoureIdLength);
            attachmentId = Utils.encodeBase64String(newBuffer).replace('/', '-');
        } else {
            attachmentId = mediaId;
        }

        return attachmentId;
    }

    /**
     * Gets the resource id.
     *
     * @return the resource id.
     */
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * Sets the resource id.
     *
     */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type.
     */
    public ResourceType getResourceType() {
        return this.resourceType;
    }

    /**
     * Gets the request headers.
     *
     * @return the request headers.
     */
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Gets the continuation.
     *
     * @return the continuation.
     */
    public String getContinuation() {
        return this.continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }

    public boolean getIsMedia() {
        return this.isMedia;
    }

    public void setIsMedia(boolean isMedia) {
        this.isMedia = isMedia;
    }

    public boolean getIsNameBased() {
        return this.isNameBased;
    }

    public OperationType getOperationType() {
        return this.operationType;
    }

    public String getResourceAddress() {
        return resourceAddress;
    }

    public boolean isForceNameCacheRefresh() {
        return forceNameCacheRefresh;
    }

    public void setForceNameCacheRefresh(boolean forceNameCacheRefresh) {
        this.forceNameCacheRefresh = forceNameCacheRefresh;
    }

    public URI getEndpointOverride() {
        return this.endpointOverride;
    }

    public void setEndpointOverride(URI endpointOverride) {
        this.endpointOverride = endpointOverride;
    }

    public UUID getActivityId() {
        return this.activityId;
    }

    public PartitionKeyRangeIdentity getPartitionKeyRangeIdentity() {
        return partitionKeyRangeIdentity;
    }

    public void routeTo(PartitionKeyRangeIdentity partitionKeyRangeIdentity) {
        this.setPartitionKeyRangeIdentity(partitionKeyRangeIdentity);
    }

    public FeedRangeInternal getFeedRange() {
        return this.feedRange;
    }

    public void applyFeedRangeFilter(FeedRangeInternal feedRange) {
        this.feedRange = feedRange;
    }

    public Range<String> getEffectiveRange() {
        return this.effectiveRange;
    }

    public void setEffectiveRange(Range<String> range) {
        this.effectiveRange = range;
    }

    public void setPartitionKeyRangeIdentity(PartitionKeyRangeIdentity partitionKeyRangeIdentity) {
        this.partitionKeyRangeIdentity = partitionKeyRangeIdentity;
        if (partitionKeyRangeIdentity != null) {
            this.headers.put(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeIdentity.toHeader());
        } else {
            this.headers.remove(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
        }
    }

    public String getOriginalSessionToken() {
        return originalSessionToken;
    }

    public void setOriginalSessionToken(String originalSessionToken) {
        this.originalSessionToken = originalSessionToken;
    }

    public void setDefaultReplicaIndex(Integer defaultReplicaIndex) {
        this.defaultReplicaIndex = defaultReplicaIndex;
    }

    public Integer getDefaultReplicaIndex() {
        return defaultReplicaIndex;
    }

    /**
     * To avoid deserialization of PartitionKey in Address Resolver, when you set PartitionKey header value,
     * you should also set PartitionKeyInternal.
     * @param partitionKeyInternal
     */
    public void setPartitionKeyInternal(PartitionKeyInternal partitionKeyInternal) {
        this.partitionKeyInternal = partitionKeyInternal;
    }

    public PartitionKeyInternal getPartitionKeyInternal() {
        return this.partitionKeyInternal;
    }

    public boolean isChangeFeedRequest() {
        return this.headers.containsKey(HttpConstants.HttpHeaders.A_IM);
    }

    public boolean isWritingToMaster() {
        return operationType.isWriteOperation() && resourceType.isMasterResource();
    }

    public boolean isReadingFromMaster() {
        if (resourceType == ResourceType.Offer ||
                resourceType == ResourceType.Database ||
                resourceType == ResourceType.User ||
                resourceType == ResourceType.Permission ||
                resourceType == ResourceType.Topology ||
                resourceType == ResourceType.DatabaseAccount ||
                resourceType == ResourceType.PartitionKeyRange ||
                (resourceType == ResourceType.DocumentCollection
                        && (operationType == OperationType.ReadFeed
                        || operationType == OperationType.Query
                        || operationType == OperationType.SqlQuery))) {
            return true;
        }
        return false;
    }

    public boolean isValidAddress(ResourceType resourceType) {
        ResourceType resourceTypeToValidate = ResourceType.Unknown;

        if(resourceType != ResourceType.Unknown) {
            resourceTypeToValidate = resourceType;
        } else {
            if(!this.isFeed) {
                resourceTypeToValidate =this.resourceType;
            } else {
                if(this.resourceType == ResourceType.Database) {
                    return true;
                } else if(this.resourceType == ResourceType.DocumentCollection ||
                          this.resourceType == ResourceType.User) {
                    resourceTypeToValidate = ResourceType.Database;
                } else if(this.resourceType == ResourceType.Permission) {
                    resourceTypeToValidate = ResourceType.User;
                } else if(this.resourceType == ResourceType.Document ||
                        this.resourceType == ResourceType.StoredProcedure ||
                        this.resourceType == ResourceType.UserDefinedFunction ||
                        this.resourceType == ResourceType.Trigger ||
                        this.resourceType == ResourceType.Conflict ||
                        this.resourceType == ResourceType.PartitionKeyRange) {
                  resourceTypeToValidate = ResourceType.DocumentCollection;
              } else if(this.resourceType == ResourceType.Attachment) {
                  resourceTypeToValidate = ResourceType.Document;
              }  else {
                  return false;
              }
            }
        }

        if (this.isNameBased) {
            return PathsHelper.validateResourceFullName(resourceType != ResourceType.Unknown ? resourceType : resourceTypeToValidate, this.resourceAddress);
        } else {
            return PathsHelper.validateResourceId(resourceTypeToValidate, this.resourceId);
        }
    }

    public static RxDocumentServiceRequest createFromResource(RxDocumentServiceRequest request, Resource modifiedResource) {
        RxDocumentServiceRequest modifiedRequest;
        if (!request.getIsNameBased()) {
            modifiedRequest = RxDocumentServiceRequest.create(request.clientContext,
                                                              request.getOperationType(),
                                                              request.getResourceId(),
                                                              request.getResourceType(),
                                                              modifiedResource,
                                                              request.headers);
        } else {
            modifiedRequest = RxDocumentServiceRequest.createFromName(request.clientContext,
                                                                      request.getOperationType(),
                                                                      modifiedResource,
                                                                      request.getResourceAddress(),
                                                                      request.getResourceType());
        }
        return modifiedRequest;
    }

    public void clearRoutingHints() {
        this.partitionKeyRangeIdentity = null;
        this.requestContext.resolvedPartitionKeyRange = null;
    }

    public synchronized Flux<ByteBuf> getContentAsByteBufFlux() {
        if (contentAsByteArray == null) {
            return Flux.empty();
        }

        return Flux.just(Unpooled.wrappedBuffer(contentAsByteArray));
    }

    public synchronized Flux<byte[]> getContentAsByteArrayFlux() {
        if (contentAsByteArray == null) {
            return Flux.empty();
        }

        return Flux.just(contentAsByteArray);
    }

    public int getContentLength() {
        return contentAsByteArray != null ? contentAsByteArray.length : 0;
    }

    public byte[] getContentAsByteArray() {
        return contentAsByteArray;
    }

    @Override
    public RxDocumentServiceRequest clone() {
        RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(this.clientContext, this.getOperationType(), this.resourceId,this.getResourceType(),this.getHeaders());
        rxDocumentServiceRequest.setPartitionKeyInternal(this.getPartitionKeyInternal());
        rxDocumentServiceRequest.setContentBytes(rxDocumentServiceRequest.contentAsByteArray);
        rxDocumentServiceRequest.setContinuation(this.getContinuation());
        rxDocumentServiceRequest.setDefaultReplicaIndex(this.getDefaultReplicaIndex());
        rxDocumentServiceRequest.setEndpointOverride(this.getEndpointOverride());
        rxDocumentServiceRequest.setForceNameCacheRefresh(this.isForceNameCacheRefresh());
        rxDocumentServiceRequest.setIsMedia(this.getIsMedia());
        rxDocumentServiceRequest.setOriginalSessionToken(this.getOriginalSessionToken());
        rxDocumentServiceRequest.setPartitionKeyRangeIdentity(this.getPartitionKeyRangeIdentity());
        rxDocumentServiceRequest.forceCollectionRoutingMapRefresh = this.forceCollectionRoutingMapRefresh;
        rxDocumentServiceRequest.forcePartitionKeyRangeRefresh = this.forcePartitionKeyRangeRefresh;
        rxDocumentServiceRequest.UseGatewayMode = this.UseGatewayMode;
        rxDocumentServiceRequest.requestContext = this.requestContext;
        return rxDocumentServiceRequest;
    }

    public void dispose() {
        if (this.isDisposed) {
            return;
        }

        if (this.contentAsByteArray != null) {
            this.contentAsByteArray = null;
        }

        this.isDisposed = true;
    }

    private static Map<String, Object> getProperties(Object options) {
        if (options == null) {
            return null;
        } else if (options instanceof RequestOptions) {
            return ((RequestOptions) options).getProperties();
        } else if (options instanceof CosmosQueryRequestOptions) {
            return ModelBridgeInternal.getPropertiesFromQueryRequestOptions(
                (CosmosQueryRequestOptions) options);
        } else if (options instanceof CosmosChangeFeedRequestOptions) {
            return ModelBridgeInternal.getPropertiesFromChangeFeedRequestOptions(
                (CosmosChangeFeedRequestOptions) options);
        } else {
            return null;
        }
    }

    private static String getThroughputControlGroupName(Object options) {
        if (options == null) {
            return null;
        } else if (options instanceof RequestOptions) {
            return ((RequestOptions) options).getThroughputControlGroupName();
        } else if (options instanceof CosmosQueryRequestOptions) {
            return ((CosmosQueryRequestOptions) options).getThroughputControlGroupName();
        } else if (options instanceof CosmosChangeFeedRequestOptions) {
            return ((CosmosChangeFeedRequestOptions) options).getThroughputControlGroupName();
        } else {
            return null;
        }
    }

    public static byte[] toByteArray(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }

        byteBuffer.rewind();
        byte[] arr = new byte[byteBuffer.limit()];
        byteBuffer.get(arr);
        return arr;
    }

    private static ByteBuffer wrapByteBuffer(byte[] bytes) {
        return bytes != null ? ByteBuffer.wrap(bytes) : null;
    }

    public CosmosDiagnostics createCosmosDiagnostics() {
        return this.clientContext.createDiagnostics();
    }

    /**
     * Getter for property 'addressRefresh'.
     *
     * @return Value for property 'addressRefresh'.
     */
    public boolean isAddressRefresh() {
        return isAddressRefresh;
    }

    /**
     * Setter for property 'addressRefresh'.
     *
     * @param addressRefresh Value to set for property 'addressRefresh'.
     */
    public void setAddressRefresh(final boolean addressRefresh) {
        isAddressRefresh = addressRefresh;
    }

    public String getThroughputControlGroupName() { return this.throughputControlGroupName; }

    public int getNumberOfItemsInBatchRequest() {
        return numberOfItemsInBatchRequest;
    }

    public void setNumberOfItemsInBatchRequest(int numberOfItemsInBatchRequest) {
        this.numberOfItemsInBatchRequest = numberOfItemsInBatchRequest;
    }
}
