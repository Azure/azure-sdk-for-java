// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.ContentSerializationFormat;
import com.azure.cosmos.implementation.EnumerationDirection;
import com.azure.cosmos.implementation.FanoutOperationState;
import com.azure.cosmos.implementation.MigrateCollectionDirective;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.ReadFeedKeyType;
import com.azure.cosmos.implementation.RemoteStorageType;
import com.azure.cosmos.implementation.ResourceId;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.EnumUtils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.IndexingDirective;
import com.fasterxml.jackson.annotation.JsonFilter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.azure.cosmos.implementation.HttpConstants.Headers;
import static com.azure.cosmos.implementation.HttpConstants.HeaderValues;
import static com.azure.cosmos.implementation.directconnectivity.WFConstants.BackendHeaders;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdConsistencyLevel;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdContentSerializationFormat;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdEnumerationDirection;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdFanoutOperationState;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdIndexingDirective;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdMigrateCollectionDirective;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdOperationType;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdReadFeedKeyType;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRemoteStorageType;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdRequestHeader;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

@JsonFilter("RntbdToken")
final class RntbdRequestHeaders extends RntbdTokenStream<RntbdRequestHeader> {

    // region Fields

    private static final String URL_TRIM = "/";

    // endregion

    // region Constructors

    RntbdRequestHeaders(final RntbdRequestArgs args, final RntbdRequestFrame frame) {

        this(Unpooled.EMPTY_BUFFER);

        checkNotNull(args, "args");
        checkNotNull(frame, "frame");

        final RxDocumentServiceRequest request = args.serviceRequest();
        final byte[] content = request.getContentAsByteArray();

        this.getPayloadPresent().setValue(content != null && content.length > 0);
        this.getReplicaPath().setValue(args.replicaPath());
        this.getTransportRequestID().setValue(args.transportRequestId());

        final HttpHeaders headers = request.getHeaders();

        // Special-case headers

        this.addAimHeader(headers);
        this.addAllowScanOnQuery(headers);
        this.addBinaryIdIfPresent(headers);
        this.addCanCharge(headers);
        this.addCanOfferReplaceComplete(headers);
        this.addCanThrottle(headers);
        this.addCollectionRemoteStorageSecurityIdentifier(headers);
        this.addConsistencyLevelHeader(headers);
        this.addContentSerializationFormat(headers);
        this.addContinuationToken(request);
        this.addDateHeader(headers);
        this.addDisableRUPerMinuteUsage(headers);
        this.addEmitVerboseTracesInQuery(headers);
        this.addEnableLogging(headers);
        this.addEnableLowPrecisionOrderBy(headers);
        this.addEntityId(headers);
        this.addEnumerationDirection(headers);
        this.addExcludeSystemProperties(headers);
        this.addFanoutOperationStateHeader(headers);
        this.addIfModifiedSinceHeader(headers);
        this.addIndexingDirectiveHeader(headers);
        this.addIsAutoScaleRequest(headers);
        this.addIsFanout(headers);
        this.addIsReadOnlyScript(headers);
        this.addIsUserRequest(headers);
        this.addMatchHeader(headers, frame.getOperationType());
        this.addMigrateCollectionDirectiveHeader(headers);
        this.addPageSize(headers);
        this.addPopulateCollectionThroughputInfo(headers);
        this.addPopulatePartitionStatistics(headers);
        this.addPopulateQueryMetrics(headers);
        this.addPopulateQuotaInfo(headers);
        this.addProfileRequest(headers);
        this.addQueryForceScan(headers);
        this.addRemoteStorageType(headers);
        this.addResourceIdOrPathHeaders(request);
        this.addResponseContinuationTokenLimitInKb(headers);
        this.addShareThroughput(headers);
        this.addStartAndEndKeys(headers);
        this.addSupportSpatialLegacyCoordinates(headers);
        this.addUsePolygonsSmallerThanAHemisphere(headers);
        this.addReturnPreference(headers);

        // Normal headers (Strings, Ints, Longs, etc.)

        this.fillTokenFromHeader(headers, this::getAllowTentativeWrites, BackendHeaders.ALLOW_TENTATIVE_WRITES);
        this.fillTokenFromHeader(headers, this::getAuthorizationToken, Headers.AUTHORIZATION);
        this.fillTokenFromHeader(headers, this::getBinaryPassThroughRequest, BackendHeaders.BINARY_PASSTHROUGH_REQUEST);
        this.fillTokenFromHeader(headers, this::getBindReplicaDirective, BackendHeaders.BIND_REPLICA_DIRECTIVE);
        this.fillTokenFromHeader(headers, this::getClientRetryAttemptCount, Headers.CLIENT_RETRY_ATTEMPT_COUNT);
        this.fillTokenFromHeader(headers, this::getCollectionPartitionIndex, BackendHeaders.COLLECTION_PARTITION_INDEX);
        this.fillTokenFromHeader(headers, this::getCollectionRid, BackendHeaders.COLLECTION_RID);
        this.fillTokenFromHeader(headers, this::getCollectionServiceIndex, BackendHeaders.COLLECTION_SERVICE_INDEX);
        this.fillTokenFromHeader(headers, this::getEffectivePartitionKey, BackendHeaders.EFFECTIVE_PARTITION_KEY);
        this.fillTokenFromHeader(headers, this::getEnableDynamicRidRangeAllocation, BackendHeaders.ENABLE_DYNAMIC_RID_RANGE_ALLOCATION);
        this.fillTokenFromHeader(headers, this::getFilterBySchemaRid, Headers.FILTER_BY_SCHEMA_RESOURCE_ID);
        this.fillTokenFromHeader(headers, this::getGatewaySignature, Headers.GATEWAY_SIGNATURE);
        this.fillTokenFromHeader(headers, this::getPartitionCount, BackendHeaders.PARTITION_COUNT);
        this.fillTokenFromHeader(headers, this::getPartitionKey, Headers.PARTITION_KEY);
        this.fillTokenFromHeader(headers, this::getPartitionKeyRangeId, Headers.PARTITION_KEY_RANGE_ID);
        this.fillTokenFromHeader(headers, this::getPartitionResourceFilter, BackendHeaders.PARTITION_RESOURCE_FILTER);
        this.fillTokenFromHeader(headers, this::getPostTriggerExclude, Headers.POST_TRIGGER_EXCLUDE);
        this.fillTokenFromHeader(headers, this::getPostTriggerInclude, Headers.POST_TRIGGER_INCLUDE);
        this.fillTokenFromHeader(headers, this::getPreTriggerExclude, Headers.PRE_TRIGGER_EXCLUDE);
        this.fillTokenFromHeader(headers, this::getPreTriggerInclude, Headers.PRE_TRIGGER_INCLUDE);
        this.fillTokenFromHeader(headers, this::getPrimaryMasterKey, BackendHeaders.PRIMARY_MASTER_KEY);
        this.fillTokenFromHeader(headers, this::getPrimaryReadonlyKey, BackendHeaders.PRIMARY_READONLY_KEY);
        this.fillTokenFromHeader(headers, this::getRemainingTimeInMsOnClientRequest, Headers.REMAINING_TIME_IN_MS_ON_CLIENT_REQUEST);
        this.fillTokenFromHeader(headers, this::getResourceSchemaName, BackendHeaders.RESOURCE_SCHEMA_NAME);
        this.fillTokenFromHeader(headers, this::getResourceTokenExpiry, Headers.RESOURCE_TOKEN_EXPIRY);
        this.fillTokenFromHeader(headers, this::getRestoreMetadataFilter, Headers.RESTORE_METADATA_FILTER);
        this.fillTokenFromHeader(headers, this::getRestoreParams, BackendHeaders.RESTORE_PARAMS);
        this.fillTokenFromHeader(headers, this::getSecondaryMasterKey, BackendHeaders.SECONDARY_MASTER_KEY);
        this.fillTokenFromHeader(headers, this::getSecondaryReadonlyKey, BackendHeaders.SECONDARY_READONLY_KEY);
        this.fillTokenFromHeader(headers, this::getSessionToken, Headers.SESSION_TOKEN);
        this.fillTokenFromHeader(headers, this::getSharedOfferThroughput, Headers.SHARED_OFFER_THROUGHPUT);
        this.fillTokenFromHeader(headers, this::getTargetGlobalCommittedLsn, Headers.TARGET_GLOBAL_COMMITTED_LSN);
        this.fillTokenFromHeader(headers, this::getTargetLsn, Headers.TARGET_LSN);
        this.fillTokenFromHeader(headers, this::getTimeToLiveInSeconds, BackendHeaders.TIME_TO_LIVE_IN_SECONDS);
        this.fillTokenFromHeader(headers, this::getTransportRequestID, Headers.TRANSPORT_REQUEST_ID);

        // Will be null in case of direct, which is fine - BE will use the value slice the connection context this.
        // When this is used in Gateway, the header value will be populated with the proxied HTTP request's header,
        // and BE will respect the per-request value.

        this.fillTokenFromHeader(headers, this::getClientVersion, Headers.VERSION);
    }

