// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public class KafkaCosmosConstants {

    public static class StatusCodes {
        public static final int NOTFOUND = 404;
        public static final int REQUEST_TIMEOUT = 408;
        public static final int GONE = 410;

        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    public static class SubStatusCodes {
        public static final int READ_SESSION_NOT_AVAILABLE = 1002;
        public static final int PARTITION_KEY_RANGE_GONE = 1002;
        public static final int COMPLETING_SPLIT_OR_MERGE = 1007;
    }
}
