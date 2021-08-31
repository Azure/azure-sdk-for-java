// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

import java.util.Optional;

/**
 * Util class for ApplicationId perfix
 */
public class ApplicationId {
    //    There is 24 char limitation about the app id. So some abbreviation needs to be applied:
    //    az: for Azure
    //    sp: for Spring
    //    sc: for Spring Cloud
    //    sd: for Spring Data
    //    ss: for Spring Streams
    //    si: for Spring Integration
    //    kv: for Key Vault
    //    se: for Security
    //    jca: for JCA
    //    sb: for Storage Blobs
    //    sf: for Storage Files
    //    sq: for Storage Queue
    //    eh: for Event Hub
    //    bus: for Service Bus
    //    cfg: for App Config
    //    cos: for Cosmos
    //    aad: for AAD
    //    b2c: for AAD B2C

    /**
     * AZURE_SPRING_AAD does not contain VERSION, because AAD server support 2 headers:
     * 1. x-client-SKU;
     * 2. x-client-VER;
     */
    public static final String AZURE_SPRING_AAD = "az-sp-aad";
    public static final String AZURE_SPRING_B2C = "az-sp-b2c";
    public static final String AZURE_SPRING_EVENT_HUB = "az-sc-eh/;";
    public static final String AZURE_SPRING_KEY_VAULT = "az-sp-kv/";
    public static final String AZURE_SPRING_SERVICE_BUS = "az-sp-bus/";
    public static final String AZURE_SPRING_STORAGE_BLOB = "az-sp-sb/";
    public static final String AZURE_SPRING_STORAGE_FILES = "az-sp-sf/";
    public static final String AZURE_SPRING_STORAGE_QUEUE = "az-si-sq/";

    //TODO: version should contains each starter library's version
    public static final String VERSION = Optional.of(ApplicationId.class)
        .map(Class::getPackage)
        .map(Package::getImplementationVersion)
        .orElse("unknown");
}
