// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Used internally. Constants in the Azure Spring Boot Core library.
 */
public class Constants {

    public static final Set<String> AZURE_SPRING_PROPERTIES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "authority-host",
            "client-id",
            "client-secret",
            "certificate-path",
            "msi-enabled",
            "tenant-id",
            "environment"
        )));

    public static final String AZURE_CHINA_AUTHORITY_HOST = "https://login.chinacloudapi.cn/";
    public static final String AZURE_GERMANY_AUTHORITY_HOST = "https://login.microsoftonline.de/";
    public static final String AZURE_GLOBAL_AUTHORITY_HOST = "https://login.microsoftonline.com/";
    public static final String AZURE_US_GOVERNMENT_AUTHORITY_HOST = "https://login.microsoftonline.us/";

    public static final String PREFIX = "spring.cloud.azure";
}
