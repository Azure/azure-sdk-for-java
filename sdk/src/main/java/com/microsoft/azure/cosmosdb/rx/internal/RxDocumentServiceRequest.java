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

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.PathsHelper;
import com.microsoft.azure.cosmosdb.internal.QueryCompatibilityMode;
import com.microsoft.azure.cosmosdb.internal.RequestChargeTracker;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.StoreResponse;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;

import rx.Observable;
import rx.observables.StringObservable;

/**
 * This is core Transport/Connection agnostic request to the Azure Cosmos DB database service.
 */
public class RxDocumentServiceRequest {
    private String resourceId;
    private final ResourceType resourceType;
    private final String path;
    private final Map<String, String> headers;
    private volatile String continuation;
    private boolean isMedia = false;
    private final boolean isNameBased;
    private final OperationType operationType;
    private final String resourceAddress;
    private volatile boolean forceNameCacheRefresh;
    private volatile boolean forceAddressRefresh;
    private volatile long sessionLsn;
    private volatile URI endpointOverride = null;
    private final String activityId;
    private volatile RequestChargeTracker requestChargeTracker;
    private volatile String resourceFullName;
    private volatile long quorumSelectedLSN;
    private volatile long globalCommittedSelectedLSN;
    private volatile StoreResponse globalStrongWriteResponse;
    private volatile ConsistencyLevel originalRequestConsistencyLevel;
    private volatile String originalSessionToken;
    private volatile String resolvedCollectionRid;
    private volatile PartitionKeyRangeIdentity partitionKeyRangeIdentity;
    private volatile PartitionKeyRange resolvedPartitionKeyRange;
    private volatile Integer defaultReplicaIndex;

    private Observable<byte[]> contentObservable;
    private byte[] byteContent;
    
    // NOTE: TODO: these fields are copied from .Net SDK
    // some of these fields are missing from the main java sdk service request
    // so it means most likely the corresponding features are also missing from the main sdk
    // we need to wire this up.
    public boolean UseGatewayMode;
    public boolean useWriteEndpoint;
    public boolean clearSessionTokenOnSessionReadFailure;

    public boolean isReadOnlyRequest() {
        return this.operationType == OperationType.Read
                || this.operationType == OperationType.ReadFeed
                || this.operationType == OperationType.Head
                || this.operationType == OperationType.HeadFeed
                || this.operationType == OperationType.Query
                || this.operationType == OperationType.SqlQuery;
    }
    
    /**
     * Creates a AbstractDocumentServiceRequest
     * 
     * @param operationType     the operation type.
     * @param resourceId        the resource id.
     * @param resourceType      the resource type.   
     * @param path              the path.
     * @param headers           the headers
     */
    private RxDocumentServiceRequest(OperationType operationType,
            String resourceId,
            ResourceType resourceType,
            String path,
            Map<String, String> headers) {
        this.operationType = operationType;
        this.resourceType = resourceType;
        this.path = path;
        this.sessionLsn = -1;
        this.headers = headers != null ? headers : new HashMap<>();
        this.isNameBased = Utils.isNameBased(path);
        this.activityId = Utils.randomUUID().toString();
        if (!this.isNameBased) {
            if (resourceType == ResourceType.Media) {
                this.resourceId = getAttachmentIdFromMediaId(resourceId);
            } else {
                this.resourceId = resourceId;
            }
            this.resourceAddress = resourceId;
        } else {
            this.resourceAddress = this.path;
            this.resourceId = null;
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
            Observable<byte[]> contentObservable,
            byte[] content,
            String path,
            Map<String, String> headers) {
        this( operationType,
             resourceId,
             resourceType,
             path,
             headers);
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
            Observable<byte[]> contentObservable,
            Map<String, String> headers) {
        this(operationType, extractIdFromUri(path), resourceType, contentObservable, null, path, headers);
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
            Map<String, String> headers) {
        this(operationType, extractIdFromUri(path), resourceType, null, byteContent, path, headers);
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
            Map<String, String> headers) {
        this(operationType, extractIdFromUri(path), resourceType, null , null, path, headers);
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
            Observable<byte[]> content,
            Map<String, String> headers) {
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, content, headers);
    }
    
