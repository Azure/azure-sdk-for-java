// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Document service lease.
 */
@JsonSerialize(using = ServiceItemLeaseV1.ServiceItemLeaseV1JsonSerializer.class)
public class ServiceItemLeaseV1 implements Lease {

    private static final Logger logger = LoggerFactory.getLogger(ServiceItemLeaseV1.class);

    private String id;
    private String _etag;
    private String leaseToken;
    private String owner;
    private String continuationToken;
    private LeaseVersion version;
    private FeedRangeInternal feedRangeInternal;
    private Map<String, String> properties;
    private String timestamp;  // ExplicitTimestamp
    private String _ts;

    public ServiceItemLeaseV1() {
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
        this.timestamp = currentTime.toString();
        this._ts = String.valueOf(currentTime.getSecond());
        this.properties = new HashMap<>();
        //  By default, this is EPK_RANGE_BASED_LEASE version
        //  However, keeping the design open for more lease versions in the future.
        this.version = LeaseVersion.EPK_RANGE_BASED_LEASE;
    }

    public ServiceItemLeaseV1(ServiceItemLeaseV1 other) {
        this.id = other.id;
        this._etag = other._etag;
        this.leaseToken = other.leaseToken;
        this.owner = other.owner;
        this.continuationToken = other.continuationToken;
        this.properties = other.properties;
        this.timestamp = other.timestamp;
        this._ts = other._ts;
        this.version = other.version;
        this.feedRangeInternal = other.feedRangeInternal;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public ServiceItemLeaseV1 withId(String id) {
        this.id = id;
        return this;
    }

    public String getETag() {
        return this._etag;
    }

    public ServiceItemLeaseV1 withETag(String etag) {
        this._etag = etag;
        return this;
    }

    public String getLeaseToken() {
        return this.leaseToken;
    }

    @Override
    public FeedRangeInternal getFeedRange() {
        return this.feedRangeInternal;
    }

    public ServiceItemLeaseV1 withLeaseToken(String leaseToken) {
        this.leaseToken = leaseToken;
        return this;
    }

    public ServiceItemLeaseV1 withFeedRange(FeedRangeInternal feedRangeInternal) {
        this.feedRangeInternal = feedRangeInternal;
        return this;
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    @Override
    public LeaseVersion getVersion() {
        return version;
    }

    public ServiceItemLeaseV1 withVersion(LeaseVersion leaseVersion) {
        this.version = leaseVersion;
        return this;
    }

    public ServiceItemLeaseV1 withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public String getContinuationToken() {
        return this.continuationToken;
    }

    @Override
    public ChangeFeedState getContinuationState(String containerRid, FeedRangeInternal feedRange) {
        checkNotNull(containerRid, "Argument 'containerRid' must not be null.");
        checkNotNull(feedRange, "Argument 'feedRange' must not be null.");

        //  TODO: (kuthapar) - we don't need to worry about the feedRange anymore. Remove it
        return new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.FULL_FIDELITY,
            ChangeFeedStartFromInternal.createFromETagAndFeedRange(this.continuationToken, feedRange),
            null);
    }

    @Override
    public void setContinuationToken(String continuationToken) {
        this.withContinuationToken(continuationToken);
    }

    public ServiceItemLeaseV1 withContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public void setOwner(String owner) {
        this.withOwner(owner);
    }

    @Override
    public void setTimestamp(Instant timestamp) {
        this.withTimestamp(timestamp);
    }

    public void setTimestamp(Date date) {
        this.withTimestamp(date.toInstant());
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void setId(String id) {
        this.withId(id);
    }

    @Override
    public void setConcurrencyToken(String concurrencyToken) {
        this.withETag(concurrencyToken);
    }

    public ServiceItemLeaseV1 withConcurrencyToken(String concurrencyToken) {
        return this.withETag(concurrencyToken);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.withProperties(properties);
    }

    public ServiceItemLeaseV1 withProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public String getTs() {
        return this._ts;
    }

    public ServiceItemLeaseV1 withTs(String ts) {
        this._ts = ts;
        return this;
    }

    @Override
    public String getTimestamp() {
        if (this.timestamp == null) {
            return UNIX_START_TIME.plusSeconds(Long.parseLong(this.getTs())).toString();
        }
        return this.timestamp;
    }

    public ServiceItemLeaseV1 withTimestamp(Instant timestamp) {
        this.timestamp = timestamp.toString();
        return this;
    }

    public String getExplicitTimestamp() {
        return this.timestamp;
    }

    @Override
    public String getConcurrencyToken() {
        return this.getETag();
    }

    @Override
    public void setVersion(LeaseVersion leaseVersion) {
        this.version = leaseVersion;
    }

    @Override
    public void setFeedRange(FeedRangeInternal feedRangeInternal) {
        this.feedRangeInternal = feedRangeInternal;
    }

    public static ServiceItemLeaseV1 fromDocument(InternalObjectNode document) {
        ServiceItemLeaseV1 lease = new ServiceItemLeaseV1()
            .withId(document.getId())
            .withETag(document.getETag())
            .withTs(ModelBridgeInternal.getStringFromJsonSerializable(document, Constants.Properties.LAST_MODIFIED))
            .withOwner(ModelBridgeInternal.getStringFromJsonSerializable(document,PROPERTY_NAME_OWNER))
            .withLeaseToken(ModelBridgeInternal.getStringFromJsonSerializable(document,PROPERTY_NAME_LEASE_TOKEN))
            .withContinuationToken(ModelBridgeInternal.getStringFromJsonSerializable(document,PROPERTY_NAME_CONTINUATION_TOKEN))
            .withVersion(LeaseVersion.fromVersionId(ModelBridgeInternal.getIntFromJsonSerializable(document, PROPERTY_NAME_VERSION)));

        JsonNode feedRangeNode = (JsonNode) document.get(PROPERTY_NAME_FEED_RANGE);
        if (feedRangeNode != null) {
            try {
                lease.withFeedRange(
                    Utils.getSimpleObjectMapper().convertValue(feedRangeNode, FeedRangeInternal.class));
            } catch (Exception e) {
                logger.warn("Failed to parse feed range ", e);
            }
        }

        String leaseTimestamp = ModelBridgeInternal.getStringFromJsonSerializable(document,PROPERTY_NAME_TIMESTAMP);
        if (leaseTimestamp != null) {
            return lease.withTimestamp(ZonedDateTime.parse(leaseTimestamp).toInstant());
        } else {
            return lease;
        }
    }

    public void setServiceItemLease(Lease lease) {
        this.setId(lease.getId());
        this.setConcurrencyToken(lease.getConcurrencyToken());
        this.setOwner(lease.getOwner());
        this.withLeaseToken(lease.getLeaseToken());
        this.setContinuationToken(getContinuationToken());
        this.setVersion(lease.getVersion());
        this.setFeedRange(lease.getFeedRange());

        String leaseTimestamp = lease.getTimestamp();
        if (leaseTimestamp != null) {
            this.setTimestamp(ZonedDateTime.parse(leaseTimestamp).toInstant());
        } else {
            this.setTimestamp(lease.getTimestamp());
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s Owner='%s' Continuation=%s Version=%s FeedRange=%s Timestamp(local)=%s Timestamp(server)=%s",
            this.getId(),
            this.getOwner(),
            this.getContinuationToken(),
            this.getVersion(),
            this.getFeedRange(),
            this.getTimestamp(),
            UNIX_START_TIME.plusSeconds(Long.parseLong(this.getTs())));
    }

    @SuppressWarnings("serial")
    static final class ServiceItemLeaseV1JsonSerializer extends StdSerializer<ServiceItemLeaseV1> {
        // this value should be incremented if changes are made to the ServiceItemLease class members
        private static final long serialVersionUID = 1L;

        protected ServiceItemLeaseV1JsonSerializer() { this(null); }

        protected ServiceItemLeaseV1JsonSerializer(Class<ServiceItemLeaseV1> t) {
            super(t);
        }

        @Override
        public void serialize(ServiceItemLeaseV1 lease, JsonGenerator writer, SerializerProvider serializerProvider) {
            try {
                writer.writeStartObject();
                writer.writeStringField(Constants.Properties.ID, lease.getId());
                writer.writeStringField(Constants.Properties.E_TAG, lease.getETag());
                writer.writeStringField(PROPERTY_NAME_LEASE_TOKEN, lease.getLeaseToken());
                writer.writeStringField(PROPERTY_NAME_CONTINUATION_TOKEN, lease.getContinuationToken());
                writer.writeStringField(PROPERTY_NAME_TIMESTAMP, lease.getTimestamp());
                writer.writeStringField(PROPERTY_NAME_OWNER, lease.getOwner());
                writer.writeNumberField(PROPERTY_NAME_VERSION, lease.getVersion().getVersionId());
                writer.writeObjectField(PROPERTY_NAME_FEED_RANGE, lease.getFeedRange());
                writer.writeEndObject();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
