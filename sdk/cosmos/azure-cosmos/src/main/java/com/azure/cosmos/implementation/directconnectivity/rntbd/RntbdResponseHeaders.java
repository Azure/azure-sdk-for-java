// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders;
import static com.azure.cosmos.implementation.directconnectivity.WFConstants.BackendHeaders;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdIndexingDirective;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;

@SuppressWarnings("UnstableApiUsage")
@JsonFilter("RntbdToken")
class RntbdResponseHeaders extends RntbdTokenStream<RntbdResponseHeader> {

    // region Fields

    @JsonProperty
    private final RntbdToken LSN;
    @JsonProperty
    private final RntbdToken collectionLazyIndexProgress;
    @JsonProperty
    private final RntbdToken collectionPartitionIndex;
    @JsonProperty
    private final RntbdToken collectionSecurityIdentifier;
    @JsonProperty
    private final RntbdToken collectionServiceIndex;
    @JsonProperty
    private final RntbdToken collectionUpdateProgress;
    @JsonProperty
    private final RntbdToken continuationToken;
    @JsonProperty
    private final RntbdToken currentReplicaSetSize;
    @JsonProperty
    private final RntbdToken currentWriteQuorum;
    @JsonProperty
    private final RntbdToken databaseAccountId;
    @JsonProperty
    private final RntbdToken disableRntbdChannel;
    @JsonProperty
    private final RntbdToken eTag;
    @JsonProperty
    private final RntbdToken globalCommittedLSN;
    @JsonProperty
    private final RntbdToken hasTentativeWrites;
    @JsonProperty
    private final RntbdToken indexTermsGenerated;
    @JsonProperty
    private final RntbdToken indexingDirective;
    @JsonProperty
    private final RntbdToken isRUPerMinuteUsed;
    @JsonProperty
    private final RntbdToken itemCount;
    @JsonProperty
    private final RntbdToken itemLSN;
    @JsonProperty
    private final RntbdToken itemLocalLSN;
    @JsonProperty
    private final RntbdToken lastStateChangeDateTime;
    @JsonProperty
    private final RntbdToken localLSN;
    @JsonProperty
    private final RntbdToken logResults;
    @JsonProperty
    private final RntbdToken numberOfReadRegions;
    @JsonProperty
    private final RntbdToken offerReplacePending;
    @JsonProperty
    private final RntbdToken ownerFullName;
    @JsonProperty
    private final RntbdToken ownerId;
    @JsonProperty
    private final RntbdToken partitionKeyRangeId;
    @JsonProperty
    private final RntbdToken payloadPresent;
    @JsonProperty
    private final RntbdToken queriesPerformed;
    @JsonProperty
    private final RntbdToken queryMetrics;
    @JsonProperty
    private final RntbdToken indexUtilization;
    @JsonProperty
    private final RntbdToken quorumAckedLSN;
    @JsonProperty
    private final RntbdToken quorumAckedLocalLSN;
    @JsonProperty
    private final RntbdToken readsPerformed;
    @JsonProperty
    private final RntbdToken requestCharge;
    @JsonProperty
    private final RntbdToken requestValidationFailure;
    @JsonProperty
    private final RntbdToken restoreState;
    @JsonProperty
    private final RntbdToken retryAfterMilliseconds;
    @JsonProperty
    private final RntbdToken schemaVersion;
    @JsonProperty
    private final RntbdToken scriptsExecuted;
    @JsonProperty
    private final RntbdToken serverDateTimeUtc;
    @JsonProperty
    private final RntbdToken sessionToken;
    @JsonProperty
    private final RntbdToken shareThroughput;
    @JsonProperty
    private final RntbdToken storageMaxResoureQuota;
    @JsonProperty
    private final RntbdToken storageResourceQuotaUsage;
    @JsonProperty
    private final RntbdToken subStatus;
    @JsonProperty
    private final RntbdToken transportRequestID;
    @JsonProperty
    private final RntbdToken writesPerformed;
    @JsonProperty
    private final RntbdToken xpRole;
    @JsonProperty
    private final RntbdToken backendRequestDurationMilliseconds;

    // endregion

