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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.Integers;
import com.microsoft.azure.cosmosdb.internal.InternalServerErrorException;
import com.microsoft.azure.cosmosdb.internal.Lists;
import com.microsoft.azure.cosmosdb.internal.Longs;
import com.microsoft.azure.cosmosdb.internal.MutableVolatile;
import com.microsoft.azure.cosmosdb.internal.OperationType;
import com.microsoft.azure.cosmosdb.internal.PathsHelper;
import com.microsoft.azure.cosmosdb.internal.ResourceType;
import com.microsoft.azure.cosmosdb.internal.RuntimeConstants;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.rx.internal.BadRequestException;
import com.microsoft.azure.cosmosdb.rx.internal.Configs;
import com.microsoft.azure.cosmosdb.rx.internal.HttpClientFactory;
import com.microsoft.azure.cosmosdb.rx.internal.InvalidPartitionException;
import com.microsoft.azure.cosmosdb.rx.internal.NotFoundException;
import com.microsoft.azure.cosmosdb.rx.internal.PartitionIsMigratingException;
import com.microsoft.azure.cosmosdb.rx.internal.PartitionKeyRangeIsSplittingException;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import com.microsoft.azure.cosmosdb.rx.internal.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.client.RxClient;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpRequestHeaders;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.cosmosdb.internal.Utils.trimBeginningAndEndingSlashes;
/*
 * The following code only support Document Write without any error handling support.
 */
public class HttpTransportClient extends TransportClient {
    private final Logger logger = LoggerFactory.getLogger(HttpTransportClient.class);
    private final CompositeHttpClient<ByteBuf, ByteBuf> httpClient;
    private final Map<String, String> defaultHeaders;
    private final Configs configs;

    CompositeHttpClient<ByteBuf, ByteBuf> createHttpClient(int requestTimeout) {
        // TODO: use one instance of SSL context everywhere
        HttpClientFactory httpClientFactory = new HttpClientFactory(this.configs);
        httpClientFactory.withRequestTimeoutInMillis(requestTimeout * 1000);
        httpClientFactory.withPoolSize(configs.getDirectHttpsMaxConnectionLimit());

        return httpClientFactory.toHttpClientBuilder().build();
    }

    public HttpTransportClient(Configs configs, int requestTimeout, UserAgentContainer userAgent) {
        this.configs = configs;
        this.httpClient = createHttpClient(requestTimeout);

        this.defaultHeaders = new HashMap<>();

        // Set requested API version header for version enforcement.
        this.defaultHeaders.put(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);
        this.defaultHeaders.put(HttpConstants.HttpHeaders.CACHE_CONTROL, HttpConstants.HeaderValues.NoCache);

        if (userAgent == null) {
            userAgent = new UserAgentContainer();
        }

        this.defaultHeaders.put(HttpConstants.HttpHeaders.USER_AGENT, userAgent.getUserAgent());
        this.defaultHeaders.put(HttpConstants.HttpHeaders.ACCEPT, RuntimeConstants.MediaTypes.JSON);
    }

    @Override
    public void close() {
        httpClient.shutdown();
    }

