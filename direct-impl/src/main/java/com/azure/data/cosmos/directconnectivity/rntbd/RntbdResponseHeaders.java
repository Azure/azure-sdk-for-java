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
 *
 */

package com.azure.data.cosmos.directconnectivity.rntbd;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.azure.data.cosmos.internal.HttpConstants.HttpHeaders;
import static com.azure.data.cosmos.directconnectivity.WFConstants.BackendHeaders;
import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdIndexingDirective;
import static com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;

@JsonFilter("RntbdToken")
class RntbdResponseHeaders extends RntbdTokenStream<RntbdResponseHeader> {

    // region Fields

    @JsonProperty
    final private RntbdToken LSN;
    @JsonProperty
    final private RntbdToken collectionLazyIndexProgress;
    @JsonProperty
    final private RntbdToken collectionPartitionIndex;
    @JsonProperty
    final private RntbdToken collectionSecurityIdentifier;
    @JsonProperty
    final private RntbdToken collectionServiceIndex;
    @JsonProperty
    final private RntbdToken collectionUpdateProgress;
    @JsonProperty
    final private RntbdToken continuationToken;
    @JsonProperty
    final private RntbdToken currentReplicaSetSize;
    @JsonProperty
    final private RntbdToken currentWriteQuorum;
    @JsonProperty
    final private RntbdToken databaseAccountId;
    @JsonProperty
    final private RntbdToken disableRntbdChannel;
    @JsonProperty
    final private RntbdToken eTag;
    @JsonProperty
    final private RntbdToken globalCommittedLSN;
    @JsonProperty
    final private RntbdToken hasTentativeWrites;
    @JsonProperty
    final private RntbdToken indexTermsGenerated;
    @JsonProperty
    final private RntbdToken indexingDirective;
    @JsonProperty
    final private RntbdToken isRUPerMinuteUsed;
    @JsonProperty
    final private RntbdToken itemCount;
    @JsonProperty
    final private RntbdToken itemLSN;
    @JsonProperty
    final private RntbdToken itemLocalLSN;
    @JsonProperty
    final private RntbdToken lastStateChangeDateTime;
    @JsonProperty
    final private RntbdToken localLSN;
    @JsonProperty
    final private RntbdToken logResults;
    @JsonProperty
    final private RntbdToken numberOfReadRegions;
    @JsonProperty
    final private RntbdToken offerReplacePending;
    @JsonProperty
    final private RntbdToken ownerFullName;
    @JsonProperty
    final private RntbdToken ownerId;
    @JsonProperty
    final private RntbdToken partitionKeyRangeId;
    @JsonProperty
    final private RntbdToken payloadPresent;
    @JsonProperty
    final private RntbdToken queriesPerformed;
    @JsonProperty
    final private RntbdToken queryMetrics;
    @JsonProperty
    final private RntbdToken quorumAckedLSN;
    @JsonProperty
    final private RntbdToken quorumAckedLocalLSN;
    @JsonProperty
    final private RntbdToken readsPerformed;
    @JsonProperty
    final private RntbdToken requestCharge;
    @JsonProperty
    final private RntbdToken requestValidationFailure;
    @JsonProperty
    final private RntbdToken restoreState;
    @JsonProperty
    final private RntbdToken retryAfterMilliseconds;
    @JsonProperty
    final private RntbdToken schemaVersion;
    @JsonProperty
    final private RntbdToken scriptsExecuted;
    @JsonProperty
    final private RntbdToken serverDateTimeUtc;
    @JsonProperty
    final private RntbdToken sessionToken;
    @JsonProperty
    final private RntbdToken shareThroughput;
    @JsonProperty
    final private RntbdToken storageMaxResoureQuota;
    @JsonProperty
    final private RntbdToken storageResourceQuotaUsage;
    @JsonProperty
    final private RntbdToken subStatus;
    @JsonProperty
    final private RntbdToken transportRequestID;
    @JsonProperty
    final private RntbdToken writesPerformed;
    @JsonProperty
    final private RntbdToken xpRole;

    // endregion

    private RntbdResponseHeaders() {

        super(RntbdResponseHeader.set, RntbdResponseHeader.map);

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
    }

    boolean isPayloadPresent() {
        return this.payloadPresent.isPresent() && this.payloadPresent.getValue(Byte.class) != 0x00;
    }

