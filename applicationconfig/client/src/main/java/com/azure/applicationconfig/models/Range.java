// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

import com.azure.applicationconfig.ConfigurationAsyncClient;
import com.azure.applicationconfig.ConfigurationClient;

/**
 * A configuration for selecting a range of revisions when retrieving configuration setting revisions from the
 * Application Configuration service.
 *
 * @see ConfigurationAsyncClient#listSettingRevisions(SettingSelector)
 * @see ConfigurationClient#listSettingRevisions(SettingSelector)
 * @see SettingSelector#range(Range)
 */
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
    public int start() {
        return start;
    }

    /**
     * @return the end of the range.
     */
    public int end() {
        return end;
    }
}
