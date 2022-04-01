// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseBuilder;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Document service lease base.
 */
@JsonSerialize(using = ServiceItemLease.ServiceItemLeaseJsonSerializer.class)
@JsonDeserialize(using = ServiceItemLease.ServiceItemLeaseJsonDeserializer.class)
public abstract class ServiceItemLease implements Lease {
    private final String id;
    private final String leaseToken;

    private String continuationToken;
    private String _etag;
    private FeedRangeInternal feedRangeInternal;
    private String owner;
    private Map<String, String> properties;
    private String timestamp;  // ExplicitTimestamp
    private long _ts;

    public ServiceItemLease(
            String id,
            String leaseToken,
            String owner,
            FeedRangeInternal feedRangeInternal,
            String continuationToken,
            String etag,
            Map<String, String> properties,
            String timestamp,
            long ts) {
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
    public String getLeaseToken() {
        return this.leaseToken;
    }

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
    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp.toString();
    }

    @Override
    public void setConcurrencyToken(String concurrencyToken) {

        this._etag = concurrencyToken;
    }

    public long getTs() {
        return this._ts;
    }

    public ServiceItemLease setTs(long ts) {
        this._ts = ts;
        return this;
    }

    @Override
    public String getTimestamp() {
        if (this.timestamp == null) {
            return LeaseConstants.UNIX_START_TIME.plusSeconds(this._ts).toString();
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
        return this._etag;
    }

    public void setServiceItemLease(Lease lease) {
        this.setConcurrencyToken(lease.getConcurrencyToken());
        this.setOwner(lease.getOwner());
        this.setContinuationToken(getContinuationToken());

        String leaseTimestamp = lease.getTimestamp();
        if (leaseTimestamp != null) {
            this.setTimestamp(ZonedDateTime.parse(leaseTimestamp).toInstant());
        } else {
            this.timestamp = lease.getTimestamp();
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s Owner='%s' Continuation=%s Timestamp(local)=%s Timestamp(server)=%s",
                this.id,
                this.owner,
                this.continuationToken,
                this.timestamp,
                LeaseConstants.UNIX_START_TIME.plusSeconds(this._ts));
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
                writer.writeStringField(Constants.Properties.ID, lease.id);
                writer.writeStringField(Constants.Properties.E_TAG, lease._etag);
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_LEASE_TOKEN, lease.leaseToken);
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_CONTINUATION_TOKEN, lease.continuationToken);
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_TIMESTAMP, lease.timestamp);
                writer.writeStringField(LeaseConstants.PROPERTY_NAME_OWNER, lease.owner);
                writer.writeNumberField(LeaseConstants.PROPERTY_VERSION, lease.getVersion().getVersionId());
                writer.writeObjectField(LeaseConstants.PROPERTY_FEED_RANGE, lease.feedRangeInternal);
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
            JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

            String id = rootNode.get(Constants.Properties.ID).asText();
            String etag = rootNode.get(Constants.Properties.E_TAG).asText();
            String leaseToken = rootNode.get(LeaseConstants.PROPERTY_NAME_LEASE_TOKEN).asText();
            String continuationToken = rootNode.get(LeaseConstants.PROPERTY_NAME_CONTINUATION_TOKEN).asText();
            String timeStamp = rootNode.get(LeaseConstants.PROPERTY_NAME_TIMESTAMP).asText();
            String owner = rootNode.get(LeaseConstants.PROPERTY_NAME_OWNER).asText();
            long ts = rootNode.get(LeaseConstants.PROPERTY_TS).asLong();

            JsonNode feedRangeNode = rootNode.get(LeaseConstants.PROPERTY_FEED_RANGE);
            FeedRangeInternal feedRange = jsonParser.getCodec().treeToValue(feedRangeNode, FeedRangeInternal.class);

            LeaseBuilder leaseBuilder = Lease.builder()
                    .id(id)
                    .etag(etag)
                    .owner(owner)
                    .leaseToken(leaseToken)
                    .continuationToken(continuationToken)
                    .timestamp(timeStamp)
                    .ts(ts)
                    .feedRange(feedRange);

            ServiceItemLeaseVersion version = ServiceItemLeaseVersion.valueOf(rootNode.get(LeaseConstants.PROPERTY_VERSION).intValue()).get();

            if (version == ServiceItemLeaseVersion.EPKRangeBasedLease) {
                return (ServiceItemLease) leaseBuilder.buildEpkBasedLease();
            }
            if (version == ServiceItemLeaseVersion.PartitionKeyRangeBasedLease) {
                return (ServiceItemLease) leaseBuilder.buildPartitionBasedLease();
            }

            throw JsonMappingException.from(
                    jsonParser,
                    "Unsupported lease type: " + version);
        }
    }
}
