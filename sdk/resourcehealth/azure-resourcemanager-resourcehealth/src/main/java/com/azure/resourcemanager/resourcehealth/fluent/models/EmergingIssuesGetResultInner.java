// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.resourcehealth.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.ProxyResource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resourcehealth.models.StatusActiveEvent;
import com.azure.resourcemanager.resourcehealth.models.StatusBanner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

/** The Get EmergingIssues operation response. */
@JsonFlatten
@Fluent
public class EmergingIssuesGetResultInner extends ProxyResource {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(EmergingIssuesGetResultInner.class);

    /*
     * Timestamp for when last time refreshed for ongoing emerging issue.
     */
    @JsonProperty(value = "properties.refreshTimestamp")
    private OffsetDateTime refreshTimestamp;

    /*
     * The list of emerging issues of banner type.
     */
    @JsonProperty(value = "properties.statusBanners")
    private List<StatusBanner> statusBanners;

    /*
     * The list of emerging issues of active event type.
     */
    @JsonProperty(value = "properties.statusActiveEvents")
    private List<StatusActiveEvent> statusActiveEvents;

    /**
     * Get the refreshTimestamp property: Timestamp for when last time refreshed for ongoing emerging issue.
     *
     * @return the refreshTimestamp value.
     */
    public OffsetDateTime refreshTimestamp() {
        return this.refreshTimestamp;
    }

    /**
     * Set the refreshTimestamp property: Timestamp for when last time refreshed for ongoing emerging issue.
     *
     * @param refreshTimestamp the refreshTimestamp value to set.
     * @return the EmergingIssuesGetResultInner object itself.
     */
    public EmergingIssuesGetResultInner withRefreshTimestamp(OffsetDateTime refreshTimestamp) {
        this.refreshTimestamp = refreshTimestamp;
        return this;
    }

    /**
     * Get the statusBanners property: The list of emerging issues of banner type.
     *
     * @return the statusBanners value.
     */
    public List<StatusBanner> statusBanners() {
        return this.statusBanners;
    }

    /**
     * Set the statusBanners property: The list of emerging issues of banner type.
     *
     * @param statusBanners the statusBanners value to set.
     * @return the EmergingIssuesGetResultInner object itself.
     */
    public EmergingIssuesGetResultInner withStatusBanners(List<StatusBanner> statusBanners) {
        this.statusBanners = statusBanners;
        return this;
    }

    /**
     * Get the statusActiveEvents property: The list of emerging issues of active event type.
     *
     * @return the statusActiveEvents value.
     */
    public List<StatusActiveEvent> statusActiveEvents() {
        return this.statusActiveEvents;
    }

    /**
     * Set the statusActiveEvents property: The list of emerging issues of active event type.
     *
     * @param statusActiveEvents the statusActiveEvents value to set.
     * @return the EmergingIssuesGetResultInner object itself.
     */
    public EmergingIssuesGetResultInner withStatusActiveEvents(List<StatusActiveEvent> statusActiveEvents) {
        this.statusActiveEvents = statusActiveEvents;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (statusBanners() != null) {
            statusBanners().forEach(e -> e.validate());
        }
        if (statusActiveEvents() != null) {
            statusActiveEvents().forEach(e -> e.validate());
        }
    }
}