    private RntbdResponseHeaders(ByteBuf in) {

        super(RntbdResponseHeader.set, RntbdResponseHeader.map, in);

        this.LSN = this.get(RntbdResponseHeader.LSN);
        this.collectionLazyIndexProgress = this.get(RntbdResponseHeader.CollectionLazyIndexProgress);
        this.collectionPartitionIndex = this.get(RntbdResponseHeader.CollectionPartitionIndex);
        this.collectionSecurityIdentifier = this.get(RntbdResponseHeader.CollectionSecurityIdentifier);
        this.collectionServiceIndex = this.get(RntbdResponseHeader.CollectionServiceIndex);
        this.collectionUpdateProgress = this.get(RntbdResponseHeader.CollectionUpdateProgress);
        this.continuationToken = this.get(RntbdResponseHeader.ContinuationToken);
        this.currentReplicaSetSize = this.get(RntbdResponseHeader.CurrentReplicaSetSize);
        this.currentWriteQuorum = this.get(RntbdResponseHeader.CurrentWriteQuorum);
        this.databaseAccountId = this.get(RntbdResponseHeader.DatabaseAccountId);
        this.disableRntbdChannel = this.get(RntbdResponseHeader.DisableRntbdChannel);
        this.eTag = this.get(RntbdResponseHeader.ETag);
        this.globalCommittedLSN = this.get(RntbdResponseHeader.GlobalCommittedLSN);
        this.hasTentativeWrites = this.get(RntbdResponseHeader.HasTentativeWrites);
        this.indexTermsGenerated = this.get(RntbdResponseHeader.IndexTermsGenerated);
        this.indexingDirective = this.get(RntbdResponseHeader.IndexingDirective);
        this.isRUPerMinuteUsed = this.get(RntbdResponseHeader.IsRUPerMinuteUsed);
        this.itemCount = this.get(RntbdResponseHeader.ItemCount);
        this.itemLSN = this.get(RntbdResponseHeader.ItemLSN);
        this.itemLocalLSN = this.get(RntbdResponseHeader.ItemLocalLSN);
        this.lastStateChangeDateTime = this.get(RntbdResponseHeader.LastStateChangeDateTime);
        this.localLSN = this.get(RntbdResponseHeader.LocalLSN);
        this.logResults = this.get(RntbdResponseHeader.LogResults);
        this.numberOfReadRegions = this.get(RntbdResponseHeader.NumberOfReadRegions);
        this.offerReplacePending = this.get(RntbdResponseHeader.OfferReplacePending);
        this.ownerFullName = this.get(RntbdResponseHeader.OwnerFullName);
        this.ownerId = this.get(RntbdResponseHeader.OwnerId);
        this.partitionKeyRangeId = this.get(RntbdResponseHeader.PartitionKeyRangeId);
        this.payloadPresent = this.get(RntbdResponseHeader.PayloadPresent);
        this.queriesPerformed = this.get(RntbdResponseHeader.QueriesPerformed);
        this.queryMetrics = this.get(RntbdResponseHeader.QueryMetrics);
        this.indexUtilization = this.get(RntbdResponseHeader.IndexUtilization);
        this.quorumAckedLSN = this.get(RntbdResponseHeader.QuorumAckedLSN);
        this.quorumAckedLocalLSN = this.get(RntbdResponseHeader.QuorumAckedLocalLSN);
        this.readsPerformed = this.get(RntbdResponseHeader.ReadsPerformed);
        this.requestCharge = this.get(RntbdResponseHeader.RequestCharge);
        this.requestValidationFailure = this.get(RntbdResponseHeader.RequestValidationFailure);
        this.restoreState = this.get(RntbdResponseHeader.RestoreState);
        this.retryAfterMilliseconds = this.get(RntbdResponseHeader.RetryAfterMilliseconds);
        this.schemaVersion = this.get(RntbdResponseHeader.SchemaVersion);
        this.scriptsExecuted = this.get(RntbdResponseHeader.ScriptsExecuted);
        this.serverDateTimeUtc = this.get(RntbdResponseHeader.ServerDateTimeUtc);
        this.sessionToken = this.get(RntbdResponseHeader.SessionToken);
        this.shareThroughput = this.get(RntbdResponseHeader.ShareThroughput);
        this.storageMaxResoureQuota = this.get(RntbdResponseHeader.StorageMaxResoureQuota);
        this.storageResourceQuotaUsage = this.get(RntbdResponseHeader.StorageResourceQuotaUsage);
        this.subStatus = this.get(RntbdResponseHeader.SubStatus);
        this.transportRequestID = this.get(RntbdResponseHeader.TransportRequestID);
        this.writesPerformed = this.get(RntbdResponseHeader.WritesPerformed);
        this.xpRole = this.get(RntbdResponseHeader.XPRole);
        this.backendRequestDurationMilliseconds = this.get(RntbdResponseHeader.BackendRequestDurationMilliseconds);
    }

