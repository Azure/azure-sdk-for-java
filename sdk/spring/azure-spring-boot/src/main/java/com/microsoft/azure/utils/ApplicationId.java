// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.utils;

import java.util.Optional;

public class ApplicationId {
    //    There is 24 char limitation about the app id. So some abbreviation needs to be applied:
    //    az: for Azure
    //    sp: for Spring
    //    sc: for Spring Cloud
    //    sd: for Spring Data
    //    ss: for Spring Streams
    //    kv: for Key Vault
    //    sb: for Storage Blobs
    //    sf: for Storage Files
    //    eh: for Event Hub
    //    bus: for Service Bus
    //    cfg: for App Config
    //    cos: for Cosmos
    //    aad: for AAD
    //    b2c: for AAD B2C
    public static final String VERSION = Optional.of(ApplicationId.class)
                                                 .map(Class::getPackage)
                                                 .map(Package::getImplementationVersion)
                                                 .orElse("unknown-version");
    public static final String AZURE_SPRING_KEY_VAULT = "az-sp-kv/" + VERSION;
    public static final String AZURE_SPRING_SERVICE_BUS = "az-sp-bus/" + VERSION;

}
