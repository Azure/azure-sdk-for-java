// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.monitor.query.LogsQueryClient;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Cloud audiences available for {@link LogsQueryClient}.
 */
public final class LogsQueryClientAudience extends ExpandableStringEnum<LogsQueryClientAudience> {

    /** Static audience for Azure public cloud. */
    public static final LogsQueryClientAudience AZURE_PUBLIC_CLOUD = fromString("https://api.monitor.azure.com");

    /** Static default audience that uses Azure public cloud. */
    public static final LogsQueryClientAudience DEFAULT = fromString("https://api.monitor.azure.com");

    /**
     * Creates or finds a LogsQueryClientAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding LogsQueryClientAudience.
     */
    @JsonCreator
    public static LogsQueryClientAudience fromString(String name) {
        return fromString(name, LogsQueryClientAudience.class);
    }

    /** @return known LogsQueryClientAudience values. */
    public static Collection<LogsQueryClientAudience> values() {
        return values(LogsQueryClientAudience.class);
    }
}