    List<Map.Entry<String, String>> asList(RntbdContext context, UUID activityId) {

        ImmutableList.Builder<Map.Entry<String, String>> builder = ImmutableList.builderWithExpectedSize(this.computeCount() + 2);
        builder.add(new Entry(HttpHeaders.SERVER_VERSION, context.getServerVersion()));
        builder.add(new Entry(HttpHeaders.ACTIVITY_ID, activityId.toString()));

        collectEntries((token, toEntry) -> {
            if (token.isPresent()) {
                builder.add(toEntry.apply(token));
            }
        });

        return builder.build();
    }

    public Map<String, String> asMap(RntbdContext context, UUID activityId) {

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builderWithExpectedSize(this.computeCount() + 2);
        builder.put(new Entry(HttpHeaders.SERVER_VERSION, context.getServerVersion()));
        builder.put(new Entry(HttpHeaders.ACTIVITY_ID, activityId.toString()));

        collectEntries((token, toEntry) -> {
            if (token.isPresent()) {
                builder.put(toEntry.apply(token));
            }
        });

        return builder.build();
    }

    static RntbdResponseHeaders decode(ByteBuf in) {
        RntbdResponseHeaders headers = new RntbdResponseHeaders();
        RntbdTokenStream.decode(in, headers);
        return headers;
    }

    public static RntbdResponseHeaders fromMap(Map<String, String> map, boolean payloadPresent) {

        RntbdResponseHeaders headers = new RntbdResponseHeaders();
        headers.payloadPresent.setValue(payloadPresent);
        headers.setValues(map);

        return headers;
    }

    public void setValues(Map<String, String> headers) {

        mapValue(this.LSN, BackendHeaders.LSN, Long::parseLong, headers);
        mapValue(this.collectionLazyIndexProgress, HttpHeaders.COLLECTION_LAZY_INDEXING_PROGRESS, Integer::parseInt, headers);
        mapValue(this.collectionLazyIndexProgress, BackendHeaders.COLLECTION_PARTITION_INDEX, Integer::parseInt, headers);
        mapValue(this.collectionSecurityIdentifier, BackendHeaders.COLLECTION_SECURITY_IDENTIFIER, String::toString, headers);
        mapValue(this.collectionServiceIndex, BackendHeaders.COLLECTION_SERVICE_INDEX, Integer::parseInt, headers);
        mapValue(this.collectionUpdateProgress, HttpHeaders.COLLECTION_INDEX_TRANSFORMATION_PROGRESS, Integer::parseInt, headers);
        mapValue(this.continuationToken, HttpHeaders.CONTINUATION, String::toString, headers);
        mapValue(this.currentReplicaSetSize, BackendHeaders.CURRENT_REPLICA_SET_SIZE, Integer::parseInt, headers);
        mapValue(this.currentWriteQuorum, BackendHeaders.CURRENT_WRITE_QUORUM, Integer::parseInt, headers);
        mapValue(this.databaseAccountId, BackendHeaders.DATABASE_ACCOUNT_ID, String::toString, headers);
        mapValue(this.disableRntbdChannel, HttpHeaders.DISABLE_RNTBD_CHANNEL, Boolean::parseBoolean, headers);
        mapValue(this.eTag, HttpHeaders.E_TAG, String::toString, headers);
        mapValue(this.globalCommittedLSN, BackendHeaders.GLOBAL_COMMITTED_LSN, Long::parseLong, headers);
        mapValue(this.hasTentativeWrites, BackendHeaders.HAS_TENTATIVE_WRITES, Boolean::parseBoolean, headers);
        mapValue(this.indexingDirective, HttpHeaders.INDEXING_DIRECTIVE, RntbdIndexingDirective::valueOf, headers);
        mapValue(this.isRUPerMinuteUsed, BackendHeaders.IS_RU_PER_MINUTE_USED, Byte::parseByte, headers);
        mapValue(this.itemCount, HttpHeaders.ITEM_COUNT, Integer::parseInt, headers);
        mapValue(this.itemLSN, BackendHeaders.ITEM_LSN, Long::parseLong, headers);
        mapValue(this.itemLocalLSN, BackendHeaders.ITEM_LOCAL_LSN, Long::parseLong, headers);
        mapValue(this.lastStateChangeDateTime, HttpHeaders.LAST_STATE_CHANGE_UTC, String::toString, headers);
        mapValue(this.lastStateChangeDateTime, HttpHeaders.LAST_STATE_CHANGE_UTC, String::toString, headers);
        mapValue(this.localLSN, BackendHeaders.LOCAL_LSN, Long::parseLong, headers);
        mapValue(this.logResults, HttpHeaders.LOG_RESULTS, String::toString, headers);
        mapValue(this.numberOfReadRegions, BackendHeaders.NUMBER_OF_READ_REGIONS, Integer::parseInt, headers);
        mapValue(this.offerReplacePending, BackendHeaders.OFFER_REPLACE_PENDING, Boolean::parseBoolean, headers);
        mapValue(this.ownerFullName, HttpHeaders.OWNER_FULL_NAME, String::toString, headers);
        mapValue(this.ownerId, HttpHeaders.OWNER_ID, String::toString, headers);
        mapValue(this.partitionKeyRangeId, BackendHeaders.PARTITION_KEY_RANGE_ID, String::toString, headers);
        mapValue(this.queryMetrics, BackendHeaders.QUERY_METRICS, String::toString, headers);
        mapValue(this.quorumAckedLSN, BackendHeaders.QUORUM_ACKED_LSN, Long::parseLong, headers);
        mapValue(this.quorumAckedLocalLSN, BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long::parseLong, headers);
        mapValue(this.requestCharge, HttpHeaders.REQUEST_CHARGE, Double::parseDouble, headers);
        mapValue(this.requestValidationFailure, BackendHeaders.REQUEST_VALIDATION_FAILURE, Byte::parseByte, headers);
        mapValue(this.restoreState, BackendHeaders.RESTORE_STATE, String::toString, headers);
        mapValue(this.retryAfterMilliseconds, HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, Integer::parseInt, headers);
        mapValue(this.schemaVersion, HttpHeaders.SCHEMA_VERSION, String::toString, headers);
        mapValue(this.serverDateTimeUtc, HttpHeaders.X_DATE, String::toString, headers);
        mapValue(this.sessionToken, HttpHeaders.SESSION_TOKEN, String::toString, headers);
        mapValue(this.shareThroughput, BackendHeaders.SHARE_THROUGHPUT, Boolean::parseBoolean, headers);
        mapValue(this.storageMaxResoureQuota, HttpHeaders.MAX_RESOURCE_QUOTA, String::toString, headers);
        mapValue(this.storageResourceQuotaUsage, HttpHeaders.CURRENT_RESOURCE_QUOTA_USAGE, String::toString, headers);
        mapValue(this.subStatus, BackendHeaders.SUB_STATUS, Integer::parseInt, headers);
        mapValue(this.transportRequestID, HttpHeaders.TRANSPORT_REQUEST_ID, Integer::parseInt, headers);
        mapValue(this.xpRole, BackendHeaders.XP_ROLE, Integer::parseInt, headers);
    }

