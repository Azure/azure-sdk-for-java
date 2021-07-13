// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.context.core.util;

/**
 * The User Agent constants.
 */
public class Constants {

    // for the User-Agent header set in track2 SDKs
    private static final String SNAPSHOT_VERSION = "snapshot";
    private static final String AZURE = "az";
    private static final String SPRING_CLOUD = "sc";
    private static final String SPRING_STREAMS = "ss";
    private static final String SPRING_INTEGRATION = "si";
    private static final String KEY_VAULT = "kv";
    private static final String STORAGE_BLOBS = "sb";
    private static final String STORAGE_FILE_SHARE = "sf";
    private static final String STORAGE_QUEUE = "sq";
    private static final String EVENT_HUB = "eh";
    private static final String SERVICE_BUS = "bus";
    private static final String APP_CONFIGURATION = "cfg";


    public static final String SPRING_CLOUD_VERSION = SNAPSHOT_VERSION;

    // the max length of application id is 24
    public static final String SPRING_INTEGRATION_STORAGE_QUEUE_APPLICATION_ID =
        String.join("-", AZURE, SPRING_INTEGRATION, STORAGE_QUEUE) + "/" + SPRING_CLOUD_VERSION;

    public static final String SPRING_EVENT_HUB_APPLICATION_ID =
        String.join("-", AZURE, SPRING_CLOUD, EVENT_HUB) + "/" + SPRING_CLOUD_VERSION;
    public static final String SPRING_SERVICE_BUS_APPLICATION_ID =
        String.join("-", AZURE, SPRING_CLOUD, SERVICE_BUS) + "/" + SPRING_CLOUD_VERSION;
}
