// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.implementation.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Document service lease base.
 */
@JsonSerialize(using = ServiceItemLease.ServiceItemLeaseJsonSerializer.class)
@JsonDeserialize(using = ServiceItemLease.ServiceItemLeaseJsonDeserializer.class)
public abstract class ServiceItemLease implements Lease {
    private String id;
    private String _etag;
    private String leaseToken;
    private String owner;
    private String continuationToken;

    private Map<String, String> properties;
    private String timestamp;  // ExplicitTimestamp
    private String _ts;
    private FeedRangeInternal feedRangeInternal;

    public ServiceItemLease(
            String id,
            String leaseToken,
            String owner,
            FeedRangeInternal feedRangeInternal,
            String continuationToken,
            String etag,
            Map<String, String> properties,
            String timestamp,
            String ts) {
        this.id = id;
        this.leaseToken = leaseToken;
        this.owner = owner;
        this.feedRangeInternal = feedRangeInternal;
        this.continuationToken = continuationToken;
        this._etag = etag;
        this.properties = properties;
        this.timestamp = timestamp;
        this._ts = ts;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getETag() {
        return this._etag;
    }

    public void setEtag(String etag) { this._etag = etag; }

    @Override
    public String getLeaseToken() {
        return this.leaseToken;
    }

    public void setLeaseToken(String leaseToken) { this.leaseToken = leaseToken; }

    @Override
    public String getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getContinuationToken() {
        return this.continuationToken;
    }

    @Override
    public void setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
    }

    @Override
    public ChangeFeedState getContinuationState(
        String containerRid,
        FeedRangeInternal feedRange) {

        checkNotNull(containerRid, "Argument 'containerRid' must not be null.");
        checkNotNull(feedRange, "Argument 'feedRange' must not be null.");

        return new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            ChangeFeedStartFromInternal.createFromETagAndFeedRange(this.continuationToken, feedRange),
            null);
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public void setTimestamp(Instant timestamp) {

        this.setTimestamp(timestamp.toString());
    }

    public void setTimestamp(Date date) {
        this.setTimestamp(date.toInstant());
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void setConcurrencyToken(String concurrencyToken) {

        this._etag = concurrencyToken;
    }

    public String getTs() {
        return this._ts;
    }

    public ServiceItemLease setTs(String ts) {
        this._ts = ts;
        return this;
    }

    @Override
    public String getTimestamp() {
        if (this.timestamp == null) {
            return LeaseConstants.UNIX_START_TIME.plusSeconds(Long.parseLong(this.getTs())).toString();
        }
        return this.timestamp;
    }

    @Override
    public void setFeedRange(FeedRangeInternal feedRange) {
        this.feedRangeInternal = feedRange;
    }

    @Override
    public FeedRangeInternal getFeedRange() {
        return this.feedRangeInternal;
    }

    @Override
    public String getConcurrencyToken() {
        return this.getETag();
    }

    public void setServiceItemLease(Lease lease) {
        this.setId(lease.getId());
        this.setConcurrencyToken(lease.getConcurrencyToken());
        this.setOwner(lease.getOwner());
        this.setLeaseToken(lease.getLeaseToken());
        this.setContinuationToken(getContinuationToken());

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
            "%s Owner='%s' Continuation=%s Timestamp(local)=%s Timestamp(server)=%s",
            this.getId(),
            this.getOwner(),
            this.getContinuationToken(),
            this.getTimestamp(),
            LeaseConstants.UNIX_START_TIME.plusSeconds(Long.parseLong(this.getTs())));
    }

    @SuppressWarnings("serial")
    static final class ServiceItemLeaseJsonSerializer extends StdSerializer<ServiceItemLease> {
        // this value should be incremented if changes are made to the ServiceItemLease class members
        private static final long serialVersionUID = 1L;

        protected ServiceItemLeaseJsonSerializer() { this(null); }

        protected ServiceItemLeaseJsonSerializer(Class<ServiceItemLease> t) {
            super(t);
        }

        @Override
        public void serialize(ServiceItemLease lease, JsonGenerator writer, SerializerProvider serializerProvider) {
            try {
                writer.writeStartObject();
                writer.writeStringField(Constants.Properties.ID, lease.getId());
                writer.writeStringField(Constants.Properties.E_TAG, lease.getETag());
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_LEASE_TOKEN, lease.getLeaseToken());
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_CONTINUATION_TOKEN, lease.getContinuationToken());
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_TIMESTAMP, lease.getTimestamp());
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_OWNER, lease.getOwner());
                writer.writeNumberField(LeaseConstants.PROPERTY_VERSION, lease.getVersion().getValue());
                writer.writeObjectField(LeaseConstants.PROPERTY_FEED_RANGE, lease.getFeedRange());
                writer.writeEndObject();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static final class ServiceItemLeaseJsonDeserializer extends StdDeserializer<ServiceItemLease> {

        protected ServiceItemLeaseJsonDeserializer() {
            this(null);
        }

        protected ServiceItemLeaseJsonDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ServiceItemLease deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);

            ServiceItemLeaseVersion version = ServiceItemLeaseVersion.valueOf(node.get(LeaseConstants.PROPERTY_VERSION).numberValue().toString());

            if (version == ServiceItemLeaseVersion.EPKRangeBasedLease) {
                return jsonParser.getCodec().treeToValue(node, ServiceItemLeaseEpk.class);
            }

            return jsonParser.getCodec().treeToValue(node, ServiceItemLeaseCore.class);
        }
    }
}
