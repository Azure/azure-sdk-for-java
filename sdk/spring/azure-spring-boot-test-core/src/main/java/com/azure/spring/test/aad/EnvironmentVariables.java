// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.aad;

public class EnvironmentVariables {
    // Test accounts
    public static final String AAD_TENANT_ID_1 = System.getenv("AAD_TENANT_ID_1");
    public static final String AAD_USER_NAME_1 = System.getenv("AAD_USER_NAME_1");
    public static final String AAD_USER_PASSWORD_1 = System.getenv("AAD_USER_PASSWORD_1");
    public static final String AAD_SINGLE_TENANT_CLIENT_ID = System.getenv("AAD_SINGLE_TENANT_CLIENT_ID");
    public static final String AAD_SINGLE_TENANT_CLIENT_SECRET = System.getenv("AAD_SINGLE_TENANT_CLIENT_SECRET");
    public static final String AAD_MULTI_TENANT_CLIENT_ID = System.getenv("AAD_MULTI_TENANT_CLIENT_ID");
    public static final String AAD_MULTI_TENANT_CLIENT_SECRET = System.getenv("AAD_MULTI_TENANT_CLIENT_SECRET");
    public static final String AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE =
        System.getenv("AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE");
    public static final String AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE =
        System.getenv("AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE");

    // scopes
    public static final String SCOPE_GRAPH_READ = "ResourceAccessGraph.Read";
    public static final String MULTI_TENANT_SCOPE_GRAPH_READ =
        toFullNameScope(AAD_MULTI_TENANT_CLIENT_ID, "ResourceAccessGraph.Read");

    public static String toFullNameScope(String clientId, String scope) {
        return "api://" + clientId + "/" + scope;
    }
}