    public Single<StoreResponse> invokeStoreAsync(
        URI physicalAddress,
        ResourceOperation resourceOperation,
        RxDocumentServiceRequest request) {

        try {

            // uuid correlation manager
            UUID activityId = UUID.fromString(request.getActivityId());

            if (resourceOperation.operationType == OperationType.Recreate) {
                Map<String, String> errorResponseHeaders = new HashMap<>();
                errorResponseHeaders.put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");

                logger.error("Received Recreate request on Http client");
                throw new InternalServerErrorException(RMResources.InternalServerError, null, errorResponseHeaders, null);
            }

            HttpClientRequest<ByteBuf> httpRequest = prepareHttpMessage(activityId, physicalAddress, resourceOperation, request);
            RxClient.ServerInfo serverInfo = new RxClient.ServerInfo(physicalAddress.getHost(), physicalAddress.getPort());

            MutableVolatile<Instant> sendTimeUtc = new MutableVolatile<>();

            Single<HttpClientResponse<ByteBuf>> responseMessage = this.httpClient.submit(serverInfo, httpRequest).toSingle();
            responseMessage = responseMessage.doOnSubscribe(() -> {
                 sendTimeUtc.v = Instant.now();
                this.beforeRequest(
                        activityId,
                        httpRequest.getUri(),
                        request.getResourceType(),
                        httpRequest.getHeaders());
            });

            responseMessage = responseMessage.onErrorResumeNext(t -> {

                Exception exception = Utils.as(t, Exception.class);
                if (exception == null) {
                    logger.error("critical failure", t);
                    t.printStackTrace();
                    assert false : "critical failure";
                    return Single.error(t);
                }

                //Trace.CorrelationManager.ActivityId = activityId;
                if (WebExceptionUtility.isWebExceptionRetriable(exception)) {
                    logger.debug("Received retriable exception {} " +
                                    "sending the request to {}, will re-resolve the address " +
                                    "send time UTC: {}",
                            exception,
                            physicalAddress,
                            sendTimeUtc);

                    GoneException goneException = new GoneException(
                            String.format(
                                    RMResources.ExceptionMessage,
                                    RMResources.Gone),
                            exception,
                            null,
                            physicalAddress);

                    return Single.error(goneException);
                } else if (request.isReadOnlyRequest()) {
                    logger.trace("Received exception {} on readonly request" +
                                    "sending the request to {}, will reresolve the address " +
                                    "send time UTC: {}",
                            exception,
                            physicalAddress,
                            sendTimeUtc);

                    GoneException goneException = new GoneException(
                            String.format(
                                    RMResources.ExceptionMessage,
                                    RMResources.Gone),
                            exception,
                            null,
                            physicalAddress);

                    return Single.error(goneException);
                } else {
                    // We can't throw a GoneException here because it will cause retry and we don't
                    // know if the request failed before or after the message got sent to the server.
                    // So in order to avoid duplicating the request we will not retry.
                    // TODO: a possible solution for this is to add the ability to send a request to the server
                    // to check if the previous request was received or not and act accordingly.
                    ServiceUnavailableException serviceUnavailableException = new ServiceUnavailableException(
                            String.format(
                                    RMResources.ExceptionMessage,
                                    RMResources.ServiceUnavailable),
                            exception,
                            null,
                            physicalAddress.toString());
                    serviceUnavailableException.getResponseHeaders().put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");
                    serviceUnavailableException.getResponseHeaders().put(HttpConstants.HttpHeaders.WRITE_REQUEST_TRIGGER_ADDRESS_REFRESH, "1");
                    return Single.error(serviceUnavailableException);
                }
            }).doOnSuccess(httpClientResponse -> {
                Instant receivedTimeUtc = Instant.now();
                double durationInMilliSeconds = (receivedTimeUtc.toEpochMilli() - sendTimeUtc.v.toEpochMilli());
                this.afterRequest(
                        activityId,
                        httpClientResponse.getStatus().code() ,
                        durationInMilliSeconds,
                        httpClientResponse.getHeaders());
            }).doOnError( e -> {
                Instant receivedTimeUtc = Instant.now();
                double durationInMilliSeconds = (receivedTimeUtc.toEpochMilli() - sendTimeUtc.v.toEpochMilli());
                this.afterRequest(
                        activityId,
                        0,
                        durationInMilliSeconds,
                        null);
            });

            return responseMessage.flatMap(rsp -> processHttpResponse(request.getResourceAddress(), httpRequest, activityId.toString(), rsp, physicalAddress));

        } catch (Exception e) {
            // TODO improve on exception catching
            return Single.error(e);
        }
    }

    private void beforeRequest(UUID activityId, String uri, ResourceType resourceType, HttpRequestHeaders requestHeaders) {
        // TODO: perf counters
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
    }

    private void afterRequest(UUID activityId,
                              int statusCode,
                              double durationInMilliSeconds,
                              HttpResponseHeaders responseHeaders) {
        // TODO: perf counters
        // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
    }

    private static void addHeader(HttpRequestHeaders requestHeaders, String headerName, RxDocumentServiceRequest request) {
        String headerValue = request.getHeaders().get(headerName);
        if (!Strings.isNullOrEmpty(headerValue)) {
            requestHeaders.add(headerName, headerValue);
        }
    }

    private static void addHeader(HttpRequestHeaders requestHeaders, String headerName, String headerValue) {
        if (!Strings.isNullOrEmpty(headerValue)) {
            requestHeaders.add(headerName, headerValue);
        }
    }

