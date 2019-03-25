// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License
package com.azure.applicationconfig.models;

/**
 * A range of configuration setting revisions.
 */
public class RevisionRange {
    private final Integer start;
    private final Integer end;

    /**
     * Creates a range of revisions starting from {@param start} onwards.
     *
     * @param start The revision number to start fetching data from.
     * @throws IllegalArgumentException If {@code start} is less than 0.
     */
    public RevisionRange(int start) {
        if (start < 0) {
            throw new IllegalArgumentException("'start' cannot be less than 0.");
        }

        this.start = start;
        this.end = null;
    }

    /**
     * Creates a range of revisions to fetch {@link ConfigurationSetting}s starting from {@param start} until
     * {@param end}.
     *
     * @param start The revision number to start fetching {@link ConfigurationSetting} changes.
     * @param end   The revision number to stop fetching {@link ConfigurationSetting} changes.
     * @throws IllegalArgumentException If {@code start} is less than 0 or {@code end} is less than 0.
     */
    public RevisionRange(int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("'start' cannot be less than 0.");
        } else if (end < 0) {
            throw new IllegalArgumentException("'end' cannot be less than 0.");
        }

        this.start = start;
        this.end = end;
    }

    public Integer start() { return start; }

    public Integer end() { return end; }
}