    boolean isPayloadPresent() {
        return this.payloadPresent.isPresent() && this.payloadPresent.getValue(Byte.class) != 0x00;
    }

    List<Map.Entry<String, String>> asList(final RntbdContext context, final UUID activityId) {

        final ImmutableList.Builder<Map.Entry<String, String>> builder = ImmutableList.builderWithExpectedSize(this.computeCount() + 2);
        builder.add(new Entry(HttpHeaders.SERVER_VERSION, context.serverVersion()));
        builder.add(new Entry(HttpHeaders.ACTIVITY_ID, activityId.toString()));

        this.collectEntries((token, toEntry) -> {
            if (token.isPresent()) {
                builder.add(toEntry.apply(token));
            }
        });

        return builder.build();
    }

    public Map<String, String> asMap(final RntbdContext context, final UUID activityId) {

        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builderWithExpectedSize(this.computeCount() + 2);
        builder.put(new Entry(HttpHeaders.SERVER_VERSION, context.serverVersion()));
        builder.put(new Entry(HttpHeaders.ACTIVITY_ID, activityId.toString()));

        this.collectEntries((token, toEntry) -> {
            if (token.isPresent()) {
                builder.put(toEntry.apply(token));
            }
        });

        return builder.build();
    }

    static RntbdResponseHeaders decode(final ByteBuf in) {
        final RntbdResponseHeaders headers = new RntbdResponseHeaders(in);
        RntbdTokenStream.decode(headers);
        return headers;
    }

    public static RntbdResponseHeaders fromMap(final Map<String, String> map, final boolean payloadPresent) {

        final RntbdResponseHeaders headers = new RntbdResponseHeaders(Unpooled.EMPTY_BUFFER);
        headers.payloadPresent.setValue(payloadPresent);
        headers.setValues(map);

        return headers;
    }

