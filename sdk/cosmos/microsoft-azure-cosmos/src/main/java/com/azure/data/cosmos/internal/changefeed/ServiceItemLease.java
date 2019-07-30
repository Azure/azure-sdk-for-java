// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import com.azure.data.cosmos.internal.changefeed.internal.Constants;

/**
 * Document service lease.
 */
public class ServiceItemLease implements Lease {
    private static final ZonedDateTime UNIX_START_TIME = ZonedDateTime.parse("1970-01-01T00:00:00.0Z[UTC]");

    // TODO: add JSON annotations and rename the item.
    private String id;
    private String _etag;
    private String LeaseToken;
    private String Owner;
    private String ContinuationToken;

    private Map<String, String> properties;
    private String timestamp;  // ExplicitTimestamp
    private String _ts;

    public ServiceItemLease() {
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("UTC"));
        this.timestamp = currentTime.toString();
        this._ts = String.valueOf(currentTime.getSecond());
        this.properties = new HashMap<>();
    }

    public ServiceItemLease(ServiceItemLease other)
    {
        this.id = other.id;
        this._etag = other._etag;
        this.LeaseToken = other.LeaseToken;
        this.Owner = other.Owner;
        this.ContinuationToken = other.ContinuationToken;
        this.properties = other.properties;
        this.timestamp = other.timestamp;
        this._ts = other._ts;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public ServiceItemLease withId(String id) {
        this.id = id;
        return this;
    }

    @JsonIgnore
    public String getEtag() {
        return this._etag;
    }

    public ServiceItemLease withEtag(String etag) {
        this._etag = etag;
        return this;
    }

    @JsonProperty("LeaseToken")
    public String getLeaseToken() {
        return this.LeaseToken;
    }

    public ServiceItemLease withLeaseToken(String leaseToken) {
        this.LeaseToken = leaseToken;
        return this;
    }

    @JsonProperty("Owner")
    @Override
    public String getOwner() {
        return this.Owner;
    }

    public ServiceItemLease withOwner(String owner) {
        this.Owner = owner;
        return this;
    }

    @JsonProperty("ContinuationToken")
    @Override
    public String getContinuationToken() {
        return this.ContinuationToken;
    }

    @Override
    public void setContinuationToken(String continuationToken) {
        this.withContinuationToken(continuationToken);
    }

    public ServiceItemLease withContinuationToken(String continuationToken) {
        this.ContinuationToken = continuationToken;
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
    public void setTimestamp(ZonedDateTime timestamp) {
        this.withTimestamp(timestamp);
    }

    public void setTimestamp(Date date) {
        this.withTimestamp(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    public void setTimestamp(Date date, ZoneId zoneId) {
        this.withTimestamp(date.toInstant().atZone(zoneId));
    }

    @Override
    public void setId(String id) {
        this.withId(id);
    }

    @Override
    public void setConcurrencyToken(String concurrencyToken) {
        this.withEtag(concurrencyToken);
    }

    public ServiceItemLease withConcurrencyToken(String concurrencyToken) {
        return this.withEtag(concurrencyToken);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.withProperties(properties);
    }

    public ServiceItemLease withProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    @JsonIgnore
    public String getTs() {
        return this._ts;
    }

    public ServiceItemLease withTs(String ts) {
        this._ts = ts;
        return this;
    }

    @JsonProperty("timestamp")
    @Override
    public String getTimestamp() {
        if (this.timestamp == null) {
            return UNIX_START_TIME.plusSeconds(Long.parseLong(this.getTs())).toString();
        }
        return this.timestamp;
    }

    public ServiceItemLease withTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp.toString();
        return this;
    }

    @JsonIgnore
    public String getExplicitTimestamp() {
        return this.timestamp;
    }

    @JsonIgnore
    @Override
    public String getConcurrencyToken() {
        return this.getEtag();
    }

    public static ServiceItemLease fromDocument(Document document) {
        return new ServiceItemLease()
            .withId(document.id())
            .withEtag(document.etag())
            .withTs(document.getString(Constants.Properties.LAST_MODIFIED))
            .withOwner(document.getString("Owner"))
            .withLeaseToken(document.getString("LeaseToken"))
            .withContinuationToken(document.getString("ContinuationToken"));
    }

    public static ServiceItemLease fromDocument(CosmosItemProperties document) {
        return new ServiceItemLease()
            .withId(document.id())
            .withEtag(document.etag())
            .withTs(document.getString(Constants.Properties.LAST_MODIFIED))
            .withOwner(document.getString("Owner"))
            .withLeaseToken(document.getString("LeaseToken"))
            .withContinuationToken(document.getString("ContinuationToken"));
    }

    @Override
    public String toString() {
        return String.format(
            "%s Owner='%s' Continuation=%s Timestamp(local)=%s Timestamp(server)=%s",
            this.getId(),
            this.getOwner(),
            this.getContinuationToken(),
            this.getTimestamp(),
            UNIX_START_TIME.plusSeconds(Long.parseLong(this.getTs())));
    }
}
