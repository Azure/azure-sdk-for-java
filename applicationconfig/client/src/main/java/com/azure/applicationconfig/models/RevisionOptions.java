// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

public class RevisionOptions extends RequestOptions {
    private RevisionRange range;

    /**
     * Gets the range of {@link ConfigurationSetting} revisions to fetch from the service. If none is specified, it will
     * return all of the revisions of that setting.
     *
     * @return The range of revisions for a ConfigurationSetting to fetch.
     */
    public RevisionRange range() { return this.range; }

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
}