    public void setValues(final Map<String, String> headers) {

        this.mapValue(this.LSN, BackendHeaders.LSN, Long::parseLong, headers);
        this.mapValue(this.collectionLazyIndexProgress, HttpHeaders.COLLECTION_LAZY_INDEXING_PROGRESS, Integer::parseInt, headers);
        this.mapValue(this.collectionLazyIndexProgress, BackendHeaders.COLLECTION_PARTITION_INDEX, Integer::parseInt, headers);
        this.mapValue(this.collectionSecurityIdentifier, BackendHeaders.COLLECTION_SECURITY_IDENTIFIER, String::toString, headers);
        this.mapValue(this.collectionServiceIndex, BackendHeaders.COLLECTION_SERVICE_INDEX, Integer::parseInt, headers);
        this.mapValue(this.collectionUpdateProgress, HttpHeaders.COLLECTION_INDEX_TRANSFORMATION_PROGRESS, Integer::parseInt, headers);
        this.mapValue(this.continuationToken, HttpHeaders.CONTINUATION, String::toString, headers);
        this.mapValue(this.currentReplicaSetSize, BackendHeaders.CURRENT_REPLICA_SET_SIZE, Integer::parseInt, headers);
        this.mapValue(this.currentWriteQuorum, BackendHeaders.CURRENT_WRITE_QUORUM, Integer::parseInt, headers);
        this.mapValue(this.databaseAccountId, BackendHeaders.DATABASE_ACCOUNT_ID, String::toString, headers);
        this.mapValue(this.disableRntbdChannel, HttpHeaders.DISABLE_RNTBD_CHANNEL, Boolean::parseBoolean, headers);
        this.mapValue(this.eTag, HttpHeaders.E_TAG, String::toString, headers);
        this.mapValue(this.globalCommittedLSN, BackendHeaders.GLOBAL_COMMITTED_LSN, Long::parseLong, headers);
        this.mapValue(this.hasTentativeWrites, BackendHeaders.HAS_TENTATIVE_WRITES, Boolean::parseBoolean, headers);
        this.mapValue(this.indexingDirective, HttpHeaders.INDEXING_DIRECTIVE, RntbdIndexingDirective::valueOf, headers);
        this.mapValue(this.isRUPerMinuteUsed, BackendHeaders.IS_RU_PER_MINUTE_USED, Byte::parseByte, headers);
        this.mapValue(this.itemCount, HttpHeaders.ITEM_COUNT, Integer::parseInt, headers);
        this.mapValue(this.itemLSN, BackendHeaders.ITEM_LSN, Long::parseLong, headers);
        this.mapValue(this.itemLocalLSN, BackendHeaders.ITEM_LOCAL_LSN, Long::parseLong, headers);
        this.mapValue(this.lastStateChangeDateTime, HttpHeaders.LAST_STATE_CHANGE_UTC, String::toString, headers);
        this.mapValue(this.lastStateChangeDateTime, HttpHeaders.LAST_STATE_CHANGE_UTC, String::toString, headers);
        this.mapValue(this.localLSN, BackendHeaders.LOCAL_LSN, Long::parseLong, headers);
        this.mapValue(this.logResults, HttpHeaders.LOG_RESULTS, String::toString, headers);
        this.mapValue(this.numberOfReadRegions, BackendHeaders.NUMBER_OF_READ_REGIONS, Integer::parseInt, headers);
        this.mapValue(this.offerReplacePending, BackendHeaders.OFFER_REPLACE_PENDING, Boolean::parseBoolean, headers);
        this.mapValue(this.ownerFullName, HttpHeaders.OWNER_FULL_NAME, String::toString, headers);
        this.mapValue(this.ownerId, HttpHeaders.OWNER_ID, String::toString, headers);
        this.mapValue(this.partitionKeyRangeId, BackendHeaders.PARTITION_KEY_RANGE_ID, String::toString, headers);
        this.mapValue(this.queryMetrics, BackendHeaders.QUERY_METRICS, String::toString, headers);
        this.mapValue(this.indexUtilization, BackendHeaders.INDEX_UTILIZATION, String::toString, headers);
        this.mapValue(this.quorumAckedLSN, BackendHeaders.QUORUM_ACKED_LSN, Long::parseLong, headers);
        this.mapValue(this.quorumAckedLocalLSN, BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long::parseLong, headers);
        this.mapValue(this.requestCharge, HttpHeaders.REQUEST_CHARGE, Double::parseDouble, headers);
        this.mapValue(this.requestValidationFailure, BackendHeaders.REQUEST_VALIDATION_FAILURE, Byte::parseByte, headers);
        this.mapValue(this.restoreState, BackendHeaders.RESTORE_STATE, String::toString, headers);
        this.mapValue(this.retryAfterMilliseconds, HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, Integer::parseInt, headers);
        this.mapValue(this.schemaVersion, HttpHeaders.SCHEMA_VERSION, String::toString, headers);
        this.mapValue(this.serverDateTimeUtc, HttpHeaders.X_DATE, String::toString, headers);
        this.mapValue(this.sessionToken, HttpHeaders.SESSION_TOKEN, String::toString, headers);
        this.mapValue(this.shareThroughput, BackendHeaders.SHARE_THROUGHPUT, Boolean::parseBoolean, headers);
        this.mapValue(this.storageMaxResoureQuota, HttpHeaders.MAX_RESOURCE_QUOTA, String::toString, headers);
        this.mapValue(this.storageResourceQuotaUsage, HttpHeaders.CURRENT_RESOURCE_QUOTA_USAGE, String::toString, headers);
        this.mapValue(this.subStatus, BackendHeaders.SUB_STATUS, Integer::parseInt, headers);
        this.mapValue(this.transportRequestID, HttpHeaders.TRANSPORT_REQUEST_ID, Integer::parseInt, headers);
        this.mapValue(this.xpRole, BackendHeaders.XP_ROLE, Integer::parseInt, headers);
        this.mapValue(this.backendRequestDurationMilliseconds, BackendHeaders.BACKEND_REQUEST_DURATION_MILLISECONDS, Double::parseDouble, headers);
    }

    @Override
    public String toString() {
        final ObjectWriter writer = RntbdObjectMapper.writer();
        try {
            return writer.writeValueAsString(this);
        } catch (final JsonProcessingException error) {
            throw new CorruptedFrameException(error);
        }
    }

