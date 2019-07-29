// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ChangeFeedOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.directconnectivity.WFConstants;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This is core Transport/Connection agnostic request to the Azure Cosmos DB database service.
 */
public class RxDocumentServiceRequest {
    private static final char PREFER_HEADER_SEPERATOR = ';';
    private static final String PREFER_HEADER_VALUE_FORMAT = "%s=%s";

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
    private final String activityId;
    private volatile String resourceFullName;

    private volatile String originalSessionToken;
    private volatile PartitionKeyRangeIdentity partitionKeyRangeIdentity;
    private volatile Integer defaultReplicaIndex;

    public DocumentServiceRequestContext requestContext;

    private Flux<byte[]> contentObservable;
    private byte[] byteContent;
    
    // NOTE: TODO: these fields are copied from .Net SDK
    // some of these fields are missing from the main java sdk service request
    // so it means most likely the corresponding features are also missing from the main sdk
    // we need to wire this up.
    public boolean UseGatewayMode;

    private volatile boolean isDisposed = false;
    public volatile String entityId;
    public volatile String queryString;
    public volatile boolean isFeed;
    public volatile AuthorizationTokenType authorizationTokenType;
    public volatile Map<String, Object> properties;

    public boolean isReadOnlyRequest() {
        return this.operationType == OperationType.Read
                || this.operationType == OperationType.ReadFeed
                || this.operationType == OperationType.Head
                || this.operationType == OperationType.HeadFeed
                || this.operationType == OperationType.Query
                || this.operationType == OperationType.SqlQuery;
    }