    private RntbdRequestHeaders(ByteBuf in) {
        super(RntbdRequestHeader.set, RntbdRequestHeader.map, in);
    }

    // endregion

    // region Methods

    static RntbdRequestHeaders decode(final ByteBuf in) {
        final RntbdRequestHeaders metadata = new RntbdRequestHeaders(in);
        return RntbdRequestHeaders.decode(metadata);
    }

    // endregion

    // region Privates

    private RntbdToken getAIM() {
        return this.get(RntbdRequestHeader.A_IM);
    }

    private RntbdToken getAllowTentativeWrites() {
        return this.get(RntbdRequestHeader.AllowTentativeWrites);
    }

    private RntbdToken getAttachmentName() {
        return this.get(RntbdRequestHeader.AttachmentName);
    }

    private RntbdToken getAuthorizationToken() {
        return this.get(RntbdRequestHeader.AuthorizationToken);
    }

    private RntbdToken getBinaryId() {
        return this.get(RntbdRequestHeader.BinaryId);
    }

    private RntbdToken getBinaryPassThroughRequest() {
        return this.get(RntbdRequestHeader.BinaryPassthroughRequest);
    }

    private RntbdToken getBindReplicaDirective() {
        return this.get(RntbdRequestHeader.BindReplicaDirective);
    }

    private RntbdToken getCanCharge() {
        return this.get(RntbdRequestHeader.CanCharge);
    }

