// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

/**
 * Refs: https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens
 */
public class AADTokenClaim {

    public static final String NAME = "name";
    public static final String TID = "tid";
    public static final String ROLES = "roles";
    public static final String ISS = "iss";
    public static final String AUD = "aud";
    public static final String GROUPS = "groups";
    public static final String AZP = "azp";
    public static final String APP_ID = "appid";
    public static final String TFP = "tfp";
}