    private String GetMatch(RxDocumentServiceRequest request, ResourceOperation resourceOperation) {
        switch (resourceOperation.operationType) {
            case Delete:
            case ExecuteJavaScript:
            case Replace:
            case Update:
            case Upsert:
                return request.getHeaders().get(HttpConstants.HttpHeaders.IF_MATCH);

            case Read:
            case ReadFeed:
                return request.getHeaders().get(HttpConstants.HttpHeaders.IF_NONE_MATCH);

            default:
                return null;
        }
    }

    private HttpClientRequest<ByteBuf> prepareHttpMessage(
        UUID activityId,
        URI physicalAddress,
        ResourceOperation resourceOperation,
        RxDocumentServiceRequest request) throws Exception {

        HttpClientRequest<ByteBuf> httpRequestMessage = null;
        URI requestUri;
        HttpMethod method;

        // The StreamContent created below will own and dispose its underlying stream, but we may need to reuse the stream on the
        // RxDocumentServiceRequest for future requests. Hence we need to clone without incurring copy cost, so that when
        // HttpRequestMessage -> StreamContent -> MemoryStream all get disposed, the original stream will be left open.
        switch (resourceOperation.operationType) {
            case Create:
                requestUri = this.getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                httpRequestMessage.withContent(request.getContent());
                break;

            case ExecuteJavaScript:
                requestUri = this.getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                httpRequestMessage.withContent(request.getContent());
                break;

            case Delete:
                requestUri = this.getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.DELETE;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                break;

            case Read:
                requestUri = this.getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.GET;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                break;

            case ReadFeed:
                requestUri = this.getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.GET;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                break;

            case Replace:
                requestUri = this.getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.PUT;
                assert request.getContent() != null;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                httpRequestMessage.withContent(request.getContent());
                break;

            case Update:
                requestUri = this.getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = new HttpMethod("PATCH");
                assert request.getContent() != null;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                httpRequestMessage.withContent(request.getContent());
                break;

            case Query:
            case SqlQuery:
                requestUri = this.getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                httpRequestMessage.withContent(request.getContent());
                HttpTransportClient.addHeader(httpRequestMessage.getHeaders(), HttpConstants.HttpHeaders.CONTENT_TYPE, request);
                break;

            case Upsert:
                requestUri = this.getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.POST;
                assert request.getContent() != null;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                httpRequestMessage.withContent(request.getContent());
                break;

            case Head:
                requestUri = this.getResourceEntryUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.HEAD;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                break;

            case HeadFeed:
                requestUri = this.getResourceFeedUri(resourceOperation.resourceType, physicalAddress, request);
                method = HttpMethod.HEAD;
                httpRequestMessage = HttpClientRequest.create(method, requestUri.toString());
                break;

            default:
                assert false : "Unsupported operation type";
                throw new IllegalStateException();
        }

        Map<String, String> documentServiceRequestHeaders = request.getHeaders();
        HttpRequestHeaders httpRequestHeaders = httpRequestMessage.getHeaders();

        // add default headers
        for(Map.Entry<String, String> entry: defaultHeaders.entrySet()) {
            HttpTransportClient.addHeader(httpRequestHeaders, entry.getKey(), entry.getValue());
        }

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.VERSION, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.USER_AGENT, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PAGE_SIZE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PRE_TRIGGER_INCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PRE_TRIGGER_EXCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POST_TRIGGER_INCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POST_TRIGGER_EXCLUDE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.AUTHORIZATION, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.INDEXING_DIRECTIVE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.MIGRATE_COLLECTION_DIRECTIVE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.SESSION_TOKEN, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PREFER, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.RESOURCE_TOKEN_EXPIRY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ENABLE_SCAN_IN_QUERY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.EMIT_VERBOSE_TRACES_IN_QUERY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CAN_CHARGE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CAN_THROTTLE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ENABLE_LOW_PRECISION_ORDER_BY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ENABLE_LOGGING, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_READ_ONLY_SCRIPT, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CONTENT_SERIALIZATION_FORMAT, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CONTINUATION, request.getContinuation());
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.ACTIVITY_ID, activityId.toString());
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PARTITION_KEY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, request);

        String dateHeader = HttpUtils.getDateHeader(documentServiceRequestHeaders);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.X_DATE, dateHeader);
        HttpTransportClient.addHeader(httpRequestHeaders, "Match", this.GetMatch(request, resourceOperation));
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IF_MODIFIED_SINCE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.A_IM, request);
        if (!request.getIsNameBased()) {
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.RESOURCE_ID, request.getResourceId());
        }

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.ENTITY_ID, request.entityId);

        String fanoutRequestHeader = request.getHeaders().get(WFConstants.BackendHeaders.IS_FANOUT_REQUEST);
        HttpTransportClient.addHeader(httpRequestMessage.getHeaders(), WFConstants.BackendHeaders.IS_FANOUT_REQUEST, fanoutRequestHeader);

        if (request.getResourceType() == ResourceType.DocumentCollection) {
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.COLLECTION_PARTITION_INDEX, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.COLLECTION_PARTITION_INDEX));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.COLLECTION_SERVICE_INDEX, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.COLLECTION_SERVICE_INDEX));
        }

        if (documentServiceRequestHeaders.get(WFConstants.BackendHeaders.BIND_REPLICA_DIRECTIVE) != null) {
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.BIND_REPLICA_DIRECTIVE, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.BIND_REPLICA_DIRECTIVE));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.PRIMARY_MASTER_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.PRIMARY_MASTER_KEY));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.SECONDARY_MASTER_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.SECONDARY_MASTER_KEY));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.PRIMARY_READONLY_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.PRIMARY_READONLY_KEY));
            HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.SECONDARY_READONLY_KEY, documentServiceRequestHeaders.get(WFConstants.BackendHeaders.SECONDARY_READONLY_KEY));
        }

        if (documentServiceRequestHeaders.get(HttpConstants.HttpHeaders.CAN_OFFER_REPLACE_COMPLETE) != null) {
            HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CAN_OFFER_REPLACE_COMPLETE, documentServiceRequestHeaders.get(HttpConstants.HttpHeaders.CAN_OFFER_REPLACE_COMPLETE));
        }

        //Query
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_QUERY, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.QUERY, request);

        // Upsert
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.IS_UPSERT, request);

        // SupportSpatialLegacyCoordinates
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.SUPPORT_SPATIAL_LEGACY_COORDINATES, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.PARTITION_COUNT, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.COLLECTION_RID, request);

        // Filter by schema
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.FILTER_BY_SCHEMA_RESOURCE_ID, request);

        // UsePolygonsSmallerThanAHemisphere
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.USE_POLYGONS_SMALLER_THAN_AHEMISPHERE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.GATEWAY_SIGNATURE, request);

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_QUOTA_INFO, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_QUERY_METRICS, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.FORCE_QUERY_SCAN, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB, request);
        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.REMOTE_STORAGE_TYPE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.SHARE_THROUGHPUT, request);

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_PARTITION_STATISTICS, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.POPULATE_COLLECTION_THROUGHPUT_INFO, request);

        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.REMAINING_TIME_IN_MS_ON_CLIENT_REQUEST, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.CLIENT_RETRY_ATTEMPT_COUNT, request);

        // target lsn for head requests.
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.TARGET_LSN, request);
        HttpTransportClient.addHeader(httpRequestHeaders, HttpConstants.HttpHeaders.TARGET_GLOBAL_COMMITTED_LSN, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.FEDERATION_ID_FOR_AUTH, request);

        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.FANOUT_OPERATION_STATE, request);
        HttpTransportClient.addHeader(httpRequestHeaders, WFConstants.BackendHeaders.ALLOW_TENTATIVE_WRITES, request);

        HttpTransportClient.addHeader(httpRequestHeaders, CustomHeaders.HttpHeaders.EXCLUDE_SYSTEM_PROPERTIES, request);

        return httpRequestMessage;
    }

    static URI getResourceFeedUri(ResourceType resourceType, URI physicalAddress, RxDocumentServiceRequest request) throws Exception {
        switch (resourceType) {
            case Attachment:
                return getAttachmentFeedUri(physicalAddress, request);
            case DocumentCollection:
                return getCollectionFeedUri(physicalAddress, request);
            case Conflict:
                return getConflictFeedUri(physicalAddress, request);
            case Database:
                return getDatabaseFeedUri(physicalAddress);
            case Document:
                return getDocumentFeedUri(physicalAddress, request);
            case Permission:
                return getPermissionFeedUri(physicalAddress, request);
            case StoredProcedure:
                return getStoredProcedureFeedUri(physicalAddress, request);
            case Trigger:
                return getTriggerFeedUri(physicalAddress, request);
            case User:
                return getUserFeedUri(physicalAddress, request);

            case UserDefinedFunction:
                return getUserDefinedFunctionFeedUri(physicalAddress, request);
            case Schema:
                return getSchemaFeedUri(physicalAddress, request);
            case Offer:
                return getOfferFeedUri(physicalAddress, request);

//          Other types: Replica, Module, ModuleCommand, Record, UserDefinedType not applicable to SDK.

            default:
                assert false : "Unexpected resource type: " + resourceType;
                throw new NotFoundException();
        }
    }

    static URI getResourceEntryUri(ResourceType resourceType, URI physicalAddress, RxDocumentServiceRequest request) throws Exception {
        switch (resourceType) {
            case Attachment:
                return getAttachmentEntryUri(physicalAddress, request);
            case DocumentCollection:
                return getCollectionEntryUri(physicalAddress, request);
            case Conflict:
                return getConflictEntryUri(physicalAddress, request);
            case Database:
                return getDatabaseEntryUri(physicalAddress, request);
            case Document:
                return getDocumentEntryUri(physicalAddress, request);
            case Permission:
                return getPermissionEntryUri(physicalAddress, request);
            case StoredProcedure:
                return getStoredProcedureEntryUri(physicalAddress, request);
            case Trigger:
                return getTriggerEntryUri(physicalAddress, request);
            case User:
                return getUserEntryUri(physicalAddress, request);
            case UserDefinedFunction:
                return getUserDefinedFunctionEntryUri(physicalAddress, request);
            case Schema:
                return getSchemaEntryUri(physicalAddress, request);
            case Offer:
                return getOfferEntryUri(physicalAddress, request);

//          Other types: Replica, Module, ModuleCommand, Record, UserDefinedType not applicable to SDK.

            default:
                assert false: "Unexpected resource type: " + resourceType;
                throw new IllegalStateException();
        }
    }

    static URI createURI(URI baseAddress, String resourcePath) {
        return baseAddress.resolve(HttpUtils.urlEncode(trimBeginningAndEndingSlashes(resourcePath)));
    }

    static URI getRootFeedUri(URI baseAddress) {
        return baseAddress;
    }

    static URI getDatabaseFeedUri(URI baseAddress) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Database, StringUtils.EMPTY, true));
    }

    static URI getDatabaseEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Database, request, false));
    }

    static URI getCollectionFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.DocumentCollection, request, true));
    }

    static URI getStoredProcedureFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.StoredProcedure, request, true));
    }

    static URI getTriggerFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Trigger, request, true));
    }

    static URI getUserDefinedFunctionFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.UserDefinedFunction, request, true));
    }

    static URI getCollectionEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.DocumentCollection, request, false));
    }

    static URI getStoredProcedureEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.StoredProcedure, request, false));
    }

    static URI getTriggerEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Trigger, request, false));
    }

    static URI getUserDefinedFunctionEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.UserDefinedFunction, request, false));
    }

    static URI getDocumentFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Document, request, true));
    }

    static URI getDocumentEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Document, request, false));
    }

    static URI getConflictFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Conflict, request, true));
    }

    static URI getConflictEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Conflict, request, false));
    }

    static URI getAttachmentFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Attachment, request, true));
    }

    static URI getAttachmentEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Attachment, request, false));
    }

    static URI getUserFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.User, request, true));
    }

    static URI getUserEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.User, request, false));
    }

    static URI getPermissionFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Permission, request, true));
    }

    static URI getPermissionEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Permission, request, false));
    }

    static URI getOfferFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Offer, request, true));
    }


    static URI getSchemaFeedUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Schema, request, true));
    }

    static URI getSchemaEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Schema, request, false));
    }

    static URI getOfferEntryUri(URI baseAddress, RxDocumentServiceRequest request) throws Exception {
        return createURI(baseAddress, PathsHelper.generatePath(ResourceType.Offer, request, false));
    }

    static String getHeader(String[] names, String[] values, String name) {
        for (int idx = 0; idx < names.length; idx++) {
            if (Strings.areEqual(names[idx], name)) {
                return values[idx];
            }
        }

        return null;
    }

    private Single<StoreResponse> processHttpResponse(String resourceAddress, HttpClientRequest<ByteBuf> request, String activityId, HttpClientResponse<ByteBuf> response, URI physicalAddress) {
        if (response == null) {
            InternalServerErrorException exception =
                new InternalServerErrorException(
                    String.format(
                        RMResources.ExceptionMessage,
                        RMResources.InvalidBackendResponse),
                    null,
                    physicalAddress);
            exception.getResponseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
                activityId);
            exception.getResponseHeaders().put(HttpConstants.HttpHeaders.REQUEST_VALIDATION_FAILURE, "1");

            return Single.error(exception);
        }

        // If the status code is < 300 or 304 NotModified (we treat not modified as success) then it means that it's a success code and shouldn't throw.
        if (response.getStatus().code() < HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY ||
            response.getStatus().code() == HttpConstants.StatusCodes.NOT_MODIFIED) {
            return HttpTransportClient.createStoreResponseFromHttpResponse(response);
        }
        else {
            return this.createErrorResponseFromHttpResponse(resourceAddress, activityId, request, response);
        }
    }

    private Single<StoreResponse> createErrorResponseFromHttpResponse(String resourceAddress, String activityId,
                                                                      HttpClientRequest<ByteBuf> request,
                                                                      HttpClientResponse<ByteBuf> response) {
        HttpResponseStatus statusCode = response.getStatus();
        Single<String> errorMessageObs = ErrorUtils.getErrorResponseAsync(response);

        return errorMessageObs.flatMap(
                errorMessage -> {
                    long responseLSN = -1;

                    List<String> lsnValues;
                    if ((lsnValues = response.getHeaders().getAll(WFConstants.BackendHeaders.LSN)) != null) {
                        String temp = lsnValues.isEmpty() ? null : lsnValues.get(0);
                        responseLSN = Longs.tryParse(temp, responseLSN);
                    }

                    String responsePartitionKeyRangeId = null;
                    List<String> partitionKeyRangeIdValues;
                    if ((partitionKeyRangeIdValues = response.getHeaders().getAll(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID)) != null) {
                        responsePartitionKeyRangeId = Lists.firstOrDefault(partitionKeyRangeIdValues, null);
                    }

                    DocumentClientException exception;

                    switch (statusCode.code()) {
                        case HttpConstants.StatusCodes.UNAUTHORIZED:
                            exception = new UnauthorizedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.Unauthorized : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.FORBIDDEN:
                            exception = new ForbiddenException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.Forbidden : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.NOTFOUND:
                            // HTTP.SYS returns NotFound (404) if the URI
                            // is not registered. This is really an indication that
                            // the replica which registered the URI is not
                            // available at the server. We detect this case by
                            // the presence of Content-Type header in the response
                            // and map it to HTTP Gone (410), which is the more
                            // appropriate response for this case.
                            if (response.getContent() != null && response.getHeaders() != null && response.getHeaders().get(HttpConstants.HttpHeaders.CONTENT_TYPE) != null &&
                                    !Strings.isNullOrEmpty(response.getHeaders().get(HttpConstants.HttpHeaders.CONTENT_TYPE)) &&
                                    Strings.containsIgnoreCase(response.getHeaders().get(HttpConstants.HttpHeaders.CONTENT_TYPE), RuntimeConstants.MediaTypes.TEXT_HTML)) {
                                // Have the request URL in the exception message for debugging purposes.
                                exception = new GoneException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                RMResources.Gone),
                                        request.getUri());
                                exception.getResponseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
                                        activityId);

                                break;
                            } else {
                                exception = new NotFoundException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.NotFound : errorMessage),
                                        response.getHeaders(),
                                        request.getUri());
                                break;
                            }

                        case HttpConstants.StatusCodes.BADREQUEST:
                            exception = new BadRequestException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.BadRequest : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.METHOD_NOT_ALLOWED:
                            exception = new MethodNotAllowedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.MethodNotAllowed : errorMessage),
                                    null,
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.GONE: {

                            // TODO: update perf counter
                            // https://msdata.visualstudio.com/CosmosDB/_workitems/edit/258624
                            ErrorUtils.logGoneException(request.getUri(), activityId);

                            Integer nSubStatus = 0;
                            String valueSubStatus = null;

                            valueSubStatus = response.getHeaders().get(WFConstants.BackendHeaders.SUB_STATUS);
                            if (!Strings.isNullOrEmpty(valueSubStatus)) {
                                if ((nSubStatus = Integers.tryParse(valueSubStatus)) == null) {
                                    exception = new InternalServerErrorException(
                                            String.format(
                                                    RMResources.ExceptionMessage,
                                                    RMResources.InvalidBackendResponse),
                                            response.getHeaders(),
                                            request.getUri());
                                    break;
                                }
                            }

                            if (nSubStatus == HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE) {
                                exception = new InvalidPartitionException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.getHeaders(),
                                        request.getUri());
                                break;
                            } else if (nSubStatus == HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE) {
                                exception = new PartitionKeyRangeGoneException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.getHeaders(),
                                        request.getUri());
                                break;
                            } else if (nSubStatus == HttpConstants.SubStatusCodes.COMPLETING_SPLIT) {
                                exception = new PartitionKeyRangeIsSplittingException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.getHeaders(),
                                        request.getUri());
                                break;
                            } else if (nSubStatus == HttpConstants.SubStatusCodes.COMPLETING_PARTITION_MIGRATION) {
                                exception = new PartitionIsMigratingException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                Strings.isNullOrEmpty(errorMessage) ? RMResources.Gone : errorMessage),
                                        response.getHeaders(),
                                        request.getUri());
                                break;
                            } else {
                                // Have the request URL in the exception message for debugging purposes.
                                exception = new GoneException(
                                        String.format(
                                                RMResources.ExceptionMessage,
                                                RMResources.Gone),
                                        response.getHeaders(),
                                        request.getUri());

                                exception.getResponseHeaders().put(HttpConstants.HttpHeaders.ACTIVITY_ID,
                                        activityId);
                                break;
                            }
                        }

                        case HttpConstants.StatusCodes.CONFLICT:
                            exception = new ConflictException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.EntityAlreadyExists : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.PRECONDITION_FAILED:
                            exception = new PreconditionFailedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.PreconditionFailed : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.REQUEST_ENTITY_TOO_LARGE:
                            exception = new RequestEntityTooLargeException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            String.format(
                                                    RMResources.RequestEntityTooLarge,
                                                    HttpConstants.HttpHeaders.PAGE_SIZE)),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.LOCKED:
                            exception = new LockedException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.Locked : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.SERVICE_UNAVAILABLE:
                            exception = new ServiceUnavailableException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.ServiceUnavailable : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.REQUEST_TIMEOUT:
                            exception = new RequestTimeoutException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.RequestTimeout : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.RETRY_WITH:
                            exception = new RetryWithException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.RetryWith : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        case HttpConstants.StatusCodes.TOO_MANY_REQUESTS:
                            exception =
                                    new RequestRateTooLargeException(
                                            String.format(
                                                    RMResources.ExceptionMessage,
                                                    Strings.isNullOrEmpty(errorMessage) ? RMResources.TooManyRequests : errorMessage),
                                            response.getHeaders(),
                                            request.getUri());

                            List<String> values = response.getHeaders().getAll(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS);

                            if (values == null || values.isEmpty()) {
                                logger.warn("RequestRateTooLargeException being thrown without RetryAfter.");
                            } else {
                                exception.getResponseHeaders().put(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, values.get(0));
                            }

                            break;

                        case HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR:
                            exception = new InternalServerErrorException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            Strings.isNullOrEmpty(errorMessage) ? RMResources.InternalServerError : errorMessage),
                                    response.getHeaders(),
                                    request.getUri());
                            break;

                        default:
                            logger.error("Unrecognized status code {} returned by backend. ActivityId {}", statusCode, activityId);
                            ErrorUtils.logException(request.getUri(), activityId);
                            exception = new InternalServerErrorException(
                                    String.format(
                                            RMResources.ExceptionMessage,
                                            RMResources.InvalidBackendResponse),
                                    response.getHeaders(),
                                    request.getUri());
                            break;
                    }

                    BridgeInternal.setLSN(exception, responseLSN);
                    BridgeInternal.setPartitionKeyRangeId(exception, responsePartitionKeyRangeId);
                    BridgeInternal.setResourceAddress(exception, resourceAddress);
                    BridgeInternal.setRequestHeaders(exception, HttpUtils.asMap(request.getHeaders()));

                    return Single.error(exception);
                }
        );
    }

    private static Single<StoreResponse> createStoreResponseFromHttpResponse(
        HttpClientResponse<ByteBuf> responseMessage) {

        Single<StoreResponse> storeResponse = ResponseUtils.toStoreResponse(responseMessage);
        return storeResponse;
    }
}
