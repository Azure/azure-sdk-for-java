// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.util;

public class EnvironmentVariables {
    // Test accounts
    public static final String AAD_TENANT_ID_1 = System.getenv("AAD_TENANT_ID_1");
    public static final String AAD_MULTI_TENANT_CLIENT_ID = System.getenv("AAD_MULTI_TENANT_CLIENT_ID");
    public static final String AAD_MULTI_TENANT_CLIENT_SECRET = System.getenv("AAD_MULTI_TENANT_CLIENT_SECRET");
    public static final String AAD_USER_NAME_1 = System.getenv("AAD_USER_NAME_1");
    public static final String AAD_USER_PASSWORD_1 = System.getenv("AAD_USER_PASSWORD_1");

    // scopes
    public static final String SCOPE_GRAPH_READ = toFullNameScope("ResourceAccessGraph.Read");
    public static final String SCOPE_1 = toFullNameScope("TestScope1");
    public static final String SCOPE_2 = toFullNameScope("TestScope2");

    private static String toFullNameScope(String scope) {
        return "api://" + AAD_MULTI_TENANT_CLIENT_ID + "/" + scope;
    }
}