    /**
     * Creates a DocumentServiceRequest with a stream.
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
            Map<String, String> headers) {
        // StringObservable is mis-named. It doesn't make any assumptions on character set
        // and handles bytes only
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, StringObservable.from(inputStream), headers);
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

        return new RxDocumentServiceRequest(operation, resourceType, relativePath,
                // TODO: this re-encodes, can we improve performance here?
                resource.toJson().getBytes(StandardCharsets.UTF_8), headers);
    }

    /**
     * Creates a DocumentServiceRequest with a query.
     *
     * @param operation    the operation type.
     * @param resourceType the resource type.
     * @param relativePath the relative URI path.
     * @param query        the query.
     * @param headers      the request headers.
     * @return the created document service request.
     */
    public static RxDocumentServiceRequest create(OperationType operation,
            ResourceType resourceType,
            String relativePath,
            String query,
            Map<String, String> headers) {
        
        return new RxDocumentServiceRequest(operation, resourceType, relativePath,
                query.getBytes(StandardCharsets.UTF_8), headers);
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
            if (querySpec.getParameters() != null && querySpec.getParameters().size() > 0) {
                throw new IllegalArgumentException(
                        String.format("Unsupported argument in query compatibility mode '{%s}'",
                                queryCompatibilityMode.name()));
            }

            operation = OperationType.SqlQuery;
            queryText = querySpec.getQueryText();
            break;

        case Default:
        case Query:
        default:
            operation = OperationType.Query;
            queryText = querySpec.toJson();
            break;
        }

        Observable<byte[]> body = StringObservable.encode(Observable.just(queryText), StandardCharsets.UTF_8);
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, body, headers);
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
        return new RxDocumentServiceRequest(operation, resourceType, relativePath, headers);
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
        String path = PathsHelper.generatePath(resourceType, resourceId, Utils.isFeedRequest(operation));
        return new RxDocumentServiceRequest(operation, resourceId, resourceType, null, null, path, headers);
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
        // before last ( at length -2 ) and the the type will before it
        // ( at length -3 )
        // In the second case, to extract the resource type it will the element
        // before last ( at length -2 )
        String[] pathParts = path.split("/");
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
     * Gets the path.
     *
     * @return the path.
     */
    public String getPath() {
        return this.path;
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

    public boolean isForceAddressRefresh() {
        return forceAddressRefresh;
    }

    public void setForceAddressRefresh(boolean forceAddressRefresh) {
        this.forceAddressRefresh = forceAddressRefresh;
    }

    public long getSessionLsn() {
        return this.sessionLsn;
    }

    public void setSessionLsn(long sessionLsn) {
        this.sessionLsn = sessionLsn;
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

    public RequestChargeTracker getRequestChargeTracker() {
        return requestChargeTracker;
    }

    public void setRequestChargeTracker(RequestChargeTracker requestChargeTracker) {
        this.requestChargeTracker = requestChargeTracker;
    }

    public String getResourceFullName() {
        if (this.isNameBased) {
            String trimmedPath = Utils.trimBeginingAndEndingSlashes(this.path);
            String[] segments = trimmedPath.split("/");

            if (segments.length % 2 == 0) {
                // if path has even segments, it is the individual resource
                // like dbs/db1/colls/coll1
                if (Utils.IsResourceType(segments[segments.length - 2])) {
                    this.resourceFullName = trimmedPath;
                }
            } else {
                // if path has odd segments, get the parent(dbs/db1 from
                // dbs/db1/colls)
                if (Utils.IsResourceType(segments[segments.length - 1])) {
                    this.resourceFullName = trimmedPath.substring(0, trimmedPath.lastIndexOf("/"));
                }
            }
        } else {
            this.resourceFullName = this.getResourceId().toLowerCase();
        }

        return this.resourceFullName;
    }

    public String getResolvedCollectionRid() {
        return resolvedCollectionRid;
    }

    public void setResolvedCollectionRid(String resolvedCollectionRid) {
        this.resolvedCollectionRid = resolvedCollectionRid;
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

    public PartitionKeyRange getResolvedPartitionKeyRange() {
        return resolvedPartitionKeyRange;
    }

    public void setResolvedPartitionKeyRange(PartitionKeyRange resolvedPartitionKeyRange) {
        this.resolvedPartitionKeyRange = resolvedPartitionKeyRange;
    }

    public long getQuorumSelectedLSN() {
        return quorumSelectedLSN;
    }

    public void setQuorumSelectedLSN(long quorumSelectedLSN) {
        this.quorumSelectedLSN = quorumSelectedLSN;
    }

    public ConsistencyLevel getOriginalRequestConsistencyLevel() {
        return originalRequestConsistencyLevel;
    }

    public void setOriginalRequestConsistencyLevel(ConsistencyLevel originalRequestConsistencyLevel) {
        this.originalRequestConsistencyLevel = originalRequestConsistencyLevel;
    }

    public StoreResponse getGlobalStrongWriteResponse() {
        return globalStrongWriteResponse;
    }

    public void setGlobalStrongWriteResponse(StoreResponse globalStrongWriteResponse) {
        this.globalStrongWriteResponse = globalStrongWriteResponse;
    }

    public long getGlobalCommittedSelectedLSN() {
        return globalCommittedSelectedLSN;
    }

    public void setGlobalCommittedSelectedLSN(long globalCommittedSelectedLSN) {
        this.globalCommittedSelectedLSN = globalCommittedSelectedLSN;
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

    public Observable<byte[]> getContentObservable() {
        return contentObservable;
    }
    
    public byte[] getContent() {
        return byteContent;
    }
}
