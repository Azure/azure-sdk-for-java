// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import java.time.ZonedDateTime;

public class LeaseConstants {
    static final ZonedDateTime UNIX_START_TIME = ZonedDateTime.parse("1970-01-01T00:00:00.0Z[UTC]");
    public static final String PROPERTY_NAME_LEASE_TOKEN = "LeaseToken";
    public static final String PROPERTY_NAME_CONTINUATION_TOKEN = "ContinuationToken";
    public static final String PROPERTY_NAME_TIMESTAMP = "timestamp";
    public static final String PROPERTY_NAME_OWNER = "Owner";
    public static final String PROPERTY_FEED_RANGE = "feedRange";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_TS ="_ts";
}
