package com.microsoft.azure.servicebus.management;

import java.time.Duration;

public class ManagementClientConstants {
    public static Duration MAX_DURATION = Duration.parse("P10675199DT2H48M5.4775807S");

    static int QUEUE_NAME_MAX_LENGTH = 260;
    static int TOPIC_NAME_MAX_LENGTH = 260;
    static int SUBSCRIPTION_NAME_MAX_LENGTH = 50;
    static int RULE_NAME_MAX_LENGTH = 50;

    static String ATOM_NS = "http://www.w3.org/2005/Atom";
    static String SB_NS = "http://schemas.microsoft.com/netservices/2010/10/servicebus/connect";
    static String XML_SCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";
    static String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    static String ATOM_CONTENT_TYPE = "application/atom+xml";
    static String API_VERSION = "2017-04";
    static String API_VERSION_QUERY = "api-version=" + API_VERSION;

    static String ServiceBusSupplementartyAuthorizationHeaderName = "ServiceBusSupplementaryAuthorization";
    static String ServiceBusDlqSupplementaryAuthorizationHeaderName = "ServiceBusDlqSupplementaryAuthorization";
    static String HttpErrorSubCodeFormatString = "SubCode=%s";
    static String ConflictOperationInProgressSubCode =
        String.format(HttpErrorSubCodeFormatString, ExceptionErrorCodes.ConflictOperationInProgress);
    static String ForbiddenInvalidOperationSubCode =
        String.format(HttpErrorSubCodeFormatString, ExceptionErrorCodes.ForbiddenInvalidOperation);

    // Defaults
    static Duration DEFAULT_HISTORY_DEDUP_WINDOW = Duration.ofMinutes(1);
    static Duration DEFAULT_LOCK_DURATION = Duration.ofSeconds(60);
    static int DEFAULT_MAX_DELIVERY_COUNT = 10;
    static long DEFAULT_MAX_SIZE_IN_MB = 1024;

    // Limits to values
    static Duration MIN_ALLOWED_TTL = Duration.ofSeconds(1);
    static Duration MAX_ALLOWED_TTL = MAX_DURATION;
    static Duration MIN_LOCK_DURATION = Duration.ofSeconds(5);
    static Duration MAX_LOCK_DURATION = Duration.ofMinutes(5);
    static Duration MIN_ALLOWED_AUTODELETE_DURATION = Duration.ofMinutes(5);
    static Duration MAX_DUPLICATE_HISTORY_DURATION = Duration.ofDays(1);
    static Duration MIN_DUPLICATE_HISTORY_DURATION = Duration.ofSeconds(20);
    static int MIN_ALLOWED_MAX_DELIVERYCOUNT = 1;
    static int MAX_USERMETADATA_LENGTH = 1024;

    static char[] InvalidEntityPathCharacters = { '@', '?', '#', '*' };

    // Authorization constants
    static int SupportedClaimsCount = 3;

    static class ExceptionErrorCodes {
        public static String ConflictOperationInProgress = "40901";
        public static String ForbiddenInvalidOperation = "40301";
    }
}