    private RntbdToken getCanOfferReplaceComplete() {
        return this.get(RntbdRequestHeader.CanOfferReplaceComplete);
    }

    private RntbdToken getCanThrottle() {
        return this.get(RntbdRequestHeader.CanThrottle);
    }

    private RntbdToken getClientRetryAttemptCount() {
        return this.get(RntbdRequestHeader.ClientRetryAttemptCount);
    }

    private RntbdToken getClientVersion() {
        return this.get(RntbdRequestHeader.ClientVersion);
    }

    private RntbdToken getCollectionName() {
        return this.get(RntbdRequestHeader.CollectionName);
    }

    private RntbdToken getCollectionPartitionIndex() {
        return this.get(RntbdRequestHeader.CollectionPartitionIndex);
    }

    private RntbdToken getCollectionRemoteStorageSecurityIdentifier() {
        return this.get(RntbdRequestHeader.CollectionRemoteStorageSecurityIdentifier);
    }

    private RntbdToken getCollectionRid() {
        return this.get(RntbdRequestHeader.CollectionRid);
    }

    private RntbdToken getCollectionServiceIndex() {
        return this.get(RntbdRequestHeader.CollectionServiceIndex);
    }

    private RntbdToken getConflictName() {
        return this.get(RntbdRequestHeader.ConflictName);
    }

    private RntbdToken getConsistencyLevel() {
        return this.get(RntbdRequestHeader.ConsistencyLevel);
    }

    private RntbdToken getContentSerializationFormat() {
        return this.get(RntbdRequestHeader.ContentSerializationFormat);
    }

    private RntbdToken getContinuationToken() {
        return this.get(RntbdRequestHeader.ContinuationToken);
    }

    private RntbdToken getDatabaseName() {
        return this.get(RntbdRequestHeader.DatabaseName);
    }

    private RntbdToken getDate() {
        return this.get(RntbdRequestHeader.Date);
    }

    private RntbdToken getDisableRUPerMinuteUsage() {
        return this.get(RntbdRequestHeader.DisableRUPerMinuteUsage);
    }

    private RntbdToken getDocumentName() {
        return this.get(RntbdRequestHeader.DocumentName);
    }

    private RntbdToken getEffectivePartitionKey() {
        return this.get(RntbdRequestHeader.EffectivePartitionKey);
    }

    private RntbdToken getReturnPreference() {
        return this.get(RntbdRequestHeader.ReturnPreference);
    }

    private RntbdToken getEmitVerboseTracesInQuery() {
        return this.get(RntbdRequestHeader.EmitVerboseTracesInQuery);
    }

    private RntbdToken getEnableDynamicRidRangeAllocation() {
        return this.get(RntbdRequestHeader.EnableDynamicRidRangeAllocation);
    }

    private RntbdToken getEnableLogging() {
        return this.get(RntbdRequestHeader.EnableLogging);
    }

    private RntbdToken getEnableLowPrecisionOrderBy() {
        return this.get(RntbdRequestHeader.EnableLowPrecisionOrderBy);
    }

    private RntbdToken getEnableScanInQuery() {
        return this.get(RntbdRequestHeader.EnableScanInQuery);
    }

    private RntbdToken getEndEpk() {
        return this.get(RntbdRequestHeader.EndEpk);
    }

    private RntbdToken getEndId() {
        return this.get(RntbdRequestHeader.EndId);
    }

    private RntbdToken getEntityId() {
        return this.get(RntbdRequestHeader.EntityId);
    }

    private RntbdToken getEnumerationDirection() {
        return this.get(RntbdRequestHeader.EnumerationDirection);
    }

    private RntbdToken getExcludeSystemProperties() {
        return this.get(RntbdRequestHeader.ExcludeSystemProperties);
    }

    private RntbdToken getFanoutOperationState() {
        return this.get(RntbdRequestHeader.FanoutOperationState);
    }

    private RntbdToken getFilterBySchemaRid() {
        return this.get(RntbdRequestHeader.FilterBySchemaRid);
    }

    private RntbdToken getForceQueryScan() {
        return this.get(RntbdRequestHeader.ForceQueryScan);
    }

    private RntbdToken getGatewaySignature() {
        return this.get(RntbdRequestHeader.GatewaySignature);
    }

    private RntbdToken getIfModifiedSince() {
        return this.get(RntbdRequestHeader.IfModifiedSince);
    }

    private RntbdToken getIndexingDirective() {
        return this.get(RntbdRequestHeader.IndexingDirective);
    }

    private RntbdToken getIsAutoScaleRequest() {
        return this.get(RntbdRequestHeader.IsAutoScaleRequest);
    }

    private RntbdToken getIsFanout() {
        return this.get(RntbdRequestHeader.IsFanout);
    }

    private RntbdToken getIsReadOnlyScript() {
        return this.get(RntbdRequestHeader.IsReadOnlyScript);
    }

    private RntbdToken getIsUserRequest() {
        return this.get(RntbdRequestHeader.IsUserRequest);
    }

    private RntbdToken getMatch() {
        return this.get(RntbdRequestHeader.Match);
    }

    private RntbdToken getMigrateCollectionDirective() {
        return this.get(RntbdRequestHeader.MigrateCollectionDirective);
    }

