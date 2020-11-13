// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import java.util.Arrays;
import java.util.List;

public class TestConstants {
    public static final String SERVICE_ENVIRONMENT_PROPERTY = "azure.activedirectory.environment";
    public static final String CLIENT_ID_PROPERTY = "azure.activedirectory.client-id";
    public static final String CLIENT_SECRET_PROPERTY = "azure.activedirectory.client-secret";
    public static final String TARGETED_GROUPS_PROPERTY = "azure.activedirectory.user-group.allowed-groups";
    public static final String ALLOW_TELEMETRY_PROPERTY = "azure.activedirectory.allow-telemetry";

    public static final String CLIENT_ID = "real_client_id";
    public static final String CLIENT_SECRET = "real_client_secret";
    public static final List<String> TARGETED_GROUPS = Arrays.asList("group1", "group2", "group3");

    public static final String ACCESS_TOKEN = "real_jwt_access_token";
    public static final String BEARER_TOKEN = "Bearer " + ACCESS_TOKEN;
}
