// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import java.time.Duration;

public class ManagementClientConstants {
    public static Duration MAX_DURATION = Duration.parse("P10675199DT2H48M5.4775807S");

    static final int QUEUE_NAME_MAX_LENGTH = 260;
    static final int TOPIC_NAME_MAX_LENGTH = 260;
    static final int SUBSCRIPTION_NAME_MAX_LENGTH = 50;
    static final int RULE_NAME_MAX_LENGTH = 50;

    static final String ATOM_NS = "http://www.w3.org/2005/Atom";
    static final String SB_NS = "http://schemas.microsoft.com/netservices/2010/10/servicebus/connect";
    static final String XML_SCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";

    static final String SERVICEBUS_SUPPLEMENTARTY_AUTHORIZATION_HEADER_NAME = "ServiceBusSupplementaryAuthorization";
    static final String SERVICEBUS_DLQ_SUPPLEMENTARTY_AUTHORIZATION_HEADER_NAME = "ServiceBusDlqSupplementaryAuthorization";
    static final String HTTP_ERROR_SUB_CODE_FORMAT_STRING = "SubCode=%s";
    static final String CONFLICT_OPERATION_IN_PROGRESS_SUB_CODE =
        String.format(HTTP_ERROR_SUB_CODE_FORMAT_STRING, ExceptionErrorCodes.CONFLICT_OPERATION_IN_PROGRESS);
    static final String FORBIDDEN_INVALID_OPERATION_SUB_CODE =
        String.format(HTTP_ERROR_SUB_CODE_FORMAT_STRING, ExceptionErrorCodes.FORBIDDEN_INVALID_OPERATION);

    // Defaults
    static final Duration DEFAULT_HISTORY_DEDUP_WINDOW = Duration.ofMinutes(1);
    static final Duration DEFAULT_LOCK_DURATION = Duration.ofSeconds(60);
    static final int DEFAULT_MAX_DELIVERY_COUNT = 10;
    static final long DEFAULT_MAX_SIZE_IN_MB = 1024;

    // Limits to values
    static final Duration MIN_ALLOWED_TTL = Duration.ofSeconds(1);
    static final Duration MAX_ALLOWED_TTL = MAX_DURATION;
    static final Duration MIN_ALLOWED_AUTODELETE_DURATION = Duration.ofMinutes(5);
    static final Duration MAX_DUPLICATE_HISTORY_DURATION = Duration.ofDays(1);
    static final Duration MIN_DUPLICATE_HISTORY_DURATION = Duration.ofSeconds(20);
    static final int MIN_ALLOWED_MAX_DELIVERYCOUNT = 1;
    static final int MAX_USERMETADATA_LENGTH = 1024;

    static final char[] INVALID_ENTITY_PATH_CHARACTERS = { '@', '?', '#', '*' };

    // Authorization constants
    static final int SUPPORTED_CLAIMS_COUNT = 3;

    static class ExceptionErrorCodes {
        public static final String CONFLICT_OPERATION_IN_PROGRESS = "40901";
        public static final String FORBIDDEN_INVALID_OPERATION = "40301";
    }
}
