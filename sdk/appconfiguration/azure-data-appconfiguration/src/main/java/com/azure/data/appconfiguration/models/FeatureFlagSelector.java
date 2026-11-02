// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.MatchConditions;
import com.azure.core.util.CoreUtils;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Selects dedicated feature flag resources or their revisions. Names identify resources under {@code /ff},
 * not keys prefixed with {@code .appconfig.featureflag/}.
 */
@Fluent
public final class FeatureFlagSelector {
    private String nameFilter;
    private String labelFilter;
    private List<String> tagsFilter;
    private FeatureFlagFields[] fields;
    private OffsetDateTime acceptDateTime;
    private List<MatchConditions> matchConditions;

    /**
     * Creates a selector that requests all feature flags and fields.
     */
    public FeatureFlagSelector() {
    }

    /**
     * Gets the name filter.
     * @return the name filter.
     */
    public String getNameFilter() {
        return nameFilter;
    }

    /**
     * Sets the feature flag name filter.
     * @param nameFilter the name filter.
     * @return this selector.
     */
    public FeatureFlagSelector setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
        return this;
    }

    /**
     * Gets the label filter.
     * @return the label filter.
     */
    public String getLabelFilter() {
        return labelFilter;
    }

    /**
     * Sets the label filter. Use {@code "\0"} to select resources without a label.
     * @param labelFilter the label filter.
     * @return this selector.
     */
    public FeatureFlagSelector setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
        return this;
    }

    /**
     * Gets the tag filters.
     * @return the tag filters, or null.
     */
    public List<String> getTagsFilter() {
        return tagsFilter;
    }

    /**
     * Sets tag filters in {@code tag=value} form.
     * @param tagsFilter the tag filters.
     * @return this selector.
     */
    public FeatureFlagSelector setTagsFilter(List<String> tagsFilter) {
        this.tagsFilter = tagsFilter == null ? null : Collections.unmodifiableList(new ArrayList<>(tagsFilter));
        return this;
    }

    /**
     * Gets the requested fields.
     * @return the requested fields, or an empty array for all fields.
     */
    public FeatureFlagFields[] getFields() {
        return fields == null ? new FeatureFlagFields[0] : CoreUtils.clone(fields);
    }

    /**
     * Sets the fields to include in responses.
     * @param fields the fields, or null for all fields.
     * @return this selector.
     */
    public FeatureFlagSelector setFields(FeatureFlagFields... fields) {
        this.fields = CoreUtils.clone(fields);
        return this;
    }

    /**
     * Gets the point-in-time selection.
     * @return the point in time, or null for current resources.
     */
    public OffsetDateTime getAcceptDateTime() {
        return acceptDateTime;
    }

    /**
     * Requests resources as they existed at a given time.
     * @param acceptDateTime the point in time, or null for current resources.
     * @return this selector.
     */
    public FeatureFlagSelector setAcceptDatetime(OffsetDateTime acceptDateTime) {
        this.acceptDateTime = acceptDateTime;
        return this;
    }

    /**
     * Gets per-page match conditions.
     * @return conditions in page order, or null.
     */
    public List<MatchConditions> getMatchConditions() {
        return matchConditions;
    }

    /**
     * Sets conditions in page order. Use {@link MatchConditions#setIfNoneMatch(String)} with previously received
     * page ETags to detect changes. Unchanged pages have status 304 and no items.
     * @param matchConditions the conditions, or null for unconditional retrieval.
     * @return this selector.
     */
    public FeatureFlagSelector setMatchConditions(List<MatchConditions> matchConditions) {
        this.matchConditions
            = matchConditions == null ? null : Collections.unmodifiableList(new ArrayList<>(matchConditions));
        return this;
    }
}
