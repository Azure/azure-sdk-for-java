// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig.models;

import java.time.OffsetDateTime;
import java.util.EnumSet;

public class RevisionOptions extends RequestOptions {
    private RevisionRange range;

    /**
     * Gets the range of {@link ConfigurationSetting} revisions to fetch from the service. If none is specified, it will
     * return all of the revisions of that setting.
     *
     * @return The range of revisions for a ConfigurationSetting to fetch.
     */
    public RevisionRange range() {
        return this.range;
    }

    /**
     * Sets the range of {@link ConfigurationSetting} revisions to fetch from the service. If none is specified, all of
     * the revisions will be returned.
     *
     * @param range The range of revisions for a ConfigurationSetting to fetch.
     * @return The updated RevisionOptions object.
     */
    public RevisionOptions range(RevisionRange range) {
        this.range = range;
        return this;
    }

    @Override
    public RevisionOptions key(String key) {
        super.key(key);
        return this;
    }

    /**
     * If set, then revisions of the {@link ConfigurationSetting} are returned up until that time.
     *
     * <p>
     * For example, if an acceptDatetime of 'March 20, 2019 19:00:00 UTC' is set, then all the revisions for matching
     * ConfigurationSettings are returned up until that date time.
     * </p>
     *
     * @param datetime The value of the configuration setting at that given {@link OffsetDateTime}.
     * @return The updated RevisionOptions object.
     */
    @Override
    public RevisionOptions acceptDatetime(OffsetDateTime datetime) {
        super.acceptDatetime(datetime);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionOptions fields(EnumSet<ConfigurationSettingField> fields) {
        super.fields(fields);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionOptions label(String label) {
        super.label(label);
        return this;
    }
}
