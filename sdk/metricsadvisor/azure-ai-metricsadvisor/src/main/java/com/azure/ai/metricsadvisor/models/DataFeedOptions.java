// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Fluent;

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
     * Get the admins property: data feed administrator.
     *
     * @return the admins value.
     */
    public List<String> getAdmins() {
        return this.admins;
    }

    /**
     * Set the admins property: data feed administrator.
     *
     * @param admins the admins value to set.
     *
     * @return the DataFeedOptions object itself.
     */
    public DataFeedOptions setAdmins(List<String> admins) {
        this.admins = admins;
        return this;
    }

    /**
     * Get the viewers property: data feed viewer.
     *
     * @return the viewers value.
     */
    public List<String> getViewers() {
        return this.viewers;
    }

    /**
     * Set the viewers property: data feed viewer.
     *
     * @param viewers the viewers value to set.
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
