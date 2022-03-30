// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.LeaseConstants;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseCore;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseEpk;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseVersion;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class LeaseBuilder {
    private static Logger logger = LoggerFactory.getLogger(LeaseBuilder.class);

    private String id;
    private String _etag;
    private String leaseToken;
    private String owner;
    private String continuationToken;

    private Map<String, String> properties;
    private String timestamp;
    private String _ts;
    private FeedRangeInternal feedRangeInternal;

    LeaseBuilder() {
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
        this.timestamp = currentTime.toString();
        this._ts = String.valueOf(currentTime.getSecond());
        this.properties = new HashMap<>();
    }

    public LeaseBuilder id (String id) {
        checkArgument(!StringUtils.isEmpty(id), "Argument 'id' can not be null nor empty");

        this.id = id;
        return this;
    }

    public LeaseBuilder etag(String etag) {
        this._etag = etag;
        return this;
    }

    public LeaseBuilder leaseToken(String leaseToken) {
        checkArgument(!StringUtils.isEmpty(leaseToken), "Argument 'leaseToken' can not be null nor empty");

        this.leaseToken = leaseToken;
        return this;
    }

    public LeaseBuilder owner(String owner) {
        this.owner = owner;
        return this;
    }

    public LeaseBuilder continuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }

    public LeaseBuilder properties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }
    public LeaseBuilder timestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public LeaseBuilder ts(String ts) {
        this._ts = ts;
        return this;
    }

    public LeaseBuilder feedRange(FeedRangeInternal feedRangeInternal) {
        this.feedRangeInternal = feedRangeInternal;
        return this;
    }

    public Lease buildPartitionBasedLease() {
        return new ServiceItemLeaseCore(
                this.id,
                this.leaseToken,
                this.owner,
                this.feedRangeInternal,
                this.continuationToken,
                this._etag,
                this.properties,
                this.timestamp,
                this._ts);
    }

    public Lease buildEpkBasedLease() {
        return new ServiceItemLeaseEpk(
                this.id,
                this.leaseToken,
                this.owner,
                this.feedRangeInternal,
                this.continuationToken,
                this._etag,
                this.properties,
                this.timestamp,
                this._ts);
    }

    public Lease buildFromDocument(InternalObjectNode document) {
        checkNotNull(document, "Argument 'document' can not be null");

        this
            .id(document.getId())
            .etag(document.getETag())
            .ts(ModelBridgeInternal.getStringFromJsonSerializable(document, Constants.Properties.LAST_MODIFIED))
            .owner(ModelBridgeInternal.getStringFromJsonSerializable(document, LeaseConstants.PROPERTY_NAME_OWNER))
            .leaseToken(ModelBridgeInternal.getStringFromJsonSerializable(document, LeaseConstants.PROPERTY_NAME_LEASE_TOKEN))
            .continuationToken(ModelBridgeInternal.getStringFromJsonSerializable(document, LeaseConstants.PROPERTY_NAME_CONTINUATION_TOKEN))
            .timestamp(ModelBridgeInternal.getStringFromJsonSerializable(document, LeaseConstants.PROPERTY_NAME_TIMESTAMP));

        JsonNode feedRangeNode = (JsonNode) document.get(LeaseConstants.PROPERTY_FEED_RANGE);
        if (feedRangeNode != null) {
            try {
                this.feedRange(
                    Utils.getSimpleObjectMapper().convertValue(feedRangeNode, FeedRangeInternal.class));
            } catch (Exception e) {
                logger.warn("Failed to parse feed range ", e);
            }
        }

        ServiceItemLeaseVersion version =
                ServiceItemLeaseVersion.valueOf(
                        ModelBridgeInternal.getIntFromJsonSerializable(document, LeaseConstants.PROPERTY_VERSION)).get();

        switch (version) {
            case PartitionKeyRangeBasedLease:
                return this.buildPartitionBasedLease();
            case EPKRangeBasedLease:
                return this.buildEpkBasedLease();
            default:
                throw new IllegalStateException("Lease version " + version + " is not supported");
        }
    }
}
