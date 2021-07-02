// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.Collections;
import java.util.List;

/**
 * The DataFeedOptions model.
 */
@Fluent
public final class DataFeedOptions {
    private String dataFeedDescription;
    private DataFeedRollupSettings dataFeedRollupSettings;
    private DataFeedMissingDataPointFillSettings dataFeedMissingDataPointFillSettings;
    private DataFeedAccessMode dataFeedAccessMode;
    private List<String> admins;
    private List<String> viewers;
    private String actionLinkTemplate;

    /**
     * Get the data feed roll up properties.
     *
     * @return the data feed roll up properties.
     */
    public DataFeedRollupSettings getRollupSettings() {
        return this.dataFeedRollupSettings;
    }

    /**
     * Get the data feed data point fill properties.
     *
     * @return the data point fill properties.
     */
    public DataFeedMissingDataPointFillSettings getMissingDataPointFillSettings() {
        return this.dataFeedMissingDataPointFillSettings;
    }

    /**
     * Set the data feed roll up properties.
     *
     * @param dataFeedRollupSettings the data roll up settings value to set.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setRollupSettings(final DataFeedRollupSettings dataFeedRollupSettings) {
        this.dataFeedRollupSettings = dataFeedRollupSettings;
        return this;
    }

    /**
     * Set the data feed, data point fill properties.
     *
     * @param dataFeedMissingDataPointFillSettings the data point fill settings value to set.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setMissingDataPointFillSettings(
        final DataFeedMissingDataPointFillSettings dataFeedMissingDataPointFillSettings) {
        this.dataFeedMissingDataPointFillSettings = dataFeedMissingDataPointFillSettings;
        return this;
    }

    /**
     * Get the data feed access mode, default is Private.
     *
     * @return the DataFeedAccessMode value.
     */
    public DataFeedAccessMode getAccessMode() {
        return this.dataFeedAccessMode;
    }

    /**
     * Set the data feed access mode, default is Private.
     *
     * @param dataFeedAccessMode the value to set.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setAccessMode(DataFeedAccessMode dataFeedAccessMode) {
        this.dataFeedAccessMode = dataFeedAccessMode;
        return this;
    }

    /**
     * Get the list of data feed administrator emails and clientIds.
     * <p>
     * The administrators have total control over the DataFeed, being allowed to update, delete, or pause the DataFeed.
     * Each element in this list represents a user with administrator access, but the value of each string element
     * is either user email address or clientId uniquely identifying the user service principal.
     *
     * @return a list containing email or clientId of admins
     */
    public List<String> getAdmins() {
        return this.admins == null
            ? null : Collections.unmodifiableList(this.admins);
    }

    /**
     * Set the list of data feed administrator emails and clientIds.
     * <p>
     * The administrators have total control over the DataFeed, being allowed to update, delete, or pause the DataFeed.
     * Each element in this list represents a user with administrator access, but the value of each string element
     * is either user email address or clientId uniquely identifying the user service principal.
     *
     * @param admins a list containing email or clientId of admins
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setAdmins(List<String> admins) {
        this.admins = admins;
        return this;
    }

    /**
     * Get the list of data feed viewer emails and clientIds.
     * <p>
     * The Viewers have read-only access to a DataFeed. Each element in this list represents a user with viewer access,
     * but the value of each string element is either user email address or clientId uniquely identifying
     * the user service principal.
     *
     * @return a list containing email or clientId of viewers
     */
    public List<String> getViewers() {
        return this.viewers == null
            ? null : Collections.unmodifiableList(this.viewers);
    }

    /**
     * Set the list of data feed viewer emails and clientIds.
     * <p>
     * The Viewers have read-only access to a DataFeed. Each element in this list represents a user with viewer access,
     * but the value of each string element is either user email address or clientId uniquely identifying
     * the user service principal.
     *
     * @param viewers a list containing email or clientId of viewers.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setViewers(List<String> viewers) {
        this.viewers = viewers;
        return this;
    }

    /**
     * Get the dataFeedDescription property: data feed description.
     *
     * @return the dataFeedDescription value.
     */
    public String getDescription() {
        return this.dataFeedDescription;
    }

    /**
     * Set the dataFeedDescription property: data feed description.
     *
     * @param dataFeedDescription the dataFeedDescription value to set.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setDescription(String dataFeedDescription) {
        this.dataFeedDescription = dataFeedDescription;
        return this;
    }

    /**
     * Get the action link for alert.
     *
     * @return the actionLinkTemplate value.
     */
    public String getActionLinkTemplate() {
        return this.actionLinkTemplate;
    }

    /**
     * Set the action link for alert.
     *
     * @param actionLinkTemplate the actionLinkTemplate value to set.
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setActionLinkTemplate(final String actionLinkTemplate) {
        this.actionLinkTemplate = actionLinkTemplate;
        return this;
    }
}