    private RntbdToken getPageSize() {
        return this.get(RntbdRequestHeader.PageSize);
    }

    private RntbdToken getPartitionCount() {
        return this.get(RntbdRequestHeader.PartitionCount);
    }

    private RntbdToken getPartitionKey() {
        return this.get(RntbdRequestHeader.PartitionKey);
    }

    private RntbdToken getPartitionKeyRangeId() {
        return this.get(RntbdRequestHeader.PartitionKeyRangeId);
    }

    private RntbdToken getPartitionKeyRangeName() {
        return this.get(RntbdRequestHeader.PartitionKeyRangeName);
    }

    private RntbdToken getPartitionResourceFilter() {
        return this.get(RntbdRequestHeader.PartitionResourceFilter);
    }

    private RntbdToken getPayloadPresent() {
        return this.get(RntbdRequestHeader.PayloadPresent);
    }

    private RntbdToken getPermissionName() {
        return this.get(RntbdRequestHeader.PermissionName);
    }

    private RntbdToken getPopulateCollectionThroughputInfo() {
        return this.get(RntbdRequestHeader.PopulateCollectionThroughputInfo);
    }

    private RntbdToken getPopulatePartitionStatistics() {
        return this.get(RntbdRequestHeader.PopulatePartitionStatistics);
    }

    private RntbdToken getPopulateQueryMetrics() {
        return this.get(RntbdRequestHeader.PopulateQueryMetrics);
    }

    private RntbdToken getPopulateQuotaInfo() {
        return this.get(RntbdRequestHeader.PopulateQuotaInfo);
    }

    private RntbdToken getPostTriggerExclude() {
        return this.get(RntbdRequestHeader.PostTriggerExclude);
    }

    private RntbdToken getPostTriggerInclude() {
        return this.get(RntbdRequestHeader.PostTriggerInclude);
    }

    private RntbdToken getPreTriggerExclude() {
        return this.get(RntbdRequestHeader.PreTriggerExclude);
    }

    private RntbdToken getPreTriggerInclude() {
        return this.get(RntbdRequestHeader.PreTriggerInclude);
    }

    private RntbdToken getPrimaryMasterKey() {
        return this.get(RntbdRequestHeader.PrimaryMasterKey);
    }

    private RntbdToken getPrimaryReadonlyKey() {
        return this.get(RntbdRequestHeader.PrimaryReadonlyKey);
    }

    private RntbdToken getProfileRequest() {
        return this.get(RntbdRequestHeader.ProfileRequest);
    }

    private RntbdToken getReadFeedKeyType() {
        return this.get(RntbdRequestHeader.ReadFeedKeyType);
    }

    private RntbdToken getRemainingTimeInMsOnClientRequest() {
        return this.get(RntbdRequestHeader.RemainingTimeInMsOnClientRequest);
    }

    private RntbdToken getRemoteStorageType() {
        return this.get(RntbdRequestHeader.RemoteStorageType);
    }

    private RntbdToken getReplicaPath() {
        return this.get(RntbdRequestHeader.ReplicaPath);
    }

    private RntbdToken getResourceId() {
        return this.get(RntbdRequestHeader.ResourceId);
    }

    private RntbdToken getResourceSchemaName() {
        return this.get(RntbdRequestHeader.ResourceSchemaName);
    }

    private RntbdToken getResourceTokenExpiry() {
        return this.get(RntbdRequestHeader.ResourceTokenExpiry);
    }

    private RntbdToken getResponseContinuationTokenLimitInKb() {
        return this.get(RntbdRequestHeader.ResponseContinuationTokenLimitInKb);
    }

    private RntbdToken getRestoreMetadataFilter() {
        return this.get(RntbdRequestHeader.RestoreMetadaFilter);
    }

    private RntbdToken getRestoreParams() {
        return this.get(RntbdRequestHeader.RestoreParams);
    }

    private RntbdToken getSchemaName() {
        return this.get(RntbdRequestHeader.SchemaName);
    }

    private RntbdToken getSecondaryMasterKey() {
        return this.get(RntbdRequestHeader.SecondaryMasterKey);
    }

    private RntbdToken getSecondaryReadonlyKey() {
        return this.get(RntbdRequestHeader.SecondaryReadonlyKey);
    }

    private RntbdToken getSessionToken() {
        return this.get(RntbdRequestHeader.SessionToken);
    }

    private RntbdToken getShareThroughput() {
        return this.get(RntbdRequestHeader.ShareThroughput);
    }

    private RntbdToken getSharedOfferThroughput() {
        return this.get(RntbdRequestHeader.SharedOfferThroughput);
    }

    private RntbdToken getStartEpk() {
        return this.get(RntbdRequestHeader.StartEpk);
    }

    private RntbdToken getStartId() {
        return this.get(RntbdRequestHeader.StartId);
    }

    private RntbdToken getStoredProcedureName() {
        return this.get(RntbdRequestHeader.StoredProcedureName);
    }

    private RntbdToken getSupportSpatialLegacyCoordinates() {
        return this.get(RntbdRequestHeader.SupportSpatialLegacyCoordinates);
    }

    private RntbdToken getTargetGlobalCommittedLsn() {
        return this.get(RntbdRequestHeader.TargetGlobalCommittedLsn);
    }