    @Override
    public String toString() {
        ObjectWriter writer = RntbdObjectMapper.writer();
        try {
            return writer.writeValueAsString(this);
        } catch (JsonProcessingException error) {
            throw new CorruptedFrameException(error);
        }
    }

    private void collectEntries(BiConsumer<RntbdToken, Function<RntbdToken, Map.Entry<String, String>>> collector) {

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
            toSessionTokenEntry(HttpHeaders.SESSION_TOKEN, token)
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
    }

    private void mapValue(RntbdToken token, String name, Function<String, Object> parse, Map<String, String> headers) {

        String value = headers.get(name);

        if (value != null) {
            token.setValue(parse.apply(value));
        }
    }

    private static Map.Entry<String, String> toBooleanEntry(String name, RntbdToken token) {
        return new Entry(name, String.valueOf(token.getValue(Byte.class) != 0));
    }

    private static Map.Entry<String, String> toByteEntry(String name, RntbdToken token) {
        return new Entry(name, Byte.toString(token.getValue(Byte.class)));
    }

    private static Map.Entry<String, String> toCurrencyEntry(String name, RntbdToken token) {
        BigDecimal value = new BigDecimal(Math.round(token.getValue(Double.class) * 100D)).scaleByPowerOfTen(-2);
        return new Entry(name, value.toString());
    }

    private static Map.Entry<String, String> toIntegerEntry(String name, RntbdToken token) {
        return new Entry(name, Long.toString(token.getValue(Long.class)));
    }

    private static Map.Entry<String, String> toLongEntry(String name, RntbdToken token) {
        return new Entry(name, Long.toString(token.getValue(Long.class)));
    }

    private Map.Entry<String, String> toSessionTokenEntry(String name, RntbdToken token) {
        return new Entry(name, this.partitionKeyRangeId.getValue(String.class) + ":" + this.sessionToken.getValue(String.class));
    }

    private static Map.Entry<String, String> toStringEntry(String name, RntbdToken token) {
        return new Entry(name, token.getValue(String.class));
    }

    final private static class Entry extends AbstractMap.SimpleImmutableEntry<String, String> {
        Entry(String name, String value) {
            super(name, value);
        }
    }
}