    private void collectEntries(final BiConsumer<RntbdToken, Function<RntbdToken, Map.Entry<String, String>>> collector) {

        collector.accept(this.LSN, token ->
            toLongEntry(BackendHeaders.LSN, token)
        );

        collector.accept(this.collectionLazyIndexProgress, token ->
            toIntegerEntry(HttpHeaders.COLLECTION_LAZY_INDEXING_PROGRESS, token)
        );

        collector.accept(this.collectionPartitionIndex, token ->
            toIntegerEntry(BackendHeaders.COLLECTION_PARTITION_INDEX, token)
        );

        collector.accept(this.collectionSecurityIdentifier, token ->
            toStringEntry(BackendHeaders.COLLECTION_SECURITY_IDENTIFIER, token)
        );

        collector.accept(this.collectionServiceIndex, token ->
            toIntegerEntry(BackendHeaders.COLLECTION_SERVICE_INDEX, token)
        );

        collector.accept(this.collectionUpdateProgress, token ->
            toIntegerEntry(HttpHeaders.COLLECTION_INDEX_TRANSFORMATION_PROGRESS, token)
        );

        collector.accept(this.continuationToken, token ->
            toStringEntry(HttpHeaders.CONTINUATION, token)
        );

        collector.accept(this.currentReplicaSetSize, token ->
            toIntegerEntry(BackendHeaders.CURRENT_REPLICA_SET_SIZE, token)
        );

        collector.accept(this.currentWriteQuorum, token ->
            toIntegerEntry(BackendHeaders.CURRENT_WRITE_QUORUM, token)
        );

        collector.accept(this.databaseAccountId, token ->
            toStringEntry(BackendHeaders.DATABASE_ACCOUNT_ID, token)
        );

        collector.accept(this.disableRntbdChannel, token ->
            toBooleanEntry(HttpHeaders.DISABLE_RNTBD_CHANNEL, token)
        );

        collector.accept(this.eTag, token ->
            toStringEntry(HttpHeaders.E_TAG, token)
        );

        collector.accept(this.globalCommittedLSN, token ->
            toLongEntry(BackendHeaders.GLOBAL_COMMITTED_LSN, token)
        );

        collector.accept(this.hasTentativeWrites, token ->
            toBooleanEntry(BackendHeaders.HAS_TENTATIVE_WRITES, token)
        );

        collector.accept(this.indexingDirective, token ->
            new Entry(HttpHeaders.INDEXING_DIRECTIVE, RntbdIndexingDirective.fromId(token.getValue(Byte.class)).name())
        );

        collector.accept(this.isRUPerMinuteUsed, token ->
            toByteEntry(BackendHeaders.IS_RU_PER_MINUTE_USED, token)
        );

        collector.accept(this.itemCount, token ->
            toIntegerEntry(HttpHeaders.ITEM_COUNT, token)
        );

        collector.accept(this.itemLSN, token ->
            toLongEntry(BackendHeaders.ITEM_LSN, token)
        );

        collector.accept(this.itemLocalLSN, token ->
            toLongEntry(BackendHeaders.ITEM_LOCAL_LSN, token)
        );

        collector.accept(this.lastStateChangeDateTime, token ->
            toStringEntry(HttpHeaders.LAST_STATE_CHANGE_UTC, token)
        );

        collector.accept(this.localLSN, token ->
            toLongEntry(BackendHeaders.LOCAL_LSN, token)
        );

        collector.accept(this.logResults, token ->
            toStringEntry(HttpHeaders.LOG_RESULTS, token)
        );

        collector.accept(this.numberOfReadRegions, token ->
            toIntegerEntry(BackendHeaders.NUMBER_OF_READ_REGIONS, token)
        );

        collector.accept(this.offerReplacePending, token ->
            toBooleanEntry(BackendHeaders.OFFER_REPLACE_PENDING, token)
        );

        collector.accept(this.ownerFullName, token ->
            toStringEntry(HttpHeaders.OWNER_FULL_NAME, token)
        );

        collector.accept(this.ownerId, token ->
            toStringEntry(HttpHeaders.OWNER_ID, token)
        );

        collector.accept(this.partitionKeyRangeId, token ->
            toStringEntry(BackendHeaders.PARTITION_KEY_RANGE_ID, token)
        );

        collector.accept(this.queryMetrics, token ->
            toStringEntry(BackendHeaders.QUERY_METRICS, token)
        );

        collector.accept(this.indexUtilization, token ->
            toStringEntry(BackendHeaders.INDEX_UTILIZATION, token)
        );

        collector.accept(this.quorumAckedLSN, token ->
            toLongEntry(BackendHeaders.QUORUM_ACKED_LSN, token)
        );

        collector.accept(this.quorumAckedLocalLSN, token ->
            toLongEntry(BackendHeaders.QUORUM_ACKED_LOCAL_LSN, token)
        );

        collector.accept(this.requestCharge, token ->
            toCurrencyEntry(HttpHeaders.REQUEST_CHARGE, token)
        );

        collector.accept(this.requestValidationFailure, token ->
            toByteEntry(BackendHeaders.REQUEST_VALIDATION_FAILURE, token)
        );

        collector.accept(this.restoreState, token ->
            toStringEntry(BackendHeaders.RESTORE_STATE, token)
        );

        collector.accept(this.retryAfterMilliseconds, token ->
            toIntegerEntry(HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, token)
        );

        collector.accept(this.schemaVersion, token ->
            toStringEntry(HttpHeaders.SCHEMA_VERSION, token)
        );

        collector.accept(this.serverDateTimeUtc, token ->
            toStringEntry(HttpHeaders.X_DATE, token)
        );

        collector.accept(this.sessionToken, token ->
            this.toSessionTokenEntry(HttpHeaders.SESSION_TOKEN, token)
        );

        collector.accept(this.shareThroughput, token ->
            toBooleanEntry(BackendHeaders.SHARE_THROUGHPUT, token)
        );

        collector.accept(this.storageMaxResoureQuota, token ->
            toStringEntry(HttpHeaders.MAX_RESOURCE_QUOTA, token)
        );

        collector.accept(this.storageResourceQuotaUsage, token ->
            toStringEntry(HttpHeaders.CURRENT_RESOURCE_QUOTA_USAGE, token)
        );

        collector.accept(this.subStatus, token ->
            toIntegerEntry(BackendHeaders.SUB_STATUS, token)
        );

        collector.accept(this.transportRequestID, token ->
            toIntegerEntry(HttpHeaders.TRANSPORT_REQUEST_ID, token)
        );

        collector.accept(this.xpRole, token ->
            toIntegerEntry(BackendHeaders.XP_ROLE, token)
        );

        collector.accept(this.backendRequestDurationMilliseconds, token ->
            toDoubleEntry(BackendHeaders.BACKEND_REQUEST_DURATION_MILLISECONDS, token)
        );
    }