    private RntbdToken getTargetLsn() {
        return this.get(RntbdRequestHeader.TargetLsn);
    }

    private RntbdToken getTimeToLiveInSeconds() {
        return this.get(RntbdRequestHeader.TimeToLiveInSeconds);
    }

    private RntbdToken getTransportRequestID() {
        return this.get(RntbdRequestHeader.TransportRequestID);
    }

    private RntbdToken getTriggerName() {
        return this.get(RntbdRequestHeader.TriggerName);
    }

    private RntbdToken getUsePolygonsSmallerThanAHemisphere() {
        return this.get(RntbdRequestHeader.UsePolygonsSmallerThanAHemisphere);
    }

    private RntbdToken getUserDefinedFunctionName() {
        return this.get(RntbdRequestHeader.UserDefinedFunctionName);
    }

    private RntbdToken getUserDefinedTypeName() {
        return this.get(RntbdRequestHeader.UserDefinedTypeName);
    }

    private RntbdToken getUserName() {
        return this.get(RntbdRequestHeader.UserName);
    }

    private void addAimHeader(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.A_IM);
        if (StringUtils.isNotEmpty(value)) {
            this.getAIM().setValue(value);
        }
    }

    private void addAllowScanOnQuery(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.ENABLE_SCAN_IN_QUERY);
        if (StringUtils.isNotEmpty(value)) {
            this.getEnableScanInQuery().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addBinaryIdIfPresent(final HttpHeaders headers) {
        final String value = headers.getValue(BackendHeaders.BINARY_ID);
        if (StringUtils.isNotEmpty(value)) {
            this.getBinaryId().setValue(Base64.getDecoder().decode(value));
        }
    }

    private void addCanCharge(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.CAN_CHARGE);
        if (StringUtils.isNotEmpty(value)) {
            this.getCanCharge().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addCanOfferReplaceComplete(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.CAN_OFFER_REPLACE_COMPLETE);
        if (StringUtils.isNotEmpty(value)) {
            this.getCanOfferReplaceComplete().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addCanThrottle(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.CAN_THROTTLE);
        if (StringUtils.isNotEmpty(value)) {
            this.getCanThrottle().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addCollectionRemoteStorageSecurityIdentifier(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.COLLECTION_REMOTE_STORAGE_SECURITY_IDENTIFIER);
        if (StringUtils.isNotEmpty(value)) {
            this.getCollectionRemoteStorageSecurityIdentifier().setValue(value);
        }
    }

    private void addConsistencyLevelHeader(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.CONSISTENCY_LEVEL);

        if (StringUtils.isNotEmpty(value)) {

            final ConsistencyLevel level = BridgeInternal.fromServiceSerializedFormat(value);

            if (level == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    Headers.CONSISTENCY_LEVEL,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (level) {
                case STRONG:
                    this.getConsistencyLevel().setValue(RntbdConsistencyLevel.Strong.id());
                    break;
                case BOUNDED_STALENESS:
                    this.getConsistencyLevel().setValue(RntbdConsistencyLevel.BoundedStaleness.id());
                    break;
                case SESSION:
                    this.getConsistencyLevel().setValue(RntbdConsistencyLevel.Session.id());
                    break;
                case EVENTUAL:
                    this.getConsistencyLevel().setValue(RntbdConsistencyLevel.Eventual.id());
                    break;
                case CONSISTENT_PREFIX:
                    this.getConsistencyLevel().setValue(RntbdConsistencyLevel.ConsistentPrefix.id());
                    break;
                default:
                    assert false;
                    break;
            }
        }
    }

    private void addContentSerializationFormat(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.CONTENT_SERIALIZATION_FORMAT);

        if (StringUtils.isNotEmpty(value)) {

            final ContentSerializationFormat format = EnumUtils.getEnumIgnoreCase(
                ContentSerializationFormat.class,
                value);

            if (format == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    Headers.CONTENT_SERIALIZATION_FORMAT,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (format) {
                case JsonText:
                    this.getContentSerializationFormat().setValue(RntbdContentSerializationFormat.JsonText.id());
                    break;
                case CosmosBinary:
                    this.getContentSerializationFormat().setValue(RntbdContentSerializationFormat.CosmosBinary.id());
                    break;
                default:
                    assert false;
            }
        }
    }

    private void addContinuationToken(final RxDocumentServiceRequest request) {
        final String value = request.getContinuation();
        if (StringUtils.isNotEmpty(value)) {
            this.getContinuationToken().setValue(value);
        }
    }

    private void addDateHeader(final HttpHeaders headers) {

        // Since the HTTP date header is overridden by some proxies/http client libraries, we support an additional date
        // header and prefer that to the (regular) date header

        String value = headers.getValue(Headers.X_DATE);

        if (StringUtils.isEmpty(value)) {
            value = headers.getValue(Headers.HTTP_DATE);
        }

        if (StringUtils.isNotEmpty(value)) {
            this.getDate().setValue(value);
        }
    }

    private void addDisableRUPerMinuteUsage(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.DISABLE_RU_PER_MINUTE_USAGE);
        if (StringUtils.isNotEmpty(value)) {
            this.getDisableRUPerMinuteUsage().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addEmitVerboseTracesInQuery(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.EMIT_VERBOSE_TRACES_IN_QUERY);
        if (StringUtils.isNotEmpty(value)) {
            this.getEmitVerboseTracesInQuery().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addEnableLogging(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.ENABLE_LOGGING);
        if (StringUtils.isNotEmpty(value)) {
            this.getEnableLogging().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addEnableLowPrecisionOrderBy(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.ENABLE_LOW_PRECISION_ORDER_BY);
        if (StringUtils.isNotEmpty(value)) {
            this.getEnableLowPrecisionOrderBy().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addEntityId(final HttpHeaders headers) {
        final String value = headers.getValue(BackendHeaders.ENTITY_ID);
        if (StringUtils.isNotEmpty(value)) {
            this.getEntityId().setValue(value);
        }
    }

    private void addEnumerationDirection(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.ENUMERATION_DIRECTION);

        if (StringUtils.isNotEmpty(value)) {

            final EnumerationDirection direction = EnumUtils.getEnumIgnoreCase(EnumerationDirection.class, value);

            if (direction == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    Headers.ENUMERATION_DIRECTION,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (direction) {
                case Forward:
                    this.getEnumerationDirection().setValue(RntbdEnumerationDirection.Forward.id());
                    break;
                case Reverse:
                    this.getEnumerationDirection().setValue(RntbdEnumerationDirection.Reverse.id());
                    break;
                default:
                    assert false;
            }
        }
    }

    private void addExcludeSystemProperties(final HttpHeaders headers) {
        final String value = headers.getValue(BackendHeaders.EXCLUDE_SYSTEM_PROPERTIES);
        if (StringUtils.isNotEmpty(value)) {
            this.getExcludeSystemProperties().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addFanoutOperationStateHeader(final HttpHeaders headers) {

        final String value = headers.getValue(BackendHeaders.FANOUT_OPERATION_STATE);

        if (StringUtils.isNotEmpty(value)) {

            final FanoutOperationState format = EnumUtils.getEnumIgnoreCase(FanoutOperationState.class, value);

            if (format == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    BackendHeaders.FANOUT_OPERATION_STATE,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (format) {
                case Started:
                    this.getFanoutOperationState().setValue(RntbdFanoutOperationState.Started.id());
                    break;
                case Completed:
                    this.getFanoutOperationState().setValue(RntbdFanoutOperationState.Completed.id());
                    break;
                default:
                    assert false;
            }
        }
    }

    private void addIfModifiedSinceHeader(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.IF_MODIFIED_SINCE);
        if (StringUtils.isNotEmpty(value)) {
            this.getIfModifiedSince().setValue(value);
        }
    }

    private void addIndexingDirectiveHeader(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.INDEXING_DIRECTIVE);

        if (StringUtils.isNotEmpty(value)) {

            final IndexingDirective directive = EnumUtils.getEnumIgnoreCase(IndexingDirective.class, value);

            if (directive == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    Headers.INDEXING_DIRECTIVE,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (directive) {
                case DEFAULT:
                    this.getIndexingDirective().setValue(RntbdIndexingDirective.Default.id());
                    break;
                case EXCLUDE:
                    this.getIndexingDirective().setValue(RntbdIndexingDirective.Exclude.id());
                    break;
                case INCLUDE:
                    this.getIndexingDirective().setValue(RntbdIndexingDirective.Include.id());
                    break;
                default:
                    assert false;
            }
        }
    }

    private void addIsAutoScaleRequest(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.IS_AUTO_SCALE_REQUEST);
        if (StringUtils.isNotEmpty(value)) {
            this.getIsAutoScaleRequest().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addIsFanout(final HttpHeaders headers) {
        final String value = headers.getValue(BackendHeaders.IS_FANOUT_REQUEST);
        if (StringUtils.isNotEmpty(value)) {
            this.getIsFanout().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addIsReadOnlyScript(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.IS_READ_ONLY_SCRIPT);
        if (StringUtils.isNotEmpty(value)) {
            this.getIsReadOnlyScript().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addIsUserRequest(final HttpHeaders headers) {
        final String value = headers.getValue(BackendHeaders.IS_USER_REQUEST);
        if (StringUtils.isNotEmpty(value)) {
            this.getIsUserRequest().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addMatchHeader(final HttpHeaders headers, final RntbdOperationType operationType) {

        String match = null;

        switch (operationType) {
            case Read:
            case ReadFeed:
                match = headers.getValue(Headers.IF_NONE_MATCH);
                break;
            default:
                match = headers.getValue(Headers.IF_MATCH);
                break;
        }

        if (StringUtils.isNotEmpty(match)) {
            this.getMatch().setValue(match);
        }
    }

    private void addMigrateCollectionDirectiveHeader(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.MIGRATE_COLLECTION_DIRECTIVE);

        if (StringUtils.isNotEmpty(value)) {

            final MigrateCollectionDirective directive = EnumUtils.getEnumIgnoreCase(MigrateCollectionDirective.class, value);

            if (directive == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    Headers.MIGRATE_COLLECTION_DIRECTIVE,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (directive) {
                case Freeze:
                    this.getMigrateCollectionDirective().setValue(RntbdMigrateCollectionDirective.Freeze.id());
                    break;
                case Thaw:
                    this.getMigrateCollectionDirective().setValue(RntbdMigrateCollectionDirective.Thaw.id());
                    break;
                default:
                    assert false;
                    break;
            }
        }
    }

    private void addPageSize(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.PAGE_SIZE);

        if (StringUtils.isNotEmpty(value)) {
            final long aLong = parseLong(Headers.PAGE_SIZE, value, -1, 0xFFFFFFFFL);
            this.getPageSize().setValue((int)(aLong < 0 ? 0xFFFFFFFFL : aLong));
        }
    }

    private void addPopulateCollectionThroughputInfo(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.POPULATE_COLLECTION_THROUGHPUT_INFO);
        if (StringUtils.isNotEmpty(value)) {
            this.getPopulateCollectionThroughputInfo().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addPopulatePartitionStatistics(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.POPULATE_PARTITION_STATISTICS);
        if (StringUtils.isNotEmpty(value)) {
            this.getPopulatePartitionStatistics().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addPopulateQueryMetrics(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.POPULATE_QUERY_METRICS);
        if (StringUtils.isNotEmpty(value)) {
            this.getPopulateQueryMetrics().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addPopulateQuotaInfo(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.POPULATE_QUOTA_INFO);
        if (StringUtils.isNotEmpty(value)) {
            this.getPopulateQuotaInfo().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addProfileRequest(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.PROFILE_REQUEST);
        if (StringUtils.isNotEmpty(value)) {
            this.getProfileRequest().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addQueryForceScan(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.FORCE_QUERY_SCAN);
        if (StringUtils.isNotEmpty(value)) {
            this.getForceQueryScan().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addRemoteStorageType(final HttpHeaders headers) {

        final String value = headers.getValue(BackendHeaders.REMOTE_STORAGE_TYPE);

        if (StringUtils.isNotEmpty(value)) {

            final RemoteStorageType type = EnumUtils.getEnumIgnoreCase(RemoteStorageType.class, value);

            if (type == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    BackendHeaders.REMOTE_STORAGE_TYPE,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (type) {
                case Standard:
                    this.getRemoteStorageType().setValue(RntbdRemoteStorageType.Standard.id());
                    break;
                case Premium:
                    this.getRemoteStorageType().setValue(RntbdRemoteStorageType.Premium.id());
                    break;
                default:
                    assert false;
            }
        }
    }

    private void addResourceIdOrPathHeaders(final RxDocumentServiceRequest request) {

        final String value = request.getResourceId();

        if (StringUtils.isNotEmpty(value)) {
            // Name-based can also have ResourceId because gateway might have generated it
            this.getResourceId().setValue(ResourceId.parse(request.getResourceType(), value));
        }

        if (request.getIsNameBased()) {

            // Assumption: format is like "dbs/dbName/colls/collName/docs/docName" or "/dbs/dbName/colls/collName",
            // not "apps/appName/partitions/partitionKey/replicas/replicaId/dbs/dbName"

            final String address = request.getResourceAddress();
            final String[] fragments = StringUtils.split(address, URL_TRIM);
            int count = fragments.length;

            if (count >= 2) {
                switch (fragments[0]) {
                    case Paths.DATABASES_PATH_SEGMENT:
                        this.getDatabaseName().setValue(fragments[1]);
                        break;
                    default:
                        final String reason = String.format(Locale.ROOT, RMResources.InvalidResourceAddress,
                            value, address);
                        throw new IllegalStateException(reason);
                }
            }

            if (count >= 4) {
                switch (fragments[2]) {
                    case Paths.COLLECTIONS_PATH_SEGMENT:
                        this.getCollectionName().setValue(fragments[3]);
                        break;
                    case Paths.USERS_PATH_SEGMENT:
                        this.getUserName().setValue(fragments[3]);
                        break;
                    case Paths.USER_DEFINED_TYPES_PATH_SEGMENT:
                        this.getUserDefinedTypeName().setValue(fragments[3]);
                        break;
                }
            }

            if (count >= 6) {
                switch (fragments[4]) {
                    case Paths.DOCUMENTS_PATH_SEGMENT:
                        this.getDocumentName().setValue(fragments[5]);
                        break;
                    case Paths.STORED_PROCEDURES_PATH_SEGMENT:
                        this.getStoredProcedureName().setValue(fragments[5]);
                        break;
                    case Paths.PERMISSIONS_PATH_SEGMENT:
                        this.getPermissionName().setValue(fragments[5]);
                        break;
                    case Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT:
                        this.getUserDefinedFunctionName().setValue(fragments[5]);
                        break;
                    case Paths.TRIGGERS_PATH_SEGMENT:
                        this.getTriggerName().setValue(fragments[5]);
                        break;
                    case Paths.CONFLICTS_PATH_SEGMENT:
                        this.getConflictName().setValue(fragments[5]);
                        break;
                    case Paths.PARTITION_KEY_RANGES_PATH_SEGMENT:
                        this.getPartitionKeyRangeName().setValue(fragments[5]);
                        break;
                    case Paths.SCHEMAS_PATH_SEGMENT:
                        this.getSchemaName().setValue(fragments[5]);
                        break;
                }
            }

            if (count >= 8) {
                switch (fragments[6]) {
                    case Paths.ATTACHMENTS_PATH_SEGMENT:
                        this.getAttachmentName().setValue(fragments[7]);
                        break;
                }
            }
        }
    }

    private void addResponseContinuationTokenLimitInKb(final HttpHeaders headers) {

        final String value = headers.getValue(Headers.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB);

        if (StringUtils.isNotEmpty(value)) {
            final long aLong = parseLong(Headers.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB, value, 0, 0xFFFFFFFFL);
            this.getResponseContinuationTokenLimitInKb().setValue((int)(aLong < 0 ? 0xFFFFFFFFL : aLong));
        }
    }

    private void addShareThroughput(final HttpHeaders headers) {
        final String value = headers.getValue(BackendHeaders.SHARE_THROUGHPUT);
        if (StringUtils.isNotEmpty(value)) {
            this.getShareThroughput().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addStartAndEndKeys(final HttpHeaders headers) {

        String value = headers.getValue(Headers.READ_FEED_KEY_TYPE);

        if (StringUtils.isNotEmpty(value)) {

            final ReadFeedKeyType type = EnumUtils.getEnumIgnoreCase(ReadFeedKeyType.class, value);

            if (type == null) {
                final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue,
                    Headers.READ_FEED_KEY_TYPE,
                    value);
                throw new IllegalStateException(reason);
            }

            switch (type) {
                case ResourceId:
                    this.getReadFeedKeyType().setValue(RntbdReadFeedKeyType.ResourceId.id());
                    break;
                case EffectivePartitionKey:
                    this.getReadFeedKeyType().setValue(RntbdReadFeedKeyType.EffectivePartitionKey.id());
                    break;
                default:
                    assert false;
            }
        }

        final Base64.Decoder decoder = Base64.getDecoder();

        value = headers.getValue(Headers.START_ID);

        if (StringUtils.isNotEmpty(value)) {
            this.getStartId().setValue(decoder.decode(value));
        }

        value = headers.getValue(Headers.END_ID);

        if (StringUtils.isNotEmpty(value)) {
            this.getEndId().setValue(decoder.decode(value));
        }

        value = headers.getValue(Headers.START_EPK);

        if (StringUtils.isNotEmpty(value)) {
            this.getStartEpk().setValue(decoder.decode(value));
        }

        value = headers.getValue(Headers.END_EPK);

        if (StringUtils.isNotEmpty(value)) {
            this.getEndEpk().setValue(decoder.decode(value));
        }
    }

    private void addSupportSpatialLegacyCoordinates(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.SUPPORT_SPATIAL_LEGACY_COORDINATES);
        if (StringUtils.isNotEmpty(value)) {
            this.getSupportSpatialLegacyCoordinates().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addUsePolygonsSmallerThanAHemisphere(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.USE_POLYGONS_SMALLER_THAN_AHEMISPHERE);
        if (StringUtils.isNotEmpty(value)) {
            this.getUsePolygonsSmallerThanAHemisphere().setValue(Boolean.parseBoolean(value));
        }
    }

    private void addReturnPreference(final HttpHeaders headers) {
        final String value = headers.getValue(Headers.PREFER);
        if (StringUtils.isNotEmpty(value) && value.contains(HeaderValues.PREFER_RETURN_MINIMAL)) {
            this.getReturnPreference().setValue(true);
        }
    }

    private void fillTokenFromHeader(final HttpHeaders headers, final Supplier<RntbdToken> supplier, final String name) {

        final String value = headers.getValue(name);

        if (StringUtils.isNotEmpty(value)) {

            final RntbdToken token = supplier.get();

            switch (token.getTokenType()) {

                case SmallString:
                case String:
                case ULongString: {

                    token.setValue(value);
                    break;
                }
                case Byte: {

                    token.setValue(Boolean.parseBoolean(value));
                    break;
                }
                case Double: {

                    token.setValue(parseDouble(name, value));
                    break;
                }
                case Long: {

                    final long aLong = parseLong(name, value, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    token.setValue(aLong);
                    break;
                }
                case ULong: {

                    final long aLong = parseLong(name, value, 0, 0xFFFFFFFFL);
                    token.setValue(aLong);
                    break;
                }
                case LongLong: {

                    final long aLong = parseLong(name, value);
                    token.setValue(aLong);
                    break;
                }
                default: {
                    assert false : "Recognized header has neither special-case nor default handling to convert "
                        + "from header String to RNTBD token";
                    break;
                }
            }
        }
    }

    private static double parseDouble(final String name, final String value) {

        final double aDouble;

        try {
            aDouble = Double.parseDouble(value);
        } catch (final NumberFormatException error) {
            final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue, name, value);
            throw new IllegalStateException(reason);
        }
        return aDouble;
    }

    private static long parseLong(final String name, final String value) {
        final long aLong;
        try {
            aLong = Long.parseLong(value);
        } catch (final NumberFormatException error) {
            final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue, name, value);
            throw new IllegalStateException(reason);
        }
        return aLong;
    }

    private static long parseLong(final String name, final String value, final long min, final long max) {
        final long aLong = parseLong(name, value);
        if (!(min <= aLong && aLong <= max)) {
            final String reason = String.format(Locale.ROOT, RMResources.InvalidRequestHeaderValue, name, aLong);
            throw new IllegalStateException(reason);
        }
        return aLong;
    }
}