    public boolean isReadOnlyScript() {
        String isReadOnlyScript = this.headers.get(HttpConstants.HttpHeaders.IS_READ_ONLY_SCRIPT);
        if(StringUtils.isEmpty(isReadOnlyScript)) {
            return false;
        } else {
            return this.operationType.equals(OperationType.ExecuteJavaScript) && isReadOnlyScript.equalsIgnoreCase(Boolean.TRUE.toString());
        }
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
    private RxDocumentServiceRequest(OperationType operationType,
                                     String resourceIdOrFullName,
                                     ResourceType resourceType,
                                     byte[] byteContent,
                                     Map<String, String> headers,
                                     boolean isNameBased,
                                     AuthorizationTokenType authorizationTokenType) {
        this.operationType = operationType;
        this.forceNameCacheRefresh = false;
        this.resourceType = resourceType;
        this.byteContent = byteContent;
        this.headers = headers != null ? headers : new HashMap<>();
        this.activityId = Utils.randomUUID().toString();
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
     * @param resourceIdOrFullName        the request id or full name.
     * @param resourceType      the resource type.
     * @param path              the path.
     * @param headers           the headers
     */
    private RxDocumentServiceRequest(OperationType operationType,
            String resourceIdOrFullName,
            ResourceType resourceType,
            String path,
            Map<String, String> headers) {
        this.requestContext = new DocumentServiceRequestContext();
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.requestContext.sessionToken = null;
        this.headers = headers != null ? headers : new HashMap<>();
        this.activityId = Utils.randomUUID().toString();
        this.isFeed = false;
        PathInfo pathInfo = new PathInfo(false, null, null, false);
        if (StringUtils.isNotEmpty(path)) {
            if (PathsHelper.tryParsePathSegments(path, pathInfo, null)) {
                this.isNameBased = pathInfo.isNameBased;
                this.isFeed = pathInfo.isFeed;
                resourceIdOrFullName = pathInfo.resourceIdOrFullName;
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
            this.resourceAddress = resourceIdOrFullName;
        }

        if (StringUtils.isNotEmpty(this.headers.get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID))) {
            this.partitionKeyRangeIdentity = PartitionKeyRangeIdentity
                    .fromHeader(this.headers.get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID));
        }
    }

    /**
     * Creates a DocumentServiceRequest
     *
     * @param resourceId        the resource Id.
     * @param resourceType      the resource type.
     * @param content           the byte content observable\
     * @param contentObservable the byte content observable
     * @param headers           the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            String resourceId,
            ResourceType resourceType,
            Flux<byte[]> contentObservable,
            byte[] content,
            String path,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        this( operationType,
             resourceId,
             resourceType,
             path,
             headers);
        this.authorizationTokenType = authorizationTokenType;
        this.byteContent = content;
        this.contentObservable = contentObservable;
    }

    /**
     * Creates a DocumentServiceRequest with an HttpEntity.
     *
     * @param resourceType          the resource type.
     * @param path                  the relative URI path.
     * @param contentObservable     the byte content observable
     * @param headers               the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            ResourceType resourceType,
            String path,
            Flux<byte[]> contentObservable,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        this(operationType, extractIdFromUri(path), resourceType, contentObservable, null, path, headers, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest with an HttpEntity.
     *
     * @param resourceType the resource type.
     * @param path         the relative URI path.
     * @param byteContent  the byte content.
     * @param headers      the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            ResourceType resourceType,
            String path,
            byte[] byteContent,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        this(operationType, extractIdFromUri(path), resourceType, null, byteContent, path, headers, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest with an HttpEntity.
     *
     * @param resourceType          the resource type.
     * @param path                  the relative URI path.
     * @param headers               the request headers.
     */
    private RxDocumentServiceRequest(OperationType operationType,
            ResourceType resourceType,
            String path,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        this(operationType, extractIdFromUri(path), resourceType, null , null, path, headers, authorizationTokenType);
    }

    public void setContentBytes(byte[] bytes) {
        this.byteContent = bytes;
    }

    /**
     * Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param content      the content observable
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Flux<byte[]> content,
            Map<String, String> headers) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, content, headers, AuthorizationTokenType.PrimaryMasterKey);
    }

    /**
     * Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param content      the content observable
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Flux<byte[]> content,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, content, headers, authorizationTokenType);
    }

    /** Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param inputStream  the input stream.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            InputStream inputStream,
            Map<String, String> headers) throws IOException {
        Flux<byte[]> byteFlux = Flux.just(IOUtils.toByteArray(inputStream));
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, byteFlux, headers, AuthorizationTokenType.PrimaryMasterKey);
    }

    /** Creates a DocumentServiceRequest with a stream.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param inputStream  the input stream.
     * @param headers      the request headers.
     * @param authorizationTokenType      the request authorizationTokenType.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            InputStream inputStream,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) throws IOException {
        Flux<byte[]> byteFlux = Flux.just(IOUtils.toByteArray(inputStream));
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, byteFlux, headers, authorizationTokenType);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Resource resource,
            Map<String, String> headers) {
        return RxDocumentServiceRequest.create(operation, resourceType, relativePath, resource, headers, (RequestOptions)null);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Resource resource,
            Map<String, String> headers,
            Object options) {

        RxDocumentServiceRequest request = new RxDocumentServiceRequest(operation, resourceType, relativePath,
                // TODO: this re-encodes, can we improve performance here?
                resource.toJson().getBytes(StandardCharsets.UTF_8), headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
        return request;
    }

    /**
     * Creates a DocumentServiceRequest with a query.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param query        the query.
     * @param headers      the request headers.
     * @param options      the request/feed/changeFeed options.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            String query,
            Map<String, String> headers,
            Object options) {
        RxDocumentServiceRequest request = new RxDocumentServiceRequest(operation, resourceType, relativePath,
                query.getBytes(StandardCharsets.UTF_8), headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
        return request;
    }

    /**
     * Creates a DocumentServiceRequest with a query.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param query        the query.
     * @param headers      the request headers.
     * @param authorizationTokenType      the request authorizationTokenType.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            String query,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath,
                query.getBytes(StandardCharsets.UTF_8), headers, authorizationTokenType);
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
    public static RxDocumentServiceRequest create(ResourceType resourceType,
            String relativePath,
            SqlQuerySpec querySpec,
            QueryCompatibilityMode queryCompatibilityMode,
            Map<String, String> headers) {
        OperationType operation;
        String queryText;
        switch (queryCompatibilityMode) {
        case SqlQuery:
            if (querySpec.parameters() != null && querySpec.parameters().size() > 0) {
                throw new IllegalArgumentException(
                        String.format("Unsupported argument in query compatibility mode '{%s}'",
                                queryCompatibilityMode.toString()));
            }

            operation = OperationType.SqlQuery;
            queryText = querySpec.queryText();
            break;

        case Default:
        case Query:
        default:
            operation = OperationType.Query;
            queryText = querySpec.toJson();
            break;
        }

        Flux<byte[]> body = Flux.just(queryText).map(s -> StandardCharsets.UTF_8.encode(s).array());
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, body, headers, AuthorizationTokenType.PrimaryMasterKey);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Map<String, String> headers) {
        return RxDocumentServiceRequest.create(operation, resourceType, relativePath, headers, (RequestOptions)null);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Map<String, String> headers,
            Object options) {
        RxDocumentServiceRequest request = new RxDocumentServiceRequest(operation, resourceType, relativePath, headers, AuthorizationTokenType.PrimaryMasterKey);
        request.properties = getProperties(options);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, headers, authorizationTokenType);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            Resource resource,
            ResourceType resourceType,
            String relativePath,
            Map<String, String> headers) {
        byte[] resourceContent = resource.toJson().getBytes(StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, resourceContent, headers, AuthorizationTokenType.PrimaryMasterKey);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            Resource resource,
            ResourceType resourceType,
            String relativePath,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        byte[] resourceContent = resource.toJson().getBytes(StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, resourceContent, headers, authorizationTokenType);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            String resourceId,
            ResourceType resourceType,
            Map<String, String> headers) {
        return new RxDocumentServiceRequest(operation, resourceId,resourceType, null, headers, false, AuthorizationTokenType.PrimaryMasterKey) ;
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
    public static RxDocumentServiceRequest create(OperationType operation,
            String resourceId,
            ResourceType resourceType,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(operation, resourceId, resourceType, null, headers, false, authorizationTokenType);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            String resourceId,
            ResourceType resourceType,
            Resource resource,
            Map<String, String> headers) {
        byte[] resourceContent = resource.toJson().getBytes(StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operation, resourceId, resourceType, resourceContent, headers, false, AuthorizationTokenType.PrimaryMasterKey);
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
    public static RxDocumentServiceRequest create(OperationType operation,
            String resourceId,
            ResourceType resourceType,
            Resource resource,
            Map<String, String> headers,
            AuthorizationTokenType authorizationTokenType) {
        byte[] resourceContent = resource.toJson().getBytes(StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operation, resourceId, resourceType, resourceContent, headers, false, authorizationTokenType);
    }

    /**
     * Creates a DocumentServiceRequest with operationType and resourceType
     * @param operation     the operation type
     * @param resourceType  the resource type
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
                                                  ResourceType resourceType) {
        return new RxDocumentServiceRequest(operation, null, resourceType, null, null);
    }

    public static RxDocumentServiceRequest createFromName(
            OperationType operationType,
            String resourceFullName,
            ResourceType resourceType) {
        return new RxDocumentServiceRequest(operationType,
                resourceFullName,
                resourceType,
                null,
                new HashMap<>(),
                true,
                AuthorizationTokenType.PrimaryMasterKey
        );
    }

    public static RxDocumentServiceRequest createFromName(
            OperationType operationType,
            String resourceFullName,
            ResourceType resourceType,
            AuthorizationTokenType authorizationTokenType) {
        return new RxDocumentServiceRequest(operationType,
                resourceFullName,
                resourceType,
                null,
                new HashMap<>(),
                true,
                authorizationTokenType
        );
    }

    public static RxDocumentServiceRequest createFromName(
            OperationType operationType,
            Resource resource,
            String resourceFullName,
            ResourceType resourceType) {
        byte[] resourceContent = resource.toJson().getBytes(StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operationType,
                resourceFullName,
                resourceType,
                resourceContent,
                new HashMap<>(),
                true,
                AuthorizationTokenType.PrimaryMasterKey
        );
    }

    public static RxDocumentServiceRequest createFromName(
            OperationType operationType,
            Resource resource,
            String resourceFullName,
            ResourceType resourceType,
            AuthorizationTokenType authorizationTokenType) {
        byte[] resourceContent = resource.toJson().getBytes(StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operationType,
                resourceFullName,
                resourceType,
                resourceContent,
                new HashMap<>(),
                true,
                authorizationTokenType
        );
    }

    private static String extractIdFromUri(String path) {
        if (path.length() == 0) {
            return path;
        }

        if (path.charAt(path.length() - 1) != '/') {
            path = path + '/';
        }

        if (path.charAt(0) != '/') {
            path = '/' + path;
        }
        // This is a hack. We need a padding '=' so that path.split("/")
        // returns even number of string pieces.
        // TODO(pushi): Improve the code and remove the hack.
        path = path + '=';

        // The path will be in the form of 
        // /[resourceType]/[resourceId]/ or
        // /[resourceType]/[resourceId]/[resourceType]/
        // The result of split will be in the form of
        // [[[resourceType], [resourceId] ... ,[resourceType], ""]
        // In the first case, to extract the resourceId it will the element
        // before last ( at length -2 ) and the type will before it
        // ( at length -3 )
        // In the second case, to extract the resource type it will the element
        // before last ( at length -2 )
        String[] pathParts = StringUtils.split(path, "/");
        if (pathParts.length % 2 == 0) {
            // request in form /[resourceType]/[resourceId]/.
            return pathParts[pathParts.length - 2];
        } else {
            // request in form /[resourceType]/[resourceId]/[resourceType]/.
            return pathParts[pathParts.length - 3];
        }
    }

    static String getAttachmentIdFromMediaId(String mediaId) {
        // '/' was replaced with '-'.
        byte[] buffer = Utils.Base64Decoder.decode(mediaId.replace('-', '/').getBytes());

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

    public String getActivityId() {
        return this.activityId;
    }

    public PartitionKeyRangeIdentity getPartitionKeyRangeIdentity() {
        return partitionKeyRangeIdentity;
    }

    public void routeTo(PartitionKeyRangeIdentity partitionKeyRangeIdentity) {
        this.setPartitionKeyRangeIdentity(partitionKeyRangeIdentity);
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

    public void addPreferHeader(String preferHeaderName, String preferHeaderValue) {
        String headerToAdd = String.format(PREFER_HEADER_VALUE_FORMAT, preferHeaderName, preferHeaderValue);
        String preferHeader = this.headers.get(HttpConstants.HttpHeaders.PREFER);
        if(StringUtils.isNotEmpty(preferHeader)) {
            preferHeader += PREFER_HEADER_SEPERATOR + headerToAdd;
        } else {
            preferHeader = headerToAdd;
        }
        this.headers.put(HttpConstants.HttpHeaders.PREFER, preferHeader);
    }

    public static RxDocumentServiceRequest CreateFromResource(RxDocumentServiceRequest request, Resource modifiedResource) {
        RxDocumentServiceRequest modifiedRequest;
        if (!request.getIsNameBased()) {
            modifiedRequest = RxDocumentServiceRequest.create(request.getOperationType(),
                                                              request.getResourceId(),
                                                              request.getResourceType(),
                                                              modifiedResource,
                                                              request.headers);
        } else {
            modifiedRequest = RxDocumentServiceRequest.createFromName(request.getOperationType(),
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

    public Flux<byte[]> getContentObservable() {
        return contentObservable;
    }

    public byte[] getContent() {
        return byteContent;
    }

    public RxDocumentServiceRequest clone() {
        RxDocumentServiceRequest rxDocumentServiceRequest = RxDocumentServiceRequest.create(this.getOperationType(), this.resourceId,this.getResourceType(),this.getHeaders());
        rxDocumentServiceRequest.setContentBytes(this.getContent());
        rxDocumentServiceRequest.setContinuation(this.getContinuation());
        rxDocumentServiceRequest.setDefaultReplicaIndex(this.getDefaultReplicaIndex());
        rxDocumentServiceRequest.setEndpointOverride(this.getEndpointOverride());
        rxDocumentServiceRequest.setForceNameCacheRefresh(this.isForceNameCacheRefresh());
        rxDocumentServiceRequest.setIsMedia(this.getIsMedia());
        rxDocumentServiceRequest.setOriginalSessionToken(this.getOriginalSessionToken());
        rxDocumentServiceRequest.setPartitionKeyRangeIdentity(this.getPartitionKeyRangeIdentity());
        rxDocumentServiceRequest.contentObservable = this.getContentObservable();
        rxDocumentServiceRequest.forceCollectionRoutingMapRefresh = this.forceCollectionRoutingMapRefresh;
        rxDocumentServiceRequest.forcePartitionKeyRangeRefresh = this.forcePartitionKeyRangeRefresh;
        rxDocumentServiceRequest.UseGatewayMode = this.UseGatewayMode;
        rxDocumentServiceRequest.queryString = this.queryString;
        rxDocumentServiceRequest.requestContext = this.requestContext;
        return rxDocumentServiceRequest;
    }

    public void Dispose() {
        if (this.isDisposed) {
            return;
        }

        if (this.byteContent != null) {
            this.byteContent = null;
        }

        this.isDisposed = true;
    }

    private static Map<String, Object> getProperties(Object options) {
        if (options == null) {
            return null;
        } else if (options instanceof RequestOptions) {
            return ((RequestOptions) options).getProperties();
        } else if (options instanceof FeedOptions) {
            return ((FeedOptions) options).properties();
        } else if (options instanceof ChangeFeedOptions) {
            return ((ChangeFeedOptions) options).properties();
        } else {
            return null;
        }
    }
}
