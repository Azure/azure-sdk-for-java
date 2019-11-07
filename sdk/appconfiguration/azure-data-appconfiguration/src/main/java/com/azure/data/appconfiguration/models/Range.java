// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Immutable;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingPage;

/**
 * A configuration for selecting a range of revisions that will be returned in a single
 * {@link ConfigurationSettingPage response page} when retrieving revisions from the App Configuration service.
 *
 * @see ConfigurationAsyncClient#listRevisions(SettingSelector)
 * @see ConfigurationClient#listRevisions(SettingSelector)
 * @see SettingSelector#setRange(Range)
 */
@Immutable
public class Range {
    private final int start;
    private final int end;

    /**
     * Creates a range used to select the specified revision range.
     * @param start Start of the range.
     * @param end End of the range.
     */
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @return the start of the range.
     */
    public int getStart() {
        return start;
    }

    /**
     * @return the end of the range.
     */
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("%d-%d", this.start, this.end);
    }
}
