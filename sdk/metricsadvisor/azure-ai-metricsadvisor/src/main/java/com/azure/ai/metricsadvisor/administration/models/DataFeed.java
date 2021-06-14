// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.DataFeedHelper;
import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The Data feed metadata model.
 */
@Fluent
public final class DataFeed {
    private String id;
    private Map<String, String> metricIds;
    private OffsetDateTime createdTime;
    private DataFeedStatus dataFeedStatus;
    private DataFeedSourceType dataFeedSourceType;
    private boolean isAdmin;
    private String creator;
    private DataFeedSource dataFeedSource;
    private DataFeedGranularity dataFeedGranularity;
    private DataFeedIngestionSettings dataFeedIngestionSettings;
    private DataFeedOptions dataFeedOptions;
    private String dataFeedName;
    private DataFeedSchema dataFeedSchema;

    static {
        DataFeedHelper.setAccessor(new DataFeedHelper.DataFeedAccessor() {
            @Override
            public void setId(DataFeed feed, String id) {
                feed.setId(id);
            }

            @Override
            public void setMetricIds(DataFeed feed, Map<String, String> metricIds) {
                feed.setMetricIds(metricIds);
            }

            @Override
            public void setCreatedTime(DataFeed feed, OffsetDateTime createdTime) {
                feed.setCreatedTime(createdTime);
            }

            @Override
            public void setStatus(DataFeed feed, DataFeedStatus dataFeedStatus) {
                feed.setStatus(dataFeedStatus);
            }

            @Override
            public void setSourceType(DataFeed feed, DataFeedSourceType dataFeedSourceType) {
                feed.setSourceType(dataFeedSourceType);
            }

            @Override
            public void setIsAdmin(DataFeed feed, boolean isAdmin) {
                feed.setIsAdmin(isAdmin);
            }

            @Override
            public void setCreator(DataFeed feed, String creator) {
                feed.setCreator(creator);
            }
        });
    }
    // ReadOnly:start

    /**
     * Get the data feed unique id.
     *
     * @return the dataFeedId value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the list of associated metrics Ids.
     *
     * @return the metricIds value.
     */
    public Map<String, String> getMetricIds() {
        return this.metricIds;
    }

    /**
     * Get the data feed created time.
     *
     * @return the createdTime value.
     */
    public OffsetDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * Get the data feed status.
     *
     * @return the dataFeedStatus value.
     */
    public DataFeedStatus getStatus() {
        return this.dataFeedStatus;
    }

    /**
     * Get if the data feed the query user is one of data feed administrator or not.
     *
     * @return the isAdmin value.
     */
    public boolean isAdmin() {
        return this.isAdmin;
    }

    // ReadOnly:end

    /**
     * Get the data feed name.
     *
     * @return the dataFeedName value.
     */
    public String getName() {
        return this.dataFeedName;
    }

    /**
     * Get the data feed source.
     *
     * @return the dataFeedSource value.
     */
    public DataFeedSource getSource() {
        return this.dataFeedSource;
    }

    /**
     * Get the data feed schema properties.
     *
     * @return the dataFeedSchema value.
     */
    public DataFeedSchema getSchema() {
        return this.dataFeedSchema;
    }

    /**
     * Get the granularity properties of the time series.
     *
     * @return the dataFeedGranularity value.
     */
    public DataFeedGranularity getGranularity() {
        return this.dataFeedGranularity;
    }

    /**
     * Get the data feed creator.
     *
     * @return the creator value.
     */
    public String getCreator() {
        return this.creator;
    }

    /**
     * Get the data feed ingestion properties of the time series.
     *
     * @return the dataFeedIngestionSettings value.
     */
    public DataFeedIngestionSettings getIngestionSettings() {
        return this.dataFeedIngestionSettings;
    }

    /**
     * Get the data feed metadata properties.
     *
     * @return the dataFeedOptions value.
     */
    public DataFeedOptions getOptions() {
        return this.dataFeedOptions;
    }

    /**
     * Get the data feed source type.
     *
     * @return the dataFeedSourceType value.
     */
    public DataFeedSourceType getSourceType() {
        return this.dataFeedSourceType;
    }

    /**
     * Set the data feed name.
     *
     * @param dataFeedName the dataFeedName value to set.
     *
     * @return the DataFeedDetail object itself.
     */
    public DataFeed setName(String dataFeedName) {
        this.dataFeedName = dataFeedName;
        return this;
    }

    /**
     * Set the data feed source.
     *
     * @param dataFeedSource the the data feed source value to set.
     *
     * @return the DataFeedDetail object itself.
     */
    public DataFeed setSource(DataFeedSource dataFeedSource) {
        this.dataFeedSource = dataFeedSource;
        return this;
    }

    /**
     * Set the data feed schema properties.
     *
     * @param dataFeedSchema the data feed schema properties value to set.
     *
     * @return the DataFeed object itself.
     */
    public DataFeed setSchema(DataFeedSchema dataFeedSchema) {
        this.dataFeedSchema = dataFeedSchema;
        return this;
    }

    /**
     * Set the data feed granularity settings for the time series.
     *
     * @param granularity the the data feed granularity settings for the time series value to set.
     *
     * @return the DataFeed object itself.
     */
    public DataFeed setGranularity(DataFeedGranularity granularity) {
        this.dataFeedGranularity = granularity;
        return this;
    }

    /**
     * Set the data feed ingestion settings.
     *
     * @param dataFeedIngestionSettings the data feed ingestion settings value to set.
     *
     * @return the DataFeed object itself.
     */
    public DataFeed setIngestionSettings(DataFeedIngestionSettings dataFeedIngestionSettings) {
        this.dataFeedIngestionSettings = dataFeedIngestionSettings;
        return this;
    }

    /**
     * Set the data feed metadata properties.
     *
     * @param dataFeedOptions the the data feed metadata properties value to set.
     *
     * @return the DataFeed object itself.
     */
    public DataFeed setOptions(DataFeedOptions dataFeedOptions) {
        this.dataFeedOptions = dataFeedOptions;
        return this;
    }

    void setId(String id) {
        this.id = id;
    }

    void setMetricIds(Map<String, String> metricIds) {
        this.metricIds = metricIds;
    }

    void setCreatedTime(OffsetDateTime createdTime) {
        this.createdTime = createdTime;
    }

    void setStatus(DataFeedStatus dataFeedStatus) {
        this.dataFeedStatus = dataFeedStatus;
    }

    void setSourceType(DataFeedSourceType dataFeedSourceType) {
        this.dataFeedSourceType = dataFeedSourceType;
    }

    void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    void setCreator(String creator) {
        this.creator = creator;
    }
}
