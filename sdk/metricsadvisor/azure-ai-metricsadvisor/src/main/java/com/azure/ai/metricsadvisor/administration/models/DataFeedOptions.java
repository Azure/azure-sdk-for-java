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
    private List<String> adminEmails;
    private List<String> viewerEmails;
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
     * Get the list of data feed administrator emails.
     *
     * @return the adminEmails value.
     */
    public List<String> getAdminEmails() {
        return this.adminEmails == null
            ? null : Collections.unmodifiableList(this.adminEmails);
    }

    /**
     * Set the list of data feed administrator emails.
     *
     * @param adminEmails the adminEmails value to set.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
        return this;
    }

    /**
     * Get the list of data feed viewer emails.
     *
     * @return the viewerEmails value.
     */
    public List<String> getViewerEmails() {
        return this.viewerEmails == null
            ? null : Collections.unmodifiableList(this.viewerEmails);
    }

    /**
     * Set the list of data feed viewer emails.
     *
     * @param viewerEmails the viewerEmails value to set.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setViewerEmails(List<String> viewerEmails) {
        this.viewerEmails = viewerEmails;
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