    private void mapValue(final RntbdToken token, final String name, final Function<String, Object> parse, final Map<String, String> headers) {

        final String value = headers.get(name);

        if (value != null) {
            token.setValue(parse.apply(value));
        }
    }

    private static Map.Entry<String, String> toBooleanEntry(final String name, final RntbdToken token) {
        return new Entry(name, String.valueOf(token.getValue(Byte.class) != 0));
    }

    private static Map.Entry<String, String> toByteEntry(final String name, final RntbdToken token) {
        return new Entry(name, Byte.toString(token.getValue(Byte.class)));
    }

    private static Map.Entry<String, String> toCurrencyEntry(final String name, final RntbdToken token) {
        final BigDecimal value = new BigDecimal(Math.round(token.getValue(Double.class) * 100D)).scaleByPowerOfTen(-2);
        return new Entry(name, value.toString());
    }

    private static Map.Entry<String, String> toIntegerEntry(final String name, final RntbdToken token) {
        return new Entry(name, Long.toString(token.getValue(Long.class)));
    }

    private static Map.Entry<String, String> toDoubleEntry(final String name, final RntbdToken token) {
        return new Entry(name, Double.toString(token.getValue(Double.class)));
    }

    private static Map.Entry<String, String> toLongEntry(final String name, final RntbdToken token) {
        return new Entry(name, Long.toString(token.getValue(Long.class)));
    }

    private Map.Entry<String, String> toSessionTokenEntry(final String name, final RntbdToken token) {
        return new Entry(name, this.partitionKeyRangeId.getValue(String.class) + ":" + this.sessionToken.getValue(String.class));
    }

    private static Map.Entry<String, String> toStringEntry(final String name, final RntbdToken token) {
        return new Entry(name, token.getValue(String.class));
    }

    private static final class Entry extends AbstractMap.SimpleImmutableEntry<String, String> implements Serializable {
        private static final long serialVersionUID = -5926883743469858929L;

        Entry(final String name, final String value) {
            super(name, value);
        }
    }
}
